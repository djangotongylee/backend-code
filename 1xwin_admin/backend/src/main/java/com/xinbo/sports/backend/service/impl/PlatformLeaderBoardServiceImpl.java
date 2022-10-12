package com.xinbo.sports.backend.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinbo.sports.backend.base.ReportCommon;
import com.xinbo.sports.backend.io.bo.ComprehensiveChart;
import com.xinbo.sports.backend.io.bo.ReportCenter;
import com.xinbo.sports.backend.io.bo.StartEndTime;
import com.xinbo.sports.backend.io.dto.Platform;
import com.xinbo.sports.backend.io.po.PlatProfitAndBetCountChartPo;
import com.xinbo.sports.backend.io.po.ReportStatisticsPo;
import com.xinbo.sports.backend.mapper.ReportStatisticsMapper;
import com.xinbo.sports.backend.redis.GameCache;
import com.xinbo.sports.backend.service.IPlatformLeaderBoardService;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.service.cache.redis.UserCache;
import com.xinbo.sports.service.common.Constant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.utils.BeanConvertUtils;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.alibaba.fastjson.JSON.parseObject;

/**
 * <p>
 * 报表中心-各平台盈利排行版
 * </p>
 *
 * @author andy
 * @since 2020/6/16
 */
@Slf4j
@Service
public class PlatformLeaderBoardServiceImpl implements IPlatformLeaderBoardService {
    @Resource
    private ReportStatisticsMapper reportStatisticsMapper;
    @Resource
    private ReportCommon reportCommon;
    @Resource
    private ConfigCache configCache;
    @Resource
    private GameCache gameCache;
    @Resource
    private UserCache userCache;
    private static final AtomicInteger REPORT_THREAD_POOL_ID = new AtomicInteger();


    private ThreadPoolExecutor getReportThreadPool() {
        return new ThreadPoolExecutor(
                32,
                48,
                2L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                x -> new Thread(x, "报表中心->统计_THREAD_POOL_" + REPORT_THREAD_POOL_ID.getAndIncrement()));

    }

    @Override
    public List<ReportCenter.PlatformLeaderBoardResBody> platformLeaderBoard(ReportCenter.PlatformLeaderBoardReqBody reqBody) {
        List<ReportCenter.PlatformLeaderBoardResBody> list = null;
        ReportStatisticsPo reqBo = BeanConvertUtils.beanCopy(reqBody, ReportStatisticsPo::new);
        reqBo.setTableName(switchTableName(reqBo.getGameListId()));

        /** 排除测试账号 **/
        List<Integer> testUidList = userCache.getTestUidList();
        reqBo.setTestUidList(testUidList);
        List<ReportStatisticsPo> reportStatisticsPos = reportStatisticsMapper.platLeaderBoardProfitStatisticsList(reqBo);
        if (Optional.ofNullable(reportStatisticsPos).isPresent()) {
            list = BeanConvertUtils.copyListProperties(reportStatisticsPos, ReportCenter.PlatformLeaderBoardResBody::new);
        }
        return list;
    }

    /**
     * 综合走势图-各平台投注总额
     *
     * @param reqBody
     * @return
     */
    @Override
    public List<ComprehensiveChart.ListCoinChart> platBetCoinChart(StartEndTime reqBody) {
        if (null == reqBody) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        ReportStatisticsPo reqBo = BeanConvertUtils.beanCopy(reqBody, ReportStatisticsPo::new);
        List<ComprehensiveChart.ListCoinChart> betCoinList = new ArrayList<>();
        var gameList = gameList();
        if (!gameList.isEmpty()) {
            var testUidList = userCache.getTestUidList();
            for (var entity : gameList) {
                String tableName = switchTableName(entity.getId());
                if (StringUtils.isNotBlank(tableName)) {
                    // 各平台投注额
                    reqBo.setTableName(tableName);
                    reqBo.setColumnName("xb_coin");

                    /** 排除测试账号 **/
                    reqBo.setTestUidList(testUidList);
                    BigDecimal coin = reportStatisticsMapper.getPlatCoinStatistics(reqBo);
                    ComprehensiveChart.ListCoinChart coinEntity = ComprehensiveChart.ListCoinChart.builder()
                            .name(entity.getName())
                            .coin(coin)
                            .build();
                    betCoinList.add(coinEntity);
                }
            }
        }
        return betCoinList;
    }

