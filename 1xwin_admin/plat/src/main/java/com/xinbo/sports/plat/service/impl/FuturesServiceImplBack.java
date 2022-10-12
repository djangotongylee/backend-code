//package com.xinbo.sports.plat.service.impl;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import com.xinbo.sports.dao.generator.po.BetslipsFuturesLottery;
//import com.xinbo.sports.dao.generator.po.CoinPlatTransfer;
//import com.xinbo.sports.dao.generator.service.BetslipsFuturesLotteryService;
//import com.xinbo.sports.dao.generator.service.CoinPlatTransferService;
//import com.xinbo.sports.plat.base.CommissionBase;
//import com.xinbo.sports.plat.base.CommonPersistence;
//import com.xinbo.sports.plat.factory.PlatAbstractFactory;
//import com.xinbo.sports.plat.io.bo.FuturesLotteryRequestParameter;
//import com.xinbo.sports.plat.io.bo.PlatFactoryParams;
//import com.xinbo.sports.plat.io.bo.PlatFactoryParams.*;
//import com.xinbo.sports.plat.io.dto.base.CoinTransferPlatReqDto;
//import com.xinbo.sports.plat.io.enums.BasePlatParam;
//import com.xinbo.sports.plat.io.enums.FuturesLotteryRoutineParam;
//import com.xinbo.sports.service.base.CoinPlatTransfersBase;
//import com.xinbo.sports.service.base.UserServiceBase;
//import com.xinbo.sports.service.cache.redis.ConfigCache;
//import com.xinbo.sports.service.cache.redis.UserCache;
//import com.xinbo.sports.service.common.Constant;
//import com.xinbo.sports.service.exception.BusinessException;
//import com.xinbo.sports.service.io.bo.UserCacheBo;
//import com.xinbo.sports.utils.DateNewUtils;
//import com.xinbo.sports.utils.HttpUtils;
//import com.xinbo.sports.utils.components.response.CodeInfo;
//import lombok.Setter;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.stereotype.Service;
//
//import javax.annotation.Resource;
//import javax.validation.Valid;
//import javax.validation.constraints.NotNull;
//import java.math.BigDecimal;
//import java.net.ConnectException;
//import java.net.http.HttpConnectTimeoutException;
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
///**
// * <p>
// * BWG彩票
// * </p>
// *
// * @author andy
// * @since 2020/9/28
// */
//@Slf4j
//@Service("FuturesServiceImpl")
//public class FuturesServiceImplBack implements PlatAbstractFactory {
//    protected static final String MODEL = "Futures";
//    @Resource
//    private ConfigCache configCache;
//    @Resource
//    private CoinPlatTransfersBase coinPlatTransfersBase;
//    @Resource
//    private BetslipsFuturesLotteryService betslipsFuturesLotteryServiceImpl;
//    @Resource
//    private UserServiceBase userServiceBase;
//    @Resource
//    private CoinPlatTransferService coinPlatTransferServiceImpl;
//    @Resource
//    private UserCache userCache;
//    @Resource
//    private CommonPersistence commonPersistence;
//    @Resource
//    private CommissionBase commissionBase;
//
//    private String sysLang = null;
//    @Setter
//    protected FuturesLotteryRequestParameter.Config config;
//
//    protected int getCode(JSONObject p) {
//        int code = -1;
//        if (null != p) {
//            code = p.getInteger("code");
//        }
//        return code;
//    }
//
//    protected boolean isOk(JSONObject p) {
//        return FuturesLotteryRoutineParam.ErrorCode.SUCCESS.getCode() == getCode(p);
//    }
//
//    /**
//     * 校验错误码
//     *
//     * @param code 第三方错误码
//     */
//    protected void checkErrorCode(int code) {
//        if (null == FuturesLotteryRoutineParam.ERROR_CODE_MAPPER.get(code) && 0 != code) {
//            throw new BusinessException(CodeInfo.PLAT_SYSTEM_ERROR);
//        }
//        throw new BusinessException(FuturesLotteryRoutineParam.ERROR_CODE_MAPPER.get(code));
//    }
//
//    private JSONObject register(PlatLoginReqDto req) {
//        checkParams(req);
//        swithLang(req.getLang());
//        FuturesLotteryRequestParameter.LoginReqBody reqBody = FuturesLotteryRequestParameter.LoginReqBody.builder()
//                .appType(StringUtils.isNotBlank(req.getDevice()) && "d".equals(req.getDevice()) ? 2 : 1)
//                .sysLang(sysLang)
//                .username(req.getUsername())
//                .platId(config.getPlatId())
//                .companyKey(config.getCompanyKey())
//                .build();
//
//        String username = userServiceBase.filterUsername(req.getUsername());
//        UserCacheBo.UserCacheInfo userCacheInfo = userCache.getUserInfoByUserName(username);
//        if (null != userCacheInfo && null != userCacheInfo.getRole()) {
//            // 角色[非必填]:0-会员 4-测试
//            Integer role = userCacheInfo.getRole();
//            reqBody.setRole(4 != role ? 0 : role);
//        }
//        String responseBody = send(FuturesLotteryRequestParameter.UrlEnum.REGISTER_OR_LOGIN.getMethodName(), JSON.toJSONString(reqBody));
//        return JSON.parseObject(responseBody);
//    }
//
//    @Override
//    public PlatGameLoginResDto login(PlatLoginReqDto req) {
//        JSONObject result = register(req);
//        if (!isOk(result)) {
//            checkErrorCode(getCode(result));
//        }
//        // 成功后,拼接登录地址
//        return PlatGameLoginResDto.builder().type(1).url(getResultData(result).getString("loginUrl")).build();
//    }
//
//    @Override
//    public PlatQueryBalanceResDto queryBalance(PlatQueryBalanceReqDto req) {
//        FuturesLotteryRequestParameter.QueryBalanceReqBody reqBody = FuturesLotteryRequestParameter.QueryBalanceReqBody.builder()
//                .username(req.getUsername())
//                .platId(config.getPlatId())
//                .companyKey(config.getCompanyKey())
//                .build();
//        String responseBody = send(FuturesLotteryRequestParameter.UrlEnum.QUERYBALANCE.getMethodName(), JSON.toJSONString(reqBody));
//        JSONObject result = JSON.parseObject(responseBody);
//        if (!isOk(result)) {
//            checkErrorCode(getCode(result));
//        }
//        return PlatQueryBalanceResDto.builder().platCoin(getResultData(result).getBigDecimal("coin")).build();
//    }
//
//    /**
//     * getSuccess DATA
//     *
//     * @param result result
//     * @return resultData
//     */
//    private JSONObject getResultData(JSONObject result) {
//        return (JSONObject) result.get("data");
//    }
//
//    @Override
//    public PlatCoinTransferResDto coinUp(@Valid PlatCoinTransferReqDto platCoinTransferUpReqDto) {
//        checkParams(platCoinTransferUpReqDto);
//        CoinTransferPlatReqDto build = CoinTransferPlatReqDto.builder()
//                .coin(platCoinTransferUpReqDto.getCoin())
//                .orderId(platCoinTransferUpReqDto.getOrderId())
//                .username(platCoinTransferUpReqDto.getUsername())
//                .build();
//        // DEPOSIT-存款
//        return processCoinUpDown(build, "DEPOSIT");
//    }
//
//    @Override
//    public PlatCoinTransferResDto coinDown(@Valid PlatCoinTransferReqDto platCoinTransferDownReqDto) {
//        checkParams(platCoinTransferDownReqDto);
//        BigDecimal coin = platCoinTransferDownReqDto.getCoin();
//        // 是否全部下分: 1-全部 0-按金额
//        if (null != platCoinTransferDownReqDto.getIsFullAmount() && platCoinTransferDownReqDto.getIsFullAmount().intValue() == 1) {
//            // 查询余额
//            PlatQueryBalanceResDto platQueryBalanceResDto = queryBalance(
//                    PlatQueryBalanceReqDto.builder()
//                            .username(platCoinTransferDownReqDto.getUsername())
//                            .build()
//            );
//            if (null != platQueryBalanceResDto) {
//                coin = platQueryBalanceResDto.getPlatCoin();
//            }
//        }
//        if (coin.compareTo(BigDecimal.ZERO) <= 0) {
//            throw new BusinessException(CodeInfo.PLAT_COIN_INSUFFICIENT);
//        }
//        CoinTransferPlatReqDto build = CoinTransferPlatReqDto.builder()
//                .IsFullAmount(platCoinTransferDownReqDto.getIsFullAmount())
//                .coin(coin)
//                .orderId(platCoinTransferDownReqDto.getOrderId())
//                .username(platCoinTransferDownReqDto.getUsername())
//                .build();
//        // DRAWAL-提款
//        return processCoinUpDown(build, "DRAWAL");
//    }
//
//    /**
//     * 上下分逻辑处理
//     *
//     * @param dto      DTO
//     * @param category 类型:DEPOSIT-存款,DRAWAL-提款
//     * @return PlatCoinTransferResDto
//     */
//    private PlatCoinTransferResDto processCoinUpDown(CoinTransferPlatReqDto dto, String category) {
//        checkParams(dto);
//        String username = dto.getUsername();
//        String orderId = dto.getOrderId();
//        BigDecimal coin = dto.getCoin();
//        FuturesLotteryRequestParameter.DepositOrWithdrawalReqBody reqBody = FuturesLotteryRequestParameter.DepositOrWithdrawalReqBody.builder()
//                .username(username)
//                .category(category)
//                .coin(coin)
//                .orderPlat(orderId)
//
//                .platId(config.getPlatId())
//                .companyKey(config.getCompanyKey())
//                .build();
//        String responseBody = send(FuturesLotteryRequestParameter.UrlEnum.DEPOSIT_OR_WITHDRAWAL.getMethodName(), JSON.toJSONString(reqBody));
//        JSONObject result = JSON.parseObject(responseBody);
//        if (!isOk(result)) {
//            checkErrorCode(getCode(result));
//        }
//        JSONObject resultData = getResultData(result);
//        // 第三方平台余额
//        BigDecimal balance = resultData.getBigDecimal("coin");
//        // 第三方平台单号
//        long orderPlat = resultData.getLong("orderId");
//        log.info("[orderPlat=" + orderPlat + ",balance=" + balance + "]");
//        updateCoinUpDown(orderId, 1, coin, orderPlat + "", null);
//        return PlatCoinTransferResDto.builder().platCoin(balance).build();
//    }
//
//    /**
//     * 更新上下分记录
//     *
//     * @param orderId   上下分主键Id
//     * @param status    状态
//     * @param coin      金额
//     * @param orderPlat 第三方平台ID
//     * @param msg       错误信息
//     */
//    private void updateCoinUpDown(String orderId, Integer status, BigDecimal coin, String orderPlat, String msg) {
//        coinPlatTransfersBase.updateOrderPlat(orderId, status, coin, orderPlat, msg);
//    }
//
//
//    /**
//     * 检查转账状态
//     *
//     * @author: David
//     * @date: 04/05/2020
//     */
//    @Override
//    public Boolean checkTransferStatus(String orderId) {
//        if (StringUtils.isBlank(orderId)) {
//            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
//        }
//        CoinPlatTransfer po = coinPlatTransferServiceImpl.getById(orderId);
//        if (null == po) {
//            throw new BusinessException(CodeInfo.RECORD_NOT_EXIST);
//        }
//        UserCacheBo.UserCacheInfo user = userCache.getUserInfoById(po.getUid());
//        if (null == user) {
//            throw new BusinessException(CodeInfo.PLAT_ACCOUNT_NOT_EXISTS);
//        }
//        String username = userServiceBase.buildUsername(user.getUsername());
//        FuturesLotteryRequestParameter.CheckDepositOrWithdrawalStatusReqBody reqBody = FuturesLotteryRequestParameter.CheckDepositOrWithdrawalStatusReqBody.builder()
//                .username(username)
//                .orderPlat(orderId)
//                .platId(config.getPlatId())
//                .companyKey(config.getCompanyKey())
//                .build();
//        String responseBody = send(FuturesLotteryRequestParameter.UrlEnum.CHECK_DEPOSIT_OR_WITHDRAWALSTATUS.getMethodName(), JSON.toJSONString(reqBody));
//        JSONObject result = JSON.parseObject(responseBody);
//        return isOk(result);
//    }
//
//    @Override
//    public void pullBetsLips() {
//        String requestParams = "";
//        int lastFlUpdatedAt = 0;
//        try {
//            String strLastFlUpdatedAt = configCache.getLastUpdateTimeByModel(MODEL);
//            lastFlUpdatedAt = StringUtils.isBlank(configCache.getLastUpdateTimeByModel(MODEL)) ? getLastFlUpdatedAt() : Integer.parseInt(strLastFlUpdatedAt);
//
//            FuturesLotteryRequestParameter.GetBetListReqBody reqBody = FuturesLotteryRequestParameter.GetBetListReqBody.builder()
////                    .updatedAt(lastFlUpdatedAt)
//                    .updatedAt(1604419200)
//                    .platId(config.getPlatId())
//                    .companyKey(config.getCompanyKey())
//                    .build();
//            requestParams = JSON.toJSONString(reqBody);
//            String resBody = pullSend(FuturesLotteryRequestParameter.UrlEnum.GET_BET_LIST.getMethodName(), requestParams);
//            saveOrUpdateBatch(resBody);
//        } catch (BusinessException e) {
//            // 状态:0-三方异常
//            if (e.getCode().equals(CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0.getCode())) {
//                addBetSlipsException(0, requestParams, e.getMessage());
//            }
//        } catch (Exception e) {
//            // 状态:1-数据异常
//            log.error(MODEL + CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_1.getMsg() + ":" + e);
//            addBetSlipsException(1, requestParams, e.toString());
//        } finally {
//            // 拉单节点变化
//            configCache.reSetLastUpdateTimeByModel(MODEL, String.valueOf(getLastFlUpdatedAt()));
//        }
//    }
//
//    /**
//     * 最近结算时间
//     *
//     * @return 最近结算时间
//     */
//    private Integer getLastFlUpdatedAt() {
//        Integer lastFlUpdatedAt = 0;
//        BetslipsFuturesLottery po = betslipsFuturesLotteryServiceImpl.lambdaQuery()
//                .orderByDesc(BetslipsFuturesLottery::getFlUpdatedAt)
//                .last("limit 1").one();
//        if (null != po) {
//            lastFlUpdatedAt = po.getFlUpdatedAt();
//        }
//        return lastFlUpdatedAt;
//    }
//
//    /**
//     * 数据批量持久化拉单数据
//     *
//     * @param responseBody responseBody
//     */
//    private void saveOrUpdateBatch(String responseBody) {
//        JSONObject result = JSON.parseObject(responseBody);
//        if (!isOk(result)) {
//            // 状态:0-三方异常
//            checkErrorCode(getCode(result));
//        }
//        Object data = result.get("data");
//        if (Optional.ofNullable(data).isEmpty() || !(data instanceof JSONArray)) {
//            return;
//        }
//        var jsonArray = (JSONArray) data;
//        Map<String, Integer> usernameIdMap = userServiceBase.getUsernameIdMap();
//        if (null == usernameIdMap || jsonArray.isEmpty()) {
//            return;
//        }
//
//        // 分片处理
//        int totalSize = jsonArray.size();
//        int limitSize = 100;
//        int totalPage = (totalSize + limitSize - 1) / limitSize;
//        for (int skipIndex = 1; skipIndex <= totalPage; skipIndex++) {
//            List<BetslipsFuturesLottery> list = processRows(jsonArray, skipIndex, limitSize, usernameIdMap);
//            if (!list.isEmpty()) {
//                //结算佣金
//                if (Constant.BWG05.equals(configCache.getConfigByTitle(Constant.PLAT_PREFIX))) {
//                    commissionBase.commissionFutures(list);
//                }
//                betslipsFuturesLotteryServiceImpl.saveOrUpdateBatch(list, list.size());
//                log.info("入库成功,size=>{}", list.size());
//            }
//        }
//    }
//
//    private List<BetslipsFuturesLottery> processRows(JSONArray rows, int index, int pageSize, Map<String, Integer> usernameIdMap) {
//        return rows.stream().skip((index - 1) * pageSize).limit(pageSize)
//                .map(x -> {
//                    JSONObject jsonObject = (JSONObject) x;
//                    String username = userServiceBase.filterUsername(jsonObject.getString("username"));
//                    Integer xbUid = usernameIdMap.get(username);
//                    if (null != xbUid) {
//                        BetslipsFuturesLottery futuresLottery = jsonObject.toJavaObject(BetslipsFuturesLottery.class);
//                        futuresLottery.setXbUid(xbUid);
//                        futuresLottery.setXbUsername(username);
//                        futuresLottery.setFlCreatedAt(futuresLottery.getCreatedAt());
//                        futuresLottery.setFlUpdatedAt(futuresLottery.getUpdatedAt());
//                        return futuresLottery;
//                    }
//                    return null;
//                })
//                .filter(Objects::nonNull)
//                .map(bwg -> {
//                    bwg.setXbCoin(bwg.getCoin());
//                    bwg.setXbValidCoin(bwg.getCoin());
//                    // 开奖状态:WAIT-等待开奖,WIN-赢,LOST-输,TIE-和,CANCEL-撤单
//                    if (!(FuturesLotteryRoutineParam.FuturesLotteryStatus.CANCEL.getCode().equals(bwg.getStatus()) || FuturesLotteryRoutineParam.FuturesLotteryStatus.WAIT.getCode().equals(bwg.getStatus()))) {
//                        // 输赢金额
//                        bwg.setXbProfit(bwg.getWinLose());
//                    }
//                    bwg.setXbStatus(FuturesLotteryRoutineParam.STATUS_2_XBSTATUS.get(bwg.getStatus()));
//                    bwg.setUpdatedAt(DateNewUtils.now());
//                    return bwg;
//                }).collect(Collectors.toList());
//    }
//
//    @Override
//    public Boolean registerUser(PlatRegisterReqDto reqDto) {
//        if (null == reqDto) {
//            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
//        }
//        JSONObject register = register(PlatLoginReqDto.builder()
//                .device(reqDto.getDevice())
//                .username(reqDto.getUsername())
//                .lang(reqDto.getLang())
//                .build());
//        return FuturesLotteryRoutineParam.ErrorCode.SUCCESS.getCode() == getCode(register);
//    }
//
//    protected String send(String reqMethod, String reqBody) {
//        String result = null;
//        try {
//            String uri = config.getApiUrl() + reqMethod;
//            result = HttpUtils.doPost(uri, reqBody, MODEL);
//        } catch (ConnectException e) {
//            throw new BusinessException(CodeInfo.PLAT_CONNECTION_REFUSED);
//        } catch (HttpConnectTimeoutException e) {
//            throw new BusinessException(CodeInfo.PLAT_TIME_OUT);
//        } catch (Exception e) {
//            throw new BusinessException(CodeInfo.PLAT_CONNECTION_EXCEPTION);
//        }
//        return result;
//    }
//
//    /**
//     * 语言切换
//     *
//     * @param lang
//     */
//    private void swithLang(String lang) {
//        switch (lang) {
//            case "en":
//                sysLang = FuturesLotteryRoutineParam.Lang.EN_US.getCode();
//                break;
//            case "zh":
//                sysLang = FuturesLotteryRoutineParam.Lang.ZH_CN.getCode();
//                break;
//            case "vi":
//                sysLang = FuturesLotteryRoutineParam.Lang.VI_VN.getCode();
//                break;
//            case "th":
//                sysLang = FuturesLotteryRoutineParam.Lang.TH_TH.getCode();
//                break;
//            default:
//                break;
//        }
//    }
//
//    @Override
//    public PlatLogoutResDto logout(PlatLogoutReqDto dto) {
//        throw new UnsupportedOperationException();
//    }
//
//    private <E> void checkParams(E params) {
//        if (null == params) {
//            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
//        }
//    }
//
//    /**
//     * 封装三方异常抛出
//     *
//     * @param msg MSG
//     */
//    private void buildCodeInfo(String msg) {
//        CodeInfo codeInfo = CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0;
//        codeInfo.setMsg(msg);
//        throw new BusinessException(codeInfo);
//    }
//
//    /**
//     * 拉单send
//     *
//     * @param url       URL
//     * @param reqParams reqParams
//     * @return String
//     */
//    private String pullSend(String url, String reqParams) {
//        String result = "";
//        try {
//            result = send(url, reqParams);
//        } catch (Exception e) {
//            log.error(MODEL + CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0.getMsg() + ":" + e.toString());
//            buildCodeInfo(e.toString());
//        }
//        return result;
//    }
//
//    /**
//     * 拉单异常 再次拉单
//     *
//     * @param dto dto.requestInfo 拉单请求参数
//     */
//    @Override
//    public void betSlipsExceptionPull(@NotNull PlatFactoryParams.BetsRecordsSupplementReqDto dto) {
//        try {
//            String resBody = pullSend(FuturesLotteryRequestParameter.UrlEnum.GET_BET_LIST.getMethodName(), dto.getRequestInfo());
//            saveOrUpdateBatch(resBody);
//        } catch (BusinessException e) {
//            // 状态:0-三方异常
//            log.error(MODEL + CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0.getMsg() + ":" + e);
//            throw e;
//        } catch (Exception e) {
//            // 状态:1-数据异常
//            log.error(MODEL + CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_1.getMsg() + ":" + e);
//            throw e;
//        }
//    }
//
//    /**
//     * 添加拉单异常表
//     *
//     * @param category      状态:0-三方异常 1-数据异常
//     * @param requestParams 请求参数
//     * @param exceptionInfo 异常信息:三方-返回数据 数据-异常处理
//     */
//    private void addBetSlipsException(Integer category, String requestParams, String exceptionInfo) {
//        commonPersistence.addBetSlipsException(BasePlatParam.GAME.FUTURES_LOTTERY.getCode(), requestParams, category, exceptionInfo, 0);
//    }
//}
