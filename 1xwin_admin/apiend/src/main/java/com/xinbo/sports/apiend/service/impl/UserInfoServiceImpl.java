package com.xinbo.sports.apiend.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinbo.sports.apiend.base.GameCommonBase;
import com.xinbo.sports.apiend.cache.redis.GameCache;
import com.xinbo.sports.apiend.io.bo.UserParams.*;
import com.xinbo.sports.apiend.io.dto.mapper.UserInfoResDto;
import com.xinbo.sports.apiend.io.dto.platform.CoinTransferReqDto;
import com.xinbo.sports.apiend.io.dto.platform.GameBalanceReqDto;
import com.xinbo.sports.apiend.mapper.UserInfoMapper;
import com.xinbo.sports.apiend.service.IGameService;
import com.xinbo.sports.apiend.service.IUserInfoService;
import com.xinbo.sports.apiend.service.transactional.UserTrans;
import com.xinbo.sports.dao.generator.mapper.UserAddressMapper;
import com.xinbo.sports.dao.generator.po.User;
import com.xinbo.sports.dao.generator.po.UserAddress;
import com.xinbo.sports.dao.generator.po.UserProfile;
import com.xinbo.sports.dao.generator.service.UserAddressService;
import com.xinbo.sports.dao.generator.service.UserProfileService;
import com.xinbo.sports.dao.generator.service.UserService;
import com.xinbo.sports.service.base.RegisterBonusPromotions;
import com.xinbo.sports.service.base.UserServiceBase;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.service.cache.redis.UserCache;
import com.xinbo.sports.service.common.Constant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.constant.ConstData;
import com.xinbo.sports.service.io.dto.UserInfo;
import com.xinbo.sports.service.io.enums.BaseEnum;
import com.xinbo.sports.service.thread.ThreadHeaderLocalData;
import com.xinbo.sports.utils.*;
import com.xinbo.sports.utils.components.response.CodeInfo;
import com.xinbo.sports.utils.components.sms.MobileRegularExp;
import com.xinbo.sports.utils.components.sms.changlan.Sms253Util;
import com.xinbo.sports.utils.components.sms.changlan.SmsEnum;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.xinbo.sports.service.io.constant.ConstData.INVALID_LOGIN_MAX_TIMES;
import static com.xinbo.sports.service.io.dto.BaseParams.HeaderInfo;
import static com.xinbo.sports.service.io.dto.api.SmsParams.SmsSendDto;

/**
 * @author: David
 * @date: 06/04/2020
 * @description:
 */
@Service
@Slf4j(topic = "UserInfoServiceImplxxxxxxx")
public class UserInfoServiceImpl implements IUserInfoService {
    public static final int STATUS_ACTIVE = 10;
    public static final String VERIFY = "1";
    public static final String CODE = "$Code";
    public static final String PLAT = "plat";
    public static final String SIGNATURE = "{$Signature}";
    public static final String VAR = "{#var#}";
    @Resource
    UserProfileService userProfileServiceImpl;
    @Resource
    UserService userServiceImpl;
    @Resource
    UserAddressService userAddressServiceImpl;
    @Resource
    UserAddressMapper userAddressMapper;
    @Resource
    UserInfoMapper userInfoMapper;
    @Resource
    UserTrans userTrans;
    @Resource
    JwtUtils jwtUtils;
    @Resource
    UserCache userCache;
    @Resource
    ConfigCache configCache;
    @Resource
    GameCommonBase gameCommonBase;
    @Resource
    UserServiceBase userServiceBase;
    Random random = new Random();
    @Resource
    private IGameService gameServiceImpl;
    @Resource
    private GameCache gameCache;
    @Resource
    private UserInfoServiceImpl userInfoServiceImpl;
    @Resource
    private RegisterBonusPromotions registerBonusPromotions;

