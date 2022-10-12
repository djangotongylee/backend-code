package com.xinbo.sports.task.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.xinbo.sports.dao.generator.po.*;
import com.xinbo.sports.dao.generator.service.*;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams.PlatGameQueryDateDto;
import com.xinbo.sports.service.cache.KeyConstant;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.service.common.Constant;
import com.xinbo.sports.service.io.dto.promotions.ApplicationActivityReqDto;
import com.xinbo.sports.task.base.GameCommonBase;
import com.xinbo.sports.task.base.PromotionsTaskBase;
import com.xinbo.sports.task.service.IPromotionsTaskService;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.DateUtils;
import com.xinbo.sports.utils.JedisUtil;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.ObjIntConsumer;
import java.util.stream.Collectors;

import static com.alibaba.fastjson.JSON.parseObject;

/**
 * @author: wells
 * @date: 2020/5/3
 * @description:优惠活动定时任务
 */
@Slf4j
@Service("iPromotionsTaskServiceImpl")
public class IPromotionsTaskServiceImpl implements IPromotionsTaskService {
    //真人游戏
    private static final int GAME_LIVE = 3;
    //利润
    private static final String PROFIT = "profit";
    //流水
    private static final String FLOW_CLAIM = "flowClaim";
    //扩展信息中的详情
    private static final String DETAILS = "details";
    @Autowired
    private CoinRewardsService coinRewardsServiceImpl;
    @Autowired
    private PromotionsService promotionsServiceImpl;
    @Autowired
    private UserService userServiceImpl;
    @Autowired
    private UserProfileService userProfileServiceImpl;
    @Autowired
    private CoinDepositService coinDepositServiceImpl;
    @Autowired
    private UserLevelRebateService userLevelRebateServiceImpl;
    @Autowired
    private UserLevelService userLevelServiceImpl;
    @Autowired
    private PromotionsTaskBase promotionsTaskBase;
    @Autowired
    private PromotionsExtendServiceImpl promotionsExtendServiceImpl;
    @Autowired
    private GameCommonBase gameCommonBase;
    @Autowired
    private GameListService gameListServiceImpl;
    @Autowired
    private CoinRebateService coinRebateServiceImpl;
    @Autowired
    private JedisUtil jedisUtil;
    @Autowired
    private CoinRewardsInviteService coinRewardsInviteServiceImpl;
    @Autowired
    private ConfigCache configCache;
    @Autowired
    private CoinCommissionService coinCommissionServiceImpl;
    @Autowired
    private PersistenceServiceImpl persistenceServiceImpl;


    /**
     * 前n天时间
     *
     * @param dayCounts 天数
     * @return 开始时间，结束时间的二元组
     */
    private Pair<Integer, Integer> getBeforeTime(int dayCounts) {
        int now = DateNewUtils.now();
        ZonedDateTime zoneNow = DateNewUtils.utc8Zoned(now);
        int startTime = (int) zoneNow.minusDays(dayCounts).withHour(0).withMinute(0).withSecond(0).withNano(0).toEpochSecond();
        int endTime = (int) zoneNow.withHour(0).withMinute(0).withSecond(0).withNano(0).toEpochSecond();
        return Pair.of(startTime, endTime);
    }

