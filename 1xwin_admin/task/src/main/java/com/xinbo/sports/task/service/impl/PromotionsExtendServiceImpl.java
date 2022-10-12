package com.xinbo.sports.task.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.*;
import com.xinbo.sports.dao.generator.po.AgentCommissionRate;
import com.xinbo.sports.dao.generator.po.CoinCommission;
import com.xinbo.sports.dao.generator.po.User;
import com.xinbo.sports.dao.generator.po.UserProfile;
import com.xinbo.sports.dao.generator.service.CoinCommissionService;
import com.xinbo.sports.dao.generator.service.PromotionsService;
import com.xinbo.sports.dao.generator.service.UserProfileService;
import com.xinbo.sports.dao.generator.service.UserService;
import com.xinbo.sports.dao.generator.service.impl.AgentCommissionRateServiceImpl;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams;
import com.xinbo.sports.service.cache.redis.UserCache;
import com.xinbo.sports.service.common.Constant;
import com.xinbo.sports.service.io.bo.UserCacheBo;
import com.xinbo.sports.task.base.GameCommonBase;
import com.xinbo.sports.utils.DateNewUtils;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

import static com.alibaba.fastjson.JSON.parseObject;

/**
 * @author: wells
 * @date: 2020/6/10
 * @description:
 */
@Slf4j
@Service
public class PromotionsExtendServiceImpl {
    //最新额度
    private static final String MIN_COIN = "minCoin";
    //利润
    private static final String PROFIT = "profit";
    //流水
    private static final String FLOW_CLAIM = "flowClaim";
    @Autowired
    private AgentCommissionRateServiceImpl agentCommissionRateService;
    /**
     * 代理佣金Map
     */
    @SuppressWarnings("UnstableApiUsage")
    private static final RangeMap<Integer, Pair<BigDecimal, BigDecimal>> commissionRangeMap = TreeRangeMap.create();

    /**
     * 代理现金奖励Map
     */
    @SuppressWarnings("UnstableApiUsage")
    private static final RangeMap<Integer, BigDecimal> cashRangeMap = TreeRangeMap.create();
    @Autowired
    private PromotionsService promotionsServiceImpl;
    @Autowired
    private UserProfileService userProfileServiceImpl;
    @Autowired
    private GameCommonBase gameCommonBase;
    @Autowired
    private UserService userServiceImpl;
    @Autowired
    private PersistenceServiceImpl persistenceServiceImpl;
    @Autowired
    private CoinCommissionService coinCommissionServiceImpl;
    @Autowired
    private UserCache userCache;


    @PostConstruct
    @SuppressWarnings("UnstableApiUsage")
    private void putEntity() {
        commissionRangeMap.put(Range.closedOpen(3, 10), Pair.of(BigDecimal.valueOf(1000), BigDecimal.valueOf(0.1)));
        commissionRangeMap.put(Range.closedOpen(10, 20), Pair.of(BigDecimal.valueOf(5000), BigDecimal.valueOf(0.2)));
        commissionRangeMap.put(Range.closedOpen(20, 30), Pair.of(BigDecimal.valueOf(300000), BigDecimal.valueOf(0.3)));
        commissionRangeMap.put(Range.closedOpen(30, 50), Pair.of(BigDecimal.valueOf(800000), BigDecimal.valueOf(0.35)));
        commissionRangeMap.put(Range.closedOpen(50, 80), Pair.of(BigDecimal.valueOf(10000000), BigDecimal.valueOf(0.40)));
        commissionRangeMap.put(Range.atLeast(80), Pair.of(BigDecimal.valueOf(60000000), BigDecimal.valueOf(0.50)));


        cashRangeMap.put(Range.closedOpen(3, 6), BigDecimal.valueOf(38));
        cashRangeMap.put(Range.closedOpen(6, 10), BigDecimal.valueOf(88));
        cashRangeMap.put(Range.closedOpen(10, 15), BigDecimal.valueOf(188));
        cashRangeMap.put(Range.closedOpen(15, 20), BigDecimal.valueOf(388));
        cashRangeMap.put(Range.closedOpen(20, 25), BigDecimal.valueOf(588));
        cashRangeMap.put(Range.closedOpen(25, 30), BigDecimal.valueOf(888));
        cashRangeMap.put(Range.closedOpen(30, 35), BigDecimal.valueOf(1388));
        cashRangeMap.put(Range.closedOpen(35, 40), BigDecimal.valueOf(2088));
        cashRangeMap.put(Range.closedOpen(40, 45), BigDecimal.valueOf(3088));
        cashRangeMap.put(Range.closedOpen(45, 50), BigDecimal.valueOf(4088));
        cashRangeMap.put(Range.atLeast(50), BigDecimal.valueOf(5888));
    }


