package com.xinbo.sports.plat.io.bo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author: wells
 * @date: 2020/5/27
 * @description:
 */

public interface BTIRequestParameter {

    @Builder
    @Data
    class Token {
        @JSONField(name = "grant_type")
        private String grantType;
        @JSONField(name = "client_id")
        private String clientId;
        @JSONField(name = "client_secret")
        private String clientSecret;
    }


    @Builder
    @Data
    class Login {

        @JSONField(name = "langCode")
        private String langCode;
        // Game, Tournament, Launchpad
        @JSONField(name = "contentCode")
        private String contentCode;
        // Unknown, Desktop, Mobile
        @JSONField(name = "platform")
        private String platform;

    }


    @Builder
    @Data
    class CreateUser {
        @JSONField(name = "AgentUserName")
        private String agentUserName;
        @JSONField(name = "AgentPassword")
        private String agentPassword;
        @JSONField(name = "MerchantCustomerCode")
        private String merchantCustomerCode;
        @JSONField(name = "LoginName")
        private String loginName;
        @JSONField(name = "CurrencyCode")
        private String currencyCode;
        @JSONField(name = "CountryCode")
        private String countryCode;
        @JSONField(name = "City")
        private String city;
        @JSONField(name = "FirstName")
        private String firstName;
        @JSONField(name = "LastName")
        private String lastName;
        @JSONField(name = "Group1ID")
        private String group1ID;
        @JSONField(name = "CustomerMoreInfo")
        private String customerMoreInfo;
        @JSONField(name = "CustomerDefaultLanguage")
        private String customerDefaultLanguage;
        @JSONField(name = "DomainID")
        private String domainID;
        @JSONField(name = "DateOfBirth")
        private String dateOfBirth;

    }

    @Builder
    @Data
    class GetBalance {
        @JSONField(name = "AgentUserName")
        private String agentUserName;
        @JSONField(name = "AgentPassword")
        private String agentPassword;
        @JSONField(name = "MerchantCustomerCode")
        private String merchantCustomerCode;
    }

    @Builder
    @Data
    class TransferToWHL {
        @JSONField(name = "AgentUserName")
        private String agentUserName;
        @JSONField(name = "AgentPassword")
        private String agentPassword;
        @JSONField(name = "MerchantCustomerCode")
        private String merchantCustomerCode;
        @JSONField(name = "Amount")
        private BigDecimal amount;
        @JSONField(name = "RefTransactionCode")
        private String refTransactionCode;
        @JSONField(name = "BonusCode")
        private String bonusCode;
    }

    @Builder
    @Data
    class TransferFromWHL {
        @JSONField(name = "AgentUserName")
        private String agentUserName;
        @JSONField(name = "AgentPassword")
        private String agentPassword;
        @JSONField(name = "MerchantCustomerCode")
        private String merchantCustomerCode;
        @JSONField(name = "Amount")
        private BigDecimal amount;
        @JSONField(name = "RefTransactionCode")
        private String refTransactionCode;
    }

    @Builder
    @Data
    class CheckTransaction {
        @JSONField(name = "AgentUserName")
        private String agentUserName;
        @JSONField(name = "AgentPassword")
        private String agentPassword;
        @JSONField(name = "RefTransactionCode")
        private String refTransactionCode;
    }


    @Builder
    @Data
    class GetCustomerAuthToken {
        @JSONField(name = "AgentUserName")
        private String agentUserName;
        @JSONField(name = "AgentPassword")
        private String agentPassword;
        @JSONField(name = "MerchantCustomerCode")
        private String MerchantCustomerCode;
    }

    @Builder
    @Data
    class GetAuthenticationToken {
        @JSONField(name = "AgentUserName")
        private String agentUserName;
        @JSONField(name = "AgentPassword")
        private String agentPassword;
    }

    @Builder
    @Data
    class BettingHistory {
        @JSONField(name = "From")
        private String From;
        @JSONField(name = "To")
        private String To;
    }


    @Data
    class BetDetail {
        /**
         * 注单号->PurchaseID
         */
        @JSONField(name = "PurchaseID")
        private Long id;
        @JSONField(name = "Gain")
        private BigDecimal gain;

        @JSONField(name = "pl")
        private BigDecimal pl;

