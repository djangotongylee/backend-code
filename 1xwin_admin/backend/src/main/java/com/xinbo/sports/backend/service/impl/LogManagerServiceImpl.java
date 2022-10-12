package com.xinbo.sports.backend.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xinbo.sports.backend.io.bo.Log;
import com.xinbo.sports.backend.service.ILogManagerService;
import com.xinbo.sports.dao.generator.mapper.UserLoginLogMapper;
import com.xinbo.sports.dao.generator.po.AdminLoginLog;
import com.xinbo.sports.dao.generator.po.AdminOperateLog;
import com.xinbo.sports.dao.generator.po.UserLoginLog;
import com.xinbo.sports.dao.generator.service.AdminLoginLogService;
import com.xinbo.sports.dao.generator.service.AdminOperateLogService;
import com.xinbo.sports.dao.generator.service.UserLoginLogService;
import com.xinbo.sports.service.cache.redis.AdminCache;
import com.xinbo.sports.service.cache.redis.UserCache;
import com.xinbo.sports.service.cache.redis.UserChannelRelCache;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.dto.BaseParams;
import com.xinbo.sports.service.thread.ThreadHeaderLocalData;
import com.xinbo.sports.utils.BeanConvertUtils;
import com.xinbo.sports.utils.DateUtils;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;
import com.xinbo.sports.utils.components.response.CodeInfo;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * <p>
 * 日志管理实现类
 * </p>
 *
 * @author andy
 * @since 2020/6/5
 */
@Service
public class LogManagerServiceImpl extends ServiceImpl<UserLoginLogMapper, UserLoginLog> implements ILogManagerService {
    @Resource
    private AdminOperateLogService adminOperateLogServiceImpl;
    @Resource
    private AdminLoginLogService adminLoginLogServiceImpl;
    @Resource
    private UserCache userCache;
    @Resource
    private UserLoginLogService userLoginLogServiceImpl;
    @Resource
    private UserChannelRelCache userChannelRelCache;
    @Resource
    private AdminCache adminCache;

    @Override
    public ResPage<Log.UserLoginLogInfoResBody> listUserLoginLog(ReqPage<Log.UserLoginLogInfoReqBody> reqBody) {
        if (null == reqBody) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        LambdaQueryWrapper<UserLoginLog> wrapper = null;
        if (null != reqBody.getData()) {
            wrapper = getUserLoginLogLambdaQueryWrapper(reqBody.getData());
        }
        Page<UserLoginLog> page = baseMapper.selectPage(reqBody.getPage(), wrapper);
        Page<Log.UserLoginLogInfoResBody> target = BeanConvertUtils.copyPageProperties(page, Log.UserLoginLogInfoResBody::new);
        ResPage<Log.UserLoginLogInfoResBody> resPage = ResPage.get(target);
        resPage.getList().stream().parallel().forEach(s -> s.setUserFlagList(userCache.getUserFlagList(s.getUid())));
        return resPage;
    }

    @NotNull
    private LambdaQueryWrapper<UserLoginLog> getUserLoginLogLambdaQueryWrapper(Log.UserLoginLogInfoReqBody data) {
        List<Integer> onLineNumList = null;
        if ((StringUtils.isNotBlank(data.getOnLineNumFlag()) && "Y".equals(data.getOnLineNumFlag()))) {
            onLineNumList = userChannelRelCache.getChannelUids();
        }
        LambdaQueryWrapper<UserLoginLog> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(null != data.getUid(), UserLoginLog::getUid, data.getUid());
        wrapper.eq(null != data.getCategory(), UserLoginLog::getCategory, data.getCategory());
        wrapper.likeRight(StringUtils.isNotBlank(data.getUsername()), UserLoginLog::getUsername, data.getUsername());
        wrapper.likeRight(StringUtils.isNotBlank(data.getIp()), UserLoginLog::getIp, data.getIp());
        wrapper.ge(null != data.getStartTime(), UserLoginLog::getCreatedAt, data.getStartTime());
        wrapper.le(null != data.getEndTime(), UserLoginLog::getCreatedAt, data.getEndTime());
        wrapper.in(Optional.ofNullable(onLineNumList).isPresent() && !onLineNumList.isEmpty(), UserLoginLog::getUid, onLineNumList);
        return wrapper;
    }

    private void processStatus(Log.LogInfoReqBody data) {
        if (null != data.getStatus()) {
            Integer currentTime = DateUtils.getCurrentTime();
            Integer status = data.getStatus();
            if (status == 0) {
                data.setStartTime(getOnlineTime(currentTime));
                data.setEndTime(null);
            } else if (status == 1) {
                data.setEndTime(getOnlineTime(currentTime));
            }
        }
    }

    /**
     * 当前时间往前推迟30分钟:在线
     *
     * @param currentTime
     * @return
     */
    private int getOnlineTime(Integer currentTime) {
        return currentTime - 60 * 30;
    }

