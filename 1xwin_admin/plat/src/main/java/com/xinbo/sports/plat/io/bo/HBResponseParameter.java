package com.xinbo.sports.plat.io.bo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author: David
 * @date: 02/05/2020
 * @description: Habanero平台api接口入参
 */
public interface HBResponseParameter {
    /**
     * 上分
     */
    @Data
    @Builder
    @AllArgsConstructor
    class DepositPlayerMoneyRequest {
        @JSONField(name = "Success")
        private String Success;
        @JSONField(name = "Amount")
        private String Amount;
        @JSONField(name = "RealBalance")
        private String RealBalance;
        @JSONField(name = "TransactionId")
        private String TransactionId;
        @JSONField(name = "CurrencyCode")
        private String CurrencyCode;
        @JSONField(name = "Message")
        private BigDecimal Message;
    }

    /**
     * 查询余额
     */
    @Builder
    @Data
    @AllArgsConstructor
    class QueryPlayerResponse {
        private Boolean Found;
        private String PlayerId;
        private String BrandId;
        private String BrandName;
        private String Token;
        private BigDecimal RealBalance;
        private String CurrencyCode;
        private String CurrencySymbol;
        private Boolean HasBonus;
        private BigDecimal BonusBalance;
        private Integer BonusSpins;
        private String BonusGameKeyName;
        private BigDecimal BonusPercentage;
        private BigDecimal BonusWagerRemaining;
        private String Message;
    }

    /**
     * 登出
     */
    @Builder
    @Data
    @AllArgsConstructor
    class LogOutPlayerResponse {
        private Boolean Success;
        private String Message;
    }

    /**
     * 登出
     */
    @Data
    @Builder
    @AllArgsConstructor
    class GetBrandCompletedGameResultsV2Response {
        private String PlayerId;
        private String BrandId;
        private String Username;
        private String BrandGameId;
        private String GameKeyName;
        private Integer GameTypeId;
        private String DtStarted;
        private String DtCompleted;
        private String FriendlyGameInstanceId;
        private String GameInstanceId;
        private Integer GameStateId;
        private BigDecimal Stake;
        private BigDecimal Payout;
        private BigDecimal JackpotWin;
        private BigDecimal JackpotContribution;
        private String CurrencyCode;
        private Integer ChannelTypeId;
        private BigDecimal BalanceAfter;
        private BigDecimal BonusStake;
        private BigDecimal BonusPayout;
        private BigDecimal BonusToReal;
        private String BonusCoupon;
    }
}
