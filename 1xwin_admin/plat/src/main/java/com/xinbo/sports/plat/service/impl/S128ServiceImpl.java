package com.xinbo.sports.plat.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.xinbo.sports.dao.generator.po.BetSlipsSupplemental;
import com.xinbo.sports.dao.generator.po.BetslipsS128;
import com.xinbo.sports.dao.generator.service.BetslipsS128Service;
import com.xinbo.sports.plat.base.CommonPersistence;
import com.xinbo.sports.plat.factory.PlatAbstractFactory;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams;
import com.xinbo.sports.plat.io.bo.S128RequestParameter;
import com.xinbo.sports.plat.io.bo.S128Response;
import com.xinbo.sports.plat.io.enums.BasePlatParam;
import com.xinbo.sports.plat.io.enums.S128PlatEnum;
import com.xinbo.sports.service.base.CoinPlatTransfersBase;
import com.xinbo.sports.service.base.ExceptionThreadLocal;
import com.xinbo.sports.service.base.UserServiceBase;
import com.xinbo.sports.service.cache.KeyConstant;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.service.common.RedisConstant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.enums.BaseEnum;
import com.xinbo.sports.utils.*;
import com.xinbo.sports.utils.components.response.CodeInfo;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.*;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.alibaba.fastjson.JSON.toJSONString;
import static com.xinbo.sports.utils.components.response.CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_1;

@Slf4j
@Service("S128ServiceImpl")
public class S128ServiceImpl implements PlatAbstractFactory {
    protected static final String MODEL = "S128";
    private static final String SUCCESS = "00";

    @Autowired
    private JedisUtil jedisUtil;
    @Resource
    protected ConfigCache configCache;
    @Resource
    private UserServiceBase userServiceBase;
    @Autowired
    private BetslipsS128Service betslipsS128ServiceImpl;
    @Resource
    private CoinPlatTransfersBase coinPlatTransfersBase;
    RestTemplate restTemplate = new RestTemplate();
    /*获取参数配置*/
    @Setter
    S128PlatEnum.PlatConfig config;
    @Resource
    private CommonPersistence commonPersistence;


    /**
     * 获取游戏登录链接
     *
     * @param platLoginReqDto {username, lang, device, slotId}
     * @return 登录链接
     */
    @Override
    public PlatFactoryParams.PlatGameLoginResDto login(PlatFactoryParams.PlatLoginReqDto platLoginReqDto) {
        String sessionId = null;
        var sessionBO = S128RequestParameter.SessionId.builder()
                .agentCode(config.getAgentCode())
                .apiKey(config.getApiKey())
                .loginId(platLoginReqDto.getUsername())
                .name(platLoginReqDto.getUsername())
                .build();
        String call = send(config.getApiUrl() + S128PlatEnum.S128MethodEnum.GETSESSIONID.getMethodName(), S128PlatEnum.S128MethodEnum.GETSESSIONID.getMethodNameDesc(), sessionBO);
        S128Response.GetSessionIdResult getSessionIdResult = XmlBuilder.xmlStrToObject(call, S128Response.GetSessionIdResult::new);
        if (getSessionIdResult.getStatus_code().equals(SUCCESS)) {
            sessionId = getSessionIdResult.getSession_id();
            var loginBO = S128RequestParameter.Login.builder()
                    .sessionId(sessionId)
                    .lang(platLoginReqDto.getLang() != null ? switchLanguage(platLoginReqDto.getLang()) : switchLanguage("en"))
                    .loginId(platLoginReqDto.getUsername())
                    .build();
            String url = (platLoginReqDto.getDevice() != null && platLoginReqDto.getDevice().equals(BaseEnum.DEVICE.D.getValue())) ? config.getPcUrl() + S128PlatEnum.S128MethodEnum.PCLOGIN.getMethodName() : config.getH5Url() + S128PlatEnum.S128MethodEnum.MOBLIELOGIN.getMethodName();
            return PlatFactoryParams.PlatGameLoginResDto.builder().type(2).url(renderForm(url, loginBO)).build();
        } else {
            log.error("登陆异常：" + getSessionIdResult.getStatus_text());
            throw new BusinessException(S128PlatEnum.L_MAP.getOrDefault(getSessionIdResult.getStatus_code(), CodeInfo.PLAT_SYSTEM_ERROR));
        }
    }

