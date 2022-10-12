package com.xinbo.sports.plat.io.bo;

import lombok.Builder;
import lombok.Data;

/**
 * Author:Abin
 * Date:2020/12/26 19:34
 **/
@Data
@Builder
public class EBETRespose {
    String accessToken;
    Integer subChannelId;
    String username;
    Integer status;
    String nickname;
    String currency;
}
