package com.xinbo.sports.utils;

import com.github.mervick.aes_everywhere.Aes256;
import lombok.extern.slf4j.Slf4j;

/**
 * @description: DS棋牌接口调用依赖AES256加密工具类
 * @author: andy
 * @date: 2020/8/3
 */
@Slf4j
public class Aes256Utils {
    /**
     * AES256加密
     *
     * @param content 明文内容
     * @param aesKey  AES加密KEY
     * @return 加密后秘文
     */
    public static String encrypt(String content, String aesKey) {
        try {
            return Aes256.encrypt(content, aesKey);
        } catch (Exception e) {
            log.error("decrypt失败:" + e);
        }
        return null;
    }

    /**
     * AES256解密
     *
     * @param content 秘文内容
     * @param aesKey  AES加密KEY
     * @return 解密后明文内容
     */
    public static String decrypt(String content, String aesKey) {
        try {
            return Aes256.decrypt(content, aesKey);
        } catch (Exception e) {
            log.error("decrypt失败:" + e);
        }
        return null;
    }
}
