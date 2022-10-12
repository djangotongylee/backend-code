package com.xinbo.sports.dao.generator.po;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

/**
 * <p>
 * PG电子注单表
 * </p>
 *
 * @author David
 * @since 2021-02-01
 */
@TableName("sp_betslips_pg_game")
public class BetslipsPgGame extends Model<BetslipsPgGame> {

    private static final long serialVersionUID=1L;

    /**
     * ID -> betId订单号
     */
    @TableId("id")
    private String id;

    /**
     * 对应user表id
     */
    @TableField("xb_uid")
    private Integer xbUid;

    /**
     * 对应user表username
     */
    @TableField("xb_username")
    private String xbUsername;

    /**
     * 子注单id
     */
    @TableField("bet_id")
    private String betId;

    /**
     * 玩家姓名
     */
    @TableField("player_name")
    private String playerName;

    /**
     * 游戏id
     */
    @TableField("game_id")
    private Integer gameId;

    /**
     * 下注类型-1真钱
     */
    @TableField("bet_type")
    private Integer betType;

    /**
     * 交易类型 1: Cash,2: Bonus,3: Free game
     */
    @TableField("transaction_type")
    private Integer transactionType;

    /**
     * 平台
     */
    @TableField("platform")
    private Integer platform;

    /**
     * 货币类型
     */
    @TableField("currency")
    private String currency;

    /**
     * 下注金额
     */
    @TableField("bet_amount")
    private BigDecimal betAmount;

    /**
     * 派彩金额
     */
    @TableField("win_amount")
    private BigDecimal winAmount;

    /**
     * 奖池贡献金额
     */
    @TableField("jackpot_contribution_amount")
    private BigDecimal jackpotContributionAmount;

    /**
     * 奖池赢取金额
     */
    @TableField("jackpot_win_amount")
    private BigDecimal jackpotWinAmount;

    /**
     * 下注前金额
     */
    @TableField("balance_before")
    private BigDecimal balanceBefore;

    /**
     * 下注后金额
     */
    @TableField("balance_after")
    private BigDecimal balanceAfter;

    /**
     * 1: Non-last hand 2: Last hand 3: Adjusted
     */
    @TableField("hands_status")
    private Integer handsStatus;

    /**
     * 数据更新时间
     */
    @TableField("row_version")
    private String rowVersion;

    /**
     * 下注时间
     */
    @TableField("betTime")
    private String betTime;

    /**
     * 结算时间
     */
    @TableField("betEndTime")
    private String betEndTime;

    /**
     * 投注金额
     */
    @TableField("xb_coin")
    private BigDecimal xbCoin;

    /**
     * 有效投注额
     */
    @TableField("xb_valid_coin")
    private BigDecimal xbValidCoin;

    /**
     * 盈亏金额
     */
    @TableField("xb_profit")
    private BigDecimal xbProfit;

    /**
     * 注单状态
     */
    @TableField("xb_status")
    private Integer xbStatus;

    @TableField("created_at")
    private Integer createdAt;

    @TableField("updated_at")
    private Integer updatedAt;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getXbUid() {
        return xbUid;
    }

    public void setXbUid(Integer xbUid) {
        this.xbUid = xbUid;
    }

    public String getXbUsername() {
        return xbUsername;
    }

    public void setXbUsername(String xbUsername) {
        this.xbUsername = xbUsername;
    }

    public String getBetId() {
        return betId;
    }

    public void setBetId(String betId) {
        this.betId = betId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public Integer getGameId() {
        return gameId;
    }

    public void setGameId(Integer gameId) {
        this.gameId = gameId;
    }

    public Integer getBetType() {
        return betType;
    }

    public void setBetType(Integer betType) {
        this.betType = betType;
    }

    public Integer getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(Integer transactionType) {
        this.transactionType = transactionType;
    }

    public Integer getPlatform() {
        return platform;
    }

    public void setPlatform(Integer platform) {
        this.platform = platform;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getBetAmount() {
        return betAmount;
    }

    public void setBetAmount(BigDecimal betAmount) {
        this.betAmount = betAmount;
    }

    public BigDecimal getWinAmount() {
        return winAmount;
    }

    public void setWinAmount(BigDecimal winAmount) {
        this.winAmount = winAmount;
    }

    public BigDecimal getJackpotContributionAmount() {
        return jackpotContributionAmount;
    }

    public void setJackpotContributionAmount(BigDecimal jackpotContributionAmount) {
        this.jackpotContributionAmount = jackpotContributionAmount;
    }

    public BigDecimal getJackpotWinAmount() {
        return jackpotWinAmount;
    }

    public void setJackpotWinAmount(BigDecimal jackpotWinAmount) {
        this.jackpotWinAmount = jackpotWinAmount;
    }

    public BigDecimal getBalanceBefore() {
        return balanceBefore;
    }

    public void setBalanceBefore(BigDecimal balanceBefore) {
        this.balanceBefore = balanceBefore;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public Integer getHandsStatus() {
        return handsStatus;
    }

    public void setHandsStatus(Integer handsStatus) {
        this.handsStatus = handsStatus;
    }

    public String getRowVersion() {
        return rowVersion;
    }

    public void setRowVersion(String rowVersion) {
        this.rowVersion = rowVersion;
    }

    public String getBetTime() {
        return betTime;
    }

    public void setBetTime(String betTime) {
        this.betTime = betTime;
    }

    public String getBetEndTime() {
        return betEndTime;
    }

    public void setBetEndTime(String betEndTime) {
        this.betEndTime = betEndTime;
    }

    public BigDecimal getXbCoin() {
        return xbCoin;
    }

    public void setXbCoin(BigDecimal xbCoin) {
        this.xbCoin = xbCoin;
    }

    public BigDecimal getXbValidCoin() {
        return xbValidCoin;
    }

    public void setXbValidCoin(BigDecimal xbValidCoin) {
        this.xbValidCoin = xbValidCoin;
    }

    public BigDecimal getXbProfit() {
        return xbProfit;
    }

    public void setXbProfit(BigDecimal xbProfit) {
        this.xbProfit = xbProfit;
    }

    public Integer getXbStatus() {
        return xbStatus;
    }

    public void setXbStatus(Integer xbStatus) {
        this.xbStatus = xbStatus;
    }

    public Integer getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Integer createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Integer updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    @Override
    public String toString() {
        return "BetslipsPgGame{" +
        "id=" + id +
        ", xbUid=" + xbUid +
        ", xbUsername=" + xbUsername +
        ", betId=" + betId +
        ", playerName=" + playerName +
        ", gameId=" + gameId +
        ", betType=" + betType +
        ", transactionType=" + transactionType +
        ", platform=" + platform +
        ", currency=" + currency +
        ", betAmount=" + betAmount +
        ", winAmount=" + winAmount +
        ", jackpotContributionAmount=" + jackpotContributionAmount +
        ", jackpotWinAmount=" + jackpotWinAmount +
        ", balanceBefore=" + balanceBefore +
        ", balanceAfter=" + balanceAfter +
        ", handsStatus=" + handsStatus +
        ", rowVersion=" + rowVersion +
        ", betTime=" + betTime +
        ", betEndTime=" + betEndTime +
        ", xbCoin=" + xbCoin +
        ", xbValidCoin=" + xbValidCoin +
        ", xbProfit=" + xbProfit +
        ", xbStatus=" + xbStatus +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        "}";
    }
}
