package com.xinbo.sports.service.base;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.utils.FastDFSClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * <p>
 * FastDFS辅助类
 * 描述:支付文件上传、删除、下载功能
 * </p>
 *
 * @author andy
 * @since 2020/6/2
 */
@Slf4j(topic = "[ FastDFSTemplate ]")
@Component
public class FastDFSTemplate {
    /**
     * 文件大小限制 2M以内
     */
    private static final int MAX_SIZE = 1024 * 1024 * 2;
    @Resource
    private FastDFSClientUtils fastDFSClientUtils;
    @Resource
    private ConfigCache configCache;

    private static final String[] IMAGE_TYPE = new String[]{

            ".bmp", ".jpg",
            ".jpeg", ".gif", ".png"};
    /**
     * 文件上传
     *
     * @param file MultipartFile
     * @return JSONObject
     */
    public JSONObject uploadFile(MultipartFile file) {


        if (file != null) {
            // 校验图片格式
            boolean isLegal = false;
            for (String type : IMAGE_TYPE) {
                if (StringUtils.endsWithIgnoreCase(file.getOriginalFilename(), type)) {
                    isLegal = true;
                    break;
                }
            }
            if (!isLegal) {
                throw new BusinessException("请上传图片格式");
            }
            String returnFileUrl = upload(file);
            if (returnFileUrl.equals("error")) {
                throw new BusinessException("上传失败");
            }

            JSONObject data = new JSONObject();
            data.put("path", returnFileUrl);
            data.put("staticServer", "https://bwg2020.oss-ap-southeast-1.aliyuncs.com/");
            return data;
        } else {
            throw new BusinessException("上传失败");
        }
    }

    private String upload(MultipartFile uploadFile){
        // 获取oss的Bucket名称
        String bucketName = "bwg2020";
        // 获取oss的地域节点
        String endpoint = "oss-ap-southeast-1.aliyuncs.com";
        // 获取oss的AccessKeySecret
        String accessKeySecret = "RmFuLnj0Voc8lxuHdCkOaTqOjNQV66";
        // 获取oss的AccessKeyId
        String accessKeyId = "LTAI5tRVNV6gpNAWhantL66D";
        // 获取oss目标文件夹
        String filehost = "files";
        // 返回图片上传后返回的url
        String returnImgeUrl = "";


        // 获取文件原名称
        String originalFilename = uploadFile.getOriginalFilename();
        // 获取文件类型
        String fileType = originalFilename.substring(originalFilename.lastIndexOf("."));
        // 新文件名称
        String newFileName = UUID.randomUUID().toString() + fileType;
        // 构建日期路径, 例如：OSS目标文件夹/2020/10/31/文件名
        String filePath = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
        // 文件上传的路径地址
        String uploadImgeUrl = filehost + "/" + filePath + "/" + newFileName;
        // 获取文件输入流
        InputStream inputStream = null;
        try {
            inputStream = uploadFile.getInputStream();
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new BusinessException("上传失败");
        }
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentType("image/jpg");
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        //文件上传至阿里云OSS
        ossClient.putObject(bucketName, uploadImgeUrl, inputStream, meta);
        /**
         * 注意：在实际项目中，文件上传成功后，数据库中存储文件地址
         */
        // 获取文件上传后的图片返回地址
        returnImgeUrl = uploadImgeUrl;

        return returnImgeUrl;
    }

    /**
     * 文件删除
     *
     * @param fullPath fullPath
     */
    public void deleteFile(String fullPath) {
        String staticServer = configCache.getStaticServer();
        if (StringUtils.isNotBlank(fullPath) && fullPath.indexOf(staticServer) != -1) {
            fullPath = fullPath.substring(staticServer.length());
        }
        fastDFSClientUtils.deleteFile(fullPath);
        log.info(formatInfo(), "文件删除", staticServer, fullPath, "文件删除");
    }

    /**
     * LOG -> INFO 输出格式
     *
     * @return 正常格式
     */
    private static String formatInfo() {
        return "\n===================\t[ {} 成功 ]\t========================================================="
                + "\nRootPath==========>\t{}"
                + "\nSubPath===========>\t{}"
                + "\n===================\t[ {} 成功 ]\t=========================================================\n";
    }

    /**
     * LOG -> ERROR 输出格式
     *
     * @return 异常格式
     */
    private static String formatError() {
        return "\n===================\t[ {} 失败 ]\t========================================================="
                + "\nRootPath==========>\t{}"
                + "\nSubPath===========>\t{}"
                + "\nException=========>\t{}"
                + "\n===================\t[ {} 失败 ]\t=========================================================\n";
    }
}
