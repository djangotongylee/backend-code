package com.xinbo.sports.plat.io.bo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Builder;
import lombok.Data;
import org.springframework.context.annotation.Primary;

import javax.validation.constraints.Negative;
import java.math.BigDecimal;

/**
 * @author: David
 * @date: 02/05/2020
 * @description: SBO_BET 利记平台api接口入参
 */
public interface HBRequestParameter {

    /**
     * 登录或创建用户
     */
    @Data
    @Builder
    class GetGamesRequest {
        @JSONField(name = "BrandId")
        private String BrandId;
        @JSONField(name = "APIKey")
        private String APIKey;
    }

    /**
     * 登录或创建用户
     */
    @Data
    @Builder
    class LoginOrCreatePlayerRequest {
        @JSONField(name = "BrandId")
        private String BrandId;
        @JSONField(name = "APIKey")
        private String APIKey;
        @JSONField(name = "PlayerHostAddress")
        private String PlayerHostAddress;
        @JSONField(name = "UserAgent")
        private String UserAgent;
        @JSONField(name = "KeepExistingToken")
        private Boolean KeepExistingToken;
        @JSONField(name = "Username")
        private String Username;
        @JSONField(name = "Password")
        private String Password;
        @JSONField(name = "CurrencyCode")
        private String CurrencyCode;
    }

    /**
     * 上分
     */
    @Data
    @Builder
    class DepositPlayerMoneyRequest {
        @JSONField(name = "BrandId")
        private String BrandId;
        @JSONField(name = "APIKey")
        private String APIKey;
        @JSONField(name = "Username")
        private String Username;
        @JSONField(name = "Password")
        private String Password;
        @JSONField(name = "CurrencyCode")
        private String CurrencyCode;
        @JSONField(name = "Amount")
        private BigDecimal Amount;
        @JSONField(name = "RequestId")
        private String RequestId;
    }

    /**
     * 下分
     */
    @Data
    @Builder
    class WithdrawPlayerMoney {
        @JSONField(name = "BrandId")
        private String BrandId;
        @JSONField(name = "APIKey")
        private String APIKey;
        @JSONField(name = "Username")
        private String Username;
        @JSONField(name = "Password")
        private String Password;
        @JSONField(name = "CurrencyCode")
        private String CurrencyCode;
        @JSONField(name = "Amount")
        private BigDecimal Amount;
        @JSONField(name = "WithdrawAll")
        private Boolean WithdrawAll;
        @JSONField(name = "RequestId")
        private String RequestId;
    }

    /**
     * 查询余额
     */
    @Data
    @Builder
    class QueryPlayerRequest {
        @JSONField(name = "BrandId")
        private String BrandId;
        @JSONField(name = "APIKey")
        private String APIKey;
        @JSONField(name = "Username")
        private String Username;
        @JSONField(name = "Password")
        private String Password;
    }

    /**
     * 登出
     */
    @Data
    @Builder
    class LogOutPlayer {
        @JSONField(name = "BrandId")
        private String BrandId;
        @JSONField(name = "APIKey")
        private String APIKey;
        @JSONField(name = "Username")
        private String Username;
        @JSONField(name = "Password")
        private String Password;
    }

    /**
     * 登出
     */
    @Data
    @Builder
    class GetBrandCompletedGameResultsV2 {
        @JSONField(name = "BrandId")
        private String BrandId;
        @JSONField(name = "APIKey")
        private String APIKey;
        @JSONField(name = "DtStartUTC")
        private String DtStartUTC;
        @JSONField(name = "DtEndUTC")
        private String DtEndUTC;
    }

    /**
     * 查询转账状态入参
     */
    @Data
    @Builder
    class checkTransferStatusReqDto {
        @JSONField(name = "BrandId")
        private String BrandId;
        @JSONField(name = "APIKey")
        private String APIKey;
        @JSONField(name = "RequestId")
        private String RequestId;
    }

    /**
     * 查询转账状态入参
     */
    @Data
    @Builder
    class checkTransferStatusResDto {
        private Boolean Success;
        private String PlayerId;
        private String Username;
        private String CurrencyCode;
        private String DtAdded;
        private BigDecimal Amount;
        private BigDecimal BalanceAfter;
        private String TransactionId;
    }
}
