package com.xinbo.sports.dao.generator.po;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

/**
 * <p>
 * 返水
 * </p>
 *
 * @author David
 * @since 2021-02-01
 */
@TableName("sp_coin_rebate")
public class CoinRebate extends Model<CoinRebate> {

    private static final long serialVersionUID=1L;

    @TableId("id")
    private Long id;

    /**
     * 标题
     */
    @TableField("title")
    private String title;

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
     * 金额
     */
    @TableField("coin")
    private BigDecimal coin;

    /**
     * 即时金额
     */
    @TableField("coin_before")
    private BigDecimal coinBefore;

    /**
     * 有效投注额
     */
    @TableField("coin_bet_valid")
    private BigDecimal coinBetValid;

    /**
     * 返水比例
     */
    @TableField("rate")
    private BigDecimal rate;

    /**
     * 游戏ID
     */
    @TableField("game_list_id")
    private Integer gameListId;

    /**
     * 游戏平台ID
     */
    @TableField("plat_id")
    private Integer platId;

    /**
     * 状态:1-正常 0-撤销
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public BigDecimal getCoinBefore() {
        return coinBefore;
    }

    public void setCoinBefore(BigDecimal coinBefore) {
        this.coinBefore = coinBefore;
    }

    public BigDecimal getCoinBetValid() {
        return coinBetValid;
    }

    public void setCoinBetValid(BigDecimal coinBetValid) {
        this.coinBetValid = coinBetValid;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public Integer getGameListId() {
        return gameListId;
    }

    public void setGameListId(Integer gameListId) {
        this.gameListId = gameListId;
    }

    public Integer getPlatId() {
        return platId;
    }

    public void setPlatId(Integer platId) {
        this.platId = platId;
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
        return "CoinRebate{" +
        "id=" + id +
        ", title=" + title +
        ", uid=" + uid +
        ", username=" + username +
        ", coin=" + coin +
        ", coinBefore=" + coinBefore +
        ", coinBetValid=" + coinBetValid +
        ", rate=" + rate +
        ", gameListId=" + gameListId +
        ", platId=" + platId +
        ", status=" + status +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        "}";
    }
}
