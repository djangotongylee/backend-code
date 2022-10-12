package com.xinbo.sports.plat.io.bo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author: wells
 * @date: 2020/5/27
 * @description:
 */

public interface TCGLotteryRequestParameter {


    @Builder
    @Data
    class Login {

        /**
         * {"method":"lg", "username": "phoenix", "language": "VI", "product_type": 384, "game_code": "HANOIVNC",
         * "game_mode": "1", "platform": "flash", "lottery_bet_mode": "SEA_Tradition", "view": "lobby","series":
         * [{"game_group_code": "VNC","prize_mode_id": 1}] }
         */
        @JSONField(name = "username")
        private String username;
        @JSONField(name = "language")
        private String language;
        @JSONField(name = "method")
        private String method;
        @JSONField(name = "product_type")
        private String productType;
        @JSONField(name = "game_code")
        private String gameCode;
        @JSONField(name = "game_mode")
        private Integer gameMode;
        @JSONField(name = "platform")
        private String platform;
        @JSONField(name = "lottery_bet_mode")
        private String lotteryBetMode;
        @JSONField(name = "view")
        private String view;
        @JSONField(name = "series")
        private Object[] series;

    }


    @Builder
    @Data
    class Register {

        /**
         * {"method":"cm", "username":"phoenix", "password":"1q2w3e4r", "
         * ":"CNY"}
         */
        @JSONField(name = "username")
        private String username;
        @JSONField(name = "password")
        private String password;
        @JSONField(name = "currency")
        private String currency;
        @JSONField(name = "method")
        private String method;


    }

    @Builder
    @Data
    class FundTransfer {

        /**
         * params	{"method":"ft", "username":"phoenix", "product_type":"7", "fund_type":"1", "amount": 100.50,
         * "reference_no":"TCGTESTFI2017001"}
         */

        private BigDecimal amount;
        @JSONField(name = "method")
        private String method;
        private String username;
        @JSONField(name = "fund_type")
        private Integer fundType;
        @JSONField(name = "reference_no")
        private String referenceNo;
        @JSONField(name = "product_type")
        private String productType;

    }

    @Builder
    @Data
    class CheckTransactionStatus {

        /**
         * {"method":"cs", "product_type":"7", "ref_no":"TCGTESTFI2017001"}
         */

        @JSONField(name = "method")
        private String method;
        @JSONField(name = "product_type")
        private String productType;
        @JSONField(name = "ref_no")
        private String refNo;

    }

    @Builder
    @Data
    class QueryBalance {

        /**
         * 查询余额
         * sysLang : 0
         * username : test999
         */
        private String method;
        private String username;
        @JSONField(name = "product_type")
        private String productType;

    }


    @Builder
    @Data
    class GetGameList {
        private String method;
        @JSONField(name = "lotto_type")
        private String lottoType;

    }

    @Builder
    @Data
    class GetUnsettleBetList {

        private String method;
        @JSONField(name = "batch_name")
        private String batchName;
        private String page;

    }

    @Data
    class BetDetail {
        /**
         * id -> bet_order_no
         */
        @JSONField(name = "bet_order_no", serializeUsing = Long.class)
        private Long id;
        /**
         * 投注金额
         */
        @JSONField(name = "bet_amount")
        private BigDecimal betAmount;

        /**
         * 游戏代码
         */
        @JSONField(name = "game_code")
        private String gameCode;

        /**
         * 投注时间
         */
        @JSONField(name = "bet_time", format = "yyyy-MM-dd'T'HH:mm:ss")
        private Date betTime;

        /**
         * 交易时间
         */
        @JSONField(name = "trans_time", format = "yyyy-MM-dd'T'HH:mm:ss")
        private Date transTime;

        /**
         * 投注内容
         */
        @JSONField(name = "bet_content_id")
        private String betContentId;

        /**
         * 玩法代码
         */
        @JSONField(name = "play_code")
        private String playCode;

        /**
         * 订单号(后台查询)
         * <p>
         * 订单号（后台查询）
         */
        @JSONField(name = "order_num")
        private String orderNum;

        /**
         * 追号（true,false）
         */
        @JSONField(name = "chase")
        private String chase;

        /**
         * 期号
         */
        @JSONField(name = "numero")
        private String numero;

        /**
         * 投注实际内容
         */
        @JSONField(name = "betting_content")
        private String bettingContent;

        /**
         * 玩法id
         */
        @JSONField(name = "play_id")
        private Integer playId;

        /**
         * 冻结时间
         */
        @JSONField(name = "freeze_time", format = "yyyy-MM-dd'T'HH:mm:ss")
        private Date freezeTime;

        /**
         * 下注倍数
         */
        @JSONField(name = "multiple")
        private Integer multiple;

        /**
         * 用户名
         */
        @JSONField(name = "username")
        private String username;

        /**
         * 产品代码
         */
        @JSONField(name = "product_type")
        private String productType;

        /**
         * 单式注单（true,false）
         */
        @JSONField(name = "single")
        private String single;

        /**
         * 商户代码
         */
        @JSONField(name = "merchant_code")
        private String merchantCode;

        /**
         * 中奖金额
         */
        @JSONField(name = "win_amount")
        private BigDecimal winAmount;

        /**
         * 结算时间
         */
        @JSONField(name = "settlement_time", format = "yyyy-MM-dd'T'HH:mm:ss")
        private Date settlementTime;

        /**
         * 净输赢
         */
        @JSONField(name = "netPNL")
        private BigDecimal netPNL;

        /**
         * 订单状态(1:WIN | 2:LOSE | 3:CANCELLED | 4:TIE )(1:已中奖｜2:未中奖 ｜3:取消 | 4:和)
         */
        @JSONField(name = "bet_status")
        private Integer betStatus;


    }

    @Getter
    enum CURRENCY {
        /**
         * 币种 -tcg
         * CNY	人民币
         * IDR	印尼盾
         * THB	泰铢
         * MYR	马币
         * VND	越南盾
         * TWD	新台币
         * KRW	韩元
         */
        RMB("CNY", "人民币"),
        THB("THB", "泰铢"),
        HKD("HKD", "港币"),
        PHP("PHP", "菲律宾披索"),
        VND("VND", "越南盾"),
        IDR("IDR", "印尼盾"),
        INR("INR", "印度卢比"),
        TWD("TWD", "新台币"),
        KRW("KRW", "韩元"),
        MYR("MYR", "马来西亚币");

        private String code;
        private String desc;

        CURRENCY(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }

    /**
     * 平台参数
     * Language 语言	Language Code 语言代码
     * 简体中文 Simplified Chinese	ZH_CN
     * 英文 English	EN
     * 泰语 Thailand	TH
     * 越南语 Vietnamese	VN (For TCG_SEA LOTTO：VI;东南亚彩请用：VI)
     * 印尼语 Indonesian	ID
     * 日语 Japanese	JA
     * 韩语 korean	KO
     */
    @Getter
    enum LANGS {
        /**
         * 语言信息
         */
        EN("EN", "英文"),
        ZH("ZH_CN", "中文"),
        VI("VN", "越南语"),
        TH("TH", "泰语"),
        ID("ID", "印尼语"),
        JA("JA", "日语"),
        KO("KO", "韩语"),
        ;

        private String code;
        private String message;

        LANGS(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }


}
