package com.xinbo.sports.dao.generator.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

/**
 * <p>
 * 礼物表
 * </p>
 *
 * @author David
 * @since 2021-02-01
 */
@TableName("sp_gift_list")
public class GiftList extends Model<GiftList> {

    private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 礼物名称
     */
    @TableField("name")
    private String name;

    /**
     * 类型:1-升级奖励 2-投注豪礼
     */
    @TableField("category")
    private Integer category;

    /**
     * PC端地址
     */
    @TableField("img_pc")
    private String imgPc;

    /**
     * H5端地址
     */
    @TableField("img_h5")
    private String imgH5;

    /**
     * 申请条件(等级、投注金额)
     */
    @TableField("required")
    private Integer required;

    /**
     * 礼品数量
     */
    @TableField("nums")
    private Integer nums;

    /**
     * 状态:1-启用 0-停用
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public String getImgPc() {
        return imgPc;
    }

    public void setImgPc(String imgPc) {
        this.imgPc = imgPc;
    }

    public String getImgH5() {
        return imgH5;
    }

    public void setImgH5(String imgH5) {
        this.imgH5 = imgH5;
    }

    public Integer getRequired() {
        return required;
    }

    public void setRequired(Integer required) {
        this.required = required;
    }

    public Integer getNums() {
        return nums;
    }

    public void setNums(Integer nums) {
        this.nums = nums;
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
        return "GiftList{" +
        "id=" + id +
        ", name=" + name +
        ", category=" + category +
        ", imgPc=" + imgPc +
        ", imgH5=" + imgH5 +
        ", required=" + required +
        ", nums=" + nums +
        ", status=" + status +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        "}";
    }
}
