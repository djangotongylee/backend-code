package com.xinbo.sports.utils;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.HashMap;
import java.util.Random;

/**
 * @author: David
 * @date: 06/04/2020
 * @description:
 */
public class TextUtils {
    /**
     * 生成随机字符串
     */
    public static String generateRandomString(Integer length) {
        return RandomStringUtils.randomAlphabetic(length);
    }

    /**
     * 生成随机数字
     */
    public static Integer generatePromoCode() {
        // 100000 ～ 999999 随机字符串
        Random rand = new Random();
        return rand.nextInt(990000) + 10000;
    }

    /**
     * @desc: 生成ApiToken
     * @params: []
     * @return: java.lang.String
     * @author: David
     * @date: 06/04/2020
     */
    public static String generateApiToken() {
        return TextUtils.generateRandomString(32) + "_" + DateUtils.getCurrentTime();
    }

    /**
     * 拼接表单
     *
     * @param url
     * @param paramsMap
     * @return
     */
    public static String renderForm(String url, HashMap<String, Object> paramsMap) {
        var stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("<form action='%s' method='post'>", url));
        paramsMap.forEach((k, v) ->
                stringBuilder.append(String.format("<input type='hidden' name='%s' value='%s' />", k, v)));
        stringBuilder.append("</form>loading......");
        return stringBuilder.toString();
    }
}
