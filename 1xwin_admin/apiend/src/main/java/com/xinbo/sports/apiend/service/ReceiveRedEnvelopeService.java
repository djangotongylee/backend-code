package com.xinbo.sports.apiend.service;

import com.xinbo.sports.apiend.io.dto.promotions.ReceiveEnvelopeResDto;
import com.xinbo.sports.apiend.io.dto.promotions.ReceiveResDto;

/**
 * @Author : Wells
 * @Date : 2021-01-13 12:05 上午
 * @Description : 红包领取
 */
public interface ReceiveRedEnvelopeService {
    /**
     * 红包领取
     *
     * @return ReceiveEnvelopeDto
     */
    ReceiveEnvelopeResDto receiveRedEnvelope(ReceiveResDto reqDto);
}
