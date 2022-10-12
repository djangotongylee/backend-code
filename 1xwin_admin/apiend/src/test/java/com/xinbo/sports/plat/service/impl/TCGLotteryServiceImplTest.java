package com.xinbo.sports.plat.service.impl;

import com.xinbo.sports.apiend.ApiendApplication;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams;
import com.xinbo.sports.plat.service.impl.tcg.TCGLotteryServiceImpl;
import com.xinbo.sports.utils.DESCUtils;
import com.xinbo.sports.utils.HashUtil;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApiendApplication.class)
class TCGLotteryServiceImplTest {

    @Autowired
    private TCGLotteryServiceImpl tcgLotteryService;


    private String API_URL = "http://www.connect6play.com/doBusiness.do";
    private String Merchant_Code = "xinbothb";
    private String DES_Key = "Rf9pKktQ";
    private String SHA256_Key = "H3QrlBK0Tn5gr5Bx";
    private String requestJson = "{\"method\":\"cm\",\"username\":\"phoenix\",\"password\":\"1q2w3e4r\",\"currency\":\"THB\"}";


    @Test
    void checkTransaction() {
        //tcgLotteryService.betsRecordsSupplemental(PlatFactoryParams.BetsRecordsSupplementReqDto.builder().start(1597569000).end(1597639800).build());
    }

    @Test
    void contextLoads() throws Exception {
        // addDay("20200131");
        pullData();
        System.out.println(99999);
        tcgLotteryService.checkTransferStatus("dddd999");


    }

    public static String addDay(String s) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            Calendar cd = Calendar.getInstance();
            cd.setTime(sdf.parse(s));
            cd.add(Calendar.DATE, 1);//增加一天
            return sdf.format(cd.getTime());
        } catch (Exception e) {
            return null;
        }
    }

    private void pullData() {
        tcgLotteryService.pullBetsLips();
    }

    private void doLogin() {
        PlatFactoryParams.PlatLoginReqDto dto = PlatFactoryParams.PlatLoginReqDto.builder()
                .username("btxxctes78811").device("d").lang("VI").build();
        tcgLotteryService.login(dto);
    }

    //tcg lottery cm
    private String doCreate() {
        requestJson = "{method:cm,username:phoenix,password:1q2w3e4r,currency:THB}";

        return null;
    }

    private void doEncrypt(String json) throws Exception {
        RestTemplate restTemplate = new RestTemplate();

        // 参数加密
        DESCUtils des = new DESCUtils(DES_Key);
        String encryptedParams = des.encrypt(json);
        //签名档加密
        String sign = HashUtil.sha256(encryptedParams + SHA256_Key);
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<String, Object>();
        paramMap.add("merchant_code", URLEncoder.encode(Merchant_Code, "UTF-8"));
        paramMap.add("params", URLEncoder.encode(encryptedParams, "UTF-8"));
        paramMap.add("sigh", URLEncoder.encode(sign, "UTF-8"));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<MultiValueMap<String, Object>>(paramMap, headers);
//
//        //组连接字串
//        String data = "merchant_code="+ URLEncoder.encode(Merchant_Code,"UTF-8")
//                + "&params="+URLEncoder.encode(encryptedParams,"UTF-8")
//                + "&sign="+URLEncoder.encode(sign,"UTF-8");
//        System.out.println(data);
        String result = restTemplate.postForObject(API_URL, httpEntity, String.class);
        System.out.println(result);


    }
//    public void getGameList()  {
//        var gameListBO = TCGLotteryRequestParameter.GetGameList.builder()
//                .method(TCGPlatEnum.TCGMethodEnum.GLGL.getMethodName()).lottoType("SEA_Tradition").build();
//        JSONObject call = send(TCGPlatEnum.TCGMethodEnum.GLGL, paramMap.get(API_URL), gameListBO);
//        if (call.get("status").equals(0)) {
//            DictItem dictItem = new DictItem();
//            JSONArray games = call.getJSONArray("games");
//            if (games != null && !games.isEmpty()) {
//                for (var x : games) {
//                    JSONObject jo = (JSONObject) x;
//                    JSONArray games1 = jo.getJSONArray("games");
//                    if (games1 != null && !games1.isEmpty()) {
//                        for (var g : games1) {
//                            dictItem.setReferId(95);
//                            dictItem.setStatus(1);
//                            dictItem.setCreatedAt(DateUtils.getCurrentTime());
//                            dictItem.setUpdatedAt(DateUtils.getCurrentTime());
//                            JSONObject o = (JSONObject) g;
//                            dictItem.setSort(o.getInteger("sorting"));
//                            dictItem.setCode(o.getString("gameId"));
//                            dictItem.setTitle(o.getString("remark"));
//                            dictItemServiceImpl.save(dictItem);
//                        }
//                    }
//
//                }
//
//            }
//        }
    // }

}