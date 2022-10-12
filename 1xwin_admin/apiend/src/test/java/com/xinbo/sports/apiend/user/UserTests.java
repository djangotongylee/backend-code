package com.xinbo.sports.apiend.user;

import com.xinbo.sports.service.base.UserServiceBase;
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
//@RunWith(SpringRunner.class)
//@SpringBootTest
@Slf4j
public class UserTests {
    @Autowired
    UserServiceBase userServiceBase;

    @Test
    public void getSlotFavorite() {
        userServiceBase.slotFavoriteByUid(1, 204);
    }

    @Test
    public void replace() {
        String str = "sss  aaa  ccc";
        str = str.replace(" ", "");
        System.out.println("str=" + str);
    }
}
