package com.xinbo.sports.backend.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.xinbo.sports.backend.io.bo.GameRecord.GameDetailsReqBody;
import com.xinbo.sports.backend.io.bo.GameRecord.GameListReqBody;
import com.xinbo.sports.backend.io.dto.RecordParams.*;
import com.xinbo.sports.backend.io.dto.record.*;
import com.xinbo.sports.backend.service.IGameRecordService;
import com.xinbo.sports.dao.generator.po.BetslipsCq9WaterMargin;
import com.xinbo.sports.dao.generator.po.BetslipsJokerGame;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;
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

@Slf4j
@RestController
@RequestMapping("/v1/gameRecord")
@Api(tags = "游戏记录")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GamesRecordController {

    private final IGameRecordService gameRecordServiceImpl;


    @ApiOperationSupport(author = "Andy", order = 1)
    @ApiOperation(value = "游戏列表", notes = "全部游戏列表")
    @PostMapping("/gameListByGroupId")
    public Result<List<GameListResBody>> gameListByGroupId(@Valid @RequestBody GameListReqBody dto) {
        List<GameListResBody> list = gameRecordServiceImpl.getGameListByGroupId(dto);
        return Result.ok(list);
    }

    @ApiOperationSupport(author = "David", order = 2)
    @ApiOperation(value = "注单总计", notes = "注单总计")
    @PostMapping("/Gross")
    public Result<GrossResBody> gross(@RequestBody QueryGameRecordDto dto) {
        return Result.ok(gameRecordServiceImpl.getGross(dto));
    }

    @ApiOperationSupport(author = "David", order = 3)
    @ApiOperation(value = "电游列表", notes = "电游注单列表")
    @PostMapping("/getEGameRecord")
    public Result<ResPage<EGamesBetslips>> getEGamesGameRecord(@RequestBody ReqPage<QueryGameRecordDto> dto) {
        return Result.ok(gameRecordServiceImpl.getEGamesRecord(dto));
    }

    @ApiOperationSupport(author = "David", order = 29)
    @ApiOperation(value = "体育列表", notes = "体育注单列表")
    @PostMapping("/getSportRecord")
    public Result<ResPage<SportBetslips>> getSportGameRecord(@RequestBody ReqPage<QueryGameRecordDto> dto) {
        return Result.ok(gameRecordServiceImpl.getSportRecord(dto));
    }


    @ApiOperationSupport(author = "David", order = 4)
    @ApiOperation(value = "电竞注单列表", notes = "电竞注单列表")
    @PostMapping("/getESportGameRecord")
    public Result<ResPage<ESportsBetslips>> getESportGameRecord(@RequestBody ReqPage<QueryGameRecordDto> dto) {
        return Result.ok(gameRecordServiceImpl.getESportGameRecord(dto));
    }


    @ApiOperationSupport(author = "David", order = 5)
    @ApiOperation(value = "棋牌列表", notes = "棋牌注单列表")
    @PostMapping("/getChessGameRecord")
    public Result<ResPage<ChessBetslips>> getChessGameRecord(@RequestBody ReqPage<QueryGameRecordDto> dto) {
        return Result.ok(gameRecordServiceImpl.getChessGameRecord(dto));
    }

    @ApiOperationSupport(author = "David", order = 6)
    @ApiOperation(value = "捕鱼注单列表", notes = "捕鱼注单列表")
    @PostMapping("/getFishingGameRecord")
    public Result<ResPage<FishingBetslips>> getFishingGameRecord(@RequestBody ReqPage<QueryGameRecordDto> dto) {
        return Result.ok(gameRecordServiceImpl.getFishingGameRecord(dto));
    }

    @ApiOperationSupport(author = "David", order = 7)
    @ApiOperation(value = "真人注单列表", notes = "真人注单列表")
    @PostMapping("/getLiveGameRecordByPlat")
    public Result<ResPage<LiveBetslips>> getLiveGameRecord(@RequestBody ReqPage<QueryGameRecordDto> dto) {
        return Result.ok(gameRecordServiceImpl.getLiveGameRecord(dto));
    }

    @ApiOperationSupport(author = "David", order = 8)
    @ApiOperation(value = "动物竞技注单列表", notes = "动物竞技注单列表")
    @PostMapping("/getAnimalFightGameRecordByPlat")
    public Result<ResPage<AnimalFightBetslips>> getAnimalFightGameRecord(@RequestBody ReqPage<QueryGameRecordDto> dto) {
        return Result.ok(gameRecordServiceImpl.getAnimalFightGameRecord(dto));
    }

    @ApiOperationSupport(author = "David", order = 9)
    @ApiOperation(value = "彩票注单列表", notes = "彩票注单列表")
    @PostMapping("/getLotteryGameRecord")
    public Result<ResPage<LotteryBetslips>> getLotteryGameRecord(@RequestBody ReqPage<QueryGameRecordDto> dto) {
        return Result.ok(gameRecordServiceImpl.getLotteryGameRecord(dto));
    }


    @ApiOperationSupport(author = "Andy", order = 10)
    @ApiOperation(value = "Mg游戏注单详情", notes = "mg游戏注单详情")
    @PostMapping("/mgGameDetails")
    public Result<BetslipsMgDto> getMgGameDetails(@RequestBody GameDetailsReqBody dto) {
        return Result.ok(gameRecordServiceImpl.getMgGameDetails(dto));
    }

    @ApiOperationSupport(author = "Andy", order = 11)
    @ApiOperation(value = "Upg游戏注单详情", notes = "upg游戏注单详情")
    @PostMapping("/upgGameDetails")
    public Result<BetslipsUpgDto> getUpgGameDetails(@RequestBody GameDetailsReqBody dto) {
        return Result.ok(gameRecordServiceImpl.getUpgGameDetails(dto));
    }

    @ApiOperationSupport(author = "Andy", order = 12)
    @ApiOperation(value = "Hb游戏注单详情", notes = "hb游戏注单详情")
    @PostMapping("/hbGameDetails")
    public Result<BetslipsHbDto> getHbGameDetails(@RequestBody GameDetailsReqBody dto) {
        return Result.ok(gameRecordServiceImpl.getHbGameDetails(dto));
    }

    @ApiOperationSupport(author = "Andy", order = 13)
    @ApiOperation(value = "bbin游戏注单详情", notes = "bbin游戏注单详情")
    @PostMapping("/bbinGameDetails")
    public Result<BetslipsBbinGameDto> getBbinGameDetails(@RequestBody GameDetailsReqBody dto) {
        return Result.ok(gameRecordServiceImpl.getBbinGameDetails(dto));
    }

    @ApiOperationSupport(author = "Andy", order = 14)
    @ApiOperation(value = "捕鱼达人游戏注单详情", notes = "捕鱼达人游戏注单详情")
    @PostMapping("/bbinFishingExpertGameDetails")
    public Result<BetslipsBbinFishingExpertDto> getBbinFishingExpertDetails(@RequestBody GameDetailsReqBody dto) {
        return Result.ok(gameRecordServiceImpl.getBbinFishingExpertDetails(dto));
    }

    @ApiOperationSupport(author = "Andy", order = 15)
    @ApiOperation(value = "捕鱼大师游戏注单详情", notes = "捕鱼大师游戏注单详情")
    @PostMapping("/bbinFishingMasterGameDetails")
    public Result<BetslipsBbinFishingMasterDto> getBbinFishingMasterDetails(@RequestBody GameDetailsReqBody dto) {
        return Result.ok(gameRecordServiceImpl.getBbinFishingMasterDetails(dto));
    }

    @ApiOperationSupport(author = "Andy", order = 16)
    @ApiOperation(value = "tcg游戏注单详情", notes = "tcg游戏注单详情")
    @PostMapping("/tcgLotteryDetails")
    public Result<BetslipsTcgDto> getTcgLotteryDetails(@RequestBody GameDetailsReqBody dto) {
        return Result.ok(gameRecordServiceImpl.getTcgLotteryDetails(dto));
    }

    @ApiOperationSupport(author = "Andy", order = 17)
    @ApiOperation(value = "Slotto游戏注单详情", notes = "Slotto游戏注单详情")
    @PostMapping("/SlottoDetails")
    public Result<BetslipsSlottoDto> getSlottoGameDetails(@RequestBody GameDetailsReqBody dto) {
        return Result.ok(gameRecordServiceImpl.getSlottoDetails(dto));
    }

    @ApiOperationSupport(author = "Andy", order = 18)
    @ApiOperation(value = "ag游戏注单详情", notes = "ag游戏注单详情")
    @PostMapping("/agLiveDetails")
    public Result<BetslipsAgDto> getAgLiveDetails(@RequestBody GameDetailsReqBody dto) {
        return Result.ok(gameRecordServiceImpl.getAgLiveDetails(dto));
    }

    @ApiOperationSupport(author = "Andy", order = 19)
    @ApiOperation(value = "bbin真人游戏注单详情", notes = "bbin真人注单详情")
    @PostMapping("/bbinLiveDetails")
    public Result<BetslipsBbinLiveDto> getBbinLiveGameDetails(@RequestBody GameDetailsReqBody dto) {
        return Result.ok(gameRecordServiceImpl.getBbinLiveGameDetails(dto));
    }

    @ApiOperationSupport(author = "Andy", order = 20)
    @ApiOperation(value = "vm真人注单详情", notes = "vm真人注单详情")
    @PostMapping("/vmLiveDetails")
    public Result<BetslipsWmDto> getVmLiveDetails(@RequestBody GameDetailsReqBody dto) {
        return Result.ok(gameRecordServiceImpl.getVmLiveDetails(dto));
    }

    @ApiOperationSupport(author = "Andy", order = 21)
    @ApiOperation(value = "dg真人注单详情", notes = "dg真人注单详情")
    @PostMapping("/dgLiveDetails")
    public Result<BetslipsDgDto> getDgLiveDetails(@RequestBody GameDetailsReqBody dto) {
        return Result.ok(gameRecordServiceImpl.getDgLiveDetails(dto));
    }

    @ApiOperationSupport(author = "Andy", order = 22)
    @ApiOperation(value = "gg捕鱼注单详情", notes = "gg捕鱼注单详情")
    @PostMapping("/ggLiveDetails")
    public Result<BetslipsGgDto> getGgLiveDetails(@RequestBody GameDetailsReqBody dto) {
        return Result.ok(gameRecordServiceImpl.getGgLiveDetails(dto));
    }

    @ApiOperationSupport(author = "Andy", order = 23)
    @ApiOperation(value = "ds棋牌注单详情", notes = "ds棋牌注单详情")
    @PostMapping("/dsChessDetails")
    public Result<BetslipsDsDto> getDsChessDetails(@RequestBody GameDetailsReqBody dto) {
        return Result.ok(gameRecordServiceImpl.getDsChessDetails(dto));
    }

    @ApiOperationSupport(author = "Andy", order = 24)
    @ApiOperation(value = "ibc体育注单详情", notes = "ibc体育注单详情")
    @PostMapping("/ibcSportDetails")
    public Result<BetslipsShabaSportsDto> getIbcSportDetails(@RequestBody GameDetailsReqBody dto) {
        return Result.ok(gameRecordServiceImpl.getIbcSportDetails(dto));
    }

    @ApiOperationSupport(author = "Andy", order = 25)
    @ApiOperation(value = "sbo体育注单详情", notes = "sbo体育注单详情")
    @PostMapping("/sboSportDetails")
    public Result<BetslipsSboSportsDto> getSboSportDetails(@RequestBody GameDetailsReqBody dto) {
        return Result.ok(gameRecordServiceImpl.getSboSportDetails(dto));
    }

    @ApiOperationSupport(author = "Andy", order = 26)
    @ApiOperation(value = "S128斗鸡注单详情", notes = "S128斗鸡注单详情")
    @PostMapping("/s128AnimalFightDetails")
    public Result<BetslipsS128Dto> getS128AnimalFightDetails(@RequestBody GameDetailsReqBody dto) {
        return Result.ok(gameRecordServiceImpl.getS128AnimalFightDetails(dto));
    }

    @ApiOperationSupport(author = "Andy", order = 27)
    @ApiOperation(value = "aeSexy注单详情", notes = "aeSexy注单详情")
    @PostMapping("/AESexyLiveDetails")
    public Result<BetslipsAeSexyDto> getAESexyLiveDetails(@RequestBody GameDetailsReqBody dto) {
        return Result.ok(gameRecordServiceImpl.getAESexyLiveDetails(dto));
    }

    @ApiOperationSupport(author = "Andy", order = 28)
    @ApiOperation(value = "aeKingMaker注单详情", notes = "aeKingMaker注单详情")
    @PostMapping("/AEKingMakerDetails")
    public Result<BetslipsAeKingMakerDto> getAEKingMakerDetails(@RequestBody GameDetailsReqBody dto) {
        return Result.ok(gameRecordServiceImpl.getAEKingMakerChessDetails(dto));
    }

    @ApiOperationSupport(author = "Andy", order = 28)
    @ApiOperation(value = "futures注单详情", notes = "futures注单详情")
    @PostMapping("/futuresDetails")
    public Result<BetslipsFuturesLotteryDto> getFuturesDetails(@RequestBody GameDetailsReqBody dto) {
        return Result.ok(gameRecordServiceImpl.getFuturesDetails(dto));
    }

    @ApiOperationSupport(author = "Andy", order = 29)
    @ApiOperation(value = "cq9game注单详情", notes = "CQ9注单详情")
    @PostMapping("/cq9GameDetails")
    public Result<BetslipsCq9GameDto> getCQ9GameDetails(@RequestBody GameDetailsReqBody dto) {
        return Result.ok(gameRecordServiceImpl.getCQ9GameDetails(dto));
    }

    @ApiOperationSupport(author = "Andy", order = 30)
    @ApiOperation(value = "CQ9ThunderFighter注单详情", notes = "CQ9ThunderFighter注单详情")
    @PostMapping("/CQ9ThunderFighterDetails")
    public Result<BetslipsCq9ThunderFighterDto> getCQ9ThunderFighterDetails(@RequestBody GameDetailsReqBody dto) {
        return Result.ok(gameRecordServiceImpl.getCQ9ThunderFighterDetails(dto));
    }

    @ApiOperationSupport(author = "Andy", order = 31)
    @ApiOperation(value = "CQ9Paradise注单详情", notes = "CQ9Paradise注单详情")
    @PostMapping("/CQ9ParadiseDetails")
    public Result<BetslipsCq9ParadiseDto> getCQ9ParadiseDetails(@RequestBody GameDetailsReqBody dto) {
        return Result.ok(gameRecordServiceImpl.getCQ9ParadiseDetails(dto));
    }

    @ApiOperationSupport(author = "Andy", order = 32)
    @ApiOperation(value = "CQ9ThunderFighter注单详情", notes = "CQ9ThunderFighter注单详情")
    @PostMapping("/CQ9WaterMarginDetails")
    public Result<BetslipsCq9WaterMargin> getCq9WaterMarginDetails(@RequestBody GameDetailsReqBody dto) {
        return Result.ok(gameRecordServiceImpl.getCq9WaterMarginDetails(dto));
    }

    @ApiOperationSupport(author = "Andy", order = 33)
    @ApiOperation(value = "jokerGame注单详情", notes = "jokerGame注单详情")
    @PostMapping("/jokerGameDetails")
    public Result<BetslipsJokerGame> getJokerGameDetails(@RequestBody GameDetailsReqBody dto) {
        return Result.ok(gameRecordServiceImpl.getJokerGameDetails(dto));
    }

    @ApiOperationSupport(author = "Andy", order = 34)
    @ApiOperation(value = "JokerHaiba注单详情", notes = "JokerHaiba")
    @PostMapping("/JokerHaibaDetails")
    public Result<BetslipsJokerHaibaDto> getJokerHaibaDetail(@RequestBody GameDetailsReqBody dto) {
        return Result.ok(gameRecordServiceImpl.getJokerHaibaDetail(dto));
    }
    @ApiOperationSupport(author = "Andy", order = 35)
    @ApiOperation(value = "SV388斗鸡注单详情", notes = "SV388CockFight")
    @PostMapping("/SV388CockFightDetail")
    public Result<BetslipsSv388Dto> getSV388CockFightDetail(@RequestBody GameDetailsReqBody dto) {
        return Result.ok(gameRecordServiceImpl.getSV388CockFightDetail(dto));
    }
    @ApiOperationSupport(author = "Andy", order = 35)
    @ApiOperation(value = "SALive注单详情", notes = "SALive")
    @PostMapping("/SALiveDetails")
    public Result<BetslipsSaDto> getSALiveDetails(@RequestBody GameDetailsReqBody dto) {
        return Result.ok(gameRecordServiceImpl.getSALiveDetails(dto));
    }
    @ApiOperationSupport(author = "Andy", order = 36)
    @ApiOperation(value = "BtiSport注单详情", notes = "BtiSport")
    @PostMapping("/BtiSportDetails")
    public Result<BetslipsBtiDto> getBtiSportDetails(@RequestBody GameDetailsReqBody dto) {
        return Result.ok(gameRecordServiceImpl.getBtiSportDetails(dto));
    }
    @ApiOperationSupport(author = "Andy", order = 37)
    @ApiOperation(value = "PGChess注单详情", notes = "PGChess")
    @PostMapping("/PGChessDetails")
    public Result<BetslipsPgChessDto> getPGChessDetails(@RequestBody GameDetailsReqBody dto) {
        return Result.ok(gameRecordServiceImpl.getPGChessDetails(dto));
    }
    @ApiOperationSupport(author = "Andy", order = 38)
    @ApiOperation(value = "PGGame注单详情", notes = "PGGame")
    @PostMapping("/PGGameDetails")
    public Result<BetslipsPgGameDto> getPGGameDetails(@RequestBody GameDetailsReqBody dto) {
        return Result.ok(gameRecordServiceImpl.getPGGameDetails(dto));
    }



    @ApiOperationSupport(author = "Andy", order = 38)
    @ApiOperation(value = "EbetLive注单详情", notes = "EbetLive")
    @PostMapping("/EBetDetails")
    public Result<BetslipsEbetDto> getEBetDetails(@RequestBody GameDetailsReqBody dto) {
        return Result.ok(gameRecordServiceImpl.getEBetDetails(dto));
    }
}
