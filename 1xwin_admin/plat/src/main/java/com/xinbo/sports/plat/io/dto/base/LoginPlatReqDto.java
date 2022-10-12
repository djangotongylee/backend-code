package com.xinbo.sports.plat.io.dto.base;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author: David
 * @date: 14/05/2020
 * @description:
 */
@Data
public class LoginPlatReqDto extends BasePlatReqDto {
    @ApiModelProperty(name = "username", value = "用户名", example = "test999")
    private String username;

    @ApiModelProperty(name = "slotId", value = "电子游戏类ID", example = "")
    private String slotId;
}
