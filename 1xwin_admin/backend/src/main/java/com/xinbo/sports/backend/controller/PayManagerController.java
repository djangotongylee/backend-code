package com.xinbo.sports.backend.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.xinbo.sports.backend.io.bo.PayManager;
import com.xinbo.sports.backend.service.IPayManagerService;
import com.xinbo.sports.dao.generator.po.PayBankList;
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
import java.util.Map;

/**
 * <p>
 * 支付管理
 * </p>
 *
 * @author andy
 * @since 2020/8/12
 */
@Slf4j
@RestController
@RequestMapping("/v1/PayManager")
@Api(tags = {"支付管理"})
public class PayManagerController {
    @Resource
    private IPayManagerService payManagerServiceImpl;

    @ApiOperationSupport(author = "Andy", order = 1)
    @ApiOperation(value = "三方平台-列表", notes = "三方平台-列表")
    @PostMapping("/payPlatList")
    public Result<ResPage<PayManager.PayPlatListResBody>> payPlatList(@Valid @RequestBody ReqPage<PayManager.PayPlatListReqBody> reqBody) {
        return Result.ok(payManagerServiceImpl.payPlatList(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 2)
    @ApiOperation(value = "三方平台-新增", notes = "三方平台-新增")
    @PostMapping("/payPlatAdd")
    public Result<Boolean> payPlatAdd(@Valid @RequestBody PayManager.PayPlatAddReqBody reqBody) {
        payManagerServiceImpl.payPlatAdd(reqBody);
        return Result.ok(true);
    }

    @ApiOperationSupport(author = "Andy", order = 3)
    @ApiOperation(value = "三方平台-修改", notes = "三方平台-修改")
    @PostMapping("/payPlatUpdate")
    public Result<Boolean> payPlatUpdate(@Valid @RequestBody PayManager.PayPlatUpdateReqBody reqBody) {
        payManagerServiceImpl.payPlatUpdate(reqBody);
        return Result.ok(true);
    }

//    @ApiOperationSupport(author = "Andy", order = 4)
//    @ApiOperation(value = "三方平台-详情", notes = "三方平台-详情")
//    @PostMapping("/payPlatDetail")
//    public Result<PayManager.PayPlatDetailResBody> payPlatDetail(@Valid @RequestBody PayManager.CommonIdReq reqBody) {
//        return Result.ok(payManagerServiceImpl.payPlatDetail(reqBody));
//    }

    @ApiOperationSupport(author = "Andy", order = 6)
    @ApiOperation(value = "在线通道-列表", notes = "在线通道-列表")
    @PostMapping("/payOnLineList")
    public Result<ResPage<PayManager.PayOnLineListResBody>> payOnLineList(@Valid @RequestBody ReqPage<PayManager.PayOnLineListReqBody> reqBody) {
        return Result.ok(payManagerServiceImpl.payOnLineList(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 7)
    @ApiOperation(value = "在线代付通道-列表", notes = "在线代付通道-列表")
    @PostMapping("/payoutOnLineList")
    public Result<ResPage<PayManager.PayoutOnLineListResBody>> payoutOnLineList(@Valid @RequestBody ReqPage<PayManager.PayoutOnLineListReqBody> reqBody) {
        return Result.ok(payManagerServiceImpl.payoutOnLineList(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 8)
    @ApiOperation(value = "在线通道-修改", notes = "在线通道-修改")
    @PostMapping("/payOnLineUpdate")
    public Result<Boolean> payOnLineUpdate(@Valid @RequestBody PayManager.PayOnLineUpdateReqBody reqBody) {
        payManagerServiceImpl.payOnLineUpdate(reqBody);
        return Result.ok(true);
    }

    @ApiOperationSupport(author = "Andy", order = 11)
    @ApiOperation(value = "离线通道-列表", notes = "离线通道-列表")
    @PostMapping("/payOffLineList")
    public Result<ResPage<PayManager.PayOffLineListResBody>> payOffLineList(@Valid @RequestBody ReqPage<PayManager.PayOffLineListReqBody> reqBody) {
        return Result.ok(payManagerServiceImpl.payOffLineList(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 12)
    @ApiOperation(value = "离线通道-新增", notes = "离线通道-新增")
    @PostMapping("/payOffLineAdd")
    public Result<Boolean> payOffLineAdd(@Valid @RequestBody PayManager.PayOffLineAddReqBody reqBody) {
        payManagerServiceImpl.payOffLineAdd(reqBody);
        return Result.ok(true);
    }

    @ApiOperationSupport(author = "Andy", order = 13)
    @ApiOperation(value = "离线通道-修改", notes = "离线通道-修改")
    @PostMapping("/payOffLineUpdate")
    public Result<Boolean> payOffLineUpdate(@Valid @RequestBody PayManager.PayOffLineUpdateReqBody reqBody) {
        payManagerServiceImpl.payOffLineUpdate(reqBody);
        return Result.ok(true);
    }

    @ApiOperationSupport(author = "Andy", order = 14)
    @ApiOperation(value = "离线通道-详情", notes = "离线通道-详情")
    @PostMapping("/payOffLineDetail")
    public Result<PayManager.PayOffLineDetailResBody> payOffLineDetail(@Valid @RequestBody PayManager.CommonIdReq reqBody) {
        return Result.ok(payManagerServiceImpl.payOffLineDetail(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 15)
    @ApiOperation(value = "在线代付通道-修改", notes = "代付通道-修改")
    @PostMapping("/payoutOnLineUpdate")
    public Result<Boolean> payoutOnLineUpdate(@Valid @RequestBody PayManager.PayoutOnLineUpdateReqBody reqBody) {
        return Result.ok(payManagerServiceImpl.payoutOnLineUpdate(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 16)
    @ApiOperation(value = "在线代付通道-添加", notes = "代付通道-新增")
    @PostMapping("/payoutOnLineAdd")
    public Result<Boolean> payoutOnLineAdd(@Valid @RequestBody PayManager.PayoutOnLineAddReqBody reqBody) {
        return Result.ok(payManagerServiceImpl.addPayoutOnLine(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 17)
    @ApiOperation(value = "代付银行列表", notes = "代付银行列表")
    @PostMapping("/listBank")
    public Result<List<PayManager.PayoutBankList>> listBank(@RequestBody PayManager.ListBankReqDto dto) {
        return Result.ok(payManagerServiceImpl.listBank(dto));
    }

    @ApiOperationSupport(author = "Andy", order = 18)
    @ApiOperation(value = "在线代付名称列表", notes = "在线代付名称列表")
    @PostMapping("/payoutPayNameList")
    public Result<List<PayManager.PayoutPayNameListResBody>> payoutPayNameList() {
        return Result.ok(payManagerServiceImpl.payoutPayNameList());
    }
}
