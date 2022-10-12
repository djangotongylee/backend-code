package com.xinbo.sports.apiend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinbo.sports.apiend.io.dto.centeragent.*;
import com.xinbo.sports.apiend.io.dto.mapper.RewardsInviteReq;
import com.xinbo.sports.apiend.io.dto.mapper.RewardsInviteRes;
import com.xinbo.sports.apiend.mapper.CenterAgentMapper;
import com.xinbo.sports.apiend.service.ICenterAgentService;
import com.xinbo.sports.apiend.service.IUserInfoService;
import com.xinbo.sports.dao.generator.po.*;
import com.xinbo.sports.dao.generator.service.*;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.service.cache.redis.UserCache;
import com.xinbo.sports.service.cache.redis.UserChannelRelCache;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.bo.UserCacheBo;
import com.xinbo.sports.service.io.dto.BaseParams;
import com.xinbo.sports.utils.BeanConvertUtils;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;


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
public class CenterAgentServiceImpl implements ICenterAgentService {
    protected static final String SP_COIN_DEPOSIT = "sp_coin_deposit";
    protected static final String SP_COIN_DEPOSIT_PAY_COIN = "pay_coin";
    protected static final String SP_COIN_WITHDRAWAL = "sp_coin_withdrawal";
    protected static final String SP_COIN_WITHDRAWAL_COIN = "coin";

    @Resource
    protected CoinDepositService coinDepositServiceImpl;
    @Resource
    protected CenterAgentMapper centerAgentMapper;
    @Resource
    protected IUserInfoService userInfoServiceImpl;
    @Resource
    protected ConfigCache configCache;
    @Resource
    protected UserCache userCache;
    @Resource
    protected CoinWithdrawalService coinWithdrawalServiceImpl;
    @Resource
    private UserService userServiceImpl;
    @Resource
    protected CoinCommissionService coinCommissionServiceImpl;
    @Resource
    protected UserLoginLogService userLoginLogServiceImpl;
    @Resource
    protected UserProfileService userProfileServiceImpl;
    @Resource
    protected UserChannelRelCache userChannelRelCache;


