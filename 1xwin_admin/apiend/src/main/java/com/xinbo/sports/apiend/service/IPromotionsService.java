package com.xinbo.sports.apiend.service;

import com.xinbo.sports.apiend.io.dto.promotions.*;
import com.xinbo.sports.service.io.dto.promotions.ApplicationActivityReqDto;

import java.util.List;

/**
 * @author: wells
 * @date: 2020/4/14
 * @description:优惠活动
 */

public interface IPromotionsService {
    /**
     * 优惠活动->列表
     *
     * @return
     */
    List<PromotionsGroupResDto> promotionsList();

    /**
     * 优惠活动->详情
     *
     * @return
     */
    PromotionsInfoResDto promotionsInfo(PromotionsInfoReqDto reqDto);

    /**
     * 优优惠活动->全场返水
     *
     * @return
     */
    List<LevelRebateListResDto> levelRebateList();

    /**
     * 优惠活动->vip会员成长
     *
     * @return
     */
    List<LevelListResDto> levelList();

    /**
     * 优惠活动->每日签到
     *
     * @return
     */
    UserSingResDto userSign();

    /**
     * 优惠活动->查看每日签到
     *
     * @return
     */
    UserSingResDto userSignList();

    /**
     * 优惠活动->申请优惠活动
     *
     * @param reqDto
     * @return
     */
    void applicationActivity(ApplicationActivityReqDto reqDto);

    /**
     * 优惠活动vip专属
     *
     * @return
     */
    VipExclusiveResDto vipExclusive();

    /**
     * 优惠活动领取生日礼金
     *
     * @return
     */
    void birthdayGift();


}
