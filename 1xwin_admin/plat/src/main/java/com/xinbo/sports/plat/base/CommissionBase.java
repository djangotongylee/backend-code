package com.xinbo.sports.plat.base;

import com.xinbo.sports.dao.generator.po.*;
import com.xinbo.sports.dao.generator.service.*;
import com.xinbo.sports.plat.io.enums.FuturesLotteryRoutineParam;
import com.xinbo.sports.utils.DateNewUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.ObjIntConsumer;
import java.util.stream.Collectors;

/**
 * @Author : Wells
 * @Date : 2020/10/13 12:10 上午
 * @Description : futures彩票计算代理金额
 **/
@Component
public class CommissionBase {
    @Resource
    private AgentCommissionRateService agentCommissionRateServiceImpl;
    @Resource
    private UserProfileService userProfileServiceImpl;
    @Resource
    private CoinCommissionService coinCommissionServiceImpl;
    @Resource
    private UserService userServiceImpl;
    @Resource
    private CoinLogService coinLogServiceImpl;

    /**
     * Futures彩票佣金计算
     *
     * @Return void :
     **/
    public void commissionFutures(List<BetslipsFuturesLottery> futuresLottery) {
        var userProfileMap = userProfileServiceImpl.list().stream().collect(Collectors.toMap(UserProfile::getUid, v1 -> v1));
        Map<Integer, BigDecimal> commissionRateMap = agentCommissionRateServiceImpl.list().stream()
                .collect(Collectors.toMap(AgentCommissionRate::getAgentLevel, AgentCommissionRate::getAgentLevelRate));
        var commissionList = new ArrayList<CoinCommission>();
        //id查询用户名
        Map<Integer, BigDecimal> userIdToCoinMap = userServiceImpl.list().stream().collect(Collectors.toMap(User::getId, User::getCoin));
        futuresLottery.forEach(futures -> {
            if (!(FuturesLotteryRoutineParam.FuturesLotteryStatus.WAIT.getCode().equals(futures.getStatus()) ||
                    FuturesLotteryRoutineParam.FuturesLotteryStatus.CANCEL.getCode().equals(futures.getStatus()))) {
                var userProfile = userProfileMap.get(futures.getXbUid());
                ObjIntConsumer<Integer> biConsumer = (supUid, levelId) -> {
                    var rate = commissionRateMap.get(levelId);
                    if (rate.compareTo(BigDecimal.ZERO) > 0 && supUid != 0) {
                        var supUid1Coin = futures.getCoinFee().multiply(rate);
                        var coinBefore = userIdToCoinMap.get(supUid);
                        var coinCommission = addCoinCommission(supUid, levelId, supUid1Coin, futures.getCoinFee(), String.valueOf(futures.getXbUid()), rate, coinBefore);
                        commissionList.add(coinCommission);
                    }
                };
                if (Objects.nonNull(userProfile)) {
                    biConsumer.accept(userProfile.getSupUid1(), 1);
                    biConsumer.accept(userProfile.getSupUid2(), 2);
                    biConsumer.accept(userProfile.getSupUid3(), 3);
                    biConsumer.accept(userProfile.getSupUid4(), 4);
                    biConsumer.accept(userProfile.getSupUid5(), 5);
                    biConsumer.accept(userProfile.getSupUid6(), 6);
                }
            }
        });
        if (!CollectionUtils.isEmpty(commissionList)) {
            Map<Integer, List<CoinCommission>> collect = commissionList.stream().collect(Collectors.groupingBy(CoinCommission::getUid));
            var now = DateNewUtils.now();
            coinCommissionServiceImpl.saveBatch(commissionList);
            var coinLogList = new ArrayList<CoinLog>();
            commissionList.forEach(coinCommission -> {
                //日志入库
                CoinLog coinLog = new CoinLog();
                coinLog.setUid(coinCommission.getUid());
                coinLog.setReferId(coinCommission.getId());
                coinLog.setSubCategory(0);
                //活动类型
                coinLog.setCategory(6);
                coinLog.setCoin(coinCommission.getCoin());
                coinLog.setCoinBefore(coinCommission.getCoinBefore());
                coinLog.setStatus(1);
                coinLog.setCreatedAt(now);
                coinLog.setUpdatedAt(now);
                coinLogList.add(coinLog);
            });
            coinLogServiceImpl.saveBatch(coinLogList);
            collect.forEach((uid, list) -> {
                var value = list.stream().map(CoinCommission::getCoin).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
                userServiceImpl.lambdaUpdate().setSql("coin = coin+" + value).eq(User::getId, uid).update();
            });
        }
    }

    /**
     * @param uid            uid
     * @param level          代理级别
     * @param coin           金额
     * @param subBetTrunover 下级流水总额
     * @param subUids        下级UIDS
     * @param agentLevelRate 佣金比例
     */
    private CoinCommission addCoinCommission(Integer uid, int level, BigDecimal coin, BigDecimal subBetTrunover,
                                             String subUids, BigDecimal agentLevelRate, BigDecimal coinBefore) {
        //当月
        var zoneTime = DateNewUtils.utc8Zoned(DateNewUtils.now());
        var yearMonth = DateNewUtils.utc8Str(zoneTime, DateNewUtils.Format.yyyyMMdd).substring(0, 6);
        var now = DateNewUtils.now();
        CoinCommission coinCommission = new CoinCommission();
        coinCommission.setUid(uid);
        coinCommission.setRiqi(Integer.valueOf(yearMonth));
        coinCommission.setAgentLevel(level);
        coinCommission.setCoin(coin);
        coinCommission.setSubUids(subUids);
        coinCommission.setSubBetTrunover(subBetTrunover);
        coinCommission.setRate(agentLevelRate);
        coinCommission.setCoinBefore(coinBefore);
        // 类型：0：流水佣金，1：人数佣金
        coinCommission.setCategory(0);
        coinCommission.setCreatedAt(now);
        coinCommission.setUpdatedAt(now);
        return coinCommission;
    }
}
