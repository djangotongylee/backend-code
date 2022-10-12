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
 * DG注单表
 * </p>
 *
 * @author David
 * @since 2021-02-01
 */
@TableName("sp_betslips_dg")
public class BetslipsDg extends Model<BetslipsDg> {

    private static final long serialVersionUID=1L;

    /**
     * ID -> id
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
     * 会员账号
     */
    @TableField("username")
    private String username;

    /**
     * 游戏大厅号: 1,2-旗舰厅 3,4-现场厅 5-联盟厅 7-国际厅
     */
    @TableField("lobby_id")
    private Integer lobbyId;

    /**
     * 游戏桌号
     */
    @TableField("table_id")
    private Integer tableId;

    /**
     * 游戏靴号
     */
    @TableField("shoe_id")
    private Long shoeId;

    /**
     * 游戏局号
     */
    @TableField("play_id")
    private Long playId;

    /**
     * 游戏类型
     */
    @TableField("game_type")
    private Integer gameType;

    /**
     * 游戏Id
     */
    @TableField("game_id")
    private Integer gameId;

    /**
     * 会员Id
     */
    @TableField("member_id")
    private Long memberId;

    /**
     * 游戏下注时间
     */
    @TableField("bet_time")
    private Date betTime;

    /**
     * 游戏结算时间
     */
    @TableField("cal_time")
    private Date calTime;

    /**
     * 派彩金额 (输赢应扣除下注金额),总派彩金额
     */
    @TableField("win_or_Loss")
    private BigDecimal winOrLoss;

    /**
     * 好路追注派彩金额
     */
    @TableField("win_or_lossz")
    private BigDecimal winOrLossz;

    /**
     * 下注金额(总金额)
     */
    @TableField("bet_points")
    private BigDecimal betPoints;

    /**
     * 好路追注金额
     */
    @TableField("bet_pointsz")
    private BigDecimal betPointsz;

    /**
     * 有效下注金额
     */
    @TableField("available_bet")
    private BigDecimal availableBet;

    /**
     * 游戏结果
     */
    @TableField("result")
    private String result;

    /**
     * 下注注单(总单)
     */
    @TableField("bet_detail")
    private String betDetail;

    /**
     * 好路追注注单
     */
    @TableField("bet_detailz")
    private String betDetailz;

    /**
     * 下注时客户端IP
     */
    @TableField("ip")
    private String ip;

    /**
     * 游戏唯一ID
     */
    @TableField("ext")
    private String ext;

    /**
     * 是否结算:0-未结算 1-已结算 2-已撤销(该注单为对冲注单)
     */
    @TableField("is_revocation")
    private Integer isRevocation;

    /**
     * 余额
     */
    @TableField("balance_before")
    private BigDecimal balanceBefore;

    /**
     * 撤销的那比注单的ID(对冲注单才有)
     */
    @TableField("parent_bet_id")
    private Long parentBetId;

    /**
     * 货币ID
     */
    @TableField("currency_id")
    private Integer currencyId;

    /**
     * 下注时客户端类
     */
    @TableField("device_type")
    private Integer deviceType;

    /**
     * 追注转账流水号(共享钱包API可用于对账,普通转账API可忽略)
     */
    @TableField("plugin_id")
    private Long pluginId;

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

    public Integer getLobbyId() {
        return lobbyId;
    }

    public void setLobbyId(Integer lobbyId) {
        this.lobbyId = lobbyId;
    }

    public Integer getTableId() {
        return tableId;
    }

    public void setTableId(Integer tableId) {
        this.tableId = tableId;
    }

    public Long getShoeId() {
        return shoeId;
    }

    public void setShoeId(Long shoeId) {
        this.shoeId = shoeId;
    }

    public Long getPlayId() {
        return playId;
    }

    public void setPlayId(Long playId) {
        this.playId = playId;
    }

    public Integer getGameType() {
        return gameType;
    }

