package com.xinbo.sports.payment.base;

import com.xinbo.sports.service.cache.redis.AdminCache;
import com.xinbo.sports.service.cache.redis.UserCache;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.constant.ConstData;
import com.xinbo.sports.service.io.enums.BaseEnum;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

import static com.xinbo.sports.service.io.dto.BaseParams.HeaderInfo;

/**
 * @author: David
 * @date: 06/06/2020
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HeaderBase {
    private final UserCache userCache;
    private final AdminCache adminCache;

    /**
     * 无需Check Token有效性时 获取头部自定义参数信息
     *
     * @return token、lang、device
     */
    public HeaderInfo getHeaderLocalData(Boolean isValid, String end) {

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert attributes != null;
        HttpServletRequest request = attributes.getRequest();

        // 判定是否设置语言
        String lang = request.getHeader(ConstData.LANG);
        if (lang == null || !EnumUtils.isValidEnumIgnoreCase(BaseEnum.LANG.class, lang)) {
            throw new BusinessException(CodeInfo.HEADER_LANG_ERROR);
        }

        // 判定是否设置访问设备
        String device = request.getHeader(ConstData.DEVICE);
        if (device == null || !EnumUtils.isValidEnumIgnoreCase(BaseEnum.DEVICE.class, device)) {
            throw new BusinessException(CodeInfo.HEADER_DEVICE_ERROR);
        }

        // 不强制校验获取到语言、设备信息直接返回
        if (Boolean.FALSE.equals(isValid)) {
            return HeaderInfo.builder().lang(lang).device(device).build();
        }

        String apiToken = request.getHeader("Authorization");
        if (StringUtils.isNotBlank(apiToken) && apiToken.startsWith(ConstData.TOKEN_START_WITH)) {
            // 获取正式apiToken
            apiToken = apiToken.substring(ConstData.TOKEN_START_WITH.length());
        } else {
            throw new BusinessException(CodeInfo.STATUS_CODE_401);
        }

        try {
            // Token 验证有效性
            HeaderInfo headerInfo = end.equals("Admin") ? adminCache.validUserToken(apiToken) : userCache.validUserToken(apiToken);
            headerInfo.setDevice(device);
            headerInfo.setLang(lang);
            return headerInfo;
        } catch (Exception e) {
            log.error(e.toString());
            throw new BusinessException(CodeInfo.STATUS_CODE_401);
        }
    }
}
