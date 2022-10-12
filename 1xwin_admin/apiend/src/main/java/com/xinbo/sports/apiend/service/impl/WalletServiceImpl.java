package com.xinbo.sports.apiend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.xinbo.sports.apiend.cache.redis.HomeCache;
import com.xinbo.sports.apiend.io.dto.wallet.BankAddReqBody;
import com.xinbo.sports.apiend.io.dto.wallet.BankInfoList;
import com.xinbo.sports.apiend.io.dto.wallet.BankUpdateReqBody;
import com.xinbo.sports.apiend.io.dto.wallet.UserBankList;
import com.xinbo.sports.apiend.service.IWalletService;
import com.xinbo.sports.dao.generator.po.BankList;
import com.xinbo.sports.dao.generator.po.UserBank;
import com.xinbo.sports.dao.generator.po.UserProfile;
import com.xinbo.sports.dao.generator.service.UserBankService;
import com.xinbo.sports.dao.generator.service.UserProfileService;
import com.xinbo.sports.service.base.UserServiceBase;
import com.xinbo.sports.service.cache.redis.BankCache;
import com.xinbo.sports.service.common.Constant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.constant.ConstData;
import com.xinbo.sports.service.io.dto.BaseParams;
import com.xinbo.sports.service.thread.ThreadHeaderLocalData;
import com.xinbo.sports.utils.BeanConvertUtils;
import com.xinbo.sports.utils.DateUtils;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 钱包业务接口实现类
 * </p>
 *
 * @author andy
 * @since 2020/6/2
 */
@Slf4j
@Service
public class WalletServiceImpl implements IWalletService {
    @Resource
    private UserBankService userBankServiceImpl;
    @Resource
    private UserProfileService userProfileServiceImpl;
    @Resource
    private HomeCache homeCache;
    @Resource
    private BankCache bankCache;
    @Resource
    private UserServiceBase userServiceBase;

    /**
     * 获取系统银行卡列表
     *
     * @return 银行卡列表
     */
    @Override
    public List<BankInfoList> bankInfoList() {
        return BeanConvertUtils.copyListProperties(bankCache.getBankListCache(), BankInfoList::new, (source, bankInfoList) -> {
            bankInfoList.setValue(source.getId());
            bankInfoList.setLabel(source.getName());
        });
    }

    @Override
    public List<UserBankList> userBankList() {
        BaseParams.HeaderInfo headerInfo = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        // 获取静态资源地址
        return userBankServiceImpl.lambdaQuery()
                .eq(UserBank::getUid, headerInfo.getId())
                .in(UserBank::getStatus, 1, 2)
                .list()
                .stream()
                .map(o -> {
                    UserBankList userBankList = BeanConvertUtils.beanCopy(o, UserBankList::new);
                    // 获取缓存中Bank信息(已拼接静态资源地址)
                    BankList cache = this.bankCache.getBankCache(userBankList.getBankId());
                    if (cache != null) {
                        userBankList.setName(cache.getName());
                        userBankList.setIcon(cache.getIcon());
                    }
                    return userBankList;
                }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bankAdd(@Valid @RequestBody BankAddReqBody reqBody) {
        BaseParams.HeaderInfo headerInfo = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        Integer currentTime = DateUtils.getCurrentTime();
        UserBank userBank = BeanConvertUtils.beanCopy(reqBody, UserBank::new);
        if (null != userBank) {
            var bankInfo = bankCache.getBankCache(userBank.getBankId());
            // 持卡人姓名不能为空(泰国地区除外)
            if (null != bankInfo && !Constant.COUNTRY_THAILAND.equals(bankInfo.getCountry()) && StringUtils.isBlank(userBank.getAccountName())) {
                throw new BusinessException(CodeInfo.BANK_ACCOUNT_NAME_NOT_NULL);
            }

            List<UserBank> bankAccount = userBankServiceImpl.list(new QueryWrapper<UserBank>().eq("bank_account", reqBody.getBankAccount()));
            if (!bankAccount.isEmpty()) {
                throw new BusinessException(CodeInfo.BANKCARD_ALREADY_EXIST);
            }
            userBank.setCreatedAt(currentTime);
            userBank.setUpdatedAt(currentTime);
            Integer uid = headerInfo.getId();
            LambdaQueryWrapper<UserBank> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserBank::getUid, uid);
            wrapper.in(UserBank::getStatus, 1, 2);
            int userBankCount = userBankServiceImpl.count(wrapper);
            if (userBankCount >= ConstData.USER_MAX_BIND_BANK) {
                throw new BusinessException(CodeInfo.BANK_BIND_OVER_LIMIT);
            }
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserBank::getStatus, 1);
            wrapper.eq(UserBank::getUid, uid);
            userBankCount = userBankServiceImpl.count(wrapper);
            if (userBankCount > 0) {
                userBank.setStatus(2);
            } else {
                // 默认银行卡-状态:1-启用(默认) 2-启用 3-停用 4-删除
                userBank.setStatus(1);
            }
            userBank.setUid(uid);
            userBank.setUsername(headerInfo.getUsername());

            // 开户行地址
            userBank.setMark(reqBody.getAddress());
            userBankServiceImpl.save(userBank);
            UserProfile userProfile = new UserProfile();
            // 是否绑定银行卡:1-已绑定 0-未绑定
            userProfile.setBindBank(1);
            // 更新sp_user_profile的realName，realName=accountName
            userProfile.setRealname(userBank.getAccountName());
            userProfile.setUpdatedAt(currentTime);
            userProfileServiceImpl.update(userProfile, new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUid, userBank.getUid()));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bankUpdate(@Valid @RequestBody BankUpdateReqBody reqBody) {
        BaseParams.HeaderInfo headerInfo = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        Integer status = reqBody.getStatus();
        Integer id = reqBody.getId();
        Integer uid = headerInfo.getId();
        Integer currentTime = DateUtils.getCurrentTime();
        UserBank userBank = new UserBank();
        userBank.setStatus(status);
        userBank.setUpdatedAt(currentTime);
        LambdaQueryWrapper<UserBank> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserBank::getUid, uid)
                .eq(UserBank::getId, id);
        userBankServiceImpl.update(userBank, wrapper);
        // 一个用户下，只能有一张默认卡(状态:1-启用(默认) 2-启用 3-停用 4-删除)
        if (null != status && status == 1) {
            // status = 2
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserBank::getUid, uid)
                    .ne(UserBank::getId, id).eq(UserBank::getStatus, 1);
            List<UserBank> list = userBankServiceImpl.list(wrapper);
            if (!list.isEmpty()) {
                for (UserBank po : list) {
                    if (null != po) {
                        po.setStatus(2);
                        po.setUpdatedAt(currentTime);
                    }
                }
                userBankServiceImpl.updateBatchById(list);
            }
        }
    }
}