    /***
     * 获取红利
     * @param coin      最低获取红利的金额
     * @param promotionsId 活动ID
     */
    public Map<String, BigDecimal> getProfitCoin(BigDecimal coin, Integer promotionsId) {
        Map<String, BigDecimal> map = new HashMap<>();
        //获取活动规则
        String info = promotionsServiceImpl.getById(promotionsId).getInfo();
        //流水倍率
        BigDecimal multiple = parseObject(info).getBigDecimal(FLOW_CLAIM);
        JSONArray jsonArray = parseObject(info).getJSONArray("rule");
        jsonArray.sort(Comparator.comparing(x -> ((JSONObject) x).getBigDecimal(MIN_COIN)));
        for (int i = 0; i < jsonArray.size(); i++) {
            BigDecimal startCoin = jsonArray.getJSONObject(i).getBigDecimal(MIN_COIN);
            BigDecimal endCoin = i == jsonArray.size() - 1 ? BigDecimal.valueOf(Long.MAX_VALUE) : jsonArray.getJSONObject(i + 1).getBigDecimal(MIN_COIN);
            //充值金额在活动的哪个奖励范围,获取对应的奖励
            if (coin.compareTo(startCoin) >= 0 && coin.compareTo(endCoin) < 0) {
                BigDecimal profit = jsonArray.getJSONObject(i).getBigDecimal(PROFIT);
                BigDecimal flowClaim = profit.multiply(multiple);
                map.put(PROFIT, profit);
                map.put(FLOW_CLAIM, flowClaim);
                break;
            }
        }
        return map;
    }

    /**
     * 加盟代理佣金与奖励
     */
    public void agentCommissionAndReWard(String flag, int count) {
        //查询所有用户
        List<UserProfile> userProfileList = userProfileServiceImpl.lambdaQuery()
                .eq(UserProfile::getStatus, 10)
                //supUid1为0代表无上级代理
                .ne(UserProfile::getSupUid1, 0)
                .list();
        //Table row对应级别;column对应supUid;值对应uid
        Table<Integer, Integer, Integer> table = HashBasedTable.create();
        //调用自定义函数组装数据
        TripleConsumer tableConsumer = (level, supUId, uid) -> {
            if (supUId != 0) {
                table.put(level, uid, supUId);
            }
        };
        userProfileList.forEach(userProfile -> {
            tableConsumer.accept(1, userProfile.getSupUid1(), userProfile.getUid());
            tableConsumer.accept(2, userProfile.getSupUid2(), userProfile.getUid());
            tableConsumer.accept(3, userProfile.getSupUid3(), userProfile.getUid());
            tableConsumer.accept(4, userProfile.getSupUid4(), userProfile.getUid());
            tableConsumer.accept(5, userProfile.getSupUid5(), userProfile.getUid());
            tableConsumer.accept(6, userProfile.getSupUid6(), userProfile.getUid());
        });
        Map<Integer, BigDecimal> collect = agentCommissionRateService.list().stream().collect(Collectors.toMap(AgentCommissionRate::getAgentLevel, AgentCommissionRate::getAgentLevelRate));
        //id查询用户名
        Map<Integer, BigDecimal> userIdToCoinMap = userServiceImpl.list().stream()
                .collect(Collectors.toMap(User::getId, User::getCoin));
        table.rowMap().forEach((level, userMap) -> {
            //只计算返佣比例大于0
            if (collect.get(level).compareTo(BigDecimal.ZERO) > 0) {
                HashMultimap<Integer, Integer> hashMultimap = HashMultimap.create();
                userMap.forEach((uid, supUid) -> hashMultimap.put(supUid, uid));
                for (Integer uid : hashMultimap.keySet()) {
                    if (Objects.isNull(userCache.getUserInfoById(uid))) {
                        continue;
                    }
                    if (Constant.AGENT.equals(flag)) {
                        agentPersistence(uid, hashMultimap.get(uid), level, userIdToCoinMap.get(uid), count);
                    } else if (Constant.FLOW.equals(flag)) {
                        XxlJobLogger.log("level=" + level, "uid=" + uid, "下级uid=" + hashMultimap.get(uid));
                        flowCommission(uid, hashMultimap.get(uid), collect.get(level), level, userIdToCoinMap.get(uid), count);
                    }
                }
            }
        });
    }


