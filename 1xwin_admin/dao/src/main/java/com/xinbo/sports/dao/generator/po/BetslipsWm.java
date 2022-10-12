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
 * wm视讯注单表
 * </p>
 *
 * @author David
 * @since 2021-02-01
 */
@TableName("sp_betslips_wm")
public class BetslipsWm extends Model<BetslipsWm> {

    private static final long serialVersionUID=1L;

    /**
     * ID -> bet_id
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
     * 会员账号
     */
    @TableField("user")
    private String user;

    /**
     * 下注時間
     */
    @TableField("bet_time")
    private Date betTime;

    /**
     * 下注前金额
     */
    @TableField("before_cash")
    private BigDecimal beforeCash;

    /**
     * 下注金额
     */
    @TableField("bet")
    private BigDecimal bet;

    /**
     * 有效下注
     */
    @TableField("valid_bet")
    private BigDecimal validBet;

    /**
     * 退水金额
     */
    @TableField("water")
    private BigDecimal water;

    /**
     * 下注结果
     */
    @TableField("result")
    private BigDecimal result;

    /**
     * 下注代碼Code的部分
     */
    @TableField("bet_code")
    private String betCode;

    /**
     * 下注内容
     */
    @TableField("bet_result")
    private String betResult;

    /**
     * 下注退水金额
     */
    @TableField("water_bet")
    private String waterBet;

    /**
     * 输赢金额
     */
    @TableField("win_loss")
    private BigDecimal winLoss;

    /**
     * IP
     */
    @TableField("ip")
    private String ip;

    /**
     * 游戏类别: 101-百家乐 102-龙虎 103-轮盘 104-骰宝 105-牛牛 106-三公 107-番摊 108-色碟 110-鱼虾蟹 111-炸金花 112-温州牌九 113-二八杠
     */
    @TableField("gid")
    private Integer gid;

    /**
     * 场次编号
     */
    @TableField("event")
    private Long event;

    /**
     * 子场次编号
     */
    @TableField("event_child")
    private Integer eventChild;

    /**
     * 场次编号
     */
    @TableField("round")
    private Long round;

    /**
     * 子场次编号
     */
    @TableField("sub_round")
    private Integer subRound;

    /**
     * 桌台编号
     */
    @TableField("table_id")
    private Integer tableId;

    /**
     * 牌型ex:庄:♦3♦3 闲:♥9♣10
     */
    @TableField("game_result")
    private String gameResult;

    /**
     * 游戏名称ex:百家乐
     */
    @TableField("gname")
    private String gname;

    /**
     * 0-一般 1-免佣
     */
    @TableField("commission")
    private Integer commission;

    /**
     * Y-有重对 N-非重对
     */
    @TableField("reset")
    private String reset;

    /**
     * 结算时间
     */
    @TableField("set_time")
    private Date setTime;

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

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Date getBetTime() {
        return betTime;
    }

    public void setBetTime(Date betTime) {
        this.betTime = betTime;
    }

    public BigDecimal getBeforeCash() {
        return beforeCash;
    }

    public void setBeforeCash(BigDecimal beforeCash) {
        this.beforeCash = beforeCash;
    }

    public BigDecimal getBet() {
        return bet;
    }

    public void setBet(BigDecimal bet) {
        this.bet = bet;
    }

    public BigDecimal getValidBet() {
        return validBet;
    }

    public void setValidBet(BigDecimal validBet) {
        this.validBet = validBet;
    }

    public BigDecimal getWater() {
        return water;
    }

    public void setWater(BigDecimal water) {
        this.water = water;
    }

    public BigDecimal getResult() {
        return result;
    }

    public void setResult(BigDecimal result) {
        this.result = result;
    }

    public String getBetCode() {
        return betCode;
    }

    public void setBetCode(String betCode) {
        this.betCode = betCode;
    }

    public String getBetResult() {
        return betResult;
    }

    public void setBetResult(String betResult) {
        this.betResult = betResult;
    }

    public String getWaterBet() {
        return waterBet;
    }

    public void setWaterBet(String waterBet) {
        this.waterBet = waterBet;
    }

    public BigDecimal getWinLoss() {
        return winLoss;
    }

    public void setWinLoss(BigDecimal winLoss) {
        this.winLoss = winLoss;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getGid() {
        return gid;
    }

    public void setGid(Integer gid) {
        this.gid = gid;
    }

    public Long getEvent() {
        return event;
    }

    public void setEvent(Long event) {
        this.event = event;
    }

    public Integer getEventChild() {
        return eventChild;
    }

    public void setEventChild(Integer eventChild) {
        this.eventChild = eventChild;
    }

    public Long getRound() {
        return round;
    }

    public void setRound(Long round) {
        this.round = round;
    }

    public Integer getSubRound() {
        return subRound;
    }

    public void setSubRound(Integer subRound) {
        this.subRound = subRound;
    }

    public Integer getTableId() {
        return tableId;
    }

    public void setTableId(Integer tableId) {
        this.tableId = tableId;
    }

    public String getGameResult() {
        return gameResult;
    }

    public void setGameResult(String gameResult) {
        this.gameResult = gameResult;
    }

    public String getGname() {
        return gname;
    }

    public void setGname(String gname) {
        this.gname = gname;
    }

    public Integer getCommission() {
        return commission;
    }

    public void setCommission(Integer commission) {
        this.commission = commission;
    }

    public String getReset() {
        return reset;
    }

    public void setReset(String reset) {
        this.reset = reset;
    }

    public Date getSetTime() {
        return setTime;
    }

    public void setSetTime(Date setTime) {
        this.setTime = setTime;
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
        return "BetslipsWm{" +
        "id=" + id +
        ", xbUid=" + xbUid +
        ", xbUsername=" + xbUsername +
        ", user=" + user +
        ", betTime=" + betTime +
        ", beforeCash=" + beforeCash +
        ", bet=" + bet +
        ", validBet=" + validBet +
        ", water=" + water +
        ", result=" + result +
        ", betCode=" + betCode +
        ", betResult=" + betResult +
        ", waterBet=" + waterBet +
        ", winLoss=" + winLoss +
        ", ip=" + ip +
        ", gid=" + gid +
        ", event=" + event +
        ", eventChild=" + eventChild +
        ", round=" + round +
        ", subRound=" + subRound +
        ", tableId=" + tableId +
        ", gameResult=" + gameResult +
        ", gname=" + gname +
        ", commission=" + commission +
        ", reset=" + reset +
        ", setTime=" + setTime +
        ", xbCoin=" + xbCoin +
        ", xbValidCoin=" + xbValidCoin +
        ", xbProfit=" + xbProfit +
        ", xbStatus=" + xbStatus +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        "}";
    }
}
