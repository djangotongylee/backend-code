package com.xinbo.sports.apiend.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinbo.sports.apiend.base.GameCommonBase;
import com.xinbo.sports.apiend.base.ThreadPoolExecutorFactory;
import com.xinbo.sports.apiend.cache.redis.GameCache;
import com.xinbo.sports.apiend.io.bo.HomeParams;
import com.xinbo.sports.apiend.io.bo.PlatformParams.PlatListResInfo;
import com.xinbo.sports.apiend.io.dto.StartEndTime;
import com.xinbo.sports.apiend.io.dto.platform.*;
import com.xinbo.sports.apiend.service.IGameService;
import com.xinbo.sports.apiend.service.IUserInfoService;
import com.xinbo.sports.dao.generator.po.*;
import com.xinbo.sports.dao.generator.service.CoinPlatTransferService;
import com.xinbo.sports.dao.generator.service.UserLoginLogService;
import com.xinbo.sports.dao.generator.service.UserService;
import com.xinbo.sports.dao.generator.service.impl.GameListServiceImpl;
import com.xinbo.sports.plat.factory.IVerificationMethods;
import com.xinbo.sports.plat.factory.PlatAbstractFactory;
import com.xinbo.sports.plat.factory.PlatSlotAbstractFactory;
import com.xinbo.sports.plat.factory.PlatSportsAbstractFactory;
import com.xinbo.sports.plat.io.dto.base.BetslipsDetailDto;
import com.xinbo.sports.plat.service.impl.bti.BTISportsServiceImpl;
import com.xinbo.sports.plat.service.impl.ebet.EBETLiveServiceImpl;
import com.xinbo.sports.plat.service.impl.pg.PGChessServiceImpl;
import com.xinbo.sports.service.base.PlatServiceBase;
import com.xinbo.sports.service.base.UserServiceBase;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.constant.ConstData;
import com.xinbo.sports.service.io.dto.BaseParams;
import com.xinbo.sports.service.io.dto.UserInfo;
import com.xinbo.sports.service.thread.ThreadHeaderLocalData;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.IpUtil;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.xinbo.sports.apiend.io.bo.PlatformParams.GameQueryDateDto;
import static com.xinbo.sports.apiend.io.bo.PlatformParams.PlatTransferResInfo;
import static com.xinbo.sports.plat.io.bo.PlatFactoryParams.*;
import static com.xinbo.sports.service.io.bo.PlatServiceBaseParams.InsertCoinPlatRecordsReqDto;
import static com.xinbo.sports.service.io.bo.PlatServiceBaseParams.InsertCoinPlatRecordsResDto;
import static com.xinbo.sports.service.io.dto.BaseParams.HeaderInfo;
import static java.util.Objects.nonNull;

/**
 * @author: David
 * @date: 06/04/2020
 * @description:
 */
@Service
@Slf4j
public class GameServiceImpl implements IGameService {
    public static final String MODEL = "model";
    @Resource
    GameListServiceImpl gameListServiceImpl;
    @Resource
    UserService userServiceImpl;
    @Resource
    PlatServiceBase platServiceBase;
    @Resource
    IUserInfoService userInfoServiceImpl;
    @Resource
    CoinPlatTransferService coinPlatTransferServiceImpl;
    @Resource
    UserServiceBase userServiceBase;
    @Resource
    private GameCommonBase gameCommonBase;
    @Resource
    private GameCache gameCache;
    @Resource
    private UserLoginLogService userLoginLogServiceImpl;
    @Resource
    private HttpServletRequest httpServletRequest;
    @Resource
    private PGChessServiceImpl pgChessServiceImpl;
    @Resource
    private BTISportsServiceImpl btiSportsServiceImpl;
    @Resource
    private EBETLiveServiceImpl ebetLiveServiceImpl;

    /**
     * ??????????????????
     *
     * @return ????????????
     */
    @Override
    public List<HomeParams.GameIndexResDto> list() {
        return gameCache.getGroupGameListCache();
    }


    /**
     * ????????????
     *
     * @return ????????????
     */
    @Override
    public List<GameListInfo> gameList() {
        return gameCache.getGameListResCache();
    }

