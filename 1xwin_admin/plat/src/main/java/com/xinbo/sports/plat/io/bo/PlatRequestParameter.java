package com.xinbo.sports.plat.io.bo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Builder;
import lombok.Data;

/**
 * @author: David
 * @date: 02/05/2020
 * @description: 三方平台入参表
 */
public interface PlatRequestParameter {

    /**
     * SBO注册代理入参实体类
     */
    @Data
    @Builder
    class RegisterAgent {
        @JSONField(name = "Username")
        private String username;
        @JSONField(name = "Password")
        private String password;
    }

    /**
     * 注册会员
     */
    @Data
    @Builder
    class RegisterPlayer {
        @JSONField(name = "Username")
        private String username;
        @JSONField(name = "Agent")
        private String agent;
    }

    /**
     * 更新会员状态
     */
    @Data
    @Builder
    class UpdatePlayerStatus {
        @JSONField(name = "Username")
        private String username;
        @JSONField(name = "Status")
        private String status;
    }

    /**
     * 登出
     */
    @Data
    @Builder
    class Logout {
        @JSONField(name = "Username")
        private String username;
    }

    /**
     * 登录
     */
    @Data
    @Builder
    class Login {
        @JSONField(name = "Username")
        private String username;
        /*zh-cn=>简体中文、en=>英文*/
        private String lang;
        private String theme;
        /*d=>desktop   m=> mobile*/
        private String device;
    }
}
