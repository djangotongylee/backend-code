package com.xinbo.sports.apiend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinbo.sports.apiend.io.dto.centeragent.*;
import com.xinbo.sports.apiend.io.dto.mapper.RewardsInviteReq;
import com.xinbo.sports.apiend.io.dto.mapper.RewardsInviteRes;
import com.xinbo.sports.dao.generator.po.*;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.bo.UserCacheBo;
import com.xinbo.sports.service.io.dto.BaseParams;
import com.xinbo.sports.utils.BeanConvertUtils;
import com.xinbo.sports.utils.components.pagination.BasePage;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
public class CenterAgentDetailsServiceImpl extends CenterAgentServiceImpl {

    @Override
    public ResPage<PlayerActivityDetailsResBody> playerActivityDetails(@Valid ReqPage<PlayerActivityDetailsReqBody> reqBody) {
        ResPage<PlayerActivityDetailsResBody> result = new ResPage<>();
        checkPrams(reqBody);
        PlayerActivityDetailsReqBody reqBodyData = reqBody.getData();
        BaseParams.HeaderInfo currentLoginUser = userInfoServiceImpl.getHeadLocalData();
        String username = reqBodyData.getUsername();
        List<Integer> uidList = userCache.getSubordinateUidListByUid(currentLoginUser.getId());
        if (Optional.ofNullable(uidList).isEmpty() || uidList.isEmpty()) {
            return result;
        }
        // 按username查询
        if (StringUtils.isNotBlank(username)) {
            UserCacheBo.UserCacheInfo userCacheInfo = userCache.getUserInfoByUserName(username);
            if (null == userCacheInfo || !uidList.contains(userCacheInfo.getUid())) {
                return result;
            }
            uidList = List.of(userCacheInfo.getUid());
        }

        // 类型:1-总注册人数 2-新增人数 3-首充金额 4-二充金额
        switch (reqBodyData.getCategory()) {
            case 1:
                result = pageRegister(reqBody.getPage(), currentLoginUser.getId(), null, null, username);
                break;
            case 2:
                result = pageRegister(reqBody.getPage(), currentLoginUser.getId(), reqBodyData.getStartTime(), reqBodyData.getEndTime(), username);
                break;
            case 3:
                result = pageCoinDeposit(reqBody.getPage(), uidList, reqBodyData.getStartTime(), reqBodyData.getEndTime(), 1);
                break;
            case 4:
                result = pageCoinDeposit(reqBody.getPage(), uidList, reqBodyData.getStartTime(), reqBodyData.getEndTime(), 2);
                break;
            default:
                break;
        }
        return result;
    }

    @Override
    public ResPage<RewardsCommissionDetailsResBody> rewardsCommissionDetails(@Valid ReqPage<RewardsCommissionDetailsReqBody> reqBody) {
        ResPage<RewardsCommissionDetailsResBody> result = new ResPage<>();
        checkPrams(reqBody);
        RewardsCommissionDetailsReqBody reqBodyData = reqBody.getData();
        Integer uid = userInfoServiceImpl.getHeadLocalData().getId();
        Integer startTime = reqBodyData.getStartTime();
        Integer endTime = reqBodyData.getEndTime();
        // 大类型:1-邀请奖励 2-佣金奖励
        switch (reqBodyData.getCategory()) {
            case 1:
                // 类型:0-被邀请奖金1-邀请奖金 2-充值返利
                result = pageRewardsInviteList(reqBodyData.getSubCategory(), uid, startTime, endTime, reqBodyData.getUsername(), reqBody.getPage());
                break;
            case 2:
                // 类型:0-流水佣金 1-活跃会员佣金 2-满额人头彩金
                result = pageCoinCommissionList(reqBodyData.getSubCategory(), uid, startTime, endTime, reqBody.getPage());
                break;
            default:
                break;
        }
        return result;
    }

