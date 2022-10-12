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
 * BTI体育注单表
 * </p>
 *
 * @author David
 * @since 2021-02-01
 */
@TableName("sp_betslips_bti")
public class BetslipsBti extends Model<BetslipsBti> {

    private static final long serialVersionUID=1L;

    /**
     * id -> BetID
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
     * 可能赢取金额
     */
    @TableField("gain")
    private BigDecimal gain;

    /**
     * 输赢
     */
    @TableField("pl")
    private BigDecimal pl;

    /**
     * 游戏代码
     */
    @TableField("non_cash_out_amount")
    private BigDecimal nonCashOutAmount;

    /**
     * 投注时间
     */
    @TableField("combo_bonus_amount")
    private BigDecimal comboBonusAmount;

    /**
     * 结算时间
     */
    @TableField("bet_settled_date")
    private String betSettledDate;

    /**
     * 结算时间
     */
    @TableField("update_date")
    private Date updateDate;

    @TableField("odds")
    private Integer odds;

    /**
     * Odds in user style (American, Decimal, Fractional)
     */
    @TableField("odds_in_user_style")
    private String oddsInUserStyle;

    @TableField("odds_style_of_user")
    private String oddsStyleOfUser;

    /**
     * In case of live betting – Score 
for Home team at the time of 
the bet
     */
    @TableField("live_score1")
    private String liveScore1;

    @TableField("total_stake")
    private BigDecimal totalStake;

    /**
     * 玩法id
     */
    @TableField("odds_dec")
    private String oddsDec;

    /**
     * 有效金额
     */
    @TableField("valid_stake")
    private BigDecimal validStake;

    /**
     * 游戏类型
     */
    @TableField("branch_id")
    private Integer branchId;

    /**
     * Platform name: Web/Mobile
     */
    @TableField("platform")
    private String platform;

    /**
     * 产品代码
     */
    @TableField("return_amount")
    private BigDecimal returnAmount;

    /**
     * 单式注单（true,false）
     */
    @TableField("domain_id")
    private Integer domainId;

    /**
     * 投注状态：half won/lost: Won, Lost, Half won, Half lost, Canceled, Draw, Open
     */
    @TableField("bet_status")
    private String betStatus;

    /**
     * 品牌名称
     */
    @TableField("brand")
    private String brand;

    /**
     * 用户名
     */
    @TableField("user_name")
    private String userName;

    /**
     * 投注类型名称
     */
    @TableField("bet_type_name")
    private String betTypeName;

    /**
     * 投注类型id
     */
    @TableField("bet_type_id")
    private Integer betTypeId;

    /**
     * 创建时间
     */
    @TableField("creation_date")
    private Date creationDate;

    /**
     * 输赢状态：Won, Lost, Canceled, Draw, Open
     */
    @TableField("status")
    private String status;

    /**
     * 客户id
     */
    @TableField("customer_id")
    private Integer customerId;

    /**
     * 商户id
     */
    @TableField("merchant_customer_id")
    private String merchantCustomerId;

    /**
     * 货币代码
     */
    @TableField("currency")
    private String currency;

    /**
     * 用户等级id
     */
    @TableField("player_level_id")
    private Integer playerLevelId;

    /**
     * 用户等级名称
     */
    @TableField("player_level_name")
    private String playerLevelName;

    /**
     * 投注选项
     */
    @TableField("selections")
    private String selections;

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

    public BigDecimal getGain() {
        return gain;
    }

    public void setGain(BigDecimal gain) {
        this.gain = gain;
    }

    public BigDecimal getPl() {
        return pl;
    }

    public void setPl(BigDecimal pl) {
        this.pl = pl;
    }

    public BigDecimal getNonCashOutAmount() {
        return nonCashOutAmount;
    }

    public void setNonCashOutAmount(BigDecimal nonCashOutAmount) {
        this.nonCashOutAmount = nonCashOutAmount;
    }

    public BigDecimal getComboBonusAmount() {
        return comboBonusAmount;
    }

    public void setComboBonusAmount(BigDecimal comboBonusAmount) {
        this.comboBonusAmount = comboBonusAmount;
    }

    public String getBetSettledDate() {
        return betSettledDate;
    }

