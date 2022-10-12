package com.xinbo.sports.dao.generator.po;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

/**
 * <p>
 * 佣金表
 * </p>
 *
 * @author David
 * @since 2021-02-01
 */
@TableName("sp_coin_commission")
public class CoinCommission extends Model<CoinCommission> {

    private static final long serialVersionUID=1L;

    @TableId("id")
    private Long id;

    /**
     * 代理UID
     */
    @TableField("uid")
    private Integer uid;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 代理层级
     */
    @TableField("agent_level")
    private Integer agentLevel;

    /**
     * 佣金时间
     */
    @TableField("riqi")
    private Integer riqi;

    /**
     * 佣金金额
     */
    @TableField("coin")
    private BigDecimal coin;

    /**
     * 下级UIDS
     */
    @TableField("sub_uids")
    private String subUids;

    /**
     * 下级流水总额
     */
    @TableField("sub_bet_trunover")
    private BigDecimal subBetTrunover;

    /**
     * 佣金比例
     */
    @TableField("rate")
    private BigDecimal rate;

    /**
     * 即时余额
     */
    @TableField("coin_before")
    private BigDecimal coinBefore;

    /**
     * 状态:0-未发放 1-已发放
     */
    @TableField("status")
    private Integer status;

    /**
     * 类型:0-流水佣金 1-活跃会员佣金 2-满额人头彩金
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

    public Integer getAgentLevel() {
        return agentLevel;
    }

    public void setAgentLevel(Integer agentLevel) {
        this.agentLevel = agentLevel;
    }

    public Integer getRiqi() {
        return riqi;
    }

    public void setRiqi(Integer riqi) {
        this.riqi = riqi;
    }

    public BigDecimal getCoin() {
        return coin;
    }

    public void setCoin(BigDecimal coin) {
        this.coin = coin;
    }

    public String getSubUids() {
        return subUids;
    }

    public void setSubUids(String subUids) {
        this.subUids = subUids;
    }

    public BigDecimal getSubBetTrunover() {
        return subBetTrunover;
    }

    public void setSubBetTrunover(BigDecimal subBetTrunover) {
        this.subBetTrunover = subBetTrunover;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public BigDecimal getCoinBefore() {
        return coinBefore;
    }

    public void setCoinBefore(BigDecimal coinBefore) {
        this.coinBefore = coinBefore;
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
        return "CoinCommission{" +
        "id=" + id +
        ", uid=" + uid +
        ", username=" + username +
        ", agentLevel=" + agentLevel +
        ", riqi=" + riqi +
        ", coin=" + coin +
        ", subUids=" + subUids +
        ", subBetTrunover=" + subBetTrunover +
        ", rate=" + rate +
        ", coinBefore=" + coinBefore +
        ", status=" + status +
        ", category=" + category +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        "}";
    }
}
