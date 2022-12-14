package com.xinbo.sports.plat.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.xinbo.sports.dao.generator.po.BetslipsShabaSports;
import com.xinbo.sports.dao.generator.service.BetslipsShabaSportsService;
import com.xinbo.sports.service.base.CoinPlatTransfersBase;
import com.xinbo.sports.plat.base.CommonPersistence;
import com.xinbo.sports.plat.base.PlatHttpBase;
import com.xinbo.sports.plat.factory.PlatAbstractFactory;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams.*;
import com.xinbo.sports.plat.io.bo.ShaBaSportsParameter;
import com.xinbo.sports.plat.io.enums.BasePlatParam;
import com.xinbo.sports.plat.io.enums.IBCExceptionParam;
import com.xinbo.sports.plat.io.enums.SBPlatEnum;
import com.xinbo.sports.service.base.ExceptionThreadLocal;
import com.xinbo.sports.service.base.UserServiceBase;
import com.xinbo.sports.service.cache.KeyConstant;
import com.xinbo.sports.service.common.RedisConstant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.enums.BaseEnum;
import com.xinbo.sports.utils.BeanConvertUtils;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.DateUtils;
import com.xinbo.sports.utils.JedisUtil;
import com.xinbo.sports.utils.components.response.CodeInfo;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.alibaba.fastjson.JSON.toJSONString;
import static com.xinbo.sports.plat.io.bo.ShaBaSportsParameter.*;
import static com.xinbo.sports.plat.io.enums.SBPlatEnum.SBActionEnum.*;
import static com.xinbo.sports.utils.components.response.CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0;
import static java.util.Objects.*;

/**
 * @author: wells
 * @date: 2020/7/14
 * @description:
 */
@Slf4j
@Service("IBCServiceImpl")
public class IBCServiceImpl implements PlatAbstractFactory {
    /*?????????*/
    protected static final String ERROR_CODE = "error_code";
    /*????????????????????????*/
    private static final String BET_DETAILS = "BetDetails";
    /*??????*/
    private static final Map<String, String> LANG_MAP = Map.of(
            "zh", "cs",
            "en", "en",
            "th", "th",
            "vi", "vn");
    /*??????????????????*/
    @Setter
    private SBPlatConfig config;
    @Resource
    private JedisUtil jedisUtil;
    @Resource
    private BetslipsShabaSportsService betslipsShabaSportsServiceImpl;
    @Resource
    private CoinPlatTransfersBase coinPlatTransfersBase;
    @Resource
    private UserServiceBase userServiceBase;
    @Resource
    private CommonPersistence commonPersistence;

    /**
     * ????????????????????????
     *
     * @param platLoginReqDto {username, lang, device, slotId}
     * @return ????????????
     */
    @Override
    public PlatGameLoginResDto login(PlatLoginReqDto platLoginReqDto) {
        //????????????
        var loginBo = LoginBo.builder()
                .vendorId(config.getVendorId())
                .vendorMemberId(config.getOperatorId() + platLoginReqDto.getUsername())
                .build();
        JSONObject loginCall = send(LOGIN, loginBo);
        var code = loginCall.getInteger(ERROR_CODE);
        //?????????????????????????????????
        if (code != null && 2 == code) {
            registerUser(PlatRegisterReqDto.builder().username(platLoginReqDto.getUsername()).build());
            loginCall = send(LOGIN, loginBo);
        }
        //????????????
        String reUrl;
        if (BaseEnum.DEVICE.M.getValue().equals(platLoginReqDto.getDevice())) {
            reUrl = config.getH5LoginUrl();
        } else {
            reUrl = config.getPcLoginUrl();
        }
        reUrl = reUrl + "?lang=" + LANG_MAP.get(platLoginReqDto.getLang()) + "&token=" + loginCall.getString("Data");
        return PlatGameLoginResDto.builder().type(1).url(reUrl).build();
    }

