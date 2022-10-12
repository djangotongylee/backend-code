package com.xinbo.sports.dao.generator.po;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

/**
 * <p>
 * 客户表补充表
 * </p>
 *
 * @author David
 * @since 2021-02-01
 */
@TableName("sp_user_profile")
public class UserProfile extends Model<UserProfile> {

    private static final long serialVersionUID=1L;

    /**
     * UID
     */
    @TableId("uid")
    private Integer uid;

    /**
     * 真实姓名
     */
    @TableField("realname")
    private String realname;

    /**
     * 个性签名
     */
    @TableField("signature")
    private String signature;

    /**
     * 生日
     */
    @TableField("birthday")
    private String birthday;

    /**
     * 区号
     */
    @TableField("area_code")
    private String areaCode;

    /**
     * 手机号码
     */
    @TableField("mobile")
    private String mobile;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 性别:1-男 0-女 2-未知
     */
    @TableField("sex")
    private Integer sex;

    /**
     * 自动上下分:1-是 0-否
     */
    @TableField("auto_transfer")
    private Integer autoTransfer;

    /**
     * 是否绑定银行卡:1-已绑定 0-未绑定
     */
    @TableField("bind_bank")
    private Integer bindBank;

    /**
     * 家庭地址
     */
    @TableField("address")
    private String address;

    /**
     * 积分
     */
    @TableField("score")
    private Integer score;

    /**
     * 推广码
     */
    @TableField("promo_code")
    private Integer promoCode;

    /**
     * 上1级代理
     */
    @TableField("sup_uid_1")
    private Integer supUid1;

    /**
     * 上2级代理
     */
    @TableField("sup_uid_2")
    private Integer supUid2;

    /**
     * 上3级代理
     */
    @TableField("sup_uid_3")
    private Integer supUid3;

    /**
     * 上4级代理
     */
    @TableField("sup_uid_4")
    private Integer supUid4;

    /**
     * 上5级代理
     */
    @TableField("sup_uid_5")
    private Integer supUid5;

    /**
     * 上6级代理
     */
    @TableField("sup_uid_6")
    private Integer supUid6;

    /**
     * 登录密码
     */
    @TableField("password_hash")
    private String passwordHash;

    /**
     * 取款密码
     */
    @TableField("password_coin")
    private String passwordCoin;

    /**
     * 状态:10-正常 9-冻结 8-删除
     */
    @TableField("status")
    private Integer status;

    /**
     * 额外打码量
     */
    @TableField("extra_code")
    private BigDecimal extraCode;

    /**
     * 额外打码量规则；0-一次生效，1-长久生效
     */
    @TableField("extra_code_rule")
    private Integer extraCodeRule;

    /**
     * 升级后余额
     */
    @TableField("upgrade_balance")
    private BigDecimal upgradeBalance;

    @TableField("created_at")
    private Integer createdAt;

    @TableField("updated_at")
    private Integer updatedAt;


    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public Integer getAutoTransfer() {
        return autoTransfer;
    }

    public void setAutoTransfer(Integer autoTransfer) {
        this.autoTransfer = autoTransfer;
    }

    public Integer getBindBank() {
        return bindBank;
    }

    public void setBindBank(Integer bindBank) {
        this.bindBank = bindBank;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(Integer promoCode) {
        this.promoCode = promoCode;
    }

    public Integer getSupUid1() {
        return supUid1;
    }

    public void setSupUid1(Integer supUid1) {
        this.supUid1 = supUid1;
    }

    public Integer getSupUid2() {
        return supUid2;
    }

    public void setSupUid2(Integer supUid2) {
        this.supUid2 = supUid2;
    }

    public Integer getSupUid3() {
        return supUid3;
    }

    public void setSupUid3(Integer supUid3) {
        this.supUid3 = supUid3;
    }

    public Integer getSupUid4() {
        return supUid4;
    }

    public void setSupUid4(Integer supUid4) {
        this.supUid4 = supUid4;
    }

    public Integer getSupUid5() {
        return supUid5;
    }

    public void setSupUid5(Integer supUid5) {
        this.supUid5 = supUid5;
    }

    public Integer getSupUid6() {
        return supUid6;
    }

    public void setSupUid6(Integer supUid6) {
        this.supUid6 = supUid6;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPasswordCoin() {
        return passwordCoin;
    }

    public void setPasswordCoin(String passwordCoin) {
        this.passwordCoin = passwordCoin;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public BigDecimal getExtraCode() {
        return extraCode;
    }

    public void setExtraCode(BigDecimal extraCode) {
        this.extraCode = extraCode;
    }

    public Integer getExtraCodeRule() {
        return extraCodeRule;
    }

    public void setExtraCodeRule(Integer extraCodeRule) {
        this.extraCodeRule = extraCodeRule;
    }

    public BigDecimal getUpgradeBalance() {
        return upgradeBalance;
    }

    public void setUpgradeBalance(BigDecimal upgradeBalance) {
        this.upgradeBalance = upgradeBalance;
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
        return this.uid;
    }

    @Override
    public String toString() {
        return "UserProfile{" +
        "uid=" + uid +
        ", realname=" + realname +
        ", signature=" + signature +
        ", birthday=" + birthday +
        ", areaCode=" + areaCode +
        ", mobile=" + mobile +
        ", email=" + email +
        ", sex=" + sex +
        ", autoTransfer=" + autoTransfer +
        ", bindBank=" + bindBank +
        ", address=" + address +
        ", score=" + score +
        ", promoCode=" + promoCode +
        ", supUid1=" + supUid1 +
        ", supUid2=" + supUid2 +
        ", supUid3=" + supUid3 +
        ", supUid4=" + supUid4 +
        ", supUid5=" + supUid5 +
        ", supUid6=" + supUid6 +
        ", passwordHash=" + passwordHash +
        ", passwordCoin=" + passwordCoin +
        ", status=" + status +
        ", extraCode=" + extraCode +
        ", extraCodeRule=" + extraCodeRule +
        ", upgradeBalance=" + upgradeBalance +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        "}";
    }
}
