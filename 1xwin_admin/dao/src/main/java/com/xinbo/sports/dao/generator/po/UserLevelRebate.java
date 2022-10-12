package com.xinbo.sports.dao.generator.po;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

/**
 * <p>
 * 会员等级返水
 * </p>
 *
 * @author David
 * @since 2021-02-01
 */
@TableName("sp_user_level_rebate")
public class UserLevelRebate extends Model<UserLevelRebate> {

    private static final long serialVersionUID=1L;

    /**
     * 会员等级ID
     */
    @TableId("level_id")
    private Integer levelId;

    /**
     * 游戏组:1-体育赛事 2-电子游戏 3-真人娱乐 4-捕鱼游戏 5-棋牌游戏 6-电子竞技
     */
    @TableField("group_id")
    private Integer groupId;

    /**
     * 返水比例
     */
    @TableField("rebate_rate")
    private BigDecimal rebateRate;

    /**
     * 状态:1-启用 0-停用
     */
    @TableField("status")
    private Integer status;

    @TableField("created_at")
    private Integer createdAt;

    @TableField("updated_at")
    private Integer updatedAt;


    public Integer getLevelId() {
        return levelId;
    }

    public void setLevelId(Integer levelId) {
        this.levelId = levelId;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public BigDecimal getRebateRate() {
        return rebateRate;
    }

    public void setRebateRate(BigDecimal rebateRate) {
        this.rebateRate = rebateRate;
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
        return this.levelId;
    }

    @Override
    public String toString() {
        return "UserLevelRebate{" +
        "levelId=" + levelId +
        ", groupId=" + groupId +
        ", rebateRate=" + rebateRate +
        ", status=" + status +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        "}";
    }
}
