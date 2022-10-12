package com.xinbo.sports.payment.io;

import io.swagger.models.auth.In;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author: David
 * @date: 14/07/2020
 */
public interface EasyPayNewParams {
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
        VI("vi-VN", "越南语"),
        HI("hi-IN","北印度语"),


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
        private String payMemberid;
        private String payOrderid;
        private Long payApplydate;
        private String payBankcode;
        private String payNotifyurl;
        private String payCallbackurl;
        private BigDecimal payAmount;
        private String payMd5sign;
        private String payProductname;
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



    /**
     * 异步回调入参数
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class NotifyUrlReqDto {
        private Integer code;
        private String merchantId;
        private String message;
        private String outTradeNo;
        private BigDecimal coin;
        private String time;
        private String attach;
        private String sign;
    }


    /**
     * 异步回调入参数
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class DFNotifyUrlReqDto {
        private Integer code;
        private String message;
        private String outTradeNo;
        private BigDecimal coin;
        private String merchantId;
        private String time;
        private String sign;
    }

    @Getter
    enum CURRENCY {
        //货币类型，默认为1，[1:CNY,2:USD,3:HKD,4:BD,5:THB]，人民币: CNY, 美元: USD, 港币: HKD, 泰铢: THB
        CHINA("1", "CNY"),
        AMERICAN("2", "USD"),
        BAHRAIN("4", "BD"),
        THAILAND("Thailand", "THB"),
        VIETNAM("VND", "USD"),
        HONGKONG("3", "HKD"),
        INDIA("India", "INR");
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
        String withdrawUrl;
        String notifyUrl;
        String returnUrl;
        String payModel;
        Integer status;
        Integer createdAt;
        Integer updatedAt;
    }

    @Getter
    enum EasyPayMethodEnum {
        /**
         * 接口请求
         */
        SCANPAY("scanpay", "/pay/sfpay/scanpay"),
        GATEWAY("gateway", "/pay/v1/pay/createOrder"),
        PAYMENT("payment", "/pay/sfpay/payment"),
        QUERY("query", "/pay/v1/df/queryOrder"),

        ;

        private String name;
        private String url;

        EasyPayMethodEnum(String name, String url) {
            this.name = name;
            this.url = url;
        }
    }
}
