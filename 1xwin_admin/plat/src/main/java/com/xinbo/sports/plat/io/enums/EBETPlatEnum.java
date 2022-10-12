package com.xinbo.sports.plat.io.enums;

import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.*;
import org.apache.groovy.util.Maps;

import java.util.Map;

/**
 * @author: wells
 * @date: 2020/5/28
 * @description:
 */

public interface EBETPlatEnum {


    /**
     * mg方法枚举
     */
    @Getter
    @AllArgsConstructor
    enum EBETMethodEnum {
        LOGINREQUEST("syncuser", "创建玩家"),
        REGUSERINFO("RegUserInfo", "创建玩家"),
        GETBETSDETAILS("userbethistory", "获取下注信息"),
        DEPOSIT("recharge", "充值"),
        DEPOSITSTATUS("rechargestatus", "充值状态"),
        WITHDRAW("DebitBalanceDV", "取款"),
        USERINFO("userinfo", "查询用户信息"),
        BALANCE("getusermoney", "查询余额"),
        CHECKORDERID("CheckOrderId", "校验转账"),
        ;

        private String methodName;
        private String methodNameDesc;
    }

    /**
     *
     */
    Map<Integer, CodeInfo> MAP = Maps.of(
            4037, CodeInfo.PLAT_ACCOUNT_NOT_EXISTS,
            5001, CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0,
            5002, CodeInfo.PLAT_COIN_INSUFFICIENT,
            4027, CodeInfo.PLAT_IP_NOT_ACCESS
    );

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class PlatConfig {
        String loginUrl;
        String private_key;
        String publick_key;
        String apiUrl;
        String currency;
        Integer channelId;
    }


}
