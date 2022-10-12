package com.xinbo.sports.plat.io.enums;

import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.*;
import org.apache.groovy.util.Maps;

import java.util.Map;

/**
 * @author: wells
 * @date: 2020/7/22
 * @description:
 */

public interface SLottoPlatEnum {

    /**
     * SLotto方法枚举
     */
    @Getter
    @AllArgsConstructor
    enum SLActionEnum {
        QUERYBALANCE("/api/getprofile?apiuser={apiuser}&apipass={apipass}&user={user}&pass={pass}&loginID={loginID}", "查询余额"),
        CREATE("/api/createplayer?apiuser={apiuser}&apipass={apipass}&user={user}&pass={pass}&loginID={loginID}&loginPass={loginPass}&fullName={fullName}", "注册用户"),
        DEPOSIT("/api/deposit?apiuser={apiuser}&apipass={apipass}&user={user}&pass={pass}&loginID={loginID}&amount={amount}&remarks={remarks}", "上分"),
        WITHDRAW("/api/withdraw?apiuser={apiuser}&apipass={apipass}&user={user}&pass={pass}&loginID={loginID}&amount={amount}&remarks={remarks}", "下分"),
        CHECKTRANSACTION("/api/transaction?apiuser={apiuser}&apipass={apipass}&user={user}&pass={pass}&transactionId={transactionId}", "转账校验"),
        BETLOGIN("/api/betLogin?apiuser={apiuser}&apipass={apipass}&user={user}&pass={pass}", "获取token"),
        LOBBY("/apiform/bet", "登陆大厅"),
        BETDETAILSLIST("/api/betdetailslist?apiuser={apiuser}&apipass={apipass}&user={user}&pass={pass}&dateFrom={dateFrom}&dateTo={dateTo}&currentPage={currentPage}", "拉取注单"),
        WINNUMBER("api/winnumber?apiuser={apiuser}&apipass={apipass}&user={user}&pass={pass}&dateFrom={dateFrom}&dateTo={dateTo}", "注单结果");

        private String methodName;
        private String methodNameDesc;
    }

    /**
     * 1 - Invalid API User/Password
     * 2 - Invalid Login ID/Password
     * 3 - Missing Parameters
     * 4 - Login ID Existed
     * 5 - Invalid New Password
     * 8 - Invalid Profile
     * 9 - Invalid Amount
     * 10 - Invalid Transaction ID
     * 12 - Invalid New Status
     * 13 - Invalid bet amount
     * 21 - Missing/Invalid Draw Date
     * 22 - Login ID Not Exsited
     * 999 - system error
     */
    Map<Integer, CodeInfo> MAP = Maps.of(
            1, CodeInfo.PLAT_INVALID_PARAM,
            3, CodeInfo.PLAT_SYSTEM_ERROR,
            4, CodeInfo.PLAT_ACCOUNT_EXISTS,
            7, CodeInfo.PLAT_INVALID_PARAM,
            8, CodeInfo.PLAT_INVALID_PARAM,
            9, CodeInfo.PLAT_SYSTEM_ERROR,
            10, CodeInfo.PLAT_ID_OCCUPATION,
            12, CodeInfo.PLAT_SYSTEM_ERROR,
            13, CodeInfo.PLAT_PLAT_NO_SUFFICIENT,
            22, CodeInfo.PLAT_ACCOUNT_NOT_EXISTS,
            999, CodeInfo.PLAT_SYSTEM_ERROR
    );

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class PlatConfig {
        String apiUrl;
        String apiuser;
        String apipass;
        String user;
        String pass;
        String currency;
        String environment;
    }


}