    @Override
    public DepositWithdrawalDetailsStatisticsResBody playerActivityDetailsStatistics(@Valid PlayerActivityDetailsReqBody reqBody) {
        DepositWithdrawalDetailsStatisticsResBody result = DepositWithdrawalDetailsStatisticsResBody.builder().totalCoin(BigDecimal.ZERO).build();
        checkPrams(reqBody);
        BaseParams.HeaderInfo currentLoginUser = userInfoServiceImpl.getHeadLocalData();
        List<Integer> uidList = userCache.getSubordinateUidListByUid(currentLoginUser.getId());
        String username = reqBody.getUsername();
        if (Optional.ofNullable(uidList).isEmpty() || uidList.isEmpty()) {
            return result;
        }
        // 按username查询
        if (StringUtils.isNotBlank(username)) {
            UserCacheBo.UserCacheInfo userCacheInfo = userCache.getUserInfoByUserName(username);
            if (null == userCacheInfo || !uidList.contains(userCacheInfo.getUid())) {
                return result;
            }
            uidList = List.of(userCacheInfo.getUid());
        }
        Integer category = reqBody.getCategory();
        Integer startTime = reqBody.getStartTime();
        Integer endTime = reqBody.getEndTime();
        // 类型:1-总注册人数 2-新增人数 3-首充金额 4-二充金额
        switch (category) {
            case 3:
                result = playerActivityDetailsStatisticsDeposit(1, uidList, startTime, endTime);
                break;
            case 4:
                result = playerActivityDetailsStatisticsDeposit(2, uidList, startTime, endTime);
                break;
            default:
                break;
        }
        return result;
    }

    private DepositWithdrawalDetailsStatisticsResBody playerActivityDetailsStatisticsDeposit(Integer depStatus, List<Integer> uidList, Integer startTime, Integer endTime) {
        List<CoinDeposit> list = coinDepositServiceImpl.lambdaQuery()
                .in(CoinDeposit::getStatus, 1, 2, 9)
                .eq(null != depStatus, CoinDeposit::getDepStatus, depStatus)
                .in(CoinDeposit::getUid, uidList)
                .ge(null != startTime, CoinDeposit::getUpdatedAt, startTime)
                .le(null != endTime, CoinDeposit::getUpdatedAt, endTime)
                .select(CoinDeposit::getPayCoin)
                .list();
        if (Optional.ofNullable(list).isEmpty() || list.isEmpty()) {
            return DepositWithdrawalDetailsStatisticsResBody.builder().totalCoin(BigDecimal.ZERO).build();
        }
        return DepositWithdrawalDetailsStatisticsResBody.builder()
                .totalCoin(list.stream().map(CoinDeposit::getPayCoin).reduce(new BigDecimal(0), (x, y) -> x.add(y)))
                .build();
    }

    @Override
    public DepositWithdrawalDetailsStatisticsResBody depositWithdrawalDetailsStatistics(@Valid DepositWithdrawalDetailsReqBody reqBody) {
        checkPrams(reqBody);
        BaseParams.HeaderInfo currentLoginUser = userInfoServiceImpl.getHeadLocalData();
        List<Integer> uidList = userCache.getSubordinateUidListByUid(currentLoginUser.getId());
        String username = reqBody.getUsername();

        if (Optional.ofNullable(uidList).isEmpty() || uidList.isEmpty()) {
            return DepositWithdrawalDetailsStatisticsResBody.builder().totalCoin(BigDecimal.ZERO).build();
        }
        // 按username查询
        if (StringUtils.isNotBlank(username)) {
            UserCacheBo.UserCacheInfo userCacheInfo = userCache.getUserInfoByUserName(username);
            if (null == userCacheInfo || !uidList.contains(userCacheInfo.getUid())) {
                return DepositWithdrawalDetailsStatisticsResBody.builder().totalCoin(BigDecimal.ZERO).build();
            }
            uidList = List.of(userCacheInfo.getUid());
        }

        Integer startTime = reqBody.getStartTime();
        Integer endTime = reqBody.getEndTime();
        // 类型:1-存款 2-提款
        if (1 == reqBody.getCategory()) {
            return depositDetailsStatistics(uidList, startTime, endTime);
        }
        return withdrawalDetailsStatistics(uidList, startTime, endTime);
    }

