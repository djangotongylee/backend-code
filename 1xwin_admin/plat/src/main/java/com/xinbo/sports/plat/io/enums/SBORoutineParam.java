package com.xinbo.sports.plat.io.enums;

import com.google.common.collect.ImmutableMap;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.*;

import java.util.Map;

/**
 * @author: David
 * @date: 04/05/2020
 * @description:
 */
public interface SBORoutineParam {
    /**
     * 错误码映射
     */
    Map<Integer, CodeInfo> ERROR_CODE_MAPPER = new ImmutableMap.Builder<Integer, CodeInfo>().
            put(ErrorCode.USER_NAME_ALREADY_EXISTS.getCode(), CodeInfo.PLAT_ACCOUNT_EXISTS).
            put(ErrorCode.USER_NOT_EXISTS.getCode(), CodeInfo.PLAT_ACCOUNT_NOT_EXISTS).
            put(ErrorCode.AMOUNT_NOT_ENOUGH.getCode(), CodeInfo.PLAT_COIN_INSUFFICIENT).
            put(ErrorCode.AMOUNT_NOT_ENOUGH_FOR_ROLLBACK.getCode(), CodeInfo.PLAT_COIN_INSUFFICIENT).
            put(ErrorCode.INVALID_CURRENCY.getCode(), CodeInfo.PLAT_INVALID_CURRENCY).
            put(ErrorCode.INVALID_LANGUAGE.getCode(), CodeInfo.PLAT_INVALID_LANGUAGE).
            put(ErrorCode.INVALID_AGENT.getCode(), CodeInfo.PLAT_INVALID_AGENT_ACCOUNT).
            put(ErrorCode.INVALID_REQUEST_FORMAT.getCode(), CodeInfo.PLAT_INVALID_PARAM).
            put(ErrorCode.INVALID_COMPANY_KEY.getCode(), CodeInfo.PLAT_INVALID_PARAM).
            put(ErrorCode.WITHDRAWAL_TOO_MANY_TIMES.getCode(), CodeInfo.PLAT_REQUEST_FREQUENT).
            put(ErrorCode.INNER_ERROR.getCode(), CodeInfo.PLAT_SYSTEM_ERROR)
            .build();

    @Getter
    enum Lang {
        /**
         * 语言
         */
        EN("en"),
        ZH_TW("zh-tw"),
        ZH_CN("zh-cn"),
        TH_TH("th-th"),
        ID_ID("id-id"),
        JA_JP("ja-jp"),
        VI_VN("vi-vn"),
        DE_DE("de-de"),
        ;

        String value;

        Lang(String value) {
            this.value = value;
        }
    }

    /**
     * Currency	Region	描述
     * AUD	    AU	    地区IP若為AU，货币僅能使用澳元。
     * CAD	    CA	    地区IP若為CA，货币僅能使用加元。
     * CHF	    CH	    地区IP若為CH，货币僅能使用联邦瑞士法郎（瑞郎）。
     * CNY	    CN	    地区IP若為CN，货币僅能使用人民币。
     * <p>
     * GBP	    GB	    地区IP若為GB，货币僅能使用英镑。
     * HKD	    HK	    地区IP若為HK，货币僅能使用港币。
     * IDR	    ID	    地区IP若為ID，货币僅能使用印尼盾。
     * JPY	    JP	    地区IP若為JP，货币僅能使用日圆。
     * <p>
     * KRW	    KR	    地区IP若為KR，货币僅能使用韩元。
     * MMK	    MM	    地区IP若為MM，货币僅能使用缅甸元。
     * MYR	    MY	    地区IP若為MY，货币僅能使用马来西亚令吉。
     * NOK	    NO	    地区IP若為NO，货币僅能使用挪威币。
     * <p>
     * NZD	    NZ	    地区IP若為NZ，货币僅能使用新西兰币
     * SEK	    SE	    地区IP若為SE，货币僅能使用瑞典克朗。
     * THB	    TH	    地区IP若為TH，货币僅能使用泰铢。
     * USD	    PE	    地区IP若為PE，货币僅能使用美元。
     * <p>
     * VND	    VN	    地区IP若為VN，货币僅能使用越南盾。
     * ZAR	    ZA	    地区IP若為ZA，货币僅能使用南非兰特。
     */
    @Getter
    enum Currency {
        AUD("AUD", "AU", "地区IP若為AU，货币僅能使用澳元"),
        CAD("CAD", "CA", "地区IP若為CA，货币僅能使用加元"),
        CHF("CHF", "CH", "地区IP若為CH，货币僅能使用联邦瑞士法郎（瑞郎）"),
        CNY("CNY", "CN", "地区IP若為CN，货币僅能使用人民币"),

