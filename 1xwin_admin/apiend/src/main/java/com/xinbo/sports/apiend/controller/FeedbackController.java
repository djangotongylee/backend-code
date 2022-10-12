package com.xinbo.sports.apiend.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.xinbo.sports.apiend.io.dto.Feedback;
import com.xinbo.sports.apiend.service.IFeedbackService;
import com.xinbo.sports.utils.components.pagination.BasePage;
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
@Api(tags = "意见反馈")
public class FeedbackController {
    @Resource
    private IFeedbackService feedbackServiceImpl;

    @ApiOperationSupport(author = "Andy", order = 1)
    @ApiOperation(value = "意见反馈-问题类型列表", notes = "意见反馈-问题类型列表:查询问题类型列表")
    @PostMapping("/listCategory")
    public Result<List<Feedback.ListCategory>> listCategory() {
        return Result.ok(feedbackServiceImpl.listCategory());
    }

    @ApiOperationSupport(author = "Andy", order = 2)
    @ApiOperation(value = "意见反馈-提交反馈", notes = "意见反馈-提交反馈:新增反馈记录")
    @PostMapping("/addFeedback")
    public Result<String> addFeedback(@Valid @RequestBody Feedback.AddFeedbackReqBody reqBody) {
        feedbackServiceImpl.addFeedback(reqBody);
        return Result.ok();
    }

    @ApiOperationSupport(author = "Andy", order = 3)
    @ApiOperation(value = "意见反馈-我的反馈列表-删除", notes = "意见反馈-我的反馈列表-删除")
    @PostMapping("/delFeedback")
    public Result<Boolean> delFeedback(@Valid @RequestBody Feedback.DelFeedbackReqBody reqBody) {
        return Result.ok(feedbackServiceImpl.delFeedback(reqBody));
    }

    @ApiOperationSupport(author = "Andy", order = 4)
    @ApiOperation(value = "意见反馈-我的反馈列表", notes = "意见反馈-我的反馈列表")
    @PostMapping("/feedbackList")
    public Result<ResPage<Feedback.ListFeedBackResBody>> feedbackList(@Valid @RequestBody BasePage reqBody) {
        return Result.ok(feedbackServiceImpl.feedbackList(reqBody));
    }
}
