package com.xinbo.sports.plat.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.PascalNameFilter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinbo.sports.dao.generator.po.*;
import com.xinbo.sports.dao.generator.service.BetslipsJokerHaibaService;
import com.xinbo.sports.dao.generator.service.BetslipsJokerGameService;
import com.xinbo.sports.dao.generator.service.BetslipsJokerJackpotService;
import com.xinbo.sports.dao.generator.service.GameSlotService;
import com.xinbo.sports.plat.base.CommonPersistence;
import com.xinbo.sports.plat.base.SlotServiceBase;
import com.xinbo.sports.plat.factory.ISuppleMethods;
import com.xinbo.sports.plat.factory.PlatAbstractFactory;
import com.xinbo.sports.plat.factory.PlatSlotAbstractFactory;
import com.xinbo.sports.plat.io.bo.JokerRequestParameter;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams;
import com.xinbo.sports.plat.io.enums.BasePlatParam;
import com.xinbo.sports.plat.io.enums.JokerPlatEnum;
import com.xinbo.sports.service.base.CoinPlatTransfersBase;
import com.xinbo.sports.service.base.UserServiceBase;
import com.xinbo.sports.service.cache.KeyConstant;
import com.xinbo.sports.service.common.RedisConstant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.enums.BaseEnum;
import com.xinbo.sports.utils.*;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.response.CodeInfo;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.alibaba.fastjson.JSON.toJSONString;
import static com.xinbo.sports.plat.io.enums.JokerPlatEnum.JokerMethodEnum.CU;
import static com.xinbo.sports.plat.io.enums.JokerPlatEnum.JokerMethodEnum.GC;
import static com.xinbo.sports.plat.io.enums.JokerPlatEnum.MAP;
import static com.xinbo.sports.utils.components.response.CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_1;
import static java.util.stream.Collectors.joining;

@Slf4j
@Service("JokerServiceImpl")
public class JokerServiceImpl implements PlatSlotAbstractFactory, ISuppleMethods {
    protected static final String MODEL = "JOKER";
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    public static final String HAIBA = "kk8nqm3cfwtng";
    /*获取参数配置*/
    @Setter
    public JokerPlatEnum.PlatConfig config;
    @Resource
    private JedisUtil jedisUtil;
    @Resource
    private CoinPlatTransfersBase coinPlatTransfersBase;
    @Autowired
    private BetslipsJokerGameService betslipsJokerGameServiceImpl;
    @Autowired
    private BetslipsJokerHaibaService betslipsJokerHaibaServiceImpl;
    @Autowired
    private BetslipsJokerJackpotService betslipsJokerJackpotServiceImpl;
    @Resource
    private UserServiceBase userServiceBase;
    @Resource
    private SlotServiceBase slotServiceBase;
    @Resource
    private GameSlotService gameSlotServiceImpl;
    @Resource
    private CommonPersistence commonPersistence;

    /**
     * 获取游戏登录链接
     *
     * @param reqDto {username, lang, device, slotId}
     * @return 登录链接
     */
    @Override
    public PlatFactoryParams.PlatGameLoginResDto login(PlatFactoryParams.PlatLoginReqDto reqDto) {
        var lang = EnumUtils.getEnumIgnoreCase(JokerRequestParameter.LANGS.class, reqDto.getLang()).getCode();
        var loginBO = JokerRequestParameter.Login.builder()
                .method(JokerPlatEnum.JokerMethodEnum.PLAY.getMethodName())
                .timestamp(DateNewUtils.now())
                .username(reqDto.getUsername())
                .build();
        var send = send(config.getApiUrl(), loginBO, JokerPlatEnum.JokerMethodEnum.PLAY.getMethodNameDesc());
        var token = parseObject(send).getString("Token");
        var mobile = reqDto.getDevice().equals(BaseEnum.DEVICE.M.getValue()) ? true : false;
        return PlatFactoryParams.PlatGameLoginResDto.builder().type(1)
                .url(config.getForwardUrl() + "?token=" + token + "&game=" + reqDto.getSlotId() + "&redirectUrl=" + config.getRedirectUrl() + "&mobile=" + mobile + "&lang=" + lang).build();
    }


