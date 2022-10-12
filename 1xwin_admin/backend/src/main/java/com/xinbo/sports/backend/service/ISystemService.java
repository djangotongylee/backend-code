package com.xinbo.sports.backend.service;

import com.xinbo.sports.backend.io.dto.System;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;
import com.xinbo.sports.utils.components.response.CodeInfo;

import java.util.List;

/**
 * <p>
 * 系统设置
 * </p>
 *
 * @author andy
 * @since 2020/10/5
 */
public interface ISystemService {
    /**
     * Banner图 -> 列表查询
     *
     * @param reqBody reqBody
     * @return Banner图列表
     */
    List<System.BannerListResBody> bannerList(System.BannerListReqBody reqBody);

    /**
     * Banner图 -> 新增
     *
     * @param reqBody reqBody
     * @return true or false
     */
    boolean addBanner(System.AddBannerReqBody reqBody);

    /**
     * Banner图 -> 修改
     *
     * @param reqBody reqBody
     * @return true or false
     */
    boolean updateBanner(System.UpdateBannerReqBody reqBody);

    /**
     * Banner图 -> 删除
     *
     * @param reqBody reqBody
     * @return true or false
     */
    boolean delBanner(System.DelBannerReqBody reqBody);

    /**
     * 预设开奖->列表
     *
     * @param reqBody reqBody
     * @return 分页数据
     */
    ResPage<System.GetOpenPresetListResBody> getOpenPresetList(ReqPage<System.GetOpenPresetListReqBody> reqBody);

    /**
     * 预设开奖->新增/修改
     *
     * @param reqBody reqBody
     * @return true
     */
    boolean saveOrUpdateOpenPreset(System.SaveOrUpdateOpenPresetReqBody reqBody);

    /**
     * 预设开奖->删除
     *
     * @param reqBody reqBody
     * @return true
     */
    boolean deleteOpenPreset(System.DeleteOpenPresetReqBody reqBody);

    /**
     * Config init
     *
     * @return 初始化sp_config表数据
     */
    List<System.InitResDto> init();

    /**
     * 预设开奖->导出
     *
     * @param reqBody
     * @return
     */
    List<System.ExportOpenPresetListResBody> exportOpenPresetList(System.ExportOpenPresetListReqBody reqBody);

    /**
     * 预设开奖->新增批量预设
     *
     * @param reqBody
     * @return
     */
    boolean saveOrUpdateBatchOpenPreset(System.SaveOrUpdateBatchOpenPresetReqBody reqBody);

    /**
     * 预设开奖->获取当前期号信息
     *
     * @param reqBody
     * @return
     */
    System.GetLotteryActionNoResBody getLotteryActionNo(System.GetLotteryActionNoReqBody reqBody);

    /**
     * 根据语言获取Banner信息
     *
     * @param reqDto 请求参数实例
     * @return
     */
    System.BannerByLangResDto geBannersByLang(System.BannerByLangReqDto reqDto);

    /**
     * 查询佣金规则
     *
     * @return System.CommissionRule
     */
    System.CommissionRuleDto getCommissionRule();

    /**
     * 修改佣金规则
     *
     * @param reqDto 请求参数
     * @return Boolean
     */
    Boolean updateCommissionRule(System.CommissionRuleDto reqDto);

    /**
     * 查询平台配置
     *
     * @return RegisterLoginConfigDto
     */
    System.PlatConfigDto getPlatConfig();

    /**
     * 修改平台配置
     *
     * @param reqDto 修改参数
     * @return boolean
     */
    Boolean updatePlatConfig(System.PlatConfigDto reqDto);

    /**
     * 预设开奖概率分布
     *
     * @param reqDto reqDto
     * @return OpenRateDistributeReqDto
     */
    System.OpenRateDistributeResDto getOpenRateDistribute(System.OpenRateDistributeReqDto reqDto);

    /**
     * 查询在线客服配置
     *
     * @return OnlineServiceConfigDto
     */
    System.OnlineServiceConfigDto getOnlineServiceConfig();

    /**
     * 修改在线客服配置
     *
     * @param reqDto OnlineServiceConfigDto
     * @return Boolean
     */
    Boolean updateOnlineServiceConfig(System.OnlineServiceConfigDto reqDto);
}
