package com.xinbo.sports.plat.io.bo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * FuturesLottery
 * </p>
 *
 * @author andy
 * @since 2020/9/28
 */
public interface FuturesLotteryRequestParameter {
    /**
     * config配置类:对应sp_plat_list表的config字段
     */
    @Data
    @Builder
    @AllArgsConstructor
    class Config {
        /**
         * 域名地址
         */
        private String apiUrl;

        private String companyKey;

        private Integer platId;


        /**
         * 币种:正式環境有提供泰銖 ，測試環境目前統一都是提供人民幣幣別測試
         */
        private String currency;
        /**
         * 环境:PROD-生产环境
         */
        private String environment;
        /**
         * 用户名前缀:
         * 规则:接口方的username=前缀+username
         */
        private String prefix;
    }

    /**
     * 登录
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class LoginReqBody {
        private String companyKey;
        private Integer platId;

        /**
         * username[由6到16位字母、数字、下划线或纯字母组成,不能纯数字]
         */
        private String username;

        /**
         * 设备类型[必填]:1-H5端 2-PC端
         */
        private Integer appType;

        /**
         * 语言[必填]:zh-CN-简体中文 zh-TW-繁体中文 vi-VN-越南语 en-US-英语 th-TH-泰语
         */
        private String sysLang;

