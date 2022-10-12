package com.xinbo.sports.apiend.service;

import com.xinbo.sports.apiend.io.dto.centeragent.DepositOrWithdrawalReqBody;
import com.xinbo.sports.apiend.io.dto.centeragent.DepositOrWithdrawalResBody;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;

/**
 * <p>
 * 代理中心->充值提现
 * </p>
 *
 * @author andy
 * @since 2020/4/22
 */
public interface ICenterAgentDepositOrWithdrawalService {

    /**
     * 代理中心->充值提现
     *
     * @param reqBody reqBody
     * @return 下级分页列表(六个层级)
     */
    ResPage<DepositOrWithdrawalResBody> depositOrWithdrawal(ReqPage<DepositOrWithdrawalReqBody> reqBody);
}
