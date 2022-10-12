package com.xinbo.sports.apiend.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import com.github.xiaoymin.knife4j.annotations.DynamicParameter;
import com.github.xiaoymin.knife4j.annotations.DynamicParameters;
import com.xinbo.sports.apiend.io.dto.center.GiftRecordsDetailsResDto;
import com.xinbo.sports.apiend.io.dto.center.GiftRecordsReqDto;
import com.xinbo.sports.apiend.io.dto.center.GiftRecordsResDto;
import com.xinbo.sports.apiend.service.ICenterService;
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
 * @author: David
 * @date: 21/04/2020
 * @description: 个人中心模块
 */
@Slf4j
@RestController
@Api(tags = "个人中心 @David")
@ApiSort(5)
@RequestMapping("/v1/center")
public class CenterController {
    @Resource
    ICenterService centerServiceImpl;

    @PostMapping(value = "/giftRecords")
    @ApiOperationSupport(author = "David", order = 1)
    @ApiOperation(value = "个人中心 - 实物领取", notes = "个人中心 - 实物领取")
    public Result<ResPage<GiftRecordsResDto>> giftRecords(@Valid @RequestBody ReqPage<GiftRecordsReqDto> dto) {
        return Result.ok(centerServiceImpl.giftRecords(dto));
    }

    @PostMapping(value = "/giftRecordsDetails")
    @ApiOperationSupport(author = "Andy", order = 2,
            params = @DynamicParameters(name = "", properties = {
                    @DynamicParameter(name = "id", value = "ID", example = "69743418217205760", required = true)
            }))
    @ApiOperation(value = "个人中心 - 奖品详情", notes = "个人中心 - 奖品详情")
    public Result<GiftRecordsDetailsResDto> giftRecordsDetails(@Valid @RequestBody JSONObject req) {
        return Result.ok(centerServiceImpl.giftRecordsDetails(req.getLong("id")));
    }
}
