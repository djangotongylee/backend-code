package com.xinbo.sports.utils;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;

/**
 * <p>
 * 银行相关工具类
 * </p>
 *
 * @author andy
 * @since 2020/10/22
 */
public class BankUtils {
    /**
     * 银行卡卡号屏蔽部分号码
     *
     * @param bankAccount 16位或19位
     * @return 屏蔽后的卡号
     */
    public static String bankAccountFilter(String bankAccount) {
        if (StringUtils.isBlank(bankAccount) || bankAccount.length() < 11) {
            return bankAccount;
        }
        String start = bankAccount.substring(0, 4);
        String tmp = bankAccount.substring(4, bankAccount.length());
        String end = "";
        if (tmp.length() % 4 == 0) {
            end = tmp.substring(tmp.length() - 4);
        } else {
            end = tmp.substring(tmp.length() - 7);
        }
        StringBuilder result = new StringBuilder();
        result.append(start);
        for (int i = 0; i < bankAccount.length() - (start.length() + end.length()); i++) {
            result.append("*");
        }
        result.append(end);
        return result.toString();
    }

//    public static void main(String[] args) {
//        // 1233523451196111  -> 1233********6111
//        String bankAccount = "1233523451196111";
//        System.out.println(bankAccountFilter(bankAccount));
//        // 1233523451196177999 -> 1233********6177999
//        bankAccount = "1233523451196177999";
//        System.out.println(bankAccountFilter(bankAccount));
//    }
}
