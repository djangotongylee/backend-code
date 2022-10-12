package com.xinbo.sports.apiend.controller;


import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import com.xinbo.sports.apiend.aop.annotation.UnCheckToken;
import com.xinbo.sports.apiend.base.GameCommonBase;
import com.xinbo.sports.apiend.io.bo.PlatformParams;
import com.xinbo.sports.apiend.io.bo.PlatformParams.PlatListResInfo;
import com.xinbo.sports.apiend.io.bo.PlatformParams.PlatTransferResInfo;
import com.xinbo.sports.apiend.io.dto.StartEndTime;
import com.xinbo.sports.apiend.io.dto.platform.*;
import com.xinbo.sports.apiend.service.IGameService;
import com.xinbo.sports.apiend.service.impl.GameBatchServiceImpl;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams.PlatGameLoginResDto;
import com.xinbo.sports.plat.io.dto.base.BetslipsDetailDto;
import com.xinbo.sports.service.thread.ThreadHeaderLocalData;
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
 * @author Davids
 */
@Slf4j
@RestController
@Api(tags = "首页 - 平台游戏")
@ApiSort(3)
@RequestMapping("/v1/platform")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PlatformController {
    private final IGameService gameServiceImpl;
    private final GameCommonBase gameCommonBase;
    private final GameBatchServiceImpl gameBatchServiceImpl;


    @ApiOperationSupport(author = "David", order = 1)
    @ApiOperation(value = "平台列表", notes = "平台列表")
    @PostMapping("/platList")
    public Result<List<PlatListResInfo>> platList() {
        return Result.ok(gameServiceImpl.platList());
    }

    @ApiOperationSupport(author = "David", order = 2)
    @ApiOperation(value = "游戏列表", notes = "游戏列表")
    @PostMapping("/gameList")
    public Result<List<GameListInfo>> gameList() {
        return Result.ok(gameServiceImpl.gameList());
    }

    @ApiOperationSupport(author = "David", order = 3)
    @PostMapping(value = "/login")
    @ApiOperation(value = "登录第三方游戏", notes = "登录第三方游戏")
    public Result<PlatGameLoginResDto> login(@Valid @RequestBody GameLoginReqDto dto) {
        return Result.ok(gameServiceImpl.login(dto));
    }

    @ApiOperationSupport(author = "David", order = 4)
    @PostMapping(value = "/coinTransfer")
    @ApiOperation(value = "三方游戏平台(上、下分)", notes = "三方游戏平台(上下分)")
    public Result<CoinTransferResDto> coinTransfer(@Valid @RequestBody CoinTransferReqDto dto) {
        dto.setName(ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get().getUsername());
        return Result.ok(gameServiceImpl.coinTransfer(dto));
    }

    @ApiOperationSupport(author = "David", order = 5)
    @PostMapping(value = "/coinDownBatch")
    @ApiOperation(value = "三方游戏平台->批量下分", notes = "三方游戏平台->批量下分")
    public Result<CoinDownBatch.CoinDownBatchResBody> coinDownBatch(@Valid @RequestBody List<CoinDownBatch.CoinDownBatchReqBody> reqBodyList) {
        return Result.ok(gameBatchServiceImpl.coinDownBatch(reqBodyList, ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get().getUsername()));
    }


    @ApiOperationSupport(author = "David", order = 6)
    @PostMapping(value = "/queryBalance")
    @ApiOperation(value = "三方游戏余额查询", notes = "余额查询")
    public Result<GameBalanceResDto> queryBalance(@Valid @RequestBody GameBalanceReqDto dto) {
        return Result.ok(gameServiceImpl.queryBalanceByPlatId(dto));
    }

    @UnCheckToken
    @PostMapping(value = "/slotGameList")
    @ApiOperationSupport(author = "David", order = 10)
    @ApiOperation(value = "老虎机游戏列表", notes = "老虎机游戏列表")
    public Result<ResPage<PlatFactoryParams.PlatSlotGameResDto>> slotGameList(@Valid @RequestBody ReqPage<SlotGameReqDto> dto) {
        return Result.ok(gameServiceImpl.slotGameList(dto));
    }

    @ApiOperationSupport(author = "David", order = 11)
    @PostMapping(value = "/slotGameFavorite")
    @ApiOperation(value = "收藏/取消 老虎机游戏", notes = "老虎机游戏收藏")
    public Result<Boolean> slotGameFavorite(@Valid @RequestBody SlotGameFavoriteReqDto dto) {
        return Result.ok(gameServiceImpl.slotGameFavorite(dto));
    }

    @ApiOperation(value = "根据游戏ID查询投注列表", notes = "根据游戏ID查询投注列表")
    @ApiOperationSupport(author = "David", ignoreParameters = {"dto.uid"}, order = 20)
    @PostMapping("/getBetListByDate")
    public Result<ResPage<PlatFactoryParams.PlatBetListResDto>> getBetListByDate(@Valid @RequestBody ReqPage<PlatformParams.GameQueryDateDto> dto) {
        return Result.ok(gameServiceImpl.getBetListByDate(dto));
    }

    @ApiOperation(value = "根据游戏ID查询总输赢、总盈亏", notes = "根据游戏ID查询总输赢、总盈亏")
    @ApiOperationSupport(author = "David", order = 21)
    @PostMapping("/getCoinStatisticsByDate")
    public Result<PlatFactoryParams.PlatCoinStatisticsResDto> getCoinStatisticsByDate(@Valid @RequestBody PlatformParams.GameQueryDateDto dto) {
        return Result.ok(gameServiceImpl.getCoinStatisticsByDate(dto));
    }

    @ApiOperation(value = "根据游戏总上分、总下分", notes = "根据游戏总上分、总下分")
    @ApiOperationSupport(author = "David", ignoreParameters = {"dto.id"}, order = 22)
    @PostMapping("/getCoinByDate")
    public Result<PlatTransferResInfo> getCoinTransferByDate(@Valid @RequestBody StartEndTime dto) {
        return Result.ok(gameServiceImpl.getCoinTransferByDate(dto));
    }

    @ApiOperation(value = "根据id查询注单详情", notes = "根据id查询游戏详情")
    @ApiOperationSupport(author = "wells", ignoreParameters = {"dto.id"}, order = 23)
    @PostMapping("/getBetslipsDetail")
    public Result<BetslipsDetailDto.Betslips> getBetslipsDetail(@Valid @RequestBody BetslipsDetailDto.BetslipsDetailReqDto reqDto) {
        return Result.ok(gameCommonBase.getBetslipsDetail(reqDto));
    }

    @ApiOperation(value = "每日赛事预告", notes = "每日赛事预告")
    @ApiOperationSupport(author = "wells", order = 25)
    @PostMapping("/getSportSchedule")
    public Result<ResPage<BetslipsDetailDto.SportSchedule>> getSportSchedule(@Valid @RequestBody ReqPage<BetslipsDetailDto.SportScheduleReqDto> dto) {
        return Result.ok(gameServiceImpl.getSportSchedule(dto));
    }

    @ApiOperation(value = "前往赛事", notes = "前往体育赛事")
    @ApiOperationSupport(author = "wells", order = 27)
    @PostMapping("/forwardEvent")
    public Result<BetslipsDetailDto.ForwardEvent> forwardEvent(@Valid @RequestBody BetslipsDetailDto.ForwardEventReq reqDto) {
        return Result.ok(gameServiceImpl.forwardEvent(reqDto));
    }

    @UnCheckToken
    @ApiOperation(value = "三方验证", notes = "三方验证")
    @ApiOperationSupport(author = "wells", order = 26)
    @RequestMapping(method={RequestMethod.POST,RequestMethod.GET}, value = "/verification/{path}")
    public <T>T  verifySession(HttpServletRequest httpServletRequest,@PathVariable String path) {
        return gameServiceImpl.verifySession(httpServletRequest,path);
    }
}
