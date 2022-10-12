package com.xinbo.sports.backend.service;


import com.xinbo.sports.backend.io.bo.GameRecord.GameDetailsReqBody;
import com.xinbo.sports.backend.io.bo.GameRecord.GameListReqBody;
import com.xinbo.sports.backend.io.dto.RecordParams.*;
import com.xinbo.sports.backend.io.dto.record.*;
import com.xinbo.sports.dao.generator.po.BetslipsCq9WaterMargin;
import com.xinbo.sports.dao.generator.po.BetslipsJokerGame;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;
import com.xinbo.sports.utils.components.response.CodeInfo;

import java.util.List;

/**
 * 游戏投注记录接口
 *
 * @author suki
 */
public interface IGameRecordService {

    List<GameListResBody> getGameListByGroupId(GameListReqBody dto);

    GrossResBody getGross(QueryGameRecordDto dto);

    ResPage<ESportsBetslips> getESportGameRecord(ReqPage<QueryGameRecordDto> dto);

    ResPage<EGamesBetslips> getEGamesRecord(ReqPage<QueryGameRecordDto> dto);

    ResPage<ChessBetslips> getChessGameRecord(ReqPage<QueryGameRecordDto> dto);

    ResPage<FishingBetslips> getFishingGameRecord(ReqPage<QueryGameRecordDto> dto);

    ResPage<LiveBetslips> getLiveGameRecord(ReqPage<QueryGameRecordDto> dto);

    ResPage<AnimalFightBetslips> getAnimalFightGameRecord(ReqPage<QueryGameRecordDto> dto);

    ResPage<LotteryBetslips> getLotteryGameRecord(ReqPage<QueryGameRecordDto> dto);

    ResPage<SportBetslips> getSportGameRecord(ReqPage<QueryGameRecordDto> dto);

    BetslipsMgDto getMgGameDetails(GameDetailsReqBody dto);

    BetslipsUpgDto getUpgGameDetails(GameDetailsReqBody dto);

    BetslipsHbDto getHbGameDetails(GameDetailsReqBody dto);

    BetslipsBbinGameDto getBbinGameDetails(GameDetailsReqBody dto);

    BetslipsBbinFishingExpertDto getBbinFishingExpertDetails(GameDetailsReqBody dto);

    BetslipsTcgDto getTcgLotteryDetails(GameDetailsReqBody dto);

    BetslipsSlottoDto getSlottoDetails(GameDetailsReqBody dto);

    BetslipsAgDto getAgLiveDetails(GameDetailsReqBody dto);

    BetslipsBbinLiveDto getBbinLiveGameDetails(GameDetailsReqBody dto);

    BetslipsWmDto getVmLiveDetails(GameDetailsReqBody dto);

    BetslipsDgDto getDgLiveDetails(GameDetailsReqBody dto);

    BetslipsGgDto getGgLiveDetails(GameDetailsReqBody dto);

    BetslipsDsDto getDsChessDetails(GameDetailsReqBody dto);

    BetslipsShabaSportsDto getIbcSportDetails(GameDetailsReqBody dto);

    BetslipsSboSportsDto getSboSportDetails(GameDetailsReqBody dto);

    BetslipsS128Dto getS128AnimalFightDetails(GameDetailsReqBody dto);

    BetslipsBbinFishingMasterDto getBbinFishingMasterDetails(GameDetailsReqBody dto);

    BetslipsAeSexyDto getAESexyLiveDetails(GameDetailsReqBody dto);

    BetslipsAeKingMakerDto getAEKingMakerChessDetails(GameDetailsReqBody dto);

    ResPage<SportBetslips> getSportRecord(ReqPage<QueryGameRecordDto> dto);

    BetslipsFuturesLotteryDto getFuturesDetails(GameDetailsReqBody dto);

    BetslipsCq9GameDto getCQ9GameDetails(GameDetailsReqBody dto);

    BetslipsCq9ThunderFighterDto getCQ9ThunderFighterDetails(GameDetailsReqBody dto);

    BetslipsCq9ParadiseDto getCQ9ParadiseDetails(GameDetailsReqBody dto);

    BetslipsCq9WaterMargin getCq9WaterMarginDetails(GameDetailsReqBody dto);

    BetslipsJokerGame getJokerGameDetails(GameDetailsReqBody dto);

    BetslipsJokerHaibaDto getJokerHaibaDetail(GameDetailsReqBody dto);

    BetslipsSv388Dto getSV388CockFightDetail(GameDetailsReqBody dto);

    BetslipsSaDto getSALiveDetails(GameDetailsReqBody dto);

    BetslipsBtiDto getBtiSportDetails(GameDetailsReqBody dto);

    BetslipsPgChessDto getPGChessDetails(GameDetailsReqBody dto);

    BetslipsPgGameDto getPGGameDetails(GameDetailsReqBody dto);

    BetslipsEbetDto getEBetDetails(GameDetailsReqBody dto);
}
