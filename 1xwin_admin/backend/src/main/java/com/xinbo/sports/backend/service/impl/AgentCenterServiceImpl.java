package com.xinbo.sports.backend.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinbo.sports.backend.io.bo.user.AgentCenterParameter.*;
import com.xinbo.sports.backend.service.IAgentCenterService;
import com.xinbo.sports.dao.generator.po.CoinCommission;
import com.xinbo.sports.dao.generator.po.User;
import com.xinbo.sports.dao.generator.po.UserProfile;
import com.xinbo.sports.dao.generator.service.CoinCommissionService;
import com.xinbo.sports.dao.generator.service.UserProfileService;
import com.xinbo.sports.dao.generator.service.UserService;
import com.xinbo.sports.service.cache.redis.UserCache;
import com.xinbo.sports.service.common.Constant;
import com.xinbo.sports.utils.BeanConvertUtils;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;


/**
 * @author: wells
 * @date: 2020/8/27
 * @description:
 */
@Service
public class AgentCenterServiceImpl implements IAgentCenterService {
    private static final String SUP_UID = "getSupUid";
    @Autowired
    private UserService userServiceImpl;
    @Autowired
    private UserProfileService userProfileServiceImpl;
    @Autowired
    private CoinCommissionService coinCommissionServiceImpl;
    @Autowired
    private UserCache userCache;

    /**
     * 代理中心-会员列表
     *
     * @param reqBody 请求参数石实例类
     * @return 分页响应实体类
     */
    @Override
    public ResPage<AgentUserListResBody> agentUserList(ReqPage<AgentUserListReqBody> reqBody) {
        //角色过滤
        var roleList = List.of(Constant.USER_ROLE_HY, Constant.USER_ROLE_DL);
        var reqDto = reqBody.getData();
        var userCacheInfo = userCache.getUserInfoByUserName(Optional.ofNullable(reqDto.getAgentUsername()).orElse(""));
        var agentId = Objects.nonNull(userCacheInfo) ? userCacheInfo.getUid() : 0;
        var userList = userServiceImpl.lambdaQuery()
                .in(User::getRole, roleList)
                .list();
        if (CollectionUtils.isEmpty(userList)) {
            return new ResPage<>();
        }
        //会员Id对应的用户信息
        var userMap = userList.stream().collect(Collectors.toMap(User::getId, v -> v));
        Integer usernameToUid = null;
        if (nonNull(reqDto.getUsername())) {
            var userIdInfo = userCache.getUserInfoByUserName(Optional.ofNullable(reqDto.getUsername()).orElse(""));
            usernameToUid = Objects.nonNull(userIdInfo) ? userIdInfo.getUid() : 0;
        }
        var ids = userList.stream().filter(user -> reqDto.getLevelId() != null && user.getLevelId().equals(reqDto.getLevelId()))
                .map(User::getId).collect(Collectors.toList());
        var queryWrapper = new QueryWrapper<UserProfile>().lambda()
                //UID
                .eq(nonNull(reqDto.getUid()), UserProfile::getUid, reqDto.getUid())
                //用户名
                .eq(nonNull(reqDto.getUsername()) && nonNull(usernameToUid), UserProfile::getUid, usernameToUid)
                //会员状态
                .eq(nonNull(reqDto.getStatus()), UserProfile::getStatus, reqDto.getStatus())
                //开始时间
                .ge(nonNull(reqDto.getStartTime()), UserProfile::getCreatedAt, reqDto.getStartTime())
                //结束时间
                .le(nonNull(reqDto.getEndTime()), UserProfile::getCreatedAt, reqDto.getEndTime());
        //初始化的用户
        queryWrapper.in(reqDto.getLevelId() == null, UserProfile::getUid, userMap.keySet());
        //会员等级
        queryWrapper.in(reqDto.getLevelId() != null && !CollectionUtils.isEmpty(ids), UserProfile::getUid, ids);
        queryWrapper.eq(reqDto.getLevelId() != null && CollectionUtils.isEmpty(ids), UserProfile::getUid, null);
        getQueryWrapper(reqDto, agentId, queryWrapper);
        //不分页查询用户信息统计团对人数
        var userProfileList = userProfileServiceImpl.lambdaQuery()
                .in(UserProfile::getUid, userMap.keySet())
                .list();
        var page = userProfileServiceImpl.page(reqBody.getPage(), queryWrapper);
        //获取代理名字
        IntFunction<String> nameFunction = id -> {
            var user = userMap.get(id);
            if (isNull(user)) {
                return "";
            }
            return user.getUsername();
        };
        var returnPage = BeanConvertUtils.copyPageProperties(page, AgentUserListResBody::new,
                (userProfile, agentUserList) -> {
                    var user = userMap.get(userProfile.getUid());
                    agentUserList.setSupUid1Name(nameFunction.apply(userProfile.getSupUid1()));
                    agentUserList.setSupUid2Name(nameFunction.apply(userProfile.getSupUid2()));
                    agentUserList.setSupUid3Name(nameFunction.apply(userProfile.getSupUid3()));
                    agentUserList.setSupUid4Name(nameFunction.apply(userProfile.getSupUid4()));
                    agentUserList.setSupUid5Name(nameFunction.apply(userProfile.getSupUid5()));
                    agentUserList.setSupUid6Name(nameFunction.apply(userProfile.getSupUid6()));
                    agentUserList.setCoin(user.getCoin());
                    agentUserList.setUserName(user.getUsername());
                    agentUserList.setLevelId(user.getLevelId());
                    agentUserList.setUserName(user.getUsername());
                    agentUserList.setTeamCount(getTeam(userProfile.getUid(), userProfileList));
                    agentUserList.setTeamAmount(getTeamAmount(userProfile.getUid(), userProfileList, userMap));
                });
        return ResPage.get(returnPage);
    }