    private DepositWithdrawalDetailsStatisticsResBody depositDetailsStatistics(List<Integer> uidList, Integer startTime, Integer endTime) {
        List<CoinDeposit> list = coinDepositServiceImpl.lambdaQuery()
                .in(CoinDeposit::getStatus, 1, 2, 9)
                .in(CoinDeposit::getUid, uidList)
                .ge(null != startTime, CoinDeposit::getUpdatedAt, startTime)
                .le(null != endTime, CoinDeposit::getUpdatedAt, endTime)
                .select(CoinDeposit::getPayCoin)
                .list();
        if (Optional.ofNullable(list).isEmpty() || list.isEmpty()) {
            return DepositWithdrawalDetailsStatisticsResBody.builder().totalCoin(BigDecimal.ZERO).build();
        }
        return DepositWithdrawalDetailsStatisticsResBody.builder()
                .totalCoin(list.stream().map(CoinDeposit::getPayCoin).reduce(new BigDecimal(0), (x, y) -> x.add(y)))
                .build();
    }

    private DepositWithdrawalDetailsStatisticsResBody withdrawalDetailsStatistics(List<Integer> uidList, Integer startTime, Integer endTime) {
        List<CoinWithdrawal> list = coinWithdrawalServiceImpl.lambdaQuery()
                .eq(CoinWithdrawal::getStatus, 1)
                .in(CoinWithdrawal::getUid, uidList)
                .ge(null != startTime, CoinWithdrawal::getUpdatedAt, startTime)
                .le(null != endTime, CoinWithdrawal::getUpdatedAt, endTime)
                .select(CoinWithdrawal::getCoin)
                .list();
        if (Optional.ofNullable(list).isEmpty() || list.isEmpty()) {
            return DepositWithdrawalDetailsStatisticsResBody.builder().totalCoin(BigDecimal.ZERO).build();
        }
        return DepositWithdrawalDetailsStatisticsResBody.builder()
                .totalCoin(list.stream().map(CoinWithdrawal::getCoin).reduce(new BigDecimal(0), (x, y) -> x.add(y)))
                .build();
    }

    @Override
    public RewardsCommissionDetailsStatisticsResBody rewardsCommissionDetailsStatistics(@Valid RewardsCommissionDetailsReqBody reqBody, Integer uid) {
        return super.rewardsCommissionDetailsStatistics(reqBody, uid);
    }

    private ResPage<PlayerActivityDetailsResBody> pageRegister(Page<UserProfile> reqPage, Integer uid, Integer startTime, Integer endTime, String userName) {
        Page<User> page = centerAgentMapper.subordinateList(reqPage, SubordinateListReqBody.builder().uid(uid).username(userName).startTime(startTime).endTime(endTime).build());
        List<User> tmpPage = page.getRecords();
        List<PlayerActivityDetailsResBody> list = new ArrayList<>();
        for (User user : tmpPage) {
            PlayerActivityDetailsResBody build = PlayerActivityDetailsResBody.builder()
                    .createdAt(user.getCreatedAt())
                    .uid(user.getId())
                    .username(user.getUsername())
                    .build();
            list.add(build);
        }
        Page<PlayerActivityDetailsResBody> resPage = BeanConvertUtils.copyPageProperties(page, PlayerActivityDetailsResBody::new);
        ResPage<PlayerActivityDetailsResBody> resBody = ResPage.get(resPage);
        resBody.setList(list);
        return resBody;
    }

