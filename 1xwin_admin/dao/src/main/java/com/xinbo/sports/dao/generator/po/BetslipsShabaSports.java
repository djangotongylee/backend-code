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
 * 
 * </p>
 *
 * @author David
 * @since 2021-02-01
 */
@TableName("sp_betslips_shaba_sports")
public class BetslipsShabaSports extends Model<BetslipsShabaSports> {

    private static final long serialVersionUID=1L;

    /**
     * 对应trans_id注单号
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
    @TableField("vendor_member_id")
    private String vendorMemberId;

    /**
     * 厂商 ID。此 ID 为厂商自行定义
     */
    @TableField("operator_id")
    private String operatorId;

    /**
     * 联盟编号
     */
    @TableField("league_id")
    private Integer leagueId;

    /**
     * 赛事编号
     */
    @TableField("match_id")
    private Integer matchId;

    /**
     * 主队编号
     */
    @TableField("home_id")
    private Integer homeId;

    /**
     * 客队编号
     */
    @TableField("away_id")
    private Integer awayId;

    /**
     * 队伍编号
     */
    @TableField("team_id")
    private Integer teamId;

    /**
     * 赛事开球时间
     */
    @TableField("match_datetime")
    private Date matchDatetime;

    /**
     * 体育种类。请参考附件〝体育种类表”
     */
    @TableField("sport_type")
    private Integer sportType;

    /**
     * 下注类型。请参考附件〝下注类型表”
     */
    @TableField("bet_type")
    private Integer betType;

    /**
     * 混合过关注单号码，使用此号码于
     */
    @TableField("parlay_ref_no")
    private Long parlayRefNo;

    /**
     * 注单赔率
     */
    @TableField("odds")
    private BigDecimal odds;

    /**
     * 会员投注金额
     */
    @TableField("stake")
    private BigDecimal stake;

    /**
     * 投注交易时间
     */
    @TableField("transaction_time")
    private Date transactionTime;

    /**
     * 注单状态
     */
    @TableField("ticket_status")
    private String ticketStatus;

    /**
     * 此注输或赢的金额
     */
    @TableField("winlost_amount")
    private BigDecimal winlostAmount;

    /**
     * 下注后的余额
     */
    @TableField("after_amount")
    private BigDecimal afterAmount;

    /**
     * 为此会员设置币别。请参考附件〝币别表”
     */
    @TableField("currency")
    private Integer currency;

    /**
     * 决胜时间
     */
    @TableField("winlost_datetime")
    private Date winlostDatetime;

    /**
     * 赔率类型
     */
    @TableField("odds_type")
    private Integer oddsType;

    /**
     *  当 bettype 为 468 或 469, 此字段则显示
     */
    @TableField("odds_Info")
    private String oddsInfo;

    /**
     * 下注对象
     */
    @TableField("bet_team")
    private String betTeam;

    /**
     * 当 bet_team=aos 时,才返回此字段,返回的值
     */
    @TableField("exculding")
    private String exculding;

    /**
     *  X 与 Y 的值。请参考附件〝下注类型表” 例) bettype 145 - Set X Winner
     */
    @TableField("bet_tag")
    private String betTag;

    /**
     * 主队让球
     */
    @TableField("home_hdp")
    private BigDecimal homeHdp;

    /**
     * 客队让球
     */
    @TableField("away_hdp")
    private BigDecimal awayHdp;

    /**
     * 让球
     */
    @TableField("hdp")
    private BigDecimal hdp;

    /**
     * 下注平台表。请参考附件中〝下注平台表”
     */
    @TableField("betfrom")
    private String betfrom;

    /**
     * 是否在滚球时下注
     */
    @TableField("islive")
    private String islive;

    /**
     * 下注时主队得分.
     */
    @TableField("home_score")
    private Integer homeScore;

    /**
     * 下注时客队得分
     */
    @TableField("away_score")
    private Integer awayScore;

    /**
     * 注单结算的时间
     */
    @TableField("settlement_time")
    private Date settlementTime;

    /**
     * 主队信息
     */
    @TableField("home_team_name")
    private String homeTeamName;

    /**
     * 联赛信息
     */
    @TableField("league_name")
    private String leagueName;

    /**
     * 客队信息
     */
    @TableField("away_team_name")
    private String awayTeamName;

    /**
     * 厂商备注
     */
    @TableField("customInfo1")
    private String customInfo1;

    /**
     * 厂商备注
     */
    @TableField("customInfo2")
    private String customInfo2;

    /**
     * 厂商备注
     */
    @TableField("customInfo3")
    private String customInfo3;

    /**
     * 厂商备注
     */
    @TableField("customInfo4")
    private String customInfo4;

