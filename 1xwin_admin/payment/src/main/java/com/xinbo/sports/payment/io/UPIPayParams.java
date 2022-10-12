package com.xinbo.sports.payment.io;

import lombok.*;

import java.math.BigDecimal;

/**
 * @author: David
 * @date: 14/07/2020
 */
public interface UPIPayParams {
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
     * 'payOrderId' => 'P01169720200922055109153',
     * 'amount' => '200000',
     * 'mchId' => '153',
     * 'productId' => '8000',
     * 'mchOrderNo' => 'I2020092217483092368',
     * 'appId' => '251a5a1e996449d3a53128858cf2c7b9',
     * 'sign' => '3A079E045C269A451AC97FDE1686A256',
     * 'backType' => '2',
     * 'param1' => 'test;test;test',
     * 'status' => '5',
     * 异步回调入参数
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class NotifyUrlReqDto {
        private String payOrderId;
        private BigDecimal amount;
        private String mchId;
        private String productId;
        private String mchOrderNo;
        private String paySuccTime;
        private String appId;
        private String sign;
        private String backType;
        private String param1;
        private String status;
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
        String returnUrl;
        String payModel;
        Integer status;
        Integer createdAt;
        Integer updatedAt;
    }//


    @Getter
    enum UPIPayMethodEnum {
        /**
         * 接口请求
         */
        PAY_IN("deposit", "/v1/idpay/pay_center"),
        AGENT_PAY("withdraw", "/v1/idpay/remit"),
        ;

        private String name;
        private String url;

        UPIPayMethodEnum(String name, String url) {
            this.name = name;
            this.url = url;
        }
    }
}
