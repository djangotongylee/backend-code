package com.xinbo.sports.dao.generator.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

/**
 * <p>
 * 在线渠道配置表
 * </p>
 *
 * @author David
 * @since 2021-02-01
 */
@TableName("sp_pay_online")
public class PayOnline extends Model<PayOnline> {

    private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 三方Code
     */
    @TableField("code")
    private String code;

    /**
     * 支付名称
     */
    @TableField("pay_name")
    private String payName;

    /**
     * 支付模型
     */
    @TableField("pay_model")
    private String payModel;

    /**
     * 最低支付金额
     */
    @TableField("coin_min")
    private Integer coinMin;

    /**
     * 最高支付金额
     */
    @TableField("coin_max")
    private Integer coinMax;

    /**
     * 快捷金额
     */
    @TableField("coin_range")
    private String coinRange;

    /**
     * 类型:1-银联 2-微信 3-支付宝 4-QQ 5-QR扫码 6-upi
     */
    @TableField("category")
    private Integer category;

    /**
     * 支付分层(user_level位运算和)
     */
    @TableField("level_bit")
    private Integer levelBit;

    /**
     * 排序:从高到低
     */
    @TableField("sort")
    private Integer sort;

    /**
     * 备注信息
     */
    @TableField("mark")
    private String mark;

    /**
     * 状态:1-启用 0-停用 2-删除
     */
    @TableField("status")
    private Integer status;

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPayName() {
        return payName;
    }

    public void setPayName(String payName) {
        this.payName = payName;
    }

    public String getPayModel() {
        return payModel;
    }

    public void setPayModel(String payModel) {
        this.payModel = payModel;
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
        return "PayOnline{" +
        "id=" + id +
        ", code=" + code +
        ", payName=" + payName +
        ", payModel=" + payModel +
        ", coinMin=" + coinMin +
        ", coinMax=" + coinMax +
        ", coinRange=" + coinRange +
        ", category=" + category +
        ", levelBit=" + levelBit +
        ", sort=" + sort +
        ", mark=" + mark +
        ", status=" + status +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        "}";
    }
}
