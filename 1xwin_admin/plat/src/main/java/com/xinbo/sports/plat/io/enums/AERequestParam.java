package com.xinbo.sports.plat.io.enums;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author: David
 * @date: 02/05/2020
 * @description: SEXYBCRT Sexy真人接口入参
 */
public interface AERequestParam {
    /**
     * 基类
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    class BaseReqDto {
        private String cert;
        private String agentId;
    }

    /**
     * 注册会员
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @SuperBuilder
    @AllArgsConstructor
    @NoArgsConstructor
    class CreateMemberReqDto extends BaseReqDto {
        private String userId;
        private String currency;
        private String betLimit;
        private String language;
    }

    /**
     * 登录游戏
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @SuperBuilder
    @AllArgsConstructor
    @NoArgsConstructor
    class LoginReqDto extends BaseReqDto {
        private String userId;
        // 可选
        private String isMobileLogin;
        private String externalURL;
        private String gameForbidden;
        private String gameType;
        private String platform;
        private String language;
    }

    /**
     * 登出游戏
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @SuperBuilder
    @AllArgsConstructor
    @NoArgsConstructor
    class LogoutReqDto extends BaseReqDto {
        private String userIds;
    }

    /**
     * 查询余额
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @SuperBuilder
    @AllArgsConstructor
    @NoArgsConstructor
    class QueryBalanceReqDto extends BaseReqDto {
        private String userId;
        // 可选
        // a series of userIds split by comma(,) if alluser is 1, No input required
        private String userIds;
        //1 : return all user's balance 0 : return user's balance from given parameter
        private Integer alluser;
    }

    /**
     * 查询余额
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class QueryBalanceResDto {
        private String status;
        private Integer count;
        private Integer querytime;
        private List<QueryBalanceSubResDto> results;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class QueryBalanceSubResDto {
        private String userId;
        private BigDecimal balance;
        private String lastModified;
    }

    /**
     * 检查登录状态入参
     * When there's any exceptions during invoking deposit/ withdraw api,
     * you may invoke checkTransferOperation api 200 seconds after for the following informations
     * scenario 1:response status == 0000 and txStatus == 1 means deposit/withdraw success
     * scenario 2:response status == 0000 and txStatus == 0 means deposit/withdraw failure
     * scenario 3:response status == 1017 means deposit/withdraw failure
     * If there's any other status or exceptions occurred please refer to agent report or contact our support team
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @SuperBuilder
    @AllArgsConstructor
    @NoArgsConstructor
    class CheckTransferReqDto extends BaseReqDto {
        private String txCode;
    }

    /**
     * 下分
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @SuperBuilder
    @AllArgsConstructor
    @NoArgsConstructor
    class WithdrawalReqDto extends BaseReqDto {
        private String userId;
        private String txCode;
        // 1: All, 0: Partial; default = 1
        private Integer withdrawType;
        private String transferAmount;
    }

    /**
     * 上分
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @SuperBuilder
    @AllArgsConstructor
    @NoArgsConstructor
    class DepositReqDto extends BaseReqDto {
        private String userId;
        private String txCode;
        private BigDecimal transferAmount;
    }

    /**
     * 上分返回参数
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class DepositOrWithdrawalResDto {
        private String status;
        private BigDecimal amount;
        private String method;
        private BigDecimal currentBalance;
        private String databaseId;
        private String lastModified;
        private String txCode;
    }

    /**
     * Get Transaction By Update Date
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @SuperBuilder
    @AllArgsConstructor
    @NoArgsConstructor
    class GetTransactionByUpdateDateReqDto extends BaseReqDto {
        // ex: "2018-09-26T12:00:00+08:00"
        private String timeFrom;
        private String platform;
        // 可选
        // Transaction status, ref transaction status, if not pass the param, default txStatus include 1, 2, 9, -1(JDB/JDBFISH case)
        private Integer status;
        private String currency;
    }

    /**
     * Get Summary By Tx Time Hour
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @SuperBuilder
    @AllArgsConstructor
    @NoArgsConstructor
    class GetSummaryByTxTimeHourReqDto extends BaseReqDto {
        // ex: "2018-09-26T12+08:00"
        private String startTime;
        private String endTime;
    }

    @Data
    class GetSummaryByTxTimeHourResDto {
        private BigDecimal betAmount;
        private BigDecimal realBetAmount;
        private BigDecimal realWinAmount;
        private BigDecimal winAmount;
        private BigDecimal jackpotBetAmount;
        private BigDecimal jackpotWinAmount;
        private String currency;
        private BigDecimal turnover;
    }

    /**
     * Get Summary By Tx Time Hour
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @SuperBuilder
    @AllArgsConstructor
    @NoArgsConstructor
    class GetTransactionByTxTimeReqDto extends BaseReqDto {
        // ex: "2018-09-26T12+08:00"
        private String startTime;
        private String endTime;
        private String platform;
        // 可选
        private String userId;
        private String status;
        private String currency;

    }
}
