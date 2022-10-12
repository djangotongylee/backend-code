package com.xinbo.sports.apiend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xinbo.sports.apiend.io.dto.StartEndTime;
import com.xinbo.sports.apiend.io.dto.centeragent.*;
import com.xinbo.sports.apiend.mapper.CenterAgentMapper;
import com.xinbo.sports.apiend.service.ICenterAgentStatisticsService;
import com.xinbo.sports.apiend.service.IUserInfoService;
import com.xinbo.sports.dao.generator.po.UserProfile;
import com.xinbo.sports.dao.generator.service.UserProfileService;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.dto.BaseParams;
import com.xinbo.sports.utils.BeanConvertUtils;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * <p>
 * 代理中心业务处理接口实现类
 * </p>
 *
 * @author andy
 * @since 2020/4/22
 */
@Slf4j
@Service
public class CenterAgentStatisticsServiceImpl implements ICenterAgentStatisticsService {
    private static final String SP_COIN_DEPOSIT = "sp_coin_deposit";
    private static final String SP_COIN_DEPOSIT_PAY_COIN = "pay_coin";
    private static final String SP_COIN_WITHDRAWAL = "sp_coin_withdrawal";
    private static final String SP_COIN_WITHDRAWAL_COIN = "coin";
    @Resource
    private UserProfileService userProfileServiceImpl;
    @Resource
    private CenterAgentMapper centerAgentMapper;
    @Resource
    private IUserInfoService userInfoServiceImpl;
    @Resource
    private CenterAgentDetailsServiceImpl centerAgentDetailsServiceImpl;

