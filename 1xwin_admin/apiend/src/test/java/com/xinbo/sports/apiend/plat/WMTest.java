package com.xinbo.sports.apiend.plat;

import com.alibaba.fastjson.JSONObject;
import com.xinbo.sports.plat.service.impl.wm.WMLiveServiceImpl;
import com.xinbo.sports.utils.components.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.LinkedHashMap;

/**
 * <p>
 * WM真人
 * </p>
 *
 * @author andy
 * @since 2020/5/19
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class WMTest {
    @Resource
    private WMLiveServiceImpl wMAliveServiceImpl;

    /**
     * 注册会员
     */
    @Test
    public void registerUser() {
        JSONObject params = new JSONObject(new LinkedHashMap());
        params.put("user", "user18454851234560015");
        // 密码   最小值:6字符   最大值:64字符
        params.put("password", "user18454851234560011");
        // 姓名   最大值:30字符
        params.put("username", "user18454851234560011");
        // 0:中文, 1:英文
        params.put("syslang", 0);
    }

    /**
     * 登录
     */
    @Test
    public void login() {
        JSONObject params = new JSONObject(new LinkedHashMap());
        params.put("user", "user18454851234560015");
        params.put("password", "user18454851234560011");
        //params.put("lang", 9);
        //params.put("mode", "onlybac");
        //print(wMAliveServiceImpl.login(params));
    }

    //    /**
//     * 登出游戏
//     */
//    @Test
//    public void logout() {
//        JSONObject params = new JSONObject(new LinkedHashMap());
//        params.put("user", "user18454851234560015");
//        // 密码   最小值:6字符   最大值:64字符
//        // 0:中文, 1:英文
//        params.put("syslang", 0);
//        print(wMAliveServiceImpl.logout(params));
//    }
//    /**
//     * 游戏纪录
//     */
    @Test
    public void getBetListByDate() {
        wMAliveServiceImpl.pullBetsLips();
    }

    @Test
    public void checkTransferStatus() {
        boolean flag = wMAliveServiceImpl.checkTransferStatus("83296946416128000");
        log.info("result={}", flag);
    }

    private void print(Result result) {
        if (null != result) {
            log.info("[code=" + result.getCode() + ",msg=" + result.getMsg() + ",data=" + result.getData() + "]");
        }
    }
}
