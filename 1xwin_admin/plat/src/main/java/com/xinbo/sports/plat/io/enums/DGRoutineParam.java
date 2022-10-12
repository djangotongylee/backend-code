package com.xinbo.sports.plat.io.enums;

import com.google.common.collect.ImmutableMap;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.Getter;

import java.util.Map;

/**
 * <p>
 * DG视讯
 * </p>
 *
 * @author andy
 * @since 2020/5/20
 */
public interface DGRoutineParam {
    /**
     * 错误码映射
     */
    Map<Integer, CodeInfo> ERROR_CODE_MAPPER = new ImmutableMap
            .Builder<Integer, CodeInfo>().
            put(ErrorCode.E1.getCode(), CodeInfo.PLAT_INVALID_PARAM).
            put(ErrorCode.E4.getCode(), CodeInfo.PLAT_INVALID_OPERATOR).
            put(ErrorCode.E400.getCode(), CodeInfo.PLAT_IP_NOT_ACCESS).
            put(ErrorCode.E403.getCode(), CodeInfo.PLAT_IP_NOT_ACCESS).
            put(ErrorCode.E401.getCode(), CodeInfo.PLAT_TIME_OUT).
            put(ErrorCode.E406.getCode(), CodeInfo.PLAT_TIME_OUT).
            put(ErrorCode.E405.getCode(), CodeInfo.PLAT_REQUEST_FREQUENT).
            put(ErrorCode.E322.getCode(), CodeInfo.PLAT_INVALID_CURRENCY).
            put(ErrorCode.E301.getCode(), CodeInfo.PLAT_INVALID_AGENT_ACCOUNT).
            put(ErrorCode.E118.getCode(), CodeInfo.PLAT_INVALID_AGENT_ACCOUNT).
            put(ErrorCode.E323.getCode(), CodeInfo.PLAT_ID_OCCUPATION).
            put(ErrorCode.E116.getCode(), CodeInfo.PLAT_ACCOUNT_EXISTS).
            put(ErrorCode.E103.getCode(), CodeInfo.PLAT_ACCOUNT_EXISTS).
            put(ErrorCode.E102.getCode(), CodeInfo.PLAT_ACCOUNT_NOT_EXISTS).
            put(ErrorCode.E114.getCode(), CodeInfo.PLAT_ACCOUNT_NOT_EXISTS).
            put(ErrorCode.E100.getCode(), CodeInfo.PLAT_ACCOUNT_OCCUPATION).
            put(ErrorCode.E120.getCode(), CodeInfo.PLAT_COIN_INSUFFICIENT).
            put(ErrorCode.E119.getCode(), CodeInfo.PLAT_PLAT_NO_SUFFICIENT).
            put(ErrorCode.E300.getCode(), CodeInfo.PLAT_UNDER_MAINTENANCE)
            .build();

    /**
     * 语言
     */
    @Getter
    enum Lang {
        EN(0, "en", "英文"),
        ZH_CN(1, "cn", "简体中文"),
        ZH_TW(2, "tw", "繁體中文"),
        KR(3, "kr", "韩语"),
        MY(4, "my", "缅甸语"),
        TH(5, "th", "泰语"),
        VI(6, "vi", "越南语");

        /**
         * 代号
         */
        private int id;
        /**
         * 简写
         */
        private String code;
        /**
         * 描述
         */
        private String desc;

        Lang(int id, String code, String desc) {
            this.id = id;
            this.code = code;
            this.desc = desc;
        }
    }

