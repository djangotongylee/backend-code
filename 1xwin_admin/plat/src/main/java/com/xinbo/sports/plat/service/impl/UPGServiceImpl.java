package com.xinbo.sports.plat.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinbo.sports.dao.generator.po.BetSlipsSupplemental;
import com.xinbo.sports.dao.generator.po.BetslipsUpg;
import com.xinbo.sports.dao.generator.po.GameSlot;
import com.xinbo.sports.dao.generator.service.BetslipsUpgService;
import com.xinbo.sports.dao.generator.service.impl.GameSlotServiceImpl;
import com.xinbo.sports.service.base.CoinPlatTransfersBase;
import com.xinbo.sports.plat.base.CommonPersistence;
import com.xinbo.sports.plat.base.SlotServiceBase;
import com.xinbo.sports.plat.factory.ISuppleMethods;
import com.xinbo.sports.plat.factory.PlatAbstractFactory;
import com.xinbo.sports.plat.factory.PlatSlotAbstractFactory;
import com.xinbo.sports.plat.io.bo.MGRequestParameter;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams;
import com.xinbo.sports.plat.io.bo.UPGRequestParameter;
import com.xinbo.sports.plat.io.enums.BasePlatParam;
import com.xinbo.sports.plat.io.enums.MGPlatEnum;
import com.xinbo.sports.plat.io.enums.UPGPlatEnum;
import com.xinbo.sports.service.base.ExceptionThreadLocal;
import com.xinbo.sports.service.base.UserServiceBase;
import com.xinbo.sports.service.cache.KeyConstant;
import com.xinbo.sports.service.common.RedisConstant;
import com.xinbo.sports.service.exception.BusinessException;
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
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.alibaba.fastjson.JSON.*;
import static com.xinbo.sports.utils.components.response.CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_1;

@Slf4j
@Service("UPGServiceImpl")
public class UPGServiceImpl implements PlatSlotAbstractFactory, ISuppleMethods {
    @Autowired
    private JedisUtil jedisUtil;
    @Resource
    private CoinPlatTransfersBase coinPlatTransfersBase;
    @Autowired
    private BetslipsUpgService betslipsUpgServiceImpl;
    @Resource
    private UserServiceBase userServiceBase;
    @Resource
    private SlotServiceBase slotServiceBase;
    @Autowired
    private GameSlotServiceImpl gameSlotService;
    @Resource
    private CommonPersistence commonPersistence;
    SimpleClientHttpRequestFactory clientHttpRequestFactory = new SimpleClientHttpRequestFactory();

    RestTemplate restTemplate = new RestTemplate();
    protected static final String MODEL = "UPG";
    /*访问id*/
    private static final String CLIENT_ID = "client_id";
    /*访问密钥*/
    private static final String CLIENT_SECRET = "client_secret";
    private static final String GRANT_TYPE = "grant_type";
    private static final String TOKEN_TYPE = "Bearer ";
    @Setter
    UPGPlatEnum.PlatConfig config;


    /**
     * 获取游戏登录链接
     *
     * @param reqDto {username, lang, device, slotId}
     * @return 登录链接
     */
    @Override
    public PlatFactoryParams.PlatGameLoginResDto login(PlatFactoryParams.PlatLoginReqDto reqDto) {
        var loginBO = UPGRequestParameter.Login.builder()
                .langCode(EnumUtils.getEnumIgnoreCase(MGRequestParameter.LANGS.class, reqDto.getLang()).getCode())
                .contentCode(reqDto.getSlotId() == null ? "UPG_treasureHeroes" : reqDto.getSlotId())
                .platform(reqDto.getDevice().equals("d") ? "Mobile" : "Desktop").build();
        String methodName = config.getApiUrl() + config.getClientId() + "/" + UPGPlatEnum.UPGMethodEnum.GETCONTENTURL.getMethodName();
        String loginUrl = String.format(methodName, reqDto.getUsername());
        JSONObject call = send(loginUrl, loginBO, UPGPlatEnum.UPGMethodEnum.GETCONTENTURL.getMethodNameDesc());
        return PlatFactoryParams.PlatGameLoginResDto.builder().type(1).url(call.getString("url")).build();
    }

