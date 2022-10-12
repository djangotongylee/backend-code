package com.xinbo.sports.plat.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xinbo.sports.apiend.ApiendApplication;
import com.xinbo.sports.dao.generator.po.GameSlot;
import com.xinbo.sports.dao.generator.service.impl.GameSlotServiceImpl;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams;
import com.xinbo.sports.plat.service.impl.mg.MGGameServiceImpl;
import com.xinbo.sports.plat.service.impl.upg.UPGGameServiceImpl;
import com.xinbo.sports.utils.DateUtils;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static com.alibaba.fastjson.JSON.parseArray;
import static com.alibaba.fastjson.JSON.parseObject;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApiendApplication.class)
class MGGameServiceImplTest {

    @Autowired
    private MGGameServiceImpl mgGameService;
    @Autowired
    private GameSlotServiceImpl gameSlotService;
    @Autowired
    private UPGGameServiceImpl upgService;

    private String API_URL = "https://api-xinbo.k2net.io";
    private String Token_URL = "https://sts-xinbo.k2net.io";
    private String client_id = "XinboTest";
    private String client_secret = "6962aaf008c44317a7f59483e095c7";
    private String grant_type = "client_credentials";
    RestTemplate restTemplate = new RestTemplate();

    @Test
    void contextLoads() throws Exception {
        //mgGameService.genSupplementsOrders(PlatFactoryParams.GenSupplementsOrdersReqDto.builder().start(1597653987).end(1597653987).build());
//        mgGameService.betsRecordsSupplemental(PlatFactoryParams.BetsRecordsSupplementReqDto.builder().requestInfo("{\"limit\":1000,\"startingAfter\":\"03CEC6F80172410B00010000000185703367\"}_1597653987").build());

        // upgService.pullGames();
        mgGameService.pullBetsLips();

       /* doLogin();
        System.out.println(99999);*/

    }

    @Test
    void checkTransaction() {
        // mgGameService.coinUp(PlatFactoryParams.PlatCoinTransferReqDto.builder().username("suki001").coin(BigDecimal.TEN).orderId("99999999").build());
        mgGameService.checkTransferStatus("83306959897497600");
        //upgService.checkTransaction(PlatFactoryParams.PlatCheckTransferReqDto.builder().orderId("77777").build());

    }

