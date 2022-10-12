package com.xinbo.sports.backend.service;

import com.xinbo.sports.backend.io.bo.Feedback;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;

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
     * 意见反馈-回复
     *
     * @param reqBody
     * @return
     */
    void addFeedBackReply(Feedback.AddFeedBackReplyReqBody reqBody);

    /**
     * 问题反馈-列表
     *
     * @param reqBody
     * @return
     */
    ResPage<Feedback.ListFeedBackResBody> listFeedBack(ReqPage<Feedback.ListFeedBackReqBody> reqBody);
}