    /**
     * 代理id与代理层级
     * 1.代理id与代理层级都有值，则正常查询
     * 2.代理id为空，代理层级不为空，则查询当前层级符合代理id的记录（当前级的id不为0）
     * 3.代理id不为空，代理层级为空，则查询每个层级的id任意一个符合代理id的记录
     *
     * @param reqDto       请求实例
     * @param agentId      代理ID
     * @param queryWrapper 构造条件参数
     */
    private void getQueryWrapper(AgentUserListReqBody reqDto, Integer agentId, LambdaQueryWrapper<UserProfile> queryWrapper) {
        if (reqDto.getAgentLayer() != null && agentId != 0) {
            queryWrapper.eq(reqDto.getAgentLayer() == 1, UserProfile::getSupUid1, agentId);
            queryWrapper.eq(reqDto.getAgentLayer() == 2, UserProfile::getSupUid2, agentId);
            queryWrapper.eq(reqDto.getAgentLayer() == 3, UserProfile::getSupUid3, agentId);
            queryWrapper.eq(reqDto.getAgentLayer() == 4, UserProfile::getSupUid4, agentId);
            queryWrapper.eq(reqDto.getAgentLayer() == 5, UserProfile::getSupUid5, agentId);
            queryWrapper.eq(reqDto.getAgentLayer() == 6, UserProfile::getSupUid6, agentId);
        } else if (reqDto.getAgentLayer() != null) {
            queryWrapper.ne(reqDto.getAgentLayer() == 1, UserProfile::getSupUid1, 0);
            queryWrapper.ne(reqDto.getAgentLayer() == 2, UserProfile::getSupUid2, 0);
            queryWrapper.ne(reqDto.getAgentLayer() == 3, UserProfile::getSupUid3, 0);
            queryWrapper.ne(reqDto.getAgentLayer() == 4, UserProfile::getSupUid4, 0);
            queryWrapper.ne(reqDto.getAgentLayer() == 5, UserProfile::getSupUid5, 0);
            queryWrapper.ne(reqDto.getAgentLayer() == 6, UserProfile::getSupUid6, 0);
        } else if (reqDto.getAgentLayer() == null && agentId != 0) {
            queryWrapper.and(wrapper ->
                    wrapper.eq(UserProfile::getSupUid1, agentId)
                            .or().eq(UserProfile::getSupUid2, agentId)
                            .or().eq(UserProfile::getSupUid3, agentId)
                            .or().eq(UserProfile::getSupUid4, agentId)
                            .or().eq(UserProfile::getSupUid5, agentId)
                            .or().eq(UserProfile::getSupUid6, agentId)
            );
        }
    }

    /**
     * 获取团队人数信息
     *
     * @param uid             用户ID
     * @param userProfileList 用户实例集合
     * @return 团队集合
     */
    private List<Team> getTeam(Integer uid, List<UserProfile> userProfileList) {
        var teamList = new ArrayList<Team>();
        IntFunction<Long> function = layer ->
                userProfileList.stream().filter(x -> getSupUid(x, layer).equals(uid)).count();
        for (int i = 1; i < 7; i++) {
            teamList.add(Team.builder().agentLayer(i).count(function.apply(i).intValue()).build());
        }
        return teamList;
    }

    /**
     * 计算团队金额
     *
     * @param uid             用户id
     * @param userProfileList 用户上下关系集合
     * @param userMap         用户余额集合
     * @return 团队金额集合
     */
    private List<TeamAmount> getTeamAmount(Integer uid, List<UserProfile> userProfileList, Map<Integer, User> userMap) {
        var teamList = new ArrayList<TeamAmount>();
        IntFunction<BigDecimal> function = layer ->
                userProfileList.stream().filter(x ->
                        getSupUid(x, layer).equals(uid)
                ).map(x -> {
                    var user = userMap.get(x.getUid());
                    return Objects.nonNull(user) ? user.getCoin() : BigDecimal.ZERO;
                }).reduce(BigDecimal::add)
                        .orElse(BigDecimal.ZERO);
        var total = BigDecimal.ZERO;
        for (int i = 1; i < 7; i++) {
            var amount = function.apply(i);
            teamList.add(TeamAmount.builder().agentLayer(i).amount(amount).build());
            total = total.add(amount);
        }
        teamList.add(TeamAmount.builder().agentLayer(7).amount(total).build());
        return teamList;
    }

