package com.xinbo.sports.backend.service;

import com.xinbo.sports.service.io.dto.BaseParams;
import com.xinbo.sports.service.thread.ThreadHeaderLocalData;

/**
 * @description:
 * @author: andy
 * @date: 2020/8/6
 */
public interface IAdminInfoBase {
    /**
     * 获取Header信息
     *
     * @return 实体
     */
    static BaseParams.HeaderInfo getHeadLocalData() {
        return ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
    }
}
