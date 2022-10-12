package com.xinbo.sports.utils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: wells
 * @date: 2020/7/7
 * @description:
 */
@Slf4j
public class I18nUtils extends ReloadableResourceBundleMessageSource {

    /**
     * 请求头信息，通过此解析
     */
    public static final String HTTP_ACCEPT_LANGUAGE = "Accept-Language";


    /**
     * 获取当前语种
     *
     * @return
     */
    public static Locale getLocale() {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest httpServletRequest = servletRequestAttributes.getRequest();
        String locale = httpServletRequest.getHeader(HTTP_ACCEPT_LANGUAGE);
        if (LANG_COUNTRY.EN_US.language.equals(locale)) {
            return Locale.US;
        } else if (LANG_COUNTRY.VI_VN.language.equals(locale)) {
            return new Locale(LANG_COUNTRY.VI_VN.language, LANG_COUNTRY.VI_VN.country);
        } else if (LANG_COUNTRY.TH_TH.language.equals(locale)) {
            return new Locale(LANG_COUNTRY.TH_TH.language, LANG_COUNTRY.TH_TH.country);
        }
        return Locale.SIMPLIFIED_CHINESE;
    }

    /**
     * 根据语种获取国际化信息
     *
     * @param propertyKey
     * @return
     */
    public static String getLocaleMessage(String propertyKey) {
        Locale locale = getLocale();
        if (StringUtils.isNotEmpty(propertyKey)) {
            var bundleMessageSource = (ResourceBundleMessageSource) SpringUtils.getBean("bundleMessageSource");
            String key = "";
            //活动文本内容不读国际化文件
            if (!propertyKey.contains("<") && !propertyKey.contains(">")) {
                key = propertyKey.replace(" ", "");
            }
            try {
                return bundleMessageSource.getMessage(key, null, locale);
            } catch (RuntimeException e) {
                return propertyKey;
            }
        }
        return propertyKey;
    }

    /**
     * 根据语种获取国际化信息
     *
     * @param propertyKey
     * @param params
     * @return
     */
    public static String getLocaleMessageWithPlaceHolder(String propertyKey, Object... params) {
        Locale locale = getLocale();
        var bundleMessageSource = (ResourceBundleMessageSource) SpringUtils.getBean("bundleMessageSource");
        return bundleMessageSource.getMessage(propertyKey, params, locale);
    }

    /**
     * 在图片的末尾自动加上对应彩种的后缀(a.png -> a_ZH.png)
     *
     * @param img 图片地址
     * @return 切换后的图片目录
     */
    @NotNull
    public static String getLocaleImg(@NotNull String img) {
        int i = img.lastIndexOf('.');
        if (i == -1) {
            return img;
        }

        Locale locale = getLocale();
        String localeUrl = locale.getLanguage() + '-' + locale.getCountry();
        return img.substring(0, i) + "_" + localeUrl + img.substring(i);
    }

    /**
     * 在图片的末尾自动加上对应彩种的后缀(a.png -> a_ZH.png)
     *
     * @param img    图片地址
     * @param locale locale
     * @return 切换后的图片目录
     */
    @NotNull
    public static String getLocaleImg(@NotNull String img, Locale locale) {
        int i = img.lastIndexOf('.');
        if (i == -1) {
            return img;
        }

        String localeUrl = locale.getLanguage() + '-' + locale.getCountry();
        return img.substring(0, i) + "_" + localeUrl + img.substring(i);
    }

    @SneakyThrows
    public List resolveCode(Locale locale, String paramKey) {
        List<String> list = new ArrayList<>();
        var bundleMessageSource = (ResourceBundleMessageSource) SpringUtils.getBean("bundleMessageSource");
        var basename = bundleMessageSource.getBasenameSet().iterator().next();
        List<String> filenames = calculateAllFilenames(basename, locale);
        var fileList = filenames.stream().filter(x -> x.contains(locale.getLanguage() + "_" + locale.getCountry())).collect(Collectors.toList());
        PropertiesHolder propHolder = getProperties(fileList.get(0));
        var proper = propHolder.getProperties();
        for (Map.Entry<Object, Object> entry : proper.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue().toString().replace(" ", "");
            if (value.contains(paramKey.replace(" ", ""))) {
                list.add(key.toString());
            }
        }
        return list;
    }

    @SneakyThrows
    public List<String> resolveCode(Locale locale) {
        List<String> list = new ArrayList<>();
        var bundleMessageSource = (ResourceBundleMessageSource) SpringUtils.getBean("bundleMessageSource");
        var basename = bundleMessageSource.getBasenameSet().iterator().next();
        List<String> filenames = calculateAllFilenames(basename, locale);
        var fileList = filenames.stream().filter(x -> x.contains(locale.getLanguage() + "_" + locale.getCountry())).collect(Collectors.toList());
        PropertiesHolder propHolder = getProperties(fileList.get(0));
        var proper = propHolder.getProperties();
        for (Map.Entry<Object, Object> entry : proper.entrySet()) {
            var value = entry.getValue();
            var key = entry.getKey().toString().replace(" ", "");
            var ele = key.replace(" ", "") + "=" + value;
            list.add(ele);
        }
        return list;
    }

    @Getter
    @NoArgsConstructor
    enum LANG_COUNTRY {
        /**
         * 请求语言参数
         */
        // 繁体
        // ZH_TW("zh", "TW"),
        // 中文
        ZH_CN("zh", "CN"),
        // 英语
        EN_US("en", "US"),
        // 泰语
        TH_TH("th", "TH"),
        // 越南语
        VI_VN("vi", "VN");

        String language;
        String country;

        LANG_COUNTRY(String language, String country) {
            this.language = language;
            this.country = country;
        }
    }
}
