package com.xinbo.sports.service.base;

import com.xinbo.sports.dao.generator.po.Promotions;
import com.xinbo.sports.service.cache.redis.PromotionsCache;
import com.xinbo.sports.service.io.dto.promotions.ApplicationActivityReqDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.xinbo.sports.service.common.Constant.*;

/**
 * @Author : Wells
 * @Date : 2021-01-27 5:28 下午
 * @Description : xx
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RegisterBonusPromotions {
    private final PromotionsCache promotionsCache;
    private final PromotionsBase promotionsBase;

    /**
     * 用户注册活动-自动派彩
     */
    @Async
    public void userRegisterAutoPromotions(Integer uid) {
        var promotionsOptional = promotionsCache.getPromotionsListCache().stream()
                .filter(x -> REGISTER_BONUS.equals(x.getCode()) && AUTO_PAY.equals(x.getPayoutCategory())).findFirst();
        promotionsOptional.ifPresent(promotions -> getPayCoin(promotions,uid));
    }


    /**
     * 计算彩金
     *
     * @param promotions 活动实例
     */
    public void getPayCoin(Promotions promotions,Integer uid) {
            var info = promotions.getInfo();
            if (!StringUtils.isEmpty(info)) {
                var ruleJson = parseObject(info);
                var payCoin = ruleJson.getBigDecimal(PAY_COIN);
                var flowClain = Optional.ofNullable(ruleJson.getBigDecimal(FLOW_CLAIN)).orElse(BigDecimal.ZERO);
                var availableCoin = payCoin.multiply(flowClain).setScale(4, RoundingMode.DOWN);
                ApplicationActivityReqDto applicationActivityReqDto = ApplicationActivityReqDto.builder()
                        .id(promotions.getId())
                        .mosaicCoin(availableCoin)
                        .availableCoin(payCoin)
                        .build();
                promotionsBase.executePromotionsPersistence(applicationActivityReqDto, uid);
            }
    }
}
