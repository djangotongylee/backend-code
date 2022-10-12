package com.xinbo.sports.backend.thread;

import com.xinbo.sports.dao.generator.po.Admin;
import com.xinbo.sports.dao.generator.po.AdminLoginLog;
import com.xinbo.sports.dao.generator.service.AdminLoginLogService;
import com.xinbo.sports.dao.generator.service.AdminService;
import com.xinbo.sports.service.cache.redis.AdminCache;
import com.xinbo.sports.utils.DateNewUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Description:
 * @Author: David
 * @Date: 03/06/2020
 */
@Slf4j
@Component
public class LoginLogTask {
    @Resource
    AdminService adminServiceImpl;
    @Resource
    AdminLoginLogService adminLoginLogServiceImpl;
    @Resource
    AdminCache adminCache;


    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    public void run(Integer uid, String ip, String userAgent, String device) {
        Integer time = DateNewUtils.now();

        // 更新Updated_at时间
        Admin admin = adminServiceImpl.getById(uid);
        admin.setUpdatedAt(time);
        adminServiceImpl.updateById(admin);
        adminCache.updateAdminById(uid);
        // 插入Admin_login_log日志
        AdminLoginLog adminLoginLog = new AdminLoginLog();
        adminLoginLog.setUid(uid);
        adminLoginLog.setUsername(admin.getUsername());
        adminLoginLog.setUpdatedAt(time);
        adminLoginLog.setCreatedAt(time);
        adminLoginLog.setIp(ip);
        adminLoginLog.setUserAgent(userAgent);
        adminLoginLog.setCategory(device);
        adminLoginLogServiceImpl.save(adminLoginLog);
        log.info("=======登录OK....uid={},ip={}", uid, ip);
    }

}
