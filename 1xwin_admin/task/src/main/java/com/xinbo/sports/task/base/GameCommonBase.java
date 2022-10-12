package com.xinbo.sports.task.base;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xinbo.sports.dao.generator.po.GameList;
import com.xinbo.sports.dao.generator.po.PlatList;
import com.xinbo.sports.dao.generator.service.GameListService;
import com.xinbo.sports.dao.generator.service.PlatListService;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams.PlatGameQueryDateDto;
import com.xinbo.sports.plat.io.enums.BasePlatParam;
import com.xinbo.sports.service.cache.KeyConstant;
import com.xinbo.sports.service.common.Constant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.utils.JedisUtil;
import com.xinbo.sports.utils.SpringUtils;
import com.xinbo.sports.utils.components.response.CodeInfo;
import com.xxl.job.core.log.XxlJobLogger;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.alibaba.fastjson.JSON.parseObject;
import static java.util.Objects.nonNull;

/**
 * @author: wells
 * @date: 2020/6/12
 * @description:
 */
@Component
public class GameCommonBase {
    /*投注金额字段*/
    public static final String BET_FIELD = "xb_valid_coin";
    /*创建时间*/
    public static final String CREATED_AT = "created_at";
    /*注单表service实例表前缀*/
    public static final String BET_PREFIX = "betslips";
    /*注单表service实例表后缀*/
    public static final String BET_SUFFIX = "ServiceImpl";
    /*用户id对应的数据库字段*/
    public static final String XB_UID = "xb_uid";
    @Autowired
    private GameListService gameListServiceImpl;
    @Autowired
    private PlatListService platListServiceImpl;
    @Autowired
    private JedisUtil jedisUtil;

    /**
     * 获取某游戏某平台的某些用户的有效投注金额
     */
    public Map<Integer, BigDecimal> getUserBetCoin(PlatGameQueryDateDto reqDto, Set<Integer> uidSet) {
        //获取游戏的基本配置信息
        var gameLists = getConfig(reqDto);
        //uid对应的有效投注金额
        Map<Integer, BigDecimal> userBetCoinMap = new HashMap<>();
        var winHalf = BasePlatParam.BetRecordsStatus.WIN_HALF.getCode();
        var loseHalf = BasePlatParam.BetRecordsStatus.LOSE_HALF.getCode();
        for (var game : gameLists) {
            var configJsonObject = parseObject(game.getModel());
            var betslips = BET_PREFIX + configJsonObject.getString(BET_PREFIX) + BET_SUFFIX;
            var iService = (IService) SpringUtils.getBean(betslips);
            var selectStr = "";
            //全场返水，当状态为输半赢半时，只计算一半的流水
            if (Objects.nonNull(reqDto.getPromotionsId()) && Constant.REBATE_ALL_GAMES.equals(reqDto.getPromotionsId())) {
                selectStr = XB_UID + ",ifNull(sum(if(xb_status =" + winHalf + " or xb_status=" + loseHalf + "  , " + BET_FIELD + "/2 ," + BET_FIELD + ") ),0)as  " + BET_FIELD;
            } else {
                selectStr = XB_UID + ",ifNull(sum(" + BET_FIELD + "),0 )as  " + BET_FIELD;
            }
            //有效投注金额字段
            var queryWrapper = new QueryWrapper<>()
                    .select(selectStr)
                    .ge(nonNull(reqDto.getStartTime()), CREATED_AT, reqDto.getStartTime())
                    .lt(nonNull(reqDto.getEndTime()), CREATED_AT, reqDto.getEndTime());
            if (CollectionUtils.isEmpty(uidSet)) {
                queryWrapper.eq(nonNull(reqDto.getUid()), XB_UID, reqDto.getUid());
            } else {
                queryWrapper.in(XB_UID, uidSet);
            }
            queryWrapper.groupBy(XB_UID);
            List<Map<String, Object>> reList = iService.listMaps(queryWrapper);
            if (!CollectionUtils.isEmpty(reList)) {
                XxlJobLogger.log(game.getName() + "reList=" + reList);
                reList.forEach(map -> {
                    var uid = Integer.valueOf(map.getOrDefault(XB_UID, 0).toString());
                    var coinBet = new BigDecimal(map.getOrDefault(BET_FIELD, BigDecimal.ZERO).toString());
                    if (nonNull(userBetCoinMap.get(uid))) {
                        userBetCoinMap.put(uid, userBetCoinMap.get(uid).add(coinBet));
                    } else {
                        userBetCoinMap.put(uid, coinBet);
                    }
                });
            }
        }
        return userBetCoinMap;
    }

    /**
     * 获取某游戏某平台的所有用户或单个的有效投注金额
     */
    public Map<Integer, BigDecimal> getUserBetCoin(PlatGameQueryDateDto reqDto) {
        return getUserBetCoin(reqDto, null);
    }

    /**
     * 获取游戏的基本配置信息
     */
    public List<GameList> getConfig(PlatGameQueryDateDto reqDto) {
        var gameLists = gameListServiceImpl.lambdaQuery()
                .eq(nonNull(reqDto.getGameId()), GameList::getId, reqDto.getGameId())
                .eq(nonNull(reqDto.getPlatId()), GameList::getGroupId, reqDto.getPlatId())
                .eq(GameList::getStatus, 1)
                .list();
        //判断游戏是否存在
        if (CollectionUtils.isEmpty(gameLists)) {
            throw new BusinessException(CodeInfo.GAME_RECORDS_EMPTY);
        }
        return gameLists;
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
     * 获取游戏列表配置信息
     *
     * @return 平台配置信息
     */
    public List<GameList> getGameListCacheAll() {
        var map = jedisUtil.hgetall(KeyConstant.GAME_LIST_HASH);
        if (!CollectionUtils.isEmpty(map)) {
            return map.values().stream().map(game -> JSON.parseObject(game).toJavaObject(GameList.class)).collect(Collectors.toList());
        }
        var list = gameListServiceImpl.lambdaQuery().list();
        if (!CollectionUtils.isEmpty(list)) {
            list.forEach(game -> jedisUtil.hset(KeyConstant.GAME_LIST_HASH, String.valueOf(game.getId()), JSON.toJSONString(game)));
        }
        return list;
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
}
