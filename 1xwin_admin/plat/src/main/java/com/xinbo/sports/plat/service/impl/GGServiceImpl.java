package com.xinbo.sports.plat.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.xinbo.sports.dao.generator.po.BetSlipsSupplemental;
import com.xinbo.sports.dao.generator.po.BetslipsGg;
import com.xinbo.sports.dao.generator.po.CoinLog;
import com.xinbo.sports.dao.generator.po.CoinPlatTransfer;
import com.xinbo.sports.dao.generator.service.BetSlipsSupplementalService;
import com.xinbo.sports.dao.generator.service.BetslipsGgService;
import com.xinbo.sports.dao.generator.service.CoinLogService;
import com.xinbo.sports.dao.generator.service.impl.CoinPlatTransferServiceImpl;
import com.xinbo.sports.plat.base.PlatHttpBase;
import com.xinbo.sports.plat.base.StatusBase;
import com.xinbo.sports.plat.factory.PlatAbstractFactory;
import com.xinbo.sports.plat.io.bo.GGFishingParameter.*;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams;
import com.xinbo.sports.plat.io.enums.BasePlatParam;
import com.xinbo.sports.plat.io.enums.BasePlatParam.GAME;
import com.xinbo.sports.plat.io.enums.GGPlatEnum;
import com.xinbo.sports.service.base.CoinPlatTransfersBase;
import com.xinbo.sports.service.base.ExceptionThreadLocal;
import com.xinbo.sports.service.base.UserServiceBase;
import com.xinbo.sports.service.cache.KeyConstant;
import com.xinbo.sports.service.common.RedisConstant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.dto.BaseParams;
import com.xinbo.sports.service.io.enums.BaseEnum;
import com.xinbo.sports.service.thread.ThreadHeaderLocalData;
import com.xinbo.sports.utils.*;
import com.xinbo.sports.utils.components.response.CodeInfo;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.groovy.util.Maps;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.alibaba.fastjson.JSON.toJSONString;
import static com.xinbo.sports.plat.io.bo.PlatFactoryParams.*;
import static com.xinbo.sports.plat.io.enums.GGPlatEnum.GGMethodEnum.*;
import static com.xinbo.sports.utils.components.response.CodeInfo.*;
import static java.util.Objects.nonNull;

/**
 * @author: wells
 * @date: 2020/5/19
 * @description: GG??????
 */
@Slf4j
@Service("GGServiceImpl")
public class GGServiceImpl implements PlatAbstractFactory {
    /*????????????*/
    private static final String DES_PARAMS_KEY = "params";
    /*??????key*/
    private static final String PARAMS_MD5_KEY = "key";
    /*????????????*/
    private static final String DO_TRAN = "/api/doLink.do";
    /*??????*/
    private static final String DO_REPORT = "/api/doReport.do";
    private static final String D_BALANCE = "dbalance";
    /*??????*/
    private static final Map<String, String> LANG_MAP = Maps.of(
            "zh", "zh-CN",
            "en", "en-US",
            "th", "th-TH",
            "vi", "vi-VN");
    /*????????????*/
    @Setter
    private GGPlatConfig config;
    @Autowired
    private BetslipsGgService betslipsGgServiceImpl;
    @Autowired
    private JedisUtil jedisUtil;
    @Autowired
    private CoinPlatTransfersBase coinPlatTransfersBase;
    @Autowired
    private CoinPlatTransferServiceImpl coinPlatTransferServiceImpl;
    @Autowired
    private UserServiceBase userServiceBase;
    @Autowired
    private CoinLogService coinLogServiceImpl;
    @Autowired
    private BetSlipsSupplementalService betSlipsSupplementalServiceImpl;