    /**
     * ??????????????????
     *
     * @return ????????????
     */
    @Override
    public List<PlatListResInfo> platList() {
        return gameCache.getPlatListResCache();
    }

    /**
     * ??????????????????
     *
     * @param dto {id, slotId}
     * @return ????????????
     */
    @Override
    public PlatGameLoginResDto login(@NotNull GameLoginReqDto dto) {
        HeaderInfo headerInfo = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        UserInfo userInfo = userInfoServiceImpl.findIdentityByApiToken();

        PlatLoginReqDto build = PlatLoginReqDto.builder()
                .device(headerInfo.getDevice())
                .lang(headerInfo.getLang())
                .username(userServiceBase.buildUsername(headerInfo.getUsername().toLowerCase()))
                .slotId(dto.getSlotId())
                .build();
        try {
            GameList gameListCache = gameCache.getGameListCache(dto.getId());
            if (gameListCache.getStatus() == 2) {
                CodeInfo.PLAT_GAME_UNDER_MAINTENANCE.setMsg(gameListCache.getMaintenance());
                throw new BusinessException(CodeInfo.PLAT_GAME_UNDER_MAINTENANCE);
            }
            PlatAbstractFactory config = platFactory(dto.getId());
            if (userInfo.getRole() == 4 && dto.getId() != 703) {
                throw new BusinessException(CodeInfo.PLAT_NO_AUTHORITIES_TO_ACCESS);
            }
            PlatGameLoginResDto login = config.login(build);
            if (login.getUrl().isBlank()) {
                throw new BusinessException(CodeInfo.LOGIN_INVALID);
            }
            // ??????????????????
            String device = headerInfo.getDevice();
            Integer uid = headerInfo.getId();
            String username = headerInfo.getUsername();
            String ip = IpUtil.getIp(httpServletRequest);
            String gameName = Optional.ofNullable(gameCache.getGameListCache(dto.getId())).isPresent() ? gameCache.getGameListCache(dto.getId()).getName() : "";
            int now = DateNewUtils.now();
            ThreadPoolExecutorFactory.THREAD_POOL.submit(() -> {
                UserLoginLog userLoginLog = new UserLoginLog();
                userLoginLog.setUid(uid);
                userLoginLog.setUsername(username);
                userLoginLog.setUpdatedAt(now);
                userLoginLog.setCreatedAt(now);
                userLoginLog.setDevice(device);
                userLoginLog.setIp(ip);
                userLoginLog.setGameName(gameName);
                // ??????:0-?????? 1-?????? 2-????????????
                userLoginLog.setCategory(2);
                userLoginLog.setCoin(userServiceImpl.getById(uid).getCoin());
                userLoginLogServiceImpl.save(userLoginLog);
            });
            var userCoin = userServiceImpl.getById(headerInfo.getId()).getCoin();
            // ??????????????????????????????????????? ??????????????????1
            if (userInfo.getAutoTransfer().equals(1) && userInfo.getCoin().compareTo(BigDecimal.ONE) >= 0) {
                var transferReqDto = new CoinTransferReqDto();
                transferReqDto.setId(gameCache.getGameListCache(dto.getId()).getPlatListId());
                //??????:0-?????? 1-??????
                transferReqDto.setDirection(0);
                transferReqDto.setCoin(userInfo.getCoin());
                transferReqDto.setName(headerInfo.getUsername());
                CoinTransferResDto coinTransferResDto = coinTransfer(transferReqDto);
                userCoin = coinTransferResDto.getCoin();
            }
            login.setCoin(userCoin);
            return login;
        } catch (BusinessException e) {
            log.error(e.toString());
            throw e;
        } catch (Exception e) {
            log.error(e.toString());
            throw new BusinessException(CodeInfo.LOGIN_INVALID);
        }
    }

