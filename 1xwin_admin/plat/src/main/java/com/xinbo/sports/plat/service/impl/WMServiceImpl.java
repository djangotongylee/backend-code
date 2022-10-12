package com.xinbo.sports.plat.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xinbo.sports.dao.generator.po.BetSlipsSupplemental;
import com.xinbo.sports.dao.generator.po.BetslipsWm;
import com.xinbo.sports.dao.generator.po.CoinPlatTransfer;
import com.xinbo.sports.dao.generator.service.BetslipsWmService;
import com.xinbo.sports.dao.generator.service.CoinPlatTransferService;
import com.xinbo.sports.plat.base.CommonPersistence;
import com.xinbo.sports.plat.factory.PlatAbstractFactory;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams.*;
import com.xinbo.sports.plat.io.bo.WmLiveRequestParameter;
import com.xinbo.sports.plat.io.dto.base.CoinTransferPlatReqDto;
import com.xinbo.sports.plat.io.enums.BasePlatParam;
import com.xinbo.sports.plat.io.enums.WMRoutineParam;
import com.xinbo.sports.plat.io.enums.WMUrlEnum;
import com.xinbo.sports.service.base.CoinPlatTransfersBase;
import com.xinbo.sports.service.base.UserServiceBase;
import com.xinbo.sports.service.cache.redis.UserCache;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.bo.UserCacheBo;
import com.xinbo.sports.utils.BeanConvertUtils;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.HttpUtils;
import com.xinbo.sports.utils.MD5;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
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
 * WM真人
 * 描述:
 * 第三方测试环境
 * https://api.a45.me/api/public/Gateway.php?cmd=Hello&vendorId=sbtwapi&signature=1e2c9409edb2cb66a25b8e4ac345606a
 * </p>
 *
 * @author andy
 * @since 2020/5/19
 */
@Slf4j
@Service("WMServiceImpl")
public class WMServiceImpl implements PlatAbstractFactory {
    private static final String MODEL = "WM";
    private static final String VENDOR_ID = "vendorId";
    private static final String SIGNATURE = "signature";
    private static final String CMD = "cmd";

    private static final String KEY_SYS_LANG = "syslang";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_RESULT = "result";
    @Setter
    private WmLiveRequestParameter.WmConfig config;
    private int sysLang = 0;
    @Resource
    private com.xinbo.sports.service.cache.redis.ConfigCache configCache;
    @Resource
    private CoinPlatTransfersBase coinPlatTransfersBase;
    @Resource
    private BetslipsWmService betslipsWmServiceImpl;
    @Resource
    private UserServiceBase userServiceBase;
    @Resource
    private CoinPlatTransferService coinPlatTransferServiceImpl;
    @Resource
    private UserCache userCache;
    @Resource
    private CommonPersistence commonPersistence;

