package com.xinbo.sports.plat.io.enums;

import lombok.Getter;

import java.util.List;

/**
 * <p>
 * DG视讯
 * </p>
 *
 * @author andy
 * @since 2020/5/20
 */
public interface BasePlatParam {
    /**
     * 平台参数
     */
    @Getter
    enum LANGS {
        /**
         * 语言信息
         */
        EN("en", "英文"),
        ZH("zh", "中文"),
        VI("vi", "越南语"),
        TH("th", "泰语"),
        ;

        private String code;
        private String message;

        LANGS(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }

    /**
     * 货币类型
     */
    @Getter
    enum CURRENCY {
        /**
         * 货币
         */
        USD("USD", "美元"),
        RMB("RMB", "人民币"),
        HKD("HKD", "港币"),
        PHP("PHP", "菲律宾披索"),
        VND("VND", "越南盾"),
        THB("THB", "泰铢"),
        INR("INR", "印度卢比"),
        MYR("MYR", "马来西亚币");

        private String code;
        private String desc;

        CURRENCY(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }

    /**
     * 设备类型
     */
    @Getter
    enum Device {
        /**
         * 语言信息
         */
        PC("d", "桌面版"),
        H5("m", "手机H5"),
        IOS("ios", "IOS"),
        ANDROID("android", "安卓");

        private String code;
        private String message;

        Device(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }

    /**
     * 注单返回类型(三方自身没有带则用这个)
     */
    @Getter
    enum BetRecordsStatus {
        /**
         * 注单状态
         */
        WIN(1, "赢"),
        LOSE(2, "输"),
        DRAW(3, "和"),
        CANCEL(4, "取消"),
        WAIT_SETTLE(5, "待结算"),
        GAME_CANCEL(6, "赛事取消"),
        BET_CONFIRM(7, "投注已确认"),
        BET_REFUSE(8, "投注拒绝"),
        WIN_HALF(9, "赢一半"),
        LOSE_HALF(10, "输一半"),
        WAIT_CONFIRM(11, "订单待确认"),
        ;

        private Integer code;
        private String message;

        BetRecordsStatus(Integer code, String message) {
            this.code = code;
            this.message = message;
        }
    }

    @Getter
    enum BasePlatEnum {
        /**
         * 拉单时效数据
         */
        WM("WM", 5),
        DG("DG", 0),
        AG("AG", 0),
        SBO("SBO", 0),
        HB("Habanero", 0),
        DS("DS", 0);
        /**
         * 平台名称
         */
        private String model;
        /**
         * 延迟时间:单位(分钟)
         */
        private Integer minutes;

        BasePlatEnum(String model, Integer minutes) {
            this.model = model;
            this.minutes = minutes;
        }
    }


    @Getter
    enum GAME {
        /**
         * 平台参数
         */
        SBO_SPORTS(101),
        IBC_SPORTS(105),
        HB_GAMES(202),
        MG_GAMES(203),
        UPG_GAMES(204),
        CQ9_GAMES(205),
        JOKER_GAMES(206),
        PG_GAMES(207),
        WM_LIVE(301),
        DG_LIVE(303),
        AG_LIVE(304),
        SEXY_LIVE(305),
        SA_LIVE(306),
        EBET_LIVE(307),
        GG_FINISH(401),
        CQ9_PARADISE(404),
        CQ9_THUNDER_FIGHTER(405),
        CQ9_WATER_MARGIN_FINISH(406),
        JOKER_HAIBA_FINISH(407),
        DS_CHESS(501),
        KING_MAKER(502),
        PG_CHESS(503),
        TCG_LOTTERY(701),
        SLOTTO_LOTTERY(702),
        FUTURES_LOTTERY(703),
        S128_ANIMAL(801),
        SV388_ANIMAL(802),
        ;

        private Integer code;

        GAME(Integer code) {
            this.code = code;
        }
    }

    @Getter
    enum BETS_TYPE {
        /***
         * 类型: 1-拉单 2-补单
         */
        PULL_BET(1),
        SUPPLEMENT_BET(2);
        private Integer code;

        BETS_TYPE(Integer code) {
            this.code = code;
        }
    }

    /*
     * 体育有效投注额状态
     */
    List<Integer> SPORTS_STATUS_LIST = List.of(
            BasePlatParam.BetRecordsStatus.WIN.getCode(),
            BasePlatParam.BetRecordsStatus.LOSE.getCode(),
            BasePlatParam.BetRecordsStatus.DRAW.getCode(),
            BasePlatParam.BetRecordsStatus.WIN_HALF.getCode(),
            BasePlatParam.BetRecordsStatus.LOSE_HALF.getCode()
    );
}
