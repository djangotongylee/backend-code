package com.xinbo.sports.backend.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import com.xinbo.sports.backend.aop.annotation.UnCheckLog;
import com.xinbo.sports.backend.aop.annotation.UnCheckToken;
import com.xinbo.sports.backend.io.bo.AdminParams;
import com.xinbo.sports.backend.io.bo.Log;
import com.xinbo.sports.backend.service.ILogManagerService;
import com.xinbo.sports.backend.service.impl.AdminOperatorServiceImpl;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;
import com.xinbo.sports.utils.components.response.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * @author David
 * @date 03/06/2020
 */
@Slf4j
@RestController
@EnableKnife4j
@Api(tags = "后台管理")
@ApiSort(1)
@RequestMapping("/v1/admin")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AdminController {
    private final AdminOperatorServiceImpl adminOperatorServiceImpl;
    private final ILogManagerService logManagerServiceImpl;

    @PostMapping(value = "/login")
    @ApiOperation(value = "登录", notes = "管理员登录")
    @ApiOperationSupport(author = "David", order = 1)
    @UnCheckToken
    public Result<AdminParams.LoginResponse> login(@Valid @RequestBody AdminParams.LoginRequest dto, HttpServletRequest request) {
        return Result.ok(adminOperatorServiceImpl.login(dto, request));
    }

    @PostMapping(value = "/logout")
    @ApiOperation(value = "登出", notes = "登出")
    @ApiOperationSupport(author = "Andy", order = 2)
    public Result<Boolean> logout() {
        adminOperatorServiceImpl.logout();
        return Result.ok(true);
    }

    @GetMapping(value = "/profile")
    @ApiOperation(value = "获取管理员信息", notes = "获取管理员信息")
    @ApiOperationSupport(author = "David", order = 3)
    public Result<AdminParams.LoginResponse> profile() {
        return Result.ok(adminOperatorServiceImpl.profile());
    }

    @ApiOperationSupport(author = "David", order = 4)
    @PostMapping(value = "/refreshGetRule")
    @ApiOperation(value = "刷新页面获取权限信息", notes = "刷新页面获取权限信息")
    public Result<JSONObject> refreshGetRule() {
        return Result.ok(adminOperatorServiceImpl.refreshGetRule());
    }

    @UnCheckLog
    @ApiOperationSupport(author = "Andy", order = 5)
    @ApiOperation(value = "后台管理-登录日志", notes = "后台管理-登录日志:查询admin登录日志")
    @PostMapping("/listAdminLoginLog")
    public Result<ResPage<Log.LogInfo>> listAdminLoginLog(@RequestBody ReqPage<Log.LogInfoReqBody> reqBody) {
        return Result.ok(logManagerServiceImpl.listAdminLoginLog(reqBody));
    }

    @UnCheckLog
    @ApiOperationSupport(author = "Andy", order = 6)
    @ApiOperation(value = "后台管理-操作日志", notes = "后台管理-操作日志:查询admin操作日志")
    @PostMapping("/listAdminOperateLog")
    public Result<ResPage<Log.AdminOperateLog>> listAdminOperateLog(@RequestBody ReqPage<Log.AdminOperateLogReqBody> reqBody) {
        return Result.ok(logManagerServiceImpl.listAdminOperateLog(reqBody));
    }

    @UnCheckLog
    @ApiOperationSupport(author = "Andy", order = 7)
    @ApiOperation(value = "清空缓存", notes = "后台管理-操作日志:清空缓存")
    @PostMapping("/delCache")
    public Result<Boolean> delCache() {
        return Result.ok(logManagerServiceImpl.delCache());
    }

    @UnCheckLog
    @ApiOperationSupport(author = "Andy", order = 8)
    @ApiOperation(value = "在线人数统计", notes = "后台管理-操作日志:在线人数统计")
    @PostMapping("/OnLineNum")
    public Result<Log.OnLineNum> value() {
        return Result.ok(logManagerServiceImpl.getOnLineNum());
    }

}
