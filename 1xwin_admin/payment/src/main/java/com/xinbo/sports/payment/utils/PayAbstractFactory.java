package com.xinbo.sports.payment.utils;

import com.google.common.base.CaseFormat;
import com.xinbo.sports.payment.base.ThirdPayBase;
import com.xinbo.sports.payment.io.PayParams.*;
import com.xinbo.sports.utils.SpringUtils;
import java.util.List;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author: David
 * @date: 03/05/2020
 * @description: 第三方游戏平台基类
 */
@Component
public interface PayAbstractFactory {
    /**
     * 初始化工厂实体
     *
     * @param model 游戏实体名称
     * @return 工厂实体
     */
    @Nullable
    static PayAbstractFactory init(String model) {
        List listUtil=List.of("Easy6PayUtil","Easy2PayUtil","Easy1PayUtil");
        var foo=model;
        if("EasyLikePayUtil".equals(model)){
            model="EasyPayUtil";
        }
        if("Easy3PayUtil".equals(model)){
            model="EasyPayUtil";
        }
        if(listUtil.contains(model)){
            model="EasyNewPayUtil";
        }
        String className = "com.xinbo.sports.payment.utils." + model;
        try {
            Class<?> cls = Class.forName(className);
            PayAbstractFactory pay = (PayAbstractFactory) SpringUtils.getBean(cls);
            setConfig(cls, foo, pay);
            return pay;
        } catch (Exception e) {
            // 无需处理
            return null;
        }
    }

    @SneakyThrows
    static void setConfig(Class<?> cls, String model, PayAbstractFactory pay) {
        var configField = "plat";
        var setField = "set" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, configField);
        //获取平台配置
        var thirdPayBase = SpringUtils.getBean(ThirdPayBase.class);
        var field = cls.getDeclaredField(configField);
        var value = thirdPayBase.getPaymentConfig(model.split("Util")[0].toLowerCase().replace("pay", "_pay"), Class.forName(field.getType().getName()));
        var method = cls.getDeclaredMethod(setField, Class.forName(field.getType().getName()));
        //set平台配置到对应的配置类
        method.invoke(pay, value);
    }

    /**
     * 在线支付统一入口
     *
     * @param dto 入参
     * @return 支付结果
     */
    Map<Object, Object> onlinePay(OnlinePayReqDto dto);

    /**
     * 在线提现统一入口
     *
     * @param dto 入参
     * @return 支付结果
     */
    WithdrawalNotifyResDto onlineWithdraw(OnlinePayoutReqDto dto);


    Boolean checkPaymentStatus(String orderId);
}

