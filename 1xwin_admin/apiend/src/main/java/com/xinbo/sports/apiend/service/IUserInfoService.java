package com.xinbo.sports.apiend.service;

import com.xinbo.sports.apiend.io.bo.UserParams;
import com.xinbo.sports.apiend.io.bo.UserParams.*;
import com.xinbo.sports.apiend.io.dto.mapper.UserInfoResDto;
import com.xinbo.sports.service.io.dto.UserInfo;
import com.xinbo.sports.service.io.dto.api.SmsParams;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.List;

import static com.xinbo.sports.service.io.dto.BaseParams.HeaderInfo;

/**
 * @author: David
 * @date: 06/04/2020
 * @description:
 */
public interface IUserInfoService {
    /**
     * 创建用户
     *
     * @param dto {"username", "password", "sup_uid_1"}
     * @return 用户实体
     */
    UserInfoResDto create(UserParams.CreateReqDto dto, HttpServletRequest request);

    /**
     * 用户登录
     *
     * @param dto {"username", "password"}
     * @return 用户信息
     */
    UserInfoResDto login(UserParams.LoginReqDto dto, HttpServletRequest request);

    /**
     * 用户登出
     */
    void logout();

    /**
     * 获取用户信息
     *
     * @return 用户实体
     */
    UserInfoResDto getProfile();

    /**
     * 根据username获取用户
     *
     * @param username username
     * @return 实体
     */
    UserInfoResDto findIdentityByUsername(String username);

    /**
     * 更新用户信息
     *
     * @param dto 需要更新的字段
     * @return 更新后的实体
     * @throws ParseException 解析异常
     */
    UserInfoResDto updateProfile(UpdateProfileReqDto dto) throws ParseException;

    /**
     * 验证资金密码
     *
     * @param dto 密码明文
     * @return success:1-true 0-false
     * @author: David
     * @date: 21/04/2020
     */
    ValidateResDto validatePasswordCoin(ValidatePasswordCoinReqDto dto);

    /**
     * 验证资金密码 验证登录密码
     *
     * @param dto 密码明文
     * @return sucess:1-true 0-false
     * @author: David
     * @date: 21/04/2020
     */
    ValidateResDto validatePasswordHash(UserParams.ValidatePasswordHashReqDto dto);

    /**
     * 根据UID获取用户的所有收货地址
     *
     * @param uid uid
     * @return 地址集
     * @author: David
     * @date: 21/04/2020
     */
    List<UserAddressResDto> userAddressByUid(Integer uid);

    /**
     * 设置默认收货地址
     *
     * @param dto {"id"}
     * @return 设置后地址集合
     */
    List<UserAddressResDto> updateDefaultAddress(DeleteOrDefaultAddressReqDto dto);

    /**
     * 更新、新增用户地址接口
     *
     * @param dto dto.id:null-新增 其他:更新
     * @return 用户所有游戏地址集合
     * @author: David
     * @date: 21/04/2020
     */
    List<UserAddressResDto> insertSaveAddress(UserParams.InsertSaveAddressReqDto dto);

    /**
     * 删除用户指定的地址
     *
     * @param dto {"id"}
     * @return 地址集
     */
    List<UserAddressResDto> deleteAddress(DeleteOrDefaultAddressReqDto dto);


    /**
     * 根据Token获取用户
     *
     * @return 实体
     */
    UserInfo findIdentityByApiToken();

    /**
     * 根据Token获取用户
     *
     * @return 实体
     */
    UserInfoResDto findIdentityByApiTokenRes();

    /**
     * 根据Header信息
     *
     * @return 实体
     */
    HeaderInfo getHeadLocalData();


    /**
     * 发送短信验证码
     *
     * @param dto areaCode|Mobile
     */
    void sendSmsCode(SendSmsCodeReqDto dto);

    /**
     * 忘记密码
     *
     * @param dto category:1-登录 2-资金
     */
    void forgotPassword(ForgotPasswordReqDto dto);

    /**
     * 重置密码
     *
     * @param dto category:1-登录 2-资金
     */
    void resetPassword(ResetPasswordReqDto dto);

    /**
     * 修改手机号码
     *
     * @param dto 验证码
     */
    void resetMobile(ResetMobileReqDto dto);

    /**
     * 登录通过电话号码
     *
     * @param dto  参数
     * @param request httpRequest
     * @return UserInfoResDto
     */
    UserInfoResDto loginByMobile(SmsParams.SmsSendDto dto, HttpServletRequest request);
}
