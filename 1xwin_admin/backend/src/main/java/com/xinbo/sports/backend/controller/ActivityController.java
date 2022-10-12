package com.xinbo.sports.backend.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import com.xinbo.sports.backend.io.dto.PromotionsParameter.*;
import com.xinbo.sports.backend.service.IActivityService;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;
import com.xinbo.sports.utils.components.response.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * @author: wells
 * @date: 2020/6/5
 * @description:活动配置
 */
@Slf4j
@RestController
@ApiSupport(author = "wells")
@RequestMapping("/v1/activity")
@Api(tags = "活动配置", value = "ActivityController")
public class ActivityController {
    @Autowired
    private IActivityService activityServiceImpl;

    @PostMapping(value = "/promotionsGroup")
    @ApiOperation(value = "活动组", notes = "活动组")
    @ApiOperationSupport(order = 1)
    public Result<List<PromotionsGroupResDto>> promotionsGroup() {
        return Result.ok(activityServiceImpl.promotionsGroup());
    }

    @PostMapping(value = "/promotionsList")
    @ApiOperation(value = "活动列表", notes = "活动列表")
    @ApiOperationSupport(order = 2)
    public Result<ResPage<ListResDto>> promotionsList(@Valid @RequestBody ReqPage<ListReqDto> reqDto) {
        return Result.ok(activityServiceImpl.promotionsList(reqDto));
    }

    @PostMapping(value = "/saveOrUpdatePromotions")
    @ApiOperation(value = "新增或修改活动", notes = "新增或修改活动")
    @ApiOperationSupport(order = 3)
    public Result<Boolean> saveOrUpdatePromotions(@Valid @RequestBody SavaOrUpdateReqDto reqDto) {
        return Result.ok(activityServiceImpl.saveOrUpdatePromotions(reqDto));
    }

    @PostMapping(value = "/deletePromotions")
    @ApiOperation(value = "删除活动", notes = "删除活动")
    @ApiOperationSupport(order = 4)
    public Result<Boolean> deletePromotions(@Valid @RequestBody DeleteReqDto reqDto) {
        return Result.ok(activityServiceImpl.deletePromotions(reqDto));
    }

    @PostMapping(value = "/signList")
    @ApiOperation(value = "签到列表", notes = "签到列表")
    @ApiOperationSupport(order = 5)
    public Result<SingListResDto> signList(@Valid @RequestBody ReqPage<SingListReqDto> reqDto) {
        return Result.ok(activityServiceImpl.signList(reqDto));
    }

    @PostMapping(value = "/signDetail")
    @ApiOperation(value = "签到详情", notes = "签到详情")
    @ApiOperationSupport(order = 6)
    public Result<ResPage<SingDetailResDto>> signDetail(@Valid @RequestBody ReqPage<SingDetailReqDto> reqDto) {
        return Result.ok(activityServiceImpl.signDetail(reqDto));
    }

    @PostMapping(value = "/firstDepositList")
    @ApiOperation(value = "首充活动列表", notes = "首充活动列表")
    @ApiOperationSupport(order = 7)
    public Result<FirstDepositResDto> firstDepositList(@Valid @RequestBody ReqPage<RewardsReqDto> reqDto) {
        return Result.ok(activityServiceImpl.firstDepositList(reqDto));
    }

    @PostMapping(value = "/sportsAndLiveList")
    @ApiOperation(value = "体育与真人列表", notes = "体育与真人列表")
    @ApiOperationSupport(order = 8)
    public Result<SportAndLiveResDto> sportsAndLiveList(@Valid @RequestBody ReqPage<RewardsReqDto> reqDto) {
        return Result.ok(activityServiceImpl.sportsAndLiveList(reqDto));
    }

    @PostMapping(value = "/redEnvelopeWarList")
    @ApiOperation(value = "红包雨列表", notes = "红包雨列表")
    @ApiOperationSupport(order = 9)
    public Result<RedEnvelopeWarListResDto> redEnvelopeWarList(@Valid @RequestBody ReqPage<RewardsReqDto> reqDto) {
        return Result.ok(activityServiceImpl.redEnvelopeWarList(reqDto));
    }