    /**
     * ????????????jwtUtils
     *
     * @param dto {"username", "password", "sup_uid_1"}
     * @return ????????????
     */
    @Override
    public UserInfoResDto create(@NotNull CreateReqDto dto, HttpServletRequest request) {
        var registerStr = configCache.getConfigByTitle(Constant.REGISTER_LOGIN_CONFIG);
        var registerJson = parseObject(registerStr);
        var verificationCode = registerJson.getInteger(Constant.REGISTER_VERIFICATION_CODE);
        if (Constant.IS_CHECK_CODE.equals(verificationCode)) {
            if (dto.getSmsCode() != null && dto.getSmsCode().toString().length() >= 4 && dto.getSmsCode().toString().length() <= 8) {
                // ?????????????????????????????????
                SmsSendDto build = SmsSendDto.builder().areaCode(dto.getAreaCode()).mobile(dto.getMobile()).code(dto.getSmsCode()).build();
                userCache.validSmsSendCache(build);
            } else {
                throw new BusinessException(CodeInfo.SMS_INVALID);
            }
        }
        // ??????????????????
        if (null != dto) {
            String host = request.getHeader("Host");
            log.info("======> host =====>{}", host);
            dto.setLink(host);
        }
        //???????????????
        if (Objects.nonNull(dto.getPromoCode()) && dto.getPromoCode().toString().length() < 4 && dto.getPromoCode().toString().length() > 8) {
            throw new BusinessException(CodeInfo.PROMO_CODE_INVALID);
        }

        // ??????????????????????????????
        User user = userServiceImpl.lambdaQuery().eq(User::getUsername, dto.getUsername()).one();
        if (user != null) {
            throw new BusinessException(CodeInfo.ACCOUNT_EXISTS);
        }

        if (Objects.nonNull(dto.getAreaCode()) && Objects.nonNull(dto.getMobile())) {
            // ???????????????????????????
            UserProfile checkMobile = userProfileServiceImpl.lambdaQuery()
                    .eq(UserProfile::getAreaCode, dto.getAreaCode())
                    .eq(UserProfile::getMobile, dto.getMobile())
                    .one();
            if (checkMobile != null) {
                throw new BusinessException(CodeInfo.MOBILE_EXISTS);
            }
        }

        // ???????????????
        userTrans.insertUser(dto);
        UserInfoResDto userInfoResDto = findIdentityByUsername(dto.getUsername());

        // ??????JWT
        String jwtToken = genJwtToken(userInfoResDto.getId(), userInfoResDto.getUsername());
        userInfoResDto.setApiToken(jwtToken);
        userCache.setUserToken(userInfoResDto.getId(), jwtToken);

        //????????????????????????
        registerBonusPromotions.userRegisterAutoPromotions(userInfoResDto.getId());
        //??????????????????????????????
        gameCommonBase.createThirdUser(userServiceBase.buildUsername(dto.getUsername()).toLowerCase());
        return userInfoResDto;
    }


    /**
     * ????????????
     *
     * @param dto {"username", "password"}
     * @return ????????????
     */
    @Override
    public UserInfoResDto login(@NotNull LoginReqDto dto, HttpServletRequest request) {
        Long times = userCache.modifyInvalidLoginTimes(dto.getUsername(), 0L);
        if (times >= INVALID_LOGIN_MAX_TIMES) {
            throw new BusinessException(CodeInfo.LOGIN_INVALID_OVER_LIMIT);
        }

        UserInfoResDto userInfo = findIdentityByUsername(dto.getUsername());
        if (userInfo == null) {
            throw new BusinessException(CodeInfo.ACCOUNT_NOT_EXISTS);
        }

        if (Boolean.FALSE.equals(PasswordUtils.validatePasswordHash(dto.getPassword(), userInfo.getPasswordHash()))) {
            userCache.modifyInvalidLoginTimes(dto.getUsername(), 1L);
            throw new BusinessException(CodeInfo.PASSWORD_INVALID);
        }

        String jwtToken = genJwtToken(userInfo.getId(), userInfo.getUsername());
        userInfo.setApiToken(jwtToken);

        // ??????Redis??????
        userCache.setUserToken(userInfo.getId(), jwtToken);
        userCache.delInvalidLoginTimes(dto.getUsername());

        User user = new User();
        user.setUpdatedAt(DateNewUtils.now());
        user.setId(userInfo.getId());
        userServiceImpl.updateById(user);
        return userInfo;
    }

