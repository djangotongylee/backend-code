package com.xinbo.sports.apiend.io.bo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.*;

/**
 * @author: David
 * @date: 06/06/2020
 */
public interface UserParams {
    /**
     * 登录入参
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class LoginReqDto {
        /**
         * 三晟体育 (最小长度：8 最大长度：48 不区分大小写)
         */
        @NotNull(message = "用户名不能为空")
        @Pattern(regexp = "^[0-9a-zA-Z]{6,10}$", message = "6到10位字母、数字组成")
        @ApiModelProperty(name = "username", value = "用户名", example = "A2001")
        private String username;

        @NotNull(message = "密码不能为空")
        @Pattern(regexp = "^[A-Za-z0-9~'`!@#￥$%^&*()-+_=:|]{4,16}$", message = "4到16位英文、字母、特殊符号组成")
        @ApiModelProperty(name = "password", value = "密码", example = "******")
        private String password;
    }

    /**
     * 登录入参
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class LoginByMobileReqDto {
        @NotNull(message = "手机号码不能为空")
        @ApiModelProperty(name = "mobile", value = "手机号码", example = "6363456787")
        private String mobile;

        @NotNull(message = "手机验证码不能为空")
        @Pattern(regexp = "^[0-9]{4,8}$", message = "4-8位数字")
        @ApiModelProperty(name = "verificationCode", value = "手机验证码", example = "456312")
        private Integer verificationCode;
    }

    /**
     * 注册入参
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    class CreateReqDto extends SendSmsCodeReqDto {
        /**
         * 三晟体育 (最小长度：8 最大长度：48 不区分大小写)
         * TCG min=4 max=14
         */
        @NotNull(message = "用户名不能为空")
        @Pattern(regexp = "^[0-9a-zA-Z]{6,10}$", message = "6到10位字母、数字组成")
        @ApiModelProperty(name = "username", value = "用户名", example = "A2001")
        private String username;

        @NotNull(message = "密码不能为空")
        @Pattern(regexp = "^[A-Za-z0-9~'`!@#￥$%^&*()-+_=:|]{4,16}$", message = "4到16位英文、字母、特殊符号组成")
        @ApiModelProperty(name = "password", value = "密码", example = "******")
        private String password;


        @ApiModelProperty(name = "smsCode", value = "手机验证码", example = "8888")
        private Long smsCode;

        @ApiModelProperty(name = "promoCode", value = "推广码", example = "8888")
        private Integer promoCode;

        @ApiModelProperty(name = "link", value = "推广域名", example = "testwww.xinbosports.com", hidden = true)
        private String link;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel(value = "UpdateProfileReqDto", description = "更新用户返回实体类(头像需单独更新)")
    class UpdateProfileReqDto {
        @Pattern(regexp = "^[0-9a-zA-Z_/.]{6,64}$", message = "6到64位字母、数组、下划线、'/'、'.'组成")
        @ApiModelProperty(name = "avatar", value = "头像", example = "/avatar/1.png")
        private String avatar;

        @Pattern(regexp = "^[\\u4e00-\\u9fa5_a-zA-Z]{2,32}", message = "2到32位中文、字母、下划线组成")
        @ApiModelProperty(name = "realname", value = "真实姓名", example = "李四")
        private String realname;

        @Min(value = 0, message = "最小值0")
        @Max(value = 1, message = "最大值1")
        @ApiModelProperty(name = "autoTransfer", value = "自动上下分:1-是 0-否", example = "1")
        private Integer autoTransfer;

        @Pattern(regexp = "^(19|20)\\d{2}-(1[0-2]|0?[1-9])-(0?[1-9]|[1-2][0-9]|3[0-1])$", message = "生日格式不正确")
        @ApiModelProperty(name = "birthday", value = "生日", example = "1990-01-01")
        private String birthday;

        @Pattern(regexp = "^[\\u4e00-\\u9fa5_a-zA-Z]{2,32}", message = "2到32位中文、字母、下划线组成")
        @ApiModelProperty(name = "signature", value = "个性签名", example = "壁立千仞")
        private String signature;

        @Min(value = 0)
        @Max(value = 2)
        @ApiModelProperty(name = "sex", value = "性别", example = "1")
        private Integer sex;

        @Pattern(regexp = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$", message = "邮箱地址格式不对")
        @ApiModelProperty(name = "email", value = "邮箱", example = "admin@gmail.com")
        private String email;

        @ApiModelProperty(name = "address", value = "家庭地址", example = "香港|XX|AA|#1202栋，8888房")
        private String address;
    }

    /**
     * 验证登录密码
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class ValidatePasswordHashReqDto {
        @Pattern(regexp = "^[A-Za-z0-9~'`!@#￥$%^&*()-+_=:|]{4,16}$", message = "4到16位英文、字母、特殊符号组成")
        @ApiModelProperty(name = "passwordHash", value = "资金密码", example = "123qwe")
        private String passwordHash;
    }

    /**
     * 验证资金密码
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class ValidatePasswordCoinReqDto {
        @Pattern(regexp = "^[A-Za-z0-9~'`!@#￥$%^&*()-+_=:|]{4,16}$", message = "4到16位英文、字母、特殊符号组成")
        @ApiModelProperty(name = "passwordCoin", value = "资金密码", example = "123qwe")
        private String passwordCoin;
    }

    /**
     * 插入或新增用户地址
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class InsertSaveAddressReqDto {
        @Min(value = 0, message = "最小值0")
        @ApiModelProperty(name = "id", value = "地址ID", example = "1")
        private Integer id;

        @NotEmpty(message = "地址不能为空")
        @ApiModelProperty(name = "address", value = "会员地址", example = "香港|XX|AA|#1202栋，8888房")
        private String address;

        @NotEmpty(message = "姓名不能为空")
        @ApiModelProperty(name = "name", value = "收件人姓名", example = "张三")
        private String name;

        @NotEmpty(message = "区号不能为空")
        @ApiModelProperty(name = "areaCode", value = "区号")
        private String areaCode;

        @NotEmpty(message = "手机号码不能为空")
        @ApiModelProperty(name = "mobile", value = "手机号码")
        private String mobile;
    }

    /**
     * 删除或设置默认用户地址
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class DeleteOrDefaultAddressReqDto {
        @Min(value = 0, message = "ID最小0")
        @ApiModelProperty(name = "id", value = "地址ID", example = "1")
        private Integer id;
    }

    /**
     * 发送验证码
     */
    @Data
    @SuperBuilder
    @AllArgsConstructor
    @NoArgsConstructor
    class SendSmsCodeReqDto {
        @ApiModelProperty(name = "areaCode", value = "区号", example = "86")
        private String areaCode;

        @ApiModelProperty(name = "mobile", value = "手机号码", example = "123123")
        private String mobile;
    }

    @Data
    class UpdateSaveAddressReqDto {
        @Min(value = 0, message = "ID最小0")
        @ApiModelProperty(name = "id", value = "地址ID", example = "1")
        private Integer id;

        @Pattern(regexp = "^[\\u4e00-\\u9fa5A-Za-z0-9~'`!@#￥$%^&*()-+_=:,，。、|]{4,255}$", message = "4到255位中英文、字母、特殊符号组成")
        @ApiModelProperty(name = "address", value = "会员地址", example = "香港|XX|AA|#1202栋，8888房")
        private String address;
    }

    /**
     * 用户地址返回实体
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class UserAddressResDto {
        @ApiModelProperty(name = "id", value = "ID", example = "8888")
        private Integer id;

        @ApiModelProperty(name = "name", value = "收件人姓名", example = "张三")
        private String name;

        @ApiModelProperty(name = "areaCode", value = "区号", example = "086")
        private String areaCode;

        @ApiModelProperty(name = "mobile", value = "手机号码", example = "123123")
        private String mobile;

        @ApiModelProperty(name = "address", value = "会员地址", example = "香港|XX|AA|#1202栋，8888房")
        private String address;

        @ApiModelProperty(name = "status", value = "状态:1-默认地址(启用) 2-正常启用 3-删除", example = "1")
        private Integer status;

        @ApiModelProperty(name = "createdAt", value = "创建时间", example = "1587371198")
        private Integer createdAt;
    }

    /**
     * 忘记密码入参
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    class ForgotPasswordReqDto extends SendSmsCodeReqDto {
        @NotNull(message = "密码不能为空")
        @Pattern(regexp = "^[A-Za-z0-9~'`!@#￥$%^&*()-+_=:|]{4,16}$", message = "4到16位英文、字母、特殊符号组成")
        @ApiModelProperty(name = "password", value = "密码", example = "******")
        private String password;

        @NotNull(message = "验证码不能为空")
        @Min(value = 1000, message = "验证码格式不正确")
        @Max(value = 99999999, message = "验证码格式不正确")
        @ApiModelProperty(name = "smsCode", value = "手机验证码", example = "8888")
        private Long smsCode;
    }

    /**
     * 重置密码入参
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    class ResetPasswordReqDto extends SendSmsCodeReqDto {
        @NotNull(message = "密码不能为空")
        @Pattern(regexp = "^[A-Za-z0-9~'`!@#￥$%^&*()-+_=:|]{4,16}$", message = "4到16位英文、字母、特殊符号组成")
        @ApiModelProperty(name = "password", value = "密码", example = "******")
        private String password;

        @NotNull(message = "验证码不能为空")
        @Min(value = 1000, message = "验证码格式不正确")
        @Max(value = 99999999, message = "验证码格式不正确")
        @ApiModelProperty(name = "smsCode", value = "手机验证码", example = "8888")
        private Long smsCode;

        @NotNull(message = "类型不能为空")
        @Min(value = 1, message = "类型错误")
        @Max(value = 2, message = "类型错误")
        @ApiModelProperty(name = "category", value = "类型:1-登录密码 2-资金密码", example = "1")
        private Integer category;
    }

    /**
     * 修改手机号入参
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    class ResetMobileReqDto extends SendSmsCodeReqDto {
        @NotNull(message = "验证码不能为空")
        @Min(value = 1000, message = "验证码格式不正确")
        @Max(value = 99999999, message = "验证码格式不正确")
        @ApiModelProperty(name = "smsCode", value = "手机验证码", example = "8888")
        private Long smsCode;
    }

    /**
     * 密码验证是否正确
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    class ValidateResDto {
        @ApiModelProperty(name = "success", value = "状态:1-成功 0-失败", example = "1")
        private Integer success;
    }
}
