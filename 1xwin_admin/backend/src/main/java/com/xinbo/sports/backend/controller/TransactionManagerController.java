package com.xinbo.sports.backend.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.xinbo.sports.backend.aop.annotation.UnCheckLog;
import com.xinbo.sports.backend.io.bo.TransactionManager;
import com.xinbo.sports.backend.service.ITransactionManagerService;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;
import com.xinbo.sports.utils.components.response.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * <p>
 * 交易管理
 * </p>
 *
 * @author andy
 * @since 2020/3/12
 */
@Slf4j
@RestController
@RequestMapping("/v1/transaction")
@Api(tags = "交易管理")
public class TransactionManagerController {
    @Resource
    private ITransactionManagerService transactionManagerServiceImpl;

    @ApiOperationSupport(author = "Andy", order = 1)
    @ApiOperation(value = "交易记录-列表查询", notes = "交易记录-列表查询")
    @PostMapping("/listTransactionRecord")
    public Result<ResPage<TransactionManager.TransactionRecord>> listTransactionRecord(@Valid @RequestBody ReqPage<TransactionManager.ListTransactionRecordReqBody> reqBody) {
        return Result.ok(transactionManagerServiceImpl.listTransactionRecord(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 2)
    @ApiOperation(value = "交易记录-统计", notes = "交易记录-统计")
    @PostMapping("/statisticsTransaction")
    public Result<TransactionManager.StatisticsTransaction> statisticsTransaction(@Valid @RequestBody TransactionManager.ListTransactionRecordReqBody reqBody) {
        return Result.ok(transactionManagerServiceImpl.statisticsTransaction(reqBody));
    }

    @UnCheckLog
    @ApiOperationSupport(author = "Andy", order = 3)
    @ApiOperation(value = "投注总额->列表", notes = "投注总额->列表")
    @PostMapping("/getPlatBetTotalList")
    public Result<ResPage<TransactionManager.PlatBetTotalListResBody>> getPlatBetTotalList(@Valid @RequestBody ReqPage<TransactionManager.PlatBetTotalListReqBody> reqBody) {
        return Result.ok(transactionManagerServiceImpl.getPlatBetTotalList(reqBody));
    }

    @UnCheckLog
    @ApiOperationSupport(author = "Andy", order = 4)
    @ApiOperation(value = "投注总额->统计", notes = "投注总额->统计")
    @PostMapping("/getPlatBetTotalStatistics")
    public Result<TransactionManager.PlatBetTotalStatisticsResBody> getPlatBetTotalStatistics(@Valid @RequestBody TransactionManager.PlatBetTotalListReqBody reqBody) {
        return Result.ok(transactionManagerServiceImpl.getPlatBetTotalStatistics(reqBody));
    }
}
