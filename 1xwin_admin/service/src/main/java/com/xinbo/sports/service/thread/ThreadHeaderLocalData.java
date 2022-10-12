package com.xinbo.sports.service.thread;

import static com.xinbo.sports.service.io.dto.BaseParams.HeaderInfo;

/**
 * @author: David
 * @date: 06/04/2020
 * @description:
 */
public interface ThreadHeaderLocalData {
    ThreadLocal<HeaderInfo> HEADER_INFO_THREAD_LOCAL = new ThreadLocal<>();
}

