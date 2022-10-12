package com.xinbo.sports.payment.base;

import com.alibaba.fastjson.JSON;
import com.xinbo.sports.dao.generator.po.CodeRecords;
import com.xinbo.sports.dao.generator.po.CoinDeposit;
import com.xinbo.sports.dao.generator.po.PayPlat;
import com.xinbo.sports.dao.generator.service.CodeRecordsService;
import com.xinbo.sports.dao.generator.service.CoinDepositService;
import com.xinbo.sports.dao.generator.service.PayPlatService;
import com.xinbo.sports.service.base.DepositBase;
import com.xinbo.sports.service.base.UpdateUserCoinBase;
import com.xinbo.sports.service.cache.KeyConstant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.dto.UpdateUserCoinParams;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.JedisUtil;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: David
 * @date: 06/06/2020
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ThirdPayBase {
    private final PayPlatService payPlatServiceImpl;
    private final CoinDepositService coinDepositServiceImpl;
    private final UpdateUserCoinBase updateUserCoinBase;
    private final ReentrantLock reentrantLock = new ReentrantLock();
    private final CodeRecordsService codeRecordsServiceImpl;
    private final DepositBase depositBase;
    private final JedisUtil jedisUtil;

    /**
     * 更新Coin
     *
     * @param coinDepositId 存款ID
     * @param realCoin      实际支付金额
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateCoinDeposit(Long coinDepositId, BigDecimal realCoin, String... depMark) {
        boolean b = reentrantLock.tryLock();
        try {
            if (!b) {
                throw new BusinessException(CodeInfo.PLAT_REQUEST_FREQUENT);
            }

            CoinDeposit coinDeposit = coinDepositServiceImpl.getById(coinDepositId);
            var depStatus = depositBase.firstOrSecondDeposit(coinDeposit.getUid(), realCoin);
            int time = DateNewUtils.now();
            log.info("支付更新：～～～～～～～～～～" + realCoin + time + depStatus);
            // 更新CoinDeposit状态 2-自动到账
            coinDeposit.setPayCoin(realCoin);
            coinDeposit.setStatus(2);
            coinDeposit.setUpdatedAt(time);
            coinDeposit.setDepStatus(depStatus);
            //审核通过
            coinDeposit.setAuditStatus(3);
            if (!ArrayUtils.isEmpty(depMark)) {
                coinDeposit.setDepMark(depMark[0]);
            }
            log.info("更新参数：" + coinDeposit.toString());
            coinDepositServiceImpl.updateById(coinDeposit);

            // 插入打码量数据
            CodeRecords codeRecords = new CodeRecords();
            codeRecords.setUid(coinDeposit.getUid());
            codeRecords.setCoin(coinDeposit.getPayCoin());
            codeRecords.setCodeRequire(coinDeposit.getPayCoin());
            codeRecords.setCategory(1);
            codeRecords.setReferId(coinDeposit.getId());
            codeRecords.setUpdatedAt(time);
            codeRecords.setCreatedAt(time);
            codeRecordsServiceImpl.save(codeRecords);

            // 更新用户余额
            updateUserCoinBase.updateUserCoinSaveLog(UpdateUserCoinParams.UpdateUserCoinSaveLogDto.builder()
                    .category(1).coin(realCoin).outIn(1).referId(coinDepositId)
                    .uid(coinDeposit.getUid()).status(1).now(time)
                    .build());
        } catch (Exception e) {
            log.info(e.toString());
            throw e;
        } finally {
            if (b) {
                reentrantLock.unlock();
            }
        }
    }

    /**
     * 根据code获取当前支付平台
     *
     * @param model code
     * @return 平台相关信息
     */
    public <T> T getPaymentConfig(String model, Class<T> forName) {
        String payConfig = jedisUtil.hget(KeyConstant.PAY_PLATFORM_HASH, model);
        if (payConfig == null) {
            PayPlat payPlat = payPlatServiceImpl.lambdaQuery().eq(PayPlat::getCode, model).one();
            if (payPlat == null) throw new BusinessException(CodeInfo.PAY_PLAT_INVALID);
            payConfig = JSON.toJSONString(payPlat);
            jedisUtil.hset(KeyConstant.PAY_PLATFORM_HASH, model, payConfig);
        }
        return JSON.parseObject(payConfig).toJavaObject(forName);
    }
}
