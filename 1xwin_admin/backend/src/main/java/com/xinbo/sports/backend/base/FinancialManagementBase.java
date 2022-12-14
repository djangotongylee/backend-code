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
     * ??????????????????
     *
     * @param uid    ??????id
     * @param object ?????????????????????
     */
    public void addUserFlagProperties(int uid, Object object) {
        UserCacheBo.UserCacheInfo userCacheInfo = userCache.getUserInfoById(uid);
        if (null != userCacheInfo) {
            try {
                var objClass = object.getClass();
                objClass.getDeclaredMethod("setUsername", String.class).invoke(object, userCacheInfo.getUsername());
                objClass.getDeclaredMethod("setUserFlagList", List.class).invoke(object, userCache.getUserFlagList(uid));
            } catch (Exception e) {
                log.info("?????????????????????" + e.getMessage());
            }
        }
    }

    /**
     * ????????????????????????
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDepositStatus(CoinDeposit coinDeposit, LambdaUpdateWrapper<CoinDeposit> updateWrapper, UpdateDepositStatusReqDto reqDto, Integer now) {
        if (List.of(1, 2, 9).contains(reqDto.getStatus())) {
            /*
             * ??????????????????,status:1-???????????????2-???????????????9-???????????????
             * 1.????????????
             * 2.??????????????????
             * 3.??????????????????
             * 4.?????????????????????
             */
            //??????????????????
            updateUserCoinBase.updateUserCoinSaveLog(UpdateUserCoinParams.UpdateUserCoinSaveLogDto.builder()
                    .uid(coinDeposit.getUid())
                    //????????????:0-?????? 1-??????
                    .outIn(1)
                    .coin(reqDto.getPayCoin())
                    .referId(reqDto.getId())
                    .category(1)
                    .status(1)
                    .now(now)
                    .build());
            //???????????????
            var codeRecords = new CodeRecords();
            codeRecords.setUid(coinDeposit.getUid());
            codeRecords.setUsername(coinDeposit.getUsername());
            codeRecords.setCoin(reqDto.getPayCoin());
            codeRecords.setCodeRequire(reqDto.getPayCoin());
            //???????????????1?????????
            codeRecords.setCategory(1);
            codeRecords.setReferId(reqDto.getId());
            codeRecords.setCreatedAt(now);
            codeRecords.setUpdatedAt(now);
            codeRecordsServiceImpl.save(codeRecords);
            //???????????????????????????
            var depStatus = depositBase.firstOrSecondDeposit(coinDeposit.getUid(), reqDto.getPayCoin());
            updateWrapper.set(CoinDeposit::getDepStatus, depStatus);
            //????????????????????????????????????
            updateWrapper.set(nonNull(reqDto.getPayCoin()), CoinDeposit::getPayCoin, reqDto.getPayCoin());
        }
        return coinDepositServiceImpl.update(updateWrapper);
    }


    /**
     * ??????????????????
     * 1.??????????????????
     * 2.????????????????????????????????????
     * 3.???????????????????????????
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateWithdrawalStatus(CoinWithdrawal withdrawal, LambdaUpdateWrapper<CoinWithdrawal> updateWrapper, UpdateWithdrawalStatusReqDto reqDto) {
        Integer currentTime = DateUtils.getCurrentTime();
        //??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        if (List.of(WithdrawalStatus.WITHDRAWAL_SUCCESS.getCode(), WithdrawalStatus.WITHDRAWAL_FAIL.getCode(), WithdrawalStatus.SYSTEM_WITHDRAWAL.getCode())
                .contains(withdrawal.getStatus()) || (withdrawal.getStatus().equals(reqDto.getStatus()))) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        //???????????????????????????????????????????????????
        if (WithdrawalStatus.WITHDRAWAL_SUCCESS.getCode().equals(reqDto.getStatus()) || WithdrawalStatus.WITHDRAWAL_FAIL.getCode().equals(reqDto.getStatus())) {
            var count = coinOnlineWithdrawalServiceImpl.lambdaQuery()
                    .eq(CoinOnlineWithdrawal::getWithdrawalOrderId, reqDto.getId())
                    .eq(CoinOnlineWithdrawal::getStatus, 0)
                    .count();
            if (count > 0) {
                throw new BusinessException(CodeInfo.PAYOUT_APPLYING);
            }
        }

        //????????????
        if (WithdrawalStatus.WITHDRAWAL_SUCCESS.getCode().equals(reqDto.getStatus()) || WithdrawalStatus.SYSTEM_WITHDRAWAL.getCode().equals(reqDto.getStatus())) {
            //????????????????????????????????????
            var count = coinWithdrawalServiceImpl.lambdaQuery()
                    .eq(CoinWithdrawal::getUid, withdrawal.getUid())
                    //??????-???0????????????
                    .eq(CoinWithdrawal::getStatus, WithdrawalStatus.APPLY.getCode())
                    .count();
            if (count > 1) {
                throw new BusinessException(CodeInfo.USER_WITHDRAWAL_OVER_LIMIT);
            }
            //???????????????????????????
            var auditCount = codeAuditServiceImpl.lambdaQuery()
                    .eq(CodeAudit::getReferId, reqDto.getId())
                    .eq(CodeAudit::getStatus, WithdrawalStatus.WITHDRAWAL_SUCCESS.getCode())
                    .count();
            if (auditCount != 1) {
                throw new BusinessException(CodeInfo.USER_NO_PASS_AUDIT);
            }
            updateUserCoinBase.updateCoinLog(reqDto.getId(), 2, 1, currentTime);
            //?????????????????????
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
            //???????????????????????????
            userServiceImpl.lambdaUpdate()
                    .setSql("fcoin = fcoin -" + withdrawal.getCoin())
                    .set(User::getUpdatedAt, currentTime)
                    .eq(User::getId, withdrawal.getUid())
                    .update();
            //????????????
        } else if (WithdrawalStatus.WITHDRAWAL_FAIL.getCode().equals(reqDto.getStatus())) {
            //??????????????????
            updateUserCoinBase.updateUserCoinUpdateLog(UpdateUserCoinParams.UpdateUserCoinUpdateLogByReferId.builder()
                    .uid(withdrawal.getUid())
                    .coin(withdrawal.getCoin())
                    .fcoin(withdrawal.getCoin().negate())
                    .referId(reqDto.getId())
                    .category(2)
                    //??????:0-????????? 1-?????? 2-??????
                    .status(2)
                    .now(currentTime)
                    .build());
            //??????????????????
        } else if (WithdrawalStatus.AUDIT_SUCCESS.getCode().equals(reqDto.getStatus())) {
            updateWrapper.set(nonNull(reqDto.getMark()), CoinWithdrawal::getMark, reqDto.getMark());
            //????????????????????????????????????
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
     * ??????????????????
     * 1.??????????????????
     * 2???????????????????????????
     * 3.????????????????????????
     *
     * @param reqDto      ???????????????????????????id??????????????????id
     * @param codeReal    ???????????????
     * @param codeRequire ???????????????
     * @param status      ????????????
     * @param recordsList ???????????????
     * @return ????????????
     */
    @Transactional(rollbackFor = Exception.class)
    public void auditPersistence(AuditBaseParams.AuditReqDto reqDto, BigDecimal codeReal, BigDecimal codeRequire,
                                 Integer status, List<CodeRecords> recordsList, String mark) {
        var now = DateUtils.getCurrentTime();
        var currentLoginUser = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        //3-???????????? 4-????????????
        var auditStatus = 4;
        //1:?????????2?????????
        if (status == 1) {
            recordsList.forEach(x -> {
                //?????????????????????
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
        //??????????????????
        var updateWithdrawal = new UpdateWithdrawalStatusReqDto();
        updateWithdrawal.setId(reqDto.getId());
        //????????????
        if (auditStatus == 4) {
            updateWithdrawal.setMark(mark);
        }
        updateWithdrawal.setStatus(auditStatus);
        financialWithdrawalServiceImpl.updateWithdrawalStatus(updateWithdrawal);
        codeAuditServiceImpl.save(codeAudit);
    }

    /**
     * ??????????????????
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean transferPersistence(AdminTransferReqDto reqDto) {
        var currentLoginUser = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        var uid = reqDto.getUid();
        var user = userServiceImpl.getById(uid);
        var now = DateUtils.getCurrentTime();
        //????????????:0-?????? 1-??????
        var outIn = 1;
        var transferCoin = reqDto.getCoin().negate();
        if (reqDto.getCoinOperatorType() == 1) {
            outIn = 0;
            transferCoin = reqDto.getCoin();
        }
        //??????????????????????????????
        if (reqDto.getCoinOperatorType() == 2 && user.getCoin().compareTo(reqDto.getCoin()) < 0) {
            throw new BusinessException(CodeInfo.COIN_NOT_ENOUGH);
        }
        //??????
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
        //??????????????????
        return updateUserCoinBase.updateUserCoinSaveLog(UpdateUserCoinParams.UpdateUserCoinSaveLogDto.builder()
                .uid(reqDto.getUid())
                //????????????:0-?????? 1-??????
                .outIn(outIn)
                .coin(transferCoin)
                .referId(coinAdminTransfer.getId())
                .category(8)
                .status(1)
                .now(now)
                .build());
    }

    /**
     * ????????????
     *
     * @param reqDto : ?????????????????????
     * @Return boolean
     **/
    @Transactional(rollbackFor = Exception.class)
    public boolean backendDeposit(AdminTransferReqDto reqDto) {
        // ????????????????????????
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
        //9-???????????????
        coinDeposit.setPayType(0);
        //?????????ID?????????0
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
        // ?????????????????????
        CodeRecords codeRecords = new CodeRecords();
        codeRecords.setUid(coinDeposit.getUid());
        codeRecords.setCoin(coinDeposit.getPayCoin());
        codeRecords.setCodeRequire(coinDeposit.getPayCoin());
        codeRecords.setCategory(1);
        codeRecords.setReferId(coinDeposit.getId());
        codeRecords.setUpdatedAt(now);
        codeRecords.setCreatedAt(now);
        codeRecordsServiceImpl.save(codeRecords);
        //??????????????????
        return updateUserCoinBase.updateUserCoinSaveLog(UpdateUserCoinParams.UpdateUserCoinSaveLogDto.builder()
                .uid(reqDto.getUid())
                //????????????:0-?????? 1-??????
                .outIn(1)
                .coin(coin)
                .referId(coinDeposit.getId())
                .category(1)
                .status(1)
                .now(now)
                .build());
    }

    /**
     * ????????????
     *
     * @param reqDto : ?????????????????????
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
        // ??????:???????????????
        UserBank userBank = userBankServiceImpl.lambdaQuery().eq(UserBank::getUid, uid).eq(UserBank::getId, reqDto.getBankId()).one();
        if (null == userBank) {
            throw new BusinessException(CodeInfo.BANK_INVALID);
        }
        var count = coinWithdrawalServiceImpl.lambdaQuery()
                .eq(CoinWithdrawal::getUid, uid)
                .in(CoinWithdrawal::getStatus, List.of(1, 9))
                .count();
        //????????????:0-?????? 1-????????????
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
        //??????????????????
        return updateUserCoinBase.updateUserCoinSaveLog(UpdateUserCoinParams.UpdateUserCoinSaveLogDto.builder()
                .uid(reqDto.getUid())
                //????????????:0-?????? 1-??????
                .outIn(0)
                .coin(coin.negate())
                .referId(po.getId())
                .category(2)
                .status(1)
                .now(currentTime)
                .build());
    }


    /**
     * ????????????
     *
     * @param user       ????????????
     * @param referId    ??????id
     * @param coin       ????????????
     * @param coinBefore ???????????????
     */
    public CoinLog getCoinLog(User user, Long referId, Integer category, BigDecimal coin, BigDecimal coinBefore) {
        var now = DateUtils.getCurrentTime();
        //????????????
        CoinLog coinLog = new CoinLog();
        coinLog.setUid(user.getId());
        coinLog.setUsername(user.getUsername());
        coinLog.setReferId(referId);
        //????????????-????????????1
        coinLog.setCategory(category);
        coinLog.setCoin(coin);
        coinLog.setCoinBefore(coinBefore);
        coinLog.setCreatedAt(now);
        coinLog.setUpdatedAt(now);
        return coinLog;
    }
}
