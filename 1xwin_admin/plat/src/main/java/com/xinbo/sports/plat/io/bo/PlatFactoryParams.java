package com.xinbo.sports.plat.io.bo;

import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 平台工厂入参、出参实体
 *
 * @author David
 * @date: 04/06/2020
 */
public interface PlatFactoryParams {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    class BasePlatReqDto {
        @ApiModelProperty(name = "lang", value = "语言:en-英文 简体(zh-cn) 繁体(zh-tw)", example = "en")
        private String lang;
        @ApiModelProperty(name = "device", value = "设备:H5-m PC-d 安卓-android, IOS-ios", example = "m")
        private String device;
    }
    // ************************************* 登录 ****************************************

    /**
     * 登录接口入参
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class PlatLoginReqDto {
        @ApiModelProperty(name = "lang", value = "语言:en-英文 zh-简体 th-泰语 vi-越南语", example = "en")
        protected String lang;
        @ApiModelProperty(name = "device", value = "设备:H5-m PC-d 安卓-android, IOS-ios", example = "m")
        protected String device;
        @ApiModelProperty(name = "username", value = "用户名", example = "test999")
        private String username;
        @ApiModelProperty(name = "slotId", value = "电子游戏类ID", example = "aaa")
        private String slotId;
    }

    /**
     * 注册接口入参
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class PlatRegisterReqDto {
        @ApiModelProperty(name = "username", value = "用户名", example = "test999")
        private String username;
        @ApiModelProperty(name = "lang", value = "语言:en-英文, zh-cn-简体, zh-tw-繁体")
        public String lang;
        @ApiModelProperty(name = "device", value = "设备:m-H5, d-PC, android-安卓, ios-苹果")
        public String device;
    }

    /**
     * 投注记录补单
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class BetsRecordsSupplementReqDto {
        @ApiModelProperty(name = "requestInfo", value = "拉单请求信息")
        private String requestInfo;
    }

    /**
     * 生成补单记录入参
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class GenSupplementsOrdersReqDto {
        @ApiModelProperty(name = "start", value = "开始时间")
        private Integer start;
        @ApiModelProperty(name = "end", value = "结束时间")
        private Integer end;
        @ApiModelProperty(name = "gameId", value = "游戏ID")
        private Integer gameId;
    }

    /**
     * 登录接口出参
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class PlatGameLoginResDto {
        @ApiModelProperty(name = "type", value = "链接类型:1-直接跳转", example = "1")
        private int type;
        @ApiModelProperty(name = "url", value = "登录链接", example = "https://www.baidu.com")
        private String url;
        @ApiModelProperty(name = "userCoin", value = "用户余额", example = "100.00")
        private BigDecimal coin;
    }

    // *************************************登出*****************************************

    /**
     * 登出接口入参
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class PlatLogoutReqDto {
        @ApiModelProperty(name = "username", value = "用户名", example = "test999")
        private String username;
    }

    /**
     * 登出接口出参
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class PlatLogoutResDto {
        @ApiModelProperty(name = "success", value = "1-成功 0-失败")
        private Integer success;
    }

    // ************************************* 上分、下分 ****************************************

    /**
     * 上、下分入参
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @SuperBuilder
    @AllArgsConstructor
    @NoArgsConstructor
    class PlatCoinTransferReqDto extends BasePlatReqDto {
        @NotNull
        @ApiModelProperty(name = "username", value = "姓名")
        private String username;
        @NotNull
        @ApiModelProperty(name = "coin", value = "金额")
        private BigDecimal coin;
        @ApiModelProperty(name = "orderId", value = "订单ID")
        private String orderId;
        @ApiModelProperty(name = "IsFullAmount", value = "是否全部下分: 1-全部 0-按金额", example = "1")
        private Integer isFullAmount;
    }

    /**
     * 上分、下分出参
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class PlatCoinTransferResDto {
        @ApiModelProperty(name = "platCoin", value = "平台余额", example = "100.00")
        private BigDecimal platCoin;
    }

    // ************************************* 余额查询 ****************************************

    /**
     * 余额查询出参
     */
    @Data
    @SuperBuilder
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    class PlatQueryBalanceReqDto extends BasePlatReqDto {
        @ApiModelProperty(name = "username", value = "用户名", example = "test999")
        private String username;
    }

