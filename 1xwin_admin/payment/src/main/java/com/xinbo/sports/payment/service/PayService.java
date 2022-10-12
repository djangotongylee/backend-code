package com.xinbo.sports.payment.service;

import com.xinbo.sports.payment.io.PayParams.*;

import java.util.List;

/**
 * @author: David
 * @date: 14/07/2020
 */
public interface PayService {
    /**
     * 线上支付
     *
     * @return 支付列表
     */
    PayList list();

    /**
     * 线上支付
     *
     * @param dto 入参
     * @return {category, url}
     */
    PayOnlineResDto onlinePay(PaymentReqDto dto);

    /**
     * 线下支付
     *
     * @param dto 入参
     * @return {category, url}
     */
    PayOfflineResDto offlinePay(PaymentReqDto dto);

    /**
     *
     * 线上提现
     *
     * @param dto
     * @return
     */
    WithdrawalNotifyResDto onlineWithdraw(WithdrawalReqDto dto);

    /**
     * 离线支付|在线支付
     *
     * @param dto
     * @return
     */
    Object payment(Payment dto);

    /**
     * 离线支付|在线支付
     *
     * @return
     */
    List<PayListResBody> payList();

    Boolean checkPayment();
}
