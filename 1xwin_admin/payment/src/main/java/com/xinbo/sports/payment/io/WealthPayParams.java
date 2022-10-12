package com.xinbo.sports.payment.io;

import lombok.*;

/**
 * @author: David
 * @date: 14/07/2020
 */
public interface WealthPayParams {
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
     * YaarPay方法枚举
     */
    @Getter
    @AllArgsConstructor
    enum WealthPayMethodEnum {
        DEPOSIT("/merchant/deposit", "存款"),
        WITHDRAWAL("/merchant/withdrawal", "取款"),
        GETBALANCE("/merchant/GetBalance", "查询余额"),
        QUERYDEPOSITSTATUS("/merchant/QueryDepositStatus", "查询存款状态"),
        QUERY_WITHDRAWAL_STATUS("/merchant/QueryWithdrawalStatus", "查询取款状态"),
        ;


        private String methodName;
        private String methodNameDesc;
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
    }

}
