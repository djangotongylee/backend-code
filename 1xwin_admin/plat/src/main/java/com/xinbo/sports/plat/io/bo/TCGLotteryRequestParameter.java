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
         * ????????????
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
         * ????????????
         */
        @JSONField(name = "bet_amount")
        private BigDecimal betAmount;

        /**
         * ????????????
         */
        @JSONField(name = "game_code")
        private String gameCode;

        /**
         * ????????????
         */
        @JSONField(name = "bet_time", format = "yyyy-MM-dd'T'HH:mm:ss")
        private Date betTime;

        /**
         * ????????????
         */
        @JSONField(name = "trans_time", format = "yyyy-MM-dd'T'HH:mm:ss")
        private Date transTime;

        /**
         * ????????????
         */
        @JSONField(name = "bet_content_id")
        private String betContentId;

        /**
         * ????????????
         */
        @JSONField(name = "play_code")
        private String playCode;

        /**
         * ?????????(????????????)
         * <p>
         * ???????????????????????????
         */
        @JSONField(name = "order_num")
        private String orderNum;

        /**
         * ?????????true,false???
         */
        @JSONField(name = "chase")
        private String chase;

        /**
         * ??????
         */
        @JSONField(name = "numero")
        private String numero;

        /**
         * ??????????????????
         */
        @JSONField(name = "betting_content")
        private String bettingContent;

        /**
         * ??????id
         */
        @JSONField(name = "play_id")
        private Integer playId;

        /**
         * ????????????
         */
        @JSONField(name = "freeze_time", format = "yyyy-MM-dd'T'HH:mm:ss")
        private Date freezeTime;

        /**
         * ????????????
         */
        @JSONField(name = "multiple")
        private Integer multiple;

        /**
         * ?????????
         */
        @JSONField(name = "username")
        private String username;

        /**
         * ????????????
         */
        @JSONField(name = "product_type")
        private String productType;

        /**
         * ???????????????true,false???
         */
        @JSONField(name = "single")
        private String single;

        /**
         * ????????????
         */
        @JSONField(name = "merchant_code")
        private String merchantCode;

        /**
         * ????????????
         */
        @JSONField(name = "win_amount")
        private BigDecimal winAmount;

        /**
         * ????????????
         */
        @JSONField(name = "settlement_time", format = "yyyy-MM-dd'T'HH:mm:ss")
        private Date settlementTime;

        /**
         * ?????????
         */
        @JSONField(name = "netPNL")
        private BigDecimal netPNL;

        /**
         * ????????????(1:WIN | 2:LOSE | 3:CANCELLED | 4:TIE )(1:????????????2:????????? ???3:?????? | 4:???)
         */
        @JSONField(name = "bet_status")
        private Integer betStatus;


    }

    @Getter
    enum CURRENCY {
        /**
         * ?????? -tcg
         * CNY	?????????
         * IDR	?????????
         * THB	??????
         * MYR	??????
         * VND	?????????
         * TWD	?????????
         * KRW	??????
         */
        RMB("CNY", "?????????"),
        THB("THB", "??????"),
        HKD("HKD", "??????"),
        PHP("PHP", "???????????????"),
        VND("VND", "?????????"),
        IDR("IDR", "?????????"),
        INR("INR", "????????????"),
        TWD("TWD", "?????????"),
        KRW("KRW", "??????"),
        MYR("MYR", "???????????????");

        private String code;
        private String desc;

        CURRENCY(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }

    /**
     * ????????????
     * Language ??????	Language Code ????????????
     * ???????????? Simplified Chinese	ZH_CN
     * ?????? English	EN
     * ?????? Thailand	TH
     * ????????? Vietnamese	VN (For TCG_SEA LOTTO???VI;?????????????????????VI)
     * ????????? Indonesian	ID
     * ?????? Japanese	JA
     * ?????? korean	KO
     */
    @Getter
    enum LANGS {
        /**
         * ????????????
         */
        EN("EN", "??????"),
        ZH("ZH_CN", "??????"),
        VI("VN", "?????????"),
        TH("TH", "??????"),
        ID("ID", "?????????"),
        JA("JA", "??????"),
        KO("KO", "??????"),
        ;

        private String code;
        private String message;

        LANGS(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }


}
