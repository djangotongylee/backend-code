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
         * 用户名字
         */
        @JSONField(name = "Username")
        private String username;

        /**
         * 投注时间
         */
        @JSONField(name = "BetTime", format = "yyyy-MM-dd'T'HH:mm:sss")
        private Date betTime;

        /**
         * 结算时间
         */
        @JSONField(name = "PayoutTime", format = "yyyy-MM-dd'T'HH:mm:sss")
        private Date payoutTime;

        /**
         * 桌台ID
         */
        @JSONField(name = "HostID")
        private String hostId;

        /**
         * 游戏编号
         */
        @JSONField(name = "GameID")
        private String gameId;

        /**
         * 局号
         */
        @JSONField(name = "Round")
        private String round;

        /**
         * 靴
         */
        @JSONField(name = "Set")
        private String shoeId;

        /**
         * 投注金额
         */
        @JSONField(name = "BetAmount", serializeUsing = BigDecimal.class)
        private BigDecimal betAmount;

        /**
         * 有效投注额/洗碼量
         */
        @JSONField(name = "rolling", serializeUsing = BigDecimal.class)
        private BigDecimal rolling;

        /**
         * 投注後的馀额
         */
        @JSONField(name = "balance", serializeUsing = BigDecimal.class)
        private BigDecimal balance;

        /**
         * 输赢金额
         */
        @JSONField(name = "ResultAmount", serializeUsing = BigDecimal.class)
        private BigDecimal resultAmount;

        /**
         * 游戏类型
         */
        @JSONField(name = "GameType")
        private String gameType;

        /**
         * 真人游戏: 不同的投注类型
         */
        @JSONField(name = "bet_type")
        private Integer betType;

        /**
         * 投注资源设备
         */
        @JSONField(name = "BetSource")
        private Integer betSource;

        /**
         * 交易编号
         */
        @JSONField(name = "TransactionID")
        private Integer transactionId;

        /**
         * 游戏结果
         */
        @JSONField(name = "GameResult", serializeUsing = JSONArray.class)
        private String gameResult;

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
        India("INR", "印度卢比"),
        KRW("KRW", "韩元"),
        LAK("LAK", "老挝基普"),
        KHR("KHR", "柬埔寨瑞尔"),
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
        vn("vn", "越南文"),
        th("th", "泰语"),
        ;

        private String code;
        private String message;

        LANGS(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }

}
