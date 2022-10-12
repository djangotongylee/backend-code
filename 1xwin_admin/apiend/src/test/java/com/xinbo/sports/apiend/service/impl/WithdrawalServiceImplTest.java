package com.xinbo.sports.apiend.service.impl;

import com.xinbo.sports.apiend.ApiendApplication;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApiendApplication.class)
class WithdrawalServiceImplTest {
    @Autowired
    private WithdrawalServiceImpl withdrawalServiceImpl;

    @Test
    void checkWithdrawalNumsAndTotalCoin() {
       // withdrawalServiceImpl.checkWithdrawalNumsAndTotalCoin(87, 1, BigDecimal.valueOf(10));
    }
}