    /**
     * 厂商备注
     */
    @TableField("customInfo5")
    private String customInfo5;

    /**
     * 会员是否为 BA 状态
     */
    @TableField("ba_status")
    private String baStatus;

    /**
     * 版本号
     */
    @TableField("version_key")
    private Long versionKey;

    /**
     * 混合过关信息
     */
    @TableField("parlay_data")
    private String parlayData;

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

    /**
     * 创建时间
     */
    @TableField("created_at")
    private Integer createdAt;

    /**
     * 修改时间
     */
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

    public String getVendorMemberId() {
        return vendorMemberId;
    }

    public void setVendorMemberId(String vendorMemberId) {
        this.vendorMemberId = vendorMemberId;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public Integer getLeagueId() {
        return leagueId;
    }

    public void setLeagueId(Integer leagueId) {
        this.leagueId = leagueId;
    }

    public Integer getMatchId() {
        return matchId;
    }

    public void setMatchId(Integer matchId) {
        this.matchId = matchId;
    }

    public Integer getHomeId() {
        return homeId;
    }

    public void setHomeId(Integer homeId) {
        this.homeId = homeId;
    }

    public Integer getAwayId() {
        return awayId;
    }

    public void setAwayId(Integer awayId) {
        this.awayId = awayId;
    }

    public Integer getTeamId() {
        return teamId;
    }

    public void setTeamId(Integer teamId) {
        this.teamId = teamId;
    }

    public Date getMatchDatetime() {
        return matchDatetime;
    }

    public void setMatchDatetime(Date matchDatetime) {
        this.matchDatetime = matchDatetime;
    }

    public Integer getSportType() {
        return sportType;
    }

    public void setSportType(Integer sportType) {
        this.sportType = sportType;
    }

    public Integer getBetType() {
        return betType;
    }

    public void setBetType(Integer betType) {
        this.betType = betType;
    }

    public Long getParlayRefNo() {
        return parlayRefNo;
    }

    public void setParlayRefNo(Long parlayRefNo) {
        this.parlayRefNo = parlayRefNo;
    }

    public BigDecimal getOdds() {
        return odds;
    }

    public void setOdds(BigDecimal odds) {
        this.odds = odds;
    }

    public BigDecimal getStake() {
        return stake;
    }

    public void setStake(BigDecimal stake) {
        this.stake = stake;
    }

    public Date getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(Date transactionTime) {
        this.transactionTime = transactionTime;
    }

    public String getTicketStatus() {
        return ticketStatus;
    }

    public void setTicketStatus(String ticketStatus) {
        this.ticketStatus = ticketStatus;
    }

    public BigDecimal getWinlostAmount() {
        return winlostAmount;
    }

    public void setWinlostAmount(BigDecimal winlostAmount) {
        this.winlostAmount = winlostAmount;
    }

    public BigDecimal getAfterAmount() {
        return afterAmount;
    }

    public void setAfterAmount(BigDecimal afterAmount) {
        this.afterAmount = afterAmount;
    }

    public Integer getCurrency() {
        return currency;
    }

    public void setCurrency(Integer currency) {
        this.currency = currency;
    }

    public Date getWinlostDatetime() {
        return winlostDatetime;
    }

    public void setWinlostDatetime(Date winlostDatetime) {
        this.winlostDatetime = winlostDatetime;
    }

    public Integer getOddsType() {
        return oddsType;
    }

    public void setOddsType(Integer oddsType) {
        this.oddsType = oddsType;
    }

    public String getOddsInfo() {
        return oddsInfo;
    }

    public void setOddsInfo(String oddsInfo) {
        this.oddsInfo = oddsInfo;
    }

    public String getBetTeam() {
        return betTeam;
    }

    public void setBetTeam(String betTeam) {
        this.betTeam = betTeam;
    }

    public String getExculding() {
        return exculding;
    }

    public void setExculding(String exculding) {
        this.exculding = exculding;
    }

    public String getBetTag() {
        return betTag;
    }

    public void setBetTag(String betTag) {
        this.betTag = betTag;
    }

    public BigDecimal getHomeHdp() {
        return homeHdp;
    }

    public void setHomeHdp(BigDecimal homeHdp) {
        this.homeHdp = homeHdp;
    }

    public BigDecimal getAwayHdp() {
        return awayHdp;
    }

    public void setAwayHdp(BigDecimal awayHdp) {
        this.awayHdp = awayHdp;
    }

    public BigDecimal getHdp() {
        return hdp;
    }

    public void setHdp(BigDecimal hdp) {
        this.hdp = hdp;
    }

    public String getBetfrom() {
        return betfrom;
    }

    public void setBetfrom(String betfrom) {
        this.betfrom = betfrom;
    }

    public String getIslive() {
        return islive;
    }

    public void setIslive(String islive) {
        this.islive = islive;
    }

    public Integer getHomeScore() {
        return homeScore;
    }

    public void setHomeScore(Integer homeScore) {
        this.homeScore = homeScore;
    }

    public Integer getAwayScore() {
        return awayScore;
    }

    public void setAwayScore(Integer awayScore) {
        this.awayScore = awayScore;
    }

    public Date getSettlementTime() {
        return settlementTime;
    }

    public void setSettlementTime(Date settlementTime) {
        this.settlementTime = settlementTime;
    }

    public String getHomeTeamName() {
        return homeTeamName;
    }

    public void setHomeTeamName(String homeTeamName) {
        this.homeTeamName = homeTeamName;
    }

    public String getLeagueName() {
        return leagueName;
    }

    public void setLeagueName(String leagueName) {
        this.leagueName = leagueName;
    }

    public String getAwayTeamName() {
        return awayTeamName;
    }

    public void setAwayTeamName(String awayTeamName) {
        this.awayTeamName = awayTeamName;
    }

    public String getCustomInfo1() {
        return customInfo1;
    }

    public void setCustomInfo1(String customInfo1) {
        this.customInfo1 = customInfo1;
    }

    public String getCustomInfo2() {
        return customInfo2;
    }

    public void setCustomInfo2(String customInfo2) {
        this.customInfo2 = customInfo2;
    }

    public String getCustomInfo3() {
        return customInfo3;
    }

    public void setCustomInfo3(String customInfo3) {
        this.customInfo3 = customInfo3;
    }

    public String getCustomInfo4() {
        return customInfo4;
    }

    public void setCustomInfo4(String customInfo4) {
        this.customInfo4 = customInfo4;
    }

    public String getCustomInfo5() {
        return customInfo5;
    }

    public void setCustomInfo5(String customInfo5) {
        this.customInfo5 = customInfo5;
    }

    public String getBaStatus() {
        return baStatus;
    }

    public void setBaStatus(String baStatus) {
        this.baStatus = baStatus;
    }

    public Long getVersionKey() {
        return versionKey;
    }

    public void setVersionKey(Long versionKey) {
        this.versionKey = versionKey;
    }

    public String getParlayData() {
        return parlayData;
    }

    public void setParlayData(String parlayData) {
        this.parlayData = parlayData;
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
        return "BetslipsShabaSports{" +
        "id=" + id +
        ", xbUid=" + xbUid +
        ", xbUsername=" + xbUsername +
        ", vendorMemberId=" + vendorMemberId +
        ", operatorId=" + operatorId +
        ", leagueId=" + leagueId +
        ", matchId=" + matchId +
        ", homeId=" + homeId +
        ", awayId=" + awayId +
        ", teamId=" + teamId +
        ", matchDatetime=" + matchDatetime +
        ", sportType=" + sportType +
        ", betType=" + betType +
        ", parlayRefNo=" + parlayRefNo +
        ", odds=" + odds +
        ", stake=" + stake +
        ", transactionTime=" + transactionTime +
        ", ticketStatus=" + ticketStatus +
        ", winlostAmount=" + winlostAmount +
        ", afterAmount=" + afterAmount +
        ", currency=" + currency +
        ", winlostDatetime=" + winlostDatetime +
        ", oddsType=" + oddsType +
        ", oddsInfo=" + oddsInfo +
        ", betTeam=" + betTeam +
        ", exculding=" + exculding +
        ", betTag=" + betTag +
        ", homeHdp=" + homeHdp +
        ", awayHdp=" + awayHdp +
        ", hdp=" + hdp +
        ", betfrom=" + betfrom +
        ", islive=" + islive +
        ", homeScore=" + homeScore +
        ", awayScore=" + awayScore +
        ", settlementTime=" + settlementTime +
        ", homeTeamName=" + homeTeamName +
        ", leagueName=" + leagueName +
        ", awayTeamName=" + awayTeamName +
        ", customInfo1=" + customInfo1 +
        ", customInfo2=" + customInfo2 +
        ", customInfo3=" + customInfo3 +
        ", customInfo4=" + customInfo4 +
        ", customInfo5=" + customInfo5 +
        ", baStatus=" + baStatus +
        ", versionKey=" + versionKey +
        ", parlayData=" + parlayData +
        ", xbCoin=" + xbCoin +
        ", xbValidCoin=" + xbValidCoin +
        ", xbProfit=" + xbProfit +
        ", xbStatus=" + xbStatus +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        "}";
    }
}
