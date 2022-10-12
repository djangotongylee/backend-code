package com.xinbo.sports.apiend.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.xinbo.sports.apiend.base.GameCommonBase;
import com.xinbo.sports.apiend.io.dto.promotions.*;
import com.xinbo.sports.apiend.service.IPromotionsService;
import com.xinbo.sports.apiend.service.IUserInfoService;
import com.xinbo.sports.dao.generator.po.*;
import com.xinbo.sports.dao.generator.service.*;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams;
import com.xinbo.sports.service.base.PromotionsBase;
import com.xinbo.sports.service.base.RegisterBonusPromotions;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.service.cache.redis.PromotionsCache;
import com.xinbo.sports.service.common.Constant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.dto.UserInfo;
import com.xinbo.sports.service.io.dto.promotions.ApplicationActivityReqDto;
import com.xinbo.sports.service.io.enums.BaseEnum;
import com.xinbo.sports.utils.*;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.xinbo.sports.service.common.Constant.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;


/**
 * @author: wells
 * @date: 2020/4/14
 * @description: 优惠活动实现类
 */
@Slf4j
@Service("iPromotionsServiceImpl")
public class IPromotionsServiceImpl implements IPromotionsService {
    //充值状态
    public static final List<Integer> DEPOSIT_STATUS = List.of(1, 2, 9);
    //红利
    public static final String PROFIT = "profit";
    //流水
    public static final String FLOW_CLAIM = "flowClaim";
    //取第一条记录
    public static final String LIMIT_ONE = "limit 1";
    @Resource
    IUserInfoService userInfoServiceImpl;
    @Resource
    private PromotionsGroupService promotionsGroupServiceImpl;
    @Resource
    private CoinRewardsService coinRewardsServiceImpl;
    @Resource
    private CoinDepositService coinDepositServiceImpl;
    @Autowired
    private UserLevelRebateService userLevelRebateServiceImpl;
    @Autowired
    private UserSignService userSignServiceImpl;
    @Autowired
    private UserLevelService userLevelServiceImpl;
    @Autowired
    private PromotionsBase promotionsBase;
    @Autowired
    private PromotionsExtendServiceImpl promotionsExtendServiceImpl;
    @Autowired
    private ConfigCache configCache;
    @Autowired
    private GameCommonBase gameCommonBase;
    @Autowired
    private PromotionsCache promotionsCache;
    @Autowired
    private PromotionsService promotionsServiceImpl;
    @Autowired
    private RegisterBonusPromotions registerBonusPromotions;
    @Autowired
    private RegisterRewardsService registerRewardsServiceImpl;
    @Resource
    private HttpServletRequest httpServletRequest;

    /**
     * 优惠活动->列表
     *
     * @return
     */
    @Override
    public List<PromotionsGroupResDto> promotionsList() {
        String staticServer = configCache.getStaticServer();
        /* 活动组列表*/
        var promotionsGroupList = promotionsGroupServiceImpl.list(
                new LambdaQueryWrapper<PromotionsGroup>()
                        .eq(PromotionsGroup::getStatus, 1)
                        .orderByDesc(PromotionsGroup::getSort, PromotionsGroup::getId)
        );
        var groupSet = promotionsGroupList.stream().map(PromotionsGroup::getId).collect(Collectors.toSet());
        /* 活动列表 */
        List<PromotionsResDto> promotionsResDtos = promotionsCache.getPromotionsListCache()
                .stream().filter(promotions -> groupSet.contains(promotions.getCategory()))
                .map(o -> {
                    PromotionsResDto promotionsResDto = BeanConvertUtils.beanCopy(o, PromotionsResDto::new);
                    promotionsResDto.setImg(promotionsResDto.getImg().startsWith("http")?promotionsResDto.getImg():staticServer + promotionsResDto.getImg());
                    return promotionsResDto;
                }).collect(Collectors.toList());
        //活动分组，用于管理活动组
        var promotionsMap = promotionsResDtos.stream().collect(Collectors.groupingBy(PromotionsResDto::getCategory));
        var promotionsGroupResDtoList = BeanConvertUtils.copyListProperties(promotionsGroupList, PromotionsGroupResDto::new,
                (promotionsGroup, promotionsGroupResDto) ->
                        promotionsGroupResDto.setPromotionsResDtoList(promotionsMap.get(promotionsGroup.getId())));
        var promotionsGroupResDto = new PromotionsGroupResDto();
        promotionsGroupResDto.setId(0);
        promotionsGroupResDto.setCodeZh(Constant.ALL_PROMOTIONS);
        promotionsGroupResDto.setPromotionsResDtoList(promotionsResDtos);
        promotionsGroupResDtoList.add(promotionsGroupResDto);
        return promotionsGroupResDtoList.stream().sorted(Comparator.comparing(PromotionsGroupResDto::getId)).collect(Collectors.toList());
    }

