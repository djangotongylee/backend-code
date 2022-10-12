package com.xinbo.sports.service.cache.redis;

import com.xinbo.sports.service.cache.KeyConstant;
import com.xinbo.sports.utils.JedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Banner图缓存
 * </p>
 *
 * @author andy
 * @since 2020/10/6
 */
@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BannerCache {
    private final JedisUtil jedisUtil;

    /**
     * 删除Banner图缓存
     */
    public void delBanner() {
        jedisUtil.del(KeyConstant.BANNER_HASH);
    }
}