        GBP("GBP", "GB", "地区IP若為GB，货币僅能使用英镑"),
        HKD("HKD", "HK", "地区IP若為HK，货币僅能使用港币"),
        IDR("IDR", "ID", "地区IP若為ID，货币僅能使用印尼盾"),
        INR("INR", "IN", "地区IP若為IN，货币僅能使用印度卢比"),
        JPY("JPY", "JP", "地区IP若為JP，货币僅能使用日圆"),

        KRW("KRW", "KR", "地区IP若為KR，货币僅能使用韩元"),
        MMK("MMK", "MM", "地区IP若為MM，货币僅能使用缅甸元"),
        MYR("MYR", "MY", "地区IP若為MY，货币僅能使用马来西亚令吉"),
        NOK("NOK", "NO", "地区IP若為NO，货币僅能使用挪威币"),

        NZD("NZD", "NZ", "地区IP若為NZ，货币僅能使用新西兰币"),
        SEK("SEK", "SE", "地区IP若為SE，货币僅能使用瑞典克朗"),
        THB("THB", "TH", "地区IP若為TH，货币僅能使用泰铢"),
        USD("USD", "PE", "地区IP若為PE，货币僅能使用美元"),

        VND("VND", "VN", "地区IP若為VN，货币僅能使用越南盾"),
        ZAR("ZAR", "ZA", "地区IP若為ZA，货币僅能使用南非兰特");

        /**
         * 币种
         */
        private String code;
        /**
         * Region
         */
        private String region;
        /**
         * 币种说明
         */
        private String desc;

        Currency(String code, String region, String desc) {
            this.code = code;
            this.region = region;
            this.desc = desc;
        }
    }

    enum AgentStatus {
        /**
         * 代理状态
         */
        Active,
        Suspend,
        Closed
    }

    enum PlayerStatus {
        /**
         * 会员状态
         */
        Active,
        Suspend,
        Closed
    }

    enum Portfolio {
        /*体育博彩*/
        SportsBook,
        /*真人赌场*/
        Casino,
        /*电子游戏*/
        Games,
        /*虚拟体育*/
        VirtualSports,
        /*无缝游戏第三方游戏
         * */
        SeamlessGame
    }

    enum GUIDE {
        /*体育博彩*/
        SportsBook("https://{response-url}&lang={lang}&oddstyle={oddstyle}&theme={theme}&oddsmode={oddsmode}&device={device}"),
        /*体育真人赌场*/
        Casino("http://{response-url}&locale={locale}&device={device}&loginMode={loginMode}&productId={productId}"),
        /*体育电子游戏*/
        Game("http://{response-url}&gameId={gameId}");

        private String address;

        GUIDE(String address) {
            this.address = address;
        }

        public String getAddress() {
            return address;
        }

        void setAddress(String address) {
            this.address = address;
        }
    }

    enum SportsBookWap {

    }

    enum Casino {

    }

    enum Game {

    }

    enum Sports {

    }