    /***
     * 真人投注送彩金
     * 1.判断当前时间是否在活动时间内
     * 2.获取前一一天的时间
     * 3.判断前是否派奖，防止重复派奖
     * 4.检索真人投注记录,累计有效投注金额
     * 5.派送奖金,处理活动奖金
     */
    @Override
    public void liveDailyBonus() {
        var pairTime = getBeforeTime(1);
        XxlJobLogger.log("真人投注送彩金任务调度;" + DateUtils.yyyyMMddHHmmss(pairTime.getLeft()) + "结束时间" + DateUtils.yyyyMMddHHmmss(pairTime.getRight()));
        PlatGameQueryDateDto totalPlatDto = new PlatGameQueryDateDto();
        totalPlatDto.setStartTime(pairTime.getLeft());
        totalPlatDto.setEndTime(pairTime.getRight());
        totalPlatDto.setPlatId(GAME_LIVE);
        var userBetCoinMap = gameCommonBase.getUserBetCoin(totalPlatDto);
        if (CollectionUtils.isEmpty(userBetCoinMap)) {
            XxlJobLogger.log("真人投注送彩金,无有效投注");
            return;
        }
        //获取奖励的扩展信息info的详情
        var infoMap = acquireInfo(userBetCoinMap.keySet());
        userBetCoinMap.forEach((uid, betCoin) -> {
            if (isPromotionsEffective(Constant.LIVE_DAILY_BONUS, uid)) {
                XxlJobLogger.log("不能领取真人投注送彩金！");
                return;
            }
            var infoObject = new JSONObject();
            infoObject.put("betCoin", betCoin);
            infoObject.put(DETAILS, infoMap.get(uid));
            //查询活动规则-真人投注送彩金
            Map<String, BigDecimal> profitCoinMap = promotionsExtendServiceImpl.getProfitCoin(betCoin, Constant.LIVE_DAILY_BONUS);
            if (!CollectionUtils.isEmpty(profitCoinMap)) {
                BigDecimal profit = profitCoinMap.get(PROFIT);
                BigDecimal flowClaim = profitCoinMap.get(FLOW_CLAIM);
                var reqDto = ApplicationActivityReqDto.builder()
                        .id(Constant.LIVE_DAILY_BONUS)
                        .availableCoin(profit)
                        .mosaicCoin(betCoin.add(flowClaim))
                        .info(infoObject.toJSONString())
                        .build();
                promotionsTaskBase.handlePromotionsPersistence(reqDto, uid);
            } else {
                XxlJobLogger.log("真人投注送彩金,无派彩金额!");
            }
        });
    }

    /**
     * 获取奖励的扩展信息info的详情
     *
     * @param uidSet 用户信息集合
     * @return 获取奖励info的Map集合
     */
    public Map<Integer, JSONArray> acquireInfo(Set<Integer> uidSet) {
        var pairTime = getBeforeTime(1);
        //获取真人的游戏，投机每个真人游戏的有效投注额
        var gameLists = gameListServiceImpl.lambdaQuery()
                .eq(GameList::getStatus, 1)
                .eq(GameList::getGroupId, GAME_LIVE)
                .list();
        Map<Integer, JSONArray> map = uidSet.stream().collect(Collectors.toMap(x -> x, value -> new JSONArray()));
        gameLists.forEach(gameList -> {
            //查询真人有效投注
            PlatGameQueryDateDto platDto = new PlatGameQueryDateDto();
            platDto.setStartTime(pairTime.getLeft());
            platDto.setEndTime(pairTime.getRight());
            platDto.setGameId(gameList.getId());
            var coinMap = gameCommonBase.getUserBetCoin(platDto);
            map.forEach((key, jsonArray) -> {
                var jsonObject = new JSONObject();
                jsonObject.put("gameName", gameList.getName());
                jsonObject.put("betCoin", coinMap.getOrDefault(key, BigDecimal.ZERO));
                jsonArray.add(jsonObject);
            });
        });
        return map;
    }

