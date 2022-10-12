package com.xinbo.sports.payment.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import com.xinbo.sports.payment.utils.MangoPayUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.util.Map;

import static com.xinbo.sports.payment.io.MangoPayParams.NotifyUrlReqDto;

/**
 * @author: David
 * @date: 19/04/2020
 * @description:
 */
@Slf4j
@RestController
@Api(tags = "支付回调-MangoPay")
@ApiSort(1)
@RequestMapping("/v1/mangoPay")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MangoPayController {
    private final MangoPayUtil mangoPayUtil;

    @PostMapping(value = "/notifyUrl")
    @ApiOperation(value = "异步回调", notes = "在线支付异步回调")
    @ApiOperationSupport(author = "David", order = 2)
    public String notifyUrl(@Valid @RequestBody NotifyUrlReqDto dto) {
        return mangoPayUtil.notifyUrl(dto);
    }

    @GetMapping(value = "/withdrawalNotifyUrl")
    @ApiOperation(value = "提现异步回调", notes = "在线提现异步回调")
    @ApiOperationSupport(author = "David", order = 4)
    public String withdrawNotifyUrl() {
        return mangoPayUtil.withdrawNotifyUrl();
    }

}