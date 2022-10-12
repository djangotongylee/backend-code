package com.xinbo.sports.dao.generator.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

/**
 * <p>
 * 三方支付配置信息
 * </p>
 *
 * @author David
 * @since 2021-02-01
 */
@TableName("sp_pay_plat")
public class PayPlat extends Model<PayPlat> {

    private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 编码
     */
    @TableField("code")
    private String code;

    /**
     * 名称(中文)
     */
    @TableField("name")
    private String name;

    /**
     * 商户号
     */
    @TableField("business_code")
    private String businessCode;

    /**
     * 商户秘钥
     */
    @TableField("business_pwd")
    private String businessPwd;

    /**
     * 公钥
     */
    @TableField("public_key")
    private String publicKey;

    /**
     * 私钥
     */
    @TableField("private_key")
    private String privateKey;

    /**
     * 付款地址
     */
    @TableField("url")
    private String url;

    /**
     * 代付地址
     */
    @TableField("withdraw_url")
    private String withdrawUrl;

    /**
     * 异步通知回调
     */
    @TableField("notifyUrl")
    private String notifyUrl;

    /**
     * 同步通知回调
     */
    @TableField("returnUrl")
    private String returnUrl;

    /**
     * 支付模型
     */
    @TableField("pay_model")
    private String payModel;

    /**
     * 状态:0-停用 1-启用 2-删除
     */
    @TableField("status")
    private Integer status;

    @TableField("created_at")
    private Integer createdAt;

    @TableField("updated_at")
    private Integer updatedAt;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBusinessCode() {
        return businessCode;
    }

    public void setBusinessCode(String businessCode) {
        this.businessCode = businessCode;
    }

    public String getBusinessPwd() {
        return businessPwd;
    }

    public void setBusinessPwd(String businessPwd) {
        this.businessPwd = businessPwd;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getWithdrawUrl() {
        return withdrawUrl;
    }

    public void setWithdrawUrl(String withdrawUrl) {
        this.withdrawUrl = withdrawUrl;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public String getPayModel() {
        return payModel;
    }

    public void setPayModel(String payModel) {
        this.payModel = payModel;
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
        return "PayPlat{" +
        "id=" + id +
        ", code=" + code +
        ", name=" + name +
        ", businessCode=" + businessCode +
        ", businessPwd=" + businessPwd +
        ", publicKey=" + publicKey +
        ", privateKey=" + privateKey +
        ", url=" + url +
        ", withdrawUrl=" + withdrawUrl +
        ", notifyUrl=" + notifyUrl +
        ", returnUrl=" + returnUrl +
        ", payModel=" + payModel +
        ", status=" + status +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        "}";
    }
}