    //    @Test
//    void getGameListByExcel() {
//        JSONObject jsonObject =new JSONObject();
//        // List<String> list = readExcelUtils.readExcel(null);
//        List<String> list = new LinkedList<>();
//        for (var x: list) {
//            JSONArray jsonArray = JSONObject.parseArray(x);
//            for (int i = 0; i < jsonArray.size(); i++) {
//                jsonObject.put(String.valueOf(i), jsonArray.get(i));
//            }
//            if (jsonObject.get(5).equals("Desktop Flash")){
//                continue;
//            }
//            if(jsonObject.get(6).equals("Slot")){
//                GameSlot gameSlot = new GameSlot();
//                gameSlot.setGameId(203);
//                gameSlot.setStatus(1);
//                gameSlot.setId(String.valueOf(jsonObject.get("2")));
//                gameSlot.setKeyName(jsonObject.getString("1"));
//                gameSlot.setName(jsonObject.getString("1"));
//                gameSlot.setImg(String.format("/icon/mg/%s.png", jsonObject.getString("1").replace(" ","")));
//                gameSlot.setGameTypeName(jsonObject.getString("6"));
//                gameSlot.setGameTypeId(jsonObject.getString("6"));
//                gameSlot.setNameZh(jsonObject.getString("3"));
//                gameSlot.setCreatedAt(DateUtils.getCurrentTime());
//                gameSlot.setUpdatedAt(DateUtils.getCurrentTime());
//                gameSlot.setDevice(0);
//                gameSlot.setSort(1);
//                gameSlot.setIsNew(0);
//                gameSlotService.saveOrUpdate(gameSlot);            }
//        }
//    }
    private void getGameList() {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsImtpZCI6IjVDRThDMDFBNDMwRDRFOUE0MzgyN0YzRTAzQjQ5Njg2Q0IxMjE0MkQiLCJ0eXAiOiJKV1QiLCJ4NXQiOiJYT2pBR2tNTlRwcERnbjgtQTdTV2hzc1NGQzAifQ.eyJuYmYiOjE1OTY3Njc3ODQsImV4cCI6MTU5Njc3MTM4NCwiaXNzIjoiaHR0cHM6Ly9zdHMuazJuZXQuaW8iLCJhdWQiOlsiaHR0cHM6Ly9zdHMuazJuZXQuaW8vcmVzb3VyY2VzIiwiazIiXSwiY2xpZW50X2lkIjoiWGluYm9UZXN0IiwiY2xpZW50X2FnZW50IjoiWGluYm9UZXN0IiwiY2xpZW50X29wZXJhdGlvbiI6ImFueSIsImNsaWVudF9hbGxvd2VkSG9zdCI6ImFwaS14aW5iby5rMm5ldC5pbyIsInNjb3BlIjpbImFwaSJdfQ.4bwF-NViX3JPD_WMbuBiOdqXNlQAyR2tSm1k2ahHGFbnR_YpveCfrhr4tXJVme_wG0gb8s_ZjBygPFYN9teKZHli6NsAQ2MqVzECpgzSN86ELCnhEBi1tdRFktKkEDbA1mUlD6h3HKhw-otULZgB0bEvGqKIGg-7-9w8kxOHmkC3GQadMbmjTDv4LnfudhU3ZOnk4yz4lsVeXFvgYrwVUCl_FA-SfizdFhTlIk7nOlvbF6yV_TlkndIh0o8HD0E6h9SScKrpPajrdM8EVDy0-kKwKSDrr5teZtGtTDAibkQxrEq7uzzTR_0szhj1s-1ykmSeOXdVYpxnJFPPSWCfrx3FfAIHvjMFLNGlox5VUMPukPt2EbCA28wZd2ihg_uH9IYBZv3Dg-uRjkJM8hgHXHABQOcQRyg6D3Wm1InBIx0P28zF7BcpSQKD2tnu33uAUjzzZeV3aF_mp92egtckU4YVwQ5QcAp7AZs7wAOpZ2MLZaW_T-en4Fng4uhcGlslZ_hrpInMwKg5SEp85qj_1uauMuvNrA2HySiM_LbI3bKGDxTY8tHfe64Xa5qj4fahaFbviXvmwN5EehIr75L88etFoLYAVHzc_WaqYU7s27qnBkdBBa1TpKdutm9Chevw1hYkWMlTvdNyYlH7HG67DQEXaEJjFPDkFyjbGToXyxk");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        //HttpEntity
        Map<String, String> params = new HashMap<>();
        params.put("agentCode", client_id);
        HttpEntity<String> requestEntity = new HttpEntity<String>(headers);
        ResponseEntity<String> response = restTemplate.exchange(API_URL + "/api/v1/agents/XinboTest/games?agentCode={agentCode}", HttpMethod.GET, requestEntity, String.class, params);
        JSONArray objects = parseArray(response.getBody());
        if (objects != null && !objects.isEmpty()) {
            GameSlot gameSlot = new GameSlot();
            var device = 0;
            for (var x : objects) {
                JSONObject jo = (JSONObject) x;
                if (jo.get("channelCode").equals("SLOTS")) {
                    if (jo.get("platform").equals("Multi-Platform")) {
                        device = 0;
                    } else if (jo.get("platform").equals("Desktop")) {
                        device = 1;
                    } else {
                        device = 2;
                    }
                    gameSlot.setGameId(203);
                    gameSlot.setStatus(1);
                    gameSlot.setId((String) jo.get("gameCode"));
                    gameSlot.setName((String) jo.get("gameName"));
                    gameSlot.setImg(String.format("/icon/mg/%s.png", gameSlot.getId().split("_")[1]));
                    gameSlot.setGameTypeName(String.valueOf(jo.get("gameCategoryName")));
                    gameSlot.setGameTypeId((String) jo.get("gameCategoryCode"));
                    gameSlot.setNameZh(jo.getJSONArray("translatedGameName").getJSONObject(0).getString("value"));
                    gameSlot.setCreatedAt(DateUtils.getCurrentTime());
                    gameSlot.setUpdatedAt(DateUtils.getCurrentTime());
                    gameSlot.setDevice(device);
                    gameSlot.setSort(jo.getInteger("sorting"));
                    gameSlot.setIsNew(0);
                    gameSlotService.saveOrUpdate(gameSlot);
                }

            }


        }


    }


    private String getToken() {

        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<String, Object>();
        paramMap.add("grant_type", grant_type);
        paramMap.add("client_id", client_id);
        paramMap.add("client_secret", client_secret);

        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Agent", "Chrome/69.0.3497.81 Safari/537.36");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<MultiValueMap<String, Object>>(paramMap, headers);
        String result = restTemplate.postForObject(Token_URL + "/connect/token", httpEntity, String.class);
        parseObject(result);
        String access_token = null;
        if (result.contains("access_token")) {
            access_token = result.split(":")[1].split(",")[0];
        }
        System.out.println("result2====================" + result);


        return access_token;
    }

    private void doLogin() {
        PlatFactoryParams.PlatQueryBalanceReqDto dto = PlatFactoryParams.PlatQueryBalanceReqDto.builder()
                .username("suki02ethttge").device("d").lang("VI").build();
        mgGameService.queryBalance(dto);
    }

}


