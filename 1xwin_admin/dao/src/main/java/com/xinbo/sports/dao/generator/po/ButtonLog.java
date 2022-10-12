package com.xinbo.sports.dao.generator.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author David
 * @since 2021-02-01
 */
@TableName("sp_button_log")
public class ButtonLog extends Model<ButtonLog> {

    private static final long serialVersionUID=1L;

    @TableId(value = "r_id", type = IdType.AUTO)
    private Integer rId;

    @TableField("title")
    private String title;

    @TableField("p_id")
    private Integer pId;

    /**
     * 0否 1是
     */
    @TableField("is_button")
    private Integer isButton;

    @TableField("create_time")
    private Date createTime;


    public Integer getrId() {
        return rId;
    }

    public void setrId(Integer rId) {
        this.rId = rId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getpId() {
        return pId;
    }

    public void setpId(Integer pId) {
        this.pId = pId;
    }

    public Integer getIsButton() {
        return isButton;
    }

    public void setIsButton(Integer isButton) {
        this.isButton = isButton;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    protected Serializable pkVal() {
        return this.rId;
    }

    @Override
    public String toString() {
        return "ButtonLog{" +
        "rId=" + rId +
        ", title=" + title +
        ", pId=" + pId +
        ", isButton=" + isButton +
        ", createTime=" + createTime +
        "}";
    }
}
