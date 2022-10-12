package com.xinbo.sports.service.cache.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xinbo.sports.dao.generator.po.*;
import com.xinbo.sports.dao.generator.service.*;
import com.xinbo.sports.service.cache.KeyConstant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.bo.UserCacheBo;
import com.xinbo.sports.service.io.constant.ConstData;
import com.xinbo.sports.service.io.dto.BaseParams.HeaderInfo;
import com.xinbo.sports.service.io.dto.api.SmsParams.SmsSendDto;
import com.xinbo.sports.service.io.enums.BaseEnum;
import com.xinbo.sports.utils.BeanConvertUtils;
import com.xinbo.sports.utils.JedisUtil;
import com.xinbo.sports.utils.JwtUtils;
import com.xinbo.sports.utils.components.response.CodeInfo;
import com.xinbo.sports.utils.components.sms.changlan.SmsEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.xinbo.sports.service.common.Constant.DOWNLOAD_LOGO;


/**
 * <p>
 * redis缓存类:缓存用户
 * </p>
 *
 * @author David
 * @since 2020/6/23
 */
@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public final class UserCache {
    private final JedisUtil jedisUtil;
    private final JwtUtils jwtUtils;
    private final ConfigCache configCache;
    private final UserProfileService userProfileServiceImpl;
    private final UserService userServiceImpl;
    private final UserFlagService userFlagServiceImpl;
    private final UserLevelService userLevelServiceImpl;
    private final PayOfflineService payOfflineServiceImpl;
    private final PayOnlineService payOnlineServiceImpl;

    /**
     * 验证用户Token是否有效
     *
     * @return true-有效 false-无效
     */
    public HeaderInfo validUserToken(String token) {
        // 获取JWT密钥、有效时间
        BaseEnum.JWT jwtProp = configCache.getJwtProp();
        jwtUtils.init(jwtProp.getSecret(), jwtProp.getExpired());

        // JWT 解析 验证不通过则删除Token
        JSONObject jsonObject = jwtUtils.parseToken(token);
        if (jsonObject == null) {
            throw new BusinessException(CodeInfo.STATUS_CODE_401);
        }
        // 判断Token 是否过期
        if (jsonObject.getLong(ConstData.JWT_EXP_KEY) < Instant.now().getEpochSecond()) {
            throw new BusinessException(CodeInfo.STATUS_CODE_401_2);
        }
        Integer id = jsonObject.getInteger("id");

        String hget = jedisUtil.hget(KeyConstant.USER_TOKEN_HASH, id.toString());
        if (StringUtils.isBlank(hget) || !hget.equals(token)) {
            throw new BusinessException(CodeInfo.STATUS_CODE_401);
        }

        return HeaderInfo.builder()
                .id(id)
                .username(jsonObject.getString("username"))
                .build();
    }

    /**
     * 设置用户Token
     */
    public void setUserToken(@NotNull Integer uid, String token) {
        jedisUtil.hset(KeyConstant.USER_TOKEN_HASH, uid.toString(), token);
    }

    /**
     * 删除用户Token
     */
    public void delUserToken(@NotNull Integer uid) {
        jedisUtil.hdel(KeyConstant.USER_TOKEN_HASH, uid.toString());
    }

    /**
     * 无效登录次数加上指定增量值
     */
    public Long modifyInvalidLoginTimes(String username, Long nums) {
        return jedisUtil.hincrBy(KeyConstant.USER_LOGIN_INVALID_TIMES, username, nums);
    }

    /**
     * 删除用户错误登录次数
     */
    public void delInvalidLoginTimes(String username) {
        jedisUtil.hdel(KeyConstant.USER_LOGIN_INVALID_TIMES, username);
    }

    /**
     * 设置SmsCode 信息
     */
    public void setSmsSendCache(@NotNull SmsSendDto dto) {
        String field = dto.getAreaCode() + dto.getMobile();
        String data = jedisUtil.hget(KeyConstant.SMS_CODE_HASH, field);
        List<SmsSendDto> list = new LinkedList<>();
        if (StringUtils.isNotBlank(data)) {
            list = JSON.parseArray(data, SmsSendDto.class);
        }
        List<SmsSendDto> collect = list.stream()
                .filter(o -> o.getCreateAt() > Instant.now().getEpochSecond() - 3600 * 24)
                .collect(Collectors.toList());
        // 一天内最大发送条数10条
        if (collect.size() > SmsEnum.LIMIT.SMS_MAX_NUM_PER_DAY.getValue()) {
            throw new BusinessException(CodeInfo.SMS_SENT_OVER_COUNT);
        }

        // 一小时内最多发送5条
        int size = (int) collect.stream().filter(o -> o.getCreateAt() > Instant.now().getEpochSecond() - 3600).count();
        if (size >= SmsEnum.LIMIT.SMS_MAX_NUM_PER_HOUR.getValue()) {
            throw new BusinessException(CodeInfo.SMS_SENT_TOO_MANY_TIME);
        }

        collect.add(dto);

        jedisUtil.hset(KeyConstant.SMS_CODE_HASH, field, JSON.toJSONString(collect));
    }

    /**
     * 验证发送的SMS Code 码
     */
    public void validSmsSendCache(@NotNull SmsSendDto dto) {
        // 验证码后门[888888]
        if (dto.getCode() == ConstData.SMS_PASS_CODE) {
            return;
        }

        String field = dto.getAreaCode() + dto.getMobile();
        String data = jedisUtil.hget(KeyConstant.SMS_CODE_HASH, field);
        // 返回最新的一条记录
        if (StringUtils.isBlank(data)) {
            throw new BusinessException(CodeInfo.SMS_INVALID);
        }

        List<SmsSendDto> smsSendDtos = JSON.parseArray(data, SmsSendDto.class);
        // 判定最新的验证码 是否相同
        SmsSendDto smsSendDto = smsSendDtos.get(smsSendDtos.size() - 1);
        if (!dto.getCode().equals(smsSendDto.getCode())) {
            throw new BusinessException(CodeInfo.SMS_INVALID);
        }

        // 判定最新的验证码是否是5分钟内的
        if (smsSendDto.getCreateAt() < Instant.now().toEpochMilli() - ConstData.SMS_INVALID_MILLI) {
            throw new BusinessException(CodeInfo.SMS_INVALID);
        }
    }

    /**
     * 缓存下级UID列表
     *
     * @param uid UID
     * @return 下级UID列表
     */
    public List<Integer> getSubordinateUidListByUid(Integer uid) {
        String key = KeyConstant.USER_SUBORDINATE_UID_LIST_HASH;
        String value = jedisUtil.hget(key, uid.toString());
        if (StringUtils.isNotBlank(value)) {
            return JSON.parseArray(value, Integer.class);
        }
        List<Integer> uidList = userProfileServiceImpl.lambdaQuery()
                .eq(UserProfile::getSupUid1, uid)
                .eq(UserProfile::getStatus, 10)
                .list()
                .stream()
                .parallel()
                .map(UserProfile::getUid)
                .collect(Collectors.toList());
        if (!uidList.isEmpty()) {
            List<User> list = userServiceImpl.lambdaQuery()
                    .in(User::getRole, 0, 1)
                    .in(User::getId, uidList)
                    .list();
            if (Optional.ofNullable(list).isPresent()) {
                uidList = list.stream().parallel().map(User::getId).collect(Collectors.toList());
            }
        }
        jedisUtil.hset(key, uid.toString(), JSON.toJSONString(uidList));
        return uidList;
    }

    /**
     * 缓存下级UID列表(六个层级)
     *
     * @param uid UID
     * @return 下级UID列表(六个层级)
     */
    public List<Integer> getSupUid6ListByUid(Integer uid) {
        String key = KeyConstant.USER_SUBORDINATE6_UID_LIST_HASH;
        String value = jedisUtil.hget(key, uid.toString());
        if (StringUtils.isNotBlank(value)) {
            return JSON.parseArray(value, Integer.class);
        }
        LambdaQueryWrapper<UserProfile> w = Wrappers.lambdaQuery();
        w.eq(UserProfile::getStatus, 10);
        w.and(o -> o.eq(UserProfile::getSupUid1, uid)
                .or().eq(UserProfile::getSupUid2, uid)
                .or().eq(UserProfile::getSupUid3, uid)
                .or().eq(UserProfile::getSupUid4, uid)
                .or().eq(UserProfile::getSupUid5, uid)
                .or().eq(UserProfile::getSupUid6, uid));
        List<Integer> uidList = userProfileServiceImpl.list(w).stream().parallel().map(UserProfile::getUid)
                .collect(Collectors.toList());
        if (Optional.ofNullable(uidList).isPresent() && !uidList.isEmpty()) {
            List<User> list = userServiceImpl.lambdaQuery().in(User::getRole, 0, 1)
                    .in(User::getId, uidList).list();
            if (Optional.ofNullable(list).isPresent()) {
                uidList = list.stream().parallel().map(User::getId).collect(Collectors.toList());
            }
        }
        jedisUtil.hset(key, uid.toString(), JSON.toJSONString(uidList));
        return uidList;
    }

    /**
     * 根据UID获取用户信息
     *
     * @param uid UID
     * @return 用户信息
     */
    public UserCacheBo.UserCacheInfo getUserInfoById(Integer uid) {
        String key = KeyConstant.USER_BASIC_INFO_UID_HASH;
        String value = jedisUtil.hget(key, uid.toString());
        if (StringUtils.isNotBlank(value)) {
            return JSON.parseObject(value).toJavaObject(UserCacheBo.UserCacheInfo.class);
        }
        UserCacheBo.UserCacheInfo userCacheInfo = getUserBasicInfoByUid(uid);
        if (Objects.nonNull(userCacheInfo)) {
            jedisUtil.hset(key, uid.toString(), JSON.toJSONString(userCacheInfo));
        }
        return userCacheInfo;
    }

    /**
     * 根据userName获取用户信息
     *
     * @param userName 用户名
     * @return 用户信息
     */
    public UserCacheBo.UserCacheInfo getUserInfoByUserName(String userName) {
        String key = KeyConstant.USER_BASIC_INFO_USERNAME_HASH;
        String value = jedisUtil.hget(key, userName);
        if (StringUtils.isNotBlank(value)) {
            return JSON.parseObject(value).toJavaObject(UserCacheBo.UserCacheInfo.class);
        }
        UserCacheBo.UserCacheInfo userCacheInfo = getUserBasicInfoByUserName(userName);
        if (Objects.nonNull(userCacheInfo)) {
            jedisUtil.hset(key, userName, JSON.toJSONString(userCacheInfo));
        }
        return userCacheInfo;
    }

    private UserCacheBo.UserCacheInfo getUserBasicInfoByUid(Integer uid) {
        return getUserBasicInfo(uid, null);
    }

    private UserCacheBo.UserCacheInfo getUserBasicInfoByUserName(String userName) {
        return getUserBasicInfo(null, userName);
    }

    /**
     * 获取用户基本信息
     *
     * @param uid      UID
     * @param userName 用户名
     * @return 用户信息
     */
    private UserCacheBo.UserCacheInfo getUserBasicInfo(Integer uid, String userName) {
        User user = userServiceImpl.lambdaQuery()
                .eq(Objects.nonNull(uid), User::getId, uid)
                .eq(Objects.nonNull(userName), User::getUsername, userName)
                .one();
        if (Objects.nonNull(user)) {
            UserProfile userProfile = userProfileServiceImpl.lambdaQuery().eq(UserProfile::getUid, user.getId()).one();
            String avatar = user.getAvatar().startsWith("http")?user.getAvatar():configCache.getStaticServer() + user.getAvatar();
            return UserCacheBo.UserCacheInfo.builder()
                    .uid(userProfile.getUid())
                    .username(user.getUsername())
                    .avatar(avatar)
                    .levelId(user.getLevelId())
                    .role(user.getRole())
                    .promoCode(userProfile.getPromoCode())
                    .flag(user.getFlag())
                    .createdAt(userProfile.getCreatedAt())
                    .extraCode(userProfile.getExtraCode())
                    .build();
        }
        return null;
    }

    /**
     * 获取用户所拥有的会员旗
     *
     * @param uid UID
     * @return 用户会员旗
     */
    public List<UserCacheBo.UserFlagInfo> getUserFlagList(Integer uid) {
        List<UserCacheBo.UserFlagInfo> userFlagInfoList = new ArrayList<>();
        String key = KeyConstant.USER_FLAG_LIST_HASH;
        String value = jedisUtil.hget(key, uid.toString());
        if (StringUtils.isNotBlank(value)) {
            return JSON.parseArray(value, UserCacheBo.UserFlagInfo.class);
        }

        UserCacheBo.UserCacheInfo userCacheInfo = getUserInfoById(uid);
        if (null == userCacheInfo) {
            return userFlagInfoList;
        }
        LambdaQueryWrapper<UserFlag> where = Wrappers.lambdaQuery();
        where.select(UserFlag::getIcon, UserFlag::getIconColor, UserFlag::getName);
        where.eq(UserFlag::getStatus, 1);
        where.apply("bit_code & " + userCacheInfo.getFlag());
        List<UserFlag> userFlag = userFlagServiceImpl.list(where);
        userFlagInfoList = BeanConvertUtils.copyListProperties(userFlag, UserCacheBo.UserFlagInfo::new);
        if (!userFlagInfoList.isEmpty()) {
            jedisUtil.hset(key, uid.toString(), JSON.toJSONString(userFlagInfoList));
        }
        return userFlagInfoList;
    }

    /**
     * 删除用户的会员旗
     */
    public void updateUserFlag(@NotNull Integer uid) {
        jedisUtil.hdel(KeyConstant.USER_FLAG_LIST_HASH, uid.toString());
        getUserFlagList(uid);
    }

    /**
     * 缓存所有会员等级
     *
     * @return 所有会员等级
     */
    public List<UserLevel> getUserLevelList() {
        String key = KeyConstant.USER_LEVEL_ID_LIST_HASH;
        String subKey = KeyConstant.COMMON_TOTAL_HASH;
        String value = jedisUtil.hget(key, subKey);
        if (StringUtils.isNotBlank(value)) {
            return JSON.parseArray(value, UserLevel.class);
        }
        List<UserLevel> list = userLevelServiceImpl.list();
        if (!list.isEmpty()) {
            jedisUtil.hset(key, subKey, JSON.toJSONString(list));
        }
        return list;
    }

    /**
     * 根据id获取UserLevel
     *
     * @return UserLevel
     */
    public UserLevel getUserLevelById(Integer id) {
        String key = KeyConstant.USER_LEVEL_ID_LIST_HASH;
        String value = jedisUtil.hget(key, id.toString());
        if (StringUtils.isNotBlank(value)) {
            return JSON.parseObject(value).toJavaObject(UserLevel.class);
        }
        UserLevel userLevel = userLevelServiceImpl.lambdaQuery().eq(UserLevel::getId, id).one();
        if (null != userLevel) {
            jedisUtil.hset(key, id.toString(), JSON.toJSONString(userLevel));
        }
        return userLevel;
    }

    /**
     * 获取用户线下支付列表
     *
     * @param uid UID
     * @return 用户线下支付列表
     */
    public List<PayOffline> getUserPayOffLineListByUid(Integer uid) {
        String key = KeyConstant.USER_PAY_OFFLINE_HASH;
        Integer levelId = getUserInfoById(uid).getLevelId();
        String value = jedisUtil.hget(key, levelId.toString());

        if (StringUtils.isNotBlank(value)) {
            return JSON.parseArray(value, PayOffline.class);
        }
        UserLevel userLevel = getUserLevelById(levelId);
        if (null != userLevel) {
            LambdaQueryWrapper<PayOffline> where = Wrappers.lambdaQuery();
            where.eq(PayOffline::getStatus, 1);
            where.orderByDesc(PayOffline::getSort);
            where.apply("level_bit & " + userLevel.getBitCode());
            List<PayOffline> list = payOfflineServiceImpl.list(where);
            if (Optional.ofNullable(list).isPresent() && !list.isEmpty()) {
                jedisUtil.hset(key, levelId.toString(), JSON.toJSONString(list));
            }
            return list;
        }
        return new ArrayList<>();
    }

    /**
     * 获取用户线上支付列表
     *
     * @param uid UID
     * @return 用户线上支付列表
     */
    public List<PayOnline> getUserPayOnLineListByUid(Integer uid) {
        String key = KeyConstant.USER_PAY_ONLINE_HASH;
        Integer levelId = getUserInfoById(uid).getLevelId();
        String value = jedisUtil.hget(key, levelId.toString());
        if (StringUtils.isNotBlank(value)) {
            return JSON.parseArray(value, PayOnline.class);
        }
        UserLevel userLevel = getUserLevelById(levelId);
        if (null != userLevel) {
            LambdaQueryWrapper<PayOnline> where = Wrappers.lambdaQuery();
            where.eq(PayOnline::getStatus, 1);
            where.orderByDesc(PayOnline::getSort);
            where.apply("level_bit & " + userLevel.getBitCode());
            List<PayOnline> list = payOnlineServiceImpl.list(where);
            if (Optional.ofNullable(list).isPresent() && !list.isEmpty()) {
                jedisUtil.hset(key, levelId.toString(), JSON.toJSONString(list));
            }
            return list;
        }
        return new ArrayList<>();
    }


    /**
     * 删除用户线下支付列表缓存
     */
    public void delUserPayOffLineList() {
        jedisUtil.del(KeyConstant.USER_PAY_OFFLINE_HASH);
    }

    /**
     * 删除会员旗列表缓存
     */
    public void updateUserFlagList() {
        jedisUtil.del(KeyConstant.USER_FLAG_LIST_HASH);
    }

    /**
     * 根据username获取UID
     *
     * @return Map<username, uid>
     */
    public Map<String, Integer> userName2UidCacheMap() {
        String key = KeyConstant.COMMON_TOTAL_HASH;
        String data = jedisUtil.hget(KeyConstant.USER_NAME_2_UID_HASH, key);
        if (StringUtils.isNotBlank(data)) {
            JSONObject jsonObject = parseObject(data);
            return jsonObject.toJavaObject(new TypeReference<Map<String, Integer>>() {
            });
        }

        Map<String, Integer> map = userServiceImpl
                .list()
                .stream()
                .collect(Collectors.toMap(User::getUsername, User::getId));

        if (Optional.ofNullable(map).isPresent() && map.size() > 0) {
            jedisUtil.hset(KeyConstant.USER_NAME_2_UID_HASH, key, JSON.toJSONString(map));
        }
        return map;
    }

    /**
     * 更新下级代理缓存
     *
     * @param uid UID
     */
    public void updateUserSubordinateUidListHash(Integer uid) {
        jedisUtil.hdel(KeyConstant.USER_SUBORDINATE_UID_LIST_HASH, uid.toString());
        jedisUtil.del(KeyConstant.USER_SUBORDINATE6_UID_LIST_HASH);
        getSubordinateUidListByUid(uid);
    }

    /**
     * 更新缓存[用户名2UID映射关系]
     *
     * @return Map<username, uid>
     */
    public void updateUserName2UidCacheMap() {
        jedisUtil.hdel(KeyConstant.USER_NAME_2_UID_HASH, KeyConstant.COMMON_TOTAL_HASH);
        userName2UidCacheMap();
    }

    /**
     * 更新缓存[根据UID获取用户信息]
     *
     * @param uid UID
     */
    public void updateUserInfoById(Integer uid) {
        jedisUtil.hdel(KeyConstant.USER_BASIC_INFO_UID_HASH, uid.toString());
        UserCacheBo.UserCacheInfo userCacheInfo = getUserInfoById(uid);
        if (null != userCacheInfo) {
            updateUserInfoByUserName(userCacheInfo.getUsername());
        }
    }

    /**
     * 更新缓存[根据userName获取用户信息]
     *
     * @param userName 用户名
     */
    public void updateUserInfoByUserName(String userName) {
        jedisUtil.hdel(KeyConstant.USER_BASIC_INFO_USERNAME_HASH, userName);
        getUserInfoByUserName(userName);
    }

    /**
     * 更新用户线上支付列表缓存
     */
    public void updateUserPayOnLineList() {
        Set<String> hkeys = jedisUtil.hkeys(KeyConstant.USER_PAY_ONLINE_HASH);
        for (String x : hkeys) {
            jedisUtil.hdel(KeyConstant.USER_PAY_ONLINE_HASH, x);
        }
    }

    /**
     * 更新用户线下支付列表缓存
     */
    public void updateUserPayOffLineList() {
        Set<String> hkeys = jedisUtil.hkeys(KeyConstant.USER_PAY_OFFLINE_HASH);
        for (String x : hkeys) {
            jedisUtil.hdel(KeyConstant.USER_PAY_OFFLINE_HASH, x);
        }
    }

    /**
     * 更新用户线下支付列表缓存
     */
    public void updateUserPayoutOnLineList() {
        Set<String> hkeys = jedisUtil.hkeys(KeyConstant.USER_PAYOUT_ONLINE_HASH);
        for (String x : hkeys) {
            jedisUtil.hdel(KeyConstant.USER_PAYOUT_ONLINE_HASH, x);
        }
    }


    /**
     * 更新testUidTestCache
     */
    public void updateTestUidListCache() {
        String key = KeyConstant.USER_HASH;
        String subKey = KeyConstant.TEST_UID_LIST;

        jedisUtil.hdel(key, subKey);
        getTestUidList();
    }

    /**
     * 获取测试账号UID列表
     *
     * @return 测试账号UID列表
     */
    public List<Integer> getTestUidList() {
        String key = KeyConstant.USER_HASH;
        String subKey = KeyConstant.TEST_UID_LIST;
        String value = jedisUtil.hget(key, subKey);
        if (StringUtils.isNotBlank(value)) {
            return JSON.parseArray(value, Integer.class);
        }
        List<Integer> uidList = userServiceImpl.lambdaQuery()
                // 角色:0-会员 1-代理 2-总代理 3-股东 4-测试
                .eq(User::getRole, 4)
                .list().stream().map(User::getId).collect(Collectors.toList());

        if (Optional.ofNullable(uidList).isPresent() && !uidList.isEmpty()) {
            jedisUtil.hset(key, subKey, JSON.toJSONString(uidList));
        }
        return uidList;
    }

    /**
     * 根据等级Id获取UID列表
     *
     * @return 等级UID列表
     */
    public List<Integer> getUidListByLevelId(Integer levelId) {
        String key = KeyConstant.USER_HASH;
        String subKey = levelId.toString();
        String value = jedisUtil.hget(key, subKey);
        if (StringUtils.isNotBlank(value)) {
            return JSON.parseArray(value, Integer.class);
        }
        List<Integer> uidList = userServiceImpl.lambdaQuery()
                .eq(User::getLevelId, levelId)
                .list().stream().map(User::getId).collect(Collectors.toList());

        if (Optional.ofNullable(uidList).isPresent() && !uidList.isEmpty()) {
            jedisUtil.hset(key, subKey, JSON.toJSONString(uidList));
        }
        return uidList;
    }


}
