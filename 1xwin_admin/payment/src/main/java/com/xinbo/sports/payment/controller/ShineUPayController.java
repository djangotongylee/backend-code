package com.xinbo.sports.payment.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import com.xinbo.sports.payment.utils.ShineUPayUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @author: David
 * @date: 19/04/2020
 * @description:
 */
@Slf4j
@RestController
@Api(tags = "支付回调-shineUPay")
@ApiSort(1)
@RequestMapping("/v1/shineUPay")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ShineUPayController {
    private final ShineUPayUtil shineUPayUtil;

    @PostMapping(value = "/notifyUrl")
    @ApiOperation(value = "异步回调", notes = "在线支付异步回调")
    @ApiOperationSupport(author = "David", order = 2)
    public String notifyUrl(HttpServletRequest request) {
        return shineUPayUtil.notifyUrl(request);
    }

    @PostMapping(value = "/withdrawalNotifyUrl")
    @ApiOperation(value = "异步提现回调", notes = "在线支付提现回调")
    @ApiOperationSupport(author = "David", order = 3)
    public String withdrawNotifyUrl(HttpServletRequest request) {
        return shineUPayUtil.withdrawNotifyUrl(request);
    }

}