    /**
     * 综合走势图-游戏盈亏与投注注数
     *
     * @param reqBody
     * @return
     */
    @Override
    public ComprehensiveChart.PlatProfitAndBetCountChartResBody platProfitAndBetCountChart(ComprehensiveChart.PlatProfitAndBetCountChartReqBody reqBody) {
        List<ComprehensiveChart.ListChart> betCountList = new ArrayList<>();
        List<ComprehensiveChart.ListChart> betUserCountList = new ArrayList<>();
        List<ComprehensiveChart.ListCoinChart> profitList = new ArrayList<>();
        if (null != reqBody) {
            /** 排除测试账号 **/
            var testUidList = userCache.getTestUidList();

            // 游戏盈列表
            profitList = getProfitList(reqBody, testUidList);
            // 投注注数列表
            betCountList = getBetCountList(reqBody, testUidList);
            // 投注人数列表
            betUserCountList = getBetUserCountList(reqBody, testUidList);
        }
        if (Optional.ofNullable(profitList).isPresent() && !profitList.isEmpty()) {
            profitList.forEach(s -> s.setCoin(s.getCoin().negate()));
        }
        return ComprehensiveChart.PlatProfitAndBetCountChartResBody.builder()
                .betCountList(betCountList)
                .profitList(profitList)
                .betUserCountList(betUserCountList)
                .build();
    }

    /**
     * 综合走势图-游戏盈亏与投注注数:游戏盈亏
     *
     * @param reqBody reqBody
     * @return 游戏盈亏
     */
    private List<ComprehensiveChart.ListCoinChart> getProfitList(ComprehensiveChart.PlatProfitAndBetCountChartReqBody reqBody, List<Integer> testUidList) {
        List<ComprehensiveChart.ListCoinChart> resultList = reportCommon.daysRangeListWithCoin(reqBody.getStartTime(), reqBody.getEndTime());
        List<ComprehensiveChart.ListCoinChart> profitList = null;
        if (null == reqBody.getGameListId()) {
            profitList = concurrentGetPlatProfitList(reqBody.getStartTime(), reqBody.getEndTime(), testUidList);
        } else {
            profitList = processPlatProfitListChart(switchTableName(reqBody.getGameListId()), reqBody.getStartTime(), reqBody.getEndTime(), testUidList);
        }
        if (Optional.ofNullable(profitList).isEmpty()) {
            return resultList;
        }
        processListCoinChart(resultList, profitList);
        return resultList;
    }

    /**
     * 综合走势图-游戏盈亏与投注注数:投注注数
     *
     * @param reqBody reqBody
     * @return 投注注数
     */
    private List<ComprehensiveChart.ListChart> getBetCountList(ComprehensiveChart.PlatProfitAndBetCountChartReqBody reqBody, List<Integer> testUidList) {
        List<ComprehensiveChart.ListChart> resultList = reportCommon.rangeDayListWithCount(reqBody.getStartTime(), reqBody.getEndTime());
        List<ComprehensiveChart.ListChart> betCountList = null;
        if (null == reqBody.getGameListId()) {
            betCountList = concurrentGetPlatCountList(reqBody.getStartTime(), reqBody.getEndTime(), testUidList);
        } else {
            betCountList = processPlatCountListChart(switchTableName(reqBody.getGameListId()), reqBody.getStartTime(), reqBody.getEndTime(), testUidList);
        }
        if (Optional.ofNullable(betCountList).isEmpty()) {
            return resultList;
        }
        processListCountChart(resultList, betCountList);
        return resultList;
    }

    /**
     * 综合走势图-游戏盈亏与投注注数:投注人数
     *
     * @param reqBody reqBody
     * @return 投注注数
     */
    private List<ComprehensiveChart.ListChart> getBetUserCountList(ComprehensiveChart.PlatProfitAndBetCountChartReqBody reqBody, List<Integer> testUidList) {
        List<ComprehensiveChart.ListChart> resultList = reportCommon.rangeDayListWithCount(reqBody.getStartTime(), reqBody.getEndTime());

        List<ReportStatisticsPo> tmpList = null;
        if (null == reqBody.getGameListId()) {
            tmpList = concurrentGetPlatUserCountList(reqBody.getStartTime(), reqBody.getEndTime(), testUidList);
        } else {
            tmpList = processPlatUserCountListChart(switchTableName(reqBody.getGameListId()), reqBody.getStartTime(), reqBody.getEndTime(), testUidList);
        }
        if (Optional.ofNullable(tmpList).isEmpty() || tmpList.isEmpty()) {
            return resultList;
        }
        Map<String, List<Integer>> map = new HashMap<>();
        tmpList.forEach(o -> {
            String dateName = o.getDateName();
            if (map.containsKey(dateName)) {
                map.get(dateName).add(o.getUid());
            } else {
                var uidList = new ArrayList();
                uidList.add(o.getUid());
                map.put(dateName, uidList);
            }
        });

        List<ComprehensiveChart.ListChart> betUserCountList = new ArrayList<>();
        map.forEach((key, list) -> {
            var p = ComprehensiveChart.ListChart.builder().name(key).count((int) list.stream().distinct().count()).build();
            betUserCountList.add(p);
        });

        processListCountChart(resultList, betUserCountList);
        return resultList;
    }

