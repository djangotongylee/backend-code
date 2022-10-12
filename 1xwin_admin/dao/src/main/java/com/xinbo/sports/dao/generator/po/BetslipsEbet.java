package com.xinbo.sports.dao.generator.po;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

/**
 * <p>
 * ebet注单表
 * </p>
 *
 * @author David
 * @since 2021-02-01
 */
@TableName("sp_betslips_ebet")
public class BetslipsEbet extends Model<BetslipsEbet> {

    private static final long serialVersionUID=1L;

    /**
     * ID -> billNo订单号
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
     * 游戏类型
     */
    @TableField("game_type")
    private Integer gameType;

    /**
     * 游戏名称
     */
    @TableField("game_name")
    private String gameName;

    /**
     * 总投注额
     */
    @TableField("bet")
    private BigDecimal bet;

    /**
     * 牌局号码
     */
    @TableField("round_no")
    private String roundNo;

    /**
     * 总派彩金额
     */
    @TableField("payout")
    private BigDecimal payout;

    /**
     * 纯派彩总额
     */
    @TableField("payout_withoutholding")
    private BigDecimal payoutWithoutholding;

    /**
     * 下注时间
     */
    @TableField("create_time")
    private Integer createTime;

    /**
     * 派彩时间
     */
    @TableField("payout_time")
    private Integer payoutTime;

    /**
     * 投注记录ID
     */
    @TableField("bet_history_id")
    private String betHistoryId;

    /**
     * 有效投注
     */
    @TableField("valid_bet")
    private BigDecimal validBet;

    /**
     * 盈余
     */
    @TableField("balance")
    private BigDecimal balance;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Integer userId;

    /**
     * 游戏平台
     */
    @TableField("platform")
    private Integer platform;

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

    /**
     * 只有百家乐.。 闲家点数
     */
    @TableField("player_result")
    private Integer playerResult;

    /**
     * 只有百家乐.。 庄家点数
     */
    @TableField("banker_result")
    private Integer bankerResult;

    /**
     * 只有龙虎。 龙的开牌结果
     */
    @TableField("dragon_card")
    private Integer dragonCard;

    /**
     * 只有龙虎。 虎的开牌结果
     */
    @TableField("tiger_card")
    private Integer tigerCard;

    /**
     * 只有轮盘。 结果号码
     */
    @TableField("number")
    private Integer number;


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

    public Integer getGameType() {
        return gameType;
    }

    public void setGameType(Integer gameType) {
        this.gameType = gameType;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public BigDecimal getBet() {
        return bet;
    }

    public void setBet(BigDecimal bet) {
        this.bet = bet;
    }

    public String getRoundNo() {
        return roundNo;
    }

    public void setRoundNo(String roundNo) {
        this.roundNo = roundNo;
    }

    public BigDecimal getPayout() {
        return payout;
    }

    public void setPayout(BigDecimal payout) {
        this.payout = payout;
    }

    public BigDecimal getPayoutWithoutholding() {
        return payoutWithoutholding;
    }

    public void setPayoutWithoutholding(BigDecimal payoutWithoutholding) {
        this.payoutWithoutholding = payoutWithoutholding;
    }

    public Integer getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Integer createTime) {
        this.createTime = createTime;
    }

    public Integer getPayoutTime() {
        return payoutTime;
    }

    public void setPayoutTime(Integer payoutTime) {
        this.payoutTime = payoutTime;
    }

    public String getBetHistoryId() {
        return betHistoryId;
    }

    public void setBetHistoryId(String betHistoryId) {
        this.betHistoryId = betHistoryId;
    }

    public BigDecimal getValidBet() {
        return validBet;
    }

    public void setValidBet(BigDecimal validBet) {
        this.validBet = validBet;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getPlatform() {
        return platform;
    }

    public void setPlatform(Integer platform) {
        this.platform = platform;
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

    public Integer getPlayerResult() {
        return playerResult;
    }

    public void setPlayerResult(Integer playerResult) {
        this.playerResult = playerResult;
    }

    public Integer getBankerResult() {
        return bankerResult;
    }

    public void setBankerResult(Integer bankerResult) {
        this.bankerResult = bankerResult;
    }

    public Integer getDragonCard() {
        return dragonCard;
    }

    public void setDragonCard(Integer dragonCard) {
        this.dragonCard = dragonCard;
    }

    public Integer getTigerCard() {
        return tigerCard;
    }

    public void setTigerCard(Integer tigerCard) {
        this.tigerCard = tigerCard;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    @Override
    public String toString() {
        return "BetslipsEbet{" +
        "id=" + id +
        ", xbUid=" + xbUid +
        ", xbUsername=" + xbUsername +
        ", gameType=" + gameType +
        ", gameName=" + gameName +
        ", bet=" + bet +
        ", roundNo=" + roundNo +
        ", payout=" + payout +
        ", payoutWithoutholding=" + payoutWithoutholding +
        ", createTime=" + createTime +
        ", payoutTime=" + payoutTime +
        ", betHistoryId=" + betHistoryId +
        ", validBet=" + validBet +
        ", balance=" + balance +
        ", username=" + username +
        ", userId=" + userId +
        ", platform=" + platform +
        ", xbCoin=" + xbCoin +
        ", xbValidCoin=" + xbValidCoin +
        ", xbProfit=" + xbProfit +
        ", xbStatus=" + xbStatus +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        ", playerResult=" + playerResult +
        ", bankerResult=" + bankerResult +
        ", dragonCard=" + dragonCard +
        ", tigerCard=" + tigerCard +
        ", number=" + number +
        "}";
    }
}