    /**
     * 邀请好友
     * 1.判断当前时间是否在活动时间内
     * 2.判断前是否派奖，防止重复派奖
     * 3.当天用户是否有邀请的好友
     * 4.邀请人可得首充当天累计存款5%返利
     */
    @Override
    public void inviteFriends() {
        //当天注册的新用户
        var pairTime = getBeforeTime(1);
        XxlJobLogger.log("邀请好友任务调度;开始时间" + DateUtils.yyyyMMddHHmmss(pairTime.getLeft()) + "结束时间" + DateUtils.yyyyMMddHHmmss(pairTime.getRight()));
        List<UserProfile> userProfileList = userProfileServiceImpl.list(new LambdaQueryWrapper<UserProfile>()
                .ge(UserProfile::getCreatedAt, pairTime.getLeft())
                .le(UserProfile::getCreatedAt, pairTime.getRight()));
        if (CollectionUtils.isEmpty(userProfileList)) {
            XxlJobLogger.log("无新增用户！");
            return;
        }
        //邀请人与被邀请人的userSupMap
        Map<Integer, Integer> userSupMap = userProfileList.stream().collect(Collectors.toMap(UserProfile::getUid, UserProfile::getSupUid1));
        XxlJobLogger.log("被邀请人与邀请人" + userSupMap);
        List<Integer> userIds = userProfileList.stream().map(UserProfile::getUid).collect(Collectors.toList());
        List<CoinDeposit> coinDepositList = coinDepositServiceImpl.list(new LambdaQueryWrapper<CoinDeposit>()
                .ge(CoinDeposit::getCreatedAt, pairTime.getLeft())
                .le(CoinDeposit::getCreatedAt, pairTime.getRight())
                .in(CoinDeposit::getUid, userIds)
                .in(CoinDeposit::getStatus, List.of(1, 2, 9)));
        if (CollectionUtils.isEmpty(coinDepositList)) {
            XxlJobLogger.log("无用户首充记录！");
            return;
        }
        Map<Integer, List<CoinDeposit>> depMap = coinDepositList.stream().collect(Collectors.groupingBy(CoinDeposit::getUid));
        //查询所有用户id对应的username
        Map<Integer, String> userMap = userServiceImpl.list().stream().collect(Collectors.toMap(User::getId, User::getUsername));
        //计算可得彩金，需打流水
        depMap.forEach((uid, list) -> {
            if (isPromotionsEffective(Constant.INVITE_FRIENDS, uid)) {
                XxlJobLogger.log("邀请好友,已派彩！");
                return;
            }
            //总充值金额
            var totalCoin = list.stream().map(CoinDeposit::getPayCoin).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal coin = totalCoin.multiply(Constant.INVITE_RATE).setScale(2, RoundingMode.DOWN);
            list.sort(Comparator.comparing(CoinDeposit::getCreatedAt));
            XxlJobLogger.log("被邀请人:" + uid + "" + ";充值金额:" + totalCoin);
            //被邀请人的信息
            var now = DateNewUtils.now();
            CoinRewardsInvite coinRewardsInvite = new CoinRewardsInvite();
            coinRewardsInvite.setUid(uid);
            coinRewardsInvite.setUsername(userMap.get(uid));
            coinRewardsInvite.setCoin(totalCoin);
            //类型:1-邀请奖金 2-充值返利',
            coinRewardsInvite.setCategory(2);
            coinRewardsInvite.setCreatedAt(now);
            coinRewardsInvite.setUpdatedAt(now);
            var inActivityDto = ApplicationActivityReqDto.builder()
                    .id(Constant.INVITE_FRIENDS)
                    .availableCoin(coin)
                    .mosaicCoin(coin)
                    .build();
            promotionsTaskBase.invitePersistence(inActivityDto, userSupMap.get(uid), coinRewardsInvite);
            XxlJobLogger.log("邀请人：" + userSupMap.get(uid) + ";获取奖励" + coin + "成功！");
        });
    }

