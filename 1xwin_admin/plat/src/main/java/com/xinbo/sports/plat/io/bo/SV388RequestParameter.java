package com.xinbo.sports.plat.io.bo;

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

public interface SV388RequestParameter {

    @Builder
    @Data
    class Login {
        @JSONField(name = "operatorcode")
        private String operatorCode;
        @JSONField(name = "providercode")
        private String providerCode;
        @JSONField(name = "username")
        private String username;
        @JSONField(name = "password")
        private String password;
        @JSONField(name = "type")
        private String type;
        @JSONField(name = "lang")
        private String lang;
        @JSONField(name = "signature")
        private String signature;

    }


    @Builder
    @Data
    class Register {
        @JSONField(name = "operatorcode")
        private String operatorCode;
        @JSONField(name = "username")
        private String username;
        @JSONField(name = "signature")
        private String signature;
    }

    @Builder
    @Data
    class Balance {
        @JSONField(name = "operatorcode")
        private String operatorCode;
        @JSONField(name = "providercode")
        private String providerCode;
        @JSONField(name = "username")
        private String username;
        @JSONField(name = "password")
        private String password;
        @JSONField(name = "signature")
        private String signature;

    }

    @Builder
    @Data
    class GetAllBetsDetails {
        @JSONField(name = "operatorcode")
        private String operatorCode;
        @JSONField(name = "versionkey")
        private String versionKey;
        @JSONField(name = "signature")
        private String signature;
    }
    @Builder
    @Data
    class MarkBetRecord {
        @JSONField(name = "operatorcode")
        private String operatorCode;
        @JSONField(name = "ticket")
        private String ticket;
        @JSONField(name = "signature")
        private String signature;
    }


    @Builder
    @Data
    class Transfer {
        @JSONField(name = "operatorcode")
        private String operatorCode;
        @JSONField(name = "providercode")
        private String providerCode;
        @JSONField(name = "username")
        private String username;
        @JSONField(name = "password")
        private String password;
        @JSONField(name = "referenceid")
        private String referenceId;
        @JSONField(name = "signature")
        private String signature;
        @JSONField(name = "type")
        private Integer type;
        @JSONField(name = "amount")
        private Double amount;
    }


    @Data
    class BetsDetail {
        /**
         * ?????????->betUID
         */
        @JSONField(name = "id")
        private String id;

       @JSONField(name ="ref_no")
        private String refNo;

       @JSONField(name ="site")
        private String site;

       @JSONField(name ="product")
        private String product;


       @JSONField(name ="member")
        private String member;


       @JSONField(name ="game_id")
        private String gameId;

        /**
         * ??????????????????
         */
       @JSONField(name ="start_time", format = "yyyy-MM-dd'T'HH:mm:ss")
        private Date startTime;

        /**
         * ??????????????????
         */
       @JSONField(name ="match_time", format = "yyyy-MM-dd'T'HH:mm:ss")
        private Date matchTime;

        /**
         * ??????????????????
         */
       @JSONField(name ="end_time", format = "yyyy-MM-dd'T'HH:mm:ss")
        private Date endTime;

        /**
         * ??????????????????
         */
       @JSONField(name ="bet_detail")
        private String betDetail;

        /**
         * ??????????????????
         */
       @JSONField(name ="turnover")
        private BigDecimal turnover;

        /**
         * ????????????
         */
       @JSONField(name ="bet")
        private BigDecimal bet;

        /**
         * ????????????
         */
       @JSONField(name ="payout")
        private BigDecimal payout;

        /**
         * ??????
         */
       @JSONField(name ="commission")
        private BigDecimal commission;

        /**
         *  ??????????????????
         */
       @JSONField(name ="p_share")
        private BigDecimal pShare;

        /**
         * ??????????????????
         */
       @JSONField(name ="p_win")
        private BigDecimal pWin;

        /**
         * ????????????
         */
       @JSONField(name ="status")
        private Integer status;

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
        THAILAND("THB", "??????"),
        HKD("HKD", "??????"),
        PHP("PHP", "???????????????"),
        VND("VND", "?????????"),
        INDIA("IDR", "?????????"),
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
        vn("en_US", "?????????"),
        th("th_TH", "??????"),
        ;

        private String code;
        private String message;

        LANGS(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }

}
