package com.xinbo.sports.service.io.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: David
 * @date: 13/06/2020
 */
public interface BaseParams {
    /**
     * Header 信息
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    class HeaderInfo {
        @ApiModelProperty(name = "id", value = "会员ID", example = "1")
        public Integer id;
        @ApiModelProperty(name = "username", value = "会员姓名")
        public String username;
        @ApiModelProperty(name = "lang", value = "语言:en-英文, zh-cn-简体, zh-tw-繁体")
        public String lang;
        @ApiModelProperty(name = "device", value = "设备:m-H5, d-PC, android-安卓, ios-苹果")
        public String device;
    }
}