    /***
     * 全场返水
     *  1.体育中的连串过关投注额返水将计算在注单中最迟开赛的赛事当天中
     *  2.返水按照有效投注进行计算，在所有产品中，任何注单取消或本金退还以及在同一游戏中同时投注对等盘口，
     * 将不计算在有效投注。捕鱼游戏不计算返水；体育有效投注按照输赢金额进行计算。
     * （例如：下注100元，香港盘赔率0.2，如赢派彩20元，有效投注为20元；如输本金100元，有效投注为100元。）
     * 3.赢半和输半只计算一半流水，游戏返水需投注 1 倍流水即可提款；本优惠可与其他红利优惠同时享有。（除去部分标注不与返水共享的优惠。
     *
     */
    @Override
    public void rebateAllGames() {
        var pairTime = getBeforeTime(1);
        XxlJobLogger.log("全场返水任务调度;开始时间" + DateUtils.yyyyMMddHHmmss(pairTime.getLeft()) + "结束时间" + DateUtils.yyyyMMddHHmmss(pairTime.getRight()));
        List<User> userList = userServiceImpl.list(new LambdaQueryWrapper<User>().in(User::getRole, List.of(0, 1, 2)));
        //升级时间是否大于前一天的结束时间，大于结束时间则降一级处理(计算返水的有效金额是前一天的记录)
        var coinRewardList = coinRewardsServiceImpl.lambdaQuery()
                .eq(CoinRewards::getReferId, 11)
                .ge(CoinRewards::getCreatedAt, pairTime.getRight())
                .list();
        Set<Integer> uidSet = new HashSet<>();
        if (!CollectionUtils.isEmpty(coinRewardList)) {
            uidSet.addAll(coinRewardList.stream().map(CoinRewards::getUid).collect(Collectors.toSet()));
        }
        userList.forEach(user -> {
            if (uidSet.contains(user.getId())) {
                user.setLevelId(user.getLevelId() - 1);
            }
        });
        Map<Integer, Integer> levelMap = userList.stream().collect(Collectors.toMap(User::getId, User::getLevelId));
        //根据uid的会员等级获取返水比例
        List<UserLevelRebate> userLevelRebates = userLevelRebateServiceImpl.lambdaQuery()
                .eq(UserLevelRebate::getStatus, 1).list();
        //获取会员等级的返水上限
        var maxRebateMap = userLevelServiceImpl.list().stream().collect(Collectors.toMap(UserLevel::getId, UserLevel::getMaxRebateCoin));
        var uidRebateMap = new HashMap<Integer, BigDecimal>();
        //关联用户对应等级，获取返水上限。
        levelMap.forEach((uid, levelId) -> uidRebateMap.put(uid, maxRebateMap.get(levelId)));
        //Table->用户id,游戏组id,对应的返水比例
        Table<Integer, Integer, BigDecimal> rebateTable = HashBasedTable.create();
        userLevelRebates.forEach(userLevelRebate ->
                rebateTable.put(userLevelRebate.getLevelId(), userLevelRebate.getGroupId(), userLevelRebate.getRebateRate())
        );
        var groupMap = rebateTable.columnMap();
        var gameList = gameListServiceImpl.lambdaQuery().eq(GameList::getStatus, 1).list();
        //计算每个的返水
        for (var game : gameList) {
            XxlJobLogger.log("执行游戏" + game.getName() + "返水");
            //没有返水比例的游戏不参与返水活动
            if (Objects.isNull(groupMap.get(game.getGroupId()))) {
                continue;
            }
            PlatGameQueryDateDto platDto = new PlatGameQueryDateDto();
            platDto.setStartTime(pairTime.getLeft());
            platDto.setEndTime(pairTime.getRight());
            platDto.setGameId(game.getId());
            platDto.setPromotionsId(Constant.REBATE_ALL_GAMES);
            Map<Integer, BigDecimal> betMap = gameCommonBase.getUserBetCoin(platDto);
            XxlJobLogger.log(game.getName() + "获取有效投注额用户个数：" + betMap.size());
            // 计算可得彩金，需打流水入库
            for (Map.Entry<Integer, BigDecimal> entry : betMap.entrySet()) {
                var uid = entry.getKey();
                var validCoin = entry.getValue();
                //是否领取返水
                if (isRebateReceive(game.getId(), uid)) {
                    continue;
                }
                //获取返水比例
                var rebateRate = rebateTable.get(levelMap.get(uid), game.getGroupId());
                var maxRebateCoin = uidRebateMap.getOrDefault(uid, BigDecimal.ZERO);
                if (rebateRate != null && validCoin.compareTo(BigDecimal.ZERO) > 0 && maxRebateCoin.compareTo(rebateRate) > 0) {
                    var rebateCoin = validCoin.multiply(rebateRate);
                    var appDto = ApplicationActivityReqDto.builder()
                            .id(Constant.REBATE_ALL_GAMES)
                            .availableCoin(rebateCoin)
                            .mosaicCoin(rebateCoin)
                            .build();
                    promotionsTaskBase.rebatePersistence(appDto, uid, game, rebateRate, validCoin);
                }
            }
        }
    }

