package com.xinbo.sports.backend.service;

import com.xinbo.sports.backend.io.dto.Platform;
import com.xinbo.sports.plat.factory.PlatAbstractFactory;

import java.util.List;


/**
 * @author: David
 * @date: 06/04/2020
 * @description:
 */
public interface IPlatformService {

    /**
     * 第三方平台余额查询
     *
     * @param dto dto
     * @return 第三方平台余额
     */
    Platform.GameBalanceResDto queryBalanceByPlatId(Platform.GameBalanceReqDto dto);

    /**
     * 游戏列表
     *
     * @return 游戏列表
     */
    List<Platform.GameListInfo> gameList(Platform.GameListReqDto dto);

    /**
     * 游戏列表
     *
     * @return 游戏列表
     */
    List<Platform.PlatListResInfo> platList();

    /**
     * 初始化SBO代理信息
     *
     * @return true-成功 false-失败
     */
    Boolean createAgentSbo();

    /**
     * 上、下分状态异常补单
     *
     * @param dto 订单号
     * @return true-成功 false-失败
     */
    Boolean checkTransferStatus(Platform.CheckTransferStatusReqDto dto);

    /**
     * 生成补单订单号
     *
     * @param dto 游戏ID、开始～结束时间
     * @return True-成功 False-失败
     */
    Boolean genBetSlipsSupplemental(Platform.GenBetSlipsSupplementalReqDto dto);

    /**
     * 拉单补单
     *
     * @param dto 补单订单号
     * @return True-成功 False-失败
     */
    Boolean betSlipsSupplemental(Platform.BetSlipsSupplementalReqDto dto);

    /**
     * 根据GameID 获取游戏处理工厂类
     *
     * @param gameId 游戏ID
     * @param <T>    范型
     * @return 游戏工厂/老虎机工厂
     * @author David
     * @date 07/05/2020
     */
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
     * 拉单异常补单
     *
     * @param dto 补单订单号
     * @return True-成功 False-失败
     */
    Boolean betSlipsException(Platform.BetSlipsExceptionReqDto dto);

    /**
     * 同步老虎机游戏列表
     *
     * @param dto 补单订单号
     * @return True-成功 False-失败
     */
    Boolean syncSlotGameList(Platform.SyncSlotGameReqDto dto);

    /**
     * 游戏类型->平台名称->游戏名称->三级联动
     *
     * @return
     */
    List<Platform.GameGroupDict> getCascadeByGameGroupList();

}
