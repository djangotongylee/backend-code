package com.xinbo.sports.apiend.io.dto.centeragent;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 代理中心-我的下级-列表-直推人数列表(弹框)
 * </p>
 *
 * @author andy
 * @since 2020/5/4
 */
@Data
public class SubordinateListZt {

    @ApiModelProperty(name = "username", value = "用户名称", example = "andy")
    private String username;

    @ApiModelProperty(name = "createdAt", value = "时间", example = "1587226981")
    private Integer createdAt;

}
