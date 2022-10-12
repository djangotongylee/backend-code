package com.xinbo.sports.dao.generator.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

/**
 * <p>
 * 离线渠道配置表
 * </p>
 *
 * @author David
 * @since 2021-02-01
 */
@TableName("sp_pay_offline")
public class PayOffline extends Model<PayOffline> {

    private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 持卡人
     */
    @TableField("user_name")
    private String userName;

    /**
     * 收款银行
     */
    @TableField("bank_name")
    private String bankName;

    /**
     * 收款卡号
     */
    @TableField("bank_account")
    private String bankAccount;

    /**
     * 收款开户行地址
     */
    @TableField("bank_address")
    private String bankAddress;

    /**
     * 收款二维码
     */
    @TableField("qr_code")
    private String qrCode;

    /**
     * 最低支付
     */
    @TableField("coin_min")
    private Integer coinMin;

    /**
     * 最高支付
     */
    @TableField("coin_max")
    private Integer coinMax;

    /**
     * 快捷金额
     */
    @TableField("coin_range")
    private String coinRange;

    /**
     * 类型:1-银行卡 2-微信 3-支付宝
     */
    @TableField("category")
    private Integer category;

    /**
     * 银行卡关联bank_list id
     */
    @TableField("bank_id")
    private Integer bankId;

    /**
     * 分层(user_level位运算和)
     */
    @TableField("level_bit")
    private Integer levelBit;

    /**
     * 排序:从高到低
     */
    @TableField("sort")
    private Integer sort;

    /**
     * 状态:1-启用 0-停用 2-删除
     */
    @TableField("status")
    private Integer status;

    /**
     * 联系方式:1-在线客服 2-QQ 3-微信 4-WhatsApp
     */
    @TableField("contact_type")
    private Integer contactType;

    /**
     * 联系号码
     */
    @TableField("contact_nubmber")
    private String contactNubmber;

    @TableField("created_at")
    private Integer createdAt;

    @TableField("updated_at")
    private Integer updatedAt;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getBankAddress() {
        return bankAddress;
    }

    public void setBankAddress(String bankAddress) {
        this.bankAddress = bankAddress;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public Integer getCoinMin() {
        return coinMin;
    }

    public void setCoinMin(Integer coinMin) {
        this.coinMin = coinMin;
    }

    public Integer getCoinMax() {
        return coinMax;
    }

    public void setCoinMax(Integer coinMax) {
        this.coinMax = coinMax;
    }

    public String getCoinRange() {
        return coinRange;
    }

    public void setCoinRange(String coinRange) {
        this.coinRange = coinRange;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public Integer getBankId() {
        return bankId;
    }

    public void setBankId(Integer bankId) {
        this.bankId = bankId;
    }

    public Integer getLevelBit() {
        return levelBit;
    }

    public void setLevelBit(Integer levelBit) {
        this.levelBit = levelBit;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getContactType() {
        return contactType;
    }

    public void setContactType(Integer contactType) {
        this.contactType = contactType;
    }

    public String getContactNubmber() {
        return contactNubmber;
    }

    public void setContactNubmber(String contactNubmber) {
        this.contactNubmber = contactNubmber;
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
        return "PayOffline{" +
        "id=" + id +
        ", userName=" + userName +
        ", bankName=" + bankName +
        ", bankAccount=" + bankAccount +
        ", bankAddress=" + bankAddress +
        ", qrCode=" + qrCode +
        ", coinMin=" + coinMin +
        ", coinMax=" + coinMax +
        ", coinRange=" + coinRange +
        ", category=" + category +
        ", bankId=" + bankId +
        ", levelBit=" + levelBit +
        ", sort=" + sort +
        ", status=" + status +
        ", contactType=" + contactType +
        ", contactNubmber=" + contactNubmber +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        "}";
    }
}