    private ResPage<PlayerActivityDetailsResBody> pageCoinDeposit(Page<CoinDeposit> page, List<Integer> uidList, Integer startTime, Integer endTime, Integer depStatus) {
        LambdaQueryWrapper<CoinDeposit> wrapper = wherePageCoinDeposit(uidList, startTime, endTime);
        wrapper.eq(CoinDeposit::getDepStatus, depStatus);
        Page<CoinDeposit> tmpPage = coinDepositServiceImpl.page(page, wrapper);
        List<CoinDeposit> depositList = tmpPage.getRecords();
        List<PlayerActivityDetailsResBody> list = new ArrayList<>();
        for (CoinDeposit deposit : depositList) {
            PlayerActivityDetailsResBody build = PlayerActivityDetailsResBody.builder()
                    .coin(deposit.getPayCoin())
                    .createdAt(deposit.getCreatedAt())
                    .uid(deposit.getUid())
                    .username(userCache.getUserInfoById(deposit.getUid()).getUsername())
                    .build();
            list.add(build);
        }
        Page<PlayerActivityDetailsResBody> resPage = BeanConvertUtils.copyPageProperties(tmpPage, PlayerActivityDetailsResBody::new);
        ResPage<PlayerActivityDetailsResBody> resBody = ResPage.get(resPage);
        resBody.setList(list);
        return resBody;
    }

    private ResPage<RewardsCommissionDetailsResBody> pageRewardsInviteList(Integer category, Integer uid, Integer startTime, Integer endTime, String userName, Page<UserProfile> reqPage) {
        RewardsInviteReq build = RewardsInviteReq.builder()
                .uid(uid)
                .username(userName)
                .startTime(startTime)
                .endTime(endTime)
                // 类型:0-被邀请奖金1-邀请奖金 2-充值返利
                .category(category)
                .build();
        Page<RewardsInviteRes> page = centerAgentMapper.pageRewardsInviteList(reqPage, build);
        if (Optional.ofNullable(page).isEmpty()
                || Optional.ofNullable(page.getRecords()).isEmpty()
                || page.getRecords().isEmpty()) {
            return new ResPage<>();
        }
        List<RewardsCommissionDetailsResBody> list = new ArrayList<>();
        for (RewardsInviteRes po : page.getRecords()) {
            UserCacheBo.UserCacheInfo userCacheInfo = userCache.getUserInfoById(po.getUid());
            list.add(RewardsCommissionDetailsResBody.builder()
                    .coin(po.getCoin())
                    .createdAt(po.getCreatedAt())
                    .username(userCacheInfo.getUsername())
                    .rewardsCoin(po.getRewardsCoin())
                    .uid(po.getUid())
                    .levelId(userCacheInfo.getLevelId())
                    .avatar(userCacheInfo.getAvatar())
                    .category(po.getCategory())
                    .build());
        }
        Page<RewardsCommissionDetailsResBody> tmpPage = BeanConvertUtils.copyPageProperties(page, RewardsCommissionDetailsResBody::new);
        ResPage<RewardsCommissionDetailsResBody> result = ResPage.get(tmpPage);
        result.setList(list);
        return result;
    }

