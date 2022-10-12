package com.xinbo.sports.payment.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import com.xinbo.sports.payment.aop.annotation.CheckAdminToken;
import com.xinbo.sports.payment.aop.annotation.CheckToken;
import com.xinbo.sports.payment.io.PayParams.*;
import com.xinbo.sports.payment.service.PayService;
import com.xinbo.sports.utils.components.response.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * @author: David
 * @date: 19/04/2020
 * @description:
 */
@Slf4j
@RestController
@Api(tags = "支付")
@ApiSort(1)
@RequestMapping("/v1/pay")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PayController {
    private final PayService payServiceImpl;

    @CheckToken
    @ApiOperationSupport(author = "David", order = 1)
    @ApiOperation(value = "钱包-充值-支付列表", notes = "支付列表")
    @PostMapping("/list")
    public Result<PayList> list() {
        return Result.ok(payServiceImpl.list());
    }

    @CheckToken
    @PostMapping(value = "/onlinePay")
    @ApiOperation(value = "在线支付", notes = "在线支付")
    @ApiOperationSupport(author = "David", ignoreParameters = {"dto.realname"}, order = 2)
    public Result<PayOnlineResDto> onlinePay(@Valid @RequestBody PaymentReqDto dto) {
        return Result.ok(payServiceImpl.onlinePay(dto));
    }

    @CheckToken
    @PostMapping(value = "/offlinePay")
    @ApiOperation(value = "离线支付", notes = "离线支付")
    @ApiOperationSupport(author = "David", order = 3)
    public Result<PayOfflineResDto> offlinePay(@Valid @RequestBody PaymentReqDto dto) {
        return Result.ok(payServiceImpl.offlinePay(dto));
    }

    @CheckAdminToken
    @PostMapping(value = "/onlineWithdraw")
    @ApiOperation(value = "在线代付", notes = "在线代付")
    @ApiOperationSupport(author = "David", order = 4)
    public Result<WithdrawalNotifyResDto> onlineWithdraw(@Valid @RequestBody WithdrawalReqDto dto) {
        return Result.ok(payServiceImpl.onlineWithdraw(dto));
    }

    @CheckToken
    @PostMapping(value = "/payment")
    @ApiOperation(value = "离线支付|在线支付", notes = "离线支付|在线支付")
    @ApiOperationSupport(author = "Andy", order = 5)
    public Result<?> payment(@Valid @RequestBody Payment reqBody) {
        return Result.ok(payServiceImpl.payment(reqBody));
    }

    @CheckToken
    @PostMapping("/payList")
    @ApiOperationSupport(author = "Andy", order = 6)
    @ApiOperation(value = "支付列表整合NEW", notes = "支付列表整合NEW")
    public Result<List<PayListResBody>> payList() {
        return Result.ok(payServiceImpl.payList());
    }

    @PostMapping("/checkPayment")
    @ApiOperationSupport(author = "Andy", order = 7)
    @ApiOperation(value = "代付校验", notes = "代付校验")
    public Result<Boolean> checkPayment() {
        return Result.ok(payServiceImpl.checkPayment());
    }

}