    @SneakyThrows
    public String send(String url, Object object, String methodNameDesc) {
        try {
            String json = toJSONString(object, new PascalNameFilter());
            var result = HttpUtils.doPostWithStatusCode(url + "?appid=" + config.getAppID() + "&signature=" + getSignature(json), parseObject(json), JokerPlatEnum.HTTP_HEADER, MODEL);
            if (result.statusCode() == 200 || (methodNameDesc.equals(CU.getMethodNameDesc()) && result.statusCode() == 201)) {
                return new String(result.body());
            } else {
                if (methodNameDesc.equals(JokerPlatEnum.JokerMethodEnum.TCH.getMethodNameDesc())) {
                    return String.valueOf(result.statusCode());
                } else {
                    throw new BusinessException(MAP.getOrDefault(result.statusCode(), CodeInfo.PLAT_SYSTEM_ERROR));
                }
            }
        } catch (Exception e) {
            XxlJobLogger.log(MODEL + methodNameDesc + "异常:" + e.getMessage());
            log.error(MODEL + methodNameDesc + "失败!" + e.getMessage());
            throw e;
        }
    }

    /**
     * @param json
     * @return
     */
    @SneakyThrows
    private String getSignature(String json) {
        var rawData = String.valueOf(new TreeMap<>(parseObject(json, Map.class)).entrySet().stream().map(Object::toString).collect(joining("&")));
        SecretKeySpec signingKey = new SecretKeySpec(config.getSecret().getBytes(), HMAC_SHA1_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        mac.init(signingKey);
        var hashedValue = mac.doFinal(rawData.getBytes());
        var hash = Base64.getEncoder().encodeToString(hashedValue);
        return URLEncoder.encode(hash, StandardCharsets.UTF_8.toString());
    }


    @Override
    public PlatFactoryParams.PlatLogoutResDto logout(PlatFactoryParams.PlatLogoutReqDto dto) {
        return null;
    }

    /**
     * 第三方上分
     *
     * @param reqDto {"coin","orderId","username"}
     * @return
     */
    @SneakyThrows
    @Override
    public PlatFactoryParams.PlatCoinTransferResDto coinUp(PlatFactoryParams.PlatCoinTransferReqDto reqDto) {
        // 测试环境上方金额不能大于100
        if (!"PROD".equals(config.getEnvironment()) && reqDto.getCoin().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BusinessException(CodeInfo.PLAT_COIN_UP_COIN_NOT_GT_100);
        }
        return transfer(reqDto, 0);
    }

    /**
     * 转账 ： 1->存款 -1->取款
     *
     * @param reqDto
     * @param i
     * @return
     */
    private PlatFactoryParams.PlatCoinTransferResDto transfer(PlatFactoryParams.PlatCoinTransferReqDto reqDto, int i) {
        try {
            var transferBO = JokerRequestParameter.Transfer.builder()
                    .method(JokerPlatEnum.JokerMethodEnum.TC.getMethodName())
                    .timestamp(DateNewUtils.now())
                    .username(reqDto.getUsername())
                    .requestID(reqDto.getOrderId())
                    .amount(i == 0 ? String.valueOf(reqDto.getCoin()) : ("-" + reqDto.getCoin().toString()))
                    .build();
            var call = send(config.getApiUrl(), transferBO, JokerPlatEnum.JokerMethodEnum.TC.getMethodNameDesc());
            BigDecimal afterAmount = BigDecimal.ZERO;
            if (!call.contains("Message")) afterAmount = parseObject(call).getBigDecimal("Credit");
            coinPlatTransfersBase.updateOrderPlat(reqDto.getOrderId(), 1, reqDto.getCoin(), null, null);
            return PlatFactoryParams.PlatCoinTransferResDto.builder().platCoin(afterAmount).build();
        } catch (Exception e) {
            log.error(e.toString());
            coinPlatTransfersBase.updateOrderPlat(reqDto.getOrderId(), 2, reqDto.getCoin(), "", e.toString());
            throw e;
        }
    }


    /**
     * 第三方下分
     *
     * @param reqDto {"coin","orderId","username"}
     * @return
     */
    @SneakyThrows
    @Override
    public PlatFactoryParams.PlatCoinTransferResDto coinDown(PlatFactoryParams.PlatCoinTransferReqDto reqDto) {
        return transfer(reqDto, 1);
    }

    /**
     * 查询余额
     *
     * @param reqDto
     * @return
     */
    @Override
    public PlatFactoryParams.PlatQueryBalanceResDto queryBalance(PlatFactoryParams.PlatQueryBalanceReqDto reqDto) {
        var balanceBO = JokerRequestParameter.Balance.builder()
                .method(GC.getMethodName())
                .username(reqDto.getUsername())
                .timestamp(DateNewUtils.now())
                .build();
        var call = send(config.getApiUrl(), balanceBO, GC.getMethodNameDesc());
        return PlatFactoryParams.PlatQueryBalanceResDto.builder().platCoin(parseObject(call).getBigDecimal("Credit")).build();
    }


    /**
     * 注单拉取
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void pullBetsLips() {
        var pullStartTime = jedisUtil.hget(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH, RedisConstant.JOKER);
        String nextId = null;
        XxlJobLogger.log(MODEL + "进入拉单==================");
        if (Strings.isEmpty(pullStartTime)) {
            BetslipsJokerGame betslipsJoker = betslipsJokerGameServiceImpl.getOne(new LambdaQueryWrapper<BetslipsJokerGame>()
                    .orderByDesc(BetslipsJokerGame::getCreatedAt).last("limit 1"));
            var start = (betslipsJoker != null && betslipsJoker.getCreatedAt() != null) ? betslipsJoker.getCreatedAt() : DateNewUtils.now() - 60 * 60 * 12;
            pullStartTime = String.valueOf(start);
        }
        if (pullStartTime.contains("_")) {
            pullStartTime = pullStartTime.split("_")[0];
            nextId = pullStartTime.split("_")[0];
        }
        var pullEndTime = String.valueOf(Math.min(Integer.parseInt(pullStartTime) + 60 * 30, DateNewUtils.now()-15*60));
        var pullBetsDataBO = JokerRequestParameter.GetBetsDetails.builder()
                .method(JokerPlatEnum.JokerMethodEnum.TSM.getMethodName())
                .timestamp(DateNewUtils.now())
                .startDate(DateNewUtils.utc8Str(DateNewUtils.utc8Zoned(Integer.parseInt(pullStartTime)), DateNewUtils.Format.yyyy_MM_dd_HH_mm))
                .endDate(DateNewUtils.utc8Str(DateNewUtils.utc8Zoned(Integer.parseInt(pullEndTime)), DateNewUtils.Format.yyyy_MM_dd_HH_mm))
                .nextId(nextId)
                .build();
        var call = send(config.getApiUrl(), pullBetsDataBO, JokerPlatEnum.JokerMethodEnum.TSM.getMethodNameDesc());
        XxlJobLogger.log(MODEL + "进入拉单:" + call);
        if (call != null) {
            var pullEndIndex = insertJokerData(call);
            pullEndTime = null != pullEndIndex ? pullStartTime + "_" + pullEndIndex : pullEndTime;
        }
        jedisUtil.hset(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH, RedisConstant.JOKER, pullEndTime);

    }

    /**
     * 数据入库
     *
     * @param call
     * @return
     */
    private String insertJokerData(String call) {
        JSONObject data = parseObject(parseObject(call).getString("data"));
        JSONArray jsonArray = data.getJSONArray("Game");
        JSONArray jackpotArray = data.getJSONArray("Jackpot");
        List<BetslipsJokerGame> betslipsJokerlist = new ArrayList<>();
        Map<String, Integer> userMap = userServiceBase.getUsernameIdMap();
        if (jsonArray != null) {
            betslipsJokerlist = getGameList(jsonArray, userMap, betslipsJokerlist);
            List<BetslipsJokerGame> fishingList = betslipsJokerlist.stream().filter(x -> x.getGameCode().equals(HAIBA)).collect(Collectors.toList());
            List<BetslipsJokerGame> gameList = betslipsJokerlist.stream().filter(x -> !x.getGameCode().equals(HAIBA)).collect(Collectors.toList());
            betslipsJokerGameServiceImpl.saveOrUpdateBatch(gameList, 1000);
            betslipsJokerHaibaServiceImpl.saveOrUpdateBatch(BeanConvertUtils.copyListProperties(fishingList, BetslipsJokerHaiba::new), 1000);
            if (null != jackpotArray) {
                betslipsJokerJackpotServiceImpl.saveOrUpdateBatch(BeanConvertUtils.copyListProperties(getGameList(jackpotArray, userMap, betslipsJokerlist), BetslipsJokerJackpot::new), 1000);
            }
        } else if (jsonArray == null) {
            XxlJobLogger.log(MODEL + "当前无注单!" + toJSONString(call));
        } else {
            XxlJobLogger.log(MODEL + "失败!" + toJSONString(call));
            PLAT_BET_SLIPS_EXCEPTION_CATEGORY_1.setMsg(toJSONString(call));
            throw new BusinessException(PLAT_BET_SLIPS_EXCEPTION_CATEGORY_1);
        }
        return data.getString("nextId");
    }


    private List<BetslipsJokerGame> getGameList(JSONArray jsonArray, Map<String, Integer> userMap, List<BetslipsJokerGame> betslipsJokerlist) {
        for (var x : jsonArray) {
            var betDetailBo = parseObject(toJSONString(x), JokerRequestParameter.BetDetail.class);
            var userName = userServiceBase.filterUsername(betDetailBo.getUsername());
            var uid = userMap.get(userName);
            if (StringUtils.isEmpty(userName) || uid == null) {
                continue;
            }
            var retBetslipsJoker = BeanConvertUtils.copyProperties(betDetailBo, BetslipsJokerGame::new, (bo, sb) -> {
                Date time = DateUtils.yyyyMMddHHmmss(bo.getTime().replace("0000+08:00", "").replace("T", " "));
                sb.setTime(time);
                sb.setXbUsername(userName);
                sb.setXbUid(uid);
                sb.setXbCoin(bo.getAmount());
                sb.setXbValidCoin(bo.getAmount());
                sb.setXbStatus(transferStatus(bo.getResult().subtract(bo.getAmount())));
                sb.setXbProfit(bo.getResult().subtract(bo.getAmount()));
                sb.setCreatedAt((int) (time.getTime() / 1000));
                sb.setUpdatedAt(DateNewUtils.now());
            });
            betslipsJokerlist.add(retBetslipsJoker);
        }
        return betslipsJokerlist;
    }


    /**
     * 检查转账状态
     *
     * @author: David
     * @date: 04/05/2020
     */
    @Override
    public Boolean checkTransferStatus(String orderId) {
        var checkTranJokertionBO = JokerRequestParameter.VerifyTransferCredit.builder()
                .method(JokerPlatEnum.JokerMethodEnum.TCH.getMethodName())
                .timestamp(DateNewUtils.now())
                .requestID(orderId)
                .build();
        var call = send(config.getApiUrl(), checkTranJokertionBO, JokerPlatEnum.JokerMethodEnum.TCH.getMethodNameDesc());
        if (call != null && call.contains("Amount")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 注册用户
     *
     * @param reqDto 创建会员信息
     * @return
     */
    @Override
    public Boolean registerUser(PlatFactoryParams.PlatRegisterReqDto reqDto) {
        var registerBO = JokerRequestParameter.Register.builder()
                .method(CU.getMethodName())
                .username(reqDto.getUsername())
                .timestamp(DateNewUtils.now())
                .build();
        var call = send(config.getApiUrl(), registerBO, CU.getMethodNameDesc());
        return call != null;
    }


    /**
     * 状态码转换
     *
     * @param ticketStatus
     * @return
     */
    public Integer transferStatus(BigDecimal ticketStatus) {
        //状态码
        Integer status;
        if (ticketStatus.equals(BigDecimal.ZERO)) {
            status = BasePlatParam.BetRecordsStatus.DRAW.getCode();
        } else if (ticketStatus.compareTo(BigDecimal.ZERO) < 0) {
            status = BasePlatParam.BetRecordsStatus.LOSE.getCode();
        } else {
            status = BasePlatParam.BetRecordsStatus.WIN.getCode();
        }
        return status;
    }

    /**
     * 根据起始、结束时间 生成补单信息
     * 详细数据:注单- 10天的期限
     *
     * @param dto dto.start 开始时间 dto.end结束时间
     */
    @Override
    public void genSupplementsOrders(PlatFactoryParams.GenSupplementsOrdersReqDto dto) {
        if (ZonedDateTime.now().minusDays(10).toInstant().getEpochSecond() > dto.getStart()) {
            throw new BusinessException(CodeInfo.PLAT_BET_SLIPS_SUPPLE_OVER_10_DAYS);
        } else {
            ZonedDateTime now = ZonedDateTime.now();
            LinkedList<BetSlipsSupplemental> list = new LinkedList<>();
            BetslipsJokerGame startOne = betslipsJokerGameServiceImpl.getOne(new QueryWrapper<BetslipsJokerGame>().orderByDesc("created_at").lt("created_at", dto.getStart()).last("limit 1"));
            var pullDataId = startOne != null && startOne.getId() != null ? startOne.getId() : 0;
            var pullBetsDataBO = new JSONObject();
            pullBetsDataBO.put("startingAfter", pullDataId);
            pullBetsDataBO.put("limit", 1000);
            pullBetsDataBO.put("end", dto.getEnd());
            Integer currentTime = (int) now.toInstant().getEpochSecond();
            BetSlipsSupplemental po = PlatAbstractFactory
                    .buildBetSlipsSupplemental(dto.getGameId(), DateNewUtils.utc8Zoned(dto.getStart()).toString(), DateNewUtils.utc8Zoned(dto.getEnd()).toString(), toJSONString(pullBetsDataBO), currentTime);
            list.add(po);
            if (!list.isEmpty()) {
                commonPersistence.addBatchBetSlipsSupplementalList(list);
            }
        }

    }

    /**
     * 补充注单信息
     *
     * @param dto dto.requestInfo 补单请求参数
     * @author: David
     * @date: 04/05/2020
     */
    @Override
    public void betsRecordsSupplemental(PlatFactoryParams.BetsRecordsSupplementReqDto dto) {
        int endTime = 0;
        int currentTime = 0;
        JSONObject jsonObject = parseObject(dto.getRequestInfo());
        if (jsonObject.containsKey("end")) {
            endTime = jsonObject.getInteger("end");
            jsonObject.remove("end");
        }
        do {
            String path = JokerPlatEnum.JokerMethodEnum.TSM.getMethodName() + "&startingAfter={startingAfter}";
            String call = send(config.getApiUrl() + path, parseObject(toJSONString(jsonObject)), JokerPlatEnum.JokerMethodEnum.TSM.getMethodNameDesc());
            XxlJobLogger.log(MODEL + "进入补单:" + call);
            String pullIndex = insertJokerData(call);
            currentTime = Integer.parseInt(pullIndex.split("_")[0]);
            jsonObject.replace("startingAfter", pullIndex.split("_")[1]);
        } while (endTime > currentTime);
    }

    /**
     * 拉单异常 再次拉单
     *
     * @param dto dto.requestInfo 拉单请求参数
     * @author: David
     * @date: 04/05/2020
     */
    @Override
    public void betSlipsExceptionPull(PlatFactoryParams.BetsRecordsSupplementReqDto dto) {
        this.betsRecordsSupplemental(dto);
    }

    @Override
    public Page<PlatFactoryParams.PlatSlotGameResDto> getSlotGameList(ReqPage<PlatFactoryParams.PlatSlotGameReqDto> platSlotgameReqDto) {
        return slotServiceBase.getSlotGameList(platSlotgameReqDto);
    }

    @Override
    public Boolean favoriteSlotGame(PlatFactoryParams.PlatSlotGameFavoriteReqDto platSlotGameFavoriteReqDto) {
        return slotServiceBase.favoriteSlotGame(platSlotGameFavoriteReqDto);
    }

    @Override
    public void pullGames() {
        var gameListBO = JokerRequestParameter.GameList.builder()
                .method(JokerPlatEnum.JokerMethodEnum.LISTGAMES.getMethodName())
                .timestamp(DateNewUtils.now()).build();
        var send = send(config.getApiUrl(), gameListBO, JokerPlatEnum.JokerMethodEnum.LISTGAMES.getMethodNameDesc());
        JSONArray jsonArray = parseObject(send).getJSONArray("ListGames");
        if (jsonArray != null && !jsonArray.isEmpty()) {
            GameSlot gameSlot = new GameSlot();
            var device = 0;
            for (var x : jsonArray) {
                JSONObject jo = (JSONObject) x;
                gameSlot.setGameId(206);
                gameSlot.setStatus(1);
                gameSlot.setId(jo.getString("GameCode"));
                gameSlot.setName(jo.getString("GameName"));
                gameSlot.setImg(String.format("/icon/joker/%s.png", jo.getString("GameName").replace(" ", "")));
                gameSlot.setGameTypeName(jo.getString("GameCode"));
                gameSlot.setGameTypeId(jo.getString("GameCode"));
                gameSlot.setNameZh(jo.getString("GameName"));
                gameSlot.setCreatedAt(DateUtils.getCurrentTime());
                gameSlot.setUpdatedAt(DateUtils.getCurrentTime());
                gameSlot.setDevice(device);
                gameSlot.setSort(99);
                gameSlot.setIsNew(0);
                gameSlotServiceImpl.saveOrUpdate(gameSlot);
            }

        }
    }

}


