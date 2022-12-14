package com.xinbo.sports.plat.service.impl.sbo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xinbo.sports.dao.generator.po.BetSlipsSupplemental;
import com.xinbo.sports.dao.generator.po.BetslipsSboSports;
import com.xinbo.sports.plat.base.CommonPersistence;
import com.xinbo.sports.plat.factory.PlatAbstractFactory;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams.PlatGameLoginResDto;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams.PlatLoginReqDto;
import com.xinbo.sports.plat.io.bo.SBOSportsRequestParameter;
import com.xinbo.sports.plat.io.enums.BasePlatParam;
import com.xinbo.sports.plat.io.enums.SBORoutineParam;
import com.xinbo.sports.plat.io.enums.SBOUrlEnum;
import com.xinbo.sports.plat.service.impl.SBOServiceImpl;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.HttpUtils;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author: David
 * @date: 04/05/2020
 * @description:
 */
@Service
@Slf4j
public class SBOSportsServiceImpl extends SBOServiceImpl {
    private static final ThreadPoolExecutor THREAD_POOL = new ThreadPoolExecutor(
            2,
            2,
            1L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>());
    @Resource
    private CommonPersistence commonPersistence;

    @Override
    public PlatGameLoginResDto login(PlatLoginReqDto platLoginReqDto) {
        SBOSportsRequestParameter.Login build = SBOSportsRequestParameter.Login.builder()
                .username(platLoginReqDto.getUsername())
                .portfolio(this.portfolio)
                .isWapSports(false)
                .build();

        try {
            JSONObject reqParams = buildCommonReqParams(build);
            JSONObject result = this.send(SBOUrlEnum.LOGIN, reqParams.toJSONString());

            String sLink = SBORoutineParam.GUIDE.SportsBook.getAddress();
            String replace = StringUtils.replace(sLink, "//{response-url}", result.getString("url"))
                    .replace("{lang}", switchLanguage(platLoginReqDto.getLang()))
                    // ????????????: MY(Malay) HK(HongKong) EU(Euro) ID(Indonesia)
                    .replace("{oddstyle}", "HK")
                    // 1:Black????????????, 2:Blue????????????, 3:Emerald????????????, 4:Green????????????, 5:Ocean????????????, 6:SBO??????, 7:Lawn??????, 8:SBOBET-m??????, 9:Euro-layout-m????????????, 10:China-layout-m????????????
                    .replace("{theme}", "1")
                    // double (default) / single
                    .replace("{oddsmode}", "double")
                    .replace("{device}", platLoginReqDto.getDevice());
            return PlatGameLoginResDto.builder().type(1).url(replace).build();
        } catch (BusinessException e) {
            log.warn(e.toString());
            if (e.getCode().equals(CodeInfo.PLAT_ACCOUNT_NOT_EXISTS.getCode())) {
                boolean resRegister = this.registerUser(PlatFactoryParams.PlatRegisterReqDto.builder().username(platLoginReqDto.getUsername()).build());
                if (resRegister) {
                    return this.login(platLoginReqDto);
                }
            }
        }
        return null;
    }


