package com.xinbo.sports.dao.generator.po;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

/**
 * <p>
 * 存款表
 * </p>
 *
 * @author David
 * @since 2021-02-01
 */
@TableName("sp_coin_deposit")
public class CoinDeposit extends Model<CoinDeposit> {

    private static final long serialVersionUID=1L;

    @TableId("id")
    private Long id;

    /**
     * 订单号(三方平台用)
     */
    @TableField("order_id")
    private String orderId;

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
     * 支付类型:0-离线 1-在线
     */
    @TableField("pay_type")
    private Integer payType;

    /**
     * 类型:1-银联 2-微信 3-支付宝 4-QQ 5-QR扫码
     */
    @TableField("category")
    private Integer category;

    /**
     * 关联ID
     */
    @TableField("pay_refer")
    private Integer payRefer;

    /**
     * 提交金额
     */
    @TableField("coin")
    private BigDecimal coin;

    /**
     * 到账金额
     */
    @TableField("pay_coin")
    private BigDecimal payCoin;

    /**
     * 充值前金额
     */
    @TableField("coin_before")
    private BigDecimal coinBefore;

    /**
     * 打款人姓名
     */
    @TableField("dep_realname")
    private String depRealname;

    /**
     * 审核状态:0-未审核 1-审核中 2-审核失败 3-审核通过
     */
    @TableField("audit_status")
    private Integer auditStatus;

    /**
     * 审核人ID
     */
    @TableField("audit_uid")
    private Integer auditUid;

    /**
     * 审核备注
     */
    @TableField("audit_mark")
    private String auditMark;

    /**
     * 审核时间
     */
    @TableField("audited_at")
    private Integer auditedAt;

    /**
     * 上分状态:0-申请中 1-手动到账 2-自动到账 3-充值失败 8-充值锁定 9-管理员充值
     */
    @TableField("status")
    private Integer status;

    /**
     * 充值标识:1-首充 2-二充 9-其他
     */
    @TableField("dep_status")
    private Integer depStatus;

    /**
     * 打款人备注
     */
    @TableField("dep_mark")
    private String depMark;

    /**
     * 操作人ID
     */
    @TableField("admin_id")
    private Integer adminId;

    /**
     * 上分时间
     */
    @TableField("deposited_at")
    private Integer depositedAt;

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

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
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

    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public Integer getPayRefer() {
        return payRefer;
    }

    public void setPayRefer(Integer payRefer) {
        this.payRefer = payRefer;
    }

    public BigDecimal getCoin() {
        return coin;
    }

    public void setCoin(BigDecimal coin) {
        this.coin = coin;
    }

    public BigDecimal getPayCoin() {
        return payCoin;
    }

    public void setPayCoin(BigDecimal payCoin) {
        this.payCoin = payCoin;
    }

    public BigDecimal getCoinBefore() {
        return coinBefore;
    }

    public void setCoinBefore(BigDecimal coinBefore) {
        this.coinBefore = coinBefore;
    }

    public String getDepRealname() {
        return depRealname;
    }

    public void setDepRealname(String depRealname) {
        this.depRealname = depRealname;
    }

    public Integer getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(Integer auditStatus) {
        this.auditStatus = auditStatus;
    }

    public Integer getAuditUid() {
        return auditUid;
    }

    public void setAuditUid(Integer auditUid) {
        this.auditUid = auditUid;
    }

    public String getAuditMark() {
        return auditMark;
    }

    public void setAuditMark(String auditMark) {
        this.auditMark = auditMark;
    }

    public Integer getAuditedAt() {
        return auditedAt;
    }

    public void setAuditedAt(Integer auditedAt) {
        this.auditedAt = auditedAt;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getDepStatus() {
        return depStatus;
    }

    public void setDepStatus(Integer depStatus) {
        this.depStatus = depStatus;
    }

    public String getDepMark() {
        return depMark;
    }

    public void setDepMark(String depMark) {
        this.depMark = depMark;
    }

    public Integer getAdminId() {
        return adminId;
    }

    public void setAdminId(Integer adminId) {
        this.adminId = adminId;
    }

    public Integer getDepositedAt() {
        return depositedAt;
    }

    public void setDepositedAt(Integer depositedAt) {
        this.depositedAt = depositedAt;
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
        return "CoinDeposit{" +
        "id=" + id +
        ", orderId=" + orderId +
        ", title=" + title +
        ", uid=" + uid +
        ", username=" + username +
        ", payType=" + payType +
        ", category=" + category +
        ", payRefer=" + payRefer +
        ", coin=" + coin +
        ", payCoin=" + payCoin +
        ", coinBefore=" + coinBefore +
        ", depRealname=" + depRealname +
        ", auditStatus=" + auditStatus +
        ", auditUid=" + auditUid +
        ", auditMark=" + auditMark +
        ", auditedAt=" + auditedAt +
        ", status=" + status +
        ", depStatus=" + depStatus +
        ", depMark=" + depMark +
        ", adminId=" + adminId +
        ", depositedAt=" + depositedAt +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        "}";
    }
}
