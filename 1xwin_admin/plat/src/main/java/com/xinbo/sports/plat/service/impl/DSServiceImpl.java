package com.xinbo.sports.plat.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xinbo.sports.dao.generator.po.BetSlipsSupplemental;
import com.xinbo.sports.dao.generator.po.BetslipsDs;
import com.xinbo.sports.dao.generator.po.CoinPlatTransfer;
import com.xinbo.sports.dao.generator.service.BetslipsDsService;
import com.xinbo.sports.dao.generator.service.CoinPlatTransferService;
import com.xinbo.sports.service.base.CoinPlatTransfersBase;
import com.xinbo.sports.plat.base.CommonPersistence;
import com.xinbo.sports.plat.factory.PlatAbstractFactory;
import com.xinbo.sports.plat.io.bo.DsChessRequestParameter;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams.*;
import com.xinbo.sports.plat.io.dto.base.CoinTransferPlatReqDto;
import com.xinbo.sports.plat.io.enums.BasePlatParam;
import com.xinbo.sports.plat.io.enums.DsRoutineParam;
import com.xinbo.sports.service.base.UserServiceBase;
import com.xinbo.sports.service.cache.redis.UserCache;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.bo.UserCacheBo;
import com.xinbo.sports.utils.Aes256Utils;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.HttpUtils;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.http.HttpConnectTimeoutException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * DS棋牌
 * </p>
 *
 * @author andy
 * @since 2020/8/3
 */
@Slf4j
@Service("DSServiceImpl")
public class DSServiceImpl implements PlatAbstractFactory {
    private static final String MODEL = "DS";
    @Resource
    private com.xinbo.sports.service.cache.redis.ConfigCache configCache;
    @Resource
    private CoinPlatTransfersBase coinPlatTransfersBase;
    @Resource
    private BetslipsDsService betslipsDsServiceImpl;
    @Resource
    private UserServiceBase userServiceBase;
    @Resource
    private CoinPlatTransferService coinPlatTransferServiceImpl;
    @Resource
    private UserCache userCache;
    @Resource
    private CommonPersistence commonPersistence;

    private String sysLang = null;
    @Setter
    private DsChessRequestParameter.Config config;

    private static int getCode(DsChessRequestParameter.Result p) {
        int code = -1;
        if (null != p) {
            code = p.getCode();
        }
        return code;
    }

    private static boolean isOk(DsChessRequestParameter.Result p) {
        return DsRoutineParam.ErrorCode.E1.getCode() == getCode(p);
    }

    private static int getStatus(int originStatus, int isWin) {
        int status = -1;
        // 1-赢 2-输 3-和 4-取消 5-等待结算 6-赛事取消 7-投注确认 8-投注拒绝 9-赢一半 10-输一半
        switch (originStatus) {
            case 2:
                // 2-退款
                status = 6;
                break;
            case 3:
                // 3-拒絕投注
                status = 8;
                break;
            case 4:
                // 4-注單作廢
            case 5:
                // 5-取消
                status = 4;
                break;
            case 1:
                if (isWin > 0) {
                    status = 1;
                } else if (isWin < 0) {
                    status = 2;
                } else {
                    status = 3;
                }
                break;
            default:
                break;
        }

        return status;
    }

    /**
     * 处理时区
     * created_at = betTime;
     *
     * @param betTime
     * @return
     */
    private static int processCreatedAt(Date betTime) {
        SimpleDateFormat sdf = new SimpleDateFormat(DateNewUtils.Format.yyyy_MM_dd_HH_mm_ss.getValue());
        String format = sdf.format(betTime);
        return DateNewUtils.oriTimeZoneToDesTimeZone(format,
                DateNewUtils.Format.yyyy_MM_dd_HH_mm_ss,
                "GMT", "+8", "UTC", "+8"
        );
    }

    /**
     * 校验错误码
     *
     * @param code 第三方错误码
     */
    private static void checkErrorCode(int code) {
        if (null == DsRoutineParam.ERROR_CODE_MAPPER.get(code) && 0 != code) {
            throw new BusinessException(CodeInfo.PLAT_SYSTEM_ERROR);
        }
        throw new BusinessException(DsRoutineParam.ERROR_CODE_MAPPER.get(code));
    }

