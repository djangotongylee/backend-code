package com.xinbo.sports.utils.components.response;

import lombok.Getter;

/**
 * @author: David
 * @date: 06/04/2020
 * @description: 总异常状态码
 */
@Getter
public enum CodeInfo {
    // 默认状态码
    STATUS_CODE_ERROR(-1, "system error"),
    STATUS_CODE_SUCCESS(0, "success"),
    STATUS_CODE_400(400, "bad request"),
    STATUS_CODE_401(401, "Invalid Token"),
    STATUS_CODE_401_2(401, "TOKEN has expired"),
    STATUS_CODE_403(403, "forbidden"),
    STATUS_CODE_403_6(403, "IP address rejected"),
    STATUS_CODE_404(404, "Not Found"),
    STATUS_CODE_405(405, "Resources are forbidden"),
    STATUS_CODE_408(408, "Request timed out"),
    STATUS_CODE_500(500, "Internal server error, please contact the administrator"),

    // 请求统一异常
    PARAMETERS_INVALID(10000, "Parameters Invalid"),
    DEFAULT_ERROR(10001, "Default definition error method"),
    HEADER_LANG_ERROR(10002, "Invalid language"),
    HEADER_DEVICE_ERROR(10003, "Invalid device source"),
    IP_ACCESS_OVER_LIMIT(10004, "IP access limit exceeded"),
    REPEATED_SUBMIT(10005, "Repeated submit"),
    ORDER_ID_INVALID(10006, "Invalid order number"),
    CONFIG_INVALID(10007, "Configuration error"),
    RES_CODE_PROCESSING(10008, "processing"),

    /***************** 用户模块 20000 - 29999 ******************/
    // [账户 20100 - 20199]
    ACCOUNT_EXISTS(20100, "Username already exists"),
    ACCOUNT_NOT_EXISTS(20102, "Username no exists"),
    ACCOUNT_ERROR(20105, "Username/UID no exists or no match"),
    ACCOUNT_FROZEN(20103, "Account is frozen"),
    ACCOUNT_STATUS_INVALID(20104, "Account status is abnormal"),
    VERIFICATION_CODE_INVALID(20105, "Verification Code no exists"),
    // [地址 20200 - 20299]
    ADDRESS_INVALID(20201, "Invalid address"),
    ADDRESS_MAX_LIMIT(20202, "The address has reached the limit"),
    // [银行卡 20300 - 20349]
    BANK_INVALID(20301, "Invalid bankcard"),
    BANK_ADD_ERROR(20302, "Fail to add user bank card"),
    BANK_UPDATE_ERROR(20303, "Fail to update user bank card"),
    BANKCARD_ALREADY_EXIST(20305, "bankcard already exists"),
    BANK_BIND_OVER_LIMIT(20304, "one user can maximum bind 3 bank card accounts"),
    BANK_ACCOUNT_NAME_NOT_NULL(20306, "the account's name can't be empty"),
    // [提现 20350 - 20399]
    WITHDRAWAL_NUMS_MAX_LIMIT(20350, "The number of withdrawals (daily) has reached the upper limit"),
    WITHDRAWAL_TOTAL_COIN_MAX_LIMIT(20351, "The withdrawal limit has reached the upper limit"),
    WITHDRAWAL_ALREADY_AUDIT(20352, "withdrawal already audit"),
    PAYOUT_APPLYING(20353, "withdrawal still in processing"),
    MINIMUM_SINGLE_WITHDRAW(20354, "Minimum single withdrawal"),
    MINIMUM_SINGLE_EXCEPTION(20355, "Minimum single withdrawal"),

