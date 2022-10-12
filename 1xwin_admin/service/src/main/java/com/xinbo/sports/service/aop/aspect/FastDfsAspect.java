package com.xinbo.sports.service.aop.aspect;

import com.github.tobato.fastdfs.domain.fdfs.StorageNode;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * <p>
 * FastDFSAOP:支持上传文件至指定目录
 * 描述:group1/M03/00/04/wKgBvl9oazKARV64AABLaVkgPjo659.png
 * M03==>变量
 *
 * </p>
 *
 * @author andy
 * @since 2020/9/21
 */
@Slf4j
@Aspect
@Component
public class FastDfsAspect {
    @Resource
    private ConfigCache configCache;

    @Around("execution(* com.github.tobato.fastdfs.service.DefaultTrackerClient.getStoreStorage(..))")
    public Object aroundMethod(ProceedingJoinPoint pjd) {
        String storageNodeBefore = null;
        String storageNodeIndex = null;
        String storageNodeAfter = null;


        StorageNode result = null;
        try {
            result = (StorageNode) pjd.proceed();
            Integer fastDfsStoreIndex = configCache.getFastDfsStoreIndex();

            storageNodeBefore = result.toString();
            storageNodeIndex = fastDfsStoreIndex + "";
            if (null != fastDfsStoreIndex) {
                result.setStoreIndex((byte) fastDfsStoreIndex.intValue());
            }
            storageNodeAfter = result.toString();
            log.info(formatLog(), storageNodeBefore, storageNodeIndex, storageNodeAfter, null);
        } catch (Throwable e) {
            log.error(formatLog(), storageNodeBefore, storageNodeIndex, storageNodeAfter, e.getMessage(),e);
        }
        return result;
    }

    /**
     * LOG -> 输出格式
     *
     * @return 输出格式
     */
    private static String formatLog() {
        return "\n===================\t[ FastDFS->AOP ]\t========================================================="
                + "\nStorageNode Before==========>\t{}"
                + "\nStorageNode Index===========>\t{}"
                + "\nStorageNode After===========>\t{}"
                + "\nStorageNode Exception=======>\t{}"
                + "\n===================\t[ FastDFS->AOP ]\t=========================================================";
    }
}
