package com.xinbo.sports.plat.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xinbo.sports.dao.generator.po.BetSlipsException;
import com.xinbo.sports.dao.generator.po.BetSlipsSupplemental;
import com.xinbo.sports.dao.generator.po.BetslipsDg;
import com.xinbo.sports.dao.generator.service.BetslipsDgService;
import com.xinbo.sports.service.base.CoinPlatTransfersBase;
import com.xinbo.sports.plat.base.CommonPersistence;
import com.xinbo.sports.plat.factory.PlatAbstractFactory;
import com.xinbo.sports.plat.io.bo.DGLiveRequestParameter;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams;
import com.xinbo.sports.plat.io.dto.base.CoinTransferPlatReqDto;
import com.xinbo.sports.plat.io.enums.BasePlatParam;
import com.xinbo.sports.plat.io.enums.DGRoutineParam;
import com.xinbo.sports.plat.io.enums.DGUrlEnum;
import com.xinbo.sports.service.base.UserServiceBase;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.HttpUtils;
import com.xinbo.sports.utils.MD5;
import com.xinbo.sports.utils.TextUtils;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.http.HttpConnectTimeoutException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.xinbo.sports.plat.io.bo.PlatFactoryParams.*;

/**
 * <p>
 * DG视讯
 * </p>
 *
 * @author andy
 * @since 2020/5/29
 */
@Slf4j
@Service("DGServiceImpl")
public class DGServiceImpl implements PlatAbstractFactory {
    private static final String MODEL = "DG";
    private static final String REQ_USER_NAME = "username";
    private static final String REQ_MEMBER = "member";
    @Resource
    private CoinPlatTransfersBase coinPlatTransfersBase;
    @Resource
    private BetslipsDgService betslipsDgServiceImpl;
    @Resource
    private UserServiceBase userServiceBase;
    @Resource
    private CommonPersistence commonPersistence;
    private String sysLang = null;
    @Setter
    private DGLiveRequestParameter.DgConfig config;


    // 有分页时延迟执行，解决请求频繁的问题:正常补单
    private static final ScheduledExecutorService GAME_GET_REPORT_SCHEDULED_POOL = Executors.newScheduledThreadPool(1);
    // 有分页时延迟执行，解决请求频繁的问题:消费补单
    private static final ScheduledExecutorService GAME_GET_TIP_GIFT_SCHEDULED_POOL = Executors.newScheduledThreadPool(1);


    private static int getCode(JSONObject p) {
        int code = -1;
        if (Objects.nonNull(p)) {
            code = p.getIntValue("codeId");
        }
        return code;
    }

    private static boolean isOk(JSONObject p) {
        return getCode(p) == 0;
    }

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

    private static JSONObject toJSONObject(String resBody) {
        return JSON.parseObject(resBody);
    }

    /**
     * 校验错误码
     *
     * @param code 第三方错误码
     */
    private static void checkErrorCode(int code) {
        if (null == DGRoutineParam.ERROR_CODE_MAPPER.get(code) && 0 != code) {
            throw new BusinessException(CodeInfo.PLAT_SYSTEM_ERROR);
        }
        throw new BusinessException(DGRoutineParam.ERROR_CODE_MAPPER.get(code));
    }

    public int registerUser(JSONObject params) {
        String username = params.getString(REQ_USER_NAME);
        // 构造通用请求参数
        JSONObject reqBody = (JSONObject) JSON.toJSON(getReqBaseInfo());
        // 构造特有请求参数
        DGLiveRequestParameter.Register register = DGLiveRequestParameter.Register.builder()
                .username(username)
                .password(MD5.encryption(username))
                .currencyName(config.getCurrency())
                .winLimit(0)
                .build();
        JSONObject member = (JSONObject) JSON.toJSON(register);
        reqBody.put(REQ_MEMBER, member);
        return getCode(toJSONObject(send(DGUrlEnum.DGLIVE_REGISTER.getMethodName(), reqBody)));
    }

