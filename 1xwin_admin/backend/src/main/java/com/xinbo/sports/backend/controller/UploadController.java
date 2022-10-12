package com.xinbo.sports.backend.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.xinbo.sports.backend.aop.annotation.UnCheckLog;
import com.xinbo.sports.service.base.FastDFSTemplate;
import com.xinbo.sports.utils.components.response.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * <p>
 * 文件上传下载控制类
 * </p>
 *
 * @author andy
 * @since 2020/6/1
 */
@Slf4j
@RestController
@RequestMapping("/v1/upload")
@Api(tags = "文件上传")
public class UploadController {
    @Resource
    private FastDFSTemplate fastDFSTemplate;

    @UnCheckLog
    @ApiOperationSupport(author = "Andy", order = 1)
    @PostMapping("/uploadFile")
    @ApiOperation(value = "上传文件", notes = "上传文件")
    public Result<JSONObject> uploadFile(@RequestParam("file") MultipartFile file) {
        return Result.ok(fastDFSTemplate.uploadFile(file));
    }
}
