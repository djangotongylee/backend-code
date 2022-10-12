package com.xinbo.sports.dao.generator.po;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

/**
 * <p>
 * 哈巴注单表
 * </p>
 *
 * @author David
 * @since 2021-02-01
 */
@TableName("sp_betslips_hb")
public class BetslipsHb extends Model<BetslipsHb> {

    private static final long serialVersionUID=1L;

    /**
     * 注单号 对应三方拉单GameInstanceId
     */
    @TableId("id")
    private String id;

    /**
     * 对应user表id
     */
    @TableField("xb_uid")
    private Integer xbUid;

    /**
     * 对应user表username
     */
    @TableField("xb_username")
    private String xbUsername;

    /**
     * 玩家ID
     */
    @TableField("player_id")
    private String playerId;

    /**
     * 品牌ID
     */
    @TableField("brand_id")
    private String brandId;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 品牌游戏ID
     */
    @TableField("brand_game_id")
    private String brandGameId;

    /**
     * 游戏名称
     */
    @TableField("game_key_name")
    private String gameKeyName;

    /**
     * 游戏类型ID
     */
    @TableField("game_type_id")
    private Integer gameTypeId;

    /**
     * 游戏开始时间
     */
    @TableField("dt_started")
    private Date dtStarted;

    /**
     * 游戏结束时间
     */
    @TableField("dt_completed")
    private Date dtCompleted;

    /**
     * 游戏编号ID
     */
    @TableField("friendly_game_instance_id")
    private Long friendlyGameInstanceId;

    /**
     * 游戏编号名称
     */
    @TableField("game_instance_id")
    private String gameInstanceId;

    /**
     * 投注
     */
    @TableField("stake")
    private BigDecimal stake;

    /**
     * 派彩
     */
    @TableField("payout")
    private BigDecimal payout;

    /**
     * 奖池奖金
     */
    @TableField("jackpot_win")
    private BigDecimal jackpotWin;

    /**
     * 投注后的余额
     */
    @TableField("balance_after")
    private BigDecimal balanceAfter;

    /**
     * 投注金额
     */
    @TableField("xb_coin")
    private BigDecimal xbCoin;

    /**
     * 有效投注额
     */
    @TableField("xb_valid_coin")
    private BigDecimal xbValidCoin;

    /**
     * 盈亏金额
     */
    @TableField("xb_profit")
    private BigDecimal xbProfit;

    /**
     * 注单状态
     */
    @TableField("xb_status")
    private Integer xbStatus;

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

    public Integer getXbUid() {
        return xbUid;
    }

    public void setXbUid(Integer xbUid) {
        this.xbUid = xbUid;
    }

    public String getXbUsername() {
        return xbUsername;
    }

    public void setXbUsername(String xbUsername) {
        this.xbUsername = xbUsername;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getBrandId() {
        return brandId;
    }

    public void setBrandId(String brandId) {
        this.brandId = brandId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBrandGameId() {
        return brandGameId;
    }

    public void setBrandGameId(String brandGameId) {
        this.brandGameId = brandGameId;
    }

    public String getGameKeyName() {
        return gameKeyName;
    }

    public void setGameKeyName(String gameKeyName) {
        this.gameKeyName = gameKeyName;
    }

    public Integer getGameTypeId() {
        return gameTypeId;
    }

    public void setGameTypeId(Integer gameTypeId) {
        this.gameTypeId = gameTypeId;
    }

    public Date getDtStarted() {
        return dtStarted;
    }

    public void setDtStarted(Date dtStarted) {
        this.dtStarted = dtStarted;
    }

    public Date getDtCompleted() {
        return dtCompleted;
    }

    public void setDtCompleted(Date dtCompleted) {
        this.dtCompleted = dtCompleted;
    }

    public Long getFriendlyGameInstanceId() {
        return friendlyGameInstanceId;
    }

    public void setFriendlyGameInstanceId(Long friendlyGameInstanceId) {
        this.friendlyGameInstanceId = friendlyGameInstanceId;
    }

    public String getGameInstanceId() {
        return gameInstanceId;
    }

    public void setGameInstanceId(String gameInstanceId) {
        this.gameInstanceId = gameInstanceId;
    }

    public BigDecimal getStake() {
        return stake;
    }

    public void setStake(BigDecimal stake) {
        this.stake = stake;
    }

    public BigDecimal getPayout() {
        return payout;
    }

    public void setPayout(BigDecimal payout) {
        this.payout = payout;
    }

    public BigDecimal getJackpotWin() {
        return jackpotWin;
    }

    public void setJackpotWin(BigDecimal jackpotWin) {
        this.jackpotWin = jackpotWin;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public BigDecimal getXbCoin() {
        return xbCoin;
    }

    public void setXbCoin(BigDecimal xbCoin) {
        this.xbCoin = xbCoin;
    }

    public BigDecimal getXbValidCoin() {
        return xbValidCoin;
    }

    public void setXbValidCoin(BigDecimal xbValidCoin) {
        this.xbValidCoin = xbValidCoin;
    }

    public BigDecimal getXbProfit() {
        return xbProfit;
    }

    public void setXbProfit(BigDecimal xbProfit) {
        this.xbProfit = xbProfit;
    }

    public Integer getXbStatus() {
        return xbStatus;
    }

    public void setXbStatus(Integer xbStatus) {
        this.xbStatus = xbStatus;
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
        return "BetslipsHb{" +
        "id=" + id +
        ", xbUid=" + xbUid +
        ", xbUsername=" + xbUsername +
        ", playerId=" + playerId +
        ", brandId=" + brandId +
        ", username=" + username +
        ", brandGameId=" + brandGameId +
        ", gameKeyName=" + gameKeyName +
        ", gameTypeId=" + gameTypeId +
        ", dtStarted=" + dtStarted +
        ", dtCompleted=" + dtCompleted +
        ", friendlyGameInstanceId=" + friendlyGameInstanceId +
        ", gameInstanceId=" + gameInstanceId +
        ", stake=" + stake +
        ", payout=" + payout +
        ", jackpotWin=" + jackpotWin +
        ", balanceAfter=" + balanceAfter +
        ", xbCoin=" + xbCoin +
        ", xbValidCoin=" + xbValidCoin +
        ", xbProfit=" + xbProfit +
        ", xbStatus=" + xbStatus +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        "}";
    }
}