    /**
     * 平台游戏盈亏
     *
     * @param startTime
     * @param endTime
     * @param uidList
     * @return
     */
    @Override
    public BigDecimal platTotalProfit(Integer startTime, Integer endTime, List<Integer> uidList, List<Integer> testUidList) {
        var pool = getReportThreadPool();
        BigDecimal totalProfit = BigDecimal.ZERO;
        List<Future<BigDecimal>> futureList = new ArrayList<>();
        var tableNameList = tableNameList();
        for (var tableName : tableNameList) {
            ReportStatisticsPo reqBo = new ReportStatisticsPo();
            reqBo.setStartTime(startTime);
            reqBo.setEndTime(endTime);
            reqBo.setUidList(uidList);
            reqBo.setColumnName("xb_profit");
            reqBo.setTableName(tableName);
            /** 排除测试账号 **/
            reqBo.setTestUidList(testUidList);
            futureList.add(pool.submit(() -> reportStatisticsMapper.getPlatCoinStatistics(reqBo)));
        }
        for (Future<BigDecimal> future : futureList) {
            try {
                totalProfit = totalProfit.add(future.get());
            } catch (Exception e) {
                String error = "游戏盈亏统计异常";
                log.error(error + ":" + e);
            }
        }
        return totalProfit;
    }

    /**
     * 用户综合->投注总额
     *
     * @param startTime
     * @param endTime
     * @param uidList
     * @return
     */
    @Override
    public BigDecimal platCoinBet(Integer startTime, Integer endTime, List<Integer> uidList, List<Integer> testUidList) {
        var pool = getReportThreadPool();
        BigDecimal platValidCoin = BigDecimal.ZERO;
        List<Future<BigDecimal>> futureList = new ArrayList<>();
        var tableNameList = tableNameList();
        for (var tableName : tableNameList) {
            ReportStatisticsPo reqBo = new ReportStatisticsPo();
            reqBo.setStartTime(startTime);
            reqBo.setEndTime(endTime);
            reqBo.setUidList(uidList);
            reqBo.setColumnName("xb_coin");
            reqBo.setTableName(tableName);
            /** 排除测试账号 **/
            reqBo.setTestUidList(testUidList);
            futureList.add(pool.submit(() -> reportStatisticsMapper.getPlatCoinStatistics(reqBo)));
        }
        for (Future<BigDecimal> future : futureList) {
            try {
                platValidCoin = platValidCoin.add(future.get());
            } catch (Exception e) {
                String error = "用户综合->投注总额统计异常";
                log.error(error + ":" + e);
            }
        }
        return platValidCoin;
    }

    /**
     * 综合走势图-游戏盈亏与投注注数:并发请求
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 游戏盈亏
     */
    private List<ComprehensiveChart.ListCoinChart> concurrentGetPlatProfitList(Integer startTime, Integer endTime, List<Integer> testUidList) {
        List<ComprehensiveChart.ListCoinChart> list = new ArrayList<>();
        var pool = getReportThreadPool();
        try {
            List<Future<List<ComprehensiveChart.ListCoinChart>>> futureList = new ArrayList<>();
            for (String tableName : tableNameList()) {
                futureList.add(pool.submit(() -> processPlatProfitListChart(tableName, startTime, endTime, testUidList)));
            }
            for (Future<List<ComprehensiveChart.ListCoinChart>> future : futureList) {
                list.addAll(future.get());
            }
        } catch (Exception e) {
            String error = "综合走势图->游戏盈亏与投注注数->游戏盈亏";
            log.error(error + ":" + e);
        }
        return list;
    }

    private List<String> tableNameList() {
        List<String> tableNameList = new ArrayList<>();
        var gameListResCache = gameList();
        for (var game : gameListResCache) {
            String tableName = switchTableName(game.getId());
            if (StringUtils.isNotBlank(tableName)) {
                tableNameList.add(tableName);
            }
        }
        return tableNameList;
    }

