package com.xinbo.sports.backend.redis;

import com.alibaba.fastjson.JSON;
import com.xinbo.sports.backend.io.dto.Platform;
import com.xinbo.sports.dao.generator.po.GameGroup;
import com.xinbo.sports.dao.generator.po.GameList;
import com.xinbo.sports.dao.generator.po.GameSlot;
import com.xinbo.sports.dao.generator.po.PlatList;
import com.xinbo.sports.dao.generator.service.GameGroupService;
import com.xinbo.sports.dao.generator.service.GameListService;
import com.xinbo.sports.dao.generator.service.GameSlotService;
import com.xinbo.sports.dao.generator.service.PlatListService;
import com.xinbo.sports.service.cache.KeyConstant;
import com.xinbo.sports.utils.BeanConvertUtils;
import com.xinbo.sports.utils.JedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


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
public class GameCache {
    private final JedisUtil jedisUtil;
    private final GameListService gameListServiceImpl;
    private final PlatListService platListServiceImpl;
    private final GameSlotService gameSlotServiceImpl;
    private final GameGroupService gameGroupServiceImpl;

    /**
     * 获取平台列表(返回API)
     *
     * @return 平台列表
     */
    public List<Platform.PlatListResInfo> getPlatListResCache() {
        String key = KeyConstant.PLATFORM_HASH;
        String subKey = KeyConstant.PLATFORM_HASH_PLAT_LIST;
        String value = jedisUtil.hget(key, subKey);
        if (StringUtils.isNotBlank(value)) {
            return JSON.parseArray(value, Platform.PlatListResInfo.class);
        }

        List<Platform.PlatListResInfo> collect = platListServiceImpl.lambdaQuery()
                .eq(PlatList::getStatus, 1)
                .orderByDesc(PlatList::getSort)
                .list()
                .stream()
                .map(o -> BeanConvertUtils.beanCopy(o, Platform.PlatListResInfo::new))
                .collect(Collectors.toList());

        if (!collect.isEmpty()) {
            jedisUtil.hset(key, subKey, JSON.toJSONString(collect));
        }
        return collect;
    }

    /**
     * 获取游戏列表
     *
     * @return 游戏列表
     */
    public List<Platform.GameListInfo> getGameListResCache() {
        String key = KeyConstant.PLATFORM_HASH;
        String subKey = KeyConstant.PLATFORM_HASH_GAME_LIST;

        String data = jedisUtil.hget(key, subKey);
        if (StringUtils.isNotBlank(data)) {
            return JSON.parseArray(data, Platform.GameListInfo.class);
        }
        List<Platform.GameListInfo> collect = gameListServiceImpl.lambdaQuery()
                .in(GameList::getStatus, 0,1,2)
                .orderByDesc(GameList::getSort)
                .orderByDesc(GameList::getId)
                .list()
                .stream()
                .map(o -> BeanConvertUtils.beanCopy(o, Platform.GameListInfo::new))
                .collect(Collectors.toList());

        if (!collect.isEmpty()) {
            jedisUtil.hset(key, subKey, JSON.toJSONString(collect));
        }
        return collect;
    }

    /**
     * 获取平台配置信息
     *
     * @return 平台配置信息
     */
    public PlatList getPlatListCache(@NotNull Integer platId) {
        String data = jedisUtil.hget(KeyConstant.PLAT_LIST_HASH, platId.toString());
        if (StringUtils.isNotBlank(data)) {
            return JSON.parseObject(data).toJavaObject(PlatList.class);
        }

        PlatList one = platListServiceImpl.lambdaQuery().eq(PlatList::getId, platId).eq(PlatList::getStatus, 1).one();

        if (one != null) {
            jedisUtil.hset(KeyConstant.PLAT_LIST_HASH, platId.toString(), JSON.toJSONString(one));
        }
        return one;
    }

    /**
     * 获取游戏列表配置信息
     *
     * @return 平台配置信息
     */
    public GameList getGameListCache(@NotNull Integer gameId) {
        String data = jedisUtil.hget(KeyConstant.GAME_LIST_HASH, gameId.toString());
        if (StringUtils.isNotBlank(data)) {
            return JSON.parseObject(data).toJavaObject(GameList.class);
        }

        GameList one = gameListServiceImpl.lambdaQuery().eq(GameList::getId, gameId).one();

        if (one != null) {
            jedisUtil.hset(KeyConstant.GAME_LIST_HASH, gameId.toString(), JSON.toJSONString(one));
        }
        return one;
    }

    /**
     * 获取老虎机游戏列表
     */
    public GameSlot getSlotGameCache(@NotNull String id) {
        String data = jedisUtil.hget(KeyConstant.GAME_SLOT_LIST_HASH, id);
        if (StringUtils.isNotBlank(data)) {
            return JSON.parseObject(data).toJavaObject(GameSlot.class);
        }

        GameSlot one = gameSlotServiceImpl.lambdaQuery().eq(GameSlot::getId, id).one();

        if (one != null) {
            jedisUtil.hset(KeyConstant.GAME_SLOT_LIST_HASH, id, JSON.toJSONString(one));
        }

        return one;
    }

    public void updateGroupGameListCache() {
        jedisUtil.del(KeyConstant.PLATFORM_HASH);
    }

    public void updateGameListCache(Integer id) {
        jedisUtil.hdel(KeyConstant.GAME_LIST_HASH, id.toString());
        getGameListCache(id);
    }

    public void delGameListCache() {
        jedisUtil.hdel(KeyConstant.PLATFORM_HASH, KeyConstant.PLATFORM_HASH_GAME_LIST);
        Set<String> gameList = jedisUtil.hkeys(KeyConstant.GAME_LIST_HASH);
        for (String g : gameList) {
            jedisUtil.hdel(KeyConstant.GAME_LIST_HASH, g);
        }

    }

    /**
     * 获取游戏组列表
     *
     * @return 游戏组列表
     */
    public List<GameGroup> getGameGroupCache() {
        String key = KeyConstant.PLATFORM_HASH;
        String subKey = KeyConstant.PLATFORM_HASH_GAME_GROUP_LIST;

        String data = jedisUtil.hget(key, subKey);
        if (StringUtils.isNotBlank(data)) {
            return JSON.parseArray(data, GameGroup.class);
        }
        List<GameGroup> collect = gameGroupServiceImpl.lambdaQuery()
                .eq(GameGroup::getStatus, 1)
                .orderByDesc(GameGroup::getSort)
                .orderByDesc(GameGroup::getId)
                .list();

        if (!collect.isEmpty()) {
            jedisUtil.hset(key, subKey, JSON.toJSONString(collect));
        }
        return collect;
    }

    /**
     * HB哈巴游戏列表
     *
     * @return HB哈巴游戏列表
     */
    public List<GameSlot> getGameSlotListCache() {
        String key = KeyConstant.PLATFORM_HASH;
        String subKey = KeyConstant.GAME_SLOT_HASH;

        String data = jedisUtil.hget(key, subKey);
        if (StringUtils.isNotBlank(data)) {
            return JSON.parseArray(data, GameSlot.class);
        }

        List<GameSlot> gameSlotList = gameSlotServiceImpl.lambdaQuery().eq(GameSlot::getStatus, 1).list();
        if (Optional.ofNullable(gameSlotList).isPresent()) {
            jedisUtil.hset(key, subKey, JSON.toJSONString(gameSlotList));
        }
        return gameSlotList;
    }


    public void delGamePropCache(Integer id) {
        jedisUtil.hdel(KeyConstant.GAME_PROP_HASH, id.toString());
    }
}