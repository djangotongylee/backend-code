package com.xinbo.sports.plat.io.enums;

import com.xinbo.sports.utils.components.response.CodeInfo;
import org.apache.groovy.util.Maps;

import java.util.Map;

/**
 * @author: wells
 * @date: 2020/8/24
 * @description:
 */

public class IBCExceptionParam {

    private IBCExceptionParam() {

    }

    /**
     * 登录
     * 0 OK 执行成功
     * 1 Failed 执行过程中失败
     * 2 Failed 会员不存在
     * 9 Failed 厂商标识符失效
     * 10 Failed 系统维护中
     */

    private static final Map<Integer, CodeInfo> LOGIN_MAP = Maps.of(
            1, CodeInfo.PLAT_SYSTEM_ERROR,
            2, CodeInfo.PLAT_ACCOUNT_NOT_EXISTS,
            9, CodeInfo.PLAT_SYSTEM_ERROR,
            10, CodeInfo.PLAT_UNDER_MAINTENANCE
    );
    /**
     * 创建登录会员
     * 0 OK 执行成功
     * 1 Failed 执行失败
     * 2 Failed 会员名称（username）重复、使用不支援的字元
     * 3 Failed OperatorId 错误
     * 4 Failed 赔率类型格式错误
     * 5 Failed 币别格式错误
     * 6 Failed厂商会员识别码(vendor_member_id)重复、使用不支援的字元
     * 7 Failed最小限制转账金额大于最大限制转账金额 8 Failed 无效的前缀字符
     * 9 Failed 厂商标识符失效
     * 10 Failed 系统维护中
     * 11 Failed 最小限制转账金额小于 1
     * 12 Failed 长度限制 30( Vendor_Member_ID orUserName)
     */
    private static final Map<Integer, CodeInfo> CR_MAP = Maps.of(
            1, CodeInfo.PLAT_SYSTEM_ERROR,
            2, CodeInfo.PLAT_SYSTEM_ERROR,
            3, CodeInfo.PLAT_SYSTEM_ERROR,
            4, CodeInfo.PLAT_SYSTEM_ERROR,
            5, CodeInfo.PLAT_INVALID_CURRENCY,
            6, CodeInfo.PLAT_SYSTEM_ERROR,
            7, CodeInfo.PLAT_SYSTEM_ERROR,
            9, CodeInfo.PLAT_SYSTEM_ERROR,
            10, CodeInfo.PLAT_UNDER_MAINTENANCE,
            11, CodeInfo.PLAT_SYSTEM_ERROR,
            12, CodeInfo.PLAT_SYSTEM_ERROR
    );

    /**
     * 查询余额
     * 0 OK 执行成功
     * 2 Failed 会员不存在
     * 3 OK 会员被封存
     * 6 Failed 会员尚未转账过
     * 7 Failed 取得非Sportsbook用户余额错误
     */
    private static final Map<Integer, CodeInfo> BL_MAP = Maps.of(
            0, CodeInfo.PLAT_SYSTEM_ERROR,
            2, CodeInfo.PLAT_ACCOUNT_NOT_EXISTS,
            3, CodeInfo.PLAT_SYSTEM_ERROR,
            6, CodeInfo.PLAT_SYSTEM_ERROR,
            7, CodeInfo.PLAT_SYSTEM_ERROR
    );

    /**
     * 转账
     * 0 OK 执行成功
     * 1 Failed 执行过程中失败
     * 2 Failed 会员不存在
     * 3 Failed 会员余额不足
     * 4 Failed比最小或最大限制的转账金额 还更少或更多
     * 5 Failed 重复的 vendor_trans_id
     * 6 Failed 币别错误
     * 7 Failed 传入参数错误
     * 8 Failed玩家盈余限制(玩家赢超过系统 可转出有效值时) 9 Failed 厂商标识符失效
     * 10 Failed 系统维护中
     * 11 Failed 系统忙绿中，请稍后再试
     * 12 Failed 无效的前缀字符
     * 13 Failed 会员不能被解封存
     */
    private static final Map<Integer, CodeInfo> TR_MAP = Maps.of(
            0, CodeInfo.PLAT_SYSTEM_ERROR,
            1, CodeInfo.PLAT_SYSTEM_ERROR,
            2, CodeInfo.PLAT_ACCOUNT_NOT_EXISTS,
            3, CodeInfo.PLAT_COIN_INSUFFICIENT,
            4, CodeInfo.PLAT_SYSTEM_ERROR,
            5, CodeInfo.PLAT_SYSTEM_ERROR,
            6, CodeInfo.PLAT_INVALID_CURRENCY,
            7, CodeInfo.PLAT_SYSTEM_ERROR,
            8, CodeInfo.PLAT_SYSTEM_ERROR,
            10, CodeInfo.PLAT_UNDER_MAINTENANCE,
            11, CodeInfo.PLAT_TIME_OUT,
            12, CodeInfo.PLAT_SYSTEM_ERROR,
            13, CodeInfo.PLAT_SYSTEM_ERROR
    );
    /**
     * 方法集合
     */
    private static final Map<String, Map<Integer, CodeInfo>> METHOD_MAP = Maps.of(
            "登录用户", LOGIN_MAP,
            "注册用户", CR_MAP,
            "查询余额", BL_MAP,
            "资金转账", TR_MAP
    );

    /**
     * 获取异常信息
     *
     * @param code
     * @param methodNameDesc
     */
    public static CodeInfo getExceptionMessage(Integer code, String methodNameDesc) {
        return IBCExceptionParam.METHOD_MAP.get(methodNameDesc)
                .getOrDefault(code, CodeInfo.PLAT_SYSTEM_ERROR);
    }
}
