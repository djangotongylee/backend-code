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
 * SV388斗鸡注单表
 * </p>
 *
 * @author David
 * @since 2021-02-01
 */
@TableName("sp_betslips_sv388")
public class BetslipsSv388 extends Model<BetslipsSv388> {

    private static final long serialVersionUID=1L;

    /**
     * 注单号 对应三方拉单GameInstanceId
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
     * 供应商注单号
     */
    @TableField("ref_no")
    private String refNo;

    /**
     * 供应商代号
     */
    @TableField("site")
    private String site;

    /**
     * 游戏类型代号
     */
    @TableField("product")
    private String product;

    /**
     * Username 玩家帐号
     */
    @TableField("member")
    private String member;

    /**
     * 游戏代号
     */
    @TableField("game_id")
    private String gameId;

    /**
     * 游戏开始时间
     */
    @TableField("start_time")
    private Date startTime;

    /**
     * 游戏结束时间
     */
    @TableField("match_time")
    private Date matchTime;

    /**
     * 游戏结束时间
     */
    @TableField("end_time")
    private Date endTime;

    /**
     * 游戏编号名称
     */
    @TableField("bet_detail")
    private String betDetail;

    /**
     * 有效投注金额
     */
    @TableField("turnover")
    private BigDecimal turnover;

    /**
     * 投注金额
     */
    @TableField("bet")
    private BigDecimal bet;

    /**
     * 派彩金额
     */
    @TableField("payout")
    private BigDecimal payout;

    /**
     * 佣金
     */
    @TableField("commission")
    private BigDecimal commission;

    /**
     *  彩池投注金额
     */
    @TableField("p_share")
    private BigDecimal pShare;

    /**
     * 彩池派彩金额
     */
    @TableField("p_win")
    private BigDecimal pWin;

    /**
     * 注单状态
     */
    @TableField("status")
    private Integer status;

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

    public String getRefNo() {
        return refNo;
    }

    public void setRefNo(String refNo) {
        this.refNo = refNo;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
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

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getMatchTime() {
        return matchTime;
    }

    public void setMatchTime(Date matchTime) {
        this.matchTime = matchTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getBetDetail() {
        return betDetail;
    }

    public void setBetDetail(String betDetail) {
        this.betDetail = betDetail;
    }

    public BigDecimal getTurnover() {
        return turnover;
    }

    public void setTurnover(BigDecimal turnover) {
        this.turnover = turnover;
    }

    public BigDecimal getBet() {
        return bet;
    }

    public void setBet(BigDecimal bet) {
        this.bet = bet;
    }

    public BigDecimal getPayout() {
        return payout;
    }

    public void setPayout(BigDecimal payout) {
        this.payout = payout;
    }

    public BigDecimal getCommission() {
        return commission;
    }

    public void setCommission(BigDecimal commission) {
        this.commission = commission;
    }

    public BigDecimal getpShare() {
        return pShare;
    }

    public void setpShare(BigDecimal pShare) {
        this.pShare = pShare;
    }

    public BigDecimal getpWin() {
        return pWin;
    }

    public void setpWin(BigDecimal pWin) {
        this.pWin = pWin;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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
        return "BetslipsSv388{" +
        "id=" + id +
        ", xbUid=" + xbUid +
        ", xbUsername=" + xbUsername +
        ", refNo=" + refNo +
        ", site=" + site +
        ", product=" + product +
        ", member=" + member +
        ", gameId=" + gameId +
        ", startTime=" + startTime +
        ", matchTime=" + matchTime +
        ", endTime=" + endTime +
        ", betDetail=" + betDetail +
        ", turnover=" + turnover +
        ", bet=" + bet +
        ", payout=" + payout +
        ", commission=" + commission +
        ", pShare=" + pShare +
        ", pWin=" + pWin +
        ", status=" + status +
        ", xbCoin=" + xbCoin +
        ", xbValidCoin=" + xbValidCoin +
        ", xbProfit=" + xbProfit +
        ", xbStatus=" + xbStatus +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        "}";
    }
}
