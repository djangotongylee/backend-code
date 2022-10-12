package com.xinbo.sports.apiend.base;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.google.common.base.CaseFormat;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.xinbo.sports.apiend.cache.redis.GameCache;
import com.xinbo.sports.apiend.io.dto.platform.GameModelDto;
import com.xinbo.sports.dao.generator.po.GameList;
import com.xinbo.sports.dao.generator.po.GameSlot;
import com.xinbo.sports.dao.generator.po.PlatList;
import com.xinbo.sports.dao.generator.po.UserProfile;
import com.xinbo.sports.dao.generator.service.GameListService;
import com.xinbo.sports.dao.generator.service.GameSlotService;
import com.xinbo.sports.dao.generator.service.PlatListService;
import com.xinbo.sports.dao.generator.service.UserProfileService;
import com.xinbo.sports.plat.factory.PlatAbstractFactory;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams.PlatBetListResDto;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams.PlatCoinStatisticsResDto;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams.PlatGameQueryDateDto;
import com.xinbo.sports.plat.io.dto.base.BetslipsDetailDto.Betslips;
import com.xinbo.sports.plat.io.dto.base.BetslipsDetailDto.BetslipsDetailReqDto;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.enums.BaseEnum;
import com.xinbo.sports.utils.SpringUtils;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.groovy.util.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.alibaba.fastjson.JSON.*;
import static java.util.Objects.nonNull;

/**
 * @author: wells
 * @date: 2020/6/12
 * @description:
 */
@Slf4j
@Component
public class GameCommonBase {
    /*有效投注金额字段*/
    public static final String BET_FIELD = "xb_valid_coin";
    /*盈亏字段*/
    public static final String WIN_FIELD = "xb_profit";
    /*创建时间*/
    public static final String CREATED_AT = "created_at";
    /*注单表service实例表前缀*/
    public static final String BET_PREFIX = "betslips";
    /*注单表service实例表后缀*/
    public static final String BET_SUFFIX = "ServiceImpl";
    /*游戏Id的映射字段*/
    public static final String GAME_ID_FIELD = "gameIdField";
    /*betContent的映射字段*/
    public static final String BET_CONTENT_FIELD = "betContent";
    /*字典表前缀*/
    public static final String DIC_PREFIX = "dic";
    public static final String XBSTATUS = "xbStatus";
    public static final String ACTIONNO = "actionNo";
    public static final String PGGame = "PG";
    @Autowired
    private GameListService gameListServiceImpl;
    @Autowired
    private DictionaryBase dictionaryBase;
    @Autowired
    private PlatListService platListServiceImpl;
    @Autowired
    private GameCache gameCache;
    @Autowired
    private GameSlotService gameSlotServiceImpl;
    @Autowired
    private UserProfileService userProfileServiceImpl;
    @Qualifier("taskExecutor")
    @Resource
    private TaskExecutor taskExecutor;


    /**
     * 视讯局号
     */
    private static final String GAME_NO = "gameNo";
    static final Map<Integer, List<Pair<String, String>>> FILED_MAP = Maps.of(
            //视讯
            301, Lists.newArrayList(
                    Pair.of("tableId", GAME_NO)
            ),
            302, Lists.newArrayList(
                    Pair.of("serialId", GAME_NO)
            ),
            303, Lists.newArrayList(
                    Pair.of("tableId", GAME_NO)
            ),
            304, Lists.newArrayList(
                    Pair.of("gameCode", GAME_NO)
            ),
            //彩票
            701, Lists.newArrayList(
                    Pair.of("numero", "actionNo")
            ),
            702, Lists.newArrayList(
                    Pair.of("numero", "actionNo")
            )
    );

    /**
     * 获取游戏 投注总金额、盈亏总金额
     *
     * @return
     */
    public PlatCoinStatisticsResDto getCoinStatisticsByDate(@Valid PlatGameQueryDateDto reqDto) {
        //获取游戏的基本配置信息
        var gameLists = getConfig(reqDto);
        //注总金额、盈亏总金额
        var coinBet = BigDecimal.ZERO;
        var coinProfit = BigDecimal.ZERO;
        try {
            var futureList = getFuture(gameLists, reqDto);
            for (Pair<BigDecimal, BigDecimal> future : futureList) {
                coinBet = coinBet.add(future.getLeft());
                coinProfit = coinProfit.add(future.getRight());
            }
        } catch (Exception e) {
            log.info("获取三方有效注单金额失败！" + e.getMessage());
        }
        return PlatCoinStatisticsResDto.builder().coinBet(coinBet).coinProfit(coinProfit).build();
    }

