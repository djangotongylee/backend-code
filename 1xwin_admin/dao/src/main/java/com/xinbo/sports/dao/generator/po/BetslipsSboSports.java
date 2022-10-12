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
 * SBO注单表(体育)
 * </p>
 *
 * @author David
 * @since 2021-02-01
 */
@TableName("sp_betslips_sbo_sports")
public class BetslipsSboSports extends Model<BetslipsSboSports> {

    private static final long serialVersionUID=1L;

    /**
     * ID->refNo
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
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 投注的体育类型
     */
    @TableField("sports_type")
    private String sportsType;

    /**
     * 投注时间
     */
    @TableField("order_time")
    private Date orderTime;

    /**
     * 结算时间
     */
    @TableField("win_lost_date")
    private String winLostDate;

    /**
     * 最后修改时间
     */
    @TableField("modify_date")
    private Date modifyDate;

    /**
     * 最后修改时间
     */
    @TableField("odds")
    private BigDecimal odds;

    /**
     * 赔率类型:M-Malay odds,H-HongKong odds,E-Euro odds,I-Indonesia odds
     */
    @TableField("odds_style")
    private String oddsStyle;

    /**
     * 投注金额
     */
    @TableField("stake")
    private BigDecimal stake;

    /**
     * 实际投注金额(只有在賠率為負時actual stake與stake會不一樣)
     */
    @TableField("actual_stake")
    private BigDecimal actualStake;

    /**
     * 币种
     */
    @TableField("currency")
    private String currency;

    /**
     * 开奖结果
     */
    @TableField("status")
    private String status;

    /**
     * 输赢
     */
    @TableField("win_lost")
    private BigDecimal winLost;

    @TableField("turnover")
    private BigDecimal turnover;

    /**
     * 上半场输赢结果
     */
    @TableField("is_half_won_lose")
    private Integer isHalfWonLose;

    /**
     * 是否现场比赛
     */
    @TableField("is_live")
    private Integer isLive;

    /**
     * 拥有实际赌注的玩家的最大获胜额
     */
    @TableField("max_win_without_actual_stake")
    private BigDecimal maxWinWithoutActualStake;

    /**
     * IP
     */
    @TableField("ip")
    private String ip;

    /**
     * 投注信息
     */
    @TableField("sub_bet")
    private String subBet;

    /**
     * gameId(虚拟体育)
     */
    @TableField("game_id")
    private Integer gameId;

    /**
     * 产品类型(虚拟体育)
     */
    @TableField("product_type")
    private String productType;

    /**
     * 体育类型:1-真实体育 2-虚拟体育
     */
    @TableField("xb_sports_category")
    private Integer xbSportsCategory;

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSportsType() {
        return sportsType;
    }

    public void setSportsType(String sportsType) {
        this.sportsType = sportsType;
    }

    public Date getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(Date orderTime) {
        this.orderTime = orderTime;
    }

    public String getWinLostDate() {
        return winLostDate;
    }

    public void setWinLostDate(String winLostDate) {
        this.winLostDate = winLostDate;
    }

    public Date getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(Date modifyDate) {
        this.modifyDate = modifyDate;
    }

    public BigDecimal getOdds() {
        return odds;
    }

    public void setOdds(BigDecimal odds) {
        this.odds = odds;
    }

    public String getOddsStyle() {
        return oddsStyle;
    }

    public void setOddsStyle(String oddsStyle) {
        this.oddsStyle = oddsStyle;
    }

    public BigDecimal getStake() {
        return stake;
    }

    public void setStake(BigDecimal stake) {
        this.stake = stake;
    }

    public BigDecimal getActualStake() {
        return actualStake;
    }

    public void setActualStake(BigDecimal actualStake) {
        this.actualStake = actualStake;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getWinLost() {
        return winLost;
    }

    public void setWinLost(BigDecimal winLost) {
        this.winLost = winLost;
    }

    public BigDecimal getTurnover() {
        return turnover;
    }

    public void setTurnover(BigDecimal turnover) {
        this.turnover = turnover;
    }

    public Integer getIsHalfWonLose() {
        return isHalfWonLose;
    }

    public void setIsHalfWonLose(Integer isHalfWonLose) {
        this.isHalfWonLose = isHalfWonLose;
    }

    public Integer getIsLive() {
        return isLive;
    }

    public void setIsLive(Integer isLive) {
        this.isLive = isLive;
    }

    public BigDecimal getMaxWinWithoutActualStake() {
        return maxWinWithoutActualStake;
    }

    public void setMaxWinWithoutActualStake(BigDecimal maxWinWithoutActualStake) {
        this.maxWinWithoutActualStake = maxWinWithoutActualStake;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getSubBet() {
        return subBet;
    }

    public void setSubBet(String subBet) {
        this.subBet = subBet;
    }

    public Integer getGameId() {
        return gameId;
    }

    public void setGameId(Integer gameId) {
        this.gameId = gameId;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public Integer getXbSportsCategory() {
        return xbSportsCategory;
    }

    public void setXbSportsCategory(Integer xbSportsCategory) {
        this.xbSportsCategory = xbSportsCategory;
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
        return "BetslipsSboSports{" +
        "id=" + id +
        ", xbUid=" + xbUid +
        ", xbUsername=" + xbUsername +
        ", username=" + username +
        ", sportsType=" + sportsType +
        ", orderTime=" + orderTime +
        ", winLostDate=" + winLostDate +
        ", modifyDate=" + modifyDate +
        ", odds=" + odds +
        ", oddsStyle=" + oddsStyle +
        ", stake=" + stake +
        ", actualStake=" + actualStake +
        ", currency=" + currency +
        ", status=" + status +
        ", winLost=" + winLost +
        ", turnover=" + turnover +
        ", isHalfWonLose=" + isHalfWonLose +
        ", isLive=" + isLive +
        ", maxWinWithoutActualStake=" + maxWinWithoutActualStake +
        ", ip=" + ip +
        ", subBet=" + subBet +
        ", gameId=" + gameId +
        ", productType=" + productType +
        ", xbSportsCategory=" + xbSportsCategory +
        ", xbCoin=" + xbCoin +
        ", xbValidCoin=" + xbValidCoin +
        ", xbProfit=" + xbProfit +
        ", xbStatus=" + xbStatus +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        "}";
    }
}
