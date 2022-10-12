package com.xinbo.sports.payment.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import com.xinbo.sports.payment.io.YeahPayParams;
import com.xinbo.sports.payment.utils.YeahPayUtil;
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
import java.util.Map;

/**
 * @author: David
 * @date: 19/04/2020
 * @description:
 */
@Slf4j
@RestController
@Api(tags = "支付回调-yeahPay")
@ApiSort(1)
@RequestMapping("/v1/yeahPay")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class YeahPayController {
    private final YeahPayUtil yeahPayUtil;

    @PostMapping(value = "/notifyUrl")
    @ApiOperation(value = "异步回调", notes = "在线支付异步回调")
    @ApiOperationSupport(author = "David", order = 2)
    public String notifyUrl(@Valid @RequestBody Map<String, String> dto) {
        return yeahPayUtil.notifyUrl(dto);
    }

    @PostMapping(value = "/withdrawalNotifyUrl")
    @ApiOperation(value = "提现异步回调", notes = "在线代付异步回调")
    @ApiOperationSupport(author = "David", order = 2)
    public String withdrawNotifyUrl(@Valid @RequestBody Map<String, String> dto) {
        return yeahPayUtil.withdrawNotifyUrl(dto);
    }
}