    /**
     * SBO??????????????????????????????????????????
     *
     * @param strStatus ?????????
     * @author David
     */
    public Integer switchBetRecordStatus(@NotNull String strStatus) {
        // ??????: 1-??? 2-??? 3-??? 4-?????? 5-????????? 6-????????????
        int i = BasePlatParam.BetRecordsStatus.WAIT_SETTLE.getCode();
        switch (strStatus.toLowerCase()) {
            //Bet is won and payout will be credited into your account
            case "won":
                i = BasePlatParam.BetRecordsStatus.WIN.getCode();
                break;

            /* Bet is lost and the loss amount is deducted from your account */
            case "lose":
                i = BasePlatParam.BetRecordsStatus.LOSE.getCode();
                break;

            /* Refunded bets are those which in case to case basis are returned. */
            case "refund":
                /* Void bets can occur in a number of situations, where bets will not count, based on the applicable rule. When your bet is declared void, your original stake will be returned. */
            case "void":
                /* Player place bet on a live odds and trader reject the bet due to many possible reasons, e.g. ball around the gate, corner is given, free kick...etc.s */
            case "waiting rejected":
                /* When your sub bets of MixParlay being void due the match being suspended, it will show 'VOID(SUSPENDED MATCH)' */
            case "void(supended match)":
                i = BasePlatParam.BetRecordsStatus.CANCEL.getCode();
                break;

            /* Event ends in a draw result. */
            case "draw":
                i = BasePlatParam.BetRecordsStatus.DRAW.getCode();
                break;
            /* Live bet has been accepted and winner will be determined at the conclusion of the event. */
            case "running":
                /* When your sub bets of MixParlay being refund, it will show 'Done'. */
            case "done":
                i = BasePlatParam.BetRecordsStatus.WAIT_SETTLE.getCode();
                break;

            /* Live bet is being processed but has yet to be accepted. */
            case "waiting":
                i = BasePlatParam.BetRecordsStatus.WAIT_CONFIRM.getCode();
                break;
            default:
                log.info("sbo????????????????????????;???????????????" + strStatus);
                break;
        }

        return i;
    }

    /**
     * ????????????????????????
     *
     * @author: David
     * @date: 04/05/2020
     */
    @Override
    public void pullBetsLips() {
        List<String> portfolioList = List.of(SBORoutineParam.Portfolio.SportsBook.name(), SBORoutineParam.Portfolio.VirtualSports.name());
        for (String portfolio : portfolioList) {
            String redisKey = MODEL + "_" + portfolio;
            String lastTime = configCache.getLastUpdateTimeByModel(redisKey);
            long lastUpdateTime = StringUtils.isBlank(lastTime) ? 0L : Long.parseLong(lastTime);
            THREAD_POOL.submit(() -> process(portfolio, lastUpdateTime, redisKey));
        }
    }

    /**
     * ??????????????????????????? ??????????????????
     * <p>
     * SBO:
     * 1. ??????????????? ???????????????????????????  			????????????60???
     * 2. ???????????? ????????????(ID)  ??????-????????????		????????????30??????
     * 3. ????????????  ????????????						????????????????????????????????????????????????????????????????????????
     * 4. ?????????????????????(???)						???
     *
     * @param dto dto.start ???????????? dto.end????????????
     */
    @Override
    public void genSupplementsOrders(PlatFactoryParams.GenSupplementsOrdersReqDto dto) {
        // 1. ??????????????? ???????????????????????????  			????????????60???
        long preDays = 60L;
        // 2. ???????????? ????????????(ID)  ??????-????????????		????????????30??????
        long startEndRange = 30;

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

            // ??????:????????????
            JSONObject sportsBook = buildPullBetReqParamsWithZoneSameInstant(SBORoutineParam.Portfolio.SportsBook.name(), start, temp);
            Integer currentTime = (int) now.toInstant().getEpochSecond();
            BetSlipsSupplemental po = PlatAbstractFactory
                    .buildBetSlipsSupplemental(dto.getGameId(), start.toString(), temp.toString(), sportsBook.toJSONString(), currentTime);
            list.add(po);

            // ??????:????????????
            JSONObject virtualSports = buildPullBetReqParamsWithZoneSameInstant(SBORoutineParam.Portfolio.VirtualSports.name(), start, temp);
            po = PlatAbstractFactory
                    .buildBetSlipsSupplemental(dto.getGameId(), start.toString(), temp.toString(), virtualSports.toJSONString(), currentTime);
            list.add(po);


            start = start.plusMinutes(startEndRange);
        } while (temp.compareTo(end) <= 0);

