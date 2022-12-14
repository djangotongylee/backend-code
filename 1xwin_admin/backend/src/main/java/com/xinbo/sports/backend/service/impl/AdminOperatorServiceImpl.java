package com.xinbo.sports.backend.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinbo.sports.backend.io.bo.AdminParams;
import com.xinbo.sports.backend.io.constant.ConstantData;
import com.xinbo.sports.backend.service.IAdminInfoBase;
import com.xinbo.sports.backend.service.IAdminOperateService;
import com.xinbo.sports.backend.thread.LoginLogTask;
import com.xinbo.sports.dao.generator.po.Admin;
import com.xinbo.sports.dao.generator.po.AuthGroupAccess;
import com.xinbo.sports.dao.generator.service.AdminService;
import com.xinbo.sports.dao.generator.service.AuthGroupAccessService;
import com.xinbo.sports.service.cache.redis.AdminCache;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.service.common.Constant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.dto.BaseParams;
import com.xinbo.sports.service.io.enums.BaseEnum;
import com.xinbo.sports.utils.*;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.collections.map.HashedMap;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.xinbo.sports.service.io.constant.ConstData.INVALID_LOGIN_MAX_TIMES;

/**
 * @Description:
 * @Author: David
 * @Date: 03/06/2020
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AdminOperatorServiceImpl implements IAdminOperateService {
    private static final String THREAD_NAME = "SYNC_LOGIN_LOG";
    private static final String POOL_NAME = "POOL_LOGIN_LOG";
    private static final AtomicInteger NEXT_ID = new AtomicInteger(0);
    private static final ThreadPoolExecutor POOL_EXECUTOR = new ThreadPoolExecutor(
            50,
            50,
            0L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            runnable -> new Thread(runnable, String.format("%s-%s%d", POOL_NAME, THREAD_NAME, NEXT_ID.getAndDecrement()))
    );
    private final AuthManagerServiceImpl authManagerServiceImpl;
    private final LoginLogTask loginLogTask;

    private final JwtUtils jwtUtils;
    private final AdminService adminServiceImpl;
    private final ConfigCache configCache;
    private final AdminCache adminCache;
    private final AuthGroupAccessService authGroupAccessServiceImpl;

    @Override
    public AdminParams.LoginResponse login(AdminParams.LoginRequest dto, HttpServletRequest request) {
        Long times = adminCache.modifyInvalidLoginTimes(dto.getUsername(), 0L);
        if (times >= INVALID_LOGIN_MAX_TIMES) {
            throw new BusinessException(CodeInfo.LOGIN_INVALID_OVER_LIMIT);
        }
        Admin admin = findIdentityByUsername(dto.getUsername());
        if (admin == null) {
            throw new BusinessException(CodeInfo.ACCOUNT_NOT_EXISTS);
        }
        //?????????????????????
        var codeFlag = Constant.VERIFICATION_SHOW.equals(configCache.getConfigByTitle(Constant.VERIFICATION_OF_GOOGLE));
        //???????????????
        if (codeFlag && !GoogleAuthenticator.checkVerificationCode(admin.getSecret(), dto.getVerificationCode())) {
            throw new BusinessException(CodeInfo.VERIFICATION_CODE_INVALID);
        }
        if (Boolean.FALSE.equals(PasswordUtils.validatePasswordHash(dto.getPassword(), admin.getPasswordHash()))) {
            adminCache.modifyInvalidLoginTimes(dto.getUsername(), 1L);
            throw new BusinessException(CodeInfo.PASSWORD_INVALID);
        }
        String userAgent = request.getHeader("User-Agent");
        String ip = IpUtil.getIp(request);
        String device = request.getHeader("Accept-Device");
        if (admin.getAdminGroupId() != 0) {
            POOL_EXECUTOR.submit(() -> loginLogTask.run(admin.getId(), ip, userAgent, device));
        }
        String jwtToken = genJwtToken(admin.getId(), admin.getUsername());
        // ??????Redis??????
        adminCache.setUserToken(admin.getId(), jwtToken);
        adminCache.delInvalidLoginTimes(dto.getUsername());
        AdminParams.LoginResponse loginResponse = BeanConvertUtils.copyProperties(admin, AdminParams.LoginResponse::new);
        loginResponse.setApiToken(jwtToken);
        //??????????????????
        var authGroupAccess = authGroupAccessServiceImpl.getOne(new LambdaQueryWrapper<AuthGroupAccess>()
                .eq(AuthGroupAccess::getUid, admin.getId()), false);
        var role = Objects.nonNull(authGroupAccess) ? authGroupAccess.getGroupId() : 0;
        loginResponse.setRole(role);
        return loginResponse;
    }

    @Override
    public AdminParams.LoginResponse profile() {
        Integer id = IAdminInfoBase.getHeadLocalData().getId();
        Admin admin = findIdentity(id);
        return BeanConvertUtils.copyProperties(admin, AdminParams.LoginResponse::new);
    }

    /**
     * ??????????????????????????????
     *
     * @return
     */
    public JSONObject refreshGetRule() {
        var uid = IAdminInfoBase.getHeadLocalData().getId();
        //????????????
        return authManagerServiceImpl.loadAuthRule(new HashedMap() {{
            put("uid", uid);
        }});
    }

    /**
     * ??????UID?????????????????????
     *
     * @param id uid
     * @return ???????????????
     */
    public Admin findIdentity(Integer id) {
        Admin admin = adminServiceImpl.getById(id);
        if (null == admin) {
            throw new BusinessException(CodeInfo.ACCOUNT_NOT_EXISTS);
        } else if (admin.getStatus() != ConstantData.STATUS_VALID_CODE) {
            throw new BusinessException(CodeInfo.ACCOUNT_STATUS_INVALID);
        }

        return admin;
    }

    /**
     * ?????????????????????????????????
     *
     * @param username ?????????
     * @return ????????????
     */
    public Admin findIdentityByUsername(String username) {
        Admin admin = adminServiceImpl.lambdaQuery().eq(Admin::getUsername, username).one();
        if (null == admin) {
            throw new BusinessException(CodeInfo.ACCOUNT_NOT_EXISTS);
        } else if (admin.getStatus() != ConstantData.STATUS_VALID_CODE) {
            throw new BusinessException(CodeInfo.ACCOUNT_STATUS_INVALID);
        }
        return admin;
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
        map.put("webRole", "admin");
        BaseEnum.JWT jwtProp = configCache.getJwtProp();
        jwtUtils.init(jwtProp.getSecret(), jwtProp.getExpired());
        return jwtUtils.generateToken(map);
    }

    /**
     * ????????????
     */
    @Override
    public void logout() {
        BaseParams.HeaderInfo headLocalDate = IAdminInfoBase.getHeadLocalData();
        // ??????Token?????????????????????
        adminCache.delUserToken(headLocalDate.getId());
        adminCache.delInvalidLoginTimes(headLocalDate.getUsername());
    }

    private void checkVerificationCode(Admin admin, String verificationCode) {
        Base32 base32 = new Base32();
        byte[] bytes = base32.decode(admin.getSecret());
        String hexKey = Hex.encodeHexString(bytes);
        long time = (System.currentTimeMillis() / 1000) / 30;
        String hexTime = Long.toHexString(time);
        GoogleAuthenticator.generateTOTP(hexKey, hexTime, "6");
    }
}
