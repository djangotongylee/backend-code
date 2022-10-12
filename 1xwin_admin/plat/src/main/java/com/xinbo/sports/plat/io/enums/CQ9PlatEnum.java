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

public interface CQ9PlatEnum {


    /**
     * mg方法枚举
     */
    @Getter
    @AllArgsConstructor
    enum CG9MethodEnum {
        CREATEPLAYER("/gameboy/player", "建立 Player"),
        GETTIMESTAMP("/gameboy/ping", "取得時戳"),
        LOGIN("/gameboy/player/login", "登陆"),
        LOBBY("/gameboy/player/lobbylink", "取得遊戲大廳連結"),
        CHESSLOBBY("/gameboy/player/lobbylink/table", "取得棋牌遊戲大廳連結"),
        GAMELINK("/gameboy/player/gamelink", "Player取得遊戲連結"),
        WITHDRAWW("/gameboy/player/withdraw", "取款"),
        DOPOSIT("/gameboy/player/deposit", "存款"),
        BALANCE("/gameboy/player/balance/", "余额"),
        CHECKTRANSACTION("/gameboy/transaction/record/", "查询交易记录"),
        GAMELIST("/gameboy/game/list/", "获取游戏列表"),
        GETGAMEORDER("/gameboy/order/view?starttime={starttime}&endtime={endtime}&page={page}", "获取游戏记录");

        private String methodName;
        private String methodNameDesc;
    }


    /**
     * 错误码集合
     * 1	Insufficient balance.
     * 2	Player not found.
     * 3	Token invalid.
     * 4	Authorization invalid.
     * 5	Bad parameters.
     * 6	Already has same account.
     * 7	Method not allowed.
     * 8	Data not found.
     * 9	MTCode duplicate.
     * 10	Time format error.
     * 11	Query time is out of range.
     * 12	Time zone must be UTC-4.
     * 13	Game is not found.
     * 14	Your account or password is incorrect.
     * 15	Account or password must use the following characters:a-z A-Z 0-9 -_
     * 23	Game is under maintenance.
     * 24	Account too long.
     * 28	Currency is not support.
     * 29	No default pool type
     * 30	Pool uninitialize.
     * 31	Currency does not match Agent’s currency.
     * 33	Transaction in progress, please check later.
     * 100	Something wrong.
     * 101	Auth service error.
     * 102	User service error.
     * 103	Transaction service error
     * 104	Game Manager service error
     * 105	Wallet service error.
     * 106	Tviewer service error.
     * 107	Orderview service error.
     * 108	Report service error.
     * 110	Promote service error.
     * 111	PromoteRed service error.
     * 112	LuckyBag service error.
     * 200	This owner has been frozen.
     * 201	This owner has been disable.
     * 202	This player has been disable.
     */

    /**
     * 方法集合
     */
    Map<Integer, CodeInfo> METHOD_MAP = Maps.of(
            1, CodeInfo.PLAT_COIN_INSUFFICIENT,
            2, CodeInfo.PLAT_ACCOUNT_NOT_EXISTS,
            3, CodeInfo.PLAT_INVALID_PARAM,
            4, CodeInfo.PLAT_INVALID_PARAM,
            5, CodeInfo.PLAT_INVALID_PARAM,
            6, CodeInfo.PLAT_INVALID_PARAM,
            7, CodeInfo.PLAT_INVALID_PARAM,
            14, CodeInfo.PLAT_ACCOUNT_NOT_EXISTS,
            9, CodeInfo.PLAT_INVALID_PARAM,
            10, CodeInfo.PLAT_INVALID_PARAM,
            11, CodeInfo.PLAT_INVALID_PARAM,
            12, CodeInfo.PLAT_INVALID_PARAM,
            13, CodeInfo.PLAT_INVALID_PARAM,
            15, CodeInfo.PLAT_INVALID_PARAM,
            23, CodeInfo.PLAT_UNDER_MAINTENANCE,
            24, CodeInfo.PLAT_INVALID_PARAM,
            28, CodeInfo.PLAT_INVALID_PARAM
    );

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class PlatConfig {
        String token;
        String apiUrl;
        String currency;
        String environment;
    }
}
