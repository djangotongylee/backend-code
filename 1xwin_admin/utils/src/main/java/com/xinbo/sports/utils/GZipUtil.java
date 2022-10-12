package com.xinbo.sports.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.CharEncoding;
import org.apache.tomcat.util.codec.binary.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @Author william
 * @Date 2020/4/15 20:46
 * @Version 1.0
 **/
@Slf4j
public class GZipUtil {
    /**
     * 压缩GZip
     *
     * @return String
     */
    public static String gZip(String input) {
        byte[] bytes = null;
        GZIPOutputStream gzip = null;
        ByteArrayOutputStream bos = null;
        try {
            bos = new ByteArrayOutputStream();
            gzip = new GZIPOutputStream(bos);
            gzip.write(input.getBytes(CharEncoding.UTF_8));
            gzip.finish();
            gzip.close();
            bytes = bos.toByteArray();
            bos.close();
        } catch (Exception e) {
            log.error("压缩出错：", e);
        } finally {
            try {
                if (gzip != null)
                    gzip.close();
                if (bos != null)
                    bos.close();
            } catch (final IOException ioe) {
                log.error("压缩出错：", ioe);
            }
        }
        return Base64.encodeBase64String(bytes);
    }

    /**
     * 解压GZip
     *
     * @return String
     */
    public static String unGZip(String input) {
        byte[] bytes;
        String out = input;
        GZIPInputStream gzip = null;
        ByteArrayInputStream bis;
        ByteArrayOutputStream bos = null;
        try {
            bis = new ByteArrayInputStream(Base64.decodeBase64(input));
            gzip = new GZIPInputStream(bis);
            byte[] buf = new byte[1024];
            int num;
            bos = new ByteArrayOutputStream();
            while ((num = gzip.read(buf, 0, buf.length)) != -1) {
                bos.write(buf, 0, num);
            }
            bytes = bos.toByteArray();
            out = new String(bytes, CharEncoding.UTF_8);
            gzip.close();
            bis.close();
            bos.flush();
            bos.close();
        } catch (Exception e) {
            log.error("解压出错：", e);
        } finally {
            try {
                if (gzip != null)
                    gzip.close();
                if (bos != null)
                    bos.close();
            } catch (final IOException ioe) {
                log.error("解压出错：", ioe);
            }
        }
        return out;
    }

    public static void main(String[] args) {
        String text = "qw123123";
        String s = gZip(text);
        log.info(s);
        String s1 = unGZip(s);
        log.info(s1);
    }
}
