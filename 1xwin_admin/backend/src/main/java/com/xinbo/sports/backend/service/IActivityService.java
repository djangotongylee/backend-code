package com.xinbo.sports.backend.service;

import com.xinbo.sports.backend.io.dto.PromotionsParameter.*;
import com.xinbo.sports.dao.generator.po.RegisterRewards;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.List;


/**
 * @author: wells
 * @date: 2020/6/5
 * @description:活动配置
 */

public interface IActivityService {
    /***
     * 活动组
     * @return
     */
    List<PromotionsGroupResDto> promotionsGroup();

    /**
     * 活动列表
     *
     * @param reqDto
     * @return
     */
    ResPage<ListResDto> promotionsList(ReqPage<ListReqDto> reqDto);

    /**
     * 修改活动
     *
     * @param reqDto
     * @return
     */
    boolean saveOrUpdatePromotions(SavaOrUpdateReqDto reqDto);

    /**
     * 删除活动
     *
     * @param reqDto
     * @return
     */
    boolean deletePromotions(DeleteReqDto reqDto);

    /**
     * 签到列表
     *
     * @param reqDto
     * @return
     */
    SingListResDto signList(ReqPage<SingListReqDto> reqDto);

    /**
     * 签到详情
     *
     * @param reqDto
     * @return
     */
    ResPage<SingDetailResDto> signDetail(ReqPage<SingDetailReqDto> reqDto);

    /**
     * 首充活动列表
     *
     * @param reqDto
     * @return
     */
    FirstDepositResDto firstDepositList(ReqPage<RewardsReqDto> reqDto);

    /**
     * 体育与真人列表
     *
     * @param reqDto
     * @return
     */
    SportAndLiveResDto sportsAndLiveList(@Valid @RequestBody ReqPage<RewardsReqDto> reqDto);

    /**
     * 红包雨列表
     *
     * @param reqDto
     * @return
     */
    RedEnvelopeWarListResDto redEnvelopeWarList(ReqPage<RewardsReqDto> reqDto);

    /**
     * 全场返水列表
     *
     * @param reqDto
     * @return
     */
    AllRebateListResDto allRebateList(ReqPage<AllRebateListReqDto> reqDto);

    /**
     * 邀请好友返水列表
     *
     * @param reqDto
     * @return
     */
    FriendRebateListResDto friendRebateList(ReqPage<RewardsReqDto> reqDto);

    /**
     * 礼品领取列表
     *
     * @param reqDto
     * @return
     */
    ResPage<GiftReceiveListResDto> giftReceiveList(ReqPage<GiftReceiveListReqDto> reqDto);

    /**
     * 礼品领取修改状态
     *
     * @param reqDto
     * @return
     */
    boolean giftReceiveUpdate(GiftReceiveUpdateReqDto reqDto);

    /**
     * 补充单号详情
     *
     * @param reqDto
     * @return
     */
    OrderDetailDto orderDetail(OrderDetailReqDto reqDto);

    /**
     * 补充单号修改
     *
     * @param reqDto
     * @return
     */
    boolean orderUpdate(OrderDetailDto reqDto);


    /**
     * 活动默认模板
     *
     * @param reqDto
     * @return
     */
    RewardsReDto promotionsDefault(ReqPage<RewardsReqDto> reqDto);

    /**
     * 根据语言获取活动信息
     *
     * @param reqDto :
     * @Return com.xinbo.sports.backend.io.dto.PromotionsParameter.PromotionsByLangReqDto
     **/
    PromotionsByLangResDto getPromotionsByLang(PromotionsByLangReqDto reqDto);

    /**
     * 平台语言列表
     *
     * @Return java.util.List<com.xinbo.sports.backend.io.dto.PromotionsParameter.PlatLangResDto>
     **/
    List<PlatLangResDto> getPlatLang();

    /**
     * 注册彩金列表
     * @param reqDto  注册彩金列表请求实体类
     * @return registerRewardsListResDto
     */
    ResPage<RegisterRewardsListResDto> registerRewardsList(ReqPage<RegisterRewardsListReqDto> reqDto);

    /**
     *
     * @param reqDto 注册彩金修改响应实体类
     * @return Boolean
     */
    Boolean registerRewardsUpdate(RegisterRewardsUpdateReqDto reqDto);
}