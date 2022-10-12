package com.xinbo.sports.plat.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.PascalNameFilter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xinbo.sports.dao.generator.po.BetSlipsSupplemental;
import com.xinbo.sports.dao.generator.po.BetslipsSa;
import com.xinbo.sports.dao.generator.service.BetslipsSaService;
import com.xinbo.sports.plat.base.CommonPersistence;
import com.xinbo.sports.plat.factory.PlatAbstractFactory;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams;
import com.xinbo.sports.plat.io.bo.SARequestParameter;
import com.xinbo.sports.plat.io.bo.SAResponse;
import com.xinbo.sports.plat.io.enums.BasePlatParam;
import com.xinbo.sports.plat.io.enums.MGPlatEnum;
import com.xinbo.sports.plat.io.enums.SAPlatEnum;
import com.xinbo.sports.service.base.CoinPlatTransfersBase;
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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.spec.KeySpec;
import java.time.ZonedDateTime;
import java.util.*;

import static com.alibaba.fastjson.JSON.*;
import static com.xinbo.sports.utils.components.response.CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_1;
import static java.util.stream.Collectors.joining;

@Slf4j
@Service("SAServiceImpl")
public class SAServiceImpl implements PlatAbstractFactory {
    protected static final String MODEL = "SA";
    protected static final String LOBBY = "A3736";
    protected static final int SUCCESS = 0;
    @Resource
    protected ConfigCache configCache;
    /*获取参数配置*/
    @Setter
    private SAPlatEnum.PlatConfig config;
    @Resource
    private JedisUtil jedisUtil;
    @Resource
    private CoinPlatTransfersBase coinPlatTransfersBase;
    @Autowired
    private BetslipsSaService betslipsSaServiceImpl;
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
        var currency = EnumUtils.getEnumIgnoreCase(SARequestParameter.CURRENCY.class, configCache.getCountry()).getCode();
        var lang = EnumUtils.getEnumIgnoreCase(SARequestParameter.LANGS.class, reqDto.getLang()).getCode();
        var mobile = !BaseEnum.DEVICE.D.getValue().equals(reqDto.getDevice());
        var loginBO = SARequestParameter.Login.builder()
                .method(SAPlatEnum.SAMethodEnum.LOGINREQUEST.getMethodName())
                .Key(config.getSecretKey())
                .Time(DateNewUtils.getNowFormatTime(DateNewUtils.Format.yyyyMMddHHmmss))
                .Username(reqDto.getUsername())
                .CurrencyType(currency)
                .build();
        var send = send(config.getApiUrl(), loginBO, SAPlatEnum.SAMethodEnum.LOGINREQUEST.getMethodNameDesc());
        SAResponse.LoginRequestResponse loginRequestResponse = XmlBuilder.xmlStrToObject(send, SAResponse.LoginRequestResponse::new);
        if (loginRequestResponse.getErrorMsgId() == SUCCESS) {
            var url = config.getLobbyUrl() + "?username=" + reqDto.getUsername() + "&token=" + loginRequestResponse.getToken()
                    + "&lobby=" + LOBBY + "&lang=" + lang + "&returnurl=" + config.getRedirectUrl() + "&mobile=" + mobile;
            return PlatFactoryParams.PlatGameLoginResDto.builder().type(1).url(url).build();
        }
        throw new BusinessException(SAPlatEnum.LOGIN_MAP.getOrDefault(loginRequestResponse.getErrorMsgId(), CodeInfo.PLAT_SYSTEM_ERROR));
    }

    /**
     * @param url
     * @param object
     * @param methodNameDesc
     * @return
     */
    @SneakyThrows
    private String send(String url, Object object, String methodNameDesc) {
        try {
            HttpHeaders headers = new HttpHeaders();
            MultiValueMap<String, Object> paramsMap = new LinkedMultiValueMap<>();
            var map = parseObject(toJSONString(object, new PascalNameFilter()), Map.class);
            var qs = map.entrySet().stream().map(Object::toString).collect(joining("&"));
            //des
            KeySpec keySpec = new DESKeySpec(config.getEncryptKey().getBytes());
            SecretKey myDesKey = SecretKeyFactory.getInstance("DES").generateSecret(keySpec);
            Cipher desCipher;
            IvParameterSpec iv = new IvParameterSpec(config.getEncryptKey().getBytes());
            desCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            desCipher.init(Cipher.ENCRYPT_MODE, myDesKey, iv);
            byte[] text = qs.toString().getBytes();
            byte[] textEncrypted = desCipher.doFinal(text);
            // MD5
            var source = qs + config.getMd5Key() + map.get("Time") + config.getSecretKey();
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(source.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            paramsMap.add("q", Base64.getEncoder().encodeToString(textEncrypted));
            paramsMap.add("s", hashtext);
            HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(paramsMap, headers);
            log.info("请求参数：" + url);
            log.info("请求参数：" + httpEntity);
            var result = restTemplate.postForObject(url, httpEntity, String.class);
            log.info(MODEL + "三方返回内容" + result);
            return result;
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
     * @param reqDto {"coin","orderId","username"}
     * @return
     */
    @Override
    public PlatFactoryParams.PlatCoinTransferResDto coinUp(PlatFactoryParams.PlatCoinTransferReqDto reqDto) {
        var time = DateNewUtils.getNowFormatTime(DateNewUtils.Format.yyyyMMddHHmmss);
        var orderId = "IN" + time + reqDto.getUsername();
        try {
            // 测试环境上方金额不能大于100
            if (!"PROD".equals(config.getEnvironment()) && reqDto.getCoin().compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new BusinessException(CodeInfo.PLAT_COIN_UP_COIN_NOT_GT_100);
            }
            var transferBO = SARequestParameter.Deposit.builder()
                    .CreditAmount(reqDto.getCoin())
                    .Key(config.getSecretKey())
                    .method(SAPlatEnum.SAMethodEnum.DEPOSIT.getMethodName())
                    .Username(reqDto.getUsername())
                    .OrderId(orderId)
                    .Time(time)
                    .build();
            var call = send(config.getApiUrl(), transferBO, SAPlatEnum.SAMethodEnum.DEPOSIT.getMethodNameDesc());
            SAResponse.CreditBalanceDV creditBalanceDV = XmlBuilder.xmlStrToObject(call, SAResponse.CreditBalanceDV::new);
            if (creditBalanceDV.getErrorMsgId() == SUCCESS) {
                var afterAmount = creditBalanceDV.getBalance();
                coinPlatTransfersBase.updateOrderPlat(reqDto.getOrderId(), 1, reqDto.getCoin(), orderId, null);
                return PlatFactoryParams.PlatCoinTransferResDto.builder().platCoin(afterAmount).build();
            }
            throw new BusinessException(SAPlatEnum.TR_MAP.getOrDefault(creditBalanceDV.getErrorMsgId(), CodeInfo.PLAT_SYSTEM_ERROR));
        } catch (Exception e) {
            log.error(e.toString());
            coinPlatTransfersBase.updateOrderPlat(reqDto.getOrderId(), 2, reqDto.getCoin(), orderId, e.toString());
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
        var time = DateNewUtils.getNowFormatTime(DateNewUtils.Format.yyyyMMddHHmmss);
        var orderId = "OUT" + time + reqDto.getUsername();
        try {
            var transferBO = SARequestParameter.Withdraw.builder()
                    .DebitAmount(reqDto.getCoin())
                    .Key(config.getSecretKey())
                    .Username(reqDto.getUsername())
                    .method(SAPlatEnum.SAMethodEnum.WITHDRAW.getMethodName())
                    .OrderId(orderId)
                    .Time(time)
                    .build();
            String call = send(config.getApiUrl(), transferBO, SAPlatEnum.SAMethodEnum.WITHDRAW.getMethodNameDesc());
            SAResponse.DebitBalanceDV debitBalanceDV = XmlBuilder.xmlStrToObject(call, SAResponse.DebitBalanceDV::new);
            if (debitBalanceDV.getErrorMsgId() == SUCCESS) {
                var afterAmount = debitBalanceDV.getBalance();
                coinPlatTransfersBase.updateOrderPlat(reqDto.getOrderId(), 1, reqDto.getCoin(), orderId, null);
                return PlatFactoryParams.PlatCoinTransferResDto.builder().platCoin(afterAmount).build();
            } else {
                throw new BusinessException(SAPlatEnum.TR_MAP.getOrDefault(debitBalanceDV.getErrorMsgId(), CodeInfo.PLAT_SYSTEM_ERROR));
            }
        } catch (Exception e) {
            log.error(e.toString());
            coinPlatTransfersBase.updateOrderPlat(reqDto.getOrderId(), 2, reqDto.getCoin(), orderId, e.toString());
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
        var balanceBO = SARequestParameter.Balance.builder()
                .Key(config.getSecretKey())
                .method(SAPlatEnum.SAMethodEnum.BALANCE.getMethodName())
                .Username(reqDto.getUsername())
                .Time(DateNewUtils.getNowFormatTime(DateNewUtils.Format.yyyyMMddHHmmss))
                .build();
        var call = send(config.getApiUrl(), balanceBO, SAPlatEnum.SAMethodEnum.BALANCE.getMethodNameDesc());
        SAResponse.GetUserStatusResponse userStatusResponse = XmlBuilder.xmlStrToObject(call, SAResponse.GetUserStatusResponse::new);
        if (userStatusResponse.getErrorMsgId() == 0) {
            return PlatFactoryParams.PlatQueryBalanceResDto.builder().platCoin(userStatusResponse.getBalance()).build();
        }
        throw new BusinessException(SAPlatEnum.BL_MAP.getOrDefault(userStatusResponse.getErrorMsgId(), CodeInfo.PLAT_SYSTEM_ERROR));
    }


    /**
     * 注单拉取
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void pullBetsLips() {
        String pullStartTime = jedisUtil.hget(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH, RedisConstant.SA);
        XxlJobLogger.log(MODEL + "进入拉单==================");
        if (Strings.isEmpty(pullStartTime)) {
            BetslipsSa betslipsSa = betslipsSaServiceImpl.getOne(new LambdaQueryWrapper<BetslipsSa>().select(BetslipsSa::getCreatedAt)
                    .orderByDesc(BetslipsSa::getCreatedAt).last("limit 1"));
            int start = (betslipsSa != null && betslipsSa.getCreatedAt() != null) ? betslipsSa.getCreatedAt() : DateNewUtils.now() - 60 * 60 * 24;
            pullStartTime = String.valueOf(start);
        }
        var pullEndTime = String.valueOf(Math.min(Integer.parseInt(pullStartTime) + 60 * 60, DateNewUtils.now()));
        var pullBetsDataBO = SARequestParameter.GetAllBetsDetails.builder()
                .method(SAPlatEnum.SAMethodEnum.GETBETSDETAILS.getMethodName())
                .Key(config.getSecretKey())
                .Time(DateNewUtils.getNowFormatTime(DateNewUtils.Format.yyyyMMddHHmmss))
                .FromTime(DateNewUtils.utc8Str(DateNewUtils.utc8Zoned(Integer.parseInt(pullStartTime)), DateNewUtils.Format.yyyy_MM_dd_HH_mm_ss))
                .ToTime(DateNewUtils.utc8Str(DateNewUtils.utc8Zoned(Integer.parseInt(pullEndTime)), DateNewUtils.Format.yyyy_MM_dd_HH_mm_ss))
                .build();
        String call = send(config.getReportUrl(), pullBetsDataBO, SAPlatEnum.SAMethodEnum.GETBETSDETAILS.getMethodNameDesc());
        XxlJobLogger.log(MODEL + "进入拉单:" + call);
        insertSAData(call);
        jedisUtil.hset(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH, RedisConstant.SA, pullEndTime);


    }

    /**
     * 数据入库
     *
     * @param call
     * @param
     * @return
     */
    private String insertSAData(String call) {
        List<BetslipsSa> betslipsSaList = new ArrayList<>();
        Map<String, Integer> userMap = userServiceBase.getUsernameIdMap();
        SAResponse.GetAllBetDetailsForTimeIntervalResponse allBetDetailsResponse = XmlBuilder.xmlStrToObject(call, SAResponse.GetAllBetDetailsForTimeIntervalResponse::new);
        if (allBetDetailsResponse.getErrorMsgId() == SUCCESS) {
            if (allBetDetailsResponse.getBetDetailList().getBetDetail() != null) {
                for (SAResponse.BetDetail x : allBetDetailsResponse.getBetDetailList().getBetDetail()) {
                    var betDetailBo = parseObject(toJSONString(x), SARequestParameter.BetDetail.class);
                    var userName = userServiceBase.filterUsername(betDetailBo.getUsername());
                    var uid = userMap.get(userName);
                    if (StringUtils.isEmpty(userName) || uid == null) {
                        continue;
                    }
                    var retBetslipsSa = BeanConvertUtils.copyProperties(betDetailBo, BetslipsSa::new, (bo, sb) -> {
                        sb.setXbUsername(userName);
                        sb.setXbUid(uid);
                        sb.setXbCoin(bo.getBetAmount());
                        sb.setXbValidCoin(bo.getRolling());
                        sb.setXbStatus(transferStatus(bo.getResultAmount()));
                        sb.setXbProfit(bo.getResultAmount());
                        sb.setCreatedAt((int) (bo.getBetTime().getTime() / 1000));
                        sb.setUpdatedAt(DateUtils.getCurrentTime());
                    });
                    betslipsSaList.add(retBetslipsSa);
                }
                betslipsSaServiceImpl.saveOrUpdateBatch(betslipsSaList, 1000);

            } else if (allBetDetailsResponse.getBetDetailList().getBetDetail() == null) {
                XxlJobLogger.log(MODEL + "当前无注单" + allBetDetailsResponse.getBetDetailList().getBetDetail());
            } else {
                XxlJobLogger.log(MODEL + "失败!" + toJSONString(call));
                PLAT_BET_SLIPS_EXCEPTION_CATEGORY_1.setMsg(toJSONString(call));
                throw new BusinessException(PLAT_BET_SLIPS_EXCEPTION_CATEGORY_1);
            }
        }
        return call;
    }

    /**
     * 检查转账状态
     *
     * @author: David
     * @date: 04/05/2020
     */
    @Override
    public Boolean checkTransferStatus(String orderId) {
        var checkTransationBO = SARequestParameter.CheckOrderId.builder()
                .Key(config.getSecretKey())
                .method(SAPlatEnum.SAMethodEnum.CHECKORDERID.getMethodName())
                .OrderId(orderId)
                .Time(DateNewUtils.getNowFormatTime(DateNewUtils.Format.yyyyMMddHHmmss))
                .build();
        var call = send(config.getApiUrl(), checkTransationBO, SAPlatEnum.SAMethodEnum.CHECKORDERID.getMethodNameDesc());
        SAResponse.CheckOrderIdResponse checkOrderIdResponse = XmlBuilder.xmlStrToObject(call, SAResponse.CheckOrderIdResponse::new);
        if (checkOrderIdResponse.getErrorMsgId() == 0) {
            return checkOrderIdResponse.getIsExist().equals("true");
        }
        throw new BusinessException(SAPlatEnum.CHECK_MAP.getOrDefault(checkOrderIdResponse.getErrorMsgId(), CodeInfo.PLAT_SYSTEM_ERROR));
    }

    /**
     * 注册用户
     *
     * @param reqDto 创建会员信息
     * @return
     */
    @Override
    public Boolean registerUser(PlatFactoryParams.PlatRegisterReqDto reqDto) {
        var currency = EnumUtils.getEnumIgnoreCase(SARequestParameter.CURRENCY.class, configCache.getCountry()).getCode();
        var registerBO = SARequestParameter.Register.builder()
                .method(SAPlatEnum.SAMethodEnum.REGUSERINFO.getMethodName())
                .Username(reqDto.getUsername())
                .Key(config.getSecretKey())
                .Time(DateNewUtils.getNowFormatTime(DateNewUtils.Format.yyyyMMddHHmmss))
                .CurrencyType(currency)
                .build();
        String call = send(config.getApiUrl(), registerBO, SAPlatEnum.SAMethodEnum.REGUSERINFO.getMethodNameDesc());
        SAResponse.RegUserInfoResponse regUserInfoResponse = XmlBuilder.xmlStrToObject(call, SAResponse.RegUserInfoResponse::new);
        if (regUserInfoResponse.getErrorMsgId() == 0) {
            return true;
        } else {
            throw new BusinessException(SAPlatEnum.REGISTER_MAP.getOrDefault(regUserInfoResponse.getErrorMsgId(), CodeInfo.PLAT_SYSTEM_ERROR));
        }
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
            BetslipsSa startOne = betslipsSaServiceImpl.getOne(new QueryWrapper<BetslipsSa>().orderByDesc("created_at").lt("created_at", dto.getStart()).last("limit 1"));
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
            String path = MGPlatEnum.MGMethodEnum.GETBETSDETAILS.getMethodName() + "&startingAfter={startingAfter}";
            String call = send(config.getApiUrl() + path, parseObject(toJSONString(jsonObject)), MGPlatEnum.MGMethodEnum.GETBETSDETAILS.getMethodNameDesc());
            XxlJobLogger.log(MODEL + "进入补单:" + call);
            String pullIndex = insertSAData(call);
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