    /**
     * post请求
     *
     * @param url
     * @param object
     * @return
     */
    private JSONObject send(String url, Object object, String methodNameDesc) {
        try {
            String result = null;
            HttpHeaders headers = new HttpHeaders();
            MultiValueMap<String, Object> paramsMap = new LinkedMultiValueMap<>();
            Map<String, Object> itemMap = JSON.toJavaObject(parseObject(toJSONString(object)), Map.class);
            headers.set("Authorization", getToken());
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            paramsMap.setAll(itemMap);
            HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(paramsMap, headers);
            result = restTemplate.postForObject(url, httpEntity, String.class);
            return parseObject(result);
        } catch (Exception e) {
            XxlJobLogger.log("UPG:" + methodNameDesc + "异常:" + e.getMessage());
            log.error("UPG" + methodNameDesc + "失败!" + e.getMessage());
            throw e;
        }
    }


    @Override
    public PlatFactoryParams.PlatLogoutResDto logout(PlatFactoryParams.PlatLogoutReqDto dto) {
        return null;
    }

    /**
     * 第三方上分
     *
     * @param platCoinTransferUpReqDto {"coin","orderId","username"}
     * @return
     */
    @SneakyThrows
    @Override
    public PlatFactoryParams.PlatCoinTransferResDto coinUp(PlatFactoryParams.PlatCoinTransferReqDto platCoinTransferUpReqDto) {
        // 测试环境上方金额不能大于100
        if (!"PROD".equals(config.getEnvironment()) && platCoinTransferUpReqDto.getCoin().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BusinessException(CodeInfo.PLAT_COIN_UP_COIN_NOT_GT_100);
        }
        return transfer(platCoinTransferUpReqDto, 1);
    }

