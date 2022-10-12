package com.xinbo.sports.plat.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xinbo.sports.dao.generator.po.BetSlipsSupplemental;
import com.xinbo.sports.dao.generator.po.BetslipsBti;
import com.xinbo.sports.dao.generator.service.BetslipsBtiService;
import com.xinbo.sports.plat.base.CommonPersistence;
import com.xinbo.sports.plat.factory.PlatAbstractFactory;
import com.xinbo.sports.plat.factory.PlatSportsAbstractFactory;
import com.xinbo.sports.plat.io.bo.BTIRequestParameter;
import com.xinbo.sports.plat.io.bo.BTIResponse;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams;
import com.xinbo.sports.plat.io.dto.base.BetslipsDetailDto;
import com.xinbo.sports.plat.io.enums.BTIPlatEnum;
import com.xinbo.sports.plat.io.enums.BasePlatParam;
import com.xinbo.sports.service.base.CoinPlatTransfersBase;
import com.xinbo.sports.service.base.UserServiceBase;
import com.xinbo.sports.service.cache.KeyConstant;
import com.xinbo.sports.service.cache.redis.ConfigCache;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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

import static com.alibaba.fastjson.JSON.*;
import static com.xinbo.sports.plat.io.enums.BTIPlatEnum.BTIMethodEnum.*;
import static com.xinbo.sports.plat.io.enums.BTIPlatEnum.Nation_MAP;
import static com.xinbo.sports.utils.components.response.CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_1;

@Slf4j
@Service("BTIServiceImpl")
public class BTIServiceImpl implements PlatSportsAbstractFactory {
    protected static final String MODEL = "BTI";
    protected static final String NoError = "NoError";
    @Resource
    protected ConfigCache configCache;
    /*获取参数配置*/
    @Setter
    protected BTIPlatEnum.PlatConfig config;
    @Resource
    private JedisUtil jedisUtil;
    @Resource
    private CoinPlatTransfersBase coinPlatTransfersBase;
    @Autowired
    private BetslipsBtiService betslipsBtiServiceImpl;
    @Resource
    private UserServiceBase userServiceBase;
    @Resource
    private CommonPersistence commonPersistence;

    RestTemplate restTemplate = new RestTemplate();

    /**
     * 获取游戏登录链接
     *
     * @param reqDto {username, lang, device, slotId}
     * @return 登录链接
     */
    @Override
    public PlatFactoryParams.PlatGameLoginResDto login(PlatFactoryParams.PlatLoginReqDto reqDto) {
        var token = jedisUtil.get(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH + ":" + RedisConstant.BTI + ":" + reqDto.getUsername()) == null || jedisUtil.get(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH + ":" + RedisConstant.BTI + ":" + reqDto.getUsername()).isEmpty() ? getAuthToken(reqDto) : jedisUtil.get(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH + ":" + RedisConstant.BTI + ":" + reqDto.getUsername());
        var login = Nation_MAP.get(configCache.getCountry().toLowerCase());
        var lang = config.getLang().equals(reqDto.getLang()) ? "/" : reqDto.getLang();
        var url = config.getLogUrl() + String.format(login, lang).replace("///", "") + "?token=" + token;
        return PlatFactoryParams.PlatGameLoginResDto.builder().type(1).url(url).build();
    }

    /**
     * 获取用户秘钥
     *
     * @param reqDto
     * @returnx
     */
    public String getAuthToken(PlatFactoryParams.PlatLoginReqDto reqDto) {
        var tokenBO = BTIRequestParameter.GetCustomerAuthToken.builder()
                .agentUserName(config.getAgentUserName())
                .agentPassword(config.getAgentPassword())
                .MerchantCustomerCode(reqDto.getUsername())
                .build();
        BTIResponse.MerchantResponse tokenResponse = send(config.getApiUrl() + AUTH_TOKEN.getMethodName(), tokenBO, AUTH_TOKEN.getMethodNameDesc());
        var token = tokenResponse.getAuthToken().isEmpty()
                && registerUser(PlatFactoryParams.PlatRegisterReqDto.builder()
                .device(reqDto.getDevice()).lang(reqDto.getLang()).username(reqDto.getUsername()).build()).equals(Boolean.TRUE)
                ? jedisUtil.get(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH + ":" + RedisConstant.BTI + ":" + reqDto.getUsername()) : tokenResponse.getAuthToken();
        jedisUtil.setex(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH + ":" + RedisConstant.BTI + ":" + reqDto.getUsername(), 60 * 10, token);
        return token;
    }

