package com.xinbo.sports.plat.io.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * <p>
 * DGLive请求参数
 * </p>
 *
 * @author andy
 * @since 2020/5/19
 */
public interface DGLiveRequestParameter {
    /**
     * 注册新会员
     */
    @Data
    @Builder
    class Register {
        private String username;
        private String password;
        private String currencyName;
        private Integer winLimit;
    }

    /**
     * 通用
     */
    @Data
    @Builder
    class ReqBase {
        private String token;
        private int random;
    }

    /**
     * config配置类:对应sp_plat_list表的config字段
     */
    @Data
    @Builder
    @AllArgsConstructor
    class DgConfig {
        /**
         * 域名地址
         */
        private String apiUrl;

        /**
         * 域名地址:补单
         */
        private String reportUrl;

        private String agentName;

        private String apiKey;
        /**
         * 币种
         */
        private String currency;
        /**
         * 环境:PROD-生产环境
         */
        private String environment;
    }
}