    /***
     * VIP 会员成长
     * 1.晋升标准：会员的累计有效投注额达到相应级别的要求，即可在次日24点前晋级相应VIP等级。
     * 2.晋升顺序：星级等级达到相应的要求可每天晋升一级，但星级等级不可越级晋升。
     * 3.保级要求：会员在达到某星级后，90天内投注需要完成保级要求，如果在此期间完成晋级，保级要求从新按照当前等级计算
     * 4.降级标准：如果会员在一个季度（90天计算）内没有完成相应的保级要求流水，系统会自动降级一个等级，响应的返水及其他优惠也会随之调整至降级的等级。
     * 5.特别优惠：达到相应等级的星级会员可联系在线客服进行申请，礼品不能折算为现金，
     * 每个级别的名贵礼品每位会员仅能获得1次 ，BWG體育对活动拥有最终解释权。（名贵礼品仅针对6星级/7星级/8星级/9星级/10星级会员）
     * 6.升级礼金：升级礼金在会员达到该会员级别后系统自动派发，每个级别的升级礼金每位会员仅能获得1次。（升级礼金1倍流水即可提款）
     * 7.每月红包：会员在上个月有过至少1次成功存款或者有过1次有效投注（两者需满足其一），即可在每月1号获得上个月相应等级的每月红包彩金。
     * （每月红包彩金1倍流水即可提款）
     */
    @Override
    public void vipGrowUp() {
        var pairTime = getBeforeTime(1);
        XxlJobLogger.log("VIP会员成长任务调度;开始时间" + DateUtils.yyyyMMddHHmmss(pairTime.getLeft()) + "结束时间" + DateUtils.yyyyMMddHHmmss(pairTime.getRight()));
        //查询所有用户ID
        List<User> userList = userServiceImpl.list();
        var userprofileMap = userProfileServiceImpl.list().parallelStream()
                .collect(Collectors.toMap(UserProfile::getUid, UserProfile::getUpgradeBalance));
        //会员等级规则
        var levelList = userLevelServiceImpl.list();
        Map<Integer, UserLevel> userLevelMap = levelList.parallelStream()
                .collect(Collectors.toMap(UserLevel::getId, a -> a, (k1, k2) -> k1));
        //会员的最高等级
        var maxOptional = levelList.stream().max(Comparator.comparing(UserLevel::getId));
        var maxLevel = maxOptional.map(UserLevel::getId).orElse(1);
        //会员id对应的会员规则Map
        Map<Integer, UserLevel> userIdToLevelMap = userList.parallelStream()
                .filter(user -> userLevelMap.get(user.getLevelId()) != null).
                        collect(Collectors.toMap(User::getId, user -> userLevelMap.get(user.getLevelId())));
        ObjIntConsumer<String> userConsumer = (symbol, uid) -> {
            userServiceImpl.lambdaUpdate().setSql("level_id = level_id " + symbol + " 1").eq(User::getId, uid).update();
            jedisUtil.del(KeyConstant.USER_BASIC_INFO_UID_HASH);
            jedisUtil.del(KeyConstant.USER_BASIC_INFO_USERNAME_HASH);
        };
        //迭代每个个用户的有效投注金额，进行升级判断
        for (User user : userList) {
            var uid = user.getId();
            UserLevel userLevel = userIdToLevelMap.get(uid);
            //过滤已经是最高等级及不再活动时间范围内和领取过奖励的用户
            if (userLevel.getId().equals(maxLevel) || isPromotionsEffective(Constant.VIP_GROW_UP, uid, Constant.REWARDS_UPGRADE)) {
                continue;
            }
            var coinRewardList = coinRewardsServiceImpl.lambdaQuery()
                    .eq(CoinRewards::getReferId, Constant.VIP_GROW_UP)
                    .eq(CoinRewards::getUid, uid)
                    //升级与降级信息
                    .and(x -> x.apply("JSON_EXTRACT(info , '$.payType') ='" + Constant.REWARDS_UPGRADE + "'")
                            .or()
                            .apply("JSON_EXTRACT(info , '$.payType')  ='" + Constant.VIP_RELEGATION + "'")
                    )
                    .list();
            var startTime = CollectionUtils.isEmpty(coinRewardList) ? 0 :
                    coinRewardList.stream().map(CoinRewards::getCreatedAt).max(Comparator.comparing(Integer::intValue)).orElse(0);
            //晋级标准，获取投注额
            PlatGameQueryDateDto platDto = new PlatGameQueryDateDto();
            platDto.setStartTime(startTime);
            platDto.setUid(uid);
            platDto.setEndTime(pairTime.getRight());
            Map<Integer, BigDecimal> userBetCoinMap = gameCommonBase.getUserBetCoin(platDto);
            var betCoin = userBetCoinMap.getOrDefault(uid, BigDecimal.ZERO);
            XxlJobLogger.log("用户：" + uid + "获取有效投注额的条件：" + platDto);
            XxlJobLogger.log("每个用户的有效投注金额" + userBetCoinMap);
            //获取升级金额,对应下一级的升级奖励
            var levelId = userLevel.getId().equals(maxLevel) ? maxLevel : userLevel.getId() + 1;
            var nextUserLevel = userLevelMap.get(levelId);
            betCoin = userprofileMap.getOrDefault(uid, BigDecimal.ZERO).add(betCoin);
            if (betCoin.compareTo(BigDecimal.ZERO) > 0 && betCoin.compareTo(nextUserLevel.getScoreUpgrade()) >= 0) {
                //info扩展信息
                var infoJSONObject = new JSONObject();
                var detailsJSONObject = new JSONObject();
                detailsJSONObject.put("level", userLevel.getCode());
                detailsJSONObject.put("payType", Constant.REWARDS_UPGRADE);
                detailsJSONObject.put("payCoin", betCoin);
                detailsJSONObject.put("balance", betCoin.subtract(nextUserLevel.getScoreUpgrade()));
                infoJSONObject.put(DETAILS, detailsJSONObject.toJSONString());
                //升级处理,存入升级奖金,提升用户会员等级
                var activityReqDto = ApplicationActivityReqDto.builder()
                        .id(Constant.VIP_GROW_UP)
                        .availableCoin(nextUserLevel.getRewardsUpgrade())
                        .mosaicCoin(nextUserLevel.getRewardsUpgrade())
                        .info(infoJSONObject.toJSONString())
                        .build();
                promotionsTaskBase.handlePromotionsPersistence(activityReqDto, uid);
                //修改用户等级
                userConsumer.accept("+", uid);
            }
        }
        //降级处理
        vipRelegation(userIdToLevelMap, userConsumer);
        //每月红包奖励
        rewardsMonthly(userList, userIdToLevelMap);
    }

