package com.xinbo.sports.backend.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.xinbo.sports.backend.io.bo.PlatManager;
import com.xinbo.sports.backend.service.IPlatManagerService;
import com.xinbo.sports.plat.io.dto.base.BetslipsDetailDto;
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
 * 游戏管理
 * </p>
 *
 * @author andy
 * @since 2020/6/8
 */
@Slf4j
@RestController
@RequestMapping("/v1/platManager")
@Api(tags = {"游戏管理"})
public class PlatManagerController {
    @Resource
    private IPlatManagerService platManagerServiceImpl;

    @ApiOperationSupport(author = "Andy", order = 1)
    @ApiOperation(value = "平台管理-列表", notes = "平台管理-列表")
    @PostMapping("/listPlat")
    public Result<ResPage<PlatManager.ListPlatResBody>> listPlat(@Valid @RequestBody ReqPage<PlatManager.ListPlatReqBody> reqBody) {
        return Result.ok(platManagerServiceImpl.listPlat(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 2)
    @ApiOperation(value = "平台管理-编辑或启用平台", notes = "平台管理-编辑或启用平台")
    @PostMapping("/updatePlat")
    public Result<Boolean> updatePlat(@Valid @RequestBody PlatManager.UpdatePlatReqBody reqBody) {
        platManagerServiceImpl.updatePlat(reqBody);
        return Result.ok(true);
    }

    @ApiOperationSupport(author = "Andy", order = 3)
    @ApiOperation(value = "平台子游戏管理-列表", notes = "平台子游戏管理-列表")
    @PostMapping("/listSubGame")
    public Result<ResPage<PlatManager.ListSubGameResBody>> listSubGame(@Valid @RequestBody ReqPage<PlatManager.ListSubGameReqBody> dto) {
        return Result.ok(platManagerServiceImpl.listSubGame(dto));
    }

    @ApiOperationSupport(author = "Andy", order = 4)
    @ApiOperation(value = "平台子游戏管理-编辑或启用平台", notes = "平台子游戏管理-编辑或启用平台")
    @PostMapping("/updateSubGame")
    public Result<Boolean> updateSubGame(@Valid @RequestBody PlatManager.UpdateSubGameReqBody reqBody) {
        platManagerServiceImpl.updateSubGame(reqBody);
        return Result.ok(true);
    }

    @ApiOperationSupport(author = "Andy", order = 5)
    @ApiOperation(value = "游戏列表", notes = "游戏列表")
    @PostMapping("/gameList")
    public Result<List<PlatManager.GameListResBody>> gameList() {
        List<PlatManager.GameListResBody> list = platManagerServiceImpl.gameList();
        return Result.ok(list);
    }

    @ApiOperation(value = "热门赛事配置列表", notes = "热门赛事配置列表")
    @ApiOperationSupport(author = "wells", order = 6)
    @PostMapping("/sportCompetitionList")
    public Result<ResPage<BetslipsDetailDto.SportSchedule>> sportCompetitionList(@Valid @RequestBody ReqPage<BetslipsDetailDto.SportReqDto> dto) {
        return Result.ok(platManagerServiceImpl.sportCompetitionList(dto));
    }

    @PostMapping(value = "/updateSportCompetition")
    @ApiOperation(value = "修改热门赛事配置", notes = "修改热门赛事配置")
    @ApiOperationSupport(author = "wells", order = 7)
    public Result<Boolean> updateSportCompetition(@Valid @RequestBody PlatManager.UpdateSportCompetitionRedDto reqDto) {
        return Result.ok(platManagerServiceImpl.updateSportCompetition(reqDto));
    }

    @PostMapping(value = "/deleteSportCompetition")
    @ApiOperation(value = "删除热门赛事配置", notes = "删除热门赛事配置")
    @ApiOperationSupport(author = "wells", order = 8)
    public Result<Boolean> deleteSportCompetition(@Valid @RequestBody PlatManager.DeleteSportCompetitionRedDto reqDto) {
        return Result.ok(platManagerServiceImpl.deleteSportCompetition(reqDto));
    }

    @PostMapping("/teamLogonList")
    @ApiOperation(value = "队徽列表", notes = "队徽列表")
    @ApiOperationSupport(author = "wells", order = 9)
    public Result<ResPage<PlatManager.TeamLogoDto>> teamLogonList(@Valid @RequestBody ReqPage<PlatManager.TeamLogoReqDto> reqDto) {
        return Result.ok(platManagerServiceImpl.teamLogonList(reqDto));
    }

    @PostMapping("/addTeamLogon")
    @ApiOperation(value = "新增队徽", notes = "新增队徽")
    @ApiOperationSupport(author = "wells", order = 10)
    public Result<Boolean> addTeamLogon(@Valid @RequestBody PlatManager.TeamLogoDto reqDto) {
        return Result.ok(platManagerServiceImpl.addTeamLogon(reqDto));
    }

    @PostMapping("/deleteTeamLogon")
    @ApiOperation(value = "批量删除队徽", notes = "批量删除队徽")
    @ApiOperationSupport(author = "wells", order = 11)
    public Result<Boolean> deleteTeamLogon(@Valid @RequestBody PlatManager.DeleteTeamLogoDto reqDto) {
        return Result.ok(platManagerServiceImpl.deleteTeamLogon(reqDto));
    }
}