package com.xinbo.sports.plat.service.impl;

import com.xinbo.sports.dao.generator.po.BetslipsAeSexy;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams;
import com.xinbo.sports.plat.service.impl.ae.SexyLiveServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @author: David
 * @date: 14/08/2020
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
class AEServiceImplTest {
    @Resource
    private SexyLiveServiceImpl sexyLiveServiceImpl;
    @Resource
    private AEServiceImpl aeServiceImpl;

    @Test
    void registerUser() {
        PlatFactoryParams.PlatRegisterReqDto build = PlatFactoryParams.PlatRegisterReqDto.builder().username("testaaa").device("p").lang("th").build();

        aeServiceImpl.registerUser(build);
    }

    @Test
    void buildConfig() {
    }

    @Test
    void login() {
    }

    @Test
    void logout() {
    }

    @Test
    void coinUp() {
    }

    @Test
    void coinDown() {
    }

    @Test
    void queryBalance() {
    }

    @Test
    void pullBetsLips() {
        sexyLiveServiceImpl.pullBetsLips();
    }

    @Test
    void checkTransferStatus() {
    }

    @Test
    void betsRecordsSupplemental() {
    }

    @Test
    void doSend() {
    }
}