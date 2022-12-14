package com.xinbo.sports.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xinbo.sports.backend.io.bo.user.BankAddReqBody;
import com.xinbo.sports.backend.io.bo.user.BankInfoList;
import com.xinbo.sports.backend.io.bo.user.UserBankList;
import com.xinbo.sports.backend.service.IUserBankBusinessService;
import com.xinbo.sports.dao.generator.po.BankList;
import com.xinbo.sports.dao.generator.po.User;
import com.xinbo.sports.dao.generator.po.UserBank;
import com.xinbo.sports.dao.generator.po.UserProfile;
import com.xinbo.sports.dao.generator.service.BankListService;
import com.xinbo.sports.dao.generator.service.UserBankService;
import com.xinbo.sports.dao.generator.service.UserProfileService;
import com.xinbo.sports.dao.generator.service.UserService;
import com.xinbo.sports.service.base.UserServiceBase;
import com.xinbo.sports.service.cache.redis.BankCache;
import com.xinbo.sports.service.cache.redis.UserCache;
import com.xinbo.sports.service.common.Constant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.bo.UserCacheBo;
import com.xinbo.sports.service.io.constant.ConstData;
import com.xinbo.sports.utils.BeanConvertUtils;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.components.response.CodeInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 *
 * </p>
 *
 * @author andy
 * @since 2020/6/5
 */
@Service
public class UserBankBusinessServiceImpl implements IUserBankBusinessService {
    @Resource
    private UserBankService userBankServiceImpl;
    @Resource
    private BankListService bankListServiceImpl;
    @Resource
    private UserProfileService userProfileServiceImpl;
    @Resource
    private UserService userServiceImpl;
    @Resource
    private UserCache userCache;
    @Resource
    private BankCache bankCache;
    @Resource
    private UserServiceBase userServiceBase;