    /**
     * 错误码_简体中文
     */
    enum ErrorCode {
        NO_ERROR(0, "No Error"),
        INVALID_COMPANY_KEY(1, "无效的Company Key"),
        INVALID_REQUEST_FORMAT(2, "无效的请求格式"),
        INNER_ERROR(3, "内部错误"),
        INVALID_USER_NAME(4, "无效的会员名称"),
        INVALID_COUNTRY(5, "无效的国家"),
        INVALID_LANGUAGE(6, "无效的语言"),
        INVALID_CURRENCY(3101, "无效的币别"),
        INVALID_THEME_ID(3102, "无效的主题Id"),
        CREATE_AGENT_ERROR(3104, "创建代理失败"),
        UPDATE_AGENT_ERROR(3201, "更新状态失败"),
        INVALID_USER_NAME_FOR_UPDATE(3202, "无效的会员名称-更新状态"),
        ALREADY_UPDATED(3203, "已更新状态"),
        INVALID_STATUS(3204, "无效的状态"),
        INVALID_DATEA(3205, "无效的日期"),
        INVALID_LOW_AMOUNT_PER_BET(3206, "无效的单笔注单最低限额"),
        INVALID_HIGN_AMOUNT_PER_BET(3207, "无效的单笔注单最高限额"),
        INVALID_LIMIT_AMOUNT_PER_MATCH(3208, "无效的单场比赛最高限额"),
        INVALID_LIMIT_AMOUNT_LIVE_GAME(3209, "无效的真人赌场下注设定"),
        USER_NOT_EXISTS(3303, "用户不存在"),
        INVALID_AGENT(4101, "无效的代理"),
        CREATE_USER_ERROR(4102, "创建会员失败"),
        USER_NAME_ALREADY_EXISTS(4103, "会员名称存在"),
        VALID_FAILURE(4201, "验证失败"),
        INVAILD_TRANSACTION_ID(4401, "无效的交易Id"),
        INVAILD_TRANSACTION_AMOUNT(4402, "无效的交易金额."),
        TRANSACTION_ERROR(4403, "交易失败"),
        TRANSACTION_ID_REPEAT(4404, "重复使用相同的交易Id"),
        AMOUNT_NOT_ENOUGH(4501, "余额不足"),
        AMOUNT_NOT_ENOUGH_FOR_ROLLBACK(4502, "余额不足导致的回滚(Rollback Transaction)交易"),
        TRANSACTION_STATUS_CHECK_ERROR(4601, "检查交易状态失败"),
        TRANSACTION_NOT_EXISTS(4602, "未找到任何交易"),
        AMOUNT_QUERY_FAILTURE(4701, "获得余额失败"),
        QUERY_PLAYER_REPORT_ERROR(6101, "获取客户报表失败"),
        QUERY_PLAYER_BETLIPS_ERROR(6102, "获取客户注单失败"),
        BETLIPS_NOT_EXISTS(6666, "没有此下注纪录"),
        INVALID_SPORTS_CATEGORY(9527, "无效的运动类型"),
        INVALID_CASINO(9528, "无效的盘口"),
        WITHDRAWAL_TOO_MANY_TIMES(9720, "提款请求次数太过频繁"),
        INVALID_PASSWORD_FORMAT(9721, "无效的密码格式");

        Integer code;
        String message;

        ErrorCode(Integer code, String message) {
            this.code = code;
            this.message = message;
        }

        public Integer getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * 错误码_英文
     */
    enum ErrorCodeEn {
        NO_ERROR(0, "No Error"),
        INVALID_COMPANY_KEY(1, "Invalid Company Key"),
        INVALID_REQUEST_FORMAT(2, "Invalid Request Format"),
        INNER_ERROR(3, "Internal Error"),
        INVALID_USER_NAME(4, "Invalid User Name"),
        INVALID_COUNTRY(5, "Invalid Country"),
        INVALID_LANGUAGE(6, "Invalid Language"),
        INVALID_CURRENCY(3101, "Invalid Currency"),
        INVALID_THEME_ID(3102, "Invalid Theme Id"),
        CREATE_AGENT_ERROR(3104, "Create Agent Failed"),
        UPDATE_AGENT_ERROR(3201, "Update Status Fail"),
        INVALID_USER_NAME_FOR_UPDATE(3202, "Update Status Invalid Username"),
        ALREADY_UPDATED(3203, "Already Update Status"),
        INVALID_STATUS(3204, "Invalid Status"),
        INVALID_DATEA(3205, "Invalid Date"),
        INVALID_LOW_AMOUNT_PER_BET(3206, "Invalid Min Bet"),
        INVALID_HIGN_AMOUNT_PER_BET(3207, "Invalid Max Bet"),
        INVALID_LIMIT_AMOUNT_PER_MATCH(3208, "Invalid Max Per Match"),
        INVALID_LIMIT_AMOUNT_LIVE_GAME(3209, "Invalid Casino Table Limit"),
        INVALID_DOMAIN(3301, "Invalid Domain"),
        CREATE_SUPPORTED_DOMAIN_FAILED(3302, "Create Supported Domain Failed"),
        USER_NOT_EXISTS(3303, "User Doesn't Exist"),
        INVALID_AGENT(4101, "Invalid Agent"),
        CREATE_USER_ERROR(4102, "Create Player Fail"),
        USER_NAME_ALREADY_EXISTS(4103, "User Exists"),
        VALID_FAILURE(4201, "Authentication Fail"),
        INVAILD_TRANSACTION_ID(4401, "Invalid Transaction Id"),
        INVAILD_TRANSACTION_AMOUNT(4402, "Invalid Transaction Amount."),
        TRANSACTION_ERROR(4403, "Transaction Fail"),
        TRANSACTION_ID_REPEAT(4404, "Transaction Has Made With Same Id"),
        AMOUNT_NOT_ENOUGH(4501, "Insufficient Balance"),
        AMOUNT_NOT_ENOUGH_FOR_ROLLBACK(4502, "Rollback Transaction Due To Insufficient Balance"),
        TRANSACTION_STATUS_CHECK_ERROR(4601, "Check Transaction Status Fail"),
        TRANSACTION_NOT_EXISTS(4602, "No Transaction Found"),
        AMOUNT_QUERY_FAILTURE(4701, "Get Balance Fail"),
        FAIL_TO_START_TRADING(5201, "Fail To Start Trading"),
        FAIL_TO_STOP_TRADING(5301, "Fail To Stop Trading"),
        QUERY_PLAYER_REPORT_ERROR(6101, "Get Customer Report Fail"),
        QUERY_PLAYER_BETLIPS_ERROR(6102, "Get Customer BetList Fail"),
        BETLIPS_NOT_EXISTS(6666, "no bet found"),
        INVALID_SPORTS_CATEGORY(9527, "Invalid Sport Type"),
        INVALID_CASINO(9528, "Invalid Market Type"),
        WITHDRAWAL_TOO_MANY_TIMES(9720, "Withdraw request too frequent");