    /**
     * 代理入库处理
     *
     * @param uid        用户ID
     * @param set        用户集
     * @param level      等级
     * @param coinBefore 交易前金额
     * @param count      月数
     */
    @SuppressWarnings("UnstableApiUsage")
    private void agentPersistence(Integer uid, Set<Integer> set, int level, BigDecimal coinBefore, int count) {
        for (int i = 1; i <= count; i++) {
            //当前日期年月日
            var zoneTime = DateNewUtils.utc8Zoned(DateNewUtils.now()).minusMonths(i);
            var yearMonth = DateNewUtils.utc8Str(zoneTime, DateNewUtils.Format.yyyyMMdd).substring(0, 6);
            //判断是否领取佣金
            var commissionCount = coinCommissionServiceImpl.lambdaQuery()
                    .eq(CoinCommission::getRiqi, yearMonth)
                    .count();
            if (commissionCount > 0) {
                XxlJobLogger.log("代理佣金已领取！");
                return;
            }
            //前一天的时间
            ZonedDateTime zoneNow = DateNewUtils.utc8Zoned(DateNewUtils.now());
            //前一个月的开始与结束时间
            var firstDay = zoneNow.minusMonths(i).with(TemporalAdjusters.firstDayOfMonth());
            var lastDay = zoneNow.minusMonths(i - 1).with(TemporalAdjusters.firstDayOfMonth());
            int agentStartTime = (int) firstDay.withHour(0).withMinute(0).withSecond(0).withNano(0).toEpochSecond();
            int agentEndTime = (int) lastDay.withHour(0).withMinute(0).withSecond(0).withNano(0).toEpochSecond();
            //获取新增用户集合
            Set<Integer> newcomerSet = userServiceImpl.lambdaQuery().le(User::getCreatedAt, agentEndTime).ge(User::getCreatedAt, agentStartTime)
                    .list().stream().map(User::getId)
                    .collect(Collectors.toSet());
            //邀请好友奖励详情表
            List<CoinCommission> commList = new ArrayList<>();
            var platGameQueryDateDto = new PlatFactoryParams.PlatGameQueryDateDto();
            platGameQueryDateDto.setStartTime(agentStartTime);
            platGameQueryDateDto.setEndTime(agentEndTime);
            var userBetCoinMap = gameCommonBase.getUserBetCoin(platGameQueryDateDto, set);
            XxlJobLogger.log("new user=" + newcomerSet + ";user coin= " + userBetCoinMap);

            //判断用户是否为活跃用户(一个月有效投注额达到5000)
            var betActiveList = userBetCoinMap.keySet().stream().filter(x ->
                    userBetCoinMap.get(x).compareTo(BigDecimal.valueOf(5000)) >= 0
            ).collect(Collectors.toList());
            //活跃会员佣金或者满额人头彩金只计算直属下级，level为1是直属下级
            if (level == 1) {
                var betUserCount = betActiveList.size();
                for (var inUid : betActiveList) {
                    if (newcomerSet.contains(inUid)) {
                        commList.add(addCoinCommission(uid, level, BigDecimal.valueOf(100), userBetCoinMap.get(inUid),
                                String.valueOf(inUid), 2, coinBefore));
                    }
                }
                if (betUserCount >= 3) {
                    //人数佣金处理
                    var cashCoin = cashRangeMap.get(betUserCount);
                    commList.add(addCoinCommission(uid, level, cashCoin, BigDecimal.ZERO,
                            StringUtils.join(betActiveList, ","), 1, coinBefore));
                }
            }
            persistenceServiceImpl.agentCommissionPersistence(commList);
        }
    }


