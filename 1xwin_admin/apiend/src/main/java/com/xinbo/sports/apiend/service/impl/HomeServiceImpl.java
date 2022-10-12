package com.xinbo.sports.apiend.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinbo.sports.apiend.cache.redis.HomeCache;
import com.xinbo.sports.apiend.io.bo.HomeParams;
import com.xinbo.sports.apiend.io.bo.HomeParams.NoticeResDto;
import com.xinbo.sports.apiend.io.dto.mapper.BannerReqDto;
import com.xinbo.sports.apiend.io.dto.mapper.BannerResDto;
import com.xinbo.sports.apiend.service.IHomeService;
import com.xinbo.sports.dao.generator.po.Notice;
import com.xinbo.sports.dao.generator.service.impl.NoticeServiceImpl;
import com.xinbo.sports.plat.io.dto.base.BetslipsDetailDto;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.service.cache.redis.PromotionsCache;
import com.xinbo.sports.service.common.Constant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.dto.BaseParams;
import com.xinbo.sports.service.thread.ThreadHeaderLocalData;
import com.xinbo.sports.utils.BeanConvertUtils;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;
import com.xinbo.sports.utils.components.response.CodeInfo;
import io.jsonwebtoken.lang.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.xinbo.sports.service.common.Constant.*;
import static java.util.Objects.nonNull;