    // [密码 20400 - 20499]
    PASSWORD_INVALID(20401, "INVALID PASSWORD"),
    PASSWORD_EMPTY(20402, "No password has been set"),
    // [手机 20500 - 20599]
    MOBILE_INVALID(20501, "Invalid mobile phone number"),
    MOBILE_EXISTS(20502, "Phone number already exists"),
    // [推广码 20600 - 20699]
    PROMO_CODE_INVALID(20601, "Share Code Invalid"),
    PROMO_CODE_GEN_INVALID(20602, "Share Code generation error"),
    // [金额 20700 - 20799]
    COIN_TRANSFER_INVALID(20701, "Transaction Error"),
    COIN_TRANSFER_SF_ERROR(20702, "Deposit Fail"),
    COIN_TRANSFER_XF_ERROR(20703, "Withdraw Fail"),
    COIN_QUERY_INVALID(20704, "Query Balance error"),
    COIN_MODIFY_ERROR(20705, "User balance update failed"),
    COIN_NOT_ENOUGH(20706, "Insufficient balance"),
    COIN_TRANSFER_OVER_LIMIT(20707, "Amount at less 1"),
    COIN_TRANSFER_EXCEED(20708, "The deposit amount is less than the minimum OR greater than the maximum"),
    // [登录/登出 20800 - 20899]
    LOGIN_INVALID(20801, "Login exception"),
    LOGIN_INVALID_OVER_LIMIT(20802, "Too many login errors, please contact the administrator or retrieve your password"),
    LOGOUT_INVALID(20803, "User logout failed"),
    TRY_AGAIN(20804, "pls try again after 3 seconds"),
    // [注册 20900 - 20999]
    REGISTER_INVALID(20902, "Registration error"),
    // [会员旗 21000 - 21099]
    FLAG_UPDATE_ERROR(21001, "Member flag modification error"),
    FLAG_LIST_ERROR(21002, "Member flag list query error"),
    FLAG_DICT_ERROR(21003, "Member dictionary query error"),
    FLAG_ADD_INVALID(21003, "Member flag added error"),
    FLAG_DELETE_ERROR(21004, "Member flag deletion error"),
    FLAG_QUERY_ERROR(21005, "Member flag query error"),
    // [会员登记 21100 - 21199]
    USER_LEVEL_LIST_ERROR(21100, "Membership level query failed"),
    USER_LEVEL_UPDATE_ERROR(21202, "Membership level modification failed"),
    UPDATE_ERROR(21203, "modification failed"),
    BINDING_MOBILE_NUMBER(21204, "Please bind your mobile phone number first"),

    /***************** SMS 30000 - 30099 ******************/
    SMS_SENT_TOO_MANY_TIME(30000, "Maximum send up to 5 items per hour"),
    SMS_SENT_OVER_COUNT(30001, "Maximum send up to 10 items per day"),
    SMS_SENT_ERROR(30002, "Sent error"),
    SMS_INVALID(30003, "Sms Code Invalid"),
    SMS_INVALID_NUMBER(30004, "Incorrect phone number format"),

