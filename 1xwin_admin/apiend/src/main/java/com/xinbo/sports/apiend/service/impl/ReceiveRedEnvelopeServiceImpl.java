package com.xinbo.sports.apiend.service.impl;

import com.xinbo.sports.apiend.io.dto.promotions.ReceiveEnvelopeResDto;
import com.xinbo.sports.apiend.io.dto.promotions.ReceiveResDto;
import com.xinbo.sports.apiend.service.ReceiveRedEnvelopeService;
import com.xinbo.sports.dao.generator.po.CoinRewards;
import com.xinbo.sports.dao.generator.po.Promotions;
import com.xinbo.sports.dao.generator.service.CoinRewardsService;
import com.xinbo.sports.service.base.GameStatisticsBase;
import com.xinbo.sports.service.base.PromotionsBase;
import com.xinbo.sports.service.cache.KeyConstant;
import com.xinbo.sports.service.cache.redis.PromotionsCache;
import com.xinbo.sports.service.common.Constant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.bo.PlatServiceBaseParams;
import com.xinbo.sports.service.io.dto.BaseParams;
import com.xinbo.sports.service.io.dto.promotions.ApplicationActivityReqDto;
import com.xinbo.sports.service.thread.ThreadHeaderLocalData;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.JedisUtil;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;
import java.util.stream.IntStream;

import static com.alibaba.fastjson.JSON.parseObject;

