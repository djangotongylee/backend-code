package com.xinbo.sports.payment.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import com.xinbo.sports.payment.utils.UPIPayUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;


/**
 * @author: David
 * @date: 19/04/2020
 * @description:
 */
@Slf4j
@RestController
@Api(tags = "支付回调-UPI-Pay")
@ApiSort(1)
@RequestMapping("/v1/upiPay")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UPIPayController {
    private final UPIPayUtil upiPayUtil;

    @GetMapping(value = "/notifyUrl")
    @ApiOperation(value = "异步回调", notes = "在线支付异步回调")
    @ApiOperationSupport(author = "David", order = 3)
    public String notifyUrl(@Valid @RequestParam Map<String, String> dto) {
        return upiPayUtil.notifyUrl(dto);
    }

    @GetMapping(value = "/withdrawalNotifyUrl")
    @ApiOperation(value = "提现异步回调", notes = "在线提款异步回调")
    @ApiOperationSupport(author = "David", order = 3)
    public String withdrawalNotifyUrl(@Valid @RequestParam Map<String, String> dto) {
        return upiPayUtil.withdrawNotifyUrl(dto);
    }
}

