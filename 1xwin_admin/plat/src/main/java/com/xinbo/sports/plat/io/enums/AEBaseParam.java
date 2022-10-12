package com.xinbo.sports.plat.io.enums;

import com.google.common.collect.ImmutableMap;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.*;

import java.util.Map;

/**
 * @author: David
 * @date: 13/08/2020
 * @description: Sexy 真人参数详情
 */
public interface AEBaseParam {
    /**
     * 错误码映射
     * 少PLAT_SYSTEM_ERROR, PLAT_TIME_OUT、PLAT_ID_OCCUPATION
     */
    Map<String, CodeInfo> ERROR_CODE_MAPPER = new ImmutableMap.Builder<String, CodeInfo>().
            put(AEBaseParam.ErrorCode.INVALID_REQUEST_FORMAT.getCode(), CodeInfo.PLAT_INVALID_PARAM).
            put(AEBaseParam.ErrorCode.INVALID_CERTIFICATE.getCode(), CodeInfo.PLAT_INVALID_OPERATOR).
            put(AEBaseParam.ErrorCode.INVALID_IP.getCode(), CodeInfo.PLAT_IP_NOT_ACCESS).
            put(AEBaseParam.ErrorCode.PLAT_UNDER_MAINTENANCE.getCode(), CodeInfo.PLAT_UNDER_MAINTENANCE).
            put(AEBaseParam.ErrorCode.REQUEST_FREQUENT.getCode(), CodeInfo.PLAT_REQUEST_FREQUENT).
            put(AEBaseParam.ErrorCode.INVALID_CURRENCY.getCode(), CodeInfo.PLAT_INVALID_CURRENCY).
            put(AEBaseParam.ErrorCode.INVALID_LANGUAGE.getCode(), CodeInfo.PLAT_INVALID_LANGUAGE).
            put(AEBaseParam.ErrorCode.INVALID_AGENT.getCode(), CodeInfo.PLAT_INVALID_AGENT_ACCOUNT).

            put(AEBaseParam.ErrorCode.ACCOUNT_EXISTED.getCode(), CodeInfo.PLAT_INVALID_OPERATOR).
            put(AEBaseParam.ErrorCode.ACCOUNT_NOT_EXISTED.getCode(), CodeInfo.PLAT_ACCOUNT_NOT_EXISTS).
            put(AEBaseParam.ErrorCode.INVALID_USER_ID.getCode(), CodeInfo.PLAT_ACCOUNT_NOT_EXISTS).

            put(AEBaseParam.ErrorCode.ACCOUNT_OCCUPATION.getCode(), CodeInfo.PLAT_ACCOUNT_OCCUPATION).
            put(AEBaseParam.ErrorCode.NOT_ENOUGH_BALANCE.getCode(), CodeInfo.PLAT_COIN_INSUFFICIENT).
            put(AEBaseParam.ErrorCode.TX_CODE_IS_NOT_EXIST.getCode(), CodeInfo.PLAT_TRANSFER_ID_INVALID).
            build();

    /**
     * HTTP 请求头设置
     */
    Map<String, String> HTTP_HEADER = new ImmutableMap.Builder<String, String>().
            put("Content-Type", "application/x-www-form-urlencoded").
            put("charset", "UTF-8").
            build();

    String RESPONSE_FLAG = "status";

    @Getter
    enum UrlEnum {
        /**
         * 接口请求
         */
        CREATE_MEMBER("Create Member", "/wallet/createMember"),
        LOGIN("Login", "/wallet/login"),
        LOGOUT("Logout", "/wallet/logout"),
        GET_BALANCE("Get Balance", "/wallet/getBalance"),
        CHECK_TRANSFER_OPERATION("Check Transfer Operation", "/wallet/checkTransferOperation"),
        WITHDRAW("Withdraw", "/wallet/withdraw"),
        DEPOSIT("Deposit", "/wallet/deposit"),
        // Step1. 拉单调用接口
        GET_TRANSACTION_BY_UPDATE_DATE("Get Transaction By Update Date", "/fetch/getTransactionByUpdateDate"),
        // Step2. 对账接口
        GET_SUMMARY_BY_TX_TIME_HOUR("Get Summary By Tx Time Hour", "/fetch/getSummaryByTxTimeHour"),
        // Step3. 补账接口
        GET_TRANSACTION_BY_TX_TIME("Get Transaction By TxTime", "/fetch/getTransactionByTxTime"),

        ;

        private String name;
        private String url;

        UrlEnum(String name, String url) {
            this.name = name;
            this.url = url;
        }
    }

