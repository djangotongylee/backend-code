package com.xinbo.sports.dao.generator.po;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

/**
 * <p>
 * 账变明细
 * </p>
 *
 * @author David
 * @since 2021-02-01
 */
@TableName("sp_coin_log")
public class CoinLog extends Model<CoinLog> {

    private static final long serialVersionUID=1L;

    @TableId("id")
    private Long id;

    /**
     * UID
     */
    @TableField("uid")
    private Integer uid;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 类型:1-存款 2-提款 3-上分 4-下分 5-返水 6-佣金 7-活动(奖励) 8-系统调账
     */
    @TableField("category")
    private Integer category;

    /**
     * 子类型:活动表-id
     */
    @TableField("sub_category")
    private Integer subCategory;

    /**
     * 关联ID
     */
    @TableField("refer_id")
    private Long referId;

    /**
     * 金额
     */
    @TableField("coin")
    private BigDecimal coin;

    /**
     * 前金额
     */
    @TableField("coin_before")
    private BigDecimal coinBefore;

    /**
     * 收支类型:0-支出 1-收入
     */
    @TableField("out_in")
    private Integer outIn;

    /**
     * 状态:0-处理中 1-成功 2-失败
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

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public Integer getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(Integer subCategory) {
        this.subCategory = subCategory;
    }

    public Long getReferId() {
        return referId;
    }

    public void setReferId(Long referId) {
        this.referId = referId;
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

    public Integer getOutIn() {
        return outIn;
    }

    public void setOutIn(Integer outIn) {
        this.outIn = outIn;
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
        return "CoinLog{" +
        "id=" + id +
        ", uid=" + uid +
        ", username=" + username +
        ", category=" + category +
        ", subCategory=" + subCategory +
        ", referId=" + referId +
        ", coin=" + coin +
        ", coinBefore=" + coinBefore +
        ", outIn=" + outIn +
        ", status=" + status +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        "}";
    }
}
