package com.xinbo.sports.payment.io;

import lombok.*;

/**
 * @author: David
 * @date: 14/07/2020
 */
public interface AllNetPayParams {

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
    enum allNetPayUrlEnum {
        /**
         * 接口请求
         */
        PAY("pay", "/Pay_Index.html"),
        QUERY("query", "/Pay_Trade_query.html"),
        PAYOUT("payout","/Payment_Dfpay_add.html"),
        ;

        private String name;
        private String url;

        allNetPayUrlEnum(String name, String url) {
            this.name = name;
            this.url = url;
        }
    }
    @Getter
    enum STATUS {
        /**
         * 语言
         */
        SUCCESS("OK"),
        FAIL("fail"),
        ;
        String code;

        STATUS(String code) {
            this.code = code;
        }
    }
}
