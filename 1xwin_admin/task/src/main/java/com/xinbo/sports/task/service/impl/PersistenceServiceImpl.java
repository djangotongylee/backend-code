package com.xinbo.sports.task.service.impl;

import com.xinbo.sports.dao.generator.po.CoinCommission;
import com.xinbo.sports.dao.generator.service.CoinCommissionService;
import com.xinbo.sports.dao.generator.service.CoinLogService;
import com.xinbo.sports.dao.generator.service.UserService;
import com.xinbo.sports.service.base.UpdateUserCoinBase;
import com.xinbo.sports.service.io.dto.UpdateUserCoinParams;
import com.xinbo.sports.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author: wells
 * @date: 2020/7/11
 * @description:
 */
@Component
public class PersistenceServiceImpl {
    @Autowired
    private CoinCommissionService coinCommissionServiceImpl;
    @Autowired
    private UserService userServiceImpl;
    @Autowired
    CoinLogService coinLogServiceImpl;
    @Autowired
    private UpdateUserCoinBase updateUserCoinBase;


    /**
     * 代理佣金模块持久化
     */
    @Transactional(rollbackFor = Exception.class)
    public void agentCommissionPersistence(List<CoinCommission> commList) {
        //佣金记录入库处理
        coinCommissionServiceImpl.saveBatch(commList);
        var now = DateUtils.getCurrentTime();
        commList.forEach(coinCommission ->
                //修改用户金额
                updateUserCoinBase.updateUserCoinSaveLog(UpdateUserCoinParams.UpdateUserCoinSaveLogDto.builder()
                        .uid(coinCommission.getUid())
                        .coin(coinCommission.getCoin())
                        .referId(coinCommission.getId())
                        .category(6)
                        .subCategory(coinCommission.getCategory())
                        .status(1)
                        .now(now)
                        .build())
        );
    }


    /**
     * 代理佣金模块持久化
     */
    @Transactional(rollbackFor = Exception.class)
    public void flowCommissionPersistence(List<CoinCommission> commList) {
        //佣金记录入库处理
        coinCommissionServiceImpl.updateBatchById(commList);
        var now = DateUtils.getCurrentTime();
        commList.forEach(coinCommission ->
                //修改用户金额
                updateUserCoinBase.updateUserCoinSaveLog(UpdateUserCoinParams.UpdateUserCoinSaveLogDto.builder()
                        .uid(coinCommission.getUid())
                        .coin(coinCommission.getCoin())
                        .referId(coinCommission.getId())
                        .category(6)
                        .subCategory(coinCommission.getCategory())
                        .status(1)
                        .now(now)
                        .build())
        );
    }

}