    /**
     * ??????
     */
    @Override
    public Boolean registerUser(PlatRegisterReqDto platLoginReqDto) {
        var createMemberBO = CreateMemberBO.builder()
                .vendorId(config.getVendorId())
                .vendorMemberId(config.getOperatorId() + platLoginReqDto.getUsername())
                .username(config.getOperatorId() + platLoginReqDto.getUsername())
                .operatorid(config.getOperatorId().replace("_", ""))
                .oddsType(config.getOddsType())
                .currency(EnumUtils.getEnum(SBPlatEnum.CURRENCY.class, config.getCurrency()).getCode())
                .maxTransfer(new BigDecimal(config.getMaxTransfer()))
                .minTransfer(new BigDecimal(config.getMinTransfer()))
                .build();
        send(CREATE, createMemberBO);
        return true;
    }


    /**
     * ????????????
     *
     * @param dto {"username"}
     * @return success:1-?????? 0-??????
     */
    @Override
    public PlatLogoutResDto logout(PlatLogoutReqDto dto) {
        return null;
    }

    /**
     * ????????????
     *
     * @param platCoinTransferUpReqDto {"coin","orderId","username"}
     * @return {platCoin}
     */
    @Override
    public PlatCoinTransferResDto coinUp(PlatCoinTransferReqDto platCoinTransferUpReqDto) {
        return transfer(platCoinTransferUpReqDto, 1);
    }

    /**
     * ????????????
     *
     * @param platCoinTransferDownReqDto {"coin","orderId","username"}
     * @return {platCoin}
     */
    @Override
    public PlatCoinTransferResDto coinDown(PlatCoinTransferReqDto platCoinTransferDownReqDto) {
        return transfer(platCoinTransferDownReqDto, 0);
    }

    /**
     * ??????
     */
    public PlatCoinTransferResDto transfer(PlatCoinTransferReqDto dto, Integer transferId) {
        var transferBo = FundTransfer.builder()
                .vendorId(config.getVendorId())
                .vendorMemberId(config.getOperatorId() + dto.getUsername())
                .vendorTransId(config.getOperatorId() + dto.getOrderId())
                .amount(dto.getCoin())
                .currency(EnumUtils.getEnum(SBPlatEnum.CURRENCY.class, config.getCurrency()).getCode())
                .direction(transferId)
                .walletId(1)
                .build();
        JSONObject call = send(FUND_TRANSFER, transferBo);
        var afterAmount = call.getJSONObject("Data").getBigDecimal("after_amount");
        afterAmount = afterAmount == null ? BigDecimal.ZERO : afterAmount;
        return PlatCoinTransferResDto.builder().platCoin(afterAmount).build();
    }

    /**
     * ??????????????????
     *
     * @param dto {"username"}
     * @return {platCoin}
     */
    @Override
    public PlatQueryBalanceResDto queryBalance(PlatQueryBalanceReqDto dto) {
        var checkUserBalanceBo = CheckUserBalance.builder()
                .vendorId(config.getVendorId())
                .vendorMemberIds(config.getOperatorId() + dto.getUsername())
                .walletId(1)
                .build();
        JSONObject call = send(CHECK_USER_BALANCE, checkUserBalanceBo);
        var balance = call.getJSONArray("Data").getJSONObject(0).getBigDecimal("balance");
        balance = balance == null ? BigDecimal.ZERO : balance;
        return PlatQueryBalanceResDto.builder().platCoin(balance).build();
    }