    /**
     * 综合走势图-游戏盈亏与投注注数->投注注数
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 投注注数
     */
    private List<ComprehensiveChart.ListChart> concurrentGetPlatCountList(Integer startTime, Integer endTime, List<Integer> testUidList) {
        List<ComprehensiveChart.ListChart> list = new ArrayList<>();
        var pool = getReportThreadPool();
        try {
            List<Future<List<ComprehensiveChart.ListChart>>> futureList = new ArrayList<>();
            for (String tableName : tableNameList()) {
                futureList.add(pool.submit(() -> processPlatCountListChart(tableName, startTime, endTime, testUidList)));
            }
            for (Future<List<ComprehensiveChart.ListChart>> future : futureList) {
                list.addAll(future.get());
            }
        } catch (Exception e) {
            String error = "综合走势图->游戏盈亏与投注注数->投注注数";
            log.error(error + ":" + e);
        }
        return list;
    }

    /**
     * 每日报表->按平台统计:投注总额|输赢总额
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public List<ReportStatisticsPo> getDailyReportStatisticsPlatCoin(Integer startTime, Integer endTime, List<Integer> uidList, List<Integer> testUidList) {
        List<ReportStatisticsPo> list = new ArrayList<>();
        var pool = getReportThreadPool();
        List<Future<ReportStatisticsPo>> futureList = new ArrayList<>();
        try {
            var gameListResCache = gameList();
            for (var game : gameListResCache) {
                String tableName = switchTableName(game.getId());
                if (StringUtils.isNotBlank(tableName)) {
                    futureList.add(pool.submit(() -> getDailyReportStatisticsPlatCoin(tableName, startTime, endTime, game.getGroupId(), uidList, null, null, testUidList)));
                }
            }

            for (Future<ReportStatisticsPo> future : futureList) {
                list.add(future.get());
            }
        } catch (Exception e) {
            String error = "每日报表->按平台统计:投注总额|输赢总额";
            log.error(error + ":" + e);
        }
        return list;
    }

    /**
     * 交易记录->全平台投注总额->统计:投注总额|输赢总额
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public List<ReportStatisticsPo> getPlatBetTotalStatistics(Integer startTime, Integer endTime, String id, Integer gameListId, String username, List<Integer> testUidList) {
        List<ReportStatisticsPo> list = new ArrayList<>();
        var pool = getReportThreadPool();
        List<Future<ReportStatisticsPo>> futureList = new ArrayList<>();
        try {
            var gameList = gameList();
            if (Optional.ofNullable(gameList).isEmpty() || gameList.isEmpty()) {
                return list;
            }
            if (null != gameListId) {
                gameList = gameList.stream().filter(o -> o.getId().equals(gameListId)).collect(Collectors.toList());
            }

            for (var game : gameList) {
                String tableName = switchTableName(game.getId());
                if (StringUtils.isNotBlank(tableName)) {
                    futureList.add(pool.submit(() -> getDailyReportStatisticsPlatCoin(tableName, startTime, endTime, game.getGroupId(), null, id, username, testUidList)));
                }
            }

            for (Future<ReportStatisticsPo> future : futureList) {
                list.add(future.get());
            }
        } catch (Exception e) {
            String error = "交易记录->全平台投注总额->统计:投注总额|输赢总额";
            log.error(error + ":" + e);
        }
        return list;
    }

    /**
     * 每日报表->按平台统计:投注笔数
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public long dailyReportStatisticsBetCount(Integer startTime, Integer endTime, List<Integer> uidList, List<Integer> testUidList) {
        long betCount = 0;
        List<Future<Long>> futureList = new ArrayList<>();
        var pool = getReportThreadPool();
        try {
            var gameListResCache = gameList();
            for (var game : gameListResCache) {
                String tableName = switchTableName(game.getId());
                if (StringUtils.isNotBlank(tableName)) {
                    futureList.add(pool.submit(() -> dailyReportStatisticsBetCount(tableName, startTime, endTime, uidList, testUidList)));
                }
            }
        } catch (Exception e) {
            String error = "每日报表->按平台统计:投注笔数";
            log.error(error + ":" + e);
        }

        for (Future<Long> future : futureList) {
            try {
                betCount += future.get();
            } catch (Exception e) {
                String error = "每日报表->按平台统计:投注笔数";
                log.error(error + ":" + e);
            }
        }
        return betCount;
    }

    /**
     * 综合走势图-游戏盈亏与投注注数:投注人数
     *
     * @return 投注注数
     */
    public long dailyReportStatisticsBetUserCount(Integer startTime, Integer endTime, List<Integer> uidList, List<Integer> testUidList) {
        long betUserCount = 0;
        List<ReportStatisticsPo> betUserCountList = new ArrayList<>();
        var pool = getReportThreadPool();
        try {
            List<Future<List<ReportStatisticsPo>>> futureList = new ArrayList<>();
            for (String tableName : tableNameList()) {
                futureList.add(pool.submit(() -> dailyReportStatisticsBetUserCount(tableName, startTime, endTime, uidList, testUidList)));
            }
            for (Future<List<ReportStatisticsPo>> future : futureList) {
                betUserCountList.addAll(future.get());
            }
        } catch (Exception e) {
            String error = "每日报表->按平台统计:投注人数";
            log.error(error + ":" + e);
        }
        if (!betUserCountList.isEmpty()) {
            betUserCount = betUserCountList.stream().map(ReportStatisticsPo::getUid).collect(Collectors.toSet()).size();
        }
        return betUserCount;

    }