    /***************** 优惠活动 30100 - 30199 ******************/
    ACTIVE_APPLICATION_SUCCESS(30100, "Successful apply!！"),
    ACTIVE_PROFIT_NO_ENOUGH(30101, "The amount of profit or negative profit is not enough！"),
    ACTIVE_CLOSE(30102, "The event is closed！"),
    ACTIVE_ALREADY_SIGN_IN(30103, "Checked in!"),
    ACTIVE_NO_FIRST_DEPOSIT(30104, "No first deposit record!!"),
    ACTIVE_NO_SPORTS_WIN_8_GAMES(30105, "No sports record for 8 consecutive wins!!"),
    ACTIVE_NO_RED_ENVELOPE_RAIN(30106, "Unable to receive red envelope rain!"),
    ACTIVE_NO_ENVELOPE_COUNT(30107, "Not enough times!"),
    ACTIVE_RECEIVE_ENVELOPE_RAIN(30108, "Hourly cash red envelope rain bonus has been received!!"),
    ACTIVE_NO_USER(30109, "No member information!!"),
    ACTIVE_BIRTHDAY_DATE_EXCEED(30110, "Member registration time is less than three months!"),
    ACTIVE_NO_IN_BIRTHDAY_DATE(30111, "Today is not your birthday, you cannot receive the gift!"),
    ACTIVE_NO_USER_LEVEL(30112, "Not qualify membership level!"),
    ACTIVE_LEVEL_GIFT_RECEIVE(30112, "The birthday gift has been received!!"),
    ACTIVE_NO_USER_ADDRESS(30113, "The personal address is not complete, please complete your personal information!"),
    ACTIVE_NO_GIFT_NUMS(30114, "Not enough gift packs!"),
    ACTIVE_NO_GIFT_LIST(30115, "No grand prize record!"),
    ACTIVE_NO_GIFT_COIN(30116, "No prizes can be claimed!!"),
    ACTIVE_BIRTHDAY_DATE_EMPTY(30117, "The user's birthday is empty, please complete it!"),
    ACTIVE_GIFT_IS_RECEIVE(30118, "Grand prize has been received!"),
    ACTIVE_BET_COIN_NO_ENOUGH(30119, "Insufficient valid bet amount!"),
    ACTIVE_RECEIVE_IN_NOT_BET(30120, "Please collect it before betting！"),
    ACTIVE_NEW_SEASON_NO_RECORD(30120, "No deposit record during the season!"),
    ACTIVE_COIN_ALREADY_RECEIVE(30121, "The winnings have been claimed!"),
    ACTIVE_NO_REACH_REQUIREMENT(30122, "The first deposit amount didn't reach the bonus requirement!"),
    NO_ACTIVE_GROUP_RECORD(30123, "No activity group record!"),
    NO_ACTIVE_RECORD(30124, "No activity record!!"),
    ACTIVE_FLAG_REPEAT(30125, "Event logo can not be repeated!!"),
    ACTIVE_CODE_REPEAT(30126, "Active code can not be repeated!！"),
    PICTURE_CODE_REPEAT(30127, "The picture logo can not be repeated!!"),
    PICTURE_NEED_BIND_PHONE(30128, "This activity needs to bind a mobile phone number!"),
    RED_ENVELOPE_FLOW_NO_ENOUGH(30129, "The effective betting flow of the day has not been met. Come on!"),
    RED_ENVELOPE_NUMBER_FINISH(30130, "The number of the day has been finished!"),

    /***************** 游戏平台相关 39000 - 39999 ******************/
    // 本平台异常
    GAME_NOT_EXISTS(39001, "Game no exist"),
    GAME_NOT_OPEN(39002, "The game is close for maintenance, please wait patiently"),
    GAME_UNDER_MAINTENANCE(39003, "The game is under maintenance"),
    GAME_SLOT_FAVORITE_ALREADY(39004, "Already in favorite list"),
    GAME_SLOT_FAVORITE_NOT_YET(39005, "Please collect to the favorite list first"),
    GAME_RECORDS_EMPTY(39006, "No such game record!"),
    PLAT_FACTORY_NOT_EXISTS(39007, "Platform factory does not exist"),
    PLAT_UNSUPPORTED_OPERATION(60017, "Plat not support this operate"),
    PLAT_TRANSFER_SUPPLE_INVALID(60018, "Transfer order error"),
    GAME_LIST_ONLY_SUPPORT_SLOT(60019, "Non-video games cannot be operated"),
    PLAT_BET_SLIPS_SUPPLE_OVER_7_DAYS(69901, "Replenishment time cannot exceed 7 days"),
    PLAT_BET_SLIPS_SUPPLE_OVER_10_DAYS(69902, "Replenishment time cannot exceed 10 days"),
    PLAT_BET_SLIPS_SUPPLE_OVER_14_DAYS(69903, "Replenishment time cannot exceed 14 days"),
    PLAT_BET_SLIPS_SUPPLE_OVER_30_DAYS(69904, "Replenishment time cannot exceed 30 days"),
    PLAT_BET_SLIPS_SUPPLE_OVER_60_DAYS(69905, "Replenishment time cannot exceed 60 days"),
    PLAT_BET_SLIPS_SUPPLE_OVER_90_DAYS(69906, "Replenishment time cannot exceed 90 days"),
    PLAT_BET_SLIPS_SUPPLE_OVER_180_DAYS(69907, "Replenishment time cannot exceed 180 days"),
    // 三方平台映射异常
    PLAT_INVALID_PARAM(39901, "Invalid Parameter "),
    PLAT_INVALID_OPERATOR(39902, "Illegal Operation"),
    PLAT_IP_NOT_ACCESS(39903, "IP is forbidden to access"),
    PLAT_UNDER_MAINTENANCE(39904, "platform is under maintain or platformID no exist"),
    PLAT_CONNECTION_REFUSED(39905, "Connection Rejection"),
    PLAT_CONNECTION_EXCEPTION(39906, "Connection error"),
    PLAT_TIME_OUT(39907, "Connection timeout"),
    PLAT_REQUEST_FREQUENT(39908, "Request too frequent"),
    PLAT_INVALID_CURRENCY(39909, "Invalid currency type"),
    PLAT_INVALID_LANGUAGE(39910, "Invalid language type"),
    PLAT_INVALID_AGENT_ACCOUNT(39911, "Invalid Agent"),
    PLAT_ID_OCCUPATION(39912, "ID Occupation"),
    PLAT_ACCOUNT_EXISTS(39913, "Account already exists"),
    PLAT_ACCOUNT_NOT_EXISTS(39914, "Account no exists"),
    PLAT_ACCOUNT_OCCUPATION(39915, "Account is locked"),
    PLAT_ACCOUNT_NO_MODIFIED(66108, "Has been used cannot be modified"),
    PLAT_ACCOUNT_NO_DELETE(66110, "Has been used cannot be delete"),
    //该异常码与特殊约定，不可更改
    PLAT_GAME_UNDER_MAINTENANCE(39916, "platform is under maintain"),

