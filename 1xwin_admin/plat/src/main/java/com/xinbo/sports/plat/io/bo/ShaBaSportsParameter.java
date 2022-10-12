package com.xinbo.sports.plat.io.bo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author: wells
 * @date: 2020/7/14
 * @description:
 */

public interface ShaBaSportsParameter {
    /**
     * 平台配置
     * {"apiUrl":"http://tsa.ig128.com/api/","vendorId":"r8a5yrndrp","operatorId":"XBSP",
     * "maxTransfer":"100000","minTransfer":"1","oddsType":"1","currency":"20","pcLoginUrl":
     * "http://sbtest.l0030.ig128.com/deposit_processlogin.aspx","h5LoginUrl":
     * "http://smartsbtest.l0030.ig128.com/deposit_processlogin.aspx","versionKey":"30309219"}
     */
    @Data
    class SBPlatConfig {
        private String apiUrl;
        private String vendorId;
        private String operatorId;
        private String maxTransfer;
        private String minTransfer;
        private String oddsType;
        private String currency;
        private String h5LoginUrl;
        private String pcLoginUrl;
        private String versionKey;
    }

    /**
     * 登录用户
     */
    @Data
    @Builder
    class LoginBo {
        @JSONField(name = "vendor_id")
        private String vendorId;
        @JSONField(name = "vendor_member_id")
        private String vendorMemberId;
    }

    /**
     * 创建用户请求实体
     */
    @Data
    @Builder
    class CreateMemberBO {
        @JSONField(name = "vendor_id")
        private String vendorId;
        @JSONField(name = "vendor_member_id")
        private String vendorMemberId;
        private String operatorid;
        private String username;
        @JSONField(name = "oddstype")
        private String oddsType;
        private String currency;
        @JSONField(name = "maxtransfer")
        private BigDecimal maxTransfer;
        @JSONField(name = "mintransfer")
        private BigDecimal minTransfer;
    }

    /**
     * 转账-》上分、下分
     */
    @Data
    @Builder
    class FundTransfer {
        @JSONField(name = "vendor_id")
        private String vendorId;
        @JSONField(name = "vendor_member_id")
        private String vendorMemberId;
        @JSONField(name = "vendor_trans_id")
        private String vendorTransId;
        private BigDecimal amount;
        private String currency;
        private Integer direction;
        @JSONField(name = "walletId")
        private Integer walletId;
    }

    /**
     * 查询余额
     */
    @Data
    @Builder
    class CheckUserBalance {
        @JSONField(name = "vendor_id")
        private String vendorId;
        @JSONField(name = "vendor_member_ids")
        private String vendorMemberIds;
        @JSONField(name = "walletId")
        private Integer walletId;
    }

    /**
     * 检查资金转账状态
     */
    @Data
    @Builder
    class CheckFundTransfer {
        @JSONField(name = "vendor_id")
        private String vendorId;
        @JSONField(name = "vendor_trans_id")
        private String vendorTransId;
        @JSONField(name = "walletId")
        private Integer walletId;
    }

    /**
     * 以日期时间取得下注交易细节
     */
    @Data
    @Builder
    @AllArgsConstructor
    class BetRequestParams {
        @JSONField(name = "vendor_id")
        private String vendorId;
        @JSONField(name = "version_key")
        private Long versionKey;
    }

    /**
     * 沙巴投注信息
     */
    @Data
    class BetDetail {
        @JSONField(name = "trans_id", serializeUsing = Long.class)
        private Long id;
        @JSONField(name = "uid")
        private Integer uid;
        @JSONField(name = "vendor_member_id")
        private String vendorMemberId;
        @JSONField(name = "operator_id")
        private String operatorId;
        @JSONField(name = "league_id")
        private Integer leagueId;
        @JSONField(name = "match_id")
        private Integer matchId;
        @JSONField(name = "home_id")
        private Integer homeId;
        @JSONField(name = "away_id")
        private Integer awayId;
        @JSONField(name = "team_id")
        private Integer teamId;
        @JSONField(name = "match_datetime", format = "yyyy-MM-dd'T'HH:mm:ss")
        private Date matchDatetime;
        @JSONField(name = "sport_type")
        private Integer sportType;
        @JSONField(name = "bet_type")
        private Integer betType;
        @JSONField(name = "parlay_ref_no")
        private Long parlayRefNo;
        @JSONField(name = "odds", serializeUsing = BigDecimal.class)
        private BigDecimal odds;
        @JSONField(name = "stake", serializeUsing = BigDecimal.class)
        private BigDecimal stake;
        @JSONField(name = "transaction_time", format = "yyyy-MM-dd'T'HH:mm:ss")
        private Date transactionTime;
        @JSONField(name = "ticket_status")
        private String ticketStatus;
        @JSONField(name = "winlost_amount", serializeUsing = BigDecimal.class)
        private BigDecimal winlostAmount;
        @JSONField(name = "after_amount", serializeUsing = BigDecimal.class)
        private BigDecimal afterAmount;
        @JSONField(name = "currency")
        private Integer currency;
        @JSONField(name = "winlost_datetime", format = "yyyy-MM-dd'T'HH:mm:ss")
        private Date winlostDatetime;
        @JSONField(name = "odds_type")
        private Integer oddsType;
        @JSONField(name = "odds_Info")
        private String oddsInfo;
        @JSONField(name = "bet_team")
        private String betTeam;
        @JSONField(name = "exculding")
        private String exculding;
        @JSONField(name = "bet_tag")
        private String betTag;
        @JSONField(name = "home_hdp", serializeUsing = BigDecimal.class)
        private BigDecimal homeHdp;
        @JSONField(name = "away_hdp", serializeUsing = BigDecimal.class)
        private BigDecimal awayHdp;
        @JSONField(name = "hdp", serializeUsing = BigDecimal.class)
        private BigDecimal hdp;
        @JSONField(name = "betfrom")
        private String betfrom;
        @JSONField(name = "islive")
        private String islive;
        @JSONField(name = "home_score")
        private Integer homeScore;
        @JSONField(name = "away_score")
        private Integer awayScore;
        @JSONField(name = "settlement_time", format = "yyyy-MM-dd'T'HH:mm:ss")
        private Date settlementTime;
        @JSONField(name = "customInfo1")
        private String customInfo1;
        @JSONField(name = "customInfo2")
        private String customInfo2;
        @JSONField(name = "customInfo3")
        private String customInfo3;
        @JSONField(name = "customInfo4")
        private String customInfo4;
        @JSONField(name = "customInfo5")
        private String customInfo5;
        @JSONField(name = "ba_status")
        private String baStatus;
        @JSONField(name = "version_key")
        private Long versionKey;
        @JSONField(name = "ParlayData")
        private String parlayData;
        //JSONArray类型数据处理
        @JSONField(name = "leaguename", serializeUsing = JSONArray.class)
        private String leagueName;
        @JSONField(name = "hometeamname", serializeUsing = JSONArray.class)
        private String homeTeamName;
        @JSONField(name = "awayteamname", serializeUsing = JSONArray.class)
        private String awayTeamName;
    }


}
