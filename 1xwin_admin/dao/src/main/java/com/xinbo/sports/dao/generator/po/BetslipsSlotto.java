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
 * slotto彩票注单表
 * </p>
 *
 * @author David
 * @since 2021-01-11
 */
@TableName("sp_betslips_slotto")
public class BetslipsSlotto extends Model<BetslipsSlotto> {

    private static final long serialVersionUID=1L;

    /**
     * id -> bet_order_no
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
     * 条号
     */
    @TableField("row_num")
    private Integer rowNum;

    /**
     * 收据编号
     */
    @TableField("order_id")
    private String orderId;

    /**
     * 用户名
     */
    @TableField("login_name")
    private String loginName;

    /**
     * 结算日期
     */
    @TableField("draw_date")
    private Date drawDate;

    /**
     * 下注日期
     */
    @TableField("date_bet")
    private Date dateBet;

    /**
     * 下注日期
     */
    @TableField("bet_type")
    private Integer betType;

    /**
     * 下注位置
     */
    @TableField("bet_position")
    private String betPosition;

    /**
     * 下注数字
     */
    @TableField("bet_number")
    private String betNumber;

    /**
     * 下注金额
     */
    @TableField("confirmed")
    private BigDecimal confirmed;

    /**
     * 赢取金额
     */
    @TableField("strikePL")
    private BigDecimal strikePL;

    /**
     * 佣金金额
     */
    @TableField("commPL")
    private BigDecimal commPL;

    /**
     * 注单状态
     */
    @TableField("Status")
    private String Status;

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

    public Integer getRowNum() {
        return rowNum;
    }

    public void setRowNum(Integer rowNum) {
        this.rowNum = rowNum;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public Date getDrawDate() {
        return drawDate;
    }

    public void setDrawDate(Date drawDate) {
        this.drawDate = drawDate;
    }

    public Date getDateBet() {
        return dateBet;
    }

    public void setDateBet(Date dateBet) {
        this.dateBet = dateBet;
    }

    public Integer getBetType() {
        return betType;
    }

    public void setBetType(Integer betType) {
        this.betType = betType;
    }

    public String getBetPosition() {
        return betPosition;
    }

    public void setBetPosition(String betPosition) {
        this.betPosition = betPosition;
    }

    public String getBetNumber() {
        return betNumber;
    }

    public void setBetNumber(String betNumber) {
        this.betNumber = betNumber;
    }

    public BigDecimal getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(BigDecimal confirmed) {
        this.confirmed = confirmed;
    }

    public BigDecimal getStrikePL() {
        return strikePL;
    }

    public void setStrikePL(BigDecimal strikePL) {
        this.strikePL = strikePL;
    }

    public BigDecimal getCommPL() {
        return commPL;
    }

    public void setCommPL(BigDecimal commPL) {
        this.commPL = commPL;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String Status) {
        this.Status = Status;
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
        return "BetslipsSlotto{" +
        "id=" + id +
        ", xbUid=" + xbUid +
        ", xbUsername=" + xbUsername +
        ", rowNum=" + rowNum +
        ", orderId=" + orderId +
        ", loginName=" + loginName +
        ", drawDate=" + drawDate +
        ", dateBet=" + dateBet +
        ", betType=" + betType +
        ", betPosition=" + betPosition +
        ", betNumber=" + betNumber +
        ", confirmed=" + confirmed +
        ", strikePL=" + strikePL +
        ", commPL=" + commPL +
        ", Status=" + Status +
        ", xbCoin=" + xbCoin +
        ", xbValidCoin=" + xbValidCoin +
        ", xbProfit=" + xbProfit +
        ", xbStatus=" + xbStatus +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        "}";
    }
}
