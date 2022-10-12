package com.xinbo.sports.task.handler;

/**
 * @author: wells
 * @date: 2020/6/17
 * @description:
 */

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.stereotype.Component;

/**
 * 任务Handler示例（Bean模式）
 */


@Component
public class DemoJobHandler {
    @XxlJob("demoJobHandler")
    public ReturnT<String> execute(String param) {
        XxlJobLogger.log("XXL-JOB, Hello World.");
        return ReturnT.SUCCESS;
    }

    @XxlJob("testJob")
    public ReturnT<String> testJob(String s) {
        for (int i = 0; i < 10; i++) {
            XxlJobLogger.log("XXL-JOB, testJob");
        }
        return ReturnT.SUCCESS;
    }

}