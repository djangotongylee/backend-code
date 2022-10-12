package com.xinbo.sports.service.common;

import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;

/**
 * <p>
 * 统一常量类
 * </p>
 *
 * @author andy
 * @since 2020-02-07
 */
public interface Constant {
    // 银行卡所在国家
    String COUNTRY_THAILAND = "Thailand";

    String INSERT_SUCCESS = "新增成功";
    String UPDATE_SUCCESS = "修改成功";
    String DELETE_SUCCESS = "删除成功";
    String CREATED_AT = "created_at";
    String UPDATED_AT = "updated_at";
    String CATEGORY = "category";
    String STATUS = "status";
    String DEP_STATUS = "dep_status";
    String UID = "uid";
    String ID = "id";
    String USERNAME = "username";
    String USER_ROLE = "role";
    String ROOT = "root";

    String SUPER_ADMIN = "0";

    String TERMINAL_H5 = "H5";
    String TERMINAL_PC = "PC";
    String GAME_ID_FIELD = "gameIdField";
    String ACTION_NO = "actionNo";
    //是
    Integer SUCCESS = 1;
    //否
    Integer FAIL = 0;

    /**
     * 上下方类型:0-上分 1-下分
     */
    int API_CATEGORY_SF = 0;
    /**
     * 上下方类型:0-上分 1-下分
     */
    int API_CATEGORY_XF = 1;
    /**
     * 上下方状态:0-提交申请 1-上分成功 2-上分失败
     */
    int API_TRANSFER_STATUS_SQ = 0;
    /**
     * 上下方状态:0-提交申请 1-上分成功 2-上分失败
     */
    int API_TRANSFER_STATUS_SUCCESS = 1;
    /**
     * 上下方状态:0-提交申请 1-上分成功 2-上分失败
     */
    int API_TRANSFER_STATUS_FAIL = 2;
    /**
     * 类型:1-存款 2-提款 3-上分 4-下分 5-返水 6-佣金 7-活动(奖励)
     */
    int API_COIN_LOG_CATEGORY_CK = 1;
    /**
     * 类型:1-存款 2-提款 3-上分 4-下分 5-返水 6-佣金 7-活动(奖励)
     */
    int API_COIN_LOG_CATEGORY_TK = 2;
    /**
     * 类型:1-存款 2-提款 3-上分 4-下分 5-返水 6-佣金 7-活动(奖励)
     */
    int API_COIN_LOG_CATEGORY_SF = 3;
    /**
     * 类型:1-存款 2-提款 3-上分 4-下分 5-返水 6-佣金 7-活动(奖励)
     */
    int API_COIN_LOG_CATEGORY_XF = 4;
    /**
     * 类型:1-存款 2-提款 3-上分 4-下分 5-返水 6-佣金 7-活动(奖励)
     */
    int API_COIN_LOG_CATEGORY_FS = 5;
    /**
     * 类型:1-存款 2-提款 3-上分 4-下分 5-返水 6-佣金 7-活动(奖励)
     */
    int API_COIN_LOG_CATEGORY_YJ = 6;
    /**
     * 类型:1-存款 2-提款 3-上分 4-下分 5-返水 6-佣金 7-活动(奖励)
     */
    int API_COIN_LOG_CATEGORY_HD = 7;
    /**
     * 类型:0-支出 1-收入
     */
    int API_COIN_LOG_STATUS_ZC = 0;
    /**
     * 类型:0-支出 1-收入
     */
    int API_COIN_LOG_STATUS_SY = 1;
    /**
     * 支付类型:0-离线 1-在线
     */
    int API_COIN_DEPOSIT_PAY_TYPE_OFFLINE = 0;
    /**
     * 支付类型:0-离线 1-在线
     */
    int API_COIN_DEPOSIT_PAY_TYPE_ONLINE = 1;
    /**
     * 一倍流水首存
     */
    Integer FIRST_DEPOSIT_DOUBLE = 1;
    /**
     * 首存超高红利
     */
    Integer FIRST_DEPOSIT_SUPER_BONUS = 2;
    /**
     * 新赛季回归送彩金
     */
    Integer NEW_SEASON_COMEBACK = 3;
    /**
     * 新赛季回归盈利金额
     */
    BigDecimal NEW_SEASON_PROFIT_COIN = BigDecimal.valueOf(5000);
    /**
     * 体育连赢8场
     */
    Integer WIN_8_GAMES_IN_A_ROW = 4;
    /**
     * 整点现金红包雨
     */
    Integer RED_ENVELOPE_RAIN = 5;
    /**
     * 签到拿彩金
     */
    Integer SIGN_IN_BONUS = 6;
    /**
     * 体育连赢场数
     */
    Integer WIN_GAMES_COUNT = 8;
    /**
     * 真人投注送彩金
     */
    Integer LIVE_DAILY_BONUS = 7;
    /**
     * 真人连赢、好运不停
     */
    Integer LIVE_WIN_8_GAMES_IN_A_ROW = 8;
    /**
     * 邀请好友
     */
    Integer INVITE_FRIENDS = 9;
    /**
     * 红包雨时间间隔10s
     */
    int DATA_INTERVAL = 10;
    /**
     * 全场返水
     */
    Integer REBATE_ALL_GAMES = 10;
    /**
     * VIP 会员成长
     */
    Integer VIP_GROW_UP = 11;
    /**
     * 体育尊享VIP
     */
    Integer SPORTS_EXCLUSIVE_VIP = 12;
    /**
     * 投注豪礼(体育)
     */
    Integer BET_LUXURY_GIFT = 13;
    /**
     * 每日首存领福利
     */
    String DAILY_FIRST_DEPOSIT = "Daily first deposit";


