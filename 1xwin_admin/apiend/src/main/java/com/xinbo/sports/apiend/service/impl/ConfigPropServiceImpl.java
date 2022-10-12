package com.xinbo.sports.apiend.service.impl;

import com.xinbo.sports.apiend.service.IConfigPropService;
import com.xinbo.sports.dao.generator.po.Config;
import com.xinbo.sports.dao.generator.service.ConfigService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author: David
 * @date: 06/04/2020
 * @description:
 */
@Service
public class ConfigPropServiceImpl implements IConfigPropService {
    @Resource
    ConfigService configServiceImpl;

    @Override
    public String getStaticServer() {
        Config config = configServiceImpl.lambdaQuery()
                .eq(Config::getTitle, "static_server")
                .one();

        return config == null ? "" : config.getValue();
    }

    @Override
    public String getOnlineServer() {
        Config config = configServiceImpl.lambdaQuery()
                .eq(Config::getTitle, "online_server")
                .one();

        return config == null ? "" : config.getValue();
    }

    @Override
    public String getDownload() {
        Config config = configServiceImpl.lambdaQuery()
                .eq(Config::getTitle, "download")
                .one();

        return config == null ? "" : config.getValue();
    }
}
