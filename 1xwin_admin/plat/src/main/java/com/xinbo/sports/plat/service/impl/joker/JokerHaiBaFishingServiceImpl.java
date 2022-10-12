package com.xinbo.sports.plat.service.impl.joker;

import com.xinbo.sports.plat.io.bo.JokerRequestParameter;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams;
import com.xinbo.sports.plat.io.enums.JokerPlatEnum;
import com.xinbo.sports.plat.service.impl.JokerServiceImpl;
import com.xinbo.sports.service.io.enums.BaseEnum;
import com.xinbo.sports.utils.DateNewUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.stereotype.Service;

import static com.alibaba.fastjson.JSON.parseObject;

@Slf4j
@Service
public class JokerHaiBaFishingServiceImpl extends JokerServiceImpl {
    @Override
    public PlatFactoryParams.PlatGameLoginResDto login(PlatFactoryParams.PlatLoginReqDto reqDto) {
        var lang = EnumUtils.getEnumIgnoreCase(JokerRequestParameter.LANGS.class, reqDto.getLang()).getCode();
        var loginBO = JokerRequestParameter.Login.builder()
                .method(JokerPlatEnum.JokerMethodEnum.PLAY.getMethodName())
                .timestamp(DateNewUtils.now())
                .username(reqDto.getUsername())
                .build();
        var send = send(config.getApiUrl(), loginBO, JokerPlatEnum.JokerMethodEnum.PLAY.getMethodNameDesc());
        var token = parseObject(send).getString("Token");
        var mobile = reqDto.getDevice().equals(BaseEnum.DEVICE.M.getValue()) ? true : false;
        return PlatFactoryParams.PlatGameLoginResDto.builder().type(1)
                .url(config.getForwardUrl() + "?token=" + token + "&game=" + HAIBA + "&redirectUrl=" + config.getRedirectUrl() + "&mobile=" + mobile + "&lang=" + lang).build();
    }
}
