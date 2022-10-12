package com.xinbo.sports.apiend.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import com.xinbo.sports.apiend.aop.annotation.UnCheckToken;
import com.xinbo.sports.apiend.io.dto.StartEndTime;
import com.xinbo.sports.apiend.io.dto.centeragent.*;
import com.xinbo.sports.apiend.service.ICenterAgentDepositOrWithdrawalService;
import com.xinbo.sports.apiend.service.ICenterAgentService;
import com.xinbo.sports.apiend.service.impl.CenterAgentStatisticsServiceImpl;
import com.xinbo.sports.apiend.service.impl.UserInfoServiceImpl;
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

/**
 * <p>
 * 代理中心
 * </p>
 *
 * @author andy
 * @since 2020/4/22
 */
@Slf4j
@RestController
@RequestMapping("/v1/centerAgent")
@Api(tags = "代理中心")
@ApiSort(6)
public class CenterAgentController {
    @Resource
    private ICenterAgentService centerAgentDetailsServiceImpl;
    @Resource
    private CenterAgentStatisticsServiceImpl centerAgentStatisticsServiceImpl;
    @Resource
    private UserInfoServiceImpl userInfoServiceImpl;
    @Resource
    private ICenterAgentDepositOrWithdrawalService centerAgentDepositOrWithdrawalServiceImpl;