    /**
     * 多线程获取各个游戏的有效投注
     *
     * @param gameLists
     * @param reqDto
     * @return
     */
    public List<Pair<BigDecimal, BigDecimal>> getFuture(List<GameList> gameLists, PlatGameQueryDateDto reqDto) throws ExecutionException, InterruptedException {
        //创建线程池
        ThreadPoolExecutor pool = new ThreadPoolExecutor(gameLists.size(), gameLists.size(), 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(100));
        ListeningExecutorService guavaExecutors = MoreExecutors.listeningDecorator(pool);
        var futureList = new ArrayList<ListenableFuture<Pair<BigDecimal, BigDecimal>>>();
        for (var game : gameLists) {
            var futurePair = guavaExecutors.submit(new Callable<Pair<BigDecimal, BigDecimal>>() {
                @Override
                public Pair<BigDecimal, BigDecimal> call() throws Exception {
                    var configJsonObject = parseObject(game.getModel());
                    var betslips = BET_PREFIX + configJsonObject.getString(BET_PREFIX) + BET_SUFFIX;
                    var iService = (IService) SpringUtils.getBean(betslips);
                    var stringBuilder = new StringBuilder();
                    //投注金额字段
                    stringBuilder.append("ifNull(sum(" + BET_FIELD + "),0 )as  " + BET_FIELD);
                    //盈亏字段
                    stringBuilder.append(",ifNull(sum(" + WIN_FIELD + "),0)as " + WIN_FIELD);
                    var queryWrapper = new QueryWrapper<>()
                            .select(stringBuilder.toString())
                            .eq(nonNull(reqDto.getUid()), "xb_uid", reqDto.getUid())
                            .ge(nonNull(reqDto.getStartTime()), CREATED_AT, reqDto.getStartTime())
                            .le(nonNull(reqDto.getEndTime()), CREATED_AT, reqDto.getEndTime())
                            .last("limit 1");
                    var object = iService.getMap(queryWrapper);
                    if (nonNull(object)) {
                        var reJson = parseObject(toJSONString(object));
                        return Pair.of(reJson.getBigDecimal(BET_FIELD), reJson.getBigDecimal(WIN_FIELD));
                    }
                    return Pair.of(BigDecimal.ZERO, BigDecimal.ZERO);
                }
            });
            futureList.add(futurePair);
        }
        var resultsFuture = Futures.successfulAsList(futureList);
        return resultsFuture.get();
    }