    @Override
    public List<UserBankList> userBankList(int uid) {
        List<UserBankList> resultList = new ArrayList<>();
        LambdaQueryWrapper<UserBank> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(UserBank::getUid, uid);
        List<UserBank> userBanks = userBankServiceImpl.list(wrapper);
        String username = "";
        User user = userServiceImpl.getById(uid);
        if (null != user) {
            username = user.getUsername();
        }
        var unaryOperator = userServiceBase.checkShow(Constant.BANK_ACCOUNT);
        for (UserBank po : userBanks) {
            UserBankList userBank = BeanConvertUtils.beanCopy(po, UserBankList::new);
            if (null != userBank) {
                if (null != userBank.getBankId()) {
                    BankList bank = bankListServiceImpl.getOne(new LambdaQueryWrapper<BankList>().eq(BankList::getId, po.getBankId()));
                    if (null != bank) {
                        userBank.setName(bank.getName());
                    }
                }
                userBank.setAddress(po.getMark());
                userBank.setUsername(username);
                userBank.setBankAccount(unaryOperator.apply(userBank.getBankAccount()));
                resultList.add(userBank);
            }
        }
        return resultList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bankAdd(@Valid @RequestBody BankAddReqBody reqBody) {
        if (null == reqBody) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        UserCacheBo.UserCacheInfo userCacheInfo = userCache.getUserInfoById(reqBody.getUid());
        if (null == userCacheInfo) {
            throw new BusinessException(CodeInfo.USER_NOT_EXISTS);
        }
        if (null == reqBody.getId()) {
            addUserBank(reqBody, userCacheInfo);
        } else {
            updateUserBank(reqBody);
        }
    }

    private void addUserBank(BankAddReqBody reqBody, UserCacheBo.UserCacheInfo userCacheInfo) {
        UserBank userBank = BeanConvertUtils.beanCopy(reqBody, UserBank::new);
        int userBankCount = checkUserMaxBindBank(userBank.getUid());
        if (userBankCount >= ConstData.USER_MAX_BIND_BANK) {
            throw new BusinessException(CodeInfo.BANK_BIND_OVER_LIMIT);
        }

        // ???????????????????????????(??????????????????)
        var bankInfo = bankCache.getBankCache(userBank.getBankId());
        if (null != bankInfo && !Constant.COUNTRY_THAILAND.equals(bankInfo.getCountry()) && StringUtils.isBlank(userBank.getAccountName())) {
            throw new BusinessException(CodeInfo.BANK_ACCOUNT_NAME_NOT_NULL);
        }

        LambdaQueryWrapper<UserBank> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(UserBank::getStatus, 1);
        wrapper.eq(UserBank::getUid, userBank.getUid());
        userBankCount = userBankServiceImpl.count(wrapper);
        if (userBankCount > 0) {
            userBank.setStatus(2);
        } else {
            // ???????????????-??????:1-??????(??????) 2-?????? 3-?????? 4-??????
            userBank.setStatus(1);
        }
        Integer currentTime = DateNewUtils.now();
        // ???????????????
        userBank.setMark(reqBody.getAddress());
        userBank.setCreatedAt(currentTime);
        userBank.setUpdatedAt(currentTime);
        userBank.setUsername(userCacheInfo.getUsername());
        userBankServiceImpl.save(userBank);
        userProfileServiceImpl.lambdaUpdate()
                // ?????????????????????:1-????????? 0-?????????
                .set(UserProfile::getBindBank, 1)
                // ??????sp_user_profile???realName???realName=accountName
                .set(UserProfile::getRealname, userBank.getAccountName())
                .set(UserProfile::getUpdatedAt, currentTime)
                .eq(UserProfile::getUid, userBank.getUid())
                .update();
    }

    private void updateUserBank(BankAddReqBody reqBody) {
        Integer currentTime = DateNewUtils.now();
        UserBank userBank = BeanConvertUtils.beanCopy(reqBody, UserBank::new);
        List<UserBank> list = null;
        // ??????status=1(???????????????:1-??????(??????) 2-?????? 3-?????? 4-??????)
        if (null != reqBody.getStatus() && 1 == reqBody.getStatus()) {
            // ????????????????????????
            list = getDefaultUserBankByUid(userBank.getUid(), 1);
            if (Optional.ofNullable(list).isPresent() && !list.isEmpty()) {
                list.forEach(s -> {
                    // ???????????????:1-??????(??????) 2-?????? 3-?????? 4-??????
                    s.setStatus(2);
                    s.setUpdatedAt(currentTime);
                });
            }
        }
        // ???????????????
        userBank.setMark(reqBody.getAddress());
        userBank.setUpdatedAt(currentTime);
        if (Optional.ofNullable(list).isEmpty()) {
            list = new ArrayList<>();
        }
        list.add(userBank);
        userBankServiceImpl.updateBatchById(list);

        // ??????->?????????????????????????????????????????????????????????????????????????????????3???
        int userBankCount = checkUserMaxBindBank(userBank.getUid());
        if (userBankCount > ConstData.USER_MAX_BIND_BANK) {
            throw new BusinessException(CodeInfo.BANK_BIND_OVER_LIMIT);
        }

        // ?????????????????????
        List<UserBank> defaultUserBankList = getDefaultUserBankByUid(userBank.getUid(), 1);
        if (defaultUserBankList.isEmpty()) {
            defaultUserBankList = getDefaultUserBankByUid(userBank.getUid(), 2);
            // ???????????????????????????????????????
            if (!defaultUserBankList.isEmpty()) {
                UserBank userBank1 = defaultUserBankList.get(0);
                userBank1.setUpdatedAt(currentTime);
                userBank1.setStatus(1);
                userBankServiceImpl.updateById(userBank1);
            }
        }
    }

    /**
     * ??????UID|Status?????????????????????
     *
     * @param uid    UID
     * @param status ??????:1-??????(??????) 2-?????? 3-?????? 4-??????
     * @return ???????????????
     */
    private List<UserBank> getDefaultUserBankByUid(Integer uid, Integer status) {
        return userBankServiceImpl.lambdaQuery()
                .eq(UserBank::getUid, uid)
                .eq(UserBank::getStatus, status)
                .orderByDesc(UserBank::getUpdatedAt)
                .list();
    }

    @Override
    public List<BankInfoList> bankInfoList() {
        return BeanConvertUtils.copyListProperties(bankCache.getBankListCache(), BankInfoList::new);
    }

    /**
     * get???????????????????????????
     *
     * @param uid UID
     * @return ??????
     */
    private int checkUserMaxBindBank(Integer uid) {
        LambdaQueryWrapper<UserBank> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(UserBank::getUid, uid);
        wrapper.in(UserBank::getStatus, 1, 2);
        return userBankServiceImpl.count(wrapper);
    }
}
