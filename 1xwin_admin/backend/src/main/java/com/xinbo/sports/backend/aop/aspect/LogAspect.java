package com.xinbo.sports.backend.aop.aspect;

import com.alibaba.fastjson.JSON;
import com.xinbo.sports.backend.base.ThreadPoolExecutorFactory;
import com.xinbo.sports.backend.configuration.tenant.TenantContextHolder;
import com.xinbo.sports.backend.service.IAdminInfoBase;
import com.xinbo.sports.dao.generator.po.AdminOperateLog;
import com.xinbo.sports.dao.generator.service.AdminOperateLogService;
import com.xinbo.sports.service.cache.redis.AdminCache;
import com.xinbo.sports.service.common.Constant;
import com.xinbo.sports.service.io.dto.BaseParams;
import com.xinbo.sports.utils.IpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.NamedThreadLocal;
import org.springframework.core.annotation.Order;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * <p>
 * 操作日志AOP
 * </p>
 *
 * @author andy
 * @since 2020/8/11
 */
@Slf4j(topic = "操作日志AOP")
@Order(2)
@Aspect
@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LogAspect {
    private static final ThreadLocal<ZonedDateTime> THREAD_LOCAL_START_TIME = new NamedThreadLocal<>("THREAD_LOCAL_START_TIME");
    private static final ThreadLocal<AdminOperateLog> THREAD_LOCAL_ADMIN_OPERATE_LOG = new NamedThreadLocal<>("THREAD_LOCAL_ADMIN_OPERATE_LOG");
    private static final ThreadLocal<BaseParams.HeaderInfo> HEADER_INFO = TokenAspect.HEADER_INFO;

    private final HttpServletRequest request;
    private final AdminOperateLogService adminOperateLogServiceImpl;
    private final AdminCache adminCache;

    @Pointcut("execution(* com.xinbo.sports.backend.controller..*(..))"
            + " && !@annotation(com.xinbo.sports.backend.aop.annotation.UnCheckToken)"
            + " && !@annotation(com.xinbo.sports.backend.aop.annotation.UnCheckLog)")
    public void logPointcut() {
        // do nothing here
    }

    @Before("logPointcut()")
    public void doBefore(JoinPoint joinPoint) {
        log.info("=====> @doBefore");
        THREAD_LOCAL_START_TIME.set(LocalDateTime.now().atZone(ZoneId.systemDefault()));
        HEADER_INFO.set(IAdminInfoBase.getHeadLocalData());
    }

    @After("logPointcut()")
    public void doAfter(JoinPoint joinPoint) {
        String clientIp = IpUtil.getIp(request);
        log.info("=====> @After 计时开始:{}\tURI:{}", THREAD_LOCAL_START_TIME.get().toInstant().toEpochMilli(), request.getRequestURI());
        String requestUri = request.getRequestURI();
        String paramValues = "";
        if (joinPoint.getArgs().length > 0) {
            paramValues = JSON.toJSONString(joinPoint.getArgs()[0]);
        }
        var admin = adminCache.getAdminInfoById(HEADER_INFO.get().id);
        if (admin.getAdminGroupId() != 0) {
            // add AdminOperateLog
            AdminOperateLog operateLog = new AdminOperateLog();
            operateLog.setUid(HEADER_INFO.get().getId());
            operateLog.setUsername(HEADER_INFO.get().getUsername());
            operateLog.setIp(clientIp);
            operateLog.setUrl(requestUri);
            operateLog.setReqParams(paramValues);
            int currentTime = (int) LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond();
            operateLog.setCreatedAt(currentTime);
            operateLog.setUpdatedAt(currentTime);
            THREAD_LOCAL_ADMIN_OPERATE_LOG.set(operateLog);
        }


        // 统计耗时
        long start = THREAD_LOCAL_START_TIME.get().toInstant().toEpochMilli();
        long end = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        log.info("=====> @After 计时结束:{}\tURI:{}\t耗时:{}毫秒", end, requestUri, (end - start));

        // clear
        THREAD_LOCAL_START_TIME.remove();
        HEADER_INFO.remove();
        TenantContextHolder.clear();
    }


    @AfterReturning(returning = "result", pointcut = "logPointcut()")
    public void doAfterReturning(Object result) {
        if (null != THREAD_LOCAL_ADMIN_OPERATE_LOG.get()) {
            AdminOperateLog adminOperateLog = THREAD_LOCAL_ADMIN_OPERATE_LOG.get();
            var str = result + "";
            var resParams = str.length() > 10000 ? str.substring(0, 10000) : str;
            adminOperateLog.setResParams(resParams);
            ThreadPoolExecutorFactory.LOG_AOP_THREAD_POOL.execute(new SaveLogThread(adminOperateLog, adminOperateLogServiceImpl));
            log.info("=====> @AfterReturning\t{}", JSON.toJSONString(adminOperateLog));
            THREAD_LOCAL_ADMIN_OPERATE_LOG.remove();
        }
    }


    /**
     * 异常通知 记录操作报错日志
     *
     * @param joinPoint joinPoint
     * @param e         Throwable
     */
    @AfterThrowing(pointcut = "logPointcut()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Throwable e) {
        if (null != THREAD_LOCAL_ADMIN_OPERATE_LOG.get()) {
            AdminOperateLog adminOperateLog = THREAD_LOCAL_ADMIN_OPERATE_LOG.get();
            StackTraceElement[] stackTrace = e.getStackTrace();
            for (StackTraceElement element : stackTrace) {
                log.info(element.toString());
            }
            adminOperateLog.setResParams("" + e);
            ThreadPoolExecutorFactory.LOG_AOP_THREAD_POOL.execute(new SaveLogThread(adminOperateLog, adminOperateLogServiceImpl));
            log.error("=====> @AfterThrowing\t{}", JSON.toJSONString(adminOperateLog));
            THREAD_LOCAL_ADMIN_OPERATE_LOG.remove();
        }
    }


    /**
     * 保存日志线程
     */
    private static class SaveLogThread implements Runnable {
        private AdminOperateLog adminOperateLog;
        private AdminOperateLogService adminOperateLogServiceImpl;

        public SaveLogThread(AdminOperateLog log, AdminOperateLogService logService) {
            this.adminOperateLog = log;
            this.adminOperateLogServiceImpl = logService;
        }

        @Override
        public void run() {
            if (!Constant.ROOT.equals(adminOperateLog.getUsername())) {
                adminOperateLogServiceImpl.save(adminOperateLog);
            }
        }
    }
}
