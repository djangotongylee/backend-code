package com.xinbo.sports.plat.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.xinbo.sports.apiend.ApiendApplication;
import com.xinbo.sports.apiend.base.GameCommonBase;
import com.xinbo.sports.dao.generator.service.BetslipsGgService;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApiendApplication.class)
class GGFishingServiceImplTest {
    @Autowired
    private GGServiceImpl GGServiceImpl;
    @Autowired
    private BetslipsGgService betslipsGgServiceImpl;
    @Autowired
    private GameCommonBase gameCommonBase;
    @Autowired
    private IBCServiceImpl IBCServiceImpl;


    JSONObject jsonObject = new JSONObject() {{
        put("username", "wells0001");
    }};

    @Test
    void login() {
        //String address = gGFishingServiceImpl.login(jsonObject).getData();
        //System.out.println("address==" + address);
        // var repDto = PlatFactoryParams.PlatRegisterReqDto.builder().username("ggtest0001").device("d").lang("en").build();
        //  GGServiceImpl.registerUser(repDto);

        //GGServiceImpl.checkTransferStatus("8330857873106944");
        // IBCServiceImpl.checkTransferStatus("83309163408658432");
        //BBINServiceImpl.checkTransferStatus("83309499108167680");
        GGServiceImpl.pullBetsLips();
    }

    //    @Test
//    void logout() {
//        gGFishingServiceImpl.logout(jsonObject);
//    }
//
//    @Test
//    void queryBalance() {
//        Object dbbalance = gGFishingServiceImpl.queryBalance(jsonObject).getData();
//        System.out.println("dbbalance=" + dbbalance);
//    }
//
//    @Test
//    void coinUp() {
//        jsonObject.put("coin", new BigDecimal(1000));
//        gGFishingServiceImpl.coinUp(jsonObject);
//    }
//
//    @Test
//    void coinDown() {
//        jsonObject.put("coin", new BigDecimal(100));
//        gGFishingServiceImpl.coinDown(jsonObject);
//    }
//
//    @Test
//    void records() {
//    }
//
    @Test
    void getBetListByDate() {
        GGServiceImpl.pullBetsLips();
    }
//
//    @Test
//    void getHistoryBetList() {
//        //gGFishingServiceImpl.getHistoryBetList();
//        String startTime = "2020-05-23 14:25:00";
//        String endTime = "2020-05-26 14:35:00";
//        var pairList = new ArrayList<Pair<String, String>>();
//        int intervalDate = 20 * 60 * 1000;
//        long startDate = DateUtils.yyyyMMddHHmmss(startTime).getTime();
//        long endDate = DateUtils.yyyyMMddHHmmss(endTime).getTime();
//        int difference = (int) (endDate - startDate);
//        if (difference < intervalDate) {
//            pairList.add(Pair.of(startTime, endTime));
//            return;
//        }
//        for (int i = 1; ; i++) {
//            if (difference > i * intervalDate) {
//                long startDateTemp = startDate + (i - 1) * intervalDate;
//                long endDateTemp = startDate + i * intervalDate;
//                pairList.add(Pair.of(DateUtils.yyyyMMddHHmmss(startDateTemp), DateUtils.yyyyMMddHHmmss(endDateTemp)));
//            } else {
//                long startDateTemp = startDate + i * intervalDate;
//                pairList.add(Pair.of(DateUtils.yyyyMMddHHmmss(startDateTemp), endTime));
//                break;
//            }
//        }
//
//    }
//
//    @Test
//    void getCoinStatisticsByDate() {
//        var dto = PlatFactoryParams.GameQueryDateDto.builder()
//                .startTime(1590510381)
//                .endTime(1591097815)
//                .uid(25)
//                .build();
//        gGFishingServiceImpl.getCoinStatisticsByDate(dto);
//    }

    @Test
    void getCoinStatisticsByDate() {
        // var dto = new PlatFactoryParams.PlatGameQueryDateDto();
        //gGFishingServiceImpl.getCoinStatisticsByDate(dto);
        //gGFishingServiceImpl.pullBetsLips();
        // CoinStatisticsBase.getCoinByBet(new PlatFactoryParams.PlatGameQueryDateDto(), betslipsGgServiceImpl);
        var platGameQueryDateDto = new PlatFactoryParams.PlatGameQueryDateDto();
        platGameQueryDateDto.setGameId(101);
        //gameCommonBase.getCoinStatisticsByDate(platGameQueryDateDto);

        ReqPage<PlatFactoryParams.PlatGameQueryDateDto> t = new ReqPage<PlatFactoryParams.PlatGameQueryDateDto>();
        t.setData(platGameQueryDateDto);
        var res = gameCommonBase.getBetsRecords(t);
        res.getCurrent();
    }

    @Test
    public void testLocalDate() {
        //时区转换
        LocalDateTime localDateTime = LocalDateTime.now();
        var time = LocalDateTime.now(ZoneId.of("-4"));
        var zdt = time.atZone(ZoneId.systemDefault());
        Date date = Date.from(zdt.toInstant());
        System.out.println("time=" + time);
        System.out.println("date1=" + date.getTime());
        System.out.println("date=" + date);
    }
}