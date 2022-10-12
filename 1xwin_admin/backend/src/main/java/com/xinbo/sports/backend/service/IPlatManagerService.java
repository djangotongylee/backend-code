package com.xinbo.sports.backend.service;

import com.xinbo.sports.backend.io.bo.PlatManager;
import com.xinbo.sports.plat.io.dto.base.BetslipsDetailDto;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;

import java.util.List;

/**
 * <p>
 * 游戏管理->平台管理
 * </p>
 *
 * @author andy
 * @since 2020/6/8
 */
public interface IPlatManagerService {
    /**
     * 平台管理-列表
     *
     * @param reqBody
     * @return
     */
    ResPage<PlatManager.ListPlatResBody> listPlat(ReqPage<PlatManager.ListPlatReqBody> reqBody);

    /**
     * 平台管理-修改平台
     *
     * @param reqBody
     */
    void updatePlat(PlatManager.UpdatePlatReqBody reqBody);

    /**
     * 平台子游戏管理-列表
     *
     * @param dto
     * @return
     */
    ResPage<PlatManager.ListSubGameResBody> listSubGame(ReqPage<PlatManager.ListSubGameReqBody> dto);

    /**
     * 平台子游戏管理-修改平台
     *
     * @param reqBody
     */
    void updateSubGame(PlatManager.UpdateSubGameReqBody reqBody);

    /**
     * 游戏列表
     *
     * @return
     */
    List<PlatManager.GameListResBody> gameList();

    /**
     * 热门赛事配置列表
     *
     * @param dto 请求参数
     * @return 分页数据
     */

    ResPage<BetslipsDetailDto.SportSchedule> sportCompetitionList(ReqPage<BetslipsDetailDto.SportReqDto> dto);

    /**
     * 修改热门赛事配置
     *
     * @param reqDto 请求参数
     * @return Boolean
     */

    Boolean updateSportCompetition(PlatManager.UpdateSportCompetitionRedDto reqDto);

    /**
     * 删除热门赛事配置
     *
     * @param reqDto 请求参数
     * @return Boolean
     */

    Boolean deleteSportCompetition(PlatManager.DeleteSportCompetitionRedDto reqDto);

    /**
     * 队徽列表
     *
     * @return List<PlatManager.TeamLogoDto>
     */
    ResPage<PlatManager.TeamLogoDto> teamLogonList( ReqPage<PlatManager.TeamLogoReqDto> reqDto);

    /**
     * 新增队徽
     *
     * @return Boolean
     */
    Boolean addTeamLogon(PlatManager.TeamLogoDto reqDto);

    /**
     * 批量删除队徽
     *
     * @return Boolean
     */
    Boolean deleteTeamLogon(PlatManager.DeleteTeamLogoDto reqDto);
}
