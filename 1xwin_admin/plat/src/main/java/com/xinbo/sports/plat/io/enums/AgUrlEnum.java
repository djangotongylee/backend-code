package com.xinbo.sports.plat.io.enums;

import lombok.Getter;

/**
 * <p>
 * AG视讯
 * </p>
 *
 * @author andy
 * @since 2020/7/9
 */
@Getter
public enum AgUrlEnum {
    /**
     * 通用请求key
     */
    AGLIVE_DOBUSINESS("doBusiness.do", "params", "key"),
    /**
     * 进入游戏请求方法
     */
    AGLIVE_FORWARDGAME("forwardGame.do", "params", "key"),
    /**
     * 拉单-真人下注紀錄:请求方法
     */
    AGLIVE_GETORDERS("getorders.xml", "params", "key"),

    /**
     * 拉单-開牌結果:请求方法
     */
    AGLIVE_GETROUNDSRES("getroundsres.xml", "params", "key"),

    /**
     * 拉单-获取游戏类型方法
     */
    AGLIVE_GAMETYPES("gametypes.xml", "params", "key"),

    /**
     * 拉单-游戏玩法下注类型
     */
    AGLIVE_GAMEPLAYTYPES("gameplaytypes.xml", "params", "key");

    private String method;
    private String params;
    private String key;

    /**
     * @param method 通用方法
     * @param params params
     * @param key    key
     */
    AgUrlEnum(String method, String params, String key) {
        this.method = method;
        this.params = params;
        this.key = key;
    }
}
