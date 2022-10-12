package com.xinbo.sports.backend.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.xinbo.sports.backend.io.bo.Feedback;
import com.xinbo.sports.backend.service.IFeedbackService;
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
 * 意见反馈
 * </p>
 *
 * @author andy
 * @since 2020/6/1
 */
@Slf4j
@RestController
@RequestMapping("/v1/feedback")
@Api(tags = "问题反馈")
public class FeedbackController {
    @Resource
    private IFeedbackService feedbackServiceImpl;

    @ApiOperationSupport(author = "Andy", order = 1)
    @ApiOperation(value = "问题反馈-列表", notes = "问题反馈-列表")
    @PostMapping("/listFeedBack")
    public Result<ResPage<Feedback.ListFeedBackResBody>> listFeedBack(@RequestBody ReqPage<Feedback.ListFeedBackReqBody> reqBody) {
        return Result.ok(feedbackServiceImpl.listFeedBack(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 2)
    @ApiOperation(value = "问题反馈-回复", notes = "问题反馈-回复")
    @PostMapping("/addFeedBackReply")
    public Result<Boolean> addFeedBackReply(@Valid @RequestBody Feedback.AddFeedBackReplyReqBody reqBody) {
        feedbackServiceImpl.addFeedBackReply(reqBody);
        return Result.ok(true);
    }
}
