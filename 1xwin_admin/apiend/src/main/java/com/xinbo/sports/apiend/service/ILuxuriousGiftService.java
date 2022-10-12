package com.xinbo.sports.apiend.service;

import com.xinbo.sports.apiend.io.dto.promotions.LuxuriousGiftListReqDto;
import com.xinbo.sports.apiend.io.dto.promotions.LuxuriousGiftListResDto;
import com.xinbo.sports.apiend.io.dto.promotions.LuxuriousGiftReceiveResDto;
import com.xinbo.sports.service.io.dto.promotions.LuxuriousGiftReceiveReqDto;

/**
 * @author: wells
 * @date: 2020/5/8
 * @description:
 */

public interface ILuxuriousGiftService {
    /**
     * 优惠活动->豪礼列表
     *
     * @param reqDto
     * @return
     */
    LuxuriousGiftListResDto luxuriousGiftList(LuxuriousGiftListReqDto reqDto);

    /**
     * 优惠活动->豪礼领取
     *
     * @param reqDto
     * @return
     */
    LuxuriousGiftReceiveResDto luxuriousGiftReceive(LuxuriousGiftReceiveReqDto reqDto);
}
