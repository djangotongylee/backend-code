//package com.xinbo.sports.plat.service.impl.bbin;
//
//import com.alibaba.fastjson.JSONObject;
//import com.xinbo.sports.apiend.ApiendApplication;
//import com.xinbo.sports.plat.io.bo.PlatFactoryParams;
//import com.xinbo.sports.plat.service.impl.BBINServiceImpl;
//import org.junit.jupiter.api.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import java.io.File;
//
//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = ApiendApplication.class)
//class BBINLiveServiceImplTest {
//    @Autowired
//    private BBINLiveServiceImpl bBINLiveServiceImpl;
//    @Autowired
//    private BBINGameServiceImpl bBINGameServiceImpl;
//    @Autowired
//    private BBINSportsServiceImpl bBINSportsServiceImpl;
//    @Autowired
//    private BBINNewSportsServiceImpl bBINewSportsServiceImpl;
//    @Autowired
//    private BBINServiceImpl bBINServiceImpl;
//    @Autowired
//    private BBINFishingMastersServiceImpl bBINFinishMastersServiceImpl;
//    @Autowired
//    private BBINFishingExpertServiceImpl bBINFinishExpertServiceImpl;
//
//
//    JSONObject jsonObject = new JSONObject() {{
//        put("username", "wells0005");
//    }};
//
//    @Test
//    void login() {
////        bBINFinishMastersServiceImpl.login(jsonObject);
////        bBINFinishExpertServiceImpl.login(jsonObject);
////        bbinLiveServiceImpl.login(jsonObject);
////        bBINGameServiceImpl.login(jsonObject);
//        //bBINSportsServiceImpl.login(jsonObject);
//        //bBINewSportsServiceImpl.login(jsonObject);
//    }
//
//    //
////    @Test
////    void logout() {
////    }
////
////    @Test
////    void registerAgent() {
////        bBINLiveServiceImpl.registerAgent(jsonObject);
////    }
////
////    @Test
////    void registerUser() {
////    }
////
////   @Test
////   void coinUp() {
////       jsonObject.put("coin", 1000);
////       bBINLiveServiceImpl.coinUp(jsonObject);
////   }
////
////   @Test
////   void coinDown() {
////       jsonObject.put("coin", 100);
////       bBINLiveServiceImpl.coinUp(jsonObject);
////   }
////
////   @Test
////   void records() {
////   }
////
////   @Test
////   void queryBalance() {
////       bBINLiveServiceImpl.queryBalance(jsonObject);
////   }
////
//    @Test
//    void getBetListByDate() {
//        bBINewSportsServiceImpl.pullBetsLips();
//        //bBINFinishMastersServiceImpl.pullBetsLips();
//        // bBINFinishExpertServiceImpl.getBetListByDate();
//    }
////
////   @Test
////   void createSession() {
////       bBINLiveServiceImpl.createSession(jsonObject);
////   }
//
//
////   @Test
////   void getCoinStatisticsByDate() {
////       var dto = PlatFactoryParams.GameQueryDateDto.builder()
////               .startTime(1590510381)
////               .endTime(1591097815)
////               .uid(26)
////               .build();
////       bBINLiveServiceImpl.getCoinStatisticsByDate(dto);
////   }
//
//    @Test
//    void pic() {
//        File file = new File("D:\\icon");
//        File[] tempList = file.listFiles();
//        for (int i = 0; i < tempList.length; i++) {
//            var fileName = tempList[i].getName();
//            System.out.println(tempList[i].getName());
//            if (fileName.contains("_")) {
//                var desFile = "D:\\icon\\" + fileName.split("_")[1];
//                tempList[i].renameTo(new File(desFile));
//            }
//        }
//    }
//
//}