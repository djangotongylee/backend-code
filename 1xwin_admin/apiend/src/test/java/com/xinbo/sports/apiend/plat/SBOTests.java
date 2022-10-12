package com.xinbo.sports.apiend.plat;

import com.alibaba.fastjson.JSONObject;
import com.xinbo.sports.plat.io.bo.SBOSportsRequestParameter;
import com.xinbo.sports.plat.service.impl.sbo.SBOSportsServiceImpl;
import com.xinbo.sports.utils.components.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.ExecutionException;

/**
 * @author: David
 * @date: 02/05/2020
 * @description:
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class SBOTests {
    @Autowired
    SBOSportsServiceImpl sboSportsServiceImpl;

    @Test
    public void registerAgent() throws ExecutionException, InterruptedException {
        Boolean booleanResult = sboSportsServiceImpl.registerAgent();
    }

    @Test
    public void registerPlayer() {
        JSONObject params = new JSONObject();
        params.put("agent", "xbagent2020");
        params.put("username", "xbsports001");
        // Result<Boolean> booleanResult = sboSportsServiceImpl.registerUser(params);
    }

    @Test
    public void pullBetsLips() throws Exception {
        sboSportsServiceImpl.pullBetsLips();
    }

    //@Test
    //public void login(){
    //    PlatRequestParameter.Login m = PlatRequestParameter.Login
    //            .builder()
    //            .username("xbsports011")
    //            .lang("zh_tw")
    //            .device("m")
    //            .build();

    //    Result<String> booleanResult = sboSportsServiceImpl.login(m);
    //}
}
