package com.xinbo.sports.backend.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import com.xinbo.sports.backend.io.dto.FinancialAdminParameter.AdminTransferReqDto;
import com.xinbo.sports.backend.io.dto.FinancialAdminParameter.PromotionsListReqDto;
import com.xinbo.sports.backend.io.dto.FinancialAdminParameter.UserCoinReqDto;
import com.xinbo.sports.backend.io.dto.FinancialAdminParameter.UserCoinResDto;
import com.xinbo.sports.backend.io.dto.FinancialDepositParameter.*;
import com.xinbo.sports.backend.io.dto.FinancialWithdrawalParameter.*;
import com.xinbo.sports.backend.service.FinancialAdminService;
import com.xinbo.sports.backend.service.FinancialDepositService;
import com.xinbo.sports.backend.service.FinancialWithdrawalService;
import com.xinbo.sports.service.io.dto.AuditBaseParams;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;
import com.xinbo.sports.utils.components.response.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * @author: wells
 * @date: 2020/8/12
 * @description:
 */
@RestController
@ApiSupport(author = "wells")
@RequestMapping("/v1/financialManagement")
@Api(tags = "财务管理", value = "FinancialManagementController")
public class FinancialManagementController {
    @Autowired
    private FinancialDepositService financialDepositServiceImpl;
    @Autowired
    private FinancialWithdrawalService financialWithdrawalServiceImpl;
    @Autowired
    private FinancialAdminService financialAdminServiceImpl;

    @PostMapping(value = "/depositList")
    @ApiOperation(value = "充值列表", notes = "充值列表")
    @ApiOperationSupport(order = 1)
    public Result<ResPage<DepositListResDto>> depositList(@Valid @RequestBody ReqPage<DepositListReqDto> reqDto) {
        return Result.ok(financialDepositServiceImpl.depositList(reqDto));
    }

    @PostMapping(value = "/depositSum")
    @ApiOperation(value = "充值记录汇总", notes = "充值记录汇总")
    @ApiOperationSupport(order = 2)
    public Result<DepositSumResDto> depositSum(@Valid @RequestBody DepositListReqDto reqDto) {
        return Result.ok(financialDepositServiceImpl.depositSum(reqDto));
    }

    @PostMapping(value = "/updateDepositStatus")
    @ApiOperation(value = "修改充值状态", notes = "修改充值状态")
    @ApiOperationSupport(order = 3)
    public Result<UpdateDepositStatusResDto> updateDepositStatus(@Valid @RequestBody UpdateDepositStatusReqDto reqDto) {
        return Result.ok(financialDepositServiceImpl.updateDepositStatus(reqDto));
    }

    @PostMapping(value = "/depositDetail")
    @ApiOperation(value = "充值记录详情", notes = "充值记录详情")
    @ApiOperationSupport(order = 4)
    public Result<DepositDetailResDto> depositDetail(@Valid @RequestBody DepositDetailReqDto reqDto) {
        return Result.ok(financialDepositServiceImpl.depositDetail(reqDto));
    }


    @PostMapping(value = "/withdrawalList")
    @ApiOperation(value = "提款列表", notes = "提款列表")
    @ApiOperationSupport(order = 5)
    public Result<ResPage<WithdrawalListResDto>> withdrawalList(@Valid @RequestBody ReqPage<WithdrawalListReqDto> reqDto) {
        return Result.ok(financialWithdrawalServiceImpl.withdrawalList(reqDto));
    }

    @PostMapping(value = "/WithdrawalSum")
    @ApiOperation(value = "提款记录汇总", notes = "提款记录汇总")
    @ApiOperationSupport(order = 6)
    public Result<WithdrawalSumResDto> withdrawalSum(@Valid @RequestBody WithdrawalListReqDto reqDto) {
        return Result.ok(financialWithdrawalServiceImpl.withdrawalSum(reqDto));
    }

    @PostMapping(value = "/updateWithdrawalStatus")
    @ApiOperation(value = "修改提款状态", notes = "修改提款状态")
    @ApiOperationSupport(order = 7)
    public Result<UpdateWithdrawalStatusResDto> updateWithdrawalStatus(@Valid @RequestBody UpdateWithdrawalStatusReqDto reqDto) {
        return Result.ok(financialWithdrawalServiceImpl.updateWithdrawalStatus(reqDto));
    }

    @PostMapping(value = "/WithdrawalDetail")
    @ApiOperation(value = "提款记录详情", notes = "提款记录详情")
    @ApiOperationSupport(order = 8)
    public Result<WithdrawalDetailResDto> withdrawalDetail(@Valid @RequestBody WithdrawalDetailReqDto reqDto) {
        return Result.ok(financialWithdrawalServiceImpl.withdrawalDetail(reqDto));
    }

    @PostMapping(value = "/isAudit")
    @ApiOperation(value = "稽核", notes = "稽核")
    @ApiOperationSupport(order = 9)
    public Result<UpdateWithdrawalStatusResDto> isAudit(@Valid @RequestBody AuditBaseParams.AuditReqDto reqDto) {
        return Result.ok(financialWithdrawalServiceImpl.isAudit(reqDto));
    }

    @PostMapping(value = "/auditDetail")
    @ApiOperation(value = "稽核明细", notes = "稽核明细")
    @ApiOperationSupport(order = 10)
    public Result<AuditDetailResDto> auditDetail(@Valid @RequestBody ReqPage<AuditBaseParams.AuditReqDto> reqDto) {
        return Result.ok(financialWithdrawalServiceImpl.auditDetail(reqDto));
    }

    @PostMapping(value = "/adminTransfer")
    @ApiOperation(value = "人工调账", notes = "人工调账")
    @ApiOperationSupport(order = 11)
    public Result<Boolean> adminTransfer(@Valid @RequestBody AdminTransferReqDto reqDto) {
        return Result.ok(financialAdminServiceImpl.adminTransfer(reqDto));
    }

    @PostMapping(value = "/getUserCoin")
    @ApiOperation(value = "获取用户余额", notes = "获取用户余额")
    @ApiOperationSupport(order = 12)
    public Result<UserCoinResDto> getUserCoin(@Valid @RequestBody UserCoinReqDto reqDto) {
        return Result.ok(financialAdminServiceImpl.getUserCoin(reqDto));
    }

    @PostMapping(value = "/getPromotionsList")
    @ApiOperation(value = "获取活动列表", notes = "获取活动列表")
    @ApiOperationSupport(order = 13)
    public Result<List<PromotionsListReqDto>> getPromotionsList() {
        return Result.ok(financialAdminServiceImpl.getPromotionsList());
    }

}