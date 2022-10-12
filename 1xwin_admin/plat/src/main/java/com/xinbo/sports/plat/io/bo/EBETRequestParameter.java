package com.xinbo.sports.plat.io.bo;

import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.models.auth.In;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * @author: wells
 * @date: 2020/5/27
 * @description:
 */

public interface EBETRequestParameter {

     Integer PAGE_SIZE=500;

     Integer PAGE_NUM=1;



    @Builder
    @Data
    class Register {

        private Integer channelId;
        private String signature;
        private String username;
        private String currency;

    }

    @Builder
    @Data
    class Login {
        @JSONField(name = "bet_type")
        private Integer betType;
        @JSONField(name = "operator_token")
        private String operatorToken;
        @JSONField(name = "operator_player_session")
        private String operatorPlayerSession;
        @JSONField(name = "language")
        private String language;

    }

    @Builder
    @Data
    class GetHistoryForSpecificTimeRange {

        private Integer channelId;
        private Long timestamp;
        private String signature;
        private String username;
        private String currency;
        private String startTimeStr;
        private String endTimeStr;
        private Integer pageNum;
        private Integer pageSize;
        private Integer betstatus;



    }

    @Builder
    @Data
    class FundTransfer {
        private Integer channelId;
        private String username;
        private Long timestamp;
        private String signature;
        private BigDecimal money;
        private String rechargeReqId;
        private String currency;
        private BigDecimal coin;
    }



    @Builder
    @Data
    class FundTransferStatus {
        private Integer channelId;
        private String signature;
        private String rechargeReqId;
        private String currency;
    }

    @Builder
    @Data
    class QueryBalance {
        private Integer channelId;
        private String username;
        private String signature;
        private Integer subChannelId;
        private Integer minMoney;
        private String currency;
        private Integer walletType;
    }

    @Builder
    @Data
    class GameList {
        @JSONField(name = "operator_token")
        private String operatorToken;
        @JSONField(name = "secret_key")
        private String secretKey;
        @JSONField(name = "currency")
        private String currency;

    }

    @Builder
    @Data
    class GetGameReport {

        @JSONField(name = "fromDate")
        private String fromDate;
        @JSONField(name = "toDate")
        private String toDate;
        @JSONField(name = "timeAggregation")
        private String timeAggregation;
        @JSONField(name = "currency")
        private String currency;
        @JSONField(name = "utcOffset")
        private Integer utcOffset;

    }


    @Builder
    @Data
    class GetUserInfo {
        private Integer channelId;
        private String signature;
        private String username;
        private Long timestamp;
        private String currency;
    }


    @Data
    class BetDetail {
        /**
         * 注单号->betUID
         */
        private Integer gameType;
        private String gameName;
        @JSONField(name = "bet", serializeUsing = BigDecimal.class)
        private BigDecimal bet;
        private String roundNo;
        @JSONField(name = "payout", serializeUsing = BigDecimal.class)
        private BigDecimal payout;
        @JSONField(name = "payoutWithoutholding", serializeUsing = BigDecimal.class)
        private BigDecimal payoutWithoutholding;
        private Integer createTime;
        private Integer payoutTime;
        private String betHistoryId;
        @JSONField(name = "validBet", serializeUsing = BigDecimal.class)
        private BigDecimal validBet;
        @JSONField(name = "balance", serializeUsing = BigDecimal.class)
        private BigDecimal balance;
        private String username;
        private Integer userId;
        private Integer platform;
        private Integer playerResult;
        private Integer bankerResult;
        private Integer dragonCard;
        private Integer tigerCard;
        private Integer number;


    }

    @Builder
    @Data
    class LoginProxy {
        @JSONField(name = "operator_token")
        private String operatorToken;
        @JSONField(name = "secret_key")
        private String secretKey;

    }

    @Builder
    @Data
    class BetDetails {
        @JSONField(name = "t")
        private String t;
        @JSONField(name = "secret_key")
        private String secretKey;

    }

