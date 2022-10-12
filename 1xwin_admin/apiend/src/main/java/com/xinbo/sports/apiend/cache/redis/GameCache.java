package com.xinbo.sports.apiend.cache.redis;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xinbo.sports.apiend.io.bo.HomeParams.GameIndexResDto;
import com.xinbo.sports.apiend.io.bo.PlatformParams.PlatListResInfo;
import com.xinbo.sports.apiend.io.dto.platform.GameListDto;
import com.xinbo.sports.apiend.io.dto.platform.GameListInfo;
import com.xinbo.sports.apiend.io.dto.platform.GameModelDto;
import com.xinbo.sports.apiend.mapper.GamePropMapper;
import com.xinbo.sports.dao.generator.po.GameGroup;
import com.xinbo.sports.dao.generator.po.GameList;
import com.xinbo.sports.dao.generator.po.PlatList;
import com.xinbo.sports.dao.generator.po.UserLevelRebate;
import com.xinbo.sports.dao.generator.service.GameGroupService;
import com.xinbo.sports.dao.generator.service.GameListService;
import com.xinbo.sports.dao.generator.service.PlatListService;
import com.xinbo.sports.dao.generator.service.UserLevelRebateService;
import com.xinbo.sports.service.cache.KeyConstant;
import com.xinbo.sports.utils.BeanConvertUtils;
import com.xinbo.sports.utils.JedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.alibaba.fastjson.JSON.parseObject;


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
    private final GameGroupService gameGroupServiceImpl;
    private final GameListService gameListServiceImpl;
    private final UserLevelRebateService userLevelRebateServiceImpl;
    private final PlatListService platListServiceImpl;
    private final GamePropMapper gamePropMapper;

    /**
     * 获取平台列表(返回API)
     *
     * @return 平台列表
     */
    public List<PlatListResInfo> getPlatListResCache() {
        String key = KeyConstant.PLATFORM_HASH;
        String subKey = KeyConstant.PLATFORM_HASH_PLAT_LIST;
        String value = jedisUtil.hget(key, subKey);
        if (StringUtils.isNotBlank(value)) {
            return JSON.parseArray(value, PlatListResInfo.class);
        }

        List<PlatListResInfo> collect = platListServiceImpl.lambdaQuery()
                .eq(PlatList::getStatus, 1)
                .orderByDesc(PlatList::getSort)
                .orderByDesc(PlatList::getId)
                .list()
                .stream()
                .map(o -> BeanConvertUtils.beanCopy(o, PlatListResInfo::new))
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
    public List<GameListInfo> getGameListResCache() {
        String key = KeyConstant.PLATFORM_HASH;
        String subKey = KeyConstant.PLATFORM_HASH_GAME_LIST;

        String data = jedisUtil.hget(key, subKey);
        if (StringUtils.isNotBlank(data)) {
            return JSON.parseArray(data, GameListInfo.class);
        }
        List<GameListInfo> collect = gameListServiceImpl.lambdaQuery()
                .eq(GameList::getStatus, 1)
                .orderByDesc(GameList::getSort)
                .orderByDesc(GameList::getId)
                .list()
                .stream()
                .map(o -> BeanConvertUtils.beanCopy(o, GameListInfo::new))
                .collect(Collectors.toList());

        if (!collect.isEmpty()) {
            jedisUtil.hset(key, subKey, JSON.toJSONString(collect));
        }
        return collect;
    }

    /**
     * 获取游戏组-游戏列表
     *
     * @return 游戏组-游戏列表
     */
    public List<GameIndexResDto> getGroupGameListCache() {
        String key = KeyConstant.PLATFORM_HASH;
        String subKey = KeyConstant.PLATFORM_HASH_GROUP_GAME_LIST;

        String value = jedisUtil.hget(key, subKey);
        if (StringUtils.isNotBlank(value)) {
            return JSON.parseArray(value, GameIndexResDto.class);
        }

        // 游戏组
        List<GameGroup> gameGroup = gameGroupServiceImpl.lambdaQuery().select(GameGroup::getId, GameGroup::getName, GameGroup::getNameAbbr).eq(GameGroup::getStatus, 1).orderByDesc(GameGroup::getSort).orderByDesc(GameGroup::getId).list();
        // 获取最高反水
        QueryWrapper<UserLevelRebate> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("max(rebate_rate) as rebateRate", "group_id as groupId")
                .eq("status", 1)
                .groupBy("group_id");
        Map<Integer, BigDecimal> rebateCollect = userLevelRebateServiceImpl.list(queryWrapper)
                .stream()
                .collect(Collectors.toMap(UserLevelRebate::getGroupId, UserLevelRebate::getRebateRate));
        // 游戏列表
        List<GameList> gameList = gameListServiceImpl.lambdaQuery()
                .select(GameList::getId, GameList::getName, GameList::getGroupId, GameList::getModel, GameList::getStatus,GameList::getMaintenance)
                .in(GameList::getStatus, 1, 2)
                .orderByDesc(GameList::getSort)
                .orderByDesc(GameList::getId)
                .list();

        // 合并游戏至游戏组
        Map<Integer, List<GameListDto>> mapGroup = gameList.stream().collect(Collectors.toMap(
                GameList::getGroupId,
                game -> {
                    List<GameListDto> gameListDtolist = new ArrayList<>();
                    GameListDto gameListDto = BeanConvertUtils.copyProperties(game, GameListDto::new, (bo, sb) -> {
                        sb.setSupportIframe(parseObject(bo.getModel()).getBooleanValue("iframe"));
                        sb.setMaintenance(JSON.parseObject(bo.getMaintenance()));
                    });
                    gameListDtolist.add(gameListDto);
                    return gameListDtolist;
                },
                //  解决 MapKey 的冲突
                (List<GameListDto> existing, List<GameListDto> replacement) -> {
                    existing.addAll(replacement);
                    return existing;
                }
        ));

        // 过滤掉没有子游戏项目的项目组
        List<GameIndexResDto> collect = gameGroup.stream()
                .filter(gameGroup1 -> mapGroup.get(gameGroup1.getId()) != null)
                .map(o -> {
                    GameIndexResDto gameIndexRes = BeanConvertUtils.copyProperties(
                            o,
                            GameIndexResDto::new,
                            (game, gameIndexResDto) -> gameIndexResDto.setList(mapGroup.get(game.getId())));

                    // 设置游戏种类的最高反水
                    BigDecimal rebate = rebateCollect.getOrDefault(gameIndexRes.getId(), BigDecimal.ZERO);
                    gameIndexRes.setRebate(rebate.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.DOWN).toString());

                    return gameIndexRes;
                })
                .collect(Collectors.toList());

        if (!collect.isEmpty()) {
            jedisUtil.hset(key, subKey, JSON.toJSONString(collect));
        }
        return collect;
    }

    /**
     * 获取游戏属性(返回API)
     *
     * @return 游戏属性列表
     */
    public GameModelDto getGamePropCache(@NotNull Integer gameId) {
        String data = jedisUtil.hget(KeyConstant.GAME_PROP_HASH, gameId.toString());
        if (StringUtils.isNotBlank(data)) {
            return parseObject(data).toJavaObject(GameModelDto.class);
        }

        GameModelDto collect = gamePropMapper.getGameProp(gameId);
        if (collect != null) {
            jedisUtil.hset(KeyConstant.GAME_PROP_HASH, gameId.toString(), JSON.toJSONString(collect));
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
            return parseObject(data).toJavaObject(PlatList.class);
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
            return parseObject(data).toJavaObject(GameList.class);
        }

        GameList one = gameListServiceImpl.lambdaQuery().eq(GameList::getId, gameId).one();

        if (one != null) {
            jedisUtil.hset(KeyConstant.GAME_LIST_HASH, gameId.toString(), JSON.toJSONString(one));
        }
        return one;
    }

    public List<GameModelDto> getGamePropListCache(Integer gameId) {
        return gamePropMapper.getGamePropList(gameId);
    }
}