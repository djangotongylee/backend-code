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
 * S128斗鸡注单表
 * </p>
 *
 * @author David
 * @since 2021-02-01
 */
@TableName("sp_betslips_s128")
public class BetslipsS128 extends Model<BetslipsS128> {

    private static final long serialVersionUID=1L;

    /**
     * id -> ticket_id
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
     * 登录帐号
     */
    @TableField("login_id")
    private String loginId;

    /**
     * 赛场编号
     */
    @TableField("arena_code")
    private String arenaCode;

    /**
     * 赛场名中文名字
     */
    @TableField("arena_name_cn")
    private String arenaNameCn;

    /**
     * 赛事编号
     */
    @TableField("match_no")
    private String matchNo;

    /**
     * 赛事类型
     */
    @TableField("match_type")
    private String matchType;

    /**
     * 赛事日期
     */
    @TableField("match_date")
    private Date matchDate;

    /**
     * 日场次
     */
    @TableField("fight_no")
    private Integer fightNo;

    /**
     * 赛事时间
     */
    @TableField("fight_datetime")
    private Date fightDatetime;

    /**
     * 龍斗鸡
     */
    @TableField("meron_cock")
    private String meronCock;

    /**
     * 龍斗鸡中文名字
     */
    @TableField("meron_cock_cn")
    private String meronCockCn;

    /**
     * 鳳斗鸡
     */
    @TableField("wala_cock")
    private String walaCock;

    /**
     * 鳳斗鸡中文名字
     */
    @TableField("wala_cock_cn")
    private String walaCockCn;

    /**
     * 投注-MERON/WALA/BDD/FTD
     */
    @TableField("bet_on")
    private String betOn;

    /**
     * 赔率类型
     */
    @TableField("odds_type")
    private String oddsType;

    /**
     * 要求赔率
     */
    @TableField("odds_asked")
    private BigDecimal oddsAsked;

    /**
     *  给出赔率
     */
    @TableField("odds_given")
    private BigDecimal oddsGiven;

    /**
     * 投注金额
     */
    @TableField("stake")
    private Integer stake;

    /**
     * 奖金
     */
    @TableField("stake_money")
    private BigDecimal stakeMoney;

    /**
     * 转账前余额
     */
    @TableField("balance_open")
    private BigDecimal balanceOpen;

    /**
     * 转账后余额
     */
    @TableField("balance_close")
    private BigDecimal balanceClose;

    /**
     * 创建时间
     */
    @TableField("created_datetime")
    private Date createdDatetime;

    /**
     * 赛事结果-MERON/WALA/BDD/FTD
     */
    @TableField("fight_result")
    private String fightResult;

    /**
     * 状态-WIN/LOSE/REFUND/CANCEL/VOID
     */
    @TableField("status")
    private String status;

    /**
     * 输赢
     */
    @TableField("winloss")
    private BigDecimal winloss;

    /**
     * 所得佣金
     */
    @TableField("comm_earned")
    private BigDecimal commEarned;

    /**
     * 派彩
     */
    @TableField("payout")
    private BigDecimal payout;

    /**
     * 转账前余额
     */
    @TableField("balance_open1")
    private BigDecimal balanceOpen1;

    /**
     * 转账后余额
     */
    @TableField("balance_close1")
    private BigDecimal balanceClose1;

    /**
     * 处理时间
     */
    @TableField("processed_datetime")
    private Date processedDatetime;

    /**
     * 投注金额
     */
    @TableField("xb_coin")
    private BigDecimal xbCoin;

    /**
     * 盈亏金额
     */
    @TableField("xb_profit")
    private BigDecimal xbProfit;

    /**
     * 有效投注额
     */
    @TableField("xb_valid_coin")
    private BigDecimal xbValidCoin;

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

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getArenaCode() {
        return arenaCode;
    }

    public void setArenaCode(String arenaCode) {
        this.arenaCode = arenaCode;
    }

    public String getArenaNameCn() {
        return arenaNameCn;
    }

    public void setArenaNameCn(String arenaNameCn) {
        this.arenaNameCn = arenaNameCn;
    }

    public String getMatchNo() {
        return matchNo;
    }

    public void setMatchNo(String matchNo) {
        this.matchNo = matchNo;
    }

    public String getMatchType() {
        return matchType;
    }

    public void setMatchType(String matchType) {
        this.matchType = matchType;
    }

    public Date getMatchDate() {
        return matchDate;
    }

    public void setMatchDate(Date matchDate) {
        this.matchDate = matchDate;
    }

    public Integer getFightNo() {
        return fightNo;
    }

    public void setFightNo(Integer fightNo) {
        this.fightNo = fightNo;
    }

    public Date getFightDatetime() {
        return fightDatetime;
    }

    public void setFightDatetime(Date fightDatetime) {
        this.fightDatetime = fightDatetime;
    }

    public String getMeronCock() {
        return meronCock;
    }

    public void setMeronCock(String meronCock) {
        this.meronCock = meronCock;
    }

    public String getMeronCockCn() {
        return meronCockCn;
    }

    public void setMeronCockCn(String meronCockCn) {
        this.meronCockCn = meronCockCn;
    }

    public String getWalaCock() {
        return walaCock;
    }

