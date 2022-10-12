package com.xinbo.sports.backend.base;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xinbo.sports.backend.io.dto.FinancialAdminParameter.AdminTransferReqDto;
import com.xinbo.sports.backend.io.dto.FinancialDepositParameter.UpdateDepositStatusReqDto;
import com.xinbo.sports.backend.io.dto.FinancialWithdrawalParameter.UpdateWithdrawalStatusReqDto;
import com.xinbo.sports.backend.io.dto.FinancialWithdrawalParameter.WithdrawalStatus;
import com.xinbo.sports.backend.service.FinancialWithdrawalService;
import com.xinbo.sports.dao.generator.po.*;
import com.xinbo.sports.dao.generator.service.*;
import com.xinbo.sports.service.base.AuditBase;
import com.xinbo.sports.service.base.DepositBase;
import com.xinbo.sports.service.base.UpdateUserCoinBase;
import com.xinbo.sports.service.cache.KeyConstant;
import com.xinbo.sports.service.cache.redis.UserCache;
import com.xinbo.sports.service.common.Constant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.bo.UserCacheBo;
import com.xinbo.sports.service.io.dto.AuditBaseParams;
import com.xinbo.sports.service.io.dto.BaseParams;
import com.xinbo.sports.service.io.dto.UpdateUserCoinParams;
import com.xinbo.sports.service.thread.ThreadHeaderLocalData;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.DateUtils;
import com.xinbo.sports.utils.JedisUtil;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.xinbo.sports.backend.io.dto.FinancialAdminParameter.OperatorTypeEnum.BACKEND_DEPOSIT;
import static com.xinbo.sports.backend.io.dto.FinancialAdminParameter.OperatorTypeEnum.BACKEND_WITHDRAWAL;
import static java.util.Objects.nonNull;

/**
 * @author: wells
 * @date: 2020/8/13
 * @description:
 */
@Slf4j
@Component
public class FinancialManagementBase {
    @Autowired
    private CoinDepositService coinDepositServiceImpl;
    @Autowired
    private UserService userServiceImpl;
    @Resource
    private CoinLogService coinLogServiceImpl;
    @Autowired
    private CodeRecordsService codeRecordsServiceImpl;
    @Autowired
    private CoinWithdrawalService coinWithdrawalServiceImpl;
    @Autowired
    private CodeAuditService codeAuditServiceImpl;
    @Autowired
    private CoinAdminTransferService coinAdminTransferServiceImpl;
    @Autowired
    private UserCache userCache;
    @Autowired
    private FinancialWithdrawalService financialWithdrawalServiceImpl;
    @Autowired
    private DepositBase depositBase;
    @Autowired
    private UserBankService userBankServiceImpl;
    @Autowired
    private CoinOnlineWithdrawalService coinOnlineWithdrawalServiceImpl;
    @Autowired
    private UpdateUserCoinBase updateUserCoinBase;
    @Autowired
    private AuditBase auditBase;
    @Autowired
    private UserProfileService userProfileServiceImpl;
    @Autowired
    private JedisUtil jedisUtil;

    /**
     * 设置会员信息
     *
     * @param uid    用户id
     * @param object 设置属性的对象
     */
    public void addUserFlagProperties(int uid, Object object) {
        UserCacheBo.UserCacheInfo userCacheInfo = userCache.getUserInfoById(uid);
        if (null != userCacheInfo) {
            try {
                var objClass = object.getClass();
                objClass.getDeclaredMethod("setUsername", String.class).invoke(object, userCacheInfo.getUsername());
                objClass.getDeclaredMethod("setUserFlagList", List.class).invoke(object, userCache.getUserFlagList(uid));
            } catch (Exception e) {
                log.info("获取会员旗异常" + e.getMessage());
            }
        }
    }