    /**
     * ??????
     *
     * @param platLoginReqDto {"username":"","slotId","1"}
     * @return
     */
    @Override
    public PlatGameLoginResDto login(PlatLoginReqDto platLoginReqDto) {
        LoginBO loginBO = LoginBO.builder()
                .cagent(config.getCagent())
                .loginname(platLoginReqDto.getUsername())
                .password(CryptoUtils.md5(platLoginReqDto.getUsername()))
                .method(CA.getMethodName())
                .actype(Integer.valueOf(config.getAcType()))
                .cur(EnumUtils.getEnum(GGPlatEnum.CURRENCY.class, config.getCurrency()).getCode()).build();
        JSONObject call = send(config.getApiUrl() + DO_TRAN, loginBO);
        Integer code = call.getInteger("code");
        if (code != null && code.equals(0)) {
            log.info("GG??????????????????,  " + toJSONString(call));
            String address = forwardGame(config.getApiUrl() + DO_TRAN, platLoginReqDto);
            return PlatGameLoginResDto.builder().type(1).url(address).build();
        } else {
            log.error("GG??????????????????, error = " + toJSONString(call));
            throw new BusinessException(GGPlatEnum.CA_MAP.get(code));
        }
    }

    /**
     * ????????????
     *
     * @param platLoginReqDto
     * @return
     */
    @Override
    public Boolean registerUser(PlatRegisterReqDto platLoginReqDto) {
        var registerDto = PlatLoginReqDto.builder()
                .username(platLoginReqDto.getUsername())
                .lang(platLoginReqDto.getLang())
                .device(platLoginReqDto.getDevice())
                .build();
        login(registerDto);
        return true;
    }

    /**
     * ??????
     *
     * @param dto {"username":"","slotId","1"}
     * @return
     */
    @Override
    public PlatLogoutResDto logout(PlatLogoutReqDto dto) {
        LogoutBO logoutBO = LogoutBO.builder()
                .cagent(config.getCagent())
                .loginname(dto.getUsername())
                .password(CryptoUtils.md5(dto.getUsername()))
                .method(TY.getMethodName())
                .build();
        JSONObject call = send(config.getApiUrl() + DO_TRAN, logoutBO);
        Integer code = call.getInteger("code");
        if (code != null && code.equals(0)) {
            log.info("GG??????????????????, " + toJSONString(call));
            return PlatLogoutResDto.builder().success(1).build();
        } else {
            log.error("GG??????????????????, error = " + toJSONString(call));
            throw new BusinessException(GGPlatEnum.CA_MAP.getOrDefault(code, CodeInfo.PLAT_SYSTEM_ERROR));
        }
    }

    /**
     * ??????
     *
     * @param platCoinTransferUpReqDto
     * @return
     */
    @Override
    public PlatCoinTransferResDto coinUp(PlatCoinTransferReqDto platCoinTransferUpReqDto) {
        var billNo = String.valueOf(updateOrderId(platCoinTransferUpReqDto.getOrderId()));
        CoinUpBO coinUpBO = CoinUpBO.builder()
                .cagent(config.getCagent())
                .loginname(platCoinTransferUpReqDto.getUsername())
                .password(CryptoUtils.md5(platCoinTransferUpReqDto.getUsername()))
                .method(TC.getMethodName())
                .billno(billNo)
                .type("IN")
                .credit(platCoinTransferUpReqDto.getCoin())
                .cur(EnumUtils.getEnum(GGPlatEnum.CURRENCY.class, config.getCurrency()).getCode())
                .ip("")
                .build();
        JSONObject call = send(config.getApiUrl() + DO_TRAN, coinUpBO);
        Integer code = call.getInteger("code");
        if (code != null && code.equals(0)) {
            log.info("????????????" + toJSONString(call));
            coinPlatTransfersBase.updateOrderPlat(billNo, 1, platCoinTransferUpReqDto.getCoin(), null, null);
            return PlatCoinTransferResDto.builder().platCoin(call.getBigDecimal(D_BALANCE)).build();
        } else {
            coinPlatTransfersBase.updateOrderPlat(billNo, 2, platCoinTransferUpReqDto.getCoin(), null, call.getString("msg"));
            log.error("????????????, error = " + toJSONString(call));
            throw new BusinessException(GGPlatEnum.TC_MAP.getOrDefault(code, CodeInfo.PLAT_SYSTEM_ERROR));
        }
    }

