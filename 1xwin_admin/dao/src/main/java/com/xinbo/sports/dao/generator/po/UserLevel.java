package com.xinbo.sports.dao.generator.po;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

/**
 * <p>
 * 会员等级
 * </p>
 *
 * @author David
 * @since 2021-02-01
 */
@TableName("sp_user_level")
public class UserLevel extends Model<UserLevel> {

    private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 位运算占位符值
     */
    @TableField("bit_code")
    private Integer bitCode;

    /**
     * 会员等级
     */
    @TableField("code")
    private String code;

    /**
     * 等级名称
     */
    @TableField("name")
    private String name;

    /**
     * 升级积分
     */
    @TableField("score_upgrade")
    private BigDecimal scoreUpgrade;

    /**
     * 保级积分
     */
    @TableField("score_relegation")
    private BigDecimal scoreRelegation;

    /**
     * 升级奖励
     */
    @TableField("rewards_upgrade")
    private BigDecimal rewardsUpgrade;

    /**
     * 每月红包
     */
    @TableField("rewards_monthly")
    private BigDecimal rewardsMonthly;

    /**
     * 生日奖励
     */
    @TableField("rewards_birthday")
    private BigDecimal rewardsBirthday;

    /**
     * 提款次数(每日)
     */
    @TableField("withdrawal_nums")
    private Integer withdrawalNums;

    /**
     * 提款限额(万/日)
     */
    @TableField("withdrawal_total_coin")
    private Integer withdrawalTotalCoin;

    /**
     * 返水上限
     */
    @TableField("max_rebate_coin")
    private BigDecimal maxRebateCoin;

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

    public Integer getBitCode() {
        return bitCode;
    }

    public void setBitCode(Integer bitCode) {
        this.bitCode = bitCode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getScoreUpgrade() {
        return scoreUpgrade;
    }

    public void setScoreUpgrade(BigDecimal scoreUpgrade) {
        this.scoreUpgrade = scoreUpgrade;
    }

    public BigDecimal getScoreRelegation() {
        return scoreRelegation;
    }

    public void setScoreRelegation(BigDecimal scoreRelegation) {
        this.scoreRelegation = scoreRelegation;
    }

    public BigDecimal getRewardsUpgrade() {
        return rewardsUpgrade;
    }

    public void setRewardsUpgrade(BigDecimal rewardsUpgrade) {
        this.rewardsUpgrade = rewardsUpgrade;
    }

    public BigDecimal getRewardsMonthly() {
        return rewardsMonthly;
    }

    public void setRewardsMonthly(BigDecimal rewardsMonthly) {
        this.rewardsMonthly = rewardsMonthly;
    }

    public BigDecimal getRewardsBirthday() {
        return rewardsBirthday;
    }

    public void setRewardsBirthday(BigDecimal rewardsBirthday) {
        this.rewardsBirthday = rewardsBirthday;
    }

    public Integer getWithdrawalNums() {
        return withdrawalNums;
    }

    public void setWithdrawalNums(Integer withdrawalNums) {
        this.withdrawalNums = withdrawalNums;
    }

    public Integer getWithdrawalTotalCoin() {
        return withdrawalTotalCoin;
    }

    public void setWithdrawalTotalCoin(Integer withdrawalTotalCoin) {
        this.withdrawalTotalCoin = withdrawalTotalCoin;
    }

    public BigDecimal getMaxRebateCoin() {
        return maxRebateCoin;
    }

    public void setMaxRebateCoin(BigDecimal maxRebateCoin) {
        this.maxRebateCoin = maxRebateCoin;
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
        return "UserLevel{" +
        "id=" + id +
        ", bitCode=" + bitCode +
        ", code=" + code +
        ", name=" + name +
        ", scoreUpgrade=" + scoreUpgrade +
        ", scoreRelegation=" + scoreRelegation +
        ", rewardsUpgrade=" + rewardsUpgrade +
        ", rewardsMonthly=" + rewardsMonthly +
        ", rewardsBirthday=" + rewardsBirthday +
        ", withdrawalNums=" + withdrawalNums +
        ", withdrawalTotalCoin=" + withdrawalTotalCoin +
        ", maxRebateCoin=" + maxRebateCoin +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        "}";
    }
}
