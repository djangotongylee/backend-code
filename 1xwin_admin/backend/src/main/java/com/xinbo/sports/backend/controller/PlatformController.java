package com.xinbo.sports.backend.controller;


import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import com.xinbo.sports.backend.aop.annotation.UnCheckLog;
import com.xinbo.sports.backend.io.dto.Platform;
import com.xinbo.sports.backend.service.IPlatformService;
import com.xinbo.sports.plat.aop.annotation.ExceptionEnum;
import com.xinbo.sports.plat.aop.annotation.ThirdPlatException;
import com.xinbo.sports.utils.components.response.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * @author Davids
 */
@Slf4j
@RestController
@Api(tags = "第三方平台游戏")
@ApiSort(3)
@RequestMapping("/v1/platform")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PlatformController {
    private final IPlatformService platformServiceImpl;

    @ApiOperationSupport(author = "David", order = 1)
    @ApiOperation(value = "平台列表", notes = "平台列表")
    @PostMapping("/platList")
    public Result<List<Platform.PlatListResInfo>> platList() {
        return Result.ok(platformServiceImpl.platList());
    }

    @ApiOperationSupport(author = "David", order = 2)
    @ApiOperation(value = "游戏列表", notes = "游戏列表")
    @PostMapping("/gameList")
    public Result<List<Platform.GameListInfo>> gameList(@Valid @RequestBody Platform.GameListReqDto dto) {
        return Result.ok(platformServiceImpl.gameList(dto));
    }

    @ApiOperationSupport(author = "David", order = 3)
    @PostMapping(value = "/queryBalance")
    @ApiOperation(value = "三方游戏余额查询", notes = "余额查询")
    public Result<Platform.GameBalanceResDto> queryBalance(@Valid @RequestBody Platform.GameBalanceReqDto dto) {
        return Result.ok(platformServiceImpl.queryBalanceByPlatId(dto));
    }

    @UnCheckLog
    @ApiOperationSupport(author = "David", order = 99)
    @ApiOperation(value = "SBO 初始化代理", notes = "初始化代理")
    @PostMapping("/createAgentSbo")
    public Result<Boolean> createAgentSbo() {
        return Result.ok(platformServiceImpl.createAgentSbo());
    }

    @UnCheckLog
    @ApiOperationSupport(author = "David", order = 4)
    @ApiOperation(value = "上、下分状态异常补单", notes = "上、下分状态异常补单")
    @PostMapping("/checkTransferStatus")
    public Result<Boolean> checkTransferStatus(@Valid @RequestBody Platform.CheckTransferStatusReqDto dto) {
        return Result.ok(platformServiceImpl.checkTransferStatus(dto));
    }

    @ApiOperationSupport(author = "David", order = 5)
    @ApiOperation(value = "生成补单订单", notes = "生成补单订单")
    @PostMapping("/genBetSlipsSupplemental")
    public Result<Boolean> genBetSlipsSupplemental(@Valid @RequestBody Platform.GenBetSlipsSupplementalReqDto dto) {
        return Result.ok(platformServiceImpl.genBetSlipsSupplemental(dto));
    }

    @ThirdPlatException(exceptionType = ExceptionEnum.MANUAL_SUPPLEMENT_EXCEPTION)
    @ApiOperationSupport(author = "David", order = 6)
    @ApiOperation(value = "三方平台补单:正常补单", notes = "三方平台补单:正常补单")
    @PostMapping("/betSlipsSupplemental")
    public Result<Boolean> betSlipsSupplemental(@Valid @RequestBody Platform.BetSlipsSupplementalReqDto dto) {
        return Result.ok(platformServiceImpl.betSlipsSupplemental(dto));
    }

    @ThirdPlatException(exceptionType = ExceptionEnum.MANUAL_PULL_EXCEPTION)
    @ApiOperationSupport(author = "David", order = 7)
    @ApiOperation(value = "三方平台补单:拉单异常补单", notes = "三方平台补单:拉单异常补单")
    @PostMapping("/betSlipsException")
    public Result<Boolean> betSlipsException(@Valid @RequestBody Platform.BetSlipsExceptionReqDto dto) {
        return Result.ok(platformServiceImpl.betSlipsException(dto));
    }

    @UnCheckLog
    @ApiOperationSupport(author = "David", order = 98)
    @ApiOperation(value = "同步老虎机游戏列表", notes = "同步用户及游戏列表")
    @PostMapping("/syncSlotGameList")
    public Result<Boolean> syncSlotGameList(@Valid @RequestBody Platform.SyncSlotGameReqDto dto) {
        return Result.ok(platformServiceImpl.syncSlotGameList(dto));
    }

    @UnCheckLog
    @ApiOperationSupport(author = "Andy", order = 99)
    @ApiOperation(value = "游戏类型->平台名称->游戏名称->三级联动", notes = "游戏类型->平台名称->游戏名称->三级联动")
    @PostMapping("/getCascadeByGameGroupList")
    public Result<List<Platform.GameGroupDict>> getCascadeByGameGroupList() {
        return Result.ok(platformServiceImpl.getCascadeByGameGroupList());
    }
}
