package com.xinbo.sports.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinbo.sports.backend.io.bo.notice.*;
import com.xinbo.sports.backend.service.INoticeManagerService;
import com.xinbo.sports.dao.generator.po.Notice;
import com.xinbo.sports.dao.generator.po.User;
import com.xinbo.sports.dao.generator.service.NoticeService;
import com.xinbo.sports.dao.generator.service.UserService;
import com.xinbo.sports.service.base.NoticeBase;
import com.xinbo.sports.service.base.websocket.WebSocketClient;
import com.xinbo.sports.service.common.Constant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.utils.BeanConvertUtils;
import com.xinbo.sports.utils.DateUtils;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * 公告管理
 * </p>
 *
 * @author andy
 * @since 2020/6/8
 */
@Slf4j
@Service
public class NoticeManagerServiceImpl implements INoticeManagerService {
    @Resource
    private NoticeService noticeServiceImpl;
    @Resource
    private UserService userServiceImpl;


    @Resource
    private WebSocketClient webSocketClient;
    private static final AtomicInteger POOL_ID = new AtomicInteger();
    public static final ThreadPoolExecutor NETTY_THREAD_POOL = new ThreadPoolExecutor(
            8,
            32,
            1L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            x -> new Thread(x, "公告信息推送_THREAD_" + POOL_ID.getAndIncrement()));


    @Override
    public ResPage<ListNoticeResBody> list(ReqPage<ListNoticeReqBody> reqBody) {
        if (null == reqBody) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        LambdaQueryWrapper<Notice> wrapper = null;
        if (null != reqBody.getData()) {
            ListNoticeReqBody data = reqBody.getData();
            wrapper = Wrappers.lambdaQuery();
            if (null != data.getId()) {
                wrapper.eq(Notice::getId, data.getId());
            }
            if (null != data.getCategory()) {
                wrapper.eq(Notice::getCategory, data.getCategory());
            }
            if (null != data.getStatus()) {
                wrapper.eq(Notice::getStatus, data.getStatus());
            }
            if (StringUtils.isNotBlank(data.getTitle())) {
                wrapper.like(Notice::getTitle, data.getTitle());
            }
        }
        Page<Notice> page1 = noticeServiceImpl.page(reqBody.getPage(), wrapper);
        Page<ListNoticeResBody> page = BeanConvertUtils.copyPageProperties(page1, ListNoticeResBody::new);
        return ResPage.get(page);
    }

    @Autowired
    private NoticeBase noticeBase;

    @Override
    public void add(AddNoticeReqBody reqBody) {
        Integer currentTime = DateUtils.getCurrentTime();
        Notice notice = BeanConvertUtils.beanCopy(reqBody, Notice::new);
        notice.setCreatedAt(currentTime);
        notice.setUpdatedAt(currentTime);
        boolean flag = noticeServiceImpl.save(notice);
        //推送公告消息
        if (flag) {
            noticeBase.writeNotification(reqBody.getCategory(), notice.getId());
        }
    }

    @Override
    public void update(UpdateNoticeReqBody reqBody) {
        Integer currentTime = DateUtils.getCurrentTime();
        Notice notice = BeanConvertUtils.beanCopy(reqBody, Notice::new);
        notice.setUpdatedAt(currentTime);
        if (null != notice.getId()) {
            var flag = noticeServiceImpl.update(notice, new LambdaQueryWrapper<Notice>().eq(Notice::getId, notice.getId()));
            //推送公告消息
            if (flag) {
                noticeBase.writeNotification(reqBody.getCategory(), reqBody.getId());
            }
        }
    }

    @Override
    public void delete(DeleteNoticeReqBody reqBody) {
        noticeServiceImpl.removeById(reqBody.getId());
    }

    @Override
    public ResPage<UidListResBody> uidList(ReqPage<UidListReqBody> reqBody) {
        UidListReqBody data = reqBody.getData();
        LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery();
        wrapper.ne(User::getRole, Constant.USER_ROLE_CS);
        if (data != null && data.getUserMark() != null) {
            wrapper.eq(User::getId, data.getUserMark()).or().likeRight(User::getUsername, data.getUserMark());
        }
        Page<UidListResBody> page = BeanConvertUtils.copyPageProperties(userServiceImpl.page(reqBody.getPage(), wrapper), UidListResBody::new);
        return ResPage.get(page);
    }
}