    /**
     * 投注列表返回集
     *
     * @param platGameQueryDateDto
     * @return
     */
    @SneakyThrows
    public ResPage<PlatBetListResDto> getBetsRecords(@Valid ReqPage<PlatGameQueryDateDto> platGameQueryDateDto) {
        List<Integer> uidList = new ArrayList<>();
        List<List<PlatBetListResDto>> resultList = new ArrayList<>();
        if (platGameQueryDateDto.getData() != null && platGameQueryDateDto.getData().getUid() != null) {
            if (null != platGameQueryDateDto.getData().getIsAgent() && platGameQueryDateDto.getData().getIsAgent() == 1) {
                List<UserProfile> collect = userProfileServiceImpl.list(new QueryWrapper<UserProfile>().select("uid")
                        .eq("sup_uid_1", platGameQueryDateDto.getData().getUid())
                        .or().eq("sup_uid_2", platGameQueryDateDto.getData().getUid())
                        .or().eq("sup_uid_3", platGameQueryDateDto.getData().getUid())
                        .or().eq("sup_uid_4", platGameQueryDateDto.getData().getUid())
                        .or().eq("sup_uid_5", platGameQueryDateDto.getData().getUid())
                        .or().eq("sup_uid_6", platGameQueryDateDto.getData().getUid()));
                uidList = CollectionUtils.isEmpty(collect) ? Collections.singletonList(0) : collect.stream().map(UserProfile::getUid).collect(Collectors.toList());
            } else {
                uidList.add(platGameQueryDateDto.getData().getUid());
            }
        }
        //获取游戏的基本配置信息
        var gameId = null != platGameQueryDateDto.getData() && null != platGameQueryDateDto.getData().getGameId() ? platGameQueryDateDto.getData().getGameId() : null;
        var games = gameCache.getGamePropListCache(gameId);
        CountDownLatch cdl = new CountDownLatch(games.size());
        for (GameModelDto gameModel : games) {
            BetTotalEntity bt = new BetTotalEntity();
            bt.setGameModelDto(gameModel);
            bt.setPlatGameQueryDateDto(platGameQueryDateDto.getData());
            bt.setUidList(uidList);
            bt.setCdl(cdl);
            bt.setResultList(resultList);
            taskExecutor.execute(new BetList(bt));
        }
        cdl.await();
        //分页
        List<PlatBetListResDto> collect = resultList.stream().flatMap(Collection::stream)
                .collect(Collectors.toList());
        var page = new Page<PlatBetListResDto>(platGameQueryDateDto.getCurrent(), platGameQueryDateDto.getSize(), collect.size());
        List<PlatBetListResDto> betList = switchSorted(platGameQueryDateDto, collect).get().collect(Collectors.toList()).stream()
                .skip(platGameQueryDateDto.getSize() * (platGameQueryDateDto.getCurrent() - 1))
                .limit(platGameQueryDateDto.getSize()).collect(Collectors.toList());
        page.setRecords(betList);
        return ResPage.get(page);
    }

    /**
     * 排序处理
     *
     * @param platGameQueryDateDto
     * @param betList
     * @return
     */
    private Supplier<Stream<PlatBetListResDto>> switchSorted(ReqPage<PlatGameQueryDateDto> platGameQueryDateDto, List<PlatBetListResDto> betList) {
        return () -> {
            String sortKey = Optional.ofNullable(platGameQueryDateDto.getSortKey()).orElse("DESC");
            var fieldArr = platGameQueryDateDto.getSortField();
            switch (ArrayUtils.isNotEmpty(fieldArr) ? platGameQueryDateDto.getSortField()[0] : "createdAt") {
                case "id":
                    return sortKey.equals("ASC") ? betList.stream().sorted(Comparator.comparing(PlatBetListResDto::getId).thenComparing(PlatBetListResDto::getCreatedAt, Comparator.reverseOrder())) : betList.stream().sorted(Comparator.comparing(PlatBetListResDto::getId).reversed().thenComparing(PlatBetListResDto::getCreatedAt, Comparator.reverseOrder()));
                case "username":
                    return sortKey.equals("ASC") ? betList.stream().sorted(Comparator.comparing(PlatBetListResDto::getUsername).thenComparing(PlatBetListResDto::getId, Comparator.reverseOrder())) : betList.stream().sorted(Comparator.comparing(PlatBetListResDto::getUsername).reversed().thenComparing(PlatBetListResDto::getCreatedAt, Comparator.reverseOrder()));
                case "uid":
                    return sortKey.equals("ASC") ? betList.stream().sorted(Comparator.comparing(PlatBetListResDto::getUid).thenComparing(PlatBetListResDto::getId, Comparator.reverseOrder())) : betList.stream().sorted(Comparator.comparing(PlatBetListResDto::getUid).reversed().thenComparing(PlatBetListResDto::getCreatedAt, Comparator.reverseOrder()));
                case XBSTATUS:
                    return sortKey.equals("ASC") ? betList.stream().sorted(Comparator.comparing(PlatBetListResDto::getXbStatus).thenComparing(PlatBetListResDto::getId, Comparator.reverseOrder())) : betList.stream().sorted(Comparator.comparing(PlatBetListResDto::getXbStatus).reversed().thenComparing(PlatBetListResDto::getCreatedAt, Comparator.reverseOrder()));
                case "platName":
                    return sortKey.equals("ASC") ? betList.stream().sorted(Comparator.comparing(PlatBetListResDto::getPlatName).thenComparing(PlatBetListResDto::getId, Comparator.reverseOrder())) : betList.stream().sorted(Comparator.comparing(PlatBetListResDto::getPlatName).reversed().thenComparing(PlatBetListResDto::getCreatedAt, Comparator.reverseOrder()));
                case "name":
                    return sortKey.equals("ASC") ? betList.stream().sorted(Comparator.comparing(PlatBetListResDto::getName).thenComparing(PlatBetListResDto::getId, Comparator.reverseOrder())) : betList.stream().sorted(Comparator.comparing(PlatBetListResDto::getName).reversed().thenComparing(PlatBetListResDto::getCreatedAt, Comparator.reverseOrder()));
                case "createdAt":
                    return sortKey.equals("ASC") ? betList.stream().sorted(Comparator.comparing(PlatBetListResDto::getCreatedAt).thenComparing(PlatBetListResDto::getId, Comparator.reverseOrder())) : betList.stream().sorted(Comparator.comparing(PlatBetListResDto::getCreatedAt).reversed().thenComparing(PlatBetListResDto::getId, Comparator.reverseOrder()));
                default:
                    return betList.stream().sorted(Comparator.comparing(PlatBetListResDto::getCreatedAt).reversed().thenComparing(PlatBetListResDto::getId, Comparator.reverseOrder()));
            }
        };
    }