    @Getter
    enum CURRENCY {
        /**
         * AED 阿拉伯联合酋长国迪拉姆
         * ALL 阿尔巴尼亚列克
         * AMD 亚美尼亚德拉姆
         * ARS 阿根廷比索
         * AUD 澳大利亚元
         * AZN 阿塞拜疆马纳特
         * BAM 波黑可兑换马克
         * BDT 孟加拉塔卡
         * BGN 保加利亚列弗
         * BIF 布隆迪法郎
         * BND 汶莱元
         * BOB 玻利维亚诺
         * BRL 巴西里亚伊（雷亚尔）
         * CAD 加拿大元
         * CHF 瑞士法郎
         * CLP 智利比索
         * CNY 人民币
         * COP 哥伦比亚比索
         * CRC 哥斯达黎加科朗
         * CSD 塞爾維亞第納爾
         * CZK 捷克克朗
         * DKK 丹麦克朗
         * DOP 多米尼加比索
         * DZD 阿尔及利亚第纳尔
         * EGP 埃及镑
         * EUR 欧元
         * GBP 英镑
         * GEL 格鲁吉亚拉里
         * GHS 加纳塞地
         * GTQ 危地马拉格查尔
         * HNL 洪都拉斯伦皮拉
         * HRK 克罗地亚库纳
         * HUF 匈牙利福林
         * IDR 印度尼西亚卢比盾
         * ILS 以色列谢克尔
         * INR 印度卢比
         * IQD 伊拉克第纳尔
         * IRR 伊朗里亚尔
         * ISK 冰岛克朗
         * JPY 日元
         * KES 肯尼亚先令
         * KGS 吉尔吉斯斯坦索姆
         * KHR 柬埔寨利尔斯
         * KRW 韩元
         * KZT 坚戈
         * LBP 黎巴嫩镑
         * LKR 斯里兰卡卢比
         * LYD 利比亚第纳尔
         * MAD 摩洛哥迪拉姆
         * MBTC 比特币（虚拟货币）
         * MKD 马其顿第纳尔
         * MMK 缅甸元
         * MNT 蒙古圖格裡克
         * MWK 马拉维克瓦查
         * MXN 墨西哥比索
         * MZN 莫桑比克梅蒂卡尔
         * NGN 尼日利亚奈拉
         * NIO 尼加拉瓜科多巴
         * NOK 挪威克朗
         * NZD 新西兰元
         * PAB 巴拿马巴波亚
         * PEN 秘鲁索尔
         * PLN 波兰兹罗提
         * PYG 巴拉圭瓜拉尼
         * QAR 卡塔尔里亚尔
         * RON 罗马尼亚列伊
         * RSD 塞尔维亚第纳尔
         * RUB 俄罗斯卢布
         * SAR 沙特里亚尔
         * SCR 塞舌尔卢比
         * SDG 苏丹镑
         * SEK 瑞典克朗
         * SVC 萨尔瓦多科朗
         * SYP 叙利亚镑
         * THB 泰铢
         * TND 突尼斯第纳尔
         * TRY 土耳其里拉
         * TZS 坦桑尼亚先令
         * UAH 乌克兰赫夫米
         * UBTC 比特币（虚拟货币）
         * UGX 乌干达先令
         * USD 美元
         * UYU 乌拉圭比索
         * VND 越南盾
         * XAF 中非金融合作法郎
         * XOF CFA 法郎
         * YER 也门里亚尔
         * ZAR 南非兰特
         * ZMW 赞比亚克瓦查
         */
        RMB("CNY", "人民币"),
        THB("THB", "泰铢"),
        HKD("HKD", "港币"),
        PHP("PHP", "菲律宾披索"),
        VND("VND", "越南盾"),
        IDR("IDR", "印尼盾"),
        KRW("KRW", "韩元"),
        LAK("LAK", "老挝基普"),
        KHR("KHR", "柬埔寨瑞尔"),
        INR("INR", "印度卢比"),
        USD("USD", "美元"),
        MMK("KHR", "缅元"),
        MYR("MYR", "马来西亚币"),
        AED("AED", "阿拉伯联合酋长国迪拉姆"),
        ALL("ALL", "阿尔巴尼亚列克"),
        AMD("AMD", "亚美尼亚德拉姆"),
        ARS("ARS", "阿根廷比索"),
        AUD("AUD", "澳大利亚元"),
        AZN("AZN", "阿塞拜疆马纳特"),
        BAM("BAM", "波黑可兑换马克"),
        BDT("BDT", "孟加拉塔卡"),
        BGN("BGN", "保加利亚列弗"),
        BIF("BIF", "布隆迪法郎"),
        BND("BND", "汶莱元"),
        BOB("BOB", "玻利维亚诺"),
        BRL("BRL", "巴西里亚伊（雷亚尔）"),
        CAD("CAD", "加拿大元"),
        CHF("CHF", "瑞士法郎"),
        CLP("CLP", "智利比索"),
        CNY("CNY", "人民币"),
        COP("COP", "哥伦比亚比索"),
        CRC("CRC", "哥斯达黎加科朗"),
        CSD("CSD", "塞爾維亞第納爾"),
        CZK("CZK", "捷克克朗"),
        DKK("DKK", "丹麦克朗"),
        DOP("DOP", "多米尼加比索"),
        DZD("DZD", "阿尔及利亚第纳尔"),
        EGP("EGP", "埃及镑"),
        EUR("EUR", "欧元"),
        GBP("GBP", "英镑"),
        GEL("GEL", "格鲁吉亚拉里"),
        GHS("GHS", "加纳塞地"),
        GTQ("GTQ", "危地马拉格查尔"),
        HNL("HNL", "洪都拉斯伦皮拉"),
        HRK("HRK", "克罗地亚库纳"),
        HUF("HUF", "匈牙利福林"),
        ILS("ILS", "以色列谢克尔"),
        IQD("IQD", "伊拉克第纳尔"),
        IRR("IRR", "伊朗里亚尔"),
        ISK("ISK", "冰岛克朗"),
        JPY("JPY", "日元"),
        KES("KES", "肯尼亚先令"),
        KGS("KGS", "吉尔吉斯斯坦索姆"),
        KZT("KZT", "坚戈"),
        LBP("LBP", "黎巴嫩镑"),
        LKR("LKR", "斯里兰卡卢比"),
        LYD("LYD", "利比亚第纳尔"),
        MAD("MAD", "摩洛哥迪拉姆"),
        MBT("MBTC", "比特币（虚拟货币）"),
        MKD("MKD", "马其顿第纳尔"),
        MNT("MNT", "蒙古圖格裡克"),
        MWK("MWK", "马拉维克瓦查"),
        MXN("MXN", "墨西哥比索"),
        MZN("MZN", "莫桑比克梅蒂卡尔"),
        NGN("NGN", "尼日利亚奈拉"),
        NIO("NIO", "尼加拉瓜科多巴"),
        NOK("NOK", "挪威克朗"),
        NZD("NZD", "新西兰元"),
        PAB("PAB", "巴拿马巴波亚"),
        PEN("PEN", "秘鲁索尔"),
        PLN("PLN", "波兰兹罗提"),
        PYG("PYG", "巴拉圭瓜拉尼"),
        QAR("QAR", "卡塔尔里亚尔"),
        RON("RON", "罗马尼亚列伊"),
        RSD("RSD", "塞尔维亚第纳尔"),
        RUB("RUB", "俄罗斯卢布"),
        SAR("SAR", "沙特里亚尔"),
        SCR("SCR", "塞舌尔卢比"),
        SDG("SDG", "苏丹镑"),
        SEK("SEK", "瑞典克朗"),
        SVC("SVC", "萨尔瓦多科朗"),
        SYP("SYP", "叙利亚镑"),
        TND("TND", "突尼斯第纳尔"),
        TRY("TRY", "土耳其里拉"),
        TZS("TZS", "坦桑尼亚先令"),
        UAH("UAH", "乌克兰赫夫米"),
        UBT("UBTC", "比特币（虚拟货币）"),
        UGX("UGX", "乌干达先令"),
        UYU("UYU", "乌拉圭比索"),
        XAF("XAF", "中非金融合作法郎"),
        XOF("XOF", "CFA法郎"),
        YER("YER", "也门里亚尔"),
        ZAR("ZAR", "南非兰特"),
        ZMW("ZMW", "赞比亚克瓦查"),
        ;


