package com.xinbo.sports.plat.io.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * @author: wells
 * @date: 2020/7/22
 * @description:
 */

public interface SBPlatEnum {

    /**
     * 沙巴体育方法枚举
     */
    @Getter
    @AllArgsConstructor
    enum SBActionEnum {
        LOGIN("LogIn", "登录用户"),
        CREATE("CreateMember", "注册用户"),
        FUND_TRANSFER("FundTransfer", "资金转账"),
        CHECK_USER_BALANCE("CheckUserBalance", "查询余额"),
        CHECK_FUND_TRANSFER("CheckFundTransfer", "检查资金转账状态"),
        GET_BET("GetBetDetail", "获取会员投注信息");
        private String methodName;
        private String methodNameDesc;
    }


    /**
     * 货币
     */
    @Getter
    @AllArgsConstructor
    enum CURRENCY {
        MYR("2", "马元"),
        USD("3", "美元"),
        THB("4", "泰铢 "),
        HKD("5", "港币"),
        EUR("6", "欧元"),
        AUD("9", "澳币"),
        GBP("12", " 英镑"),
        RMB("13", "人民币"),
        IDR("15", "印度尼西亚盾"),
        UUS("20", "虚拟货币(测试环境使用)"),
        JAP("32", "日圆"),
        CHF("41", "瑞士法郎"),
        WON("45", "韩元"),
        BND("46", "文莱元"),
        MXN("49", "墨西哥比绍"),
        CAN("50", "加币"),
        INH("51", "(VND) 越南盾"),
        DKK("52", "丹麦克朗"),
        SEK("53", "瑞典克朗"),
        NOK("54", "挪威克朗"),
        RUB("55", "卢布"),
        PLN("56", "波兰兹罗提"),
        CZK("57", "捷克克朗"),
        RON("58", "罗马尼亚列伊"),
        INR("61", "卢比"),
        MMK("70", "(MKK) 缅甸元"),
        KHR("71", "柬埔寨瑞尔"),
        LIR("73", " 土耳其里拉");

        private String code;
        private String name;
    }


}