    /**
     * 反射根据字段名获取值
     *
     * @param object 对象
     * @param layer  等级
     * @return 字段值
     */
    public Integer getSupUid(Object object, Integer layer) {
        try {
            return (Integer) object.getClass().getMethod(SUP_UID + layer).invoke(object);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * 代理中心-佣金盈利
     *
     * @param reqBody 请求实例类
     * @return 响应实例类
     */
    @Override
    public AgentCommissionResBody agentCommissionProfit(ReqPage<AgentCommissionReqBody> reqBody) {
        var reqDto = reqBody.getData();
        var queryWrapper = new LambdaQueryWrapper<CoinCommission>()
                //UID
                .eq(nonNull(reqDto.getAgentId()), CoinCommission::getUid, reqDto.getAgentId())
                //用户名
                .eq(nonNull(reqDto.getUsername()), CoinCommission::getUsername, reqDto.getUsername())
                //佣金状态
                .eq(nonNull(reqDto.getStatus()), CoinCommission::getStatus, reqDto.getStatus())
                //佣金类型
                .eq(nonNull(reqDto.getCategory()), CoinCommission::getCategory, reqDto.getCategory())
                //开始时间
                .ge(nonNull(reqDto.getStartTime()), CoinCommission::getCreatedAt, reqDto.getStartTime())
                //结束时间
                .le(nonNull(reqDto.getEndTime()), CoinCommission::getCreatedAt, reqDto.getEndTime());
        var page = coinCommissionServiceImpl.page(reqBody.getPage(), queryWrapper);
        var returnPage = BeanConvertUtils.copyPageProperties(page, AgentCommissionList::new,
                (commission, agentCommission) -> {
                    var date = new StringBuffer(commission.getRiqi() + "");
                    date = date.insert(4, "-");
                    if (commission.getCategory() == 0) {
                        date = date.insert(7, "-");
                    }
                    agentCommission.setRiqi(date.toString());
                    var sunUid = agentCommission.getSubUids();
                    if (Strings.isNotEmpty(sunUid)) {
                        String[] subUidArr = sunUid.split(",");
                        var subIds = Arrays.stream(subUidArr).map(x -> userCache.getUserInfoById(Integer.valueOf(x.trim())).getUsername())
                                .collect(Collectors.joining(","));
                        agentCommission.setSubUids(subIds);
                    }
                    if (commission.getCategory() == 0) {
                        agentCommission.setSubBetTrunover(String.valueOf(commission.getSubBetTrunover().setScale(2, RoundingMode.DOWN)));
                    } else {
                        var length = Strings.isNotEmpty(sunUid) ? sunUid.split(",").length : 0;
                        agentCommission.setSubBetTrunover(String.valueOf(length));
                    }
                });
        //累计金额
        var totalCoin = BigDecimal.ZERO;
        //总投注额
        var totalBetCoin = BigDecimal.ZERO;
        var commissionList = coinCommissionServiceImpl.list(queryWrapper);
        if (!CollectionUtils.isEmpty(commissionList)) {
            totalCoin = commissionList.stream().map(CoinCommission::getCoin).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
            //为流水佣金类型时计算总投注额
            if (reqDto.getCategory() != null && reqDto.getCategory() == 0) {
                totalBetCoin = commissionList.stream().map(CoinCommission::getSubBetTrunover).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
            }
        }
        return AgentCommissionResBody.builder().agentCommissionResPage(ResPage.get(returnPage)).totalCoin(totalCoin).totalBetCoin(totalBetCoin).build();
    }

    /**
     * @Author Wells
     * @Description 佣金收益用户详情
     * @Date 2020/9/17 10:58 下午
     * @param1 reqBody
     * @Return com.xinbo.sports.utils.components.response.CodeInfo
     */
    @Override
    public ResPage<AgentCommissionDetailsResDtoBody> agentCommissionDetails(ReqPage<AgentCommissionDetailsReqBody> reqBody) {
        var coinCommission = coinCommissionServiceImpl.getById(reqBody.getData().getId());
        var ids = nonNull(coinCommission) ? coinCommission.getSubUids() : "";
        var query = new LambdaQueryWrapper<User>();
        if (!StringUtils.isEmpty(ids)) {
            query.in(User::getId, Arrays.asList(ids.split(",")));
        } else {
            return ResPage.get(new Page<>());
        }
        var page = userServiceImpl.page(reqBody.getPage(), query);
        var returnPage = BeanConvertUtils.copyPageProperties(page, AgentCommissionDetailsResDtoBody::new, (source, agentCommissionDetailsResDtoBody) ->
                agentCommissionDetailsResDtoBody.setUid(source.getId())
        );
        return ResPage.get(returnPage);
    }
}

