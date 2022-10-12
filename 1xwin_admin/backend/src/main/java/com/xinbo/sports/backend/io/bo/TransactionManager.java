package com.xinbo.sports.backend.io.bo;

import com.xinbo.sports.service.io.bo.UserCacheBo;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 交易管理
 * </p>
 *
 * @author andy
 * @since 2020/6/15
 */
public interface TransactionManager {

    /**
     * 交易记录-列表-请求body
     */
    @Data
    class ListTransactionRecordReqBody extends StartEndTime {
        @ApiModelProperty(name = "username", value = "用户名", example = "2")
        private String username;
        @ApiModelProperty(name = "UID", value = "UID", example = "84")
        private Integer uid;

        @ApiModelProperty(name = "category", value = "交易类型:1-存款 2-提款 3-上分 4-下分 5-返水 6-佣金 7-活动(奖励) 8-系统调账", example = "2")
        private Integer category;

        @ApiModelProperty(name = "ID", value = "ID或订单号", example = "59749182176628736")
        private String id;

        @ApiModelProperty(name = "status", value = "状态:0-处理中 1-成功 2-失败", example = "1")
        private Integer status;

    }

    /**
     * 交易记录-列表-实体
     */
    @Data
    class TransactionRecord {
        @ApiModelProperty(name = "id", value = "ID", example = "59749182201794560")
        private Long id;

        @ApiModelProperty(name = "UID", value = "UID", example = "44")
        private Integer uid;

        @ApiModelProperty(name = "username", value = "username", example = "andy001")
        private String username;

        @ApiModelProperty(name = "category", value = "交易类型:1-存款 2-提款 3-上分 4-下分 5-返水 6-佣金 7-活动(奖励) 8-系统调账", example = "2")
        private Integer category;

        @ApiModelProperty(name = "referId", value = "订单号", example = "59749182176628736")
        private Long referId;

        @ApiModelProperty(name = "coin", value = "交易金额", example = "1000.00")
        private BigDecimal coin;

        @ApiModelProperty(name = "coinBefore", value = "交易前金额", example = "9900.00")
        private BigDecimal coinBefore;

        @ApiModelProperty(name = "outIn", value = "收支类型:0-支出 1-收入", example = "0")
        private Integer outIn;

        @ApiModelProperty(name = "status", value = "状态:0-处理中 1-成功 2-失败", example = "0")
        private Integer status;

        @ApiModelProperty(name = "createdAt", value = "交易时间", example = "1592053315")
        private Integer createdAt;

        @ApiModelProperty(name = "userFlagList", value = "会员旗列表")
        private List<UserCacheBo.UserFlagInfo> userFlagList;
    }

    /**
     * 交易记录-统计-实体
     */
    @Data
    @Builder
    class StatisticsTransaction {
        @ApiModelProperty(name = "coinDeposit", value = "总存款", example = "10000.00")
        private BigDecimal coinDeposit;
        @ApiModelProperty(name = "coinWithdrawal", value = "总提款", example = "10000.00")
        private BigDecimal coinWithdrawal;
        @ApiModelProperty(name = "coinUp", value = "总上方", example = "10000.00")
        private BigDecimal coinUp;
        @ApiModelProperty(name = "coinDown", value = "总下方", example = "10000.00")
        private BigDecimal coinDown;
        @ApiModelProperty(name = "coinRebate", value = "总返水", example = "10000.00")
        private BigDecimal coinRebate;
        @ApiModelProperty(name = "coinCommission", value = "总佣金", example = "10000.00")
        private BigDecimal coinCommission;
        @ApiModelProperty(name = "coinRewards", value = "总活动", example = "10000.00")
        private BigDecimal coinRewards;
        @ApiModelProperty(name = "coinReconciliation", value = "总调账", example = "10000.00")
        private BigDecimal coinReconciliation;
    }

    @Data
    @Builder
    class BetStatistics {
        @ApiModelProperty(name = "coinDeposit", value = "总存款", example = "10000.00")
        private BigDecimal coinDeposit;
        @ApiModelProperty(name = "coinWithdrawal", value = "总提款", example = "10000.00")
        private BigDecimal coinWithdrawal;
        @ApiModelProperty(name = "coinUp", value = "总上方", example = "10000.00")
        private BigDecimal coinUp;
        @ApiModelProperty(name = "coinDown", value = "总下方", example = "10000.00")
        private BigDecimal coinDown;
        @ApiModelProperty(name = "coinRebate", value = "总返水", example = "10000.00")
        private BigDecimal coinRebate;
        @ApiModelProperty(name = "coinCommission", value = "总佣金", example = "10000.00")
        private BigDecimal coinCommission;
        @ApiModelProperty(name = "coinRewards", value = "总活动", example = "10000.00")
        private BigDecimal coinRewards;
        @ApiModelProperty(name = "coinReconciliation", value = "总调账", example = "10000.00")
        private BigDecimal coinReconciliation;
    }

    @Data
    @Builder
    class PlatBetTotalListReqBody extends StartEndTime {
        @ApiModelProperty(name = "id", value = "ID", example = "1")
        private String id;
        @ApiModelProperty(name = "username", value = "用户名", example = "andy77777")
        private String username;
        @ApiModelProperty(name = "platId", value = "平台ID:gameListId", example = "703")
        private Integer platId;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class PlatBetTotalListResBody {
        @ApiModelProperty(name = "id", value = "ID", example = "1")
        private String id;

        @ApiModelProperty(name = "username", value = "用户名", example = "andy77777")
        private String username;
        @ApiModelProperty(name = "uid", value = "uid", example = "1")
        private Integer uid;
        @ApiModelProperty(name = "userFlagList", value = "会员旗列表")
        private List<UserCacheBo.UserFlagInfo> userFlagList;

        @ApiModelProperty(name = "gameListName", value = "平台名称", example = "Futures")
        private String gameListName;
        @ApiModelProperty(name = "actionNo", value = "局号/期号", example = "20201009348")
        private String actionNo;
        @ApiModelProperty(name = "gameName", value = "游戏名称", example = "PARITY")
        private String gameName;

        @ApiModelProperty(name = "coin", value = "投注金额", example = "10000.00")
        private BigDecimal coin;
        @ApiModelProperty(name = "profit", value = "输赢金额", example = "10000.00")
        private BigDecimal profit;
        @ApiModelProperty(name = "status", value = "状态", example = "10000.00")
        private Integer status;

        @ApiModelProperty(name = "createdAt", value = "创建时间", example = "10000.00")
        private Integer createdAt;
        @ApiModelProperty(name = "updatedAt", value = "更新时间", example = "10000.00")
        private Integer updatedAt;
    }

    /**
     * 交易记录->全平台投注总额->统计
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class PlatBetTotalStatisticsResBody {
        @ApiModelProperty(name = "betCoin", value = "投注金额", example = "100.00")
        private BigDecimal betCoin;
        @ApiModelProperty(name = "profitCoin", value = "盈亏金额", example = "100.00")
        private BigDecimal profitCoin;
    }
}
