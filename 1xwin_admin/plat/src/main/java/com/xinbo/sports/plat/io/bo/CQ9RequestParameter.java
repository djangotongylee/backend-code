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

public interface CQ9RequestParameter {

    @Builder
    @Data
    class Player {
        @JSONField(name = "account")
        private String account;
        @JSONField(name = "password")
        private String password;
    }


    @Builder
    @Data
    class Login {

        @JSONField(name = "account")
        private String account;
        @JSONField(name = "password")
        private String password;

    }


    @Builder
    @Data
    class Lobby {

        @JSONField(name = "usertoken")
        private String usertoken;
        @JSONField(name = "lang")
        private String lang;

    }

    @Builder
    @Data
    class ChessLobby {

        @JSONField(name = "usertoken")
        private String usertoken;
        @JSONField(name = "lang")
        private String lang;

    }

    @Builder
    @Data
    class GameLink {
        @JSONField(name = "usertoken")
        private String usertoken;
        @JSONField(name = "gamehall")
        private String gamehall;
        @JSONField(name = "gamecode")
        private String gamecode;
        @JSONField(name = "gameplat")
        private String gameplat;
        @JSONField(name = "lang")
        private String lang;

    }

    @Builder
    @Data
    class Withdraw {
        @JSONField(name = "account")
        private String account;
        @JSONField(name = "mtcode")
        private String mtcode;
        @JSONField(name = "amount")
        private BigDecimal amount;

    }

    @Builder
    @Data
    class Deposit {
        //※字串長度限制36個字元
        @JSONField(name = "account")
        private String account;
        @JSONField(name = "mtcode")
        private String mtcode;
        //※數值長度限制小數點前12位、小數點後4位
        @JSONField(name = "amount")
        private BigDecimal amount;
    }


    @Builder
    @Data
    class Balance {

        @JSONField(name = "account")
        private String account;

    }

    @Builder
    @Data
    class Order {

        @JSONField(name = "starttime")
        private String starttime;
        @JSONField(name = "endtime")
        private String endtime;
        @JSONField(name = "page")
        private Integer page;
    }

    @Builder
    @Data
    class CheckTransaction {
        @JSONField(name = "mtcode")
        private String mtcode;
    }

    @Builder
    @Data
    class GameList {
        @JSONField(name = "gamehall")
        private String gamehall;
    }


    @Data
    class BetslipsCq9 {
        /**
         * 注单号->round
         */
        @JSONField(name = "round")
        private String id;
        @JSONField(name = "endroundtime")
        private Date endRoundTime;
        @JSONField(name = "createtime")
        private Date createTime;
        @JSONField(name = "bettime")
        private Date betTime;
        @JSONField(name = "gamehall")
        private String gameHall;
        @JSONField(name = "gametype")
        private String gameType;
        @JSONField(name = "gameplat")
        private String gamePlat;
        @JSONField(name = "gamecode")
        private String gameCode;
        @JSONField(name = "account")
        private String account;
        @JSONField(name = "balance", serializeUsing = BigDecimal.class)
        private BigDecimal balance;
        @JSONField(name = "win", serializeUsing = BigDecimal.class)
        private BigDecimal win;
        @JSONField(name = "bet", serializeUsing = BigDecimal.class)
        private BigDecimal bet;
        @JSONField(name = "validbet", serializeUsing = BigDecimal.class)
        private BigDecimal validBet;
        @JSONField(name = "jackpot", serializeUsing = BigDecimal.class)
        private BigDecimal jackpot;
        @JSONField(name = "jackpotcontribution", serializeUsing = JSONArray.class)
        private String jackpotContribution;
        @JSONField(name = "jackpottype")
        private String jackpotType;
        @JSONField(name = "status")
        private String status;
        @JSONField(name = "detail", serializeUsing = JSONArray.class)
        private String detail;
        @JSONField(name = "singlerowbet")
        private Boolean singleRowBet;
        @JSONField(name = "gamerole")
        private String gameRole;
        @JSONField(name = "bankertype")
        private String bankerType;
        @JSONField(name = "rake", serializeUsing = BigDecimal.class)
        private BigDecimal rake;
        @JSONField(name = "roomfee", serializeUsing = BigDecimal.class)
        private BigDecimal roomFee;
        @JSONField(name = "bettype", serializeUsing = JSONArray.class)
        private String bettype;
        @JSONField(name = "gameresult")
        private String gameResult;
        @JSONField(name = "tabletype")
        private String tableType;
        @JSONField(name = "tableid")
        private String tableId;
        @JSONField(name = "roundnumber")
        private String roundNumber;

    }

    @Data
    class BetDetail {
        /**
         * 注单号->round
         */
        @JSONField(name = "round")
        private String id;
        @JSONField(name = "endroundtime")
        private Date endRoundTime;
        @JSONField(name = "createtime")
        private Date createTime;
        @JSONField(name = "bettime")
        private Date betTime;
        @JSONField(name = "gamehall")
        private String gameHall;
        @JSONField(name = "gametype")
        private String gameType;
        @JSONField(name = "gameplat")
        private String gamePlat;
        @JSONField(name = "gamecode")
        private String gameCode;
        @JSONField(name = "account")
        private String account;
        @JSONField(name = "balance", serializeUsing = BigDecimal.class)
        private BigDecimal balance;
        @JSONField(name = "win", serializeUsing = BigDecimal.class)
        private BigDecimal win;
        @JSONField(name = "bet", serializeUsing = BigDecimal.class)
        private BigDecimal bet;
        @JSONField(name = "validbet", serializeUsing = BigDecimal.class)
        private BigDecimal validBet;
        @JSONField(name = "jackpot", serializeUsing = BigDecimal.class)
        private BigDecimal jackpot;
        @JSONField(name = "jackpotcontribution", serializeUsing = JSONArray.class)
        private String jackpotContribution;
        @JSONField(name = "jackpottype")
        private String jackpotType;
        @JSONField(name = "status")
        private String status;
        @JSONField(name = "detail", serializeUsing = JSONArray.class)
        private String detail;
        @JSONField(name = "singlerowbet")
        private Boolean singleRowBet;
        @JSONField(name = "gamerole")
        private String gameRole;
        @JSONField(name = "bankertype")
        private String bankerType;
        @JSONField(name = "rake", serializeUsing = BigDecimal.class)
        private BigDecimal rake;
        @JSONField(name = "roomfee", serializeUsing = BigDecimal.class)
        private BigDecimal roomFee;
        @JSONField(name = "bettype", serializeUsing = JSONArray.class)
        private String bettype;
        @JSONField(name = "gameresult")
        private String gameResult;
        @JSONField(name = "tabletype")
        private String tableType;
        @JSONField(name = "tableid")
        private String tableId;
        @JSONField(name = "roundnumber")
        private String roundNumber;

        @JSONField(name = "xb_username")
        private BigDecimal xbUsername;

        @JSONField(name = "xb_uid")
        private BigDecimal xbUid;

        @JSONField(name = "xb_coin")
        private BigDecimal xbCoin;

        @JSONField(name = "xb_valid_coin")
        private BigDecimal xbValidCoin;

        @JSONField(name = "xb_profit")
        private BigDecimal xbProfit;
        @JSONField(name = "xb_status")
        private Integer xbStatus;

        @JSONField(name = "created_at")
        private Integer createdAt;

        @JSONField(name = "updated_at")
        private Integer updatedAt;

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
        ZH("zh-cn", "中文"),
        VI("vn", "越南语"),
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
