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
 * AG注单表
 * </p>
 *
 * @author David
 * @since 2021-02-01
 */
@TableName("sp_betslips_ag")
public class BetslipsAg extends Model<BetslipsAg> {

    private static final long serialVersionUID=1L;

    /**
     * ID -> billNo订单号
     */
    @TableId("id")
    private Long id;

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
     * 用户名
     */
    @TableField("play_name")
    private String playName;

    /**
     * 局号
     */
    @TableField("game_code")
    private String gameCode;

    /**
     * 派彩额度
     */
    @TableField("net_amount")
    private BigDecimal netAmount;

    /**
     * 下注时间
     */
    @TableField("bet_time")
    private Date betTime;

    /**
     * 游戏类型
     */
    @TableField("game_type")
    private String gameType;

    /**
     * 投注额度
     */
    @TableField("bet_amount")
    private BigDecimal betAmount;

    /**
     * 有效投注额度
     */
    @TableField("valid_bet_amount")
    private BigDecimal validBetAmount;

    /**
     * 订单状态:0异常(请联系客服) 1已派彩 -8取消指定局注单 -9取消指定注单
     */
    @TableField("flag")
    private Integer flag;

    /**
     * 玩法类型
     */
    @TableField("play_type")
    private Integer playType;

    /**
     * 投注币种
     */
    @TableField("currency")
    private String currency;

    /**
     * 桌台号 (此處為虛擬桌號，非實際桌號。)
     */
    @TableField("table_code")
    private String tableCode;

    /**
     * 派彩时间
     */
    @TableField("recalcu_time")
    private Date recalcuTime;

    /**
     * 余额
     */
    @TableField("before_credit")
    private BigDecimal beforeCredit;

    /**
     * 投注IP
     */
    @TableField("bet_ip")
    private String betIp;

    /**
     * 平台类型为AGIN
     */
    @TableField("platform_type")
    private String platformType;

    /**
     * 注示
     */
    @TableField("remark")
    private String remark;

    /**
     * 廳別代碼
     */
    @TableField("round")
    private String round;

    /**
     * 遊戲結果
     */
    @TableField("result")
    private String result;

    /**
     * 開牌結果
     */
    @TableField("rounders")
    private String rounders;

    /**
     * 0-PC 大于等于1-手机
     */
    @TableField("device_type")
    private Integer deviceType;

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


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public String getPlayName() {
        return playName;
    }

    public void setPlayName(String playName) {
        this.playName = playName;
    }

    public String getGameCode() {
        return gameCode;
    }

    public void setGameCode(String gameCode) {
        this.gameCode = gameCode;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    public Date getBetTime() {
        return betTime;
    }

    public void setBetTime(Date betTime) {
        this.betTime = betTime;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    public BigDecimal getBetAmount() {
        return betAmount;
    }

    public void setBetAmount(BigDecimal betAmount) {
        this.betAmount = betAmount;
    }

    public BigDecimal getValidBetAmount() {
        return validBetAmount;
    }

    public void setValidBetAmount(BigDecimal validBetAmount) {
        this.validBetAmount = validBetAmount;
    }

    public Integer getFlag() {
        return flag;
    }

    public void setFlag(Integer flag) {
        this.flag = flag;
    }

    public Integer getPlayType() {
        return playType;
    }

    public void setPlayType(Integer playType) {
        this.playType = playType;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getTableCode() {
        return tableCode;
    }

    public void setTableCode(String tableCode) {
        this.tableCode = tableCode;
    }

    public Date getRecalcuTime() {
        return recalcuTime;
    }

    public void setRecalcuTime(Date recalcuTime) {
        this.recalcuTime = recalcuTime;
    }

    public BigDecimal getBeforeCredit() {
        return beforeCredit;
    }

    public void setBeforeCredit(BigDecimal beforeCredit) {
        this.beforeCredit = beforeCredit;
    }

    public String getBetIp() {
        return betIp;
    }

    public void setBetIp(String betIp) {
        this.betIp = betIp;
    }

    public String getPlatformType() {
        return platformType;
    }

    public void setPlatformType(String platformType) {
        this.platformType = platformType;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getRound() {
        return round;
    }

    public void setRound(String round) {
        this.round = round;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getRounders() {
        return rounders;
    }

    public void setRounders(String rounders) {
        this.rounders = rounders;
    }

    public Integer getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(Integer deviceType) {
        this.deviceType = deviceType;
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
        return "BetslipsAg{" +
        "id=" + id +
        ", xbUid=" + xbUid +
        ", xbUsername=" + xbUsername +
        ", playName=" + playName +
        ", gameCode=" + gameCode +
        ", netAmount=" + netAmount +
        ", betTime=" + betTime +
        ", gameType=" + gameType +
        ", betAmount=" + betAmount +
        ", validBetAmount=" + validBetAmount +
        ", flag=" + flag +
        ", playType=" + playType +
        ", currency=" + currency +
        ", tableCode=" + tableCode +
        ", recalcuTime=" + recalcuTime +
        ", beforeCredit=" + beforeCredit +
        ", betIp=" + betIp +
        ", platformType=" + platformType +
        ", remark=" + remark +
        ", round=" + round +
        ", result=" + result +
        ", rounders=" + rounders +
        ", deviceType=" + deviceType +
        ", xbCoin=" + xbCoin +
        ", xbValidCoin=" + xbValidCoin +
        ", xbProfit=" + xbProfit +
        ", xbStatus=" + xbStatus +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        "}";
    }
}
