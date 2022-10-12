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

public interface SAPlatEnum {


    /**
     * mg方法枚举
     */
    @Getter
    @AllArgsConstructor
    enum SAMethodEnum {
        LOGINREQUEST("LoginRequest", "创建玩家"),
        REGUSERINFO("RegUserInfo", "创建玩家"),
        GETBETSDETAILS("GetAllBetDetailsForTimeIntervalDV", "获取下注信息"),
        DEPOSIT("CreditBalanceDV", "存款"),
        WITHDRAW("DebitBalanceDV", "取款"),
        GETALLBETSDETAILS("DebitAllBalanceDV", "取全款"),
        BALANCE("GetUserStatusDV", "查询余额"),
        CHECKORDERID("CheckOrderId", "校验转账"),
        ;

        private String methodName;
        private String methodNameDesc;
    }
    Map<Integer, CodeInfo> LOGIN_MAP = Maps.of(
            129, CodeInfo.PLAT_UNDER_MAINTENANCE,
            130, CodeInfo.PLAT_ACCOUNT_OCCUPATION,
            133, CodeInfo.PLAT_REGISTER_USER_ERROR
            );
    Map<Integer, CodeInfo> BL_MAP = Maps.of(
            100, CodeInfo.PLAT_INVALID_PARAM,
            108, CodeInfo.PLAT_INVALID_PARAM,
            116, CodeInfo.PLAT_ACCOUNT_NOT_EXISTS
    );
    Map<Integer, CodeInfo> TR_MAP = Maps.of(
        106, CodeInfo.PLAT_UNDER_MAINTENANCE,
        108, CodeInfo.PLAT_INVALID_PARAM,
        116, CodeInfo.PLAT_ACCOUNT_NOT_EXISTS,
        120, CodeInfo.PLAT_COIN_BELOW_MIN_LIMIT,
        121, CodeInfo.PLAT_COIN_INSUFFICIENT,
        122, CodeInfo.PLAT_ID_OCCUPATION,
        127, CodeInfo.PLAT_INVALID_PARAM,
        142, CodeInfo.PLAT_INVALID_PARAM,
        145, CodeInfo.PLAT_INVALID_PARAM
    );

    Map<Integer, CodeInfo> CHECK_MAP = Maps.of(
            106, CodeInfo.PLAT_UNDER_MAINTENANCE,
            124, CodeInfo.PLAT_INVALID_PARAM,
            127, CodeInfo.PLAT_INVALID_PARAM
    );

    Map<Integer, CodeInfo> REGISTER_MAP = Maps.of(
            108, CodeInfo.PLAT_INVALID_PARAM,
            113, CodeInfo.PLAT_ACCOUNT_EXISTS,
            114, CodeInfo.PLAT_INVALID_CURRENCY,
            133, CodeInfo.PLAT_REGISTER_USER_ERROR
    );

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class PlatConfig {
        String reportUrl;
        String apiUrl;
        String environment;
        String md5Key;
        String encryptKey;
        String secretKey;
        String redirectUrl;
        String lobbyUrl;
    }
}
