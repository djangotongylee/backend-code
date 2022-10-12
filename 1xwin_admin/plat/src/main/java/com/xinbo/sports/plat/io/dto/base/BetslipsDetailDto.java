package com.xinbo.sports.plat.io.dto.base;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author: wells
 * @date: 2020/8/3
 * @description:
 */

public interface BetslipsDetailDto {

    /***
     * 注单详情请求实体类
     */
    @Data
    class BetslipsDetailReqDto {
        @ApiModelProperty(name = "betId", value = "注单ID")
        private String betId;
        @ApiModelProperty(name = "gameId", value = "游戏ID")
        private Integer gameId;
    }

    /**
     * 注单公共实体类
     */
    @Data
    class Betslips {
        @ApiModelProperty(name = "id", value = "订单号")
        private String id;
        @ApiModelProperty(name = "betType", value = "投注类型")
        private String betType;
        @ApiModelProperty(name = "actionNo", value = "期号")
        private String actionNo;
        @ApiModelProperty(name = "gameNo", value = "局号")
        private String gameNo;
        @ApiModelProperty(name = "xbCoin", value = "投注金额")
        private BigDecimal xbCoin;
        @ApiModelProperty(name = "xbValidCoin", value = "有效投注额")
        private BigDecimal xbValidCoin;
        @ApiModelProperty(name = "xbProfit", value = "盈亏金额")
        private BigDecimal xbProfit;
        @ApiModelProperty(name = "xbStatus", value = "状态")
        private String xbStatus;
        @ApiModelProperty(name = "createdAt", value = "结算时间")
        private Integer createdAt;
    }

    /***
     * 赛事预告请求实体类
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class SportScheduleReqDto {
        @ApiModelProperty(name = "gameId", value = "游戏ID")
        private Integer gameId;
        @ApiModelProperty(name = "status", value = "状态")
        private Integer status;
        @ApiModelProperty(name = "timestamp", value = "开赛时间")
        private Integer timestamp;
    }

    /**
     * 实体类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class SportSchedule {
        @ApiModelProperty(name = "timestamp", value = "时间")
        private Integer timestamp;
        @ApiModelProperty(name = "gameId", value = "游戏id")
        private Integer gameId;
        @ApiModelProperty(name = "gameName", value = "游戏名称")
        private String gameName;
        @ApiModelProperty(name = "Branch", value = "体育类型")
        private String branch;
        @ApiModelProperty(name = "League", value = "联队")
        private String league;
        @ApiModelProperty(name = "MEID", value = "活动id")
        private Integer MEID;
        @ApiModelProperty(name = "IsOption", value = "是否主赛事")
        private Integer isOption;
        @ApiModelProperty(name = "Participant1", value = "主队")
        private participant1 participant1;
        @ApiModelProperty(name = "Participant2", value = "客队")
        private participant2 participant2;
        @ApiModelProperty(name = "moneyLine", value = "独赢盘/1/2")
        private String moneyLine;
        @ApiModelProperty(name = "sort", value = "优先级排序")
        private Integer sort;
        @ApiModelProperty(name = "operatorId", value = "操作者id")
        private Integer operatorId;
        @ApiModelProperty(name = "operatorName", value = "操作者名称")
        private String operatorName;
        @ApiModelProperty(name = "operatorTime", value = "操作者时间")
        private Integer operatorTime;
        @ApiModelProperty(name = "status", value = "状态 0-禁用，1-启用")
        private Integer status;

        @Data
        public static class participant1 {
            @ApiModelProperty(name = "name", value = "名称")
            private String name;
            @ApiModelProperty(name = "icon", value = "图标")
            private String icon;
        }

        @Data
        public static class participant2 {
            @ApiModelProperty(name = "name", value = "名称")
            private String name;
            @ApiModelProperty(name = "icon", value = "图标")
            private String icon;
        }

    }

    @Data
    class ForwardEvent {
        @ApiModelProperty(name = "link", value = "前往链接")
        private String link;

    }

    @Data
    class ForwardEventReq {
        @ApiModelProperty(name = "masterEventID", value = "赛事活动id")
        private Integer masterEventID;
        @ApiModelProperty(name = "gameId", value = "游戏id")
        private Integer gameId;

    }

    @Data
    class verifySession {
        @ApiModelProperty(name = "data", value = "数据")
        private data data;
        @ApiModelProperty(name = "error", value = "错误")
        private error error;

    }

    @Data
    class data {
        @ApiModelProperty(name = "player_name", value = "玩家姓名")
        private String player_name;
        @ApiModelProperty(name = "currency", value = "货币")
        private String currency;
        @ApiModelProperty(name = "nickname", value = "昵称")
        private String nickname;
    }

    @Data
    class error {
        @ApiModelProperty(name = "code", value = "错误代码")
        private String code;
        @ApiModelProperty(name = "message", value = "错误信息")
        private String message;
    }


    /***
     * 赛事预告请求实体类
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class SportReqDto {
        @ApiModelProperty(name = "status", value = "状态;0-禁用，1-启用")
        private Integer status;
        @ApiModelProperty(name = "startTime", value = "开赛开始时间")
        private Integer startTime;
        @ApiModelProperty(name = "endTime", value = "开赛结束时间")
        private Integer endTime;
    }
}
