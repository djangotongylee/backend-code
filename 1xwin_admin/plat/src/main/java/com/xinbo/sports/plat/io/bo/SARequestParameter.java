package com.xinbo.sports.plat.io.bo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author: wells
 * @date: 2020/5/27
 * @description:
 */

public interface SARequestParameter {


    @Builder
    @Data
    class Login {

        @JSONField(name = "method")
        private String method;
        @JSONField(name = "Key")
        private String Key;
        @JSONField(name = "Username")
        private String Username;
        @JSONField(name = "CurrencyType")
        private String CurrencyType;
        @JSONField(name = "Time")
        private String Time;

    }


    @Builder
    @Data
    class Register {

        @JSONField(name = "method")
        private String method;
        @JSONField(name = "Key")
        private String Key;
        @JSONField(name = "Time")
        private String Time;
        @JSONField(name = "Username")
        private String Username;
        @JSONField(name = "CurrencyType")
        private String CurrencyType;
    }

    @Builder
    @Data
    class Balance {

        @JSONField(name = "method")
        private String method;
        @JSONField(name = "Key")
        private String Key;
        @JSONField(name = "Time")
        private String Time;
        @JSONField(name = "Username")
        private String Username;

    }

    @Builder
    @Data
    class GetAllBetsDetails {
        @JSONField(name = "method")
        private String method;
        @JSONField(name = "Key")
        private String Key;
        @JSONField(name = "Time")
        private String Time;
        @JSONField(name = "FromTime")
        private String FromTime;
        @JSONField(name = "ToTime")
        private String ToTime;
    }

    @Builder
    @Data
    class Deposit {
        @JSONField(name = "method")
        private String method;
        @JSONField(name = "Key")
        private String Key;
        @JSONField(name = "Time")
        private String Time;
        @JSONField(name = "OrderId")
        private String OrderId;
        @JSONField(name = "Username")
        private String Username;
        @JSONField(name = "CreditAmount")
        private BigDecimal CreditAmount;
    }

    @Builder
    @Data
    class Withdraw {
        @JSONField(name = "method")
        private String method;
        @JSONField(name = "Key")
        private String Key;
        @JSONField(name = "Time")
        private String Time;
        @JSONField(name = "Username")
        private String Username;
        @JSONField(name = "OrderId")
        private String OrderId;
        @JSONField(name = "DebitAmount")
        private BigDecimal DebitAmount;
    }

    @Builder
    @Data
    class WithdrawAll {
        @JSONField(name = "method")
        private String method;
        @JSONField(name = "Key")
        private String Key;
        @JSONField(name = "Time")
        private String Time;
        @JSONField(name = "Username")
        private String Username;
        @JSONField(name = "OrderId")
        private String OrderId;
    }

    @Builder
    @Data
    class CheckOrderId {
        @JSONField(name = "method")
        private String method;
        @JSONField(name = "Key")
        private String Key;
        @JSONField(name = "Time")
        private String Time;
        @JSONField(name = "OrderId")
        private String OrderId;
    }


    @Data
    class BetDetail {
        @JSONField(name = "BetID")
        private String id;
        /**
         * ????????????
         */
        @JSONField(name = "Username")
        private String username;

        /**
         * ????????????
         */
        @JSONField(name = "BetTime", format = "yyyy-MM-dd'T'HH:mm:sss")
        private Date betTime;

        /**
         * ????????????
         */
        @JSONField(name = "PayoutTime", format = "yyyy-MM-dd'T'HH:mm:sss")
        private Date payoutTime;

        /**
         * ??????ID
         */
        @JSONField(name = "HostID")
        private String hostId;

        /**
         * ????????????
         */
        @JSONField(name = "GameID")
        private String gameId;

        /**
         * ??????
         */
        @JSONField(name = "Round")
        private String round;

        /**
         * ???
         */
        @JSONField(name = "Set")
        private String shoeId;

        /**
         * ????????????
         */
        @JSONField(name = "BetAmount", serializeUsing = BigDecimal.class)
        private BigDecimal betAmount;

        /**
         * ???????????????/?????????
         */
        @JSONField(name = "rolling", serializeUsing = BigDecimal.class)
        private BigDecimal rolling;

        /**
         * ??????????????????
         */
        @JSONField(name = "balance", serializeUsing = BigDecimal.class)
        private BigDecimal balance;

        /**
         * ????????????
         */
        @JSONField(name = "ResultAmount", serializeUsing = BigDecimal.class)
        private BigDecimal resultAmount;

        /**
         * ????????????
         */
        @JSONField(name = "GameType")
        private String gameType;

        /**
         * ????????????: ?????????????????????
         */
        @JSONField(name = "bet_type")
        private Integer betType;

        /**
         * ??????????????????
         */
        @JSONField(name = "BetSource")
        private Integer betSource;

        /**
         * ????????????
         */
        @JSONField(name = "TransactionID")
        private Integer transactionId;

        /**
         * ????????????
         */
        @JSONField(name = "GameResult", serializeUsing = JSONArray.class)
        private String gameResult;

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
        India("INR", "????????????"),
        KRW("KRW", "??????"),
        LAK("LAK", "????????????"),
        KHR("KHR", "???????????????"),
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
     * zh_TW	????????????
     * zh_CN	????????????
     * en_US	??????
     * th	??????
     * vn	?????????
     * jp	??????
     * id	?????????
     * it	????????????
     * ms	?????????
     * es	????????????
     */
    @Getter
    enum LANGS {
        /**
         * ?????????????????????????????????
         * ????????????
         */
        zh("zh_CN", "??????"),
        en("en_US", "??????"),
        vn("vn", "?????????"),
        th("th", "??????"),
        ;

        private String code;
        private String message;

        LANGS(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }

}
