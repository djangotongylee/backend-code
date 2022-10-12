package com.xinbo.sports.apiend.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinbo.sports.apiend.base.GameCommonBase;
import com.xinbo.sports.apiend.service.IUserInfoService;
import com.xinbo.sports.dao.generator.po.CoinDeposit;
import com.xinbo.sports.dao.generator.po.CoinRewards;
import com.xinbo.sports.dao.generator.po.GameList;
import com.xinbo.sports.dao.generator.po.Promotions;
import com.xinbo.sports.dao.generator.service.CoinDepositService;
import com.xinbo.sports.dao.generator.service.CoinRewardsService;
import com.xinbo.sports.dao.generator.service.GameListService;
import com.xinbo.sports.dao.generator.service.PromotionsService;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams;
import com.xinbo.sports.service.base.PromotionsBase;
import com.xinbo.sports.service.common.Constant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.dto.UserInfo;
import com.xinbo.sports.service.io.dto.promotions.ApplicationActivityReqDto;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.DateUtils;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.xinbo.sports.service.common.Constant.DAILY_FIRST_DEPOSIT;
import static java.util.Objects.nonNull;

/**
 * @author: wells
 * @date: 2020/6/10
 * @description:
 */
@Slf4j
@Service
public class PromotionsExtendServiceImpl {
    //红利
    public static final String PROFIT = "profit";
    //流水
    public static final String FLOW_CLAIM = "flowClaim";
    //领取奖励的最小标准
    public static final String MIN_COIN = "minCoin";
    //获取第一条
    public static final String LIMIT_ONE = "limit 1";
    @Resource
    IUserInfoService userInfoServiceImpl;
    @Autowired
    private PromotionsService promotionsServiceImpl;
    @Autowired
    private CoinRewardsService coinRewardsServiceImpl;
    @Autowired
    private GameListService gameListServiceImpl;
    @Autowired
    private CoinDepositService coinDepositServiceImpl;
    @Autowired
    private PromotionsBase promotionsBase;
    @Autowired
    private GameCommonBase gameCommonBase;
    @Autowired
    private IPromotionsServiceImpl iPromotionsServiceImpl;


    /**
     * 新赛季回归计算平台盈利
     *
     * @param startTime 开始时间
     * @return 盈利金额
     */
    public BigDecimal getPlatProfit(Integer startTime) {
        //获取用户信息
        UserInfo userInfo = userInfoServiceImpl.findIdentityByApiToken();
        //计算体育的赛季盈利
        var gameLists = gameListServiceImpl.lambdaQuery()
                //1：体育赛事
                .eq(GameList::getGroupId, 1)
                .eq(GameList::getStatus, 1)
                .list();
        BigDecimal betTotalCoin = BigDecimal.ZERO;
        for (GameList gameList : gameLists) {
            var platGameQueryDateDto = PlatFactoryParams.PlatGameQueryDateDto.builder()
                    .uid(userInfo.getId())
                    .gameId(gameList.getId())
                    .endTime(startTime).build();
            var coinProfit = gameCommonBase.getCoinStatisticsByDate(platGameQueryDateDto).getCoinProfit();
            betTotalCoin = betTotalCoin.add(coinProfit);
        }
        return betTotalCoin;
    }

