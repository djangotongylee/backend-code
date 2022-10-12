package com.xinbo.sports.dao.generator.po;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

/**
 * <p>
 * 邀请好友奖励表
 * </p>
 *
 * @author David
 * @since 2021-02-01
 */
@TableName("sp_coin_rewards_invite")
public class CoinRewardsInvite extends Model<CoinRewardsInvite> {

    private static final long serialVersionUID=1L;

    @TableId("id")
    private Long id;

    /**
     * 好友ID
     */
    @TableField("uid")
    private Integer uid;

    /**
     * 好友用户名
     */
    @TableField("username")
    private String username;

    /**
     * 金额类型:1-首存金额 2-友注册当天总存款
     */
    @TableField("coin")
    private BigDecimal coin;

    /**
     * 同sp_coin_rewards表Id
     */
    @TableField("refer_id")
    private Long referId;

    /**
     * 种类:0-被邀请奖金 1-邀请奖金 2-充值返利
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

    public BigDecimal getCoin() {
        return coin;
    }

    public void setCoin(BigDecimal coin) {
        this.coin = coin;
    }

    public Long getReferId() {
        return referId;
    }

    public void setReferId(Long referId) {
        this.referId = referId;
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
        return "CoinRewardsInvite{" +
        "id=" + id +
        ", uid=" + uid +
        ", username=" + username +
        ", coin=" + coin +
        ", referId=" + referId +
        ", category=" + category +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        "}";
    }
}
