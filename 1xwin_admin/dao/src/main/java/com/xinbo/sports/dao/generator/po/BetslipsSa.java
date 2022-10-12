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
 * SA真人注单表
 * </p>
 *
 * @author David
 * @since 2021-02-01
 */
@TableName("sp_betslips_sa")
public class BetslipsSa extends Model<BetslipsSa> {

    private static final long serialVersionUID=1L;

    /**
     * id -> BetID投注编号	
     */
    @TableId("id")
    private Long id;

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
     * 用户名字
     */
    @TableField("username")
    private String username;

    /**
     * 投注时间
     */
    @TableField("bet_time")
    private Date betTime;

    /**
     * 结算时间
     */
    @TableField("payout_time")
    private Date payoutTime;

    /**
     * 桌台ID
     */
    @TableField("host_id")
    private String hostId;

    /**
     * 游戏编号
     */
    @TableField("game_id")
    private String gameId;

    /**
     * 局号
     */
    @TableField("round")
    private String round;

    /**
     * 靴
     */
    @TableField("shoe_id")
    private String shoeId;

    /**
     * 投注金额
     */
    @TableField("bet_amount")
    private BigDecimal betAmount;

    /**
     * 有效投注额/洗碼量
     */
    @TableField("rolling")
    private BigDecimal rolling;

    /**
     * 投注後的馀额
     */
    @TableField("balance")
    private BigDecimal balance;

    /**
     * 输赢金额
     */
    @TableField("result_amount")
    private BigDecimal resultAmount;

    /**
     * 游戏类型
     */
    @TableField("game_type")
    private String gameType;

    /**
     * 真人游戏: 不同的投注类型
     */
    @TableField("bet_type")
    private Integer betType;

    /**
     * 投注资源设备
     */
    @TableField("bet_source")
    private Integer betSource;

    /**
     * 交易编号
     */
    @TableField("transaction_id")
    private Integer transactionId;

    /**
     * 游戏结果
     */
    @TableField("game_result")
    private String gameResult;

    /**
     * 投注金额
     */
    @TableField("xb_coin")
    private BigDecimal xbCoin;

    /**
     * 盈亏金额
     */
    @TableField("xb_profit")
    private BigDecimal xbProfit;

    /**
     * 有效投注额
     */
    @TableField("xb_valid_coin")
    private BigDecimal xbValidCoin;

    /**
     * 注单状态
     */
    @TableField("xb_status")
    private Integer xbStatus;

    @TableField("created_at")
    private Integer createdAt;

    @TableField("updated_at")
    private Integer updatedAt;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getBetTime() {
        return betTime;
    }

    public void setBetTime(Date betTime) {
        this.betTime = betTime;
    }

    public Date getPayoutTime() {
        return payoutTime;
    }

    public void setPayoutTime(Date payoutTime) {
        this.payoutTime = payoutTime;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getRound() {
        return round;
    }

    public void setRound(String round) {
        this.round = round;
    }

    public String getShoeId() {
        return shoeId;
    }

    public void setShoeId(String shoeId) {
        this.shoeId = shoeId;
    }

    public BigDecimal getBetAmount() {
        return betAmount;
    }

    public void setBetAmount(BigDecimal betAmount) {
        this.betAmount = betAmount;
    }

    public BigDecimal getRolling() {
        return rolling;
    }

    public void setRolling(BigDecimal rolling) {
        this.rolling = rolling;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getResultAmount() {
        return resultAmount;
    }

    public void setResultAmount(BigDecimal resultAmount) {
        this.resultAmount = resultAmount;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    public Integer getBetType() {
        return betType;
    }

    public void setBetType(Integer betType) {
        this.betType = betType;
    }

    public Integer getBetSource() {
        return betSource;
    }

    public void setBetSource(Integer betSource) {
        this.betSource = betSource;
    }

    public Integer getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }

    public String getGameResult() {
        return gameResult;
    }

    public void setGameResult(String gameResult) {
        this.gameResult = gameResult;
    }

    public BigDecimal getXbCoin() {
        return xbCoin;
    }

    public void setXbCoin(BigDecimal xbCoin) {
        this.xbCoin = xbCoin;
    }

    public BigDecimal getXbProfit() {
        return xbProfit;
    }

    public void setXbProfit(BigDecimal xbProfit) {
        this.xbProfit = xbProfit;
    }

    public BigDecimal getXbValidCoin() {
        return xbValidCoin;
    }

    public void setXbValidCoin(BigDecimal xbValidCoin) {
        this.xbValidCoin = xbValidCoin;
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
        return "BetslipsSa{" +
        "id=" + id +
        ", xbUid=" + xbUid +
        ", xbUsername=" + xbUsername +
        ", username=" + username +
        ", betTime=" + betTime +
        ", payoutTime=" + payoutTime +
        ", hostId=" + hostId +
        ", gameId=" + gameId +
        ", round=" + round +
        ", shoeId=" + shoeId +
        ", betAmount=" + betAmount +
        ", rolling=" + rolling +
        ", balance=" + balance +
        ", resultAmount=" + resultAmount +
        ", gameType=" + gameType +
        ", betType=" + betType +
        ", betSource=" + betSource +
        ", transactionId=" + transactionId +
        ", gameResult=" + gameResult +
        ", xbCoin=" + xbCoin +
        ", xbProfit=" + xbProfit +
        ", xbValidCoin=" + xbValidCoin +
        ", xbStatus=" + xbStatus +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        "}";
    }
}
