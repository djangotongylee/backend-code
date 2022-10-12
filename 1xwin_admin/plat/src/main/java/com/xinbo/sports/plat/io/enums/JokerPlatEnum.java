package com.xinbo.sports.plat.io.enums;

import com.google.common.collect.ImmutableMap;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.*;
import org.apache.groovy.util.Maps;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author: wells
 * @date: 2020/5/28
 * @description:
 */

public interface JokerPlatEnum {


    /**
     * mg方法枚举
     */
    @Getter
    @AllArgsConstructor
    enum JokerMethodEnum {
        GC("GC", "查询余额"),
        TC("TC", "转账"),
        CU("CU", "创建玩家"),
        TSM("TSM", "投注记录"),
        TCH("TCH", "校验转账"),
        PLAY("PLAY", "大厅"),
        LISTGAMES("ListGames", "游戏列表"),
        ;


        private String methodName;
        private String methodNameDesc;
    }


    /**
     * 错误码集合
     */
    Map<Integer, CodeInfo> MAP = Maps.of(
            400, CodeInfo.PLAT_INVALID_PARAM,
            404, CodeInfo.PLAT_ACCOUNT_NOT_EXISTS,
            401, CodeInfo.PLAT_ACCOUNT_OCCUPATION,
            403, CodeInfo.PLAT_IP_NOT_ACCESS,
            500, CodeInfo.PLAT_SYSTEM_ERROR
    );

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class PlatConfig {
        String appID;
        String secret;
        String apiUrl;
        String forwardUrl;
        String secretKey;
        String environment;
        String redirectUrl;
    }

    Map<String, String> HTTP_HEADER = new ImmutableMap.Builder<String, String>().
            put("Content-Type", "application/json").
            put("charset", StandardCharsets.UTF_8.toString()).
            build();
}
