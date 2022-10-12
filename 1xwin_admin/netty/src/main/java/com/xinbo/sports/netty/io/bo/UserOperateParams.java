package com.xinbo.sports.netty.io.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: David
 * @date: 17/09/2020
 */
public interface UserOperateParams {
    /**
     * 登录、登出日志
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    class addLoginOrLogoutLogReqDto {
        public Integer id;
        public String username;
    }
}