    private static int getStatus(int value) {
        // 1-赢 2-输 3-和 4-取消 5-等待结算 6-赛事取消 7-投注确认 8-投注拒绝 9-赢一半 10-输一半
        int status = 3;
        if (value > 0) {
            status = 1;
        } else if (value < 0) {
            status = 2;
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

    private static int getCode(JSONObject p) {
        int code = -1;
        if (Objects.nonNull(p)) {
            code = p.getIntValue("errorCode");
        }
        return code;
    }

    private static JSONObject toJSONObject(String resBody) {
        return JSON.parseObject(resBody);
    }

    private static boolean isOk(JSONObject p) {
        return getCode(p) == 0;
    }

    /**
     * 校验错误码
     *
     * @param code 第三方错误码
     */
    private static void checkErrorCode(int code) {
        if (null == WMRoutineParam.ERROR_CODE_MAPPER.get(code) && 0 != code) {
            throw new BusinessException(CodeInfo.PLAT_SYSTEM_ERROR);
        }
        throw new BusinessException(WMRoutineParam.ERROR_CODE_MAPPER.get(code));
    }

    @Override
    public PlatGameLoginResDto login(PlatLoginReqDto platLoginReqDto) {
        if (Objects.isNull(platLoginReqDto)) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        try {
            switchLang(platLoginReqDto.getLang());
            StringBuilder appendRequestParam = appendRequestParam(WMUrlEnum.WMLIVE_SIGNINGAME.getMethodName());
            String user = platLoginReqDto.getUsername();
            String password = buildPwd(user);
            appendRequestParam.append("&").append("user").append("=").append(user);
            appendRequestParam.append("&").append(KEY_PASSWORD).append("=").append(password);
            appendRequestParam.append("&").append("lang").append("=").append(sysLang);
            JSONObject result = toJSONObject(send(appendRequestParam.toString()));
            if (!isOk(result)) {
                checkErrorCode(getCode(result));
            }
            return PlatGameLoginResDto.builder().type(1).url(result.getString(KEY_RESULT)).build();
        } catch (BusinessException e) {
            if (CodeInfo.PLAT_ACCOUNT_NOT_EXISTS.getCode().equals(e.getCode())) {
                JSONObject p = new JSONObject();
                p.put(KEY_USERNAME, platLoginReqDto.getUsername());
                p.put(KEY_SYS_LANG, sysLang);
                if (0 == registerUser(p)) {
                    return login(platLoginReqDto);
                }
            }
            throw e;
        }
    }

    @Override
    public PlatLogoutResDto logout(PlatLogoutReqDto dto) {
        PlatLogoutResDto result = null;
        StringBuilder appendRequestParam = appendRequestParam(WMUrlEnum.WMLIVE_LOGOUTGAME.getMethodName());
        String user = dto.getUsername();
        if (StringUtils.isNotBlank(user)) {
            appendRequestParam.append("&").append("user").append("=").append(user);
        }
        if (isOk(toJSONObject(send(appendRequestParam.toString())))) {
            result = PlatLogoutResDto.builder().success(1).build();
        }
        return result;
    }

    private int registerUser(JSONObject params) {
        if (Objects.isNull(params)) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        String user = params.getString(KEY_USERNAME);
        String password = buildPwd(user);
        StringBuilder appendRequestParam = appendRequestParam(WMUrlEnum.WMLIVE_MEMBERREGISTER.getMethodName());
        appendRequestParam.append("&").append("user").append("=").append(user);
        appendRequestParam.append("&").append(KEY_PASSWORD).append("=").append(password);
        appendRequestParam.append("&").append(KEY_USERNAME).append("=").append(params.getString(KEY_USERNAME));
        appendRequestParam.append("&").append(KEY_SYS_LANG).append("=").append(params.getInteger(KEY_SYS_LANG));
        return getCode(toJSONObject(send(appendRequestParam.toString())));
    }

    @Override
    public PlatCoinTransferResDto coinUp(PlatCoinTransferReqDto platCoinTransferUpReqDto) {
        CoinTransferPlatReqDto build = BeanConvertUtils.beanCopy(platCoinTransferUpReqDto, CoinTransferPlatReqDto::new);
        return processCoinUpDown(build);
    }

    @Override
    public PlatCoinTransferResDto coinDown(PlatCoinTransferReqDto platCoinTransferDownReqDto) {
        BigDecimal coin = platCoinTransferDownReqDto.getCoin();
        // 是否全部下分: 1-全部 0-按金额
        if (null != platCoinTransferDownReqDto.getIsFullAmount() && platCoinTransferDownReqDto.getIsFullAmount().intValue() == 1) {
            // 查询余额
            PlatQueryBalanceResDto platQueryBalanceResDto = queryBalance(BeanConvertUtils.beanCopy(platCoinTransferDownReqDto, PlatQueryBalanceReqDto::new));
            if (Objects.nonNull(platQueryBalanceResDto)) {
                coin = platQueryBalanceResDto.getPlatCoin();
            }
        }
        if (coin.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(CodeInfo.PLAT_COIN_INSUFFICIENT);
        }
        if (coin.compareTo(BigDecimal.ZERO) > 0) {
            coin = coin.negate();
        }

        CoinTransferPlatReqDto build = BeanConvertUtils.beanCopy(platCoinTransferDownReqDto, CoinTransferPlatReqDto::new);
        build.setCoin(coin);
        return processCoinUpDown(build);
    }

    private PlatCoinTransferResDto processCoinUpDown(CoinTransferPlatReqDto platCoinTransferUpReqDto) {
        if (Objects.isNull(platCoinTransferUpReqDto)) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        String username = platCoinTransferUpReqDto.getUsername();
        switchLang(platCoinTransferUpReqDto.getLang());

        String orderId = platCoinTransferUpReqDto.getOrderId();
        BigDecimal coin = platCoinTransferUpReqDto.getCoin();
        JSONObject result = processRequestParamsByChangeBalance(username, coin, orderId);
        if (!isOk(result)) {
            int code = getCode(result);
            // 请求太频繁,回退用户金额
            if (WMRoutineParam.ChangeBalanceErrorCode.E10804.getCode() == code) {
                updateCoinUpDown(orderId, 2, coin, null, null != result ? result.toJSONString() : null);
            }
            checkErrorCode(code);
        }
        JSONObject jsonObject = result.getJSONObject(KEY_RESULT);
        if (null == jsonObject) {
            log.error(MODEL + " response's " + KEY_RESULT + " is null");
            throw new BusinessException(result.toJSONString());
        }
        // 第三方平台余额
        String balance = jsonObject.getString("cash");
        // 第三方平台订单号
        String orderPlat = jsonObject.getString("orderId");
        updateCoinUpDown(orderId, 1, coin, orderPlat, null);
        log.info(MODEL + "[orderPlat=" + orderPlat + ",balance=" + balance + "]");
        return PlatCoinTransferResDto.builder().platCoin(new BigDecimal(balance)).build();
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
        if (coin.compareTo(BigDecimal.ZERO) < 0) {
            coin = coin.negate();
        }
        coinPlatTransfersBase.updateOrderPlat(orderId, status, coin, orderPlat, msg);
    }

    /**
     * 处理上下分请求参数
     *
     * @param username
     * @param coin
     * @return
     */
    private JSONObject processRequestParamsByChangeBalance(String username, BigDecimal coin, String orderId) {
        StringBuilder appendRequestParam = appendRequestParam(WMUrlEnum.WMLIVE_CHANGEBALANCE.getMethodName());
        String user = username;
        appendRequestParam.append("&").append("user").append("=").append(user);
        appendRequestParam.append("&").append(KEY_SYS_LANG).append("=").append(sysLang);
        appendRequestParam.append("&").append("money").append("=").append(coin);
        appendRequestParam.append("&").append("order").append("=").append(orderId);
        return toJSONObject(send(appendRequestParam.toString()));
    }

    @Override
    public PlatQueryBalanceResDto queryBalance(PlatQueryBalanceReqDto platQueryBalanceReqDto) {
        if (Objects.isNull(platQueryBalanceReqDto)) {
            log.error("params is null...");
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        switchLang(platQueryBalanceReqDto.getLang());
        StringBuilder appendRequestParam = appendRequestParam(WMUrlEnum.WMLIVE_GETBALANCE.getMethodName());
        String user = platQueryBalanceReqDto.getUsername();
        appendRequestParam.append("&").append("user").append("=").append(user);
        appendRequestParam.append("&").append("lang").append("=").append(sysLang);
        JSONObject result = toJSONObject(send(appendRequestParam.toString()));
        if (!isOk(result)) {
            checkErrorCode(getCode(result));
        }
        return PlatQueryBalanceResDto.builder().platCoin(new BigDecimal(result.getString(KEY_RESULT))).build();
    }

    @Override
    public void pullBetsLips() {
        String params = "";
        long lastUpdateTime = 0L;
        try {
            ZonedDateTime currentTime = LocalDateTime.now()
                    .atZone(DateNewUtils.getZoneId("UTC", "+8"))
                    .withZoneSameInstant(DateNewUtils.getZoneId("GMT", "+8"));
            ZonedDateTime startZonedDateTime = null;
            ZonedDateTime endZonedDateTime = null;
            // 根据MODEL获取最近更新时间
            String lastTime = configCache.getLastUpdateTimeByModel(MODEL);
            lastUpdateTime = StringUtils.isBlank(lastTime) ? 0L : Long.parseLong(lastTime);
            // redis没记录
            if (lastUpdateTime == 0) {
                BetslipsWm betsLipsWm = getBetsLipsWm();
                // 数据库没记录
                if (null == betsLipsWm) {
                    // -1天
                    startZonedDateTime = currentTime.minusDays(1);
                } else {
                    startZonedDateTime = betsLipsWm.getBetTime().toInstant()
                            .atZone(DateNewUtils.getZoneId("UTC", "+8"))
                            .withZoneSameInstant(DateNewUtils.getZoneId("GMT", "+8"));
                }
            } else {
                startZonedDateTime = Instant.ofEpochSecond(lastUpdateTime)
                        .atZone(DateNewUtils.getZoneId("UTC", "+8"))
                        .withZoneSameInstant(DateNewUtils.getZoneId("GMT", "+8"));
            }
            // +1天
            endZonedDateTime = startZonedDateTime.plusDays(1);
            if (endZonedDateTime.toEpochSecond() > currentTime.toEpochSecond()) {
                endZonedDateTime = currentTime;
            }
            // redis存储最近更新时间
            lastUpdateTime = endZonedDateTime.toEpochSecond();
            // -5分钟
            startZonedDateTime = startZonedDateTime.minusMinutes(BasePlatParam.BasePlatEnum.WM.getMinutes());
            endZonedDateTime = endZonedDateTime.minusMinutes(BasePlatParam.BasePlatEnum.WM.getMinutes());
            params = buildPullBetReqParams(startZonedDateTime, endZonedDateTime);
            String responseBody = pullSend(params);
            saveOrUpdateBatch(responseBody);
            // 拉单节点变化
            configCache.reSetLastUpdateTimeByModel(MODEL, String.valueOf(lastUpdateTime));
        } catch (BusinessException e) {
            // 状态:0-三方异常
            if (e.getCode().equals(CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0.getCode())) {
                addBetSlipsException(0, params, e.getMessage());
            }
        } catch (Exception e) {
            // 状态:1-数据异常
            log.error(MODEL + CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_1.getMsg() + ":" + e);
            addBetSlipsException(1, params, e.toString());
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
        StringBuilder appendRequestParam = appendRequestParam(WMUrlEnum.WMLIVE_GETMEMBERTRADEREPORT.getMethodName());
        appendRequestParam.append("&").append("user").append("=").append(username);
        appendRequestParam.append("&").append("order").append("=").append(orderId);
        JSONObject result = toJSONObject(send(appendRequestParam.toString()));
        return isOk(result);
    }

    @Override
    public Boolean registerUser(PlatRegisterReqDto reqDto) {
        if (Objects.isNull(reqDto)) {
            log.error("params is null...");
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        switchLang(reqDto.getLang());
        JSONObject p = new JSONObject();
        p.put(KEY_USERNAME, reqDto.getUsername());
        p.put(KEY_SYS_LANG, sysLang);
        return 0 == registerUser(p);
    }

    private List<BetslipsWm> processJSONArray(JSONObject result) {
        List<BetslipsWm> list = new ArrayList<>();
        if (Optional.ofNullable(result.getJSONArray(KEY_RESULT)).isEmpty() || result.getJSONArray(KEY_RESULT).isEmpty()) {
            return list;
        }
        Map<String, Integer> usernameIdMap = userServiceBase.getUsernameIdMap();
        if (null == usernameIdMap) {
            return list;
        }
        return result.getJSONArray(KEY_RESULT).stream()
                .map(o -> {
                    JSONObject jsonObj = (JSONObject) o;
                    BetslipsWm entity = jsonObj.toJavaObject(BetslipsWm.class);
                    String username = userServiceBase.filterUsername(entity.getUser());
                    Integer xbUid = usernameIdMap.get(username);
                    if (null != xbUid) {
                        entity.setXbUid(xbUid);
                        entity.setXbUsername(username);
                        entity.setId(jsonObj.getLongValue("betId"));
                        return entity;
                    }
                    return null;
                })
                .filter(Objects::nonNull).parallel()
                .map(entity -> {
                    if (null != entity.getBetTime()) {
                        entity.setCreatedAt(processCreatedAt(entity.getBetTime()));
                    }
                    entity.setXbCoin(entity.getBet());
                    entity.setXbValidCoin(entity.getValidBet());
                    entity.setXbProfit(entity.getWinLoss());
                    entity.setXbStatus(getStatus(entity.getWinLoss().compareTo(BigDecimal.ZERO)));
                    entity.setUpdatedAt(DateNewUtils.now());
                    return entity;
                }).collect(Collectors.toList());
    }

    /**
     * 游戏纪录报表:拼接请求参数
     *
     * @return
     */
    private String requestParamsByGetDateTimeReport(String startTime, String endTime, Integer timetype, Integer datatype, Integer gameno1) {
        StringBuilder appendRequestParam = appendRequestParam(WMUrlEnum.WMLIVE_GETDATETIMEREPORT.getMethodName());
        appendRequestParam.append("&").append(KEY_SYS_LANG).append("=").append(sysLang);
        appendRequestParam.append("&").append("startTime").append("=").append(startTime);
        if (null != endTime) {
            appendRequestParam.append("&").append("endTime").append("=").append(endTime);
        }
        if (null != timetype) {
            appendRequestParam.append("&").append("timetype").append("=").append(timetype);
        }
        if (null != datatype) {
            appendRequestParam.append("&").append("datatype").append("=").append(datatype);
        }
        if (null != gameno1) {
            appendRequestParam.append("&").append("gameno1").append("=").append(gameno1);
        }
        return appendRequestParam.toString();
    }

    private String send(String requestParam) {
        String result = null;
        String url = "";
        try {
            url = config.getApiUrl() + requestParam;
            result = HttpUtils.doGet(url, MODEL);
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
     * 封装公共请求参数
     *
     * @param cmd 请求方法名
     * @return StringBuilder
     */
    private StringBuilder appendRequestParam(String cmd) {
        StringBuilder stringBuffer = new StringBuilder();
        stringBuffer.append("?").append(CMD).append("=").append(cmd);
        stringBuffer.append("&").append(VENDOR_ID).append("=").append(config.getVendorId());
        stringBuffer.append("&").append(SIGNATURE).append("=").append(config.getSignature());
        return stringBuffer;
    }

    /**
     * 语言切换
     *
     * @param lang
     */
    public void switchLang(String lang) {
        switch (lang) {
            case "en":
                sysLang = WMRoutineParam.LANGS.EN.getCode();
                break;
            case "zh":
                sysLang = WMRoutineParam.LANGS.CN.getCode();
                break;
            case "th":
                sysLang = WMRoutineParam.LANGS.TH.getCode();
                break;
            case "vi":
                sysLang = WMRoutineParam.LANGS.VI.getCode();
                break;
            case "tw":
                sysLang = WMRoutineParam.LANGS.TW.getCode();
                break;
            case "jp":
                sysLang = WMRoutineParam.LANGS.JP.getCode();
                break;
            case "ko":
                sysLang = WMRoutineParam.LANGS.KO.getCode();
                break;
            case "id":
                sysLang = WMRoutineParam.LANGS.ID.getCode();
                break;
            case "in":
                sysLang = WMRoutineParam.LANGS.IN.getCode();
                break;
            case "xw":
                sysLang = WMRoutineParam.LANGS.XW.getCode();
                break;
            default:
                break;
        }
    }

    /**
     * 密码:最小值:6字符,最大值:64字符
     *
     * @return 第三方密码
     */
    public String buildPwd(String user) {
        return MD5.encryption(user);
    }

    /**
     * 获取最新拉单记录
     *
     * @return 最新拉单记录
     */
    private BetslipsWm getBetsLipsWm() {
        return betslipsWmServiceImpl.lambdaQuery()
                .orderByDesc(BetslipsWm::getBetTime)
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
        commonPersistence.addBetSlipsException(BasePlatParam.GAME.WM_LIVE.getCode(), requestParams, category, exceptionInfo, 0);
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
     * @param reqParams reqParams
     * @return String
     */
    private String pullSend(String reqParams) {
        String result = "";
        try {
            result = send(reqParams);
        } catch (Exception e) {
            log.error(MODEL + CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0.getMsg() + ":" + e.toString());
            buildCodeInfo(e.toString());
        }
        return result;
    }

    /**
     * 根据起始、结束时间 生成补单信息
     * <p>
     * WM:
     * 1. 补单可拉取 最长多少天前的数据  		    小于等于60天
     * 2. 每次补单 时间跨度(ID)  开始-结束时间		小于等于1天
     * 3. 补单接口  间隔时间						大于等于10秒
     * 4. 补单最大数据量(条)						无
     *
     * @param dto dto.start 开始时间 dto.end结束时间
     */
    @Override
    public void genSupplementsOrders(@NotNull PlatFactoryParams.GenSupplementsOrdersReqDto dto) {
        // 1. 补单可拉取 最长多少天前的数据  		    小于等于60天
        long preDays = 60L;
        // 2. 每次补单 时间跨度(ID)  开始-结束时间		小于等于1天
        long startEndRange = 60L;

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime start = DateNewUtils.utc8Zoned(dto.getStart());
        ZonedDateTime end = DateNewUtils.utc8Zoned(dto.getEnd());
        if (start.compareTo(now.minusDays(preDays)) < 0 || start.compareTo(end) >= 0) {
            throw new BusinessException(CodeInfo.PLAT_BET_SLIPS_SUPPLE_OVER_60_DAYS);
        }
        if (end.compareTo(now) > 0) {
            end = now;
        }
        ZonedDateTime temp;
        LinkedList<BetSlipsSupplemental> list = new LinkedList<>();
        do {
            temp = start.plusMinutes(startEndRange);

            // 请求参数
            String reqParams = buildPullBetReqParamsWithZoneSameInstant(start, temp);
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
     * 封装拉单请求参数
     *
     * @param startZonedDateTime 开始时间
     * @param endZonedDateTime   结束时间
     * @return JSONObject 请求参数
     */
    private String buildPullBetReqParams(ZonedDateTime startZonedDateTime, ZonedDateTime endZonedDateTime) {
        String startTime = startZonedDateTime.format(DateNewUtils.getDateTimeFormatter(DateNewUtils.Format.yyyyMMddHHmmss));
        String endTime = endZonedDateTime.format(DateNewUtils.getDateTimeFormatter(DateNewUtils.Format.yyyyMMddHHmmss));
        log.info("时间范围[" + startTime + " - " + endTime + "]");
        return requestParamsByGetDateTimeReport(startTime, endTime, 0, null, null);
    }

    /**
     * 数据批量持久化拉单数据
     *
     * @param responseBody responseBody
     */
    private void saveOrUpdateBatch(String responseBody) {
        JSONObject result = toJSONObject(responseBody);
        if(result!=null) {
            // 排除107的异常:请求成功没有数据
            if (!isOk(result) && WMRoutineParam.ErrorCode.E107.getCode() != getCode(result)) {
                // 状态:0-三方异常
                buildCodeInfo(responseBody);
            }
            List<BetslipsWm> list = processJSONArray(result);
            if (!CollectionUtils.isEmpty(list)) {
                betslipsWmServiceImpl.saveOrUpdateBatch(list,list.size());
            }
        }
    }

    /**
     * 修改时区GMT+8
     *
     * @param startZonedDateTime 开始时间
     * @param endZonedDateTime   结束时间
     * @return 请求参数
     */
    private String buildPullBetReqParamsWithZoneSameInstant(ZonedDateTime startZonedDateTime, ZonedDateTime endZonedDateTime) {
        startZonedDateTime = startZonedDateTime.withZoneSameInstant(DateNewUtils.getZoneId("GMT", "+8"));
        endZonedDateTime = endZonedDateTime.withZoneSameInstant(DateNewUtils.getZoneId("GMT", "+8"));
        return buildPullBetReqParams(startZonedDateTime, endZonedDateTime);
    }

    /**
     * 补充注单信息
     *
     * @param dto dto.requestInfo 补单请求参数
     */
    @Override
    public void betsRecordsSupplemental(@NotNull PlatFactoryParams.BetsRecordsSupplementReqDto dto) {
        try {
            String resBody = pullSend(dto.getRequestInfo());
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
