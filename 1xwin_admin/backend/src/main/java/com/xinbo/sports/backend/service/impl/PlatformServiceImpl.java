package com.xinbo.sports.backend.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.google.common.collect.Lists;
import com.xinbo.sports.backend.base.DictionaryBase;
import com.xinbo.sports.backend.io.dto.Platform;
import com.xinbo.sports.backend.redis.GameCache;
import com.xinbo.sports.backend.service.IAdminInfoBase;
import com.xinbo.sports.backend.service.IPlatformService;
import com.xinbo.sports.dao.generator.po.*;
import com.xinbo.sports.dao.generator.service.BetSlipsExceptionService;
import com.xinbo.sports.dao.generator.service.BetSlipsSupplementalService;
import com.xinbo.sports.dao.generator.service.CoinPlatTransferService;
import com.xinbo.sports.plat.factory.PlatAbstractFactory;
import com.xinbo.sports.plat.factory.PlatSlotAbstractFactory;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams;
import com.xinbo.sports.plat.service.impl.sbo.SBOSportsServiceImpl;
import com.xinbo.sports.service.base.PlatServiceBase;
import com.xinbo.sports.service.base.UserServiceBase;
import com.xinbo.sports.service.cache.redis.UserCache;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.bo.GameCommonBo;
import com.xinbo.sports.service.io.bo.UserCacheBo;
import com.xinbo.sports.service.io.constant.ConstData;
import com.xinbo.sports.service.io.enums.BaseEnum;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.xinbo.sports.plat.io.bo.PlatFactoryParams.PlatQueryBalanceReqDto;
import static com.xinbo.sports.plat.io.bo.PlatFactoryParams.PlatQueryBalanceResDto;
import static com.xinbo.sports.service.io.dto.BaseParams.HeaderInfo;