    public int register(PlatRegisterReqDto req) {
        checkParams(req);
        JSONObject reqBody = new JSONObject(new LinkedHashMap<>());
        reqBody.put(DsChessRequestParameter.ReqKey.AGENT.getKey(), config.getAgent());
        reqBody.put(DsChessRequestParameter.ReqKey.ACCOUNT.getKey(), req.getUsername());
        // 加密
        reqBody = buildCommonRequestBody(reqBody.toJSONString());
        String responseBody = send(DsChessRequestParameter.DsUrlEnum.REGISTER.getMethodName(), reqBody);
        DsChessRequestParameter.DsResponse dsResponse = JSON.parseObject(responseBody, DsChessRequestParameter.DsResponse.class);
        DsChessRequestParameter.Result result = dsResponse.getResult();
        return getCode(result);
    }

    @Override
    public PlatGameLoginResDto login(PlatLoginReqDto req) {
        checkParams(req);
        try {
            swithLang(req.getLang());
            JSONObject reqBody = new JSONObject(new LinkedHashMap<>());
            reqBody.put(DsChessRequestParameter.ReqKey.GAME_ID.getKey(), "0001");
            reqBody.put(DsChessRequestParameter.ReqKey.AGENT.getKey(), config.getAgent());
            reqBody.put(DsChessRequestParameter.ReqKey.ACCOUNT.getKey(), req.getUsername());
            reqBody.put(DsChessRequestParameter.ReqKey.LANG.getKey(), sysLang);
            // 加密
            reqBody = buildCommonRequestBody(reqBody.toJSONString());
            String responseBody = send(DsChessRequestParameter.DsUrlEnum.LOGIN.getMethodName(), reqBody);
            DsChessRequestParameter.DsResponse dsResponse = JSON.parseObject(responseBody, DsChessRequestParameter.DsResponse.class);
            DsChessRequestParameter.Result result = dsResponse.getResult();
            if (!isOk(result)) {
                checkErrorCode(getCode(result));
            }
            // 成功后,拼接登录地址
            return PlatGameLoginResDto.builder().type(1).url(dsResponse.getUrl()).build();
        } catch (BusinessException e) {
            if (CodeInfo.PLAT_ACCOUNT_NOT_EXISTS.getCode().equals(e.getCode())) {
                PlatRegisterReqDto registerReqDto = PlatRegisterReqDto.builder()
                        .device(req.getDevice())
                        .lang(req.getLang())
                        .username(req.getUsername())
                        .build();
                if (DsRoutineParam.ErrorCode.E1.getCode() == register(registerReqDto)) {
                    return login(req);
                }
            }
            throw e;
        }
    }

    @Override
    public PlatQueryBalanceResDto queryBalance(PlatQueryBalanceReqDto req) {
        checkParams(req);
        JSONObject reqBody = new JSONObject(new LinkedHashMap<>());
        reqBody.put(DsChessRequestParameter.ReqKey.AGENT.getKey(), config.getAgent());
        reqBody.put(DsChessRequestParameter.ReqKey.ACCOUNT.getKey(), req.getUsername());
        // 加密
        reqBody = buildCommonRequestBody(reqBody.toJSONString());
        String responseBody = send(DsChessRequestParameter.DsUrlEnum.BALANCE.getMethodName(), reqBody);
        DsChessRequestParameter.DsResponse dsResponse = JSON.parseObject(responseBody, DsChessRequestParameter.DsResponse.class);
        DsChessRequestParameter.Result result = dsResponse.getResult();
        if (!isOk(result)) {
            checkErrorCode(getCode(result));
        }
        BigDecimal balance = new BigDecimal(dsResponse.getBalance());
        return PlatQueryBalanceResDto.builder().platCoin(balance).build();
    }

    @Override
    public PlatCoinTransferResDto coinUp(@Valid PlatCoinTransferReqDto platCoinTransferUpReqDto) {
        checkParams(platCoinTransferUpReqDto);
        CoinTransferPlatReqDto build = CoinTransferPlatReqDto.builder()
                .coin(platCoinTransferUpReqDto.getCoin())
                .orderId(platCoinTransferUpReqDto.getOrderId())
                .username(platCoinTransferUpReqDto.getUsername())
                .build();
        return processCoinUpDown(build, 1);
    }