    /**
     * ???????????????(??????:??????->?????? ??????:??????->??????)
     *
     * @param dto ??????
     * @return {id, coin, platCoin}
     */
    @Override
    public CoinTransferResDto coinTransfer(@NotNull CoinTransferReqDto dto) {
        UserInfo userInfo = userInfoServiceImpl.findIdentityByApiToken();
        // ???????????????
        if (userInfo.getAutoTransfer() == 0) {
            // ?????????????????????
            if (dto.getCoin().compareTo(BigDecimal.valueOf(1)) < 0) {
                throw new BusinessException(CodeInfo.COIN_TRANSFER_OVER_LIMIT);
            }
            // ???????????? ????????????????????????
            if (dto.getDirection() == 0 && userInfo.getCoin().compareTo(dto.getCoin()) < 0) {
                throw new BusinessException(CodeInfo.COIN_NOT_ENOUGH);
            }
        } else {
            if (dto.getDirection().equals(0)) {
                // ??????2????????????
                var coin = userInfo.getCoin().setScale(2, RoundingMode.DOWN);
                dto.setCoin(coin);
            }
        }

        // ????????????????????? ???Futures??????
        if (userInfo.getRole() == 4 && dto.getId() != 17) {
            throw new BusinessException(CodeInfo.PLAT_NO_AUTHORITIES_TO_ACCESS);
        }

        HeaderInfo headerInfo = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        try {
            // ??????????????????
            PlatAbstractFactory config = platFactoryByPlatId(dto.getId());
            // ???????????????????????????
            InsertCoinPlatRecordsReqDto build = InsertCoinPlatRecordsReqDto.builder()
                    .coin(dto.getCoin())
                    .direction(dto.getDirection())
                    .isFullAmount(userInfo.getAutoTransfer())
                    .platId(dto.getId())
                    .uid(userInfo.getId())
                    .build();
            InsertCoinPlatRecordsResDto insertResDto = platServiceBase.insertCoinPlatRecords(build);

            // ??????????????????
            PlatCoinTransferReqDto transferDto = PlatCoinTransferReqDto.builder()
                    .coin(dto.getCoin())
                    .orderId(insertResDto.getOrderId().toString())
                    .username(userServiceBase.buildUsername(userInfo.getUsername()))
                    .isFullAmount(0)
                    .lang(headerInfo.getLang())
                    .build();
            // ????????????????????? ?????? ?????? ??????
            PlatCoinTransferResDto result = dto.getDirection() == 0 ? config.coinUp(transferDto) : config.coinDown(transferDto);

            User user = userServiceImpl.lambdaQuery()
                    .select(User::getCoin)
                    .eq(User::getId, userInfo.getId())
                    .one();
            BigDecimal platCoin = BigDecimal.ZERO;
            if (nonNull(result)) {
                platCoin = result.getPlatCoin();
            }

            return CoinTransferResDto.builder()
                    .id(dto.getId())
                    .coin(user.getCoin())
                    .platCoin(platCoin)
                    .build();
        } catch (BusinessException e) {
            log.error(e.toString());
            if (e.getCode().equals(CodeInfo.PLAT_ACCOUNT_NOT_EXISTS.getCode())) {
                PlatRegisterReqDto build = PlatRegisterReqDto.builder()
                        .username(userServiceBase.buildUsername(headerInfo.getUsername()))
                        .device(headerInfo.getDevice())
                        .lang(headerInfo.getLang())
                        .build();
                if (Boolean.TRUE.equals(platFactoryByPlatId(dto.getId()).registerUser(build))) {
                    return coinTransfer(dto);
                }
            }
            throw e;
        } catch (Exception e) {
            log.error(e.toString());
            throw new BusinessException(CodeInfo.COIN_TRANSFER_INVALID);
        }
    }

    /**
     * ??????????????????
     *
     * @param gameId gameId
     * @return ????????????
     */
    @Override
    public GameModelDto checkGameStatus(Integer gameId) {
        GameModelDto gameModelDto = gameCache.getGamePropCache(gameId);
        if (gameModelDto == null) {
            throw new BusinessException(CodeInfo.GAME_NOT_EXISTS);
        } else if (gameModelDto.getGameStatus() != 1 && !gameId.toString().startsWith(String.valueOf(ConstData.SLOT_GAME_CATEGORY))) {
            throw new BusinessException(CodeInfo.GAME_NOT_OPEN);
        } else if (gameModelDto.getPlatStatus() != 1) {
            throw new BusinessException(CodeInfo.GAME_UNDER_MAINTENANCE);
        }

        return gameModelDto;
    }

