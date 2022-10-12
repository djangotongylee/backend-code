package com.xinbo.sports.apiend.service;

import com.xinbo.sports.apiend.io.dto.Feedback;
import com.xinbo.sports.utils.components.pagination.BasePage;
import com.xinbo.sports.utils.components.pagination.ResPage;

import java.util.List;

/**
 * <p>
 * 意见反馈业务接口
 * </p>
 *
 * @author andy
 * @since 2020/6/2
 */
public interface IFeedbackService {

    /**
     * 意见反馈-问题类型列表
     *
     * @return
     */
    List<Feedback.ListCategory> listCategory();

    /**
     * 意见反馈-提交反馈
     *
     * @param reqBody
     * @return
     */
    void addFeedback(Feedback.AddFeedbackReqBody reqBody);

    /**
     * 意见反馈-问题反馈列表
     *
     * @return
     */
    ResPage<Feedback.ListFeedBackResBody> feedbackList(BasePage reqBody);

    /**
     * 意见反馈-问题反馈列表-删除
     *
     * @param reqBody reqBody
     */
    boolean delFeedback(Feedback.DelFeedbackReqBody reqBody);
}
