package com.xinbo.sports.service.cache;

/**
 * @author admin
 */
public final class KeyConstant {
    /**
     * HASH 通用KEY
     */
    public static final String COMMON_TOTAL_HASH = "TOTAL";
    /**
     * 用户ID、Channel映射表
     */
    public static final String REL_UID_CHANNEL = "REL_UID_CHANNEL";

    /**
     * COMMON 系统配置相关
     */
    public static final String CONFIG_HASH = "CONFIG_HASH";
    public static final String CONFIG_HASH_API = "CONFIG_HASH_API";
    public static final String CONFIG_HASH_BACKEND = "CONFIG_HASH_BACKEND";
    public static final String CONFIG_HASH_JWT = "JWT";
    public static final String CONFIG_HASH_RSA = "RSA";
    public static final String CONFIG_HASH_INIT_RES = "INIT_RES";
    public static final String CONFIG_HASH_SMS = "SMS";
    public static final String GAME_LIST_ID_2_BET_SLIPS_MAPPER = "GAME_LIST_ID_2_BET_SLIPS_MAPPER";

    /**
     * 用户相关
     */
    public static final String USER_TOKEN_HASH = "USER_TOKEN_HASH";
    public static final String USER_LOGIN_INVALID_TIMES = "USER_LOGIN_INVALID_TIMES";
    public static final String SMS_CODE_HASH = "SMS_CODE_HASH";
    public static final String USER_SUBORDINATE_UID_LIST_HASH = "USER_SUBORDINATE_UID_LIST_HASH";
    public static final String USER_SUBORDINATE6_UID_LIST_HASH = "USER_SUBORDINATE6_UID_LIST_HASH";
    public static final String USER_BASIC_INFO_UID_HASH = "USER_BASIC_INFO_UID_HASH";
    public static final String USER_BASIC_INFO_USERNAME_HASH = "USER_BASIC_INFO_USERNAME_HASH";
    public static final String USER_FLAG_LIST_HASH = "USER_FLAG_LIST_HASH";
    public static final String USER_LEVEL_LIST = "USER_LEVEL_LIST";
    public static final String USER_LEVEL_ID_LIST_HASH = "USER_LEVEL_ID_LIST_HASH";
    public static final String USER_PAY_OFFLINE_HASH = "USER_PAY_OFFLINE_HASH";
    public static final String USER_PAY_ONLINE_HASH = "USER_PAY_ONLINE_HASH";
    public static final String USER_PAYOUT_ONLINE_HASH = "USER_PAYOUT_ONLINE_HASH";
    public static final String USER_NAME_2_UID_HASH = "USER_NAME_2_UID_HASH";

    public static final String USER_HASH = "USER_HASH";
    public static final String TEST_UID_LIST = "TEST_UID_LIST";
    public static final String LEVEL_UID_LIST = "LEVEL_UID_LIST";
    /**
     * Admin相关
     */
    public static final String ADMIN_TOKEN_HASH = "ADMIN_TOKEN_HASH";
    public static final String ADMIN_LOGIN_INVALID_TIMES = "ADMIN_LOGIN_INVALID_TIMES";
    public static final String ADMIN_INFO_ID_HASH = "ADMIN_INFO_ID_HASH";
    public static final String ADMIN_GROUP_ID_HASH = "ADMIN_GROUP_ID_HASH";

    /**
     * 首页平台游戏相关
     */
    public static final String PLATFORM_HASH = "PLATFORM_HASH";
    public static final String PLATFORM_HASH_PLAT_LIST = "PLAT_LIST";
    public static final String PLATFORM_HASH_GAME_LIST = "GAME_LIST";
    public static final String PLATFORM_HASH_GAME_GROUP_LIST = "GAME_GROUP";
    public static final String PLATFORM_HASH_GROUP_GAME_LIST = "GROUP_GAME_LIST";
    public static final String PLATFORM_GAME_PULL_DATA_HASH = "PLATFORM_GAME_PULL_DATA_HASH";

    /**
     * 游戏相关
     */
    public static final String GAME_PROP_HASH = "GAME_PROP_HASH";
    public static final String GAME_LIST_HASH = "GAME_LIST_HASH";
    public static final String PLAT_LIST_HASH = "PLAT_LIST_HASH";
    public static final String GAME_SLOT_HASH = "GAME_SLOT_HASH";
    public static final String PLAT_CONFIG_HASH = "PLAT_CONFIG_HASH";

    public static final String GAME_SLOT_LIST_HASH = "GAME_SLOT_LIST_HASH";

    public static final String BANNER_HASH = "BANNER_HSET";
    public static final String BANK_LIST_HASH = "BANK_LIST_HASH";
    public static final String BANK_LIST_COUNTRY_HASH = "BANK_LIST_COUNTRY_HASH";
    /**
     * 活动相关
     */
    public static final String PROMOTIONS_HASH = "PROMOTIONS_HASH";

    /**
     * 字典相关:api字典
     */
    public static final String DICTIONARY_API_HASH = "DICTIONARY_API_HASH";
    /**
     * 字典相关:backend字典
     */
    public static final String DICTIONARY_BACKEND_HASH = "DICTIONARY_BACKEND_HASH";
    /**
     * VIP用户等级降级
     */
    public static final String USER_RELEGATION = "USER_RELEGATION";
    /**
     * Netty推送缓存KEY
     */
    public static final String NETTY_PUSH_DN = "NETTY_PUSH_DN";
    public static final String NETTY_PUSH_WN = "NETTY_PUSH_WN";
    /**
     * 支付平台配置
     */
    public static final String PAY_PLATFORM_HASH = "PAY_PLATFORM_HASH";
    /**
     * 订阅提款信息
     */
    public static final String SUBSCRIBE_WN = "WN";
    /**
     * 订阅充值信息
     */
    public static final String SUBSCRIBE_DN = "DN";

    /**
     * 公告消息队列
     */
    public static final String NOTIFICATION_QUEUE = "NOTIFICATION_QUEUE";
    /**
     * 存款提款条数
     */
    public static final String DEPOSIT_WITHDRAWAL_QUEUE = "DEPOSIT_WITHDRAWAL_QUEUE";
    public static final String SPORT_SCHEDULE = "_SPORT_SCHEDULE:";

    /**
     * 红包金额缓存key
     */
    public static final String RED_ENVELOPE_HASH = "RED_ENVELOPE";
}
