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
         * 注单号->betUID
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
        Thailand("THB", "泰铢"),
        HKD("HKD", "港币"),
        PHP("PHP", "菲律宾披索"),
        VND("VND", "越南盾"),
        India("IDR", "印尼盾"),
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
     * 英文 English	EN
     * 泰语 Thailand	TH
     * 越南语 Vietnamese	VN (For TCG_SEA LOTTO：VI;东南亚彩请用：VI)
     * 印尼语 Indonesian	ID
     * 日语 Japanese	JA
     * 韩语 korean	KO
     */
    @Getter
    enum LANGS {
        /**
         * 中英，部分游戏支持泰语
         * 语言信息
         */
        EN("en", "英文"),
        ZH("zh", "中文"),
        VI("en", "英文"),
        TH("en", "英文"),
        ;

        private String code;
        private String message;

        LANGS(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }

}
