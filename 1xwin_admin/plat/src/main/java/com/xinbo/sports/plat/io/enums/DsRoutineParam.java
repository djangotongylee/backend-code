package com.xinbo.sports.plat.io.enums;

import com.google.common.collect.ImmutableMap;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.Getter;

import java.util.Map;

/**
 * <p>
 * DS棋牌
 * </p>
 *
 * @author andy
 * @since 2020/8/3
 */
public interface DsRoutineParam {
    /**
     * 错误码映射
     */
    Map<Integer, CodeInfo> ERROR_CODE_MAPPER = new ImmutableMap
            .Builder<Integer, CodeInfo>().
            put(ErrorCode.E8.getCode(), CodeInfo.PLAT_INVALID_PARAM).
            put(ErrorCode.E12.getCode(), CodeInfo.PLAT_INVALID_PARAM).
            put(ErrorCode.E1001.getCode(), CodeInfo.PLAT_INVALID_PARAM).
            put(ErrorCode.E9.getCode(), CodeInfo.PLAT_ACCOUNT_OCCUPATION).
            put(ErrorCode.E11.getCode(), CodeInfo.PLAT_INVALID_AGENT_ACCOUNT).
            put(ErrorCode.E10.getCode(), CodeInfo.PLAT_ACCOUNT_NOT_EXISTS).
            put(ErrorCode.E6.getCode(), CodeInfo.PLAT_ACCOUNT_NOT_EXISTS).
            put(ErrorCode.E7.getCode(), CodeInfo.PLAT_TIME_OUT)
            .build();

    /**
     * 语言
     */
    @Getter
    enum Lang {
        ZH_CN("zh_cn", "簡中"),
        EN_US("en_us", "英文"),
        VI("vi_vn", "越南文"),
        TH("th_th", "泰文"),
        ID("id_id", "印尼文");
        /**
         * 代号
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

    /**
     * 异常码
     * <p>
     * 0	未知錯誤
     * 1	成功
     * 2	重複
     * 3	必填欄位
     * 4	登入失敗
     * 5	API存取失敗
     * 6	找不到資訊
     * 7	請求超時
     * 8	驗證碼錯誤
     * 9	用戶鎖定
     * 10	找不到玩家
     * 11	找不到代理
     * 12	数据类型错误
     * 1000	簽章失敗
     * 1001	金額錯誤
     * 1002	交易序號重複
     * 1003	驗證失敗
     * 1004	資料庫存取失敗
     * 1005	找不到錢包
     * 1006	找不到交易
     * 1007	處理失敗
     * 1008	交易金額不得為負數
     * 1009	找不到代理配額
     * 1010	代理配額不足
     * 1011	代理提款鎖定
     * 1012	時間超出範圍
     * 1013	超過小數點兩位
     * 1028	遊戲未結算
     */
    @Getter
    enum ErrorCode {
        E0(0, "未知錯誤"),
        E1(1, "成功"),
        E2(2, "重複"),
        E3(3, "必填欄位"),
        E4(4, "登入失敗"),
        E5(5, "API存取失敗"),
        E6(6, "找不到資訊"),
        E7(7, "請求超時"),
        E8(8, "驗證碼錯誤"),
        E9(9, "用戶鎖定"),
        E10(10, "找不到玩家"),
        E11(11, "找不到代理"),
        E12(12, "数据类型错误"),
        E1000(1000, "簽章失敗"),
        E1001(1001, "金額錯誤"),
        E1002(1002, "交易序號重複"),
        E1003(1003, "驗證失敗"),
        E1004(1004, "資料庫存取失敗"),
        E1005(1005, "找不到錢包"),
        E1006(1006, "找不到交易"),
        E1007(1007, "處理失敗"),
        E1008(1008, "交易金額不得為負數"),
        E1009(1009, "找不到代理配額"),
        E1010(1010, "代理配額不足"),
        E1011(1011, "代理提款鎖定"),
        E1012(1012, "時間超出範圍"),
        E1013(1013, "超過小數點兩位"),
        E1028(1028, "遊戲未結算");
        int code;
        String message;

        ErrorCode(int code, String message) {
            this.code = code;
            this.message = message;
        }
    }

    /**
     * 币种
     * CNY	人民幣
     * EUR	歐元
     * GBP	英鎊
     * HKD	港幣
     * IDR	印尼盾 (K)
     * JPY	日元
     * KRW	韓元
     * MYR	馬來西亞令吉
     * SGD	新加坡元
     * THB	泰銖
     * USD	美元
     * VND	越南盾 (K)
     * MMK	緬甸元 (K)
     * RUB	俄羅斯盧布
     */
    @Getter
    enum Currency {
        CNY("CNY", "人民币"),
        EUR("EUR", "歐元"),
        GBP("GBP", "英鎊"),
        HKD("HKD", "港幣"),
        IDR("IDR", "印尼盾 (K)"),
        JPY("JPY", "日元"),
        KRW("KRW", "韓元"),
        MYR("MYR", "馬來西亞令吉"),
        SGD("SGD", "新加坡元"),
        THB("THB", "泰銖"),
        USD("USD", "美元"),
        VND("VND", "越南盾 (K)"),
        MMK("MMK", "緬甸元 (K)"),
        RUB("RUB", "俄羅斯盧布");

        /**
         * 币种
         */
        private String code;
        /**
         * 币种说明
         */
        private String desc;

        Currency(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }
}
