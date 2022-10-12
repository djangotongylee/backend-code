package com.xinbo.sports.payment.io;

import lombok.*;

import java.math.BigDecimal;

/**
 * @author: David
 * @date: 14/07/2020
 */
public interface InPayParams {
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
    class PaymentReqDto {
        private String merchantNum;
        private String orderNo;
        private String platformOrderNo;
        private BigDecimal amount;
        private String attch;
        private String state;
        private String payTime;
        private String sign;
        private String actualPayAmount;
    }

    /**
     * 同步回调入参数
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class ReturnUrlReqDto {
        private String retCode;
        private String retMsg;
        private String sign;
        private String payOrderId;
        private String payParams;

    }

    @Getter
    enum CURRENCY {
        CHINA("CNY", "人民币"),
        THAILAND("THB", "泰铢"),
        VIETNAM("VND", "越南盾"),
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
        String withdrawUrl;
        String returnUrl;
        String payModel;
        Integer status;
        Integer createdAt;
        Integer updatedAt;
    }


    /**
     * 异步回调入参数
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class WithdrawalReqDto {
        private String merchantNum;
        private String orderNo;
        private BigDecimal withdrawAmount;
        private String sign;
    }

    @Getter
    enum InPayUrlEnum {
        /**
         * 接口请求
         */
        PAY("deposit", "/api/startOrder"),
        TRANS("withdraw", "/api/startPayForAnotherOrder"),
        CHECK("check", "/api/getOrderInfo"),
        ;

        private String name;
        private String url;

        InPayUrlEnum(String name, String url) {
            this.name = name;
            this.url = url;
        }
    }

}