    /**
     * ??????????????????
     *
     * @param dto ??????ID
     * @return ???????????????????????????
     */
    @Override
    public GameBalanceResDto queryBalanceByPlatId(GameBalanceReqDto dto) {
        UserInfo userInfo = userInfoServiceImpl.findIdentityByApiToken();
        HeaderInfo headerInfo = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        try {
            // ?????????????????????????????????
            PlatAbstractFactory config = platFactoryByPlatId(dto.getId());
            PlatQueryBalanceResDto platQueryBalanceResDto = config.queryBalance(
                    PlatQueryBalanceReqDto.builder()
                            .username(userServiceBase.buildUsername(userInfo.getUsername()))
                            .lang(headerInfo.getLang())
                            .build()
            );
            var coin = platQueryBalanceResDto.getPlatCoin();
            return GameBalanceResDto.builder()
                    .coin(coin)
                    .id(dto.getId())
                    .build();
        } catch (BusinessException e) {
            log.error(e.toString());
            if (e.getCode().equals(CodeInfo.PLAT_ACCOUNT_NOT_EXISTS.getCode())) {
                // S128?????? ??????Browser?????????????????? ??????????????????????????????0
                if (dto.getId() == 15) {
                    return GameBalanceResDto.builder().coin(BigDecimal.ZERO).id(dto.getId()).build();
                }
                PlatRegisterReqDto build = PlatRegisterReqDto.builder()
                        .username(userServiceBase.buildUsername(headerInfo.getUsername()))
                        .device(headerInfo.getDevice())
                        .lang(headerInfo.getLang())
                        .build();
                if (Boolean.TRUE.equals(platFactoryByPlatId(dto.getId()).registerUser(build))) {
                    return queryBalanceByPlatId(dto);
                }
            }
            throw e;
        } catch (Exception e) {
            log.error(e.toString());
            throw new BusinessException(CodeInfo.COIN_QUERY_INVALID);
        }
    }

    /**
     * ???????????????????????????
     *
     * @param dto {"id"??? "category", "name"}
     * @return ?????????????????????
     */
    @Override
    public ResPage<PlatSlotGameResDto> slotGameList(ReqPage<SlotGameReqDto> dto) {
        // ??????Header?????? Token???lang???device
        HeaderInfo headLocalDate = userInfoServiceImpl.getHeadLocalData();
        // ??????????????????uid=0
        int uid = headLocalDate.getId() != null ? headLocalDate.getId() : 0;

        // ??????????????????????????????????????????
        GameList one = gameListServiceImpl.lambdaQuery()
                .eq(GameList::getId, dto.getData().getId())
                .ne(GameList::getStatus, 0)
                .one();
        if (one == null) {
            throw new BusinessException(CodeInfo.GAME_NOT_EXISTS);
        } else if (!one.getGroupId().equals(ConstData.SLOT_GAME_CATEGORY)) {
            throw new BusinessException(CodeInfo.GAME_LIST_ONLY_SUPPORT_SLOT);
        }

        // ???????????????????????????
        try {
            PlatSlotGameReqDto build = PlatSlotGameReqDto.builder()
                    .lang(headLocalDate.getLang())
                    .device(headLocalDate.getDevice())
                    .category(dto.getData().getCategory())
                    .name(dto.getData().getName())
                    .uid(uid)
                    .id(dto.getData().getId())
                    .build();

            PlatSlotAbstractFactory config = platFactory(dto.getData().getId());

            ReqPage<PlatSlotGameReqDto> m = new ReqPage<>();
            m.setPage(dto.getCurrent(), dto.getSize());
            m.setData(build);
            Page<PlatSlotGameResDto> slotGameList = config.getSlotGameList(m);
            return ResPage.get(slotGameList);
        } catch (Exception e) {
            log.error(e.toString());
            throw e;
        }
    }

