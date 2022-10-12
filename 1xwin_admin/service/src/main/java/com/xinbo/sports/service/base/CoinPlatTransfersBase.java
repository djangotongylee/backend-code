package com.xinbo.sports.service.base;

import com.xinbo.sports.dao.generator.po.CoinPlatTransfer;
import com.xinbo.sports.dao.generator.service.CoinPlatTransferService;
import com.xinbo.sports.service.common.Constant;
import com.xinbo.sports.service.io.dto.UpdateUserCoinParams;
import com.xinbo.sports.utils.BeanConvertUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

import static com.xinbo.sports.service.common.Constant.API_COIN_LOG_CATEGORY_SF;

/**
 * @author: David
 * @date: 03/05/2020
 * @description: 第三方游戏平台上下分通用表
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CoinPlatTransfersBase {
    private final CoinPlatTransferService coinPlatTransferServiceImpl;
    private final UpdateUserCoinBase updateUserCoinBase;

    /**
     * 更新上下分记录表 上分、下分成功/失败后 调用
     *
     * @param orderId   sp_coin_plat_transfer主键ID
     * @param status    状态:0-提交申请 1-成功 2-失败
     * @param amount    金额
     * @param orderPlat 平台订单号
     * @param mark      备注:status=2时赋值
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderPlat(String orderId, Integer status, BigDecimal amount, String orderPlat, String mark) {
        Integer time = (int) Instant.now().getEpochSecond();

        CoinPlatTransfer transfer = coinPlatTransferServiceImpl.lambdaQuery()
                .eq(CoinPlatTransfer::getId, orderId)
                .one();

        // 下分因为包含实际下分金额 所以需要判定
        if (transfer.getCategory() == 1) {
            transfer.setCoin(amount);
        }
        if (StringUtils.isNotBlank(orderId)) {
            transfer.setOrderPlat(orderPlat);
        }
        transfer.setUpdatedAt(time);
        // 状态:0-提交申请 1-成功 2-失败
        transfer.setStatus(status);
        transfer.setMark(mark);
        coinPlatTransferServiceImpl.updateById(transfer);

        // 更新账变明细记录依赖实体
        var build = UpdateUserCoinParams.UpdateUserCoinUpdateLogByReferId.builder()
                .uid(transfer.getUid())
                .referId(transfer.getId())
                .now(time)
                .build();

        // 上方失败:需要回退用户余额(用户余额增加)
        if (status == 2 && transfer.getCategory().equals(0)) {
            build.setCategory(API_COIN_LOG_CATEGORY_SF);
            build.setCoin(transfer.getCoin());
            build.setStatus(2);
            updateUserCoinBase.updateUserCoinUpdateLog(build);
        } else if (status == 1 && amount.compareTo(BigDecimal.ZERO) != 0 && transfer.getCategory().equals(1)) {
            // 下分成功 添加下分金额
            build.setCategory(Constant.API_COIN_LOG_CATEGORY_XF);
            build.setCoin(amount);
            build.setStatus(1);

            // 新增账变明细记录依赖实体
            var saveBuild = BeanConvertUtils.beanCopy(build, UpdateUserCoinParams.UpdateUserCoinSaveLogDto::new);
            // 收支类型:0-支出 1-收入
            saveBuild.setOutIn(1);
            updateUserCoinBase.updateUserCoinSaveLog(saveBuild);
        } else if (status == 1 && amount.compareTo(BigDecimal.ZERO) != 0 && transfer.getCategory().equals(0)) {
            // 上分成功:更新sp_coin_log表的status=1
            updateUserCoinBase.updateCoinLog(transfer.getId(), API_COIN_LOG_CATEGORY_SF, 1, time);
        }
    }

    /**
     * 成功后 更新订单表
     *
     * @param orderId     sp_coin_plat_transfer主键ID
     * @param realAmount  实际金额
     * @param orderPlatId 三方平台账号
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean successUpdateTransferOrder(String orderId, BigDecimal realAmount, String orderPlatId) {
        Integer time = (int) Instant.now().getEpochSecond();

        CoinPlatTransfer transfer = coinPlatTransferServiceImpl.lambdaQuery()
                .eq(CoinPlatTransfer::getId, orderId)
                .one();
        // 更新状态
        boolean update = coinPlatTransferServiceImpl.lambdaUpdate()
                .set(CoinPlatTransfer::getCoin, realAmount)
                .set(CoinPlatTransfer::getOrderPlat, orderPlatId)
                .set(CoinPlatTransfer::getUpdatedAt, time)
                .set(CoinPlatTransfer::getStatus, 1)
                .set(CoinPlatTransfer::getMark, "Success")
                .eq(CoinPlatTransfer::getId, transfer.getId())
                .eq(CoinPlatTransfer::getStatus, 0)

                .update();
        if (Boolean.FALSE.equals(update)) {
            return false;
        }

        if (transfer.getCategory().equals(1)) {
            // 下分成功 添加下分金额
            var build = UpdateUserCoinParams.UpdateUserCoinUpdateLogByReferId.builder()
                    .uid(transfer.getUid())
                    .referId(transfer.getId())
                    .category(Constant.API_COIN_LOG_CATEGORY_XF)
                    .coin(realAmount)
                    .status(1)
                    .now(time)
                    .build();
            var saveBuild = BeanConvertUtils.beanCopy(build, UpdateUserCoinParams.UpdateUserCoinSaveLogDto::new);
            // 收支类型:0-支出 1-收入
            saveBuild.setOutIn(1);
            updateUserCoinBase.updateUserCoinSaveLog(saveBuild);
        } else {
            // 上分成功: 更新sp_coin_log表status
            updateUserCoinBase.updateCoinLog(transfer.getId(), API_COIN_LOG_CATEGORY_SF, 1, time);
        }

        return true;
    }

    /**
     * 失败后 更新订单信息
     *
     * @param orderId sp_coin_plat_transfer主键ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean failureUpdateTransferOrder(String orderId, String failInfo) {
        // 系统当前时间戳
        Integer time = (int) Instant.now().getEpochSecond();

        // 根据主键ID查询平台上下分记录
        CoinPlatTransfer transfer = coinPlatTransferServiceImpl.lambdaQuery()
                .eq(CoinPlatTransfer::getId, orderId)
                .one();

        // 更新状态(平台上下分记录表)
        boolean update = coinPlatTransferServiceImpl.lambdaUpdate()
                .set(CoinPlatTransfer::getStatus, 2)
                .set(CoinPlatTransfer::getMark, failInfo)
                .set(CoinPlatTransfer::getUpdatedAt, time)
                .set(CoinPlatTransfer::getChecked, 1)
                .eq(CoinPlatTransfer::getStatus, 0)
                .eq(CoinPlatTransfer::getChecked, 0)
                .eq(CoinPlatTransfer::getId, transfer.getId())
                .update();
        if (Boolean.FALSE.equals(update)) {
            return false;
        }

        // 上分失败 需要回退用户余额(用户余额增加)
        if (transfer.getCategory().equals(0)) {
            var build = UpdateUserCoinParams.UpdateUserCoinUpdateLogByReferId.builder()
                    .uid(transfer.getUid())
                    .category(API_COIN_LOG_CATEGORY_SF)
                    .coin(transfer.getCoin())
                    .status(2)
                    .referId(transfer.getId())
                    .now(time)
                    .build();
            updateUserCoinBase.updateUserCoinUpdateLog(build);
        }

        return true;
    }
}