    @Getter
    enum Lang {
        /**
         * 语言
         */
        EN("en", "英语"),
        ZH("cn", "中文"),
        JP("jp", "日语"),
        TH("th", "泰文"),
        VI("vi", "越南语"),
        ;
        private String code;
        private String desc;

        Lang(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }

    @Getter
    enum Currency {
        /**
         * 币种
         */
        PHP("PHP", "菲律宾PESO"),
        MYR("MYR", "马来西亚币"),
        CNY("CNY", "人民币"),
        IDR("IDR", "印尼盾"),
        THB("THB", "泰铢"),
        PTI("PTI", "1:1"),
        PTV("PTV", "1:1"),
        INR("INR", "印度卢比"),
        ;

        private String code;
        private String desc;

        Currency(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }


    @Getter
    enum TransactionType {
        /**
         * 拉单状态
         */
        CANCEL(-1),
        BET(0),
        SETTLED(1),
        VOID(2),
        INVALID(9),
        ;

        private Integer code;

        TransactionType(Integer code) {
            this.code = code;
        }
    }

    @Getter
    enum PLATFORM {
        /**
         * 游戏平台
         */
        SEXYBCRT("SEXYBCRT", "LIVE", "AE_SEXYBCRT"),
        KINGMAKER("KINGMAKER", "TABLE", "AE_KINGMAKER"),
        ;

        private String code;
        private String category;
        private String model;

        PLATFORM(String code, String category, String model) {
            this.code = code;
            this.category = category;
            this.model = model;
        }
    }

    enum ErrorCode {
        /**
         * 错误码
         */
        System_Busy("9998", "System Busy"),
        Fail("9999", "Fail"),
        Success("0000", "Success"),
        Please_Input_All_data("10", "please input all data!"),
        INVALID_USER_ID("1000", "Invalid user Id"),
        ACCOUNT_EXISTED("1001", "Account existed"),
        ACCOUNT_NOT_EXISTED("1002", "Account is not exists "),
        Operation_ID_does_not_exist("1003", "Operation ID does not exist "),
        INVALID_CURRENCY("1004", "Invalid Currency "),
        INVALID_LANGUAGE("1005", "language is not exists "),
        PT_Setting_is_empty("1006", "PT Setting is empty! "),
        Invalid_PT_setting_with_parent("1007", "Invalid PT setting with parent! "),
        Invalid_token("1008", "Invalid token! "),
        Invalid_timeZone("1009", "Invalid timeZone "),
        Invalid_amount("1010", "Invalid amount "),
        Invalid_txCode("1011", "Invalid txCode "),
        Has_Pending_Transfe("1012", "Has Pending Transfer "),
        ACCOUNT_OCCUPATION("1013", "Account is Lock "),
        Account_is_Suspend("1014", "Account is Suspend "),
        Account_is_Close("1015", "Account is Close "),
        TxCode_already_operation("1016", "TxCode already operation! "),
        TX_CODE_IS_NOT_EXIST("1017", "TxCode is not exist"),
        NOT_ENOUGH_BALANCE("1018", "Not Enouth Balance "),
        No_Data("1019", "No Data "),
        Cashier_Id_is_not_exists("1020", "Cashier Id is not exists "),
        Cashier_Id_is_not_outlet_downline("1021", "Cashier Id is not outlet downline ."),
        User_Id_is_not_operation_downline("1022", "User Id is not operation downline "),
        Invalid_House_Id("1023", "Invalid House Id "),
        Invalid_date_time_format("1024", "Invalid date time format "),
        Invalid_transaction_status("1025", "Invalid transaction status "),
        Invalid_bet_limit_setting("1026", "Invalid bet limit setting "),
        INVALID_CERTIFICATE("1027", "Invalid Certificate "),
        REQUEST_FREQUENT("1028", "Unable to proceed. please try again later. "),
        INVALID_IP("1029", "invalid IP address. "),
        invalid_Device("1030", "invalid Device. "),
        PLAT_UNDER_MAINTENANCE("1031", "System is under maintenance. "),
        Duplicate_login("1032", "Duplicate login. "),
        Invalid_Game("1033", "Invalid Game. "),
        Time_does_not_meet("1034", "Time does not meet. "),
        INVALID_AGENT("1035", "Invalid Agent Id. "),
        INVALID_REQUEST_FORMAT("1036", "Invalid parameters."),
        Invalid_customer_setting("1037", "Invalid customer setting."),
        ;
        String code;
        String message;

        ErrorCode(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * 平台配置信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class Config {
        // API 接口
        String apiUrl;
        // 拉单接口
        String recordUrl;
        String cert;
        String agentId;
        String prefix;
        String betLimit;
        String currency;
        String environment;
        // 返回url
        String externalURL;
    }
}