    /**
     * 第三方额度转换
     *
     * @param reqDto
     * @param transferId
     * @return
     * @throws IOException
     */
    private PlatFactoryParams.PlatCoinTransferResDto transfer(PlatFactoryParams.PlatCoinTransferReqDto reqDto, Integer transferId) throws IOException {
        try {
            var transferBO = UPGRequestParameter.FundTransfer.builder()
                    .playerId(reqDto.getUsername()).amount(reqDto.getCoin())
                    .type(transferId.equals(1) ? "Deposit" : "Withdraw").externalTransactionId(String.valueOf(reqDto.getOrderId())).build();
            JSONObject call = send(config.getApiUrl() + config.getClientId() + "/" + UPGPlatEnum.UPGMethodEnum.CREATETRANSACTION.getMethodName(), transferBO, UPGPlatEnum.UPGMethodEnum.CREATETRANSACTION.getMethodNameDesc());

            BigDecimal afterAmount = BigDecimal.ZERO;
            if (call.get("status").equals("Succeeded")) {
                afterAmount = queryBalance(PlatFactoryParams.PlatQueryBalanceReqDto.builder().username(reqDto.getUsername()).build()).getPlatCoin();
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
     *
     * @param platCoinTransferDownReqDto {"coin","orderId","username"}
     * @return
     */
    @SneakyThrows
    @Override
    public PlatFactoryParams.PlatCoinTransferResDto coinDown(PlatFactoryParams.PlatCoinTransferReqDto platCoinTransferDownReqDto) {
        return transfer(platCoinTransferDownReqDto, 2);
    }

    /**
     * 查询余额
     *
     * @param reqDto
     * @return
     */
    @Override
    public PlatFactoryParams.PlatQueryBalanceResDto queryBalance(PlatFactoryParams.PlatQueryBalanceReqDto reqDto) {
        var balanceBO = UPGRequestParameter.QueryBalance.builder().properties("balance").build();
        String call = null;
        String methodName = config.getApiUrl() + config.getClientId() + "/" + UPGPlatEnum.UPGMethodEnum.GETPLAYERDETAILS.getMethodName();
        if (java.lang.Boolean.TRUE.equals(registerUser(PlatFactoryParams.PlatRegisterReqDto.builder().username(reqDto.getUsername()).build()))) {
            String balanceUrl = String.format(methodName, reqDto.getUsername(), UPGPlatEnum.UPGMethodEnum.GETPLAYERDETAILS.getMethodNameDesc());
            call = sendGet(balanceUrl, balanceBO, UPGPlatEnum.UPGMethodEnum.GETPLAYERDETAILS.getMethodNameDesc());
        }
        return PlatFactoryParams.PlatQueryBalanceResDto.builder().platCoin(parseObject(call).getJSONObject("balance").getBigDecimal("total")).build();
    }

    /**
     * get方法
     *
     * @param url
     * @param object
     * @param methodNameDesc
     * @return
     */
    private String sendGet(String url, Object object, String methodNameDesc) {
        ResponseEntity<String> result = null;
        try {
            clientHttpRequestFactory.setConnectTimeout(1000 * 3 * 60);
            restTemplate.setRequestFactory(clientHttpRequestFactory);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", getToken());
            Map<String, Object> itemMap = JSON.toJavaObject(parseObject(toJSONString(object)), Map.class);
            HttpEntity request = new HttpEntity(headers);
            ExceptionThreadLocal.setRequestParams(toJSONString(object));
            result = restTemplate.exchange(url, HttpMethod.GET, request, String.class, itemMap);
            if (result.getStatusCode().value() != 200 && result.getStatusCode().value() != 201) {
                throw new BusinessException(MGPlatEnum.METHOD_MAP.get(methodNameDesc).getOrDefault(result.getStatusCode().value(), CodeInfo.PLAT_SYSTEM_ERROR));
            }
        } catch (Exception e) {
            if (methodNameDesc.contains("下注")) {
                CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0.setMsg(e.getMessage());
                throw new BusinessException(CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0);
            } else if (e.getMessage().contains("403")) {
                throw new BusinessException(CodeInfo.PLAT_IP_NOT_ACCESS);
            } else {
                throw e;
            }
        }
        return result.getBody();
    }

    /**
     * 注单拉取
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void pullBetsLips() {
        String pullEndId = jedisUtil.hget(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH, RedisConstant.UPG);
        if (Strings.isEmpty(pullEndId)) {
            BetslipsUpg betslipsUpg = betslipsUpgServiceImpl.getOne(new LambdaQueryWrapper<BetslipsUpg>()
                    .orderByDesc(BetslipsUpg::getCreatedAt).last("limit 1"));
            pullEndId = (betslipsUpg != null && betslipsUpg.getId() != null) ? betslipsUpg.getId() : "0";
        }
        UPGRequestParameter.GetBetsDetails pullBetsDataBO = UPGRequestParameter.GetBetsDetails.builder()
                .limit(1000).startingAfter(pullEndId).build();
        var path = UPGPlatEnum.UPGMethodEnum.GETBETSDETAILS.getMethodName() + "&startingAfter={startingAfter}";
        String call = sendGet(config.getApiUrl() + config.getClientId() + "/" + path, pullBetsDataBO, UPGPlatEnum.UPGMethodEnum.GETBETSDETAILS.getMethodNameDesc());
        if (call != null) {
            var pullEndIndex = insertUPGData(call);
            pullEndId = pullEndIndex.contains("_") && !"0".equals(pullEndIndex.split("_")[1]) ? pullEndIndex.split("_")[1] : pullEndId;
            jedisUtil.hset(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH, RedisConstant.UPG, pullEndId);
        }

    }


    private String insertUPGData(String call) {
        var now = DateUtils.getCurrentTime();
        Integer pullEndTime = DateNewUtils.now();
        String pullEndId = "0";
        if (call != null) {
            JSONArray jsonArray = parseArray(call);
            List<BetslipsUpg> betslipsUpglist = new ArrayList<>();
            Map<String, Integer> userMap = userServiceBase.getUsernameIdMap();
            if (jsonArray != null && !jsonArray.isEmpty()) {
                for (var x : jsonArray) {
                    var betDetailBo = parseObject(toJSONString(x), UPGRequestParameter.BetDetail.class);
                    var userName = userServiceBase.filterUsername(betDetailBo.getPlayerId());
                    var uid = userMap.get(userName);
                    if (StringUtils.isEmpty(userName) || uid == null) {
                        continue;
                    }
                    var retBetslipsUpg = BeanConvertUtils.copyProperties(betDetailBo, BetslipsUpg::new, (bo, sb) -> {
                        sb.setXbUsername(userName);
                        sb.setXbUid(uid);
                        sb.setXbCoin(bo.getBetAmount());
                        sb.setXbValidCoin(bo.getBetAmount());
                        sb.setXbStatus(transferStatus(bo.getPayoutAmount().subtract(bo.getBetAmount())));
                        sb.setXbProfit(bo.getPayoutAmount().subtract(bo.getBetAmount()));
                        sb.setCreatedAt((int) (bo.getCreatedDateutc().getTime() / 1000) + 8 * 60 * 60);
                        sb.setUpdatedAt(now);
                    });
                    betslipsUpglist.add(retBetslipsUpg);
                }
                betslipsUpgServiceImpl.saveOrUpdateBatch(betslipsUpglist, 1000);
                pullEndId = ((JSONObject) jsonArray.get(jsonArray.size() - 1)).get("betUID").toString();
                pullEndTime = DateNewUtils.oriTimeZoneToBeiJingZone(((JSONObject) jsonArray.get(jsonArray.size() - 1)).get("createdDateUTC").toString(),
                        DateNewUtils.Format.yyyy_MM_dd_T_HH_mm_ss_SSS, "UTC", "+8") + 60 * 60 * 12;
            } else if (jsonArray.size() == 0) {
                log.info(MODEL + "当前无新注单");
            } else {
                XxlJobLogger.log(MODEL + "数据插入异常!" + toJSONString(call));
                PLAT_BET_SLIPS_EXCEPTION_CATEGORY_1.setMsg(toJSONString(call));
                throw new BusinessException(PLAT_BET_SLIPS_EXCEPTION_CATEGORY_1);
            }
        }
        return pullEndTime + "_" + pullEndId;
    }

    /**
     * 检查转账状态
     *
     * @author: David
     * @date: 04/05/2020
     */
    @Override
    public Boolean checkTransferStatus(String orderId) {
        String call = null;
        try {
            var checkTransationBO = UPGRequestParameter.CheckTransaction.builder()
                    .idempotencyKey(orderId)
                    .build();
            call = sendGet(config.getApiUrl() + config.getClientId() + "/" + UPGPlatEnum.UPGMethodEnum.CHECKTRANSATIONS.getMethodName(), checkTransationBO, UPGPlatEnum.UPGMethodEnum.CHECKTRANSATIONS.getMethodNameDesc());
            if (call != null && parseObject(call).get("status").equals("Succeeded")) {
                return true;
            }
        } catch (Exception e) {
            if (e.getMessage().contains("TransactionDoesNotExist")) {
                return false;
            } else {
                throw new BusinessException(CodeInfo.PLAT_SYSTEM_ERROR);
            }
        }
        throw new BusinessException(CodeInfo.PLAT_SYSTEM_ERROR);
    }


    /**
     * 用户注册
     *
     * @param reqDto 创建会员信息
     * @return
     */
    @Override
    public Boolean registerUser(PlatFactoryParams.PlatRegisterReqDto reqDto) {
        var registerBO = UPGRequestParameter.Register.builder().playerId(reqDto.getUsername()).build();
        JSONObject call = send(config.getApiUrl() + config.getClientId() + "/" + UPGPlatEnum.UPGMethodEnum.CREATEPLAYER.getMethodName(), registerBO, UPGPlatEnum.UPGMethodEnum.CREATEPLAYER.getMethodNameDesc());
        return call != null;
    }

    /**
     * token获取
     *
     * @return
     */
    public String getToken() {
        String accesToken = null;
        String url = null;
        try {
            accesToken = jedisUtil.get(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH + ":" + RedisConstant.UPG + ": token :");
            if (accesToken != null) {
                return accesToken;
            } else {
                String clientId = config.getClientId();
                String clientSecret = config.getClientSecret();
                String grantType = config.getGrantType();
                url = config.getTokenUrl() + UPGPlatEnum.UPGMethodEnum.TOKEN.getMethodName();
                String params = GRANT_TYPE + "=" + grantType + "&" + CLIENT_ID + "=" + clientId + "&" + CLIENT_SECRET + "=" + clientSecret;
                log.info("请求参数：" + params);
                JSONObject result = parseObject(HttpUtils.postHttp(url, params));
                accesToken = TOKEN_TYPE + result.get("access_token");
                log.info("三方返回内容" + result);
                jedisUtil.setex(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH + ":" + RedisConstant.UPG + ":token:", ((Integer) result.get("expires_in")) - 5, accesToken);
                return accesToken;
            }

        } catch (Exception e) {
            log.error("获取三方法数据失败,执行方法;" + e.getMessage());
            return null;
        }
    }

    @Override
    public Page<PlatFactoryParams.PlatSlotGameResDto> getSlotGameList(ReqPage<PlatFactoryParams.PlatSlotGameReqDto> platSlotgameReqDto) {
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
        var gamesBO = UPGRequestParameter.GetGames.builder()
                .agentCode(config.getClientId())
                .build();
        String call = sendGet(config.getApiUrl() + config.getClientId() + "/" + UPGPlatEnum.UPGMethodEnum.GETGAMES.getMethodName(), gamesBO, UPGPlatEnum.UPGMethodEnum.GETGAMES.getMethodNameDesc());
        JSONArray jsonArray = parseArray(call);
        if (jsonArray != null && !jsonArray.isEmpty()) {
            GameSlot gameSlot = new GameSlot();
            var device = 0;
            for (var x : jsonArray) {
                JSONObject jo = (JSONObject) x;
                if (jo.get("channelCode").equals("UPGSLOTS")) {
                    if (jo.get("platform").equals("Multi-Platform")) {
                        device = 0;
                    } else if (jo.get("platform").equals("Desktop")) {
                        device = 1;
                    } else {
                        device = 2;
                    }
                    gameSlot.setGameId(204);
                    gameSlot.setStatus(1);
                    gameSlot.setId((String) jo.get("gameCode"));
                    gameSlot.setName((String) jo.get("gameName"));
                    gameSlot.setImg(String.format("/icon/upg/%s.png", jo.get("gameName").toString().replace(" ", "")));
                    gameSlot.setGameTypeName(String.valueOf(jo.get("gameCategoryName")));
                    gameSlot.setGameTypeId((String) jo.get("gameCategoryCode"));
                    gameSlot.setNameZh(jo.getJSONArray("translatedGameName").getJSONObject(0).getString("value"));
                    gameSlot.setCreatedAt(DateUtils.getCurrentTime());
                    gameSlot.setUpdatedAt(DateUtils.getCurrentTime());
                    gameSlot.setDevice(device);
                    gameSlot.setSort(jo.getInteger("sorting"));
                    gameSlot.setIsNew(0);
                    gameSlotService.saveOrUpdate(gameSlot);

                }
            }
        }
    }

    /**
     * 根据起始、结束时间 生成补单信息
     * 不能超过10天
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
            BetslipsUpg startOne = betslipsUpgServiceImpl.getOne(new QueryWrapper<BetslipsUpg>().orderByDesc("created_at").lt("created_at", dto.getStart()).last("limit 1"));
            var pullDataId = startOne != null && startOne.getId() != null ? startOne.getId() : "0";
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
        int currentTime;
        JSONObject jsonObject = parseObject(dto.getRequestInfo());
        if (jsonObject.containsKey("end")) {
            endTime = jsonObject.getInteger("end");
            jsonObject.remove("end");
        }
        do {
            String path = MGPlatEnum.MGMethodEnum.GETBETSDETAILS.getMethodName() + "&startingAfter={startingAfter}";
            String call = sendGet(config.getApiUrl() + config.getClientId() + "/" + path, parseObject(toJSONString(jsonObject)), MGPlatEnum.MGMethodEnum.GETBETSDETAILS.getMethodNameDesc());
            XxlJobLogger.log(MODEL + "进入补单:" + call);
            String pullIndex = insertUPGData(call);
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

}