    private String renderForm(String url, Object params) {
        var stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("<form action='%s' method='post'>", url));
        Map<String, Object> itemMap = JSON.toJavaObject(parseObject(toJSONString(params)), Map.class);
        itemMap.forEach((k, v) ->
                stringBuilder.append(String.format("<input type='hidden' name='%s' value='%s' />", k, v)));
        stringBuilder.append("</form>正在跳转 ...");
        return stringBuilder.toString();
    }

    private String switchLanguage(String lang) {
        String language;
        switch (lang) {
            case "zh":
                language = S128PlatEnum.Lang.ZH_CN.getValue();
                break;
            case "th":
                language = S128PlatEnum.Lang.TH_TH.getValue();
                break;
            case "vi":
                language = S128PlatEnum.Lang.VI_VN.getValue();
                break;
            case "en":
            default:
                language = S128PlatEnum.Lang.EN.getValue();
                break;
        }

        return language;
    }

    /**
     * 登出游戏
     *
     * @param platLoginReqDto {"username"}
     * @return success:1-成功 0-失败
     * @author: David
     * @date: 04/05/2020
     */
    @Override
    public PlatFactoryParams.PlatLogoutResDto logout(PlatFactoryParams.PlatLogoutReqDto platLoginReqDto) {
        return null;
    }

    /**
     * 三方上分
     *
     * @param platCoinTransferUpReqDto {"coin","orderId","username"}
     * @return {platCoin}
     * @author: David
     * @date: 04/05/2020
     */
    @Override
    public PlatFactoryParams.PlatCoinTransferResDto coinUp(PlatFactoryParams.PlatCoinTransferReqDto platCoinTransferUpReqDto) {
        // 测试环境上方金额不能大于100
        if (!"PROD".equals(config.getEnvironment()) && platCoinTransferUpReqDto.getCoin().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BusinessException(CodeInfo.PLAT_COIN_UP_COIN_NOT_GT_100);
        }
        var depositBO = S128RequestParameter.Deposit.builder()
                .agentCode(config.getAgentCode())
                .apiKey(config.getApiKey())
                .loginId(platCoinTransferUpReqDto.getUsername())
                .refNo(platCoinTransferUpReqDto.getOrderId())
                .amount(platCoinTransferUpReqDto.getCoin())
                .oddsType(config.getOddsType())
                .name(platCoinTransferUpReqDto.getUsername())
                .build();
        String call = send(config.getApiUrl() + S128PlatEnum.S128MethodEnum.DEPOSIT.getMethodName(), S128PlatEnum.S128MethodEnum.DEPOSIT.getMethodNameDesc(), depositBO);
        S128Response.Deposit deposit = XmlBuilder.xmlStrToObject(call, S128Response.Deposit::new);
        if (deposit.getStatus_code().equals(SUCCESS)) {
            BigDecimal afterAmount = new BigDecimal(deposit.getBalance_close());
            coinPlatTransfersBase.updateOrderPlat(platCoinTransferUpReqDto.getOrderId(), 1, platCoinTransferUpReqDto.getCoin(), null, null);
            return PlatFactoryParams.PlatCoinTransferResDto.builder().platCoin(afterAmount).build();
        } else {
            coinPlatTransfersBase.updateOrderPlat(platCoinTransferUpReqDto.getOrderId(), 2, platCoinTransferUpReqDto.getCoin(), "", deposit.getStatus_text());
            throw new BusinessException(S128PlatEnum.D_MAP.getOrDefault(deposit.getStatus_code(), CodeInfo.PLAT_SYSTEM_ERROR));
        }
    }

    /**
     * 三方下分
     *
     * @param platCoinTransferDownReqDto {"coin","orderId","username"}
     * @return {platCoin}
     * @author: David
     * @date: 04/05/2020
     */
    @Override
    public PlatFactoryParams.PlatCoinTransferResDto coinDown(PlatFactoryParams.PlatCoinTransferReqDto platCoinTransferDownReqDto) {
        var withdrawBO = S128RequestParameter.Withdraw.builder()
                .agentCode(config.getAgentCode())
                .apiKey(config.getApiKey())
                .loginId(platCoinTransferDownReqDto.getUsername())
                .refNo(platCoinTransferDownReqDto.getOrderId())
                .amount(platCoinTransferDownReqDto.getCoin())
                .oddsType(config.getOddsType())
                .build();
        String call = send(config.getApiUrl() + S128PlatEnum.S128MethodEnum.WITHDRAW.getMethodName(), S128PlatEnum.S128MethodEnum.WITHDRAW.getMethodNameDesc(), withdrawBO);
        S128Response.Withdraw withdraw = XmlBuilder.xmlStrToObject(call, S128Response.Withdraw::new);
        if (withdraw.getStatus_code().equals(SUCCESS)) {
            BigDecimal afterAmount = new BigDecimal(withdraw.getBalance_close());
            coinPlatTransfersBase.updateOrderPlat(platCoinTransferDownReqDto.getOrderId(), 1, platCoinTransferDownReqDto.getCoin(), null, null);
            return PlatFactoryParams.PlatCoinTransferResDto.builder().platCoin(afterAmount).build();
        } else {
            coinPlatTransfersBase.updateOrderPlat(platCoinTransferDownReqDto.getOrderId(), 2, platCoinTransferDownReqDto.getCoin(), null, withdraw.getStatus_text());
            throw new BusinessException(S128PlatEnum.W_MAP.getOrDefault(withdraw.getStatus_code(), CodeInfo.PLAT_SYSTEM_ERROR));
        }

    }

    /**
     * 查询三方余额
     *
     * @param platQueryBalanceReqDto {"username"}
     * @return {platCoin}
     * @author: David
     * @date: 04/05/2020
     */
    @Override
    public PlatFactoryParams.PlatQueryBalanceResDto queryBalance(PlatFactoryParams.PlatQueryBalanceReqDto platQueryBalanceReqDto) {
        var balanceBO = S128RequestParameter.QueryBalance.builder()
                .agentCode(config.getAgentCode())
                .apiKey(config.getApiKey())
                .loginId(platQueryBalanceReqDto.getUsername())
                .build();
        String call = send(config.getApiUrl() + S128PlatEnum.S128MethodEnum.GETBALANCE.getMethodName(), S128PlatEnum.S128MethodEnum.GETBALANCE.getMethodNameDesc(), balanceBO);
        S128Response.GetBalanceResult getBalanceResult = XmlBuilder.xmlStrToObject(call, S128Response.GetBalanceResult::new);
        if (getBalanceResult.getStatus_code().equals(SUCCESS)) {
            BigDecimal balance = new BigDecimal(getBalanceResult.getBalance());
            return PlatFactoryParams.PlatQueryBalanceResDto.builder().platCoin(balance).build();
        } else {
            if (getBalanceResult.getStatus_code().equals("61.02")) {
                return PlatFactoryParams.PlatQueryBalanceResDto.builder().platCoin(BigDecimal.ZERO).build();
            }
            throw new BusinessException(S128PlatEnum.C_MAP.getOrDefault(getBalanceResult.getStatus_code(), CodeInfo.PLAT_SYSTEM_ERROR));
        }
    }

    /**
     * 拉取三方注单信息
     *
     * @author: David
     * @date: 04/05/2020
     */
    @Override
    public void pullBetsLips() {
        BetslipsS128 betslipsS128 = null;
        int startTime = DateUtils.getCurrentTime();
        String call = null;
        String pullEndTime = jedisUtil.hget(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH, RedisConstant.S128);
        if (Strings.isEmpty(pullEndTime)) {
            betslipsS128 = betslipsS128ServiceImpl.getOne(new LambdaQueryWrapper<BetslipsS128>()
                    .orderByDesc(BetslipsS128::getCreatedDatetime).last("limit 1"));
            startTime = (betslipsS128 != null && betslipsS128.getCreatedAt() != null) ? betslipsS128.getCreatedAt() : (startTime - 24 * 60 * 60);
            pullEndTime = DateUtils.yyyyMMddHH(startTime);
        }
        XxlJobLogger.log("S128进入拉单==================" + pullEndTime);
        DateUtils.strTranInt(pullEndTime);
        var getTicketBO = S128RequestParameter.GetBetsDetails.builder()
                .agentCode(config.getAgentCode())
                .apiKey(config.getApiKey())
                .startDatetime(pullEndTime)
                .endDatetime(DateUtils.yyyyMMddHH(DateUtils.strTranInt(pullEndTime) + 20 * 60))
                .build();

        call = send(config.getApiUrl() + S128PlatEnum.S128MethodEnum.GETPROCESSEDTICKET.getMethodName(), S128PlatEnum.S128MethodEnum.GETPROCESSEDTICKET.getMethodNameDesc(), getTicketBO);
        insertS128CockFightData(call, pullEndTime);
    }

    /**
     * 插入斗鸡数据表
     *
     * @param call
     * @param pullEndTime
     */
    private void insertS128CockFightData(String call, String pullEndTime) {
        S128Response.ProcessedTicket processedTicket = XmlBuilder.xmlStrToObject(call, S128Response.ProcessedTicket::new);
        if (processedTicket.getStatus_code().equals(SUCCESS)) {
            List<String> strings = Lists.newArrayList();
            if (processedTicket.getData() != null && Strings.isNotEmpty(processedTicket.getData())) {
                XxlJobLogger.log("S128进入拉单:" + processedTicket.getData());
                if (processedTicket.getData().contains("|")) {
                    strings = Arrays.asList(processedTicket.getData().split("\\|"));
                } else {
                    strings.add(processedTicket.getData());
                }
                Map<String, Integer> userMap = userServiceBase.getUsernameIdMap();
                List<BetslipsS128> betslipsS128list = new ArrayList<>();
                for (var x : strings) {
                    List<String> betDetailBo = Arrays.asList(x.split(","));
                    JSONObject jsonObject = new JSONObject();
                    for (int i = 0; i < betDetailBo.size(); i++) {
                        jsonObject.put(String.valueOf(i), betDetailBo.get(i));
                    }
                    var betSlipsS128Details = parseObject(toJSONString(jsonObject), S128RequestParameter.BetSlipsS128Details.class);
                    var userName = userServiceBase.filterUsername(betDetailBo.get(1));
                    var uid = userMap.get(userName);
                    if (StringUtils.isEmpty(userName) || uid == null) {
                        continue;
                    }
                    var retBetslipsS128 = BeanConvertUtils.copyProperties(betSlipsS128Details, BetslipsS128::new, (bo, sb) -> {
                        sb.setXbUid(uid);
                        sb.setXbUsername(userName);
                        sb.setXbCoin(BigDecimal.valueOf(bo.getStake()));
                        sb.setXbValidCoin(Arrays.asList("CANCEL", "REFUND", "VOID").stream().anyMatch(i -> i.equals(bo.getStatus())) ? BigDecimal.ZERO : bo.getStakeMoney());
                        sb.setXbStatus(statusMAP.get(bo.getStatus()));
                        sb.setXbProfit(bo.getWinloss());
                        sb.setCreatedAt((int) (bo.getCreatedDatetime().getTime() / 1000));
                        sb.setUpdatedAt(DateUtils.getCurrentTime());
                    });
                    betslipsS128list.add(retBetslipsS128);
                }
                betslipsS128ServiceImpl.saveOrUpdateBatch(betslipsS128list, 1000);
            } else {
                XxlJobLogger.log("S128斗鸡拉单结束:当前无注单");
            }
            if (pullEndTime != null && DateUtils.strTranInt(pullEndTime) + 20 * 60 <= DateUtils.getCurrentTime()) {
                jedisUtil.hset(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH, RedisConstant.S128, DateUtils.yyyyMMddHH(DateUtils.strTranInt(pullEndTime) + 20 * 60));
            }
        } else {
            XxlJobLogger.log("S128斗鸡拉单失败!" + toJSONString(call));
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
        var checkTransferBO = S128RequestParameter.CheckTransfer.builder()
                .agentCode(config.getAgentCode())
                .apiKey(config.getApiKey())
                .refNo(orderId)
                .build();
        String call = send(config.getApiUrl() + S128PlatEnum.S128MethodEnum.CHECKTRANSFER.getMethodName(), S128PlatEnum.S128MethodEnum.CHECKTRANSFER.getMethodNameDesc(), checkTransferBO);
        S128Response.CheckTransfer checkTransfer = XmlBuilder.xmlStrToObject(call, S128Response.CheckTransfer::new);
        if (checkTransfer.getStatus_code().equals(SUCCESS)) {
            if (checkTransfer.getFound().equals("1")) {
                return true;
            } else {
                return false;
            }
        } else {
            throw new BusinessException(S128PlatEnum.R_MAP.getOrDefault(checkTransfer.getStatus_code(), CodeInfo.PLAT_SYSTEM_ERROR));
        }
    }

    /**
     * 创建会员
     * S128并无注册接口！！！
     *
     * @param reqDto 创建会员信息
     * @return true-成功 false-失败
     */
    @Override
    public Boolean registerUser(PlatFactoryParams.PlatRegisterReqDto reqDto) {
        return false;
    }

    /**
     * 状态码转换
     */
    Map<String, Integer> statusMAP = Map.of(
            "WIN", BasePlatParam.BetRecordsStatus.WIN.getCode(),
            "LOSE", BasePlatParam.BetRecordsStatus.LOSE.getCode(),
            "CANCEL", BasePlatParam.BetRecordsStatus.CANCEL.getCode(),
            "REFUND", BasePlatParam.BetRecordsStatus.DRAW.getCode(),
            "VOID", BasePlatParam.BetRecordsStatus.BET_REFUSE.getCode()
    );


    private String send(String url, String methodName, Object object) {
        String result = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            MultiValueMap<String, Object> paramsMap = new LinkedMultiValueMap<>();
            Map<String, Object> itemMap = JSON.toJavaObject(parseObject(toJSONString(object)), Map.class);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            paramsMap.setAll(itemMap);
            HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(paramsMap, headers);
            log.info("S128斗鸡请求参数：" + methodName + httpEntity);
            //异常参数
            ExceptionThreadLocal.setRequestParams(toJSONString(object));
            result = restTemplate.postForObject(url, httpEntity, String.class);
            if (result != null && result.contains("Access Denied")) {
                throw new BusinessException(CodeInfo.PLAT_IP_NOT_ACCESS);
            }
            log.info("S128斗鸡三方返回内容" + result);
            return result;
        } catch (Exception e) {
            if (methodName.contains("processed")) {
                CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0.setMsg(e.getMessage());
                throw new BusinessException(CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0);
            } else {
                throw e;
            }
        }
    }


    /**
     * 根据起始、结束时间 生成补单信息
     * keep for 60 days
     * 2020-08-19 09:00
     *
     * @param dto dto.start 开始时间 dto.end结束时间
     */
    @Override
    public void genSupplementsOrders(PlatFactoryParams.GenSupplementsOrdersReqDto dto) {
        if (ZonedDateTime.now().minusDays(60).toInstant().getEpochSecond() > dto.getStart()) {
            throw new BusinessException(CodeInfo.PLAT_BET_SLIPS_SUPPLE_OVER_60_DAYS);
        } else {
            ZonedDateTime start = DateNewUtils.utc8Zoned(dto.getStart());
            ZonedDateTime end = DateNewUtils.utc8Zoned(dto.getEnd());
            ZonedDateTime now = ZonedDateTime.now();
            if (end.compareTo(now) > 0) {
                end = now;
            }
            ZonedDateTime temp;
            LinkedList<BetSlipsSupplemental> list = new LinkedList<>();
            do {
                temp = start.plusMinutes(30);
                var getTicketBO = S128RequestParameter.GetBetsDetails.builder()
                        .agentCode(config.getAgentCode())
                        .apiKey(config.getApiKey())
                        .startDatetime(DateNewUtils.utc8Str(start, DateNewUtils.Format.yyyy_MM_dd_HH_mm))
                        .endDatetime(DateNewUtils.utc8Str(temp, DateNewUtils.Format.yyyy_MM_dd_HH_mm))
                        .build();
                Integer currentTime = (int) now.toInstant().getEpochSecond();
                BetSlipsSupplemental po = PlatAbstractFactory
                        .buildBetSlipsSupplemental(dto.getGameId(), start.toString(), temp.toString(), toJSONString(getTicketBO), currentTime);
                list.add(po);

                start = start.plusMinutes(30);
            } while (temp.compareTo(end) <= 0);

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
        String call = send(config.getApiUrl() + S128PlatEnum.S128MethodEnum.GETPROCESSEDTICKET.getMethodName(), S128PlatEnum.S128MethodEnum.GETPROCESSEDTICKET.getMethodNameDesc(), parseObject(dto.getRequestInfo()));
        insertS128CockFightData(call, null);
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