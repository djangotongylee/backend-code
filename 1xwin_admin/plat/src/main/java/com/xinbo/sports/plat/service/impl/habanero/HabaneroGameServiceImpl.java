package com.xinbo.sports.plat.service.impl.habanero;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xinbo.sports.dao.generator.po.BetSlipsSupplemental;
import com.xinbo.sports.dao.generator.po.BetslipsHb;
import com.xinbo.sports.dao.generator.po.GameSlot;
import com.xinbo.sports.plat.base.CommonPersistence;
import com.xinbo.sports.plat.base.SlotServiceBase;
import com.xinbo.sports.plat.factory.ISuppleMethods;
import com.xinbo.sports.plat.factory.PlatAbstractFactory;
import com.xinbo.sports.plat.io.bo.HBRequestParameter;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams.PlatSlotGameFavoriteReqDto;
import com.xinbo.sports.plat.io.enums.BasePlatParam;
import com.xinbo.sports.plat.io.enums.HBUrlEnum;
import com.xinbo.sports.plat.service.impl.HabaneroServiceImpl;
import com.xinbo.sports.service.cache.redis.DictionaryCache;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.DateUtils;
import com.xinbo.sports.utils.HttpUtils;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author: David
 * @date: 04/05/2020
 * @description:
 */
@Service
@Slf4j
public class HabaneroGameServiceImpl extends HabaneroServiceImpl implements ISuppleMethods {
    protected static final Pattern PATTERN = Pattern.compile("[0-9]{1,4}Hand$");
    protected static final String GAME_TYPE_ID = "GameTypeId";
    protected static final int GAME_TYPE_VIDEO_POKER_ID = 6;
    @Resource
    private SlotServiceBase slotServiceBase;
    @Resource
    private CommonPersistence commonPersistence;
    @Resource
    private DictionaryCache dictionaryCache;

    @Override
    public Boolean favoriteSlotGame(PlatSlotGameFavoriteReqDto platSlotGameFavoriteReqDto) {
        return slotServiceBase.favoriteSlotGame(platSlotGameFavoriteReqDto);
    }

