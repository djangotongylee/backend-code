package com.xinbo.sports.plat.service.impl.pg;

import com.xinbo.sports.plat.factory.IVerificationMethods;
import com.xinbo.sports.plat.io.bo.PGRequestParameter;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams;
import com.xinbo.sports.plat.io.dto.base.BetslipsDetailDto;
import com.xinbo.sports.plat.service.impl.PGServiceImpl;
import com.xinbo.sports.utils.DESCUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.util.Map;

import static com.alibaba.fastjson.JSON.toJSONString;
import static java.net.URLDecoder.decode;


@Slf4j
@Service
public class PGChessServiceImpl extends PGServiceImpl implements IVerificationMethods {
    @SneakyThrows
    @Override
    public PlatFactoryParams.PlatGameLoginResDto login(PlatFactoryParams.PlatLoginReqDto reqDto) {
        var lang = EnumUtils.getEnumIgnoreCase(PGRequestParameter.LANGS.class, reqDto.getLang()).getCode();
        DESCUtils descUtils = new DESCUtils(config.getOperatorToken());
        String encrypt = descUtils.encrypt(reqDto.getUsername());
        String url = config.getLobbyUrl() + "?operator_token=" + config.getOperatorToken()
                + "&operator_player_session=" + URLEncoder.encode(encrypt) + "&bet_type=1&language=" + lang;
        return PlatFactoryParams.PlatGameLoginResDto.builder().type(1).url(url).build();
    }

    public BetslipsDetailDto.verifySession verifySession(Map<String, String> reqDto) {
        BetslipsDetailDto.verifySession verifySession = new BetslipsDetailDto.verifySession();
        BetslipsDetailDto.data data = new BetslipsDetailDto.data();
        BetslipsDetailDto.error error = new BetslipsDetailDto.error();
        DESCUtils descUtils = new DESCUtils(config.getOperatorToken());
        try {
            if (reqDto.get("operator_token").equals(config.getOperatorToken()) && reqDto.get("secret_key").equals(config.getSecretKey())) {
                verifySession.setError(null);
                var decrypt = descUtils.decrypt(decode(reqDto.get("operator_player_session")));
                data.setPlayer_name(decrypt);
                data.setCurrency(config.getCurrency());
                verifySession.setData(data);
                log.info(MODEL + "验证成功登陆：" + toJSONString(verifySession));
                return verifySession;
            } else {
                error.setCode("1034");
                error.setMessage("错误请求");
                verifySession.setError(error);
                verifySession.setData(null);
                log.error(MODEL + "验证请求参数：" + toJSONString(reqDto));
                log.error(MODEL + "验证成功失败：" + toJSONString(verifySession));
                return verifySession;
            }
        } catch (Exception e) {
            error.setCode("1200");
            error.setMessage("内部服务器错误");
            verifySession.setError(error);
            verifySession.setData(null);
            log.error(MODEL + "验证请求参数：" + toJSONString(reqDto));
            log.error(MODEL + "验证成功失败：" + toJSONString(verifySession));
            return verifySession;
        }
    }

    @Override
    public <T> T verifySession(HttpServletRequest reqDto) {
        BetslipsDetailDto.verifySession verifySession = new BetslipsDetailDto.verifySession();
        BetslipsDetailDto.data data = new BetslipsDetailDto.data();
        BetslipsDetailDto.error error = new BetslipsDetailDto.error();
        DESCUtils descUtils = new DESCUtils(config.getOperatorToken());
        try {
            if (reqDto.getParameter("operator_token").equals(config.getOperatorToken()) && reqDto.getParameter("secret_key").equals(config.getSecretKey())) {
                verifySession.setError(null);
                var decrypt = descUtils.decrypt(decode(reqDto.getParameter("operator_player_session")));
                data.setPlayer_name(decrypt);
                data.setCurrency(config.getCurrency());
                verifySession.setData(data);
                log.info(MODEL + "验证成功登陆：" + toJSONString(verifySession));
                return (T) verifySession;
            } else {
                error.setCode("1034");
                error.setMessage("错误请求");
                verifySession.setError(error);
                verifySession.setData(null);
                log.error(MODEL + "验证请求参数：" + toJSONString(reqDto));
                log.error(MODEL + "验证成功失败：" + toJSONString(verifySession));
                return (T) verifySession;
            }
        } catch (Exception e) {
            error.setCode("1200");
            error.setMessage("内部服务器错误");
            verifySession.setError(error);
            verifySession.setData(null);
            log.error(MODEL + "验证请求参数：" + toJSONString(reqDto));
            log.error(MODEL + "验证成功失败：" + toJSONString(verifySession));
            return (T) verifySession;
        }
    }
}
