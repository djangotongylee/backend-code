package com.xinbo.sports.apiend.service.impl;

import com.alibaba.fastjson.JSON;
import com.xinbo.sports.apiend.io.dto.wallet.WithdrawalAddReqBody;
import com.xinbo.sports.apiend.io.dto.wallet.WithdrawalHint;
import com.xinbo.sports.apiend.service.IUserInfoService;
import com.xinbo.sports.apiend.service.IWithdrawalService;
import com.xinbo.sports.dao.generator.po.CoinWithdrawal;
import com.xinbo.sports.dao.generator.po.UserBank;
import com.xinbo.sports.dao.generator.po.UserLevel;
import com.xinbo.sports.dao.generator.service.CoinWithdrawalService;
import com.xinbo.sports.dao.generator.service.UserBankService;
import com.xinbo.sports.service.base.AuditBase;
import com.xinbo.sports.service.base.UpdateUserCoinBase;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.service.cache.redis.UserCache;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.dto.UpdateUserCoinParams;
import com.xinbo.sports.service.io.dto.UserInfo;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.I18nUtils;
import com.xinbo.sports.utils.PasswordUtils;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import static com.alibaba.fastjson.JSON.parseObject;

/**
 * <p>
 * 提现业务处理接口实现类
 * </p>
 *
 * @author andy
 * @since 2020/4/20
 */
