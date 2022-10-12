package com.xinbo.sports.service.io.constant;


/**
 * <p>
 * 统一常量类
 * </p>
 *
 * @author David
 * @since 2020-02-07
 */
public class ConstData {
    /**
     * 验证码有效周期
     */
    public static final Long SMS_INVALID_MILLI = 3600 * 5 * 1000L;
    public static final Long INVALID_LOGIN_MAX_TIMES = 10L;

    public static final int USER_MAX_BIND_BANK = 3;
    public static final int USER_MAX_BIND_ADDRESS = 3;
    public static final int SLOT_GAME_CATEGORY = 2;
    public static final int SPORT_GAME_CATEGORY = 1;
    public static final String TOKEN_START_WITH = "Bearer ";
    public static final int SMS_PASS_CODE = 888888;
    public static final String JWT_EXP_KEY = "exp";

    /**
     * Header语言
     */
    public static final String LANG = "Accept-Language";
    /**
     * Header设备
     */
    public static final String DEVICE = "Accept-Device";
    /**
     * Token
     */
    public static final String TOKEN = "Authorization";
}

