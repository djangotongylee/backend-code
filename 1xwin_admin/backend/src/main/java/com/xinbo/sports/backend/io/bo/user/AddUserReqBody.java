package com.xinbo.sports.backend.io.bo.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * <p>
 * 会员管理-添加会员-请求body
 * </p>
 *
 * @author andy
 * @since 2020/3/12
 */
@Data
public class AddUserReqBody {

    @NotNull(message = "role不能为空")
    @ApiModelProperty(required = true, name = "role", value = "账号类型:0-会员 1-代理", example = "0")
    private Integer role;

    @NotBlank(message = "username不能为空")
    @ApiModelProperty(required = true, name = "username", value = "用户名", example = "username123")
    private String username;

    @NotBlank(message = "supUsername不能为空")
    @ApiModelProperty(required = true, name = "supUsername", value = "上级代理", example = "david2020")
    private String supUsername;

    @NotNull(message = "levelId不能为空")
    @ApiModelProperty(required = true, name = "levelId", value = "会员等级", example = "1")
    private Integer levelId;

    @NotBlank(message = "passwordHash不能为空")
    @ApiModelProperty(required = true, name = "passwordHash", value = "登录密码", example = "1234556678")
    private String passwordHash;
}