    @Override
    public PlatCoinTransferResDto coinDown(@Valid PlatCoinTransferReqDto platCoinTransferDownReqDto) {
        checkParams(platCoinTransferDownReqDto);
        BigDecimal coin = platCoinTransferDownReqDto.getCoin();
        // 是否全部下分: 1-全部 0-按金额
        if (null != platCoinTransferDownReqDto.getIsFullAmount() && platCoinTransferDownReqDto.getIsFullAmount().intValue() == 1) {
            // 查询余额
            PlatQueryBalanceResDto platQueryBalanceResDto = queryBalance(
                    PlatQueryBalanceReqDto.builder()
                            .username(platCoinTransferDownReqDto.getUsername())
                            .build()
            );
            if (null != platQueryBalanceResDto) {
                coin = platQueryBalanceResDto.getPlatCoin();
            }
        }
        if (coin.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(CodeInfo.PLAT_COIN_INSUFFICIENT);
        }
        CoinTransferPlatReqDto build = CoinTransferPlatReqDto.builder()
                .IsFullAmount(platCoinTransferDownReqDto.getIsFullAmount())
                .coin(coin)
                .orderId(platCoinTransferDownReqDto.getOrderId())
                .username(platCoinTransferDownReqDto.getUsername())
                .build();
        return processCoinUpDown(build, 0);
    }

    /**
     * 上下分逻辑处理
     *
     * @param dto  DTO
     * @param type 交易種類( 存款:1 提款:0)
     * @return PlatCoinTransferResDto
     */
    private PlatCoinTransferResDto processCoinUpDown(CoinTransferPlatReqDto dto, Integer type) {
        checkParams(dto);
        String username = dto.getUsername();
        String orderId = dto.getOrderId();
        BigDecimal coin = dto.getCoin();
        JSONObject reqBody = new JSONObject(new LinkedHashMap<>());
        reqBody.put(DsChessRequestParameter.ReqKey.SERIAL.getKey(), orderId);
        reqBody.put(DsChessRequestParameter.ReqKey.AGENT.getKey(), config.getAgent());
        reqBody.put(DsChessRequestParameter.ReqKey.ACCOUNT.getKey(), username);
        reqBody.put(DsChessRequestParameter.ReqKey.AMOUNT.getKey(), coin + "");
        reqBody.put(DsChessRequestParameter.ReqKey.OPER_TYPE.getKey(), type);
        // 加密
        reqBody = buildCommonRequestBody(reqBody.toJSONString());
        String responseBody = send(DsChessRequestParameter.DsUrlEnum.TRANSFER.getMethodName(), reqBody);
        DsChessRequestParameter.DsResponse dsResponse = JSON.parseObject(responseBody, DsChessRequestParameter.DsResponse.class);
        DsChessRequestParameter.Result result = dsResponse.getResult();
        if (!isOk(result)) {
            checkErrorCode(getCode(result));
        }
        // 第三方平台余额
        BigDecimal balance = new BigDecimal(dsResponse.getBalance());
        // 第三方平台单号
        String orderPlat = dsResponse.getTrans_id();
        log.info("[orderPlat=" + orderPlat + ",balance=" + balance + "]");
        updateCoinUpDown(orderId, 1, coin, orderPlat, null);
        return PlatCoinTransferResDto.builder().platCoin(balance).build();
    }

    /**
     * 更新上下分记录
     *
     * @param orderId   上下分主键Id
     * @param status    状态
     * @param coin      金额
     * @param orderPlat 第三方平台ID
     * @param msg       错误信息
     */
    private void updateCoinUpDown(String orderId, Integer status, BigDecimal coin, String orderPlat, String msg) {
        coinPlatTransfersBase.updateOrderPlat(orderId, status, coin, orderPlat, msg);
    }

