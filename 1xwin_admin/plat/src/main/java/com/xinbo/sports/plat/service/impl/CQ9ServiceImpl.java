package com.xinbo.sports.plat.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinbo.sports.dao.generator.po.*;
import com.xinbo.sports.dao.generator.service.BetslipsCq9GameService;
import com.xinbo.sports.dao.generator.service.BetslipsCq9ParadiseService;
import com.xinbo.sports.dao.generator.service.BetslipsCq9ThunderFighterService;
import com.xinbo.sports.dao.generator.service.BetslipsCq9WaterMarginService;
import com.xinbo.sports.dao.generator.service.impl.GameSlotServiceImpl;
import com.xinbo.sports.plat.base.CommonPersistence;
import com.xinbo.sports.plat.base.SlotServiceBase;
import com.xinbo.sports.plat.factory.ISuppleMethods;
import com.xinbo.sports.plat.factory.PlatAbstractFactory;
import com.xinbo.sports.plat.factory.PlatSlotAbstractFactory;
import com.xinbo.sports.plat.io.bo.CQ9RequestParameter;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams;
import com.xinbo.sports.plat.io.enums.BasePlatParam;
import com.xinbo.sports.plat.io.enums.CQ9PlatEnum;
import com.xinbo.sports.plat.io.enums.TCGPlatEnum;
import com.xinbo.sports.service.base.CoinPlatTransfersBase;
import com.xinbo.sports.service.base.ExceptionThreadLocal;
import com.xinbo.sports.service.base.UserServiceBase;
import com.xinbo.sports.service.cache.KeyConstant;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.service.common.RedisConstant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.enums.BaseEnum;
import com.xinbo.sports.utils.BeanConvertUtils;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.DateUtils;
import com.xinbo.sports.utils.JedisUtil;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.alibaba.fastjson.JSON.*;
import static com.xinbo.sports.utils.components.response.CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_1;

@Slf4j
@Service("CQ9ServiceImpl")
public class CQ9ServiceImpl implements PlatSlotAbstractFactory, ISuppleMethods {
    protected static final String MODEL = "CQ9";
    protected static final String STATUS = "status";
    protected static final String BALANCE = "balance";
    protected static final String DATA = "data";
    @Resource
    protected ConfigCache configCache;
    /*获取参数配置*/
    @Setter
    public CQ9PlatEnum.PlatConfig config;
    @Resource
    private JedisUtil jedisUtil;
    @Resource
    private CoinPlatTransfersBase coinPlatTransfersBase;
    @Autowired
    private BetslipsCq9GameService betslipsCq9GameServiceImpl;
    @Autowired
    private BetslipsCq9ParadiseService betslipsCq9ParadiseServiceImpl;
    @Autowired
    private BetslipsCq9ThunderFighterService betslipsCq9ThunderFighterServiceImpl;
    @Autowired
    private BetslipsCq9WaterMarginService betslipsCq9WaterMarginServiceImpl;
    @Resource
    private UserServiceBase userServiceBase;
    @Resource
    private SlotServiceBase slotServiceBase;
    @Autowired
    private GameSlotServiceImpl gameSlotService;
    @Resource
    private CommonPersistence commonPersistence;

    RestTemplate restTemplate = new RestTemplate();
    SimpleClientHttpRequestFactory clientHttpRequestFactory = new SimpleClientHttpRequestFactory();

    /**
     * 获取游戏登录链接
     *
     * @param reqDto {username, lang, device, slotId}
     * @return 登录链接
     */
    @Override
    public PlatFactoryParams.PlatGameLoginResDto login(PlatFactoryParams.PlatLoginReqDto reqDto) {
        var slotLoginBO = CQ9RequestParameter.GameLink.builder()
                .gamecode(reqDto.getSlotId())
                .gameplat(BaseEnum.DEVICE.D.getValue().equals(reqDto.getDevice()) ? "web" : "mobile")
                .usertoken(getUserToken(reqDto))
                .lang(EnumUtils.getEnumIgnoreCase(CQ9RequestParameter.LANGS.class, reqDto.getLang()).getCode())
                .gamehall("CQ9").build();
        JSONObject send = send(config.getApiUrl() + CQ9PlatEnum.CG9MethodEnum.GAMELINK.getMethodName(), slotLoginBO, config.getApiUrl() + CQ9PlatEnum.CG9MethodEnum.GAMELINK.getMethodNameDesc());
        return PlatFactoryParams.PlatGameLoginResDto.builder().type(1).url(send.getJSONObject(DATA).getString("url")).build();
    }