    /**
     * ????????????????????????
     */
    @Override
    public void pullBetsLips() {
        //??????????????????
        var versionKey = jedisUtil.hget(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH, RedisConstant.SHA_BA);
        BetslipsShabaSports betslipsShabaSports = null;
        if (Strings.isEmpty(versionKey)) {
            betslipsShabaSports = betslipsShabaSportsServiceImpl.lambdaQuery()
                    .orderByDesc(BetslipsShabaSports::getVersionKey)
                    .last("limit 1").one();
        }
        //?????????????????????versionKey???0??????
        var reqVersionKey = Long.parseLong(config.getVersionKey());
        if (betslipsShabaSports != null || Strings.isNotEmpty(versionKey)) {
            reqVersionKey = nonNull(betslipsShabaSports) ? betslipsShabaSports.getVersionKey() : 0;
            reqVersionKey = Strings.isNotEmpty(versionKey) ? Long.parseLong(versionKey) : reqVersionKey;
        }
        XxlJobLogger.log("??????VersionKey=" + reqVersionKey);
        var call = pullData(reqVersionKey);
        var jsonArray = call.getJSONObject("Data").getJSONArray(BET_DETAILS);
        saveDataBase(jsonArray);
        //??????????????????
        var lastVersionKey = call.getJSONObject("Data").getLong("last_version_key");
        XxlJobLogger.log("??????lastVersionKey=" + lastVersionKey);
        jedisUtil.hset(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH, RedisConstant.SHA_BA, String.valueOf(lastVersionKey));
    }

    /**
     * ??????????????????
     *
     * @author: David
     * @date: 04/05/2020
     */
    @Override
    public Boolean checkTransferStatus(String orderId) {
        try {
            var checkFundTransferBo = CheckFundTransfer.builder()
                    .vendorId(config.getVendorId())
                    .vendorTransId(config.getOperatorId() + orderId)
                    .walletId(1)
                    .build();
            var call = send(CHECK_FUND_TRANSFER, checkFundTransferBo);
            var code = call.getInteger(ERROR_CODE);
            if (code == 0) {
                return true;
            } else if (code == 1) {
                return false;
            } else {
                log.info("???????????????????????????");
                throw new BusinessException(CodeInfo.PLAT_SYSTEM_ERROR);
            }
        } catch (Exception e) {
            log.info("??????????????????" + e.getMessage());
            throw new BusinessException(CodeInfo.PLAT_SYSTEM_ERROR);
        }
    }

    /**
     * ????????????????????????????????????
     */
    private JSONObject pullData(Long reqVersionKey) {
        //?????????????????????
        var betRequestParamsBo = BetRequestParams.builder()
                .vendorId(config.getVendorId())
                .versionKey(reqVersionKey)
                .build();
        var call = send(GET_BET, betRequestParamsBo);
        processException(call, GET_BET);
        return call;
    }

    /**
     * ????????????
     */
    public void saveDataBase(JSONArray jsonArray) {
        var now = DateUtils.getCurrentTime();
        if (jsonArray != null && !jsonArray.isEmpty()) {
            //??????????????????
            Map<String, Integer> userMap = userServiceBase.getUsernameIdMap();
            List<BetslipsShabaSports> betslipsShabaSportsList = new ArrayList<>();
            for (var x : jsonArray) {
                var betDetailBo = parseObject(toJSONString(x), ShaBaSportsParameter.BetDetail.class);
                //???????????????????????????
                var userName = userServiceBase.filterUsername(betDetailBo.getVendorMemberId().replace(config.getOperatorId(), ""));
                if (StringUtils.isEmpty(userName)) {
                    continue;
                }
                var retBetslipsSha = BeanConvertUtils.copyProperties(betDetailBo, BetslipsShabaSports::new, (bo, sb) -> {
                    var xbStatus = transferStatus(bo.getTicketStatus());
                    var validCoin = BasePlatParam.SPORTS_STATUS_LIST.contains(xbStatus) ? bo.getStake() : BigDecimal.ZERO;
                    var uid = userMap.get(userName);
                    sb.setXbUsername(userName);
                    sb.setXbUid(uid == null ? 0 : uid);
                    sb.setXbCoin(bo.getStake());
                    sb.setXbValidCoin(validCoin);
                    sb.setXbProfit(bo.getWinlostAmount());
                    sb.setXbStatus(transferStatus(bo.getTicketStatus()));
                    var zoneTime = DateNewUtils.utc8Zoned((int) (bo.getTransactionTime().getTime() / 1000));
                    var formatTime = DateNewUtils.utc8Str(zoneTime, DateNewUtils.Format.yyyy_MM_dd_HH_mm_ss);
                    var createdAt = DateNewUtils.oriTimeZoneToBeiJingZone(formatTime, DateNewUtils.Format.yyyy_MM_dd_HH_mm_ss, "UTC", "-4");
                    sb.setCreatedAt(createdAt);
                    sb.setUpdatedAt(now);
                });
                betslipsShabaSportsList.add(retBetslipsSha);
            }
            betslipsShabaSportsServiceImpl.saveOrUpdateBatch(betslipsShabaSportsList, 1000);
        }
    }

