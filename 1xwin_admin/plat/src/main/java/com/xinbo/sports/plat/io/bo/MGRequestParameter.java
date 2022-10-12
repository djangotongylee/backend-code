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

public interface MGRequestParameter {

    @Builder
    @Data
    class Token {
        @JSONField(name = "grant_type")
        private String grantType;
        @JSONField(name = "client_id")
        private String clientId;
        @JSONField(name = "client_secret")
        private String clientSecret;
    }


    @Builder
    @Data
    class Login {

        @JSONField(name = "langCode")
        private String langCode;
        // Game, Tournament, Launchpad
        @JSONField(name = "contentCode")
        private String contentCode;
        // Unknown, Desktop, Mobile
        @JSONField(name = "platform")
        private String platform;

    }


    @Builder
    @Data
    class Register {

        @JSONField(name = "playerId")
        private String playerId;

    }

    @Builder
    @Data
    class FundTransfer {

        @JSONField(name = "playerId")
        private String playerId;
        @JSONField(name = "type")
        private String type;
        @JSONField(name = "amount")
        private BigDecimal amount;
        @JSONField(name = "externalTransactionId")
        private String externalTransactionId;

    }

    @Builder
    @Data
    class QueryBalance {
        @JSONField(name = "properties")
        private String properties;

    }

    @Builder
    @Data
    class GetGames {
        @JSONField(name = "agentCode")
        private String agentCode;

    }

    @Builder
    @Data
//Get bets details (bet by bet) 获取下注信息
    class GetBetsDetails {
        @JSONField(name = "limit")
        private Integer limit;
        @JSONField(name = "startingAfter")
        private String startingAfter;
    }


    @Builder
    @Data
    class GetGameReport {

        @JSONField(name = "fromDate")
        private String fromDate;
        @JSONField(name = "toDate")
        private String toDate;
        @JSONField(name = "timeAggregation")
        private String timeAggregation;
        @JSONField(name = "currency")
        private String currency;
        @JSONField(name = "utcOffset")
        private Integer utcOffset;

    }


    @Data
    class BetDetail {
        /**
         * 注单号->betUID
         */
        @JSONField(name = "betUID")
        private String id;
        @JSONField(name = "createdDateUTC", format = "yyyy-MM-dd'T'HH:mm:sss'z'")
        private Date createdDateutc;
        @JSONField(name = "gameStartTimeUTC", format = "yyyy-MM-dd'T'HH:mm:sss'z'")
        private Date gameStartTimeutc;
        @JSONField(name = "gameEndTimeUTC", format = "yyyy-MM-dd'T'HH:mm:sss'z'")
        private Date gameEndTimeutc;
        @JSONField(name = "playerId")
        private String playerId;
        @JSONField(name = "productId")
        private String productId;
        @JSONField(name = "productPlayerId")
        private String productPlayerId;
        @JSONField(name = "platform")
        private String platform;
        @JSONField(name = "gameCode")
        private String gameCode;
        @JSONField(name = "currency")
        private String currency;
        @JSONField(name = "betAmount", serializeUsing = BigDecimal.class)
        private BigDecimal betAmount;
        @JSONField(name = "payoutAmount", serializeUsing = BigDecimal.class)
        private BigDecimal payoutAmount;
        @JSONField(name = "betStatus")
        private String betStatus;
        @JSONField(name = "PCA")
        private String pca;
        @JSONField(name = "externalTransactionId")
        private String externalTransactionId;
        @JSONField(name = "metadata", serializeUsing = JSONArray.class)
        private String metadata;

    }

    @Builder
    @Data
    class CheckTransaction {
        @JSONField(name = "idempotencyKey")
        private String idempotencyKey;

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
        THB("THB", "泰铢"),
        HKD("HKD", "港币"),
        PHP("PHP", "菲律宾披索"),
        VND("VND", "越南盾"),
        IDR("IDR", "印尼盾"),
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
