package com.xinbo.sports.service.exception;


import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

/**
 * @author: David
 * @date: 03/03/2020
 * @description:
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BusinessException extends RuntimeException {

    protected final Integer code;
    protected final String message;

    public BusinessException(@NotNull CodeInfo codeInfo) {
        this.code = codeInfo.getCode();
        this.message = codeInfo.getMsg();
    }

    public BusinessException(String msg) {
        this.code = CodeInfo.STATUS_CODE_ERROR.getCode();
        this.message = msg;
    }
}
