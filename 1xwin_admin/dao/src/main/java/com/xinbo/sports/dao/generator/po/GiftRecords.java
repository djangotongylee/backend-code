package com.xinbo.sports.dao.generator.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

/**
 * <p>
 * 礼物记录表
 * </p>
 *
 * @author David
 * @since 2021-02-01
 */
@TableName("sp_gift_records")
public class GiftRecords extends Model<GiftRecords> {

    private static final long serialVersionUID=1L;

    @TableId("id")
    private Long id;

    /**
     * 快递单号
     */
    @TableField("post_no")
    private String postNo;

    /**
     * 礼物ID
     */
    @TableField("gift_id")
    private Integer giftId;

    /**
     * 礼物名称
     */
    @TableField("gift_name")
    private String giftName;

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
     * 收获地址
     */
    @TableField("addr")
    private String addr;

    /**
     * 状态:0-申请中 1-同意 2-拒绝 3-已发货 4-已送达
     */
    @TableField("status")
    private Integer status;

    /**
     * 备注
     */
    @TableField("mark")
    private String mark;

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

    public String getPostNo() {
        return postNo;
    }

    public void setPostNo(String postNo) {
        this.postNo = postNo;
    }

    public Integer getGiftId() {
        return giftId;
    }

    public void setGiftId(Integer giftId) {
        this.giftId = giftId;
    }

    public String getGiftName() {
        return giftName;
    }

    public void setGiftName(String giftName) {
        this.giftName = giftName;
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

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
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
        return "GiftRecords{" +
        "id=" + id +
        ", postNo=" + postNo +
        ", giftId=" + giftId +
        ", giftName=" + giftName +
        ", uid=" + uid +
        ", username=" + username +
        ", addr=" + addr +
        ", status=" + status +
        ", mark=" + mark +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        "}";
    }
}
