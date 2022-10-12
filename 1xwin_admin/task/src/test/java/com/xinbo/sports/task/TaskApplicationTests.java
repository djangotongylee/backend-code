package com.xinbo.sports.task;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.google.common.collect.*;
import com.xinbo.sports.dao.generator.po.User;
import com.xinbo.sports.dao.generator.service.BetSlipsExceptionService;
import com.xinbo.sports.dao.generator.service.impl.UserServiceImpl;
import com.xinbo.sports.plat.io.bo.GGFishingParameter;
import com.xinbo.sports.plat.service.impl.futures.FuturesLotteryServiceImpl;
import com.xinbo.sports.task.base.GameCommonBase;
import com.xinbo.sports.task.base.PromotionsTaskBase;
import com.xinbo.sports.task.job.DailyReportJob;
import com.xinbo.sports.task.job.PaymentNotifyJob;
import com.xinbo.sports.task.job.PlatPullDataJob;
import com.xinbo.sports.task.job.PromotionsJob;
import com.xinbo.sports.task.service.IPromotionsTaskService;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.DateUtils;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static com.alibaba.fastjson.JSON.parseObject;

@RunWith(SpringRunner.class)
@SpringBootTest
class TaskApplicationTests {
    @Autowired
    private PlatPullDataJob platPullDataJob;
    @Autowired
    private PromotionsJob promotionsJob;
    @Autowired
    private GameCommonBase gameCommonBase;
    @Autowired
    private UserServiceImpl userServiceImpl;
    @Autowired
    private BetSlipsExceptionService betSlipsExceptionServiceImpl;
    @Autowired
    private IPromotionsTaskService iPromotionsTaskServiceImpl;
    @Autowired
    private DailyReportJob dailyReportJob;
    @Autowired
    private FuturesLotteryServiceImpl futuresLotteryServiceImpl;
    @Autowired
    private PaymentNotifyJob paymentNotifyJob;
    @Autowired
    private PromotionsTaskBase promotionsTaskBase;


    @Test
    void testJob() {
        var str = "{\"cagent\":\"TE399\",\"enddate\":\"2020-08-20 00:05:00\",\"method\":\"br3\",\"startdate\":\"2020-08-20 00:10:00\"}";
        var betListByDateBo = parseObject(str, GGFishingParameter.BetListByDateBo.class);

        String params = "{}";
        String params1 = "{\"parent\":\"BBIN\",\"model\":\"BBINGame\"}";
        String params2 = "{\"parent\":\"BBIN\",\"model\":\"BBINLive\"}";
        String params3 = "{\"gameId\":402,\"parent\":\"BBIN\",\"model\":\"BBINFishingExpert\"}";
        String params4 = "{\"parent\":\"BBIN\",\"model\":\"BBINFishingMasters\"}";
        String params5 = "{\"gameId\":401,\"parent\":\"GG\",\"model\":\"GGFishingGame\"}";
        String params6 = "{\"gameId\":105,\"parent\":\"IBC\",\"model\":\"ShaBaSports\"}";
        String params7 = "{\"parent\":\"AE\",\"model\":\"SexyLive\",\"gameId\":\"305\"}";
        String params8 = "{\"gameId\":\"503\"}";
        // String params7 = "{\"parent\":\"BWG\",\"model\":\"BWGLottery\",\"gameId\":\"703\"}";
        platPullDataJob.pullData(params8);

        //String params8 = "{\"gameId\":403}";
//        platPullDataJob.supplementData("");
//
//        platPullDataJob.scanSupplement(params8);
//        // 异常回归
//        platPullDataJob.regressionPullData(params5);
//        promotionsJob.liveDailyBonus(params);
//        PlatFactoryParams.PlatGameQueryDateDto platDto = new PlatFactoryParams.PlatGameQueryDateDto();
//        platDto.setStartTime(DateUtils.getCurrentTime() - 30 * 24 * 3600);
//        platDto.setEndTime(DateUtils.getCurrentTime());
//        platDto.setGameId(401);
//        gameCommonBase.getUserBetCoin(platDto);
//        promotionsJob.agent(params);
//        promotionsJob.inviteFriends(params);
//        userServiceImpl.lambdaUpdate().setSql("coin=coin+" + BigDecimal.valueOf(10.21)).eq(User::getId, 12).update();
    }

    @Test
    void testRang() {
        RangeMap<Integer, Pair<BigDecimal, BigDecimal>> rangeMap = TreeRangeMap.create();
        rangeMap.put(Range.openClosed(0, 3), Pair.of(BigDecimal.valueOf(1000), BigDecimal.valueOf(0.1)));
        rangeMap.put(Range.openClosed(3, 10), Pair.of(BigDecimal.valueOf(5000), BigDecimal.valueOf(0.2)));
        rangeMap.put(Range.openClosed(10, 20), Pair.of(BigDecimal.valueOf(300000), BigDecimal.valueOf(0.3)));
        rangeMap.put(Range.openClosed(20, 30), Pair.of(BigDecimal.valueOf(800000), BigDecimal.valueOf(0.35)));
        rangeMap.put(Range.openClosed(30, 50), Pair.of(BigDecimal.valueOf(10000000), BigDecimal.valueOf(0.40)));
        rangeMap.put(Range.openClosed(50, 80), Pair.of(BigDecimal.valueOf(60000000), BigDecimal.valueOf(0.50)));

        var pair = rangeMap.get(3);
        System.out.println("pair=" + pair.getLeft() + ";" + pair.getRight());
    }