    /**
     * 综合走势图-游戏盈亏与投注注数->投注人数
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 投注注数
     */
    private List<ReportStatisticsPo> concurrentGetPlatUserCountList(Integer startTime, Integer endTime, List<Integer> testUidList) {
        List<ReportStatisticsPo> list = new ArrayList<>();
        var pool = getReportThreadPool();
        try {
            List<Future<List<ReportStatisticsPo>>> futureList = new ArrayList<>();
            for (String tableName : tableNameList()) {
                futureList.add(pool.submit(() -> processPlatUserCountListChart(tableName, startTime, endTime, testUidList)));
            }
            for (Future<List<ReportStatisticsPo>> future : futureList) {
                list.addAll(future.get());
            }
        } catch (Exception e) {
            String error = "综合走势图->游戏盈亏与投注注数->投注人数";
            log.error(error + ":" + e);
        }
        return list;
    }

    private List<Platform.GameListInfo> gameList() {
        return gameCache.getGameListResCache();
    }

    private List<ComprehensiveChart.ListCoinChart> processPlatProfitListChart(String tableName, Integer startTime, Integer endTime, List<Integer> testUidList) {
        ReportStatisticsPo reqPo = new ReportStatisticsPo();
        reqPo.setTableName(tableName);
        reqPo.setStartTime(startTime);
        reqPo.setEndTime(endTime);

        /** 排除测试账号 **/
        reqPo.setTestUidList(testUidList);
        List<PlatProfitAndBetCountChartPo> po = reportStatisticsMapper.getPlatProfitStatistics(reqPo);
        return BeanConvertUtils.copyListProperties(po, ComprehensiveChart.ListCoinChart::new);
    }

    private List<ComprehensiveChart.ListChart> processPlatCountListChart(String tableName, Integer startTime, Integer endTime, List<Integer> testUidList) {
        ReportStatisticsPo reqPo = new ReportStatisticsPo();
        reqPo.setTableName(tableName);
        reqPo.setStartTime(startTime);
        reqPo.setEndTime(endTime);

        /** 排除测试账号 **/
        reqPo.setTestUidList(testUidList);
        List<PlatProfitAndBetCountChartPo> po = reportStatisticsMapper.getPlatBetCountStatistics(reqPo);
        return BeanConvertUtils.copyListProperties(po, ComprehensiveChart.ListChart::new);
    }

    private ReportStatisticsPo getDailyReportStatisticsPlatCoin(String tableName, Integer startTime, Integer endTime, Integer groupId, List<Integer> uidList, String id, String username, List<Integer> testUidList) {
        ReportStatisticsPo reqPo = new ReportStatisticsPo();
        reqPo.setTableName(tableName);
        reqPo.setStartTime(startTime);
        reqPo.setEndTime(endTime);
        reqPo.setUidList(uidList);

        reqPo.setUsername(username);
        reqPo.setId(id);
        /** 排除测试账号 **/
        reqPo.setTestUidList(testUidList);
        var resPo = reportStatisticsMapper.getDailyReportStatisticsPlatCoin(reqPo);
        resPo.setGroupId(groupId);
        return resPo;
    }

