package com.xinbo.sports.apiend.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import com.xinbo.sports.apiend.aop.annotation.UnCheckToken;
import com.xinbo.sports.apiend.io.bo.HomeParams;
import com.xinbo.sports.apiend.io.dto.mapper.BannerReqDto;
import com.xinbo.sports.apiend.io.dto.mapper.BannerResDto;
import com.xinbo.sports.apiend.service.impl.HomeServiceImpl;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;
import com.xinbo.sports.utils.components.response.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 * @author: David
 * @date: 19/04/2020
 * @description:
 */
@Slf4j
@RestController
@Api(tags = "首页")
@ApiSort(1)
@RequestMapping("/v1/home")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HomeController {
    private final HomeServiceImpl homeServiceImpl;

    @UnCheckToken
    @PostMapping(value = "/init")
    @ApiOperation(value = "初始化接口", notes = "初始化")
    @ApiOperationSupport(author = "David", order = 1)
    public Result<List<HomeParams.InitResDto>> init(HttpServletRequest request) {
        return Result.ok(homeServiceImpl.init(request));
    }

    @UnCheckToken
    @PostMapping(value = "/index")
    @ApiOperation(value = "首页", notes = "首页")
    @ApiOperationSupport(author = "David", order = 2)
    public Result<HomeParams.IndexResDto> index() {
        return Result.ok(homeServiceImpl.index());
    }

    @UnCheckToken
    @PostMapping(value = "/game")
    @ApiOperation(value = "三方游戏", notes = "三方游戏列表")
    @ApiOperationSupport(author = "David", order = 5)
    public Result<List<HomeParams.GameIndexResDto>> game() {
        return Result.ok(homeServiceImpl.game());
    }

    @UnCheckToken
    @PostMapping(value = "/banner")
    @ApiOperation(value = "Banner图", notes = "Banner列表")
    @ApiOperationSupport(author = "David")
    public Result<List<BannerResDto>> banner(@Valid @RequestBody BannerReqDto reqDto) {
        return Result.ok(homeServiceImpl.banner(reqDto));
    }

    @UnCheckToken
    @PostMapping(value = "/notice")
    @ApiOperation(value = "公告", notes = "公告列表")
    @ApiOperationSupport(author = "David", order = 4)
    public Result<ResPage<HomeParams.NoticeResDto>> notice(@Valid @RequestBody ReqPage<HomeParams.NoticeReqDto> dto) {
        return Result.ok(homeServiceImpl.notice(dto));
    }

    @UnCheckToken
    @GetMapping(value = "/delCache")
    @ApiOperation(value = "缓存清理", notes = "缓存清理")
    @ApiOperationSupport(author = "David", order = 4)
    public Result<Boolean> delCache(@RequestParam(value = "cacheKey") String cacheKey,
                                    @RequestParam(value = "cacheField", required = false) String cacheField) {
        return Result.ok(homeServiceImpl.delCache(cacheKey, cacheField));
    }
    @PostMapping(value = "/delNotice")
    @ApiOperation(value = "删除站内信", notes = "删除站内信")
    @ApiOperationSupport(author = "David", order = 6 )
    public Result<Boolean> delNotice(@Valid @RequestBody HomeParams.DelNoticeReqDto dto) {
        return Result.ok(homeServiceImpl.delNotice(dto));
    }
}

