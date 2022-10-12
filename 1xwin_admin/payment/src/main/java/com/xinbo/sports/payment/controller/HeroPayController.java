package com.xinbo.sports.payment.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import com.xinbo.sports.payment.utils.HeroPayUtil;
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
@Api(tags = "支付回调-heroPay")
@ApiSort(1)
@RequestMapping("/v1/heroPay")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HeroPayController {
    private final HeroPayUtil heroPayUtil;

    @PostMapping(value = "/notifyUrl")
    @ApiOperation(value = "异步回调", notes = "在线支付异步回调")
    @ApiOperationSupport(author = "David", order = 3)
    public String notifyUrl(@Valid @RequestParam Map<String, String> dto) {
        return heroPayUtil.notifyUrl(dto);
    }

    @GetMapping(value = "/WithdrawalNotifyUrl")
    @ApiOperation(value = "提现异步回调", notes = "在线提现异步回调")
    @ApiOperationSupport(author = "David", order = 3)
    public String withdrawNotifyUrl(@Valid @RequestParam Map<String, String> dto) {
        return heroPayUtil.withdrawNotifyUrl(dto);
    }
}

