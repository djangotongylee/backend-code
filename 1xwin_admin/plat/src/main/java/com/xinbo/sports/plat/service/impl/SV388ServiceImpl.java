package com.xinbo.sports.plat.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xinbo.sports.dao.generator.po.BetSlipsSupplemental;
import com.xinbo.sports.dao.generator.po.BetslipsSv388;
import com.xinbo.sports.dao.generator.service.BetslipsSv388Service;
import com.xinbo.sports.plat.base.CommonPersistence;
import com.xinbo.sports.plat.factory.PlatAbstractFactory;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams;
import com.xinbo.sports.plat.io.bo.SV388RequestParameter;
import com.xinbo.sports.plat.io.enums.BasePlatParam;
import com.xinbo.sports.plat.io.enums.MGPlatEnum;
import com.xinbo.sports.plat.io.enums.SV388PlatEnum;
import com.xinbo.sports.service.base.CoinPlatTransfersBase;
import com.xinbo.sports.service.base.ExceptionThreadLocal;
import com.xinbo.sports.service.base.UserServiceBase;
import com.xinbo.sports.service.cache.KeyConstant;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.service.common.RedisConstant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.utils.BeanConvertUtils;
import com.xinbo.sports.utils.DateUtils;
import com.xinbo.sports.utils.JedisUtil;
import com.xinbo.sports.utils.components.response.CodeInfo;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.alibaba.fastjson.JSON.toJSONString;
import static com.xinbo.sports.plat.io.enums.SV388PlatEnum.MAP;
import static com.xinbo.sports.plat.io.enums.SV388PlatEnum.SV388MethodEnum.*;
import static com.xinbo.sports.utils.components.response.CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_1;