    /**
     * 获取游戏的基本配置信息
     *
     * @param reqDto
     * @return
     */
    public List<GameList> getConfig(@Valid PlatGameQueryDateDto reqDto) {
        var gameLists = gameListServiceImpl.lambdaQuery()
                .eq(nonNull(reqDto.getGameId()), GameList::getId, reqDto.getGameId())
                .eq(nonNull(reqDto.getPlatId()), GameList::getGroupId, reqDto.getPlatId())
                //启动状态
                .eq(GameList::getStatus, 1)
                .list();
        //判断游戏是否存在
        if (CollectionUtils.isEmpty(gameLists)) {
            throw new BusinessException(CodeInfo.GAME_RECORDS_EMPTY);
        }
        return gameLists;
    }

    /**
     * 创建三方平台的账号
     */
    @Async
    public void createThirdUser(String userName) {
        List<PlatList> platLists = platListServiceImpl.lambdaQuery().eq(PlatList::getStatus, 1).list();
        if (platLists.isEmpty()) {
            log.error("createThirdUser创建三方平台账号，无平台信息！");
            return;
        }
        var dto = PlatFactoryParams.PlatLoginReqDto.builder()
                .username(userName)
                //默认设备h5
                .device(BaseEnum.DEVICE.M.getValue())
                //默认语言zh-》中文简体
                .lang(BaseEnum.LANG.TH.getValue()).build();
        platLists.forEach(platList -> {
            try {
                PlatAbstractFactory platAbstractFactory = PlatAbstractFactory.initByPlat(platList.getName());
                platAbstractFactory.login(dto);
                log.info(platList.getName() + "平台，创建账号" + dto.getUsername() + "成功！");
            } catch (RuntimeException e) {
                log.error(platList.getName() + "平台，创建账号异常;" + e.getMessage());
            }
        });
    }

    /**
     * 根据平台ID 用户名异步创建平台账号
     *
     * @param userName 用户名
     * @param platId   平台ID
     */
    @Async
    public void createThirdUserByPlatId(String userName, Integer platId) {
        PlatList platList = gameCache.getPlatListCache(platId);
        if (null == platList) {
            log.error("createThirdUser创建三方平台账号，无平台信息！");
            return;
        }

        var dto = PlatFactoryParams.PlatLoginReqDto.builder()
                .username(userName)
                //默认设备h5
                .device(BaseEnum.DEVICE.M.getValue())
                //默认语言zh-》中文简体
                .lang(BaseEnum.LANG.TH.getValue()).build();

        try {
            PlatAbstractFactory platAbstractFactory = PlatAbstractFactory.initByPlat(platList.getName());
            Objects.requireNonNull(platAbstractFactory).login(dto);
            log.info(platList.getName() + "平台，创建账号" + dto.getUsername() + "成功！");
        } catch (RuntimeException e) {
            log.error(platList.getName() + "平台，创建账号异常;" + e.getMessage());
        }
    }