    /**
     * 获取活动金额详情
     * *一倍流水首充
     * 1.判断是否一倍流水首充或首充超高红利
     * 2.获取第一笔充值金额
     * 3.判断首充获利
     *
     * @param reqDto
     * @return
     */
    @Override
    public PromotionsInfoResDto promotionsInfo(PromotionsInfoReqDto reqDto) {
        //获取用户信息
        var userInfo = userInfoServiceImpl.findIdentityByApiToken();
        Promotions promotions;
        if (DAILY_FIRST_DEPOSIT.equals(reqDto.getCode())) {
            promotions = promotionsServiceImpl.getOne(new LambdaQueryWrapper<Promotions>()
                    .eq(Promotions::getCode, reqDto.getCode()), false);
            reqDto.setId(promotions.getId());
        }
        promotions = promotionsCache.getPromotionsCache(reqDto.getId());
        var promotionsInfoResDto = BeanConvertUtils.copyProperties(promotions, PromotionsInfoResDto::new, (source, res) -> res.setDescription(source.getDescript()));
        var payCoin = BigDecimal.ZERO;
        //首充或超高红利
        var profitCoinMap = new HashMap<String, BigDecimal>();
        if (reqDto.getId().equals(FIRST_DEPOSIT_DOUBLE) || reqDto.getId().equals(FIRST_DEPOSIT_SUPER_BONUS)) {
            //是否获取活动奖励
            var count = coinRewardsServiceImpl.count(new LambdaQueryWrapper<CoinRewards>()
                    .eq(CoinRewards::getUid, userInfo.getId())
                    //首充与超高利首充
                    .in(CoinRewards::getReferId, Lists.newArrayList(FIRST_DEPOSIT_DOUBLE, FIRST_DEPOSIT_SUPER_BONUS)));
            if (count == 0) {
                var coinDeposit = coinDepositServiceImpl.getOne(new LambdaQueryWrapper<CoinDeposit>()
                        .eq(CoinDeposit::getUid, userInfo.getId())
                        .in(CoinDeposit::getStatus, DEPOSIT_STATUS)
                        .orderByAsc(CoinDeposit::getCreatedAt)
                        .last(LIMIT_ONE));
                if (isNull(coinDeposit)) {
                    log.info(CodeInfo.ACTIVE_NO_FIRST_DEPOSIT.getMsg());
                    return promotionsInfoResDto;
                }
                payCoin = coinDeposit.getPayCoin();
                //根据充值金额,获取红利
                profitCoinMap.putAll(promotionsExtendServiceImpl.getProfitCoin(payCoin, reqDto.getId()));
            }
        }
        //每日首存领福利活动
        if (DAILY_FIRST_DEPOSIT.equals(reqDto.getCode())) {
            var coinDeposit = getDailyFirstDeposit();
            if (Objects.isNull(coinDeposit)) {
                return promotionsInfoResDto;
            }
            payCoin = coinDeposit.getPayCoin();
            //根据充值金额,获取红利
            profitCoinMap.putAll(promotionsExtendServiceImpl.getProfitCoin(payCoin, reqDto.getId()));
        }
        if (!CollectionUtils.isEmpty(profitCoinMap)) {
            BigDecimal profit = profitCoinMap.get(PROFIT);
            BigDecimal flowClaim = profitCoinMap.get(FLOW_CLAIM);
            promotionsInfoResDto.setApplicaCoin(payCoin);
            promotionsInfoResDto.setAvailableCoin(profit);
            promotionsInfoResDto.setMosaicCoin(flowClaim);
        }
        return promotionsInfoResDto;
    }

