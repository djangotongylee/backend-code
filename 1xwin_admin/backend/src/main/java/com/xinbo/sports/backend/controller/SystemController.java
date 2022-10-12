package com.xinbo.sports.backend.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import com.xinbo.sports.backend.aop.annotation.UnCheckToken;
import com.xinbo.sports.backend.io.dto.System;
import com.xinbo.sports.backend.service.ISystemService;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;
import com.xinbo.sports.utils.components.response.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * 系统配置
 * </p>
 *
 * @author andy
 * @since 2020/10/5
 */
@Slf4j
@RestController
@Api(tags = "系统配置")
@ApiSupport(order = 19, author = "Andy")
@RequestMapping("/v1/system")
public class SystemController {
    @Resource
    private ISystemService systemServiceImpl;

    @ApiOperationSupport(author = "Andy", order = 1)
    @PostMapping(value = "/bannerList")
    @ApiOperation(value = "Banner图 -> 列表查询", notes = "Banner图 -> 列表查询")
    public Result<List<System.BannerListResBody>> bannerList(@RequestBody System.BannerListReqBody reqBody) {
        return Result.ok(systemServiceImpl.bannerList(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 2)
    @PostMapping(value = "/addBanner")
    @ApiOperation(value = "Banner图 -> 新增", notes = "Banner图 -> 新增")
    public Result<Boolean> addBanner(@Valid @RequestBody System.AddBannerReqBody reqBody) {
        return Result.ok(systemServiceImpl.addBanner(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 3)
    @PostMapping(value = "/updateBanner")
    @ApiOperation(value = "Banner图 -> 修改", notes = "Banner图 -> 修改")
    public Result<Boolean> updateBanner(@Valid @RequestBody System.UpdateBannerReqBody reqBody) {
        return Result.ok(systemServiceImpl.updateBanner(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 4)
    @PostMapping(value = "/delBanner")
    @ApiOperation(value = "Banner图 -> 删除", notes = "Banner图 -> 删除")
    public Result<Boolean> delBanner(@Valid @RequestBody System.DelBannerReqBody reqBody) {
        return Result.ok(systemServiceImpl.delBanner(reqBody));
    }

    @ApiOperationSupport(order = 6, author = "Andy")
    @PostMapping(value = "/getOpenPresetList")
    @ApiOperation(value = "预设开奖->列表", notes = "预设开奖 -> 列表")
    public Result<ResPage<System.GetOpenPresetListResBody>> getOpenPresetList(@Valid @RequestBody ReqPage<System.GetOpenPresetListReqBody> reqBody) {
        return Result.ok(systemServiceImpl.getOpenPresetList(reqBody));
    }

    @ApiOperationSupport(order = 7, author = "Andy")
    @PostMapping(value = "/saveOrUpdateOpenPreset")
    @ApiOperation(value = "预设开奖->新增/修改", notes = "预设开奖 -> 新增/修改")
    public Result<Boolean> saveOrUpdateOpenPreset(@Valid @RequestBody System.SaveOrUpdateOpenPresetReqBody reqBody) {
        return Result.ok(systemServiceImpl.saveOrUpdateOpenPreset(reqBody));
    }

    @ApiOperationSupport(order = 8, author = "Andy")
    @PostMapping(value = "/deleteOpenPreset")
    @ApiOperation(value = "预设开奖->删除", notes = "预设开奖->删除")
    public Result<Boolean> deleteOpenPreset(@Valid @RequestBody System.DeleteOpenPresetReqBody reqBody) {
        return Result.ok(systemServiceImpl.deleteOpenPreset(reqBody));
    }

    @UnCheckToken
    @PostMapping(value = "/init")
    @ApiOperation(value = "初始化接口", notes = "初始化")
    @ApiOperationSupport(author = "David", order = 1)
    public Result<List<System.InitResDto>> init() {
        return Result.ok(systemServiceImpl.init());
    }


    @ApiOperationSupport(order = 9, author = "Andy")
    @PostMapping(value = "/exportOpenPresetList")
    @ApiOperation(value = "预设开奖->导出", notes = "预设开奖->导出:excel")
    public Result<List<System.ExportOpenPresetListResBody>> exportOpenPresetList(@Valid @RequestBody System.ExportOpenPresetListReqBody reqBody) {
        return Result.ok(systemServiceImpl.exportOpenPresetList(reqBody));
    }

    @ApiOperationSupport(order = 11, author = "Andy")
    @PostMapping(value = "/saveOrUpdateBatchOpenPreset")
    @ApiOperation(value = "预设开奖->新增批量预设", notes = "预设开奖 -> 新增批量预设")
    public Result<Boolean> saveOrUpdateBatchOpenPreset(@Valid @RequestBody System.SaveOrUpdateBatchOpenPresetReqBody reqBody) {
        return Result.ok(systemServiceImpl.saveOrUpdateBatchOpenPreset(reqBody));
    }

    @ApiOperationSupport(order = 12, author = "David")
    @PostMapping(value = "/getLotteryActionNo")
    @ApiOperation(value = "预设开奖->获取当前期号信息", notes = "预设开奖->获取当前期号信息")
    public Result<System.GetLotteryActionNoResBody> getLotteryActionNo(@RequestBody System.GetLotteryActionNoReqBody reqBody) {
        return Result.ok(systemServiceImpl.getLotteryActionNo(reqBody));
    }

    @PostMapping(value = "/geBannersByLang")
    @ApiOperation(value = "根据语言获取Banner信息", notes = "根据语言获取Banner信息")
    @ApiOperationSupport(order = 13)
    public Result<System.BannerByLangResDto> geBannersByLang(@Valid @RequestBody System.BannerByLangReqDto reqDto) {
        return Result.ok(systemServiceImpl.geBannersByLang(reqDto));
    }


    @PostMapping(value = "/getCommissionRule")
    @ApiOperation(value = "查询代理配置", notes = "查询代理配置")
    @ApiOperationSupport(order = 14)
    public Result<System.CommissionRuleDto> getCommissionRule() {
        return Result.ok(systemServiceImpl.getCommissionRule());
    }

    @PostMapping(value = "/updateCommissionRule")
    @ApiOperation(value = "修改代理配置", notes = "修改代理配置")
    @ApiOperationSupport(order = 15)
    public Result<Boolean> updateCommissionRule(@Valid @RequestBody System.CommissionRuleDto reqDto) {
        return Result.ok(systemServiceImpl.updateCommissionRule(reqDto));
    }

    @PostMapping(value = "/getPlatConfig")
    @ApiOperation(value = "查询平台配置", notes = "查询平台配置")
    @ApiOperationSupport(order = 16)
    public Result<System.PlatConfigDto> getPlatConfig() {
        return Result.ok(systemServiceImpl.getPlatConfig());
    }

    @PostMapping(value = "/updatePlatConfig")
    @ApiOperation(value = "修改平台配置", notes = "修改平台配置")
    @ApiOperationSupport(order = 17)
    public Result<Boolean> updatePlatConfig(@Valid @RequestBody System.PlatConfigDto reqDto) {
        return Result.ok(systemServiceImpl.updatePlatConfig(reqDto));
    }

    @ApiOperationSupport(order = 18)
    @PostMapping(value = "/getOpenRateDistribute")
    @ApiOperation(value = "预设开奖概率分布", notes = "预设开奖概率分布")
    public Result<System.OpenRateDistributeResDto> getOpenRateDistribute(@Valid @RequestBody System.OpenRateDistributeReqDto reqDto) {
        return Result.ok(systemServiceImpl.getOpenRateDistribute(reqDto));
    }

    @PostMapping(value = "/getOnlineServiceConfig")
    @ApiOperation(value = "查询在线客服配置", notes = "查询在线客服配置")
    @ApiOperationSupport(order = 19)
    public Result<System.OnlineServiceConfigDto> getOnlineServiceConfig() {
        return Result.ok(systemServiceImpl.getOnlineServiceConfig());
    }

    @PostMapping(value = "/updateOnlineServiceConfig")
    @ApiOperation(value = "修改在线客服配置", notes = "修改在线客服配置")
    @ApiOperationSupport(order = 20)
    public Result<Boolean> updateOnlineServiceConfig(@Valid @RequestBody System.OnlineServiceConfigDto reqDto) {
        return Result.ok(systemServiceImpl.updateOnlineServiceConfig(reqDto));
    }

}