    @Override
    public void pullBetsLips() {
        String requestParams = "";
        long lastUpdateTime = 0L;
        try {
            ZonedDateTime currentTime = LocalDateTime.now()
                    .atZone(DateNewUtils.getZoneId("UTC", "+8"))
                    .withZoneSameInstant(DateNewUtils.getZoneId("GMT", "+8"));
            ZonedDateTime startZonedDateTime;
            ZonedDateTime endZonedDateTime;
            // 根据MODEL获取最近更新时间
            String lastTime = configCache.getLastUpdateTimeByModel(MODEL);
            lastUpdateTime = StringUtils.isBlank(lastTime) ? 0L : Long.parseLong(lastTime);
            // redis没记录
            if (lastUpdateTime == 0) {
                BetslipsDs po = getBetsLipsDs();
                // 数据库没记录
                if (null == po) {
                    // -1天
                    startZonedDateTime = currentTime.minusMinutes(60 * 24L);
                } else {
                    startZonedDateTime = po.getFinishAt().toInstant()
                            .atZone(DateNewUtils.getZoneId("UTC", "+8"))
                            .withZoneSameInstant(DateNewUtils.getZoneId("GMT", "+8"));
                }
            } else {
                startZonedDateTime = Instant.ofEpochSecond(lastUpdateTime)
                        .atZone(DateNewUtils.getZoneId("UTC", "+8"))
                        .withZoneSameInstant(DateNewUtils.getZoneId("GMT", "+8"));
            }
            // +60分钟
            endZonedDateTime = startZonedDateTime.plusMinutes(60);
            if (endZonedDateTime.toEpochSecond() > currentTime.toEpochSecond()) {
                endZonedDateTime = currentTime;
            }
            // redis存储最近更新时间
            lastUpdateTime = endZonedDateTime.toEpochSecond();
            // -延迟时间
            startZonedDateTime = startZonedDateTime.minusMinutes(BasePlatParam.BasePlatEnum.DS.getMinutes());
            endZonedDateTime = endZonedDateTime.minusMinutes(BasePlatParam.BasePlatEnum.DS.getMinutes());
            JSONObject reqBody = buildPullBetReqParams(startZonedDateTime, endZonedDateTime);
            requestParams = reqBody.toJSONString();
            String resBody = pullSend(DsChessRequestParameter.DsUrlEnum.GET_BET_RECORDS.getMethodName(), reqBody);
            saveOrUpdateBatch(resBody);
            // 拉单节点变化
            configCache.reSetLastUpdateTimeByModel(MODEL, String.valueOf(lastUpdateTime));
        } catch (BusinessException e) {
            // 状态:0-三方异常
            if (e.getCode().equals(CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0.getCode())) {
                addBetSlipsException(0, requestParams, e.getMessage());
            }
        } catch (Exception e) {
            // 状态:1-数据异常
            log.error(MODEL + CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_1.getMsg() + ":" + e);
            addBetSlipsException(1, requestParams, e.toString());
        }
    }

    /**
     * 数据批量持久化拉单数据
     *
     * @param responseBody responseBody
     */
    private void saveOrUpdateBatch(String responseBody) {
        DsChessRequestParameter.DsResponse dsResponse = JSON.parseObject(responseBody, DsChessRequestParameter.DsResponse.class);
        DsChessRequestParameter.Result result = dsResponse.getResult();
        if (!isOk(result)) {
            // 状态:0-三方异常
            buildCodeInfo(responseBody);
        }
        if (Optional.ofNullable(dsResponse.getRows()).isEmpty() || dsResponse.getRows().isEmpty()) {
            return;
        }
        List<BetslipsDs> list = processRows(dsResponse.getRows());
        if (!list.isEmpty()) {
            betslipsDsServiceImpl.saveOrUpdateBatch(list,list.size());
        }
    }

    /**
     * 封装拉单请求参数
     *
     * @param startZonedDateTime 开始时间
     * @param endZonedDateTime   结束时间
     * @return JSONObject 请求参数
     */
    private JSONObject buildPullBetReqParams(ZonedDateTime startZonedDateTime, ZonedDateTime endZonedDateTime) {
        // GMT+8 格式为2018-10-19T08:54:14+01:00
        // 2020-08-04T16:47:37.470505
        String startTime = startZonedDateTime.format(DateNewUtils.getDateTimeFormatter(DateNewUtils.Format.yyyy_MM_dd_T_HH_mm_ss));
        String endTime = endZonedDateTime.format(DateNewUtils.getDateTimeFormatter(DateNewUtils.Format.yyyy_MM_dd_T_HH_mm_ss));
        startTime = startTime + startZonedDateTime.getOffset();
        endTime = endTime + endZonedDateTime.getOffset();
        log.info("时间范围[" + startTime + " - " + endTime + "]");
        JSONObject finishTime = new JSONObject(new LinkedHashMap<>());
        finishTime.put(DsChessRequestParameter.ReqKey.START_TIME.getKey(), startTime);
        finishTime.put(DsChessRequestParameter.ReqKey.END_TIME.getKey(), endTime);
        JSONObject reqBody = new JSONObject(new LinkedHashMap<>());
        reqBody.put(DsChessRequestParameter.ReqKey.FINISH_TIME.getKey(), finishTime);
        reqBody.put(DsChessRequestParameter.ReqKey.LIMIT.getKey(), 5000);
        // 加密后返回
        return buildCommonRequestBody(reqBody.toJSONString());
    }