    /**
     * 每日首存领福利
     *
     * @return 充值记录
     */
    public CoinDeposit getDailyFirstDeposit() {
        //获取用户信息
        var userInfo = userInfoServiceImpl.findIdentityByApiToken();
        var zoneNow = DateNewUtils.utc8Zoned(DateNewUtils.now());
        int startTime = (int) zoneNow.withHour(0).withMinute(0).withSecond(0).withNano(0).toEpochSecond();
        int endTime = (int) zoneNow.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0).toEpochSecond();
        //查询用户的充值信息，判断当日是否存首存
        return coinDepositServiceImpl.getOne(new LambdaQueryWrapper<CoinDeposit>()
                        .eq(CoinDeposit::getUid, userInfo.getId())
                        .in(CoinDeposit::getStatus, DEPOSIT_STATUS)
                        .ge(CoinDeposit::getUpdatedAt, startTime)
                        .le(CoinDeposit::getUpdatedAt, endTime)
                        .orderByAsc(CoinDeposit::getUpdatedAt)
                , false
        );
    }

    /**
     * 优优惠活动->全场返水
     *
     * @return
     */
    @Override
    public List<LevelRebateListResDto> levelRebateList() {
        var rebateList = userLevelRebateServiceImpl.lambdaQuery().eq(UserLevelRebate::getStatus, 1).list();
        var map = rebateList.stream().collect(Collectors.groupingBy(UserLevelRebate::getLevelId));
        Function<BigDecimal, String> function = x -> x.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.DOWN) + "%";
        List<LevelRebateListResDto> returnRebateList = new ArrayList<>();
        map.forEach((levelId, userLevel) -> {
            var rebateListResDto = new LevelRebateListResDto();
            rebateListResDto.setLevelId(levelId);
            userLevel.forEach(userGroup -> {
                switch (userGroup.getGroupId()) {
                    case 1:
                        rebateListResDto.setSports(function.apply(userGroup.getRebateRate()));
                        break;
                    case 2:
                        rebateListResDto.setEGames(function.apply(userGroup.getRebateRate()));
                        break;
                    case 3:
                        rebateListResDto.setLivesGame(function.apply(userGroup.getRebateRate()));
                        break;
                    case 4:
                        rebateListResDto.setFinishGame(function.apply(userGroup.getRebateRate()));
                        break;
                    case 5:
                        rebateListResDto.setChess(function.apply(userGroup.getRebateRate()));
                        break;
                    case 6:
                        rebateListResDto.setESports(function.apply(userGroup.getRebateRate()));
                        break;
                    default:
                        break;
                }
            });
            returnRebateList.add(rebateListResDto);
        });
        return returnRebateList;
    }

    /***
     * 优惠活动->vip会员成长
     * @return
     */
    @Override
    public List<LevelListResDto> levelList() {
        //获取用户信息
        var userInfo = userInfoServiceImpl.findIdentityByApiToken();
        var userLevelList = userLevelServiceImpl.list();
        var coinRewards = coinRewardsServiceImpl.lambdaQuery()
                .eq(CoinRewards::getUid, userInfo.getId())
                .eq(CoinRewards::getReferId, Constant.VIP_GROW_UP)
                .orderByDesc(CoinRewards::getCreatedAt)
                .last(LIMIT_ONE)
                .one();
        var createdAt = coinRewards != null ? coinRewards.getCreatedAt() : 0;
        //查询投注->已完成金额
        var platGameQueryDateDto = PlatFactoryParams.PlatGameQueryDateDto.builder()
                .uid(userInfo.getId())
                .startTime(createdAt)
                .build();
        BigDecimal betTotalCoin = gameCommonBase.getCoinStatisticsByDate(platGameQueryDateDto).getCoinBet();
        var currentLevelCoin = betTotalCoin.add(userInfo.getUpgradeBalance());
        var upgradeMap = userLevelList.stream().collect(Collectors.toMap(UserLevel::getId, UserLevel::getScoreUpgrade));
        var maxId = userLevelList.stream().max(Comparator.comparing(UserLevel::getId)).orElseGet(UserLevel::new).getId();
        return BeanConvertUtils.copyListProperties(userLevelList, LevelListResDto::new, (userLevel, resDto) -> {
            var levelId = userLevel.getId();
            var scoreUpgrade = levelId.equals(maxId) ? upgradeMap.get(maxId) : upgradeMap.get(levelId + 1);
            resDto.setScoreUpgrade(scoreUpgrade);
            if (levelId < userInfo.getLevelId()) {
                resDto.setCurrentFlow(resDto.getScoreUpgrade());
            } else if (levelId.equals(userInfo.getLevelId())) {
                resDto.setCurrentFlow(currentLevelCoin);
            } else {
                resDto.setCurrentFlow(BigDecimal.ZERO);
            }
        });
    }

    /***
     * * 1.判断今日是否签到
     * 2.周天-判断今日是否跨年
     * 3.存入签到记录
     * 4.修改用户金额
     * 5.插入coin_log日志
     * @return
     */
    @Override
    public UserSingResDto userSign() {
        // 判定签到活动是否存在 已经是否处于开启状态
        Promotions sign_bonus = promotionsCache.getPromotionsCache(Constant.SIGN_IN_BONUS);
        if (sign_bonus == null || sign_bonus.getStatus() != 1) {
            throw new BusinessException(CodeInfo.ACTIVE_CLOSE);
        }

        //获取用户信息
        UserInfo userInfo = userInfoServiceImpl.findIdentityByApiToken();
        var localDate = LocalDate.now();
        int year = localDate.getYear();
        //当前周的星期几
        int dayOfWeek = localDate.getDayOfWeek().getValue();
        //ISO8061为准
        WeekFields weekFields = WeekFields.of(DayOfWeek.MONDAY, 4);
        //一年的第几周
        int week = localDate.get(weekFields.weekOfWeekBasedYear());
        UserSign userSign = userSignServiceImpl.getOne(new LambdaQueryWrapper<UserSign>()
                .eq(UserSign::getYear, year).eq(UserSign::getNw, week).eq(UserSign::getUid, userInfo.getId()));
        if (nonNull(userSign) && Strings.isNotEmpty(userSign.getDay()) && userSign.getDay().contains(String.valueOf(dayOfWeek))) {
            throw new BusinessException(CodeInfo.ACTIVE_ALREADY_SIGN_IN);
        }
        if (nonNull(userSign)) {
            //修改签到记录
            var signUpdate = new UserSign();
            signUpdate.setId(userSign.getId());
            signUpdate.setDay(userSign.getDay() + "," + dayOfWeek);
            userSignServiceImpl.updateById(signUpdate);
        } else {
            //插入签到记录
            var addSign = new UserSign();
            addSign.setUid(userInfo.getId());
            addSign.setUsername(userInfo.getUsername());
            addSign.setYear(year);
            addSign.setNw(week);
            addSign.setDay(String.valueOf(dayOfWeek));
            addSign.setCreatedAt(DateUtils.getCurrentTime());
            addSign.setUpdatedAt(DateUtils.getCurrentTime());
            userSignServiceImpl.save(addSign);
        }
        //签到逻辑处理
        BigDecimal finalCoin = getUserSignCoin();
        var reqDto = ApplicationActivityReqDto.builder()
                .id(Constant.SIGN_IN_BONUS)
                .availableCoin(finalCoin)
                .mosaicCoin(finalCoin)
                .build();
        promotionsBase.executePromotionsPersistence(reqDto, userInfo.getId());
        //签到完成返回签到列表
        return userSignList();
    }

    /**
     * 优惠活动->查看每日签到
     *
     * @return
     */
    @Override
    public UserSingResDto userSignList() {
        //获取用户信息
        UserInfo userInfo = userInfoServiceImpl.findIdentityByApiToken();
        Promotions promotions = promotionsCache.getPromotionsCache(Constant.SIGN_IN_BONUS);
        int now = DateUtils.getCurrentTime();
        if (now < promotions.getStartedAt() || now > promotions.getEndedAt()) {
            throw new BusinessException(CodeInfo.ACTIVE_CLOSE);
        }
        int year = LocalDate.now().getYear();
        int dayOfWeek = LocalDate.now().getDayOfWeek().getValue();
        //ISO8061为准
        WeekFields weekFields = WeekFields.of(DayOfWeek.MONDAY, 4);
        int week = LocalDate.now().get(weekFields.weekOfWeekBasedYear());
        LocalDate starDate = LocalDate.now().plusDays(-dayOfWeek);
        LocalDate endDate = LocalDate.now().plusDays((long) (7 - dayOfWeek));
        UserSign userSign = userSignServiceImpl.getOne(new LambdaQueryWrapper<UserSign>()
                .eq(UserSign::getYear, year).eq(UserSign::getNw, week).eq(UserSign::getUid, userInfo.getId()));
        List<String> indexList = new ArrayList<>();
        if (userSign != null && !StringUtils.isEmpty(userSign.getDay())) {
            indexList.addAll(new ArrayList<>(Arrays.asList(userSign.getDay().split(","))));
        }
        //是否跨年
        if (starDate.getYear() != endDate.getYear()) {
            int fastYear = starDate.getYear();
            int fastWeek = starDate.get(weekFields.weekOfWeekBasedYear());
            UserSign fastUserSign = userSignServiceImpl.getOne(new LambdaQueryWrapper<UserSign>()
                    .eq(UserSign::getYear, fastYear).eq(UserSign::getNw, fastWeek).eq(UserSign::getUid, userInfo.getId()));
            if (fastUserSign != null && !StringUtils.isEmpty(fastUserSign.getDay())) {
                indexList.addAll(new ArrayList<>(Arrays.asList(fastUserSign.getDay().split(","))));
            }
        }
        //查询一周的签到记录
        List<UserSingListResDto> userSingListResDtoList = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            UserSingListResDto userSingListResDto = new UserSingListResDto();
            userSingListResDto.setWeekOfDay(i);
            if (indexList.contains(String.valueOf(i))) {
                //status 0:已过期,1:已签到,2,未签到
                userSingListResDto.setStatus(1);
            } else {
                userSingListResDto.setStatus(i < dayOfWeek ? 0 : 2);
            }
            userSingListResDtoList.add(userSingListResDto);
        }
        UserSingResDto userSingResDto = new UserSingResDto();
        userSingResDto.setUserSignList(userSingListResDtoList);
        //获取累计天数,获取累计金额
        Pair<Integer, BigDecimal> pair = getConsecutiveDays();
        userSingResDto.setConsecutiveDays(pair.getLeft());
        userSingResDto.setCumulativeAmount(pair.getRight());
        //签到金额
        userSingResDto.setSignCoin(getUserSignCoin());
        return userSingResDto;
    }

    /**
     * 获取累计天数
     *
     * @return
     */
    public Pair<Integer, BigDecimal> getConsecutiveDays() {
        //获取用户信息
        UserInfo userInfo = userInfoServiceImpl.findIdentityByApiToken();
        //查询签到记录，按时间降序
        List<CoinRewards> coinRewardList = coinRewardsServiceImpl.list(new LambdaQueryWrapper<CoinRewards>()
                .eq(CoinRewards::getUid, userInfo.getId())
                .eq(CoinRewards::getReferId, Constant.SIGN_IN_BONUS)
                .orderByDesc(CoinRewards::getCreatedAt));
        int consecutiveDay = 0;
        BigDecimal cumulativeAmount = BigDecimal.ZERO;
        if (!CollectionUtils.isEmpty(coinRewardList)) {
            //累计天数计算
            List<String> createTimeList = coinRewardList.stream()
                    .map(x -> DateUtils.yyyyMMdd(x.getCreatedAt()))
                    .collect(Collectors.toList());
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate nowDate = LocalDate.now();
            String firstDate = dtf.format(nowDate);
            String secondDate = dtf.format(nowDate.plusDays(-1));
            //计算连续的天数
            Predicate<String> predicate = x -> !createTimeList.contains(x);
            var firstBoolean = predicate.test(firstDate) && predicate.test(secondDate);
            do {
                //第一天或前一天开始累计
                var condition = consecutiveDay == 0 && firstBoolean;
                String tempDate = dtf.format(nowDate.plusDays(-consecutiveDay));
                if (condition || (consecutiveDay > 0 && predicate.test(tempDate))) {
                    break;
                }
                consecutiveDay++;
            } while (true);
            //累计金额
            cumulativeAmount = coinRewardList.stream().map(CoinRewards::getCoin).reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        return Pair.of(consecutiveDay, cumulativeAmount);
    }

    /***
     * 获取签到金额
     * @return
     */
    public BigDecimal getUserSignCoin() {
        //获取用户信息
        UserInfo userInfo = userInfoServiceImpl.findIdentityByApiToken();
        var localDate = LocalDate.now();
        int year = localDate.getYear();
        //ISO8061为准
        WeekFields weekFields = WeekFields.of(DayOfWeek.MONDAY, 4);
        int dayOfWeek = localDate.getDayOfWeek().getValue();
        int week = localDate.get(weekFields.weekOfWeekBasedYear());
        BigDecimal coin = new BigDecimal(1);
        LocalDate starDate = localDate.plusDays(-dayOfWeek);
        LocalDate endDate = localDate.plusDays((long) (7 - dayOfWeek));
        UserSign userSign = userSignServiceImpl.getOne(new LambdaQueryWrapper<UserSign>()
                .eq(UserSign::getYear, year).eq(UserSign::getNw, week).eq(UserSign::getUid, userInfo.getId()));
        //是否周末
        if (dayOfWeek != 7) {
            return coin;
        }
        //是否跨年
        if (starDate.getYear() == endDate.getYear()) {
            if (userSign != null && !StringUtils.isEmpty(userSign.getDay()) && userSign.getDay().split(",").length == 7) {
                coin = coin.add(new BigDecimal(5));
            }
        } else {
            int fastYear = starDate.getYear();
            UserSign fastUserSign = userSignServiceImpl.getOne(new LambdaQueryWrapper<UserSign>()
                    .eq(UserSign::getYear, fastYear).eq(UserSign::getNw, week).eq(UserSign::getUid, userInfo.getId()));
            //签到天数
            int totalDay = 0;
            if (userSign != null && !StringUtils.isEmpty(userSign.getDay())) {
                totalDay = userSign.getDay().split(",").length;
            }

            if (fastUserSign != null && !StringUtils.isEmpty(fastUserSign.getDay())) {
                totalDay += fastUserSign.getDay().split(",").length;
            }

            if (totalDay == 7) {
                coin = coin.add(new BigDecimal(5));
            }
        }
        return coin;
    }

    /**
     * 优惠活动->申请优惠活动
     *
     * @param reqDto
     * @return
     */
    @Override
    public void applicationActivity(ApplicationActivityReqDto reqDto) {
        /**首充或超高红利获取优惠活动奖励或每日首存领福利*/
        if (reqDto.getId().equals(FIRST_DEPOSIT_DOUBLE) || reqDto.getId().equals(FIRST_DEPOSIT_SUPER_BONUS) || DAILY_FIRST_DEPOSIT.equals(reqDto.getCode())) {
            if (reqDto.getAvailableCoin().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(CodeInfo.PLAT_COIN_BELOW_MIN_LIMIT);
            }
            promotionsExtendServiceImpl.firstDeposit(reqDto);
        }
        /**新赛季回归送彩金*/
        if (reqDto.getId().equals(Constant.NEW_SEASON_COMEBACK)) {
            promotionsExtendServiceImpl.newSeasonComeback(reqDto);
        }
        /**
         * 注册送彩金
         */
        if (REGISTER_BONUS.equals(reqDto.getCode())) {
            //获取用户信息
            var userInfo = userInfoServiceImpl.findIdentityByApiToken();
            var count = registerRewardsServiceImpl.lambdaQuery()
                    .eq(RegisterRewards::getUid, userInfo.getId()).count();
            if (count > 0) {
                throw new BusinessException(CodeInfo.ACTIVE_COIN_ALREADY_RECEIVE);
            }
            var now = DateNewUtils.now();
            var promotions = promotionsServiceImpl.getOne(new LambdaQueryWrapper<Promotions>()
                    .eq(Promotions::getCode, REGISTER_BONUS)
                    .eq(Promotions::getPayoutCategory, MANUAL_PAY), false);
            if (nonNull(promotions)) {
                //插入注册送彩金记录
                var registerRewards = new RegisterRewards();
                registerRewards.setUid(userInfo.getId());
                registerRewards.setUsername(userInfo.getUsername());
                registerRewards.setIp(IpUtil.getIp(httpServletRequest));
                registerRewards.setMobile(userInfo.getMobile());
                registerRewards.setPromotionsId(promotions.getId());
                registerRewards.setPromotionsName(promotions.getCodeZh());
                registerRewards.setRegisterAt(userInfo.getCreatedAt());
                registerRewards.setCreatedAt(now);
                registerRewards.setUpdatedAt(now);
                registerRewardsServiceImpl.save(registerRewards);
            }
        }
    }

    /**
     * 优惠活动vip专属
     *
     * @return
     */
    @Override
    public VipExclusiveResDto vipExclusive() {
        //获取用户信息
        UserInfo userInfo = userInfoServiceImpl.findIdentityByApiToken();
        Integer levelId = userInfo.getLevelId();
        //会员返水
        List<UserLevelRebate> userLevelRebates = userLevelRebateServiceImpl.lambdaQuery()
                .eq(UserLevelRebate::getLevelId, levelId)
                .eq(UserLevelRebate::getStatus, 1)
                .list();
        //获取下级vip信息,无下级则取当前级
        List<UserLevel> userLevelList = userLevelServiceImpl.list();
        Map<Integer, BigDecimal> vipMap = userLevelList.stream().collect(Collectors.toMap(UserLevel::getId, UserLevel::getScoreUpgrade));
        var maxVipOption = userLevelList.stream().map(UserLevel::getId).max(Comparator.comparing(Integer::intValue));
        var scoreUpgrade = BigDecimal.ZERO;
        if (maxVipOption.isPresent()) {
            if (!maxVipOption.get().equals(levelId)) {
                scoreUpgrade = vipMap.getOrDefault(levelId + 1, BigDecimal.ZERO);
            } else {
                scoreUpgrade = vipMap.get(maxVipOption.get());
            }
        }
        //会员等级
        UserLevel userLevel = userLevelServiceImpl.getById(levelId);
        Function<BigDecimal, String> function = x -> x.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.DOWN) + "%";
        VipExclusiveResDto vipExclusiveResDto = BeanConvertUtils.copyProperties(userLevel, VipExclusiveResDto::new);
        vipExclusiveResDto.setScoreUpgrade(scoreUpgrade);
        userLevelRebates.forEach(userGroup -> {
            var groupId = userGroup.getGroupId();
            switch (groupId) {
                case 1:
                    vipExclusiveResDto.setSports(function.apply(userGroup.getRebateRate()));
                    break;
                case 2:
                    vipExclusiveResDto.setEGames(function.apply(userGroup.getRebateRate()));
                    break;
                case 3:
                    vipExclusiveResDto.setLivesGame(function.apply(userGroup.getRebateRate()));
                    break;
                case 4:
                    vipExclusiveResDto.setFinishGame(function.apply(userGroup.getRebateRate()));
                    break;
                case 5:
                    vipExclusiveResDto.setChess(function.apply(userGroup.getRebateRate()));
                    break;
                case 6:
                    vipExclusiveResDto.setESports(function.apply(userGroup.getRebateRate()));
                    break;
                default:
                    break;
            }
        });
        /*
         * 已完成流水统计
         * 1：查询奖金表的时间作为下次查询流水的开始时间。
         * 2：查询用户的有效投注额
         */
        var coinReward = coinRewardsServiceImpl.lambdaQuery()
                .eq(CoinRewards::getUid, userInfo.getId())
                .eq(CoinRewards::getReferId, Constant.VIP_GROW_UP)
                .orderByDesc(CoinRewards::getCreatedAt)
                .last(LIMIT_ONE)
                .one();
        var startTime = coinReward == null ? 0 : coinReward.getCreatedAt();
        var reqDto = PlatFactoryParams.PlatGameQueryDateDto.builder().startTime(startTime).uid(userInfo.getId()).build();
        var coinBet = gameCommonBase.getCoinStatisticsByDate(reqDto).getCoinBet();
        coinBet = coinBet.add(userInfo.getUpgradeBalance());
        vipExclusiveResDto.setCompleteFlow(coinBet);
        //生日信息是否存在，等级不为0级
        if (Objects.nonNull(userInfo.getBirthday()) && userInfo.getLevelId() != 1) {
            var birthday = userInfo.getBirthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            var now = LocalDate.now();
            if (birthday.getMonth().equals(now.getMonth()) && birthday.getDayOfMonth() == now.getDayOfMonth()) {
                //判断用户是否领取生日奖金
                int count = coinRewardsServiceImpl.count(new LambdaQueryWrapper<CoinRewards>()
                        .eq(CoinRewards::getReferId, Constant.VIP_GROW_UP)
                        .eq(CoinRewards::getUid, userInfo.getId())
                        .like(CoinRewards::getInfo, Constant.REWARDS_BIRTHDAY)
                        .ge(CoinRewards::getCreatedAt, (int) now.toEpochSecond(LocalTime.of(0, 0, 0), ZoneOffset.UTC))
                        .le(CoinRewards::getCreatedAt, (int) now.toEpochSecond(LocalTime.of(23, 59, 59), ZoneOffset.UTC))
                );
                if (count == 0) {
                    //1:为可领取状态
                    vipExclusiveResDto.setIsReceiveRewards(1);
                }
            }
        }
        return vipExclusiveResDto;
    }

    /***
     * 优惠活动领取生日礼金
     * 1.获取会员等级信息
     * 2.判断此会员生日可否领取奖金(会员在注册三个月内过生日，今年将不能领取生日礼金。
     * 另注册时间大于三个月的会员需在生日当天的星级页面进行自助领取，每年可领取一次，
     * VIP5或以上可以享受生日礼金。（生日彩金1倍流水即可提款）)
     * 3.奖金入库处理
     * @return
     */
    @Override
    public void birthdayGift() {
        //获取用户信息
        UserInfo userInfo = userInfoServiceImpl.findIdentityByApiToken();
        //等级判断
        if (userInfo.getLevelId() < 1) {
            throw new BusinessException(CodeInfo.ACTIVE_NO_USER_LEVEL);
        }
        UserLevel userLevel = userLevelServiceImpl.getById(userInfo.getLevelId());
        if (userLevel == null) {
            throw new BusinessException(CodeInfo.ACTIVE_NO_USER);
        }
        //注册时间判断
        if (DateUtils.getCurrentTime() - userInfo.getCreatedAt() < 90 * 24 * 3600) {
            throw new BusinessException(CodeInfo.ACTIVE_BIRTHDAY_DATE_EXCEED);
        }
        //判断用户登记了生日日期
        if (userInfo.getBirthday() == null) {
            throw new BusinessException(CodeInfo.ACTIVE_BIRTHDAY_DATE_EMPTY);
        }
        //判断今日是否该用户得生日
        var birthday = userInfo.getBirthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        var now = LocalDate.now();
        if (!birthday.getMonth().equals(now.getMonth()) || birthday.getDayOfMonth() != now.getDayOfMonth()) {
            throw new BusinessException(CodeInfo.ACTIVE_NO_IN_BIRTHDAY_DATE);
        }
        var startTime = (int) now.toEpochSecond(LocalTime.of(0, 0, 0), ZoneOffset.UTC);
        var endTime = (int) now.toEpochSecond(LocalTime.of(23, 59, 59), ZoneOffset.UTC);
        //判断用户是否领取生日奖金
        int count = coinRewardsServiceImpl.count(new LambdaQueryWrapper<CoinRewards>()
                .eq(CoinRewards::getReferId, Constant.VIP_GROW_UP)
                .eq(CoinRewards::getUid, userInfo.getId())
                .like(CoinRewards::getInfo, Constant.REWARDS_BIRTHDAY)
                .ge(CoinRewards::getCreatedAt, startTime)
                .le(CoinRewards::getCreatedAt, endTime)
        );
        if (count > 0) {
            throw new BusinessException(CodeInfo.ACTIVE_LEVEL_GIFT_RECEIVE);
        }
        //生日礼金
        BigDecimal giftCoin = userLevel.getRewardsBirthday();
        //info扩展信息
        var infoJSONObject = new JSONObject();
        var detailsJSONObject = new JSONObject();
        detailsJSONObject.put("level", userLevel.getCode());
        detailsJSONObject.put("payType", Constant.REWARDS_BIRTHDAY);
        detailsJSONObject.put("payCoin", giftCoin);
        infoJSONObject.put("details", detailsJSONObject.toJSONString());
        var applicationActivityReqDto = ApplicationActivityReqDto.builder()
                .id(Constant.VIP_GROW_UP)
                .availableCoin(giftCoin)
                .mosaicCoin(giftCoin)
                .info(infoJSONObject.toJSONString())
                .build();
        promotionsBase.executePromotionsPersistence(applicationActivityReqDto, userInfo.getId());
    }
}
