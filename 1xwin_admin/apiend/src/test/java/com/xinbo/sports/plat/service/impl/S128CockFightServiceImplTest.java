package com.xinbo.sports.plat.service.impl;

import com.xinbo.sports.apiend.ApiendApplication;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams;
import com.xinbo.sports.plat.service.impl.s128.S128CockFightServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApiendApplication.class)
public class S128CockFightServiceImplTest {
    @Autowired
    private S128CockFightServiceImpl s128CockfightService;

    @Test
    public void CheckTransaction() {
        s128CockfightService.checkTransferStatus("555556hhhh");
    }

    @Test
    public void queryBalace() {
        PlatFactoryParams.PlatQueryBalanceReqDto xbttestkk222 = PlatFactoryParams.PlatQueryBalanceReqDto.builder().username("xbttes000tkk222").build();
        s128CockfightService.queryBalance(xbttestkk222);
    }

    @Test
    public void register() {
        PlatFactoryParams.PlatRegisterReqDto xbttestkk222 = PlatFactoryParams.PlatRegisterReqDto.builder().username("xbttes00290").build();
        s128CockfightService.registerUser(xbttestkk222);
    }

    @Test
    public void Login() {
        PlatFactoryParams.PlatLoginReqDto dto = PlatFactoryParams.PlatLoginReqDto.builder().username("suki001").device("m").lang("en").build();
        s128CockfightService.login(dto);
    }

    @Test
    public void deposit() {
        PlatFactoryParams.PlatCoinTransferReqDto dto = PlatFactoryParams.PlatCoinTransferReqDto.builder()
                .orderId("555556hhhh").coin(BigDecimal.TEN).username("suki001").build();
        s128CockfightService.coinUp(dto);
    }

    @Test
    public void pullData() {
        s128CockfightService.pullBetsLips();//https://css.cfb2.net/api/auth_login.aspx
       /* final RestTemplate restTemplate = new RestTemplate();
        final HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        final CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setRedirectStrategy(new LaxRedirectStrategy())
                .build();
        factory.setHttpClient(httpClient);
        restTemplate.setRequestFactory(factory);
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<String, Object>();
        paramMap.add("lang", "zh-CN");
        paramMap.add("login_id", "xbttes000tkk222");
        paramMap.add("session_id", "66266AD0FB10465494759543AC9DC577");

        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Agent", "Chrome/69.0.3497.81 Safari/537.36");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<MultiValueMap<String, Object>>(paramMap, headers);
        ResponseEntity response = restTemplate.postForEntity("https://css.cfb2.net/api/auth_login.aspx", httpEntity, String.class);
*/
    }
    //   String link = "https://api2288.cfb2.net/api/auth_login.aspx";
    //   String response = readResponse(doHttpPost(link, "usernamebox=BLOB&passwordbox=password"));

}




