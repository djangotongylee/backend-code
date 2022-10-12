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

public interface S128RequestParameter {

    @Builder
    @Data
    class Login {

        @JSONField(name = "session_id")
        private String sessionId;
        @JSONField(name = "lang")
        private String lang;
        @JSONField(name = "login_id")
        private String loginId;

    }


    @Builder
    @Data
    class Register {
        @JSONField(name = "agent_code")
        private String agentCode;
        @JSONField(name = "api_key")
        private String apiKey;
        @JSONField(name = "login_id")
        private String loginId;

    }

    @Builder
    @Data
    class SessionId {

        @JSONField(name = "agent_code")
        private String agentCode;
        @JSONField(name = "api_key")
        private String apiKey;
        @JSONField(name = "login_id")
        private String loginId;
        @JSONField(name = "name")
        private String name;
        @JSONField(name = "odds_type")
        private String oddsType;

    }

    @Builder
    @Data
    class QueryBalance {
        @JSONField(name = "agent_code")
        private String agentCode;
        @JSONField(name = "api_key")
        private String apiKey;
        @JSONField(name = "login_id")
        private String loginId;

    }

    @Builder
    @Data
    class Deposit {
        @JSONField(name = "agent_code")
        private String agentCode;
        @JSONField(name = "api_key")
        private String apiKey;
        @JSONField(name = "login_id")
        private String loginId;
        @JSONField(name = "name")
        private String name;
        @JSONField(name = "amount")
        private BigDecimal amount;
        @JSONField(name = "ref_no")
        private String refNo;
        @JSONField(name = "odds_type")
        private String oddsType;
    }

    @Builder
    @Data
    class Withdraw {
        @JSONField(name = "agent_code")
        private String agentCode;
        @JSONField(name = "api_key")
        private String apiKey;
        @JSONField(name = "login_id")
        private String loginId;
        @JSONField(name = "name")
        private String name;
        @JSONField(name = "amount")
        private BigDecimal amount;
        @JSONField(name = "ref_no")
        private String refNo;
        @JSONField(name = "odds_type")
        private String oddsType;
    }

    @Builder
    @Data
    class GetBetsDetails {

        @JSONField(name = "api_key")
        private String apiKey;
        @JSONField(name = "agent_code")
        private String agentCode;
        @JSONField(name = "start_datetime")
        private String startDatetime;
        @JSONField(name = "end_datetime")
        private String endDatetime;

    }

    @Builder
    @Data
    class CheckTransfer {

        @JSONField(name = "api_key")
        private String apiKey;
        @JSONField(name = "agent_code")
        private String agentCode;
        @JSONField(name = "ref_no")
        private String refNo;

    }


    @Data
    class BetSlipsS128Details {
        /**
         * ID -> 注单号码
         */
        @JSONField(name = "0")
        private Long id;

        /**
         * 登录帐号
         */
        @JSONField(name = "1")
        private String loginId;

        /**
         * 赛场编号
         */
        @JSONField(name = "2")
        private String arenaCode;

        /**
         * 赛场名中文名字
         */
        @JSONField(name = "3")
        private String arenaNameCn;

        /**
         * 赛事编号
         */
        @JSONField(name = "4")
        private String matchNo;

        /**
         * 赛事类型
         */
        @JSONField(name = "5")
        private String matchType;

        /**
         * 赛事日期
         */
        @JSONField(name = "6")
        private Date matchDate;
        /**
         * 日场次
         */
        @JSONField(name = "7")
        private Integer fightNo;

        /**
         * 赛事时间
         */

        @JSONField(name = "8", format = "yyyy-MM-dd'T'HH:mm:ss")
        private Date fightDatetime;

        /**
         * 龍斗鸡
         */
        @JSONField(name = "9")
        private String meronCock;

        /**
         * 龍斗鸡中文名字
         */
        @JSONField(name = "10")
        private String meronCockCn;

        /**
         * 鳳斗鸡
         */
        @JSONField(name = "11")
        private String walaCock;

        /**
         * 鳳斗鸡中文名字
         */
        @JSONField(name = "12")
        private String walaCockCn;

        /**
         * 投注
         */
        @JSONField(name = "13")
        private String betOn;

        /**
         * 赔率类型
         */
        @JSONField(name = "14")
        private String oddsType;
        /**
         * 要求赔率
         */

        @JSONField(name = "15")
        private BigDecimal oddsAsked;

        /**
         * 给出赔率
         */
        @JSONField(name = "16")
        private BigDecimal oddsGiven;

        /**
         * 投注金额
         */
        @JSONField(name = "17")
        private Integer stake;

        /**
         * 奖金
         */
        @JSONField(name = "18")
        private BigDecimal stakeMoney;