    @ApiOperationSupport(author = "Andy", order = 1)
    @ApiOperation(value = "代理中心-我的报表", notes = "代理中心-我的报表")
    @PostMapping("/reports")
    public Result<ReportsResBody> reports(@RequestBody ReportsReqBody reqBody) {
        return Result.ok(centerAgentStatisticsServiceImpl.reports(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 2)
    @ApiOperation(value = "代理中心-我的报表-玩家活跃度详情-列表", notes = "代理中心-我的报表-玩家活跃度详情")
    @PostMapping("/reports/playerActivityDetails")
    public Result<ResPage<PlayerActivityDetailsResBody>> playerActivityDetails(@RequestBody ReqPage<PlayerActivityDetailsReqBody> reqBody) {
        return Result.ok(centerAgentDetailsServiceImpl.playerActivityDetails(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 3)
    @ApiOperation(value = "代理中心-我的报表-玩家活跃度详情-统计", notes = "代理中心-我的报表-玩家活跃度详情")
    @PostMapping("/reports/playerActivityDetailsStatistics")
    public Result<DepositWithdrawalDetailsStatisticsResBody> playerActivityDetailsStatistics(@RequestBody PlayerActivityDetailsReqBody reqBody) {
        return Result.ok(centerAgentDetailsServiceImpl.playerActivityDetailsStatistics(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 3)
    @ApiOperation(value = "代理中心-我的报表-充值与提现详情-列表", notes = "代理中心-我的报表-充值与提现详情-列表")
    @PostMapping("/reports/depositWithdrawalDetails")
    public Result<ResPage<DepositWithdrawalDetailsResBody>> depositWithdrawalDetails(@RequestBody ReqPage<DepositWithdrawalDetailsReqBody> reqBody) {
        return Result.ok(centerAgentDetailsServiceImpl.depositWithdrawalDetails(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 4)
    @ApiOperation(value = "代理中心-我的报表-充值与提现详情-统计", notes = "代理中心-我的报表-充值与提现详情-统计")
    @PostMapping("/reports/depositWithdrawalDetailsStatistics")
    public Result<DepositWithdrawalDetailsStatisticsResBody> depositWithdrawalDetailsStatistics(@RequestBody DepositWithdrawalDetailsReqBody reqBody) {
        return Result.ok(centerAgentDetailsServiceImpl.depositWithdrawalDetailsStatistics(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 5)
    @ApiOperation(value = "代理中心-我的报表-用户详情-列表", notes = "代理中心-我的报表-列表")
    @PostMapping("/reports/depositWithdrawalUserDetails")
    public Result<ResPage<SubordinateInfo>> depositWithdrawalUserDetails(@RequestBody ReqPage<DepositWithdrawalUserDetailsReqBody> reqBody) {
        return Result.ok(centerAgentDetailsServiceImpl.depositWithdrawalUserDetails(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 6)
    @ApiOperation(value = "代理中心-我的报表-用户详情-统计", notes = "代理中心-我的报表-统计")
    @PostMapping("/reports/depositWithdrawalUserDetailsStatistics")
    public Result<DepositWithdrawalUserDetailsResBody> depositWithdrawalUserDetailsStatistics(@RequestBody DepositWithdrawalUserDetailsReqBody reqBody) {
        return Result.ok(centerAgentDetailsServiceImpl.depositWithdrawalUserDetailsStatistics(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 7)
    @ApiOperation(value = "代理中心-我的报表-奖金与佣金详情-列表", notes = "代理中心-我的报表-奖金与佣金详情-列表")
    @PostMapping("/reports/rewardsCommissionDetails")
    public Result<ResPage<RewardsCommissionDetailsResBody>> rewardsCommissionDetails(@RequestBody ReqPage<RewardsCommissionDetailsReqBody> reqBody) {
        return Result.ok(centerAgentDetailsServiceImpl.rewardsCommissionDetails(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 8)
    @ApiOperation(value = "代理中心-我的报表-奖金与佣金详情-统计", notes = "代理中心-我的报表-奖金与佣金详情-统计")
    @PostMapping("/reports/rewardsCommissionDetailsStatistics")
    public Result<RewardsCommissionDetailsStatisticsResBody> rewardsCommissionDetailsStatistics(@RequestBody RewardsCommissionDetailsReqBody reqBody) {
        return Result.ok(centerAgentDetailsServiceImpl.rewardsCommissionDetailsStatistics(reqBody, userInfoServiceImpl.getHeadLocalData().getId()));
    }

    @ApiOperationSupport(author = "Andy", order = 9)
    @ApiOperation(value = "代理中心-我的报表-奖金与佣金详情-列表->活跃会员佣金->会员详情", notes = "代理中心-我的报表-奖金与佣金详情-列表->活跃会员佣金->会员详情")
    @PostMapping("/reports/rewardsCommissionActiveDetails")
    public Result<ResPage<SubordinateInfo>> rewardsCommissionActiveDetails(@RequestBody ReqPage<RewardsCommissionDetailsActiveReqBody> reqBody) {
        return Result.ok(centerAgentDetailsServiceImpl.rewardsCommissionActiveDetails(reqBody));
    }

    @UnCheckToken
    @ApiOperationSupport(author = "Andy", order = 10)
    @ApiOperation(value = "H5-我的团队-统计", notes = "H5-我的团队-统计")
    @PostMapping("/myTeamStatisticsHisOrNew")
    public Result<MyTeamStatisticsResBody> myTeamStatisticsHisOrNew(@RequestBody StartEndTime reqBody) {
        return Result.ok(centerAgentStatisticsServiceImpl.myTeamStatisticsHisOrNew(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 11)
    @ApiOperation(value = "代理中心-我的下级-列表", notes = "代理中心-我的下级-列表")
    @PostMapping("/subordinate/list")
    public Result<SubordinateListResBody> subordinateList(@RequestBody ReqPage<SubordinateListReqBody> reqBody) {
        return Result.ok(centerAgentDetailsServiceImpl.subordinateList(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 12)
    @ApiOperation(value = "代理中心-我的下级-列表-直推人数列表(弹框)", notes = "代理中心-我的下级-列表-直推人数列表(弹框)")
    @PostMapping("/subordinate/listZT")
    public Result<SubordinateListZtResBody> subordinateListZT(@RequestBody ReqPage<SubordinateListZtReqBody> reqBody) {
        return Result.ok(centerAgentDetailsServiceImpl.subordinateListZT(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 13)
    @ApiOperation(value = "代理中心-我的下级-统计", notes = "代理中心-我的下级-统计")
    @PostMapping("/subordinate/statistics")
    public Result<SubordinateStatisticsResBody> subordinateStatistics() {
        return Result.ok(centerAgentDetailsServiceImpl.subordinateStatistics());
    }

    @ApiOperationSupport(author = "Andy", order = 14)
    @ApiOperation(value = "代理中心-充值提现", notes = "代理中心-充值提现")
    @PostMapping("/subordinate/depositOrWithdrawal")
    public Result<ResPage<DepositOrWithdrawalResBody>> depositOrWithdrawal(@Valid @RequestBody ReqPage<DepositOrWithdrawalReqBody> reqBody) {
        return Result.ok(centerAgentDepositOrWithdrawalServiceImpl.depositOrWithdrawal(reqBody));
    }

}
