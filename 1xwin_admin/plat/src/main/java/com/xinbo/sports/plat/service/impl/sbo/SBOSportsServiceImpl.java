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
                    // 盘口类型: MY(Malay) HK(HongKong) EU(Euro) ID(Indonesia)
                    .replace("{oddstyle}", "HK")
                    // 1:Black黑色主题, 2:Blue蓝色主题, 3:Emerald翡翠主题, 4:Green绿色主题, 5:Ocean海洋主题, 6:SBO主题, 7:Lawn主题, 8:SBOBET-m主題, 9:Euro-layout-m歐洲主題, 10:China-layout-m中國主題
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
     * SBO注单信息映射到正确的返回状态
     *
     * @param strStatus 状态值
     * @author David
     */
    public Integer switchBetRecordStatus(@NotNull String strStatus) {
        // 状态: 1-赢 2-输 3-平 4-撤单 5-未结算 6-等待确认
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
                log.info("sbo体育状态转换异常;状态字段为" + strStatus);
                break;
        }

        return i;
    }

    /**
     * 拉取三方注单信息
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
     * 根据起始、结束时间 生成补单信息
     * <p>
     * SBO:
     * 1. 补单可拉取 最长多少天前的数据  			小于等于60天
     * 2. 每次补单 时间跨度(ID)  开始-结束时间		小于等于30分钟
     * 3. 补单接口  间隔时间						並無間隔的限制，但我方建議一分鐘呼叫三到五次即可
     * 4. 补单最大数据量(条)						无
     *
     * @param dto dto.start 开始时间 dto.end结束时间
     */
    @Override
    public void genSupplementsOrders(PlatFactoryParams.GenSupplementsOrdersReqDto dto) {
        // 1. 补单可拉取 最长多少天前的数据  			小于等于60天
        long preDays = 60L;
        // 2. 每次补单 时间跨度(ID)  开始-结束时间		小于等于30分钟
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

            // 补单:真实体育
            JSONObject sportsBook = buildPullBetReqParamsWithZoneSameInstant(SBORoutineParam.Portfolio.SportsBook.name(), start, temp);
            Integer currentTime = (int) now.toInstant().getEpochSecond();
            BetSlipsSupplemental po = PlatAbstractFactory
                    .buildBetSlipsSupplemental(dto.getGameId(), start.toString(), temp.toString(), sportsBook.toJSONString(), currentTime);
            list.add(po);

            // 补单:虚拟体育
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
     * 补充注单信息
     *
     * @param dto dto.requestInfo 补单请求参数
     * @author: David
     * @date: 04/05/2020
     */
    @Override
    public void betsRecordsSupplemental(PlatFactoryParams.BetsRecordsSupplementReqDto dto) {
        try {
            JSONObject result = pullSend(SBOUrlEnum.GET_BET_LIST_BY_MODIFY_DATE, dto.getRequestInfo());
            saveOrUpdateBatch(result);
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
     * xbSportsCategory 体育类型:1-真实体育 2-虚拟体育
     *
     * @param portfolio SportsBook-真实体育 VirtualSports-虚拟体育
     * @return 体育类型:1-真实体育 2-虚拟体育
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
     * 逻辑处理
     *
     * @param portfolio      SportsBook-真实体育 VirtualSports-虚拟体育
     * @param lastUpdateTime 节点变化时间
     * @param redisKey       节点变化时间的KEY
     */
    private void process(String portfolio, long lastUpdateTime, String redisKey) {
        String requestParams = "";
        // 拉单节点变化
        long tmpLastUpdateTime = lastUpdateTime;
        try {
            int xbSportsCategory = switchXbSportsCategory(portfolio);
            ZonedDateTime currentTime = LocalDateTime.now()
                    .atZone(DateNewUtils.getZoneId("UTC", "+8"))
                    .withZoneSameInstant(DateNewUtils.getZoneId("GMT", "-4"));
            ZonedDateTime startZonedDateTime = buildStartTime(tmpLastUpdateTime, xbSportsCategory, currentTime);
            // +30分钟
            ZonedDateTime endZonedDateTime = startZonedDateTime.plusMinutes(30);
            if (endZonedDateTime.toEpochSecond() > currentTime.toEpochSecond()) {
                endZonedDateTime = currentTime;
            }
            // redis存储最近更新时间
            tmpLastUpdateTime = endZonedDateTime.toEpochSecond();
            // -延迟时间
            startZonedDateTime = startZonedDateTime.minusMinutes(BasePlatParam.BasePlatEnum.SBO.getMinutes());
            endZonedDateTime = endZonedDateTime.minusMinutes(BasePlatParam.BasePlatEnum.SBO.getMinutes());

            JSONObject reqParams = buildReqParams(portfolio, startZonedDateTime, endZonedDateTime);
            requestParams = reqParams.toJSONString();
            JSONObject result = pullSend(SBOUrlEnum.GET_BET_LIST_BY_MODIFY_DATE, reqParams.toJSONString());
            saveOrUpdateBatch(result);

            // 拉单节点变化
            configCache.reSetLastUpdateTimeByModel(redisKey, String.valueOf(tmpLastUpdateTime));
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
     * build请求参数
     *
     * @param startZonedDateTime 开始时间
     * @param endZonedDateTime   结束时间
     * @return JSONObject
     */
    private JSONObject buildReqParams(String portfolio, ZonedDateTime startZonedDateTime, ZonedDateTime endZonedDateTime) {
        String startTime = startZonedDateTime.format(DateNewUtils.getDateTimeFormatter(DateNewUtils.Format.yyyy_MM_dd_HH_mm_ss_SSS));
        String endTime = endZonedDateTime.format(DateNewUtils.getDateTimeFormatter(DateNewUtils.Format.yyyy_MM_dd_HH_mm_ss_SSS));
        log.info("时间范围[" + startTime + " - " + endTime + "]");
        // 构建拉单参数
        SBOSportsRequestParameter.GetBetListByModifyDate m = SBOSportsRequestParameter.GetBetListByModifyDate.builder()
                .portfolio(portfolio)
                .startDate(startTime)
                .endDate(endTime)
                .build();
        return buildCommonReqParams(m);
    }

    /**
     * build开始时间
     *
     * @param lastUpdateTime   节点变化时间
     * @param xbSportsCategory 体育类型:1-真实体育 2-虚拟体育
     * @param currentTime      服务当前时间
     * @return ZonedDateTime
     */
    private ZonedDateTime buildStartTime(long lastUpdateTime, int xbSportsCategory, ZonedDateTime currentTime) {
        ZonedDateTime startZonedDateTime;
        // redis没记录
        if (lastUpdateTime == 0) {
            BetslipsSboSports betslipsSboSports = getBetsLipsSboSports(xbSportsCategory);
            // 数据库没记录
            if (null == betslipsSboSports) {
                // -1天
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
     * 获取最新拉单记录
     *
     * @param xbSportsCategory 体育类型:1-真实体育 2-虚拟体育
     * @return 最新拉单记录
     */
    private BetslipsSboSports getBetsLipsSboSports(Integer xbSportsCategory) {
        return betslipsSboSportsServiceImpl.lambdaQuery()
                .eq((null != xbSportsCategory), BetslipsSboSports::getXbSportsCategory, xbSportsCategory)
                .orderByDesc(BetslipsSboSports::getModifyDate)
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
        commonPersistence.addBetSlipsException(BasePlatParam.GAME.SBO_SPORTS.getCode(), requestParams, category, exceptionInfo, 0);
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
     * @param thirdSBOSEnum URL
     * @param reqParams     reqParams
     * @return JSONObject
     */
    private JSONObject pullSend(SBOUrlEnum thirdSBOSEnum, String reqParams) {
        /* 通过model从redis获取域名、CompanyKey、ServerId */
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
     * 修改时区GMT-4
     *
     * @param portfolio          SportsBook-真实体育 VirtualSports-虚拟体育
     * @param startZonedDateTime 开始时间
     * @param endZonedDateTime   结束时间
     * @return 待请求参数
     */
    private JSONObject buildPullBetReqParamsWithZoneSameInstant(String portfolio, ZonedDateTime startZonedDateTime, ZonedDateTime endZonedDateTime) {
        startZonedDateTime = startZonedDateTime.withZoneSameInstant(DateNewUtils.getZoneId("GMT", "-4"));
        endZonedDateTime = endZonedDateTime.withZoneSameInstant(DateNewUtils.getZoneId("GMT", "-4"));
        return buildReqParams(portfolio, startZonedDateTime, endZonedDateTime);
    }

    /**
     * 数据批量持久化拉单数据
     *
     * @param responseBody responseBody
     */
    private void saveOrUpdateBatch(JSONObject responseBody) {
        JSONArray data = responseBody.getJSONArray("result");
        if (Optional.ofNullable(data).isEmpty() || data.isEmpty()) {
            return;
        }
        // 用户表 id - username 映射集
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

            // 将返回的 订单时间由 GMT-4 -> UTC+8 存createdAt
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
            // 体育类型:1-真实体育 2-虚拟体育;      gameId和productType为虚拟体育特有字段
            int xbSportsCategory = (StringUtils.isNotBlank(dest.getProductType()) || null != dest.getGameId()) ? 2 : 1;
            dest.setXbSportsCategory(xbSportsCategory);
            return dest;
        }).filter(o -> o.getXbUid() != null).collect(Collectors.toList());

        betslipsSboSportsServiceImpl.saveOrUpdateBatch(dataList,dataList.size());
    }

    /**
     * 拉单异常 再次拉单
     *
     * @param dto dto.requestInfo 拉单请求参数
     */
    @Override
    public void betSlipsExceptionPull(@javax.validation.constraints.NotNull PlatFactoryParams.BetsRecordsSupplementReqDto dto) {
        this.betsRecordsSupplemental(dto);
    }

}
