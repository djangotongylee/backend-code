package com.xinbo.sports.backend.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.xinbo.sports.backend.io.dto.System;
import com.xinbo.sports.backend.service.ISystemService;
import com.xinbo.sports.dao.generator.po.AgentCommissionRate;
import com.xinbo.sports.dao.generator.po.Banner;
import com.xinbo.sports.dao.generator.po.Config;
import com.xinbo.sports.dao.generator.service.AgentCommissionRateService;
import com.xinbo.sports.dao.generator.service.BannerService;
import com.xinbo.sports.dao.generator.service.ConfigService;
import com.xinbo.sports.plat.io.bo.FuturesLotteryRequestParameter;
import com.xinbo.sports.plat.service.impl.futures.FuturesLotteryServiceImpl;
import com.xinbo.sports.service.base.FastDFSTemplate;
import com.xinbo.sports.service.cache.KeyConstant;
import com.xinbo.sports.service.cache.redis.BannerCache;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.service.common.Constant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.enums.BaseEnum;
import com.xinbo.sports.utils.BeanConvertUtils;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.JedisUtil;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;
import com.xinbo.sports.utils.components.response.CodeInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.alibaba.fastjson.JSON.toJSONString;
import static com.xinbo.sports.service.common.Constant.*;
import static java.util.Objects.nonNull;

/**
 * <p>
 * 系统设置
 * </p>
 *
 * @author andy
 * @since 2020/10/5
 */
@Service
public class SystemServiceImpl implements ISystemService {
    @Resource
    private BannerService bannerServiceImpl;
    @Resource
    private ConfigCache configCache;
    @Resource
    private BannerCache bannerCache;
    @Resource
    private FastDFSTemplate fastDFSTemplate;
    @Resource
    private FuturesLotteryServiceImpl futuresLotteryServiceImpl;
    @Resource
    private ConfigService configServiceImpl;
    @Resource
    private JedisUtil jedisUtil;
    @Resource
    private AgentCommissionRateService agentCommissionRateServiceImpl;

    private static final String TMP_HREF = "{}";

    @Override
    public List<System.BannerListResBody> bannerList(System.BannerListReqBody reqBody) {
        // 类型:1-首页 2-活动
        Integer category = null;
        var lang = BaseEnum.LANG.EN.getValue();
        if (null != reqBody) {
            category = reqBody.getCategory();
            lang = reqBody.getLang();
        }
        List<Banner> list = bannerServiceImpl.lambdaQuery()
                .eq(null != category, Banner::getCategory, category)
                .list();
        List<System.BannerListResBody> resBodyList = new ArrayList<>();
        if (Optional.ofNullable(list).isEmpty() || list.isEmpty()) {
            return resBodyList;
        }
        String staticServer = configCache.getStaticServer();
        for (Banner x : list) {
            System.BannerListResBody bannerListResBody = BeanConvertUtils.beanCopy(x, System.BannerListResBody::new);

            if (TMP_HREF.equals(x.getHref()) || TMP_HREF.equals(parseObject(x.getHref()).getString(lang))) {
                bannerListResBody.setHref(null);
            } else {
                bannerListResBody.setHref(parseObject(x.getHref()).getString(lang));
            }
            var imgJson = parseObject(x.getImg());
            if (null != imgJson) {
                var h5 = (JSONObject) imgJson.get(TERMINAL_H5);
                if (null != h5) {
                    bannerListResBody.setImgH5(h5.getString(lang).startsWith("http")?h5.getString(lang):staticServer + h5.getString(lang));
                }
                var pc = (JSONObject) imgJson.get(TERMINAL_PC);
                if (null != pc) {
                    bannerListResBody.setImgPC(staticServer + pc.getString(lang));
                }
            }
            resBodyList.add(bannerListResBody);
        }
        return resBodyList;
    }

    @Override
    public boolean addBanner(System.AddBannerReqBody reqBody) {
        if (null == reqBody || StringUtils.isBlank(reqBody.getLang())) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }

