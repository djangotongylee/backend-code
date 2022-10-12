package com.xinbo.sports.apiend.service.impl;

import com.xinbo.sports.apiend.ApiendApplication;
import com.xinbo.sports.apiend.service.IPromotionsService;
import com.xinbo.sports.service.base.RegisterBonusPromotions;
import com.xinbo.sports.service.io.dto.promotions.ApplicationActivityReqDto;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApiendApplication.class)
class PromotionsExtendServiceImplTest {
    @Autowired
    private PromotionsExtendServiceImpl promotionsExtendServiceImpl;
    @Autowired
    private RegisterBonusPromotions userRegisterPromotions;
    @Autowired
    private IPromotionsService iPromotionsServiceImpl;

    @Test
    void getPlatProfit() {
        promotionsExtendServiceImpl.getPlatProfit(1591761513);
    }

    @Test
    void userRegister() {
        //userRegisterPromotions.userRegisterAutoPromotions(12);
        var activity = ApplicationActivityReqDto.builder().id(16)
                .code("New Register Get Bonus").build();
        iPromotionsServiceImpl.applicationActivity(activity);
    }
}