/**
 * @author: David
 * @date: 06/04/2020
 * @description:
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PlatformServiceImpl implements IPlatformService {
    private final UserServiceBase userServiceBase;
    private final SBOSportsServiceImpl sboSportsServiceImpl;
    private final CoinPlatTransferService coinPlatTransferServiceImpl;
    private final BetSlipsSupplementalService betSlipsSupplementalServiceImpl;
    private final BetSlipsExceptionService betSlipsExceptionServiceImpl;
    private final GameCache gameCache;
    private final UserCache userCache;
    private final PlatServiceBase platServiceBase;
    private final DictionaryBase dictionaryBase;

    /**
     * 游戏列表
     *
     * @return 游戏列表
     */
    @Override
    public List<Platform.GameListInfo> gameList(Platform.GameListReqDto dto) {
        List<Platform.GameListInfo> gameListResCache = gameCache.getGameListResCache();
        if (dto.getGroupId() == 0) {
            return gameListResCache;
        } else {
            return gameListResCache.stream().filter(gameListInfo -> gameListInfo.getGroupId().equals(dto.getGroupId())).collect(Collectors.toList());
        }
    }

    /**
     * 游戏平台列表
     *
     * @return 平台列表
     */
    @Override
    public List<Platform.PlatListResInfo> platList() {
        return gameCache.getPlatListResCache();
    }


    /**
     * 账户余额查询
     *
     * @param dto 游戏ID
     * @return 游戏余额、平台余额
     */
    @Override
    public Platform.GameBalanceResDto queryBalanceByPlatId(Platform.GameBalanceReqDto dto) {
        UserCacheBo.UserCacheInfo userCacheInfo = userCache.getUserInfoById(dto.getUid());
        if (null == userCacheInfo) {
            throw new BusinessException(CodeInfo.USER_NOT_EXISTS);
        }
        HeaderInfo headLocalData = IAdminInfoBase.getHeadLocalData();
        try {
            // 查询余额用父类查询即可
            PlatAbstractFactory config = platFactoryByPlatId(dto.getId());
            PlatQueryBalanceResDto platQueryBalanceResDto = config.queryBalance(
                    PlatQueryBalanceReqDto.builder()
                            .username(userServiceBase.buildUsername(userCacheInfo.getUsername()))
                            .lang(headLocalData.getLang())
                            .build()
            );

            return Platform.GameBalanceResDto.builder()
                    .coin(platQueryBalanceResDto.getPlatCoin())
                    .id(dto.getId())
                    .uid(dto.getUid())
                    .build();
        } catch (BusinessException e) {
            log.error(e.toString());
            if (e.getCode().equals(CodeInfo.PLAT_ACCOUNT_NOT_EXISTS.getCode())) {
                return Platform.GameBalanceResDto.builder()
                        .coin(BigDecimal.ZERO)
                        .id(dto.getId())
                        .uid(dto.getUid())
                        .build();
            }
            throw e;
        } catch (Exception e) {
            log.error(e.toString());
            throw new BusinessException(CodeInfo.COIN_QUERY_INVALID);
        }
    }

    /**
     * 初始化SBO代理信息
     *
     * @return true-成功 false-失败
     */
    @Override
    public Boolean createAgentSbo() {
        platFactory(101);
        return sboSportsServiceImpl.registerAgent();
    }

    /**
     * 上、下分状态异常补单
     *
     * @param dto 订单号
     * @return true-成功 false-失败
     */
    @Override
    public Boolean checkTransferStatus(Platform.CheckTransferStatusReqDto dto) {
        CoinPlatTransfer record = coinPlatTransferServiceImpl.getById(dto.getOrderId());
        if (record == null) {
            throw new BusinessException(CodeInfo.PLAT_TRANSFER_ID_INVALID);
        }

        try {
            // 查询余额用父类查询即可
            PlatAbstractFactory config = platFactoryByPlatId(record.getPlatListId());

            Boolean result = config.checkTransferStatus(record.getId() + "");
            platServiceBase.updateCoinPlatTransferStatusById(record, result);
        } catch (UnsupportedOperationException e) {
            throw new BusinessException(CodeInfo.PLAT_UNSUPPORTED_OPERATION);
        } catch (Exception e) {
            log.error(e.toString());
            throw new BusinessException(CodeInfo.PLAT_TRANSFER_SUPPLE_INVALID);
        }

        return true;
    }

    /**
     * 拉单补单
     *
     * @param dto 游戏ID、开始～结束时间
     * @return True-成功 False-失败
     */
    @Override
    public Boolean genBetSlipsSupplemental(Platform.GenBetSlipsSupplementalReqDto dto) {
        // 通过游戏ID 生成补单信息
        PlatAbstractFactory config = platFactory(dto.getGameId());
        PlatFactoryParams.GenSupplementsOrdersReqDto build = PlatFactoryParams.GenSupplementsOrdersReqDto.builder()
                .gameId(dto.getGameId())
                .start(dto.getStartTime())
                .end(dto.getEndTime())
                .build();
        config.genSupplementsOrders(build);
        return true;
    }

    /**
     * 拉单补单
     *
     * @param dto 补单订单号
     * @return True-成功 False-失败
     */
    @Override
    public Boolean betSlipsSupplemental(@NotNull Platform.BetSlipsSupplementalReqDto dto) {
        BetSlipsSupplemental order = betSlipsSupplementalServiceImpl.getById(dto.getId());
        if (null == order) {
            throw new BusinessException(CodeInfo.ORDER_ID_INVALID);
        }
        // 通过游戏ID 生成补单信息
        PlatAbstractFactory config = platFactory(order.getGameListId());
        PlatFactoryParams.BetsRecordsSupplementReqDto build = PlatFactoryParams.BetsRecordsSupplementReqDto.builder()
                .requestInfo(order.getRequest())
                .build();
        config.betsRecordsSupplemental(build);

        // 更新状态:0-未处理 1-处理成功 2-处理失败
        betSlipsSupplementalServiceImpl.lambdaUpdate()
                .set(BetSlipsSupplemental::getStatus, 1)
                .set(BetSlipsSupplemental::getUpdatedAt, DateNewUtils.now())
                .eq(BetSlipsSupplemental::getId, order.getId())
                .update();
        return true;
    }

    /**
     * 根据GameID 获取游戏处理工厂类
     *
     * @param gameId 游戏ID
     * @param <T>    范型
     * @return 游戏工厂/老虎机工厂
     * @author David
     * @date 07/05/2020
     */
    @Override
    public <T> T platFactory(Integer gameId) {
        GameList game = gameCache.getGameListCache(gameId);
        if (game == null) {
            throw new BusinessException(CodeInfo.CONFIG_INVALID);
        }
        GameCommonBo.GameConfig config = JSON.parseObject(game.getModel()).toJavaObject(GameCommonBo.GameConfig.class);

        T factory;
        if (game.getGroupId().equals(ConstData.SLOT_GAME_CATEGORY)) {
            // 老虎机游戏专用
            factory = (T) PlatSlotAbstractFactory.init(config.getModel(), config.getParent());
        } else {
            factory = (T) PlatAbstractFactory.init(config.getModel(), config.getParent());
        }

        if (null == factory) {
            throw new BusinessException(CodeInfo.PLAT_FACTORY_NOT_EXISTS);
        }

        return factory;
    }

    /**
     * 根据 PlatId 获取游戏父工厂类
     *
     * @param platId 平台ID
     * @return 游戏工厂父类
     * @author David
     * @date 07/05/2020
     */
    @Override
    public PlatAbstractFactory platFactoryByPlatId(Integer platId) {
        PlatList one = gameCache.getPlatListCache(platId);
        if (null == one || one.getStatus() != 1) {
            throw new BusinessException(CodeInfo.PLAT_FACTORY_NOT_EXISTS);
        }

        PlatAbstractFactory platAbstractFactory = PlatAbstractFactory.initByPlat(one.getName());
        if (null == platAbstractFactory) {
            throw new BusinessException(CodeInfo.PLAT_FACTORY_NOT_EXISTS);
        }

        return platAbstractFactory;
    }

    /**
     * 拉单异常补单
     *
     * @param dto 补单订单号
     * @return True-成功 False-失败
     */
    @Override
    public Boolean betSlipsException(Platform.BetSlipsExceptionReqDto dto) {
        BetSlipsException order = betSlipsExceptionServiceImpl.getById(dto.getId());
        if (null == order) {
            throw new BusinessException(CodeInfo.ORDER_ID_INVALID);
        }
        // 通过游戏ID 生成补单信息
        PlatAbstractFactory config = platFactory(order.getGameListId());
        PlatFactoryParams.BetsRecordsSupplementReqDto build = PlatFactoryParams.BetsRecordsSupplementReqDto.builder()
                .requestInfo(order.getRequest())
                .build();
        config.betSlipsExceptionPull(build);

        // 更新状态:0-未处理 1-处理成功 2-处理失败
        betSlipsExceptionServiceImpl.lambdaUpdate()
                .set(BetSlipsException::getStatus, 1)
                .set(BetSlipsException::getUpdatedAt, DateNewUtils.now())
                .eq(BetSlipsException::getId, order.getId())
                .update();

        return true;
    }

    /**
     * 同步老虎机游戏列表
     *
     * @param dto 补单订单号
     * @return True-成功 False-失败
     */
    @Override
    public Boolean syncSlotGameList(Platform.SyncSlotGameReqDto dto) {
        PlatSlotAbstractFactory config = platFactory(dto.getGameId());
        config.pullGames();

        return true;
    }

    /**
     * 游戏类型->平台名称->游戏名称->三级联动
     *
     * @return 三级联动列表
     */
    @Override
    public List<Platform.GameGroupDict> getCascadeByGameGroupList() {
        // 当前登录账号
        var currentAdmin = IAdminInfoBase.getHeadLocalData();
        // 一级:游戏类型list
        var gameGroupCache = gameCache.getGameGroupCache();
        // 二级:平台名称list
        var gameListCache = gameCache.getGameListResCache();
        // 三级:游戏名称list
        var gameSlotListCache = gameCache.getGameSlotListCache();

        // 返回
        return gameGroupCache.stream().map(gameGroup -> {
            var groupId = gameGroup.getId();
            // 一级:游戏类型
            var gameGroupDict = Platform.GameGroupDict.builder().groupId(groupId).groupName(gameGroup.getNameAbbr()).build();

            // 二级:平台名称list
            var gameListDictList = gameListCache.stream()
                    .filter(x -> x.getGroupId().equals(groupId))
                    .map(gameListCacheEntity -> {
                        var gameListDict = Platform.GameListDict.builder().gameListId(gameListCacheEntity.getId()).gameListName(gameListCacheEntity.getName()).build();

                        // 三级:游戏名称list
                        List<Platform.GameListSubDict> subDictList;

                        // 游戏列表(老虎机)
                        if (2 == gameListCacheEntity.getGroupId() && Optional.ofNullable(gameSlotListCache).isPresent() && !gameSlotListCache.isEmpty()) {
                            subDictList = getGameSlotListByLang(gameSlotListCache, currentAdmin.getLang());
                        } else {
                            subDictList = getGameSubListByModel(gameListCacheEntity.getModel());
                        }
                        // 关联游戏名称list
                        gameListDict.setList(Optional.ofNullable(subDictList).isEmpty() ? Lists.newArrayList() : subDictList);
                        return gameListDict;
                    })
                    .collect(Collectors.toList());

            // 关联平台名称list(二级)
            gameGroupDict.setList(gameListDictList);
            return gameGroupDict;
        }).collect(Collectors.toList());
    }

    /**
     * 获取游戏名称list-老虎机
     *
     * @param gameSlotList 老虎机列表
     * @param lang         语言
     * @return List<Platform.GameListSubDict>
     */
    private List<Platform.GameListSubDict> getGameSlotListByLang(List<GameSlot> gameSlotList, String lang) {
        List<Platform.GameListSubDict> list = new ArrayList<>();
        if (lang.equals(BaseEnum.LANG.ZH.getValue())) {
            for (var o : gameSlotList) {
                if (null != o && StringUtils.isNotBlank(o.getId()) && StringUtils.isNotBlank(o.getNameZh())) {
                    list.add(Platform.GameListSubDict.builder().gameId(o.getId()).gameName(o.getNameZh()).build());
                }
            }
        } else {
            for (var o : gameSlotList) {
                if (null != o && StringUtils.isNotBlank(o.getId()) && StringUtils.isNotBlank(o.getName())) {
                    list.add(Platform.GameListSubDict.builder().gameId(o.getId()).gameName(o.getName()).build());
                }
            }
        }
        return list;
    }

    /**
     * 获取游戏名称ListByDictCategory
     *
     * @param model model
     * @return List<Platform.GameListSubDict>
     */
    private List<Platform.GameListSubDict> getGameSubListByModel(String model) {
        List<Platform.GameListSubDict> gameListSubDictList = new ArrayList<>();
        // 游戏列表
        var dictItemMap = dictionaryBase.getDictItemMapByModel(model);
        if (Optional.ofNullable(dictItemMap).isPresent() && !dictItemMap.isEmpty()) {
            // 字典码->字典名称
            dictItemMap.forEach((code, title) -> {
                if (StringUtils.isNotBlank(code) && StringUtils.isNotBlank(title)) {
                    gameListSubDictList.add(Platform.GameListSubDict.builder()
                            .gameId(code)
                            .gameName(title)
                            .build());
                }
            });
        }
        return gameListSubDictList;
    }
}