        // H5
        var terminal = new JSONObject();
        if (StringUtils.isNotBlank(reqBody.getImgH5())) {
            var imgJsonH5 = new JSONObject();
            imgJsonH5.put(reqBody.getLang(), reqBody.getImgH5());
            terminal.put(TERMINAL_H5, imgJsonH5);
        }
        // PC
        if (StringUtils.isNotBlank(reqBody.getImgPC())) {
            var imgJsonPC = new JSONObject();
            imgJsonPC.put(reqBody.getLang(), reqBody.getImgPC());
            terminal.put(TERMINAL_PC, imgJsonPC);
        }

        var hrefJson = new JSONObject();
        hrefJson.put(reqBody.getLang(), TMP_HREF.equals(reqBody.getHref()) ? "" : reqBody.getHref());
        Banner banner = BeanConvertUtils.beanCopy(reqBody, Banner::new);
        banner.setHref(hrefJson.toJSONString());
        banner.setImg(terminal.toJSONString());
        int now = DateNewUtils.now();
        banner.setCreatedAt(now);
        banner.setUpdatedAt(now);
        boolean flag = bannerServiceImpl.save(banner);
        if (flag) {
            bannerCache.delBanner();
        }
        return flag;
    }

    @Override
    public boolean updateBanner(System.UpdateBannerReqBody reqBody) {
        if (null == reqBody || null == reqBody.getId()) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        String lang = reqBody.getLang();
        var oldBanner = bannerServiceImpl.getById(reqBody.getId());
        var imgJson = parseObject(oldBanner.getImg());
        String staticServer = configCache.getStaticServer();

        //修改IMG地址->H5
        if (StringUtils.isNotBlank(reqBody.getImgH5()) && StringUtils.isNotBlank(lang)) {
            var img = reqBody.getImgH5();
            if (img.contains(staticServer)) {
                img = img.substring(staticServer.length());
            }
            var val = (JSONObject) imgJson.get(TERMINAL_H5);
            if (null == val) {
                val = new JSONObject();
            }
            val.put(lang, img);
            imgJson.put(TERMINAL_H5, val);
        }
        //修改IMG地址->PC
        if (StringUtils.isNotBlank(reqBody.getImgPC()) && StringUtils.isNotBlank(lang)) {
            var img = reqBody.getImgPC();
            if (img.contains(staticServer)) {
                img = img.substring(staticServer.length());
            }
            var val = (JSONObject) imgJson.get(TERMINAL_PC);
            if (null == val) {
                val = new JSONObject();
            }
            val.put(lang, img);
            imgJson.put(TERMINAL_PC, val);
        }
        //修改href地址
        if (nonNull(reqBody.getHref()) && nonNull(reqBody.getLang())) {
            var hrefJson = parseObject(oldBanner.getHref());
            hrefJson.put(reqBody.getLang(), TMP_HREF.equals(reqBody.getHref()) ? "" : reqBody.getHref());
            reqBody.setHref(hrefJson.toJSONString());
        }
        Banner banner = BeanConvertUtils.beanCopy(reqBody, Banner::new);
        banner.setImg(imgJson.toJSONString());
        banner.setUpdatedAt(DateNewUtils.now());
        boolean flag = bannerServiceImpl.saveOrUpdate(banner);
        if (flag) {
            bannerCache.delBanner();
        }
        return flag;
    }

    @Override
    public boolean delBanner(System.DelBannerReqBody reqBody) {
        if (null == reqBody || null == reqBody.getId()) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        Banner banner = bannerServiceImpl.getById(reqBody.getId());
        if (null == banner) {
            return false;
        }
        boolean flag = bannerServiceImpl.removeById(reqBody.getId());
        if (flag) {
            bannerCache.delBanner();
//            var imgJson = parseObject(banner.getImg());
//            imgJson.values().forEach(value -> {
//                var json = (JSONObject)value;
//                json.values().forEach(x -> {
//                    fastDFSTemplate.deleteFile(x + "");
//                });
//            });
        }
        return flag;
    }

    @Override
    public ResPage<System.GetOpenPresetListResBody> getOpenPresetList(ReqPage<System.GetOpenPresetListReqBody> reqBody) {
        if (null == reqBody) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        ReqPage<FuturesLotteryRequestParameter.GetOpenPresetListReqBody> platReqBody = BeanConvertUtils.beanCopy(reqBody, ReqPage::new);
        if (null != reqBody.getData()) {
            platReqBody.setData(BeanConvertUtils.beanCopy(reqBody.getData(), FuturesLotteryRequestParameter.GetOpenPresetListReqBody::new));
        }
        ResPage<FuturesLotteryRequestParameter.GetOpenPresetListResBody> page = futuresLotteryServiceImpl.getOpenPresetList(platReqBody);
        ResPage<System.GetOpenPresetListResBody> resBodyResPage = BeanConvertUtils.beanCopy(page, ResPage::new);
        resBodyResPage.setList(BeanConvertUtils.copyListProperties(page.getList(), System.GetOpenPresetListResBody::new));
        return resBodyResPage;
    }

    @Override
    public boolean saveOrUpdateOpenPreset(System.SaveOrUpdateOpenPresetReqBody reqBody) {
        if (null == reqBody) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        return futuresLotteryServiceImpl.saveOrUpdateOpenPreset(BeanConvertUtils.beanCopy(reqBody, FuturesLotteryRequestParameter.SaveOrUpdateOpenPresetReqBody::new));
    }

    @Override
    public boolean deleteOpenPreset(System.DeleteOpenPresetReqBody reqBody) {
        if (null == reqBody || null == reqBody.getId()) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        return futuresLotteryServiceImpl.deleteOpenPreset(BeanConvertUtils.beanCopy(reqBody, FuturesLotteryRequestParameter.DeleteOpenPresetReqBody::new));
    }

    @Override
    public List<System.InitResDto> init() {
        // 类型: 1-前端 2-后台
        return configCache.getConfigCacheByShowApp(2)
                .stream()
                .map(o -> BeanConvertUtils.beanCopy(o, System.InitResDto::new))
                .collect(Collectors.toList());
    }

    @Override
    public List<System.ExportOpenPresetListResBody> exportOpenPresetList(System.ExportOpenPresetListReqBody reqBody) {
        if (null == reqBody) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        var list = futuresLotteryServiceImpl.exportOpenPresetList(BeanConvertUtils.beanCopy(reqBody, FuturesLotteryRequestParameter.ExportOpenPresetListReqBody::new));
        return BeanConvertUtils.copyListProperties(list, System.ExportOpenPresetListResBody::new);
    }

    @Override
    public boolean saveOrUpdateBatchOpenPreset(System.SaveOrUpdateBatchOpenPresetReqBody reqBody) {
        if (null == reqBody) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        var nums = reqBody.getNums();
        if (nums <= 0 || nums > 480) {
            throw new BusinessException(CodeInfo.OPEN_PRESET_NUMS_INVALID);
        }
        return futuresLotteryServiceImpl.saveOrUpdateBatchOpenPreset(BeanConvertUtils.beanCopy(reqBody, FuturesLotteryRequestParameter.SaveOrUpdateBatchOpenPresetReqBody::new));
    }

    @Override
    public System.GetLotteryActionNoResBody getLotteryActionNo(System.GetLotteryActionNoReqBody reqBody) {
        if (null == reqBody) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        var entity = futuresLotteryServiceImpl.getLotteryActionNo(BeanConvertUtils.beanCopy(reqBody, FuturesLotteryRequestParameter.GetLotteryActionNoReqBody::new));
        return BeanConvertUtils.beanCopy(entity, System.GetLotteryActionNoResBody::new);
    }

    /**
     * 根据语言获取Banner信息
     *
     * @param reqDto 请求参数实例
     */
    @Override
    public System.BannerByLangResDto geBannersByLang(System.BannerByLangReqDto reqDto) {
        var lang = reqDto.getLang();
        var staticServer = configCache.getStaticServer();
        var bannerObj = bannerServiceImpl.getById(reqDto.getId());
        String href = null;

        if (!(TMP_HREF.equals(bannerObj.getHref()) || TMP_HREF.equals(parseObject(bannerObj.getHref()).getString(lang)))) {
            href = parseObject(bannerObj.getHref()).getString(lang);
        }
        String imgH5 = null;
        String imgPC = null;
        var imgJson = parseObject(bannerObj.getImg());
        if (null != imgJson) {
            var h5 = (JSONObject) imgJson.get(TERMINAL_H5);
            if (null != h5) {
                imgH5 = h5.getString(lang).startsWith("http")?h5.getString(lang):staticServer + h5.getString(lang);
            }
            var pc = (JSONObject) imgJson.get(TERMINAL_PC);
            if (null != pc) {
                imgPC = staticServer + pc.getString(lang);
            }
        }
        return System.BannerByLangResDto.builder().imgH5(imgH5).imgPC(imgPC).href(href).build();
    }

    /**
     * 查询佣金规则
     *
     * @return System.CommissionRule
     */
    @Override
    public System.CommissionRuleDto getCommissionRule() {
        var agentList = agentCommissionRateServiceImpl.list();
        var commissionRateList = BeanConvertUtils.copyListProperties(agentList, System.CommissionRateDto::new, (source, target) ->
                target.setAgentLevelRate(source.getAgentLevelRate().multiply(new BigDecimal(100)))
        );
        var commissionRule = parseObject(configCache.getConfigByTitle(Constant.COMMISSION_RULE)).toJavaObject(System.CommissionRuleDto.class);
        commissionRule.setCommissionRateList(commissionRateList);
        var minDrawCoinJson = parseObject(configCache.getConfigByTitle(MIN_DRAW_COIN));
        commissionRule.setMinCoin(minDrawCoinJson.getBigDecimal(MIN_COIN));
        var jsonArray = minDrawCoinJson.getJSONArray(COIN_RANGE);
        commissionRule.setCoinRangeList(jsonArray.toJavaList(Integer.class));
        return commissionRule;
    }


    /**
     * 修改佣金规则
     *
     * @param reqDto 请求参数
     * @return Boolean
     */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateCommissionRule(System.CommissionRuleDto reqDto) {
        var ruleJson = parseObject(configCache.getConfigByTitle(Constant.COMMISSION_RULE));
        var configJson = parseObject(toJSONString(reqDto));
        configJson.put(Constant.OLD_SETTLE_TYPE, ruleJson.getInteger(Constant.SETTLE_TYPE));
        configJson.put(Constant.OLD_EFFECT_TYPE, ruleJson.getInteger(Constant.EFFECT_TYPE));
        //生成生效日期
        ZonedDateTime zoneNow = DateNewUtils.utc8Zoned(DateNewUtils.now());
        var effectDate = DateNewUtils.utc8Str(zoneNow.plusDays(1), DateNewUtils.Format.yyyy_MM_dd);
        if (Constant.EFFECT_MONTH.equals(reqDto.getEffectType())) {
            var firstDay = zoneNow.plusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
            effectDate = DateNewUtils.utc8Str(firstDay, DateNewUtils.Format.yyyy_MM_dd);
        }
        configJson.put(Constant.EFFECT_DATE, effectDate);
        var updateFlag = configServiceImpl.lambdaUpdate()
                .set(Config::getValue, configJson.toJSONString())
                .set(Config::getUpdatedAt, DateNewUtils.now())
                .eq(Config::getTitle, Constant.COMMISSION_RULE)
                .update();
        //修改佣金比例
        var commissionFlag = true;
        var commissionRateList = reqDto.getCommissionRateList();
        if (!CollectionUtils.isEmpty(commissionRateList)) {
            var agentRateList = BeanConvertUtils.copyListProperties(commissionRateList, AgentCommissionRate::new, (source, target) ->
                    target.setAgentLevelRate(source.getAgentLevelRate().divide(new BigDecimal(100), 4, RoundingMode.DOWN))
            );
            commissionFlag = agentCommissionRateServiceImpl.updateBatchById(agentRateList);
        }
        if (updateFlag) {
            jedisUtil.hdel(KeyConstant.CONFIG_HASH, Constant.COMMISSION_RULE);
        }
        //修改提款配置
        var minDrawCoinJson = parseObject(configCache.getConfigByTitle(MIN_DRAW_COIN));
        minDrawCoinJson.put(MIN_COIN, reqDto.getMinCoin());
        minDrawCoinJson.put(COIN_RANGE, reqDto.getCoinRangeList());
        var minDrawFlag = configServiceImpl.lambdaUpdate()
                .set(Config::getValue, minDrawCoinJson.toJSONString())
                .set(Config::getUpdatedAt, DateNewUtils.now())
                .eq(Config::getTitle, Constant.MIN_DRAW_COIN)
                .update();
        jedisUtil.hdel(KeyConstant.CONFIG_HASH, Constant.MIN_DRAW_COIN);
        return updateFlag && commissionFlag && minDrawFlag;
    }

    /**
     * 查询平台配置
     *
     * @return RegisterLoginConfigDto
     */
    @Override
    public System.PlatConfigDto getPlatConfig() {
        var platConfigDto = new System.PlatConfigDto();
        var registerLoginJson = parseObject(configCache.getConfigByTitle(Constant.REGISTER_LOGIN_CONFIG));
        platConfigDto.setRegisterMobile(registerLoginJson.getInteger(REGISTER_MOBILE));
        platConfigDto.setRegisterInviteCode(registerLoginJson.getInteger(REGISTER_INVITE_CODE));
        platConfigDto.setLoginType(registerLoginJson.getString(LOGIN_TYPE));
        var verificationOfGoogle = configCache.getConfigByTitle(Constant.VERIFICATION_OF_GOOGLE);
        platConfigDto.setVerificationOfGoogle(verificationOfGoogle);
        platConfigDto.setTitle(configCache.getConfigByTitle(TITLE));
        platConfigDto.setPlatLogo(configCache.getConfigByTitle(PLAT_LOGO).startsWith("http")?  configCache.getConfigByTitle(PLAT_LOGO):configCache.getStaticServer() + configCache.getConfigByTitle(PLAT_LOGO));
        platConfigDto.setLang(List.of(configCache.getConfigByTitle(LANG).split(",")));
        platConfigDto.setStaticServer(parseObject(configCache.getConfigByTitle(STATIC_SERVER)).getString(URL));
        platConfigDto.setWsServer(configCache.getConfigByTitle(WS_SERVER));
        platConfigDto.setSms(configCache.getConfigByTitle(SMS));
        platConfigDto.setDownload(parseObject(configCache.getConfigByTitle(DOWNLOAD)).getString(URL));
        platConfigDto.setDownloadShow(parseObject(configCache.getConfigByTitle(DOWNLOAD)).getString(STATUS));
        platConfigDto.setDownloadLogo(configCache.getConfigByTitle(DOWNLOAD_LOGO).startsWith("http")?  configCache.getConfigByTitle(DOWNLOAD_LOGO):configCache.getStaticServer() + configCache.getConfigByTitle(DOWNLOAD_LOGO));
        return platConfigDto;
    }

    /**
     * 修改平台配置
     *
     * @param reqDto 修改参数
     * @return boolean
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updatePlatConfig(System.PlatConfigDto reqDto) {
        var configJson = parseObject(toJSONString(reqDto));
        configJson.put(REGISTER_VERIFICATION_CODE, reqDto.getRegisterMobile());
        var updateMap = new HashMap<String, String>();
        var registerLoginJson = parseObject(configCache.getConfigByTitle(Constant.REGISTER_LOGIN_CONFIG));
        registerLoginJson.put(REGISTER_MOBILE, reqDto.getRegisterMobile());
        registerLoginJson.put(REGISTER_INVITE_CODE, reqDto.getRegisterInviteCode());
        registerLoginJson.put(LOGIN_TYPE, reqDto.getLoginType());
        registerLoginJson.put(VERIFICATION_OF_GOOGLE, reqDto.getVerificationOfGoogle());
        updateMap.put(REGISTER_VERIFICATION_CODE, registerLoginJson.toJSONString());
        updateMap.put(TITLE, reqDto.getTitle());
        updateMap.put(PLAT_LOGO, filterStaticServer(reqDto.getPlatLogo()));
        updateMap.put(LANG, String.join(",", reqDto.getLang()));
        var staticServerJson = parseObject(configCache.getConfigByTitle(STATIC_SERVER));
        staticServerJson.put(URL, reqDto.getStaticServer());
        updateMap.put(STATIC_SERVER, staticServerJson.toJSONString());
        updateMap.put(WS_SERVER, reqDto.getWsServer());
        updateMap.put(SMS, reqDto.getSms());
        var downloadJson = parseObject(configCache.getConfigByTitle(DOWNLOAD));
        downloadJson.put(URL, reqDto.getDownload());
        downloadJson.put(STATUS, reqDto.getDownloadShow());
        updateMap.put(DOWNLOAD, downloadJson.toJSONString());
        updateMap.put(DOWNLOAD_LOGO, filterStaticServer(reqDto.getDownloadLogo()));
        updateMap.put(REGISTER_LOGIN_CONFIG, configJson.toJSONString());
        updateMap.put(VERIFICATION_OF_GOOGLE, reqDto.getVerificationOfGoogle());
        //循环修改配置
        updateMap.forEach((key, value) ->
                configServiceImpl.lambdaUpdate()
                        .set(nonNull(value), Config::getValue, value)
                        .set(Config::getUpdatedAt, DateNewUtils.now())
                        .eq(Config::getTitle, key)
                        .update()
        );
        jedisUtil.del(KeyConstant.CONFIG_HASH);
        return true;
    }

    /**
     * 修改时过滤静态服务器地址
     *
     * @param url 地址
     * @return String
     */
    public String filterStaticServer(String url) {
        var staticServer = parseObject(configCache.getConfigByTitle(STATIC_SERVER)).getString(URL);
        if (Strings.isNotEmpty(url) && url.startsWith(staticServer)) {
            return url.replace(staticServer, "");
        }
        return url;
    }

    /**
     * 查询时添加url
     *
     * @param url 地址
     * @return string
     */
    public String addStaticService(String url) {
        var staticServer = parseObject(configCache.getConfigByTitle(STATIC_SERVER)).getString(URL);
        if (Strings.isNotEmpty(url)) {
            return staticServer + url;
        }
        return url;
    }

    /**
     * 预设开奖概率分布
     *
     * @param reqDto reqDto
     * @return OpenRateDistributeReqDto
     */
    @Override
    public System.OpenRateDistributeResDto getOpenRateDistribute(System.OpenRateDistributeReqDto reqDto) {
        var entity = futuresLotteryServiceImpl.getOpenRateDistribute(BeanConvertUtils.beanCopy(reqDto, FuturesLotteryRequestParameter.OpenRateDistributeReqDto::new));
        return BeanConvertUtils.beanCopy(entity, System.OpenRateDistributeResDto::new);
    }

    /**
     * 查询在线客服配置
     *
     * @return OnlineServiceConfigDto
     */
    @Override
    public System.OnlineServiceConfigDto getOnlineServiceConfig() {
        var onlineServiceConfigDto = new System.OnlineServiceConfigDto();
        var onlineJson = parseObject(configCache.getConfigByTitle(ONLINE_SERVER));
        onlineServiceConfigDto.setPcUrl(parseObject(onlineJson.getString(PC)).getString(URL));
        onlineServiceConfigDto.setH5Url(parseObject(onlineJson.getString(H5)).getString(URL));
        onlineServiceConfigDto.setPcCategory(parseObject(onlineJson.getString(PC)).getInteger(CATEGORY));
        onlineServiceConfigDto.setH5Category(parseObject(onlineJson.getString(H5)).getInteger(CATEGORY));
        onlineServiceConfigDto.setMailKefu(configCache.getConfigByTitle(MAIL_KEFU));
        onlineServiceConfigDto.setMailHelp(configCache.getConfigByTitle(MAIL_HELP));
        onlineServiceConfigDto.setMailSuggest(configCache.getConfigByTitle(MAIL_SUGGEST));
        onlineServiceConfigDto.setMobile(configCache.getConfigByTitle(MOBILE));
        onlineServiceConfigDto.setTelegram(configCache.getConfigByTitle(TELEGRAM));
        onlineServiceConfigDto.setSkype(configCache.getConfigByTitle(SKYPE));
        onlineServiceConfigDto.setLine(configCache.getConfigByTitle(LINE));
        onlineServiceConfigDto.setWhatsapp(configCache.getConfigByTitle(WHATSAPP));
        onlineServiceConfigDto.setTelegramUrl(addStaticService(configCache.getConfigByTitle(TELEGRAM_URL)));
        onlineServiceConfigDto.setSkypeUrl(addStaticService(configCache.getConfigByTitle(SKYPE_URL)));
        onlineServiceConfigDto.setLineUrl(addStaticService(configCache.getConfigByTitle(LINE_URL)));
        onlineServiceConfigDto.setWhatsappUrl(addStaticService(configCache.getConfigByTitle(WHATSAPP_URL)));
        return onlineServiceConfigDto;
    }

    /**
     * 修改在线客服配置
     *
     * @param reqDto OnlineServiceConfigDto
     * @return Boolean
     */
    @Override
    public Boolean updateOnlineServiceConfig(System.OnlineServiceConfigDto reqDto) {
        var onlineJson = parseObject(configCache.getConfigByTitle(ONLINE_SERVER));
        var pcJson = parseObject(onlineJson.getString(PC));
        var h5Json = parseObject(onlineJson.getString(H5));
        pcJson.put(URL, reqDto.getPcUrl());
        pcJson.put(CATEGORY, reqDto.getPcCategory());
        h5Json.put(URL, reqDto.getH5Url());
        h5Json.put(CATEGORY, reqDto.getH5Category());
        onlineJson.put(PC, pcJson);
        onlineJson.put(H5, h5Json);
        var updateMap = new HashMap<String, String>();
        if (nonNull(reqDto.getPcUrl()) && nonNull(reqDto.getH5Url()) && nonNull(reqDto.getPcCategory()) && nonNull(reqDto.getH5Category())) {
            updateMap.put(ONLINE_SERVER, onlineJson.toJSONString());
        }
        updateMap.put(MAIL_KEFU, reqDto.getMailKefu());
        updateMap.put(MAIL_HELP, reqDto.getMailHelp());
        updateMap.put(MAIL_SUGGEST, reqDto.getMailSuggest());
        updateMap.put(MOBILE, reqDto.getMobile());
        updateMap.put(TELEGRAM, reqDto.getTelegram());
        updateMap.put(SKYPE, reqDto.getSkype());
        updateMap.put(LINE, reqDto.getLine());
        updateMap.put(WHATSAPP, reqDto.getWhatsapp());
        updateMap.put(TELEGRAM_URL, filterStaticServer(reqDto.getTelegramUrl()));
        updateMap.put(SKYPE_URL, filterStaticServer(reqDto.getSkypeUrl()));
        updateMap.put(LINE_URL, filterStaticServer(reqDto.getLineUrl()));
        updateMap.put(WHATSAPP_URL, filterStaticServer(reqDto.getWhatsappUrl()));
        //循环修改配置
        updateMap.forEach((key, value) ->
                configServiceImpl.lambdaUpdate()
                        .set(nonNull(value), Config::getValue, CLEAR.equals(value) ? "" : value)
                        .set(Config::getUpdatedAt, DateNewUtils.now())
                        .eq(Config::getTitle, key)
                        .update()
        );
        jedisUtil.del(KeyConstant.CONFIG_HASH);
        return true;
    }
}