    public void setBetSettledDate(String betSettledDate) {
        this.betSettledDate = betSettledDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public Integer getOdds() {
        return odds;
    }

    public void setOdds(Integer odds) {
        this.odds = odds;
    }

    public String getOddsInUserStyle() {
        return oddsInUserStyle;
    }

    public void setOddsInUserStyle(String oddsInUserStyle) {
        this.oddsInUserStyle = oddsInUserStyle;
    }

    public String getOddsStyleOfUser() {
        return oddsStyleOfUser;
    }

    public void setOddsStyleOfUser(String oddsStyleOfUser) {
        this.oddsStyleOfUser = oddsStyleOfUser;
    }

    public String getLiveScore1() {
        return liveScore1;
    }

    public void setLiveScore1(String liveScore1) {
        this.liveScore1 = liveScore1;
    }

    public BigDecimal getTotalStake() {
        return totalStake;
    }

    public void setTotalStake(BigDecimal totalStake) {
        this.totalStake = totalStake;
    }

    public String getOddsDec() {
        return oddsDec;
    }

    public void setOddsDec(String oddsDec) {
        this.oddsDec = oddsDec;
    }

    public BigDecimal getValidStake() {
        return validStake;
    }

    public void setValidStake(BigDecimal validStake) {
        this.validStake = validStake;
    }

    public Integer getBranchId() {
        return branchId;
    }

    public void setBranchId(Integer branchId) {
        this.branchId = branchId;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public BigDecimal getReturnAmount() {
        return returnAmount;
    }

    public void setReturnAmount(BigDecimal returnAmount) {
        this.returnAmount = returnAmount;
    }

    public Integer getDomainId() {
        return domainId;
    }

    public void setDomainId(Integer domainId) {
        this.domainId = domainId;
    }

    public String getBetStatus() {
        return betStatus;
    }

    public void setBetStatus(String betStatus) {
        this.betStatus = betStatus;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getBetTypeName() {
        return betTypeName;
    }

    public void setBetTypeName(String betTypeName) {
        this.betTypeName = betTypeName;
    }

    public Integer getBetTypeId() {
        return betTypeId;
    }

    public void setBetTypeId(Integer betTypeId) {
        this.betTypeId = betTypeId;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getMerchantCustomerId() {
        return merchantCustomerId;
    }

    public void setMerchantCustomerId(String merchantCustomerId) {
        this.merchantCustomerId = merchantCustomerId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Integer getPlayerLevelId() {
        return playerLevelId;
    }

    public void setPlayerLevelId(Integer playerLevelId) {
        this.playerLevelId = playerLevelId;
    }

    public String getPlayerLevelName() {
        return playerLevelName;
    }

    public void setPlayerLevelName(String playerLevelName) {
        this.playerLevelName = playerLevelName;
    }

    public String getSelections() {
        return selections;
    }

    public void setSelections(String selections) {
        this.selections = selections;
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
        return "BetslipsBti{" +
        "id=" + id +
        ", xbUid=" + xbUid +
        ", xbUsername=" + xbUsername +
        ", gain=" + gain +
        ", pl=" + pl +
        ", nonCashOutAmount=" + nonCashOutAmount +
        ", comboBonusAmount=" + comboBonusAmount +
        ", betSettledDate=" + betSettledDate +
        ", updateDate=" + updateDate +
        ", odds=" + odds +
        ", oddsInUserStyle=" + oddsInUserStyle +
        ", oddsStyleOfUser=" + oddsStyleOfUser +
        ", liveScore1=" + liveScore1 +
        ", totalStake=" + totalStake +
        ", oddsDec=" + oddsDec +
        ", validStake=" + validStake +
        ", branchId=" + branchId +
        ", platform=" + platform +
        ", returnAmount=" + returnAmount +
        ", domainId=" + domainId +
        ", betStatus=" + betStatus +
        ", brand=" + brand +
        ", userName=" + userName +
        ", betTypeName=" + betTypeName +
        ", betTypeId=" + betTypeId +
        ", creationDate=" + creationDate +
        ", status=" + status +
        ", customerId=" + customerId +
        ", merchantCustomerId=" + merchantCustomerId +
        ", currency=" + currency +
        ", playerLevelId=" + playerLevelId +
        ", playerLevelName=" + playerLevelName +
        ", selections=" + selections +
        ", xbCoin=" + xbCoin +
        ", xbValidCoin=" + xbValidCoin +
        ", xbProfit=" + xbProfit +
        ", xbStatus=" + xbStatus +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        "}";
    }
}