    int COIN_BET_BET_TYPE_FD = 5;
    /**
     * 角色:0-会员 1-代理 2-总代理 3-股东 4-测试 10-系统账号'
     */
    int USER_ROLE_HY = 0;
    /**
     * 角色:0-会员 1-代理 2-总代理 3-股东 4-测试 10-系统账号'
     */
    int USER_ROLE_DL = 1;
    /**
     * 角色:0-会员 1-代理 2-总代理 3-股东 4-测试 10-系统账号'
     */
    int USER_ROLE_ZDL = 2;
    /**
     * 角色:0-会员 1-代理 2-总代理 3-股东 4-测试 10-系统账号'
     */
    int USER_ROLE_GD = 3;
    /**
     * 角色:0-会员 1-代理 2-总代理 3-股东 4-测试 10-系统账号'
     */
    int USER_ROLE_CS = 4;
    /**
     * 角色:0-会员 1-代理 2-总代理 3-股东 4-测试 10-系统账号'
     */
    int USER_ROLE_XT = 10;
    /**
     * 升级礼金
     */
    String REWARDS_UPGRADE = "升级彩金";
    /**
     *
     */
    String VIP_RELEGATION = "VIP降级";
    /**
     * 每月红包礼金
     */
    String REWARDS_MONTHLY = "每月红包";
    /**
     * 生日奖励
     */
    String REWARDS_BIRTHDAY = "生日奖励";
    /**
     * 邀请好友返利比例
     */
    BigDecimal INVITE_RATE = BigDecimal.valueOf(0.05);
    /**
     * 体育平台Id
     */
    Integer SPORTS_PLAT_ID = 1;
    /**
     * 稽核成功
     */
    String AUDIT_SUCCESS = "稽核成功";
    /**
     * 稽核失败
     */
    String AUDIT_FAIL = "稽核失败";


    String MANUAL_AUDIT_SUCCESS = "手动稽核成功";

    String ALL_PROMOTIONS = "全部优惠";

    /**
     * 平台bwg05
     */
    String BWG05 = "b05";
    /**
     * 平台bwg01
     */
    String BWG01 = "b01";
    /**
     * FuturesLottery 彩票
     */
    Integer FUTURES_LOTTERY = 703;
    /**
     * 平台bwg05
     */
    String PLAT_PREFIX = "plat_prefix";

    /**
     * 税收类型;WIN_LOSE：输赢，TURNOVER_COIN：有效投注额
     */
    String WIN_LOSE = "winLose";
    String TURNOVER_COIN = "turnoverCoin";
    /**
     * 权限列表
     */
    String PERMISSION_LIST = "PERMISSION_LIST";

    /**
     * 充值标识:1-首充 2-二充 3-三充 9-其他
     */
    int DEP_STATUS_FIRST = 1;
    /**
     * 充值标识:1-首充 2-二充 3-三充 9-其他
     */
    int DEP_STATUS_SECOND = 2;
    /**
     * 充值标识:1-首充 2-二充 3-三充 9-其他
     */
    int DEP_STATUS_THIRD = 3;
    /**
     * 充值标识:1-首充 2-二充 3-三充 9-其他
     */
    int DEP_STATUS_OTHER = 9;

    String SUPPLEMENT_MARK = "投注流水不足,还需金额";

    /**
     * 存款记录
     */
    String PUSH_DN = "PUSH_DN";
    /**
     * 提款记录
     */
    String PUSH_WN = "PUSH_WN";

