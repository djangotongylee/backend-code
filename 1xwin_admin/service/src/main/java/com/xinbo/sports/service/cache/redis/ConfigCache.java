package com.xinbo.sports.service.cache.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.xinbo.sports.dao.generator.po.Config;
import com.xinbo.sports.dao.generator.po.PlatList;
import com.xinbo.sports.dao.generator.service.ConfigService;
import com.xinbo.sports.dao.generator.service.PlatListService;
import com.xinbo.sports.service.cache.KeyConstant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.enums.BaseEnum;
import com.xinbo.sports.utils.JedisUtil;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.xinbo.sports.service.io.enums.BaseEnum.JWT;
import static com.xinbo.sports.service.io.enums.BaseEnum.RSA;


/**
 * <p>
 * 系统相关信息缓存
 * </p>
 *
 * @author David
 * @since 2020/6/23
 */
@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConfigCache {
    private final JedisUtil jedisUtil;
    private final ConfigService configServiceImpl;
    private final PlatListService platListServiceImpl;

    /**
     * 从缓存中获取JWT 属性
     *
     * @return key|expiredTime
     */
    public JWT getJwtProp() {
        String data = jedisUtil.hget(KeyConstant.CONFIG_HASH, KeyConstant.CONFIG_HASH_JWT);
        if (StringUtils.isNotBlank(data)) {
            return JSON.parseObject(data).toJavaObject(JWT.class);
        }

        Config config = configServiceImpl.lambdaQuery().eq(Config::getTitle, "jwt").one();
        if (Objects.nonNull(config)) {
            String secret = config.getValue();
            JSONObject jsonObject = JSON.parseObject(secret);
            JWT build = JWT.builder()
                    .secret(jsonObject.getString("secret"))
                    .expired(jsonObject.getLong("expired"))
                    .build();

            jedisUtil.hset(KeyConstant.CONFIG_HASH, KeyConstant.CONFIG_HASH_JWT, JSON.toJSONString(build));
            return build;
        }
        return JWT.builder().build();
    }

    /**
     * 获取RSA密钥信息
     *
     * @return publicKey|privateKey
     */
    public RSA getRsaProps() {
        String data = jedisUtil.hget(KeyConstant.CONFIG_HASH, KeyConstant.CONFIG_HASH_RSA);
        if (StringUtils.isNotBlank(data)) {
            return JSON.parseObject(data).toJavaObject(RSA.class);
        }

        Config config = configServiceImpl.lambdaQuery().eq(Config::getTitle, "rsa").one();
        if (Objects.nonNull(config)) {
            String secret = config.getValue();
            JSONObject jsonObject = JSON.parseObject(secret);
            RSA build = RSA.builder()
                    .privateKey(jsonObject.getString("private_key"))
                    .publicKey(jsonObject.getString("public_key"))
                    .build();

            jedisUtil.hset(KeyConstant.CONFIG_HASH, KeyConstant.CONFIG_HASH_RSA, JSON.toJSONString(build));
            return build;
        }
        return RSA.builder().build();
    }

    /**
     * 获取静态资源服务器
     *
     * @return 静态资源服务器
     */
    public String getStaticServer() {
        return getStaticServerConfig().getUrl();
    }

    /**
     * 获取FastDFS文件上传路径(M03=>3)
     *
     * @return group1/变量/00/04/wKgBvl9oazKARV64AABLaVkgPjo659.png
     */
    public Integer getFastDfsStoreIndex() {
        return getStaticServerConfig().getIndex();
    }

    /**
     * getFastDFS StaticServerConfig
     *
     * @return StaticServerConfig
     */
    public BaseEnum.StaticServerConfig getStaticServerConfig() {
        String staticServer = getConfigByTitle("static_server");
        return JSON.parseObject(staticServer).toJavaObject(BaseEnum.StaticServerConfig.class);
    }

    /**
     * 获取国家简称
     *
     * @return 英文简称
     */
    public String getCountry() {
        return getConfigByTitle("country");
    }

    /**
     * 获取三方游戏前缀
     *
     * @return 三方游戏平台前缀名称
     */
    public String getPlatPrefix() {
        return getConfigByTitle("plat_prefix");
    }

    /**
     * 获取webSocket地址
     *
     * @return webSocket地址
     */
    public String getWsServer() {
        return getConfigByTitle("ws_server");
    }

    /**
     * 获取游戏列表ID与注单表对应的表明
     *
     * @param gameListId
     * @return 表名
     */
    public String getBetSlipsTableNameByGameListId(Integer gameListId) {
        String data = getConfigByTitle(KeyConstant.GAME_LIST_ID_2_BET_SLIPS_MAPPER);
        JSONObject jsonObject = parseObject(data);
        Map<Integer, String> map = jsonObject.toJavaObject(new TypeReference<Map<Integer, String>>() {
        });
        return map.get(gameListId);
    }

    /**
     * 根据标题获取配置的Value值
     *
     * @param title 标题
     * @return 值
     */
    public String getConfigByTitle(String title) {
        String data = jedisUtil.hget(KeyConstant.CONFIG_HASH, title);
        if (StringUtils.isNotBlank(data)) {
            return data;
        }

        Config one = configServiceImpl.lambdaQuery().eq(Config::getTitle, title).one();
        if (Objects.nonNull(one)) {
            jedisUtil.hset(KeyConstant.CONFIG_HASH, title, one.getValue());
            return one.getValue();
        }
        throw new BusinessException(CodeInfo.CONFIG_INVALID);
    }

    /**
     * 获取平台config字段对应的信息
     *
     * @param model       平台名称
     * @param toJavaClass 类型
     * @param <T>         返回类型
     * @return 配置信息
     */
    public <T> T platConfigByModelName(String model, Class<T> toJavaClass) {
        String config = jedisUtil.hget(KeyConstant.PLAT_CONFIG_HASH, model);
        if (StringUtils.isNotBlank(config)) {
            return JSON.parseObject(config).toJavaObject(toJavaClass);
        }
        PlatList one = platListServiceImpl.lambdaQuery().eq(PlatList::getName, model).one();
        if (one != null && StringUtils.isNotEmpty(one.getConfig())) {
            jedisUtil.hset(KeyConstant.PLAT_CONFIG_HASH, model, one.getConfig());
            return JSON.parseObject(one.getConfig()).toJavaObject(toJavaClass);
        }

        log.error("=====" + model + ": 平台配置异常");
        throw new BusinessException(CodeInfo.CONFIG_INVALID);
    }

    /**
     * 拉单接口调用:根据MODEL获取最近更新时间
     *
     * @param model MODEL
     * @return 最近更新时间
     */
    public String getLastUpdateTimeByModel(String model) {
        String result = "";
        String key = KeyConstant.PLATFORM_GAME_PULL_DATA_HASH;
        String value = jedisUtil.hget(key, model);
        if (StringUtils.isNotBlank(value)) {
            result = value;
            log.info("[getLastUpdateTimeByModel:\tmodel={},\tlastUpdateTime={}]", model, result);
        }
        return result;
    }

    /**
     * 拉单接口调用:根据MODEL更新最近时间
     *
     * @param model          MODEL
     * @param lastUpdateTime 最近时间
     */
    public void reSetLastUpdateTimeByModel(String model, String lastUpdateTime) {
        log.info("[reSetLastUpdateTimeByModel:\tmodel={},\tlastUpdateTime={}]", model, lastUpdateTime);
        jedisUtil.hset(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH, model, lastUpdateTime);
    }

    public void updatePlatConfigByModelName(String name) {
        jedisUtil.hdel(KeyConstant.PLAT_CONFIG_HASH, name);
        String config = jedisUtil.hget(KeyConstant.PLAT_CONFIG_HASH, name);
        if (StringUtils.isNotBlank(config)) {
            return;
        }
        PlatList one = platListServiceImpl.lambdaQuery().eq(PlatList::getName, name).one();
        if (one != null && StringUtils.isNotEmpty(one.getConfig())) {
            jedisUtil.hset(KeyConstant.PLAT_CONFIG_HASH, name, one.getConfig());
        }
    }

    public PlatList getPlatListByName(String name) {
        String config = jedisUtil.hget(KeyConstant.PLAT_LIST_HASH, name);
        if (StringUtils.isNotBlank(config)) {
            return JSON.parseObject(config).toJavaObject(PlatList.class);
        }
        PlatList one = platListServiceImpl.lambdaQuery().eq(PlatList::getName, name).one();
        if (one != null && StringUtils.isNotEmpty(one.getConfig())) {
            jedisUtil.hset(KeyConstant.PLAT_LIST_HASH, name, JSON.toJSONString(one));
        }
        return one;
    }

    /**
     * @Author Wells
     * @Description 初始化平台参数
     * @Date 2020/9/16 4:01 下午
     * @param1
     * @Return void
     **/
    @PostConstruct
    private void initConfig() {
        var platList = platListServiceImpl.lambdaQuery().eq(PlatList::getStatus, 1).list();
        if (!CollectionUtils.isEmpty(platList)) {
            platList.forEach(plat ->
                    jedisUtil.hset(KeyConstant.PLAT_CONFIG_HASH, plat.getName(), plat.getConfig())
            );
        }
    }

    /**
     * 获取sms配置
     */
    public JSONObject getSmsProp() {
        String data = jedisUtil.hget(KeyConstant.CONFIG_HASH, KeyConstant.CONFIG_HASH_SMS);
        if (StringUtils.isNotBlank(data)) {
            return parseObject(data);
        }

        Config config = configServiceImpl.lambdaQuery().eq(Config::getTitle, "sms").one();
        if (Objects.nonNull(config)) {
            String secret = config.getValue();
            if (!secret.isEmpty()) {
                jedisUtil.hset(KeyConstant.CONFIG_HASH, KeyConstant.CONFIG_HASH_SMS, secret);
                return parseObject(secret);
            }
        }
        return new JSONObject();
    }

    public String getRegisterMobile() {
        return getConfigByTitle("register_mobile");
    }

    public String getRegisterInviteCode() {
        return getConfigByTitle("register_invite_code");
    }

    /**
     * 获取RedisKey失效时间
     *
     * @return RedisKey失效时间秒
     */
    public String getRedisExpireTime() {
        String time = "10";
        try {
            time = getConfigByTitle("redis_expire_time");
            if (StringUtils.isBlank(time)) {
                time = "10";
            }
        } catch (Exception e) {
            log.error("redis_expire_time ===> {}", e.getMessage(), e);
        }
        return time;
    }

    /**
     * 获取前端或backend配置列表
     *
     * @param showApp 1-前端 2-后台
     * @return 前端或backend配置列表
     */
    public List<Config> getConfigCacheByShowApp(Integer showApp) {
        List<Integer> showAppList = null;
        String key = KeyConstant.CONFIG_HASH;
        String subKey = null;
        // 类型: 0-全部 1-前端 2-后台
        if (1 == showApp) {
            subKey = KeyConstant.CONFIG_HASH_API;
            showAppList = List.of(0, 1);
        } else {
            subKey = KeyConstant.CONFIG_HASH_BACKEND;
            showAppList = List.of(0, 2);
        }
        String value = jedisUtil.hget(key, subKey);
        if (StringUtils.isNotBlank(value)) {
            return JSON.parseArray(value, Config.class);
        }
        List<Config> collect = configServiceImpl.lambdaQuery()
                .eq(Config::getStatus, 1)
                .in(Optional.ofNullable(showAppList).isPresent() && !showAppList.isEmpty(), Config::getShowApp, showAppList)
                .list();
        if (!collect.isEmpty()) {
            jedisUtil.hset(key, subKey, JSON.toJSONString(collect));
        }

        return collect;
    }


}