    public void setGameType(Integer gameType) {
        this.gameType = gameType;
    }

    public Integer getGameId() {
        return gameId;
    }

    public void setGameId(Integer gameId) {
        this.gameId = gameId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public Date getBetTime() {
        return betTime;
    }

    public void setBetTime(Date betTime) {
        this.betTime = betTime;
    }

    public Date getCalTime() {
        return calTime;
    }

    public void setCalTime(Date calTime) {
        this.calTime = calTime;
    }

    public BigDecimal getWinOrLoss() {
        return winOrLoss;
    }

    public void setWinOrLoss(BigDecimal winOrLoss) {
        this.winOrLoss = winOrLoss;
    }

    public BigDecimal getWinOrLossz() {
        return winOrLossz;
    }

    public void setWinOrLossz(BigDecimal winOrLossz) {
        this.winOrLossz = winOrLossz;
    }

    public BigDecimal getBetPoints() {
        return betPoints;
    }

    public void setBetPoints(BigDecimal betPoints) {
        this.betPoints = betPoints;
    }

    public BigDecimal getBetPointsz() {
        return betPointsz;
    }

    public void setBetPointsz(BigDecimal betPointsz) {
        this.betPointsz = betPointsz;
    }

    public BigDecimal getAvailableBet() {
        return availableBet;
    }

    public void setAvailableBet(BigDecimal availableBet) {
        this.availableBet = availableBet;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getBetDetail() {
        return betDetail;
    }

    public void setBetDetail(String betDetail) {
        this.betDetail = betDetail;
    }

    public String getBetDetailz() {
        return betDetailz;
    }

    public void setBetDetailz(String betDetailz) {
        this.betDetailz = betDetailz;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public Integer getIsRevocation() {
        return isRevocation;
    }

    public void setIsRevocation(Integer isRevocation) {
        this.isRevocation = isRevocation;
    }

    public BigDecimal getBalanceBefore() {
        return balanceBefore;
    }

    public void setBalanceBefore(BigDecimal balanceBefore) {
        this.balanceBefore = balanceBefore;
    }

    public Long getParentBetId() {
        return parentBetId;
    }

    public void setParentBetId(Long parentBetId) {
        this.parentBetId = parentBetId;
    }

    public Integer getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(Integer currencyId) {
        this.currencyId = currencyId;
    }

    public Integer getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(Integer deviceType) {
        this.deviceType = deviceType;
    }

    public Long getPluginId() {
        return pluginId;
    }

    public void setPluginId(Long pluginId) {
        this.pluginId = pluginId;
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
        return "BetslipsDg{" +
        "id=" + id +
        ", xbUid=" + xbUid +
        ", xbUsername=" + xbUsername +
        ", username=" + username +
        ", lobbyId=" + lobbyId +
        ", tableId=" + tableId +
        ", shoeId=" + shoeId +
        ", playId=" + playId +
        ", gameType=" + gameType +
        ", gameId=" + gameId +
        ", memberId=" + memberId +
        ", betTime=" + betTime +
        ", calTime=" + calTime +
        ", winOrLoss=" + winOrLoss +
        ", winOrLossz=" + winOrLossz +
        ", betPoints=" + betPoints +
        ", betPointsz=" + betPointsz +
        ", availableBet=" + availableBet +
        ", result=" + result +
        ", betDetail=" + betDetail +
        ", betDetailz=" + betDetailz +
        ", ip=" + ip +
        ", ext=" + ext +
        ", isRevocation=" + isRevocation +
        ", balanceBefore=" + balanceBefore +
        ", parentBetId=" + parentBetId +
        ", currencyId=" + currencyId +
        ", deviceType=" + deviceType +
        ", pluginId=" + pluginId +
        ", xbCoin=" + xbCoin +
        ", xbValidCoin=" + xbValidCoin +
        ", xbProfit=" + xbProfit +
        ", xbStatus=" + xbStatus +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        "}";
    }
}
