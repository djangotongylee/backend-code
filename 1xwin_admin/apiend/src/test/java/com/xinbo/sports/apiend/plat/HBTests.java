package com.xinbo.sports.apiend.plat;

import com.xinbo.sports.plat.service.impl.habanero.HabaneroGameServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author: David
 * @date: 02/05/2020
 * @description:
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class HBTests {
    @Autowired
    HabaneroGameServiceImpl habaneroGameServiceImpl;

    @Test
    public void getGames() {
        habaneroGameServiceImpl.pullGames();
    }

    @Test
    public void getBetListByDate() {
        habaneroGameServiceImpl.pullBetsLips();
    }

    @Test
    public void login() {
        // habaneroServiceImpl.login();
    }

}
