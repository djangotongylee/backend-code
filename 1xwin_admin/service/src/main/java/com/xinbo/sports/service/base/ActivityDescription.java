package com.xinbo.sports.service.base;

import com.xinbo.sports.service.cache.redis.ConfigCache;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * @author: wells
 * @date: 2020/7/22
 * @description:
 */
@Slf4j
@Component
public class ActivityDescription {
    /**
     * 请求头信息，通过此解析
     */
    public static final String HTTP_ACCEPT_LANGUAGE = "Accept-Language";
    /**
     * 公共目录
     */
    public static final String COMMON_FILE_PATH = "/promotions/description/bwg/i18n/";
    /**
     * 文件前缀
     */
    public static final String PROMOTIONS_PREFIX = "promotions_";

    @Resource
    private ConfigCache configCache;

    /**
     * 操作文件标识
     */
    @Getter
    enum Description {
        SAVE(),
        UPDATE(),
        DELETE();
    }

    /**
     * 根据语言获取活动的详细描述
     *
     * @return
     */

    public static String getDescription(Integer proId, String locale) {
        String reStr = "";
        try {
//            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//            HttpServletRequest httpServletRequest = servletRequestAttributes.getRequest();
//            String locale = File.separator + httpServletRequest.getHeader(HTTP_ACCEPT_LANGUAGE) + File.separator;
            log.info("getDescription=" + locale);
            //获取当前类下的resource路径
            InputStream proIn = ActivityDescription.class.getClassLoader().getResourceAsStream("activity" + locale + "promotions_" + proId + ".txt");
            BufferedReader proBuffer = new BufferedReader(new InputStreamReader(proIn));
            String line = "";
            var stringBuilder = new StringBuilder();
            while ((line = proBuffer.readLine()) != null) {
                stringBuilder.append(line);
            }
            reStr = stringBuilder.toString();
            log.info("info=" + proBuffer.readLine());
            URL url = ActivityDescription.class.getClassLoader().getResource("activity");
            log.info("url=" + url);
        } catch (Exception e) {
            log.info("读取文件失败" + e.getMessage());
        }
        return reStr;
    }

    @SneakyThrows
    public String getStaticDescription(Integer proId) {
        String staticServer = configCache.getStaticServer();
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest httpServletRequest = servletRequestAttributes.getRequest();
        //  String locale = httpServletRequest.getHeader(HTTP_ACCEPT_LANGUAGE);
        String locale = "zh";
        var path = staticServer + COMMON_FILE_PATH + locale + "/" + PROMOTIONS_PREFIX + proId + ".txt";
        var stringBuilder = new StringBuilder();
        //根据网络文件地址创建URL
        URL url = new URL(path);
        URLConnection urlConn = url.openConnection();
        urlConn.setDoOutput(true);
        OutputStreamWriter out = new OutputStreamWriter(
                urlConn.getOutputStream());
        out.write("string=" + 1234);

        //  OutputStream outputStream = urlConn.getOutputStream();
        //  outputStream.write("12345".getBytes());
        out.flush();
        out.close();
        //获取此路径的连接
        //  HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        //InputStream is = conn.getInputStream();
        // try (
        // BufferedReader proBuffer = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        //) {
        String line = "";
        //   while ((line = proBuffer.readLine()) != null) {
        stringBuilder.append(line);
        //  }
        //  } catch (Exception e) {
        //log.info("读取文件失败" + e.getMessage());
        //  }
        return stringBuilder.toString();
    }


    public void getFileContent(Integer proId) {
        String staticServer = configCache.getStaticServer();
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest httpServletRequest = servletRequestAttributes.getRequest();
        //String locale = httpServletRequest.getHeader(HTTP_ACCEPT_LANGUAGE);
        String locale = "zh";
        var path = staticServer + COMMON_FILE_PATH + locale + "/" + PROMOTIONS_PREFIX + proId + ".txt";
        BufferedReader bf = null;
        String line = "";//文件每行内容
        String result = "";//文件结果内容
        try {
            URL url = new URL(path);
            //建立URL链接
            URLConnection conn = url.openConnection();
            //设置模拟请求头
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            //开始链接
            conn.connect();
            //因为要用到URLConnection子类的方法，所以强转成子类
            HttpURLConnection urlConn = (HttpURLConnection) conn;
            //响应200
            if (urlConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                //字节或字符读取的方式太慢了，用BufferedReader封装按行读取
                bf = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

                while ((line = bf.readLine()) != null) {
                    result += line + "\n";
                }
                //result 获取得所有文件内容
                System.out.println(result);
                //通过已获取的文件内容   FTP上传至服务器新建文件中
            } else {
                System.out.println("无法链接到URL!");
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bf != null) {
                    bf.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public boolean changeValueByPropertyName(String propertiesFileName,
                                             String propertyName, String propertyValue) {
        // 获取src下的文件路径
        propertiesFileName = this.getClass().getResource(propertiesFileName).getPath();
        //System.out.println(propertiesFileName);
        boolean writeOK = true;
        Properties p = new Properties();
        FileInputStream in;
        try {
            in = new FileInputStream(propertiesFileName);
            p.load(in);//
            in.close();
            p.setProperty(propertyName, propertyValue);// 设置属性值，如不属性不存在新建
            // p.setProperty("testProperty","testPropertyValue");
            FileOutputStream out = new FileOutputStream(propertiesFileName);// 输出流
            p.store(out, "Just Test");// 设置属性头，如不想设置，请把后面一个用""替换掉
            out.flush();// 清空缓存，写入磁盘
            out.close();// 关闭输出流
        } catch (Exception e) {
            e.printStackTrace();
        }
        return writeOK;
    }


}
