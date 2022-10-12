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
 * TCG彩票注单表
 * </p>
 *
 * @author David
 * @since 2021-02-01
 */
@TableName("sp_betslips_tcg")
public class BetslipsTcg extends Model<BetslipsTcg> {

    private static final long serialVersionUID=1L;

    /**
     * id -> bet_order_no
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
     * 投注金额
     */
    @TableField("bet_amount")
    private BigDecimal betAmount;

    /**
     * 游戏代码
     */
    @TableField("game_code")
    private String gameCode;

    /**
     * 投注时间
     */
    @TableField("bet_time")
    private Date betTime;

    /**
     * 交易时间
     */
    @TableField("trans_time")
    private Date transTime;

    /**
     * 投注内容
     */
    @TableField("bet_content_id")
    private String betContentId;

    /**
     * 玩法代码
     */
    @TableField("play_code")
    private String playCode;

    /**
     * 订单号(后台查询)

订单号（后台查询）
     */
    @TableField("order_num")
    private String orderNum;

    /**
     * 追号（true,false）
     */
    @TableField("chase")
    private String chase;

    /**
     * 期号
     */
    @TableField("numero")
    private String numero;

    /**
     * 投注实际内容
     */
    @TableField("betting_content")
    private String bettingContent;

    /**
     * 玩法id
     */
    @TableField("play_id")
    private Integer playId;

    /**
     * 冻结时间
     */
    @TableField("freeze_time")
    private Date freezeTime;

    /**
     * 下注倍数
     */
    @TableField("multiple")
    private Integer multiple;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 产品代码
     */
    @TableField("product_type")
    private String productType;

    /**
     * 单式注单（true,false）
     */
    @TableField("single")
    private String single;

    /**
     * 商户代码
     */
    @TableField("merchant_code")
    private String merchantCode;

    /**
     * 中奖金额
     */
    @TableField("win_amount")
    private BigDecimal winAmount;

    /**
     * 结算时间
     */
    @TableField("settlement_time")
    private Date settlementTime;

    /**
     * 净输赢
     */
    @TableField("netPNL")
    private BigDecimal netPNL;

    /**
     * 订单状态(1:WIN | 2:LOSE | 3:CANCELLED | 4:TIE )(1:已中奖｜2:未中奖 ｜3:取消 | 4:和)
     */
    @TableField("bet_status")
    private Integer betStatus;

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

    public BigDecimal getBetAmount() {
        return betAmount;
    }

    public void setBetAmount(BigDecimal betAmount) {
        this.betAmount = betAmount;
    }

    public String getGameCode() {
        return gameCode;
    }

    public void setGameCode(String gameCode) {
        this.gameCode = gameCode;
    }

    public Date getBetTime() {
        return betTime;
    }

    public void setBetTime(Date betTime) {
        this.betTime = betTime;
    }

    public Date getTransTime() {
        return transTime;
    }

    public void setTransTime(Date transTime) {
        this.transTime = transTime;
    }

    public String getBetContentId() {
        return betContentId;
    }

    public void setBetContentId(String betContentId) {
        this.betContentId = betContentId;
    }

    public String getPlayCode() {
        return playCode;
    }

    public void setPlayCode(String playCode) {
        this.playCode = playCode;
    }

    public String getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(String orderNum) {
        this.orderNum = orderNum;
    }

    public String getChase() {
        return chase;
    }

    public void setChase(String chase) {
        this.chase = chase;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getBettingContent() {
        return bettingContent;
    }

    public void setBettingContent(String bettingContent) {
        this.bettingContent = bettingContent;
    }

    public Integer getPlayId() {
        return playId;
    }

    public void setPlayId(Integer playId) {
        this.playId = playId;
    }

    public Date getFreezeTime() {
        return freezeTime;
    }

    public void setFreezeTime(Date freezeTime) {
        this.freezeTime = freezeTime;
    }

    public Integer getMultiple() {
        return multiple;
    }

    public void setMultiple(Integer multiple) {
        this.multiple = multiple;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getSingle() {
        return single;
    }

    public void setSingle(String single) {
        this.single = single;
    }

    public String getMerchantCode() {
        return merchantCode;
    }

    public void setMerchantCode(String merchantCode) {
        this.merchantCode = merchantCode;
    }

    public BigDecimal getWinAmount() {
        return winAmount;
    }

    public void setWinAmount(BigDecimal winAmount) {
        this.winAmount = winAmount;
    }

    public Date getSettlementTime() {
        return settlementTime;
    }

    public void setSettlementTime(Date settlementTime) {
        this.settlementTime = settlementTime;
    }

    public BigDecimal getNetPNL() {
        return netPNL;
    }

    public void setNetPNL(BigDecimal netPNL) {
        this.netPNL = netPNL;
    }

    public Integer getBetStatus() {
        return betStatus;
    }

    public void setBetStatus(Integer betStatus) {
        this.betStatus = betStatus;
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
        return "BetslipsTcg{" +
        "id=" + id +
        ", xbUid=" + xbUid +
        ", xbUsername=" + xbUsername +
        ", betAmount=" + betAmount +
        ", gameCode=" + gameCode +
        ", betTime=" + betTime +
        ", transTime=" + transTime +
        ", betContentId=" + betContentId +
        ", playCode=" + playCode +
        ", orderNum=" + orderNum +
        ", chase=" + chase +
        ", numero=" + numero +
        ", bettingContent=" + bettingContent +
        ", playId=" + playId +
        ", freezeTime=" + freezeTime +
        ", multiple=" + multiple +
        ", username=" + username +
        ", productType=" + productType +
        ", single=" + single +
        ", merchantCode=" + merchantCode +
        ", winAmount=" + winAmount +
        ", settlementTime=" + settlementTime +
        ", netPNL=" + netPNL +
        ", betStatus=" + betStatus +
        ", xbCoin=" + xbCoin +
        ", xbValidCoin=" + xbValidCoin +
        ", xbProfit=" + xbProfit +
        ", xbStatus=" + xbStatus +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        "}";
    }
}
