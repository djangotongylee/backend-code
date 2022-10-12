package com.xinbo.sports.plat.aop.annotation;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: wells
 * @date: 2020/8/18
 * @description: 三方平台拉取数据异常枚举
 */
@Getter
@AllArgsConstructor
public enum ExceptionEnum {
    PULL_EXCEPTION("pull_data", "拉单异常"),
    SUPPLEMENT_EXCEPTION("supplement_exception", "补单异常"),
    REGRESSION_EXCEPTION("regression_exception", "拉单异常回归异常"),
    MANUAL_PULL_EXCEPTION("manual_supplement_exception", "手动补单异常"),
    MANUAL_SUPPLEMENT_EXCEPTION("manual_supplement_exception", "手动拉单异常");

    private String code;
    private String msg;
}