    /**
     * ?????????????????????
     *
     * @param dto {gameId, gameSlotId, direction}
     * @return true:?????? false:??????
     */
    @Override
    public Boolean slotGameFavorite(SlotGameFavoriteReqDto dto) {
        UserInfo userInfo = userInfoServiceImpl.findIdentityByApiToken();

        // ??????????????????????????????????????????
        GameList one = gameListServiceImpl.lambdaQuery()
                .eq(GameList::getId, dto.getGameId())
                .ne(GameList::getStatus, 0)
                .one();
        if (one == null) {
            throw new BusinessException(CodeInfo.GAME_NOT_EXISTS);
        } else if (!one.getGroupId().equals(ConstData.SLOT_GAME_CATEGORY)) {
            throw new BusinessException(CodeInfo.GAME_LIST_ONLY_SUPPORT_SLOT);
        }

        PlatSlotAbstractFactory config = platFactory(dto.getGameId());
        return config.favoriteSlotGame(
                PlatSlotGameFavoriteReqDto.builder()
                        .gameId(dto.getGameId())
                        .uid(userInfo.getId())
                        .gameSlotId(dto.getGameSlotId())
                        .direction(dto.getDirection())
                        .build()
        );
    }


    /**
     * ??????GameID ???????????????????????????
     *
     * @param gameId ??????ID
     * @param <T>    ??????
     * @return ????????????/???????????????
     * @author David
     * @date 07/05/2020
     */
    @Override
    public <T> T platFactory(Integer gameId) {
        GameModelDto game = checkGameStatus(gameId);
        JSONObject jsonObject = JSON.parseObject(game.getGameModel());
        if (null == jsonObject || jsonObject.getString(MODEL) == null) {
            throw new BusinessException(CodeInfo.PLAT_FACTORY_NOT_EXISTS);
        }

        T factory;
        if (game.getGroupId().equals(ConstData.SLOT_GAME_CATEGORY)) {
            // ?????????????????????
            factory = (T) PlatSlotAbstractFactory.init(jsonObject.getString(MODEL), game.getPlatModel());
        } else {
            factory = (T) PlatAbstractFactory.init(jsonObject.getString(MODEL), game.getPlatModel());
        }

        if (null == factory) {
            throw new BusinessException(CodeInfo.PLAT_FACTORY_NOT_EXISTS);
        }

        return factory;
    }

    /**
     * ?????? PlatId ????????????????????????
     *
     * @param platId ??????ID
     * @return ??????????????????
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
     * ??????????????????????????????????????? ?????????????????????????????????
     *
     * @param dto {gameId, startTime, endTime}
     * @return ?????????????????????????????????
     */
    @Override
    public PlatCoinStatisticsResDto getCoinStatisticsByDate(GameQueryDateDto dto) {
        UserInfo userInfo = userInfoServiceImpl.findIdentityByApiToken();
        PlatGameQueryDateDto build = PlatGameQueryDateDto.builder()
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .uid(userInfo.getId())
                .gameId(dto.getId())
                .lang(ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get().getLang())
                .build();
        try {
            return gameCommonBase.getCoinStatisticsByDate(build);
        } catch (Exception e) {
            log.info(e.toString());
            throw e;
        }
    }

    /**
     * ??????????????????????????????????????? ????????????????????????
     *
     * @param dto {gameId, startTime, endTime}
     * @return ????????????????????????
     */
    @Override
    public PlatTransferResInfo getCoinTransferByDate(StartEndTime dto) {
        UserInfo userInfo = userInfoServiceImpl.findIdentityByApiToken();

        // ???????????????????????????
        List<CoinPlatTransfer> list = coinPlatTransferServiceImpl.lambdaQuery()
                .select(CoinPlatTransfer::getCoin, CoinPlatTransfer::getCategory)
                .eq(CoinPlatTransfer::getStatus, 1)
                .eq(CoinPlatTransfer::getUid, userInfo.getId())
                .between(CoinPlatTransfer::getCreatedAt, dto.getStartTime(), dto.getEndTime())
                .list();
        BigDecimal coinUp = BigDecimal.ZERO;
        BigDecimal coinDown = BigDecimal.ZERO;
        for (CoinPlatTransfer coinPlatTransfer : list) {
            if (coinPlatTransfer.getCategory().equals(0)) {
                coinUp = coinUp.add(coinPlatTransfer.getCoin());
            } else {
                coinDown = coinDown.add(coinPlatTransfer.getCoin());
            }

        }

        return PlatTransferResInfo.builder()
                .coinDown(coinDown)
                .coinUp(coinUp)
                .build();
    }


