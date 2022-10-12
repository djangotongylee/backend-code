package com.xinbo.sports.service.base;

import lombok.Builder;
import lombok.Data;

import java.util.Objects;

/**
 * @author: wells
 * @date: 2020/8/19
 * @description:
 */

public interface ExceptionThreadLocal {
    public static ThreadLocal<ExceptionDto> THREAD_LOCAL = new ThreadLocal<>();


    @Data
    @Builder
    class ExceptionDto {
        //开始时间
        private String startTime;
        //结束时间
        private String endTime;
        //请求参数
        private String requestParams;
        //补单id或拉单异常id
        private Long orderId;
    }

    /**
     * 请求参数requestParams加入到 THREAD_LOCAL
     *
     * @param requestParams
     */
    public static void setRequestParams(String requestParams) {
        var exceptionDto = THREAD_LOCAL.get();
        if (Objects.isNull(exceptionDto)) {
            var dto = ExceptionDto.builder().requestParams(requestParams).build();
            THREAD_LOCAL.set(dto);
        } else {
            exceptionDto.setRequestParams(requestParams);
        }
    }

    public static void setOrderId(Long orderId) {
        var exceptionDto = THREAD_LOCAL.get();
        if (Objects.isNull(exceptionDto)) {
            var dto = ExceptionDto.builder().orderId(orderId).build();
            THREAD_LOCAL.set(dto);
        } else {
            exceptionDto.setOrderId(orderId);
        }

    }

}