    /**
     * ????????????-????????????????????????xb???????????????
     *
     * @param ticketStatus ?????????
     * @return ??????????????????
     */
    public Integer transferStatus(String ticketStatus) {
        //?????????
        var status = BasePlatParam.BetRecordsStatus.WAIT_SETTLE.getCode();
        switch (ticketStatus.toLowerCase()) {
            //?????????
            case "waiting":
                //?????????
            case "running":
                status = BasePlatParam.BetRecordsStatus.WAIT_SETTLE.getCode();
                break;
            //??????
            case "void":
                //??????
            case "refund":
                status = BasePlatParam.BetRecordsStatus.BET_REFUSE.getCode();
                break;
            case "reject":
                status = BasePlatParam.BetRecordsStatus.GAME_CANCEL.getCode();
                break;
            case "lose":
                status = BasePlatParam.BetRecordsStatus.LOSE.getCode();
                break;
            case "won":
                status = BasePlatParam.BetRecordsStatus.WIN.getCode();
                break;
            case "draw":
                status = BasePlatParam.BetRecordsStatus.DRAW.getCode();
                break;
            case "half won":
                status = BasePlatParam.BetRecordsStatus.WIN_HALF.getCode();
                break;
            case "half lose":
                status = BasePlatParam.BetRecordsStatus.LOSE_HALF.getCode();
                break;
            default:
                break;
        }
        return status;
    }

