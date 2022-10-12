package com.xinbo.sports.backend.service;

import com.xinbo.sports.backend.io.dto.FinancialAdminParameter.AdminTransferReqDto;
import com.xinbo.sports.backend.io.dto.FinancialAdminParameter.PromotionsListReqDto;
import com.xinbo.sports.backend.io.dto.FinancialAdminParameter.UserCoinReqDto;
import com.xinbo.sports.backend.io.dto.FinancialAdminParameter.UserCoinResDto;

import java.util.List;


/**
 * @author: wells
 * @date: 2020/8/13
 * @description:
 */

public interface FinancialAdminService {
    /**
     * 人工调账
     *
     * @param reqDto
     * @return
     */
    boolean adminTransfer(AdminTransferReqDto reqDto);

    /**
     * 查询用户余额
     *
     * @param reqDto
     * @return
     */
    UserCoinResDto getUserCoin(UserCoinReqDto reqDto);

    /**
     * 获取活动列表
     *
     * @return
     */
    List<PromotionsListReqDto> getPromotionsList();
}