    /**
     * ????????????????????????????????????
     *
     * @param dto {gameId, startTime, endTime}
     * @return ??????????????????
     */
    @Override
    public ResPage<PlatBetListResDto> getBetListByDate(ReqPage<GameQueryDateDto> dto) {
        UserInfo userInfo = userInfoServiceImpl.findIdentityByApiToken();
        PlatGameQueryDateDto build = PlatGameQueryDateDto.builder()
                .startTime(dto.getData().getStartTime())
                .endTime(dto.getData().getEndTime())
                .uid(userInfo.getId())
                .username(dto.getData().getUsername())
                .gameId(dto.getData().getId())
                .isAgent(dto.getData() != null && dto.getData().getIsAgent() != null ? dto.getData().getIsAgent() : null)
                .lang(ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get().getLang())
                .build();
        try {
            ReqPage<PlatGameQueryDateDto> m = new ReqPage<>();
            m.setPage(dto.getCurrent(), dto.getSize());
            m.setSortField(dto.getSortField());
            m.setSortKey(dto.getSortKey());
            m.setData(build);
            return gameCommonBase.getBetsRecords(m);
        } catch (Exception e) {
            log.info(e.toString());
            throw e;
        }
    }


    /**
     * ????????????
     *
     * @param dto        dto
     * @param userInfo   userInfo
     * @param headerInfo headerInfo
     * @return
     */
    @Override
    public CoinTransferResDto coinTransfer(@NotNull CoinTransferReqDto dto, UserInfo userInfo, HeaderInfo headerInfo) {
        // ???????????? ????????????????????????
        if (dto.getDirection() == 0 && userInfo.getCoin().compareTo(dto.getCoin()) < 0) {
            throw new BusinessException(CodeInfo.COIN_NOT_ENOUGH);
        }
        // ???????????????
        if (userInfo.getAutoTransfer() == 0) {
            // ?????????????????????
            if (dto.getCoin().compareTo(BigDecimal.valueOf(1)) < 0) {
                throw new BusinessException(CodeInfo.COIN_TRANSFER_OVER_LIMIT);
            }
        } else {
            if (dto.getDirection().equals(0)) {
                // ??????2????????????
                var coin = userInfo.getCoin().setScale(2, RoundingMode.DOWN);
                dto.setCoin(coin);
            }
        }

        // ????????????????????? ???Futures??????
        if (userInfo.getRole() == 4 && dto.getId() != 17) {
            throw new BusinessException(CodeInfo.PLAT_NO_AUTHORITIES_TO_ACCESS);
        }

        try {
            // ??????????????????
            PlatAbstractFactory config = platFactoryByPlatId(dto.getId());
            // ???????????????????????????
            InsertCoinPlatRecordsReqDto build = InsertCoinPlatRecordsReqDto.builder()
                    .coin(dto.getCoin())
                    .direction(dto.getDirection())
                    .isFullAmount(userInfo.getAutoTransfer())
                    .platId(dto.getId())
                    .uid(userInfo.getId())
                    .build();
            InsertCoinPlatRecordsResDto insertResDto = platServiceBase.insertCoinPlatRecords(build);

            // ??????????????????
            PlatCoinTransferReqDto transferDto = PlatCoinTransferReqDto.builder()
                    .coin(dto.getCoin())
                    .orderId(insertResDto.getOrderId().toString())
                    .username(userServiceBase.buildUsername(userInfo.getUsername()))
                    .isFullAmount(0)
                    .lang(headerInfo.getLang())
                    .build();
            // ????????????????????? ?????? ?????? ??????
            PlatCoinTransferResDto result = dto.getDirection() == 0 ? config.coinUp(transferDto) : config.coinDown(transferDto);

            User user = userServiceImpl.lambdaQuery()
                    .select(User::getCoin)
                    .eq(User::getId, userInfo.getId())
                    .one();
            BigDecimal platCoin = BigDecimal.ZERO;
            if (nonNull(result)) {
                platCoin = result.getPlatCoin();
            }

            return CoinTransferResDto.builder()
                    .id(dto.getId())
                    .coin(user.getCoin())
                    .platCoin(platCoin)
                    .build();
        } catch (BusinessException e) {
            log.error(e.toString());
            if (e.getCode().equals(CodeInfo.PLAT_ACCOUNT_NOT_EXISTS.getCode())) {
                PlatRegisterReqDto build = PlatRegisterReqDto.builder()
                        .username(userServiceBase.buildUsername(headerInfo.getUsername()))
                        .device(headerInfo.getDevice())
                        .lang(headerInfo.getLang())
                        .build();
                if (Boolean.TRUE.equals(platFactoryByPlatId(dto.getId()).registerUser(build))) {
                    return coinTransfer(dto);
                }
            }
            throw e;
        } catch (Exception e) {
            log.error(e.toString());
            throw new BusinessException(CodeInfo.COIN_TRANSFER_INVALID);
        }
    }