    // 上下分、查询余额
    PLAT_COIN_INSUFFICIENT(39916, "Insufficient balance"),
    PLAT_PLAT_NO_SUFFICIENT(39917, "Insufficient platform points"),
    PLAT_COIN_OVER_MAX_LIMIT(39918, "Amount exceeds the maximum limit"),
    PLAT_COIN_BELOW_MIN_LIMIT(39919, "Amount exceeds the minimum limit"),
    PLAT_TRANSFER_ID_INVALID(39920, "Order number does not exist"),
    // PLAT 系统异常码
    PLAT_REGISTER_USER_ERROR(39921, "Registration failed"),
    PLAT_PULL_GAME_LIST_ERROR(39922, "Error getting game list"),
    PLAT_SEND_PARAMS_INVALID(39923, "The platform sends the parameter error"),
    PLAT_COIN_UP_COIN_NOT_GT_100(39924, "The deposit amount can't over 100 in the staging environment"),
    PLAT_LOGOUT_ERROR(39925, "Logout Error"),
    PLAT_TRANSFER_CHECK_ORDER_INVALID(39926, "Order no exist"),
    PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0(39927, "Third party error"),
    PLAT_BET_SLIPS_EXCEPTION_CATEGORY_1(39928, "Data error"),
    // 预设开奖
    PLAT_LOTTERY_ACTION_NO_EXPIRED(40004, "issue expired"),
    PLAT_TIME_LIMIT_3_DAYS(40005, "The Time Interval limit for 3 days!"),
    // PLAT 统一映射异常码
    PLAT_SYSTEM_ERROR(39999, "Internal server error, please contact the administrator"),
    PLAT_NO_AUTHORITIES_TO_ACCESS(39988, "no authorities to access"),
    /***************** PAYMENT 40000 - 49999 ******************/
    PAY_USER_INVALID(40000, "User no exist"),
    PAY_PAY_ID_INVALID(40001, "Payment method error"),
    PAY_PLAT_INVALID(40002, "Payment platform error"),
    PAY_PLAT_LOAD_INVALID(40003, "Payment loading error"),
    PAY_PLAT_ORDER_ID_INVALID(40003, "Invalid order ID"),
    PAY_PLAT_UPDATE_ORDER_ID_INVALID(40004, "Fail to update order number"),
    PAY_PLAT_ADD_ORDER_FAILURE(40005, "Fail to add withdrawal application"),
    PAY_PLAT_UPDATE_ORDER_FAILURE(40005, "Fail to update withdrawal application"),

