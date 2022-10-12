package com.xinbo.sports.dao.generator.po;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

/**
 * <p>
 * 代付记录表
 * </p>
 *
 * @author David
 * @since 2021-02-01
 */
@TableName("sp_coin_online_withdrawal")
public class CoinOnlineWithdrawal extends Model<CoinOnlineWithdrawal> {

    private static final long serialVersionUID=1L;

    @TableId("id")
    private Long id;

    /**
     * 用户ID
     */
    @TableField("uid")
    private Integer uid;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 代付订单id
     */
    @TableField("order_id")
    private String orderId;

    /**
     * 提现订单id
     */
    @TableField("withdrawal_order_id")
    private String withdrawalOrderId;

    /**
     * 代付代码
     */
    @TableField("payout_code")
    private String payoutCode;

    /**
     * 提现金额
     */
    @TableField("coin")
    private BigDecimal coin;

    /**
     * bank_info信息
     */
    @TableField("bank_info")
    private String bankInfo;

    /**
     * 备注
     */
    @TableField("mark")
    private String mark;

    /**
     * 状态: 0-申请 1-提款成功 2-提款失败  
     */
    @TableField("status")
    private Integer status;

    /**
     * 提现通道类型:0-upi 1-bankcard
     */
    @TableField("category")
    private Integer category;

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

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getWithdrawalOrderId() {
        return withdrawalOrderId;
    }

    public void setWithdrawalOrderId(String withdrawalOrderId) {
        this.withdrawalOrderId = withdrawalOrderId;
    }

    public String getPayoutCode() {
        return payoutCode;
    }

    public void setPayoutCode(String payoutCode) {
        this.payoutCode = payoutCode;
    }

    public BigDecimal getCoin() {
        return coin;
    }

    public void setCoin(BigDecimal coin) {
        this.coin = coin;
    }

    public String getBankInfo() {
        return bankInfo;
    }

    public void setBankInfo(String bankInfo) {
        this.bankInfo = bankInfo;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
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
        return "CoinOnlineWithdrawal{" +
        "id=" + id +
        ", uid=" + uid +
        ", username=" + username +
        ", orderId=" + orderId +
        ", withdrawalOrderId=" + withdrawalOrderId +
        ", payoutCode=" + payoutCode +
        ", coin=" + coin +
        ", bankInfo=" + bankInfo +
        ", mark=" + mark +
        ", status=" + status +
        ", category=" + category +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        "}";
    }
}