    @Override
    public PlatGameLoginResDto login(PlatLoginReqDto platLoginReqDto) {
        if (null == platLoginReqDto) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        String username = "";
        try {
            username = platLoginReqDto.getUsername();
            processLang(platLoginReqDto.getLang());
            // 构造通用请求参数
            JSONObject reqBody = (JSONObject) JSON.toJSON(getReqBaseInfo());
            // 构造特殊参数
            reqBody.put("lang", sysLang);
            reqBody.put("domains", "1");
            JSONObject member = new JSONObject();
            member.put(REQ_USER_NAME, username);
            reqBody.put(REQ_MEMBER, member);
            JSONObject result = toJSONObject(send(DGUrlEnum.DGLIVE_LOGIN.getMethodName(), reqBody));
            if (!isOk(result)) {
                checkErrorCode(getCode(result));
            }
            // 成功后,拼接登录地址
            return getPlatGameLoginResDto(platLoginReqDto, result);
        } catch (BusinessException e) {
            if (CodeInfo.PLAT_ACCOUNT_NOT_EXISTS.getCode().equals(e.getCode())) {
                JSONObject reg = new JSONObject();
                reg.put(REQ_USER_NAME, username);
                if (0 == registerUser(reg)) {
                    return login(platLoginReqDto);
                }
            }
            throw e;
        }
    }

    private PlatGameLoginResDto getPlatGameLoginResDto(PlatLoginReqDto platLoginReqDto, JSONObject result) {
        StringBuilder loginUrl = new StringBuilder();
        String token = result.getString("token");
        JSONArray list = result.getJSONArray("list");
        if (!list.isEmpty()) {
            // 设备:H5-m PC-d 安卓-android, IOS-ios
            String device = platLoginReqDto.getDevice();
            if (StringUtils.isNotBlank(device) && device.equals("d")) {
                loginUrl.append(list.get(0));
            } else {
                loginUrl.append(list.get(1));
            }
        }
        loginUrl.append(token).append("&language=").append(sysLang);
        log.info("loginUrl=" + loginUrl);
        return PlatGameLoginResDto.builder().type(1).url(loginUrl.toString()).build();
    }

    @Override
    public PlatQueryBalanceResDto queryBalance(PlatQueryBalanceReqDto platQueryBalanceReqDto) {
        String username = "";
        if (null != platQueryBalanceReqDto) {
            username = platQueryBalanceReqDto.getUsername();
        }
        // 构造通用请求参数
        JSONObject reqBody = (JSONObject) JSON.toJSON(getReqBaseInfo());
        JSONObject member = new JSONObject();
        member.put(REQ_USER_NAME, username);
        reqBody.put(REQ_MEMBER, member);
        JSONObject result = toJSONObject(send(DGUrlEnum.DGLIVE_GETBALANCE.getMethodName(), reqBody));
        if (!isOk(result)) {
            checkErrorCode(getCode(result));
        }
        JSONObject resMember = result.getJSONObject(REQ_MEMBER);
        BigDecimal balance = BigDecimal.ZERO;
        if (Objects.nonNull(resMember)) {
            balance = resMember.getBigDecimal("balance");
            log.info("balance=" + balance);
        }
        return PlatQueryBalanceResDto.builder().platCoin(balance).build();
    }

    @Override
    public PlatCoinTransferResDto coinUp(@Valid PlatCoinTransferReqDto platCoinTransferUpReqDto) {
        CoinTransferPlatReqDto build = CoinTransferPlatReqDto.builder()
                .coin(platCoinTransferUpReqDto.getCoin())
                .orderId(platCoinTransferUpReqDto.getOrderId())
                .username(platCoinTransferUpReqDto.getUsername())
                .build();
        return processCoinUpDown(build);
    }

