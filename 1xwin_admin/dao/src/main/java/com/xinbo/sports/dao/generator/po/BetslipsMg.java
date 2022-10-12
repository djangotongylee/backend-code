package com.xinbo.sports.dao.generator.po;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

/**
 * <p>
 * MG电子注单表
 * </p>
 *
 * @author David
 * @since 2021-02-01
 */
@TableName("sp_betslips_mg")
public class BetslipsMg extends Model<BetslipsMg> {

    private static final long serialVersionUID=1L;

    /**
     * 注单号->WagersID
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
     * 注单创建时间
     */
    @TableField("created_dateUTC")
    private Date createdDateutc;

    /**
     * 游戏开始时间
     */
    @TableField("game_start_timeUTC")
    private Date gameStartTimeutc;

    /**
     * 游戏结束时间
     */
    @TableField("game_end_timeUTC")
    private Date gameEndTimeutc;

    /**
     * 用户名
     */
    @TableField("player_id")
    private String playerId;

    /**
     * 厂商id
     */
    @TableField("product_id")
    private String productId;

    /**
     * 产品id
     */
    @TableField("product_player_id")
    private String productPlayerId;

    /**
     * 平台
     */
    @TableField("platform")
    private String platform;

    /**
     * 游戏code
     */
    @TableField("game_code")
    private String gameCode;

    /**
     * 币种
     */
    @TableField("currency")
    private String currency;

    /**
     * 下注金额
     */
    @TableField("bet_amount")
    private BigDecimal betAmount;

    /**
     * 赢取金额
     */
    @TableField("payout_amount")
    private BigDecimal payoutAmount;

    /**
     * 注单状态
     */
    @TableField("bet_status")
    private String betStatus;

    @TableField("pca")
    private String pca;

    /**
     * 转账id
     */
    @TableField("external_transaction_id")
    private String externalTransactionId;

    /**
     * 主要数据
     */
    @TableField("metadata")
    private String metadata;

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

    /**
     * 注单号
     */
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

    public Date getCreatedDateutc() {
        return createdDateutc;
    }

    public void setCreatedDateutc(Date createdDateutc) {
        this.createdDateutc = createdDateutc;
    }

    public Date getGameStartTimeutc() {
        return gameStartTimeutc;
    }

    public void setGameStartTimeutc(Date gameStartTimeutc) {
        this.gameStartTimeutc = gameStartTimeutc;
    }

    public Date getGameEndTimeutc() {
        return gameEndTimeutc;
    }

    public void setGameEndTimeutc(Date gameEndTimeutc) {
        this.gameEndTimeutc = gameEndTimeutc;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductPlayerId() {
        return productPlayerId;
    }

    public void setProductPlayerId(String productPlayerId) {
        this.productPlayerId = productPlayerId;
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

    public BigDecimal getPayoutAmount() {
        return payoutAmount;
    }

    public void setPayoutAmount(BigDecimal payoutAmount) {
        this.payoutAmount = payoutAmount;
    }

    public String getBetStatus() {
        return betStatus;
    }

    public void setBetStatus(String betStatus) {
        this.betStatus = betStatus;
    }

    public String getPca() {
        return pca;
    }

    public void setPca(String pca) {
        this.pca = pca;
    }

    public String getExternalTransactionId() {
        return externalTransactionId;
    }

    public void setExternalTransactionId(String externalTransactionId) {
        this.externalTransactionId = externalTransactionId;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
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
        return "BetslipsMg{" +
        "id=" + id +
        ", xbUid=" + xbUid +
        ", xbUsername=" + xbUsername +
        ", createdDateutc=" + createdDateutc +
        ", gameStartTimeutc=" + gameStartTimeutc +
        ", gameEndTimeutc=" + gameEndTimeutc +
        ", playerId=" + playerId +
        ", productId=" + productId +
        ", productPlayerId=" + productPlayerId +
        ", platform=" + platform +
        ", gameCode=" + gameCode +
        ", currency=" + currency +
        ", betAmount=" + betAmount +
        ", payoutAmount=" + payoutAmount +
        ", betStatus=" + betStatus +
        ", pca=" + pca +
        ", externalTransactionId=" + externalTransactionId +
        ", metadata=" + metadata +
        ", xbCoin=" + xbCoin +
        ", xbValidCoin=" + xbValidCoin +
        ", xbProfit=" + xbProfit +
        ", xbStatus=" + xbStatus +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        "}";
    }
}