    /**
     * ??????????????????
     *
     * @param sbActionEnum ????????????
     * @param object       ???????????????
     */
    public JSONObject send(SBPlatEnum.SBActionEnum sbActionEnum, Object object) {
        JSONObject jsonObject = parseObject(toJSONString(object));
        JSONObject call = new JSONObject();
        try {
            List<String> listParams = Lists.newArrayList();
            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                String param = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8);
                listParams.add(param);
            }
            String params = StringUtils.join(listParams, "&");
            //????????????
            ExceptionThreadLocal.setRequestParams(toJSONString(object));
            String result = PlatHttpBase.postHttp(config.getApiUrl() + sbActionEnum.getMethodName(), params);
            call = parseObject(result);
        } catch (Exception e) {
            XxlJobLogger.log("??????????????????????????????,????????????" + sbActionEnum.getMethodName() + ";" + e.getMessage());
        }
        //?????????????????????
        if (sbActionEnum.getMethodName().equals(FUND_TRANSFER.getMethodName())) {
            var code = call.getInteger(ERROR_CODE);
            var transferId = jsonObject.getString("vendor_trans_id").replace(config.getOperatorId(), "");
            if (code != null && code != 0) {
                coinPlatTransfersBase.updateOrderPlat(transferId, 2, jsonObject.getBigDecimal("amount"), null, call.getString("message"));
            } else {
                var orderPlat = call.getJSONObject("Data").getString("trans_id");
                coinPlatTransfersBase.updateOrderPlat(transferId, 1, jsonObject.getBigDecimal("amount"), orderPlat, null);
            }
        }
        return processException(call, sbActionEnum);
    }

    /**
     * ?????????JSON???????????????
     *
     * @param call         ????????????
     * @param sbActionEnum ??????????????????
     */
    private JSONObject processException(JSONObject call, SBPlatEnum.SBActionEnum sbActionEnum) {
        //send????????????????????????
        if (isNull(call) || Strings.isEmpty(toJSONString(call))) {
            PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0.setMsg(toJSONString(call));
            throw new BusinessException(PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0);
        }
        var code = call.getInteger(ERROR_CODE);
        //??????????????????????????????????????????
        if (code != null && 2 == code && LOGIN.equals(sbActionEnum)) {
            return call;
        }
        if (code != null && 0 == code) {
            log.info("????????????" + sbActionEnum.getMethodNameDesc() + "??????!");
        } else {
            log.error("????????????" + sbActionEnum.getMethodNameDesc() + "??????!");
            if (GET_BET.equals(sbActionEnum)) {
                PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0.setMsg(call.toJSONString());
                throw new BusinessException(PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0);
            }
            throw new BusinessException(IBCExceptionParam.getExceptionMessage(code, sbActionEnum.getMethodNameDesc()));
        }
        return call;
    }

    /**
     * @param dto dto.start ???????????? dto.end????????????
     */
    @Override
    public void genSupplementsOrders(GenSupplementsOrdersReqDto dto) {
        var jsonObject = new JSONObject();
        jsonObject.put("startTime", dto.getStart());
        jsonObject.put("endTime", dto.getEnd());
        commonPersistence.addBetSlipsSupplemental(BasePlatParam.GAME.IBC_SPORTS.getCode(),
                jsonObject.toJSONString(),
                0, "",
                DateNewUtils.utc8Zoned(dto.getStart()).toString(),
                DateNewUtils.utc8Zoned(dto.getEnd()).toString());
    }

    /**
     * ??????????????????
     * 1.???????????????????????????????????????????????????????????????ID,?????????????????????????????????ID
     * 2.???????????????????????????????????????ID?????????????????????????????????ID???
     * 3.????????????????????????????????????ID?????????????????????????????????
     *
     * @param dto dto.start ???????????? dto.end ????????????
     * @author: David
     * @date: 04/05/2020
     */
    @Override
    public void betsRecordsSupplemental(PlatFactoryParams.BetsRecordsSupplementReqDto dto) {
        var requestJson = parseObject(dto.getRequestInfo());
        var startTime = requestJson.getInteger("startTime");
        var endTime = requestJson.getInteger("endTime");
        //??????ID
        var startBets = betslipsShabaSportsServiceImpl.getOne(new LambdaQueryWrapper<BetslipsShabaSports>()
                        .le(BetslipsShabaSports::getCreatedAt, startTime)
                        .orderByDesc(BetslipsShabaSports::getVersionKey),
                false);
        //??????ID
        var endBets = betslipsShabaSportsServiceImpl.getOne(new LambdaQueryWrapper<BetslipsShabaSports>()
                        .ge(BetslipsShabaSports::getCreatedAt, endTime)
                        .orderByAsc(BetslipsShabaSports::getVersionKey),
                false);
        var startId = startBets == null ? Long.valueOf(config.getVersionKey()) : startBets.getVersionKey();
        // ????????????
        Long lastVersionKey;
        //????????????????????????
        var isContains = false;
        do {
            var call = pullData(startId);
            //??????????????????
            lastVersionKey = call.getJSONObject("Data").getLong("last_version_key");
            var jsonArray = call.getJSONObject("Data").getJSONArray(BET_DETAILS);
            if (Objects.nonNull(endBets) && !jsonArray.isEmpty()) {
                var count = jsonArray.stream().filter(x -> parseObject(toJSONString(x)).getLong("versionKey").equals(endBets.getVersionKey())).count();
                isContains = count > 0;
                saveDataBase(jsonArray);
            }
        } while (!startId.equals(lastVersionKey) || isContains);
    }

    /**
     * ???????????? ????????????
     *
     * @param dto dto.requestInfo ??????????????????
     * @author: David
     * @date: 04/05/2020
     */
    @Override
    public void betSlipsExceptionPull(BetsRecordsSupplementReqDto dto) {
        var bo = parseObject(dto.getRequestInfo(), BetRequestParams.class);
        var call = send(GET_BET, bo);
        var jsonArray = call.getJSONObject("Data").getJSONArray(BET_DETAILS);
        saveDataBase(jsonArray);
    }
}
