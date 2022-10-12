package com.xinbo.sports.service.base;

import com.xinbo.sports.service.io.dto.UpdateUserCoinParams.UpdateUserCoinSaveLogDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * <p>
 * 下分成功->处理队列
 * </p>
 *
 * @author andy
 * @since 2020/12/13
 */
@Component
@Slf4j
public class CoinDownQueue {
    private static final String TOPIC_MSG = "[下分成功->处理队列]";
    @Resource
    private UpdateUserCoinBase updateUserCoinBase;
    private BlockingQueue<UpdateUserCoinSaveLogDto> queue = new LinkedBlockingQueue<>();

    /**
     * 生产
     *
     * @param coinLog CoinLog
     */
    public void putCoinDown(UpdateUserCoinSaveLogDto coinLog) {
        try {
            queue.put(coinLog);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(" ====={}->入队===>异常:{}", TOPIC_MSG, e.getMessage(), e);
        }

    }

    /**
     * 消费
     *
     * @return CoinLog
     */
    public UpdateUserCoinSaveLogDto takeCoinDown() {
        UpdateUserCoinSaveLogDto coinLog = null;
        try {
            coinLog = queue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(" ====={}->出队===>异常:{}", TOPIC_MSG, e.getMessage(), e);
        }
        return coinLog;
    }

    @PostConstruct
    public void consume() {
        log.info("============ {}-->已监听 ============", TOPIC_MSG);
        ExecutorService pool = Executors.newCachedThreadPool();
        pool.submit(() -> {
            while (true) {
                UpdateUserCoinSaveLogDto coinLog = takeCoinDown();
                updateUserCoinBase.updateUserCoinSaveLog(coinLog);
                log.info("===={}-->已处理[referId={}],待处理队列大小:{}", TOPIC_MSG, coinLog.getReferId(), queue.size());
            }
        });
    }

}
