package com.xinbo.sports.plat.io.enums;

import lombok.Getter;

/**
 * <p>
 * WM真人
 * </p>
 *
 * @author andy
 * @since 2020/5/20
 */
@Getter
public enum WMUrlEnum {
    WMLIVE_MEMBERREGISTER("MemberRegister", "注册会员"),
    WMLIVE_SIGNINGAME("SigninGame", "开游戏(登录)"),
    WMLIVE_LOGOUTGAME("LogoutGame", "登出游戏"),
    WMLIVE_CHANGEPASSWORD("ChangePassword", "更新密码"),
    WMLIVE_GETBALANCE("GetBalance", "取余额"),
    WMLIVE_CHANGEBALANCE("ChangeBalance", "加扣点"),
    WMLIVE_GETMEMBERTRADEREPORT("GetMemberTradeReport", "get交易纪录"),
    WMLIVE_GETDATETIMEREPORT("GetDateTimeReport", "游戏纪录报表");

    private String methodName;
    private String methodNameDesc;

    /**
     * @param methodName     方法名称
     * @param methodNameDesc 方法名称描述
     */
    WMUrlEnum(String methodName, String methodNameDesc) {
        this.methodName = methodName;
        this.methodNameDesc = methodNameDesc;
    }
}