    /**
     * 异常码
     */
    @Getter
    enum ErrorCode {
        E1(1, "参数错误"),
        E2(2, "Token验证失败"),
        E4(4, "非法操作"),
        E10(10, "日期格式错误"),
        E11(11, "数据格式错误"),
        E97(97, "没有权限"),
        E98(98, "操作失败"),
        E99(99, "未知错误"),
        E100(100, "账号被锁定"),
        E101(101, "账号格式错误"),
        E102(102, "账号不存在"),
        E103(103, "此账号被占用"),
        E104(104, "密码格式错误"),
        E105(105, "密码错误"),
        E106(106, "新旧密码相同"),
        E107(107, "会员账号不可用"),
        E108(108, "登入失败"),
        E109(109, "注册失败"),
        E113(113, "传入的代理账号不是代理"),
        E114(114, "找不到会员"),
        E116(116, "账号已占用"),
        E117(117, "找不到会员所属的分公司"),
        E118(118, "找不到指定的代理"),
        E119(119, "存取款操作时代理点数不足"),
        E120(120, "余额不足"),
        E121(121, "盈利限制必须大于或等于0"),
        E150(150, "免费试玩账号用完"),
        E300(300, "系统维护"),
        E301(301, "代理账号找不到"),
        E320(320, "APIKey错误"),
        E321(321, "找不到相应的限红组"),
        E322(322, "找不到指定的货币类型"),
        E323(323, "转账流水号占用"),
        E324(324, "转账失败"),
        E325(325, "代理状态不可用"),
        E326(326, "会员代理没有视频组"),
        E328(328, "API类型找不到"),
        E329(329, "会员代理信息不完整"),
        E400(400, "客户端IP受限"),
        E401(401, "网络延迟"),
        E402(402, "连接关闭"),
        E403(403, "客户端来源受限"),
        E404(404, "请求的资源不存在"),
        E405(405, "请求太频繁"),
        E406(406, "请求超时"),
        E407(407, "找不到游戏地址"),
        E500(500, "空指针异常"),
        E501(501, "系统异常"),
        E502(502, "系统忙"),
        E503(503, "数据操作异常");
        int code;
        String message;

        ErrorCode(int code, String message) {
            this.code = code;
            this.message = message;
        }
    }

    /**
     * 1	CNY	人民币
     * 2	USD	美元
     * 3	MYR	马来西亚币
     * 4	HKD	港币
     * <p>
     * 5	THB	泰珠
     * 6	SGD	新加坡元
     * 7	PHP	菲律宾比索
     * 8	TWD	台币
     * <p>
     * 9	VND	越南盾
     * 10	IDR	印尼(盾)
     * 11	JPY	日元
     * 12	KHR	柬埔寨币
     * <p>
     * 13	KRW	韩元
     * 16	AUD	澳大利亚元
     * 19	INR	印度卢比
     * 20	EUR	欧元
     * <p>
     * 21	GBP	    英镑
     * 22	CAD	    加拿大
     * 23	KRW2    韩元	已去除3个0，游戏中1块，等同于实际1000块
     * 24	MMK	    缅甸币
     * <p>
     * 25	MMK2	缅甸币	已去除3个0，游戏中1块，等同于实际1000块
     * 29	VND2	越南盾	已去除3个0，游戏中1块，等同于实际1000块
     * 30	IDR2	印尼(盾)	已去除3个0，游戏中1块，等同于实际1000块
     */
    @Getter
    enum Currency {
        CNY("CNY", "人民币"),
        USD("USD", "美元"),
        MYR("MYR", "马来西亚币"),
        HKD("HKD", "港币"),

        THB("THB", "泰珠"),
        SGD("SGD", "新加坡元"),
        PHP("PHP", "菲律宾比索"),
        TWD("TWD", "台币"),

        VND("VND", "越南盾"),
        IDR("IDR", "印尼(盾)"),
        JPY("JPY", "日元"),
        KHR("KHR", "柬埔寨币"),

        KRW("KRW", "韩元"),
        AUD("AUD", "澳大利亚元"),
        INR("INR", "印度卢比"),
        EUR("EUR", "欧元"),

        GBP("GBP", "英镑"),
        CAD("CAD", "加拿大"),
        KRW2("KRW2", "韩元"),
        MMK("MMK", "缅甸币"),

        MMK2("MMK2", "缅甸币"),
        VND2("VND2", "越南盾"),
        IDR2("IDR2", "印尼(盾)");

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
