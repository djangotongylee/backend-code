package com.xinbo.sports.plat.io.dto.base;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author: David
 * @date: 14/05/2020
 * @description:
 */
@Data
public class BasePlatReqDto {
    @ApiModelProperty(name = "lang", value = "语言:en-英文 简体(zh-cn) 繁体(zh-tw)", example = "en")
    private String lang;

    @ApiModelProperty(name = "device", value = "设备:H5-m PC-d 安卓-android, IOS-ios", example = "m")
    private String device;
}