    @PostMapping(value = "/allRebateList")
    @ApiOperation(value = "全场返水列表", notes = "全场返水列表")
    @ApiOperationSupport(order = 10)
    public Result<AllRebateListResDto> allRebateList(@Valid @RequestBody ReqPage<AllRebateListReqDto> reqDto) {
        return Result.ok(activityServiceImpl.allRebateList(reqDto));
    }

    @PostMapping(value = "/friendRebateList")
    @ApiOperation(value = "邀请好友返水列表", notes = "邀请好友返水列表")
    @ApiOperationSupport(order = 11)
    public Result<FriendRebateListResDto> friendRebateList(@Valid @RequestBody ReqPage<RewardsReqDto> reqDto) {
        return Result.ok(activityServiceImpl.friendRebateList(reqDto));
    }

    @PostMapping(value = "/giftReceiveList")
    @ApiOperation(value = "礼品领取列表", notes = "礼品领取列表")
    @ApiOperationSupport(order = 12)
    public Result<ResPage<GiftReceiveListResDto>> giftReceiveList(@Valid @RequestBody ReqPage<GiftReceiveListReqDto> reqDto) {
        return Result.ok(activityServiceImpl.giftReceiveList(reqDto));
    }

    @PostMapping(value = "/giftReceiveUpdate")
    @ApiOperation(value = "礼品领取修改状态", notes = "礼品领取修改状态")
    @ApiOperationSupport(order = 13)
    public Result<Boolean> giftReceiveUpdate(@Valid @RequestBody GiftReceiveUpdateReqDto reqDto) {
        return Result.ok(activityServiceImpl.giftReceiveUpdate(reqDto));
    }

    @PostMapping(value = "/orderDetail")
    @ApiOperation(value = "补充单号详情", notes = "补充单号详情")
    @ApiOperationSupport(order = 14)
    public Result<OrderDetailDto> orderDetail(@Valid @RequestBody OrderDetailReqDto reqDto) {
        return Result.ok(activityServiceImpl.orderDetail(reqDto));
    }

    @PostMapping(value = "/orderUpdate")
    @ApiOperation(value = "补充单号修改", notes = "补充单号修改")
    @ApiOperationSupport(order = 15)
    public Result<Boolean> orderUpdate(@Valid @RequestBody OrderDetailDto reqDto) {
        return Result.ok(activityServiceImpl.orderUpdate(reqDto));
    }

    @PostMapping(value = "/promotionsDefault")
    @ApiOperation(value = "活动默认模板", notes = "活动默认模板")
    @ApiOperationSupport(order = 16)
    public Result<RewardsReDto> promotionsDefault(@Valid @RequestBody ReqPage<RewardsReqDto> reqDto) {
        return Result.ok(activityServiceImpl.promotionsDefault(reqDto));
    }


    @PostMapping(value = "/getPlatLang")
    @ApiOperation(value = "平台语言列表", notes = "平台语言列表")
    @ApiOperationSupport(order = 17)
    public Result<List<PlatLangResDto>> getPlatLang() {
        return Result.ok(activityServiceImpl.getPlatLang());
    }

    @PostMapping(value = "/getPromotionsByLang")
    @ApiOperation(value = "根据语言获取活动信息", notes = "根据语言获取活动信息")
    @ApiOperationSupport(order = 18)
    public Result<PromotionsByLangResDto> getPromotionsByLang(@Valid @RequestBody PromotionsByLangReqDto reqDto) {
        return Result.ok(activityServiceImpl.getPromotionsByLang(reqDto));
    }

    @PostMapping(value = "/registerRewardsList")
    @ApiOperation(value = "注册彩金列表", notes = "注册彩金列表")
    @ApiOperationSupport(order = 19)
    public Result<ResPage<RegisterRewardsListResDto>> registerRewardsList(@Valid @RequestBody ReqPage<RegisterRewardsListReqDto> reqDto) {
        return Result.ok(activityServiceImpl.registerRewardsList(reqDto));
    }

    @PostMapping(value = "/registerRewardsUpdate")
    @ApiOperation(value = "注册彩金修改", notes = "注册彩金修改")
    @ApiOperationSupport(order = 20)
    public Result<Boolean> registerRewardsUpdate(@Valid @RequestBody RegisterRewardsUpdateReqDto reqDto) {
        return Result.ok(activityServiceImpl.registerRewardsUpdate(reqDto));
    }
}
