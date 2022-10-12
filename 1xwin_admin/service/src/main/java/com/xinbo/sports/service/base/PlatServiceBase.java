package com.xinbo.sports.service.base;

import com.xinbo.sports.dao.generator.po.CoinPlatTransfer;
import com.xinbo.sports.dao.generator.po.User;
import com.xinbo.sports.dao.generator.service.CoinPlatTransferService;
import com.xinbo.sports.dao.generator.service.UserService;
import com.xinbo.sports.service.common.Constant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.bo.PlatServiceBaseParams.InsertCoinPlatRecordsReqDto;
import com.xinbo.sports.service.io.bo.PlatServiceBaseParams.InsertCoinPlatRecordsResDto;
import com.xinbo.sports.service.io.dto.UpdateUserCoinParams;
import com.xinbo.sports.utils.BeanConvertUtils;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.DateUtils;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: David
 * @date: 15/05/2020
 * @description:
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PlatServiceBase {
    private final UserService userServiceImpl;
    private final CoinPlatTransferService coinPlatTransferServiceImpl;
    private final CoinPlatTransfersBase coinPlatTransfersBase;
    private final UpdateUserCoinBase updateUserCoinBase;

    /**
     * 三方平台转入转出新增记录
     *
     * @param dto 入参
     * @return 出参
     */
    @Transactional(rollbackFor = Exception.class)
    public InsertCoinPlatRecordsResDto insertCoinPlatRecords(InsertCoinPlatRecordsReqDto dto) {
            // 添加转账记录
            CoinPlatTransfer transfer = BeanConvertUtils.beanCopy(dto, CoinPlatTransfer::new);
            // 获取用户信息
            User user = userServiceImpl.getById(transfer.getUid());
            // 插入上下分记录
            Integer time = DateUtils.getCurrentTime();
            transfer.setCoinBefore(user.getCoin());
            transfer.setCategory(dto.getDirection());
            transfer.setCreatedAt(time);
            transfer.setUpdatedAt(time);
            transfer.setPlatListId(dto.getPlatId());
            transfer.setUsername(user.getUsername());
            coinPlatTransferServiceImpl.save(transfer);

            Integer direction = dto.getDirection();
            if (direction != null && direction.equals(0)) {
                // 上分  需要先减去用户余额
                updateUserCoinBase.updateUserCoinSaveLog(UpdateUserCoinParams.UpdateUserCoinSaveLogDto.builder()
                        .category(Constant.API_COIN_LOG_CATEGORY_SF)
                        .uid(transfer.getUid())
                        .referId(transfer.getId())
                        .coin(transfer.getCoin().negate())
                        .status(0)
                        .outIn(0)
                        .now(time)
                        .build());
            }

            return InsertCoinPlatRecordsResDto.builder()
                    .orderId(transfer.getId())
                    .coin(transfer.getCoin())
                    .build();

    }

    /**
     * 根据平台ID和status获取列表
     *
     * @param platListId 平台ID
     * @param status     状态:0-提交申请 1-成功 2-失败
     * @return List
     */
    public List<CoinPlatTransfer> getCoinPlatTransferList(Integer platListId, Integer status) {
        // -5分钟
        int now = DateNewUtils.now() - 60 * 5;
        return coinPlatTransferServiceImpl.lambdaQuery()
                .eq(CoinPlatTransfer::getPlatListId, platListId)
                .eq(CoinPlatTransfer::getStatus, status)
                .lt(CoinPlatTransfer::getUpdatedAt, now)
                .list();
    }

    /**
     * 调用第三方返回结果后,更新CoinPlatTransfer表的status或user表的coin字典
     * 逻辑:上方失败或下方成功:增加用户余额
     *
     * @param po               待更新实体
     * @param callResultStatus 返回结果
     * @return 更新结果
     */
    public boolean updateCoinPlatTransferStatusById(CoinPlatTransfer po, boolean callResultStatus) {
        if (callResultStatus) {
            return coinPlatTransfersBase.successUpdateTransferOrder(po.getId() + "", po.getCoin(), po.getOrderPlat());
        }
        return coinPlatTransfersBase.failureUpdateTransferOrder(po.getId() + "", "");
    }

}