    /**
     * ????????????
     */
    @Override
    public void logout() {
        HeaderInfo headLocalDate = getHeadLocalData();
        // ??????Token?????????????????????
        userCache.delUserToken(headLocalDate.getId());
        userCache.delInvalidLoginTimes(headLocalDate.getUsername());
    }

    /**
     * ??????????????????
     *
     * @return ????????????
     */
    @SneakyThrows
    @Override
    public UserInfoResDto getProfile() {
        HeaderInfo headLocalDate = getHeadLocalData();
        var userInfo = userInfoMapper.getUserInfoById(headLocalDate.getId());
        if (Constant.BWG05.equals(configCache.getConfigByTitle(Constant.PLAT_PREFIX))) {
            //bwg05?????????????????????
            var platId = gameCache.getGameListCache(Constant.FUTURES_LOTTERY).getPlatListId();
            var dto = new GameBalanceReqDto();
            dto.setId(platId);
            var platCoin = gameServiceImpl.queryBalanceByPlatId(dto).getCoin();
            if (platCoin.compareTo(BigDecimal.ZERO) > 0) {
                userInfo.setCoin(userInfo.getCoin().add(platCoin));
                var transferReqDto = new CoinTransferReqDto();
                transferReqDto.setId(dto.getId());
                transferReqDto.setDirection(1);
                transferReqDto.setCoin(platCoin);
                transferReqDto.setName(headLocalDate.getUsername());
                gameServiceImpl.coinTransfer(transferReqDto);
            }
        }
        return userInfo;
    }

    /**
     * ??????username????????????
     *
     * @param username username
     * @return ??????
     */
    @Override
    public UserInfoResDto findIdentityByUsername(String username) {
        UserInfoResDto userInfoResDto = userInfoMapper.getUserInfoByUsername(username);
        if (userInfoResDto == null) {
            throw new BusinessException(CodeInfo.ACCOUNT_NOT_EXISTS);
        }
        if (userInfoResDto.getStatus() != STATUS_ACTIVE) {
            throw new BusinessException(CodeInfo.ACCOUNT_STATUS_INVALID);
        }
        return userInfoResDto;
    }

    /**
     * ??????????????????
     *
     * @param dto ?????????????????????
     * @return ??????????????????
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoResDto updateProfile(UpdateProfileReqDto dto) {
        HeaderInfo headLocalDate = getHeadLocalData();

        if (StringUtils.isNotBlank(dto.getAvatar())) {
            User user = userServiceImpl.getById(headLocalDate.getId());
            user.setAvatar(dto.getAvatar());
            userServiceImpl.updateById(user);
            dto.setAvatar(null);
            // ????????????
            userCache.updateUserInfoById(user.getId());
            userCache.updateUserFlag(user.getId());
        }

        JSONObject json = (JSONObject) JSON.toJSON(dto);
        long size = json.entrySet().stream().filter(k -> k.getValue() != null).count();
        if (size != 0) {
            UserProfile userProfile = userProfileServiceImpl.getById(headLocalDate.getId());
            BeanConvertUtils.beanCopy(dto, userProfile);
            userProfileServiceImpl.updateById(userProfile);
        }

        return findIdentityByApiTokenRes();
    }

    /**
     * ??????????????????
     *
     * @param dto ????????????
     * @return success:1-true 0-false
     * @author: David
     * @date: 21/04/2020
     */
    @Override
    public ValidateResDto validatePasswordCoin(ValidatePasswordCoinReqDto dto) {
        UserInfo userInfo = findIdentityByApiToken();
        if (StringUtils.isBlank(userInfo.getPasswordCoin())) {
            throw new BusinessException(CodeInfo.PASSWORD_EMPTY);
        }

        if (Boolean.FALSE.equals(PasswordUtils.validatePasswordHash(dto.getPasswordCoin(), userInfo.getPasswordCoin()))) {
            throw new BusinessException(CodeInfo.PASSWORD_INVALID);
        }
        return ValidateResDto.builder().success(1).build();
    }