    /**
     * 余额查询出参
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class PlatQueryBalanceResDto {
        @ApiModelProperty(name = "platCoin", value = "平台余额", example = "100.00")
        private BigDecimal platCoin;
    }

    // ************************************* 老虎机游戏列表 ****************************************

    /**
     * 老虎机游戏列表入参
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @SuperBuilder
    @AllArgsConstructor
    @NoArgsConstructor
    class PlatSlotGameReqDto extends BasePlatReqDto {
        @ApiModelProperty(name = "id", value = "游戏ID", example = "101")
        private Integer id;
        @ApiModelProperty(name = "uid", value = "用户ID", example = "101")
        private Integer uid;
        @ApiModelProperty(name = "category", value = "种类:0-全部游戏 1-热门游戏 2-最新游戏 3-我的收藏", example = "1")
        private Integer category;
        @ApiModelProperty(name = "name", value = "游戏名称(搜索用)", example = "经典扑克100手")
        private String name;
        @ApiModelProperty(name = "device", value = "设备", example = "d")
        private String device;
    }

    /**
     * 老虎机游戏列表出参
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class PlatSlotGameResDto {
        @ApiModelProperty(name = "id", value = "游戏ID", example = "1")
        private String id;

        @ApiModelProperty(name = "name", value = "游戏名称", example = "倫敦獵人")
        private String name;

        @ApiModelProperty(name = "img", value = "游戏名称", example = "倫敦獵人")
        private String img;

        @ApiModelProperty(name = "isNew", value = "游戏名称", example = "1")
        private Integer isNew;

        @ApiModelProperty(name = "favoriteStar", value = "收藏值", example = "999")
        private Integer favoriteStar;

        @ApiModelProperty(name = "hotStar", value = "热度值", example = "900")
        private Integer hotStar;

        @ApiModelProperty(name = "isFavorite", value = "是否收藏:1-已收藏 0-未收藏", example = "1")
        private Integer isFavorite;
    }

    // ************************************* 老虎机游戏收藏 ****************************************

    /**
     * 收藏老虎机游戏入参
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class PlatSlotGameFavoriteReqDto {
        @ApiModelProperty(name = "gameId", value = "游戏ID")
        private Integer gameId;

        @ApiModelProperty(name = "gameSlotId", value = "老虎机游戏ID(电子类填写)")
        private String gameSlotId;

        @ApiModelProperty(name = "uid", value = "会员ID")
        private Integer uid;

        @ApiModelProperty(name = "direction", value = "类型:0-取消收藏 1-添加收藏")
        private Integer direction;
    }

    // ************************************* 统计金额、注单记录 ****************************************

    /**
     * 平台按日期查询统计金额、注单信息公共入参
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @SuperBuilder
    @AllArgsConstructor
    @NoArgsConstructor
    class PlatGameQueryDateDto extends BasePlatReqDto {
        @ApiModelProperty(name = "startTime", value = "开始时间")
        @NotNull(message = "startTime不能为空")
        private Integer startTime;

        @ApiModelProperty(name = "endTime", value = "结束时间")
        @NotNull(message = "endTime不能为空")
        private Integer endTime;

        @ApiModelProperty(name = "uid", value = "用户ID", example = "1")
        private Integer uid;

        @ApiModelProperty(name = "username", value = "用户名", example = "1")
        private String username;

        @ApiModelProperty(name = "gameId", value = "游戏ID", example = "1")
        private Integer gameId;

        @ApiModelProperty(name = "platId", value = "平台ID", example = "1")
        private Integer platId;

        @ApiModelProperty(name = "isAgent", value = "代理", example = "单用户不传 1->代理")
        private Integer isAgent;

        @ApiModelProperty(name = "promotionsId", value = "活动ID", example = "1")
        private Integer promotionsId;
    }

    /**
     * 平台按日期查询投注金额、盈亏金额
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class PlatCoinStatisticsResDto {
        @ApiModelProperty(name = "coinBet", value = "投注金额", example = "101")
        private BigDecimal coinBet;

        @ApiModelProperty(name = "coinProfit", value = "盈亏金额", example = "100.00")
        private BigDecimal coinProfit;
    }

    /**
     * 平台注单统一返回接口
     */
    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class PlatBetListResDto {
        @ApiModelProperty(name = "id", value = "注单号", example = "101")
        private String id;
        @ApiModelProperty(name = "username", value = "用户名")
        @JSONField(name = "xbUsername")
        private String username;

        @ApiModelProperty(name = "uid", value = "用户id")
        @JSONField(name = "xbUid")
        private Integer uid;

        @ApiModelProperty(name = "name", value = "平台名称", example = "百家乐")
        private String name;

        @ApiModelProperty(name = "platName", value = "平台名称", example = "平台")
        private String platName;

        @ApiModelProperty(name = "betContent", value = "投注内容")
        private String betContent;

        @ApiModelProperty(name = "actionNo", value = "期号")
        private String actionNo;

        @ApiModelProperty(name = "coinBet", value = "投注金额", example = "100")
        @JSONField(name = "xbCoin", serializeUsing = BigDecimal.class)
        private BigDecimal coinBet;

        @ApiModelProperty(name = "coinProfit", value = "盈亏金额", example = "100")
        @JSONField(name = "xbProfit", serializeUsing = BigDecimal.class)
        private BigDecimal coinProfit;

        // 状态: 1-赢 2-输 3-平 4-撤单 5-未结算 6-等待确认
        @ApiModelProperty(name = "xbStatus", value = "1-赢 2-输 3-和 4-取消 5-等待结算 6-赛事取消 7-投注确认 8-投注拒绝 9-赢一半 10-输一半", example = "1")
        private Integer xbStatus;

        @ApiModelProperty(name = "time", value = "注单时间", example = "100")
        private Integer createdAt;
    }

    /**
     * 三方转账校验入参
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @SuperBuilder
    @AllArgsConstructor
    @NoArgsConstructor
    class PlatCheckTransferReqDto extends BasePlatReqDto {
        @NotNull
        @ApiModelProperty(name = "orderId", value = "订单ID")
        private String orderId;

    }

    /**
     * 异常处理实例
     */
    @Data
    @Builder
    @AllArgsConstructor
    class ExceptionDto {
        //日期yyyy-mm-dd
        private String date;
        //开始时间HH-mm-ss
        private String startTime;
        //结束时间HH-mm-ss
        private String endTime;
    }

}
