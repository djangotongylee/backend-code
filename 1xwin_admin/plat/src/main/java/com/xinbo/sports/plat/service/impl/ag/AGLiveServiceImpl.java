package com.xinbo.sports.plat.service.impl.ag;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.google.common.collect.Lists;
import com.xinbo.sports.dao.generator.po.BetSlipsSupplemental;
import com.xinbo.sports.dao.generator.po.BetslipsAg;
import com.xinbo.sports.dao.generator.po.Dict;
import com.xinbo.sports.dao.generator.po.DictItem;
import com.xinbo.sports.dao.generator.service.BetslipsAgService;
import com.xinbo.sports.dao.generator.service.DictItemService;
import com.xinbo.sports.dao.generator.service.DictService;
import com.xinbo.sports.plat.base.CommonPersistence;
import com.xinbo.sports.plat.factory.PlatAbstractFactory;
import com.xinbo.sports.plat.io.bo.AgLiveRequestParameter;
import com.xinbo.sports.plat.io.bo.AgLiveResponse;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams;
import com.xinbo.sports.plat.io.enums.AgRoutineParam;
import com.xinbo.sports.plat.io.enums.AgUrlEnum;
import com.xinbo.sports.plat.io.enums.BasePlatParam;
import com.xinbo.sports.plat.service.impl.AGServiceImpl;
import com.xinbo.sports.service.base.UserServiceBase;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.utils.*;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.xinbo.sports.plat.io.enums.BasePlatParam.BasePlatEnum;

/**
 * <p>
 * AG视讯
 * </p>
 *
 * @author andy
 * @since 2020/7/9
 */
@Service
@Slf4j
public class AGLiveServiceImpl extends AGServiceImpl {
    private static final String DIC_BETSLIPS_AG_GAME_TYPE = "dic_betslips_ag_game_type";
    private static final String DIC_BETSLIPS_AG_PLAY_TYPE = "dic_betslips_ag_play_type";
    @Resource
    private DictService dictServiceImpl;
    @Resource
    private DictItemService dictItemServiceImpl;
    @Resource
    private BetslipsAgService betslipsAgServiceImpl;
    @Resource
    private UserServiceBase userServiceBase;
    @Resource
    private CommonPersistence commonPersistence;

    private static Date text2Date(String time) {
        return DateNewUtils.zonedDateTime2Date(time, DateNewUtils.Format.yyyy_MM_dd_HH_mm_ss, "GMT", "-4");
    }