    public void setWalaCock(String walaCock) {
        this.walaCock = walaCock;
    }

    public String getWalaCockCn() {
        return walaCockCn;
    }

    public void setWalaCockCn(String walaCockCn) {
        this.walaCockCn = walaCockCn;
    }

    public String getBetOn() {
        return betOn;
    }

    public void setBetOn(String betOn) {
        this.betOn = betOn;
    }

    public String getOddsType() {
        return oddsType;
    }

    public void setOddsType(String oddsType) {
        this.oddsType = oddsType;
    }

    public BigDecimal getOddsAsked() {
        return oddsAsked;
    }

    public void setOddsAsked(BigDecimal oddsAsked) {
        this.oddsAsked = oddsAsked;
    }

    public BigDecimal getOddsGiven() {
        return oddsGiven;
    }

    public void setOddsGiven(BigDecimal oddsGiven) {
        this.oddsGiven = oddsGiven;
    }

    public Integer getStake() {
        return stake;
    }

    public void setStake(Integer stake) {
        this.stake = stake;
    }

    public BigDecimal getStakeMoney() {
        return stakeMoney;
    }

    public void setStakeMoney(BigDecimal stakeMoney) {
        this.stakeMoney = stakeMoney;
    }

    public BigDecimal getBalanceOpen() {
        return balanceOpen;
    }

    public void setBalanceOpen(BigDecimal balanceOpen) {
        this.balanceOpen = balanceOpen;
    }

    public BigDecimal getBalanceClose() {
        return balanceClose;
    }

    public void setBalanceClose(BigDecimal balanceClose) {
        this.balanceClose = balanceClose;
    }

    public Date getCreatedDatetime() {
        return createdDatetime;
    }

    public void setCreatedDatetime(Date createdDatetime) {
        this.createdDatetime = createdDatetime;
    }

    public String getFightResult() {
        return fightResult;
    }

    public void setFightResult(String fightResult) {
        this.fightResult = fightResult;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getWinloss() {
        return winloss;
    }

    public void setWinloss(BigDecimal winloss) {
        this.winloss = winloss;
    }

    public BigDecimal getCommEarned() {
        return commEarned;
    }

    public void setCommEarned(BigDecimal commEarned) {
        this.commEarned = commEarned;
    }

    public BigDecimal getPayout() {
        return payout;
    }

    public void setPayout(BigDecimal payout) {
        this.payout = payout;
    }

    public BigDecimal getBalanceOpen1() {
        return balanceOpen1;
    }

    public void setBalanceOpen1(BigDecimal balanceOpen1) {
        this.balanceOpen1 = balanceOpen1;
    }

    public BigDecimal getBalanceClose1() {
        return balanceClose1;
    }

    public void setBalanceClose1(BigDecimal balanceClose1) {
        this.balanceClose1 = balanceClose1;
    }

    public Date getProcessedDatetime() {
        return processedDatetime;
    }

    public void setProcessedDatetime(Date processedDatetime) {
        this.processedDatetime = processedDatetime;
    }

    public BigDecimal getXbCoin() {
        return xbCoin;
    }

    public void setXbCoin(BigDecimal xbCoin) {
        this.xbCoin = xbCoin;
    }

    public BigDecimal getXbProfit() {
        return xbProfit;
    }

    public void setXbProfit(BigDecimal xbProfit) {
        this.xbProfit = xbProfit;
    }

    public BigDecimal getXbValidCoin() {
        return xbValidCoin;
    }

    public void setXbValidCoin(BigDecimal xbValidCoin) {
        this.xbValidCoin = xbValidCoin;
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
        return "BetslipsS128{" +
        "id=" + id +
        ", xbUid=" + xbUid +
        ", xbUsername=" + xbUsername +
        ", loginId=" + loginId +
        ", arenaCode=" + arenaCode +
        ", arenaNameCn=" + arenaNameCn +
        ", matchNo=" + matchNo +
        ", matchType=" + matchType +
        ", matchDate=" + matchDate +
        ", fightNo=" + fightNo +
        ", fightDatetime=" + fightDatetime +
        ", meronCock=" + meronCock +
        ", meronCockCn=" + meronCockCn +
        ", walaCock=" + walaCock +
        ", walaCockCn=" + walaCockCn +
        ", betOn=" + betOn +
        ", oddsType=" + oddsType +
        ", oddsAsked=" + oddsAsked +
        ", oddsGiven=" + oddsGiven +
        ", stake=" + stake +
        ", stakeMoney=" + stakeMoney +
        ", balanceOpen=" + balanceOpen +
        ", balanceClose=" + balanceClose +
        ", createdDatetime=" + createdDatetime +
        ", fightResult=" + fightResult +
        ", status=" + status +
        ", winloss=" + winloss +
        ", commEarned=" + commEarned +
        ", payout=" + payout +
        ", balanceOpen1=" + balanceOpen1 +
        ", balanceClose1=" + balanceClose1 +
        ", processedDatetime=" + processedDatetime +
        ", xbCoin=" + xbCoin +
        ", xbProfit=" + xbProfit +
        ", xbValidCoin=" + xbValidCoin +
        ", xbStatus=" + xbStatus +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        "}";
    }
}
