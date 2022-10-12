package com.xinbo.sports.plat.io.enums;

import com.google.common.collect.ImmutableMap;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.Getter;

import java.util.Map;

/**
 * <p>
 * WM真人
 * </p>
 *
 * @author andy
 * @since 2020/5/20
 */
public interface WMRoutineParam {
    /**
     * 错误码映射
     */
    Map<Integer, CodeInfo> ERROR_CODE_MAPPER = new ImmutableMap.Builder<Integer, CodeInfo>().
            put(MemberRegisterErrorCode.E104.getCode(), CodeInfo.PLAT_ACCOUNT_EXISTS).
            put(ChangeBalanceErrorCode.E10501.getCode(), CodeInfo.PLAT_ACCOUNT_NOT_EXISTS).
            put(ChangeBalanceErrorCode.E10805.getCode(), CodeInfo.PLAT_COIN_INSUFFICIENT).
            put(ChangeBalanceErrorCode.E10804.getCode(), CodeInfo.PLAT_REQUEST_FREQUENT).
            put(ChangeBalanceErrorCode.E10806.getCode(), CodeInfo.PLAT_COIN_OVER_MAX_LIMIT).
            put(ErrorCode.E10411.getCode(), CodeInfo.PLAT_REQUEST_FREQUENT).
            put(ErrorCode.E10418.getCode(), CodeInfo.PLAT_REQUEST_FREQUENT).
            put(ErrorCode.E911.getCode(), CodeInfo.PLAT_UNDER_MAINTENANCE)
            .build();

    /**
     * 语言
     */
    @Getter
    enum LANGS {
        CN(0, "简体中文"),
        EN(1, "英文"),
        TH(2, "泰文"),
        VI(3, "越文"),
        JP(4, "日文"),
        KO(5, "韩文"),
        IN(6, "印度文"),
        MS(7, "马来西亚文"),
        ID(8, "印尼文"),
        TW(9, "繁體中文"),
        XW(10, "西文");
        int code;
        String message;

        LANGS(int code, String message) {
            this.code = code;
            this.message = message;
        }
    }

    /**
     * 注册异常码
     */
    @Getter
    enum MemberRegisterErrorCode {
        E104(104, "新增会员资料错误,此帐号已被使用!!"),
        E10404(10404, "帐号长度过长"),
        E10405(10405, "帐号长度过短"),
        E10406(10406, "密码长度过短"),
        E10407(10407, "密码长度过长"),
        E10409(10409, "姓名长度过长"),
        E10502(10502, "帐号名不得为空"),
        E10508(10508, "密码不得为空"),
        E10509(10509, "姓名不得为空"),
        E10419(10419, "筹码格式错误(请用逗号隔开)"),
        E10420(10420, "筹码个数错误(介于5-10个)"),
        E10421(10421, "筹码种类错误"),
        E10422(10422, "帐号只接受英文、数字、下划线与@");
        int code;
        String message;

        MemberRegisterErrorCode(int code, String message) {
            this.code = code;
            this.message = message;
        }
    }

    /**
     * 加扣点异常码(上下分)
     */
    @Getter
    enum ChangeBalanceErrorCode {
        E901(901, "转点失败"),
        E10410(10410, "会员上笔交易未成功，请联系客服人员解锁"),
        E10501(10501, "查无此帐号，请检查"),
        E10507(10507, "此账号非此代理下线，不能使用此功能"),
        E10801(10801, "加扣点不得为零"),
        E10802(10802, "加扣点为空，或未设置(money)参数"),
        E10803(10803, "加扣点不得为汉字"),
        E10804(10804, "不得5秒內重复转帐"),
        E10805(10805, "转账失败，该账号余额不足"),
        E10806(10806, "转账失败，账户代理已超过信用额度"),
        E10807(10807, "转帐失败，该笔单号已存在");
        int code;
        String message;

        ChangeBalanceErrorCode(int code, String message) {
            this.code = code;
            this.message = message;
        }
    }

    /**
     * 其他异常码
     */
    @Getter
    enum ErrorCode {
        E103(103, "代理商ID与识别码格式错误"),
        E900(900, "查无此函数"),
        E911(911, "维护中"),
        E10201(10201, "此功能仅能查询一天内的报表，您已超过上限"),
        E10301(10301, "代理商ID为空,请检查(vendorId)"),
        E10302(10302, "没有这个代理商ID"),
        E10303(10303, "有此代理商ID,但代理商代码(signature)错误"),
        E10304(10304, "代理商代码(signature)为空"),
        E10411(10411, "請於30秒後再試"),
        E10418(10418, "請於10秒後再試"),
        E10501(10501, "查无此帐号,请检查"),
        E10502(10502, "帐号名不得为空"),
        E10505(10505, "此帐号已被停用"),
        E10507(10507, "此账号非此代理下线,不能使用此功能"),
        E10512(10512, "帐号密码格式错误"),
        E10601(10601, "限額未開放，請檢查"),
        E107(107, "请求成功没有数据");
        int code;
        String message;

        ErrorCode(int code, String message) {
            this.code = code;
            this.message = message;
        }
    }

    /**
     * SGD/新加坡元
     * MYR/马来西亚令吉
     * HKD/港币
     * RMB/人民币
     * <p>
     * JPY/日圆
     * AUD/澳洲元
     * USD/美元
     * KRW/韩圆
     * <p>
     * THB/泰铢
     * NZD/新西兰元
     * INR/印度卢比
     * BND/汶莱元
     * <p>
     * GBP/英镑
     * EUR/欧元
     * PHP/菲律宾披索
     * SEK/瑞典克朗
     * <p>
     * ZAR/南非兰特
     * NTD/新台币
     * ---------------------
     * 支援1:1&1:1000
     * IDR/印尼盾
     * VND/越南盾
     * MMK/缅元
     * LAK/寮国基普
     */
    @Getter
    enum Currency {
        SGD("SGD", "新加坡元"),
        MYR("MYR", "马来西亚令吉"),
        HKD("HKD", "港币"),
        RMB("RMB", "人民币"),

        JPY("JPY", "日元"),
        AUD("AUD", "澳洲元"),
        USD("USD", "美元"),
        KRW("KRW", "韩圆"),

        THB("THB", "泰铢"),
        NZD("NZD", "新西兰元"),
        INR("INR", "印度卢比"),
        BND("BND", "汶莱元"),

        GBP("GBP", "英镑"),
        EUR("EUR", "欧元"),
        PHP("PHP", "菲律宾披索"),
        SEK("SEK", "瑞典克朗"),

        ZAR("ZAR", "南非兰特"),
        NTD("NTD", "新台币"),

        IDR("IDR", "印尼盾"),
        VND("VND", "越南盾"),
        MMK("MMK", "缅元"),
        LAK("VND", "寮国基普");

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
