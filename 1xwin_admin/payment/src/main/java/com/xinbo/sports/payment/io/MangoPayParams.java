package com.xinbo.sports.payment.io;

import lombok.*;

import java.math.BigDecimal;

/**
 * @author: David
 * @date: 14/07/2020
 */
public interface MangoPayParams {
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


    /**
     * 异步回调入参数
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class NotifyUrlReqDto {
        private String merchantId;
        private String orderSn;
        private String queryId;
        private String goodsName;
        private BigDecimal amount;
        private Integer status;
        private String realMoney;
        private String message;
        private String sign;
    }

    /**
     * 同步回调入参数
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class ReturnUrlReqDto {
        private String orderSn;
        private String queryId;
        private String amount;
        private String status;
        private String realMoney;

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
    enum MangoPayUrlEnum {
        /**
         * 接口请求
         */
        SCANPAY("scanpay", "/pay/sfpay/scanpay"),
        GATEWAY("gateway", "/pay/sfpay/gateway"),
        PAYMENT("payment", "/pay/sfpay/payment"),
        QUERY("query", "/pay/sfpay/payment-query-order"),
        ;

        private String name;
        private String url;

        MangoPayUrlEnum(String name, String url) {
            this.name = name;
            this.url = url;
        }
    }
}
