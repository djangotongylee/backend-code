package com.xinbo.sports.apiend.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import com.xinbo.sports.apiend.io.dto.wallet.*;
import com.xinbo.sports.apiend.service.ITransactionService;
import com.xinbo.sports.apiend.service.IWalletService;
import com.xinbo.sports.apiend.service.IWithdrawalService;
import com.xinbo.sports.service.base.NoticeBase;
import com.xinbo.sports.service.common.Constant;
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
import java.util.List;

/**
 * <p>
 * 钱包
 * </p>
 *
 * @author andy
 * @since 2020/4/7
 */
@Slf4j
@RestController
@RequestMapping("/v1/wallet")
@Api(tags = "钱包")
@ApiSort(4)
public class WalletController {
    @Resource
    private IWithdrawalService withdrawalServiceImpl;
    @Resource
    private ITransactionService transactionServiceImpl;
    @Resource
    private IWalletService walletServiceImpl;
    @Resource
    private NoticeBase noticeBase;

    @ApiOperationSupport(author = "Andy", order = 1)
    @ApiOperation(value = "钱包-卡片管理-银行列表", notes = "钱包-卡片管理-银行列表")
    @PostMapping("/bankInfoList")
    public Result<List<BankInfoList>> bankInfoList() {
        return Result.ok(walletServiceImpl.bankInfoList());
    }

    @ApiOperationSupport(author = "Andy", order = 2)
    @ApiOperation(value = "钱包-卡片管理-用户银行卡列表", notes = "钱包-卡片管理-用户银行卡列表")
    @PostMapping("/userBankList")
    public Result<List<UserBankList>> userBankList() {
        return Result.ok(walletServiceImpl.userBankList());
    }

    @ApiOperationSupport(author = "Andy", order = 3)
    @ApiOperation(value = "钱包-卡片管理-添加用户银行卡", notes = "钱包-卡片管理-添加用户银行卡")
    @PostMapping("/bankAdd")
    public Result<?> bankAdd(@Valid @RequestBody BankAddReqBody reqBody) {
        walletServiceImpl.bankAdd(reqBody);
        return Result.ok();
    }

    @ApiOperationSupport(author = "Andy", order = 4)
    @ApiOperation(value = "钱包-卡片管理-修改用户银行卡", notes = "钱包-卡片管理-修改用户银行卡")
    @PostMapping("/bankUpdate")
    public Result<?> bankUpdate(@Valid @RequestBody BankUpdateReqBody reqBody) {
        walletServiceImpl.bankUpdate(reqBody);
        return Result.ok();
    }

    @ApiOperationSupport(author = "Andy", order = 5)
    @ApiOperation(value = "钱包-提现", notes = "钱包-提现")
    @PostMapping("/withdrawalAdd")
    public Result<?> withdrawalAdd(@Valid @RequestBody WithdrawalAddReqBody reqBody) {
        var flag = withdrawalServiceImpl.addWithdrawal(reqBody);
        if (flag) {
            //推送提款条数
            noticeBase.writeDepositAndWithdrawalCount(Constant.PUSH_WN);
        }
        return Result.ok(flag);
    }

    @ApiOperationSupport(author = "Andy", order = 6)
    @ApiOperation(value = "钱包-交易记录-列表查询", notes = "钱包-交易记录-列表查询:查询交易记录")
    @PostMapping("/transactionList")
    public Result<ResPage<TransactionListResBody>> transactionList(@Valid @RequestBody ReqPage<TransactionListReqBody> reqBody) {
        return Result.ok(transactionServiceImpl.transactionList(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 7)
    @ApiOperation(value = "钱包-交易记录-交易详情", notes = "钱包-交易记录-交易详情:查询交易详情")
    @PostMapping("/transactionDetail")
    public Result<Object> transactionDetail(@Valid @RequestBody TransactionDetailReqBody reqBody) {
        return Result.ok(transactionServiceImpl.transactionDetail(reqBody));
    }

    @ApiOperationSupport(author = "wells", order = 8)
    @ApiOperation(value = "钱包-交易记录-邀请记录", notes = "钱包-交易记录-交易详情: 邀请记录")
    @PostMapping("/transactionInvite")
    public Result<TransactionInvite.ResBody> transactionInvite(@Valid @RequestBody ReqPage<TransactionInvite.ReqBody> reqBody) {
        return Result.ok(transactionServiceImpl.transactionInvite(reqBody));
    }

    @ApiOperationSupport(author = "wells", order = 9)
    @ApiOperation(value = "钱包-提款提示信息", notes = "钱包-提款提示信息")
    @PostMapping("/withdrawalHint")
    public Result<WithdrawalHint.HintResBody> withdrawalHint() {
        return Result.ok(transactionServiceImpl.withdrawalHint());
    }
}