    /***
     * 获取红利
     * @param coin      最低获取红利的金额
     * @param promotionsId 活动ID
     * @return 用户对应的红利
     */
    public Map<String, BigDecimal> getProfitCoin(BigDecimal coin, Integer promotionsId) {
        Map<String, BigDecimal> map = new HashMap<>();
        //获取活动规则
        String info = promotionsServiceImpl.getById(promotionsId).getInfo();
        JSONArray jsonArray = parseObject(info).getJSONArray("rule");
        //流水倍率
        BigDecimal multiple = parseObject(info).getBigDecimal(FLOW_CLAIM);
        jsonArray.sort(Comparator.comparing(x -> ((JSONObject) x).getBigDecimal(MIN_COIN)));
        for (int i = 0; i < jsonArray.size(); i++) {
            BigDecimal startCoin = jsonArray.getJSONObject(i).getBigDecimal(MIN_COIN);
            BigDecimal endCoin = i == jsonArray.size() - 1 ? BigDecimal.valueOf(Long.MAX_VALUE) : jsonArray.getJSONObject(i + 1).getBigDecimal(MIN_COIN);
            //充值金额在活动的哪个奖励范围,获取对应的奖励
            if (coin.compareTo(startCoin) >= 0 && coin.compareTo(endCoin) < 0) {
                BigDecimal flowClaim;
                BigDecimal profit = jsonArray.getJSONObject(i).getBigDecimal(PROFIT);
                if (promotionsId.equals(Constant.NEW_SEASON_COMEBACK)) {
                    flowClaim = profit.multiply(multiple);
                } else {
                    flowClaim = (coin.add(profit)).multiply(multiple);
                }
                map.put(PROFIT, profit);
                map.put(FLOW_CLAIM, flowClaim);
                break;
            }
        }
        return map;
    }

