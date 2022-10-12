package com.xinbo.sports.backend.service;

import com.xinbo.sports.backend.io.dto.FinancialDepositParameter.*;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;
import org.springframework.stereotype.Service;

/**
 * @author: wells
 * @date: 2020/8/12
 * @description:
 */

@Service
public interface FinancialDepositService {
    /**
     * 充值列表
     *
     * @param reqDto
     * @return
     */
     ResPage<DepositListResDto> depositList(ReqPage<DepositListReqDto> reqDto);

    /**
     * 充值列表汇总
     *
     * @param reqDto
     * @return
     */
    DepositSumResDto depositSum(DepositListReqDto reqDto);

    /**
     * 修改充值记录
     *
     * @param reqDto
     * @return
     */
    UpdateDepositStatusResDto updateDepositStatus(UpdateDepositStatusReqDto reqDto);

    /**
     * 充值记录详情
     *
     * @param reqDto
     * @return
     */
    DepositDetailResDto depositDetail(DepositDetailReqDto reqDto);
}