    private static void shutdownExecutorService(ExecutorService executorService) {
        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    protected static <P> void checkPrams(P e) {
        if (null == e) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
    }

    protected static LambdaQueryWrapper<CoinDeposit> wherePageCoinDeposit(List<Integer> uidList, Integer startTime, Integer endTime) {
        LambdaQueryWrapper<CoinDeposit> where = Wrappers.lambdaQuery();
        where.in(CoinDeposit::getStatus, 1, 2, 9);
        where.in(nonNull(uidList), CoinDeposit::getUid, uidList);
        if (null != startTime) {
            where.ge(CoinDeposit::getCreatedAt, startTime);
        }
        if (null != endTime) {
            where.le(CoinDeposit::getCreatedAt, endTime);
        }
        return where;
    }

    private static LambdaQueryWrapper<CoinWithdrawal> wherePageCoinWithdrawal(List<Integer> uidList, Integer startTime, Integer endTime) {
        LambdaQueryWrapper<CoinWithdrawal> where = Wrappers.lambdaQuery();
        where.eq(CoinWithdrawal::getStatus, 1);
        where.in(CoinWithdrawal::getUid, uidList);
        if (null != startTime) {
            where.ge(CoinWithdrawal::getCreatedAt, startTime);
        }
        if (null != endTime) {
            where.le(CoinWithdrawal::getCreatedAt, endTime);
        }
        return where;
    }

    /**
     * 直推统计规则:统计sup_uid_1=当前用户的数据
     * <p>
     * 直推人数=代理+玩家
     * 代理人数role=1
     * 玩家人数role=0
     */
    @Override
    public SubordinateListResBody subordinateList(ReqPage<SubordinateListReqBody> reqBody) {
        SubordinateListResBody subordinateListResBody = new SubordinateListResBody();
        ResPage<SubordinateListResBody.SubordinateList> resPage = new ResPage<>();
        checkPrams(reqBody);
        BaseParams.HeaderInfo userInfo = userInfoServiceImpl.getHeadLocalData();
        checkUserIsNotExists(userInfo);
        // 线程池对象
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        try {
            String username = null;
            Integer role = null;
            if (null != reqBody.getData()) {
                username = reqBody.getData().getUsername();
                role = reqBody.getData().getRole();
            }

            SubordinateListReqBody build = SubordinateListReqBody.builder()
                    .uid(userInfo.getId())
                    .username(username)
                    .role(role)
                    .build();

            Page<User> userPage = centerAgentMapper.subordinateList(reqBody.getPage(), build);
            List<List<User>> userGroup = getSubordinateList(build);
            List<User> userList = new ArrayList<>();
            List<Integer> uidCollect = new ArrayList<>();
            if (!userGroup.isEmpty()) {
                userList = userGroup.get(1);
                uidCollect = userGroup.get(0).stream()
                        .map(User::getId).collect(Collectors.toList());
            }
            List<SubordinateListResBody.SubordinateList> list = new ArrayList<>();
            if (Optional.ofNullable(userPage).isEmpty()
                    || Optional.ofNullable(userPage.getRecords()).isEmpty()
                    || userPage.getRecords().isEmpty() || userList.isEmpty()) {
                subordinateListResBody.setOfflineCount(0);
                subordinateListResBody.setActiveCount(0);
                subordinateListResBody.setSubordinateList(resPage);
                return subordinateListResBody;
            }
            List<Integer> onlineList = Optional.ofNullable(userChannelRelCache.getSubordinateListChannelUids(uidCollect)).isPresent() ? userChannelRelCache.getSubordinateListChannelUids(uidCollect) : new ArrayList<>();
            if (Optional.ofNullable(reqBody.getData()).isPresent() && Optional.ofNullable(reqBody.getData().getStatus()).isPresent() && !reqBody.getData().getStatus().isEmpty()) {
                List<User> users = reqBody.getData().getStatus().equals("0") ? userList.stream().filter(u -> !onlineList.contains(u.getId())).collect(Collectors.toList())
                        : userList.stream().filter(u -> onlineList.contains(u.getId())).collect(Collectors.toList());
                List<User> collect = users.stream()
                        .skip(userPage.getSize() * (userPage.getCurrent() - 1))
                        .limit(userPage.getSize()).collect(Collectors.toList());
                userPage.setRecords(collect);
                userPage.setTotal(userPage.getTotal() - (userList.size() - users.size()));

            }
            for (User user : userPage.getRecords()) {
                if (null != user) {
                    SubordinateListResBody.SubordinateList resBody = new SubordinateListResBody.SubordinateList();
                    resBody.setCoin(user.getCoin());
                    resBody.setCreatedAt(user.getCreatedAt());
                    resBody.setRole(user.getRole());
                    resBody.setUsername(user.getUsername());
                    resBody.setUid(user.getId());
                    resBody.setStatus(!onlineList.contains(user.getId()) ? 0 : 1);
                    // 直推人数
                    Future<Integer> ztCountFuture = executorService.submit(() -> centerAgentMapper.getSubordinateStatisticsZT(user.getId(), null));
                    // 代理人数
                    Future<Integer> dlCountFuture = executorService.submit(() -> centerAgentMapper.getSubordinateStatisticsZT(user.getId(), 1));
                    // 玩家人数
                    Future<Integer> wjCountFuture = executorService.submit(() -> centerAgentMapper.getSubordinateStatisticsZT(user.getId(), 0));
                    while (true) {
                        if (ztCountFuture.isDone() && dlCountFuture.isDone() && wjCountFuture.isDone()) {
                            resBody.setZtCount(ztCountFuture.get());
                            resBody.setDlCount(dlCountFuture.get());
                            resBody.setWjCount(wjCountFuture.get());
                            break;
                        }
                    }
                    list.add(resBody);
                }
            }
            subordinateListResBody.setOfflineCount(Math.max(userList.size() - onlineList.size(), 0));
            subordinateListResBody.setActiveCount(onlineList.size());
            Page<SubordinateListResBody.SubordinateList> tmpPage = BeanConvertUtils.copyPageProperties(userPage, SubordinateListResBody.SubordinateList::new);
            resPage = ResPage.get(tmpPage);
            resPage.setList(sorted(reqBody, list));
            subordinateListResBody.setSubordinateList(resPage);
        } catch (InterruptedException e) {
            log.warn("Interrupted!" + e);
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            log.error(CodeInfo.API_CENTER_AGENT_SUBORDINATE_LIST_ERROR + ":" + e);
        } finally {
            shutdownExecutorService(executorService);
            log.info("subordinateList:isShutdown={}", executorService.isShutdown());
        }
        return subordinateListResBody;
    }

    private List<SubordinateListResBody.SubordinateList> sorted(ReqPage<SubordinateListReqBody> reqBody, List<SubordinateListResBody.SubordinateList> list) {
        String sortKey = reqBody.getSortKey();
        String[] sortField = reqBody.getSortField();
        if (StringUtils.isNotBlank(sortKey) && Optional.ofNullable(sortField).isPresent()
                && sortField.length > 0) {
            if (sortField[0].equals("createdAt")) {
                return sortKey.equals("ASC") ? list.stream().sorted(Comparator.comparing(SubordinateListResBody.SubordinateList::getCreatedAt).thenComparing(SubordinateListResBody.SubordinateList::getUid, Comparator.reverseOrder())).collect(Collectors.toList()) : list.stream().sorted(Comparator.comparing(SubordinateListResBody.SubordinateList::getCreatedAt).reversed().thenComparing(SubordinateListResBody.SubordinateList::getUid, Comparator.reverseOrder())).collect(Collectors.toList());
            } else if (sortField[0].equals("username")) {
                return sortKey.equals("ASC") ? list.stream().sorted(Comparator.comparing(SubordinateListResBody.SubordinateList::getUsername).thenComparing(SubordinateListResBody.SubordinateList::getCreatedAt, Comparator.reverseOrder())).collect(Collectors.toList()) : list.stream().sorted(Comparator.comparing(SubordinateListResBody.SubordinateList::getUsername).reversed().thenComparing(SubordinateListResBody.SubordinateList::getCreatedAt, Comparator.reverseOrder())).collect(Collectors.toList());
            }
        }
        return list.stream().sorted(Comparator.comparing(SubordinateListResBody.SubordinateList::getCreatedAt).thenComparing(SubordinateListResBody.SubordinateList::getUid, Comparator.reverseOrder())).collect(Collectors.toList());
    }


    private List<List<User>> getSubordinateList(SubordinateListReqBody build) {
        List<Integer> uidCollect = userProfileServiceImpl.list(new QueryWrapper<UserProfile>().eq(nonNull(build.getUid()), "sup_uid_1", build.getUid())).stream().map(UserProfile::getUid).collect(Collectors.toList());
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        List<List<User>> userGroup = new ArrayList<>();
        if (!uidCollect.isEmpty()) {
            wrapper.in("id", uidCollect);
            wrapper.in(build.getRole() == null, "role", 0, 1);
            List<User> list = userServiceImpl.list(wrapper);
            userGroup.add(list);

            wrapper.eq(nonNull(build.getRole()), "role", build.getRole());
            wrapper.eq(nonNull(build.getUsername()), "username", build.getUsername());
            wrapper.eq(nonNull(build.getStartTime()), "created_at", build.getStartTime());
            wrapper.eq(nonNull(build.getEndTime()), "created_at", build.getEndTime());
            List<User> list1 = userServiceImpl.list(wrapper);
            userGroup.add(list1);
        }
        return userGroup;
    }

    /**
     * 直推统计规则:统计sup_uid_1=当前用户的数据
     * 团队统计规则:统计sup_uid_1至sup_uid_6 = 当前用户的数据
     * <p>
     * 直推人数=代理+玩家
     * 代理人数role=1
     * 玩家人数role=0
     */
    @Override
    public SubordinateStatisticsResBody subordinateStatistics() {
        SubordinateStatisticsResBody resBody = new SubordinateStatisticsResBody();
        ExecutorService executorService = Executors.newCachedThreadPool();
        try {
            // 当前登录用户信息
            BaseParams.HeaderInfo userInfo = userInfoServiceImpl.getHeadLocalData();
            checkUserIsNotExists(userInfo);
            /**
             * 直推统计规则:统计sup_uid_1=当前用户的数据
             */
            // 直推人数
            Future<Integer> ztCountFuture = executorService.submit(() -> centerAgentMapper.getSubordinateStatisticsZT(userInfo.getId(), null));
            // 代理人数
            Future<Integer> dlCountFuture = executorService.submit(() -> centerAgentMapper.getSubordinateStatisticsZT(userInfo.getId(), 1));
            // 玩家人数
            Future<Integer> wjCountFuture = executorService.submit(() -> centerAgentMapper.getSubordinateStatisticsZT(userInfo.getId(), 0));

            /**
             * 团队统计规则:统计sup_uid_1至sup_uid_6 = 当前用户的数据
             */
            // 直推人数
            Future<Integer> tdZtCountFuture = executorService.submit(() -> centerAgentMapper.getSubordinateStatisticsTD(userInfo.getId(), null));
            // 代理人数
            Future<Integer> tdDlCountFuture = executorService.submit(() -> centerAgentMapper.getSubordinateStatisticsTD(userInfo.getId(), 1));
            // 玩家人数
            Future<Integer> tdWjCountFuture = executorService.submit(() -> centerAgentMapper.getSubordinateStatisticsTD(userInfo.getId(), 0));
            while (true) {
                if (ztCountFuture.isDone() && dlCountFuture.isDone() && wjCountFuture.isDone()
                        && tdZtCountFuture.isDone() && tdDlCountFuture.isDone() && tdWjCountFuture.isDone()) {
                    resBody.setZtCount(ztCountFuture.get());
                    resBody.setDlCount(dlCountFuture.get());
                    resBody.setWjCount(wjCountFuture.get());
                    resBody.setTdZtCount(tdZtCountFuture.get());
                    resBody.setTdDlCount(tdDlCountFuture.get());
                    resBody.setTdWjCount(tdWjCountFuture.get());
                    break;
                }
            }
        } catch (InterruptedException e) {
            log.warn("Interrupted!" + e);
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            log.error(CodeInfo.API_CENTER_AGENT_SUBORDINATE_STATISTICS_ERROR + ":" + e);
        } finally {
            shutdownExecutorService(executorService);
            log.info("subordinateStatistics:isShutdown={}", executorService.isShutdown());
        }
        return resBody;
    }

    @Override
    public SubordinateListZtResBody subordinateListZT(ReqPage<SubordinateListZtReqBody> reqBody) {
        if (null == reqBody) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        SubordinateListReqBody data = SubordinateListReqBody.builder().build();
        if (null != reqBody.getData()) {
            data = BeanConvertUtils.beanCopy(reqBody.getData(), SubordinateListReqBody::new);
        }
        Integer uid = data.getUid();
        if (null == uid || uid == 0) {
            uid = -1;
        }
        User user = userServiceImpl.lambdaQuery().eq(User::getId, uid).one();
        checkUserIsNotExists(user);
        UserInfoDto userInfoDto = BeanConvertUtils.beanCopy(user, UserInfoDto::new);
        if (null != userInfoDto) {
            userInfoDto.setAvatar(userInfoDto.getAvatar().startsWith("http")? userInfoDto.getAvatar():configCache.getStaticServer() + userInfoDto.getAvatar());
        }
        data.setUid(uid);
        Page<User> page = centerAgentMapper.subordinateList(reqBody.getPage(), data);
        Page<SubordinateListZt> tmpPage = BeanConvertUtils.copyPageProperties(page, SubordinateListZt::new);
        ResPage<SubordinateListZt> resPage = ResPage.get(tmpPage);
        SubordinateListZtResBody resBody = new SubordinateListZtResBody();
        resBody.setUserInfo(userInfoDto);
        resBody.setListZT(resPage);
        return resBody;
    }

    @Override
    public ResPage<DepositWithdrawalDetailsResBody> depositWithdrawalDetails(@Valid ReqPage<DepositWithdrawalDetailsReqBody> reqBody) {
        checkPrams(reqBody);
        DepositWithdrawalDetailsReqBody reqBodyData = reqBody.getData();
        BaseParams.HeaderInfo currentLoginUser = userInfoServiceImpl.getHeadLocalData();
        List<Integer> uidList = userCache.getSubordinateUidListByUid(currentLoginUser.getId());
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
        // 类型:1-存款 2-提款
        if (1 == category) {
            return pageCoinDeposit(reqBody.getPage(), uidList, startTime, endTime);
        }
        return pageCoinWithdrawal(reqBody.getPage(), uidList, startTime, endTime);
    }

    @Override
    public ResPage<SubordinateInfo> depositWithdrawalUserDetails(@Valid ReqPage<DepositWithdrawalUserDetailsReqBody> reqBody) {
        checkPrams(reqBody);
        Integer uid = userInfoServiceImpl.getHeadLocalData().getId();
        if (null != reqBody.getData() && null != reqBody.getData().getUid()) {
            uid = reqBody.getData().getUid();
        }
        UserCacheBo.UserCacheInfo userCacheInfo = userCache.getUserInfoById(uid);
        checkUserIsNotExists(userCacheInfo);
        // 下级成员列表
        Page<User> page = centerAgentMapper.subordinateList(reqBody.getPage(), SubordinateListReqBody.builder().uid(uid).build());
        List<User> tmpPage = page.getRecords();
        List<SubordinateInfo> list = new ArrayList<>();
        for (User user : tmpPage) {
            SubordinateInfo build = SubordinateInfo.builder()
                    .createdAt(user.getCreatedAt())
                    .username(user.getUsername())
                    .avatar(userCacheInfo.getAvatar())
                    .levelId(user.getLevelId())
                    .count(userCache.getSubordinateUidListByUid(user.getId()).size())
                    .uid(user.getId())
                    .build();
            list.add(build);
        }
        Page<SubordinateInfo> resPage = BeanConvertUtils.copyPageProperties(page, SubordinateInfo::new);
        ResPage<SubordinateInfo> resBody = ResPage.get(resPage);
        resBody.setList(list);
        return resBody;

    }

    @Override
    public DepositWithdrawalUserDetailsResBody depositWithdrawalUserDetailsStatistics(@Valid DepositWithdrawalUserDetailsReqBody reqBody) {
        checkPrams(reqBody);
        UserCacheBo.UserCacheInfo userCacheInfo = userCache.getUserInfoById(reqBody.getUid());
        checkUserIsNotExists(userCacheInfo);
        Integer uid = userCacheInfo.getUid();
        // 充值与提现总额
        DepositWithdrawalDto depositWithdrawalDto = buildDepositWithdrawalCoin(uid);
        return DepositWithdrawalUserDetailsResBody.builder()
                .username(userCacheInfo.getUsername())
                .avatar(userCacheInfo.getAvatar())
                .promoCode(userCacheInfo.getPromoCode())
                .levelId(userCacheInfo.getLevelId())
                .totalDepositCoin(depositWithdrawalDto.getTotalDepositCoin())
                .totalWithdrawalCoin(depositWithdrawalDto.getTotalWithdrawalCoin())
                .build();
    }

    @Override
    public ResPage<PlayerActivityDetailsResBody> playerActivityDetails(@Valid ReqPage<PlayerActivityDetailsReqBody> reqBody) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResPage<RewardsCommissionDetailsResBody> rewardsCommissionDetails(@Valid ReqPage<RewardsCommissionDetailsReqBody> reqBody) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RewardsCommissionDetailsStatisticsResBody rewardsCommissionDetailsStatistics(RewardsCommissionDetailsReqBody reqBody, Integer uid) {
        RewardsCommissionDetailsStatisticsResBody result = RewardsCommissionDetailsStatisticsResBody.builder()
                .totalCoin(BigDecimal.ZERO)
                .totalRewardsCoin(BigDecimal.ZERO)
                .build();
        checkPrams(reqBody);
        Integer startTime = reqBody.getStartTime();
        Integer endTime = reqBody.getEndTime();
        // 大类型:1-邀请奖励 2-佣金奖励
        switch (reqBody.getCategory()) {
            case 1:
                // 类型:0-被邀请奖金1-邀请奖金 2-充值返利
                result = coinRewardsInviteStatistics(reqBody.getSubCategory(), uid, startTime, endTime, reqBody.getUsername());
                break;
            case 2:
                // 类型:0-流水佣金 1-活跃会员佣金 2-满额人头彩金
                result = coinCommissionStatistics(reqBody.getSubCategory(), uid, startTime, endTime);
                break;
            default:
                break;
        }
        return result;
    }

    @Override
    public DepositWithdrawalDetailsStatisticsResBody depositWithdrawalDetailsStatistics(DepositWithdrawalDetailsReqBody reqBody) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DepositWithdrawalDetailsStatisticsResBody playerActivityDetailsStatistics(PlayerActivityDetailsReqBody reqBody) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResPage<SubordinateInfo> rewardsCommissionActiveDetails(ReqPage<RewardsCommissionDetailsActiveReqBody> reqBody) {
        throw new UnsupportedOperationException();
    }


    private ResPage<DepositWithdrawalDetailsResBody> pageCoinDeposit(Page<CoinDeposit> page, List<Integer> uidList, Integer startTime, Integer endTime) {
        Page<CoinDeposit> tmpPage = coinDepositServiceImpl.page(page, wherePageCoinDeposit(uidList, startTime, endTime));
        List<CoinDeposit> depositList = tmpPage.getRecords();
        List<DepositWithdrawalDetailsResBody> list = depositList.stream().map(deposit -> DepositWithdrawalDetailsResBody.builder()
                .payCoin(deposit.getPayCoin())
                .createdAt(deposit.getCreatedAt())
                .status(deposit.getStatus())
                .uid(deposit.getUid())
                .username(userCache.getUserInfoById(deposit.getUid()).getUsername())
                .build()).collect(Collectors.toList());
        Page<DepositWithdrawalDetailsResBody> resPage = BeanConvertUtils.copyPageProperties(tmpPage, DepositWithdrawalDetailsResBody::new);
        ResPage<DepositWithdrawalDetailsResBody> resBody = ResPage.get(resPage);
        resBody.setList(list);
        return resBody;
    }

    private ResPage<DepositWithdrawalDetailsResBody> pageCoinWithdrawal(Page<CoinWithdrawal> page, List<Integer> uidList, Integer startTime, Integer endTime) {
        Page<CoinWithdrawal> tmpPage = coinWithdrawalServiceImpl.page(page, wherePageCoinWithdrawal(uidList, startTime, endTime));
        List<CoinWithdrawal> depositList = tmpPage.getRecords();
        List<DepositWithdrawalDetailsResBody> list = depositList.stream().map(deposit -> DepositWithdrawalDetailsResBody.builder()
                .coin(deposit.getCoin())
                .createdAt(deposit.getCreatedAt())
                .status(deposit.getStatus())
                .uid(deposit.getUid())
                .username(userCache.getUserInfoById(deposit.getUid()).getUsername())
                .build()).collect(Collectors.toList());
        Page<DepositWithdrawalDetailsResBody> resPage = BeanConvertUtils.copyPageProperties(tmpPage, DepositWithdrawalDetailsResBody::new);
        ResPage<DepositWithdrawalDetailsResBody> resBody = ResPage.get(resPage);
        resBody.setList(list);
        return resBody;
    }

    private DepositWithdrawalDto buildDepositWithdrawalCoin(Integer uid) {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        // 充值总额
        ReportsReqBody totalDepositCoinReqBody = ReportsReqBody.builder().uid(uid).build();
        totalDepositCoinReqBody.setTableName(SP_COIN_DEPOSIT);
        totalDepositCoinReqBody.setColumnName(SP_COIN_DEPOSIT_PAY_COIN);
        List<Integer> depStatusList = new ArrayList<>();
        depStatusList.add(1);
        depStatusList.add(2);
        depStatusList.add(9);
        totalDepositCoinReqBody.setDepStatusList(depStatusList);
        Future<BigDecimal> totalDepositCoinFuture = executorService.submit(() -> centerAgentMapper.getReportsCoinStatistics(totalDepositCoinReqBody));
        // 提现总额
        ReportsReqBody totalWithdrawalCoinReqBody = ReportsReqBody.builder().uid(uid).build();
        totalWithdrawalCoinReqBody.setTableName(SP_COIN_WITHDRAWAL);
        totalWithdrawalCoinReqBody.setColumnName(SP_COIN_WITHDRAWAL_COIN);
        List<Integer> statusList = new ArrayList<>();
        // 状态：0-申请中 1-成功 2-失败
        statusList.add(1);
        totalWithdrawalCoinReqBody.setStatusList(statusList);
        Future<BigDecimal> totalWithdrawalCoinFuture = executorService.submit(() -> centerAgentMapper.getReportsCoinStatistics(totalWithdrawalCoinReqBody));

        BigDecimal totalDepositCoin = BigDecimal.ZERO;
        BigDecimal totalWithdrawalCoin = BigDecimal.ZERO;
        try {
            while (true) {
                if (totalDepositCoinFuture.isDone() && totalWithdrawalCoinFuture.isDone()) {
                    totalDepositCoin = totalDepositCoinFuture.get();
                    totalWithdrawalCoin = totalWithdrawalCoinFuture.get();
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
            log.info("buildDepositWithdrawalCoin:isShutdown={}", executorService.isShutdown());
        }
        return DepositWithdrawalDto.builder().totalWithdrawalCoin(totalWithdrawalCoin).totalDepositCoin(totalDepositCoin).build();
    }

    private RewardsCommissionDetailsStatisticsResBody coinCommissionStatistics(Integer category, Integer uid, Integer startTime, Integer endTime) {
        AtomicReference<BigDecimal> totalCoin = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> totalRewardsCoin = new AtomicReference<>(BigDecimal.ZERO);

        LambdaQueryWrapper<CoinCommission> wrapper = wherePageCoinCommissionList(category, uid, startTime, endTime);
        List<CoinCommission> list = coinCommissionServiceImpl.list(wrapper);
        if (Optional.ofNullable(list).isPresent()) {
            list.forEach(s -> {
                // 下级累计流水总额
                totalCoin.set(totalCoin.get().add(s.getSubBetTrunover()));
                // 下级累计佣金金额
                totalRewardsCoin.set(totalRewardsCoin.get().add(s.getCoin()));
            });
        }
        return RewardsCommissionDetailsStatisticsResBody.builder()
                .totalCoin(totalCoin.get())
                .totalRewardsCoin(totalRewardsCoin.get()).build();
    }

    private RewardsCommissionDetailsStatisticsResBody coinRewardsInviteStatistics(Integer category, Integer uid, Integer startTime, Integer endTime, String userName) {
        if (null != category) {
            RewardsInviteRes bo = centerAgentMapper.getCoinRewardsInviteStatistics(buildRewardsInviteReq(category, uid, startTime, endTime, userName));
            return RewardsCommissionDetailsStatisticsResBody.builder()
                    .totalCoin(bo.getCoin())
                    .totalRewardsCoin(bo.getRewardsCoin())
                    .build();
        }
        // category=null,统计全部
        // 线程池对象
        ExecutorService executorService = Executors.newCachedThreadPool();
        // 类型:0-被邀请奖金 1-邀请奖金 2-充值返利
        RewardsInviteReq invited = buildRewardsInviteReq(0, uid, startTime, endTime, userName);
        Future<RewardsInviteRes> futureInvited = executorService.submit(() -> centerAgentMapper.getCoinRewardsInviteStatistics(invited));
        RewardsInviteReq invite = buildRewardsInviteReq(1, uid, startTime, endTime, userName);
        Future<RewardsInviteRes> futureInvite = executorService.submit(() -> centerAgentMapper.getCoinRewardsInviteStatistics(invite));
        RewardsInviteReq deposit = buildRewardsInviteReq(2, uid, startTime, endTime, userName);
        Future<RewardsInviteRes> futureDeposit = executorService.submit(() -> centerAgentMapper.getCoinRewardsInviteStatistics(deposit));

        BigDecimal totalCoinRewardsInvited = BigDecimal.ZERO;
        BigDecimal totalCoinRewardsInvite = BigDecimal.ZERO;
        BigDecimal totalCoinRewardsDeposit = BigDecimal.ZERO;
        try {
            totalCoinRewardsInvited = null != futureInvited.get() ? futureInvited.get().getRewardsCoin() : BigDecimal.ZERO;
            totalCoinRewardsInvite = null != futureInvite.get() ? futureInvite.get().getRewardsCoin() : BigDecimal.ZERO;
            totalCoinRewardsDeposit = null != futureDeposit.get() ? futureDeposit.get().getRewardsCoin() : BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("邀请奖励->全部->累计统计异常:" + e);
        }
        return RewardsCommissionDetailsStatisticsResBody.builder()
                .totalCoinRewardsInvited(totalCoinRewardsInvited)
                .totalCoinRewardsInvite(totalCoinRewardsInvite)
                .totalCoinRewardsDeposit(totalCoinRewardsDeposit)
                .totalRewardsCoin(totalCoinRewardsInvited.add(totalCoinRewardsInvite).add(totalCoinRewardsDeposit))
                .build();
    }

    private RewardsInviteReq buildRewardsInviteReq(Integer category, Integer uid, Integer startTime, Integer endTime, String userName) {
        return RewardsInviteReq.builder()
                .uid(uid)
                .username(userName)
                .startTime(startTime)
                .endTime(endTime)
                .category(category)
                .build();
    }

    protected LambdaQueryWrapper<CoinCommission> wherePageCoinCommissionList(Integer category, Integer uid, Integer startTime, Integer endTime) {
        LambdaQueryWrapper<CoinCommission> where = Wrappers.lambdaQuery();
        where.eq(CoinCommission::getUid, uid);
        where.ge(null != startTime, CoinCommission::getRiqi, getYearMonth(startTime));
        where.le(null != endTime, CoinCommission::getRiqi, getYearMonth(endTime));
        // 类型:0-流水佣金 1-活跃会员佣金 2-满额人头彩金
        where.eq(null != category, CoinCommission::getCategory, category);
        // 状态:0-未发放 1-已发放
        where.eq(CoinCommission::getStatus, 1);
        return where;
    }

    /**
     * 时间转换yyyyMM
     *
     * @param time 10为的时间戳
     * @return yyyyMM
     */
    private static Integer getYearMonth(Integer time) {
        if (null == time) {
            return time;
        }
        return Integer.parseInt(DateNewUtils.utc8Zoned(time).format(DateTimeFormatter.ofPattern("yyyyMM")));
    }

}