        /**
         * 游戏代码
         */
        @JSONField(name = "NonCashOutAmount")
        private BigDecimal nonCashOutAmount;

        /**
         * 投注时间
         */
        @JSONField(name = "ComboBonusAmount")
        private BigDecimal comboBonusAmount;

        /**
         * 结算时间
         */
        @JSONField(name = "BetSettledDate")
        private String betSettledDate;

        /**
         * 结算时间
         */
        @JSONField(name = "UpdateDate", format = "yyyy-MM-dd'T'HH:mm:sss")
        private Date updateDate;

        @JSONField(name = "Odds")
        private Integer odds;

        /**
         * Odds in user style (American, Decimal, Fractional)
         */
        @JSONField(name = "OddsInUserStyle")
        private String oddsInUserStyle;

        @JSONField(name = "OddsStyleOfUser")
        private String oddsStyleOfUser;

        /**
         * In case of live betting – Score
         * for Home team at the time of
         * the bet
         */
        @JSONField(name = "live_score1")
        private String liveScore1;

        @JSONField(name = "TotalStake")
        private BigDecimal totalStake;

        /**
         * 玩法id
         */
        @JSONField(name = "OddsDec")
        private String oddsDec;

        /**
         * 有效金额
         */
        @JSONField(name = "ValidStake")
        private BigDecimal validStake;



        /**
         * Platform name: Web/Mobile
         */
        @JSONField(name = "Platform")
        private String platform;

        /**
         * 产品代码
         */
        @JSONField(name = "Return")
        private BigDecimal returnAmount;

        /**
         * 单式注单（true,false）
         */
        @JSONField(name = "DomainID")
        private Integer domainId;

        /**
         * 投注状态：half won/lost: Won, Lost, Half won, Half lost, Canceled, Draw, Open
         */
        @JSONField(name = "BetStatus")
        private String betStatus;

        /**
         * 品牌名称
         */
        @JSONField(name = "Brand")
        private String brand;

        /**
         * 用户名
         */
        @JSONField(name = "UserName")
        private String userName;

        /**
         * 投注类型名称
         */
        @JSONField(name = "BetTypeName")
        private String betTypeName;

        /**
         * 投注类型id
         */
        @JSONField(name = "BetTypeId")
        private Integer betTypeId;

        /**
         * 创建时间
         */
        @JSONField(name = "CreationDate", format = "yyyy-MM-dd'T'HH:mm:ss")
        private Date creationDate;

        /**
         * 输赢状态：Won, Lost, Canceled, Draw, Open
         */
        @JSONField(name = "Status")
        private String status;

        /**
         * 客户id
         */
        @JSONField(name = "CustomerID")
        private Integer customerId;

        /**
         * 商户id
         */
        @JSONField(name = "MerchantCustomerID")
        private String merchantCustomerId;

        /**
         * 货币代码
         */
        @JSONField(name = "Currency")
        private String currency;

        /**
         * 用户等级id
         */
        @JSONField(name = "PlayerLevelID")
        private Integer playerLevelId;

        /**
         * 用户等级名称
         */
        @JSONField(name = "PlayerLevelName")
        private String playerLevelName;

        /**
         * 投注选项
         */
        @JSONField(name = "selections")
        private String selections;

    }

    @Getter
    enum CURRENCY {

        RMB("CNY", "人民币"),
        THB("THB", "泰铢"),
        HKD("HKD", "港币"),
        PHP("PHP", "菲律宾披索"),
        VND("VND", "越南盾"),
        IDR("IDR", "印尼盾"),
        KRW("KRW", "韩元"),
        LAK("LAK", "老挝基普"),
        KHR("KHR", "柬埔寨瑞尔"),
        INR("INR", "印度卢比"),
        USD("USD", "美元"),
        MMK("KHR", "缅元"),
        MYR("MYR", "马来西亚币");
        private String code;
        private String desc;

        CURRENCY(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }

    @Getter
    enum LANGS {
        EN("US", "英文"),
        ZH("CN", "中文"),
        VI("VN", "越南语"),
        TH("TH", "泰语"),
        IN("IN", "印度"),
        ;

        private String code;
        private String message;

        LANGS(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }

}
