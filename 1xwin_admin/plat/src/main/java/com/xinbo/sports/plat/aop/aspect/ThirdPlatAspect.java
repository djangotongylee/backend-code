package com.xinbo.sports.plat.aop.aspect;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xinbo.sports.dao.generator.service.BetSlipsExceptionService;
import com.xinbo.sports.dao.generator.service.BetSlipsSupplementalService;
import com.xinbo.sports.plat.aop.annotation.ExceptionEnum;
import com.xinbo.sports.plat.aop.annotation.ThirdPlatException;
import com.xinbo.sports.plat.base.CommonPersistence;
import com.xinbo.sports.service.base.ExceptionThreadLocal;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.components.response.CodeInfo;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.function.BiConsumer;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.alibaba.fastjson.JSON.toJSONString;

/**
 * @author: wells
 * @date: 2020/8/18
 * @description:
 */
@Slf4j
@Aspect
@Component
public class ThirdPlatAspect {
    @Autowired
    private CommonPersistence commonPersistence;
    @Autowired
    private BetSlipsSupplementalService betSlipsSupplementalServiceImpl;
    @Autowired
    private BetSlipsExceptionService betSlipsExceptionServiceImpl;
    //异常日志打印函数
    BiConsumer<String, Exception> biConsumer = (message, exception) -> {
        var msg = (Objects.nonNull(exception) ? exception.getMessage() : "");
        log.error(message + ":" + msg);
        XxlJobLogger.log(message + ":" + msg);
        if (Objects.nonNull(exception)) {
            StackTraceElement[] stackTrace = exception.getStackTrace();
            for (StackTraceElement element : stackTrace) {
                XxlJobLogger.log(element.toString());
            }
        }
    };

    /**
     * 三方异常处理
     */
    @AfterThrowing(pointcut = "@annotation(com.xinbo.sports.plat.aop.annotation.ThirdPlatException)", throwing = "e")
    public void handleThrowing(JoinPoint joinPoint, Exception e) {
        var now = DateNewUtils.now();
        //修改记录函数
        BiConsumer<Long, Object> baseConsumer = (orderId, iService) -> {
            //异常信息大于1000字符取1000；
            var msg = Objects.nonNull(e) ? e.getMessage() : "";
            msg = Strings.isNotEmpty(msg) && msg.length() > 1000 ? msg.substring(0, 1000) : msg;
            var jsonObject = new JSONObject();
            jsonObject.put("id", orderId);
            jsonObject.put("status", 2);
            jsonObject.put("info", msg);
            jsonObject.put("updatedAt", now);
            ((IService) iService).updateById(jsonObject);
        };
        var exceptionDto = ExceptionThreadLocal.THREAD_LOCAL.get();
        var signature = (MethodSignature) joinPoint.getSignature();
        var annotation = signature.getMethod().getAnnotation(ThirdPlatException.class);
        //获取异常类型
        var exceptionEnum = annotation.exceptionType();
        //日志信息
        var msg = "";
        //'异常状态:0-三方异常 1-数据异常',
        var category = 1;
        if (e instanceof BusinessException) {
            var businessException = (BusinessException) e;
            if (CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0.getCode().equals(businessException.getCode())) {
                category = 0;
            }
        }
        //手动补单异常处理,修改补单表的状态
        if (exceptionEnum.equals(ExceptionEnum.MANUAL_SUPPLEMENT_EXCEPTION)) {
            var object = joinPoint.getArgs()[0];
            var orderId = parseObject(toJSONString(object)).getLong("id");
            baseConsumer.accept(orderId, betSlipsSupplementalServiceImpl);
            msg = "id=" + orderId + ";手动补单异常";
        }
        //手动拉单异常处理,修改拉单异常表的状态
        if (exceptionEnum.equals(ExceptionEnum.MANUAL_PULL_EXCEPTION)) {
            var object = joinPoint.getArgs()[0];
            var orderId = parseObject(toJSONString(object)).getLong("id");
            baseConsumer.accept(orderId, betSlipsExceptionServiceImpl);
            msg = "id=" + orderId + ";手动处理拉单异常";
        }

        //未获取到参数前异常,打印日志
        if (Objects.isNull(exceptionDto)) {
            log.info(exceptionEnum.getMsg() + e.getMessage());
            biConsumer.accept(exceptionEnum.getMsg(), e);
            return;
        }
        //拉单异常
        if (exceptionEnum.equals(ExceptionEnum.PULL_EXCEPTION)) {
            var params = parseObject(String.valueOf(joinPoint.getArgs()[0]));
            var gameId = params.getInteger("gameId");
            commonPersistence.addBetSlipsException(gameId, exceptionDto.getRequestParams(), category, e.getMessage(), 0);
            msg = "游戏ID=" + gameId + ";参数=" + exceptionDto.getRequestParams() + ";拉单异常";
        }
        //补单异常，修改异常表的状态
        if (exceptionEnum.equals(ExceptionEnum.SUPPLEMENT_EXCEPTION)) {
            baseConsumer.accept(exceptionDto.getOrderId(), betSlipsSupplementalServiceImpl);
            msg = "id=" + exceptionDto.getOrderId() + ";补单异常";
        }
        //拉单异常回归，修改拉单异常表状态
        if (exceptionEnum.equals(ExceptionEnum.REGRESSION_EXCEPTION)) {
            baseConsumer.accept(exceptionDto.getOrderId(), betSlipsExceptionServiceImpl);
            msg = "id=" + exceptionDto.getOrderId() + ";拉单异常回归";
        }
        biConsumer.accept(msg, e);
    }
}
