package com.xinbo.sports.payment.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import com.xinbo.sports.payment.io.OTPayParams;
import com.xinbo.sports.payment.utils.OTPayUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: David
 * @date: 19/04/2020
 * @description:
 */
@Slf4j
@RestController
@Api(tags = "支付回调-otPay")
@ApiSort(1)
@RequestMapping("/v1/otPay")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OTPayController {
    private final OTPayUtil otPayUtil;

    @PostMapping(value = "/notifyUrl")
    @ApiOperation(value = "异步回调", notes = "在线支付异步回调")
    @ApiOperationSupport(author = "David", order = 2)
    public String notifyUrl(OTPayParams.NotifyUrlReqDto dto) {
        return otPayUtil.notifyUrl(dto);
    }


    @PostMapping(value = "/withdrawalNotifyUrl")
    @ApiOperation(value = "提现异步回调", notes = "在线提现异步回调")
    @ApiOperationSupport(author = "David", order = 4)
    public String withdrawNotifyUrl(OTPayParams.DFNotifyUrlReqDto dto) {
        return otPayUtil.withdrawNotifyUrl(dto);
    }

   /* @PostMapping(value = "/withdrawNotifyUrlAuto")
    @ApiOperation(value = "提现异步回调", notes = "在线提现异步回调")
    @ApiOperationSupport(author = "David", order = 4)
    public String withdrawNotifyUrlAuto(@Valid String orderId) {
        return easyPayUtil.withdrawNotifyUrlAuto(orderId);
    }*/



}

