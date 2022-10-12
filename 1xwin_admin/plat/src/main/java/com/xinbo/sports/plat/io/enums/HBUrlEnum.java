package com.xinbo.sports.plat.io.enums;

import lombok.Getter;

/**
 * @author: David
 * @date: 02/05/2020
 * @description:
 */
@Getter
public enum HBUrlEnum {
    /**
     * 请求接口参数
     */
    GET_GAMES("获取游戏列表", "getgames"),
    LOGIN_OR_CREATE_PLAYER("登录或创建用户", "LoginOrCreatePlayer"),
    LOGOUT("登出游戏", "LogoutPlayerRequest"),
    DEPOSIT_PLAYER_MONEY("上分", "DepositPlayerMoney"),
    WITHDRAW_PLAYER_MONEY("下分", "WithdrawPlayerMoney"),
    QUERY_TRANSFER("查询转账信息", "QueryTransfer"),
    QUERY_PLAYER("查询用户信息", "QueryPlayer"),
    LOG_OUT_PLAYER("踢出登录", "LogOutPlayer"),
    GET_BRAND_COMPLETED_GAME_RESULTS_V2("查询品牌下的注单信息", "GetBrandCompletedGameResultsV2"),
    ;


    private String name;
    private String url;

    HBUrlEnum(String name, String url) {
        this.name = name;
        this.url = url;
    }
}