    /**
     * ?????????????????? ??????????????????
     *
     * @param dto ????????????
     * @return success:1-true 0-false
     * @author: David
     * @date: 21/04/2020
     */
    @Override
    public ValidateResDto validatePasswordHash(ValidatePasswordHashReqDto dto) {
        UserInfo userInfo = findIdentityByApiToken();
        if (StringUtils.isBlank(userInfo.getPasswordHash())) {
            throw new BusinessException(CodeInfo.PASSWORD_EMPTY);
        }
        if (Boolean.FALSE.equals(PasswordUtils.validatePasswordHash(dto.getPasswordHash(), userInfo.getPasswordHash()))) {
            throw new BusinessException(CodeInfo.PASSWORD_INVALID);
        }

        return ValidateResDto.builder().success(1).build();
    }

    /**
     * ??????UID?????????????????????????????????
     *
     * @param uid uid
     * @return ?????????
     * @author: David
     * @date: 21/04/2020
     */
    @Override
    public List<UserAddressResDto> userAddressByUid(Integer uid) {
        List<UserAddress> lUserAddress = userAddressServiceImpl.lambdaQuery()
                .eq(UserAddress::getUid, uid)
                .list();

        return BeanConvertUtils.copyListProperties(lUserAddress, UserAddressResDto::new);
    }

    /**
     * ????????????????????????
     *
     * @param dto {"id"}
     * @return ?????????????????????
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<UserAddressResDto> updateDefaultAddress(DeleteOrDefaultAddressReqDto dto) {
        HeaderInfo headLocalDate = getHeadLocalData();
        UserAddress address = userAddressServiceImpl.getById(dto.getId());
        if (!address.getUid().equals(headLocalDate.getId())) {
            throw new BusinessException(CodeInfo.ADDRESS_INVALID);
        }

        Integer time = DateUtils.getCurrentTime();
        userAddressServiceImpl.lambdaUpdate()
                .set(UserAddress::getStatus, 2)
                .set(UserAddress::getUpdatedAt, time)
                .eq(UserAddress::getUid, headLocalDate.getId())
                .update();
        address.setStatus(1);
        address.setUpdatedAt(time);
        userAddressServiceImpl.updateById(address);

        return userAddressByUid(headLocalDate.getId());
    }

    /**
     * ?????????????????????????????????
     *
     * @param dto dto.id:null-?????? ??????:??????
     * @return ??????????????????????????????
     * @author: David
     * @date: 21/04/2020
     */
    @Override
    public List<UserAddressResDto> insertSaveAddress(InsertSaveAddressReqDto dto) {
        HeaderInfo headLocalDate = getHeadLocalData();
        // ?????????????????????????????????
        if (dto.getId() != null) {
            UserAddress userAddress = userAddressServiceImpl.getById(dto.getId());
            if (userAddress == null || !userAddress.getUid().equals(headLocalDate.getId())) {
                throw new BusinessException(CodeInfo.ADDRESS_INVALID);
            }

            userAddress.setAreaCode(dto.getAreaCode());
            userAddress.setMobile(dto.getMobile());
            userAddress.setName(dto.getName());
            userAddress.setAddress(dto.getAddress());
            userAddress.setUpdatedAt(DateUtils.getCurrentTime());
            userAddressServiceImpl.updateById(userAddress);
        } else {
            Integer count = userAddressServiceImpl.lambdaQuery()
                    .eq(UserAddress::getUid, headLocalDate.getId())
                    .in(UserAddress::getStatus, 1, 2)
                    .count();

            if (count >= ConstData.USER_MAX_BIND_ADDRESS) {
                // ?????????????????????????????????
                throw new BusinessException(CodeInfo.ADDRESS_MAX_LIMIT);
            }

            UserAddress userAddress = BeanConvertUtils.beanCopy(dto, UserAddress::new);
            // ???????????????????????????????????????
            userAddress.setUid(headLocalDate.getId());
            userAddress.setStatus(count == 0 ? 1 : 2);
            Integer time = DateUtils.getCurrentTime();
            userAddress.setCreatedAt(time);
            userAddress.setUpdatedAt(time);
            userAddress.setUsername(headLocalDate.getUsername());
            userAddressServiceImpl.save(userAddress);
        }

        return userAddressByUid(headLocalDate.getId());
    }

