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

public interface BTIPlatEnum {

    @Getter
    @AllArgsConstructor
    enum BTIMethodEnum {
        CREATE_USER("/CreateUser", "创建玩家"),
        LOGIN_ASIA("/%s/", "登陆"),
        LOGIN("/%s/sports", "登陆"),
        BALANCE("/GetBalance", "获取余额"),
        DEPOSIT("/TransferToWHL", "转账"),
        WITHDRAW("/TransferFromWHL", "登陆"),
        CHECK_TRANSACTION("/CheckTransaction", "转账校验"),
        FETCH_BET("/dataAPI/bettinghistory?token=", "拉取投注记录"),
        TOKEN("/dataAPI/gettoken", "获取秘钥"),
        OPEN_BETS("/dataAPI/openbets?token=", "获取未结算记录"),
        IMPLEMENTATION("/mobilegoto.aspx?MasterEventID=", "进入赛事"),
        AUTH_TOKEN("/GetCustomerAuthToken", "获取秘钥");

        private String methodName;
        private String methodNameDesc;
    }

    /**
     * Wrong/Expired Token -1000 Authentication token is wrong or expired
     * Customer doesn’t exist -2 Customer’s Username doesn’t exist in BTi’s System
     * General Error -3 General Error – system fail or time out
     * Invalid or Missing Parameters -4 Invalid or Missing parameters within the request
     * Success 0 No error
     * Wrong Agent Username or
     * Password -5 Wrong Agent Username or Password
     * Exceed query period -8 Exceed query period
     * Exceeded API calls -9 Exceeded API calls
     * API method is not allowed -10 API method is not allowed
     * "errorCode":-9,"errorMessage":"Exceeded DataApi calls
     */
    Map<Integer, CodeInfo> MAP = Maps.of(
            -1000, CodeInfo.PLAT_INVALID_PARAM,
            -2, CodeInfo.PLAT_ACCOUNT_NOT_EXISTS,
            -3, CodeInfo.PLAT_INVALID_AGENT_ACCOUNT,
            -4, CodeInfo.PLAT_INVALID_PARAM,
            -5, CodeInfo.PLAT_INVALID_AGENT_ACCOUNT,
            -8, CodeInfo.PLAT_INVALID_OPERATOR,
            -10, CodeInfo.PLAT_INVALID_OPERATOR,
            -9, CodeInfo.PLAT_REQUEST_FREQUENT
    );
    /**
     * CheckTransaction
     * NoError or
     * AuthenticationFailed or
     * MerchantIsFrozen or
     * MerchantNotActive or
     * Exception or
     * TransactionCodeNotFound
     */
    Map<String, CodeInfo> errorMAP = Maps.of(
            "AuthenticationFailed", CodeInfo.PLAT_INVALID_PARAM,
            "MerchantIsFrozen", CodeInfo.PLAT_ACCOUNT_OCCUPATION,
            "MerchantNotActive", CodeInfo.PLAT_ACCOUNT_OCCUPATION,
            "Exception", CodeInfo.PLAT_ACCOUNT_NOT_EXISTS
    );

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class PlatConfig {
        String agentUserName;
        String agentPassword;
        String reportUrl;
        String currency;
        String apiUrl;
        String logUrl;
        String environment;
        String lang;
        String branchesList;
        String xmlProxy;
    }


    Map<String, String> Nation_MAP = Maps.of(
            "india", "/%s//sports",
            "thailand", "/%s/"
    );
}
