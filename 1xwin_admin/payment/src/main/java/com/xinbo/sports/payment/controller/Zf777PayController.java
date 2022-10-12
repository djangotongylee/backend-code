package com.xinbo.sports.payment.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import com.xinbo.sports.payment.utils.Zf777PayUtil;
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
@Api(tags = "支付回调-Zf777Pay")
@ApiSort(1)
@RequestMapping("/v1/zf777Pay")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class Zf777PayController {
    private final Zf777PayUtil zf777PayUtil;

    @PostMapping(value = "/notifyUrl")
    @ApiOperation(value = "异步回调", notes = "在线支付异步回调")
    @ApiOperationSupport(author = "David", order = 2)
    public String notifyUrl(@Valid @RequestBody Map<String, String> dto) {
        return zf777PayUtil.notifyUrl(dto);
    }

    @PostMapping(value = "/withdrawNotifyUrl")
    @ApiOperation(value = "提现异步回调", notes = "在线代付异步回调")
    @ApiOperationSupport(author = "David", order = 2)
    public String withdrawNotifyUrl(@Valid @RequestBody Map<String, String> dto) {
        return zf777PayUtil.notifyUrl(dto);
    }
}