    private static int getStatus(int value) {
        // 1-赢 2-输 3-和 4-取消 5-等待结算 6-赛事取消 7-投注确认 8-投注拒绝 9-赢一半 10-输一半
        int status;
        if (value > 0) {
            status = 1;
        } else if (value < 0) {
            status = 2;
        } else {
            status = 3;
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
    private static int processCreatedAt(String betTime) {
        return DateNewUtils.oriTimeZoneToDesTimeZone(betTime,
                DateNewUtils.Format.yyyy_MM_dd_HH_mm_ss,
                "GMT", "-4", "UTC", "+8"
        );
    }

    /**
     * 拉单params
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param page      当前页
     * @param perPage   每页条数
     * @return params
     */
    private String buildGetOrdersParams(String startTime, String endTime, int page, int perPage) {
        String[] keyValues = {config.getBetSlipsCagent(), startTime, endTime, page + "", perPage + ""};
        StringBuilder textParams = new StringBuilder();
        textParams.append("?");
        textParams.append(AgLiveRequestParameter.BetSlipsReqKey.cagent).append("=").append(keyValues[0]);
        textParams.append("&").append(AgLiveRequestParameter.BetSlipsReqKey.startdate).append("=").append(keyValues[1]);
        textParams.append("&").append(AgLiveRequestParameter.BetSlipsReqKey.enddate).append("=").append(keyValues[2]);
        textParams.append("&").append(AgLiveRequestParameter.BetSlipsReqKey.page).append("=").append(keyValues[3]);
        textParams.append("&").append(AgLiveRequestParameter.BetSlipsReqKey.perpage).append("=").append(keyValues[4]);
        // getMd5Values
        textParams.append("&").append(AgUrlEnum.AGLIVE_GETORDERS.getKey()).append("=").append(getOrdersParamsMd5AfterValues(keyValues));
        return textParams.toString();
    }

    /**
     * 拉单:获取游戏类型
     *
     * @return
     */
    private List<DictItem> getGameTypes(String language) {
        String params = buildGameTypesParams(language);
        String result = send(config.getBetSlipsUrl() + AgUrlEnum.AGLIVE_GAMETYPES.getMethod(), params);
        log.info("getGameTypes xml ==" + result);
        AgLiveResponse.AgGameTypesResult gameTypesResult = XmlBuilder.xmlStrToObject(result, AgLiveResponse.AgGameTypesResult::new);

        if (Objects.nonNull(gameTypesResult) && "0".equals(gameTypesResult.getInfo())) {
            List<AgLiveResponse.GameTypesXml> gameTypesXmlList = gameTypesResult.getRow();
            return processGameTypesXmlList(gameTypesXmlList, language);
        }
        return Lists.newArrayList();
    }

    /**
     * 拉单:获取游戏类型
     *
     * @return
     */
    public byte[] dictGameTypes(String language) {
        List<DictItem> itemListChinese = getGameTypes(AgRoutineParam.BetSlipsLang.LANG_CNS.getCode());
        List<DictItem> itemListEngList = getGameTypes(AgRoutineParam.BetSlipsLang.LANG_ENGLISH.getCode());
        if (!(!Optional.ofNullable(itemListEngList).isEmpty() || !Optional.ofNullable(getDictByCategory(DIC_BETSLIPS_AG_GAME_TYPE)).isPresent())) {
            Dict dict = getDictByCategory(DIC_BETSLIPS_AG_GAME_TYPE);
            itemListEngList = dictItemServiceImpl.lambdaQuery().eq(DictItem::getReferId, dict.getId()).list();
        }
        Map<String, String> itemListEnglishMap = new HashMap<>();
        if (Optional.ofNullable(itemListChinese).isPresent()) {
            itemListEnglishMap = itemListChinese.stream().collect(Collectors.toMap(DictItem::getCode, DictItem::getTitle));
        }
        byte[] bytes = null;
        if (Optional.ofNullable(itemListEngList).isPresent()) {
            Path path = FileUtils.getAgGameTypesFileName(language);
            try {
                if (Files.exists(path)) {
                    Files.delete(path);
                }
                Files.createFile(path);
                for (DictItem dictItem : itemListEngList) {
                    StringBuilder builder = new StringBuilder();
                    builder.append(dictItem.getTitle());
                    String englishTitle = "";
                    if (itemListEnglishMap.containsKey(dictItem.getCode())) {
                        englishTitle = itemListEnglishMap.get(dictItem.getCode());
                        builder.append("=").append(englishTitle);
                    }
                    builder.append(System.getProperty("line.separator"));
                    Files.write(path, builder.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                }
                bytes = Files.readAllBytes(path);
            } catch (IOException e) {
                log.error(MODEL + "文件生成失败:" + e);
            }
        }
        return bytes;
    }

    private List<DictItem> processGameTypesXmlList(List<AgLiveResponse.GameTypesXml> gameTypesXmlList, String language) {
        List<DictItem> itemList = null;
        if (!gameTypesXmlList.isEmpty()) {
            itemList = new ArrayList<>();
            String category = DIC_BETSLIPS_AG_GAME_TYPE;
            int currentTime = DateNewUtils.now();
            Dict dict = null;
            if (Objects.nonNull(getDictByCategory(category))) {
                dict = getDictByCategory(category);
            } else {
                dict = new Dict();
                dict.setTitle("AG注单表_游戏类型");
                dict.setCategory(category);
                dict.setCreatedAt(currentTime);
                dict.setUpdatedAt(currentTime);
                dictServiceImpl.save(dict);
            }
            Integer referId = dict.getId();
            for (AgLiveResponse.GameTypesXml gameTypesXml : gameTypesXmlList) {
                DictItem item = new DictItem();
                item.setReferId(referId);
                item.setCode(gameTypesXml.getGameType());
                item.setTitle(gameTypesXml.getGameName());
                item.setCreatedAt(currentTime);
                item.setUpdatedAt(currentTime);
                itemList.add(item);
            }
            // 仅简体中文入库
            if (!itemList.isEmpty() && AgRoutineParam.BetSlipsLang.LANG_ENGLISH.getCode().equals(language)) {
                dictItemServiceImpl.lambdaUpdate().eq(DictItem::getReferId, referId).remove();
                dictItemServiceImpl.saveBatch(itemList);
            }
        }
        return itemList;
    }

    private List<DictItem> processGamePlayTypesXmlList(List<AgLiveResponse.GamePlayTypesXml> gamePlayTypesXmlList, String language) {
        List<DictItem> itemList = null;
        String category = DIC_BETSLIPS_AG_PLAY_TYPE;
        if (!gamePlayTypesXmlList.isEmpty()) {
            itemList = new ArrayList<>();
            int currentTime = DateNewUtils.now();
            Dict dict = null;
            if (Objects.nonNull(getDictByCategory(category))) {
                dict = getDictByCategory(category);
            } else {
                dict = new Dict();
                dict.setTitle("AG注单表_玩法类型");
                dict.setCategory(category);
                dict.setCreatedAt(currentTime);
                dict.setUpdatedAt(currentTime);
                dictServiceImpl.save(dict);
            }
            Integer referId = dict.getId();
            for (AgLiveResponse.GamePlayTypesXml gamePlayTypesXml : gamePlayTypesXmlList) {
                DictItem item = new DictItem();
                item.setReferId(referId);
                item.setCode(gamePlayTypesXml.getPlayType());
                item.setTitle(gamePlayTypesXml.getDescription());
                item.setCreatedAt(currentTime);
                item.setUpdatedAt(currentTime);
                itemList.add(item);
            }
            // 仅简体中文入库
            if (!itemList.isEmpty() && AgRoutineParam.BetSlipsLang.LANG_ENGLISH.getCode().equals(language)) {
                dictItemServiceImpl.lambdaUpdate().eq(DictItem::getReferId, referId).remove();
                dictItemServiceImpl.saveBatch(itemList);
            }
        }
        return itemList;
    }

    private Dict getDictByCategory(String category) {
        return dictServiceImpl.lambdaQuery().eq(Dict::getCategory, category).eq(Dict::getStatus, 1).last("limit 1").one();
    }

    /**
     * 拉单:游戏玩法下注类型
     *
     * @return
     */
    private List<DictItem> getGamePlayTypes(String language) {
        String params = buildGameTypesParams(language);
        String result = send(config.getBetSlipsUrl() + AgUrlEnum.AGLIVE_GAMEPLAYTYPES.getMethod(), params);
        log.info("getGamePlayTypes xml ==" + result);
        AgLiveResponse.AgGamePlayTypesResult playTypesResult = XmlBuilder.xmlStrToObject(result, AgLiveResponse.AgGamePlayTypesResult::new);
        if (Objects.nonNull(playTypesResult) && "0".equals(playTypesResult.getInfo())) {
            List<AgLiveResponse.GamePlayTypesXml> gamePlayTypesXmlList = playTypesResult.getRow();
            return processGamePlayTypesXmlList(gamePlayTypesXmlList, language);
        }
        return Lists.newArrayList();
    }

    /**
     * 拉单:游戏玩法下注类型
     *
     * @return
     */
    public byte[] dictGamePlayTypes(String language) {
        List<DictItem> itemListChinese = getGamePlayTypes(AgRoutineParam.BetSlipsLang.LANG_CNS.getCode());
        List<DictItem> itemListEnglish = getGamePlayTypes(AgRoutineParam.BetSlipsLang.LANG_ENGLISH.getCode());
        if (Optional.ofNullable(itemListEnglish).isEmpty() && Optional.ofNullable(getDictByCategory(DIC_BETSLIPS_AG_PLAY_TYPE)).isPresent()) {
            Dict dict = getDictByCategory(DIC_BETSLIPS_AG_PLAY_TYPE);
            itemListEnglish = dictItemServiceImpl.lambdaQuery().eq(DictItem::getReferId, dict.getId()).list();
        }

        Map<String, String> itemListEnglishMap = new HashMap<>();
        if (Optional.ofNullable(itemListChinese).isPresent()) {
            itemListEnglishMap = itemListChinese.stream().collect(Collectors.toMap(DictItem::getCode, DictItem::getTitle));
        }

        byte[] bytes = null;
        if (Optional.ofNullable(itemListEnglish).isPresent()) {
            Path path = FileUtils.getGamePlayTypesFileName(language);
            try {
                if (Files.exists(path)) {
                    Files.delete(path);
                }
                Files.createFile(path);

                for (DictItem dictItem : itemListEnglish) {
                    StringBuilder builder = new StringBuilder();
                    builder.append(dictItem.getTitle());
                    String englishTitle = "";
                    if (itemListEnglishMap.containsKey(dictItem.getCode())) {
                        englishTitle = itemListEnglishMap.get(dictItem.getCode());
                        builder.append("=").append(englishTitle);
                    }
                    builder.append(System.getProperty("line.separator"));
                    Files.write(path, builder.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                }
                bytes = Files.readAllBytes(path);
            } catch (IOException e) {
                log.error(MODEL + "文件生成失败:" + e);
            }
        }
        return bytes;
    }

    /**
     * 拉单:获取游戏类型params
     *
     * @return
     */
    private String buildGameTypesParams(String language) {
        String[] keyValues = {config.getBetSlipsCagent(), language};
        StringBuilder textParams = new StringBuilder();
        textParams.append("?");
        textParams.append(AgLiveRequestParameter.BetSlipsReqKey.cagent).append("=").append(keyValues[0]);
        textParams.append("&").append(AgLiveRequestParameter.BetSlipsReqKey.language).append("=").append(keyValues[1]);
        // getMd5Values
        textParams.append("&").append(AgUrlEnum.AGLIVE_GETORDERS.getKey()).append("=").append(getOrdersParamsMd5AfterValues(keyValues));
        return textParams.toString();
    }

    /**
     * 拉单MD5AfterValues
     *
     * <p>
     * 规则:md5Value = md5(cagent + startdate+ enddate+ gametype+order+by+page+perpage + “明码”)
     *
     * @param md5BeforeValues MD5加密前数据
     * @return MD5加密后数据
     */
    private String getOrdersParamsMd5AfterValues(String[] md5BeforeValues) {
        StringBuilder values = new StringBuilder();
        for (String value : md5BeforeValues) {
            values.append(value);
        }
        if (StringUtils.isNotBlank(values)) {
            values.append(config.getBetSlipsPidToken());
        }
        return MD5.encryption(values.toString());
    }

    /**
     * 说明:获取视讯游戏订单数据,查询时间以订单派彩时间为准.
     *
     * <p>
     * 1.只能获取10分钟以内的数据
     * 2.API重复调用频率限制15秒只能调用1次
     * 3.时区:GMT-4
     */
    @Override
    public void pullBetsLips() {
        String params = "";
        long lastUpdateTime = 0L;
        try {
            ZonedDateTime currentTime = LocalDateTime.now()
                    .atZone(DateNewUtils.getZoneId("UTC", "+8"))
                    .withZoneSameInstant(DateNewUtils.getZoneId("GMT", "-4"));
            ZonedDateTime startZonedDateTime = null;
            ZonedDateTime endZonedDateTime = null;
            // 根据MODEL获取最近更新时间
            String lastTime = configCache.getLastUpdateTimeByModel(MODEL);
            lastUpdateTime = StringUtils.isBlank(lastTime) ? 0L : Long.parseLong(lastTime);
            // redis没记录
            if (lastUpdateTime == 0) {
                BetslipsAg po = getBetsLipsAg();
                // 数据库没记录
                if (null == po) {
                    // -1天
                    startZonedDateTime = currentTime.minusDays(1);
                } else {
                    startZonedDateTime = po.getBetTime().toInstant()
                            .atZone(DateNewUtils.getZoneId("UTC", "+8"))
                            .withZoneSameInstant(DateNewUtils.getZoneId("GMT", "-4"));
                }
            } else {
                startZonedDateTime = Instant.ofEpochSecond(lastUpdateTime)
                        .atZone(DateNewUtils.getZoneId("UTC", "+8"))
                        .withZoneSameInstant(DateNewUtils.getZoneId("GMT", "-4"));
            }
            // +10分钟
            endZonedDateTime = startZonedDateTime.plusMinutes(10);
            if (endZonedDateTime.toEpochSecond() > currentTime.toEpochSecond()) {
                endZonedDateTime = currentTime;
            }
            // 更新redis最近时间
            lastUpdateTime = endZonedDateTime.toEpochSecond();
            // -延迟时间
            startZonedDateTime = startZonedDateTime.minusMinutes(BasePlatEnum.AG.getMinutes());
            endZonedDateTime = endZonedDateTime.minusMinutes(BasePlatEnum.AG.getMinutes());
            // 拉单开始、结束时间(派彩时间GMT-4)
            params = buildPullBetReqParams(startZonedDateTime, endZonedDateTime);
            String result = pullSend(config.getBetSlipsUrl() + AgUrlEnum.AGLIVE_GETORDERS.getMethod(), params);
            saveOrUpdateBatch(result);
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

    private void processAgGetOrdersResult(AgLiveResponse.AgGetOrdersResult agGetOrdersResult) {
        List<AgLiveResponse.BetSlipsAgXml> xmlList = agGetOrdersResult.getRow();
        if (Optional.ofNullable(xmlList).isEmpty()) {
            return;
        }
        List<BetslipsAg> betSlipsAgList = new ArrayList<>();
        Map<String, Integer> usernameIdMap = userServiceBase.getUsernameIdMap();
        Integer time = DateUtils.getCurrentTime();
        for (AgLiveResponse.BetSlipsAgXml entity : xmlList) {
            BetslipsAg betslipsAg = processBetSlipsAg(time, entity, usernameIdMap);
            if (null != betslipsAg) {
                betSlipsAgList.add(betslipsAg);
            }
        }
        if (!betSlipsAgList.isEmpty()) {
            betslipsAgServiceImpl.saveOrUpdateBatch(betSlipsAgList,betSlipsAgList.size());
        }
    }

    private BetslipsAg processBetSlipsAg(Integer time, AgLiveResponse.BetSlipsAgXml xmlPo, Map<String, Integer> usernameIdMap) {
        if (null == xmlPo) {
            return null;
        }
        BetslipsAg po = BeanConvertUtils.beanCopy(xmlPo, BetslipsAg::new);
        // 用户表 id - username 映射集
        if (null != usernameIdMap) {
            String username = userServiceBase.filterUsername(xmlPo.getPlayName());
            if (StringUtils.isBlank(username)) {
                return null;
            }
            po.setXbUid(usernameIdMap.get(username));
            po.setXbUsername(username);
        }

        po.setBeforeCredit(new BigDecimal(xmlPo.getBeforeCredit()));
        po.setBetAmount(new BigDecimal(xmlPo.getBetAmount()));
        po.setNetAmount(new BigDecimal(xmlPo.getNetAmount()));
        po.setValidBetAmount(new BigDecimal(xmlPo.getValidBetAmount()));
        po.setBetTime(text2Date(xmlPo.getBetTime()));
        po.setRecalcuTime(text2Date(xmlPo.getRecalcuTime()));
        // processCreatedAt
        if (Objects.nonNull(xmlPo.getBetTime())) {
            po.setCreatedAt(processCreatedAt(xmlPo.getBetTime()));
        }
        po.setUpdatedAt(time);
        po.setXbCoin(po.getBetAmount());
        po.setXbValidCoin(po.getValidBetAmount());
        // 订单状态:0异常(请联系客服) 1已派彩 -8取消指定局注单 -9取消指定注单
        if (po.getFlag() == 1) {
            po.setXbProfit(po.getNetAmount());
            po.setXbStatus(getStatus(po.getXbProfit().compareTo(BigDecimal.ZERO)));
        } else if (po.getFlag() == -8 || po.getFlag() == -9) {
            po.setXbStatus(4);
        }
        return po;
    }

    /**
     * 获取最新拉单记录
     *
     * @return 最新拉单记录
     */
    private BetslipsAg getBetsLipsAg() {
        return betslipsAgServiceImpl.lambdaQuery()
                .orderByDesc(BetslipsAg::getRecalcuTime)
                .last("limit 1")
                .one();
    }

    /**
     * 拉单-開牌結果params
     *
     * @param gameCode 游戏类型
     * @param gameType 游戏局号
     * @return reqParams
     */
    private String buildGetRoundersParams(String gameType, String gameCode, String startTime, String endTime) {
        String[] keyValues = {config.getBetSlipsCagent(), startTime, endTime, gameType, gameCode, "1", "100"};
        StringBuilder textParams = new StringBuilder();
        textParams.append("?");
        textParams.append(AgLiveRequestParameter.BetSlipsReqKey.cagent).append("=").append(keyValues[0]);
        textParams.append("&").append(AgLiveRequestParameter.BetSlipsReqKey.startdate).append("=").append(keyValues[1]);
        textParams.append("&").append(AgLiveRequestParameter.BetSlipsReqKey.enddate).append("=").append(keyValues[2]);
        textParams.append("&").append(AgLiveRequestParameter.BetSlipsReqKey.gametype).append("=").append(keyValues[3]);
        textParams.append("&").append(AgLiveRequestParameter.BetSlipsReqKey.gamecode).append("=").append(keyValues[4]);
        textParams.append("&").append(AgLiveRequestParameter.BetSlipsReqKey.page).append("=").append(keyValues[5]);
        textParams.append("&").append(AgLiveRequestParameter.BetSlipsReqKey.perpage).append("=").append(keyValues[6]);
        // getMd5Values
        textParams.append("&").append(AgUrlEnum.AGLIVE_GETROUNDSRES.getKey()).append("=").append(getOrdersParamsMd5AfterValues(keyValues));
        return textParams.toString();
    }

    /**
     * 拉单-获取開牌結果
     *
     * @param id sp_betslips_ag表主键ID
     * @return
     */
    public List<AgLiveResponse.RoundersXml> getRounders(Long id) {
        List<AgLiveResponse.RoundersXml> roundersXmlList = null;
        BetslipsAg po = betslipsAgServiceImpl.getById(id);
        ZonedDateTime startZonedDateTime = po.getBetTime().toInstant()
                .atZone(DateNewUtils.getZoneId("UTC", "+8"))
                .withZoneSameInstant(DateNewUtils.getZoneId("GMT", "-4"));
        ZonedDateTime endZonedDateTime = startZonedDateTime.plusDays(1);
        String startTime = startZonedDateTime.format(DateNewUtils.getDateTimeFormatter(DateNewUtils.Format.yyyy_MM_dd_HH_mm_ss));
        String endTime = endZonedDateTime.format(DateNewUtils.getDateTimeFormatter(DateNewUtils.Format.yyyy_MM_dd_HH_mm_ss));
        String params = buildGetRoundersParams(po.getGameType(), po.getGameCode(), startTime, endTime);
        String result = send(config.getBetSlipsUrl() + AgUrlEnum.AGLIVE_GETROUNDSRES.getMethod(), params);
        AgLiveResponse.AgGetRoundersResult roundersResult = XmlBuilder.xmlStrToObject(result, AgLiveResponse.AgGetRoundersResult::new);
        if (null != roundersResult && "0".equals(roundersResult.getInfo())) {
            roundersXmlList = updateRounders(roundersResult, id);
        }
        return roundersXmlList;
    }

    private List<AgLiveResponse.RoundersXml> updateRounders(AgLiveResponse.AgGetRoundersResult roundersResult, Long id) {
        List<AgLiveResponse.RoundersXml> xmlList = roundersResult.getRow();
        if (Optional.ofNullable(xmlList).isEmpty()) {
            return xmlList;
        }
        String rounders = JSON.toJSONString(xmlList);
        BetslipsAg betslipsAg = new BetslipsAg();
        betslipsAg.setId(id);
        betslipsAg.setRounders(rounders);
        betslipsAgServiceImpl.updateById(betslipsAg);
        return xmlList;
    }

    /**
     * 添加拉单异常表
     *
     * @param category      状态:0-三方异常 1-数据异常
     * @param requestParams 请求参数
     * @param exceptionInfo 异常信息:三方-返回数据 数据-异常处理
     */
    private void addBetSlipsException(Integer category, String requestParams, String exceptionInfo) {
        commonPersistence.addBetSlipsException(BasePlatParam.GAME.AG_LIVE.getCode(), requestParams, category, exceptionInfo, 0);
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
     * @param tmpUrl    URL
     * @param reqParams reqParams
     * @return String
     */
    private String pullSend(String tmpUrl, String reqParams) {
        String result = "";
        try {
            result = send(tmpUrl, reqParams);
        } catch (Exception e) {
            log.error(MODEL + CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0.getMsg() + ":" + e.toString());
            buildCodeInfo(e.toString());
        }
        return result;
    }

    /**
     * 根据起始、结束时间 生成补单信息
     * <p>
     * AG:
     * 1. 补单可拉取 最长多少天前的数据  			小于等于14天
     * 2. 每次补单 时间跨度(ID)  开始-结束时间		小于等于10分钟
     * 3. 补单接口  间隔时间						大于等于15秒
     * 4. 补单最大数据量(条)					    有分页<= 100 and >=500条
     *
     * @param dto dto.start 开始时间 dto.end结束时间
     */
    @Override
    public void genSupplementsOrders(@NotNull PlatFactoryParams.GenSupplementsOrdersReqDto dto) {
        // 1. 补单可拉取 最长多少天前的数据  			小于等于14天
        long preDays = 14L;
        // 2. 每次补单 时间跨度(ID)  开始-结束时间		小于等于10分钟
        long startEndRange = 10L;

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime start = DateNewUtils.utc8Zoned(dto.getStart());
        ZonedDateTime end = DateNewUtils.utc8Zoned(dto.getEnd());
        if (start.compareTo(now.minusDays(preDays)) < 0 || start.compareTo(end) >= 0) {
            throw new BusinessException(CodeInfo.PLAT_BET_SLIPS_SUPPLE_OVER_14_DAYS);
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
     * 修改时区GMT-4
     *
     * @param startZonedDateTime 开始时间
     * @param endZonedDateTime   结束时间
     * @return 请求参数
     */
    private String buildPullBetReqParamsWithZoneSameInstant(ZonedDateTime startZonedDateTime, ZonedDateTime endZonedDateTime) {
        startZonedDateTime = startZonedDateTime.withZoneSameInstant(DateNewUtils.getZoneId("GMT", "-4"));
        endZonedDateTime = endZonedDateTime.withZoneSameInstant(DateNewUtils.getZoneId("GMT", "-4"));
        return buildPullBetReqParams(startZonedDateTime, endZonedDateTime);
    }

    /**
     * 封装拉单请求参数
     *
     * @param startZonedDateTime 开始时间
     * @param endZonedDateTime   结束时间
     * @return 请求参数
     */
    private String buildPullBetReqParams(ZonedDateTime startZonedDateTime, ZonedDateTime endZonedDateTime) {
        // 拉单开始、结束时间(派彩时间GMT-4)
        String startTime = startZonedDateTime.format(DateNewUtils.getDateTimeFormatter(DateNewUtils.Format.yyyy_MM_dd_HH_mm_ss));
        String endTime = endZonedDateTime.format(DateNewUtils.getDateTimeFormatter(DateNewUtils.Format.yyyy_MM_dd_HH_mm_ss));
        log.info("时间范围[" + startTime + " - " + endTime + "]");
        return buildGetOrdersParams(startTime, endTime, 1, 500);
    }

    /**
     * 数据批量持久化拉单数据
     *
     * @param responseBody responseBody
     */
    private void saveOrUpdateBatch(String responseBody) {
        AgLiveResponse.AgGetOrdersResult agGetOrdersResult = XmlBuilder.xmlStrToObject(responseBody, AgLiveResponse.AgGetOrdersResult::new);
        if (null != agGetOrdersResult && "0".equals(agGetOrdersResult.getInfo())) {
            processAgGetOrdersResult(agGetOrdersResult);
        } else {
            // 状态:0-三方异常
            buildCodeInfo(responseBody);
        }
    }

    /**
     * 补充注单信息
     *
     * @param dto dto.requestInfo 补单请求参数
     */
    @Override
    public void betsRecordsSupplemental(@NotNull PlatFactoryParams.BetsRecordsSupplementReqDto dto) {
        try {
            String resBody = pullSend(config.getBetSlipsUrl() + AgUrlEnum.AGLIVE_GETORDERS.getMethod(), dto.getRequestInfo());
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