/**
 * @Author : Wells
 * @Date : 2021-01-13 12:06 上午
 * @Description : 红包领取
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReceiveRedEnvelopeServiceImpl implements ReceiveRedEnvelopeService {
    //红包生成规则
    private static final String GENERATE_RULE = "generateRule";
    //红包领取规则
    private static final String RECEIVE_RULE = "receiveRule";
    //要求的流水金额
    private static final String FLOW_COIN = "flowCoin";
    //流水倍数
    private static final String FLOW_CLAIM = "flowClaim";
    //红包领取次数
    private static final String RECEIVE_COUNT = "receiveCount";
    //是否设置金额
    private static final String IS_SET = "isSet";
    //不设置金额
    private static final Integer NO_SET_COIN = 0;
    //设置金额
    private static final Integer SET_COIN = 1;
    //红包总个数
    private static final String COUNT = "count";
    //红包总金额
    private static final String TOTAL_COIN = "totalCoin";
    //单个红包最小金额
    private static final String SINGLE_MIN_COIN = "singleMinCoin";
    //单个红包最小中奖率
    private static final String SINGLE_MIN_RATE = "singleMinRate";
    //单个红包最大金额
    private static final String SINGLE_MAX_COIN = "singleMaxCoin";
    //单个红包最大中奖率
    private static final String SINGLE_MAX_RATE = "singleMaxRate";
    //生成金额区间集合
    private static final String RANG_LIST = "rangList";
    //区间最大值
    private static final String MAX = "max";
    //区间最小值
    private static final String MIN = "min";
    //区间概率
    private static final String RATE = "rate";

    private final PromotionsCache promotionsCache;
    private final JedisUtil jedisUtil;
    private final CoinRewardsService coinRewardsServiceImpl;
    private final GameStatisticsBase gameStatisticsBase;
    private final PromotionsBase promotionsBase;


    /**
     * 红包领取
     *
     * @return ReceiveEnvelopeDto
     */
    @Override
    public ReceiveEnvelopeResDto receiveRedEnvelope(ReceiveResDto reqDto) {
        //获取用户信息
        BaseParams.HeaderInfo currentLoginUser = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        var uid = currentLoginUser.getId();
        var promotions = getRule();
        var now = DateNewUtils.now();
        ZonedDateTime zoneNow = DateNewUtils.utc8Zoned(now);
        int startTime = (int) zoneNow.withHour(0).withMinute(0).withSecond(0).withNano(0).toEpochSecond();
        //判断是否在活动开启时间范围内
        if (promotions.getStatus().equals(Constant.FAIL) || promotions.getStartedAt() > now || promotions.getEndedAt() < now) {
            throw new BusinessException(CodeInfo.ACTIVE_CLOSE);
        }
        var receiveRuleArray = parseObject(getRule().getInfo()).getJSONArray(RECEIVE_RULE);
        var flowClaim = parseObject(getRule().getInfo()).getBigDecimal(FLOW_CLAIM);
        //计算流水获取能领取的次数
        var patGameQueryDateDto = PlatServiceBaseParams.PlatGameQueryDateDto.builder()
                .startTime(startTime)
                .endTime(DateNewUtils.now())
                .uid(uid)
                .build();
        var realFlowCoin = gameStatisticsBase.getCoinStatisticsByDate(patGameQueryDateDto).getCoinBet();
        var canCount = 0;
        var maxCount = 0;
        for (var i = 0; i < receiveRuleArray.size(); i++) {
            var receiveRuleJsonObject = receiveRuleArray.getJSONObject(i);
            var nextFlowCoin = BigDecimal.valueOf(Integer.MAX_VALUE);
            if (i != receiveRuleArray.size() - 1) {
                var nextReceiveRuleJsonObject = receiveRuleArray.getJSONObject(i + 1);
                nextFlowCoin = nextReceiveRuleJsonObject.getBigDecimal(FLOW_COIN);
            }
            var flowCoin = receiveRuleJsonObject.getBigDecimal(FLOW_COIN);
            if (realFlowCoin.compareTo(flowCoin) >= 0 && realFlowCoin.compareTo(nextFlowCoin) < 0) {
                canCount = receiveRuleJsonObject.getInteger(RECEIVE_COUNT);
            }
            if (i == receiveRuleArray.size() - 1) {
                maxCount = receiveRuleJsonObject.getInteger(RECEIVE_COUNT);
            }
        }

        if (canCount == 0) {
            throw new BusinessException(CodeInfo.RED_ENVELOPE_FLOW_NO_ENOUGH);
        }
        //用户的每天领取次数
        var userCount = coinRewardsServiceImpl.lambdaQuery()
                .eq(CoinRewards::getUid, uid)
                .eq(CoinRewards::getReferId, promotions.getId())
                .ge(CoinRewards::getCreatedAt, startTime)
                .le(CoinRewards::getCreatedAt, now)
                .count();
        if (userCount.equals(maxCount)) {
            throw new BusinessException(CodeInfo.RED_ENVELOPE_NUMBER_FINISH);
        }
        //实际领取大于等于可领次数抛出异常
        if (userCount >= canCount) {
            throw new BusinessException(CodeInfo.ACTIVE_NO_ENVELOPE_COUNT);
        }
        var resDto = new ReceiveEnvelopeResDto();
        if (Constant.IS_RECEIVE.equals(reqDto.getIsReceive())) {
            //领取红包金额
            var redEnvelopeCoin = getRedEnvelopeCoin();
            if (realFlowCoin.compareTo(BigDecimal.ZERO) > 0) {
                var mosaicCoin = redEnvelopeCoin.multiply(flowClaim);
                //持久化活动记录及修改用户金额
                var appReqDto = ApplicationActivityReqDto.builder()
                        .id(promotions.getId())
                        .availableCoin(redEnvelopeCoin)
                        .mosaicCoin(mosaicCoin)
                        .build();
                promotionsBase.executePromotionsPersistence(appReqDto, uid);
            } else {
                //添加奖金记录表
                promotionsBase.saveRedEnvelopeCoinRewards(uid, promotions.getId());
            }
            resDto.setCoin(redEnvelopeCoin);
        }
        resDto.setTimes(canCount - userCount - 1);
        return resDto;
    }

    /**
     * 获取红包金额
     *
     * @return BigDecimal
     */
    public BigDecimal getRedEnvelopeCoin() {
        var coinStr = jedisUtil.rightPop(KeyConstant.RED_ENVELOPE_HASH);
        //无缓存记录则生成记录
        if (StringUtils.isEmpty(coinStr)) {
            initRedEnvelope();
            coinStr = jedisUtil.rightPop(KeyConstant.RED_ENVELOPE_HASH);
        }
        return new BigDecimal(coinStr);
    }


    /**
     * 生成红包存入缓存
     */
    public void generateRedEnvelope(int size, BigDecimal receiveCoin) {
        var coinList = new LinkedList<String>();
        var generateRule = parseObject(getRule().getInfo()).getJSONObject(GENERATE_RULE);
        //是否设置总金额；0-不设置，1-设置
        var isSet = generateRule.getInteger(IS_SET);
        var count = generateRule.getInteger(COUNT) - size;
        var totalCoin = generateRule.getBigDecimal(TOTAL_COIN).subtract(receiveCoin);
        var singleMinCoin = generateRule.getBigDecimal(SINGLE_MIN_COIN).setScale(2, RoundingMode.DOWN);
        var singleMinRate = generateRule.getBigDecimal(SINGLE_MIN_RATE);
        var singleMaxCoin = generateRule.getBigDecimal(SINGLE_MAX_COIN).setScale(2, RoundingMode.DOWN);
        var singleMaxRate = generateRule.getBigDecimal(SINGLE_MAX_RATE);
        var rangList = generateRule.getJSONArray(RANG_LIST);
        var singleMinCount = singleMinRate.multiply(BigDecimal.valueOf(count)).intValue();
        var singleMaxCount = singleMaxRate.multiply(BigDecimal.valueOf(count)).intValue();
        IntStream.rangeClosed(1, singleMinCount).forEach(i -> coinList.add(singleMinCoin.toString()));
        IntStream.rangeClosed(1, singleMaxCount).forEach(i -> coinList.add(singleMaxCoin.toString()));
        //红包总额减去最大红包与最小红包的占用金额
        totalCoin = totalCoin.subtract(singleMinCoin.multiply(new BigDecimal(singleMinCount))
                .add(singleMaxCoin.multiply(new BigDecimal(singleMaxCount))));
        //剩余次数
        var remainingCount = 0;
        if (SET_COIN.equals(isSet)) {
            remainingCount = count - singleMinCount - singleMaxCount;
            for (int i = 0; i < remainingCount; i++) {
                var randomRate = BigDecimal.valueOf(new Random().nextInt(10) + 1L).divide(BigDecimal.valueOf(10), 1, RoundingMode.DOWN);
                var setMaxCoin = singleMaxCoin.multiply(randomRate);
                //留取剩余红包最小总和金额
                var remainingCoin = singleMinCoin.multiply(new BigDecimal(remainingCount - i - 1));
                totalCoin = totalCoin.subtract(remainingCoin);
                setMaxCoin = totalCoin.compareTo(setMaxCoin) > 0 ? setMaxCoin : totalCoin;
                var coin = BigDecimal.valueOf(Math.random() * (setMaxCoin.subtract(singleMinCoin).intValue()) + 1).add(singleMinCoin)
                        .setScale(2, RoundingMode.DOWN);
                if (i != remainingCount - 1 && totalCoin.subtract(coin).compareTo(remainingCoin) < 0) {
                    IntStream.rangeClosed(1, remainingCount - i - 1).forEach(index -> coinList.add(singleMinCoin.toString()));
                    break;
                }
                if (i == remainingCount - 1) {
                    coin = totalCoin;
                }
                totalCoin = totalCoin.subtract(coin).add(remainingCoin);
                coinList.add(coin.toString());
            }
        }

        if (NO_SET_COIN.equals(isSet)) {
            for (int i = rangList.size() - 1; i >= 0; i--) {
                var jsonObject = rangList.getJSONObject(i);
                var maxCoin = jsonObject.getBigDecimal(MAX);
                var minCoin = jsonObject.getBigDecimal(MIN);
                var rate = jsonObject.getBigDecimal(RATE);
                var maxIndex = rate.multiply(BigDecimal.valueOf(count)).intValue();
                if (i == 1) {
                    remainingCount = remainingCount - maxIndex;
                }
                IntStream.rangeClosed(1, maxIndex).forEach(index -> {
                    var coin = BigDecimal.valueOf(Math.random() * (maxCoin.subtract(minCoin).intValue()) + 1).add(minCoin)
                            .setScale(2, RoundingMode.DOWN);
                    coinList.add(coin.toString());
                });
            }
        }
        Collections.shuffle(coinList);
        coinList.parallelStream().forEach(coin -> jedisUtil.leftPush(KeyConstant.RED_ENVELOPE_HASH, coin));
    }

    /**
     * 获取活动规则
     *
     * @return JSONObject
     */
    public Promotions getRule() {
        var promotionsOptional = promotionsCache.getPromotionsListCache().parallelStream()
                .filter(promotions -> promotions.getCode().equals(Constant.RED_ENVELOPE))
                .findFirst();
        if (promotionsOptional.isEmpty()) {
            throw new BusinessException(CodeInfo.ACTIVE_CODE_REPEAT);
        }
        //获取红包的生成规则
        return promotionsOptional.get();
    }

    /**
     * 初始化生成红包
     */
    @PostConstruct
    public void initRedEnvelope() {
        var promotionsOptional = promotionsCache.getPromotionsListCache().parallelStream()
                .filter(promotions -> promotions.getCode().equals(Constant.RED_ENVELOPE))
                .findFirst();
        if (promotionsOptional.isPresent()) {
            var promotions = promotionsOptional.get();
            var now = DateNewUtils.now();
            //判断是否在活动开启时间范围内
            if (promotions.getStatus().equals(Constant.SUCCESS) && promotions.getStartedAt() <= now && promotions.getEndedAt() >= now) {
                jedisUtil.del(KeyConstant.RED_ENVELOPE_HASH);
                //查询红包记录，减去领取的红包个数与金额
                var promotionId = getRule().getId();
                var coinRewardsList = coinRewardsServiceImpl.lambdaQuery()
                        .eq(CoinRewards::getReferId, promotionId)
                        .list();
                var receiveCoin = coinRewardsList.parallelStream().map(CoinRewards::getCoin)
                        .reduce(BigDecimal::add)
                        .orElse(BigDecimal.ZERO);
                generateRedEnvelope(coinRewardsList.size(), receiveCoin);
            }
        }
    }
}
