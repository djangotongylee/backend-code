package com.xinbo.sports.backend.service;

import com.xinbo.sports.backend.io.bo.TransactionManager;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;

/**
 * <p>
 * 交易管理
 * </p>
 *
 * @author andy
 * @since 2020/6/15
 */
public interface ITransactionManagerService {
    /**
     * 交易记录-列表查询
     *
     * @param reqBody reqBody
     * @return 列表集合
     */
    ResPage<TransactionManager.TransactionRecord> listTransactionRecord(ReqPage<TransactionManager.ListTransactionRecordReqBody> reqBody);

    /**
     * 交易记录-统计
     *
     * @param reqBody reqBody
     * @return 统计结果
     */
    TransactionManager.StatisticsTransaction statisticsTransaction(TransactionManager.ListTransactionRecordReqBody reqBody);

    /**
     * 交易记录->全平台投注总额->列表
     *
     * @param reqBody reqBody
     * @return ResPage<TransactionManager.BetTotalListResBody>
     */
    ResPage<TransactionManager.PlatBetTotalListResBody> getPlatBetTotalList(ReqPage<TransactionManager.PlatBetTotalListReqBody> reqBody);

    /**
     * 交易记录->全平台投注总额->统计
     *
     * @param reqBody reqBody
     * @return PlatBetTotalStatisticsResBody
     */
    TransactionManager.PlatBetTotalStatisticsResBody getPlatBetTotalStatistics(TransactionManager.PlatBetTotalListReqBody reqBody);
}
