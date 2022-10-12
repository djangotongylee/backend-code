package com.xinbo.sports.service.io.enums;

import lombok.*;

/**
 * @author: David
 * @date: 15/06/2020
 */
public interface BaseEnum {
    @Getter
    @NoArgsConstructor
    enum LANG {
        /**
         * 请求语言参数
         */
        // 中文
        ZH("zh"),
        // 英语
        EN("en"),
        // 泰语
        TH("th"),
        // 越南语
        VI("vi"),
        ;
        String value;

        LANG(String value) {
            this.value = value;
        }
    }

    @Getter
    @NoArgsConstructor
    enum DEVICE {
        /**
         * 请求设备
         */
        // mobile - 手机
        M("m"),
        // desktop - PC
        D("d"),
        // 安卓
        ANDROID("android"),
        // IOS
        IOS("ios"),
        ;
        String value;

        DEVICE(String value) {
            this.value = value;
        }
    }

    /**
     * RSA 公钥、私钥
     */
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    class RSA {
        String publicKey;
        String privateKey;
    }

    /**
     * JWT 密钥、过期时间
     */
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    class JWT {
        String secret;
        Long expired;
    }

    /**
     * StaticServerConfig
     */
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    class StaticServerConfig {
        String url;
        Integer index;
    }

    /**
     * sms配置 PROP253
     */
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    class PROP253 {
        String url;
        String account;
        String password;
        String code;
        String signature;
    }

    /**
     * sms配置 GlobalPROP
     */
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    class PROPSkyLine {
        String url;
        String account;
        String password;
        String senderId;
        String code;
        String signature;
    }

    /**
     * sms配置 nxcloud
     */
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    class PROP_NX_CLOUD {
        String url;
        String appKey;
        String secretKey;
        String code;
        String signature;
    }


    /**
     * sms配置 nodesms
     */
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    class PROPNODE {
        String url;
        String username;
        String api_password;
        String web_password;
        String web;
        String code;
        String signature;
    }

    /**
     * 消息推送:E(设备) : D-web端   B-后台
     */
    @Getter
    @NoArgsConstructor
    enum MessageDevice {
        D("D", "web端"),
        B("B", "后台");
        String code;
        String desc;

        MessageDevice(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }

    @Getter
    @NoArgsConstructor
    enum MessagePlatform {
        SMSTH("253", "253"),
        SMSIN("SkyLine", "天一"),
        SMSNX("nxcloud", "nxcloud"),
        SMSNODE("node", "NODESMS");
        String code;
        String desc;

        MessagePlatform(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }

    /**
     * @author: David
     * @date: 17/09/2020
     */
    @Getter
    enum MsgActionEnum {
        /**
         * 消息行为
         */
        CONNECT(1, "初始化连接"),
        CHAT(2, "聊天消息"),
        KEEP_ALIVE(3, "心跳检测"),
        PUSH_NOTICE(4, "公告推送"),
        PUSH_ENVELOP(5, "红包雨推送"),
        PUSH_DN(6, "推送存款笔数"),
        PUSH_WN(7, "推送提款笔数"),
        PUSH_ON(8, "推送在线人数"),
        ;

        Integer type;
        String content;

        MsgActionEnum(Integer type, String content) {
            this.type = type;
            this.content = content;
        }
    }
}
