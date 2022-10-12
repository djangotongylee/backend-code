package com.xinbo.sports.plat.io.enums;

import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.*;

import java.util.Map;

/**
 * @author: wells
 * @date: 2020/5/28
 * @description:
 */

public interface S128PlatEnum {


    /**
     * 天成彩票方法枚举
     */
    @Getter
    @AllArgsConstructor
    enum S128MethodEnum {
        ACTIVEACCOUNT("/active_player.aspx", "创建玩家"),
        GETBALANCE("/get_balance.aspx", "获取余额"),
        DEPOSIT("/deposit.aspx", "上分"),
        WITHDRAW("/withdraw.aspx", "下分"),
        GETSESSIONID("/get_session_id.aspx", "获取sessionId"),
        PCLOGIN("/api/auth_login.aspx", "电脑登陆"),
        MOBLIELOGIN("/api/cash/auth", "手机登陆"),
        GETOPENTICKET("/get_cockfight_open_ticket_2.aspx", "获取交易"),
        CHECKTRANSFER("/check_transfer.aspx", "查询转账"),
        GETPROCESSEDTICKET("/get_cockfight_processed_ticket_2.aspx", "获取已结算交易");


        private String methodName;
        private String methodNameDesc;
    }


    Map<String, CodeInfo> C_MAP = Map.of(
            "61.01", CodeInfo.PLAT_INVALID_PARAM,
            "61.02", CodeInfo.PLAT_ACCOUNT_NOT_EXISTS
    );

    Map<String, CodeInfo> D_MAP = Map.of(
            "61.01", CodeInfo.PLAT_INVALID_PARAM,
            "61.01a", CodeInfo.PLAT_REGISTER_USER_ERROR,
            "61.02", CodeInfo.PLAT_ID_OCCUPATION
    );

    Map<String, CodeInfo> W_MAP = Map.of(
            "61.01", CodeInfo.PLAT_INVALID_PARAM,
            "61.02", CodeInfo.PLAT_ID_OCCUPATION,
            "61.03", CodeInfo.PLAT_COIN_INSUFFICIENT
    );
    Map<String, CodeInfo> R_MAP = Map.of(
            "61.01", CodeInfo.PLAT_INVALID_PARAM,
            "61.00a", CodeInfo.PLAT_REQUEST_FREQUENT,
            "61.00", CodeInfo.PLAT_INVALID_PARAM
    );
    Map<String, CodeInfo> L_MAP = Map.of(
            "61.01", CodeInfo.PLAT_INVALID_PARAM
    );


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class PlatConfig {
        String apiUrl;
        String pcUrl;
        String h5Url;
        String agentCode;
        String apiKey;
        String currency;
        String environment;
        String oddsType;
    }

    @Getter
    enum Lang {
        /**
         * 语言
         */
        EN("en-US"),
        ZH_TW("zh-TW"),
        ZH_CN("zh-CN"),
        TH_TH("th-TH"),
        ID_ID("id-id"),
        VI_VN("vi-VN"),
        ES_ES("es-ES"),
        KO_KR("ko-KR");

        String value;

        Lang(String value) {
            this.value = value;
        }
    }
}