    @Override
    public PlatCoinTransferResDto coinDown(@Valid PlatCoinTransferReqDto platCoinTransferDownReqDto) {
        BigDecimal coin = platCoinTransferDownReqDto.getCoin();
        // 是否全部下分: 1-全部 0-按金额
        if (null != platCoinTransferDownReqDto.getIsFullAmount() && platCoinTransferDownReqDto.getIsFullAmount().intValue() == 1) {
            // 查询余额
            PlatQueryBalanceResDto platQueryBalanceResDto = queryBalance(
                    PlatQueryBalanceReqDto.builder()
                            .username(platCoinTransferDownReqDto.getUsername())
                            .build()
            );
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

        CoinTransferPlatReqDto build = CoinTransferPlatReqDto.builder()
                .IsFullAmount(platCoinTransferDownReqDto.getIsFullAmount())
                .coin(coin)
                .orderId(platCoinTransferDownReqDto.getOrderId())
                .username(platCoinTransferDownReqDto.getUsername())
                .build();
        return processCoinUpDown(build);
    }

    private PlatCoinTransferResDto processCoinUpDown(CoinTransferPlatReqDto dto) {
        if (null == dto) {
            log.error("params is null...");
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        String username = dto.getUsername();
        String orderId = dto.getOrderId();
        BigDecimal coin = dto.getCoin();
        JSONObject result = doRequestBody(username, coin, orderId);
        if (!isOk(result)) {
            int code = getCode(result);
            // 请求太频繁,回退用户金额
            if (DGRoutineParam.ErrorCode.E405.getCode() == code) {
                updateCoinUpDown(orderId, 2, coin, null, null != result ? result.toJSONString() : null);
            }
            checkErrorCode(code);
        }
        // 第三方平台余额
        BigDecimal balance = BigDecimal.ZERO;
        // 第三方平台单号
        String orderPlat = result.getString("data");
        JSONObject resMember = result.getJSONObject(REQ_MEMBER);
        if (Objects.nonNull(resMember)) {
            balance = resMember.getBigDecimal("balance");
        }
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
        if (coin.compareTo(BigDecimal.ZERO) < 0) {
            coin = coin.negate();
        }
        coinPlatTransfersBase.updateOrderPlat(orderId, status, coin, orderPlat, msg);
    }

    /**
     * 提交存取款请求body
     *
     * @param username
     * @param coin
     * @param orderId
     * @return
     */
    private JSONObject doRequestBody(String username, BigDecimal coin, String orderId) {
        JSONObject reqBody = (JSONObject) JSON.toJSON(getReqBaseInfo());
        reqBody.put("data", orderId);
        JSONObject member = new JSONObject();
        member.put(REQ_USER_NAME, username);
        member.put("amount", coin);
        reqBody.put(REQ_MEMBER, member);
        return toJSONObject(send(DGUrlEnum.DGLIVE_TRANSFER.getMethodName(), reqBody));
    }

    @Override
    public void pullBetsLips() {
        List<BetSlipsException> list = null;
        try {
            JSONObject reqBody = (JSONObject) JSON.toJSON(getReqBaseInfo());
            String requestUrl = config.getApiUrl() + DGUrlEnum.DGLIVE_GETREPORT.getMethodName() + config.getAgentName();
            String responseBody = pullSend(requestUrl, reqBody);
            JSONObject result = toJSONObject(responseBody);
            if (!isOk(result)) {
                // 状态:0-三方异常
                buildCodeInfo(responseBody);
            }
            JSONArray jsonArray = result.getJSONArray("list");
            if (Optional.ofNullable(jsonArray).isEmpty() || jsonArray.isEmpty()) {
                return;
            }
            List<BetslipsDg> batchList = processDataPersistence(jsonArray);
            if (!batchList.isEmpty()) {
                List<Long> idList = new ArrayList<>();
                batchList.forEach(s -> idList.add(s.getId()));
                if (!idList.isEmpty()) {
                    // 发送 -> 标记已抓取注单
                    sendMarkReport(idList);
                }
            }
        } catch (BusinessException e) {
            // 状态:0-三方异常
            if (e.getCode().equals(CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0.getCode())) {
                list = nextReqParamsByPullBetsLips(0, e.getMessage());
            }
        } catch (Exception e) {
            // 状态:1-数据异常
            log.error(MODEL + CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_1.getMsg() + ":" + e);
            list = nextReqParamsByPullBetsLips(1, e.toString());
        } finally {
            if (Optional.ofNullable(list).isPresent() && !list.isEmpty()) {
                commonPersistence.addBatchBetSlipsExceptionList(list);
            }
        }

    }

    /**
     * 发送 -> 标记已抓取注单
     *
     * @param idList id列表
     */
    private void sendMarkReport(List<Long> idList) {
        JSONObject reqBody = (JSONObject) JSON.toJSON(getReqBaseInfo());
        reqBody.put("list", idList);
        String msg = MODEL + "标记已抓取注单";
        if (!isOk(toJSONObject(send(DGUrlEnum.DGLIVE_MARKREPORT.getMethodName(), reqBody)))) {
            log.error(msg + "失败,idList.size=" + idList.size());
        }
        log.info(msg + "成功,idList.size=" + idList.size());
    }

    /**
     * 检查转账状态
     *
     * @param orderId 订单号,对应sp_coin_plat_transfer表的主键ID
     * @return true或异常
     */
    @Override
    public Boolean checkTransferStatus(String orderId) {
        if (StringUtils.isBlank(orderId)) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        JSONObject reqBody = (JSONObject) JSON.toJSON(getReqBaseInfo());
        reqBody.put("data", orderId);
        JSONObject result = toJSONObject(send(DGUrlEnum.DGLIVE_CHECKTRANSFER.getMethodName(), reqBody));
        return isOk(result);
    }

    @Override
    public Boolean registerUser(PlatRegisterReqDto reqDto) {
        if (Objects.isNull(reqDto)) {
            log.error("params is null...");
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        JSONObject reg = new JSONObject();
        reg.put(REQ_USER_NAME, reqDto.getUsername());
        return 0 == registerUser(reg);
    }

    private void processBedsitsDg(Integer time, BetslipsDg entity, Map<String, Integer> usernameIdMap, List<BetslipsDg> betslipsDgList) {
        if (null == entity) {
            return;
        }
        if (null == usernameIdMap) {
            return;
        }
        String username = userServiceBase.filterUsername(entity.getUsername());
        Integer xbUid = usernameIdMap.get(username);
        if (null == xbUid) {
            return;
        }
        entity.setXbUid(xbUid);
        entity.setXbUsername(username);
        // processCreatedAt
        if (Objects.nonNull(entity.getBetTime())) {
            entity.setCreatedAt(processCreatedAt(entity.getBetTime()));
        }
        entity.setUpdatedAt(time);
        entity.setXbCoin(entity.getBetPoints());
        entity.setXbValidCoin(entity.getAvailableBet());
        // 是否结算:0-未结算 1-已结算 2-已撤销(该注单为对冲注单)
        if (entity.getIsRevocation() == 1) {
            entity.setXbProfit(entity.getWinOrLoss().subtract(entity.getBetPoints()));
            entity.setXbStatus(getStatus(entity.getXbProfit().compareTo(BigDecimal.ZERO)));
        } else if (entity.getIsRevocation() == 2) {
            entity.setXbStatus(4);
        }
        entity.setUpdatedAt(time);
        betslipsDgList.add(entity);
    }

    private String send(String reqMethod, JSONObject reqBody) {
        String result = null;
        String uri = "";
        try {
            uri = config.getApiUrl() + reqMethod + config.getAgentName();
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
     * 补单发送
     *
     * @param requestUrl
     * @param reqBody
     * @return
     */
    private String pullSend(String requestUrl, JSONObject reqBody) {
        String result = "";
        try {
            result = HttpUtils.doPost(requestUrl, reqBody.toJSONString(), MODEL);
        } catch (Exception e) {
            log.error(MODEL + CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0.getMsg() + ":" + e.toString());
            buildCodeInfo(e.toString());
        }
        return result;
    }

    /**
     * 获取通用请求参数
     *
     * @return
     */
    private DGLiveRequestParameter.ReqBase getReqBaseInfo() {
        int randomNum = TextUtils.generatePromoCode();
        String agentName = config.getAgentName();
        String apiKey = config.getApiKey();
        // token = agentName + apiKey + randomNum
        String token = MD5.encryption(agentName + apiKey + randomNum);
        return DGLiveRequestParameter.ReqBase.builder()
                .token(token)
                .random(randomNum)
                .build();
    }

    /**
     * 语言切换
     *
     * @param lang
     */
    private void processLang(String lang) {
        switch (lang) {
            case "en":
                sysLang = DGRoutineParam.Lang.EN.getCode();
                break;
            case "zh":
                sysLang = DGRoutineParam.Lang.ZH_CN.getCode();
                break;
            case "tw":
                sysLang = DGRoutineParam.Lang.ZH_TW.getCode();
                break;
            case "kr":
                sysLang = DGRoutineParam.Lang.KR.getCode();
                break;
            case "my":
                sysLang = DGRoutineParam.Lang.MY.getCode();
                break;
            case "vi":
                sysLang = DGRoutineParam.Lang.VI.getCode();
                break;
            case "th":
                sysLang = DGRoutineParam.Lang.TH.getCode();
                break;
            default:
                break;
        }
    }


    @Override
    public PlatLogoutResDto logout(PlatLogoutReqDto dto) {
        throw new UnsupportedOperationException();
    }

    /**
     * 根据起始、结束时间 生成补单信息
     * <p>
     * DG:
     * 1. 补单可拉取 最长多少天前的数据  			小于等于6个月
     * 2. 每次补单 时间跨度(ID)  开始-结束时间		小于等于1天
     * 3. 补单接口  间隔时间						大于等于30秒
     * 4. 补单最大数据量(条)						有分页
     * <p>
     * 补单接口的正常下注注单和小费注单是分开哦，如有会员发送小费需要通过小费接口获取
     * 正常下注注单：http://report.dg0.co/game/getReport/
     * 小费接口：http://report.dg0.co/game/getTipGift/
     *
     * @param dto dto.start 开始时间 dto.end结束时间
     */
    @Override
    public void genSupplementsOrders(@NotNull PlatFactoryParams.GenSupplementsOrdersReqDto dto) {
        // 1. 补单可拉取 最长多少天前的数据  			小于等于6个月
        long preDays = 180L;
        // 2. 每次补单 时间跨度(ID)  开始-结束时间		小于等于1天
        long startEndRange = 60L;

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime start = DateNewUtils.utc8Zoned(dto.getStart());
        ZonedDateTime end = DateNewUtils.utc8Zoned(dto.getEnd());
        start = start.with(LocalTime.MIN);
        end = end.with(LocalTime.MAX);
        if (start.compareTo(now.minusDays(preDays)) < 0 || start.compareTo(end) >= 0) {
            throw new BusinessException(CodeInfo.PLAT_BET_SLIPS_SUPPLE_OVER_180_DAYS);
        }
        if (end.compareTo(now) > 0) {
            end = now;
        }
        ZonedDateTime temp;
        LinkedList<BetSlipsSupplemental> list = new LinkedList<>();

        // 相差几天
        long days = DateNewUtils.daysDiff(start, end);
        long maxCount = 24 * (days + 1);
        int count = 0;
        do {
            // 60分钟时间切片
            temp = start.plusMinutes(startEndRange);
            temp = temp.minusSeconds(1);

            // 外层JSON
            JSONObject outerJSONObject = new JSONObject();
            // 补单:normal正常下注补单
            outerJSONObject.put(DGUrlEnum.GAME_GETREPORT.getMethodName(), buildPullBetReqParams(start, temp));
            Integer currentTime = (int) now.toInstant().getEpochSecond();
            BetSlipsSupplemental po = PlatAbstractFactory
                    .buildBetSlipsSupplemental(dto.getGameId(), start.toString(), temp.toString(), outerJSONObject.toJSONString(), currentTime);
            list.add(po);


            // 补单:tip会员发小费补单
            outerJSONObject = new JSONObject();
            outerJSONObject.put(DGUrlEnum.GAME_GETTIPGIFT.getMethodName(), buildPullBetReqParams(start, temp));
            po = PlatAbstractFactory
                    .buildBetSlipsSupplemental(dto.getGameId(), start.toString(), temp.toString(), outerJSONObject.toJSONString(), currentTime);
            list.add(po);


            start = start.plusMinutes(startEndRange);
            count++;
        } while (count < maxCount);

        if (!list.isEmpty()) {
            commonPersistence.addBatchBetSlipsSupplementalList(list);
        }

    }

    /**
     * 拉单异常后，下次回归异常的请求参数
     *
     * @param category      状态:0-三方异常 1-数据异常
     * @param exceptionInfo 异常信息:三方-返回数据 数据-异常处理
     * @return BetSlipsException.request 请求参数
     */
    private List<BetSlipsException> nextReqParamsByPullBetsLips(Integer category, String exceptionInfo) {
        int now = DateNewUtils.now();
        int gameListId = BasePlatParam.GAME.DG_LIVE.getCode();


        List<BetSlipsException> list = new ArrayList<>();
        ZonedDateTime currentTime = LocalDateTime.now().atZone(DateNewUtils.getZoneId("UTC", "+8"));
        ZonedDateTime start = currentTime.with(LocalTime.MIN);
        // 60分钟
        long startEndRange = 60L;
        int count = 0;
        ZonedDateTime temp;
        do {
            temp = start.plusMinutes(startEndRange);
            temp = temp.minusSeconds(1);
            // 实际请求参数
            JSONObject reqParams = buildPullBetReqParams(start, temp);
            // 外层JSON
            JSONObject outerJSONObject = new JSONObject();
            // 补单:normal正常下注补单
            outerJSONObject.put(DGUrlEnum.GAME_GETREPORT.getMethodName(), reqParams);

            BetSlipsException po = new BetSlipsException();
            po.setRequest(outerJSONObject.toJSONString());
            po.setGameListId(gameListId);
            po.setInfo(exceptionInfo);
            po.setCategory(category);
            po.setCreatedAt(now);
            po.setUpdatedAt(now);
            list.add(po);


            // 补单:tip会员发小费补单
            outerJSONObject = new JSONObject();
            outerJSONObject.put(DGUrlEnum.GAME_GETTIPGIFT.getMethodName(), reqParams);
            po = new BetSlipsException();
            po.setRequest(outerJSONObject.toJSONString());
            po.setGameListId(gameListId);
            po.setInfo(exceptionInfo);
            po.setCategory(category);
            po.setCreatedAt(now);
            po.setUpdatedAt(now);
            list.add(po);


            start = start.plusMinutes(startEndRange);
            count++;
        } while (count < 24);

        return list;
    }

    /**
     * 封装拉单请求参数
     *
     * @param startZonedDateTime 开始时间
     * @param endZonedDateTime   结束时间
     * @return JSONObject 请求参数
     */
    private JSONObject buildPullBetReqParams(ZonedDateTime startZonedDateTime, ZonedDateTime endZonedDateTime) {
        startZonedDateTime = startZonedDateTime.withZoneSameInstant(DateNewUtils.getZoneId("GMT", "+8"));
        endZonedDateTime = endZonedDateTime.withZoneSameInstant(DateNewUtils.getZoneId("GMT", "+8"));


        String startTime = startZonedDateTime.format(DateNewUtils.getDateTimeFormatter(DateNewUtils.Format.yyyy_MM_dd_HH_mm_ss));
        String endTime = endZonedDateTime.format(DateNewUtils.getDateTimeFormatter(DateNewUtils.Format.yyyy_MM_dd_HH_mm_ss));
        log.info("时间范围[" + startTime + " - " + endTime + "]");
        return buildPullBetSlipsSupplementalReqParams(startTime, endTime);
    }


    /**
     * 消费补单与正常补单URL切换
     * 正常补单http://report.dg0.co/game/getReport/
     * 消费补单http://report.dg0.co/game/getTipGift/
     *
     * @param reqBody 请求body
     */
    private void switchReqMethod(JSONObject reqBody) {

        if (reqBody.containsKey(DGUrlEnum.GAME_GETREPORT.getMethodName())) {
            JSONObject reqParams = reqBody.getJSONObject(DGUrlEnum.GAME_GETREPORT.getMethodName());
            int pageCount = pullBetSlipsSupplemental(DGUrlEnum.GAME_GETREPORT.getMethodName(), reqParams);
            if (pageCount > 1) {
                for (int i = 0; i < pageCount; i++) {
                    int pageNow = 2 + i;
                    reqParams.put("pageNow", pageNow);
                    // 延迟20秒(三方接口请求频繁在10秒以上)
                    int delay = 20 * (i + 1);
                    GAME_GET_REPORT_SCHEDULED_POOL.schedule(() -> pullBetSlipsSupplemental(DGUrlEnum.GAME_GETREPORT.getMethodName(), reqParams), delay, TimeUnit.SECONDS);
                }
            }
        }

        if (reqBody.containsKey(DGUrlEnum.GAME_GETTIPGIFT.getMethodName())) {
            JSONObject reqParams = reqBody.getJSONObject(DGUrlEnum.GAME_GETTIPGIFT.getMethodName());
            int pageCount = pullBetSlipsSupplemental(DGUrlEnum.GAME_GETTIPGIFT.getMethodName(), reqParams);
            if (pageCount > 1) {
                for (int i = 0; i < pageCount; i++) {
                    int pageNow = 2 + i;
                    reqParams.put("pageNow", pageNow);
                    // 延迟20秒(三方接口请求频繁在10秒以上)
                    int delay = 20 * (i + 1);
                    GAME_GET_TIP_GIFT_SCHEDULED_POOL.schedule(() -> pullBetSlipsSupplemental(DGUrlEnum.GAME_GETTIPGIFT.getMethodName(), reqParams), delay, TimeUnit.SECONDS);
                }
            }
        }
    }

    /**
     * 补单接口
     *
     * @param reqMethod 请求方法
     * @param reqBody   请求Body
     */
    private int pullBetSlipsSupplemental(String reqMethod, JSONObject reqBody) {
        int pageCount = 0;
        String requestUrl = config.getReportUrl() + reqMethod;
        String responseBody = pullSend(requestUrl, reqBody);
        JSONObject dataObject = toJSONObject(responseBody);
        if (!isOk(dataObject)) {
            // 状态:0-三方异常
            buildCodeInfo(responseBody);
        }
        JSONObject data = dataObject.getJSONObject("data");
        if (null == data) {
            return pageCount;
        }
        int rowCount = data.getIntValue("rowCount");
        if (0 == rowCount) {
            log.info(MODEL + "======无记录======" + data.toJSONString());
            return pageCount;
        }
        processDataPersistence(data.getJSONArray("records"));
        return data.getIntValue("pageCount");
    }

    /**
     * build 补单请求参数
     *
     * @param beginTime 开始时间
     * @param endTime   结束时间
     * @return 补单请求参数
     */
    private JSONObject buildPullBetSlipsSupplementalReqParams(String beginTime, String endTime) {
        JSONObject reqBody = buildSlipsSupplementalReqKey();
        reqBody.put("beginTime", beginTime);
        reqBody.put("endTime", endTime);
        return reqBody;
    }

    /**
     * 补单通用请求参数
     *
     * @return 补单通用请求参数
     */
    private JSONObject buildSlipsSupplementalReqKey() {
        String agentName = config.getAgentName();
        String apiKey = config.getApiKey();
        // token = MD5(agentName+API key)
        String token = MD5.encryption(agentName + apiKey);
        JSONObject reqBody = new JSONObject(new LinkedHashMap<>());
        reqBody.put("token", token);
        reqBody.put("agentName", agentName);
        return reqBody;
    }


    /**
     * 数据处理并入库
     *
     * @param jsonArray 三方平台返回数据
     * @return 成功入库数据
     */
    private List<BetslipsDg> processDataPersistence(JSONArray jsonArray) {
        List<BetslipsDg> javaList = jsonArray.toJavaList(BetslipsDg.class);
        List<BetslipsDg> batchList = new ArrayList<>();
        Integer time = DateNewUtils.now();
        if (!javaList.isEmpty()) {
            Map<String, Integer> usernameIdMap = userServiceBase.getUsernameIdMap();
            for (BetslipsDg entity : javaList) {
                processBedsitsDg(time, entity, usernameIdMap, batchList);
            }
        }
        if (!batchList.isEmpty()) {
            betslipsDgServiceImpl.saveOrUpdateBatch(batchList,batchList.size());
        }
        return batchList;
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
     * 补充注单信息
     *
     * @param dto dto.requestInfo 补单请求参数
     */
    @Override
    public void betsRecordsSupplemental(@NotNull PlatFactoryParams.BetsRecordsSupplementReqDto dto) {
        try {
            switchReqMethod(toJSONObject(dto.getRequestInfo()));
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
