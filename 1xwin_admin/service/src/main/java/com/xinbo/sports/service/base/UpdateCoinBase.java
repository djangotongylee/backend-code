//package com.xinbo.sports.service.base;
//
//import com.xinbo.sports.dao.generator.po.CoinLog;
//import com.xinbo.sports.dao.generator.po.User;
//import com.xinbo.sports.dao.generator.service.CoinLogService;
//import com.xinbo.sports.dao.generator.service.UserService;
//import com.xinbo.sports.service.common.Constant;
//import com.xinbo.sports.service.exception.BusinessException;
//import com.xinbo.sports.service.io.bo.PlatServiceBaseParams.UpdateCoinReqDto;
//import com.xinbo.sports.utils.BeanConvertUtils;
//import com.xinbo.sports.utils.DateNewUtils;
//import com.xinbo.sports.utils.components.response.CodeInfo;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.math.BigDecimal;
//
///**
// * @author: David
// * @date: 10/05/2020
// * @description:
// */
//@Slf4j
//@Service
//@RequiredArgsConstructor(onConstructor = @__(@Autowired))
//public class UpdateCoinBase {
//    private final UserService userServiceImpl;
//    private final CoinLogService coinLogServiceImpl;
//    private final UpdateUserCoinQueue updateUserCoinQueue;
//
//    /**
//     * 用户金额更新(必须放在 【 事务 】 中执行)
//     *
//     * @param dto dto
//     */
//    public void updateCoin(UpdateCoinReqDto dto) {
//        User user = userServiceImpl.getById(dto.getUid());
//        if (null == user) {
//            throw new BusinessException(CodeInfo.USER_NOT_EXISTS);
//        }
//        CoinLog coinLog = BeanConvertUtils.beanCopy(dto, CoinLog::new);
//        BigDecimal coinBefore = user.getCoin();
//        BigDecimal coinOperate = coinLog.getCoin();
//        // 插入CoinLog
//        int time = DateNewUtils.now();
//        coinLog.setCreatedAt(time);
//        coinLog.setUpdatedAt(time);
//        coinLog.setCoinBefore(coinBefore);
//        coinLog.setUsername(user.getUsername());
//        CoinLog one = coinLogServiceImpl.lambdaQuery()
//                .eq(CoinLog::getCategory, dto.getCategory())
//                .eq(CoinLog::getReferId, dto.getReferId())
//                .one();
//        if (one == null) {
//            coinLogServiceImpl.save(coinLog);
//        } else {
//            coinLog.setId(one.getId());
//            coinLogServiceImpl.updateById(coinLog);
//        }
//        // 更新用户表
//        BigDecimal resultCoin = coinLog.getOutIn() == 1 ? coinBefore.add(coinOperate) : coinBefore.subtract(coinOperate);
//        if (dto.getStatus() == 2) {
//            resultCoin = coinLog.getOutIn() == 0 ? coinBefore.add(coinOperate) : coinBefore.subtract(coinOperate);
//        }
//        // 下方:统一队列处理
//        if (Constant.API_COIN_LOG_CATEGORY_XF == coinLog.getCategory()) {
//            updateUserCoinQueue.put(coinLog);
//        } else {
//            updateUser(resultCoin, DateNewUtils.now(), user.getId(), user.getUpdatedAt());
//        }
//    }
//
//    /**
//     * 更新用户余额
//     *
//     * @param coin      待更金额
//     * @param updatedAt 待更新时间
//     * @param uid       UID
//     * @param version   版本号
//     * @return 更新结果
//     */
//    private boolean updateUser(BigDecimal coin, Integer updatedAt, Integer uid, Integer version) {
//        return userServiceImpl.lambdaUpdate()
//                .set(User::getCoin, coin)
//                .set(User::getUpdatedAt, updatedAt)
//                .eq(User::getId, uid)
//                .eq(User::getUpdatedAt, version)
//                .update();
//    }
//}
