package com.xinbo.sports.apiend.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinbo.sports.apiend.io.dto.centeragent.DepositOrWithdrawalReqBody;
import com.xinbo.sports.apiend.io.dto.centeragent.DepositOrWithdrawalResBody;
import com.xinbo.sports.apiend.service.ICenterAgentDepositOrWithdrawalService;
import com.xinbo.sports.apiend.service.IUserInfoService;
import com.xinbo.sports.dao.generator.po.*;
import com.xinbo.sports.dao.generator.service.CoinDepositService;
import com.xinbo.sports.dao.generator.service.CoinWithdrawalService;
import com.xinbo.sports.dao.generator.service.PayOfflineService;
import com.xinbo.sports.dao.generator.service.PayOnlineService;
import com.xinbo.sports.service.cache.redis.BankCache;
import com.xinbo.sports.service.cache.redis.UserCache;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.bo.UserCacheBo;
import com.xinbo.sports.utils.BankUtils;
import com.xinbo.sports.utils.BeanConvertUtils;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 代理中心->充值提现
 * </p>
 *
 * @author andy
 * @since 2020/10/20
 */
@Slf4j
@Service
public class CenterAgentDepositOrWithdrawalServiceImpl implements ICenterAgentDepositOrWithdrawalService {
    @Resource
    private CoinDepositService coinDepositServiceImpl;
    @Resource
    private CoinWithdrawalService coinWithdrawalServiceImpl;
    @Resource
    private UserCache userCache;
    @Resource
    private IUserInfoService userInfoServiceImpl;
    @Resource
    private PayOnlineService payOnlineServiceImpl;
    @Resource
    private PayOfflineService payOfflineServiceImpl;
    @Resource
    private BankCache bankCache;


    /**
     * 代理中心->充值提现
     *
     * @param reqBody reqBody
     * @return 下级分页列表(六个层级)
     */
    @Override
    public ResPage<DepositOrWithdrawalResBody> depositOrWithdrawal(ReqPage<DepositOrWithdrawalReqBody> reqBody) {
        if (null == reqBody || null == reqBody.getData() || null == reqBody.getData().getCategory()) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        var reqBodyData = reqBody.getData();
        var currentLoginUser = userInfoServiceImpl.getHeadLocalData();
        List<Integer> uidList = userCache.getSupUid6ListByUid(currentLoginUser.getId());

        Integer category = reqBodyData.getCategory();
        Integer startTime = reqBodyData.getStartTime();
        Integer endTime = reqBodyData.getEndTime();
        String username = reqBodyData.getUsername();
        // 按username查询
        if (StringUtils.isNotBlank(username)) {
            UserCacheBo.UserCacheInfo userCacheInfo = userCache.getUserInfoByUserName(username);
            if (null == userCacheInfo) {
                return new ResPage<>();
            }
            uidList = uidList.stream().filter(s -> s.toString().equals(userCacheInfo.getUid().toString())).collect(Collectors.toList());
        }
        if (uidList.isEmpty()) {
            return new ResPage<>();
        }
        // 类型:1-充值 2-提现
        if (1 == category) {
            return pageCoinDeposit(reqBody.getPage(), uidList, startTime, endTime);
        }
        return pageCoinWithdrawal(reqBody.getPage(), uidList, startTime, endTime);
    }