    /**
     * 修改时区GMT+8
     *
     * @param startZonedDateTime 开始时间
     * @param endZonedDateTime   结束时间
     * @return 请求参数
     */
    private JSONObject buildPullBetReqParamsWithZoneSameInstant(ZonedDateTime startZonedDateTime, ZonedDateTime endZonedDateTime) {
        startZonedDateTime = startZonedDateTime.withZoneSameInstant(DateNewUtils.getZoneId("GMT", "+8"));
        endZonedDateTime = endZonedDateTime.withZoneSameInstant(DateNewUtils.getZoneId("GMT", "+8"));
        return buildPullBetReqParams(startZonedDateTime, endZonedDateTime);
    }

    /**
     * 检查转账状态
     *
     * @author: David
     * @date: 04/05/2020
     */
    @Override
    public Boolean checkTransferStatus(String orderId) {
        if (StringUtils.isBlank(orderId)) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        CoinPlatTransfer po = coinPlatTransferServiceImpl.getById(orderId);
        if (null == po) {
            throw new BusinessException(CodeInfo.RECORD_NOT_EXIST);
        }
        UserCacheBo.UserCacheInfo user = userCache.getUserInfoById(po.getUid());
        if (null == user) {
            throw new BusinessException(CodeInfo.PLAT_ACCOUNT_NOT_EXISTS);
        }
        String username = userServiceBase.buildUsername(user.getUsername());
        JSONObject reqBody = new JSONObject(new LinkedHashMap<>());
        reqBody.put(DsChessRequestParameter.ReqKey.AGENT.getKey(), config.getAgent());
        reqBody.put(DsChessRequestParameter.ReqKey.ACCOUNT.getKey(), username);
        reqBody.put(DsChessRequestParameter.ReqKey.SERIAL.getKey(), orderId);
        // 加密
        reqBody = buildCommonRequestBody(reqBody.toJSONString());
        String responseBody = send(DsChessRequestParameter.DsUrlEnum.TRANS_VERIFY.getMethodName(), reqBody);
        DsChessRequestParameter.DsResponse dsResponse = JSON.parseObject(responseBody, DsChessRequestParameter.DsResponse.class);
        DsChessRequestParameter.Result result = dsResponse.getResult();
        return isOk(result);
    }

    private List<BetslipsDs> processRows(List<BetslipsDs> rows) {
        Map<String, Integer> usernameIdMap = userServiceBase.getUsernameIdMap();
        if (null == usernameIdMap) {
            return new ArrayList<>();
        }
        return rows.stream()
                .map(entity -> {
                    String username = userServiceBase.filterUsername(entity.getMember());
                    Integer xbUid = usernameIdMap.get(username);
                    if (null != xbUid) {
                        entity.setXbUid(xbUid);
                        entity.setXbUsername(username);
                        return entity;
                    }
                    return null;
                })
                .filter(Objects::nonNull).parallel()
                .map(entity -> {
                    if (null != entity.getFinishAt()) {
                        entity.setCreatedAt(processCreatedAt(entity.getFinishAt()));
                    }
                    entity.setXbCoin(entity.getBetAmount());
                    entity.setXbValidCoin(entity.getValidAmount());
                    // 净输赢=下注金额-游戏赢分+手续费
                    BigDecimal xbProfit = entity.getXbCoin().subtract(entity.getPayoutAmount()).add(entity.getFeeAmount()).negate();
                    entity.setXbProfit(xbProfit);
                    // 下注狀態:1-正常 2-退款 3-拒絕投注 4-注單作廢 5-取消
                    if (1 == entity.getStatus()) {
                        entity.setXbStatus(getStatus(entity.getStatus(), entity.getXbProfit().compareTo(BigDecimal.ZERO)));
                    } else {
                        entity.setXbStatus(getStatus(entity.getStatus(), 0));
                    }
                    entity.setUpdatedAt(DateNewUtils.now());
                    return entity;
                }).collect(Collectors.toList());
    }

