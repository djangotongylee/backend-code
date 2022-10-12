package com.xinbo.sports.plat.io.bo;

import lombok.Builder;
import lombok.Data;

/**
 * Author:Abin
 * Date:2020/12/26 19:34
 **/
@Data
@Builder
public class EBETRequest {
    String cmd;
    Integer eventType;
    Integer channelId;
    String username;
    String accessToken;
    Long timestamp;
    String ip;
    String signature;
}
