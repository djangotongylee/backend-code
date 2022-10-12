package com.xinbo.sports.plat.service.impl.ebet;

import com.alibaba.fastjson.JSONObject;
import com.xinbo.sports.plat.factory.IVerificationMethods;
import com.xinbo.sports.plat.io.bo.EBETRespose;
import com.xinbo.sports.plat.service.impl.EBETServiceImpl;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;

import static com.alibaba.fastjson.JSON.parseObject;

@Slf4j
@Service
public class EBETLiveServiceImpl extends EBETServiceImpl implements IVerificationMethods {


    @Override
    public <T> T verifySession(HttpServletRequest request) {
        try {
            BufferedReader br = request.getReader();
            String str = "";
            String wholeStr = "";
            while ((str = br.readLine()) != null) {
                wholeStr += str;
            }
            JSONObject body = parseObject(wholeStr);
            String key = body.getString("timestamp") + body.getString("accessToken");
            boolean verify = verify(key.getBytes(), config.getPublick_key(), body.getString("signature"));
            if (verify) {
                return (T) EBETRespose.builder()
                        .username(body.getString("username"))
                        .accessToken(body.getString("accessToken"))
                        .nickname(configCache.getPlatPrefix() + body.getString("username"))
                        .currency(config.getCurrency())
                        .subChannelId(0)
                        .status(200)
                        .build();
            }
            return null;
        } catch (Exception e) {
            log.error(e.toString());
            throw new BusinessException(CodeInfo.LOGIN_INVALID);
        }
    }
}
