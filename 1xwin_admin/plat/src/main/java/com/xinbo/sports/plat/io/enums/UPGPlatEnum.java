package com.xinbo.sports.plat.io.enums;

import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.*;
import org.apache.groovy.util.Maps;

import java.util.Map;

/**
 * @author: wells
 * @date: 2020/5/28
 * @description:
 */

public interface UPGPlatEnum {


    /**
     * UPG方法枚举
     */
    @Getter
    @AllArgsConstructor
    enum UPGMethodEnum {
        TOKEN("/connect/token", "获取token"),
        CREATEPLAYER("/players", "创建玩家"),
        GETPLAYERDETAILS("/players/%s?properties={properties}", "获取玩家信息"),
        CREATETRANSACTION("/WalletTransactions", "创建资金交易"),
        CHECKTRANSATIONS("/WalletTransactions?idempotencyKey={idempotencyKey}", "转账校验"),
        GETCONTENTURL("/players/%s/sessions", "获取内容网址"),
        GETBETSDETAILS("/bets?limit={limit}", "获取下注信息"),
        GETGAMES("/games?agentCode={agentCode}", "获取游戏列表");

        private String methodName;
        private String methodNameDesc;
    }

    Map<Integer, CodeInfo> LOGIN_MAP = Maps.of(
            400, CodeInfo.PLAT_INVALID_PARAM,
            401, CodeInfo.PLAT_SYSTEM_ERROR,
            409, CodeInfo.PLAT_ACCOUNT_OCCUPATION,
            500, CodeInfo.PLAT_SYSTEM_ERROR
    );
    Map<Integer, CodeInfo> CR_MAP = Maps.of(
            400, CodeInfo.PLAT_INVALID_PARAM,
            401, CodeInfo.PLAT_SYSTEM_ERROR,
            404, CodeInfo.PAY_USER_INVALID,
            500, CodeInfo.PLAT_SYSTEM_ERROR
    );
    Map<Integer, CodeInfo> BL_MAP = Maps.of(
            400, CodeInfo.PLAT_INVALID_PARAM,
            401, CodeInfo.PLAT_SYSTEM_ERROR,
            404, CodeInfo.PLAT_ACCOUNT_NOT_EXISTS,
            500, CodeInfo.PLAT_SYSTEM_ERROR
    );

    Map<Integer, CodeInfo> TR_MAP = Maps.of(
            400, CodeInfo.PLAT_INVALID_PARAM,
            401, CodeInfo.PLAT_SYSTEM_ERROR,
            409, CodeInfo.PLAT_PLAT_NO_SUFFICIENT,
            500, CodeInfo.PLAT_SYSTEM_ERROR
    );

    /**
     * 方法集合
     */
    Map<String, Map<Integer, CodeInfo>> METHOD_MAP = Maps.of(
            "登录用户", LOGIN_MAP,
            "注册用户", CR_MAP,
            "查询余额", BL_MAP,
            "资金转账", TR_MAP
    );

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class PlatConfig {
        String tokenUrl;
        String apiUrl;
        String grantType;
        String clientSecret;
        String clientId;
        String environment;
    }
}
