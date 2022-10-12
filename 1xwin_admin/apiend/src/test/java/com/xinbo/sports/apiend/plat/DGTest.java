package com.xinbo.sports.apiend.plat;

import com.xinbo.sports.plat.service.impl.dg.DGLiveServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * <p>
 * WM真人
 * </p>
 *
 * @author andy
 * @since 2020/5/19
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class DGTest {
    @Resource
    private DGLiveServiceImpl dGLiveServiceImpl;

    /**
     * 查询下注记录
     */
    @Test
    public void getBetListByDate() {
        dGLiveServiceImpl.pullBetsLips();
    }

    /**
     * 查询下注记录
     */
    @Test
    public void checkTransferStatus() {
        boolean flag = dGLiveServiceImpl.checkTransferStatus("83289929454587904");
        log.info("result={}", flag);
    }
}
