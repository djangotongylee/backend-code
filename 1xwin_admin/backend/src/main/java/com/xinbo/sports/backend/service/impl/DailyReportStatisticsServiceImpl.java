package com.xinbo.sports.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xinbo.sports.backend.io.bo.ReportCenter.DailyConversionReportListResBody;
import com.xinbo.sports.backend.io.bo.ReportCenter.DailyConversionReportStatisticsResBody;
import com.xinbo.sports.backend.io.bo.ReportCenter.RevenueReqBody;
import com.xinbo.sports.backend.io.bo.ReportCenter.RevenueResBody;
import com.xinbo.sports.backend.io.bo.StartEndTime;
import com.xinbo.sports.backend.service.IDailyReportStatisticsService;
import com.xinbo.sports.dao.generator.po.RevenueStatistics;
import com.xinbo.sports.dao.generator.service.RevenueStatisticsService;
import com.xinbo.sports.utils.BeanConvertUtils;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.DateUtils;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * <p>
 * 报表中心-每日报表-统计接口实现类
 * </p>
 *
 * @author andy
 * @since 2020/6/5
 */
@Service
@Slf4j(topic = "报表中心-每日报表")
public class DailyReportStatisticsServiceImpl implements IDailyReportStatisticsService {
    @Resource
    private RevenueStatisticsService revenueStatisticsServiceImpl;
    @Resource
    private DailyReportRealTimeStatisticsServiceImpl dailyReportRealTimeStatisticsServiceImpl;
    //年
    private static final String YEAR = "year";
    //月
    private static final String MONTH = "month";
    //游戏名称
    private static final String GAME_NAME = "game_name";

    @Override
    public ResPage<DailyConversionReportListResBody> dailyReportList(ReqPage<StartEndTime> reqBody) {
        return dailyReportRealTimeStatisticsServiceImpl.dailyReportRealTimeList(reqBody);
    }

    @Override
    public DailyConversionReportStatisticsResBody dailyReportStatistics(StartEndTime reqBody) {
        return dailyReportRealTimeStatisticsServiceImpl.dailyReportRealTimeStatistics(reqBody);
    }

    /**
     * @Author Wells
     * @Date 2020/9/19 5:07 下午
     * @Description 税收报表
     * @param1 reqBody
     * @Return com.xinbo.sports.utils.components.pagination.ResPage<com.xinbo.sports.backend.io.bo.RevenueResBody>
     */
    @Override
    public ResPage<RevenueResBody> revenueStatistic(ReqPage<RevenueReqBody> reqBody) {
        var resDto = reqBody.getData();
        //开始时间的年与月
        var startTimeArr = DateUtils.yyyyMMddHHmmss(resDto.getStartTime()).split("-");
        var startYear = Integer.parseInt(startTimeArr[0]);
        var startMonth = Integer.parseInt(startTimeArr[1]);
        //结束时间的年月
        var endTimeArr = DateUtils.yyyyMMddHHmmss(resDto.getEndTime()).split("-");
        var endYear = Integer.parseInt(endTimeArr[0]);
        var endMonth = Integer.parseInt(endTimeArr[1]);
        var queryWrapper = new QueryWrapper<RevenueStatistics>()
                .select(GAME_NAME + " as gameName", YEAR, MONTH, "sum(bet_coin) as betCoin", "sum(valid_coin) as validCoin", "sum(profit_coin) as profitCoin",
                        "sum(revenue_coin) as revenueCoin", "max(rate) as rate")
                .ge(YEAR, startYear)
                .le(YEAR, endYear)
                .ge(MONTH, startMonth)
                .le(MONTH, endMonth)
                .groupBy(GAME_NAME, YEAR, MONTH);
        var page = revenueStatisticsServiceImpl.page(reqBody.getPage(), queryWrapper);
        var returnPage = BeanConvertUtils.copyPageProperties(page, RevenueResBody::new, (source, revenueResBody) -> {
            revenueResBody.setYearMonth(source.getYear() + "-" + String.format("%02d", source.getMonth()));
            revenueResBody.setRate(source.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.DOWN) + "%");
        });
        return ResPage.get(returnPage);
    }

    /**
     * 将10位长度Integer类型的时间戳转换成String 类型的时间格式，时间格式为：yyyyMMdd
     */
    private static String convertTimeToString(@NotNull Integer time) {
        DateTimeFormatter ftf = DateTimeFormatter.ofPattern(DateNewUtils.Format.yyyyMMdd.getValue());
        return ftf.format(LocalDateTime.ofInstant(Instant.ofEpochSecond(time), ZoneId.systemDefault()));
    }

}
