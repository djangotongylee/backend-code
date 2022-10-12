package com.xinbo.sports.apiend.service;

import com.xinbo.sports.apiend.io.dto.wallet.WithdrawalAddReqBody;

/**
 * <p>
 * 提现业务处理接口
 * </p>
 *
 * @author andy
 * @since 2020/4/20
 */
public interface IWithdrawalService {
    /**
     * 添加提现记录
     */
    boolean addWithdrawal(WithdrawalAddReqBody reqBody);
}
