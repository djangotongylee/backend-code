package com.xinbo.sports.plat.factory;

import com.xinbo.sports.utils.SpringUtils;
import org.springframework.stereotype.Component;


import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import static com.alibaba.fastjson.JSON.parseObject;

/**
 * @author: David
 * @date: 05/06/2020
 */
@Component
public interface IVerificationMethods {

    @NotNull
    static <T> T init(String model) {
        try {
            var configJsonObject = parseObject(model);
            var className =  configJsonObject.getString("model") + "ServiceImpl";
            T factory = (T) SpringUtils.getBean(className);
            return factory;
        } catch (Exception e) {
        }
        return null;
    }

    <T>T verifySession(HttpServletRequest httpServletRequest);

}
