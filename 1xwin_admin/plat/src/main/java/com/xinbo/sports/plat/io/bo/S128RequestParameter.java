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
         * ID -> ????????????
         */
        @JSONField(name = "0")
        private Long id;

        /**
         * ????????????
         */
        @JSONField(name = "1")
        private String loginId;

        /**
         * ????????????
         */
        @JSONField(name = "2")
        private String arenaCode;

        /**
         * ?????????????????????
         */
        @JSONField(name = "3")
        private String arenaNameCn;

        /**
         * ????????????
         */
        @JSONField(name = "4")
        private String matchNo;

        /**
         * ????????????
         */
        @JSONField(name = "5")
        private String matchType;

        /**
         * ????????????
         */
        @JSONField(name = "6")
        private Date matchDate;
        /**
         * ?????????
         */
        @JSONField(name = "7")
        private Integer fightNo;

        /**
         * ????????????
         */

        @JSONField(name = "8", format = "yyyy-MM-dd'T'HH:mm:ss")
        private Date fightDatetime;

        /**
         * ?????????
         */
        @JSONField(name = "9")
        private String meronCock;

        /**
         * ?????????????????????
         */
        @JSONField(name = "10")
        private String meronCockCn;

        /**
         * ?????????
         */
        @JSONField(name = "11")
        private String walaCock;

        /**
         * ?????????????????????
         */
        @JSONField(name = "12")
        private String walaCockCn;

        /**
         * ??????
         */
        @JSONField(name = "13")
        private String betOn;

        /**
         * ????????????
         */
        @JSONField(name = "14")
        private String oddsType;
        /**
         * ????????????
         */

        @JSONField(name = "15")
        private BigDecimal oddsAsked;

        /**
         * ????????????
         */
        @JSONField(name = "16")
        private BigDecimal oddsGiven;

        /**
         * ????????????
         */
        @JSONField(name = "17")
        private Integer stake;

        /**
         * ??????
         */
        @JSONField(name = "18")
        private BigDecimal stakeMoney;

        /**
         * ???????????????
         */
        @JSONField(name = "19")
        private BigDecimal balanceOpen;
        /**
         * ???????????????
         */
        @JSONField(name = "20")
        private BigDecimal balanceClose;
        /**
         * ????????????
         */

        @JSONField(name = "21", format = "yyyy-MM-dd'T'HH:mm:ss")
        private Date createdDatetime;
        /**
         * ???????????? MERON/WALA/BDD/FTD
         */
        @JSONField(name = "22")
        private String fightResult;
        /**
         * ?????? WIN/LOSE/REFUND/CANCEL
         * /VOID
         */
        @JSONField(name = "23")
        private String status;
        /**
         * ??????
         */
        @JSONField(name = "24")
        private BigDecimal winloss;

        /**
         * ????????????
         */
        @JSONField(name = "25")
        private BigDecimal commEarned;
        /**
         * ??????
         */
        @JSONField(name = "26")
        private BigDecimal payout;
        /**
         * ???????????????
         */
        @JSONField(name = "27")
        private BigDecimal balanceOpen1;
        /**
         * ???????????????
         */
        @JSONField(name = "28")
        private BigDecimal balanceClose1;
        /**
         * ????????????
         */
        @JSONField(name = "29", format = "yyyy-MM-dd'T'HH:mm:ss")
        private Date processedDatetime;
    }

    @Data
    class BetSlipsS128OpenDetails {
        /**
         * ID -> ????????????
         */
        @JSONField(name = "0")
        private Long id;

        /**
         * ????????????
         */
        @JSONField(name = "1")
        private String loginId;

        /**
         * ????????????
         */
        @JSONField(name = "2")
        private String arenaCode;

        /**
         * ?????????????????????
         */
        @JSONField(name = "3")
        private String arenaNameCn;

        /**
         * ????????????
         */
        @JSONField(name = "4")
        private String matchNo;

        /**
         * ????????????
         */
        @JSONField(name = "5")
        private String matchType;

        /**
         * ????????????
         */
        @JSONField(name = "6")
        private Date matchDate;
        /**
         * ?????????
         */
        @JSONField(name = "7")
        private Integer fightNo;

        /**
         * ????????????
         */

        @JSONField(name = "8", format = "yyyy-MM-dd'T'HH:mm:ss")
        private Date fightDatetime;

        /**
         * ?????????
         */
        @JSONField(name = "9")
        private String meronCock;

        /**
         * ?????????????????????
         */
        @JSONField(name = "10")
        private String meronCockCn;

        /**
         * ?????????
         */
        @JSONField(name = "11")
        private String walaCock;

        /**
         * ?????????????????????
         */
        @JSONField(name = "12")
        private String walaCockCn;

        /**
         * ??????
         */
        @JSONField(name = "13")
        private String betOn;

        /**
         * ????????????
         */
        @JSONField(name = "14")
        private String oddsType;
        /**
         * ????????????
         */

        @JSONField(name = "15")
        private BigDecimal oddsAsked;

        /**
         * ????????????
         */
        @JSONField(name = "16")
        private BigDecimal oddsGiven;

        /**
         * ????????????
         */
        @JSONField(name = "17")
        private Integer stake;

        /**
         * ??????
         */
        @JSONField(name = "18")
        private BigDecimal stakeMoney;

        /**
         * ???????????????
         */
        @JSONField(name = "19")
        private BigDecimal balanceOpen;
        /**
         * ???????????????
         */
        @JSONField(name = "20")
        private BigDecimal balanceClose;
        /**
         * ????????????
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
     * USD ??????????????????
     * VND 1:1000
     * COP
     * PEN
     */


    @Getter
    enum CURRENCY {

        RMB("RMB", "?????????"),
        AUD("AUD", "???????????????"),
        THB("THB", "??????"),
        HKD("HKD", "??????"),
        VND("VND", "?????????"),
        INR("INR", "????????????"),
        KRW("KRW", "??????"),
        IDR("IDR", "?????????"),
        MMK("MMK", "?????????"),
        COP("COP", "??????????????????"),
        MYR("MYR", "???????????????"),
        PEN("PEN", "???????????????"),
        USD("USD", "??????"),
        ;

        private String code;
        private String desc;

        CURRENCY(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }


}
