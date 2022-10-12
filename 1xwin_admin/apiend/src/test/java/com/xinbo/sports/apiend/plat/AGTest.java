package com.xinbo.sports.apiend.plat;

import com.xinbo.sports.plat.service.impl.ag.AGLiveServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * <p>
 * AG真人
 * </p>
 *
 * @author andy
 * @since 2020/5/19
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class AGTest {
    @Resource
    private AGLiveServiceImpl aGLiveServiceImpl;

    /**
     * 查询下注记录
     */
    @Test
    public void getBetListByDate() {
        aGLiveServiceImpl.pullBetsLips();
    }
}
