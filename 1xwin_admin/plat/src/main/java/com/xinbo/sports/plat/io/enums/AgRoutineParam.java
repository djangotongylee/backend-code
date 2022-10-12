package com.xinbo.sports.plat.io.enums;

import lombok.Getter;

/**
 * <p>
 * AG视讯
 * </p>
 *
 * @author andy
 * @since 2020/7/9
 */
public interface AgRoutineParam {
    /**
     * 语言
     */
    @Getter
    enum Lang {
        ZH_CN(1, "cn", "简体中文"),
        ZH_TW(2, "tw", "繁體中文"),
        EN(3, "en", "英文"),
        JP(4, "jp", "日语"),
        KO(5, "ko", "韩语"),
        TH(6, "th", "泰文"),
        VI(8, "vi", "越南语"),
        KHM(9, "khm", "柬埔寨语"),
        ID(11, "id", "印尼语"),
        PRT(23, "prt", "葡萄牙语");

        /**
         * ID
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
     * 拉单拉单接口请求参数支持语言
     */
    @Getter
    enum BetSlipsLang {
        LANG_CNS("lang_cns", "简体中文"),
        ZH_TW("lang_cnr", "繁体中文"),
        LANG_ENGLISH("lang_english", "英文");

        /**
         * 简写
         */
        private String code;
        /**
         * 描述
         */
        private String desc;

        BetSlipsLang(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }

    /**
     * 币种
     */
    @Getter
    enum Currency {
        CNY("CNY", "人民币"),
        KRW("KRW", "韩元"),
        MYR("MYR", "马来西亚币"),
        USD("USD", "美元"),
        JPY("JPY", "日元"),
        THB("THB", "泰铢"),
        BTC("BTC", "比特币"),
        IDR("IDR", "印尼盾"),
        VND("VND", "越南盾"),
        EUR("EUR", "欧元"),
        INR("INR", "印度卢比"),
        BRL("BRL", "巴西雷亚尔"),
        GBP("GBP", "英镑"),
        AUD("AUD", "澳元"),
        MMK("MMK", "缅元");

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
