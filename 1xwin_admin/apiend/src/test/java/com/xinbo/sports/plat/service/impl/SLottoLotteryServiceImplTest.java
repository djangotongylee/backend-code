package com.xinbo.sports.plat.service.impl;

import com.xinbo.sports.apiend.ApiendApplication;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams;
import com.xinbo.sports.plat.service.impl.slotto.SLottoLotteryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApiendApplication.class)
class SLottoLotteryServiceImplTest {

    @Autowired
    private SLottoLotteryServiceImpl sLottoService;


    private String API_URL = "http://api.s-lotto.com";
    private String apiuser = "igkxbsport";
    private String apipass = "b5BYe5nnbT";
    private String user = "testplzz003";
    private String pass = "123567";


    @Test
    void checkTransaction() {
        sLottoService.checkTransferStatus("77777");

    }

    @Test
    void contextLoads() throws Exception {
        // CREATEPLAYER();
        // doLogin();
        //  queryBalance();
        //  betLogin();
        //MD5.encryption("xbttestkk").substring(0,8);
        // coinUp();
        pulldata();
    }

    private void pulldata() {
        sLottoService.genSupplementsOrders(PlatFactoryParams.GenSupplementsOrdersReqDto.builder().start(1597653987).end(1597653987).build());
        sLottoService.betsRecordsSupplemental(PlatFactoryParams.BetsRecordsSupplementReqDto.builder().requestInfo("{\"apipass\":\"fytS2WnoOU\",\"apiuser\":\"apitest\",\"dateFrom\":\"2020-07-17\",\"dateTo\":\"2020-08-17\",\"pass\":\"123567\",\"user\":\"zzzzzzzzz\",\"currentPage\":\"%s\"}").build());
        sLottoService.pullBetsLips();
    }

    private void coinUp() {
        PlatFactoryParams.PlatCoinTransferReqDto reqDto = PlatFactoryParams.PlatCoinTransferReqDto.builder()
                .coin(BigDecimal.ZERO).username("btxxctdddde7").orderId("45566666").build();
        sLottoService.coinUp(reqDto);
    }

//    private void betLogin() {
//        PlatFactoryParams.PlatLoginReqDto dto = PlatFactoryParams.PlatLoginReqDto.builder()
//                .username("btxxcte7").device("d").lang("VI").build();
//        sLottoService.betLogin(dto);
//    }

    private void queryBalance() {
        PlatFactoryParams.PlatQueryBalanceReqDto reqDto = PlatFactoryParams.PlatQueryBalanceReqDto.builder()
                .username("jkkk09jjj").build();
        sLottoService.queryBalance(reqDto);
    }

    private void CREATEPLAYER() {
        PlatFactoryParams.PlatRegisterReqDto reqDto = PlatFactoryParams.PlatRegisterReqDto.builder()
                .username("jkkk09jjj").build();
        sLottoService.registerUser(reqDto);
    }

    private void doLogin() {
        PlatFactoryParams.PlatLoginReqDto dto = PlatFactoryParams.PlatLoginReqDto.builder()
                .username("btxxcte7").device("d").lang("VI").build();
        sLottoService.login(dto);
    }


}