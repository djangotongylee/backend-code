package com.xinbo.sports.plat.io.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Pattern;

/**
 * @author: David
 * @date: 04/05/2020
 * @description:
 */
@Data
public class RegisterAgentReqDto {
    @Pattern(regexp = "^[0-9a-zA-Z_\\/\\.]{6,64}$", message = "6到64位字母、数组、下划线、'/'、'.'组成")
    @ApiModelProperty(name = "category", value = "在白牌系统中唯一的代理名称", example = "xbsportsXXXX")
    private String username;
}
