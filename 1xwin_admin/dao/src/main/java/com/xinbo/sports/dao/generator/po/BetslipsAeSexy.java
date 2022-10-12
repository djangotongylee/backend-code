package com.xinbo.sports.dao.generator.po;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

/**
 * <p>
 * AE Sexy注单表
 * </p>
 *
 * @author David
 * @since 2021-02-01
 */
@TableName("sp_betslips_ae_sexy")
public class BetslipsAeSexy extends Model<BetslipsAeSexy> {

    private static final long serialVersionUID=1L;

    /**
     * ID->platform_tx_id
     */
    @TableId("id")
    private String id;

    /**
     * ae_id->ID 流水号(非唯一)
     */
    @TableField("ae_id")
    private Integer aeId;

    /**
     * 平台账号
     */
    @TableField("user_id")
    private String userId;

    /**
     * 平台类型
     */
    @TableField("platform")
    private String platform;

    /**
     * 游戏编码
     */
    @TableField("game_code")
    private String gameCode;

    /**
     * 游戏类型
     */
    @TableField("game_type")
    private String gameType;

    /**
     * 游戏类型
     */
    @TableField("bet_type")
    private String betType;

    /**
     * 订单时间
     */
    @TableField("tx_time")
    private String txTime;

    /**
     * 投注金额
     */
    @TableField("bet_amount")
    private BigDecimal betAmount;

    /**
     * 派彩金额
     */
    @TableField("win_amount")
    private BigDecimal winAmount;

    /**
     * 有效注额(同局号多笔下注记录在首笔)
     */
    @TableField("turnover")
    private BigDecimal turnover;

    /**
     * 订单状态
     */
    @TableField("tx_status")
    private Integer txStatus;

    /**
     * 真实投注金额
     */
    @TableField("real_bet_amount")
    private BigDecimal realBetAmount;

    /**
     * real win amount
     */
    @TableField("real_win_amount")
    private BigDecimal realWinAmount;

    /**
     * 奖池投注金额
     */
    @TableField("jackpot_bet_amount")
    private BigDecimal jackpotBetAmount;

    /**
     * 奖池派彩金额
     */
    @TableField("jackpot_win_amount")
    private BigDecimal jackpotWinAmount;

    /**
     * 货币
     */
    @TableField("currency")
    private String currency;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private String updateTime;

    /**
     * 局号
     */
    @TableField("round_id")
    private String roundId;

    /**
     * 游戏信息
     */
    @TableField("game_info")
    private String gameInfo;

    /**
     * 结算状态
     */
    @TableField("settle_status")
    private Integer settleStatus;

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

    public Integer getAeId() {
        return aeId;
    }

    public void setAeId(Integer aeId) {
        this.aeId = aeId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getGameCode() {
        return gameCode;
    }

    public void setGameCode(String gameCode) {
        this.gameCode = gameCode;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    public String getBetType() {
        return betType;
    }

    public void setBetType(String betType) {
        this.betType = betType;
    }

    public String getTxTime() {
        return txTime;
    }

    public void setTxTime(String txTime) {
        this.txTime = txTime;
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

    public BigDecimal getTurnover() {
        return turnover;
    }

    public void setTurnover(BigDecimal turnover) {
        this.turnover = turnover;
    }

    public Integer getTxStatus() {
        return txStatus;
    }

    public void setTxStatus(Integer txStatus) {
        this.txStatus = txStatus;
    }

    public BigDecimal getRealBetAmount() {
        return realBetAmount;
    }

    public void setRealBetAmount(BigDecimal realBetAmount) {
        this.realBetAmount = realBetAmount;
    }

    public BigDecimal getRealWinAmount() {
        return realWinAmount;
    }

    public void setRealWinAmount(BigDecimal realWinAmount) {
        this.realWinAmount = realWinAmount;
    }

    public BigDecimal getJackpotBetAmount() {
        return jackpotBetAmount;
    }

    public void setJackpotBetAmount(BigDecimal jackpotBetAmount) {
        this.jackpotBetAmount = jackpotBetAmount;
    }

    public BigDecimal getJackpotWinAmount() {
        return jackpotWinAmount;
    }

    public void setJackpotWinAmount(BigDecimal jackpotWinAmount) {
        this.jackpotWinAmount = jackpotWinAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getRoundId() {
        return roundId;
    }

    public void setRoundId(String roundId) {
        this.roundId = roundId;
    }

    public String getGameInfo() {
        return gameInfo;
    }

    public void setGameInfo(String gameInfo) {
        this.gameInfo = gameInfo;
    }

    public Integer getSettleStatus() {
        return settleStatus;
    }

    public void setSettleStatus(Integer settleStatus) {
        this.settleStatus = settleStatus;
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
        return "BetslipsAeSexy{" +
        "id=" + id +
        ", aeId=" + aeId +
        ", userId=" + userId +
        ", platform=" + platform +
        ", gameCode=" + gameCode +
        ", gameType=" + gameType +
        ", betType=" + betType +
        ", txTime=" + txTime +
        ", betAmount=" + betAmount +
        ", winAmount=" + winAmount +
        ", turnover=" + turnover +
        ", txStatus=" + txStatus +
        ", realBetAmount=" + realBetAmount +
        ", realWinAmount=" + realWinAmount +
        ", jackpotBetAmount=" + jackpotBetAmount +
        ", jackpotWinAmount=" + jackpotWinAmount +
        ", currency=" + currency +
        ", updateTime=" + updateTime +
        ", roundId=" + roundId +
        ", gameInfo=" + gameInfo +
        ", settleStatus=" + settleStatus +
        ", xbUid=" + xbUid +
        ", xbUsername=" + xbUsername +
        ", xbCoin=" + xbCoin +
        ", xbValidCoin=" + xbValidCoin +
        ", xbProfit=" + xbProfit +
        ", xbStatus=" + xbStatus +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        "}";
    }
}
