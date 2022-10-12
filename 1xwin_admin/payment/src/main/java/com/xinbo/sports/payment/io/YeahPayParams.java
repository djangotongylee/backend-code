package com.xinbo.sports.payment.io;

import lombok.*;


/**
 * @author: David
 * @date: 14/07/2020
 */
public interface YeahPayParams {
    /**
     * 语言
     */
    @Getter
    enum LANGS {
        /**
         * 语言
         */
        EN("en", "英文"),
        ZH("zh-CN", "简体中文"),
        TH("th-TH", "泰语"),
        ;

        String code;
        String message;

        LANGS(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }


    @Getter
    enum country {
        /**
         * 语言
         */
        INDIA("India"),
        ;
        String code;

        country(String code) {
            this.code = code;
        }
    }

    @Getter
    enum CURRENCY {
        //货币类型，默认为1，[1:CNY,2:USD,3:HKD,4:BD,5:THB]，人民币: CNY, 美元: USD, 港币: HKD, 泰铢: THB
        CHINA("1", "人民币"),
        AMERICAN("2", "美元"),
        BAHRAIN("4", "巴林第纳尔"),
        THAILAND("5", "泰铢"),
        VIETNAM("VND", "越南盾"),
        HONGKONG("3", "港币"),
        INDIA("INR", "印度卢比");
        private String code;
        private String desc;

        CURRENCY(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class PayPlat {
        Integer id;
        String code;
        String name;
        String businessCode;
        String businessPwd;
        String publicKey;
        String privateKey;
        String url;
        String notifyUrl;
        String returnUrl;
        String payModel;
        Integer status;
        Integer createdAt;
        Integer updatedAt;
    }

    @Getter
    enum YeahPayUrlEnum {
        /**
         * 接口请求
         */
        TOKEN("token", "/gpauth/oauth/token"),
        PAYMENT("payment", "/core/api/payment/prepay"),
        SIGN("sign", "/core/api/payment/sign"),
        QUERY("query", "/core/api/payment/payorder"),
        PAYOUT("payout", "/core/api/payment/payout"),
        QUERY_PAYOUT_ORDER("query_payout", "/core/api/payment/payoutorder/"),
        ;

        private String name;
        private String url;

        YeahPayUrlEnum(String name, String url) {
            this.name = name;
            this.url = url;
        }
    }

}
