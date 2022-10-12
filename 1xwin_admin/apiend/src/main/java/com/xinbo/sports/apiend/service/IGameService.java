package com.xinbo.sports.apiend.service;

import com.xinbo.sports.apiend.io.bo.HomeParams;
import com.xinbo.sports.apiend.io.bo.PlatformParams;
import com.xinbo.sports.apiend.io.bo.PlatformParams.PlatListResInfo;
import com.xinbo.sports.apiend.io.dto.StartEndTime;
import com.xinbo.sports.apiend.io.dto.platform.*;
import com.xinbo.sports.plat.factory.PlatAbstractFactory;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams.PlatGameLoginResDto;
import com.xinbo.sports.plat.io.dto.base.BetslipsDetailDto;
import com.xinbo.sports.service.io.dto.BaseParams;
import com.xinbo.sports.service.io.dto.UserInfo;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.xinbo.sports.apiend.io.bo.PlatformParams.GameQueryDateDto;

/**
 * @author: David
 * @date: 06/04/2020
 * @description:
 */
public interface IGameService {
    /**
     * 获取有些列表
     *
     * @return 游戏列表
     */
    List<HomeParams.GameIndexResDto> list();

    /**
     * 登录三方有些
     *
     * @param dto {id, slotId}
     * @return 游戏列表
     */
    PlatGameLoginResDto login(GameLoginReqDto dto);

    /**
     * 三方上下分
     *
     * @param dto 入参
     * @return {id, coin, platCoin}
     */
    CoinTransferResDto coinTransfer(CoinTransferReqDto dto);

    GameModelDto checkGameStatus(Integer gameId);

    GameBalanceResDto queryBalanceByPlatId(GameBalanceReqDto dto);

    ResPage<PlatFactoryParams.PlatSlotGameResDto> slotGameList(ReqPage<SlotGameReqDto> dto);

    Boolean slotGameFavorite(SlotGameFavoriteReqDto dto);

    <T> T platFactory(Integer gameId);

    /**
     * 根据 PlatId 获取游戏父工厂类
     *
     * @param platId 平台ID
     * @return 游戏工厂父类
     * @author David
     * @date 07/05/2020
     */
    PlatAbstractFactory platFactoryByPlatId(Integer platId);

    /**
     * 游戏列表
     *
     * @return 游戏列表
     */
    List<GameListInfo> gameList();

    /**
     * 游戏列表
     *
     * @return 游戏列表
     */
    List<PlatListResInfo> platList();

    /**
     * 根据交易日期获取投注列表
     *
     * @param dto {gameId, startTime, endTime}
     * @return 投注信息列表
     */
    ResPage<PlatFactoryParams.PlatBetListResDto> getBetListByDate(ReqPage<GameQueryDateDto> dto);

    /**
     * 根据交易日期获取当前游戏的 投注总金额、输赢总金额
     *
     * @param dto {gameId, startTime, endTime}
     * @return 投注总金额、输赢总金额
     */
    PlatFactoryParams.PlatCoinStatisticsResDto getCoinStatisticsByDate(GameQueryDateDto dto);

    /**
     * 根据交易日期获取当前游戏的 上分、下分总金额
     *
     * @param dto {gameId, startTime, endTime}
     * @return 投注总金额、输赢总金额
     */
    PlatformParams.PlatTransferResInfo getCoinTransferByDate(StartEndTime dto);

    /**
     * 批量下分
     *
     * @param dto        dto
     * @param userInfo   userInfo
     * @param headerInfo headerInfo
     * @return CoinTransferResDto
     */
    CoinTransferResDto coinTransfer(CoinTransferReqDto dto, UserInfo userInfo, BaseParams.HeaderInfo headerInfo);

    ResPage<BetslipsDetailDto.SportSchedule> getSportSchedule(ReqPage<BetslipsDetailDto.SportScheduleReqDto> dto);

    BetslipsDetailDto.ForwardEvent forwardEvent(BetslipsDetailDto.ForwardEventReq reqDto);

     <T>T verifySession(HttpServletRequest request,String path);

}
