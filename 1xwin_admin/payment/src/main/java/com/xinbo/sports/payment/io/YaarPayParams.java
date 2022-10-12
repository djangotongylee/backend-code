package com.xinbo.sports.payment.io;

import lombok.*;

/**
 * @author: David
 * @date: 14/07/2020
 */
public interface YaarPayParams {
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
    class PaymentReqDto {
        private Integer amount;
        private String completedTime;
        private String currency;
        private String mchOrderNo;
        private String orderTime;
        private String payOrderId;
        private String sign;
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
        THAILAND("th", "泰铢"),
        VIETNAM("VND", "越南盾"),
        INDIA("inr", "印度卢比");

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
    enum YaarPayMethodEnum {
        /**
         * 接口请求
         */
        PAY_IN("deposit", "/api/v1/payin/pay_info"),
        AGENT_PAY("withdraw", "/api/agentpay/apply"),
        ;

        private String name;
        private String url;

        YaarPayMethodEnum(String name, String url) {
            this.name = name;
            this.url = url;
        }
    }
}
