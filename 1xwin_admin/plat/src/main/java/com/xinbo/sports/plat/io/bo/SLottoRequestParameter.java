package com.xinbo.sports.plat.io.bo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author: wells
 * @date: 2020/5/27
 * @description:
 */

public interface SLottoRequestParameter {


    @Builder
    @Data
    class Login {
        /**
         * player account login username
         */
        @JSONField(name = "user")
        private String user;
        @JSONField(name = "sessionID")
        private String sessionID;
        @JSONField(name = "tokenCode")
        private String tokenCode;
    }

    @Builder
    @Data
    class BetLogin {
        @JSONField(name = "apiuser")
        private String apiuser;
        @JSONField(name = "apipass")
        private String apipass;
        /**
         * player account login username
         */
        @JSONField(name = "user")
        private String user;
        /**
         * d.player account login password
         */
        @JSONField(name = "pass")
        private String pass;

    }


    @Builder
    @Data
    class Register {

        @JSONField(name = "apiuser")
        private String apiuser;
        @JSONField(name = "apipass")
        private String apipass;
        @JSONField(name = "user")
        private String user;
        @JSONField(name = "pass")
        private String pass;
        @JSONField(name = "loginID")
        private String loginID;
        //最少8位
        @JSONField(name = "loginPass")
        private String loginPass;
        @JSONField(name = "fullName")
        private String fullName;


    }

    @Builder
    @Data
    class Deposit {

        @JSONField(name = "apiuser")
        private String apiuser;
        @JSONField(name = "apipass")
        private String apipass;
        @JSONField(name = "user")
        private String user;
        @JSONField(name = "pass")
        private String pass;
        @JSONField(name = "loginID")
        private String loginID;
        @JSONField(name = "amount")
        private BigDecimal amount;
        @JSONField(name = "remarks")
        private String remarks;

    }

    @Builder
    @Data
    class WITHDRAW {

        @JSONField(name = "apiuser")
        private String apiuser;
        @JSONField(name = "apipass")
        private String apipass;
        @JSONField(name = "user")
        private String user;
        @JSONField(name = "pass")
        private String pass;
        @JSONField(name = "loginID")
        private String loginID;
        @JSONField(name = "amount")
        private BigDecimal amount;
        @JSONField(name = "remarks")
        private String remarks;

    }

    @Builder
    @Data
    class QueryBalance {

        @JSONField(name = "apiuser")
        private String apiuser;
        @JSONField(name = "apipass")
        private String apipass;
        @JSONField(name = "user")
        private String user;
        @JSONField(name = "pass")
        private String pass;
        @JSONField(name = "loginID")
        private String loginID;

    }

    @Builder
    @Data
    class BetDetailslist {

        @JSONField(name = "apiuser")
        private String apiuser;
        @JSONField(name = "apipass")
        private String apipass;
        @JSONField(name = "user")
        private String user;
        @JSONField(name = "pass")
        private String pass;
        @JSONField(name = "dateFrom")
        private String dateFrom;
        @JSONField(name = "dateTo")
        private String dateTo;
        @JSONField(name = "currentPage")
        private String currentPage;

    }

    @Builder
    @Data
    class CheckTransaction {
        /**
         * CheckTransaction
         */
        @JSONField(name = "apiuser")
        private String apiuser;
        @JSONField(name = "apipass")
        private String apipass;
        @JSONField(name = "user")
        private String user;
        @JSONField(name = "pass")
        private String pass;
        @JSONField(name = "transactionId")
        private String transactionId;
    }

    @Data
    class BetDetail {
        /**
         * 条数号
         */
        @JSONField(name = "RowNum")
        private Integer rowNum;
        /**
         * id -> BetID
         */
        @JSONField(name = "BetID", serializeUsing = Long.class)
        private Long id;
        /**
         * 订单号
         */
        @JSONField(name = "OrderID")
        private String orderId;

        /**
         * 用户名
         */
        @JSONField(name = "LoginName")
        private String loginName;

        /**
         * 结算日期
         */
        @JSONField(name = "DrawDate", format = "yyyy-MM-dd'T'HH:mm:sss")
        private Date drawDate;

        /**
         * 投注日期
         */
        @JSONField(name = "DateBet", format = "yyyy-MM-dd'T'HH:mm:sss")
        private Date dateBet;

        /**
         * 投注类型
         */
        @JSONField(name = "BetType")
        private Integer betType;

        /**
         * 投注位
         */
        @JSONField(name = "BetPosition")
        private String betPosition;
        /**
         * 投注位
         */
        @JSONField(name = "BetNumber")
        private String betNumber;

        /**
         * 投注金额
         */
        @JSONField(name = "Confirmed")
        private BigDecimal confirmed;
        /**
         * 赢金额
         */
        @JSONField(name = "StrikePL")
        private BigDecimal strikePL;

        /**
         * 佣金
         */
        @JSONField(name = "CommPL")
        private BigDecimal commPL;

        /**
         * 状态
         */
        @JSONField(name = "Status")
        private String status;
    }
}
