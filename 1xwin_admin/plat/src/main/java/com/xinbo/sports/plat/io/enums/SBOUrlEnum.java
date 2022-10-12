package com.xinbo.sports.plat.io.enums;

import lombok.Getter;

/**
 * @author: David
 * @date: 02/05/2020
 * @description:
 */
@Getter
public enum SBOUrlEnum {
    UPDATE_AGENT_PRESET_BET_SETTINGS("更新代理预设投注设置", "/web-root/restricted/agent/update-agent-preset-bet-settings.aspx", "UPDATE_AGENT_PRESET_BET_SETTINGS"),
    UPDATE_AGENT_STATUS("更新代理状态", "/web-root/restricted/agent/update-agent-status.aspx", "UPDATE_AGENT_STATUS"),
    UPDATE_AGENT_PRESET_BET_SETTING_BY_SPORTID_AND_MARKETTYPE("通过体育ID和市场类型更新Agent预设投注设置",
            "/web-root/restricted/agent/update-agent-preset-bet-setting-by-sportid-and-markettype.aspx",
            "UPDATE_AGENT_PRESET_BET_SETTING_BY_SPORTID_AND_MARKETTYPE"),
    REGISTER_PLAYER("注册会员", "/web-root/restricted/player/register-player.aspx", "REGISTER_PLAYER"),
    LOGIN("登录", "/web-root/restricted/player/login.aspx", "LOGIN"),
    UPDATE_PLAYER_STATUS("更新会员状态", "/web-root/restricted/player/update-player-status", "UPDATE_PLAYER_STATUS"),
    DEPOSIT("会员存款", "/web-root/restricted/player/deposit.aspx", "DEPOSIT"),
    WITHDRAW("会员提款", "/web-root/restricted/player/withdraw.aspx", "WITHDRAW"),
    CHECK_TRANSACTION_STATUS("查询存提款交易状态", "/web-root/restricted/player/check-transaction-status.aspx", "checkTransactionStatus"),
    GET_PLAYER_BALANCE("获取会员余额", "/web-root/restricted/player/get-player-balance.aspx", "GET_PLAYER_BALANCE"),
    LOGOUT("会员登出", "/web-root/restricted/player/logout.aspx", "LOGOUT"),

    GET_BET_LIST_BY_TRANSACTION_DATE("根据交易日期获取注单列表", "/web-root/restricted/report/get-bet-list-by-transaction-date.aspx", "GET_BET_LIST_BY_TRANSACTION_DATE"),
    GET_BET_LIST_BY_MODIFY_DATE("根据修改日期获取注单列表", "/web-root/restricted/report/v2/get-bet-list-by-modify-date.aspx", "GET_BET_LIST_BY_MODIFY_DATE"),
    GET_BET_LIST_BY_REFNOS("根据交易编号取得注单列表", "/web-root/restricted/report/get-bet-list-by-refnos", "GET_BET_LIST_BY_REFNOS"),
    GET_FORECAST_PAGE("取得体育Forecast页面链接", "/web-root/restricted/report/v2/get-forecast-page.aspx", "GET_FORECAST_PAGE"),


    GET_LEAGUE("获得联赛", "/web-root/restricted/league/get-league.aspx", "GET_LEAGUE"),
    GET_LAST50_PAGE("取得体育Last50页面链接", "/web-root/restricted/report/v2/get-last50-page.aspx", "GET_LAST50_PAGE"),
    REGISTER_AGENT("注册代理", "/web-root/restricted/agent/register-agent.aspx", "REGISTER_AGENT");


    private String name;
    private String url;
    private String uriKey;

    SBOUrlEnum(String name, String url, String uriKey) {
        this.name = name;
        this.url = url;
        this.uriKey = uriKey;
    }
}