    /**
     * 修改充值记录状态
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDepositStatus(CoinDeposit coinDeposit, LambdaUpdateWrapper<CoinDeposit> updateWrapper, UpdateDepositStatusReqDto reqDto, Integer now) {
        if (List.of(1, 2, 9).contains(reqDto.getStatus())) {
            /*
             * 充值成功处理,status:1-手动到账，2-自动到账，9-管理员充值
             * 1.插入日志
             * 2.插入打码量表
             * 3.修改用户余额
             * 4.更新充值表状态
             */
            //修改用户金额
            updateUserCoinBase.updateUserCoinSaveLog(UpdateUserCoinParams.UpdateUserCoinSaveLogDto.builder()
                    .uid(coinDeposit.getUid())
                    //收支类型:0-支出 1-收入
                    .outIn(1)
                    .coin(reqDto.getPayCoin())
                    .referId(reqDto.getId())
                    .category(1)
                    .status(1)
                    .now(now)
                    .build());
            //打码量入库
            var codeRecords = new CodeRecords();
            codeRecords.setUid(coinDeposit.getUid());
            codeRecords.setUsername(coinDeposit.getUsername());
            codeRecords.setCoin(reqDto.getPayCoin());
            codeRecords.setCodeRequire(reqDto.getPayCoin());
            //打码量类型1：充值
            codeRecords.setCategory(1);
            codeRecords.setReferId(reqDto.getId());
            codeRecords.setCreatedAt(now);
            codeRecords.setUpdatedAt(now);
            codeRecordsServiceImpl.save(codeRecords);
            //判断是否是首充二充
            var depStatus = depositBase.firstOrSecondDeposit(coinDeposit.getUid(), reqDto.getPayCoin());
            updateWrapper.set(CoinDeposit::getDepStatus, depStatus);
            //修改充值表的实际到账金额
            updateWrapper.set(nonNull(reqDto.getPayCoin()), CoinDeposit::getPayCoin, reqDto.getPayCoin());
        }
        return coinDepositServiceImpl.update(updateWrapper);
    }


    /**
     * 提款状态修改
     * 1.判断用户余额
     * 2.当前用户是否存在多笔提款
     * 3.此提款是否稽核通过
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateWithdrawalStatus(CoinWithdrawal withdrawal, LambdaUpdateWrapper<CoinWithdrawal> updateWrapper, UpdateWithdrawalStatusReqDto reqDto) {
        Integer currentTime = DateUtils.getCurrentTime();
        //记录状态为提款成功，提款失败，系统出款不处理或者更新状态与当前记录一样不更新
        if (List.of(WithdrawalStatus.WITHDRAWAL_SUCCESS.getCode(), WithdrawalStatus.WITHDRAWAL_FAIL.getCode(), WithdrawalStatus.SYSTEM_WITHDRAWAL.getCode())
                .contains(withdrawal.getStatus()) || (withdrawal.getStatus().equals(reqDto.getStatus()))) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        //审核成功与失败时，验证代付是否成功
        if (WithdrawalStatus.WITHDRAWAL_SUCCESS.getCode().equals(reqDto.getStatus()) || WithdrawalStatus.WITHDRAWAL_FAIL.getCode().equals(reqDto.getStatus())) {
            var count = coinOnlineWithdrawalServiceImpl.lambdaQuery()
                    .eq(CoinOnlineWithdrawal::getWithdrawalOrderId, reqDto.getId())
                    .eq(CoinOnlineWithdrawal::getStatus, 0)
                    .count();
            if (count > 0) {
                throw new BusinessException(CodeInfo.PAYOUT_APPLYING);
            }
        }

        //上分成功
        if (WithdrawalStatus.WITHDRAWAL_SUCCESS.getCode().equals(reqDto.getStatus()) || WithdrawalStatus.SYSTEM_WITHDRAWAL.getCode().equals(reqDto.getStatus())) {
            //当前用户是否存在多笔提款
            var count = coinWithdrawalServiceImpl.lambdaQuery()
                    .eq(CoinWithdrawal::getUid, withdrawal.getUid())
                    //状态-》0：申请中
                    .eq(CoinWithdrawal::getStatus, WithdrawalStatus.APPLY.getCode())
                    .count();
            if (count > 1) {
                throw new BusinessException(CodeInfo.USER_WITHDRAWAL_OVER_LIMIT);
            }
            //此提款是否稽核通过
            var auditCount = codeAuditServiceImpl.lambdaQuery()
                    .eq(CodeAudit::getReferId, reqDto.getId())
                    .eq(CodeAudit::getStatus, WithdrawalStatus.WITHDRAWAL_SUCCESS.getCode())
                    .count();
            if (auditCount != 1) {
                throw new BusinessException(CodeInfo.USER_NO_PASS_AUDIT);
            }
            updateUserCoinBase.updateCoinLog(reqDto.getId(), 2, 1, currentTime);
            //修改额外打码量
            var userProfile = userProfileServiceImpl.getOne(new LambdaQueryWrapper<UserProfile>()
                    .eq(UserProfile::getUid, withdrawal.getUid()), false);
            if (Objects.nonNull(userProfile) && userProfile.getExtraCodeRule().equals(Constant.CODE_ONE)) {
                userProfileServiceImpl.lambdaUpdate().
                        set(UserProfile::getExtraCode, BigDecimal.ZERO)
                        .set(UserProfile::getUpdatedAt, currentTime)
                        .eq(UserProfile::getUid, withdrawal.getUid())
                        .update();
                jedisUtil.hdel(KeyConstant.USER_BASIC_INFO_UID_HASH, withdrawal.getUid().toString());
            }
            //修改用户的冻结金额
            userServiceImpl.lambdaUpdate()
                    .setSql("fcoin = fcoin -" + withdrawal.getCoin())
                    .set(User::getUpdatedAt, currentTime)
                    .eq(User::getId, withdrawal.getUid())
                    .update();
            //上分失败
        } else if (WithdrawalStatus.WITHDRAWAL_FAIL.getCode().equals(reqDto.getStatus())) {
            //修改用户金额
            updateUserCoinBase.updateUserCoinUpdateLog(UpdateUserCoinParams.UpdateUserCoinUpdateLogByReferId.builder()
                    .uid(withdrawal.getUid())
                    .coin(withdrawal.getCoin())
                    .fcoin(withdrawal.getCoin().negate())
                    .referId(reqDto.getId())
                    .category(2)
                    //状态:0-处理中 1-成功 2-失败
                    .status(2)
                    .now(currentTime)
                    .build());
            //手动稽核成功
        } else if (WithdrawalStatus.AUDIT_SUCCESS.getCode().equals(reqDto.getStatus())) {
            updateWrapper.set(nonNull(reqDto.getMark()), CoinWithdrawal::getMark, reqDto.getMark());
            //修改稽核表与打码量记录表
            codeAuditServiceImpl.lambdaUpdate()
                    .set(CodeAudit::getStatus, 1)
                    .set(CodeAudit::getUpdatedAt, currentTime)
                    .eq(CodeAudit::getReferId, reqDto.getId())
                    .update();
            var auditReqDto = new AuditBaseParams.AuditReqDto();
            auditReqDto.setId(reqDto.getId());
            auditReqDto.setUid(withdrawal.getUid());
            var pair = auditBase.getCodeRecords(auditReqDto, true);
            var codeRecordList = pair.getRight();
            codeRecordList.forEach(codeRecords -> {
                codeRecords.setStatus(1);
                codeRecords.setRemarks(Constant.MANUAL_AUDIT_SUCCESS);
                codeRecords.setUpdatedAt(currentTime);
            });
            codeRecordsServiceImpl.updateBatchById(codeRecordList);
        }
        return coinWithdrawalServiceImpl.update(updateWrapper);
    }

    /**
     * 稽核入库处理
     * 1.新增稽核记录
     * 2，修改打码量表状态
     * 3.修改提款记录状态
     *
     * @param reqDto      稽核请求参数，用户id，及提款记录id
     * @param codeReal    真实打码量
     * @param codeRequire 所需打码量
     * @param status      稽核状态
     * @param recordsList 打码量记录
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public void auditPersistence(AuditBaseParams.AuditReqDto reqDto, BigDecimal codeReal, BigDecimal codeRequire,
                                 Integer status, List<CodeRecords> recordsList, String mark) {
        var now = DateUtils.getCurrentTime();
        var currentLoginUser = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        //3-稽核成功 4-稽核失败
        var auditStatus = 4;
        //1:成功，2：失败
        if (status == 1) {
            recordsList.forEach(x -> {
                //更新状态为结算
                x.setStatus(1);
                x.setRemarks(Constant.AUDIT_SUCCESS);
                x.setUpdatedAt(now);
            });
            auditStatus = 3;
        } else {
            recordsList.forEach(x -> {
                x.setRemarks(Constant.AUDIT_FAIL);
                x.setUpdatedAt(now);
            });
        }
        codeRecordsServiceImpl.updateBatchById(recordsList);
        var username = userCache.getUserInfoById(reqDto.getUid()).getUsername();
        var codeAudit = new CodeAudit();
        codeAudit.setUid(reqDto.getUid());
        codeAudit.setUsername(username);
        codeAudit.setCodeRequire(codeRequire);
        codeAudit.setCodeReal(codeReal);
        codeAudit.setReferId(reqDto.getId());
        codeAudit.setAuditId(currentLoginUser.id);
        codeAudit.setStatus(status);
        codeAudit.setCreatedAt(now);
        codeAudit.setUpdatedAt(now);
        //修改提款信息
        var updateWithdrawal = new UpdateWithdrawalStatusReqDto();
        updateWithdrawal.setId(reqDto.getId());
        //稽核失败
        if (auditStatus == 4) {
            updateWithdrawal.setMark(mark);
        }
        updateWithdrawal.setStatus(auditStatus);
        financialWithdrawalServiceImpl.updateWithdrawalStatus(updateWithdrawal);
        codeAuditServiceImpl.save(codeAudit);
    }

    /**
     * 人工调整入库
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean transferPersistence(AdminTransferReqDto reqDto) {
        var currentLoginUser = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        var uid = reqDto.getUid();
        var user = userServiceImpl.getById(uid);
        var now = DateUtils.getCurrentTime();
        //收支类型:0-支出 1-收入
        var outIn = 1;
        var transferCoin = reqDto.getCoin().negate();
        if (reqDto.getCoinOperatorType() == 1) {
            outIn = 0;
            transferCoin = reqDto.getCoin();
        }
        //验证余额是否满足支出
        if (reqDto.getCoinOperatorType() == 2 && user.getCoin().compareTo(reqDto.getCoin()) < 0) {
            throw new BusinessException(CodeInfo.COIN_NOT_ENOUGH);
        }
        //调账
        var coinAdminTransfer = new CoinAdminTransfer();
        coinAdminTransfer.setAdminId(currentLoginUser.getId());
        coinAdminTransfer.setCoin(transferCoin);
        coinAdminTransfer.setCoinBefore(user.getCoin());
        coinAdminTransfer.setCategory(reqDto.getCategory());
        coinAdminTransfer.setMark(reqDto.getMark());
        coinAdminTransfer.setUid(uid);
        coinAdminTransfer.setUsername(user.getUsername());
        coinAdminTransfer.setCreatedAt(now);
        coinAdminTransfer.setUpdatedAt(now);
        coinAdminTransferServiceImpl.save(coinAdminTransfer);
        //修改用户金额
        return updateUserCoinBase.updateUserCoinSaveLog(UpdateUserCoinParams.UpdateUserCoinSaveLogDto.builder()
                .uid(reqDto.getUid())
                //收支类型:0-支出 1-收入
                .outIn(outIn)
                .coin(transferCoin)
                .referId(coinAdminTransfer.getId())
                .category(8)
                .status(1)
                .now(now)
                .build());
    }

    /**
     * 后台充值
     *
     * @param reqDto : 请求参数实体类
     * @Return boolean
     **/
    @Transactional(rollbackFor = Exception.class)
    public boolean backendDeposit(AdminTransferReqDto reqDto) {
        // 插入一条充值记录
        int now = DateUtils.getCurrentTime();
        var uid = reqDto.getUid();
        var coin = reqDto.getCoin();
        var user = userServiceImpl.getById(uid);
        BaseParams.HeaderInfo currentLoginUser = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        var depStatus = depositBase.firstOrSecondDeposit(user.getId(), coin);
        CoinDeposit coinDeposit = new CoinDeposit();
        coinDeposit.setTitle(BACKEND_DEPOSIT.getMsg());
        coinDeposit.setUid(uid);
        coinDeposit.setAdminId(currentLoginUser.id);
        coinDeposit.setAuditUid(currentLoginUser.id);
        coinDeposit.setAuditMark(Optional.ofNullable(reqDto.getMark()).orElse(""));
        coinDeposit.setUsername(user.getUsername());
        //9-管理员充值
        coinDeposit.setPayType(0);
        //无关联ID，默认0
        coinDeposit.setPayRefer(0);
        coinDeposit.setCoin(coin);
        coinDeposit.setPayCoin(coin);
        coinDeposit.setStatus(9);
        coinDeposit.setAuditStatus(3);
        coinDeposit.setDepStatus(depStatus);
        coinDeposit.setCoinBefore(user.getCoin());
        coinDeposit.setCreatedAt(now);
        coinDeposit.setUpdatedAt(now);
        coinDepositServiceImpl.save(coinDeposit);
        // 插入打码量数据
        CodeRecords codeRecords = new CodeRecords();
        codeRecords.setUid(coinDeposit.getUid());
        codeRecords.setCoin(coinDeposit.getPayCoin());
        codeRecords.setCodeRequire(coinDeposit.getPayCoin());
        codeRecords.setCategory(1);
        codeRecords.setReferId(coinDeposit.getId());
        codeRecords.setUpdatedAt(now);
        codeRecords.setCreatedAt(now);
        codeRecordsServiceImpl.save(codeRecords);
        //修改用户金额
        return updateUserCoinBase.updateUserCoinSaveLog(UpdateUserCoinParams.UpdateUserCoinSaveLogDto.builder()
                .uid(reqDto.getUid())
                //收支类型:0-支出 1-收入
                .outIn(1)
                .coin(coin)
                .referId(coinDeposit.getId())
                .category(1)
                .status(1)
                .now(now)
                .build());
    }

    /**
     * 后台提现
     *
     * @param reqDto : 请求参数实体类
     * @Return boolean
     **/
    @Transactional(rollbackFor = Exception.class)
    public boolean backendWithdrawal(AdminTransferReqDto reqDto) {
        var uid = reqDto.getUid();
        var coin = reqDto.getCoin();
        var user = userServiceImpl.getById(uid);
        BaseParams.HeaderInfo currentLoginUser = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        if (coin.compareTo(user.getCoin()) > 0) {
            throw new BusinessException(CodeInfo.COIN_NOT_ENOUGH);
        }
        // 校验:银行卡无效
        UserBank userBank = userBankServiceImpl.lambdaQuery().eq(UserBank::getUid, uid).eq(UserBank::getId, reqDto.getBankId()).one();
        if (null == userBank) {
            throw new BusinessException(CodeInfo.BANK_INVALID);
        }
        var count = coinWithdrawalServiceImpl.lambdaQuery()
                .eq(CoinWithdrawal::getUid, uid)
                .in(CoinWithdrawal::getStatus, List.of(1, 9))
                .count();
        //提现类型:0-其他 1-首次提款
        var category = count > 0 ? 0 : 1;
        String bankInfo = JSON.toJSONString(userBank);
        CoinWithdrawal po = new CoinWithdrawal();
        po.setAdminUid(currentLoginUser.id);
        po.setMark(Optional.ofNullable(reqDto.getMark()).orElse(""));
        po.setCoinBefore(user.getCoin());
        po.setBankInfo(bankInfo);
        po.setUid(uid);
        po.setUsername(user.getUsername());
        po.setCoin(coin);
        po.setStatus(9);
        Integer currentTime = DateNewUtils.now();
        po.setMark(BACKEND_WITHDRAWAL.getMsg());
        po.setCategory(category);
        po.setCreatedAt(currentTime);
        po.setUpdatedAt(currentTime);
        coinWithdrawalServiceImpl.save(po);
        //修改用户金额
        return updateUserCoinBase.updateUserCoinSaveLog(UpdateUserCoinParams.UpdateUserCoinSaveLogDto.builder()
                .uid(reqDto.getUid())
                //收支类型:0-支出 1-收入
                .outIn(0)
                .coin(coin.negate())
                .referId(po.getId())
                .category(2)
                .status(1)
                .now(currentTime)
                .build());
    }


    /**
     * 拼装日志
     *
     * @param user       用户信息
     * @param referId    关联id
     * @param coin       交易金额
     * @param coinBefore 交易前金额
     */
    public CoinLog getCoinLog(User user, Long referId, Integer category, BigDecimal coin, BigDecimal coinBefore) {
        var now = DateUtils.getCurrentTime();
        //日志入库
        CoinLog coinLog = new CoinLog();
        coinLog.setUid(user.getId());
        coinLog.setUsername(user.getUsername());
        coinLog.setReferId(referId);
        //活动类型-》充值：1
        coinLog.setCategory(category);
        coinLog.setCoin(coin);
        coinLog.setCoinBefore(coinBefore);
        coinLog.setCreatedAt(now);
        coinLog.setUpdatedAt(now);
        return coinLog;
    }
}