        /**
         * 角色[非必填]:0-会员 4-测试
         */
        private Integer role;

    }

    /**
     * 余额查询
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class QueryBalanceReqBody {
        /**
         * username[由6到16位字母、数字、下划线或纯字母组成,不能纯数字]
         */
        private String username;

        private String companyKey;
        private Integer platId;

    }

    /**
     * 存款或提款
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class DepositOrWithdrawalReqBody {
        private String username;
        private String companyKey;
        private Integer platId;

        /**
         * 金额[必填]
         */
        private BigDecimal coin;
        /**
         * 贵公司的订单号[必填项,全局唯一,最大长度32位字符串类型]
         */
        private String orderPlat;

        /**
         * 类型:DEPOSIT-存款,DRAWAL-提款
         */
        private String category;

    }

    /**
     * 检查存款或提款状态
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class CheckDepositOrWithdrawalStatusReqBody {
        private String username;
        private String companyKey;
        private Integer platId;

        /**
         * 贵公司的订单号[必填项,全局唯一,最大长度32位字符串类型]
         */
        private String orderPlat;
    }

    /**
     * 拉单接口
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class GetBetListReqBody {
        private String companyKey;
        private Integer platId;
        private Integer updatedAt;
    }

    @Getter
    enum UrlEnum {
        /**
         * 注册或登录
         */
        REGISTER_OR_LOGIN("/api/v1/plat/registerOrLogin", "注册或登录"),

        /**
         * 余额查询
         */
        QUERYBALANCE("/api/v1/plat/queryBalance", "余额查询"),
        /**
         * 存款或提款
         */
        DEPOSIT_OR_WITHDRAWAL("/api/v1/plat/depositOrWithdrawal", "存款或提款"),
        /**
         * 检查存款或提款状态
         */
        CHECK_DEPOSIT_OR_WITHDRAWALSTATUS("/api/v1/plat/checkDepositOrWithdrawalStatus", "检查存款或提款状态"),
        /**
         * 拉单接口
         */
        GET_BET_LIST("/api/v1/plat/getBetList", "拉单接口"),

        /**
         * 预设开奖->列表
         */
        GET_OPEN_PRESET_LIST("/api/v1/plat/getOpenPresetList", "预设开奖->列表"),
        /**
         * 预设开奖->新增/修改
         */
        SAVE_OR_UPDATE_OPEN_PRESET("/api/v1/plat/saveOrUpdateOpenPreset", "新增/修改"),
        /**
         * 预设开奖->删除
         */
        DELETE_OPEN_PRESET("/api/v1/plat/deleteOpenPreset", "预设开奖->删除"),


        /**
         * 预设开奖->导出
         */
        EXPORT_OPEN_PRESET_LIST("/api/v1/plat/exportOpenPresetList", "预设开奖->导出"),
        /**
         * 预设开奖->新增批量预设
         */
        SAVE_OR_UPDATE_BATCH_OPEN_PRESET("/api/v1/plat/saveOrUpdateBatchOpenPreset", "预设开奖->新增批量预设"),
        /**
         * 预设开奖->获取当前期号信息
         */
        GET_LOTTERY_ACTION_NO("/api/v1/plat/getLotteryActionNo", "预设开奖->获取当前期号信息"),
        /**
         * 预设开奖分布
         */
        GET_OPEN_RATE_DISTRIBUTE("/api/v1/plat/getOpenRateDistribute", "预设开奖->预设开奖分布"),

        /**
         * 批量获取会员余额
         */
        GET_USERLIST("/api/v1/plat/userList", "批量获取会员余额");


        private String methodName;
        private String methodNameDesc;

        /**
         * @param methodName     方法名称
         * @param methodNameDesc 方法名称描述
         */
        UrlEnum(String methodName, String methodNameDesc) {
            this.methodName = methodName;
            this.methodNameDesc = methodNameDesc;
        }
    }

    /**
     * 预设开奖列表-> 请求实体
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class GetOpenPresetListReqBody {
        private String companyKey;
        private Integer platId;

        @ApiModelProperty(name = "lotteryId", value = "彩种ID[必填]", example = "100")
        private Integer lotteryId;
        @ApiModelProperty(name = "actionNo", value = "期号[必填]", example = "20201003275")
        private Long actionNo;
        @ApiModelProperty(name = "isShow", value = "是否显示历史数据；0-不显示，1-显示", example = "0")
        private Integer isShow = 0;
        @ApiModelProperty(name = "startTime", value = "开始时间", example = "1590735421")
        private Integer startTime;
        @ApiModelProperty(name = "endTime", value = "结束时间", example = "1590735421")
        private Integer endTime;

    }

    /**
     * 预设开奖新增/修改-> 请求实体
     */
    @Data
    class SaveOrUpdateOpenPresetReqBody {
        private String companyKey;
        private Integer platId;

        @ApiModelProperty(name = "id", value = "ID", example = "1")
        private Integer id;

        @ApiModelProperty(name = "lotteryId", value = "彩种ID[必填]", example = "100", required = true)
        private Integer lotteryId;

        @ApiModelProperty(name = "actionNo", value = "期号[必填]", example = "20201003275", required = true)
        private Long actionNo;

        @ApiModelProperty(name = "data", value = "开奖结果[必填]", example = "1", required = true)
        private String data;
    }

    /**
     * 预设开奖删除-> 请求实体
     */
    @Data
    class DeleteOpenPresetReqBody {
        private String companyKey;
        private Integer platId;

        @NotNull(message = "id不能为空")
        @ApiModelProperty(name = "id", value = "ID", example = "1")
        private Integer id;
    }

    /**
     * 预设开奖 -> 列表
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class GetOpenPresetListResBody {
        @ApiModelProperty(name = "id", value = "ID", example = "1")
        private Integer id;

        @ApiModelProperty(name = "platId", value = "平台ID", example = "1000")
        private Integer platId;

        @ApiModelProperty(name = "lotteryId", value = "彩种ID", example = "100")
        private Integer lotteryId;

        @ApiModelProperty(name = "actionNo", value = "期号", example = "20201003275")
        private Long actionNo;

        @ApiModelProperty(name = "data", value = "开奖结果", example = "1")
        private String data;

        @ApiModelProperty(name = "info", value = "补充信息")
        private String info;

        @ApiModelProperty(name = "createdAt", value = "创建时间", example = "1")
        private Integer createdAt;

        @ApiModelProperty(name = "createdAt", value = "创建时间", example = "1")
        private Integer updatedAt;

        @ApiModelProperty(name = "status", value = "状态：0-未使用，1-使用", example = "1")
        private Integer status;

    }


    /**
     * 预设开奖->导出 resBody
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class ExportOpenPresetListResBody {

        @ApiModelProperty(name = "lotteryId", value = "彩种ID", example = "100")
        private Integer lotteryId;

        @ApiModelProperty(name = "lotteryName", value = "彩种名称", example = "100")
        private String lotteryName;

        @ApiModelProperty(name = "actionNo", value = "期号", example = "20201003275")
        private Long actionNo;

        @ApiModelProperty(name = "data", value = "开奖结果", example = "1")
        private String data;

        @ApiModelProperty(name = "info", value = "补充信息")
        private String info;
    }

    /**
     * 预设开奖->获取当前期号信息
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class GetLotteryActionNoResBody {
        @ApiModelProperty(name = "lotteryId", value = "ID", example = "1000")
        public Integer lotteryId;
        @ApiModelProperty(name = "actionNo", value = "期号")
        public Long actionNo;
        @ApiModelProperty(name = "actionNum", value = "期数")
        public Integer actionNum;
    }

    /**
     * 预设开奖->新增批量预设-> 请求实体
     */
    @Data
    class SaveOrUpdateBatchOpenPresetReqBody {
        private String companyKey;
        private Integer platId;

        @NotNull(message = "lotteryId不能为空")
        @ApiModelProperty(name = "lotteryId", value = "彩种ID[必填]", example = "100", required = true)
        private Integer lotteryId;

        @NotNull(message = "actionNo不能为空")
        @ApiModelProperty(name = "actionNo", value = "期号[必填]", example = "20201003275", required = true)
        private Long actionNo;

        @NotNull(message = "nums不能为空")
        @ApiModelProperty(name = "nums", value = "期数[必填1-480]", example = "10", required = true)
        private Integer nums;
    }

    /**
     * 预设开奖->获取当前期号信息-> 请求实体
     */
    @Data
    class GetLotteryActionNoReqBody {
        private String companyKey;
        private Integer platId;

        @NotNull
        @ApiModelProperty(name = "lotteryId", value = "ID", example = "100", required = true)
        public Integer lotteryId;
    }

    /**
     * 预设开奖->获取当前期号信息-> 请求实体
     */
    @Data
    @Builder
    class GetUserCoinListByUserNameListReqBody {
        private String companyKey;
        private Integer platId;

        @ApiModelProperty(name = "usernameReqList", value = "会员姓名集合")
        public List<String> usernameReqList;
    }

    /**
     * 预设开奖->导出-> 请求实体
     */
    @Data
    class ExportOpenPresetListReqBody {
        private String companyKey;
        private Integer platId;


        @ApiModelProperty(name = "lotteryId", value = "彩种ID", example = "100")
        private Integer lotteryId;
        @ApiModelProperty(name = "actionNo", value = "期号", example = "20201003275")
        private Long actionNo;
        @ApiModelProperty(name = "startTime", value = "开始时间,不能跨天", example = "1590735421")
        private Integer startTime;
        @ApiModelProperty(name = "endTime", value = "结束时间,不能跨天", example = "1590735421")
        private Integer endTime;
        @ApiModelProperty(name = "isShow", value = "是否显示历史数据；0-不显示，1-显示", example = "0")
        private Integer isShow = 0;

    }

    /**
     * 根据会员名称查询余额 resBody
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class GetUserCoinListByUserNameListResBody {
        @ApiModelProperty(name = "username", value = "会员姓名")
        public String username;
        @ApiModelProperty(name = "coin", value = "会员余额")
        public BigDecimal coin;
    }

    @Data
    @ApiModel(value = "OpenRateDistributeReqDto", description = "开奖概率分布请求参数")
    class OpenRateDistributeReqDto {
        private String companyKey;

        @NotNull(message = "不能为空")
        @ApiModelProperty(name = "actionNo", value = "期号", required = true, example = "20201003275")
        private Long actionNo;

        @NotNull(message = "不能为空")
        @ApiModelProperty(name = "lotteryId", value = "彩种ID", required = true, example = "102")
        private Integer lotteryId;

        @ApiModelProperty(name = "platId", value = "平台ID", example = "1001")
        private Integer platId;

        @ApiModelProperty(name = "openCode", value = "预设开奖号码", example = "1")
        private Integer openCode;
    }

    @Data
    @ApiModel(value = "OpenRateDistributeResDto", description = "开奖概率分布响应参数")
    class OpenRateDistributeResDto {
        @ApiModelProperty(name = "betCoin", value = "投注金额", example = "1000.00")
        private BigDecimal betCoin = BigDecimal.ZERO;
        @ApiModelProperty(name = "openCoin", value = "开奖金额", example = "700.00")
        private BigDecimal openCoin = BigDecimal.ZERO;
        @ApiModelProperty(name = "openRate", value = "中奖概率", example = "70%")
        private String openRate = "0.00%";
        @ApiModelProperty(name = "openResult", value = "开奖结果", example = "")
        private List<OpenRate> openResult;
    }

    @Data
    @ApiModel(value = "OpenRate", description = "开奖结果请求参数")
    class OpenRate {
        @ApiModelProperty(name = "code", value = "开奖结果", example = "1")
        private Integer code;
        @ApiModelProperty(name = "coin", value = "开奖金额", example = "200.00")
        private BigDecimal coin = BigDecimal.ZERO;
        @ApiModelProperty(name = "rate", value = "开奖概率", example = "20%")
        private String rate = "0.00%";
    }
}