    /**
     * 降级处理
     *
     * @param userIdToLevelMap 会员等级集合
     * @param userConsumer     会员消函数
     */
    public void vipRelegation(Map<Integer, UserLevel> userIdToLevelMap, ObjIntConsumer<String> userConsumer) {
        //保级，降级处理,前90天
        var pairTime = getBeforeTime(90);
        var vipStartTime = pairTime.getLeft();
        var vipEndTime = pairTime.getRight();
        //保级的有效投注，获取投注额
        PlatGameQueryDateDto vipPlatDto = new PlatGameQueryDateDto();
        vipPlatDto.setStartTime(vipStartTime);
        vipPlatDto.setEndTime(vipEndTime);
        Map<Integer, BigDecimal> userBetCoinRelMap = gameCommonBase.getUserBetCoin(vipPlatDto);
        for (Map.Entry<Integer, UserLevel> entry : userIdToLevelMap.entrySet()) {
            var uid = entry.getKey();
            var userLevel = entry.getValue();
            BigDecimal relegationBetCoin = userBetCoinRelMap.getOrDefault(uid, BigDecimal.ZERO);
            BigDecimal scoreRelegation = userLevel.getScoreRelegation();
            //投注金额小于保级积分，则降级处理
            var relegationTime = jedisUtil.hget(KeyConstant.USER_RELEGATION, String.valueOf(uid));
            //上次执行的时间差（一天）
            var lastTime = relegationTime == null ? DateNewUtils.now() - 24 * 3600 : Integer.parseInt(relegationTime);
            var differenceFlag = DateNewUtils.now() - lastTime < 24 * 3600;
            //会员等级最少为0级对应的id为1,不可再降级
            if (differenceFlag || userLevel.getId() == 1) {
                continue;
            }
            if (relegationBetCoin.compareTo(scoreRelegation) < 0) {
                promotionsTaskBase.handlerVipRelegation(uid);
                //用户等级调整
                userConsumer.accept("-", uid);
                jedisUtil.hset(KeyConstant.USER_RELEGATION, String.valueOf(uid), String.valueOf(DateNewUtils.now()));
            }
        }
    }

