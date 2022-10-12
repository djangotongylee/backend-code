package com.xinbo.sports.utils;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.thoughtworks.xstream.XStream;

import java.util.function.Supplier;

/**
 * @Author william
 * @Date 2020/4/20 17:08
 * @Version 1.0
 **/
public class XmlBuilder {

    /**
     * 将XML转为指定的POJO对象
     *
     * @param xml         xml数据
     * @param destination 需要转换的类
     * @return
     */
    public static <T> T xmlStrToObject(String xml, Supplier<T> destination) {
        T t = destination.get();
        XStream xstream = new XStream();
        xstream.processAnnotations(t.getClass());
        xstream.autodetectAnnotations(true);
        try {
            if (StringUtils.isNotBlank(xml)) {
                t = (T) xstream.fromXML(xml);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return t;
        }
    }

    public static JSONObject xmlStr2JSONObject() {
        XStream xstream = new XStream();
        JSONObject json = new JSONObject();
        return json;
    }

}