    /**
     * 后台
     */
    String ADMIN = "admin";
    /**
     * 前端
     */
    String WEB = "web";
    /**
     * 用户退出
     */
    String USER_QUIT = "USER_QUIT";
    /**
     * 退出标识
     */
    String LOGOUT = "logout";
    /**
     * 手机号码
     */
    String MOBILE = "mobile";
    /**
     * 真实名称
     */
    String REAL_NAME = "realName";
    /**
     * 银行卡号
     */
    String BANK_ACCOUNT = "bankAccount";
    /**
     * 代理佣金，流水佣金
     */
    String AGENT = "agent";
    String FLOW = "flow";

    //佣金规则
    String COMMISSION_RULE = "commission_rule";
    String SETTLE_TYPE = "settleType";
    String EFFECT_TYPE = "effectType";
    String OLD_SETTLE_TYPE = "oldSettleType";
    String OLD_EFFECT_TYPE = "oldEffectType";
    String EFFECT_DATE = "effectDate";

    /**
     * 生效方式：0-次月生效，1-立即生效（次日）
     */
    Integer EFFECT_MONTH = 0;
    Integer EFFECT_DAY = 1;
    /**
     * 结算方式：0-自然天结算，1-自然周结算，2-自然月结算
     */

    Integer SETTLE_DAY = 0;
    Integer SETTLE_WEEK = 1;
    Integer SETTLE_MONTH = 2;
    //额外打码量一次生效
    Integer CODE_ONE = 0;

    /**
     * 注册登录配置
     */
    String REGISTER_LOGIN_CONFIG = "register_login_config";

    //手机号码：0-显示，1-不显示
    String REGISTER_MOBILE = "registerMobile";
    //验证码：0-显示，1-不显示
    String REGISTER_VERIFICATION_CODE = "registerVerificationCode";
    //邀请码：0-显示(选填)，1-显示(必填)，2-不显示
    String REGISTER_INVITE_CODE = "registerInviteCode";
    //登录方式：0-用户名登录，1-手机号登录
    String LOGIN_TYPE = "loginType";
    //BIT
    Integer BIT = 102;
    //验证码是否验证
    Integer IS_CHECK_CODE = 0;
    //必填
    Integer IS_CHECK_REQUIRED = 1;
    //领取
    Integer IS_RECEIVE = 1;
    //红包标识
    String RED_ENVELOPE = "Red Envelope";
    String RED_ENVELOPE_ACTIVITY = "redEnvelopeActivity";

    //用户注册活动标识
    String REGISTER_BONUS = "New Register Get Bonus";
    //自动派彩
    Integer AUTO_PAY = 0;
    //自动派彩
    Integer MANUAL_PAY = 2;
    //派彩金额
    String PAY_COIN = "payCoin";
    //要求的流水金额
    String FLOW_CLAIN = "flowClaim";
    //谷歌验证配置
    String VERIFICATION_OF_GOOGLE = "verificationOfGoogle";
    //显示谷歌验证码
    String VERIFICATION_SHOW = "1";
    //平台名称
    String TITLE = "title";
    //平台logo
    String PLAT_LOGO = "platLogo";
    //语言
    String LANG = "lang";
    //静态服务器
    String STATIC_SERVER = "static_server";
    //URL
    String URL = "url";
    //Socket地址
    String WS_SERVER = "ws_server";
    //短息发送
    String SMS = "sms";
    //app首页下载地址
    String DOWNLOAD = "download";
    //app首页下载是否展示；0-不显示，1-显示
    String DOWNLOAD_SHOW = "downloadShow";
    //app首页下载Logo的URL
    String DOWNLOAD_LOGO = "downloadLogo";
    //提款配置
    String MIN_DRAW_COIN = "MinDrawCoin";
    //提款最低金额
    String MIN_COIN = "minCoin";
    //快捷金额
    String COIN_RANGE = "coinRange";
    //在线客服
    String ONLINE_SERVER = "online_server";
    String PC = "pc";
    String H5 = "h5";
    //客服邮箱
    String MAIL_KEFU = "mail_kefu";
    //帮助邮箱
    String MAIL_HELP = "mail_Help";
    //投诉邮箱
    String MAIL_SUGGEST = "mail_Suggest";
    //合营部Telegram
    String TELEGRAM = "telegram";
    //合营部Skype
    String SKYPE = "skype";
    //合营部line
    String LINE = "line";
    //合营部Whatsapp
    String WHATSAPP = "whatsapp";
    //合营部TelegramUrl
    String TELEGRAM_URL = "telegramUrl";
    //合营部SkypeUrl
    String SKYPE_URL = "skypeUrl";
    //合营部lineUrl
    String LINE_URL = "lineUrl";
    //合营部WhatsappUrl
    String WHATSAPP_URL = "whatsappUrl";
    //line二维码地址
    String QRCODE = "qrcode";
    //清空
    String CLEAR = "clear";
}

