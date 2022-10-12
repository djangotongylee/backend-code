package com.xinbo.sports.plat.io.bo;

import com.xinbo.sports.dao.generator.po.BetslipsDs;
import lombok.*;

import java.util.List;

/**
 * <p>
 * DS棋牌
 * </p>
 *
 * @author andy
 * @since 2020/7/15
 */
public interface DsChessRequestParameter {

    /**
     * 通用请求KEY
     */
    @Getter
    enum ReqKey {
        AGENT("agent"),
        ACCOUNT("account"),
        GAME_ID("game_id"),
        LANG("lang"),
        SERIAL("serial"),
        AMOUNT("amount"),
        OPER_TYPE("oper_type"),
        CHANNEL("channel"),
        DATA("data"),
        SIGN("sign"),
        FINISH_TIME("finish_time"),
        START_TIME("start_time"),
        END_TIME("end_time"),
        INDEX("index"),
        LIMIT("limit");
        private String key;

        ReqKey(String key) {
            this.key = key;
        }
    }

    /**
     * 响应-外层实体
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class DsResponse {
        private Result result;
        /**
         * 登录URL
         */
        private String url;
        /**
         * 平台餘額
         */
        private String balance;
        /**
         * 第三方订单号
         */
        private String trans_id;
        private String total;
        private List<BetslipsDs> rows;
    }

    /**
     * 响应-通用结果实体
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class Result {
        private Integer code;
        private String msg;
    }

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

        private String agent;

        private String channel;

        private String aesKey;

        private String signKey;
        /**
         * 币种
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

    @Getter
    enum DsUrlEnum {
        /**
         * 注册新会员
         */
        REGISTER("/v1/member/create", "注册新会员"),
        /**
         * 登录
         */
        LOGIN("/v1/member/login_game", "登录"),
        /**
         * 登出
         */
        LOGOUT("/v1/member/logout", "登出"),
        /**
         * 余额查询
         */
        BALANCE("/v1/trans/check_balance", "余额查询"),
        /**
         * 转账
         */
        TRANSFER("/v1/trans/transfer", "转账"),
        /**
         * 查詢交易單狀態
         */
        TRANS_VERIFY("/v1/trans/verify", "查詢交易單狀態"),
        /**
         * 拉单接口
         */
        GET_BET_RECORDS("/v1/record/get_bet_records", "取得玩家下注紀錄");


        private String methodName;
        private String methodNameDesc;

        /**
         * @param methodName     方法名称
         * @param methodNameDesc 方法名称描述
         */
        DsUrlEnum(String methodName, String methodNameDesc) {
            this.methodName = methodName;
            this.methodNameDesc = methodNameDesc;
        }
    }
}