    /**
     * ????????????????????????????????????????????????????????????
     *
     * @author: David
     * @date: 14/05/2020
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pullGames() {
        HBRequestParameter.GetGamesRequest m = HBRequestParameter.GetGamesRequest.builder()
                .BrandId(config.getBrandId())
                .APIKey(config.getApiKey())
                .build();

        try {
            JSONObject send = JSON.parseObject(send(HBUrlEnum.GET_GAMES, JSON.parseObject(JSON.toJSONString(m))));
            JSONArray games = send.getJSONArray("Games");
            Integer time = DateUtils.getCurrentTime();

            List<GameSlot> collect = games.stream().map(o -> {
                JSONObject obj = (JSONObject) o;
                GameSlot gameSlot = JSON.toJavaObject(obj, GameSlot.class);
                // ??????????????????????????? ???????????????????????????????????????
                String prefix = "/icon/habanero/rectangle/";
                if (obj.getInteger(GAME_TYPE_ID).equals(GAME_TYPE_VIDEO_POKER_ID)) {
                    // Video Poker ?????????????????????
                    String keyName = obj.getString("KeyName");
                    Matcher matcher = PATTERN.matcher(keyName);
                    prefix += matcher.find() ? keyName.substring(0, keyName.indexOf(matcher.group(0))) : keyName;
                } else {
                    prefix += obj.getString("KeyName");
                }

                // ?????????????????? ??????1-?????? 3-?????? 29-????????????????????????
                JSONArray translatedNames = obj.getJSONArray("TranslatedNames");
                Map<String, String> collect1 = translatedNames.stream()
                        .filter(id -> List.of(1, 3).contains(((JSONObject) id).getInteger("LanguageId")))
                        .collect(Collectors.toMap(
                                key -> ((JSONObject) key).getString("Locale"),
                                value -> ((JSONObject) value).getString("Translation")
                        ));
                // ??????game_hb??????
                gameSlot.setName(collect1.get("en"));
                gameSlot.setNameZh(collect1.get("zh-CN"));
                gameSlot.setImg(prefix + ".png");
                gameSlot.setId(obj.getString("BrandGameId"));
                gameSlot.setCreatedAt(time);
                gameSlot.setUpdatedAt(time);
                gameSlot.setGameId(202);
                return gameSlot;
            }).collect(Collectors.toList());
            // ??????????????????
            gameSlotServiceImpl.lambdaUpdate().eq(GameSlot::getGameId, 202).remove();
            // ??????????????????
            gameSlotServiceImpl.saveBatch(collect);
            dictionaryCache.updateSlotGameCache("202");
        } catch (Exception e) {
            log.error(e.toString());
            throw new BusinessException(CodeInfo.PLAT_PULL_GAME_LIST_ERROR);
        }
    }

    /**
     * ???????????????????????????
     * Start ~ End ?????????????????? 3??????
     * dt_completed ????????????
     *
     * @author: David
     * @date: 15/05/2020
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pullBetsLips() {
        String requestParams = "";
        // ??????????????????
        long lastUpdateTime = 0L;
        try {
            ZonedDateTime currentTime = LocalDateTime.now()
                    .atZone(DateNewUtils.getZoneId("UTC", "+8"))
                    .withZoneSameInstant(DateNewUtils.getZoneId("UTC", "+0"));
            // ??????MODEL????????????????????????
            String lastTime = configCache.getLastUpdateTimeByModel(MODEL);
            lastUpdateTime = StringUtils.isBlank(lastTime) ? 0L : Long.parseLong(lastTime);
            ZonedDateTime startZonedDateTime = getStartZonedDateTime(lastUpdateTime, currentTime);
            // +1???
            ZonedDateTime endZonedDateTime = startZonedDateTime.plusDays(1);
            if (endZonedDateTime.toEpochSecond() > currentTime.toEpochSecond()) {
                endZonedDateTime = currentTime;
            }
            // redis????????????????????????
            lastUpdateTime = endZonedDateTime.toEpochSecond();
            // -????????????
            startZonedDateTime = startZonedDateTime.minusMinutes(BasePlatParam.BasePlatEnum.HB.getMinutes());
            endZonedDateTime = endZonedDateTime.minusMinutes(BasePlatParam.BasePlatEnum.HB.getMinutes());
            requestParams = buildPullBetReqParams(startZonedDateTime, endZonedDateTime);
            String send = pullSend(HBUrlEnum.GET_BRAND_COMPLETED_GAME_RESULTS_V2, JSON.parseObject(requestParams));
            if (null != send) {
                // ????????????
                saveOrUpdateBatch(send);
                // ??????????????????
                configCache.reSetLastUpdateTimeByModel(MODEL, String.valueOf(lastUpdateTime));
            }
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

    private ZonedDateTime getStartZonedDateTime(long lastUpdateTime, ZonedDateTime currentTime) {
        ZonedDateTime startZonedDateTime;
        // redis?????????
        if (lastUpdateTime == 0) {
            BetslipsHb po = getBetsLipsHb();
            // ??????????????????
            if (null == po) {
                // -1???
                startZonedDateTime = currentTime.minusDays(1);
            } else {
                startZonedDateTime = po.getDtCompleted().toInstant()
                        .atZone(DateNewUtils.getZoneId("UTC", "+8"))
                        .withZoneSameInstant(DateNewUtils.getZoneId("UTC", "+0"));
            }
        } else {
            startZonedDateTime = Instant.ofEpochSecond(lastUpdateTime)
                    .atZone(DateNewUtils.getZoneId("UTC", "+8"))
                    .withZoneSameInstant(DateNewUtils.getZoneId("UTC", "+0"));
        }
        return startZonedDateTime;
    }

    /**
     * ????????????????????????
     *
     * @return ??????????????????
     */
    private BetslipsHb getBetsLipsHb() {
        return betslipsHbServiceImpl.lambdaQuery()
                .orderByDesc(BetslipsHb::getDtStarted)
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
        commonPersistence.addBetSlipsException(BasePlatParam.GAME.HB_GAMES.getCode(), requestParams, category, exceptionInfo, 0);
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
     * @param hbUrlEnum URL
     * @param reqParams reqParams
     * @return String
     */
    public String pullSend(@NotNull HBUrlEnum hbUrlEnum, JSONObject reqParams) {
        String url = config.getApiUrl() + hbUrlEnum.getUrl();
        String call = null;
        try {
            call = HttpUtils.doPost(url, reqParams.toJSONString(), MODEL);
        } catch (Exception e) {
            log.error(MODEL + CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0.getMsg() + ":" + e.toString());
            buildCodeInfo(e.toString());
        }
        return call;
    }

    /**
     * ??????????????????????????? ??????????????????
     * <p>
     * HB:
     * 1. ??????????????? ???????????????????????????  			????????????90???
     * 2. ???????????? ????????????(ID)  ??????-????????????		????????????90???
     * 3. ????????????  ????????????						??????????????????????????????????????????????????????????????????????????????25??? ??????????????????
     * 4. ?????????????????????(???)						???
     *
     * @param dto dto.start ???????????? dto.end????????????
     */
    @Override
    public void genSupplementsOrders(@javax.validation.constraints.NotNull PlatFactoryParams.GenSupplementsOrdersReqDto dto) {
        // 1. ??????????????? ???????????????????????????  			????????????90???
        long preDays = 90L;
        // 2. ???????????? ????????????(ID)  ??????-????????????		????????????90???
        long startEndRange = 60L;
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime start = DateNewUtils.utc8Zoned(dto.getStart());
        ZonedDateTime end = DateNewUtils.utc8Zoned(dto.getEnd());
        if (start.compareTo(now.minusDays(preDays)) < 0 || start.compareTo(end) >= 0) {
            throw new BusinessException(CodeInfo.PLAT_BET_SLIPS_SUPPLE_OVER_90_DAYS);
        }
        if (end.compareTo(now) > 0) {
            end = now;
        }
        ZonedDateTime temp;
        LinkedList<BetSlipsSupplemental> list = new LinkedList<>();
        do {
            temp = start.plusMinutes(startEndRange);

            // ????????????
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
     * ????????????UTC+0
     *
     * @param startZonedDateTime ????????????
     * @param endZonedDateTime   ????????????
     * @return ????????????
     */
    private String buildPullBetReqParamsWithZoneSameInstant(ZonedDateTime startZonedDateTime, ZonedDateTime endZonedDateTime) {
        startZonedDateTime = startZonedDateTime.withZoneSameInstant(DateNewUtils.getZoneId("UTC", "+0"));
        endZonedDateTime = endZonedDateTime.withZoneSameInstant(DateNewUtils.getZoneId("UTC", "+0"));
        return buildPullBetReqParams(startZonedDateTime, endZonedDateTime);
    }

    /**
     * ????????????????????????
     *
     * @param startZonedDateTime ????????????
     * @param endZonedDateTime   ????????????
     * @return JSONObject ????????????
     */
    private String buildPullBetReqParams(ZonedDateTime startZonedDateTime, ZonedDateTime endZonedDateTime) {
        String startTime = startZonedDateTime.format(DateNewUtils.getDateTimeFormatter(DateNewUtils.Format.yyyyMMddHHmmss));
        String endTime = endZonedDateTime.format(DateNewUtils.getDateTimeFormatter(DateNewUtils.Format.yyyyMMddHHmmss));
        // ????????????????????????:????????????????????????????????? ???:????????????UTC+0 ???:??????7???
        HBRequestParameter.GetBrandCompletedGameResultsV2 m = HBRequestParameter.GetBrandCompletedGameResultsV2.builder()
                .APIKey(config.getApiKey())
                .BrandId(config.getBrandId())
                .DtStartUTC(startTime)
                .DtEndUTC(endTime)
                .build();
        return JSON.toJSONString(m);
    }

    /**
     * ?????????????????????????????????
     *
     * @param responseBody responseBody
     */
    private void saveOrUpdateBatch(String responseBody) {
        // ??????????????????
        JSONArray array = JSON.parseArray(responseBody);
        if (Optional.ofNullable(array).isEmpty() || array.isEmpty()) {
            return;
        }
        int time = (int) Instant.now().getEpochSecond();
        // ????????? id - username ?????????
        Map<String, Integer> usernameIdMap = userServiceBase.getUsernameIdMap();
        List<BetslipsHb> collect = array.stream().map(o -> {
            JSONObject obj = (JSONObject) o;
            BetslipsHb betslipsHb = JSON.toJavaObject(obj, BetslipsHb.class);
            // ??????????????????????????????UID ?????????0
            String username = userServiceBase.filterUsername(betslipsHb.getUsername());
            Integer uid = usernameIdMap.get(username);
            betslipsHb.setXbUid(uid != null ? uid : 0);
            betslipsHb.setXbUsername(username);
            betslipsHb.setId(betslipsHb.getGameInstanceId());
            // ??? UTC+0??????  ?????? UTC+8?????? ?????????Created_at??????
            ZonedDateTime zCreatedAt = DateNewUtils.getZonedDateTime(
                    obj.getString("DtStarted"),
                    DateNewUtils.Format.yyyy_MM_dd_T_HH_mm_ss_SSS,
                    DateNewUtils.getZoneId("UTC", "+0")
            );
            // UTC+0 -> UTC+8
            long lCreateAt = zCreatedAt.withZoneSameInstant(DateNewUtils.getZoneId("UTC", "+8")).toInstant().getEpochSecond();
            betslipsHb.setXbCoin(betslipsHb.getStake());
            betslipsHb.setXbValidCoin(betslipsHb.getStake());
            BigDecimal profit = betslipsHb.getPayout().subtract(betslipsHb.getStake());
            betslipsHb.setXbProfit(profit);
            int status;
            if (profit.compareTo(BigDecimal.ZERO) < 0) {
                status = BasePlatParam.BetRecordsStatus.LOSE.getCode();
            } else if (profit.compareTo(BigDecimal.ZERO) == 0) {
                status = BasePlatParam.BetRecordsStatus.DRAW.getCode();
            } else {
                status = BasePlatParam.BetRecordsStatus.WIN.getCode();

            }
            betslipsHb.setXbStatus(status);
            betslipsHb.setCreatedAt((int) lCreateAt);
            betslipsHb.setUpdatedAt(time);

            return betslipsHb;
        }).filter(o -> o.getXbUid() != 0).collect(Collectors.toList());

        betslipsHbServiceImpl.saveOrUpdateBatch(collect, collect.size());
    }

    /**
     * ??????????????????
     *
     * @param dto dto.requestInfo ??????????????????
     */
    @Override
    public void betsRecordsSupplemental(@javax.validation.constraints.NotNull PlatFactoryParams.BetsRecordsSupplementReqDto dto) {
        try {
            JSONObject reqBody = JSON.parseObject(dto.getRequestInfo());
            String send = pullSend(HBUrlEnum.GET_BRAND_COMPLETED_GAME_RESULTS_V2, reqBody);
            // ????????????
            saveOrUpdateBatch(send);
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
     * ???????????? ????????????
     *
     * @param dto dto.requestInfo ??????????????????
     */
    @Override
    public void betSlipsExceptionPull(@javax.validation.constraints.NotNull PlatFactoryParams.BetsRecordsSupplementReqDto dto) {
        this.betsRecordsSupplemental(dto);
    }
}
