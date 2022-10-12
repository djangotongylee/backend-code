package com.xinbo.sports.plat.io.bo;

import com.alibaba.fastjson.annotation.JSONField;
import com.xinbo.sports.utils.DateUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author: wells
 * @date: 2020/6/1
 * @description:
 */

public interface BBINLiveParameter {
    /**
     * 平台配置
     * {"apiUrl":"http://linkapi.yunjigame6.com","loginUrl":"http://888.yunjigame6.com","website":"cloudg","uppername":"dtestxbty01"}
     */
    @Data
    class BBINPlatConfig {
        private String apiUrl;
        private String loginUrl;
        private String website;
        private String uppername;
    }

    /**
     * 登录
     */
    @Data
    @Builder
    class Login {
        //网站名称
        private String website;
        //会员帐号
        private String username;
        //上层帐号
        @JSONField(name = "uppername")
        private String upperName;
        //语言
        private String lang;
        //整合⾴
        @JSONField(name = "page_site")
        private String pageSite;
        //视讯
        @JSONField(name = "page_present")
        private String pagePresent;
        //0:维护时回传讯息、1:维护时导⼊整合⾴
        @JSONField(name = "maintenance_page")
        private Integer maintenancePage;
        //登⼊来源1：pc、2：h5、4： App)，其他：9
        private Integer ingress;
    }

    /**
     * 直接进入url
     */
    @Data
    @Builder
    class GameUrlBy {
        //网站名称
        private String website;
        //语言
        private String lang;
        //会员的sessionid
        private String sessionid;
        //游戏类型
        private String gametype;
        //会员帐号
        private String username;
        //上层帐号
        @JSONField(name = "uppername")
        private String upperName;
    }

    /**
     * 注册会员
     */
    @Data
    @Builder
    class RegisterAgent {
        //网站名称
        private String website;
        //会员帐号
        private String username;
        //上层帐号
        @JSONField(name = "uppername")
        private String upperName;
        //登⼊来源1：pc、2：h5、4： App)，其他：9
        private Integer ingress;
    }

    /**
     * 交易-上分，下分
     */
    @Data
    @Builder
    class TranRecord {
        //网站名称
        private String website;
        //会员帐号
        private String username;
        //上层帐号
        @JSONField(name = "uppername")
        private String upperName;
        //转帐序号(唯一值)
        @JSONField(name = "remitno")
        private String remitNo;
        //IN(转入额度) OUT(转出额度)
        private String action;
        //转帐额度(正整数)
        private Integer remit;
    }

    /**
     * 查询余额
     */
    @Data
    @Builder
    class CheckUsrBalance {
        //网站名称
        private String website;
        //会员帐号
        private String username;
        //上层帐号
        @JSONField(name = "uppername")
        private String upperName;
    }

    /**
     * 查询会员转帐是否成功
     */
    @Data
    @Builder
    class CheckTransfer {
        //网站名称
        private String website;
        //转账序号
        private String transid;
    }

    /**
     * 获取投注信息
     */
    @Data
    @Builder
    @AllArgsConstructor
    class BetRecord {
        //网站名称
        private String website;
        //上层帐号
        @JSONField(name = "uppername")
        private String upperName;
        //日期
        @JSONField(name = "rounddate")
        private String roundDate;
        //开始时间
        @JSONField(name = "starttime")
        private String startTime;
        //结束时间
        @JSONField(name = "endtime")
        private String endTime;
        //游戏种类
        @JSONField(name = "gamekind")
        private Integer gameKind;
    }

    /**
     * 捕鱼达人拉单
     */
    @Data
    @Builder
    class BetRecordBy {
        //网站名称
        private String website;
        //上层帐号
        @JSONField(name = "uppername")
        private String upperName;
        //BetTime / ModifiedTime：须选一
        private String action;
        //日期
        private String date;
        private String roundDate;
        //开始时间
        @JSONField(name = "starttime")
        private String startTime;
        //结束时间
        @JSONField(name = "endtime")
        private String endTime;
    }

    /**
     * BBIN公共实例
     */
    @Data
    class Bet {
        @JSONField(name = "WagersID", serializeUsing = Long.class)
        private Long id;
        private Integer xbUid;
        private String xbUsername;
        @JSONField(name = "UserName")
        private String username;
        @JSONField(name = "WagersDate", format = "yyyy-MM-dd HH:mm:ss")
        private Date wagersDate;
        @JSONField(name = "GameType")
        private Integer gameType;
        @JSONField(name = "Result")
        private String result;
        @JSONField(name = "BetAmount", serializeUsing = BigDecimal.class)
        private BigDecimal betAmount;
        @JSONField(name = "Payoff", serializeUsing = BigDecimal.class)
        private BigDecimal payOff;
        @JSONField(name = "Currency")
        private String currency;
        @JSONField(name = "ExchangeRate", serializeUsing = BigDecimal.class)
        private BigDecimal exchangeRate;
        @JSONField(name = "Commissionable", serializeUsing = BigDecimal.class)
        private BigDecimal commissionable;
        private Integer createdAt = DateUtils.getCurrentTime();
        private Integer updatedAt = DateUtils.getCurrentTime();
    }


    @EqualsAndHashCode(callSuper = true)
    @Data
    class BBINLiveBet extends Bet {
        @JSONField(name = "SerialID")
        private Integer serialId;
        @JSONField(name = "RoundNo")
        private String roundNo;
        @JSONField(name = "WagerDetail")
        private String wagerDetail;
        @JSONField(name = "GameCode")
        private String gameCode;
        @JSONField(name = "Card")
        private String card;
        private BigDecimal commissionable;
        @JSONField(name = "Origin")
        private String origin;
    }

    /**
     * BB电子
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    class BBINGameBet extends Bet {

    }

    /**
     * BB体育
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    class BBINSportsBet extends Bet {
        @JSONField(name = "Origin")
        private String origin;
    }

    /**
     * BB新体育
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    class BBINNewSportBet extends Bet {
        @JSONField(name = "UPTIME")
        private String upTime;
        @JSONField(name = "OrderDate")
        private String orderDate;
        @JSONField(name = "Origin")
        private String origin;
    }

    /**
     * 捕鱼
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    class BBINFinish extends Bet {
        @JSONField(name = "SerialID", serializeUsing = Integer.class)
        private Integer serialId;
        @JSONField(name = "ModifiedDate", format = "yyyy-MM-dd HH:mm:ss")
        private Date modifiedDate;
    }
}
