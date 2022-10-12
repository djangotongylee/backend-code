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

public interface SV388PlatEnum {


    /**
     * mg方法枚举
     */
    @Getter
    @AllArgsConstructor
    enum SV388MethodEnum {
        CREATEMEMBER("/createMember.aspx?operatorcode={operatorcode}&username={username}&signature={signature}", "创建玩家"),
        BALANCE("/getBalance.aspx?operatorcode={operatorcode}&providercode={providercode}&username={username}&password={password}&signature={signature}", "获取余额"),
        TRANSFER("/makeTransfer.aspx?operatorcode={operatorcode}&providercode={providercode}&username={username}&password={password}&referenceid={referenceid}&type={type}&amount={amount}&signature={signature}", "转账"),
        LAUNCHGAMES("/launchGames.aspx?operatorcode={operatorcode}&providercode={providercode}&username={username}&password={password}&type={type}&lang={lang}&html5=1&signature={signature}", "登陆"),
        GAMELIST("/getGameList.aspx", "游戏列表"),
        FETCHBET("/fetchbykey.aspx?operatorcode={operatorcode}&versionkey={versionkey}&signature={signature}", "拉取投注记录"),
        MARKBET("/mark.aspx?operatorcode={operatorcode}&ticket={ticket}&signature={signature}", "标记投注记录"),
        ;

        private String methodName;
        private String methodNameDesc;
    }

    /**
     * 0	SUCCESS 请求成功
     * 71	INVALID_REFERENCE_ID 单据号不正确
     * 73	INVALID_TRANSFER_AMOUNT 转账金额不正确
     * 81	MEMBER_NOT_FOUND 会员账号不存在
     * 82	MEMBER_EXISTED 会员账号已存在
     * 83	OPERATOR_EXISTED 代理号已存在
     * 90	INVALID_PARAMETER 请求参数不正确
     * 91	INVALID_OPERATOR 代理号不正确
     * 92	INVALID_PROVIDERCODE 供应商代号不正确
     * 93	INVALID_PARAMETER_TYPE 请求参数类型不正确
     * 94	INVALID_PARAMETER_USERNAME 账号不正确
     * 95	INVALID_PARAMETER_PASSWORD 密码不正确
     * 96	INVALID_PARAMETER_OPASSWORD 旧密码不正确
     * 97	INVALID_PARAMETER_EMPTY_DOMAINNAME 请求链接/域名不正确
     * 98	INVALID_USERNAME_OR_PASSWORD 账号/密码错误
     * 99	INVALID_SIGNATURE 加密错误
     * 992	INVALID_PARAMETER_PRODUCT_NOT_SUPPORTED_GAMETYPE 平台不兼容请求的游戏类型
     * 991	OPERATOR_STATUS_INACTIVE 代理号已冻结
     * 994	ACCESS_PROHIBITED 接口访问被禁止
     * 995	PRODUCT_NOT_ACTIVATED 平台未开通
     * 996	PRODUCT_NOT_AVAILABLE 平台不支持
     * 998	PLEASE_CONTACT_CSD 请联系客服
     * 999	UNDER_MAINTENENCE 系统维护中
     * 9999	UNKNOWN_ERROR 未知错误
     * -997	SYS_EXCEPTION, Please contact CS. 系统错误，请联络客服。
     * -998	API_KIOSK_INSUFFICIENT_BALANCE 集成系统接口余额不足
     * -999	API_ERROR 接口错误
     * 600	pre-check stage FAILED, deposit/withdraw transaction IGNORED 前期检验失败。 存款/取款 操作已被无视
     * 601	DEPO_APIREQ_BLOCKED_FOR_THIS_PRODUCT_TILL_FURTHER_NOTICE 此产品的存款 功能暂时停用维修
     * 602	WITH_APIREQ_BLOCKED_FOR_THIS_PRODUCT_TILL_FURTHER_NOTICE 此产品的取款 功能暂时停用维修
     */
    Map<Integer, CodeInfo> MAP = Maps.of(
            82, CodeInfo.ACCOUNT_EXISTS,
            61, CodeInfo.PLAT_INVALID_CURRENCY,
            70, CodeInfo.PLAT_PLAT_NO_SUFFICIENT,
            72, CodeInfo.PLAT_COIN_INSUFFICIENT,
            73, CodeInfo.PLAT_INVALID_PARAM,
            81, CodeInfo.PLAT_ACCOUNT_NOT_EXISTS,
            83, CodeInfo.PLAT_INVALID_AGENT_ACCOUNT,
            90, CodeInfo.PLAT_INVALID_PARAM,
            91, CodeInfo.PLAT_INVALID_AGENT_ACCOUNT,
            92, CodeInfo.PLAT_INVALID_PARAM,
            994, CodeInfo.PLAT_IP_NOT_ACCESS,
            995, CodeInfo.PLAT_UNDER_MAINTENANCE,
            996, CodeInfo.PLAT_UNDER_MAINTENANCE,
            999, CodeInfo.PLAT_UNDER_MAINTENANCE,
            -998, CodeInfo.PLAT_PLAT_NO_SUFFICIENT
    );

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class PlatConfig {
        String providerCode;
        String operatorCode;
        String secretKey;
        String currency;
        String apiUrl;
        String logUrl;
        String environment;
        String type;
    }
}