    /**
     * 流水佣金计算
     *
     * @param uid            用户ID
     * @param set            用户集
     * @param agentLevelRate 佣金比例
     * @param level          等级
     * @param coinBefore     交易前金额
     * @param count          天数
     */
    public void flowCommission(Integer uid, Set<Integer> set, BigDecimal agentLevelRate, int level, BigDecimal coinBefore, int count) {
        for (int i = 1; i <= count; i++) {
            //当前日期年月日
            var zoneTime = DateNewUtils.utc8Zoned(DateNewUtils.now()).minusDays(i);
            var yearMonth = DateNewUtils.utc8Str(zoneTime, DateNewUtils.Format.yyyyMMdd);
            //判断是否领取佣金
            var commissionCount = coinCommissionServiceImpl.lambdaQuery()
                    .eq(CoinCommission::getAgentLevel, level)
                    .eq(CoinCommission::getUid, uid)
                    .eq(CoinCommission::getRiqi, Integer.parseInt(yearMonth))
                    .eq(CoinCommission::getCategory, 0)
                    .count();
            if (commissionCount > 0) {
                XxlJobLogger.log("uid=" + uid + "下级uid=" + set + "流水佣金已领取！");
                continue;
            }
            //前一天的时间
            ZonedDateTime zoneNow = DateNewUtils.utc8Zoned(DateNewUtils.now());
            int agentStartTime = (int) zoneNow.minusDays(i).withHour(0).withMinute(0).withSecond(0).withNano(0).toEpochSecond();
            int agentEndTime = (int) zoneNow.minusDays(i - 1).withHour(0).withMinute(0).withSecond(0).withNano(0).toEpochSecond();
            var platGameQueryDateDto = new PlatFactoryParams.PlatGameQueryDateDto();
            platGameQueryDateDto.setStartTime(agentStartTime);
            platGameQueryDateDto.setEndTime(agentEndTime);
            var userBetCoinMap = gameCommonBase.getUserBetCoin(platGameQueryDateDto, set);
            //获取有多少用户
            var betList = new ArrayList<>(userBetCoinMap.keySet());
            if (CollectionUtils.isEmpty(betList)) {
                continue;
            }
            XxlJobLogger.log("日期=" + yearMonth + "uid=" + uid + "下级uid=" + set + "获取流水" + platGameQueryDateDto);
            List<CoinCommission> commList = new ArrayList<>();
            betList.forEach(inUid -> {
                        //流水佣金处理
                        var commission = addCoinCommission(uid, level, userBetCoinMap.get(inUid).multiply(agentLevelRate)
                                        .setScale(2, RoundingMode.DOWN), userBetCoinMap.get(inUid), inUid.toString(),
                                0, coinBefore);
                        //状态:0-未发放 1-已发放
                        commission.setStatus(0);
                        commission.setRate(agentLevelRate);
                        commission.setRiqi(Integer.parseInt(yearMonth));
                        commList.add(commission);
                    }
            );
            //佣金记录入库处理
            coinCommissionServiceImpl.saveBatch(commList);
        }
    }

    /**
     * @param uid            uid
     * @param level          代理级别
     * @param coin           金额
     * @param subBetTrunover 下级流水总额
     * @param subUids        下级UIDS
     * @param category       类型
     */
    private CoinCommission addCoinCommission(Integer uid, int level, BigDecimal coin, BigDecimal subBetTrunover,
                                             String subUids, int category, BigDecimal coinBefore) {
        //上一个月
        var zoneTime = DateNewUtils.utc8Zoned(DateNewUtils.now()).minusMonths(1);
        var yearMonth = DateNewUtils.utc8Str(zoneTime, DateNewUtils.Format.yyyyMMdd);
        //类型:0-流水佣金 1-活跃会员佣金 2-满额人头彩金
        if (category != 0) {
            yearMonth = yearMonth.substring(0, 6);
        }
        var username = Optional.ofNullable(userCache.getUserInfoById(uid)).map(UserCacheBo.UserCacheInfo::getUsername).orElse("");
        var now = DateNewUtils.now();
        CoinCommission coinCommission = new CoinCommission();
        coinCommission.setUid(uid);
        coinCommission.setUsername(username);
        coinCommission.setRiqi(Integer.valueOf(yearMonth));
        coinCommission.setAgentLevel(level);
        coinCommission.setCoin(coin);
        coinCommission.setSubUids(subUids);
        coinCommission.setSubBetTrunover(subBetTrunover);
        coinCommission.setRate(BigDecimal.ZERO);
        coinCommission.setCoinBefore(coinBefore);
        // 类型：0：流水佣金，1：人数佣金
        coinCommission.setCategory(category);
        coinCommission.setCreatedAt(now);
        coinCommission.setUpdatedAt(now);
        return coinCommission;
    }
}

/**
 * 自定义函数
 */
@FunctionalInterface
interface TripleConsumer {
    void accept(int var1, int var2, int var3);
}