    private long dailyReportStatisticsBetCount(String tableName, Integer startTime, Integer endTime, List<Integer> uidList, List<Integer> testUidList) {
        ReportStatisticsPo reqPo = new ReportStatisticsPo();
        reqPo.setTableName(tableName);
        reqPo.setStartTime(startTime);
        reqPo.setEndTime(endTime);
        reqPo.setUidList(uidList);

        /** 排除测试账号 **/
        reqPo.setTestUidList(testUidList);
        return reportStatisticsMapper.getBetCount(reqPo);
    }

    private List<ReportStatisticsPo> dailyReportStatisticsBetUserCount(String tableName, Integer startTime, Integer endTime, List<Integer> uidList, List<Integer> testUidList) {
        ReportStatisticsPo reqPo = new ReportStatisticsPo();
        reqPo.setTableName(tableName);
        reqPo.setStartTime(startTime);
        reqPo.setEndTime(endTime);
        reqPo.setUidList(uidList);

        /** 排除测试账号 **/
        reqPo.setTestUidList(testUidList);
        return reportStatisticsMapper.getBetUserCount(reqPo);
    }

    private List<ReportStatisticsPo> processPlatUserCountListChart(String tableName, Integer startTime, Integer endTime, List<Integer> testUidList) {
        ReportStatisticsPo reqPo = new ReportStatisticsPo();
        reqPo.setTableName(tableName);
        reqPo.setStartTime(startTime);
        reqPo.setEndTime(endTime);

        /** 排除测试账号 **/
        reqPo.setTestUidList(testUidList);
        return reportStatisticsMapper.getPlatBetUserCountStatistics(reqPo);
    }

    private void processListCoinChart(List<ComprehensiveChart.ListCoinChart> listCoinChart, List<ComprehensiveChart.ListCoinChart> tmpList) {
        if (!tmpList.isEmpty()) {
            Map<String, BigDecimal> map = new HashMap<>();
            for (ComprehensiveChart.ListCoinChart entity : tmpList) {
                if (null != entity && StringUtils.isNotBlank(entity.getName())) {
                    this.getListCoinChartMap(map, entity.getName(), entity.getCoin());
                }
            }
            reportCommon.fullCoinList(listCoinChart, map);
        }
    }

    private void processListCountChart(List<ComprehensiveChart.ListChart> listChart, List<ComprehensiveChart.ListChart> tmpList) {
        if (!tmpList.isEmpty()) {
            Map<String, Integer> map = new HashMap<>();
            for (ComprehensiveChart.ListChart entity : tmpList) {
                if (null != entity && StringUtils.isNotBlank(entity.getName())) {
                    this.getListCountChartMap(map, entity.getName(), entity.getCount());
                }
            }
            reportCommon.fullCountList(listChart, map);
        }
    }

    private void getListCountChartMap(Map<String, Integer> map, String name, Integer tempCount) {
        if (map.containsKey(name)) {
            int count = map.get(name);
            count = count + tempCount;
            map.put(name, count);
        } else {
            map.put(name, tempCount);
        }
    }

    private void getListCoinChartMap(Map<String, BigDecimal> map, String name, BigDecimal coin) {
        if (map.containsKey(name)) {
            BigDecimal tmpCoin = map.get(name);
            tmpCoin = tmpCoin.add(coin);
            map.put(name, tmpCoin);
        } else {
            map.put(name, coin);
        }
    }

    /**
     * 根据gameListId GET投注表名称
     *
     * @param gameListId gameList主键ID
     * @return 投注表名称
     */
    private String switchTableName(Integer gameListId) {
        return configCache.getBetSlipsTableNameByGameListId(gameListId);
    }