    /**
     * 每月红包
     *
     * @param userList         用户信息
     * @param userIdToLevelMap 用户等级信息
     */
    private void rewardsMonthly(List<User> userList, Map<Integer, UserLevel> userIdToLevelMap) {
        //每月红包-》判断是否每月的第一天
        ZonedDateTime zoneNow = DateNewUtils.utc8Zoned(DateNewUtils.now());
        int dayOfMonth = zoneNow.getDayOfMonth();
        if (dayOfMonth != 1) {
            log.info("今日不是本月一号，无法领取每月红包！");
            return;
        }
        //前一个月的开始与结束时间
        var monthStartTime = (int) zoneNow.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth()).toEpochSecond();
        var monthEndTime = (int) zoneNow.with(TemporalAdjusters.firstDayOfMonth()).toEpochSecond();
        PlatGameQueryDateDto monthPlatDto = new PlatGameQueryDateDto();
        monthPlatDto.setStartTime(monthEndTime);
        monthPlatDto.setEndTime(monthEndTime);
        //用户每月的有效投注金额
        Map<Integer, BigDecimal> userMonthBetCoin = gameCommonBase.getUserBetCoin(monthPlatDto);
        List<Integer> userIds = userList.parallelStream().map(User::getId).collect(Collectors.toList());
        //每个月的成功充值
        List<CoinDeposit> coinDepositList = coinDepositServiceImpl.list(new LambdaQueryWrapper<CoinDeposit>()
                .ge(CoinDeposit::getCreatedAt, monthStartTime)
                .le(CoinDeposit::getCreatedAt, monthEndTime)
                .in(CoinDeposit::getStatus, List.of(1, 2, 9))
                .in(CoinDeposit::getUid, userIds));
        //求每个用户的充值次数
        Map<Integer, Long> coinDepositMap = CollectionUtils.isEmpty(coinDepositList) ? new HashMap<>() :
                coinDepositList.stream().collect(Collectors.groupingBy(CoinDeposit::getUid, Collectors.counting()));
        for (Map.Entry<Integer, UserLevel> entry : userIdToLevelMap.entrySet()) {
            var uid = entry.getKey();
            var userLevel = entry.getValue();
            //1个月有过至少1次成功存款或者有过1次有效投注（两者需满足其一)
            if (userMonthBetCoin.getOrDefault(uid, BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0
                    || coinDepositMap.getOrDefault(uid, 0L) > 0) {
                if (isPromotionsEffective(Constant.VIP_GROW_UP, uid, Constant.REWARDS_MONTHLY)) {
                    continue;
                }
                var payCoin = coinDepositMap.getOrDefault(uid, 0L) > 0 ? coinDepositList.stream().filter(coinDeposit -> coinDeposit.getUid().equals(uid))
                        .map(CoinDeposit::getPayCoin).reduce(BigDecimal::add).orElse(BigDecimal.ZERO) :
                        userMonthBetCoin.get(uid);
                //info扩展信息
                var infoJSONObject = new JSONObject();
                var detailsJSONObject = new JSONObject();
                detailsJSONObject.put("level", userLevel.getCode());
                detailsJSONObject.put("payType", Constant.REWARDS_MONTHLY);
                detailsJSONObject.put("payCoin", payCoin);
                infoJSONObject.put(DETAILS, detailsJSONObject.toJSONString());
                //每月红包奖励处理
                promotionsTaskBase.handlePromotionsPersistence(ApplicationActivityReqDto.builder()
                        .id(Constant.VIP_GROW_UP)
                        .availableCoin(userLevel.getRewardsMonthly())
                        .mosaicCoin(userLevel.getRewardsMonthly())
                        .info(infoJSONObject.toJSONString())
                        .build(), uid);
            }
        }
    }

    /**
     * 加盟代理
     */
    @Override
    public void agent(int count) {
        promotionsExtendServiceImpl.agentCommissionAndReWard(Constant.AGENT, count);
    }

    /**
     * 流水佣金
     */
    @Override
    @SneakyThrows
    public void flowCommission(int count, String date) {
        promotionsExtendServiceImpl.agentCommissionAndReWard(Constant.FLOW, count);
        //查询未结算的佣金流水
        var coinCommissionList = coinCommissionServiceImpl.lambdaQuery()
                .eq(CoinCommission::getCategory, 0)
                .eq(CoinCommission::getStatus, 0)
                .list();
        if (CollectionUtils.isEmpty(coinCommissionList)) {
            log.info("无流水佣金记录！");
            return;
        }
        //获取用户的流水规则
        var ruleJson = parseObject(configCache.getConfigByTitle(Constant.COMMISSION_RULE));
        var effectDate = ruleJson.getString(Constant.EFFECT_DATE);
        var settleType = ruleJson.getInteger(Constant.SETTLE_TYPE);
        var sdf = new SimpleDateFormat(DateNewUtils.Format.yyyy_MM_dd.getValue());
        var effectSecond = (int) (sdf.parse(effectDate).getTime() / 1000);
        var now = LocalDateTime.now().atZone(DateNewUtils.getZoneId());
        //传入时间用于测试
        if (StringUtils.isNotEmpty(date)) {
            now = sdf.parse(date).toInstant().atZone(DateNewUtils.getZoneId());
        }
        var zoneNow = now.toEpochSecond();
        //当前日期大于生效日期，则按照信息的结算规则结算，否则按照旧的结算规则结算。
        if (zoneNow < effectSecond) {
            settleType = ruleJson.getInteger(Constant.OLD_SETTLE_TYPE);
        }
        //按天结算，直接更新记录，按周结算则，周一更新记录，按月结算则每月1号更新记录。
        if (Constant.SETTLE_DAY.equals(settleType) ||
                (Constant.SETTLE_WEEK.equals(settleType) && now.getDayOfWeek() == DayOfWeek.MONDAY) ||
                (Constant.SETTLE_MONTH.equals(settleType) && now.getDayOfMonth() == 1)) {
            coinCommissionList.forEach(coinCommission -> {
                coinCommission.setStatus(1);
                coinCommission.setUpdatedAt((int) zoneNow);
            });
            persistenceServiceImpl.flowCommissionPersistence(coinCommissionList);
        }
    }