    @Override
    public ResPage<BetslipsDetailDto.SportSchedule> getSportSchedule(ReqPage<BetslipsDetailDto.SportScheduleReqDto> dto) {
        List<List<BetslipsDetailDto.SportSchedule>> sportScheduleLists = new ArrayList<>();
        List<GameList> list = gameListServiceImpl.lambdaQuery().ne(GameList::getStatus, 0).eq(nonNull(dto.getData()) && nonNull(dto.getData().getGameId()), GameList::getId, dto.getData().getGameId()).list();
        if (CollectionUtils.isEmpty(list)) {
            return new ResPage<>();
        } else {
            List<Integer> collect = list.stream().filter(x -> parseObject(x.getModel()).getBooleanValue("schedule")).map(GameList::getId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(collect)) {
                return new ResPage<>();
            } else {
                for (var j : collect) {
                    PlatSportsAbstractFactory config = platFactory(j);
                    var sportScheduleList = config.getSportSchedule(dto, j);
                    sportScheduleLists.add(sportScheduleList);
                }
                List<BetslipsDetailDto.SportSchedule> schedulesCollect = sportScheduleLists.stream().flatMap(Collection::stream)
                        .collect(Collectors.toList());
                if (CollectionUtils.isEmpty(schedulesCollect)) {
                    return new ResPage<>();
                }
                var page = new Page<BetslipsDetailDto.SportSchedule>(dto.getCurrent(), dto.getSize(), schedulesCollect.size());
                var sortField = ArrayUtils.isEmpty(dto.getSortField()) ? "sort" : dto.getSortField()[0];
                var sort2 = ArrayUtils.isEmpty(dto.getSortField()) || dto.getSortField().length <= 1 ? "operatorTime" : dto.getSortField()[1];
                Stream<BetslipsDetailDto.SportSchedule> asc = dto.getSortKey() != null && dto.getSortKey().equals("ASC") ? schedulesCollect.stream().sorted(Comparator.comparing(x -> sortField).thenComparing(x -> sort2.equals(null) ? "timestamp" : "operatorTime", Comparator.reverseOrder())) : schedulesCollect.stream().sorted(Comparator.comparing(x -> sortField).reversed().thenComparing(x -> sort2, Comparator.reverseOrder()));
                List<BetslipsDetailDto.SportSchedule> schedules = asc.skip(dto.getSize() * (dto.getCurrent() - 1))
                        .limit(dto.getSize()).collect(Collectors.toList());
                page.setRecords(schedules);
                return ResPage.get(page);
            }
        }
    }


    @Override
    public BetslipsDetailDto.ForwardEvent forwardEvent(BetslipsDetailDto.ForwardEventReq reqDto) {
        PlatSportsAbstractFactory config = platFactory(reqDto.getGameId());
        BaseParams.HeaderInfo headerInfo = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        var username = userServiceBase.buildUsername(headerInfo.getUsername().toLowerCase());
        return config.forwardEvent(username, reqDto.getMasterEventID());
    }

    @Override
    public <T> T verifySession(HttpServletRequest request, String path) {
        GameList one = gameListServiceImpl.lambdaQuery().ne(GameList::getStatus, 0).like(GameList::getModel, path).one();
        if (one != null) {
            platFactory(one.getId());
            IVerificationMethods init = IVerificationMethods.init(one.getModel());
            return init.verifySession(request);
        } else {
            throw new BusinessException(CodeInfo.PLAT_SYSTEM_ERROR);
        }
    }
}