    /**
     * 交易记录-投注总额-列表
     *
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @param id         注单ID
     * @param gameListId gameListId
     * @param username   会员名称
     * @return List<ReportStatisticsPo>
     */
    public List<ReportStatisticsPo> platBetTotalList(Integer startTime, Integer endTime, String id, Integer gameListId, String username, int size, List<Integer> testUidList) {
        List<ReportStatisticsPo> list = new ArrayList<>();
        List<Future<List<ReportStatisticsPo>>> futureList = new ArrayList<>();
        try {
            var gameList = gameList();
            if (Optional.ofNullable(gameList).isEmpty() || gameList.isEmpty()) {
                return list;
            }
            if (null != gameListId) {
                gameList = gameList.stream().filter(o -> o.getId().equals(gameListId)).collect(Collectors.toList());
            }
            var pool = getReportThreadPool();
            for (var game : gameList) {
                var jsonModel = parseObject(game.getModel());
                if (null != jsonModel) {
                    // 期号/局号
                    var actionNo = jsonModel.getString(Constant.ACTION_NO);
                    // 游戏ID
                    var gameId = jsonModel.getString(Constant.GAME_ID_FIELD);
                    String tableName = switchTableName(game.getId());
                    if (StringUtils.isNotBlank(tableName)) {
                        ReportStatisticsPo reqPo = new ReportStatisticsPo();
                        reqPo.setStartTime(startTime);
                        reqPo.setEndTime(endTime);

                        reqPo.setTableName(tableName);
                        // gameId
                        reqPo.setColumnName1(gameId);
                        // actionNo
                        reqPo.setColumnName2(actionNo);
                        reqPo.setId(id);
                        reqPo.setUsername(username);
                        reqPo.setSize(size);

                        /** 排除测试账号 **/
                        reqPo.setTestUidList(testUidList);
                        var tmpFuture = pool.submit(() -> reportStatisticsMapper.getPlatBetTotalList(reqPo));
                        if (null != tmpFuture.get()) {
                            tmpFuture.get().stream().forEach(o -> {
                                o.setGroupId(game.getGroupId());
                                o.setModel(game.getModel());
                                o.setGameListId(game.getId());
                                o.setGameListName(game.getName());
                            });
                            futureList.add(tmpFuture);
                        }
                    }
                }
            }
            for (Future<List<ReportStatisticsPo>> future : futureList) {
                list.addAll(future.get());
            }
        } catch (Exception e) {
            String error = "交易记录-投注总额-列表";
            log.error(error + ":" + e);
        }
        return list;
    }

    /**
     * 交易记录-投注总额-SQL分页(单平台)
     *
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @param id         注单ID
     * @param gameListId gameListId
     * @param username   会员名称
     * @return List<ReportStatisticsPo>
     */
    public Page<ReportStatisticsPo> platBetTotalListPageByPlatId(Integer startTime, Integer endTime, String id, Integer gameListId, String username, Page page, List<Integer> testUidList) {
        var gameListPo = gameCache.getGameListCache(gameListId);
        if (null == gameListPo) {
            return new Page<>();
        }
        var tableName = switchTableName(gameListPo.getId());
        var jsonModel = parseModel(gameListPo.getModel());
        if (null == jsonModel || StringUtils.isBlank(tableName)) {
            return new Page<>();
        }
        // 期号/局号
        var actionNo = jsonModel.getString(Constant.ACTION_NO);
        // 游戏ID
        var gameId = jsonModel.getString(Constant.GAME_ID_FIELD);
        ReportStatisticsPo reqPo = new ReportStatisticsPo();
        reqPo.setStartTime(startTime);
        reqPo.setEndTime(endTime);

        reqPo.setTableName(tableName);
        // gameId
        reqPo.setColumnName1(gameId);
        // actionNo
        reqPo.setColumnName2(actionNo);
        reqPo.setId(id);
        reqPo.setUsername(username);

        /** 排除测试账号 **/
        reqPo.setTestUidList(testUidList);
        var resPage = reportStatisticsMapper.getPlatBetTotalListPage(page, reqPo);
        resPage.getRecords().stream().forEach(o -> {
            o.setGroupId(gameListPo.getGroupId());
            o.setModel(gameListPo.getModel());
            o.setGameListId(gameListPo.getId());
            o.setGameListName(gameListPo.getName());
        });
        return resPage;
    }

    private JSONObject parseModel(String model) {
        if (StringUtils.isBlank(model)) {
            return null;
        }
        return parseObject(model);
    }

