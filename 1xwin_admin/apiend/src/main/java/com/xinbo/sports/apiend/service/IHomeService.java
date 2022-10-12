package com.xinbo.sports.apiend.service;

import com.xinbo.sports.apiend.io.bo.HomeParams;
import com.xinbo.sports.apiend.io.dto.mapper.BannerReqDto;
import com.xinbo.sports.apiend.io.dto.mapper.BannerResDto;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;
import com.xinbo.sports.utils.components.response.CodeInfo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author: David
 * @date: 06/04/2020
 * @description:
 */
public interface IHomeService {
    /**
     * Init 获取系统配置相关信息
     *
     * @return 出参
     */
    List<HomeParams.InitResDto> init(HttpServletRequest request);

    /**
     * Index 首页
     *
     * @return 出参
     */
    HomeParams.IndexResDto index();

    /**
     * 获取游戏列表
     *
     * @return 游戏列表
     */
    List<HomeParams.GameIndexResDto> game();

    /**
     * 获取Banner列表
     *
     * @param reqDto Banner入参
     * @return Banner 列表信息
     */
    List<BannerResDto> banner(BannerReqDto reqDto);

    /**
     * 获取公告列表
     *
     * @param dto 入参
     * @return 公告列表
     */
    ResPage<HomeParams.NoticeResDto> notice(ReqPage<HomeParams.NoticeReqDto> dto);

    Boolean delCache(String cacheKey, String cacheField);

    Boolean delNotice(HomeParams.DelNoticeReqDto dto);
}
