package com.xinbo.sports.plat.io.bo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author: David
 * @date: 02/05/2020
 * @description: SBO_BET 利记平台api接口出参
 */
public interface SBOSportsResponseParameter {

    /**
     * SBO注册代理入参实体类
     */
    @Data
    class RegisterAgent {

    }

    /**
     * 更新代理状态
     */
    @Data
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
    class Deposit {
        private String txnId;
        private String refno;
        private BigDecimal balance;
        private BigDecimal outstanding;
    }

    /**
     * 会员提款
     */
    @Data
    class Withdraw {
        /*提款金额*/
        private BigDecimal amount;
        private String txnId;
        private String refno;
        private BigDecimal balance;
        private BigDecimal outstanding;
    }


    /**
     * 获取会员余额
     */
    @Data
    class GetPlayerBalance {
        private String username;
        private String currency;
        private BigDecimal balance;
        private BigDecimal outstanding;
    }


    @Data
    class Logout {
        @JSONField(name = "Username")
        private String username;
    }

    /**
     * 登录
     */
    @Data
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
    class GetLeague {
        private String league_id;
        private String league_name;
    }


    /**
     * 根据交易日期获取注单列表
     */
    @Data
    class GetBetListByTransactionDate {
        private Integer id;

        /**
         * UID
         */
        private Integer uid;

        /**
         * 用户名
         */
        private String username;

        /**
         * 投注的体育类型
         */
        private String sportsType;

        /**
         * 投注时间
         */
        private Date orderTime;

        /**
         * 结算时间
         */
        private Date winLostDate;

        /**
         * 最后修改时间
         */
        private Date modifyDate;

        /**
         * 最后修改时间
         */
        private BigDecimal odds;

        /**
         * 赔率类型:M-Malay odds,H-HongKong odds,E-Euro odds,I-Indonesia odds
         */
        private String oddsStyle;

        /**
         * 派奖金额
         */
        private BigDecimal stake;

        /**
         * 实际派奖金额
         */
        private BigDecimal actualStake;

        /**
         * 币种
         */
        private String currency;

        /**
         * 开奖结果
         */
        private String status;

        /**
         * 输赢
         */
        private BigDecimal winLost;

        private BigDecimal turnover;

        /**
         * 上半场输赢结果
         */
        private Integer isHalfWonLose;

        /**
         * 是否现场比赛
         */
        private Integer isLive;

        /**
         * 拥有实际赌注的玩家的最大获胜额
         */
        private BigDecimal maxWinWithoutActualStake;

        /**
         * IP
         */
        private String ip;

        /**
         * 投注信息
         */
        private String subBet;
    }


    /**
     * 根据修改日期获取注单列表
     */
    @Data
    class GetBetListByModifyDate {
        private Integer id;

        /**
         * UID
         */
        private Integer uid;

        /**
         * 用户名
         */
        private String username;

        /**
         * 投注的体育类型
         */
        private String sportsType;

        /**
         * 投注时间
         */
        private Date orderTime;

        /**
         * 结算时间
         */
        private Date winLostDate;

        /**
         * 最后修改时间
         */
        private Date modifyDate;

        /**
         * 最后修改时间
         */
        private BigDecimal odds;

        /**
         * 赔率类型:M-Malay odds,H-HongKong odds,E-Euro odds,I-Indonesia odds
         */
        private String oddsStyle;

        /**
         * 派奖金额
         */
        private BigDecimal stake;

        /**
         * 实际派奖金额
         */
        private BigDecimal actualStake;

        /**
         * 币种
         */
        private String currency;

        /**
         * 开奖结果
         */
        private String status;

        /**
         * 输赢
         */
        private BigDecimal winLost;

        private BigDecimal turnover;

        /**
         * 上半场输赢结果
         */
        private Integer isHalfWonLose;

        /**
         * 是否现场比赛
         */
        private Integer isLive;

        /**
         * 拥有实际赌注的玩家的最大获胜额
         */
        private BigDecimal maxWinWithoutActualStake;

        /**
         * IP
         */
        private String ip;

        /**
         * 投注信息
         */
        private String subBet;
    }


    /**
     * 根据交易编号取得注单列表
     */
    @Data
    class GetBetListByRefnos {
        private Integer id;

        /**
         * UID
         */
        private Integer uid;

        /**
         * 用户名
         */
        private String username;

        /**
         * 投注的体育类型
         */
        private String sportsType;

        /**
         * 投注时间
         */
        private Date orderTime;

        /**
         * 结算时间
         */
        private Date winLostDate;

        /**
         * 最后修改时间
         */
        private Date modifyDate;

        /**
         * 最后修改时间
         */
        private BigDecimal odds;

        /**
         * 赔率类型:M-Malay odds,H-HongKong odds,E-Euro odds,I-Indonesia odds
         */
        private String oddsStyle;

        /**
         * 派奖金额
         */
        private BigDecimal stake;

        /**
         * 实际派奖金额
         */
        private BigDecimal actualStake;

        /**
         * 币种
         */
        private String currency;

        /**
         * 开奖结果
         */
        private String status;

        /**
         * 输赢
         */
        private BigDecimal winLost;

        private BigDecimal turnover;

        /**
         * 上半场输赢结果
         */
        private Integer isHalfWonLose;

        /**
         * 是否现场比赛
         */
        private Integer isLive;

        /**
         * 拥有实际赌注的玩家的最大获胜额
         */
        private BigDecimal maxWinWithoutActualStake;

        /**
         * IP
         */
        private String ip;

        /**
         * 投注信息
         */
        private String subBet;

    }


    /**
     * 取得体育Forecast页面链接
     */
    @Data
    class GetForecastPage {
    }


    /**
     * 取得体育Last50页面链接
     */
    @Data
    class GetLast50Page {
    }
}
