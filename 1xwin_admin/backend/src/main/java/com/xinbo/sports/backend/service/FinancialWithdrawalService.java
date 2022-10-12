package com.xinbo.sports.backend.service;

import com.xinbo.sports.backend.io.dto.FinancialWithdrawalParameter.*;
import com.xinbo.sports.service.io.dto.AuditBaseParams.AuditReqDto;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;

/**
 * @author: wells
 * @date: 2020/8/13
 * @description:
 */

public interface FinancialWithdrawalService {
    /**
     * 提款列表
     *
     * @param reqDto
     * @return
     */
     ResPage<WithdrawalListResDto> withdrawalList(ReqPage<WithdrawalListReqDto> reqDto);

    /**
     * 提款列表汇总
     *
     * @param reqDto
     * @return
     */
    WithdrawalSumResDto withdrawalSum(WithdrawalListReqDto reqDto);

    /**
     * 修改提款记录
     *
     * @param reqDto
     * @return
     */
    UpdateWithdrawalStatusResDto updateWithdrawalStatus(UpdateWithdrawalStatusReqDto reqDto);

    /**
     * 提款记录详情
     *
     * @param reqDto
     * @return
     */
    WithdrawalDetailResDto withdrawalDetail(WithdrawalDetailReqDto reqDto);

    /**
     * 稽核
     *
     * @param reqDto
     * @return
     */
    UpdateWithdrawalStatusResDto isAudit(AuditReqDto reqDto);

    /**
     * 稽核详情
     *
     * @param reqDto
     * @return
     */
    AuditDetailResDto auditDetail(ReqPage<AuditReqDto> reqDto);


}
