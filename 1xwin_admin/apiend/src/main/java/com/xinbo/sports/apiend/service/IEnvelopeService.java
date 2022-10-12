package com.xinbo.sports.apiend.service;

import com.xinbo.sports.apiend.io.dto.promotions.EnvelopeDateResDto;
import com.xinbo.sports.utils.components.response.Result;

/**
 * @author: wells
 * @date: 2020/5/2
 * @description:
 */

public interface IEnvelopeService {
    /**
     * 优惠活动生成红包雨
     */
    //Result<List<EnvelopeResDto>> generateEnvelope();

    /**
     * 优惠活动->判断能否领取
     *
     * @return
     */
    void isReceive();

    /***
     * 优惠活动领取红包
     * @param reqDto
     * @return
     */
    // Result receiveEnvelope(receiveEnvelopeReqDto reqDto);

    /**
     * 优惠活动->红包领取时间
     *
     * @return
     */
    EnvelopeDateResDto envelopeDate();
}