        if (!list.isEmpty()) {
            commonPersistence.addBatchBetSlipsSupplementalList(list);
        }
    }

    /**
     * ??????????????????
     *
     * @param dto dto.requestInfo ??????????????????
     * @author: David
     * @date: 04/05/2020
     */
    @Override
    public void betsRecordsSupplemental(PlatFactoryParams.BetsRecordsSupplementReqDto dto) {
        try {
            JSONObject result = pullSend(SBOUrlEnum.GET_BET_LIST_BY_MODIFY_DATE, dto.getRequestInfo());
            saveOrUpdateBatch(result);
        } catch (BusinessException e) {
            // ??????:0-????????????
            log.error(MODEL + CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0.getMsg() + ":" + e);
            throw e;
        } catch (Exception e) {
            // ??????:1-????????????
            log.error(MODEL + CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_1.getMsg() + ":" + e);
            throw e;
        }
    }

    /**
     * xbSportsCategory ????????????:1-???????????? 2-????????????
     *
     * @param portfolio SportsBook-???????????? VirtualSports-????????????
     * @return ????????????:1-???????????? 2-????????????
     */
    private int switchXbSportsCategory(String portfolio) {
        int xbSportsCategory = 0;
        switch (portfolio) {
            case "SportsBook":
                xbSportsCategory = 1;
                break;
            case "VirtualSports":
                xbSportsCategory = 2;
                break;
            default:
                break;
        }
        return xbSportsCategory;
    }

    /**
     * ????????????
     *
     * @param portfolio      SportsBook-???????????? VirtualSports-????????????
     * @param lastUpdateTime ??????????????????
     * @param redisKey       ?????????????????????KEY
     */
    private void process(String portfolio, long lastUpdateTime, String redisKey) {
        String requestParams = "";
        // ??????????????????
        long tmpLastUpdateTime = lastUpdateTime;
        try {
            int xbSportsCategory = switchXbSportsCategory(portfolio);
            ZonedDateTime currentTime = LocalDateTime.now()
                    .atZone(DateNewUtils.getZoneId("UTC", "+8"))
                    .withZoneSameInstant(DateNewUtils.getZoneId("GMT", "-4"));
            ZonedDateTime startZonedDateTime = buildStartTime(tmpLastUpdateTime, xbSportsCategory, currentTime);
            // +30??????
            ZonedDateTime endZonedDateTime = startZonedDateTime.plusMinutes(30);
            if (endZonedDateTime.toEpochSecond() > currentTime.toEpochSecond()) {
                endZonedDateTime = currentTime;
            }
            // redis????????????????????????
            tmpLastUpdateTime = endZonedDateTime.toEpochSecond();
            // -????????????
            startZonedDateTime = startZonedDateTime.minusMinutes(BasePlatParam.BasePlatEnum.SBO.getMinutes());
            endZonedDateTime = endZonedDateTime.minusMinutes(BasePlatParam.BasePlatEnum.SBO.getMinutes());

            JSONObject reqParams = buildReqParams(portfolio, startZonedDateTime, endZonedDateTime);
            requestParams = reqParams.toJSONString();
            JSONObject result = pullSend(SBOUrlEnum.GET_BET_LIST_BY_MODIFY_DATE, reqParams.toJSONString());
            saveOrUpdateBatch(result);

            // ??????????????????
            configCache.reSetLastUpdateTimeByModel(redisKey, String.valueOf(tmpLastUpdateTime));
        } catch (BusinessException e) {
            // ??????:0-????????????
            if (e.getCode().equals(CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0.getCode())) {
                addBetSlipsException(0, requestParams, e.getMessage());
            }
        } catch (Exception e) {
            // ??????:1-????????????
            log.error(MODEL + CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_1.getMsg() + ":" + e);
            addBetSlipsException(1, requestParams, e.toString());
        }
    }

    /**
     * build????????????
     *
     * @param startZonedDateTime ????????????
     * @param endZonedDateTime   ????????????
     * @return JSONObject
     */
    private JSONObject buildReqParams(String portfolio, ZonedDateTime startZonedDateTime, ZonedDateTime endZonedDateTime) {
        String startTime = startZonedDateTime.format(DateNewUtils.getDateTimeFormatter(DateNewUtils.Format.yyyy_MM_dd_HH_mm_ss_SSS));
        String endTime = endZonedDateTime.format(DateNewUtils.getDateTimeFormatter(DateNewUtils.Format.yyyy_MM_dd_HH_mm_ss_SSS));
        log.info("????????????[" + startTime + " - " + endTime + "]");
        // ??????????????????
        SBOSportsRequestParameter.GetBetListByModifyDate m = SBOSportsRequestParameter.GetBetListByModifyDate.builder()
                .portfolio(portfolio)
                .startDate(startTime)
                .endDate(endTime)
                .build();
        return buildCommonReqParams(m);
    }

    /**
     * build????????????
     *
     * @param lastUpdateTime   ??????????????????
     * @param xbSportsCategory ????????????:1-???????????? 2-????????????
     * @param currentTime      ??????????????????
     * @return ZonedDateTime
     */
    private ZonedDateTime buildStartTime(long lastUpdateTime, int xbSportsCategory, ZonedDateTime currentTime) {
        ZonedDateTime startZonedDateTime;
        // redis?????????
        if (lastUpdateTime == 0) {
            BetslipsSboSports betslipsSboSports = getBetsLipsSboSports(xbSportsCategory);
            // ??????????????????
            if (null == betslipsSboSports) {
                // -1???
                startZonedDateTime = currentTime.minusDays(1);
            } else {
                startZonedDateTime = betslipsSboSports.getModifyDate().toInstant()
                        .atZone(DateNewUtils.getZoneId("UTC", "+8"))
                        .withZoneSameInstant(DateNewUtils.getZoneId("GMT", "-4"));
            }
        } else {
            startZonedDateTime = Instant.ofEpochSecond(lastUpdateTime)
                    .atZone(DateNewUtils.getZoneId("UTC", "+8"))
                    .withZoneSameInstant(DateNewUtils.getZoneId("GMT", "-4"));
        }
        return startZonedDateTime;
    }

    /**
     * ????????????????????????
     *
     * @param xbSportsCategory ????????????:1-???????????? 2-????????????
     * @return ??????????????????
     */
    private BetslipsSboSports getBetsLipsSboSports(Integer xbSportsCategory) {
        return betslipsSboSportsServiceImpl.lambdaQuery()
                .eq((null != xbSportsCategory), BetslipsSboSports::getXbSportsCategory, xbSportsCategory)
                .orderByDesc(BetslipsSboSports::getModifyDate)
                .last("limit 1")
                .one();
    }

    /**
     * ?????????????????????
     *
     * @param category      ??????:0-???????????? 1-????????????
     * @param requestParams ????????????
     * @param exceptionInfo ????????????:??????-???????????? ??????-????????????
     */
    private void addBetSlipsException(Integer category, String requestParams, String exceptionInfo) {
        commonPersistence.addBetSlipsException(BasePlatParam.GAME.SBO_SPORTS.getCode(), requestParams, category, exceptionInfo, 0);
    }

    /**
     * ????????????????????????
     *
     * @param msg MSG
     */
    private void buildCodeInfo(String msg) {
        CodeInfo codeInfo = CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0;
        codeInfo.setMsg(msg);
        throw new BusinessException(codeInfo);
    }

    /**
     * ??????send
     *
     * @param thirdSBOSEnum URL
     * @param reqParams     reqParams
     * @return JSONObject
     */
    private JSONObject pullSend(SBOUrlEnum thirdSBOSEnum, String reqParams) {
        /* ??????model???redis???????????????CompanyKey???ServerId */
        String url = thirdSBOSEnum.getUrl();
        String apiAddress = config.getApiUrl() + url;
        String sResult = "";
        try {
            sResult = HttpUtils.doPost(apiAddress, reqParams, MODEL);
        } catch (Exception e) {
            log.error(MODEL + CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0.getMsg() + ":" + e.toString());
            buildCodeInfo(e.toString());
        }
        JSONObject jResult = JSON.parseObject(sResult);
        JSONObject error = jResult.getJSONObject("error");
        Integer code = error.getInteger("id");
        if (code == null || code != 0) {
            buildCodeInfo(jResult.toJSONString());
        }
        return jResult;
    }

    /**
     * ????????????GMT-4
     *
     * @param portfolio          SportsBook-???????????? VirtualSports-????????????
     * @param startZonedDateTime ????????????
     * @param endZonedDateTime   ????????????
     * @return ???????????????
     */
    private JSONObject buildPullBetReqParamsWithZoneSameInstant(String portfolio, ZonedDateTime startZonedDateTime, ZonedDateTime endZonedDateTime) {
        startZonedDateTime = startZonedDateTime.withZoneSameInstant(DateNewUtils.getZoneId("GMT", "-4"));
        endZonedDateTime = endZonedDateTime.withZoneSameInstant(DateNewUtils.getZoneId("GMT", "-4"));
        return buildReqParams(portfolio, startZonedDateTime, endZonedDateTime);
    }

    /**
     * ?????????????????????????????????
     *
     * @param responseBody responseBody
     */
    private void saveOrUpdateBatch(JSONObject responseBody) {
        JSONArray data = responseBody.getJSONArray("result");
        if (Optional.ofNullable(data).isEmpty() || data.isEmpty()) {
            return;
        }
        // ????????? id - username ?????????
        Map<String, Integer> usernameIdMap = userServiceBase.getUsernameIdMap();
        int time = (int) Instant.now().getEpochSecond();
        List<BetslipsSboSports> dataList = data.stream().map(o -> {
            JSONObject ori = (JSONObject) o;
            BetslipsSboSports dest = JSON.toJavaObject(ori, BetslipsSboSports.class);

            String xbUsername = userServiceBase.filterUsername(dest.getUsername());
            Integer uid = usernameIdMap.get(xbUsername);
            dest.setId(ori.getString("refNo"));
            dest.setXbUid(uid);
            dest.setXbUsername(xbUsername);

            // ???????????? ??????????????? GMT-4 -> UTC+8 ???createdAt
            SimpleDateFormat sdf = new SimpleDateFormat(DateNewUtils.Format.yyyy_MM_dd_HH_mm_ss_SSS.getValue());
            String format = sdf.format(dest.getOrderTime());

            ZonedDateTime gmt = DateNewUtils.getZonedDateTime(
                    format,
                    DateNewUtils.Format.yyyy_MM_dd_HH_mm_ss_SSS,
                    DateNewUtils.getZoneId("GMT", "-4")
            );
            long createAt = gmt.withZoneSameInstant(DateNewUtils.getZoneId("UTC", "+8")).toInstant().getEpochSecond();
            var xbStatus = switchBetRecordStatus(dest.getStatus());
            var validCoin = BasePlatParam.SPORTS_STATUS_LIST.contains(xbStatus) ? dest.getTurnover() : BigDecimal.ZERO;
            dest.setXbCoin(dest.getActualStake());
            dest.setXbValidCoin(validCoin);
            dest.setXbProfit(dest.getWinLost());
            dest.setXbStatus(xbStatus);
            dest.setCreatedAt((int) createAt);
            dest.setUpdatedAt(time);
            // ????????????:1-???????????? 2-????????????;      gameId???productType???????????????????????????
            int xbSportsCategory = (StringUtils.isNotBlank(dest.getProductType()) || null != dest.getGameId()) ? 2 : 1;
            dest.setXbSportsCategory(xbSportsCategory);
            return dest;
        }).filter(o -> o.getXbUid() != null).collect(Collectors.toList());

        betslipsSboSportsServiceImpl.saveOrUpdateBatch(dataList,dataList.size());
    }

    /**
     * ???????????? ????????????
     *
     * @param dto dto.requestInfo ??????????????????
     */
    @Override
    public void betSlipsExceptionPull(@javax.validation.constraints.NotNull PlatFactoryParams.BetsRecordsSupplementReqDto dto) {
        this.betsRecordsSupplemental(dto);
    }

}
