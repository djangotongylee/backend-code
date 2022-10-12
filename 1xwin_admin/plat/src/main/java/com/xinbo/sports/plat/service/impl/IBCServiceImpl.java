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
    /*错误码*/
    protected static final String ERROR_CODE = "error_code";
    /*拉单详细数据字段*/
    private static final String BET_DETAILS = "BetDetails";
    /*语言*/
    private static final Map<String, String> LANG_MAP = Map.of(
            "zh", "cs",
            "en", "en",
            "th", "th",
            "vi", "vn");
    /*平台配置参数*/
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
     * 获取游戏登录链接
     *
     * @param platLoginReqDto {username, lang, device, slotId}
     * @return 登录链接
     */
    @Override
    public PlatGameLoginResDto login(PlatLoginReqDto platLoginReqDto) {
        //登录用户
        var loginBo = LoginBo.builder()
                .vendorId(config.getVendorId())
                .vendorMemberId(config.getOperatorId() + platLoginReqDto.getUsername())
                .build();
        JSONObject loginCall = send(LOGIN, loginBo);
        var code = loginCall.getInteger(ERROR_CODE);
        //用户不存在，则创建用户
        if (code != null && 2 == code) {
            registerUser(PlatRegisterReqDto.builder().username(platLoginReqDto.getUsername()).build());
            loginCall = send(LOGIN, loginBo);
        }
        //设备判断
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
     * 注册
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
     * 登出游戏
     *
     * @param dto {"username"}
     * @return success:1-成功 0-失败
     */
    @Override
    public PlatLogoutResDto logout(PlatLogoutReqDto dto) {
        return null;
    }

    /**
     * 三方上分
     *
     * @param platCoinTransferUpReqDto {"coin","orderId","username"}
     * @return {platCoin}
     */
    @Override
    public PlatCoinTransferResDto coinUp(PlatCoinTransferReqDto platCoinTransferUpReqDto) {
        return transfer(platCoinTransferUpReqDto, 1);
    }

    /**
     * 三方下分
     *
     * @param platCoinTransferDownReqDto {"coin","orderId","username"}
     * @return {platCoin}
     */
    @Override
    public PlatCoinTransferResDto coinDown(PlatCoinTransferReqDto platCoinTransferDownReqDto) {
        return transfer(platCoinTransferDownReqDto, 0);
    }

    /**
     * 转账
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
     * 查询三方余额
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
     * 拉取三方注单信息
     */
    @Override
    public void pullBetsLips() {
        //获取拉单信息
        var versionKey = jedisUtil.hget(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH, RedisConstant.SHA_BA);
        BetslipsShabaSports betslipsShabaSports = null;
        if (Strings.isEmpty(versionKey)) {
            betslipsShabaSports = betslipsShabaSportsServiceImpl.lambdaQuery()
                    .orderByDesc(BetslipsShabaSports::getVersionKey)
                    .last("limit 1").one();
        }
        //第一次拉取投注versionKey重0开始
        var reqVersionKey = Long.parseLong(config.getVersionKey());
        if (betslipsShabaSports != null || Strings.isNotEmpty(versionKey)) {
            reqVersionKey = nonNull(betslipsShabaSports) ? betslipsShabaSports.getVersionKey() : 0;
            reqVersionKey = Strings.isNotEmpty(versionKey) ? Long.parseLong(versionKey) : reqVersionKey;
        }
        XxlJobLogger.log("拉单VersionKey=" + reqVersionKey);
        var call = pullData(reqVersionKey);
        var jsonArray = call.getJSONObject("Data").getJSONArray(BET_DETAILS);
        saveDataBase(jsonArray);
        //最后的版本号
        var lastVersionKey = call.getJSONObject("Data").getLong("last_version_key");
        XxlJobLogger.log("拉单lastVersionKey=" + lastVersionKey);
        jedisUtil.hset(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH, RedisConstant.SHA_BA, String.valueOf(lastVersionKey));
    }

    /**
     * 检查转账状态
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
                log.info("检查状态无法判定！");
                throw new BusinessException(CodeInfo.PLAT_SYSTEM_ERROR);
            }
        } catch (Exception e) {
            log.info("检查状态异常" + e.getMessage());
            throw new BusinessException(CodeInfo.PLAT_SYSTEM_ERROR);
        }
    }

    /**
     * 请求三方注单接口获取数据
     */
    private JSONObject pullData(Long reqVersionKey) {
        //请求的参数实体
        var betRequestParamsBo = BetRequestParams.builder()
                .vendorId(config.getVendorId())
                .versionKey(reqVersionKey)
                .build();
        var call = send(GET_BET, betRequestParamsBo);
        processException(call, GET_BET);
        return call;
    }

    /**
     * 入库处理
     */
    public void saveDataBase(JSONArray jsonArray) {
        var now = DateUtils.getCurrentTime();
        if (jsonArray != null && !jsonArray.isEmpty()) {
            //获取用户信息
            Map<String, Integer> userMap = userServiceBase.getUsernameIdMap();
            List<BetslipsShabaSports> betslipsShabaSportsList = new ArrayList<>();
            for (var x : jsonArray) {
                var betDetailBo = parseObject(toJSONString(x), ShaBaSportsParameter.BetDetail.class);
                //过滤不存在的用户名
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
     * 状态转换-》三方状态转换成xb平台状态码
     *
     * @param ticketStatus 状态值
     * @return 转换后的状态
     */
    public Integer transferStatus(String ticketStatus) {
        //状态码
        var status = BasePlatParam.BetRecordsStatus.WAIT_SETTLE.getCode();
        switch (ticketStatus.toLowerCase()) {
            //等待中
            case "waiting":
                //进行中
            case "running":
                status = BasePlatParam.BetRecordsStatus.WAIT_SETTLE.getCode();
                break;
            //作废
            case "void":
                //退款
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
     * 请求三方接口
     *
     * @param sbActionEnum 请求地址
     * @param object       请求体参数
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
            //异常处理
            ExceptionThreadLocal.setRequestParams(toJSONString(object));
            String result = PlatHttpBase.postHttp(config.getApiUrl() + sbActionEnum.getMethodName(), params);
            call = parseObject(result);
        } catch (Exception e) {
            XxlJobLogger.log("获取沙巴体育数据失败,执行方法" + sbActionEnum.getMethodName() + ";" + e.getMessage());
        }
        //上下分回滚处理
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
     * 返回值JSON异常码处理
     *
     * @param call         请求参数
     * @param sbActionEnum 方法名称描述
     */
    private JSONObject processException(JSONObject call, SBPlatEnum.SBActionEnum sbActionEnum) {
        //send请求的返回值判断
        if (isNull(call) || Strings.isEmpty(toJSONString(call))) {
            PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0.setMsg(toJSONString(call));
            throw new BusinessException(PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0);
        }
        var code = call.getInteger(ERROR_CODE);
        //登录时用户不存在，则创建用户
        if (code != null && 2 == code && LOGIN.equals(sbActionEnum)) {
            return call;
        }
        if (code != null && 0 == code) {
            log.info("沙巴会员" + sbActionEnum.getMethodNameDesc() + "成功!");
        } else {
            log.error("沙巴会员" + sbActionEnum.getMethodNameDesc() + "失败!");
            if (GET_BET.equals(sbActionEnum)) {
                PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0.setMsg(call.toJSONString());
                throw new BusinessException(PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0);
            }
            throw new BusinessException(IBCExceptionParam.getExceptionMessage(code, sbActionEnum.getMethodNameDesc()));
        }
        return call;
    }

    /**
     * @param dto dto.start 开始时间 dto.end结束时间
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
     * 补充注单信息
     * 1.根据开始时间获取开始的执行的历史补单的开始ID,即小于开始的时间的最大ID
     * 2.根据结束时间获取执行的结束ID，即大于结束时间的最小ID，
     * 3.判断历史拉单数据包括结束ID，则不进行下次历史补单
     *
     * @param dto dto.start 开始时间 dto.end 结束时间
     * @author: David
     * @date: 04/05/2020
     */
    @Override
    public void betsRecordsSupplemental(PlatFactoryParams.BetsRecordsSupplementReqDto dto) {
        var requestJson = parseObject(dto.getRequestInfo());
        var startTime = requestJson.getInteger("startTime");
        var endTime = requestJson.getInteger("endTime");
        //开始ID
        var startBets = betslipsShabaSportsServiceImpl.getOne(new LambdaQueryWrapper<BetslipsShabaSports>()
                        .le(BetslipsShabaSports::getCreatedAt, startTime)
                        .orderByDesc(BetslipsShabaSports::getVersionKey),
                false);
        //结束ID
        var endBets = betslipsShabaSportsServiceImpl.getOne(new LambdaQueryWrapper<BetslipsShabaSports>()
                        .ge(BetslipsShabaSports::getCreatedAt, endTime)
                        .orderByAsc(BetslipsShabaSports::getVersionKey),
                false);
        var startId = startBets == null ? Long.valueOf(config.getVersionKey()) : startBets.getVersionKey();
        // 初始位置
        Long lastVersionKey;
        //是否包含结束位置
        var isContains = false;
        do {
            var call = pullData(startId);
            //最后的版本号
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
     * 拉单异常 再次拉单
     *
     * @param dto dto.requestInfo 拉单请求参数
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
