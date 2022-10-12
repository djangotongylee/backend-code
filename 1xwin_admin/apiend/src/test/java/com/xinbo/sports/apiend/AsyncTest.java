package com.xinbo.sports.apiend;

import com.xinbo.sports.apiend.base.GameCommonBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author: wells
 * @date: 2020/7/13
 * @description:
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApiendApplication.class)
public class AsyncTest {
    @Autowired
    private AsyncObject asyncObject;
    @Autowired
    private GameCommonBase gameCommonBase;

    @Test
    public void testAsync() {
        reAsync();
        //System.out.println("reStr=" + reStr);
    }

    public void reAsync() {
        gameCommonBase.createThirdUser("wells0001");
        while (true) {
            System.out.println("j=");
        }
    }
}
