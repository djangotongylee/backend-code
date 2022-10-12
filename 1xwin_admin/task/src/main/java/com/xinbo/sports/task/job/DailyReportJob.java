package com.xinbo.sports.task.job;

import com.xinbo.sports.task.service.RevenueStatisticTaskService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.function.BiConsumer;

/**
 * @description:生成每日报表记录
 * @author: andy
 * @date: 2020/9/3
 */
@Component
public class DailyReportJob {
    @Resource
    private RevenueStatisticTaskService revenueStatisticTaskServiceImpl;
    BiConsumer<String, Exception> biConsumer = (message, exception) -> {
        XxlJobLogger.log(message + exception);
        StackTraceElement[] stackTrace = exception.getStackTrace();
        for (StackTraceElement element : stackTrace) {
            XxlJobLogger.log(element.toString());
        }
    };

    /**
     * 每日报表-税收
     */
    @XxlJob("revenueReport")
    public ReturnT<String> revenueReport(String param) {
        try {
            revenueStatisticTaskServiceImpl.everyMonthRevenue();
        } catch (Exception e) {
            biConsumer.accept("每日报表-税收;", e);
            return ReturnT.FAIL;
        }
        return ReturnT.SUCCESS;
    }
}
