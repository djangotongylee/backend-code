package com.xinbo.sports.apiend.service;

import com.xinbo.sports.apiend.io.dto.center.GiftRecordsDetailsResDto;
import com.xinbo.sports.apiend.io.dto.center.GiftRecordsReqDto;
import com.xinbo.sports.apiend.io.dto.center.GiftRecordsResDto;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;

/**
 * @author: David
 * @date: 06/04/2020
 * @description:
 */
public interface ICenterService {
    /**
     * 礼物记录列表
     *
     * @param dto 请求DTO
     * @return 礼物记录列表
     */
    ResPage<GiftRecordsResDto> giftRecords(ReqPage<GiftRecordsReqDto> dto);

    /**
     * 礼物记录详情
     *
     * @param id 主键ID
     * @return 礼物记录详情
     */
    GiftRecordsDetailsResDto giftRecordsDetails(Long id);
}