    /**
     * ???????????????????????????
     *
     * @param dto {"id"}
     * @return ?????????
     */
    @Override
    public List<UserAddressResDto> deleteAddress(DeleteOrDefaultAddressReqDto dto) {
        HeaderInfo headLocalDate = getHeadLocalData();

        UserAddress address = userAddressServiceImpl.getById(dto.getId());
        if (!address.getUid().equals(headLocalDate.getId())) {
            throw new BusinessException(CodeInfo.ADDRESS_INVALID);
        }
        userAddressMapper.deleteById(dto.getId());

        return userAddressByUid(headLocalDate.getId());
    }

    /**
     * ??????Token????????????
     *
     * @return ??????
     */
    @Override
    public UserInfo findIdentityByApiToken() {
        HeaderInfo headerInfo = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        UserInfoResDto userInfoResDto = userInfoMapper.getUserInfoById(headerInfo.getId());
        return BeanConvertUtils.beanCopy(userInfoResDto, UserInfo::new);
    }

    /**
     * ??????Token????????????
     *
     * @return ??????
     */
    @Override
    public UserInfoResDto findIdentityByApiTokenRes() {
        HeaderInfo headerInfo = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();

        return userInfoMapper.getUserInfoById(headerInfo.getId());
    }

    /**
     * ??????Header??????
     *
     * @return ??????
     */
    @Override
    public HeaderInfo getHeadLocalData() {
        return ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
    }

    /**
     * ?????????????????????
     */
    @Override
    public void sendSmsCode(SendSmsCodeReqDto dto) {
        // ????????????????????????????????????
        if (!MobileRegularExp.isMobileNumber(dto.getAreaCode(), dto.getMobile())) {
            throw new BusinessException(CodeInfo.SMS_INVALID_NUMBER);
        }

        HeaderInfo headLocalDate = getHeadLocalData();
        int verifyCode = random.nextInt(9999 - 1000 + 1) + 1000;
        long now = Instant.now().toEpochMilli();
        // ???????????????????????????
        SmsSendDto smsSendDto = SmsSendDto.builder()
                .areaCode(dto.getAreaCode())
                .mobile(dto.getMobile())
                .code((long) verifyCode)
                .createAt(now)
                .build();
        userCache.setSmsSendCache(smsSendDto);

        JSONObject smsProp = configCache.getSmsProp();

        // ????????????
        String url;
        String format;
        String requestJson;
        String code;
        String result = "";
        var mobile = dto.getAreaCode() + dto.getMobile();
        var msg = SmsEnum.MESSAGES.EN.getValue().replace(CODE, Integer.toString(verifyCode));
        if (smsProp.getString(PLAT).equals(BaseEnum.MessagePlatform.SMSTH.getCode())) {
            BaseEnum.PROP253 prop253 = smsProp.toJavaObject(BaseEnum.PROP253.class);
            url = prop253.getUrl();
            msg = msg.replace(SIGNATURE, prop253.getSignature());
            requestJson = "{\"account\":\"%s\",\"password\":\"%s\",\"msg\":\"%s\",\"mobile\":\"%s\"}";
            format = String.format(requestJson, prop253.getAccount(), prop253.getPassword(), msg, mobile);
            code = prop253.getCode();
            result = Sms253Util.sendSmsByPost(url, format);
        } else if (smsProp.getString(PLAT).equals(BaseEnum.MessagePlatform.SMSIN.getCode())) {
            BaseEnum.PROPSkyLine skyLinePROP = smsProp.toJavaObject(BaseEnum.PROPSkyLine.class);
            //msg = msg.replace(SIGNATURE, skyLinePROP.getSignature());
            msg="(Sweta Dubey)Your verification code is {#var#}. The captcha is valid within 5 minutes.";
            msg = msg.replace(VAR, Integer.toString(verifyCode));
            requestJson = "{\"content\":\"%s\",\"numbers\":\"%s\",\"senderid\":\"%s\"}";
            var datetime = DateNewUtils.getNowFormatTime(DateNewUtils.Format.yyyyMMddHHmmss);
            var sign = MD5.encryption(skyLinePROP.getAccount() + skyLinePROP.getPassword() + datetime);
            url = skyLinePROP.getUrl() + "?" + "account=" + skyLinePROP.getAccount() + "&sign=" + sign + "&datetime=" + datetime;
            format = String.format(requestJson, msg, mobile, skyLinePROP.getSenderId());
            code = skyLinePROP.getCode();
            result = Sms253Util.sendSmsByPost(url, format);
        } else if (smsProp.getString(PLAT).equals(BaseEnum.MessagePlatform.SMSNX.getCode())) {
            BaseEnum.PROP_NX_CLOUD prop_nx_cloud = smsProp.toJavaObject(BaseEnum.PROP_NX_CLOUD.class);
            url = prop_nx_cloud.getUrl();
            msg = msg.replace(SIGNATURE, prop_nx_cloud.getSignature());
            code = prop_nx_cloud.getCode();
            var params = "appkey=" + prop_nx_cloud.getAppKey() + "&secretkey=" + prop_nx_cloud.getSecretKey() + "&phone=" + mobile + "&content=" + msg;
            result = HttpUtils.postHttp(url, params);
        } else if (smsProp.getString(PLAT).equals(BaseEnum.MessagePlatform.SMSNODE.getCode())) {
            BaseEnum.PROPNODE prop_node = smsProp.toJavaObject(BaseEnum.PROPNODE.class);
            url = prop_node.getUrl();
            msg = msg.replace(SIGNATURE, prop_node.getSignature());
            code = prop_node.getCode();
            Map<String, String> map = Map.of("account", prop_node.getUsername(), "password", prop_node.getApi_password(), "content", msg, "numbers", mobile);
            try {
                result = HttpUtils.send(url, map, new HashMap<>());
            } catch (Exception e) {
                log.info(e.getMessage());
            }
        } else {
            throw new BusinessException(CodeInfo.PLAT_SYSTEM_ERROR);
        }
        JSONObject response = parseObject(result);
        // ???????????????????????????
        if (!SmsEnum.STATUS.SUCCESS.getValue().equals(response.getString(code))) {
            log.error("??????????????????:" + response);
            throw new BusinessException(CodeInfo.SMS_SENT_ERROR);
        }
    }