    /**
     * ??????
     *
     * @param platCoinTransferDownReqDto
     * @return
     */
    @Override
    public PlatCoinTransferResDto coinDown(PlatCoinTransferReqDto platCoinTransferDownReqDto) {
        var billNo = String.valueOf(updateOrderId(platCoinTransferDownReqDto.getOrderId()));
        CoinUpBO coinUpBO = CoinUpBO.builder()
                .cagent(config.getCagent())
                .loginname(platCoinTransferDownReqDto.getUsername())
                .password(CryptoUtils.md5(platCoinTransferDownReqDto.getUsername()))
                .method(TC.getMethodName())
                .billno(billNo)
                .type("OUT")
                .credit(platCoinTransferDownReqDto.getCoin())
                .cur(config.getCurrency())
                .ip("")
                .build();
        JSONObject call = send(config.getApiUrl() + DO_TRAN, coinUpBO);
        Integer code = call.getInteger("code");
        if (code != null && code.equals(0)) {
            log.info("????????????" + toJSONString(call));
            coinPlatTransfersBase.updateOrderPlat(billNo, 1, platCoinTransferDownReqDto.getCoin(), null, null);
            return PlatCoinTransferResDto.builder().platCoin(call.getBigDecimal(D_BALANCE)).build();
        } else {
            log.error("????????????, error = " + toJSONString(call));
            coinPlatTransfersBase.updateOrderPlat(billNo, 2, platCoinTransferDownReqDto.getCoin(), null, call.getString("msg"));
            throw new BusinessException(GGPlatEnum.TC_MAP.getOrDefault(code, CodeInfo.PLAT_SYSTEM_ERROR));
        }
    }

    /**
     * ??????????????????
     *
     * @param platQueryBalanceReqDto
     * @return
     */
    @Override
    public PlatQueryBalanceResDto queryBalance(PlatQueryBalanceReqDto platQueryBalanceReqDto) {
        QueryBalanceBo queryBalanceBo = QueryBalanceBo.builder()
                .cagent(config.getCagent())
                .loginname(platQueryBalanceReqDto.getUsername())
                .password(CryptoUtils.md5(platQueryBalanceReqDto.getUsername()))
                .method(GB.getMethodName())
                .cur(config.getCurrency())
                .build();
        JSONObject call = send(config.getApiUrl() + DO_TRAN, queryBalanceBo);
        Integer code = call.getInteger("code");
        if (code != null && code.equals(0)) {
            log.info("??????????????????" + toJSONString(call));
            return PlatQueryBalanceResDto.builder().platCoin(call.getBigDecimal(D_BALANCE)).build();
        } else {
            log.error("??????????????????, error = " + toJSONString(call));
            throw new BusinessException(GGPlatEnum.GB_MAP.getOrDefault(code, CodeInfo.PLAT_SYSTEM_ERROR));
        }
    }

