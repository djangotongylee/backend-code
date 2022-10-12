package com.xinbo.sports.plat.io.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * <p>
 * WMLive请求参数
 * </p>
 *
 * @author andy
 * @since 2020/7/15
 */
public interface WmLiveRequestParameter {
    /**
     * config配置类:对应sp_plat_list表的config字段
     */
    @Data
    @Builder
    @AllArgsConstructor
    class WmConfig {
        /**
         * 域名地址
         */
        private String apiUrl;

        private String vendorId;

        private String signature;
        /**
         * 币种:正式環境有提供泰銖 ，測試環境目前統一都是提供人民幣幣別測試
         */
        private String currency;
        /**
         * 环境:PROD-生产环境
         */
        private String environment;
        /**
         * 用户名前缀:
         * 规则:接口方的username=前缀+username
         */
        private String prefix;
    }
}