    /**
     * ????????????
     *
     * @param dto category:1-?????? 2-??????
     */
    @Override
    public void forgotPassword(ForgotPasswordReqDto dto) {
        //  ???????????????????????????
        userCache.validSmsSendCache(SmsSendDto.builder()
                .areaCode(dto.getAreaCode())
                .mobile(dto.getMobile())
                .code(dto.getSmsCode())
                .build());

        // ??????????????????
        UserProfile one = userProfileServiceImpl.lambdaQuery()
                .eq(UserProfile::getAreaCode, dto.getAreaCode())
                .eq(UserProfile::getMobile, dto.getMobile())
                .one();
        if (one == null) {
            throw new BusinessException(CodeInfo.MOBILE_INVALID);
        }

        one.setPasswordHash(PasswordUtils.generatePasswordHash(dto.getPassword()));
        one.setUpdatedAt((int) Instant.now().getEpochSecond());
        // ??????????????????
        userProfileServiceImpl.updateById(one);
    }

    /**
     * ????????????
     *
     * @param dto category:1-?????? 2-??????
     */
    @Override
    public void resetPassword(@NotNull ResetPasswordReqDto dto) {
        HeaderInfo headerInfo = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        // ??????????????????????????????
        UserProfile userProfile = userProfileServiceImpl.getById(headerInfo.getId());
        if (StringUtils.isEmpty(userProfile.getAreaCode()) || StringUtils.isEmpty(userProfile.getMobile())) {
            throw new BusinessException(CodeInfo.BINDING_MOBILE_NUMBER);
        }

        if (!userProfile.getAreaCode().equals(dto.getAreaCode()) || !userProfile.getMobile().equals(dto.getMobile())) {
            throw new BusinessException(CodeInfo.MOBILE_INVALID);
        }

        userCache.validSmsSendCache(SmsSendDto.builder()
                .areaCode(dto.getAreaCode())
                .mobile(dto.getMobile())
                .code(dto.getSmsCode())
                .build());

        if (dto.getCategory() == 1) {
            userProfileServiceImpl.lambdaUpdate()
                    .eq(UserProfile::getAreaCode, dto.getAreaCode())
                    .eq(UserProfile::getMobile, dto.getMobile())
                    .set(UserProfile::getPasswordHash, PasswordUtils.generatePasswordHash(dto.getPassword()))
                    .update();
        } else {
            userProfileServiceImpl.lambdaUpdate()
                    .eq(UserProfile::getAreaCode, dto.getAreaCode())
                    .eq(UserProfile::getMobile, dto.getMobile())
                    .set(UserProfile::getPasswordCoin, PasswordUtils.generatePasswordHash(dto.getPassword()))
                    .update();
        }
    }

