package com.xinbo.sports.payment.aop.aspect;

import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.utils.components.response.CodeInfo;
import com.xinbo.sports.utils.components.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <p>
 * 全局异常处理类
 * </p>
 *
 * @author andy
 * @since 2020-02-14
 */
@RestControllerAdvice
@Slf4j
public class ExceptionHandlerAspect {
    /**
     * 自定义异常类
     *
     * @param info 异常信息
     * @return 返回对象
     */
    @ExceptionHandler({BusinessException.class})
    public Result<String> businessException(@NotNull BusinessException info) {
        return Result.error(info.getCode(), info.getMessage());
    }

    /**
     * 参数校验异常
     *
     * @param exception 异常信息
     * @return 返回实体
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handlerBindException(@NotNull MethodArgumentNotValidException exception) {
        exception.getStackTrace();
        BindingResult bindingResult = exception.getBindingResult();
        StringBuilder errorMessage = new StringBuilder();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            errorMessage.append(fieldError.getDefaultMessage());
        }

        return Result.error(CodeInfo.PARAMETERS_INVALID.getCode(), errorMessage.toString());
    }


    /**
     * 系统异常
     *
     * @param e 异常信息
     * @return 返回信息
     */
    @ExceptionHandler({Exception.class})
    public Result<String> exception(@NotNull Exception e) {
        log.error(e.getMessage());
        return Result.error(CodeInfo.STATUS_CODE_ERROR.getCode(), e.getMessage());
    }
}