    /**
     * 获取userToken
     *
     * @param reqDto
     * @return
     */
    public String getUserToken(PlatFactoryParams.PlatLoginReqDto reqDto) {
        var loginBO = CQ9RequestParameter.Login.builder().account(reqDto.getUsername()).password(reqDto.getUsername()).build();
        JSONObject call = send(config.getApiUrl() + CQ9PlatEnum.CG9MethodEnum.LOGIN.getMethodName(), loginBO, CQ9PlatEnum.CG9MethodEnum.LOGIN.getMethodNameDesc());
        return call.getJSONObject(DATA).getString("usertoken");
    }

    /**
     * @param url
     * @param object
     * @param methodNameDesc
     * @return
     */
    public JSONObject send(String url, Object object, String methodNameDesc) {
        try {
            HttpHeaders headers = new HttpHeaders();
            MultiValueMap<String, Object> paramsMap = new LinkedMultiValueMap<>();
            Map<String, Object> itemMap = JSON.toJavaObject(parseObject(toJSONString(object)), Map.class);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", config.getToken());
            paramsMap.setAll(itemMap);
            HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(paramsMap, headers);
            log.info("请求参数：" + httpEntity);
            String result = restTemplate.postForObject(url, httpEntity, String.class);
            log.info("三方返回内容" + result);
            JSONObject jsonObject = parseObject(result);
            Integer code = parseObject(jsonObject.getString(STATUS)).getInteger("code");
            if (code == 0) {
                return parseObject(result);
            } else if (code == 14 || code == 2) {
                registerUser(PlatFactoryParams.PlatRegisterReqDto.builder().username(String.valueOf(itemMap.get("account"))).build());
                login((PlatFactoryParams.PlatLoginReqDto) object);
            } else {
                throw new BusinessException(CQ9PlatEnum.METHOD_MAP.getOrDefault(code, CodeInfo.PLAT_SYSTEM_ERROR));
            }
        } catch (Exception e) {
            XxlJobLogger.log(MODEL + methodNameDesc + "异常:" + e.getMessage());
            log.error(MODEL + methodNameDesc + "失败!" + e.getMessage());
            throw e;
        }
        return null;
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
        try {
            // 测试环境上方金额不能大于100
            if (!"PROD".equals(config.getEnvironment()) && reqDto.getCoin().compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new BusinessException(CodeInfo.PLAT_COIN_UP_COIN_NOT_GT_100);
            }
            var depositBO = CQ9RequestParameter.Deposit.builder()
                    .account(reqDto.getUsername())
                    .amount(reqDto.getCoin())
                    .mtcode(reqDto.getOrderId())
                    .build();
            var send = send(config.getApiUrl() + CQ9PlatEnum.CG9MethodEnum.DOPOSIT.getMethodName(), depositBO, CQ9PlatEnum.CG9MethodEnum.DOPOSIT.getMethodNameDesc());
            BigDecimal afterAmount = BigDecimal.ZERO;
            if (send.getJSONObject(STATUS).getInteger("code") == 0) {
                afterAmount = send.getJSONObject(DATA).getBigDecimal(BALANCE);
            }
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
     * reqDto
     *
     * @param reqDto {"coin","orderId","username"}
     * @return
     */
    @SneakyThrows
    @Override
    public PlatFactoryParams.PlatCoinTransferResDto coinDown(PlatFactoryParams.PlatCoinTransferReqDto reqDto) {
        try {
            var withdrawBO = CQ9RequestParameter.Withdraw.builder()
                    .account(reqDto.getUsername())
                    .amount(reqDto.getCoin())
                    .mtcode(reqDto.getOrderId())
                    .build();
            var send = send(config.getApiUrl() + CQ9PlatEnum.CG9MethodEnum.WITHDRAWW.getMethodName(), withdrawBO, CQ9PlatEnum.CG9MethodEnum.WITHDRAWW.getMethodNameDesc());
            BigDecimal afterAmount = BigDecimal.ZERO;
            if (send.getJSONObject(STATUS).getInteger("code") == 0) {
                afterAmount = send.getJSONObject(DATA).getBigDecimal(BALANCE);
            }
            coinPlatTransfersBase.updateOrderPlat(reqDto.getOrderId(), 1, reqDto.getCoin(), null, null);
            return PlatFactoryParams.PlatCoinTransferResDto.builder().platCoin(afterAmount).build();
        } catch (Exception e) {
            log.error(e.toString());
            coinPlatTransfersBase.updateOrderPlat(reqDto.getOrderId(), 2, reqDto.getCoin(), "", e.toString());
            throw e;
        }

    }


    /**
     * 查询余额
     *
     * @param reqDto
     * @return
     */
    @Override
    public PlatFactoryParams.PlatQueryBalanceResDto queryBalance(PlatFactoryParams.PlatQueryBalanceReqDto reqDto) {
        var balanceBO = CQ9RequestParameter.Balance.builder().account(reqDto.getUsername()).build();
        var call = sendGet(config.getApiUrl() + CQ9PlatEnum.CG9MethodEnum.BALANCE.getMethodName() + reqDto.getUsername(), balanceBO, CQ9PlatEnum.CG9MethodEnum.BALANCE.getMethodNameDesc());
        return PlatFactoryParams.PlatQueryBalanceResDto.builder().platCoin(parseObject(call).getBigDecimal(BALANCE)).build();
    }

    /**
     * get请求
     *
     * @param url
     * @param object
     * @param methodNameDesc
     * @return
     */

    private String sendGet(String url, Object object, String methodNameDesc) {
        try {
            clientHttpRequestFactory.setConnectTimeout(1000 * 3 * 60);
            restTemplate.setRequestFactory(clientHttpRequestFactory);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", config.getToken());
            Map<String, Object> itemMap = JSON.toJavaObject(parseObject(toJSONString(object)), Map.class);
            HttpEntity request = new HttpEntity(headers);
            //异常处理
            ExceptionThreadLocal.setRequestParams(toJSONString(object));
            var result = restTemplate.exchange(url, HttpMethod.GET, request, String.class, itemMap).getBody();
            JSONObject status = parseObject(result).getJSONObject(STATUS);
            if (status.getInteger("code") == 0) {
                return parseObject(result).getString(DATA);
            } else if (methodNameDesc.equals(CQ9PlatEnum.CG9MethodEnum.GETGAMEORDER.getMethodNameDesc()) && status.getInteger("code") == 8) {
                return parseObject(result).getString(DATA);
            } else {
                log.info(MODEL + methodNameDesc + "失败!" + status.getString("message"));
                throw new BusinessException(CQ9PlatEnum.METHOD_MAP.getOrDefault(status.getInteger("code"), CodeInfo.PLAT_SYSTEM_ERROR));
            }
        } catch (Exception e) {
            log.error(MODEL + methodNameDesc + "失败!" + e.getMessage());
            throw e;
        }
    }


    /**
     * 注单拉取
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void pullBetsLips() {
        var page = 1;
        var pages = 1;
        do {
            var pullEndTime = jedisUtil.hget(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH, RedisConstant.CQ9);
            XxlJobLogger.log(MODEL + "进入拉单==================");
            if (Strings.isEmpty(pullEndTime)) {
                var cq9Game = betslipsCq9GameServiceImpl.getOne(new LambdaQueryWrapper<BetslipsCq9Game>()
                        .orderByDesc(BetslipsCq9Game::getCreatedAt).last("limit 1"));
                pullEndTime = (cq9Game != null && cq9Game.getCreatedAt() != null) ? String.valueOf(cq9Game.getCreatedAt()) : String.valueOf(DateNewUtils.now() - 60 * 60 * 24);
            }
            var timeMark = Integer.parseInt(pullEndTime);
            var time = DateNewUtils.utc4Str(timeMark, DateNewUtils.Format.yyyy_MM_dd);
            var startTime = DateNewUtils.utc4Str(timeMark, DateNewUtils.Format.yyyy_MM_dd_T_HH_mm_ss);
            var pullBetsDataBO = CQ9RequestParameter.Order.builder().starttime(startTime + "-04:00").endtime(time + "T23:59:59.999-04:00").page(page).build();
            var call = sendGet(config.getApiUrl() + CQ9PlatEnum.CG9MethodEnum.GETGAMEORDER.getMethodName(), pullBetsDataBO, CQ9PlatEnum.CG9MethodEnum.GETGAMEORDER.getMethodNameDesc());
            XxlJobLogger.log(MODEL + "进入拉单:" + call);
            if (call != null) {
                insertCQ9Data(call);
                var totalSize = parseObject(call).getDouble("TotalSize");
                pages = (int) Math.ceil(totalSize / 500);
                if (pages > page) page++;
            } else {
                XxlJobLogger.log(MODEL + "当前无注单" + call);
            }
            if (timeMark + 60 * 30 < DateNewUtils.now())
                jedisUtil.hset(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH, RedisConstant.CQ9, String.valueOf(timeMark + 60 * 30));
        } while (page < pages);

    }

    /**
     * 数据入库
     *
     * @param call
     * @return
     */
    private void insertCQ9Data(String call) {
        List<BetslipsCq9Game> betslipsCq9list = new ArrayList<>();
        Map<String, Integer> userMap = userServiceBase.getUsernameIdMap();
        JSONArray jsonArray = parseObject(call).getJSONArray("Data");
        if (!jsonArray.isEmpty()) {
            for (var x : jsonArray) {
                var betDetailBo = parseObject(toJSONString(x), CQ9RequestParameter.BetDetail.class);
                var userName = userServiceBase.filterUsername(betDetailBo.getAccount());
                var uid = userMap.get(userName);
                if (StringUtils.isEmpty(userName) || uid == null) {
                    continue;
                }
                var retBetslipsCq9 = BeanConvertUtils.copyProperties(betDetailBo, BetslipsCq9Game::new, (bo, sb) -> {
                    sb.setXbUsername(userName);
                    sb.setXbUid(uid);
                    sb.setXbCoin(bo.getBet());
                    sb.setXbValidCoin(bo.getBet());
                    sb.setXbStatus(transferStatus(bo.getWin().subtract(bo.getBet())));
                    sb.setXbProfit(bo.getWin().subtract(bo.getBet()));
                    sb.setCreatedAt(DateNewUtils.utc8Int((int) bo.getEndRoundTime().toInstant().getEpochSecond()));
                    sb.setUpdatedAt(DateNewUtils.now());
                });
                betslipsCq9list.add(retBetslipsCq9);
            }
            Map<String, List<BetslipsCq9Game>> fish = betslipsCq9list.stream().filter(x -> x.getGameType().equals("fish")).collect(Collectors.groupingBy(BetslipsCq9Game::getGameCode));
            List<BetslipsCq9Game> slot = betslipsCq9list.stream().filter(x -> !x.getGameType().equals("fish")).collect(Collectors.toList());
            betslipsCq9GameServiceImpl.saveOrUpdateBatch(BeanConvertUtils.copyListProperties(slot, BetslipsCq9Game::new), 1000);
            betslipsCq9ParadiseServiceImpl.saveOrUpdateBatch(BeanConvertUtils.copyListProperties(fish.get("AB3"), BetslipsCq9Paradise::new), 1000);
            betslipsCq9ThunderFighterServiceImpl.saveOrUpdateBatch(BeanConvertUtils.copyListProperties(fish.get("AT04"), BetslipsCq9ThunderFighter::new), 1000);
            betslipsCq9WaterMarginServiceImpl.saveOrUpdateBatch(BeanConvertUtils.copyListProperties(fish.get("AT06"), BetslipsCq9WaterMargin::new), 1000);
        } else {
            XxlJobLogger.log(MODEL + "失败!" + toJSONString(call));
            PLAT_BET_SLIPS_EXCEPTION_CATEGORY_1.setMsg(toJSONString(call));
            throw new BusinessException(PLAT_BET_SLIPS_EXCEPTION_CATEGORY_1);
        }

    }

    /**
     * 检查转账状态
     *
     * @author: David
     * @date: 04/05/2020
     */
    @Override
    public Boolean checkTransferStatus(String orderId) {
        var checkTransationBO = CQ9RequestParameter.CheckTransaction.builder()
                .mtcode(orderId)
                .build();
        var call = sendGet(config.getApiUrl() + CQ9PlatEnum.CG9MethodEnum.CHECKTRANSACTION.getMethodName() + orderId, checkTransationBO, CQ9PlatEnum.CG9MethodEnum.CHECKTRANSACTION.getMethodNameDesc());
        if (call != null && parseObject(call).getJSONObject(STATUS).getInteger("code") == 0) {
            var status = parseObject(call).getJSONObject(DATA).getString(STATUS);
            if (status.equals("Success")) {
                return true;
            } else if (status.equals("Failed")) {
                return false;
            }
        } else {
            throw new BusinessException(TCGPlatEnum.map.getOrDefault(parseObject(call).getJSONObject(STATUS).getInteger("code"), CodeInfo.PLAT_SYSTEM_ERROR));
        }
        throw new BusinessException(CodeInfo.STATUS_CODE_ERROR);
    }

    /**
     * 注册用户
     *
     * @param reqDto 创建会员信息
     * @return
     */
    @Override
    public Boolean registerUser(PlatFactoryParams.PlatRegisterReqDto reqDto) {
        var registerBO = CQ9RequestParameter.Player.builder().account(reqDto.getUsername()).password(reqDto.getUsername()).build();
        JSONObject call = send(config.getApiUrl() + CQ9PlatEnum.CG9MethodEnum.CREATEPLAYER.getMethodName(), registerBO, CQ9PlatEnum.CG9MethodEnum.CREATEPLAYER.getMethodNameDesc());
        return call != null;
    }

    @Override
    public Page<PlatFactoryParams.PlatSlotGameResDto> getSlotGameList
            (ReqPage<PlatFactoryParams.PlatSlotGameReqDto> platSlotgameReqDto) {
        return slotServiceBase.getSlotGameList(platSlotgameReqDto);
    }

    @Override
    public Boolean favoriteSlotGame(PlatFactoryParams.PlatSlotGameFavoriteReqDto platSlotGameFavoriteReqDto) {
        return slotServiceBase.favoriteSlotGame(platSlotGameFavoriteReqDto);
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
     * 同步老虎机游戏列表
     */
    @Override
    public void pullGames() {
        var GameListBO = CQ9RequestParameter.GameList.builder().gamehall(MODEL).build();
        var call = sendGet(config.getApiUrl() + CQ9PlatEnum.CG9MethodEnum.GAMELIST.getMethodName() + MODEL, GameListBO, CQ9PlatEnum.CG9MethodEnum.GAMELIST.getMethodNameDesc());
        JSONArray jsonArray = parseArray(call);
        if (jsonArray != null && !jsonArray.isEmpty()) {
            GameSlot gameSlot = new GameSlot();
            var device = 0;
            for (var x : jsonArray) {
                JSONObject jo = (JSONObject) x;
                gameSlot.setGameId(205);
                gameSlot.setStatus(1);
                gameSlot.setId(jo.getString("gamecode"));
                gameSlot.setName(jo.getString("gamename"));
                gameSlot.setImg(String.format("/icon/cq9/%s.png", jo.getString("gamename").replace(" ", "")));
                gameSlot.setGameTypeName(jo.getString("gametype"));
                gameSlot.setCreatedAt(DateUtils.getCurrentTime());
                gameSlot.setUpdatedAt(DateUtils.getCurrentTime());
                gameSlot.setGameTypeId(jo.getString("gametype"));
                gameSlot.setDevice(device);
                gameSlot.setSort(0);
                gameSlot.setIsNew(0);
                gameSlotService.saveOrUpdate(gameSlot);
            }

        }
    }


    /**
     * 根据起始、结束时间 生成补单信息
     * 详细数据:注单- 10天的期限
     *
     * @param dto dto.start 开始时间 dto.end结束时间
     */
    @Override
    public void genSupplementsOrders(PlatFactoryParams.GenSupplementsOrdersReqDto dto) {
        if (ZonedDateTime.now().minusDays(90).toInstant().getEpochSecond() > dto.getStart()) {
            throw new BusinessException(CodeInfo.PLAT_BET_SLIPS_SUPPLE_OVER_90_DAYS);
        } else {
            ZonedDateTime now = ZonedDateTime.now();
            Integer currentTime = (int) now.toInstant().getEpochSecond();
            LinkedList<BetSlipsSupplemental> list = new LinkedList<>();
            int i = Integer.parseInt(DateNewUtils.utc4Str(dto.getEnd(), DateNewUtils.Format.yyyyMMdd)) - Integer.parseInt(DateNewUtils.utc4Str(dto.getStart(), DateNewUtils.Format.yyyyMMdd));
            for (int j = 0; j < i + 1; j++) {
                var pullBetsDataBO = new JSONObject();
                pullBetsDataBO.put("starttime", DateNewUtils.utc4Str(dto.getEnd(), DateNewUtils.Format.yyyy_MM_dd) + "T01:00:00-04:00");
                pullBetsDataBO.put("endtime", DateNewUtils.utc4Str(dto.getEnd(), DateNewUtils.Format.yyyy_MM_dd) + "T23:59:59.999-04:00");
                BetSlipsSupplemental po = PlatAbstractFactory
                        .buildBetSlipsSupplemental(dto.getGameId(), DateNewUtils.utc8Zoned(dto.getStart()).toString(), DateNewUtils.utc8Zoned(dto.getEnd()).toString(), toJSONString(pullBetsDataBO), currentTime);
                list.add(po);
            }
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
            String call = sendGet(config.getApiUrl() + CQ9PlatEnum.CG9MethodEnum.GETGAMEORDER.getMethodName(), parseObject(toJSONString(jsonObject)), CQ9PlatEnum.CG9MethodEnum.GETGAMEORDER.getMethodNameDesc());
            XxlJobLogger.log(MODEL + "进入补单:" + call);
            insertCQ9Data(call);
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
}
