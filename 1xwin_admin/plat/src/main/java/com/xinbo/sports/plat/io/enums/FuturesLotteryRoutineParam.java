package com.xinbo.sports.plat.io.enums;

import com.google.common.collect.ImmutableMap;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

/**
 * <p>
 * FuturesLottery
 * </p>
 *
 * @author andy
 * @since 2020/9/28
 */
public interface FuturesLotteryRoutineParam {
    /**
     * 错误码映射
     */
    Map<Integer, CodeInfo> ERROR_CODE_MAPPER = new ImmutableMap
            .Builder<Integer, CodeInfo>().
            put(ErrorCode.E8.getCode(), CodeInfo.PLAT_INVALID_PARAM).
            put(ErrorCode.E1.getCode(), CodeInfo.PLAT_ACCOUNT_EXISTS).
            put(ErrorCode.E2.getCode(), CodeInfo.PLAT_ACCOUNT_NOT_EXISTS).
            put(ErrorCode.E3.getCode(), CodeInfo.PLAT_COIN_INSUFFICIENT).
            put(ErrorCode.E5.getCode(), CodeInfo.PLAT_INVALID_LANGUAGE).
            put(ErrorCode.E9.getCode(), CodeInfo.PLAT_INVALID_LANGUAGE).
            put(ErrorCode.E12.getCode(), CodeInfo.PLAT_TRANSFER_ID_INVALID).
            put(ErrorCode.E13.getCode(), CodeInfo.PLAT_UNDER_MAINTENANCE).
            put(ErrorCode.E14.getCode(), CodeInfo.PLAT_TRANSFER_ID_INVALID).
            put(ErrorCode.E15.getCode(), CodeInfo.PLAT_LOTTERY_ACTION_NO_EXPIRED).
            put(ErrorCode.E16.getCode(), CodeInfo.PLAT_ACCOUNT_OCCUPATION).
            put(ErrorCode.E17.getCode(), CodeInfo.PLAT_ACCOUNT_NO_MODIFIED).
            put(ErrorCode.E18.getCode(), CodeInfo.PLAT_ACCOUNT_NO_DELETE)
            .build();


    /**
     * 语言
     */
    @Getter
    enum Lang {
        ZH_CN("zh-CN", "简体中文"),
        ZH_TW("zh-TW", "繁体中文"),
        EN_US("en-US", "英文"),
        VI_VN("vi-VN", "越南语"),
        TH_TH("th-TH", "泰文");
        /**
         * code
         */
        private String code;
        /**
         * 描述
         */
        private String desc;

        Lang(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }

    @Getter
    enum ErrorCode {
        SUCCESS(0, "成功"),
        E1(66012, "账号已存在"),
        E2(66013, "账号不存在"),
        E3(66014, "账户余额不足"),
        E4(66015, "金额不能低于1元"),
        E5(66016, "语言设置不正确"),
        E6(66017, "companyKey不存在"),
        E7(66018, "orderPlat不能重复"),
        E8(66100, "参数无效"),
        E9(66101, "语言无效"),
        E10(66102, "设备来源无效"),
        E11(66103, "IP访问超限"),
        E12(66104, "订单号无效"),
        E13(66105, "platId不存在"),
        E14(66106, "交易记录不存在"),
        E15(40004, "期号已过期"),
        E16(66107, "账号已冻结"),
        E17(66108, "期号过期不能修改!"),
        E18(66110, "期号过期不能删除!");
        Integer code;
        String message;

        ErrorCode(Integer code, String message) {
            this.code = code;
            this.message = message;
        }
    }

    /**
     * 拉单接口-status ->xbStatus
     * <p>
     * <p>
     * // 1-赢 2-输 3-和 4-取消 5-等待结算 6-赛事取消 7-投注确认 8-投注拒绝 9-赢一半 10-输一半
     */
    @Getter
    @AllArgsConstructor
    enum FuturesLotteryStatus {
        /**
         * 等待开奖
         */
        WAIT("WAIT", 5),
        /**
         * 赢
         */
        WIN("WIN", 1),
        /**
         * 输
         */
        LOST("LOST", 2),
        /**
         * 平局
         */
        TIE("TIE", 3),
        /**
         * 取消
         */
        CANCEL("CANCEL", 6);
        String code;
        Integer xbStatus;
    }

    /**
     * status -> xbStatus
     */
    Map<String, Integer> STATUS_2_XBSTATUS = Map.of(
            FuturesLotteryStatus.WAIT.getCode(), FuturesLotteryStatus.WAIT.getXbStatus()
            , FuturesLotteryStatus.WIN.getCode(), FuturesLotteryStatus.WIN.getXbStatus()
            , FuturesLotteryStatus.LOST.getCode(), FuturesLotteryStatus.LOST.getXbStatus()
            , FuturesLotteryStatus.TIE.getCode(), FuturesLotteryStatus.TIE.getXbStatus()
            , FuturesLotteryStatus.CANCEL.getCode(), FuturesLotteryStatus.CANCEL.getXbStatus());
}
