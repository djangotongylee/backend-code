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
 * DS注单表(棋牌)
 * </p>
 *
 * @author David
 * @since 2021-02-01
 */
@TableName("sp_betslips_ds")
public class BetslipsDs extends Model<BetslipsDs> {

    private static final long serialVersionUID=1L;

    /**
     * ID->id下注編號
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
     * 下注時間
     */
    @TableField("bet_at")
    private Date betAt;

    /**
     * 结算时间
     */
    @TableField("finish_at")
    private Date finishAt;

    /**
     * 代理帳號
     */
    @TableField("agent")
    private String agent;

    /**
     * 玩家帳號
     */
    @TableField("member")
    private String member;

    /**
     * 遊戲編號
     */
    @TableField("game_id")
    private String gameId;

    /**
     * 遊戲流水號
     */
    @TableField("game_serial")
    private String gameSerial;

    /**
     * 遊戲類型
     */
    @TableField("game_type")
    private Integer gameType;

    /**
     * 遊戲回合id
     */
    @TableField("round_id")
    private Integer roundId;

    /**
     * 下注金額
     */
    @TableField("bet_amount")
    private BigDecimal betAmount;

    /**
     * 遊戲贏分(未扣除手續費)
     */
    @TableField("payout_amount")
    private BigDecimal payoutAmount;

    /**
     * 有效金額
     */
    @TableField("valid_amount")
    private BigDecimal validAmount;

    /**
     * 下注狀態:1-正常 2-退款 3-拒絕投注 4-注單作廢 5-取消
     */
    @TableField("status")
    private Integer status;

    /**
     * 手續費
     */
    @TableField("fee_amount")
    private BigDecimal feeAmount;

    /**
     * 彩金金額
     */
    @TableField("jp_amount")
    private BigDecimal jpAmount;

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

    public Date getBetAt() {
        return betAt;
    }

    public void setBetAt(Date betAt) {
        this.betAt = betAt;
    }

    public Date getFinishAt() {
        return finishAt;
    }

    public void setFinishAt(Date finishAt) {
        this.finishAt = finishAt;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public String getMember() {
        return member;
    }

    public void setMember(String member) {
        this.member = member;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getGameSerial() {
        return gameSerial;
    }

    public void setGameSerial(String gameSerial) {
        this.gameSerial = gameSerial;
    }

    public Integer getGameType() {
        return gameType;
    }

    public void setGameType(Integer gameType) {
        this.gameType = gameType;
    }

    public Integer getRoundId() {
        return roundId;
    }

    public void setRoundId(Integer roundId) {
        this.roundId = roundId;
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

    public BigDecimal getValidAmount() {
        return validAmount;
    }

    public void setValidAmount(BigDecimal validAmount) {
        this.validAmount = validAmount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public BigDecimal getFeeAmount() {
        return feeAmount;
    }

    public void setFeeAmount(BigDecimal feeAmount) {
        this.feeAmount = feeAmount;
    }

    public BigDecimal getJpAmount() {
        return jpAmount;
    }

    public void setJpAmount(BigDecimal jpAmount) {
        this.jpAmount = jpAmount;
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
        return "BetslipsDs{" +
        "id=" + id +
        ", xbUid=" + xbUid +
        ", xbUsername=" + xbUsername +
        ", betAt=" + betAt +
        ", finishAt=" + finishAt +
        ", agent=" + agent +
        ", member=" + member +
        ", gameId=" + gameId +
        ", gameSerial=" + gameSerial +
        ", gameType=" + gameType +
        ", roundId=" + roundId +
        ", betAmount=" + betAmount +
        ", payoutAmount=" + payoutAmount +
        ", validAmount=" + validAmount +
        ", status=" + status +
        ", feeAmount=" + feeAmount +
        ", jpAmount=" + jpAmount +
        ", xbCoin=" + xbCoin +
        ", xbValidCoin=" + xbValidCoin +
        ", xbProfit=" + xbProfit +
        ", xbStatus=" + xbStatus +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        "}";
    }
}
