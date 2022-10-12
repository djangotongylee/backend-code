package com.xinbo.sports.dao.generator.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

/**
 * <p>
 * 注单补单记录表
 * </p>
 *
 * @author David
 * @since 2021-02-01
 */
@TableName("sp_bet_slips_supplemental")
public class BetSlipsSupplemental extends Model<BetSlipsSupplemental> {

    private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 游戏表ID
     */
    @TableField("game_list_id")
    private Integer gameListId;

    /**
     * 开始时间
     */
    @TableField("time_start")
    private String timeStart;

    /**
     * 结束时间
     */
    @TableField("time_end")
    private String timeEnd;

    /**
     * 请求参数
     */
    @TableField("request")
    private String request;

    /**
     * 状态:0-无异常 1-拉单异常 2-数据异常
     */
    @TableField("category")
    private Integer category;

    /**
     * 异常信息:三方-返回数据 数据-异常处理
     */
    @TableField("info")
    private String info;

    /**
     * 状态:0-未处理 1-成功 2-失败
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

    public Integer getGameListId() {
        return gameListId;
    }

    public void setGameListId(Integer gameListId) {
        this.gameListId = gameListId;
    }

    public String getTimeStart() {
        return timeStart;
    }

    public void setTimeStart(String timeStart) {
        this.timeStart = timeStart;
    }

    public String getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(String timeEnd) {
        this.timeEnd = timeEnd;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
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
        return "BetSlipsSupplemental{" +
        "id=" + id +
        ", gameListId=" + gameListId +
        ", timeStart=" + timeStart +
        ", timeEnd=" + timeEnd +
        ", request=" + request +
        ", category=" + category +
        ", info=" + info +
        ", status=" + status +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        "}";
    }
}
