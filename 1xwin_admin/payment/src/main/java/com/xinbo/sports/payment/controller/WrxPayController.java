package com.xinbo.sports.payment.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import com.xinbo.sports.payment.io.WrxPayParams;
import com.xinbo.sports.payment.utils.WrxPayUtil;
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

/**
 * @author: David
 * @date: 19/04/2020
 * @description:
 */
@Slf4j
@RestController
@Api(tags = "支付回调-easyPay")
@ApiSort(1)
@RequestMapping("/v1/wrxPay")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WrxPayController {
    private final WrxPayUtil wrxPayUtil;

    @PostMapping(value = "/notifyUrl")
    @ApiOperation(value = "异步回调", notes = "在线支付异步回调")
    @ApiOperationSupport(author = "David", order = 2)
    public String notifyUrl(@Valid @RequestBody WrxPayParams.NotifyUrlReqDto dto) {
        return wrxPayUtil.notifyUrl(dto);
    }



    @PostMapping(value = "/withdrawalNotifyUrl")
    @ApiOperation(value = "提现异步回调", notes = "在线提现异步回调")
    @ApiOperationSupport(author = "David", order = 4)
    public String withdrawNotifyUrl(@Valid @RequestBody WrxPayParams.DFNotifyUrlReqDto dto) {
        return wrxPayUtil.withdrawNotifyUrl(dto);
    }

   /* @PostMapping(value = "/withdrawNotifyUrlAuto")
    @ApiOperation(value = "提现异步回调", notes = "在线提现异步回调")
    @ApiOperationSupport(author = "David", order = 4)
    public String withdrawNotifyUrlAuto(@Valid String orderId) {
        return easyPayUtil.withdrawNotifyUrlAuto(orderId);
    }*/



}

