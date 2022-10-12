package com.xinbo.sports.dao.generator.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

/**
 * <p>
 * HB哈巴游戏
 * </p>
 *
 * @author David
 * @since 2021-02-01
 */
@TableName("sp_game_slot")
public class GameSlot extends Model<GameSlot> {

    private static final long serialVersionUID=1L;

    /**
     * ID(关联BrandGameId)
     */
    @TableId("id")
    private String id;

    /**
     * 游戏ID(关联game_list)
     */
    @TableField("game_id")
    private Integer gameId;

    /**
     * 简体名称
     */
    @TableField("name")
    private String name;

    /**
     * 游戏名字(中文)
     */
    @TableField("name_zh")
    private String nameZh;

    /**
     * 英文图片
     */
    @TableField("img")
    private String img;

    /**
     * 是否新游戏:1-是 0-否
     */
    @TableField("is_new")
    private Integer isNew;

    /**
     * 游戏类型ID(0r code)
     */
    @TableField("game_type_id")
    private String gameTypeId;

    /**
     * 游戏类型名称
     */
    @TableField("game_type_name")
    private String gameTypeName;

    /**
     * 收藏值
     */
    @TableField("favorite_star")
    private Integer favoriteStar;

    /**
     * 热度
     */
    @TableField("hot_star")
    private Integer hotStar;

    /**
     * 排序
     */
    @TableField("sort")
    private Integer sort;

    /**
     * 状态:1-启用 0-停用
     */
    @TableField("status")
    private Integer status;

    /**
     * 设备:0-all 1-pc 2-h5
     */
    @TableField("device")
    private Integer device;

    @TableField("created_at")
    private Integer createdAt;

    @TableField("updated_at")
    private Integer updatedAt;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getGameId() {
        return gameId;
    }

    public void setGameId(Integer gameId) {
        this.gameId = gameId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameZh() {
        return nameZh;
    }

    public void setNameZh(String nameZh) {
        this.nameZh = nameZh;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public Integer getIsNew() {
        return isNew;
    }

    public void setIsNew(Integer isNew) {
        this.isNew = isNew;
    }

    public String getGameTypeId() {
        return gameTypeId;
    }

    public void setGameTypeId(String gameTypeId) {
        this.gameTypeId = gameTypeId;
    }

    public String getGameTypeName() {
        return gameTypeName;
    }

    public void setGameTypeName(String gameTypeName) {
        this.gameTypeName = gameTypeName;
    }

    public Integer getFavoriteStar() {
        return favoriteStar;
    }

    public void setFavoriteStar(Integer favoriteStar) {
        this.favoriteStar = favoriteStar;
    }

    public Integer getHotStar() {
        return hotStar;
    }

    public void setHotStar(Integer hotStar) {
        this.hotStar = hotStar;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getDevice() {
        return device;
    }

    public void setDevice(Integer device) {
        this.device = device;
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
        return "GameSlot{" +
        "id=" + id +
        ", gameId=" + gameId +
        ", name=" + name +
        ", nameZh=" + nameZh +
        ", img=" + img +
        ", isNew=" + isNew +
        ", gameTypeId=" + gameTypeId +
        ", gameTypeName=" + gameTypeName +
        ", favoriteStar=" + favoriteStar +
        ", hotStar=" + hotStar +
        ", sort=" + sort +
        ", status=" + status +
        ", device=" + device +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        "}";
    }
}
