package com.xinbo.sports.apiend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinbo.sports.apiend.io.dto.Feedback;
import com.xinbo.sports.apiend.service.IFeedbackService;
import com.xinbo.sports.apiend.service.IUserInfoService;
import com.xinbo.sports.dao.generator.po.FeedbackCategory;
import com.xinbo.sports.dao.generator.po.FeedbackList;
import com.xinbo.sports.dao.generator.po.FeedbackListReply;
import com.xinbo.sports.dao.generator.service.FeedbackCategoryService;
import com.xinbo.sports.dao.generator.service.FeedbackListReplyService;
import com.xinbo.sports.dao.generator.service.FeedbackListService;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.dto.BaseParams;
import com.xinbo.sports.service.io.dto.UserInfo;
import com.xinbo.sports.utils.BeanConvertUtils;
import com.xinbo.sports.utils.DateUtils;
import com.xinbo.sports.utils.components.pagination.BasePage;
import com.xinbo.sports.utils.components.pagination.ResPage;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

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
    private FeedbackCategoryService feedbackCategoryServiceImpl;
    @Resource
    private FeedbackListService feedbackListServiceImpl;
    @Resource
    private IUserInfoService userInfoServiceImpl;
    @Resource
    private FeedbackListReplyService feedbackListReplyServiceImpl;
    @Resource
    private ConfigCache configCache;


    @Override
    public List<Feedback.ListCategory> listCategory() {
        LambdaQueryWrapper<FeedbackCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FeedbackCategory::getStatus, 1);
        List<FeedbackCategory> list = feedbackCategoryServiceImpl.list(wrapper);
        return BeanConvertUtils.copyListProperties(list, Feedback.ListCategory::new);
    }

    @Override
    public void addFeedback(Feedback.AddFeedbackReqBody reqBody) {
        UserInfo userInfo = userInfoServiceImpl.findIdentityByApiToken();
        Integer currentTime = DateUtils.getCurrentTime();
        FeedbackList entity = BeanConvertUtils.beanCopy(reqBody, FeedbackList::new);
        entity.setUid(userInfo.getId());
        entity.setUsername(userInfo.getUsername());
        entity.setCreatedAt(currentTime);
        entity.setUpdatedAt(currentTime);
        // 状态:1-已回复 0-未回复
        entity.setStatus(0);
        feedbackListServiceImpl.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delFeedback(Feedback.DelFeedbackReqBody reqBody) {
        boolean flag = false;
        BaseParams.HeaderInfo currentUser = userInfoServiceImpl.getHeadLocalData();
        if (null == reqBody || null == reqBody.getId()) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }

        feedbackListServiceImpl.remove(new LambdaQueryWrapper<FeedbackList>()
                .eq(FeedbackList::getId, reqBody.getId())
                .eq(FeedbackList::getUid, currentUser.getId()));

        flag = feedbackListReplyServiceImpl.remove(new LambdaQueryWrapper<FeedbackListReply>()
                .eq(FeedbackListReply::getReferId, reqBody.getId()));
        return flag;
    }

    @Override
    public ResPage<Feedback.ListFeedBackResBody> feedbackList(BasePage reqBody) {
        BaseParams.HeaderInfo currentUser = userInfoServiceImpl.getHeadLocalData();
        Page<FeedbackList> page = feedbackListServiceImpl
                .lambdaQuery()
                .eq(FeedbackList::getUid, currentUser.getId())
                .orderByDesc(FeedbackList::getCreatedAt)
                .page(reqBody.getPage());

        List<Feedback.ListFeedBackResBody> resList = new ArrayList<>();
        page.getRecords().stream().forEach(s -> {
            Feedback.ListFeedBackResBody resBody = BeanConvertUtils.beanCopy(s, Feedback.ListFeedBackResBody::new);
            resBody.setImgList(getImgList(s.getImg()));
            FeedbackListReply replay = feedbackListReplyServiceImpl.lambdaQuery().eq(FeedbackListReply::getReferId, s.getId()).one();
            if (null != replay) {
                resBody.setReplyContent(replay.getContent());
                resBody.setReplyCreatedAt(replay.getCreatedAt());
            }
            resList.add(resBody);
        });

        Page<Feedback.ListFeedBackResBody> tmpPage = BeanConvertUtils.copyPageProperties(page, Feedback.ListFeedBackResBody::new);
        ResPage<Feedback.ListFeedBackResBody> resPage = ResPage.get(tmpPage);
        resPage.setList(resList);
        return resPage;
    }

    private List<String> getImgList(String strImg) {
        List<String> imgList = new ArrayList<>();
        if (StringUtils.isNotBlank(strImg)) {
            String staticServer = configCache.getStaticServer();
            for (String img : strImg.split(",")) {
                if (StringUtils.isNotEmpty(img)) {
                    imgList.add(img.startsWith("http")?img:staticServer + img);
                }
            }
        }
        return imgList;
    }
}