    /**
     * 根据注单ID与游戏ID获取详细注单信息
     *
     * @param reqDto
     * @return
     */
    public Betslips getBetslipsDetail(BetslipsDetailReqDto reqDto) {
        var bo = new Betslips();
        try {
            //获取平台配置
            var game = gameCache.getGamePropCache(reqDto.getGameId());
            var configJsonObject = parseObject(game.getGameModel());
            var betslips = BET_PREFIX + configJsonObject.getString(BET_PREFIX) + BET_SUFFIX;
            var iService = (IService) SpringUtils.getBean(betslips);
            Map<String, String> gameNameMap;
            //获取gameId的映射字段
            var gameIdField = configJsonObject.getString(GAME_ID_FIELD);
            var gameIdFormat = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, gameIdField);
            //电子游戏查询电子游戏列表数据
            if (game.getGroupId() == 2) {
                List<GameSlot> gameSlotList = gameSlotServiceImpl.lambdaQuery().eq(GameSlot::getGameId, reqDto.getGameId()).list();
                gameNameMap = gameSlotList.stream().collect(Collectors.toMap(GameSlot::getId, GameSlot::getName));
            } else {
                //拼接字典表的key
                var businessName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, configJsonObject.getString(BET_PREFIX));
                var dicKey = DIC_PREFIX + "_" + BET_PREFIX + "_" + businessName + "_" + gameIdField;
                gameNameMap = dictionaryBase.getCategoryMap(dicKey);
            }
            var queryWrapper = new QueryWrapper<>()
                    .eq(nonNull(reqDto.getBetId()), "id", reqDto.getBetId());
            var iServiceOne = iService.getOne(queryWrapper);
            var json = parseObject(toJSONString(iServiceOne));
            //状态转换
            var originStatus = String.valueOf(json.getInteger(XBSTATUS));
            var statusMap = dictionaryBase.getCategoryMap("dic_betslips_status");
            json.put(XBSTATUS, statusMap.getOrDefault(originStatus, originStatus));
            //转换字段
            var transferJson = transferProperties(json, FILED_MAP.get(reqDto.getGameId()));
            bo = parseObject(transferJson.toJSONString(), Betslips.class);
            //获取游戏名称
            var key = json.getString(gameIdFormat);
            var gameName = gameNameMap.getOrDefault(key, key);
            bo.setBetType(gameName);
        } catch (Exception e) {
            log.info("获取详细注单信息异常" + e.getMessage());
        }
        return bo;
    }

    /**
     * 字段属性转换
     *
     * @param jsonObject
     * @param list
     * @return
     */
    public JSONObject transferProperties(JSONObject jsonObject, List<Pair<String, String>> list) {
        if (!CollectionUtils.isEmpty(list)) {
            for (Pair<String, String> pair : list) {
                var value = jsonObject.getOrDefault(pair.getLeft(), "");
                jsonObject.put(pair.getRight(), value);
            }
        }
        return jsonObject;

    }

    private class BetList implements Runnable {
        private BetTotalEntity betTotalEntity;

        public BetList(BetTotalEntity betTotalEntity) {
            this.betTotalEntity = betTotalEntity;
        }

        @Override
        public void run() {
            try {
                var reqDto = betTotalEntity.getPlatGameQueryDateDto();
                var configJsonObject = parseObject(betTotalEntity.getGameModelDto().getGameModel());
                var betslips = BET_PREFIX + configJsonObject.getString(BET_PREFIX) + BET_SUFFIX;
                var iService = (IService) SpringUtils.getBean(betslips);
                Map<String, String> gameNameMap;
                //获取gameId的映射字段
                var gameIdField = configJsonObject.getString(GAME_ID_FIELD);
                var gameIdFormat = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, gameIdField);
                //获取betContent的映射字段
                var betContentField = null == configJsonObject.getString(BET_CONTENT_FIELD) ? "--" : configJsonObject.getString(BET_CONTENT_FIELD);
                var betContentFormat = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, betContentField);
                //获取期号的映射字段
                var actionNoField = null == configJsonObject.getString(ACTIONNO) ? "--" : configJsonObject.getString(ACTIONNO);
                var actionNoFormat = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, actionNoField);
                //电子游戏查询电子游戏列表数据
                if (betTotalEntity.getGameModelDto().getGroupId() == 2) {
                    List<GameSlot> gameSlotList = gameSlotServiceImpl.lambdaQuery().eq(GameSlot::getStatus, 1).eq(nonNull(reqDto.getGameId()), GameSlot::getGameId, reqDto.getGameId()).list();
                    if ((reqDto.getGameId() != null && reqDto.getGameId() == 207) || betTotalEntity.getGameModelDto().getPlatModel().equals(PGGame)) {
                        gameNameMap = reqDto.getLang().equals(BaseEnum.LANG.ZH.getValue()) ? gameSlotList.stream().filter(x -> x.getGameId() == 207).collect(Collectors.toMap(GameSlot::getGameTypeId, GameSlot::getNameZh)) : gameSlotList.stream().collect(Collectors.toMap(GameSlot::getGameTypeId, GameSlot::getName));
                    } else {
                        gameNameMap = reqDto.getLang().equals(BaseEnum.LANG.ZH.getValue()) ? gameSlotList.stream().collect(Collectors.toMap(GameSlot::getId, GameSlot::getNameZh)) : gameSlotList.stream().collect(Collectors.toMap(GameSlot::getId, GameSlot::getName));
                    }
                } else {
                    //拼接字典表的key
                    var businessName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, configJsonObject.getString(BET_PREFIX));
                    var dicKey = DIC_PREFIX + "_" + BET_PREFIX + "_" + businessName + "_" + gameIdField;
                    gameNameMap = dictionaryBase.getCategoryMap(dicKey);
                }
                var queryWrapper = new QueryWrapper<>()
                        .in(nonNull(betTotalEntity.getUidList()), "xb_uid", betTotalEntity.getUidList())
                        .likeRight(nonNull(reqDto.getUsername()), "xb_username", reqDto.getUsername())
                        .ge(nonNull(reqDto.getStartTime()), CREATED_AT, reqDto.getStartTime())
                        .le(nonNull(reqDto.getEndTime()), CREATED_AT, reqDto.getEndTime());

                var list = iService.list(queryWrapper);
                if (!CollectionUtils.isEmpty(list)) {
                    var jsonList = (List<PlatBetListResDto>) list.stream().map(x -> {
                        var json = parseObject(toJSONString(x));
                        var actionNo = json.getOrDefault(actionNoFormat, "--");
                        json.put("actionNo", actionNo);
                        var key = json.getString(gameIdFormat);
                        var gameName = gameNameMap.getOrDefault(key, key);
                        var betContent = json.getOrDefault(betContentFormat, "--").toString();
                        String regEx = "[`~!@#$%^&*()+=|{};\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、,？'\"]";
                        betContent = betContent.contains("[") ? parseObject(toJSONString(parseArray(betContent).get(0))).getString("betOption") : Pattern.compile(regEx).matcher(betContent).replaceAll(" ").trim();
                        json.put("name", gameName);
                        json.put(BET_CONTENT_FIELD, betContent);
                        json.put("platName", betTotalEntity.getGameModelDto().getPlatModel());
                        return parseObject(json.toJSONString(), PlatBetListResDto.class);
                    }).collect(Collectors.toList());
                    log.info(betTotalEntity.getGameModelDto().getPlatModel() + "查询注单数据共:" + jsonList.size() + "条");
                    betTotalEntity.getResultList().add(jsonList);
                }
                log.info(betTotalEntity.getGameModelDto().getPlatModel() + "查询注单数据共:" + list.size() + "条");
            } catch (Exception e) {
                log.error("查询注单记录异常" + betTotalEntity.getGameModelDto().getPlatModel() + "：" + e.toString());
            } finally {
                betTotalEntity.getCdl().countDown();
            }
        }
    }

    @Data
    private class BetTotalEntity {
        private List<List<PlatBetListResDto>> resultList;
        private GameModelDto gameModelDto;
        private PlatGameQueryDateDto platGameQueryDateDto;
        private CountDownLatch cdl;
        private List<Integer> uidList;
    }
}

