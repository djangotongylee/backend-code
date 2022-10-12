package com.xinbo.sports.plat.io.enums;

import lombok.Getter;

/**
 * <p>
 * DG视讯
 * </p>
 *
 * @author andy
 * @since 2020/5/20
 */
@Getter
public enum DGUrlEnum {
    /**
     * 注册新会员
     */
    DGLIVE_REGISTER("/user/signup/", "注册新会员"),
    /**
     * 登录
     */
    DGLIVE_LOGIN("/user/login/", "会员登入"),
    /**
     * 获取会员余额
     */
    DGLIVE_GETBALANCE("/user/getBalance/", "获取会员余额"),
    /**
     * 会员存取款
     */
    DGLIVE_TRANSFER("/account/transfer/", "会员存取款"),
    /**
     * 检查存取款操作是否成功
     */
    DGLIVE_CHECKTRANSFER("/account/checkTransfer/", "检查存取款操作是否成功"),
    /**
     * 抓取注单报表
     */
    DGLIVE_GETREPORT("/game/getReport/", "抓取注单报表"),

    /**
     * 标记已抓取注单
     */
    DGLIVE_MARKREPORT("/game/markReport/", "标记已抓取注单"),

    /**
     * 补单拉单:下注注单通过接口
     */
    GAME_GETREPORT("/game/getReport/", "下注注单通过接口"),
    /**
     * 补单拉单:小费注单通过接口
     */
    GAME_GETTIPGIFT("/game/getTipGift/", "小费注单通过接口");

    private String methodName;
    private String methodNameDesc;

    /**
     * @param methodName     方法名称
     * @param methodNameDesc 方法名称描述
     */
    DGUrlEnum(String methodName, String methodNameDesc) {
        this.methodName = methodName;
        this.methodNameDesc = methodNameDesc;
    }
}
