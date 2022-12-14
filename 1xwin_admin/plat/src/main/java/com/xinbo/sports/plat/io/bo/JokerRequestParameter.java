package com.xinbo.sports.plat.io.bo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * @author: wells
 * @date: 2020/5/27
 * @description:
 */

public interface JokerRequestParameter {


    @Builder
    @Data
    class Login {
        @JSONField(name = "method")
        private String method;
        @JSONField(name = "username")
        private String username;
        @JSONField(name = "timestamp")
        private Integer timestamp;

    }


    @Builder
    @Data
    class Register {
        @JSONField(name = "method")
        private String method;
        @JSONField(name = "username")
        private String username;
        @JSONField(name = "timestamp")
        private Integer timestamp;
    }

    @Builder
    @Data
    class Balance {
        @JSONField(name = "method")
        private String method;
        @JSONField(name = "timestamp")
        private Integer timestamp;
        @JSONField(name = "username")
        private String username;

    }

    @Builder
    @Data
    class Transfer {
        @JSONField(name = "method")
        private String method;
        @JSONField(name = "timestamp")
        private Integer timestamp;
        @JSONField(name = "username")
        private String username;
        @JSONField(name = "requestID")
        private String requestID;
        @JSONField(name = "amount")
        private String amount;
    }

    @Builder
    @Data
    class VerifyTransferCredit {
        @JSONField(name = "method")
        private String method;
        @JSONField(name = "timestamp")
        private Integer timestamp;
        @JSONField(name = "requestID")
        private String requestID;
    }

    @Builder
    @Data
    class GameList {
        @JSONField(name = "method")
        private String method;
        @JSONField(name = "timestamp")
        private Integer timestamp;

    }

    @Builder
    @Data
    class GetBetsDetails {
        @JSONField(name = "method")
        private String method;
        @JSONField(name = "timestamp")
        private Integer timestamp;
        @JSONField(name = "startDate")
        private String startDate;
        @JSONField(name = "endDate")
        private String endDate;
        @JSONField(name = "nextId")
        private String nextId;
    }

    @Data
    class BetDetail {
        /**
         * ?????????->betUID
         */
        @JSONField(name = "OCode")
        private String id;
        @JSONField(name = "Username")
        private String username;
        @JSONField(name = "GameCode")
        private String gameCode;
        @JSONField(name = "Description")
        private String description;
        @JSONField(name = "RoundID")
        private String roundId;
        @JSONField(name = "Amount", serializeUsing = BigDecimal.class)
        private BigDecimal amount;
        @JSONField(name = "FreeAmount", serializeUsing = BigDecimal.class)
        private BigDecimal freeAmount;
        @JSONField(name = "Result", serializeUsing = BigDecimal.class)
        private BigDecimal result;
        @JSONField(name = "Time")
        private String time;
        @JSONField(name = "Details")
        private String details;
        @JSONField(name = "AppID")
        private String appId;
        @JSONField(name = "CurrencyCode")
        private String currencyCode;
        @JSONField(name = "Type")
        private String type;
        @JSONField(name = "TransactionOCode")
        private String transactionOCode;
    }


    @Getter
    enum CURRENCY {
        /**
         * ?????? -tcg
         * CNY	?????????
         * IDR	?????????
         * THB	??????
         * MYR	??????
         * VND	?????????
         * TWD	?????????
         * KRW	??????
         * INR ????????????
         * USD ??????
         * MMK ??????
         * KHR ???????????????
         * LAK ????????????
         */
        RMB("CNY", "?????????"),
        Thailand("THB", "??????"),
        HKD("HKD", "??????"),
        PHP("PHP", "???????????????"),
        VND("VND", "?????????"),
        India("IDR", "?????????"),
        KRW("KRW", "??????"),
        LAK("LAK", "????????????"),
        KHR("KHR", "???????????????"),
        INR("INR", "????????????"),
        USD("USD", "??????"),
        MMK("KHR", "??????"),
        MYR("MYR", "???????????????");

        private String code;
        private String desc;

        CURRENCY(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }

    /**
     * ????????????
     * Language ??????	Language Code ????????????
     * ???????????? Simplified Chinese	ZH_CN
     * ?????? English	EN
     * ?????? Thailand	TH
     * ????????? Vietnamese	VN (For TCG_SEA LOTTO???VI;?????????????????????VI)
     * ????????? Indonesian	ID
     * ?????? Japanese	JA
     * ?????? korean	KO
     */
    @Getter
    enum LANGS {
        /**
         * ?????????????????????????????????
         * ????????????
         */
        EN("en", "??????"),
        ZH("zh", "??????"),
        VI("en", "??????"),
        TH("en", "??????"),
        ;

        private String code;
        private String message;

        LANGS(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }

}