/**
 * @author: David
 * @date: 06/04/2020
 * @description:
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HomeServiceImpl implements IHomeService {
    private final GameServiceImpl gameServiceImpl;
    private final NoticeServiceImpl noticeServiceImpl;
    private final HomeCache homeCache;
    private final PromotionsCache promotionsCache;
    private final ConfigCache configCache;

    /**
     * Init 获取系统配置相关信息
     *
     * @return 出参
     */
    @Override
    public List<HomeParams.InitResDto> init(HttpServletRequest request) {
        var resDto = homeCache.getInitResCache();
        // 红包是否显示；0-不显示，1-显示
        var redEnvelopeShow = Constant.SUCCESS;
        var promotionsOptional = promotionsCache.getPromotionsListCache().parallelStream()
                .filter(promotions -> promotions.getCode().equals(Constant.RED_ENVELOPE) && promotions.getStatus().equals(Constant.SUCCESS))
                .findFirst();
        if (promotionsOptional.isEmpty()) {
            redEnvelopeShow = Constant.FAIL;
        }
        for (var o : resDto) {
            if ("download".equals(o.getTitle()) && StringUtils.isNotBlank(o.getValue())) {
                JSONObject jsonObject = parseObject(o.getValue());
                String host = request.getHeader("Host");
                var link = subStringLink(host);
                String filterValue = jsonObject.getString("filter");
                if (link.equals(filterValue)) {
                    jsonObject.put("status", 0);
                    o.setValue(jsonObject.toJSONString());
                }
            }
            if (PLAT_LOGO.equals(o.getTitle()) || DOWNLOAD_LOGO.equals(o.getTitle()) || TELEGRAM_URL.equals(o.getTitle()) ||
                    SKYPE_URL.equals(o.getTitle()) || WHATSAPP_URL.equals(o.getTitle()) || LINE_URL.equals(o.getTitle())) {
                o.setValue(o.getValue().startsWith("http")? o.getValue():configCache.getStaticServer() + o.getValue());
            }
            if (LINE.equals(o.getTitle())) {
                var lineJson = new JSONObject();
                lineJson.put(QRCODE, configCache.getConfigByTitle(LINE_URL).startsWith("http")? configCache.getConfigByTitle(LINE_URL):configCache.getStaticServer() + configCache.getConfigByTitle(LINE_URL));
                lineJson.put(URL, o.getValue());
                o.setValue(lineJson.toJSONString());
            }
        }
        var redEnvelopeDto = new HomeParams.InitResDto();
        redEnvelopeDto.setTitle(Constant.RED_ENVELOPE_ACTIVITY);
        redEnvelopeDto.setValue(redEnvelopeShow + "");
        resDto.add(redEnvelopeDto);
        return resDto;
    }

    /**
     * Index 首页
     *
     * @return 出参
     */
    @Override
    public HomeParams.IndexResDto index() {
        // 获取公告
        ReqPage<HomeParams.NoticeReqDto> noticePage = new ReqPage<>();
        noticePage.setPage(1, 10);
        noticePage.setData(new HomeParams.NoticeReqDto(1));
        //获取赛事预告
        ReqPage<BetslipsDetailDto.SportScheduleReqDto> scheduleReqPage = new ReqPage<>();
        scheduleReqPage.setPage(1, 3);
        scheduleReqPage.setData(new BetslipsDetailDto.SportScheduleReqDto(null, 1, null));
        return HomeParams.IndexResDto.builder()
                .notice(notice(noticePage))
                .game(game())
                .matchPreview(gameServiceImpl.getSportSchedule(scheduleReqPage))
                .banner(banner(BannerReqDto.builder().category(1).build()))
                .build();
    }

    /**
     * 获取游戏列表
     *
     * @return 游戏列表
     */
    @Override
    public List<HomeParams.GameIndexResDto> game() {
        return gameServiceImpl.list();
    }

    /**
     * 获取Banner列表
     *
     * @param reqDto Banner入参
     * @return Banner 列表信息
     */
    @Override
    public List<BannerResDto> banner(@NotNull BannerReqDto reqDto) {
        return homeCache.getBannerCache(reqDto.getCategory());
    }

    /**
     * 获取公告列表
     *
     * @param dto 入参
     * @return 公告列表
     */
    @Override
    public ResPage<NoticeResDto> notice(@NotNull ReqPage<HomeParams.NoticeReqDto> dto) {
        ResPage<NoticeResDto> resPage = new ResPage<>();
        BaseParams.HeaderInfo headerInfo = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        LambdaQueryWrapper<Notice> query = new LambdaQueryWrapper<>();
        ArrayList<Object> objects = new ArrayList<>();
        query.eq(Notice::getStatus, 1)
                .eq(Notice::getCategory, dto.getData().getCategory())
                .orderByDesc(Notice::getCreatedAt);
        if (dto.getData().getCategory() == 2) {
            List<Notice> collect = noticeServiceImpl.list(query).stream()
                    .filter(notice -> notice.getUids().contains(headerInfo.getId().toString()))
                    .collect(Collectors.toList());
            collect.forEach((Notice notice) -> objects.add(notice.getId()));
            if (!Collections.isEmpty(objects)) {
                var page = noticeServiceImpl.page(dto.getPage(), query.in(Notice::getId, objects).orderByDesc(Notice::getCreatedAt));
                Page<NoticeResDto> result = BeanConvertUtils.copyPageProperties(page, NoticeResDto::new);
                resPage = ResPage.get(result);
            } else {
                resPage.setPages(1);
                resPage.setTotal(0);
                resPage.setList(new ArrayList<>());
            }
        } else {
            var page = noticeServiceImpl.page(dto.getPage(), query);
            Page<NoticeResDto> result = BeanConvertUtils.copyPageProperties(page, NoticeResDto::new);
            resPage = ResPage.get(result);
        }
        return resPage;
    }

    @Override
    public Boolean delCache(String cacheKey, String cacheField) {
        return homeCache.delCache(cacheKey, cacheField);
    }

    @Override
    public Boolean delNotice(HomeParams.DelNoticeReqDto dto) {
        BaseParams.HeaderInfo headerInfo = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        List<String> collect = Arrays.stream(dto.getId().split(",")).collect(Collectors.toList());
        boolean update = noticeServiceImpl.lambdaUpdate().setSql("uids=replace(uids,'" + headerInfo.getId() + ",',''), uids=replace(uids,'," + headerInfo.getId() + "',''),uids=replace(uids,'" + headerInfo.getId() + "','')").in(nonNull(dto.getId()), Notice::getId, collect).eq(Notice::getCategory, 2).update();
        if (Boolean.TRUE.equals(update)) {
            return true;
        } else {
            throw new BusinessException(CodeInfo.UPDATE_ERROR);
        }
    }

    /**
     * 截取原域名
     * 原http://pch5.sp.com或https://pch5.sp.com -> sp.com
     * 原http://www.pch5.sp.com或https://www.pch5.sp.com -> sp.com
     *
     * @param link 原域名
     * @return sp.com
     */
    private String subStringLink(String link) {
        log.info("======before link====>" + link);
        if (StringUtils.isNotBlank(link)) {
            String[] split = link.split("[.]");
            if (split.length >= 2) {
                link = split[split.length - 2] + "." + split[split.length - 1];
            }
        }
        log.info("======after link====>" + link);
        return link;
    }
}
