package com.xinbo.sports.dao.generator.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

/**
 * <p>
 * 注册彩金审核表
 * </p>
 *
 * @author David
 * @since 2021-02-01
 */
@TableName("sp_register_rewards")
public class RegisterRewards extends Model<RegisterRewards> {

    private static final long serialVersionUID=1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

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
     * IP
     */
    @TableField("ip")
    private String ip;

    /**
     * 手机号
     */
    @TableField("mobile")
    private String mobile;

    /**
     * 状态：0-待审核，1-通过，2-拒绝
     */
    @TableField("status")
    private Integer status;

    /**
     * 活动ID
     */
    @TableField("promotions_id")
    private Integer promotionsId;

    /**
     * 活动名称
     */
    @TableField("promotions_name")
    private String promotionsName;

    /**
     * 操作id
     */
    @TableField("operation_id")
    private Integer operationId;

    /**
     * 操作人名称
     */
    @TableField("operation_name")
    private String operationName;

    /**
     * 注册时间
     */
    @TableField("register_at")
    private Integer registerAt;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private Integer createdAt;

    /**
     * 修改时间
     */
    @TableField("updated_at")
    private Integer updatedAt;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getPromotionsId() {
        return promotionsId;
    }

    public void setPromotionsId(Integer promotionsId) {
        this.promotionsId = promotionsId;
    }

    public String getPromotionsName() {
        return promotionsName;
    }

    public void setPromotionsName(String promotionsName) {
        this.promotionsName = promotionsName;
    }

    public Integer getOperationId() {
        return operationId;
    }

    public void setOperationId(Integer operationId) {
        this.operationId = operationId;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public Integer getRegisterAt() {
        return registerAt;
    }

    public void setRegisterAt(Integer registerAt) {
        this.registerAt = registerAt;
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
        return "RegisterRewards{" +
        "id=" + id +
        ", uid=" + uid +
        ", username=" + username +
        ", ip=" + ip +
        ", mobile=" + mobile +
        ", status=" + status +
        ", promotionsId=" + promotionsId +
        ", promotionsName=" + promotionsName +
        ", operationId=" + operationId +
        ", operationName=" + operationName +
        ", registerAt=" + registerAt +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        "}";
    }
}