    /**
     * 各平台盈亏报表->列表 内存分页
     *
     * @param reqPo reqPo
     * @return List<ReportStatisticsPo>
     */
    public List<ReportStatisticsPo> getPlatformProfitList(ReportStatisticsPo reqPo) {
        var list = new ArrayList<ReportStatisticsPo>();
        var futureList = new ArrayList<Future<List<ReportStatisticsPo>>>();
        try {
            var gameList = gameList();
            if (Optional.ofNullable(gameList).isEmpty() || gameList.isEmpty()) {
                return list;
            }

            if (null != reqPo.getGroupId()) {
                gameList = gameList.stream().filter(o -> reqPo.getGroupId().equals(o.getGroupId())).collect(Collectors.toList());
            }

            if (null != reqPo.getGameListId()) {
                gameList = gameList.stream().filter(o -> o.getId().equals(reqPo.getGameListId())).collect(Collectors.toList());
            }
            var pool = getReportThreadPool();
            for (var game : gameList) {
                var jsonModel = parseObject(game.getModel());
                if (null != jsonModel) {
                    String tableName = switchTableName(game.getId());
                    if (StringUtils.isNotBlank(tableName)) {
                        var entity = new ReportStatisticsPo();
                        entity.setStartTime(reqPo.getStartTime());
                        entity.setEndTime(reqPo.getEndTime());
                        entity.setGameId(reqPo.getGameId());
                        entity.setGroupId(reqPo.getGroupId());
                        entity.setGameListId(reqPo.getGameListId());
                        entity.setSize(reqPo.getSize());
                        entity.setUidList(reqPo.getUidList());

                        // 游戏Id属性
                        var gameIdField = jsonModel.getString(Constant.GAME_ID_FIELD);
                        entity.setTableName(tableName);
                        // 设置游戏Id属性
                        entity.setColumnName1(gameIdField);
                         futureList.add(pool.submit(() -> reportStatisticsMapper.getPlatformProfitList(entity)));
                    }
                }
            }
            for (Future<List<ReportStatisticsPo>> future : futureList) {
                list.addAll(future.get());
            }
        } catch (Exception e) {
            String error = "各平台盈亏报表->列表 内存分页";
            log.error(error + ":" + e);
        }
        return list;
    }

    /**
     * 各平台盈亏报表->统计
     *
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @param gameId     游戏Id
     * @param gameListId gameListId
     * @return List<ReportStatisticsPo>
     */
    public List<ReportStatisticsPo> getPlatformProfit(Integer startTime, Integer endTime, String gameId, Integer gameListId, Integer groupId, List<Integer> testUidList, List<Integer> uidList) {
        var list = new ArrayList<ReportStatisticsPo>();
        var futureList = new ArrayList<Future<ReportStatisticsPo>>();
        try {
            var gameList = gameList();
            if (Optional.ofNullable(gameList).isEmpty() || gameList.isEmpty()) {
                return list;
            }

            if (null != groupId) {
                gameList = gameList.stream().filter(o -> groupId.equals(o.getGroupId())).collect(Collectors.toList());
            }

            if (null != gameListId) {
                gameList = gameList.stream().filter(o -> o.getId().equals(gameListId)).collect(Collectors.toList());
            }
            var pool = getReportThreadPool();
            for (var game : gameList) {
                var jsonModel = parseObject(game.getModel());
                if (null != jsonModel) {
                    String tableName = switchTableName(game.getId());
                    if (StringUtils.isNotBlank(tableName)) {
                        // 游戏ID
                        var gameIdField = jsonModel.getString(Constant.GAME_ID_FIELD);

                        ReportStatisticsPo reqPo = new ReportStatisticsPo();
                        reqPo.setStartTime(startTime);
                        reqPo.setEndTime(endTime);

                        reqPo.setTableName(tableName);
                        // gameIdField
                        reqPo.setColumnName1(gameIdField);

                        reqPo.setId(gameId);

                        reqPo.setUidList(uidList);
                        /** 排除测试账号 **/
                        reqPo.setTestUidList(testUidList);
                        futureList.add(pool.submit(() -> reportStatisticsMapper.getDailyReportStatisticsPlatCoin(reqPo)));
                    }
                }
            }
            for (Future<ReportStatisticsPo> future : futureList) {
                list.add(future.get());
            }
        } catch (Exception e) {
            String error = "各平台盈亏报表->统计";
            log.error(error + ":" + e);
        }
        return list;
    }

    /**
     * 各平台盈亏报表->列表-SQL分页(单平台)
     *
     * @param reqPo
     * @param page
     * @return
     */
    public Page<ReportStatisticsPo> getPlatformProfitListPageByPlatId(ReportStatisticsPo reqPo, Page page) {
        var gameListPo = gameCache.getGameListCache(reqPo.getGameListId());
        if (null == gameListPo) {
            return new Page<>();
        }
        var tableName = switchTableName(gameListPo.getId());
        var jsonModel = parseModel(gameListPo.getModel());
        if (null == jsonModel || StringUtils.isBlank(tableName)) {
            return new Page<>();
        }
        var gameIdField = jsonModel.getString(Constant.GAME_ID_FIELD);
        reqPo.setTableName(tableName);
        reqPo.setColumnName1(gameIdField);
        return reportStatisticsMapper.getPlatformProfitPage(page, reqPo);
    }
}
