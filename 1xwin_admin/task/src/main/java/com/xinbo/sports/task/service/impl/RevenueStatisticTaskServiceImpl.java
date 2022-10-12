package com.xinbo.sports.task.service.impl;

import com.xinbo.sports.dao.generator.po.RevenueStatistics;
import com.xinbo.sports.dao.generator.po.User;
import com.xinbo.sports.dao.generator.service.RevenueStatisticsService;
import com.xinbo.sports.dao.generator.service.UserService;
import com.xinbo.sports.service.base.GameStatisticsBase;
import com.xinbo.sports.service.common.Constant;
import com.xinbo.sports.service.io.bo.PlatServiceBaseParams;
import com.xinbo.sports.task.service.RevenueStatisticTaskService;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.DateUtils;
import com.xxl.job.core.log.XxlJobLogger;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.stream.Collectors;

import static com.alibaba.fastjson.JSON.parseObject;

/**
 * @Author : Wells
 * @Date : 2020/9/19 2:35 下午
 * @Description : 税收统计
 **/
@Service
public class RevenueStatisticTaskServiceImpl implements RevenueStatisticTaskService {
    @Resource
    private GameStatisticsBase gameStatisticsBase;
    @Resource
    private RevenueStatisticsService revenueStatisticsServiceImpl;
    @Resource
    private UserService userServiceImpl;


    /**
     * @Author Wells
     * @Date 2020/9/19 2:41 下午
     * @Description 每月税收报表
     * 1.当天统计前一天的游戏记录（三方的所有游戏），统计指标：投注金额，有效投注额，输赢金额
     * 2.入库到税收统计表，用于报表展示。
     * @param1
     * @Return void
     **/
    @Override
    public void everyMonthRevenue() {
        int now = DateNewUtils.now();
        ZonedDateTime zoneNow = DateNewUtils.utc8Zoned(now);
        int startTime = (int) zoneNow.minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0).toEpochSecond();
        int endTime = (int) zoneNow.minusDays(1).withHour(23).withMinute(59).withSecond(59).withNano(59).toEpochSecond();
        XxlJobLogger.log("税收任务调度;开始时间" + DateUtils.yyyyMMddHHmmss(startTime) + "结束时间" + DateUtils.yyyyMMddHHmmss(endTime));
        var reqDto = PlatServiceBaseParams.PlatGameQueryDateDto.builder().build();
        var gameList = gameStatisticsBase.getConfig(reqDto);
        var revenueList = new ArrayList<RevenueStatistics>();
        if (!CollectionUtils.isEmpty(gameList)) {
            gameList.forEach(game -> {
                //查询每个游戏的投注金额，有效投注额，输赢金额
                reqDto.setStartTime(startTime);
                reqDto.setEndTime(endTime);
                reqDto.setGameId(game.getId());
                if (Constant.FUTURES_LOTTERY.equals(game.getId())) {
                    var userList = userServiceImpl.lambdaQuery()
                            .ne(User::getRole, 4).list()
                            .stream().map(User::getId)
                            .collect(Collectors.toList());
                    reqDto.setUidList(userList);
                }
                var resDto = gameStatisticsBase.getCoinStatisticsByDate(reqDto);
                BigDecimal revenueCoin;
                var model = game.getModel();
                var revenueCategory = parseObject(model).getString("revenueCategory");
                if (StringUtils.isNoneEmpty(revenueCategory) && Constant.TURNOVER_COIN.equals(revenueCategory)) {
                    revenueCoin = resDto.getCoinBet().multiply(game.getRevenueRate()).setScale(4, RoundingMode.DOWN);
                } else {
                    revenueCoin = resDto.getCoinProfit().multiply(game.getRevenueRate()).setScale(4, RoundingMode.DOWN);
                }
                var revenueStatistics = new RevenueStatistics();
                revenueStatistics.setGameId(game.getId());
                revenueStatistics.setGameName(game.getName());
                revenueStatistics.setYear(zoneNow.getYear());
                revenueStatistics.setMonth(zoneNow.getMonth().getValue());
                revenueStatistics.setDay(zoneNow.getDayOfMonth());
                revenueStatistics.setBetCoin(resDto.getCoin());
                revenueStatistics.setProfitCoin(resDto.getCoinProfit());
                revenueStatistics.setValidCoin(resDto.getCoinBet());
                revenueStatistics.setRevenueCoin(revenueCoin);
                revenueStatistics.setRate(game.getRevenueRate());
                revenueStatistics.setCreatedAt(now);
                revenueStatistics.setUpdatedAt(now);
                revenueList.add(revenueStatistics);
            });
            revenueStatisticsServiceImpl.saveBatch(revenueList);
            XxlJobLogger.log("税收任务调度;执行成功!");
        }
    }


}