    /***************** APIEND 50000 - 59999 ******************/
    API_CENTER_AGENT_REPORTS_ERROR(50001, "Agent Center-My Report-Statistics Failed"),
    API_CENTER_AGENT_SUBORDINATE_LIST_ERROR(50002, "Agency Center-My Subordinate-List Query Failed"),
    API_CENTER_AGENT_SUBORDINATE_LIST_ZT_ERROR(50003, "Agency Center-My Subordinate-Direct Push List Query Failed"),
    API_CENTER_AGENT_SUBORDINATE_STATISTICS_ERROR(50004, "Agent Center-My Subordinates-Statistics Failed"),
    API_RECORDS_NOT_EXISTS(50005, "Record does not exist"),
    FORWARD_GAME_IS_ERROR(50006, "Get sessionId exception"),
    API_WITHDRAWAL_CHECK_INFO(5006, ""),

    /***************** BACKEND 60000 - 69999 ******************/
    USER_ADD_ERROR(60000, "Member fail to add"),
    USER_UPDATE_ERROR(60001, "Member modification fail"),
    USER_SUP_ACCOUNT_NOT_EXISTS(60002, "The superior agent does not exist"),
    USER_ACCOUNT_SUP_NOT_SAME(60003, "The user name and the superior agent cannot be the same"),
    USER_CAPITAL_STATISTICS_LIST_ERROR(60004, "Fund statistics fai"),
    USER_TRANSACTION_RECORD_LIST_ERROR(60005, "Transaction record query fail"),
    USER_TRANSACTION_RECORD_STATISTICS_ERROR(60006, "Transaction record fund statistics fail"),
    USER_TRANSFER_RECORD_LIST_ERROR(60007, "Query transfer record fail"),
    ROUTE_TRANSFER_SOURCE_NOT_EXISTS(60008, "route source no exists"),
    ROUTE_TRANSFER_TARGET_NOT_EXISTS(60009, "route target no exists"),
    ROUTE_TRANSFER_SUP_UN2_CHILD(60010, "The superior cannot transfer to the lower"),
    ROUTE_TRANSFER_ERROR(60011, "Route transfer failed"),
    ROUTE_TRANSFER_NOT_SAME(60012, "The route to be transferred cannot be the same as the target route"),
    USER_LIST_ERROR(60013, "User list query failed"),
    USER_WITHDRAWAL_OVER_LIMIT(60014, "There are many outstanding withdrawals！"),
    USER_NO_PASS_AUDIT(60015, "Can't withdraw  if the audit didn't finish！"),
    USER_NOT_EXISTS(60016, "Member no exist"),
    RECORD_NOT_EXIST(60017, "Record no exist"),
    ADMIN_GROUP_EXIST(60018, "admin group exist"),
    ROLE_DELETE_VERIFICATION(60019, "This role has been associated with users, if you want to delete, please remove all users corresponding to the role!"),
    ADMIN_GROUP_DELETE_VERIFICATION(60020, "This group has been associated with users. If you need to delete it, please remove all users corresponding to the roles in the group!"),
    NO_AUTH_TO_GET_RULES(60021, "no auth to get rules"),
    AGENT_GROUP_NO_UPDATE(60022, "agent group don't update"),
    ROLE_ALREADY_EXIST(60018, "role already exist!"),

    OPEN_PRESET_NUMS_INVALID(60018, "predictable number over range,range in[1-480]"),
    REPORT_TEST_USER_NOT_SEARCH_DATA(60019, "Test account cannot inquiry report data"),

    /***************** 图片上传 70000 - 70100 ******************/
    UPLOAD_PLEASE_CHOSE_IMAGE(70000, "please upload the image"),
    UPLOAD_IMAGE_IS_NOT_NULL(70001, "please choose the image"),
    UPLOAD_IMAGE_SIZE_NOT_GT_2M(70002, "upload less than 2M image please"),

    ;


    private String msg;
    private Integer code;

     CodeInfo(Integer code, String msg) {
        this.msg = msg;
        this.code = code;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
