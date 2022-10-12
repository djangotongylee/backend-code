package com.xinbo.sports.plat.io.enums;

import com.google.common.collect.ImmutableMap;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.*;
import org.apache.groovy.util.Maps;

import java.util.Map;

/**
 * @author: wells
 * @date: 2020/5/28
 * @description:
 */

public interface PGPlatEnum {


    /**
     * pg方法枚举
     */
    @Getter
    @AllArgsConstructor
    enum PGMethodEnum {
        GET_BALANCE("/v3/Cash/GetPlayerWallet", "查询余额"),
        LAUNCH_GAME("/v1/Login/LoginGame", "登陆游戏"),
        DEPOSIT("/v3/Cash/TransferIn", "存款"),
        WITHDRAW("/v3/Cash/TransferOut", "取款"),
        CREATE_PLAYER("/v1/Player/Create", "创建玩家"),
        WITHDRAWAL_ALL("/v3/Cash/TransferAllOut", "取全款"),
        URL_SCHEME("/{GameCode}/index.html", "登陆"),
        LOGIN_PROXY("/v1/Login/LoginProxy", "取运营商令牌"),
        GetHistoryForSpecificTimeRange("/Bet/v4/GetHistoryForSpecificTimeRange", "获取特定时间内注单"),
        GAME_LIST("/Game/v2/Get", "获取游戏列表"),
        ;


        private String methodName;
        private String methodNameDesc;
    }


    /**
     * 错误码集合
     */
    Map<Integer, CodeInfo> LOGIN_MAP = Maps.of(
            400, CodeInfo.PLAT_INVALID_PARAM,
            401, CodeInfo.PLAT_SYSTEM_ERROR,
            409, CodeInfo.PLAT_ACCOUNT_OCCUPATION,
            500, CodeInfo.PLAT_SYSTEM_ERROR
    );
    /**
     * 1034 Invalid request
     * 1035 Operation Failed
     * 1200 Internal server error
     * 1204 Invalid operator
     * 1305 Invalid player (player already existed)
     * 1315 Player’s operation in progress
     */
    Map<Integer, CodeInfo> CR_MAP = Maps.of(
            1034, CodeInfo.PLAT_INVALID_PARAM,
            1035, CodeInfo.PLAT_REGISTER_USER_ERROR,
            1305, CodeInfo.PLAT_ACCOUNT_EXISTS,
            1200, CodeInfo.PLAT_SYSTEM_ERROR,
            1117, CodeInfo.PLAT_SYSTEM_ERROR,
            1315, CodeInfo.PLAT_SYSTEM_ERROR
    );
    /**
     * 3001 不能空值
     * 3005 玩家钱包不存在
     */
    Map<Integer, CodeInfo> BL_MAP = Maps.of(
            1305, CodeInfo.PLAT_ACCOUNT_NOT_EXISTS,
            3001, CodeInfo.PLAT_INVALID_PARAM,
            3005, CodeInfo.PLAT_ACCOUNT_NOT_EXISTS
    );
    /**
     * 3001 不能空值
     * 3005 玩家钱包不存在
     * 3100 转账失败
     * 3101 转账请求进行中，请重试查看最新状态
     */
    Map<Integer, CodeInfo> TR_MAP = Maps.of(
            3001, CodeInfo.PLAT_INVALID_PARAM,
            3005, CodeInfo.PLAT_ACCOUNT_NOT_EXISTS,
            1305, CodeInfo.PLAT_ACCOUNT_NOT_EXISTS,
            3100, CodeInfo.COIN_TRANSFER_INVALID,
            3101, CodeInfo.TRY_AGAIN,
            3013, CodeInfo.COIN_NOT_ENOUGH,
            3015, CodeInfo.TRY_AGAIN
    );
    Map<Integer, CodeInfo> TV_MAP = Maps.of(
            400, CodeInfo.PLAT_INVALID_PARAM,
            401, CodeInfo.PLAT_IP_NOT_ACCESS,
            500, CodeInfo.PLAT_SYSTEM_ERROR
    );

    /**
     * 方法集合
     */
    Map<String, Map<Integer, CodeInfo>> METHOD_MAP = Maps.of(
            "登录用户", LOGIN_MAP,
            "创建玩家", CR_MAP,
            "查询余额", BL_MAP,
            "转账校验", TV_MAP,
            "存款", TR_MAP,
            "取款", TR_MAP
    );


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class PlatConfig {
        String operatorToken;
        String secretKey;
        String apiUrl;
        String dataUrl;
        String launchUrl;
        String environment;
        String currency;
        String lobbyUrl;
    }
}