    /**
     * 1.验证活动是否在活动时间内
     * 2.是否领取奖金
     *
     * @param pId 活动ID
     * @return 是否
     */
    public boolean isPromotionsEffective(Integer pId, Integer uid, String... info) {
        Promotions promotions = promotionsServiceImpl.getById(pId);
        var now = DateNewUtils.now();
        var pairTime = getBeforeTime(1);
        if (now < promotions.getStartedAt() || now > promotions.getEndedAt()) {
            if (pId.equals(Constant.LIVE_DAILY_BONUS)) {
                XxlJobLogger.log("真人投注送彩金,活动已结束！");
            } else if (pId.equals(Constant.VIP_GROW_UP)) {
                XxlJobLogger.log("VIP成长,活动已结束！");
            } else if (pId.equals(Constant.INVITE_FRIENDS)) {
                XxlJobLogger.log("邀请好友,活动已结束！");
            }
            return true;
        }
        if (Constant.INVITE_FRIENDS.equals(pId)) {
            var inviteCount = coinRewardsInviteServiceImpl.count(new LambdaQueryWrapper<CoinRewardsInvite>()
                    .eq(CoinRewardsInvite::getUid, uid)
                    //2-充值返利
                    .eq(CoinRewardsInvite::getCategory, 2)
                    .ge(CoinRewardsInvite::getCreatedAt, pairTime.getLeft())
                    .le(CoinRewardsInvite::getCreatedAt, pairTime.getRight())
            );
            return inviteCount > 0;
        }
        var promotionsName = info != null && info.length > 0 ? info[0] : "";
        int count = coinRewardsServiceImpl.count(new LambdaQueryWrapper<CoinRewards>()
                .eq(Objects.nonNull(pId), CoinRewards::getReferId, pId)
                .eq(Objects.nonNull(uid), CoinRewards::getUid, uid)
                .like(Strings.isNotEmpty(promotionsName), CoinRewards::getInfo, promotionsName)
                .ge(CoinRewards::getCreatedAt, pairTime.getLeft())
                .le(CoinRewards::getCreatedAt, pairTime.getRight()));
        if (count > 0) {
            if (Constant.LIVE_DAILY_BONUS.equals(pId)) {
                XxlJobLogger.log("真人投注送彩金,已派彩！");
            } else if (Constant.VIP_GROW_UP.equals(pId)) {
                XxlJobLogger.log("VIP成长,已派彩！");
            }
            return true;
        }
        return false;
    }

    /**
     * 判断返水是否领取
     *
     * @param pId 上级ID
     * @param uid 用户ID
     * @return 是否领取
     */
    public boolean isRebateReceive(Integer pId, Integer uid) {
        Promotions promotions = promotionsServiceImpl.getById(Constant.REBATE_ALL_GAMES);
        //活动是否有效
        var now = DateNewUtils.now();
        var pairTime = getBeforeTime(1);
        if (now < promotions.getStartedAt() || now > promotions.getEndedAt()) {
            return true;
        }
        //是否领取返水
        var count = coinRebateServiceImpl.lambdaQuery()
                .eq(CoinRebate::getGameListId, pId)
                .eq(CoinRebate::getUid, uid)
                //当天是否领，endTime是当天的开始时间
                .ge(CoinRebate::getCreatedAt, pairTime.getRight())
                .count();
        if (count > 0) {
            log.info("游戏id：" + pId + "返水已领取！");
            return true;
        }
        return false;
    }
}