    @Test
    void liveWin8GamesInARow() {
        //promotionsJob.liveWin8GamesInARow("");
        promotionsJob.rebateAllGames("");
        promotionsJob.agent("");
        promotionsJob.vipGrowUp("");

        BiConsumer<Long, Object> consumer = (orderId, iService1) -> {
            var jsonObject = new JSONObject();
            jsonObject.put("id", orderId);
            jsonObject.put("status", 3);
            jsonObject.put("info", "4444");
            ((IService) iService1).updateById(jsonObject);
        };
        consumer.accept(3L, betSlipsExceptionServiceImpl);
        ZonedDateTime now = ZonedDateTime.now();
        var startTime = (int) now.minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0).toEpochSecond();
        var endTime = (int) now.withHour(0).withMinute(0).withSecond(0).withNano(0).toEpochSecond();
        System.out.println(DateUtils.yyyyMMddHHmmss(startTime));
        System.out.println(DateUtils.yyyyMMddHHmmss(endTime));


        BiConsumer<String, Exception> biConsumer = (message, exception) -> {
            XxlJobLogger.log(message + exception);
            StackTraceElement[] stackTrace = exception.getStackTrace();
            for (StackTraceElement element : stackTrace) {
                XxlJobLogger.log(element.toString());
            }
        };

        try {
            var list = Lists.newArrayList("1", "2");
            list.forEach(x -> {
                System.out.println("x=" + x);
                var i = 1 / 0;
            });
        } catch (Exception e) {
            biConsumer.accept("forEach exception", e);
        }

    }

    int now = DateUtils.getCurrentTime();
    ZonedDateTime zoneNow = DateNewUtils.utc8Zoned(now);
    int startTime = (int) zoneNow.minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0).toEpochSecond();
    int endTime = (int) zoneNow.withHour(0).withMinute(0).withSecond(0).withNano(0).toEpochSecond();


    @SneakyThrows
    @Test
    void testLiveDailyBonus() {
        var zoneTime = DateNewUtils.utc8Zoned((int) (1598803202000L / 1000L));
        var formatTime = DateNewUtils.utc8Str(zoneTime, DateNewUtils.Format.yyyy_MM_dd_HH_mm_ss);
        var createdAt = DateNewUtils.oriTimeZoneToBeiJingZone(formatTime, DateNewUtils.Format.yyyy_MM_dd_HH_mm_ss, "UTC", "-4");

        iPromotionsTaskServiceImpl.agent(1);
        iPromotionsTaskServiceImpl.inviteFriends();
        iPromotionsTaskServiceImpl.liveDailyBonus();
        iPromotionsTaskServiceImpl.vipGrowUp();
        iPromotionsTaskServiceImpl.rebateAllGames();

        var list = List.of(1, 2, 4, 6, 7, 11);
        var flag = list.stream().allMatch(x -> !x.equals(0));


        Table<Integer, Integer, Integer> table = HashBasedTable.create();
        table.put(1, 2, 3);
        table.put(1, 2, 4);
        table.put(2, 3, 3);
        table.put(2, 3, 5);
        var columnMap = table.columnMap();
        var rowMap = table.rowMap();
        rowMap.forEach((key, value) -> {
            System.out.println("key=" + key);
            System.out.println("value=" + value);
        });
        System.out.println("columnMap=" + columnMap);
        System.out.println("rowMap=" + rowMap);
        Supplier<User> userSupplier = User::new;
        userSupplier.get().getUsername();
        // List<Integer> list = new ArrayList<>();


        System.out.println("startTime=" + DateUtils.yyyyMMddHHmmss(startTime));
        System.out.println("endTime=" + DateUtils.yyyyMMddHHmmss(endTime));

        System.out.println("now1=" + DateUtils.yyyyMMddHHmmss(now));
        now = DateNewUtils.now();
        Thread.sleep(5000);
        System.out.println("now2=" + DateUtils.yyyyMMddHHmmss(now));
        now = DateNewUtils.now();
        Thread.sleep(5000);
        System.out.println("now3=" + DateUtils.yyyyMMddHHmmss(now));
    }

    public static int getSupplier(final User user) {
        return 1;
    }

    @Test
    void testMonthly() {
        var zoneTime = DateNewUtils.utc8Zoned(DateNewUtils.now()).minusMonths(1);
        var yearMonth = DateNewUtils.utc8Str(zoneTime, DateNewUtils.Format.yyyyMMdd).substring(0, 6);
    }

    @Test
    void testRevenue() {
        // dailyReportJob.revenueReport("");
        // iPromotionsTaskServiceImpl.vipGrowUp();
        promotionsTaskBase.handlerVipRelegation(12);
    }

    @Test
    void testNotify() {
        paymentNotifyJob.withdrawNotifyUrl("{\"code\":\"mango_pay\"}");
    }

    @Test
    void testdel() {
        paymentNotifyJob.delUnfinishedDeposit("{\"code\":\"mango_pay\"}");
    }

    @Test
    void checkMaintenance() {
        platPullDataJob.checkMaintenance("{\"code\":\"mango_pay\"}");
    }

    @Test
    @SneakyThrows
    void future() {
        var path = File.separator + "Users" + File.separator + "mac" + File.separator + "Desktop" + File.separator + "future.txt";
        File file = new File(path);
        FileReader fileReader = new FileReader(file);
        BufferedReader reader = new BufferedReader(fileReader);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line + '\n');
        }
        reader.close();
        // futuresLotteryServiceImpl.saveOrUpdateBatch(sb.toString());
    }

    @Test
    void flowCommission() {
        var param = "{\"count\":5}";
        promotionsJob.flowCommission(param);
    }
}
