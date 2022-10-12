package com.xinbo.sports.service.base;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xinbo.sports.dao.generator.po.CoinLog;
import com.xinbo.sports.dao.generator.po.User;
import com.xinbo.sports.dao.generator.service.CoinLogService;
import com.xinbo.sports.dao.generator.service.UserService;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.dto.UpdateUserCoinParams.UpdateUserCoinSaveLogDto;
import com.xinbo.sports.service.io.dto.UpdateUserCoinParams.UpdateUserCoinUpdateLogByReferId;
import com.xinbo.sports.utils.BeanConvertUtils;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * @Author : Wells
 * @Date : 2020-11-14 4:51 下午
 * @Description : 修改用户金额
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UpdateUserCoinBase {
    private final UserService userServiceImpl;
    private final CoinLogService coinLogServiceImpl;

    /**
     * 修改用户金额并新增日志(此方法必须在事务中执行)
     */
    public boolean updateUserCoinSaveLog(UpdateUserCoinSaveLogDto reqDto) {
        int outIn = Optional.ofNullable(reqDto.outIn).orElse(0);
        var subCategory = Optional.ofNullable(reqDto.subCategory).orElse(0);
        var user = userServiceImpl.getById(reqDto.getUid());
        var oldCoin = user.getCoin();
        var now = reqDto.getNow();
        //修改用户金额
        updateConcurrentUserCoin(reqDto, now, oldCoin);
        //日志入库
        CoinLog coinLog = new CoinLog();
        coinLog.setUid(user.getId());
        coinLog.setUsername(user.getUsername());
        coinLog.setReferId(reqDto.getReferId());
        coinLog.setSubCategory(subCategory);
        coinLog.setOutIn(outIn);
        //日志类型
        coinLog.setCategory(reqDto.getCategory());
        coinLog.setCoin(reqDto.getCoin().abs());
        coinLog.setCoinBefore(user.getCoin());
        coinLog.setStatus(reqDto.getStatus());
        coinLog.setCreatedAt(now);
        coinLog.setUpdatedAt(now);
        return coinLogServiceImpl.save(coinLog);
    }


    /**
     * 修改用户金额并修改日志(此方法必须在事务中执行)
     */
    public boolean updateUserCoinUpdateLog(UpdateUserCoinUpdateLogByReferId reqDto) {
        var user = userServiceImpl.getById(reqDto.getUid());
        var oldCoin = user.getCoin();
        var now = reqDto.getNow();
        var updateUserDto = BeanConvertUtils.copyProperties(reqDto, UpdateUserCoinSaveLogDto::new);
        updateConcurrentUserCoin(updateUserDto, now, oldCoin);
        return updateCoinLog(reqDto.getReferId(), reqDto.getCategory(), reqDto.getStatus(), now);
    }


    /**
     * 创建修改日志实例
     *
     * @param referId  对应的业务记录ID
     * @param category 日志类型
     * @param status   状态
     * @param now      当前日志
     */
    public boolean updateCoinLog(Long referId, Integer category, Integer status, Integer now) {
        var updateFlag = coinLogServiceImpl.lambdaUpdate()
                .set(CoinLog::getStatus, status)
                .set(CoinLog::getUpdatedAt, now)
                .eq(CoinLog::getReferId, referId)
                .eq(CoinLog::getCategory, category)
                .eq(CoinLog::getStatus, 0)
                .update();
        if (!updateFlag) {
            throw new BusinessException(CodeInfo.UPDATE_ERROR);
        }
        return true;
    }


    /**
     * 修改用户金额
     *
     * @param reqDto 用户参数
     * @param now    当前时间
     */
    private void updateConcurrentUserCoin(UpdateUserCoinSaveLogDto reqDto, Integer now, BigDecimal oldCoin) {
        var updateUserWrapper = new LambdaUpdateWrapper<User>();
        updateUserWrapper.setSql("coin = coin +" + reqDto.getCoin())
                .set(User::getUpdatedAt, now)
                .eq(User::getId, reqDto.getUid())
                .eq(User::getCoin, oldCoin);
        Optional.ofNullable(reqDto.getFcoin()).ifPresent(fcoin ->
                updateUserWrapper.setSql("fcoin = fcoin + " + fcoin)
        );
        userServiceImpl.update(updateUserWrapper);
    }

}