@Slf4j
@Service("SV388ServiceImpl")
public class SV388ServiceImpl implements PlatAbstractFactory {
    protected static final String MODEL = "SV388";
    protected static final int SUCCESS = 0;
    protected static final String ERRCODE = "errCode";
    @Resource
    protected ConfigCache configCache;
    /*获取参数配置*/
    @Setter
    private SV388PlatEnum.PlatConfig config;
    @Resource
    private JedisUtil jedisUtil;
    @Resource
    private CoinPlatTransfersBase coinPlatTransfersBase;
    @Resource
    private UserServiceBase userServiceBase;
    @Resource
    private CommonPersistence commonPersistence;
    @Resource
    private BetslipsSv388Service betslipsSv388ServiceImpl;

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
        var lang = EnumUtils.getEnumIgnoreCase(SV388RequestParameter.LANGS.class, reqDto.getLang()).getCode();
        var username = handleUsername(reqDto.getUsername());
        var loginBO = SV388RequestParameter.Login.builder().operatorCode(config.getOperatorCode())
                .providerCode(config.getProviderCode()).username(username).password(username)
                .lang(lang).type(config.getType())
                .signature(DigestUtils.md5DigestAsHex((config.getOperatorCode() + username + config.getProviderCode() + config.getType() + username + config.getSecretKey()).getBytes()).toUpperCase())
                .build();
        var send = sendGet(config.getLogUrl() + LAUNCHGAMES.getMethodName(), loginBO, LAUNCHGAMES.getMethodNameDesc());
        return PlatFactoryParams.PlatGameLoginResDto.builder().type(1).url(parseObject(send).getString("gameUrl")).build();

    }

    /**
     * sv388请求方法
     *
     * @param url
     * @param object
     * @param methodNameDesc
     * @return
     */
    private String sendGet(String url, Object object, String methodNameDesc) {
        String result = null;
        try {
            clientHttpRequestFactory.setConnectTimeout(1000 * 3 * 60);
            restTemplate.setRequestFactory(clientHttpRequestFactory);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            Map<String, Object> itemMap = JSON.toJavaObject(parseObject(toJSONString(object)), Map.class);
            HttpEntity<HttpHeaders> request = new HttpEntity(headers);
            //异常处理
            ExceptionThreadLocal.setRequestParams(toJSONString(object));
            log.info(MODEL + " send url:" + url);
            log.info(MODEL + " send params:" + object);
            result = restTemplate.exchange(url, HttpMethod.GET, request, String.class, itemMap).getBody();
            log.info(MODEL + " return:" + result);
            if (parseObject(result).getInteger(ERRCODE) == SUCCESS) {
                return result;
            } else {
                if (LAUNCHGAMES.getMethodNameDesc().equals(methodNameDesc)) {
                    if (parseObject(result).getInteger(ERRCODE) == 81) {
                        registerUser(PlatFactoryParams.PlatRegisterReqDto.builder().username(configCache.getPlatPrefix().substring(0, 1) + itemMap.get("username")).build());
                        PlatFactoryParams.PlatGameLoginResDto login = login(PlatFactoryParams.PlatLoginReqDto.builder().username(configCache.getPlatPrefix().substring(0, 1) + itemMap.get("username")).lang(itemMap.get("lang").toString().split("_")[0]).build());
                        return toJSONString(login).replace("url","gameUrl");
                    }
                } else if (parseObject(result).getInteger(ERRCODE) == -999 || parseObject(result).getInteger(ERRCODE) == 999) {
                    throw new BusinessException(CodeInfo.TRY_AGAIN);
                } else {
                    throw new BusinessException(MAP.getOrDefault(parseObject(result).getInteger(ERRCODE), CodeInfo.PLAT_SYSTEM_ERROR));
                }
            }
        } catch (Exception e) {
            log.error(MODEL + methodNameDesc + "失败!" + e.getMessage());
            throw e;
        }
        return result;
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
    @Override
    public PlatFactoryParams.PlatCoinTransferResDto coinUp(PlatFactoryParams.PlatCoinTransferReqDto reqDto) {

        // 测试环境上方金额不能大于100
        if (!"PROD".equals(config.getEnvironment()) && reqDto.getCoin().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BusinessException(CodeInfo.PLAT_COIN_UP_COIN_NOT_GT_100);
        }
        return transfer(reqDto, 0);

    }

    /**
     * 转账入口
     *
     * @param reqDto
     * @param i
     * @return
     */
    private PlatFactoryParams.PlatCoinTransferResDto transfer(PlatFactoryParams.PlatCoinTransferReqDto reqDto, int i) {
        try {

            var username = handleUsername(reqDto.getUsername());
            var transferBO = SV388RequestParameter.Transfer.builder()
                    .operatorCode(config.getOperatorCode()).providerCode(config.getProviderCode())
                    .username(username).password(username).referenceId(reqDto.getOrderId()).type(i)
                    .amount(reqDto.getCoin().doubleValue())
                    .signature(DigestUtils.md5DigestAsHex((reqDto.getCoin().doubleValue() + config.getOperatorCode()
                            + username + config.getProviderCode() + reqDto.getOrderId() + i + username + config.getSecretKey())
                            .getBytes()).toUpperCase())
                    .build();
            var result = sendGet(config.getApiUrl() + TRANSFER.getMethodName(), transferBO, TRANSFER.getMethodNameDesc());
            if (result.contains("SUCCESS")) {
                coinPlatTransfersBase.updateOrderPlat(reqDto.getOrderId(), 1, reqDto.getCoin(), null, null);
                var afterAmount = queryBalance(PlatFactoryParams.PlatQueryBalanceReqDto.builder().username(reqDto.getUsername()).build()).getPlatCoin();
                return PlatFactoryParams.PlatCoinTransferResDto.builder().platCoin(afterAmount).build();
            }
            throw new BusinessException(MAP.getOrDefault(parseObject(result).getInteger(ERRCODE), CodeInfo.PLAT_SYSTEM_ERROR));
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
        var username = handleUsername(reqDto.getUsername());
        var balanceBO = SV388RequestParameter.Balance.builder()
                .operatorCode(config.getOperatorCode())
                .username(username)
                .password(username)
                .providerCode(config.getProviderCode())
                .signature(DigestUtils.md5DigestAsHex((config.getOperatorCode() + username + config.getProviderCode()
                        + username + config.getSecretKey()).getBytes()).toUpperCase()).build();
        var call = sendGet(config.getApiUrl() + SV388PlatEnum.SV388MethodEnum.BALANCE.getMethodName(), balanceBO, SV388PlatEnum.SV388MethodEnum.BALANCE.getMethodNameDesc());
        return PlatFactoryParams.PlatQueryBalanceResDto.builder().platCoin(parseObject(call).getBigDecimal("balance")).build();
    }

    /**
     * username特殊处理，截掉首位字母
     *
     * @param username
     * @return
     */
    private String handleUsername(String username) {
        String substring = username.toLowerCase().substring(1, username.length());
        if (substring.length() <= 12 && substring.length() >= 6) {
            return substring;
        }
        throw new BusinessException(CodeInfo.PLAT_INVALID_PARAM);
    }


    /**
     * 注单拉取
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void pullBetsLips() {
        String pullStartKey = jedisUtil.hget(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH, RedisConstant.SV388);
        XxlJobLogger.log(MODEL + "进入拉单==================");
        if (Strings.isEmpty(pullStartKey)) pullStartKey = "0";
        var pullBetsDataBO = SV388RequestParameter.GetAllBetsDetails.builder()
                .signature(DigestUtils.md5DigestAsHex((config.getOperatorCode() + config.getSecretKey()).getBytes()).toUpperCase()).operatorCode(config.getOperatorCode())
                .versionKey(pullStartKey)
                .build();
        var call = sendGet(config.getLogUrl() + SV388PlatEnum.SV388MethodEnum.FETCHBET.getMethodName(), pullBetsDataBO, SV388PlatEnum.SV388MethodEnum.FETCHBET.getMethodNameDesc());
        XxlJobLogger.log(MODEL + "进入拉单:" + call);
        if (call != null) {
            pullStartKey = insertSV388Data(call);
        }
        jedisUtil.hset(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH, RedisConstant.SV388, pullStartKey);

    }

    /**
     * 数据入库
     *
     * @param call
     * @return
     */
    private String insertSV388Data(String call) {
        JSONArray jsonArray = parseObject(call).getJSONArray("result");
        List<BetslipsSv388> betslipsSV388List = new ArrayList<>();
        Map<String, Integer> userMap = userServiceBase.getUsernameIdMap();
        if (!jsonArray.isEmpty()) {
            for (var x : jsonArray) {
                var betDetailBo = parseObject(toJSONString(x), SV388RequestParameter.BetsDetail.class);
                var userName = betDetailBo.getMember().toLowerCase().substring(2, betDetailBo.getMember().length());
                var uid = userMap.get(userName);
                if (StringUtils.isEmpty(userName) || uid == null) {
                    continue;
                }
                var retBetslipsSV388 = BeanConvertUtils.copyProperties(betDetailBo, BetslipsSv388::new, (bo, sb) -> {
                    sb.setXbUsername(userName);
                    sb.setXbUid(uid);
                    sb.setXbCoin(bo.getBet());
                    sb.setXbValidCoin(bo.getTurnover());
                    sb.setXbStatus(transferStatus(bo.getPayout().subtract(bo.getBet()), bo.getStatus()));
                    sb.setXbProfit(bo.getPayout().subtract(bo.getBet()));
                    sb.setCreatedAt((int) ((bo.getEndTime().getTime() / 1000) + 8 * 60 * 60));
                    sb.setUpdatedAt(DateUtils.getCurrentTime());
                });
                betslipsSV388List.add(retBetslipsSV388);
            }
            betslipsSv388ServiceImpl.saveOrUpdateBatch(betslipsSV388List, 1000);
            String ticket = betslipsSV388List.stream().map(BetslipsSv388::getId).collect(Collectors.joining(","));
            var markBO = SV388RequestParameter.MarkBetRecord.builder()
                    .operatorCode(config.getOperatorCode())
                    .ticket(ticket)
                    .signature(DigestUtils.md5DigestAsHex((config.getOperatorCode() + config.getSecretKey()).getBytes()).toUpperCase())
                    .build();
            sendGet(config.getLogUrl() + MARKBET.getMethodName(), markBO, MARKBET.getMethodNameDesc());
            log.info(MODEL + "标记订单号：" + ticket);
        } else if (jsonArray.isEmpty()) {
            log.info(MODEL + "当前无新注单");
        } else {
            XxlJobLogger.log(MODEL + "失败!" + toJSONString(call));
            PLAT_BET_SLIPS_EXCEPTION_CATEGORY_1.setMsg(toJSONString(call));
            throw new BusinessException(PLAT_BET_SLIPS_EXCEPTION_CATEGORY_1);
        }
        return parseObject(call).getString("lastversionkey");

    }

    /**
     * 检查转账状态
     *
     * @author: David
     * @date: 04/05/2020
     */
    @Override
    public Boolean checkTransferStatus(String orderId) {
        throw new UnsupportedOperationException();
    }

    /**
     * 注册用户
     *
     * @param reqDto 创建会员信息
     * @return
     */
    @Override
    public Boolean registerUser(PlatFactoryParams.PlatRegisterReqDto reqDto) {
        var username = handleUsername(reqDto.getUsername());
        var registerBO = SV388RequestParameter.Register.builder()
                .operatorCode(config.getOperatorCode())
                .username(username)
                .signature(DigestUtils.md5DigestAsHex((config.getOperatorCode() + username + config.getSecretKey()).getBytes()).toUpperCase())
                .build();
        String call = sendGet(config.getApiUrl() + SV388PlatEnum.SV388MethodEnum.CREATEMEMBER.getMethodName(), registerBO, SV388PlatEnum.SV388MethodEnum.CREATEMEMBER.getMethodNameDesc());
        return parseObject(call).getString("errMsg").equals("SUCCESS");
    }

    /**
     * 状态码转换
     * Status of this record 注单状态
     * 1 (valid bet record 有效注单)
     * 0 (running/ongoing match 赛事进行中)
     * -1 (invalid bet record 无效注单 e.g. voided 作废, canceled 取消)
     *
     * @param ticketStatus
     * @param status
     * @return
     */
    public Integer transferStatus(BigDecimal ticketStatus, Integer status) {
        //状态码
        Integer finalStatus;
        if (status == 0) {
            finalStatus = BasePlatParam.BetRecordsStatus.WAIT_SETTLE.getCode();
        } else if (status == 1) {
            if (ticketStatus.equals(BigDecimal.ZERO)) {
                finalStatus = BasePlatParam.BetRecordsStatus.DRAW.getCode();
            } else if (ticketStatus.compareTo(BigDecimal.ZERO) < 0) {
                finalStatus = BasePlatParam.BetRecordsStatus.LOSE.getCode();
            } else {
                finalStatus = BasePlatParam.BetRecordsStatus.WIN.getCode();
            }
        } else {
            return BasePlatParam.BetRecordsStatus.CANCEL.getCode();
        }

        return finalStatus;
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
          /*  BetslipsSV388 startOne = betslipsSV388ServiceImpl.getOne(new QueryWrapper<BetslipsSV388>().orderByDesc("created_at").lt("created_at", dto.getStart()).last("limit 1"));
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
            }*/
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
            String path = MGPlatEnum.MGMethodEnum.GETBETSDETAILS.getMethodName() + "&startingAfter={startingAfter}";
            String call = sendGet(config.getApiUrl() + path, parseObject(toJSONString(jsonObject)), MGPlatEnum.MGMethodEnum.GETBETSDETAILS.getMethodNameDesc());
            XxlJobLogger.log(MODEL + "进入补单:" + call);
            String pullIndex = insertSV388Data(call);
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
