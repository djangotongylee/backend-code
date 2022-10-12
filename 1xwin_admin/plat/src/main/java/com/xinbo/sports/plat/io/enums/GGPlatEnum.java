package com.xinbo.sports.plat.io.enums;

import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.groovy.util.Maps;

import java.util.Map;

/**
 * @author: wells
 * @date: 2020/7/22
 * @description: GG捕鱼
 */

public interface GGPlatEnum {
    /**
     * GG捕鱼方法枚举
     */
    @Getter
    @AllArgsConstructor
    public enum GGMethodEnum {
        CA("ca", "创建与登录账号"),
        FW("fw", "登录成功返回链接"),
        TY("ty", "登出"),
        TC("tc", "转账;上分，下分"),
        GB("gb", "查询余额"),
        HBR3("hbr3", "获取历史注单列表"),
        BR3("br3", "获取注单列表"),
        QX("qx", "查询转账订单状态");

        private String methodName;
        private String methodNameDesc;
    }

    /**
     * 登录注册
     * 0 成功
     * 1 其它错误
     * 2 登陆失败，账户已存在或密码错误
     * 6 代理商不存在
     * 7 Des 值错误
     * 8 Key 值错误
     * 9 资料不全
     */
    Map<Integer, CodeInfo> CA_MAP = Maps.of(
            1, CodeInfo.PLAT_SYSTEM_ERROR,
            2, CodeInfo.PLAT_SYSTEM_ERROR,
            6, CodeInfo.PLAT_ACCOUNT_NOT_EXISTS,
            7, CodeInfo.PLAT_SYSTEM_ERROR,
            8, CodeInfo.PLAT_SYSTEM_ERROR,
            9, CodeInfo.PLAT_SYSTEM_ERROR
    );

    /**
     * 查询账号余额
     * 0 成功
     * 1 其它错误
     * 2 密码错误
     * 6 代理商不存在
     * 7 Des 值错误
     * 8 Key 值错误
     * 9 资料不全
     */
    Map<Integer, CodeInfo> GB_MAP = Maps.of(
            1, CodeInfo.PLAT_SYSTEM_ERROR,
            2, CodeInfo.PLAT_SYSTEM_ERROR,
            6, CodeInfo.PLAT_ACCOUNT_NOT_EXISTS,
            7, CodeInfo.PLAT_SYSTEM_ERROR,
            8, CodeInfo.PLAT_SYSTEM_ERROR,
            9, CodeInfo.PLAT_SYSTEM_ERROR
    );

    /**
     * 检查转账状态
     * <p>
     * 0 成功
     * 1 其它错误
     * 5 订单号不存在
     * 6 代理商不存在
     * 7 Des 值错误
     * 8 Key 值错误
     * 9 资料不全
     */
    Map<Integer, CodeInfo> QX_MAP = Maps.of(
            1, CodeInfo.PLAT_SYSTEM_ERROR,
            5, CodeInfo.PLAT_SYSTEM_ERROR,
            6, CodeInfo.PLAT_ACCOUNT_NOT_EXISTS,
            7, CodeInfo.PLAT_SYSTEM_ERROR,
            8, CodeInfo.PLAT_SYSTEM_ERROR,
            9, CodeInfo.PLAT_SYSTEM_ERROR
    );
    /**
     * 账户转账
     * 0 成功
     * 1 其它错误
     * 2 密码错误
     * 3 重复转账
     * 5 余额不足
     * 6 代理商不存在
     * 7 Des 值错误
     * 8 Key 值错误
     * 9 资料不全
     */
    Map<Integer, CodeInfo> TC_MAP = Maps.of(
            1, CodeInfo.PLAT_SYSTEM_ERROR,
            2, CodeInfo.PLAT_SYSTEM_ERROR,
            3, CodeInfo.PLAT_SYSTEM_ERROR,
            5, CodeInfo.PLAT_COIN_INSUFFICIENT,
            6, CodeInfo.PLAT_ACCOUNT_NOT_EXISTS,
            7, CodeInfo.PLAT_SYSTEM_ERROR,
            8, CodeInfo.PLAT_SYSTEM_ERROR,
            9, CodeInfo.PLAT_SYSTEM_ERROR
    );


    @Getter
    @AllArgsConstructor
    enum CURRENCY {
        RMB("CNY", "人民币"),
        VND("VND", "越南盾"),
        USD("USD", "美元"),
        THB("THB", "泰国铢"),
        SGD("SGD", "新加坡元"),
        PHP("PHP", "菲律宾比索"),
        MYR("MYR", "马来西亚林吉特"),
        INR("INR", "印度卢比"),
        TWD("TWD", "新台币");

        private String code;
        private String name;

    }


}
