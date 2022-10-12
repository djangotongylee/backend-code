package com.xinbo.sports.plat.io.bo;

import com.alibaba.fastjson.annotation.JSONField;
import com.xinbo.sports.utils.DateUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author: wells
 * @date: 2020/5/25
 * @description:
 */

public interface GGFishingParameter {
    /**
     * 平台配置
     * {"apiUrl":"http://testapisw.gg626.com","reportUrl":"http://testbetrec.gg626.com","desKey":"12345678",
     * "cagent":"TE399","md5Key":"123456","currency":"HKD","acType":"1"}
     */
    @Data
    class GGPlatConfig {
        private String apiUrl;
        private String reportUrl;
        private String desKey;
        private String cagent;
        private String md5Key;
        private String currency;
        private String acType;
        private String returnUrl;
    }

    /**
     * 登录请求实体
     */
    @Data
    @Builder
    class LoginBO {
        private String cagent;
        private String loginname;
        private String password;
        private String method;
        private Integer actype;
        private String cur;
    }

    /**
     * 登出
     */
    @Data
    @Builder
    class LogoutBO {
        private String cagent;
        private String loginname;
        private String password;
        private String method;
        private String cur;
    }

    /**
     * 获取登录地址
     */
    @Data
    @Builder
    class ForwardGameBo {
        private String cagent;
        private String loginname;
        private String password;
        private String method;
        private String sid;
        private String lang;
        private Integer gametype;
        private String ip;
        private String sessionId;
        private Integer isapp;
        private Integer ishttps;
        private String returnUrl;
        private Integer iframe;
    }

    /**
     * 获取余额
     */
    @Data
    @Builder
    class QueryBalanceBo {
        private String cagent;
        private String loginname;
        private String password;
        private String method;
        private String cur;
    }

    /**
     * 查询转账订单状态
     */
    @Data
    @Builder
    class QueryOrderStatusBo {
        private String cagent;
        private String method;
        private String billno;
    }

    @Data
    @Builder
    class CoinUpBO {
        private String cagent;
        private String loginname;
        private String password;
        private String method;
        private String cur;
        private String billno;
        private String type;
        private BigDecimal credit;
        private String ip;
    }

    @Data
    @Builder
    @AllArgsConstructor
    class BetListByDateBo {
        private String cagent;
        private String startdate;
        private String enddate;
        private Integer gameId;
        private String method;
    }

    @Data
    class BetslipsGgBo {
        @JSONField(name = "betid")
        private String betId;
        @JSONField(name = "uid")
        private Integer uid;
        @JSONField(name = "gameId")
        private String gameId;
        @JSONField(name = "bet", serializeUsing = BigDecimal.class)
        private BigDecimal bet;
        @JSONField(name = "cuuency")
        private String currency;
        @JSONField(name = "linkId")
        private String linkId;
        @JSONField(name = "accountno")
        private String accountNo;
        @JSONField(name = "autoid", serializeUsing = Long.class)
        private Long autoId;
        @JSONField(name = "closed")
        private Integer closed;
        @JSONField(name = "bettimeStr", format = "yyyy/MM/dd HH:mm:ss")
        private Date bettimeStr;
        @JSONField(name = "paytimeStr", format = "yyyy/MM/dd HH:mm:ss")
        private Date paytimeStr;
        @JSONField(name = "profit", serializeUsing = BigDecimal.class)
        private BigDecimal profit;
        @JSONField(name = "origin")
        private Integer origin;
        private Integer updatedAt = DateUtils.getCurrentTime();
    }

}
