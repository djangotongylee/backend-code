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
 * GG注单表
 * </p>
 *
 * @author David
 * @since 2021-02-01
 */
@TableName("sp_betslips_gg")
public class BetslipsGg extends Model<BetslipsGg> {

    private static final long serialVersionUID=1L;

    /**
     * id -> betid
     */
    @TableId("id")
    private String id;

    /**
     * 订单唯一单号
     */
    @TableField("bet_id")
    private String betId;

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
     * 游戏编码
     */
    @TableField("game_id")
    private String gameId;

    /**
     * 投注金额
     */
    @TableField("bet")
    private BigDecimal bet;

    /**
     * 货币
     */
    @TableField("currency")
    private String currency;

    /**
     * 局号
     */
    @TableField("link_id")
    private String linkId;

    /**
     * 用户名
     */
    @TableField("account_no")
    private String accountNo;

    /**
     * 自增长ID
     */
    @TableField("auto_id")
    private Long autoId;

    /**
     * 标识:1-已结算 0-未结算
     */
    @TableField("closed")
    private Integer closed;

    /**
     * 投注时间
     */
    @TableField("bettime_str")
    private Date bettimeStr;

    /**
     * 结算时间
     */
    @TableField("paytime_str")
    private Date paytimeStr;

    /**
     * 输赢
     */
    @TableField("profit")
    private BigDecimal profit;

    /**
     * 来源:0-PCWeb 1-Android 2-iOS 3-AndroidWeb 4-iOSWEB)
     */
    @TableField("origin")
    private Integer origin;

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

    public String getBetId() {
        return betId;
    }

    public void setBetId(String betId) {
        this.betId = betId;
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

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public BigDecimal getBet() {
        return bet;
    }

    public void setBet(BigDecimal bet) {
        this.bet = bet;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getLinkId() {
        return linkId;
    }

    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public Long getAutoId() {
        return autoId;
    }

    public void setAutoId(Long autoId) {
        this.autoId = autoId;
    }

    public Integer getClosed() {
        return closed;
    }

    public void setClosed(Integer closed) {
        this.closed = closed;
    }

    public Date getBettimeStr() {
        return bettimeStr;
    }

    public void setBettimeStr(Date bettimeStr) {
        this.bettimeStr = bettimeStr;
    }

    public Date getPaytimeStr() {
        return paytimeStr;
    }

    public void setPaytimeStr(Date paytimeStr) {
        this.paytimeStr = paytimeStr;
    }

    public BigDecimal getProfit() {
        return profit;
    }

    public void setProfit(BigDecimal profit) {
        this.profit = profit;
    }

    public Integer getOrigin() {
        return origin;
    }

    public void setOrigin(Integer origin) {
        this.origin = origin;
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
        return "BetslipsGg{" +
        "id=" + id +
        ", betId=" + betId +
        ", xbUid=" + xbUid +
        ", xbUsername=" + xbUsername +
        ", gameId=" + gameId +
        ", bet=" + bet +
        ", currency=" + currency +
        ", linkId=" + linkId +
        ", accountNo=" + accountNo +
        ", autoId=" + autoId +
        ", closed=" + closed +
        ", bettimeStr=" + bettimeStr +
        ", paytimeStr=" + paytimeStr +
        ", profit=" + profit +
        ", origin=" + origin +
        ", xbCoin=" + xbCoin +
        ", xbValidCoin=" + xbValidCoin +
        ", xbProfit=" + xbProfit +
        ", xbStatus=" + xbStatus +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        "}";
    }
}
