package com.xinbo.sports.backend.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import com.xinbo.sports.backend.io.bo.FinancialRecords;
import com.xinbo.sports.backend.service.IFinancialRecordsService;
import com.xinbo.sports.service.base.DepositOrWithdrawalBase;
import com.xinbo.sports.service.base.websocket.MessageBo;
import com.xinbo.sports.service.io.enums.BaseEnum;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;
import com.xinbo.sports.utils.components.response.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * <p>
 * 财务管理-财务记录
 * </p>
 *
 * @author andy
 * @since 2020/8/14
 */
@RestController
@ApiSupport(author = "Andy")
@RequestMapping("/v1/financialRecords")
@Api(tags = "财务管理 -> 财务记录", value = "FinancialRecordsController")
public class FinancialRecordsController {
    @Resource
    private IFinancialRecordsService financialRecordsServiceImpl;
    @Resource
    private DepositOrWithdrawalBase depositOrWithdrawalBase;

    @ApiOperationSupport(author = "Andy", order = 1)
    @ApiOperation(value = "出款记录-列表", notes = "出款记录-列表")
    @PostMapping("/withdrawalList")
    public Result<ResPage<FinancialRecords.WithdrawalListResBody>> withdrawalList(@Valid @RequestBody ReqPage<FinancialRecords.WithdrawalListReqBody> reqBody) {
        return Result.ok(financialRecordsServiceImpl.withdrawalList(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 2)
    @ApiOperation(value = "出款记录-统计", notes = "出款记录-统计")
    @PostMapping("/withdrawalStatistics")
    public Result<FinancialRecords.CommonCoinResBody> withdrawalStatistics(@Valid @RequestBody FinancialRecords.WithdrawalListReqBody reqBody) {
        return Result.ok(financialRecordsServiceImpl.withdrawalStatistics(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 5)
    @ApiOperation(value = "入款记录-列表", notes = "入款记录-列表")
    @PostMapping("/depositList")
    public Result<ResPage<FinancialRecords.DepositListResBody>> depositList(@Valid @RequestBody ReqPage<FinancialRecords.DepositListReqBody> reqBody) {
        return Result.ok(financialRecordsServiceImpl.depositList(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 6)
    @ApiOperation(value = "入款记录-统计", notes = "入款记录-统计")
    @PostMapping("/depositStatistics")
    public Result<FinancialRecords.CommonCoinResBody> depositStatistics(@Valid @RequestBody FinancialRecords.DepositListReqBody reqBody) {
        return Result.ok(financialRecordsServiceImpl.depositStatistics(reqBody));
    }


    @ApiOperationSupport(author = "Andy", order = 7)
    @ApiOperation(value = "调账记录-列表", notes = "调账记录-列表")
    @PostMapping("/adminTransferList")
    public Result<ResPage<FinancialRecords.AdminTransferListResBody>> adminTransferList(@Valid @RequestBody ReqPage<FinancialRecords.AdminTransferListReqBody> reqBody) {
        return Result.ok(financialRecordsServiceImpl.adminTransferList(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 8)
    @ApiOperation(value = "调账记录-统计", notes = "调账记录-统计")
    @PostMapping("/adminTransferStatistics")
    public Result<FinancialRecords.CommonCoinResBody> adminTransferStatistics(@Valid @RequestBody FinancialRecords.AdminTransferListReqBody reqBody) {
        return Result.ok(financialRecordsServiceImpl.adminTransferStatistics(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 11)
    @ApiOperation(value = "平台转账-列表", notes = "平台转账-列表")
    @PostMapping("/platTransferList")
    public Result<ResPage<FinancialRecords.PlatTransferListResBody>> platTransferList(@Valid @RequestBody ReqPage<FinancialRecords.PlatTransferListReqBody> reqBody) {
        return Result.ok(financialRecordsServiceImpl.platTransferList(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 12)
    @ApiOperation(value = "平台转账-统计", notes = "平台转账-统计")
    @PostMapping("/platTransferStatistics")
    public Result<FinancialRecords.PlatTransferStatisticsResBody> platTransferStatistics(@Valid @RequestBody FinancialRecords.PlatTransferListReqBody reqBody) {
        return Result.ok(financialRecordsServiceImpl.platTransferStatistics(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 13)
    @ApiOperation(value = "获取推送笔数的数据->提款", notes = "获取推送笔数的数据->提款")
    @PostMapping("/getPushWnData")
    public Result<MessageBo> getPushWnData() {
        return Result.ok(depositOrWithdrawalBase.getPushWnData(BaseEnum.MessageDevice.B.getCode()));
    }

    @ApiOperationSupport(author = "Andy", order = 14)
    @ApiOperation(value = "获取推送笔数的数据->存款", notes = "获取推送笔数的数据->存款")
    @PostMapping("/getPushDnData")
    public Result<MessageBo> getPushDnData() {
        return Result.ok(depositOrWithdrawalBase.getPushDnData(BaseEnum.MessageDevice.B.getCode()));
    }

    @ApiOperationSupport(author = "Andy", order = 15)
    @ApiOperation(value = "平台转账->状态更新:成功或失败", notes = "平台转账->状态更新:成功或失败")
    @PostMapping("/updatePlatTransferStatusById")
    public Result<Boolean> updatePlatTransferStatusById(@Valid @RequestBody FinancialRecords.UpdatePlatTransferStatusByIdReqBody reqBody) {
        return Result.ok(financialRecordsServiceImpl.updatePlatTransferStatusById(reqBody));
    }
    @ApiOperationSupport(author = "Andy", order = 16)
    @ApiOperation(value = "代付记录", notes = "代付记录")
    @PostMapping("/onlineWithdrawalList")
    public Result<ResPage<FinancialRecords.OnlineWithdrawalListResBody>> onlineWithdrawalList(@Valid @RequestBody ReqPage<FinancialRecords.OnlineWithdrawalListReqBody> reqBody) {
        return Result.ok(financialRecordsServiceImpl.onlineWithdrawalList(reqBody));
    }
    @ApiOperationSupport(author = "Andy", order = 17)
    @ApiOperation(value = "代付记录-统计", notes = "代付记录-统计")
    @PostMapping("/onlineWithdrawalStatistics")
    public Result<FinancialRecords.CommonCoinResBody> onlineWithdrawalStatistics(@Valid @RequestBody FinancialRecords.OnlineWithdrawalListReqBody reqBody) {
        return Result.ok(financialRecordsServiceImpl.onlineWithdrawalStatistics(reqBody));
    }
    @ApiOperationSupport(author = "Andy", order = 18)
    @ApiOperation(value = "更新代付状态", notes = "更新代付状态")
    @PostMapping("/updateOnlineWithdrawalStatus")
    public Result<Boolean> updateOnlineWithdrawalStatus(@Valid @RequestBody FinancialRecords.UpdateOnlineWithdrawalReqBody reqBody) {
        return Result.ok(financialRecordsServiceImpl.updateOnlineWithdrawalStatus(reqBody));
    }
}