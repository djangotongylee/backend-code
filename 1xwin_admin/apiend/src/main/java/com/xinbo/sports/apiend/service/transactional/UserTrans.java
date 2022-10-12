package com.xinbo.sports.apiend.service.transactional;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.xinbo.sports.apiend.io.bo.UserParams.CreateReqDto;
import com.xinbo.sports.dao.generator.po.AgentLinks;
import com.xinbo.sports.dao.generator.po.User;
import com.xinbo.sports.dao.generator.po.UserProfile;
import com.xinbo.sports.dao.generator.service.AgentLinksService;
import com.xinbo.sports.dao.generator.service.UserProfileService;
import com.xinbo.sports.dao.generator.service.UserService;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.service.cache.redis.UserCache;
import com.xinbo.sports.service.common.Constant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.PasswordUtils;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Random;

import static com.alibaba.fastjson.JSON.parseObject;

/**
 * @author: David
 * @date: 15/06/2020
 */
@Service
@Slf4j
public class UserTrans {
    @Resource
    private UserService userServiceImpl;
    @Resource
    private UserProfileService userProfileServiceImpl;
    @Resource
    private UserCache userCache;
    @Resource
    private ConfigCache configCache;
    @Resource
    private AgentLinksService agentLinksServiceImpl;

    Random random = new Random();

    public static final String VERIFY = "1";

    /**
     * 插入User数据
     *
     * @param dto dto
     */
    @Transactional(rollbackFor = Exception.class)
    public void insertUser(@NotNull CreateReqDto dto) {
        UserProfile inviteUser = null;
        // 1.通过域名查询专属域名记录，查找上级记录,作为当前注册账号的直属上级
        Integer uid = getAgentLinksUidByLink(dto.getLink());
        if (null != uid) {
            inviteUser = userProfileServiceImpl.lambdaQuery()
                    .eq(UserProfile::getUid, uid)
                    .one();
        }

        // 2.通过输入推广码开关 和 用户输入推广码查找上级记录,作为当前注册账号的直属上级
        var registerStr = configCache.getConfigByTitle(Constant.REGISTER_LOGIN_CONFIG);
        var registerJson = parseObject(registerStr);
        var inviteCode = registerJson.getInteger(Constant.REGISTER_INVITE_CODE);
        if (null == inviteUser) {
            if (Constant.IS_CHECK_REQUIRED.equals(inviteCode) && null == dto.getPromoCode()) {
                throw new BusinessException(CodeInfo.PROMO_CODE_INVALID);
            }
            if (Objects.nonNull(dto.getPromoCode()) && (Constant.IS_CHECK_CODE.equals(inviteCode) || Constant.IS_CHECK_REQUIRED.equals(inviteCode))) {
                var count = userProfileServiceImpl.lambdaQuery()
                        .eq(Objects.nonNull(dto.getPromoCode()), UserProfile::getPromoCode, dto.getPromoCode())
                        .count();
                if (count == 0) {
                    throw new BusinessException(CodeInfo.PROMO_CODE_INVALID);
                }
            }
        }


        // 3.用户输入推广码不为空,按推广码查找直属上级
        if (null == inviteUser && null != dto.getPromoCode()) {
            inviteUser = userProfileServiceImpl.lambdaQuery()
                    .eq(UserProfile::getPromoCode, dto.getPromoCode())
                    .one();
            if (null == inviteUser) {
                throw new BusinessException(CodeInfo.PROMO_CODE_INVALID);
            }
        }

        // 插入User表数据
        int time = DateNewUtils.now();
        User userPo = new User();
        userPo.setUsername(dto.getUsername().toLowerCase());
        int index = random.nextInt(9) + 1;
        userPo.setAvatar(String.format("/avatar/%s.png", index));
        userPo.setCreatedAt(time);
        userPo.setUpdatedAt(time);
        // 1001平台注册默认送8元
        if (Constant.BWG01.equals(configCache.getConfigByTitle(Constant.PLAT_PREFIX))) {
            userPo.setCoin(new BigDecimal(8));
        }
        userServiceImpl.save(userPo);

        // 插入UserProfile数据
        UserProfile userProfile = new UserProfile();
        userProfile.setUid(userPo.getId());
        userProfile.setPasswordHash(PasswordUtils.generatePasswordHash(dto.getPassword()));
        userProfile.setAreaCode(dto.getAreaCode());
        userProfile.setMobile(dto.getMobile());
        int promoCode = random.nextInt(9999999 - 100000 + 1) + 100000;
        while (userProfileServiceImpl.lambdaQuery().eq(UserProfile::getPromoCode, promoCode).one() != null) {
            promoCode = random.nextInt(9999999 - 100000 + 1) + 100000;
            if (++index > 5) {
                throw new BusinessException(CodeInfo.PROMO_CODE_GEN_INVALID);
            }
        }
        userProfile.setPromoCode(promoCode);
        if (null != inviteUser) {
            // 设置上级层级关系
            userProfile.setSupUid1(inviteUser.getUid());
            userProfile.setSupUid2(inviteUser.getSupUid1());
            userProfile.setSupUid3(inviteUser.getSupUid2());
            userProfile.setSupUid4(inviteUser.getSupUid3());
            userProfile.setSupUid5(inviteUser.getSupUid4());
            userProfile.setSupUid6(inviteUser.getSupUid5());
            // 更新上级代理缓存
            userCache.updateUserSubordinateUidListHash(inviteUser.getUid());
        } else {
            // 默认root
            int root = 1;
            userProfile.setSupUid1(root);
            // 更新root下级缓存
            userCache.updateUserSubordinateUidListHash(root);
        }
        userProfile.setAutoTransfer(1);
        userProfile.setCreatedAt(time);
        userProfile.setUpdatedAt(time);
        userProfileServiceImpl.save(userProfile);
        userCache.updateUserName2UidCacheMap();
        userCache.updateTestUidListCache();
    }

    /**
     * 根据link获取UID
     *
     * @param link link
     * @return UID
     */
    private Integer getAgentLinksUidByLink(String link) {
        Integer uid = null;
        if (StringUtils.isNotBlank(link)) {
            link = subStringLink(link);
            log.info("======after link====>" + link);
            AgentLinks agentLinks = agentLinksServiceImpl.lambdaQuery()
                    .eq(AgentLinks::getLink, link)
                    // 状态:1-启用 0-停用
                    .eq(AgentLinks::getStatus, 1)
                    .one();
            if (null != agentLinks) {
                uid = agentLinks.getUid();
            }
        }
        return uid;
    }

    /**
     * 截取原域名
     * 原http://pch5.sp.com或https://pch5.sp.com -> sp.com
     * 原http://www.pch5.sp.com或https://www.pch5.sp.com -> sp.com
     *
     * @param link 原域名
     * @return sp.com
     */
    private String subStringLink(String link) {
        log.info("======before link====>" + link);
        if (StringUtils.isNotBlank(link)) {
            String[] split = link.split("[.]");
            if (split.length >= 2) {
                link = split[split.length - 2] + "." + split[split.length - 1];
            }
        }
        return link;
    }
}
