package com.xinbo.sports.dao.generator.po;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

/**
 * <p>
 * 平台上下分记录表
 * </p>
 *
 * @author David
 * @since 2021-02-01
 */
@TableName("sp_coin_plat_transfer")
public class CoinPlatTransfer extends Model<CoinPlatTransfer> {

    private static final long serialVersionUID=1L;

    @TableId("id")
    private Long id;

    /**
     * 平台订单号
     */
    @TableField("order_plat")
    private String orderPlat;

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
     * 金额
     */
    @TableField("coin")
    private BigDecimal coin;

    /**
     * 操作前金额
     */
    @TableField("coin_before")
    private BigDecimal coinBefore;

    /**
     * sp_plat_list主键Id
     */
    @TableField("plat_list_id")
    private Integer platListId;

    /**
     * 类型:0-上分 1-下分
     */
    @TableField("category")
    private Integer category;

    /**
     * 备注
     */
    @TableField("mark")
    private String mark;

    /**
     * 是否校验: 1-校验 0-未校验
     */
    @TableField("checked")
    private Integer checked;

    /**
     * 状态:0-提交申请 1-成功 2-失败
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

    public String getOrderPlat() {
        return orderPlat;
    }

    public void setOrderPlat(String orderPlat) {
        this.orderPlat = orderPlat;
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

    public Integer getPlatListId() {
        return platListId;
    }

    public void setPlatListId(Integer platListId) {
        this.platListId = platListId;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public Integer getChecked() {
        return checked;
    }

    public void setChecked(Integer checked) {
        this.checked = checked;
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
        return "CoinPlatTransfer{" +
        "id=" + id +
        ", orderPlat=" + orderPlat +
        ", uid=" + uid +
        ", username=" + username +
        ", coin=" + coin +
        ", coinBefore=" + coinBefore +
        ", platListId=" + platListId +
        ", category=" + category +
        ", mark=" + mark +
        ", checked=" + checked +
        ", status=" + status +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        "}";
    }
}
