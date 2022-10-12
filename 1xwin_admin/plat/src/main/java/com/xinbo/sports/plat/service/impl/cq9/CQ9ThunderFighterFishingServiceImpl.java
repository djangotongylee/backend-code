package com.xinbo.sports.plat.service.impl.cq9;

import com.alibaba.fastjson.JSONObject;
import com.xinbo.sports.plat.io.bo.CQ9RequestParameter;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams;
import com.xinbo.sports.plat.io.enums.CQ9PlatEnum;
import com.xinbo.sports.plat.service.impl.CQ9ServiceImpl;
import com.xinbo.sports.service.io.enums.BaseEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CQ9ThunderFighterFishingServiceImpl extends CQ9ServiceImpl {
    @Override
    public PlatFactoryParams.PlatGameLoginResDto login(PlatFactoryParams.PlatLoginReqDto reqDto) {
        var slotLoginBO = CQ9RequestParameter.GameLink.builder()
                .gamecode("AT04")
                .gameplat(BaseEnum.DEVICE.D.getValue().equals(reqDto.getDevice()) ? "web" : "mobile")
                .usertoken(getUserToken(reqDto))
                .lang(EnumUtils.getEnumIgnoreCase(CQ9RequestParameter.LANGS.class, reqDto.getLang()).getCode())
                .gamehall("CQ9").build();
        JSONObject send = send(config.getApiUrl() + CQ9PlatEnum.CG9MethodEnum.GAMELINK.getMethodName(), slotLoginBO, config.getApiUrl() + CQ9PlatEnum.CG9MethodEnum.GAMELINK.getMethodNameDesc());
        return PlatFactoryParams.PlatGameLoginResDto.builder().type(1).url(send.getJSONObject("data").getString("url")).build();
    }
}