    /**
     * 首充与超高红利申请奖金
     *
     * @param reqDto 请求实体类
     */
    public void firstDeposit(ApplicationActivityReqDto reqDto) {
        //判断可领取的金额
        if (reqDto.getAvailableCoin().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(CodeInfo.ACTIVE_NO_GIFT_COIN);
        }
        //获取用户信息
        var userInfo = userInfoServiceImpl.findIdentityByApiToken();
        var promotions = promotionsServiceImpl.lambdaQuery().eq(Promotions::getId, reqDto.getId()).one();
        //判断彩金是否领取
        promotionsBase.isPromotionsEffective(reqDto.getId(), userInfo.getId());
        CoinDeposit coinDeposit;
        if (DAILY_FIRST_DEPOSIT.equals(reqDto.getCode())) {
            var zoneNow = DateNewUtils.utc8Zoned(DateNewUtils.now());
            int startTime = (int) zoneNow.withHour(0).withMinute(0).withSecond(0).withNano(0).toEpochSecond();
            int endTime = (int) zoneNow.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0).toEpochSecond();
            var count = coinRewardsServiceImpl.lambdaQuery()
                    .eq(CoinRewards::getUid, userInfo.getId())
                    .eq(CoinRewards::getReferId, reqDto.getId())
                    .ge(CoinRewards::getUpdatedAt, startTime)
                    .le(CoinRewards::getUpdatedAt, endTime)
                    .count();
            if (count > 0) {
                throw new BusinessException(CodeInfo.ACTIVE_COIN_ALREADY_RECEIVE);
            }
            coinDeposit = iPromotionsServiceImpl.getDailyFirstDeposit();
        } else {
            //判断手机号码
            if (StringUtils.isEmpty(userInfo.getMobile())) {
                throw new BusinessException(CodeInfo.PICTURE_NEED_BIND_PHONE);
            }
            coinDeposit = coinDepositServiceImpl.getOne(new LambdaQueryWrapper<CoinDeposit>()
                    .eq(CoinDeposit::getUid, userInfo.getId())
                    .in(CoinDeposit::getStatus, IPromotionsServiceImpl.DEPOSIT_STATUS)
                    .orderByAsc(CoinDeposit::getCreatedAt)
                    .last(LIMIT_ONE));
        }
        if (Objects.isNull(coinDeposit)) {
            throw new BusinessException(CodeInfo.ACTIVE_NEW_SEASON_NO_RECORD);
        }
        if (DAILY_FIRST_DEPOSIT.equals(reqDto.getCode())) {
            promotions.setStartedAt(coinDeposit.getCreatedAt());
            promotions.setEndedAt(DateNewUtils.now());
        }
        //是否活在动期间进行了投注
        isBet(userInfo.getId(), promotions, null);
        //获取奖励表的扩展信息
        var jsonObject = new JSONObject();
        jsonObject.put("depositCoin", coinDeposit.getPayCoin());
        jsonObject.put("depositType", coinDeposit.getPayType());
        jsonObject.put("depositCategory", coinDeposit.getCategory());
        reqDto.setInfo(jsonObject.toJSONString());
        reqDto.setMosaicCoin(reqDto.getMosaicCoin().subtract(coinDeposit.getPayCoin()));
        promotionsBase.executePromotionsPersistence(reqDto, userInfo.getId());
    }

    /**
     * 新赛季回归送彩金
     *
     * @param reqDto 请求实体类
     */
    public void newSeasonComeback(ApplicationActivityReqDto reqDto) {
        //获取用户信息
        UserInfo userInfo = userInfoServiceImpl.findIdentityByApiToken();
        Promotions promotions = promotionsServiceImpl.lambdaQuery().eq(Promotions::getId, Constant.NEW_SEASON_COMEBACK).one();
        //判断彩金是否领取
       promotionsBase.isPromotionsEffective(reqDto.getId(), userInfo.getId());
        //计算活动开始前平台盈利
        BigDecimal platProfit = getPlatProfit(promotions.getStartedAt()).abs();
        if (platProfit.compareTo(Constant.NEW_SEASON_PROFIT_COIN) < 0) {
            throw new BusinessException(CodeInfo.ACTIVE_PROFIT_NO_ENOUGH);
        }
        //判断赛季期间有每没有投注记录;1代表体育赛事
        isBet(userInfo.getId(), promotions, 1);
        //获取活动期间第一笔的充值
        CoinDeposit coinDeposit = coinDepositServiceImpl.lambdaQuery()
                .eq(CoinDeposit::getUid, userInfo.getId())
                .ge(CoinDeposit::getCreatedAt, promotions.getStartedAt())
                .le(CoinDeposit::getCreatedAt, promotions.getEndedAt())
                .in(CoinDeposit::getStatus, List.of(1, 2, 9))
                .orderByAsc(CoinDeposit::getCreatedAt)
                .last(LIMIT_ONE)
                .one();
        //判断是否存在充值记录
        if (Objects.isNull(coinDeposit)) {
            throw new BusinessException(CodeInfo.ACTIVE_NEW_SEASON_NO_RECORD);
        }
        //计算可得彩金，需打流水
        Map<String, BigDecimal> profitCoinMap = getProfitCoin(coinDeposit.getPayCoin(), reqDto.getId());
        if (CollectionUtils.isEmpty(profitCoinMap)) {
            throw new BusinessException(CodeInfo.ACTIVE_NO_REACH_REQUIREMENT);
        }
        BigDecimal profit = profitCoinMap.get(PROFIT);
        BigDecimal flowClaim = profitCoinMap.get(FLOW_CLAIM);
        reqDto.setAvailableCoin(profit);
        reqDto.setMosaicCoin(flowClaim);
        var jsonObject = new JSONObject();
        jsonObject.put("depositCoin", coinDeposit.getPayCoin());
        jsonObject.put("depositType", coinDeposit.getPayType());
        jsonObject.put("depositCategory", coinDeposit.getCategory());
        reqDto.setInfo(jsonObject.toJSONString());
        promotionsBase.executePromotionsPersistence(reqDto, userInfo.getId());
    }


    /**
     * 活动期间是否进行投注
     *
     * @param uid        用户id
     * @param promotions 活动类
     * @param groupId    游戏组
     */
    public void isBet(Integer uid, Promotions promotions, Integer groupId) {
        var gameLists = gameListServiceImpl.lambdaQuery()
                //启动状态
                .eq(nonNull(groupId), GameList::getGroupId, groupId)
                .eq(GameList::getStatus, 1)
                .list();
        for (GameList gameList : gameLists) {
            var platGame = PlatFactoryParams.PlatGameQueryDateDto.builder()
                    .uid(uid)
                    .gameId(gameList.getId())
                    .startTime(promotions.getStartedAt())
                    .endTime(promotions.getEndedAt())
                    .build();
            ReqPage<PlatFactoryParams.PlatGameQueryDateDto> reqPage = new ReqPage<>();
            reqPage.setData(platGame);
            var total = gameCommonBase.getBetsRecords(reqPage).getTotal();
            if (total > 0) {
                throw new BusinessException(CodeInfo.ACTIVE_RECEIVE_IN_NOT_BET);
            }
        }
    }
}
