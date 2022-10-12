package com.xinbo.sports.apiend.cache.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xinbo.sports.apiend.io.dto.mapper.BannerResDto;
import com.xinbo.sports.dao.generator.po.Banner;
import com.xinbo.sports.dao.generator.service.BannerService;
import com.xinbo.sports.service.cache.KeyConstant;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.service.io.dto.BaseParams.HeaderInfo;
import com.xinbo.sports.service.io.enums.BaseEnum;
import com.xinbo.sports.service.thread.ThreadHeaderLocalData;
import com.xinbo.sports.utils.BeanConvertUtils;
import com.xinbo.sports.utils.JedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.xinbo.sports.apiend.io.bo.HomeParams.InitResDto;
import static com.xinbo.sports.service.common.Constant.TERMINAL_H5;
import static com.xinbo.sports.service.common.Constant.TERMINAL_PC;


/**
 * <p>
 * redis缓存类:首页信息
 * </p>
 *
 * @author David
 * @since 2020/6/23
 */
@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HomeCache {
    private final BannerService bannerServiceImpl;
    private final JedisUtil jedisUtil;
    private final ConfigCache configCache;

    /**
     * Init 初始化信息缓存
     *
     * @return 信息列表
     */
    public List<InitResDto> getInitResCache() {
        // 类型: 1-前端 2-后台
        return configCache.getConfigCacheByShowApp(1)
                .stream()
                .map(o -> BeanConvertUtils.beanCopy(o, InitResDto::new))
                .collect(Collectors.toList());
    }

    /**
     * 根据语言、种类 缓存Banner图
     *
     * @param category Banner 类型
     * @return Banner 列表
     */
    public List<BannerResDto> getBannerCache(@NotNull Integer category) {
        HeaderInfo headerInfo = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        var lang = headerInfo.lang;
        String key = KeyConstant.BANNER_HASH;
        String subKey = headerInfo.device + "_" + headerInfo.getLang() + "_" + category.toString();
        // Banner图  语言+category插入缓存
        String value = jedisUtil.hget(key, subKey);
        if (StringUtils.isNotBlank(value)) {
            return JSON.parseArray(value, BannerResDto.class);
        }

        String staticServer = configCache.getStaticServer();
        List<BannerResDto> collect = bannerServiceImpl.lambdaQuery()
                .eq(Banner::getStatus, 1)
                .eq(Banner::getCategory, category)
                .orderByDesc(Banner::getSort)
                .orderByDesc(Banner::getId)
                .last("limit 20")
                .list()
                .stream()
                .map(o -> {
                    BannerResDto bannerResDto = BeanConvertUtils.beanCopy(o, BannerResDto::new);
                    var imgJson = parseObject(o.getImg());
                    if (null != imgJson) {
                        var h5 = (JSONObject) imgJson.get(TERMINAL_H5);
                        var pc = (JSONObject) imgJson.get(TERMINAL_PC);
                        if (headerInfo.getDevice().equals(BaseEnum.DEVICE.D.getValue())) {
                            bannerResDto.setImg(pc.getOrDefault (lang, "").toString().startsWith("http")?pc.getOrDefault (lang, "").toString():staticServer + pc.getOrDefault (lang, ""));
                        } else {
                            bannerResDto.setImg(h5.getOrDefault(lang, "").toString().startsWith("http")?h5.getOrDefault(lang, "").toString():staticServer + h5.getOrDefault(lang, ""));
                        }
                    }
                    bannerResDto.setHref(parseObject(o.getHref()).getString(lang));
                    return bannerResDto;
                }).collect(Collectors.toList());
        if (!collect.isEmpty()) {
            jedisUtil.hset(key, subKey, JSON.toJSONString(collect));
        }

        return collect;
    }


    public Boolean delCache(@NotNull String cacheKey, String cacheField) {
        if (!cacheKey.equals("USER_TOKEN_HASH") && !cacheKey.equals("ADMIN_TOKEN_HASH") && !cacheKey.equals("REL_UID_CHANNEL")) {
            if (cacheField != null) {
                jedisUtil.hdel(cacheKey, cacheField);
                return true;
            } else {
                jedisUtil.del(cacheKey);
                return true;
            }
        }
        return false;
    }

    /**
     * 项目重启清空在线人数的缓存
     */
    @PostConstruct
    public void deleteRelUidChannel() {
        jedisUtil.del("REL_UID_CHANNEL");
    }
}