        /**
         * 转账前余额
         */
        @JSONField(name = "19")
        private BigDecimal balanceOpen;
        /**
         * 转账后余额
         */
        @JSONField(name = "20")
        private BigDecimal balanceClose;
        /**
         * 创建时间
         */

        @JSONField(name = "21", format = "yyyy-MM-dd'T'HH:mm:ss")
        private Date createdDatetime;
        /**
         * 赛事结果 MERON/WALA/BDD/FTD
         */
        @JSONField(name = "22")
        private String fightResult;
        /**
         * 状态 WIN/LOSE/REFUND/CANCEL
         * /VOID
         */
        @JSONField(name = "23")
        private String status;
        /**
         * 输赢
         */
        @JSONField(name = "24")
        private BigDecimal winloss;

        /**
         * 所得佣金
         */
        @JSONField(name = "25")
        private BigDecimal commEarned;
        /**
         * 派彩
         */
        @JSONField(name = "26")
        private BigDecimal payout;
        /**
         * 转账前余额
         */
        @JSONField(name = "27")
        private BigDecimal balanceOpen1;
        /**
         * 转账后余额
         */
        @JSONField(name = "28")
        private BigDecimal balanceClose1;
        /**
         * 处理时间
         */
        @JSONField(name = "29", format = "yyyy-MM-dd'T'HH:mm:ss")
        private Date processedDatetime;
    }

    @Data
    class BetSlipsS128OpenDetails {
        /**
         * ID -> 注单号码
         */
        @JSONField(name = "0")
        private Long id;

        /**
         * 登录帐号
         */
        @JSONField(name = "1")
        private String loginId;

        /**
         * 赛场编号
         */
        @JSONField(name = "2")
        private String arenaCode;

        /**
         * 赛场名中文名字
         */
        @JSONField(name = "3")
        private String arenaNameCn;

        /**
         * 赛事编号
         */
        @JSONField(name = "4")
        private String matchNo;

        /**
         * 赛事类型
         */
        @JSONField(name = "5")
        private String matchType;

        /**
         * 赛事日期
         */
        @JSONField(name = "6")
        private Date matchDate;
        /**
         * 日场次
         */
        @JSONField(name = "7")
        private Integer fightNo;

        /**
         * 赛事时间
         */

        @JSONField(name = "8", format = "yyyy-MM-dd'T'HH:mm:ss")
        private Date fightDatetime;

        /**
         * 龍斗鸡
         */
        @JSONField(name = "9")
        private String meronCock;

        /**
         * 龍斗鸡中文名字
         */
        @JSONField(name = "10")
        private String meronCockCn;

        /**
         * 鳳斗鸡
         */
        @JSONField(name = "11")
        private String walaCock;

        /**
         * 鳳斗鸡中文名字
         */
        @JSONField(name = "12")
        private String walaCockCn;

        /**
         * 投注
         */
        @JSONField(name = "13")
        private String betOn;

        /**
         * 赔率类型
         */
        @JSONField(name = "14")
        private String oddsType;
        /**
         * 要求赔率
         */

        @JSONField(name = "15")
        private BigDecimal oddsAsked;

        /**
         * 给出赔率
         */
        @JSONField(name = "16")
        private BigDecimal oddsGiven;

        /**
         * 投注金额
         */
        @JSONField(name = "17")
        private Integer stake;

        /**
         * 奖金
         */
        @JSONField(name = "18")
        private BigDecimal stakeMoney;

        /**
         * 转账前余额
         */
        @JSONField(name = "19")
        private BigDecimal balanceOpen;
        /**
         * 转账后余额
         */
        @JSONField(name = "20")
        private BigDecimal balanceClose;
        /**
         * 创建时间
         */

        @JSONField(name = "21", format = "yyyy-MM-dd'T'HH:mm:ss")
        private Date createdDatetime;
    }

    /**
     * AUD
     * HKD
     * IDR 1:1000
     * INR
     * KRW 1:1
     * KRW 1:1000
     * MMK 1:1000
     * MYR
     * RMB
     * THB
     * TWD
     * USD 只支持柬埔寨
     * VND 1:1000
     * COP
     * PEN
     */


    @Getter
    enum CURRENCY {

        RMB("RMB", "人民币"),
        AUD("AUD", "澳大利亚币"),
        THB("THB", "泰铢"),
        HKD("HKD", "港币"),
        VND("VND", "越南币"),
        INR("INR", "印度卢比"),
        KRW("KRW", "韩元"),
        IDR("IDR", "印尼盾"),
        MMK("MMK", "缅甸元"),
        COP("COP", "哥伦比亚比索"),
        MYR("MYR", "马来西亚币"),
        PEN("PEN", "秘鲁新索尔"),
        USD("USD", "美元"),
        ;

        private String code;
        private String desc;

        CURRENCY(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }


}
