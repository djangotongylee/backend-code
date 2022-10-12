package com.xinbo.sports.apiend.controller.pc;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import com.xinbo.sports.apiend.aop.annotation.UnCheckToken;
import com.xinbo.sports.apiend.io.dto.promotions.*;
import com.xinbo.sports.apiend.service.IEnvelopeService;
import com.xinbo.sports.apiend.service.ILuxuriousGiftService;
import com.xinbo.sports.apiend.service.IPromotionsService;
import com.xinbo.sports.apiend.service.ReceiveRedEnvelopeService;
import com.xinbo.sports.service.aop.annotation.RepeatCommit;
import com.xinbo.sports.service.io.dto.promotions.ApplicationActivityReqDto;
import com.xinbo.sports.service.io.dto.promotions.LuxuriousGiftReceiveReqDto;
import com.xinbo.sports.utils.components.response.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @author: wells
 * @date: 2020/4/7
 * @description:
 */
@Slf4j
@RestController
@ApiSupport(author = "wells")
@Api(tags = "优惠活动")
@RequestMapping("/v1/promotions")
public class PromotionsPCController {
    @Autowired
    private IPromotionsService iPromotionsServiceImpl;
    @Autowired
    private IEnvelopeService envelopeServiceImpl;
    @Autowired
    private ILuxuriousGiftService luxuriousGiftServiceImpl;
    @Autowired
    private ReceiveRedEnvelopeService receiveRedEnvelopeServiceImpl;


    @UnCheckToken
    @ApiOperationSupport(order = 1)
    @PostMapping(value = "/promotionsList")
    @ApiOperation(value = "优惠活动->列表", notes = "优惠活动列表")
    public Result<List<PromotionsGroupResDto>> promotionsList() {
        return Result.ok(iPromotionsServiceImpl.promotionsList());
    }

    @UnCheckToken
    @ApiOperationSupport(order = 2)
    @PostMapping(value = "/promotionsInfo")
    @ApiOperation(value = "优惠活动->详情", notes = "优惠活动详情")
    public Result<PromotionsInfoResDto> promotionsInfo(@Valid @RequestBody PromotionsInfoReqDto reqDto) {
        return Result.ok(iPromotionsServiceImpl.promotionsInfo(reqDto));
    }

    @ApiOperationSupport(order = 3)
    @PostMapping(value = "/levelRebateList")
    @ApiOperation(value = "优惠活动->全场返水", notes = "优惠活动全场返水")
    public Result<List<LevelRebateListResDto>> levelRebateList() {
        return Result.ok(iPromotionsServiceImpl.levelRebateList());
    }

    @ApiOperationSupport(order = 4)
    @PostMapping(value = "/levelList")
    @ApiOperation(value = "优惠活动->vip会员成长", notes = "优惠活动vip会员成长")
    public Result<List<LevelListResDto>> levelList() {
        return Result.ok(iPromotionsServiceImpl.levelList());
    }

    @ApiOperationSupport(order = 5)
    @PostMapping(value = "/vipExclusive")
    @ApiOperation(value = "优惠活动->vip专属", notes = "优惠活动vip专属")
    public Result<VipExclusiveResDto> vipExclusive() {
        return Result.ok(iPromotionsServiceImpl.vipExclusive());
    }

    @RepeatCommit
    @ApiOperationSupport(order = 6)
    @PostMapping(value = "/birthdayGift")
    @ApiOperation(value = "优惠活动->领取生日礼金", notes = "优惠活动领取生日礼金")
    public Result<String> birthdayGift() {
        iPromotionsServiceImpl.birthdayGift();
        return Result.ok();
    }

    @RepeatCommit
    @ApiOperationSupport(order = 7)
    @PostMapping(value = "/userSign")
    @ApiOperation(value = "优惠活动->每日签到", notes = "优惠活动每日签到")
    public Result<UserSingResDto> userSign() {
        return Result.ok(iPromotionsServiceImpl.userSign());
    }

    @ApiOperationSupport(order = 8)
    @PostMapping(value = "/userSignList")
    @ApiOperation(value = "优惠活动->查看每日签到", notes = "优惠活动查看每日签到")
    public Result<UserSingResDto> userSignList() {
        return Result.ok(iPromotionsServiceImpl.userSignList());
    }

    @RepeatCommit
    @ApiOperationSupport(order = 9)
    @PostMapping(value = "/applicationActivity")
    @ApiOperation(value = "优惠活动->申请优惠活动", notes = "优惠活动申请优惠活动")
    public Result<String> applicationActivity(@Valid @RequestBody ApplicationActivityReqDto reqDto) {
        iPromotionsServiceImpl.applicationActivity(reqDto);
        return Result.ok();
    }

    @ApiOperationSupport(order = 10)
    @PostMapping(value = "/isReceive")
    @ApiOperation(value = "优惠活动->判断能否领取", notes = "优惠活动判断能否领取")
    public Result<String> isReceive() {
        envelopeServiceImpl.isReceive();
        return Result.ok();
    }

    @ApiOperationSupport(order = 11)
    @PostMapping(value = "/envelopeDate")
    @ApiOperation(value = "优惠活动->红包领取时间", notes = "优惠活动红包领取时间")
    @UnCheckToken
    public Result<EnvelopeDateResDto> envelopeDate() {
        return Result.ok(envelopeServiceImpl.envelopeDate());
    }

    @UnCheckToken
    @ApiOperationSupport(order = 12)
    @PostMapping(value = "/luxuriousGiftList")
    @ApiOperation(value = "优惠活动->豪礼列表", notes = "优惠活动豪礼列表")
    public Result<LuxuriousGiftListResDto> luxuriousGiftList(@Valid @RequestBody LuxuriousGiftListReqDto reqDto) {
        return Result.ok(luxuriousGiftServiceImpl.luxuriousGiftList(reqDto));
    }

    @RepeatCommit
    @ApiOperationSupport(order = 13)
    @PostMapping(value = "/luxuriousGiftReceive")
    @ApiOperation(value = "优惠活动->豪礼领取", notes = "优惠活动豪礼领取")
    public Result<LuxuriousGiftReceiveResDto> luxuriousGiftReceive(@Valid @RequestBody LuxuriousGiftReceiveReqDto reqDto) {
        return Result.ok(luxuriousGiftServiceImpl.luxuriousGiftReceive(reqDto));
    }

    @RepeatCommit
    @ApiOperationSupport(order = 14)
    @PostMapping(value = "/receiveRedEnvelope")
    @ApiOperation(value = "红包领取", notes = "红包领取")
    public Result<ReceiveEnvelopeResDto> receiveRedEnvelope(@Valid @RequestBody ReceiveResDto reqDto) {
        return Result.ok(receiveRedEnvelopeServiceImpl.receiveRedEnvelope(reqDto));
    }
}