    /**
     * ??????????????????
     */
    @Override
    public void resetMobile(@NotNull ResetMobileReqDto dto) {
        // ???????????????????????????
        UserProfile checkMobile = userProfileServiceImpl.lambdaQuery()
                .eq(UserProfile::getAreaCode, dto.getAreaCode())
                .eq(UserProfile::getMobile, dto.getMobile())
                .one();
        if (checkMobile != null) {
            throw new BusinessException(CodeInfo.MOBILE_EXISTS);
        }

        userCache.validSmsSendCache(SmsSendDto.builder()
                .areaCode(dto.getAreaCode())
                .mobile(dto.getMobile())
                .code(dto.getSmsCode())
                .build());

        // ??????????????????
        HeaderInfo headerInfo = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        userProfileServiceImpl.lambdaUpdate()
                .eq(UserProfile::getUid, headerInfo.getId())
                .set(UserProfile::getAreaCode, dto.getAreaCode())
                .set(UserProfile::getMobile, dto.getMobile())
                .update();
    }

    /**
     * ????????????????????????
     *
     * @param dto     ??????
     * @param request httpRequest
     * @return UserInfoResDto
     */
    @Override
    public UserInfoResDto loginByMobile(SmsSendDto dto, HttpServletRequest request) {
        //??????????????????????????????
        var count = userProfileServiceImpl.lambdaQuery()
                .eq(UserProfile::getAreaCode, dto.getAreaCode())
                .eq(UserProfile::getMobile, dto.getMobile())
                .count();
        //???????????????????????????????????????
        if (count != 1) {
            throw new BusinessException(CodeInfo.MOBILE_INVALID);
        }
        // ?????????????????????????????????
        SmsSendDto build = SmsSendDto.builder().areaCode(dto.getAreaCode()).mobile(dto.getMobile()).code(dto.getCode()).build();
        userCache.validSmsSendCache(build);
        var userprofile = userProfileServiceImpl.getOne(new LambdaQueryWrapper<UserProfile>()
                        .eq(UserProfile::getAreaCode, dto.getAreaCode())
                        .eq(UserProfile::getMobile, dto.getMobile())
                , false);
        var loginUser = userServiceImpl.getById(userprofile.getUid());
        UserInfoResDto userInfo = findIdentityByUsername(loginUser.getUsername());
        String jwtToken = genJwtToken(loginUser.getId(), loginUser.getUsername());
        userInfo.setApiToken(jwtToken);
        // ??????Redis??????
        userCache.setUserToken(userInfo.getId(), jwtToken);
        userCache.delInvalidLoginTimes(loginUser.getUsername());
        return userInfo;
    }

    /**
     * ?????? uid, username ??????JWT token
     *
     * @param uid      uid
     * @param username username
     * @return token
     */
    public String genJwtToken(@NotNull Integer uid, String username) {
        // ??????API Token
        Map<String, String> map = new HashMap<>(2);
        map.put("id", uid.toString());
        map.put("username", username);
        map.put("webRole", "web");
        BaseEnum.JWT jwtProp = configCache.getJwtProp();
        jwtUtils.init(jwtProp.getSecret(), jwtProp.getExpired());
        return jwtUtils.generateToken(map);
    }
}
