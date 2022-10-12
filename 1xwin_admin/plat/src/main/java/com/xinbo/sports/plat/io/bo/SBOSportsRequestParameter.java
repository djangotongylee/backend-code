package com.xinbo.sports.plat.io.bo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author: David
 * @date: 02/05/2020
 * @description: SBO_BET 利记平台api接口入参
 */
public interface SBOSportsRequestParameter {

    /**
     * SBO注册代理入参实体类
     */
    @Data
    @Builder
    class RegisterAgent {
        @JSONField(name = "Username")
        private String username;
        @JSONField(name = "Password")
        private String password;
        @JSONField(name = "Currency")
        private String currency;
        @JSONField(name = "Min")
        private Integer min;
        @JSONField(name = "Max")
        private Integer max;
        @JSONField(name = "MaxPerMatch")
        private Integer maxPerMatch;
        @JSONField(name = "CasinoTableLimit")
        private Integer casinoTableLimit;
        @JSONField(name = "CompanyKey")
        private String companyKey;
        @JSONField(name = "ServerId")
        private String serverId;
    }

    /**
     * 更新代理状态
     */
    @Data
    @Builder
    class UpdateAgentStatus {
        @JSONField(name = "Username")
        private String username;
        /*Active / Suspend / Closed*/
        @JSONField(name = "Status")
        private String status;
    }

    /**
     * 更新代理预设投注设置
     */
    @Data
    @Builder
    class UpdateAgentPresetBetSettings {
        @JSONField(name = "Username")
        private String username;
        @JSONField(name = "Min")
        private Integer min;
        @JSONField(name = "Max")
        private Integer max;
        @JSONField(name = "MaxPerMatch")
        private Integer maxPerMatch;
        @JSONField(name = "CasinoTableLimit")
        private Integer casinoTableLimit;
    }

    /**
     * 通过体育ID和市场类型更新Agent预设投注设置
     */
    @Data
    @Builder
    class UpdateAgentPresetBetSettingBySportidAndMarkettype {
        @JSONField(name = "Username")
        private String username;
        @JSONField(name = "BetSettings")
        private List<BetSettings> betSettings;
    }

    /**
     * 投注设置
     */
    @Data
    @Builder
    class BetSettings {
        @JSONField(name = "sport_type")
        private Integer sportType;
        @JSONField(name = "market_type")
        private Integer marketType;
        @JSONField(name = "min_bet")
        private Integer minBet;
        @JSONField(name = "max_bet")
        private Integer maxBet;
        @JSONField(name = "max_bet_per_match")
        private Integer maxBetPerMatch;
    }

    /**
     * 注册会员
     */
    @Data
    @Builder
    class RegisterPlayer {
        @JSONField(name = "Username")
        private String username;
        @JSONField(name = "Agent")
        private String agent;
    }


    /**
     * 更新会员状态
     */
    @Data
    @Builder
    class UpdatePlayerStatus {
        @JSONField(name = "Username")
        private String username;
        @JSONField(name = "Status")
        private String status;
    }

    /**
     * 会员存款
     */
    @Data
    @Builder
    class Deposit {
        /*用户名*/
        @JSONField(name = "Username")
        private String username;
        /**/
        @JSONField(name = "TxnId")
        private String txnId;
        /**/
        @JSONField(name = "Amount")
        private BigDecimal amount;
    }

    /**
     * 会员提款
     */
    @Data
    @Builder
    class Withdraw {
        @JSONField(name = "Username")
        private String username;
        @JSONField(name = "TxnId")
        private String txnId;
        @JSONField(name = "IsFullAmount")
        private Boolean isFullAmount;
        @JSONField(name = "Amount")
        private BigDecimal amount;
    }


    @Data
    @Builder
    class GetPlayerBalance {
        @JSONField(name = "Username")
        private String username;
    }


    @Data
    @Builder
    class Logout {
        @JSONField(name = "Username")
        private String username;
    }

    /**
     * 登录
     */
    @Data
    @Builder
    class Login {
        @JSONField(name = "Username")
        private String username;
        @JSONField(name = "Portfolio")
        private String portfolio;
        @JSONField(name = "IsWapSports")
        private boolean isWapSports;
    }


    /**
     * 获得联赛
     */
    @Data
    @Builder
    class GetLeague {
        @JSONField(name = "LeagueNameKeyWord")
        private String leagueNameKeyWord;
        @JSONField(name = "FromDate")
        private String fromDate;
        @JSONField(name = "ToDate")
        private String toDate;
        @JSONField(name = "SportType")
        private Integer sportType;
    }


    /**
     * 根据交易日期获取注单列表
     */
    @Data
    @Builder
    class GetBetListByTransactionDate {
        @JSONField(name = "Username")
        private String username;
        @JSONField(name = "Portfolio")
        private String portfolio;
        @JSONField(name = "StartDate")
        private LocalDateTime startDate;
        @JSONField(name = "EndDate")
        private LocalDateTime endDate;
    }


    /**
     * 根据修改日期获取注单列表
     */
    @Data
    @Builder
    class GetBetListByModifyDate {
        @JSONField(name = "Portfolio")
        private String portfolio;
        @JSONField(name = "StartDate")
        //private LocalDateTime startDate;
        private String startDate;
        @JSONField(name = "EndDate")
        private String endDate;
    }


    /**
     * 根据交易编号取得注单列表
     */
    @Data
    @Builder
    class GetBetListByRefnos {
        @JSONField(name = "RefNos")
        private String refNos;
        @JSONField(name = "Portfolio")
        private String portfolio;
    }


    /**
     * 取得体育Forecast页面链接
     */
    @Data
    @Builder
    class GetForecastPage {
    }


    /**
     * 取得体育Last50页面链接
     */
    @Data
    @Builder
    class GetLast50Page {
    }

    @Data
    @Builder
    class SportBook {
        /*zh-cn=>简体中文、en=>英文*/
        private String lang;
        private String oddStyle;
        private String theme;
        private String oddSMode;
        /*d=>desktop   m=> mobile*/
        private String device;
    }

    @Data
    @Builder
    class Casino {
        /*zh-cn=>简体中文、en=>英文*/
        private String locale;
        /*d=>desktop   m=> mobile*/
        private String device;
        private String loginMode;
        private String productId;

    }

    /**
     * SBO 体育登录参数
     */
    @Data
    @Builder
    class SBOSportsLoginReqParam {
        /*zh-cn=>简体中文、en=>英文*/
        private String lang;
        private String oddStyle;
        private String theme;
        private String oddSMode;
        /*d=>desktop   m=> mobile*/
        private String device;
    }

    @Data
    @Builder
    class Games {
        /*游戏类型ID*/
        private String gameId;
    }

}
