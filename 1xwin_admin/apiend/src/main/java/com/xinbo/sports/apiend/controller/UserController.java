package com.xinbo.sports.apiend.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import com.xinbo.sports.apiend.aop.annotation.RequestLimit;
import com.xinbo.sports.apiend.aop.annotation.UnCheckToken;
import com.xinbo.sports.apiend.io.bo.UserParams;
import com.xinbo.sports.apiend.io.bo.UserParams.*;
import com.xinbo.sports.apiend.io.dto.mapper.UserInfoResDto;
import com.xinbo.sports.apiend.service.IUserInfoService;
import com.xinbo.sports.service.io.dto.api.SmsParams;
import com.xinbo.sports.service.thread.ThreadHeaderLocalData;
import com.xinbo.sports.utils.components.response.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.text.ParseException;
import java.util.List;

/**
 * @author: David
 * @date: 06/04/2020
 * @description:
 */
@Slf4j
@RestController
@Api(tags = "首页 - 用户管理")
@ApiSort(2)
@RequestMapping("/v1/user")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserController {
    private final IUserInfoService userInfoServiceImpl;

    @PostMapping(value = "/create")
    @ApiOperation(value = "创建用户", notes = "创建用户")
    @ApiOperationSupport(author = "David", order = 1)
    @UnCheckToken
    public Result<UserInfoResDto> create(@Valid @RequestBody UserParams.CreateReqDto dto, HttpServletRequest request) {
        return Result.ok(userInfoServiceImpl.create(dto, request));
    }

    @PostMapping(value = "/login")
    @ApiOperation(value = "登录", notes = "登录")
    @ApiOperationSupport(author = "David", order = 2)
    @UnCheckToken
    public Result<UserInfoResDto> login(@Valid @RequestBody UserParams.LoginReqDto dto, HttpServletRequest request) {
        return Result.ok(userInfoServiceImpl.login(dto, request));
    }

    @PostMapping(value = "/logout")
    @ApiOperation(value = "登出", notes = "登出")
    @ApiOperationSupport(author = "David", order = 2)
    public Result<String> logout() {
        userInfoServiceImpl.logout();
        return Result.ok();
    }

    @PostMapping(value = "/userProfile")
    @ApiOperation(value = "获取用户信息", notes = "获取用户信息")
    @ApiOperationSupport(author = "David", order = 3)
    public Result<UserInfoResDto> userProfile() {
        return Result.ok(userInfoServiceImpl.getProfile());
    }

    @PostMapping(value = "/updateUserProfile")
    @ApiOperation(value = "更新用户信息", notes = "更新用户信息")
    @ApiOperationSupport(author = "David", order = 4)
    public Result<UserInfoResDto> updateProfile(@Valid @RequestBody UpdateProfileReqDto dto) throws ParseException {
        return Result.ok(userInfoServiceImpl.updateProfile(dto));
    }

    @PostMapping(value = "/validatePasswordCoin")
    @ApiOperation(value = "验证资金密码", notes = "验证资金密码")
    @ApiOperationSupport(author = "David", order = 6)
    public Result<ValidateResDto> validatePasswordCoin(@Valid @RequestBody ValidatePasswordCoinReqDto dto) {
        return Result.ok(userInfoServiceImpl.validatePasswordCoin(dto));
    }


    @GetMapping(value = "/userAddress")
    @ApiOperation(value = "获取用户地址", notes = "获取用户地址")
    @ApiOperationSupport(author = "David", order = 7)
    public Result<List<UserAddressResDto>> userAddressByUid() {
        return Result.ok(userInfoServiceImpl.userAddressByUid(ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get().getId()));
    }

    @PostMapping(value = "/setDefaultAddress")
    @ApiOperation(value = "设置默认地址", notes = "设置默认地址")
    @ApiOperationSupport(author = "David", order = 8)
    public Result<List<UserAddressResDto>> setDefaultAddress(@Valid @RequestBody DeleteOrDefaultAddressReqDto dto) {
        return Result.ok(userInfoServiceImpl.updateDefaultAddress(dto));
    }

    @PostMapping(value = "/insertSaveAddress")
    @ApiOperation(value = "更新、添加地址", notes = "更新、添加地址")
    @ApiOperationSupport(author = "David", order = 9)
    public Result<List<UserAddressResDto>> insertSaveAddress(@Valid @RequestBody InsertSaveAddressReqDto dto) {
        return Result.ok(userInfoServiceImpl.insertSaveAddress(dto));
    }

    @PostMapping(value = "/deleteAddress")
    @ApiOperation(value = "删除地址", notes = "删除地址")
    @ApiOperationSupport(author = "David", order = 10)
    public Result<List<UserAddressResDto>> deleteAddress(@Valid @RequestBody DeleteOrDefaultAddressReqDto dto) {
        return Result.ok(userInfoServiceImpl.deleteAddress(dto));
    }

    @PostMapping(value = "/sendSmsCode")
    @ApiOperation(value = "获取验证码", notes = "获取验证码")
    @ApiOperationSupport(author = "David", order = 11)
    @RequestLimit()
    @UnCheckToken
    public Result<String> sendSmsCode(@Valid @RequestBody SendSmsCodeReqDto dto) {
        userInfoServiceImpl.sendSmsCode(dto);
        return Result.ok();
    }

    @PostMapping(value = "/forgotPassword")
    @ApiOperation(value = "忘记密码", notes = "忘记密码")
    @ApiOperationSupport(author = "David", order = 12, ignoreParameters = {"dto.category"})
    @RequestLimit()
    @UnCheckToken
    public Result<String> forgotPassword(@Valid @RequestBody ForgotPasswordReqDto dto) {
        userInfoServiceImpl.forgotPassword(dto);
        return Result.ok();
    }

    @PostMapping(value = "/resetPassword")
    @ApiOperation(value = "重置密码", notes = "重置密码")
    @ApiOperationSupport(author = "David", order = 12)
    @RequestLimit()
    public Result<String> resetPassword(@Valid @RequestBody ResetPasswordReqDto dto) {
        userInfoServiceImpl.resetPassword(dto);
        return Result.ok();
    }

    @PostMapping(value = "/resetMobile")
    @ApiOperation(value = "修改手机号", notes = "修改手机号")
    @ApiOperationSupport(author = "David", order = 13)
    @RequestLimit()
    public Result<String> resetMobile(@Valid @RequestBody ResetMobileReqDto dto) {
        userInfoServiceImpl.resetMobile(dto);
        return Result.ok();
    }

    @PostMapping(value = "/loginByMobile")
    @ApiOperation(value = "登录通过手机号码", notes = "登录通过手机号码")
    @ApiOperationSupport(author = "wells", order = 14)
    @UnCheckToken
    public Result<UserInfoResDto> loginByMobile(@Valid @RequestBody SmsParams.SmsSendDto dto, HttpServletRequest request) {
        return Result.ok(userInfoServiceImpl.loginByMobile(dto, request));
    }
}
