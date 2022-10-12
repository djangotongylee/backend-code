package com.xinbo.sports.service.configuration;

import com.github.tobato.fastdfs.FdfsClientConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.context.annotation.Import;
import org.springframework.jmx.support.RegistrationPolicy;

/**
 * <p>
 * 分布式文件系统/FastDFS -> 配置类
 * </p>
 *
 * @author andy
 * @since 2020/9/17
 */
@Configuration
// 导入FastDFS-Client组件
@Import(FdfsClientConfig.class)
// 解决jmx重复注册bean的问题
@EnableMBeanExport(registration = RegistrationPolicy.IGNORE_EXISTING)
public class FastDFSConfig {
}
