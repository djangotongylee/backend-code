package com.xinbo.sports.plat.io.enums;

import lombok.*;

/**
 * @author: David
 * @date: 04/05/2020
 * @description:
 */
public interface HBRoutineParam {
    enum ErrorCode {
        /**
         * 错误信息配置列表
         *
         * @author: David
         * @date: 11/05/2020
         */
        PARAMETER_ERROR("Parameter Error"),
        TIME_OUT("Time Out"),
        ;

        String message;

        ErrorCode(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

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
        TH("th", "泰语"),
        VI("vi", "Vietnamese越南");

        String code;
        String message;

        LANGS(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }

    /**
     * 货币
     */
    @Getter
    enum CHANNEL_TYPE_ID {
        DESKTOP_BROWSER(1),
        MOBILE_BROWSER(4),
        APP_ANDROID(5),
        APP_IOS(6),
        ;

        Integer id;

        CHANNEL_TYPE_ID(Integer id) {
            this.id = id;
        }
    }

    /**
     * 平台配置信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class PlatConfig {
        String brandId;
        String apiKey;
        String apiUrl;
        String loginUrl;
        String currencyCode;
        String environment;
        String playerPwd;
    }

    /**
     * 货币:参考文档第120-122页
     * -------------------------------------
     * Currency                         Code
     * -------------------------------------
     * <p>
     * Albania Lek                      ALL
     * Algerian Dinar                   DZD
     * Angolan Kwanza                   AOA
     * Argentine Peso                   ARS
     * Armenian Dram                    AMD
     * Australian Dollars               AUD
     * Azerbaijani Manat                AZN
     * Bahamian Dollar                  BSD
     * Bahraini Dinar                   BHD
     * Bangladeshi Taka                 BDT
     * Belarusian Ruble                 BYR
     * Bolivian Boliviano               BOB
     * Bosnia-Herzegovina Mark          BAM
     * Brazilian Real                   BRL
     * British Pounds                   GBP
     * Bulgarian Lev                    BGN
     * Burmese Kyat                     MMK
     * Canada Dollars                   CAD
     * Central African CFA Franc        XAF
     * Chilean Peso                     CLP
     * China Yuan Renminbi              CNY
     * Colombian Peso                   COP
     * Congolese franc                  CDF
     * Croatian Kuna                    HRK
     * Czech Koruna                     CZK
     * Danish Krone                     DKK
     * Dominican Peso                   DOP
     * Egyptian Pound                   EGP
     * Ethiopian Birr                   ETB
     * Euros                            EUR
     * Georgian Lari                    GEL
     * Ghana Cedi                       GHS
     * Haitian Gourde                   HTG
     * Hong Kong Dollars                HKD
     * Hungarian Forint                 HUF
     * Icelandic Króna                  ISK
     * Indian rupee                     INR
     * Indonesia Rupiahs (1 = Rp1)      IDR
     * Indonesia Rupiahs (1 = Rp1000)   IDR2
     * Iranian Rial                     IRR
     * Iraqi Dinar                      IQD
     * Israeli New Sheqel               ILS
     * Japan Yen                        JPY
     * Jordanian Dinar                  JOD
     * Kazakhstani Tenge                KZT
     * Kenyan Shilling                  KES
     * Korea (South) Won                KRW
     * Kuwaiti dinar                    KWD
     * Kyrgystani Som                   KGS
     * Laotian Kip (1=₭1)               LAK
     * Laotian Kip (1=₭1000)            LAK2
     * Lesotho Loti                     LSL
     * Macau Patacas                    MOP
     * Macedonian Denar                 MKD
     * Malagasy Ariary                  MGA
     * Malawian Kwacha                  MWK
     * Malaysian Ringgit                MYR
     * Mauritian Rupee                  MUR
     * Mexican Pesos                    MXN
     * Moldovan Leu                     MDL
     * Mongolian Tugrik                 MNT
     * Moroccan Dirham                  MAD
     * Mozambican Metical               MZN
     * New Zealand Dollars              NZD
     * Nigerian naira                   NGN
     * Norwegian krone                  NOK
     * Omani Rial                       OMR
     * Paraguayan Guarani               PYG
     * Peruvian Nuevo Sol               PEN
     * Philippines Pesos                PHP
     * Polish złoty                     PLN
     * Renminbi                         RMB
     * Romanian Leu                     RON
     * Russian Ruble                    RUB
     * Rwandan Franc                    RWF
     * Saudi Arabia Riyal               SAR
     * Serbian Dinar                    RSD
     * Seychelles Rupee                 SCR
     * Singapore Dollars                SGD
     * South African Rands              ZAR
     * Sudan Pound                      SDG
     * Swaziland Lilangeni              SZL
     * Swedish krona                    SEK
     * Swiss Franc                      CHF
     * Taiwan New Dollars               TWD
     * Taiwan New Dollars (NTD Code)    NTD
     * Tajikistani Somoni               TJS
     * Tanzanian Shilling               TZS
     * Thailand Baht                    THB
     * Trinidad & Tobago Dollar         TTD
     * Tunisian Dinar                   TND
     * Turkish Lira                     TRY
     * Turkmenistani manat              TMT
     * UAE Dirham                       AED
     * Ugandan Shilling                 UGX
     * Ukrainian Hryvnia                UAH
     * Uruguayan Peso                   UYU
     * US Dollars                       USD
     * Uzbekistani Som                  UZS
     * Venezuelan Bolívar               VEF
     * Vietnam Dong (1 = ₫1)            VND
     * Vietnam Dong (1 = ₫1000)         VND2
     * West African CFA Franc           XOF
     * Zambian Kwacha                   ZMW
     */
    @Getter
    enum Currency {
        /**
         * 美元
         */
        USD("USD", "US Dollars"),
        /**
         * 人民币
         */
        RMB("RMB", "Renminbi"),
        /**
         * 台币
         */
        TWD("TWD", "Taiwan New Dollars"),
        /**
         * 新台币
         */
        NTD("NTD", "Taiwan New Dollars (NTD Code)"),
        /**
         * 港币
         */
        HKD("HKD", "Hong Kong Dollars"),
        /**
         * 菲律宾披索
         */
        PHP("PHP", "Philippines Pesos"),
        /**
         * 越南盾
         */
        VND("VND", "Vietnam Dong (1 = ₫1)"),
        /**
         * 越南盾2
         */
        VND2("VND2", "Vietnam Dong (1 = ₫1000)"),
        /**
         * 泰铢
         */
        THB("THB", "Thailand Baht"),
        /**
         * 印度卢比
         */
        INR("INR", "Indian rupee"),
        /**
         * 马来西亚币
         */
        MYR("MYR", "Malaysian Ringgit");

        /**
         * 币种
         */
        private String code;
        /**
         * 币种说明
         */
        private String desc;

        Currency(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }
}
