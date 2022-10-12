package com.xinbo.sports.dao.generator.po;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

/**
 * <p>
 * futures_lottery注单表
 * </p>
 *
 * @author David
 * @since 2021-02-01
 */
@TableName("sp_betslips_futures_lottery")
public class BetslipsFuturesLottery extends Model<BetslipsFuturesLottery> {

    private static final long serialVersionUID=1L;

    /**
     * ID
     */
    @TableId("id")
    private Long id;

    /**
     * 平台ID
     */
    @TableField("plat_id")
    private Integer platId;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 彩种ID
     */
    @TableField("lottery_id")
    private Integer lotteryId;

    /**
     * 玩法ID
     */
    @TableField("played_id")
    private Integer playedId;

    /**
     * 赔率
     */
    @TableField("odds")
    private BigDecimal odds;

    /**
     * 赔率(补充)
     */
    @TableField("odds_ext")
    private BigDecimal oddsExt;

    /**
     * 投注期号
     */
    @TableField("action_no")
    private Long actionNo;

    /**
     * 序列串号(同组投注)
     */
    @TableField("serial_id")
    private Long serialId;

    /**
     * 投注号码
     */
    @TableField("bet")
    private String bet;

    /**
     * 投注金额
     */
    @TableField("coin")
    private BigDecimal coin;

    /**
     * 即时金额
     */
    @TableField("coin_before")
    private BigDecimal coinBefore;

    /**
     * 开奖号码
     */
    @TableField("open_data")
    private String openData;

    /**
     * 开奖号码补充信息
     */
    @TableField("open_data_info")
    private String openDataInfo;

    /**
     * 实际赔率
     */
    @TableField("open_odds")
    private BigDecimal openOdds;

    /**
     * 开奖状态:WAIT-等待开奖 WIN-赢 LOST-输 TIE-和 CANCEL-撤单
     */
    @TableField("status")
    private String status;

    /**
     * 中奖金额
     */
    @TableField("open_bonus")
    private BigDecimal openBonus;

    /**
     * 输赢金额
     */
    @TableField("win_lose")
    private BigDecimal winLose;

    /**
     * fl下注时间
     */
    @TableField("fl_created_at")
    private Integer flCreatedAt;

    /**
     * fl更新时间
     */
    @TableField("fl_updated_at")
    private Integer flUpdatedAt;

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

    /**
     * 手续费
     */
    @TableField("coin_fee")
    private BigDecimal coinFee;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getPlatId() {
        return platId;
    }

    public void setPlatId(Integer platId) {
        this.platId = platId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getLotteryId() {
        return lotteryId;
    }

    public void setLotteryId(Integer lotteryId) {
        this.lotteryId = lotteryId;
    }

    public Integer getPlayedId() {
        return playedId;
    }

    public void setPlayedId(Integer playedId) {
        this.playedId = playedId;
    }

    public BigDecimal getOdds() {
        return odds;
    }

    public void setOdds(BigDecimal odds) {
        this.odds = odds;
    }

    public BigDecimal getOddsExt() {
        return oddsExt;
    }

    public void setOddsExt(BigDecimal oddsExt) {
        this.oddsExt = oddsExt;
    }

    public Long getActionNo() {
        return actionNo;
    }

    public void setActionNo(Long actionNo) {
        this.actionNo = actionNo;
    }

    public Long getSerialId() {
        return serialId;
    }

    public void setSerialId(Long serialId) {
        this.serialId = serialId;
    }

    public String getBet() {
        return bet;
    }

    public void setBet(String bet) {
        this.bet = bet;
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

    public String getOpenData() {
        return openData;
    }

    public void setOpenData(String openData) {
        this.openData = openData;
    }

    public String getOpenDataInfo() {
        return openDataInfo;
    }

    public void setOpenDataInfo(String openDataInfo) {
        this.openDataInfo = openDataInfo;
    }

    public BigDecimal getOpenOdds() {
        return openOdds;
    }

    public void setOpenOdds(BigDecimal openOdds) {
        this.openOdds = openOdds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getOpenBonus() {
        return openBonus;
    }

    public void setOpenBonus(BigDecimal openBonus) {
        this.openBonus = openBonus;
    }

    public BigDecimal getWinLose() {
        return winLose;
    }

    public void setWinLose(BigDecimal winLose) {
        this.winLose = winLose;
    }

    public Integer getFlCreatedAt() {
        return flCreatedAt;
    }

    public void setFlCreatedAt(Integer flCreatedAt) {
        this.flCreatedAt = flCreatedAt;
    }

    public Integer getFlUpdatedAt() {
        return flUpdatedAt;
    }

    public void setFlUpdatedAt(Integer flUpdatedAt) {
        this.flUpdatedAt = flUpdatedAt;
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

    public BigDecimal getCoinFee() {
        return coinFee;
    }

    public void setCoinFee(BigDecimal coinFee) {
        this.coinFee = coinFee;
    }

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    @Override
    public String toString() {
        return "BetslipsFuturesLottery{" +
        "id=" + id +
        ", platId=" + platId +
        ", username=" + username +
        ", lotteryId=" + lotteryId +
        ", playedId=" + playedId +
        ", odds=" + odds +
        ", oddsExt=" + oddsExt +
        ", actionNo=" + actionNo +
        ", serialId=" + serialId +
        ", bet=" + bet +
        ", coin=" + coin +
        ", coinBefore=" + coinBefore +
        ", openData=" + openData +
        ", openDataInfo=" + openDataInfo +
        ", openOdds=" + openOdds +
        ", status=" + status +
        ", openBonus=" + openBonus +
        ", winLose=" + winLose +
        ", flCreatedAt=" + flCreatedAt +
        ", flUpdatedAt=" + flUpdatedAt +
        ", xbUid=" + xbUid +
        ", xbUsername=" + xbUsername +
        ", xbCoin=" + xbCoin +
        ", xbValidCoin=" + xbValidCoin +
        ", xbProfit=" + xbProfit +
        ", xbStatus=" + xbStatus +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        ", coinFee=" + coinFee +
        "}";
    }
}
