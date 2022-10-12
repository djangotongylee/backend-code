package com.xinbo.sports.backend.service;

import com.xinbo.sports.backend.io.bo.PayManager;
import com.xinbo.sports.dao.generator.po.PayBankList;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 支付管理
 * </p>
 *
 * @author andy
 * @since 2020/6/8
 */
public interface IPayManagerService {

    /**
     * 线上平台配置-列表
     *
     * @param reqBody reqBody
     * @return 列表
     */
    ResPage<PayManager.PayPlatListResBody> payPlatList(ReqPage<PayManager.PayPlatListReqBody> reqBody);

    /**
     * 线上平台配置-新增
     *
     * @param reqBody reqBody
     */
    void payPlatAdd(PayManager.PayPlatAddReqBody reqBody);

    /**
     * 线上平台配置-修改
     *
     * @param reqBody reqBody
     */
    void payPlatUpdate(PayManager.PayPlatUpdateReqBody reqBody);

    /**
     * 线上平台配置-详情
     *
     * @param reqBody reqBody
     * @return 详情
     */
    PayManager.PayPlatDetailResBody payPlatDetail(PayManager.CommonIdReq reqBody);

    /**
     * 线上渠道配置-列表
     *
     * @param reqBody reqBody
     * @return 列表
     */
    ResPage<PayManager.PayOnLineListResBody> payOnLineList(ReqPage<PayManager.PayOnLineListReqBody> reqBody);

    /**
     * 线上渠道配置-修改
     *
     * @param reqBody reqBody
     */
    void payOnLineUpdate(PayManager.PayOnLineUpdateReqBody reqBody);


    /**
     * 线下渠道配置-列表
     *
     * @param reqBody reqBody
     * @return 列表
     */
    ResPage<PayManager.PayOffLineListResBody> payOffLineList(ReqPage<PayManager.PayOffLineListReqBody> reqBody);

    /**
     * 线下渠道配置-新增
     *
     * @param reqBody reqBody
     */
    void payOffLineAdd(PayManager.PayOffLineAddReqBody reqBody);

    /**
     * 线下渠道配置-修改
     *
     * @param reqBody reqBody
     */
    void payOffLineUpdate(PayManager.PayOffLineUpdateReqBody reqBody);

    /**
     * 线下渠道配置-详情
     *
     * @param reqBody reqBody
     * @return 详情
     */
    PayManager.PayOffLineDetailResBody payOffLineDetail(PayManager.CommonIdReq reqBody);

    ResPage<PayManager.PayoutOnLineListResBody> payoutOnLineList(ReqPage<PayManager.PayoutOnLineListReqBody> reqBody);

    Boolean payoutOnLineUpdate(PayManager.PayoutOnLineUpdateReqBody reqBody);

    Boolean addPayoutOnLine(PayManager.PayoutOnLineAddReqBody reqBody);

    List<PayManager.PayoutBankList> listBank(PayManager.ListBankReqDto dto);

    List<PayManager.PayoutPayNameListResBody> payoutPayNameList();
}
