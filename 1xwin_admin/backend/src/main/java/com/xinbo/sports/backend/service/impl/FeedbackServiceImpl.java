package com.xinbo.sports.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinbo.sports.backend.io.bo.Feedback;
import com.xinbo.sports.backend.service.IFeedbackService;
import com.xinbo.sports.dao.generator.po.FeedbackList;
import com.xinbo.sports.dao.generator.po.FeedbackListReply;
import com.xinbo.sports.dao.generator.service.FeedbackListReplyService;
import com.xinbo.sports.dao.generator.service.FeedbackListService;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.utils.BeanConvertUtils;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 意见反馈业务接口-实现类
 * </p>
 *
 * @author andy
 * @since 2020/6/2
 */
@Slf4j
@Service
public class FeedbackServiceImpl implements IFeedbackService {
    @Resource
    private FeedbackListReplyService feedbackListReplyServiceImpl;
    @Resource
    private FeedbackListService feedbackListServiceImpl;
    @Resource
    private ConfigCache configCache;

    @Override
    public ResPage<Feedback.ListFeedBackResBody> listFeedBack(ReqPage<Feedback.ListFeedBackReqBody> reqBody) {
        ResPage<Feedback.ListFeedBackResBody> resBody = null;
        if (Objects.nonNull(reqBody)) {
            LambdaQueryWrapper<FeedbackList> wrapper = null;
            if (Objects.nonNull(reqBody.getData())) {
                wrapper = getFeedbackListLambdaQueryWrapper(reqBody.getData());
            }
            Page<FeedbackList> page = feedbackListServiceImpl.page(reqBody.getPage(), wrapper);
            Page<Feedback.ListFeedBackResBody> listFeedBackResBody = BeanConvertUtils.copyPageProperties(page, Feedback.ListFeedBackResBody::new);
            resBody = getListFeedBackResBodyResPage(listFeedBackResBody);
        }
        return resBody;
    }

    private ResPage<Feedback.ListFeedBackResBody> getListFeedBackResBodyResPage(Page<Feedback.ListFeedBackResBody> listFeedBackResBody) {
        ResPage<Feedback.ListFeedBackResBody> resBody;
        if (!listFeedBackResBody.getRecords().isEmpty()) {
            for (Feedback.ListFeedBackResBody backResBody : listFeedBackResBody.getRecords()) {
                if (StringUtils.isNotEmpty(backResBody.getImg())) {
                    getImgList(backResBody);
                }
                FeedbackListReply replay = feedbackListReplyServiceImpl.lambdaQuery().eq(FeedbackListReply::getReferId, backResBody.getId()).one();
                if (null != replay) {
                    backResBody.setReplyContent(replay.getContent());
                }
            }
        }
        resBody = ResPage.get(listFeedBackResBody);
        return resBody;
    }

    private void getImgList(Feedback.ListFeedBackResBody backResBody) {
        String staticServer = configCache.getStaticServer();
        List<String> imgList = new ArrayList<>();
        for (String img : backResBody.getImg().split(",")) {
            if (StringUtils.isNotEmpty(img)) {
                imgList.add(img.startsWith("http")?img:staticServer + img);
            }
        }
        backResBody.setImgList(imgList);
    }

    private LambdaQueryWrapper<FeedbackList> getFeedbackListLambdaQueryWrapper(Feedback.ListFeedBackReqBody data) {
        LambdaQueryWrapper<FeedbackList> wrapper = Wrappers.lambdaQuery();
        if (null != data.getStartTime()) {
            wrapper.ge(FeedbackList::getUpdatedAt, data.getStartTime());
        }
        if (null != data.getEndTime()) {
            wrapper.le(FeedbackList::getUpdatedAt, data.getEndTime());
        }
        if (null != data.getCategory()) {
            wrapper.eq(FeedbackList::getCategory, data.getCategory());
        }
        if (null != data.getUsername()) {
            wrapper.eq(FeedbackList::getUsername, data.getUsername());
        }
        return wrapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addFeedBackReply(Feedback.AddFeedBackReplyReqBody reqBody) {
        if (null != reqBody && null != reqBody.getId()) {
            Integer id = reqBody.getId();
            Integer currentTime = DateNewUtils.now();
            boolean updateStatus = feedbackListServiceImpl.lambdaUpdate()
                    // 状态:1-已回复 0-未回复
                    .set(FeedbackList::getStatus, 1)
                    .set(FeedbackList::getUpdatedAt, currentTime)
                    .eq(FeedbackList::getStatus, 0)
                    .eq(FeedbackList::getId, id)
                    .update();
            if (updateStatus) {
                FeedbackListReply reply = new FeedbackListReply();
                reply.setReferId(id);
                reply.setContent(reqBody.getContent());
                reply.setCreatedAt(currentTime);
                reply.setUpdatedAt(currentTime);
                feedbackListReplyServiceImpl.save(reply);
            }
        }
    }
}