    @Override
    public Boolean registerUser(PlatRegisterReqDto reqDto) {
        if (null == reqDto) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        return DsRoutineParam.ErrorCode.E1.getCode() == register(reqDto);
    }

    private String send(String reqMethod, JSONObject reqBody) {
        String result = null;
        try {
            String uri = config.getApiUrl() + reqMethod;
            result = HttpUtils.doPost(uri, reqBody.toJSONString(), MODEL);
        } catch (ConnectException e) {
            throw new BusinessException(CodeInfo.PLAT_CONNECTION_REFUSED);
        } catch (HttpConnectTimeoutException e) {
            throw new BusinessException(CodeInfo.PLAT_TIME_OUT);
        } catch (Exception e) {
            throw new BusinessException(CodeInfo.PLAT_CONNECTION_EXCEPTION);
        }
        return result;
    }

    /**
     * 获取通用请求参数
     *
     * @param encryptText 明文
     * @return JSONObject
     */
    private JSONObject buildCommonRequestBody(String encryptText) {
        String aesKey = config.getAesKey();
        String signKey = config.getSignKey();
        String data = Aes256Utils.encrypt(encryptText, aesKey);
        String toSign = data + signKey;
        String sign = DigestUtils.md5Hex(toSign);
        JSONObject reqBody = new JSONObject(new LinkedHashMap<>());
        reqBody.put(DsChessRequestParameter.ReqKey.CHANNEL.getKey(), config.getChannel());
        reqBody.put(DsChessRequestParameter.ReqKey.DATA.getKey(), data);
        reqBody.put(DsChessRequestParameter.ReqKey.SIGN.getKey(), sign);
        log.info(MODEL + "\t明文{}", encryptText);
        log.info(MODEL + "\t密文{}", data);
        log.info(MODEL + "\tSign前{}", toSign);
        log.info(MODEL + "\tSign后{}", sign);
        log.info(MODEL + "\taesKey={}\tsignKey={}", aesKey, signKey);
        return reqBody;
    }

    /**
     * 语言切换
     *
     * @param lang
     */
    private void swithLang(String lang) {
        switch (lang) {
            case "en":
                sysLang = DsRoutineParam.Lang.EN_US.getCode();
                break;
            case "zh":
                sysLang = DsRoutineParam.Lang.ZH_CN.getCode();
                break;
            case "vi":
                sysLang = DsRoutineParam.Lang.VI.getCode();
                break;
            case "th":
                sysLang = DsRoutineParam.Lang.TH.getCode();
                break;
            default:
                break;
        }
    }

    @Override
    public PlatLogoutResDto logout(PlatLogoutReqDto dto) {
        checkParams(dto);
        JSONObject reqBody = new JSONObject(new LinkedHashMap<>());
        reqBody.put(DsChessRequestParameter.ReqKey.AGENT.getKey(), config.getAgent());
        reqBody.put(DsChessRequestParameter.ReqKey.ACCOUNT.getKey(), dto.getUsername());
        // 加密
        reqBody = buildCommonRequestBody(reqBody.toJSONString());
        String responseBody = send(DsChessRequestParameter.DsUrlEnum.LOGOUT.getMethodName(), reqBody);
        DsChessRequestParameter.DsResponse dsResponse = JSON.parseObject(responseBody, DsChessRequestParameter.DsResponse.class);
        DsChessRequestParameter.Result result = dsResponse.getResult();
        int flag = 0;
        if (!isOk(result)) {
            checkErrorCode(getCode(result));
        }
        flag = 1;
        return PlatLogoutResDto.builder().success(flag).build();
    }

    private <E> void checkParams(E params) {
        if (null == params) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
    }

    /**
     * 获取最新拉单记录
     *
     * @return 最新拉单记录
     */
    private BetslipsDs getBetsLipsDs() {
        return betslipsDsServiceImpl.lambdaQuery()
                .orderByDesc(BetslipsDs::getFinishAt)
                .last("limit 1")
                .one();
    }