    /**
     * post form提交
     *
     * @param url
     * @param object
     * @param methodNameDesc
     * @return
     */
    private BTIResponse.MerchantResponse send(String url, Object object, String methodNameDesc) {
        try {
            HttpHeaders headers = new HttpHeaders();
            MultiValueMap<String, Object> paramsMap = new LinkedMultiValueMap<>();
            Map<String, Object> itemMap = parseObject(toJSONString(object, SerializerFeature.WRITE_MAP_NULL_FEATURES), Map.class);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            paramsMap.setAll(itemMap);
            HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(paramsMap, headers);
            log.info(MODEL + "请求路径：" + url);
            log.info(MODEL + "请求参数：" + httpEntity);
            var result = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
            log.info(MODEL + "三方返回内容" + result);
            BTIResponse.MerchantResponse response = XmlBuilder.xmlStrToObject(result.getBody(), BTIResponse.MerchantResponse::new);
            if (NoError.equals(response.getErrorCode())) {
                return response;
            } else if (methodNameDesc.equals(AUTH_TOKEN.getMethodNameDesc()) && response.getErrorCode().equals("Exception")) {
                return response;
            } else if (methodNameDesc.equals(CHECK_TRANSACTION.getMethodNameDesc()) && response.getErrorCode().equals("TransactionCodeNotFound")) {
                return response;
            }
            throw new BusinessException(BTIPlatEnum.errorMAP.getOrDefault(response.getErrorCode(), CodeInfo.STATUS_CODE_ERROR));
        } catch (Exception e) {
            XxlJobLogger.log(MODEL + methodNameDesc + "异常:" + e.getMessage());
            log.error(MODEL + methodNameDesc + "失败!" + e.getMessage());
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
        try {
            var transferBO = BTIRequestParameter.TransferToWHL.builder()
                    .agentUserName(config.getAgentUserName())
                    .agentPassword(config.getAgentPassword())
                    .merchantCustomerCode(platCoinTransferUpReqDto.getUsername())
                    .refTransactionCode(platCoinTransferUpReqDto.getOrderId())
                    .amount(platCoinTransferUpReqDto.getCoin())
                    .build();
            BTIResponse.MerchantResponse transferResponse = send(config.getApiUrl() + BTIPlatEnum.BTIMethodEnum.DEPOSIT.getMethodName(), transferBO, BTIPlatEnum.BTIMethodEnum.DEPOSIT.getMethodNameDesc());
            var afterAmount = transferResponse.getBalance();
            coinPlatTransfersBase.updateOrderPlat(platCoinTransferUpReqDto.getOrderId(), 1, platCoinTransferUpReqDto.getCoin(), null, null);
            return PlatFactoryParams.PlatCoinTransferResDto.builder().platCoin(afterAmount).build();
        } catch (Exception e) {
            log.error(e.toString());
            coinPlatTransfersBase.updateOrderPlat(platCoinTransferUpReqDto.getOrderId(), 2, platCoinTransferUpReqDto.getCoin(), "", e.toString());
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
        try {
            var transferBO = BTIRequestParameter.TransferFromWHL.builder()
                    .agentUserName(config.getAgentUserName())
                    .agentPassword(config.getAgentPassword())
                    .merchantCustomerCode(platCoinTransferDownReqDto.getUsername())
                    .refTransactionCode(platCoinTransferDownReqDto.getOrderId())
                    .amount(platCoinTransferDownReqDto.getCoin())
                    .build();
            BTIResponse.MerchantResponse transferResponse = send(config.getApiUrl() + BTIPlatEnum.BTIMethodEnum.WITHDRAW.getMethodName(), transferBO, BTIPlatEnum.BTIMethodEnum.WITHDRAW.getMethodNameDesc());
            var afterAmount = transferResponse.getBalance();
            coinPlatTransfersBase.updateOrderPlat(platCoinTransferDownReqDto.getOrderId(), 1, platCoinTransferDownReqDto.getCoin(), null, null);
            return PlatFactoryParams.PlatCoinTransferResDto.builder().platCoin(afterAmount).build();
        } catch (Exception e) {
            log.error(e.toString());
            coinPlatTransfersBase.updateOrderPlat(platCoinTransferDownReqDto.getOrderId(), 2, platCoinTransferDownReqDto.getCoin(), "", e.toString());
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
        var balanceBO = BTIRequestParameter.GetBalance.builder()
                .agentUserName(config.getAgentUserName())
                .agentPassword(config.getAgentPassword())
                .merchantCustomerCode(reqDto.getUsername())
                .build();
        BTIResponse.MerchantResponse BalanceResponse = send(config.getApiUrl() + BALANCE.getMethodName(), balanceBO, BTIPlatEnum.BTIMethodEnum.BALANCE.getMethodNameDesc());
        return PlatFactoryParams.PlatQueryBalanceResDto.builder().platCoin(BalanceResponse.getBalance()).build();
    }

    /**
     * 注单拉取
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void pullBetsLips() {
        String pullStartTime = jedisUtil.hget(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH, RedisConstant.BTI);
        XxlJobLogger.log(MODEL + "进入拉单==================");
        if (Strings.isEmpty(pullStartTime)) {
            BetslipsBti betslipsBti = betslipsBtiServiceImpl.getOne(new LambdaQueryWrapper<BetslipsBti>()
                    .orderByDesc(BetslipsBti::getCreatedAt).last("limit 1"));
            var start = (betslipsBti != null && betslipsBti.getId() != null) ? betslipsBti.getCreatedAt() : DateNewUtils.now() - 24 * 60 * 60;
            pullStartTime = String.valueOf(start);
        }
        var pullEndTime = Math.min((Integer.parseInt(pullStartTime) + 60 * 30), (DateNewUtils.now() - 10 * 60));
        var dataBO = BTIRequestParameter.BettingHistory.builder()
                .From(DateNewUtils.utc0Str(DateNewUtils.utc8Zoned(Integer.valueOf(pullStartTime)), DateNewUtils.Format.yyyy_MM_dd_T_HH_mm_ss))
                .To(DateNewUtils.utc0Str(DateNewUtils.utc8Zoned(Integer.valueOf(pullEndTime)), DateNewUtils.Format.yyyy_MM_dd_T_HH_mm_ss))
                .build();
        String result = sendJson(config.getReportUrl() + OPEN_BETS.getMethodName() + getDataToken(), dataBO);
        XxlJobLogger.log(MODEL + "OPEN_BETS->进入拉单:" + result);
        if (null != result) {
            JSONObject jsonObject = parseObject(result);
            if (jsonObject.getInteger("errorCode") == 0) {
                String bets = jsonObject.getString("Bets");
                if (null != bets) insertBTIData(bets);
                String call = sendJson(config.getReportUrl() + FETCH_BET.getMethodName() + getDataToken(), dataBO);
                XxlJobLogger.log(MODEL + "SettledBETS->进入拉单:" + call);
                if (null != call) {
                    JSONObject jsonObject1 = parseObject(call);
                    if (jsonObject1.getInteger("errorCode") == 0) {
                        String settledBets = jsonObject1.getString("Bets");
                        if (null != settledBets) insertBTIData(settledBets);
                        jedisUtil.hset(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH, RedisConstant.BTI, String.valueOf(pullEndTime));
                    }
                }
            }
        }

    }

    /**
     * 获取拉单token，时效20秒
     *
     * @return token
     */
    private String getDataToken() {
        var token = jedisUtil.get(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH + ":" + RedisConstant.BTI + ":" + "dataToken:");
        if (null == token) {
            var tokenBO = BTIRequestParameter.GetAuthenticationToken.builder()
                    .agentUserName(config.getAgentUserName())
                    .agentPassword(config.getAgentPassword())
                    .build();
            String call = sendJson(config.getReportUrl() + TOKEN.getMethodName(), tokenBO);
            jedisUtil.setex(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH + ":" + RedisConstant.BTI + ":" + "dataToken:", 10, parseObject(call).getString("token"));
            return parseObject(call).getString("token");
        } else {
            return token;
        }
    }

    /**
     * dataApi json方式请求
     *
     * @param url
     * @param object
     * @return
     */
    private String sendJson(String url, Object object) {
        String result = null;
        try {
            result = HttpUtils.doPost(url, toJSONString(object), MODEL);
            Integer errorCode = parseObject(result).getInteger("errorCode");
            if (errorCode == 0) {
                return result;
            }
            throw new BusinessException(BTIPlatEnum.MAP.getOrDefault(errorCode, CodeInfo.STATUS_CODE_ERROR));
        } catch (Exception e) {
            XxlJobLogger.log(MODEL + "拉单异常:" + e.getMessage());
            log.error(MODEL + "拉单失败!" + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 数据入库
     *
     * @param call
     * @return
     */
    private void insertBTIData(String call) {
        JSONArray jsonArray = parseArray(call);
        List<BetslipsBti> betslipsBTIList = new ArrayList<>();
        Map<String, Integer> userMap = userServiceBase.getUsernameIdMap();
        if (null != jsonArray || !jsonArray.isEmpty()) {
            for (var x : jsonArray) {
                var betDetailBo = parseObject(toJSONString(x), BTIRequestParameter.BetDetail.class);
                var userName = userServiceBase.filterUsername(betDetailBo.getUserName());
                var uid = userMap.get(userName);
                if (StringUtils.isEmpty(userName) || uid == null) {
                    continue;
                }
                var retBetslipsBTI = BeanConvertUtils.copyProperties(betDetailBo, BetslipsBti::new, (bo, sb) -> {
                    if (!bo.getSelections().isEmpty() && !parseArray(bo.getSelections()).getJSONObject(0).isEmpty())
                        sb.setBranchId(parseArray(bo.getSelections()).getJSONObject(0).getInteger("BranchID"));
                    sb.setXbUsername(userName);
                    sb.setXbUid(uid);
                    sb.setXbCoin(bo.getTotalStake());
                    sb.setXbValidCoin(bo.getValidStake());
                    Map<String, Integer> transferStatusMap = Map.of("Won", BasePlatParam.BetRecordsStatus.WIN.getCode(), "Lost", BasePlatParam.BetRecordsStatus.LOSE.getCode(),
                            "Canceled", BasePlatParam.BetRecordsStatus.CANCEL.getCode(), "Draw", BasePlatParam.BetRecordsStatus.DRAW.getCode(),
                            "Opened", BasePlatParam.BetRecordsStatus.WAIT_SETTLE.getCode(), "Halfwon", BasePlatParam.BetRecordsStatus.WIN_HALF.getCode(),
                            "Halflost", BasePlatParam.BetRecordsStatus.LOSE_HALF.getCode(), "Cashout", BasePlatParam.BetRecordsStatus.GAME_CANCEL.getCode());
                    sb.setXbStatus(transferStatusMap.get(bo.getStatus()));
                    sb.setXbProfit(bo.getPl());
                    sb.setCreatedAt((int) ((bo.getCreationDate().getTime() / 1000) + 8 * 60 * 60));
                    sb.setUpdatedAt(DateUtils.getCurrentTime());
                });
                betslipsBTIList.add(retBetslipsBTI);
            }
            betslipsBtiServiceImpl.saveOrUpdateBatch(betslipsBTIList, 1000);
        } else if (null == jsonArray || jsonArray.isEmpty()) {
            log.info(MODEL + "当前无新注单");
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
        var checkTransationBO = BTIRequestParameter.CheckTransaction.builder()
                .agentUserName(config.getAgentUserName())
                .agentPassword(config.getAgentPassword())
                .refTransactionCode(orderId)
                .build();
        BTIResponse.MerchantResponse BalanceResponse = send(config.getApiUrl() + CHECK_TRANSACTION.getMethodName(), checkTransationBO, CHECK_TRANSACTION.getMethodNameDesc());
        return BalanceResponse.getErrorCode().equals("NoError");
    }

    /**
     * 注册用户
     *
     * @param reqDto 创建会员信息
     * @return
     */
    @Override
    public Boolean registerUser(PlatFactoryParams.PlatRegisterReqDto reqDto) {
        var lang = EnumUtils.getEnumIgnoreCase(BTIRequestParameter.LANGS.class, config.getLang()).getCode();
        var registerBO = BTIRequestParameter.CreateUser.builder()
                .agentUserName(config.getAgentUserName())
                .agentPassword(config.getAgentPassword())
                .currencyCode(config.getCurrency())
                .countryCode(lang)
                .customerDefaultLanguage(config.getLang())
                .merchantCustomerCode(reqDto.getUsername())
                .loginName(reqDto.getUsername())
                .group1ID(configCache.getPlatPrefix())
                .firstName(reqDto.getUsername())
                .lastName(reqDto.getUsername())
                .build();
        BTIResponse.MerchantResponse userResponse = send(config.getApiUrl() + CREATE_USER.getMethodName(), registerBO, CREATE_USER.getMethodNameDesc());
        jedisUtil.setex(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH + ":" + RedisConstant.BTI + ":" + reqDto.getUsername(), 60 * 10, userResponse.getAuthToken());
        return !userResponse.getAuthToken().isEmpty();
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
            BetslipsBti startOne = betslipsBtiServiceImpl.getOne(new QueryWrapper<BetslipsBti>().orderByDesc("created_at").last("limit 1"));
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
    public List<BetslipsDetailDto.SportSchedule> getSportSchedule(ReqPage<BetslipsDetailDto.SportScheduleReqDto> dto, Integer gameId) {
        return this.getSportSchedule(dto, gameId);
    }

    @Override
    public BetslipsDetailDto.ForwardEvent forwardEvent(String username, Integer masterEventID) {
        return this.forwardEvent(username, masterEventID);
    }
}