    private static void shutdownExecutorService(ExecutorService executorService) {
        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    private LambdaQueryWrapper<UserProfile> whereUser(Integer uid, Integer startTime, Integer endTime) {
        LambdaQueryWrapper<UserProfile> wrapper = Wrappers.lambdaQuery();
        if (null != uid) {
            wrapper.eq(UserProfile::getSupUid1, uid);
        }
        if (null != startTime) {
            wrapper.ge(UserProfile::getCreatedAt, startTime);
        }
        if (null != endTime) {
            wrapper.le(UserProfile::getCreatedAt, endTime);
        }
        return wrapper;
    }

    /**
     * 代理中心-我的报表
     *
     * @param reqBody
     * @return
     */
    @Override
    public ReportsResBody reports(ReportsReqBody reqBody) {
        if (null == reqBody) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        BaseParams.HeaderInfo userInfo = userInfoServiceImpl.getHeadLocalData();
        if (null == userInfo) {
            throw new BusinessException(CodeInfo.ACCOUNT_NOT_EXISTS);
        }
        reqBody.setUid(userInfo.getId());
        MyTeamStatisticsResBody statisticsResBody = myTeamStatistics(reqBody);
        ReportsResBody resBody = BeanConvertUtils.beanCopy(statisticsResBody, ReportsResBody::new);
        // 历史注册人数
        int count = userProfileServiceImpl.count(whereUser(userInfo.getId(), null, null));
        resBody.setTotalCount(count);
        // 新注册人数
        resBody.setNewAddCount(statisticsResBody.getRegisterCount());
        return resBody;
    }

    public MyTeamStatisticsResBody myTeamStatistics(ReportsReqBody reqBody) {
        MyTeamStatisticsResBody resBody = new MyTeamStatisticsResBody();
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        try {
            Integer uid = reqBody.getUid();
            final Integer startTime = reqBody.getStartTime();
            final Integer endTime = reqBody.getEndTime();
            // 玩家活跃度:注册人数
            Future<Integer> registerCountFuture = executorService.submit(() -> userProfileServiceImpl.count(whereUser(uid, startTime, endTime)));
            // 玩家活跃度:首充笔数
            ReportsReqBody firstDepositCountReqBody = BeanConvertUtils.beanCopy(reqBody, ReportsReqBody::new);
            firstDepositCountReqBody.setTableName(SP_COIN_DEPOSIT);
            firstDepositCountReqBody.setColumnName(SP_COIN_DEPOSIT_PAY_COIN);
            List<Integer> depStatusList = new ArrayList<>();
            depStatusList.add(1);
            firstDepositCountReqBody.setDepStatusList(depStatusList);
            Future<Integer> firstDepositCountFuture = executorService.submit(() -> centerAgentMapper.getReportsDepositCountStatistics(firstDepositCountReqBody));

            // 玩家活跃度:首充金额
            ReportsReqBody firstDepositCoinReqBody = BeanConvertUtils.beanCopy(reqBody, ReportsReqBody::new);
            firstDepositCoinReqBody.setTableName(SP_COIN_DEPOSIT);
            firstDepositCoinReqBody.setColumnName(SP_COIN_DEPOSIT_PAY_COIN);
            firstDepositCoinReqBody.setDepStatusList(depStatusList);
            Future<BigDecimal> firstDepositCoinFuture = executorService.submit(() -> centerAgentMapper.getReportsCoinStatistics(firstDepositCoinReqBody));

            // 玩家活跃度:二充笔数
            ReportsReqBody secondDepositCountReqBody = BeanConvertUtils.beanCopy(reqBody, ReportsReqBody::new);
            secondDepositCountReqBody.setTableName(SP_COIN_DEPOSIT);
            secondDepositCountReqBody.setColumnName(SP_COIN_DEPOSIT_PAY_COIN);
            depStatusList = new ArrayList<>();
            depStatusList.add(2);
            secondDepositCountReqBody.setDepStatusList(depStatusList);
            Future<Integer> secondDepositCountFuture = executorService.submit(() -> centerAgentMapper.getReportsDepositCountStatistics(secondDepositCountReqBody));

            // 玩家活跃度:二充金额
            ReportsReqBody secondDepositCoinReqBody = BeanConvertUtils.beanCopy(reqBody, ReportsReqBody::new);
            secondDepositCoinReqBody.setTableName(SP_COIN_DEPOSIT);
            secondDepositCoinReqBody.setColumnName(SP_COIN_DEPOSIT_PAY_COIN);
            secondDepositCoinReqBody.setDepStatusList(depStatusList);
            Future<BigDecimal> secondDepositCoinFuture = executorService.submit(() -> centerAgentMapper.getReportsCoinStatistics(secondDepositCoinReqBody));

            // 充值与提现:充值总额
            ReportsReqBody totalDepositCoinReqBody = BeanConvertUtils.beanCopy(reqBody, ReportsReqBody::new);
            totalDepositCoinReqBody.setTableName(SP_COIN_DEPOSIT);
            totalDepositCoinReqBody.setColumnName(SP_COIN_DEPOSIT_PAY_COIN);
            List<Integer> statusList = new ArrayList<>();
            statusList.add(1);
            statusList.add(2);
            statusList.add(9);
            totalDepositCoinReqBody.setStatusList(statusList);
            Future<BigDecimal> totalDepositCoinFuture = executorService.submit(() -> centerAgentMapper.getReportsCoinStatistics(totalDepositCoinReqBody));

            // 充值与提现:提现总额
            ReportsReqBody totalWithdrawalCoinReqBody = BeanConvertUtils.beanCopy(reqBody, ReportsReqBody::new);
            totalWithdrawalCoinReqBody.setTableName(SP_COIN_WITHDRAWAL);
            totalWithdrawalCoinReqBody.setColumnName(SP_COIN_WITHDRAWAL_COIN);
            statusList = new ArrayList<>();
            // 状态：0-申请中 1-成功 2-失败
            statusList.add(1);
            totalWithdrawalCoinReqBody.setStatusList(statusList);
            Future<BigDecimal> totalWithdrawalCoinFuture = executorService.submit(() -> centerAgentMapper.getReportsCoinStatistics(totalWithdrawalCoinReqBody));


            // 大类型:1-邀请奖励 2-佣金奖励
            // 奖金与佣金:邀请奖励
            RewardsCommissionDetailsReqBody build1 = RewardsCommissionDetailsReqBody.builder().category(1).username(null).build();
            build1.setStartTime(startTime);
            build1.setEndTime(endTime);
            Future<RewardsCommissionDetailsStatisticsResBody> rewardsCoinFuture = executorService.submit(() -> centerAgentDetailsServiceImpl.rewardsCommissionDetailsStatistics(build1, uid));
            // 奖金与佣金:佣金奖励
            RewardsCommissionDetailsReqBody build2 = RewardsCommissionDetailsReqBody.builder().category(2).username(null).build();
            build2.setStartTime(startTime);
            build2.setEndTime(endTime);
            Future<RewardsCommissionDetailsStatisticsResBody> commissionCoinFuture = executorService.submit(() -> centerAgentDetailsServiceImpl.rewardsCommissionDetailsStatistics(build2, uid));

            while (true) {
                if (registerCountFuture.isDone() && firstDepositCountFuture.isDone() && firstDepositCoinFuture.isDone()
                        && secondDepositCountFuture.isDone() && secondDepositCoinFuture.isDone() && totalDepositCoinFuture.isDone() && totalWithdrawalCoinFuture.isDone()
                        && commissionCoinFuture.isDone() && rewardsCoinFuture.isDone()) {
                    resBody.setRegisterCount(registerCountFuture.get());
                    resBody.setFirstDepositCount(firstDepositCountFuture.get());
                    resBody.setFirstDepositCoin(firstDepositCoinFuture.get());
                    resBody.setSecondDepositCount(secondDepositCountFuture.get());
                    resBody.setSecondDepositCoin(secondDepositCoinFuture.get());
                    resBody.setTotalDepositCoin(totalDepositCoinFuture.get());
                    resBody.setTotalWithdrawalCoin(totalWithdrawalCoinFuture.get());
                    BigDecimal rewardsCoin = BigDecimal.ZERO;
                    if (null != rewardsCoinFuture.get()) {
                        rewardsCoin = rewardsCoinFuture.get().getTotalRewardsCoin();
                    }
                    BigDecimal commissionCoin = BigDecimal.ZERO;
                    if (null != commissionCoinFuture.get()) {
                        commissionCoin = commissionCoinFuture.get().getTotalRewardsCoin();
                    }
                    resBody.setRewardsCoin(rewardsCoin);
                    resBody.setCommissionCoin(commissionCoin);
                    resBody.setTotalIncome(rewardsCoin.add(commissionCoin));
                    break;
                }
            }
        } catch (InterruptedException e) {
            log.error("Interrupted:" + e);
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            log.error(CodeInfo.API_CENTER_AGENT_REPORTS_ERROR + ":" + e);
        } finally {
            shutdownExecutorService(executorService);
            log.info("executorService.isShutdown()" + executorService.isShutdown());
        }
        return resBody;
    }

    @Override
    public MyTeamStatisticsResBody myTeamStatisticsHisOrNew(StartEndTime reqBody) {
        if (null == reqBody) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        BaseParams.HeaderInfo userInfo = userInfoServiceImpl.getHeadLocalData();
        if (null == userInfo) {
            throw new BusinessException(CodeInfo.ACCOUNT_NOT_EXISTS);
        }
        //无用户信息，返回初始化的值
        if (userInfo.getId() == 0) {
            return new MyTeamStatisticsResBody();
        }
        ReportsReqBody reportsReqBody = BeanConvertUtils.beanCopy(reqBody, ReportsReqBody::new);
        reportsReqBody.setUid(userInfo.getId());
        return myTeamStatistics(reportsReqBody);
    }
}
