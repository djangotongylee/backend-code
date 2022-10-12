package com.xinbo.sports.task;

import com.xinbo.sports.task.service.impl.IPromotionsTaskServiceImpl;
import com.xinbo.sports.task.service.impl.RevenueStatisticTaskServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author: wells
 * @date: 2020/8/24
 * @description:
 */
@SpringBootTest
public class SwitchTest {
    @Resource
    private RevenueStatisticTaskServiceImpl revenueStatisticTaskServiceImpl;


    @Resource
    private IPromotionsTaskServiceImpl iPromotionsTaskServiceImpl;

    @Test
    void test() {
        String code = "2";
        switch (code) {
            case "1":
                var param = "1";
                System.out.println("param=" + param);
                break;
            case "2":
                param = "2";
                System.out.println("param=" + param);
                break;
            default:
        }
    }

    @Test
    public void testAgent() {
        iPromotionsTaskServiceImpl.agent(1);
    }


    @Test
    void revenueReportTest() {
        revenueStatisticTaskServiceImpl.everyMonthRevenue();
    }

}