        Integer code;
        String message;

        ErrorCodeEn(Integer code, String message) {
            this.code = code;
            this.message = message;
        }

        public Integer getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }

    enum SportType {
        /**
         * 体育类型
         */
        Unknown(0, "Unknown", "Unknown"),
        Soccer(1, "Soccer", "Football"),
        BasketBall(2, "BasketBall", "BasketBall"),
        Football(3, "Football", "American Football"),
        Ice_Hockey(4, "Ice_Hockey", "Ice Hockey"),
        Badminton(5, "Badminton", "Badminton"),
        Pool(6, "Pool", "Pool/Snooker"),
        Motor_Sport(7, "Motor_Sport", "Motor Sport"),
        Tennis(8, "Tennis", "Tennis"),
        Baseball(9, "Baseball", "Baseball"),
        Volleyball(10, "Volleyball", "Volleyball"),
        Others(11, "Others", "Others"),
        Golf(12, "Golf", "Golf"),
        Boxing(13, "Boxing", "Boxing"),
        Cricket(14, "Cricket", "Cricket"),
        Table_Tennis(15, "Table_Tennis", "Table Tennis"),
        Rugby(16, "Rugby", "Rugby"),
        Handball(17, "Handball", "Handball"),
        Cycling(18, "Cycling", "Cycling"),
        Athletics(19, "Athletics", "Athletics"),
        Beach_Soccer(20, "Beach_Soccer", "Beach Soccer"),
        Futsal(21, "Futsal", "Futsal"),
        Entertainment(22, "Entertainment", "Special"),
        Financial(23, "Financial", "Financial"),
        Darts(24, "Darts", "Darts"),
        Olympic(25, "Olympic", "Olympic"),
        Lacrosse(26, "Lacrosse", "Lacrosse"),
        Water_Polo(27, "Water_Polo", "Water Polo"),
        Winter_Sports(28, "Winter_Sports", "Winter Sports"),
        Squash(29, "Squash", "Squash"),
        Field_Hockey(30, "Field_Hockey", "Field Hockey"),
        Mixed_Martial_Arts(31, "Mixed_Martial_Arts", "Mixed Martial Arts"),
        E_Sports(32, "E_Sports", "E Sports"),
        Gaelic_Football(33, "Gaelic_Football", "Gaelic Football"),
        Hurling(34, "Hurling", "Hurling"),
        Muay_Thai(35, "Muay_Thai", "Muay Thai"),
        Aussie_Rules_Football(36, "Aussie_Rules_Football", "Aussie Rules Football"),
        Bandy(37, "Bandy", "Bandy"),
        Winter_Olympics(38, "Winter_Olympics", "Winter Olympics");

        private Integer id;
        private String value;
        private String name;

        SportType(Integer id, String name, String value) {
            this.id = id;
            this.value = value;
            this.name = name;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class PlatConfig {
        String apiUrl;
        String companyKey;
        String serverId;
        String environment;
        String currency;
        String agentName;
        String agentPwd;
    }
}
