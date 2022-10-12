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
     * 获取游戏列表
     *
     * @return 游戏列表
     */
    @Override
    public List<HomeParams.GameIndexResDto> list() {
        return gameCache.getGroupGameListCache();
    }


    /**
     * 游戏列表
     *
     * @return 游戏列表
     */
    @Override
    public List<GameListInfo> gameList() {
        return gameCache.getGameListResCache();
    }

    /**
     * 游戏平台列表
     *
     * @return 平台列表
     */
    @Override
    public List<PlatListResInfo> platList() {
        return gameCache.getPlatListResCache();
    }

    /**
     * 登录三方游戏
     *
     * @param dto {id, slotId}
     * @return 游戏列表
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
            // 记录登录日志
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
                // 类型:0-登出 1-登录 2-进入游戏
                userLoginLog.setCategory(2);
                userLoginLog.setCoin(userServiceImpl.getById(uid).getCoin());
                userLoginLogServiceImpl.save(userLoginLog);
            });
            var userCoin = userServiceImpl.getById(headerInfo.getId()).getCoin();
            // 自动转账钱包开启后自动上分 最小操作金额1
            if (userInfo.getAutoTransfer().equals(1) && userInfo.getCoin().compareTo(BigDecimal.ONE) >= 0) {
                var transferReqDto = new CoinTransferReqDto();
                transferReqDto.setId(gameCache.getGameListCache(dto.getId()).getPlatListId());
                //类型:0-上分 1-下分
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
     * 三方上下分(上分:平台->三方 下分:三方->平台)
     *
     * @param dto 入参
     * @return {id, coin, platCoin}
     */
    @Override
    public CoinTransferResDto coinTransfer(@NotNull CoinTransferReqDto dto) {
        UserInfo userInfo = userInfoServiceImpl.findIdentityByApiToken();
        // 非免转钱包
        if (userInfo.getAutoTransfer() == 0) {
            // 上下分金额最低
            if (dto.getCoin().compareTo(BigDecimal.valueOf(1)) < 0) {
                throw new BusinessException(CodeInfo.COIN_TRANSFER_OVER_LIMIT);
            }
            // 上分金额 不能大于用户余额
            if (dto.getDirection() == 0 && userInfo.getCoin().compareTo(dto.getCoin()) < 0) {
                throw new BusinessException(CodeInfo.COIN_NOT_ENOUGH);
            }
        } else {
            if (dto.getDirection().equals(0)) {
                // 保留2位小数点
                var coin = userInfo.getCoin().setScale(2, RoundingMode.DOWN);
                dto.setCoin(coin);
            }
        }

        // 仅支持测试账号 对Futures上分
        if (userInfo.getRole() == 4 && dto.getId() != 17) {
            throw new BusinessException(CodeInfo.PLAT_NO_AUTHORITIES_TO_ACCESS);
        }

        HeaderInfo headerInfo = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        try {
            // 获取平台工厂
            PlatAbstractFactory config = platFactoryByPlatId(dto.getId());
            // 插入一条上下分订单
            InsertCoinPlatRecordsReqDto build = InsertCoinPlatRecordsReqDto.builder()
                    .coin(dto.getCoin())
                    .direction(dto.getDirection())
                    .isFullAmount(userInfo.getAutoTransfer())
                    .platId(dto.getId())
                    .uid(userInfo.getId())
                    .build();
            InsertCoinPlatRecordsResDto insertResDto = platServiceBase.insertCoinPlatRecords(build);

            // 构建请求参数
            PlatCoinTransferReqDto transferDto = PlatCoinTransferReqDto.builder()
                    .coin(dto.getCoin())
                    .orderId(insertResDto.getOrderId().toString())
                    .username(userServiceBase.buildUsername(userInfo.getUsername()))
                    .isFullAmount(0)
                    .lang(headerInfo.getLang())
                    .build();
            // 判断是调用上分 还是 下分 函数
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
     * 检查游戏状态
     *
     * @param gameId gameId
     * @return 游戏属性
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
     * 账户余额查询
     *
     * @param dto 游戏ID
     * @return 游戏余额、平台余额
     */
    @Override
    public GameBalanceResDto queryBalanceByPlatId(GameBalanceReqDto dto) {
        UserInfo userInfo = userInfoServiceImpl.findIdentityByApiToken();
        HeaderInfo headerInfo = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        try {
            // 查询余额用父类查询即可
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
                // S128斗鸡 只能Browser自动创建账号 此处特殊处理直接返回0
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
     * 获取老虎机游戏列表
     *
     * @param dto {"id"， "category", "name"}
     * @return 老虎机游戏信息
     */
    @Override
    public ResPage<PlatSlotGameResDto> slotGameList(ReqPage<SlotGameReqDto> dto) {
        // 获取Header信息 Token、lang、device
        HeaderInfo headLocalDate = userInfoServiceImpl.getHeadLocalData();
        // 游客访问设定uid=0
        int uid = headLocalDate.getId() != null ? headLocalDate.getId() : 0;

        // 查询当前游戏是否是老虎机游戏
        GameList one = gameListServiceImpl.lambdaQuery()
                .eq(GameList::getId, dto.getData().getId())
                .ne(GameList::getStatus, 0)
                .one();
        if (one == null) {
            throw new BusinessException(CodeInfo.GAME_NOT_EXISTS);
        } else if (!one.getGroupId().equals(ConstData.SLOT_GAME_CATEGORY)) {
            throw new BusinessException(CodeInfo.GAME_LIST_ONLY_SUPPORT_SLOT);
        }

        // 获取老虎机游戏列表
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
     * 收藏老虎机游戏
     *
     * @param dto {gameId, gameSlotId, direction}
     * @return true:成功 false:失败
     */
    @Override
    public Boolean slotGameFavorite(SlotGameFavoriteReqDto dto) {
        UserInfo userInfo = userInfoServiceImpl.findIdentityByApiToken();

        // 查询当前游戏是否是老虎机游戏
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
        GameModelDto game = checkGameStatus(gameId);
        JSONObject jsonObject = JSON.parseObject(game.getGameModel());
        if (null == jsonObject || jsonObject.getString(MODEL) == null) {
            throw new BusinessException(CodeInfo.PLAT_FACTORY_NOT_EXISTS);
        }

        T factory;
        if (game.getGroupId().equals(ConstData.SLOT_GAME_CATEGORY)) {
            // 老虎机游戏专用
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
     * 根据交易日期获取当前游戏的 投注总金额、输赢总金额
     *
     * @param dto {gameId, startTime, endTime}
     * @return 投注总金额、输赢总金额
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
     * 根据交易日期获取当前游戏的 上分、下分总金额
     *
     * @param dto {gameId, startTime, endTime}
     * @return 上分、下分总金额
     */
    @Override
    public PlatTransferResInfo getCoinTransferByDate(StartEndTime dto) {
        UserInfo userInfo = userInfoServiceImpl.findIdentityByApiToken();

        // 查询上下分所有记录
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
     * 根据交易日期获取投注列表
     *
     * @param dto {gameId, startTime, endTime}
     * @return 投注信息列表
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
     * 批量下分
     *
     * @param dto        dto
     * @param userInfo   userInfo
     * @param headerInfo headerInfo
     * @return
     */
    @Override
    public CoinTransferResDto coinTransfer(@NotNull CoinTransferReqDto dto, UserInfo userInfo, HeaderInfo headerInfo) {
        // 上分金额 不能大于用户余额
        if (dto.getDirection() == 0 && userInfo.getCoin().compareTo(dto.getCoin()) < 0) {
            throw new BusinessException(CodeInfo.COIN_NOT_ENOUGH);
        }
        // 非免转钱包
        if (userInfo.getAutoTransfer() == 0) {
            // 上下分金额最低
            if (dto.getCoin().compareTo(BigDecimal.valueOf(1)) < 0) {
                throw new BusinessException(CodeInfo.COIN_TRANSFER_OVER_LIMIT);
            }
        } else {
            if (dto.getDirection().equals(0)) {
                // 保留2位小数点
                var coin = userInfo.getCoin().setScale(2, RoundingMode.DOWN);
                dto.setCoin(coin);
            }
        }

        // 仅支持测试账号 对Futures上分
        if (userInfo.getRole() == 4 && dto.getId() != 17) {
            throw new BusinessException(CodeInfo.PLAT_NO_AUTHORITIES_TO_ACCESS);
        }

        try {
            // 获取平台工厂
            PlatAbstractFactory config = platFactoryByPlatId(dto.getId());
            // 插入一条上下分订单
            InsertCoinPlatRecordsReqDto build = InsertCoinPlatRecordsReqDto.builder()
                    .coin(dto.getCoin())
                    .direction(dto.getDirection())
                    .isFullAmount(userInfo.getAutoTransfer())
                    .platId(dto.getId())
                    .uid(userInfo.getId())
                    .build();
            InsertCoinPlatRecordsResDto insertResDto = platServiceBase.insertCoinPlatRecords(build);

            // 构建请求参数
            PlatCoinTransferReqDto transferDto = PlatCoinTransferReqDto.builder()
                    .coin(dto.getCoin())
                    .orderId(insertResDto.getOrderId().toString())
                    .username(userServiceBase.buildUsername(userInfo.getUsername()))
                    .isFullAmount(0)
                    .lang(headerInfo.getLang())
                    .build();
            // 判断是调用上分 还是 下分 函数
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