    /**
     * ????????????????????????????????????
     */
    @Override
    public void pullBetsLips() {
        int now = DateUtils.getCurrentTime();
        //??????redis????????????????????????
        String pullEndTime = jedisUtil.hget(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH, RedisConstant.GG);
        BetslipsGg betslipsGg = null;
        if (StringUtils.isEmpty(pullEndTime)) {
            betslipsGg = betslipsGgServiceImpl.getOne(new LambdaQueryWrapper<BetslipsGg>()
                    .orderByDesc(BetslipsGg::getCreatedAt).last("limit 1"));
        }
        //???1??????
        int hisEndDate = now - 24 * 3600;
        if (nonNull(betslipsGg) || StringUtils.isNotEmpty(pullEndTime)) {
            var betTime = nonNull(betslipsGg) ? betslipsGg.getCreatedAt() : hisEndDate;
            var startDate = StringUtils.isEmpty(pullEndTime) ? betTime
                    : (int) (DateUtils.yyyyMMddHHmmss(pullEndTime).getTime() / 1000L);
            if (startDate > hisEndDate) {
                //????????????-5????????????
                var pairList = splitDate(startDate, now, 5);
                //??????????????????
                pullDate(pairList, BR3.getMethodName());
                return;
            }
        }
        //??????????????????-5????????????
        var pairList = splitDate(hisEndDate, now, 5);
        pullDate(pairList, BR3.getMethodName());
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
            var queryOrderStatusBo = QueryOrderStatusBo.builder()
                    .cagent(config.getCagent())
                    .method(QX.getMethodName())
                    .billno(orderId)
                    .build();
            JSONObject call = send(config.getApiUrl() + DO_TRAN, queryOrderStatusBo);
            Integer code = call.getInteger("code");
            if (code != null && 0 == code) {
                log.info("????????????????????????" + toJSONString(call));
                return true;
            } else {
                log.error("??????????????????, error = " + toJSONString(call));
                return false;
            }
        } catch (Exception e) {
            throw new BusinessException(PLAT_SYSTEM_ERROR);
        }
    }

    /**
     * ??????????????????????????? ??????????????????
     *
     * @param dto dto.start ???????????? dto.end????????????
     * @param dto dto.start ???????????? dto.end ????????????
     *            ?????????????????????30??????????????????????????????????????????
     *            1.?????????????????????????????????
     *            a.?????????????????????????????????????????????????????????????????????
     *            ?????????????????????3???30???????????????????????????????????????3?????????
     *            2.??????????????????
     */
    @SneakyThrows
    @Override
    public void genSupplementsOrders(GenSupplementsOrdersReqDto dto) {
        List<BetSlipsSupplemental> betSlipsSupplementalList = new ArrayList<>();
        BiConsumer<List<Pair<String, String>>, String> supplementFunction = (pairList, method) -> {
            var now = DateNewUtils.now();
            pairList.forEach(pair -> {
                var reqBo = reqFunction.apply(pair, BR3.getMethodName());
                var startTime = DateNewUtils.utc8Zoned((int) (DateUtils.yyyyMMddHHmmss(pair.getLeft()).getTime() / 1000L));
                var endTime = DateNewUtils.utc8Zoned((int) (DateUtils.yyyyMMddHHmmss(pair.getRight()).getTime() / 1000L));
                var po = new BetSlipsSupplemental();
                po.setGameListId(GAME.GG_FINISH.getCode());
                po.setRequest(toJSONString(reqBo));
                po.setTimeStart(startTime.toString());
                po.setTimeEnd(endTime.toString());
                po.setCreatedAt(now);
                po.setUpdatedAt(now);
                betSlipsSupplementalList.add(po);
            });
        };
        var now = (int) Instant.now().getEpochSecond();
        //?????????
        var beforeThreeDay = now - 3 * 24 * 3600;
        //???30???
        var beforeThirtyDay = now - 30 * 24 * 3600;
        //??????????????????????????????
        if (dto.getStart() >= beforeThreeDay) {
            var pairList = splitDate(dto.getStart(), dto.getEnd(), 10);
            supplementFunction.accept(pairList, BR3.getMethodName());
        } else if (dto.getEnd() <= beforeThreeDay) {
            //???????????????????????????????????????????????????
            var startTime = dto.getStart() > beforeThirtyDay ? beforeThirtyDay : dto.getStart();
            var hisPairList = splitDate(startTime, beforeThreeDay, 15);
            supplementFunction.accept(hisPairList, HBR3.getMethodName());
        } else {
            //???????????????????????????????????????????????????
            var brPairList = splitDate(dto.getStart(), beforeThreeDay, 10);
            var br3PairList = splitDate(beforeThreeDay, dto.getEnd(), 15);
            supplementFunction.accept(brPairList, BR3.getMethodName());
            supplementFunction.accept(br3PairList, HBR3.getMethodName());
        }
        betSlipsSupplementalServiceImpl.saveBatch(betSlipsSupplementalList);
    }


    /**
     * ????????????????????????
     */
    Consumer<String> consumer = requestInfo -> {
        var betListByDateBo = parseObject(requestInfo, BetListByDateBo.class);
        var call = send(config.getReportUrl() + DO_REPORT, betListByDateBo);
        insertGGDate(call);
    };

    /**
     * ??????????????????
     *
     * @author: David
     * @date: 04/05/2020
     */
    @Override
    public void betsRecordsSupplemental(PlatFactoryParams.BetsRecordsSupplementReqDto dto) {
        consumer.accept(dto.getRequestInfo());
    }

    /**
     * @param dto dto.requestInfo ??????????????????
     */
    @Override
    public void betSlipsExceptionPull(BetsRecordsSupplementReqDto dto) {
        consumer.accept(dto.getRequestInfo());
    }

    /**
     * ????????????????????????
     */
    BiFunction<Pair<String, String>, String, BetListByDateBo> reqFunction = (pair, method) -> {
        String cagent = config.getCagent();
        var startTime = pair.getRight();
        if (BR3.getMethodName().equals(method)) {
            /*??????5??????,??????????????????5??????*/
            startTime = DateUtils.yyyyMMddHHmmss(DateUtils.yyyyMMddHHmmss(pair.getLeft()).getTime() - 5 * 60 * 1000L);
        }
        return BetListByDateBo.builder()
                .cagent(cagent)
                .method(method)
                .startdate(startTime)
                .enddate(pair.getLeft())
                .build();
    };

    /**
     * ??????????????????
     *
     * @param pairList
     * @param method
     */
    public void pullDate(List<Pair<String, String>> pairList, String method) {
        pairList.forEach(pair -> {
            //??????????????????
            var betListByDateBo = reqFunction.apply(pair, method);
            JSONObject call = send(config.getReportUrl() + DO_REPORT, betListByDateBo);
            insertGGDate(call);
            if (Strings.isNotEmpty(method) && BR3.getMethodName().equals(method)) {
                jedisUtil.hset(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH, RedisConstant.GG, pair.getRight());
            }
        });
    }

    /**
     * ????????????-10????????????
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public List<Pair<String, String>> splitDate(Integer startTime, Integer endTime, int interval) {
        var pairList = new ArrayList<Pair<String, String>>();
        int intervalDate = interval * 60;
        int difference = endTime - startTime;
        if (difference < intervalDate) {
            pairList.add(Pair.of(DateUtils.yyyyMMddHHmmss(startTime), DateUtils.yyyyMMddHHmmss(endTime)));
            return pairList;
        }
        for (int i = 1; ; i++) {
            int startDateTemp = startTime + (i - 1) * intervalDate;
            if (difference > i * intervalDate) {
                Integer endDateTemp = startTime + i * intervalDate;
                pairList.add(Pair.of(DateUtils.yyyyMMddHHmmss(startDateTemp), DateUtils.yyyyMMddHHmmss(endDateTemp)));
            } else {
                pairList.add(Pair.of(DateUtils.yyyyMMddHHmmss(startDateTemp), DateUtils.yyyyMMddHHmmss(endTime)));
                break;
            }
        }
        return pairList;
    }

    /**
     * ??????????????????
     *
     * @param call ???????????????
     */
    public void insertGGDate(JSONObject call) {
        Integer code = call.getInteger("code");
        if (code != null && code == 0) {
            XxlJobLogger.log("GG??????????????????" + toJSONString(call));
            //???????????????
            JSONArray jsonArray = call.getJSONArray("recordlist3");
            if (jsonArray == null || jsonArray.isEmpty()) {
                return;
            }
            String cagent = config.getCagent();
            //??????????????????
            Map<String, Integer> userMap = userServiceBase.getUsernameIdMap();
            List<BetslipsGg> betslipsGgList = new ArrayList<>();
            for (Object x : jsonArray) {
                JSONObject jsonObject = (JSONObject) x;
                //???????????????????????????
                var accountNo = jsonObject.getString("accountno");
                var userName = userServiceBase.filterUsername(accountNo.replace(cagent, ""));
                if (StringUtils.isEmpty(userName)) {
                    continue;
                }
                var betslipsGgBo = parseObject(jsonObject.toJSONString(), BetslipsGgBo.class);
                var betslipsGg = BeanConvertUtils.copyProperties(betslipsGgBo, BetslipsGg::new, (bo, gg) -> {
                    gg.setId(gg.getBetId());
                    var uid = userMap.get(userName);
                    gg.setXbUsername(userName);
                    gg.setXbUid(uid == null ? 0 : uid);
                    gg.setXbCoin(bo.getBet());
                    gg.setXbValidCoin(bo.getBet());
                    gg.setXbProfit(bo.getProfit());
                    gg.setCreatedAt((int) (bo.getBettimeStr().getTime() / 1000L));
                    //???????????????
                    gg.setXbStatus(transferStatus(bo));
                });
                betslipsGgList.add(betslipsGg);

                betslipsGgServiceImpl.saveOrUpdateBatch(betslipsGgList, 1000);
            }
        } else {
            XxlJobLogger.log("GG??????????????????!" + call.toJSONString());
            PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0.setMsg(call.toJSONString());
            throw new BusinessException(PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0);
        }
    }

    /**
     * ????????????-????????????????????????xb???????????????
     *
     * @param bo ????????????????????????
     * @return
     */
    public Integer transferStatus(BetslipsGgBo bo) {
        //????????? 1-????????? 0-?????????
        var closed = bo.getClosed();
        //????????????
        var profit = bo.getProfit();
        switch (closed) {
            case 0:
                return BasePlatParam.BetRecordsStatus.WAIT_SETTLE.getCode();
            case 1:
                return StatusBase.checkStatus(profit);
            default:
                break;
        }
        return BasePlatParam.BetRecordsStatus.WAIT_SETTLE.getCode();
    }

    /**
     * ??????????????????
     *
     * @param url    ????????????
     * @param object ???????????????
     * @return
     */
    public JSONObject send(String url, Object object) {
        JSONObject jsonObject = parseObject(toJSONString(object));
        List<String> listParams = Lists.newArrayList();
        jsonObject.forEach((key, value) -> {
            String param = key + "=" + value;
            listParams.add(param);
        });
        String join = Joiner.on("/\\\\/").skipNulls().join(listParams);
        log.info("???????????????" + join);
        String decKey = config.getDesKey();
        String md5Key = config.getMd5Key();
        DESCUtils descUtils = new DESCUtils(decKey);
        String desParams = null;
        try {
            desParams = descUtils.encrypt(join);
        } catch (Exception e) {
            log.info("???????????????");
        }
        String paramMd5 = desParams + md5Key;
        String key = CryptoUtils.md5(paramMd5);
        var params = DES_PARAMS_KEY + "=" + desParams + "&" + PARAMS_MD5_KEY + "=" + key;
        log.info(params);
        //????????????
        ExceptionThreadLocal.setRequestParams(toJSONString(object));
        String result = PlatHttpBase.ggHttpGet(url, params, config.getCagent());
        log.info("??????????????????" + result);
        return StringUtils.isEmpty(result) ? new JSONObject() : parseObject(result);
    }


    /**
     * ??????????????????
     *
     * @return
     */
    public String forwardGame(String reqUrl, PlatLoginReqDto platLoginReqDtos) {
        String snowId = String.valueOf(IdUtils.getSnowFlakeId());
        String sid = config.getCagent() + snowId.substring(0, snowId.length() - 2);
        ForwardGameBo forwardGameBo = ForwardGameBo.builder()
                .cagent(config.getCagent())
                .loginname(platLoginReqDtos.getUsername())
                .password(CryptoUtils.md5(platLoginReqDtos.getUsername()))
                .method(FW.getMethodName())
                .sid(sid)
                .lang(LANG_MAP.get(platLoginReqDtos.getLang()))
                //??????:m-H5, d-PC, android-??????, ios-??????
                .isapp(platLoginReqDtos.getDevice().equalsIgnoreCase(BaseEnum.DEVICE.D.getValue()) ? 0 : 1)
                .gametype(0)
                .ip("")
                .sessionId(IdUtils.getSerialId())
                .ishttps(1)
                .returnUrl(config.getReturnUrl())
                .iframe(0).build();
        JSONObject call = send(reqUrl, forwardGameBo);
        Integer code = call.getInteger("code");
        if (code != null && code.equals(0)) {
            log.info("GG????????????????????????, error = " + toJSONString(call));
            return call.getString("url");
        } else {
            log.error("GG????????????????????????, error = " + toJSONString(call));
            throw new BusinessException(FORWARD_GAME_IS_ERROR);
        }
    }

    /**
     * ???????????????
     *
     * @param orderId
     * @return
     */
    public Long updateOrderId(String orderId) {
        if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(orderId) && orderId.length() > 16) {
            var newOrderId = orderId.substring(0, 16);
            Long id = Long.valueOf(newOrderId);
            coinPlatTransferServiceImpl.lambdaUpdate().set(CoinPlatTransfer::getId, newOrderId).eq(CoinPlatTransfer::getId, orderId).update();
            coinLogServiceImpl.lambdaUpdate().set(CoinLog::getReferId, id).eq(CoinLog::getReferId, orderId)
                    .in(CoinLog::getCategory, 3, 4)
                    .eq(CoinLog::getStatus, 0)
                    .update();
            return id;
        }
        return Long.valueOf(orderId);
    }


}
