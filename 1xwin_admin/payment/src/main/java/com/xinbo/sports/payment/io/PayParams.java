package com.xinbo.sports.payment.io;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author: David
 * @date: 14/07/2020
 */
public interface PayParams {

    /**
     * 线下支付返回实体
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    class PayListResBody {
        @ApiModelProperty(name = "id", value = "ID")
        private Integer id;
        @ApiModelProperty(name = "icon", value = "/icon/bank/pay_qr_code.png")
        private String icon;
        @ApiModelProperty(name = "category", value = "类型:1-银行卡 2-微信 3-支付宝 4-QQ 5-qrCode", example = "5")
        private Integer category;
        @ApiModelProperty(name = "coinMin", value = "最低支付", example = "100")
        private Integer coinMin;
        @ApiModelProperty(name = "coinMax", value = "最高支付", example = "5000")
        private Integer coinMax;
        @ApiModelProperty(name = "range", value = "区间可选金额", example = "100, 500, 1000, 2000, 3000, 5000")
        private List<String> range;


        @ApiModelProperty(name = "payType", value = "字典dic_coin_deposit_pay_type", example = "0")
        private Integer payType;
        @ApiModelProperty(name = "payInfo")
        private JSONObject payInfo;

    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    class PayList {
        @ApiModelProperty(name = "payTypeList", value = "支付类型列表")
        private List<PayOffline> payOfflineList;

        @ApiModelProperty(name = "payTypeList", value = "支付类型列表")
        private List<PayOnline> payOnlineList;
    }

    /**
     * 线下支付返回实体
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    class PayOffline {
        @ApiModelProperty(name = "id", value = "ID")
        private Integer id;
        @ApiModelProperty(name = "icon", value = "/icon/bank/pay_qr_code.png")
        private String icon;
        @ApiModelProperty(name = "userName", value = "持卡人姓名")
        private String userName;
        @ApiModelProperty(name = "bankName", value = "收款银行")
        private String bankName;
        @ApiModelProperty(name = "bankAccount", value = "收款卡号")
        private String bankAccount;
        @ApiModelProperty(name = "bankAddress", value = "收款开户行地址")
        private String bankAddress;
        @ApiModelProperty(name = "qrCode", value = "收款二维码")
        private String qrCode;
        @ApiModelProperty(name = "category", value = "类型:1-银行卡 2-微信 3-支付宝 4-QQ 5-qrCode", example = "5")
        private Integer category;
        @ApiModelProperty(name = "coinMin", value = "最低支付", example = "100")
        private Integer coinMin;
        @ApiModelProperty(name = "coinMax", value = "最高支付", example = "5000")
        private Integer coinMax;
        @ApiModelProperty(name = "range", value = "区间可选金额", example = "100, 500, 1000, 2000, 3000, 5000")
        private List<String> range;
    }

    /**
     * 线上支付返回实体
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    class PayOnline {
        @ApiModelProperty(name = "id", value = "ID")
        private Integer id;
        @ApiModelProperty(name = "icon", value = "/icon/bank/pay_qr_code.png")
        private String icon;
        @ApiModelProperty(name = "payName", value = "支付名称", example = "100")
        private String payName;
        @ApiModelProperty(name = "coinMin", value = "最低支付", example = "100")
        private Integer coinMin;
        @ApiModelProperty(name = "coinMax", value = "最高支付", example = "5000")
        private Integer coinMax;
        @ApiModelProperty(name = "category", value = "类型:1-银行卡 2-微信 3-支付宝 4-QQ 5-qrCode", example = "5")
        private Integer category;
        @ApiModelProperty(name = "range", value = "区间可选金额", example = "[100, 500, 1000, 2000, 3000, 5000]")
        private List<String> range;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    class PaymentReqDto {
        @NotNull(message = "不能为空")
        @ApiModelProperty(name = "id", value = "ID", example = "2")
        public Integer id;

        @NotNull(message = "不能为空")
        @ApiModelProperty(name = "coin", value = "金额", example = "100")
        public BigDecimal coin;

        @ApiModelProperty(name = "realname", value = "真实姓名", example = "张三")
        public String realname;
    }

    @Data
    class Payment extends PaymentReqDto {
        @NotNull(message = "不能为空")
        @ApiModelProperty(name = "payType", value = "字典dic_coin_deposit_pay_type", example = "0")
        private Integer payType;
    }

    @Data
    class PaymentCommon {
        @NotNull(message = "不能为空")
        @ApiModelProperty(name = "payType", value = "字典dic_coin_deposit_pay_type", example = "0")
        private Integer payType;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    class PayOnlineResDto {
        @ApiModelProperty(name = "id", value = "订单号", example = "202003221900567338")
        private Long id;

        @ApiModelProperty(name = "payName", value = "三方名称", example = "MangoPay QrCode")
        private String payName;

        @ApiModelProperty(name = "coin", value = "充值金额", example = "100.00")
        private BigDecimal coin;

        @ApiModelProperty(name = "createdAt", value = "提交时间", example = "1586617634")
        private Integer createdAt;

        @ApiModelProperty(name = "category", value = "类型:1-直接跳转 2-转为qrCode 3-FORM表单", example = "1")
        public Integer category;

        @ApiModelProperty(name = "coin_range", value = "充值范围", example = "1000～50000")
        public String coinRange;

        @ApiModelProperty(name = "method", value = "请求方式", example = "QRCODER GET POST")
        private String method;

        @ApiModelProperty(name = "url", value = "地址", example = "http://www.baidu.com")
        public String url;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    class PayOfflineResDto {
        @ApiModelProperty(name = "category", value = "充值方式: 1-银行卡 2-微信 3-支付宝")
        private Integer category;

        @ApiModelProperty(name = "coin", value = "充值金额", example = "100.00")
        private BigDecimal coin;

        @ApiModelProperty(name = "id", value = "订单号", example = "202003221900567338")
        private Long id;

        @ApiModelProperty(name = "createdAt", value = "提交时间", example = "1586617634")
        private Integer createdAt;

        @ApiModelProperty(name = "coin_range", value = "提交时间", example = "1586617634")
        private String coinRange;

        @ApiModelProperty(name = "bankName", value = "收款银行")
        private String bankName;

        @ApiModelProperty(name = "userName", value = "收款人")
        private String userName;

        @ApiModelProperty(name = "bankAccount", value = "收款卡号")
        private String bankAccount;

        @ApiModelProperty(name = "depRealname", value = "打款人姓名", required = true)
        private String depRealname;
    }

    /**
     * 在线支付统一入口
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    class OnlinePayReqDto {
        @ApiModelProperty(name = "depositId", value = "存款表ID", example = "en")
        public String depositId;

        @ApiModelProperty(name = "lang", value = "语言参数", example = "en")
        public String lang;

        @ApiModelProperty(name = "channel", value = "渠道", example = "907")
        public String channel;
    }


    /**
     * 在线代付统一入参数
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    class WithdrawalReqDto {
        @NotNull(message = "id can't be empty")
        @ApiModelProperty(name = "id", value = "id", example = "在线支付列表id:1->mangoPay")
        private Integer id;
        @NotNull(message = "withdrawalId can't be empty")
        @ApiModelProperty(name = "withdrawalId", value = "提款表ID", example = "en")
        public String withdrawalId;
        @ApiModelProperty(name = "lang", value = "语言参数", example = "en")
        public String lang;
        @NotNull(message = "bankCode can't be empty")
        @ApiModelProperty(name = "bankCode", value = "银行代码", example = "en")
        public String bankCode;
        @NotNull(message = "openAccountBank can't be empty")
        @ApiModelProperty(name = "openAccountBank", value = "开户银行", example = "en")
        public String openAccountBank;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    class OnlinePayoutReqDto {
        @ApiModelProperty(name = "orderId", value = "存款表ID", example = "en")
        public String orderId;
        @ApiModelProperty(name = "lang", value = "语言参数", example = "en")
        public String lang;
        @ApiModelProperty(name = "bankCode", value = "银行代码", example = "en")
        public String bankCode;
        @ApiModelProperty(name = "openAccountBank", value = "开户银行", example = "en")
        public String openAccountBank;
    }

    /**
     * 在线代付统一出参
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    class WithdrawalResDto {

        @ApiModelProperty(name = "coin", value = "余额", example = "en")
        public BigDecimal coin;

        @ApiModelProperty(name = "category", value = "类型:1-直接跳转 2-转为qrCode 3-FORM表单", example = "1")
        public Integer category;

        @ApiModelProperty(name = "method", value = "请求方式", example = "QRCODER GET POST")
        private String method;

        @ApiModelProperty(name = "url", value = "链接", example = "en")
        public String url;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    class WithdrawalNotifyResDto {

        @ApiModelProperty(name = "code", value = "true->成功，false-失败", example = "en")
        public Boolean code;

        @ApiModelProperty(name = "msg", value = "", example = "1")
        public String msg;

    }

    /**
     * 状态码
     */
    @Getter
    enum STATUS {
        /**
         * 语言
         */
        SUCCESS("success"),
        FAIL("fail"),
        ;
        String code;

        STATUS(String code) {
            this.code = code;
        }
    }

    /**
     * path
     */
    @Getter
    enum UrlEnum {
        /**
         * 接口请求
         */
        DEPOSIT_NOTIFY_URL("deposit", "/notifyUrl"),
        WITHDRAW_NOTIFY_URL("withdraw", "/withdrawalNotifyUrl"),
        DEPOSIT_URL("deposit_url", "/api/addOrder/addBuyOrder"),
        QUERY_URL("query", "/api/priceConfig/queryPrice"),
        ;

        private String name;
        private String url;

        UrlEnum(String name, String url) {
            this.name = name;
            this.url = url;
        }
    }

    String ACCOUNT_HOLDER = "accountName";
    String BANK_CARD_ACCOUNT = "bankAccount";
    String OPEN_ACCOUNT_BANK = "openAccountBank";
    String BANK_CODE = "bankCode";
    String MARK = "mark";

}