    /**
     * get充值记录
     *
     * @param page      page
     * @param uidList   下级uid
     * @param startTime startTime
     * @param endTime   endTime
     * @return 充值记录
     */
    private ResPage<DepositOrWithdrawalResBody> pageCoinDeposit(Page<CoinDeposit> page, List<Integer> uidList, Integer startTime, Integer endTime) {
        LambdaQueryWrapper<CoinDeposit> wrapper = Wrappers.lambdaQuery();
        wrapper.in(CoinDeposit::getUid, uidList);
        wrapper.ge(CoinDeposit::getCreatedAt, startTime);
        wrapper.le(CoinDeposit::getCreatedAt, endTime);
        wrapper.in(CoinDeposit::getStatus, 1, 2, 9);
        Page<CoinDeposit> tmpPage = coinDepositServiceImpl.page(page, wrapper);
        List<CoinDeposit> depositList = tmpPage.getRecords();
        List<DepositOrWithdrawalResBody> list = new ArrayList<>();
        for (CoinDeposit o : depositList) {
            DepositOrWithdrawalResBody build = DepositOrWithdrawalResBody.builder()
                    .coin(o.getPayCoin())
                    .payCoin(o.getPayCoin())
                    .category(o.getCategory())
                    .bankName(getPayName(o.getPayType(), o.getPayRefer()))
                    .createdAt(o.getCreatedAt())
                    .uid(o.getUid())
                    .username(userCache.getUserInfoById(o.getUid()).getUsername())
                    .status(o.getStatus())
                    .build();

            list.add(build);
        }
        Page<DepositOrWithdrawalResBody> resPage = BeanConvertUtils.copyPageProperties(tmpPage, DepositOrWithdrawalResBody::new);
        ResPage<DepositOrWithdrawalResBody> resBody = ResPage.get(resPage);
        resBody.setList(list);
        return resBody;
    }

    /**
     * get提现记录
     *
     * @param page      page
     * @param uidList   下级uid
     * @param startTime startTime
     * @param endTime   endTime
     * @return 提现记录
     */
    private ResPage<DepositOrWithdrawalResBody> pageCoinWithdrawal(Page<CoinWithdrawal> page, List<Integer> uidList, Integer startTime, Integer endTime) {
        LambdaQueryWrapper<CoinWithdrawal> wrapper = Wrappers.lambdaQuery();
        wrapper.in(CoinWithdrawal::getUid, uidList);
        wrapper.ge(CoinWithdrawal::getCreatedAt, startTime);
        wrapper.le(CoinWithdrawal::getCreatedAt, endTime);
        wrapper.eq(CoinWithdrawal::getStatus, 1);
        Page<CoinWithdrawal> tmpPage = coinWithdrawalServiceImpl.page(page, wrapper);
        List<CoinWithdrawal> l = tmpPage.getRecords();
        List<DepositOrWithdrawalResBody> list = new ArrayList<>();
        for (CoinWithdrawal o : l) {
            UserBank bankInfo = getBankInfo(o.getBankInfo());
            String bankName = null;
            String bankAccount = null;
            if (Optional.ofNullable(bankInfo).isPresent()) {
                bankName = null != bankCache.getBankCache(bankInfo.getBankId()) ? bankCache.getBankCache(bankInfo.getBankId()).getName() : null;
                bankAccount = bankInfo.getBankAccount();
                // 屏蔽卡号
                bankAccount = BankUtils.bankAccountFilter(bankAccount);
            }
            DepositOrWithdrawalResBody build = DepositOrWithdrawalResBody.builder()
                    .coin(o.getCoin())
                    .bankName(bankName)
                    .createdAt(o.getCreatedAt())
                    .uid(o.getUid())
                    .bankAccount(bankAccount)
                    .username(userCache.getUserInfoById(o.getUid()).getUsername())
                    .status(o.getStatus())
                    .build();
            list.add(build);
        }
        Page<DepositOrWithdrawalResBody> resPage = BeanConvertUtils.copyPageProperties(tmpPage, DepositOrWithdrawalResBody::new);
        ResPage<DepositOrWithdrawalResBody> resBody = ResPage.get(resPage);
        resBody.setList(list);
        return resBody;
    }

    /**
     * 获取支付名称
     *
     * @param payType    0-离线 1-在线
     * @param payReferId 关联ID
     * @return 支付名称
     */
    private String getPayName(Integer payType, Integer payReferId) {
        String payName = null;
        if (0 == payType) {
            PayOffline one = payOfflineServiceImpl.getById(payReferId);
            if (null != one) {
                payName = one.getBankName();
            }
        }
        if (1 == payType) {
            PayOnline one = payOnlineServiceImpl.getById(payReferId);
            if (null != one) {
                payName = one.getPayName();
            }
        }
        return payName;
    }

    private UserBank getBankInfo(String bankInfo) {
        if (StringUtils.isNotBlank(bankInfo)) {
            return JSON.parseObject(bankInfo, UserBank.class);
        }
        return null;
    }


}
