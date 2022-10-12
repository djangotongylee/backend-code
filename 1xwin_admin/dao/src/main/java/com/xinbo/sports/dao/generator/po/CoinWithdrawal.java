package com.xinbo.sports.dao.generator.po;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

/**
 * <p>
 * 提现记录表
 * </p>
 *
 * @author David
 * @since 2021-02-01
 */
@TableName("sp_coin_withdrawal")
public class CoinWithdrawal extends Model<CoinWithdrawal> {

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
     * 提现金额
     */
    @TableField("coin")
    private BigDecimal coin;

    /**
     * 提现前用户资金
     */
    @TableField("coin_before")
    private BigDecimal coinBefore;

    /**
     * 后台操作UID
     */
    @TableField("admin_uid")
    private Integer adminUid;

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
     * 提现类型:0-其他 1-首次提款
     */
    @TableField("category")
    private Integer category;

    /**
     * 状态:0-申请中 1-提款成功 2-提款失败 3-稽核成功 4-稽核失败 9-系统出款
     */
    @TableField("status")
    private Integer status;

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

    public BigDecimal getCoin() {
        return coin;
    }

    public void setCoin(BigDecimal coin) {
        this.coin = coin;
    }

    public BigDecimal getCoinBefore() {
        return coinBefore;
    }

    public void setCoinBefore(BigDecimal coinBefore) {
        this.coinBefore = coinBefore;
    }

    public Integer getAdminUid() {
        return adminUid;
    }

    public void setAdminUid(Integer adminUid) {
        this.adminUid = adminUid;
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

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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
        return "CoinWithdrawal{" +
        "id=" + id +
        ", uid=" + uid +
        ", username=" + username +
        ", coin=" + coin +
        ", coinBefore=" + coinBefore +
        ", adminUid=" + adminUid +
        ", bankInfo=" + bankInfo +
        ", mark=" + mark +
        ", category=" + category +
        ", status=" + status +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        "}";
    }
}