    @Override
    public ResPage<Log.LogInfo> listAdminLoginLog(ReqPage<Log.LogInfoReqBody> reqBody) {
        if (null == reqBody) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        LambdaQueryWrapper<AdminLoginLog> wrapper = null;
        if (null != reqBody.getData()) {
            wrapper = getAdminLoginLogLambdaQueryWrapper(reqBody.getData());
        }
        Page<AdminLoginLog> page = adminLoginLogServiceImpl.page(reqBody.getPage(), wrapper);
        Page<Log.LogInfo> target = BeanConvertUtils.copyPageProperties(page, Log.LogInfo::new);
        ResPage<Log.LogInfo> resPage = ResPage.get(target);
        for (Log.LogInfo s : resPage.getList()) {
            s.setUserFlagList(userCache.getUserFlagList(s.getUid()));
        }
        return resPage;
    }

    @NotNull
    private LambdaQueryWrapper<AdminLoginLog> getAdminLoginLogLambdaQueryWrapper(Log.LogInfoReqBody data) {
        LambdaQueryWrapper<AdminLoginLog> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Objects.nonNull(data.getUsername()), AdminLoginLog::getUsername, data.getUsername());
        wrapper.likeRight(StringUtils.isNotBlank(data.getIp()), AdminLoginLog::getIp, data.getIp());
        if (null != data.getUid()) {
            wrapper.like(AdminLoginLog::getUid, data.getUid());
        }
        // 在线状态:0-在线 1-离线
        processStatus(data);
        Integer startTime = data.getStartTime();
        Integer endTime = data.getEndTime();
        if (null != startTime) {
            if (null != endTime && startTime > endTime) {
                startTime = endTime;
            }
            wrapper.ge(AdminLoginLog::getCreatedAt, startTime);
        }
        wrapper.le(null != endTime, AdminLoginLog::getCreatedAt, endTime);
        return wrapper;
    }

    @Override
    public ResPage<Log.AdminOperateLog> listAdminOperateLog(ReqPage<Log.AdminOperateLogReqBody> reqBody) {
        if (null == reqBody) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        LambdaQueryWrapper<AdminOperateLog> wrapper = null;
        if (null != reqBody.getData()) {
            Log.AdminOperateLogReqBody data = reqBody.getData();
            wrapper = Wrappers.lambdaQuery();
            wrapper.like(null != data.getUid(), AdminOperateLog::getUid, data.getUid());
            wrapper.like(null != data.getUsername(), AdminOperateLog::getUsername, data.getUsername());
            wrapper.ge(null != data.getStartTime(), AdminOperateLog::getCreatedAt, data.getStartTime());
            wrapper.le(null != data.getEndTime(), AdminOperateLog::getCreatedAt, data.getEndTime());
        }
        Page<AdminOperateLog> page = adminOperateLogServiceImpl.page(reqBody.getPage(), wrapper);
        Page<Log.AdminOperateLog> tmpPage = BeanConvertUtils.copyPageProperties(page, Log.AdminOperateLog::new);
        List<Log.AdminOperateLog> list = new ArrayList<>();
        page.getRecords().forEach(s -> {
            Log.AdminOperateLog bean = BeanConvertUtils.beanCopy(s, Log.AdminOperateLog::new);
            if (StringUtils.isNotBlank(s.getReqParams())) {
                bean.setReqParams(JSON.parseObject(s.getReqParams()));
            }
            if (StringUtils.isNotBlank(s.getResParams())) {
                if (s.getResParams().contains("Exception")) {
                    JSONObject exception = new JSONObject();
                    exception.put("exception", s.getResParams());
                    bean.setResParams(exception);
                } else {
                    var json = new JSONObject();
                    json.put("msg", s.getResParams());
                    bean.setResParams(json);
                }
            }
            list.add(bean);
        });
        ResPage<Log.AdminOperateLog> resPage = ResPage.get(tmpPage);
        resPage.setList(list);
        return resPage;
    }

    @Override
    public Boolean delCache() {
        return true;
    }

    @Override
    public Log.OnLineNum getOnLineNum() {
        /*当前登录用户*/
        BaseParams.HeaderInfo currentLoginUser = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        var agentId = adminCache.getAdminInfoById(currentLoginUser.id).getAgentId();
        var count = 0;
        if (agentId != 0) {
            //获取代理的下级uid
            var uidList = userCache.getSupUid6ListByUid(agentId);
            count = userChannelRelCache.getSubordinateListChannelUids(uidList).size();
        } else {
            count = Optional.ofNullable(userChannelRelCache.getChannelUids()).isPresent() ? userChannelRelCache.getChannelUids().size() : 0;
        }
        return Log.OnLineNum.builder().number(count).build();
    }

    @Override
    public boolean updateUserLoginLog(Log.UpdateUserLoginLogReqBody reqBody) {
        return userLoginLogServiceImpl.lambdaUpdate()
                .set(UserLoginLog::getRemark, reqBody.getRemark())
                .eq(UserLoginLog::getId, reqBody.getId())
                .update();
    }
}