    /**
     * 添加拉单异常表
     *
     * @param category      状态:0-三方异常 1-数据异常
     * @param requestParams 请求参数
     * @param exceptionInfo 异常信息:三方-返回数据 数据-异常处理
     */
    private void addBetSlipsException(Integer category, String requestParams, String exceptionInfo) {
        commonPersistence.addBetSlipsException(BasePlatParam.GAME.DS_CHESS.getCode(), requestParams, category, exceptionInfo, 0);
    }

    /**
     * 封装三方异常抛出
     *
     * @param msg MSG
     */
    private void buildCodeInfo(String msg) {
        CodeInfo codeInfo = CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0;
        codeInfo.setMsg(msg);
        throw new BusinessException(codeInfo);
    }

    /**
     * 拉单send
     *
     * @param url       URL
     * @param reqParams reqParams
     * @return String
     */
    private String pullSend(String url, JSONObject reqParams) {
        String result = "";
        try {
            result = send(url, reqParams);
        } catch (Exception e) {
            log.error(MODEL + CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0.getMsg() + ":" + e.toString());
            buildCodeInfo(e.toString());
        }
        return result;
    }

    /**
     * 根据起始、结束时间 生成补单信息
     * <p>
     * DS:
     * 1. 补单可拉取 最长多少天前的数据 		        小于等于7天
     * 2. 每次补单 时间跨度(ID)  开始-结束时间		小于等于1小时
     * 3. 补单接口  间隔时间					    建议大于15秒
     * 4. 补单最大数据量(条)					    无
     *
     * @param dto dto.start 开始时间 dto.end结束时间
     */
    @Override
    public void genSupplementsOrders(@NotNull GenSupplementsOrdersReqDto dto) {
        // 1. 补单可拉取 最长多少天前的数据 		        小于等于7天
        long preDays = 7L;
        // 2. 每次补单 时间跨度(ID)  开始-结束时间		小于等于1小时
        long startEndRange = 60;
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime start = DateNewUtils.utc8Zoned(dto.getStart());
        ZonedDateTime end = DateNewUtils.utc8Zoned(dto.getEnd());
        if (start.compareTo(now.minusDays(preDays)) < 0 || start.compareTo(end) >= 0) {
            throw new BusinessException(CodeInfo.PLAT_BET_SLIPS_SUPPLE_OVER_7_DAYS);
        }
        if (end.compareTo(now) > 0) {
            end = now;
        }
        ZonedDateTime temp;
        LinkedList<BetSlipsSupplemental> list = new LinkedList<>();
        do {
            temp = start.plusMinutes(startEndRange);

            // 请求参数
            String reqParams = buildPullBetReqParamsWithZoneSameInstant(start, temp).toJSONString();
            Integer currentTime = (int) now.toInstant().getEpochSecond();
            BetSlipsSupplemental po = PlatAbstractFactory
                    .buildBetSlipsSupplemental(dto.getGameId(), start.toString(), temp.toString(), reqParams, currentTime);
            list.add(po);

            start = start.plusMinutes(startEndRange);
        } while (temp.compareTo(end) <= 0);

        if (!list.isEmpty()) {
            commonPersistence.addBatchBetSlipsSupplementalList(list);
        }
    }

    /**
     * 补充注单信息
     *
     * @param dto dto.requestInfo 补单请求参数
     */
    @Override
    public void betsRecordsSupplemental(@NotNull BetsRecordsSupplementReqDto dto) {
        try {
            JSONObject reqBody = JSON.parseObject(dto.getRequestInfo());
            String resBody = pullSend(DsChessRequestParameter.DsUrlEnum.GET_BET_RECORDS.getMethodName(), reqBody);
            saveOrUpdateBatch(resBody);
        } catch (BusinessException e) {
            // 状态:0-三方异常
            log.error(MODEL + CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0.getMsg() + ":" + e);
            throw e;
        } catch (Exception e) {
            // 状态:1-数据异常
            log.error(MODEL + CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_1.getMsg() + ":" + e);
            throw e;
        }
    }

    /**
     * 拉单异常 再次拉单
     *
     * @param dto dto.requestInfo 拉单请求参数
     */
    @Override
    public void betSlipsExceptionPull(@NotNull PlatFactoryParams.BetsRecordsSupplementReqDto dto) {
        this.betsRecordsSupplemental(dto);
    }
}
