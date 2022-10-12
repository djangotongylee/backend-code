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
         * 注单号->betUID
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
         * 游戏开始时间
         */
       @JSONField(name ="start_time", format = "yyyy-MM-dd'T'HH:mm:ss")
        private Date startTime;

        /**
         * 游戏结束时间
         */
       @JSONField(name ="match_time", format = "yyyy-MM-dd'T'HH:mm:ss")
        private Date matchTime;

        /**
         * 游戏结束时间
         */
       @JSONField(name ="end_time", format = "yyyy-MM-dd'T'HH:mm:ss")
        private Date endTime;

        /**
         * 游戏编号名称
         */
       @JSONField(name ="bet_detail")
        private String betDetail;

        /**
         * 有效投注金额
         */
       @JSONField(name ="turnover")
        private BigDecimal turnover;

        /**
         * 投注金额
         */
       @JSONField(name ="bet")
        private BigDecimal bet;

        /**
         * 派彩金额
         */
       @JSONField(name ="payout")
        private BigDecimal payout;

        /**
         * 佣金
         */
       @JSONField(name ="commission")
        private BigDecimal commission;

        /**
         *  彩池投注金额
         */
       @JSONField(name ="p_share")
        private BigDecimal pShare;

        /**
         * 彩池派彩金额
         */
       @JSONField(name ="p_win")
        private BigDecimal pWin;

        /**
         * 注单状态
         */
       @JSONField(name ="status")
        private Integer status;

    }


    @Getter
    enum CURRENCY {
        /**
         * 币种 -tcg
         * CNY	人民币
         * IDR	印尼盾
         * THB	泰铢
         * MYR	马币
         * VND	越南盾
         * TWD	新台币
         * KRW	韩元
         * INR 印度卢比
         * USD 美元
         * MMK 缅元
         * KHR 柬埔寨瑞尔
         * LAK 老挝基普
         */
        RMB("CNY", "人民币"),
        THAILAND("THB", "泰铢"),
        HKD("HKD", "港币"),
        PHP("PHP", "菲律宾披索"),
        VND("VND", "越南盾"),
        INDIA("IDR", "印尼盾"),
        KRW("KRW", "韩元"),
        LAK("LAK", "老挝基普"),
        KHR("KHR", "柬埔寨瑞尔"),
        INR("INR", "印度卢比"),
        USD("USD", "美元"),
        MMK("KHR", "缅元"),
        MYR("MYR", "马来西亚币");

        private String code;
        private String desc;

        CURRENCY(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }

    /**
     * 平台参数
     * Language 语言	Language Code 语言代码
     * 简体中文 Simplified Chinese	ZH_CN
     * zh_TW	繁体中文
     * zh_CN	简体中文
     * en_US	英文
     * th	泰文
     * vn	越南文
     * jp	日文
     * id	印尼文
     * it	意大利文
     * ms	马来文
     * es	西班牙文
     */
    @Getter
    enum LANGS {
        /**
         * 中英，部分游戏支持泰语
         * 语言信息
         */
        zh("zh_CN", "中文"),
        en("en_US", "英文"),
        vn("en_US", "越南文"),
        th("th_TH", "泰文"),
        ;

        private String code;
        private String message;

        LANGS(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }

}
