package com.xinbo.sports.backend.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.DynamicParameter;
import com.github.xiaoymin.knife4j.annotations.DynamicParameters;
import com.xinbo.sports.backend.io.bo.Log;
import com.xinbo.sports.backend.io.bo.PayManager;
import com.xinbo.sports.backend.io.bo.user.*;
import com.xinbo.sports.backend.io.bo.user.AgentCenterParameter.*;
import com.xinbo.sports.backend.service.*;
import com.xinbo.sports.service.cache.redis.UserCache;
import com.xinbo.sports.utils.BeanConvertUtils;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 会员管理
 * </p>
 *
 * @author andy
 * @since 2020/3/12
 */
@Slf4j
@RestController
@RequestMapping("/v1/user")
@Api(tags = "会员管理")
public class UserController {
    @Resource
    private IUserManagerService userManagerServiceImpl;
    @Resource
    private IUserBankBusinessService userBankBusinessServiceImpl;
    @Resource
    private ILogManagerService logManagerServiceImpl;
    @Resource
    private IComprehensiveChartService comprehensiveChartServiceImpl;
    @Resource
    private UserCache userCache;
    @Resource
    private IAgentCenterService agentCenterServiceImpl;


    @ApiOperationSupport(author = "Andy", order = 1)
    @ApiOperation(value = "会员列表-分页查询", notes = "会员列表查询，支持分页")
    @PostMapping("/list")
    public Result<ResPage<ListResBody>> list(@Valid @RequestBody ReqPage<ListReqBody> reqBody) {
        return Result.ok(userManagerServiceImpl.list(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 2)
    @ApiOperation(value = "会员列表-会员详情", notes = "查询会员详情")
    @PostMapping("/detail")
    public Result<DetailResBody> detail(@Valid @RequestBody DetailReqBody reqBody) {
        return Result.ok(userManagerServiceImpl.detail(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 3)
    @ApiOperation(value = "会员列表-新增会员", notes = "查询会员详情")
    @PostMapping("/addUser")
    public Result<AddUserResBody> addUser(@Valid @RequestBody AddUserReqBody reqBody) {
        return Result.ok(userManagerServiceImpl.addUser(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 4)
    @ApiOperation(value = "会员列表-修改会员", notes = "修改会员")
    @PostMapping("/updateUser")
    public Result<Boolean> updateUser(@Valid @RequestBody UpdateUserReqBody reqBody) {
        return Result.ok(userManagerServiceImpl.updateUser(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 5)
    @ApiOperation(value = "会员管理-批量修改会员等级", notes = "会员管理-批量修改会员等级")
    @PostMapping("/updateBatchLevel")
    public Result<Boolean> updateBatchLevel(@Valid @RequestBody UpdateBatchLevelReqBody reqBody) {
        userManagerServiceImpl.updateBatchLevel(reqBody);
        return Result.ok(true);
    }

    @ApiOperationSupport(author = "Andy", order = 6)
    @ApiOperation(value = "会员列表-路线转移", notes = "会员列表-路线转移")
    @PostMapping("/routeTransfer")
    public Result<Boolean> routeTransfer(@Valid @RequestBody RouteTransferReqBody reqBody) {
        return Result.ok(userManagerServiceImpl.routeTransfer(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 7)
    @ApiOperation(value = "会员列表-上级代理", notes = "会员列表-上级代理")
    @PostMapping("/supProxyList")
    public Result<List<SupProxyListReqBody>> supProxyList(@Valid @RequestBody SupProxyListReqBody reqBody) {
        List<SupProxyListReqBody> list = new ArrayList<>();
        if (null != reqBody) {
            list = userManagerServiceImpl.supProxyList(reqBody.getUid(), reqBody.getUsername());
        }
        return Result.ok(list);
    }

    @ApiOperationSupport(author = "Andy", order = 8)
    @ApiOperation(value = "会员列表-团队在线-人数", notes = "会员列表-团队在线-人数")
    @PostMapping("/listOnlineCount")
    public Result<ListOnlineCountResBody> listOnlineCount(@Valid @RequestBody ListOnlineReqBody reqBody) {
        return Result.ok(userManagerServiceImpl.listOnlineCount(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 10)
    @ApiOperation(value = "会员列表-下级列表", notes = "会员列表-下级列表")
    @PostMapping("/listChild")
    public Result<ResPage<ListOnlineResBody>> listChild(@Valid @RequestBody ListOnlineReqBody reqBody) {
        return Result.ok(userManagerServiceImpl.listChild(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 11)
    @ApiOperation(value = "会员等级管理-等级列表", notes = "会员等级管理-等级列表")
    @PostMapping("/listLevel")
    public Result<List<ListLevelResBody>> listLevel() {
        return Result.ok(userManagerServiceImpl.listLevel());
    }

    @ApiOperationSupport(author = "Andy", order = 12)
    @ApiOperation(value = "会员等级管理-修改等级", notes = "会员等级管理-修改等级")
    @PostMapping("/updateLevel")
    public Result<Boolean> updateLevel(@Valid @RequestBody UpdateLevelReqBody reqBody) {
        return Result.ok(userManagerServiceImpl.updateLevel(reqBody));
    }

    @ApiOperation(value = "会员等级管理-详情", notes = "会员等级管理-详情")
    @PostMapping("/levelDetail")
    @ApiOperationSupport(author = "Andy", order = 13,
            params = @DynamicParameters(name = "LevelDetail", properties = {
                    @DynamicParameter(name = "id", value = "等级Id", example = "1", required = true, dataTypeClass = Integer.class)
            }))
    public Result<LevelDetailResBody> levelDetail(@Valid @RequestBody JSONObject reqBody) {
        return Result.ok(userManagerServiceImpl.levelDetail(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 14)
    @ApiOperation(value = "会员等级管理-下拉列表", notes = "会员等级管理-下拉列表")
    @PostMapping("/getUserLevelList")
    public Result<List<PayManager.LevelBitBo>> getUserLevelList() {
        return Result.ok(BeanConvertUtils.copyListProperties(userCache.getUserLevelList(), PayManager.LevelBitBo::new));
    }

    @ApiOperationSupport(author = "Andy", order = 17)
    @ApiOperation(value = "会员旗管理-列表查询", notes = "会员管理-会员旗管理-列表查询")
    @PostMapping("/listFlag")
    public Result<List<ListFlagResBody>> listFlag() {
        return Result.ok(userManagerServiceImpl.listFlag());
    }

    @ApiOperationSupport(author = "Andy", order = 18)
    @ApiOperation(value = "会员旗管理-下拉列表", notes = "会员旗管理-下拉列表")
    @PostMapping("/userFlagDict")
    public Result<List<UserFlagDict>> userFlagDict() {
        return Result.ok(userManagerServiceImpl.userFlagDict());
    }

    @ApiOperationSupport(author = "Andy", order = 19)
    @ApiOperation(value = "会员旗管理-用户会员旗列表(弹框)", notes = "会员旗管理-用户会员旗列表(弹框)")
    @PostMapping("/listFlagUsed")
    public Result<ResPage<ListFlagUsedResBody>> listFlagUsed(@Valid @RequestBody ReqPage<ListFlagUsedReqBody> reqBody) {
        return Result.ok(userManagerServiceImpl.listFlagUsed(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 21)
    @ApiOperation(value = "会员旗管理-用户会员旗列表(弹框)-批量删除", notes = "会员旗管理-用户会员旗列表(弹框)-批量删除")
    @PostMapping("/delUserFlag")
    public Result<Boolean> delUserFlag(@Valid @RequestBody DelUserFlagReqBody reqBody) {
        return Result.ok(userManagerServiceImpl.delUserFlag(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 22)
    @ApiOperation(value = "会员旗管理-新增用户会员旗", notes = "会员旗管理-新增用户会员旗")
    @PostMapping("/addUserFlag")
    public Result<Boolean> addUserFlag(@Valid @RequestBody AddUserFlagReqBody reqBody) {
        return Result.ok(userManagerServiceImpl.addUserFlag(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 23)
    @ApiOperation(value = "会员旗管理-启用或禁用", notes = "会员管理-会员旗管理-启用或禁用")
    @PostMapping("/updateFlagStatus")
    public Result<Boolean> updateFlagStatus(@Valid @RequestBody UpdateFlagStatusReqBody reqBody) {
        return Result.ok(userManagerServiceImpl.updateFlagStatus(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 24)
    @ApiOperation(value = "会员旗管理-修改会员旗", notes = "会员管理-会员旗管理-修改会员旗")
    @PostMapping("/updateFlag")
    public Result<Boolean> updateFlag(@Valid @RequestBody UpdateFlagReqBody reqBody) {
        return Result.ok(userManagerServiceImpl.updateFlag(reqBody));
    }

    @ApiOperationSupport(author = "wells", order = 25)
    @ApiOperation(value = "会员列表-稽核明细", notes = "会员列表-稽核明细")
    @PostMapping("/auditDetails")
    public Result<ResPage<AuditDetailsResBody>> auditDetails(@Valid @RequestBody ReqPage<CodeUidReqBody> reqBody) {
        return Result.ok(userManagerServiceImpl.auditDetails(reqBody));
    }

    @ApiOperationSupport(author = "wells", order = 26)
    @ApiOperation(value = "会员列表-打码量明细", notes = "会员列表-打码量明细")
    @PostMapping("/codeDetails")
    public Result<CodeDetailsResBody> codeDetails(@Valid @RequestBody ReqPage<CodeUidReqBody> reqBody) {
        return Result.ok(userManagerServiceImpl.codeDetails(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 27)
    @ApiOperation(value = "会员管理-资金统计", notes = "会员管理-资金统计")
    @PostMapping("/capitalStatistics/list")
    public Result<CapitalStatisticsListResBody> listCapitalStatistics(@Valid @RequestBody CapitalStatisticsListReqBody reqBody) {
        reqBody.setUidList(List.of(reqBody.getUid()));
        CapitalStatisticsListResBody resBody = comprehensiveChartServiceImpl.userZH(reqBody);
        if (null != reqBody) {
            resBody.setCoinXTTZ(resBody.getCoinXTTZ().negate());
        }
        return Result.ok(resBody);
    }

    @ApiOperation(value = "会员列表-银行卡信息-用户银行卡列表", notes = "会员列表-银行卡信息-用户银行卡列表")
    @PostMapping("/userBankList")
    @ApiOperationSupport(author = "Andy", order = 28,
            params = @DynamicParameters(name = "userBankList", properties = {
                    @DynamicParameter(name = "uid", value = "UID", example = "1", required = true, dataTypeClass = Integer.class)
            }))
    public Result<List<UserBankList>> userBankList(@Valid @RequestBody JSONObject reqBody) {
        int uid = -1;
        if (Objects.nonNull(reqBody)) {
            uid = reqBody.getInteger("uid");
        }
        return Result.ok(userBankBusinessServiceImpl.userBankList(uid));
    }

    @ApiOperationSupport(author = "Andy", order = 29)
    @ApiOperation(value = "会员列表-银行卡信息-添加或修改用户银行卡", notes = "会员列表-银行卡信息-添加或修改用户银行卡")
    @PostMapping("/userBankAddOrUpdate")
    public Result<Boolean> userBankAddOrUpdate(@Valid @RequestBody BankAddReqBody reqBody) {
        userBankBusinessServiceImpl.bankAdd(reqBody);
        return Result.ok(true);
    }

    @ApiOperationSupport(author = "Andy", order = 30)
    @ApiOperation(value = "会员列表-银行卡信息-银行列表", notes = "会员列表-银行卡信息-银行列表")
    @PostMapping("/bankInfoList")
    public Result<List<BankInfoList>> bankInfoList() {
        return Result.ok(userBankBusinessServiceImpl.bankInfoList());
    }

    @ApiOperationSupport(author = "Andy", order = 31)
    @ApiOperation(value = "会员管理-会员登录日志", notes = "会员管理-会员登录日志:查询会员登录日志")
    @PostMapping("/listUserLoginLog")
    public Result<ResPage<Log.UserLoginLogInfoResBody>> listUserLoginLog(@RequestBody ReqPage<Log.UserLoginLogInfoReqBody> reqBody) {
        return Result.ok(logManagerServiceImpl.listUserLoginLog(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 32)
    @ApiOperation(value = "会员管理-提款消费明细", notes = "会员管理-提款消费明细")
    @PostMapping("/listCodeRecords")
    public Result<ResPage<ListCodeRecords>> listCodeRecords(@Valid @RequestBody ListCodeRecordsReqBody reqBody) {
        return Result.ok(userManagerServiceImpl.listCodeRecords(reqBody));
    }

    @ApiOperation(value = "会员管理-清空提款消费量", notes = "会员管理-清空提款消费量")
    @PostMapping("/clearCodeRecords")
    @ApiOperationSupport(author = "Andy", order = 33,
            params = @DynamicParameters(properties = {
                    @DynamicParameter(name = "uid", value = "UID", example = "1", required = true, dataTypeClass = Integer.class)
            }))
    public Result<Boolean> clearCodeRecords(@RequestBody JSONObject reqBody) {
        int uid = -1;
        if (Objects.nonNull(reqBody)) {
            uid = reqBody.getInteger("uid");
        }
        userManagerServiceImpl.clearCodeRecords(uid);
        return Result.ok(true);
    }

    @ApiOperationSupport(author = "wells", order = 34)
    @ApiOperation(value = "代理中心-会员列表", notes = "代理中心-会员列表")
    @PostMapping("/agentUserList")
    public Result<ResPage<AgentUserListResBody>> agentUserList(@Valid @RequestBody ReqPage<AgentUserListReqBody> reqBody) {
        return Result.ok(agentCenterServiceImpl.agentUserList(reqBody));
    }


    @ApiOperationSupport(author = "wells", order = 35)
    @ApiOperation(value = "代理中心-佣金收益", notes = "代理中心-佣金收益")
    @PostMapping("/agentCommissionProfit")
    public Result<AgentCommissionResBody> agentCommissionProfit(@Valid @RequestBody ReqPage<AgentCommissionReqBody> reqBody) {
        return Result.ok(agentCenterServiceImpl.agentCommissionProfit(reqBody));
    }

    @ApiOperationSupport(author = "wells", order = 36)
    @ApiOperation(value = "代理中心-佣金收益用户详情", notes = "代理中心-佣金收益用户详情")
    @PostMapping("/agentCommissionDetails")
    public Result<ResPage<AgentCommissionDetailsResDtoBody>> agentCommissionDetails(@Valid @RequestBody ReqPage<AgentCommissionDetailsReqBody> reqBody) {
        return Result.ok(agentCenterServiceImpl.agentCommissionDetails(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 37)
    @ApiOperation(value = "会员管理-会员登录日志-修改备注", notes = "会员管理-会员登录日志-修改备注")
    @PostMapping("/updateUserLoginLog")
    public Result<Boolean> updateUserLoginLog(@Valid @RequestBody Log.UpdateUserLoginLogReqBody reqBody) {
        return Result.ok(logManagerServiceImpl.updateUserLoginLog(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 38)
    @ApiOperation(value = "会员管理-验证码管理", notes = "会员管理-验证码管理")
    @PostMapping("/getVerifyCodeList")
    public Result<ResPage<SmsCodeResDto>> getVerifyCodeList(@RequestBody ReqPage<SmsCodeReqDto> reqBody) {
        return Result.ok(userManagerServiceImpl.getVerifyCodeList(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 39)
    @ApiOperation(value = "在线人数->列表查询(分页)", notes = "在线人数->列表查询(分页)")
    @PostMapping("/onlineUserCountList")
    public Result<ResPage<OnlineUserCountListResBody>> onlineUserCountList(@Valid @RequestBody ReqPage<OnlineUserCountListReqBody> reqBody) {
        return Result.ok(userManagerServiceImpl.onlineUserCountList(reqBody));
    }

    @ApiOperationSupport(author = "wells", order = 40)
    @ApiOperation(value = "清除用户token与手机验证次数", notes = "清除用户token与手机验证次数")
    @PostMapping("/clearTokenCode")
    public Result<Boolean> clearTokenCode(@Valid @RequestBody ClearTokenCodeReqBody reqBody) {
        return Result.ok(userManagerServiceImpl.clearTokenCode(reqBody));
    }

}
