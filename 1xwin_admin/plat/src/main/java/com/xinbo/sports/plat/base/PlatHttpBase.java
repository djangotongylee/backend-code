package com.xinbo.sports.plat.base;

import com.alibaba.fastjson.JSONObject;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.utils.HttpUtils;
import com.xinbo.sports.utils.components.response.CodeInfo;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * @author: wells
 * @date: 2020/8/19
 * @description: 三方拉单http
 */
public final class PlatHttpBase {
    private PlatHttpBase() {

    }

    /**
     * post请求
     *
     * @param url
     * @param postData
     * @param headerData
     * @param model
     * @return
     */
    public static String doPost(String url, JSONObject postData, Map<String, String> headerData, String model) {
        var reStr = "";
        try {
            reStr = HttpUtils.doPost(url, postData, headerData, model);
        } catch (Exception e) {
            throw new BusinessException(CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0);
        }
        return reStr;
    }

    /**
     * 异步get
     *
     * @param uri
     * @return
     */
    public static String sendAsyncGET(String uri) {
        var reStr = "";
        try {
            reStr = HttpUtils.sendAsyncGET(uri);
        } catch (Exception e) {
            throw new BusinessException(CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0);
        }
        return reStr;
    }

    /**
     * gg捕鱼get
     *
     * @param uri
     * @param json
     * @param cagent
     * @return
     */
    public static String ggHttpGet(String uri, String json, String cagent) {
        var reStr = "";
        try {
            reStr = HttpUtils.ggHttpGet(uri, json, cagent);
        } catch (Exception e) {
            throw new BusinessException(CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0);
        }
        return reStr;
    }

    /**
     * get请求
     *
     * @param reqURI
     * @param model
     * @return
     * @throws Exception
     */
    public static String doGet(String reqURI, String model) throws Exception {
        var reStr = "";
        try {
            reStr = HttpUtils.doGet(reqURI, model);
        } catch (Exception e) {
            CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0.setMsg(e.getMessage());
            throw new BusinessException(CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0);
        }
        return reStr;
    }

    /**
     * post请求
     *
     * @param reqURI
     * @param requestBody
     * @param model
     * @return
     * @throws Exception
     */
    public static String doPost(String reqURI, String requestBody, String model) throws Exception {
        var reStr = "";
        try {
            reStr = HttpUtils.doPost(reqURI, requestBody, model);
        } catch (Exception e) {
            CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0.setMsg(e.getMessage());
            throw new BusinessException(CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0);
        }
        return reStr;
    }

    /**
     * post请求
     *
     * @param uri
     * @param params
     * @return
     */
    public static String postHttp(String uri, String params) {
        var reStr = "";
        try {
            reStr = HttpUtils.postHttp(uri, params);
        } catch (Exception e) {
            CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0.setMsg(e.getMessage());
            throw new BusinessException(CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0);
        }
        return reStr;
    }

    /**
     * @param strUrl
     * @param postData
     * @param header
     * @return
     * @throws InterruptedException
     * @throws IOException
     * @throws URISyntaxException
     */
    public static String send(String strUrl, Map<String, String> postData, Map<String, String> header) throws InterruptedException, IOException, URISyntaxException {
        var reStr = "";
        try {
            reStr = HttpUtils.send(strUrl, postData, header);
        } catch (Exception e) {
            CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0.setMsg(e.getMessage());
            throw new BusinessException(CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0);
        }
        return reStr;
    }
}