    private ResPage<RewardsCommissionDetailsResBody> pageCoinCommissionList(Integer category, Integer uid, Integer startTime, Integer endTime, Page<CoinCommission> reqPage) {
        LambdaQueryWrapper<CoinCommission> wrapper = wherePageCoinCommissionList(category, uid, startTime, endTime);
        Page<CoinCommission> page = coinCommissionServiceImpl.page(reqPage, wrapper);
        if (Optional.ofNullable(page).isEmpty()
                || Optional.ofNullable(page.getRecords()).isEmpty()
                || page.getRecords().isEmpty()) {
            return new ResPage<>();
        }
        List<RewardsCommissionDetailsResBody> list = new ArrayList<>();
        for (CoinCommission po : page.getRecords()) {
            RewardsCommissionDetailsResBody build = RewardsCommissionDetailsResBody.builder()
                    .coin(po.getSubBetTrunover())
                    .createdAt(po.getCreatedAt())
                    .rewardsCoin(po.getCoin())
                    .monthName(po.getRiqi())
                    .category(po.getCategory())
                    .commissionId(po.getId())
                    .build();
            // 类型:0-流水佣金 1-活跃会员佣金 2-满额人头彩金
            // 当类型为0-流水佣金 2-满额人头彩金时,sudUIds单个，为1时subUids多个逗号分割如12,13,14
            List<UserInfoDto> commissionSubUidList = new ArrayList<>();
            if (0 == po.getCategory() || 2 == po.getCategory()) {
                build.setUid(Integer.valueOf(po.getSubUids()));
                UserCacheBo.UserCacheInfo userCacheInfo = userCache.getUserInfoById(build.getUid());
                build.setUsername(userCacheInfo.getUsername());
                build.setLevelId(userCacheInfo.getLevelId());
                build.setAvatar(userCacheInfo.getAvatar());
            } else {
                List.of(po.getSubUids().split(",")).forEach(s -> {
                    if (StringUtils.isNotBlank(s)) {
                        UserCacheBo.UserCacheInfo userCacheInfo = userCache.getUserInfoById(Integer.valueOf(s));
                        UserInfoDto userInfoDto = new UserInfoDto();
                        userInfoDto.setId(userCacheInfo.getUid());
                        userInfoDto.setUsername(userCacheInfo.getUsername());
                        userInfoDto.setAvatar(userCacheInfo.getAvatar());
                        commissionSubUidList.add(userInfoDto);
                    }
                });
                build.setCommissionCount(commissionSubUidList.size());
            }
            list.add(build);
        }
        Page<RewardsCommissionDetailsResBody> tmpPage = BeanConvertUtils.copyPageProperties(page, RewardsCommissionDetailsResBody::new);
        ResPage<RewardsCommissionDetailsResBody> result = ResPage.get(tmpPage);
        result.setList(list);
        return result;
    }

    @Override
    public ResPage<SubordinateInfo> rewardsCommissionActiveDetails(ReqPage<RewardsCommissionDetailsActiveReqBody> reqBody) {
        if (null == reqBody || null == reqBody.getData() || null == reqBody.getPage()) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        CoinCommission coinCommission = coinCommissionServiceImpl.getById(reqBody.getData().getCommissionId());
        if (null == coinCommission) {
            throw new BusinessException(CodeInfo.API_RECORDS_NOT_EXISTS);
        }

        // 活跃会员佣金List
        List<SubordinateInfo> commissionSubUidList = new ArrayList<>();
        List.of(coinCommission.getSubUids().split(",")).forEach(s -> {
            if (StringUtils.isNotBlank(s)) {
                UserCacheBo.UserCacheInfo userCacheInfo = userCache.getUserInfoById(Integer.valueOf(s));
                SubordinateInfo subordinateInfo = new SubordinateInfo();
                subordinateInfo.setUid(userCacheInfo.getUid());
                subordinateInfo.setUsername(userCacheInfo.getUsername());
                subordinateInfo.setCreatedAt(userCacheInfo.getCreatedAt());
                commissionSubUidList.add(subordinateInfo);
            }
        });
        String sortKey = reqBody.getSortKey();
        String[] sortField = reqBody.getSortField();
        if (StringUtils.isNotBlank(sortKey) && Optional.ofNullable(sortField).isPresent()
                && sortField.length > 0
                && "createdAt".equals(sortField[0])) {
            if (BasePage.DESC_SORT.equalsIgnoreCase(sortKey)) {
                commissionSubUidList.sort((x, y) -> y.getCreatedAt() - x.getCreatedAt());
            } else {
                commissionSubUidList.sort(Comparator.comparingInt(SubordinateInfo::getCreatedAt));
            }
        }

        // 内存分页
        Page<SubordinateInfo> page = new Page<>(reqBody.getCurrent(), reqBody.getSize(), commissionSubUidList.size());
        List<SubordinateInfo> collect = commissionSubUidList.stream()
                .skip(page.getSize() * (page.getCurrent() - 1))
                .limit(page.getSize()).collect(Collectors.toList());
        page.setRecords(collect);
        return ResPage.get(page);
    }
}