        private String code;
        private String desc;

        CURRENCY(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }

    /**
     * en 英文 （默认）
     * da 丹麦文
     * de 德文
     * es 西班牙文
     * fi 芬兰文
     * fr 法文
     * id 印尼文
     * it 意大利文
     * ja 日文
     * ko 韩文
     * nl 荷兰文
     * no 挪威文
     * pl 波兰文
     * pt 葡萄牙文
     * ro 罗马尼亚文
     * ru 俄文
     * sv 瑞典文
     * th 泰文
     * tr 土耳其文
     * vi 越南文
     * zh 中文
     */
    @Getter
    enum LANGS {
        /**
         * 中英，部分游戏支持泰语
         * 语言信息
         */
        EN("en", "英文"),
        ZH("zh", "中文"),
        VI("vi", "越南文"),
        TH("th", "泰文"),
        DA("da", "丹麦文"),
        DE("de", "德文"),
        ES("es", "西班牙文"),
        FI("fi", "芬兰文"),
        FR("fr", "法文"),
        ID("id", "印尼文"),
        IT("it", "意大利文"),
        JA("ja", "日文"),
        KO("ko", "韩文"),
        NL("nl", "荷兰文"),
        NO("no", "挪威文"),
        PL("pl", "波兰文"),
        PT("pt", "葡萄牙文"),
        RO("ro", "罗马尼亚文"),
        RU("ru", "俄文"),
        SV("sv", "瑞典文"),
        TR("tr", "土耳其文"),
        ;

        private String code;
        private String message;

        LANGS(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }

}