@Slf4j(topic = "用户提现Service")
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WithdrawalServiceImpl implements IWithdrawalService {
    private final IUserInfoService userInfoServiceImpl;
    private final CoinWithdrawalService coinWithdrawalServiceImpl;
    private final UserBankService userBankServiceImpl;
    private final UserCache userCache;
    private final UpdateUserCoinBase updateUserCoinBase;
    private final ReentrantLock reentrantLock = new ReentrantLock();
    private final AuditBase auditBase;
    private final ConfigCache configCache;

    /**
     * 添加提现记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addWithdrawal(@Valid WithdrawalAddReqBody reqBody) {
        boolean b = reentrantLock.tryLock();
        try {
            if (!b) {
                throw new BusinessException(CodeInfo.PLAT_REQUEST_FREQUENT);
            }
            if (null == reqBody) {
                throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
            }
            BigDecimal coin = reqBody.getCoin();
            Integer id = reqBody.getId();
            String coinPassword = reqBody.getCoinPassword();
            // 校验:金额不能低于1元
            if (coin.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(CodeInfo.COIN_TRANSFER_OVER_LIMIT);
            }
            // 校验:账号不存在
            UserInfo userInfo = userInfoServiceImpl.findIdentityByApiToken();
            if (null == userInfo) {
                throw new BusinessException(CodeInfo.ACCOUNT_NOT_EXISTS);
            }
            //冻结账户
            if (userInfo.getStatus()==9) {
                throw new BusinessException(CodeInfo.ACCOUNT_FROZEN);
            }
            String passwordCoin = userInfo.getPasswordCoin();
            // 校验资金密码
            if (Boolean.FALSE.equals(PasswordUtils.validatePasswordHash(coinPassword, passwordCoin))) {
                throw new BusinessException(CodeInfo.PASSWORD_INVALID);
            }
            Integer uid = userInfo.getId();
            Integer countNum = coinWithdrawalServiceImpl.lambdaQuery()
                    // 状态：0-申请中 1-提款成功 2-提款失败 3-稽核成功 4-稽核失败 9-系统出款
                    .in(CoinWithdrawal::getStatus, List.of(0, 3, 4))
                    .eq(CoinWithdrawal::getUid, uid).count();
            // 校验:请不要重复提交
            if (countNum > 0) {
                throw new BusinessException(CodeInfo.REPEATED_SUBMIT);
            }
            // 校验:银行卡无效
            UserBank userBank = userBankServiceImpl.lambdaQuery().eq(UserBank::getUid, uid).eq(UserBank::getId, id).one();
            if (null == userBank) {
                throw new BusinessException(CodeInfo.BANK_INVALID);
            }
            String bankInfo = JSON.toJSONString(userBank);
            BigDecimal coinBefore = userInfo.getCoin();
            // 校验:用户余额不足
            if (coin.compareTo(coinBefore) > 0) {
                throw new BusinessException(CodeInfo.COIN_NOT_ENOUGH);
            }
            // 校验:提款次数(每日) 与 提款限额(万/日)
            checkWithdrawalNumsAndTotalCoin(uid, userInfo.getLevelId(), coin);
            //提款时验证用户打码量
            auditBase.checkWithdrawal(uid);
            CoinWithdrawal po = new CoinWithdrawal();
            po.setCoinBefore(coinBefore);
            po.setBankInfo(bankInfo);
            po.setUid(uid);
            po.setUsername(userInfo.getUsername());
            po.setCoin(coin);
            Integer currentTime = DateNewUtils.now();
            po.setCreatedAt(currentTime);
            po.setUpdatedAt(currentTime);
            // 提现类型:0-其他 1-首次提款
            Integer count = coinWithdrawalServiceImpl.lambdaQuery()
                    // 状态：0-申请中 1-提款成功 2-提款失败 3-稽核成功 4-稽核失败 9-系统出款
                    .in(CoinWithdrawal::getStatus, 1, 9).eq(CoinWithdrawal::getUid, uid)
                    .count();
            if (0 == count) {
                // 提现类型:0-其他 1-首次提款
                po.setCategory(1);
            }
            // 新增提现记录
            coinWithdrawalServiceImpl.save(po);
            //修改用户金额
            return updateUserCoinBase.updateUserCoinSaveLog(UpdateUserCoinParams.UpdateUserCoinSaveLogDto.builder()
                    .uid(uid)
                    //收支类型:0-支出 1-收入
                    .outIn(0)
                    .coin(coin.negate())
                    .fcoin(coin)
                    .referId(po.getId())
                    .category(2)
                    .status(0)
                    .now(currentTime)
                    .build());
        } finally {
            if (b) {
                reentrantLock.unlock();
            }
        }
    }

    /**
     * 校验:
     * 1、提款次数(每日)
     * 2、提款限额(万/日)
     *
     * @param uid         UID
     * @param levelId     用户等级ID
     * @param currentCoin 当前提交金额
     */
    private void checkWithdrawalNumsAndTotalCoin(Integer uid, Integer levelId, BigDecimal currentCoin) {
        UserLevel userLevel = userCache.getUserLevelById(levelId);
        // 提款次数(每日)
        Integer withdrawalNumsMax = userLevel.getWithdrawalNums();
        // 提款限额(万/日)
        BigDecimal withdrawalTotalCoinMax = null != userLevel.getWithdrawalTotalCoin() ? new BigDecimal(userLevel.getWithdrawalTotalCoin()) : BigDecimal.ZERO;
        withdrawalTotalCoinMax = withdrawalTotalCoinMax.compareTo(BigDecimal.ZERO) > 0 ? withdrawalTotalCoinMax.multiply(new BigDecimal(10000)) : withdrawalTotalCoinMax;
        //单笔最新金额
        var minDrawCoinJson = parseObject(configCache.getConfigByTitle(WithdrawalHint.MIN_DRAW_COIN));
        var maxCoin = BigDecimal.valueOf(minDrawCoinJson.getInteger(WithdrawalHint.MAX_COIN));
        //单笔最小金额验证
        var minCoin = BigDecimal.valueOf(minDrawCoinJson.getInteger(WithdrawalHint.MIN_COIN));
        if (currentCoin.compareTo(minCoin) < 0) {
            var codeInfo = CodeInfo.MINIMUM_SINGLE_WITHDRAW;
            var codeInfoException = CodeInfo.MINIMUM_SINGLE_EXCEPTION;
            codeInfoException.setMsg(I18nUtils.getLocaleMessage(codeInfo.getMsg()) + " " + minCoin);
            throw new BusinessException(codeInfoException);
        }
        ZonedDateTime end = ZonedDateTime.now(ZoneId.systemDefault());
        ZonedDateTime start = end.with(LocalTime.MIN);
        List<CoinWithdrawal> list = coinWithdrawalServiceImpl.lambdaQuery()
                .eq(CoinWithdrawal::getUid, uid)
                .eq(CoinWithdrawal::getStatus, 1)
                .ge(CoinWithdrawal::getUpdatedAt, start.toEpochSecond())
                .le(CoinWithdrawal::getUpdatedAt, end.toEpochSecond())
                .list();
        int withdrawalNums = list.size();
        log.info("{}已提款次数:{} VS 最大提款次数:{}", uid, withdrawalNums, withdrawalNumsMax);
        if (withdrawalNums >= withdrawalNumsMax) {
            // 提款次数(每日)已达到上限
            throw new BusinessException(CodeInfo.WITHDRAWAL_NUMS_MAX_LIMIT);
        }
        BigDecimal withdrawalTotalCoin = list.stream().map(CoinWithdrawal::getCoin).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        withdrawalTotalCoin = withdrawalTotalCoin.add(currentCoin);
        log.info("{}已提款金额:{} VS 最大提款限额:{}", uid, withdrawalTotalCoin, withdrawalTotalCoinMax);
        if (withdrawalTotalCoin.compareTo(withdrawalTotalCoinMax) > 0 || currentCoin.compareTo(maxCoin) > 0) {
            // 提款限额(万/日)已达到上限
            throw new BusinessException(CodeInfo.WITHDRAWAL_TOTAL_COIN_MAX_LIMIT);
        }
    }
}
