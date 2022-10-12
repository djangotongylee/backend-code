package com.xinbo.sports.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinbo.sports.backend.io.bo.ReportCenter;
import com.xinbo.sports.backend.service.IMasterAccountStatisticsService;
import com.xinbo.sports.dao.generator.po.UserProfile;
import com.xinbo.sports.dao.generator.service.CoinLogService;
import com.xinbo.sports.dao.generator.service.UserProfileService;
import com.xinbo.sports.service.cache.redis.UserCache;
import com.xinbo.sports.service.common.Constant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.bo.UserCacheBo;
import com.xinbo.sports.utils.BeanConvertUtils;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.xinbo.sports.backend.io.bo.ReportCenter.*;

/**
 * <p>
 * 统计接口实现类
 * </p>
 *
 * @author andy
 * @since 2020/6/5
 */
@Service
@Slf4j
public class MasterAccountStatisticsServiceImpl implements IMasterAccountStatisticsService {
    @Resource
    private CoinLogService coinLogServiceImpl;
    @Resource
    private UserProfileService userProfileServiceImpl;
    @Resource
    private UserCache userCache;

    private static final String COIN_DEPOSIT = "coinDeposit";
    private static final String COIN_WITHDRAWAL = "coinWithdrawal";
    private static final String COIN_UP = "coinUp";
    private static final String COIN_DOWN = "coinDown";

    @Override
    public ResPage<ReportCenter.MasterAccountList> masterAccountList(ReqPage<ListMasterAccountReqBody> reqBody) {
        if (null == reqBody) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        var testUidList = userCache.getTestUidList();
        var data = reqBody.getData();
        if (StringUtils.isNotBlank(data.getUsername())) {
            var user = userCache.getUserInfoByUserName(data.getUsername());
            if (null != user && testUidList.contains(user.getUid())) {
                /** 测试账号不能查询报表数据 **/
                throw new BusinessException(CodeInfo.REPORT_TEST_USER_NOT_SEARCH_DATA);
            }
        }

        QueryWrapper wrapper = whereMasterAccountList(data);
        selectListMasterAccount(wrapper);
        wrapper.in(Constant.CATEGORY, 1, 2, 3, 4);
        wrapper.groupBy(Constant.UID);
        /** 排除测试账号 **/
        wrapper.notIn(Optional.ofNullable(testUidList).isPresent() && !testUidList.isEmpty(), Constant.UID, testUidList);

        Page<Map<String, Object>> mapPage = coinLogServiceImpl.pageMaps(reqBody.getPage(), wrapper);
        Page<MasterAccountList> tmpPage = BeanConvertUtils.copyPageProperties(mapPage, MasterAccountList::new);
        ResPage<MasterAccountList> resPage = ResPage.get(tmpPage);
        if (Optional.ofNullable(mapPage).isPresent() && Optional.ofNullable(mapPage.getRecords()).isPresent()) {
            resPage.setList(processlistMasterAccount(mapPage));
        }
        return resPage;
    }

    @Override
    public MasterAccountStatistics masterAccountStatistics(ListMasterAccountReqBody reqBody) {
        if (null != reqBody && StringUtils.isNotBlank(reqBody.getUsername())) {
            var user = userCache.getUserInfoByUserName(reqBody.getUsername());
            if (null != user && Constant.USER_ROLE_CS == user.getRole()) {
                /** 测试账号不能查询报表数据 **/
                throw new BusinessException(CodeInfo.REPORT_TEST_USER_NOT_SEARCH_DATA);
            }
        }

        QueryWrapper wrapper = whereMasterAccountList(reqBody);
        selectMasterAccountStatistics(wrapper);

        /** 排除测试账号 **/
        var testUidList = userCache.getTestUidList();
        wrapper.notIn(Optional.ofNullable(testUidList).isPresent() && !testUidList.isEmpty(), Constant.UID, testUidList);
        Map map = coinLogServiceImpl.getMap(wrapper);
        return processMasterAccountStatistics(map);
    }

    private List<MasterAccountList> processlistMasterAccount(Page<Map<String, Object>> mapPage) {
        List<MasterAccountList> list = new ArrayList<>();
        for (Map<String, Object> map : mapPage.getRecords()) {
            if (null != map) {
                Integer uid = (Integer) map.get("uid");
                UserCacheBo.UserCacheInfo userCacheInfo = userCache.getUserInfoById(uid);
                String username = "";
                if (null != userCacheInfo) {
                    username = userCacheInfo.getUsername();
                }
                List<UserCacheBo.UserFlagInfo> userFlagList = userCache.getUserFlagList(uid);
                list.add(MasterAccountList.builder()
                        .uid(uid)
                        .username(username)
                        .coinDeposit((BigDecimal) map.get(COIN_DEPOSIT))
                        .coinWithdrawal((BigDecimal) map.get(COIN_WITHDRAWAL))
                        .coinUp((BigDecimal) map.get(COIN_UP))
                        .coinDown((BigDecimal) map.get(COIN_DOWN))
                        .userFlagList(userFlagList)
                        .build());
            }
        }
        return list;
    }

    private MasterAccountStatistics processMasterAccountStatistics(Map map) {
        BigDecimal coinDeposit = BigDecimal.ZERO;
        BigDecimal coinWithdrawal = BigDecimal.ZERO;
        BigDecimal coinUp = BigDecimal.ZERO;
        BigDecimal coinDown = BigDecimal.ZERO;
        if (null != map) {
            coinDeposit = (BigDecimal) map.get(COIN_DEPOSIT);
            coinWithdrawal = (BigDecimal) map.get(COIN_WITHDRAWAL);
            coinUp = (BigDecimal) map.get(COIN_UP);
            coinDown = (BigDecimal) map.get(COIN_DOWN);
        }
        return MasterAccountStatistics.builder()
                .coinDeposit(coinDeposit)
                .coinWithdrawal(coinWithdrawal)
                .coinUp(coinUp)
                .coinDown(coinDown)
                .build();
    }

    @Override
    public ResPage<MasterAccountList> masterAccountListOfSubordinate(MasterAccountListOfSubordinateReqBody reqBody) {
        if (null == reqBody) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        LambdaQueryWrapper<UserProfile> wrapper = whereMasterAccountListOfSubordinate(reqBody.getUid());

        /** 排除测试账号 **/
        var testUidList = userCache.getTestUidList();
        wrapper.notIn(Optional.ofNullable(testUidList).isPresent() && !testUidList.isEmpty(), UserProfile::getUid, testUidList);
        Page<UserProfile> page = userProfileServiceImpl.page(reqBody.getPage(), wrapper);
        Page<MasterAccountList> tmpPage = BeanConvertUtils.copyPageProperties(page, MasterAccountList::new);
        ResPage<MasterAccountList> resPage = ResPage.get(tmpPage);
        if (Optional.ofNullable(resPage.getList()).isPresent()) {
            resPage.setList(processMasterAccountListOfSubordinate(resPage));
        }
        return resPage;
    }

    @Override
    public MasterAccountStatistics masterAccountStatisticsOfSubordinate(MasterAccountListOfSubordinateReqBody reqBody) {
        List<Integer> subordinateUidList = userCache.getSubordinateUidListByUid(reqBody.getUid());
        if (Optional.ofNullable(subordinateUidList).isEmpty() || subordinateUidList.isEmpty()) {
            return processMasterAccountStatistics(null);
        }
        QueryWrapper wrapper = whereMasterAccountOfSubordinate(subordinateUidList);
        selectMasterAccountStatistics(wrapper);
        /** 排除测试账号 **/
        var testUidList = userCache.getTestUidList();
        wrapper.notIn(Optional.ofNullable(testUidList).isPresent() && !testUidList.isEmpty(), Constant.UID, testUidList);
        Map map = coinLogServiceImpl.getMap(wrapper);
        return processMasterAccountStatistics(map);
    }

    private List<MasterAccountList> processMasterAccountListOfSubordinate(ResPage<MasterAccountList> page) {
        List<MasterAccountList> list = new ArrayList<>();
        for (MasterAccountList entity : page.getList()) {
            if (null != entity) {
                Integer uid = entity.getUid();
                UserCacheBo.UserCacheInfo user = userCache.getUserInfoById(uid);
                String username = "";
                if (null != user) {
                    username = user.getUsername();
                }
                List<Integer> tmpUidList = new ArrayList<>();
                tmpUidList.add(uid);
                QueryWrapper wrapper = whereMasterAccountOfSubordinate(tmpUidList);
                selectMasterAccountStatistics(wrapper);
                Map map = coinLogServiceImpl.getMap(wrapper);
                MasterAccountStatistics statistics = processMasterAccountStatistics(map);
                list.add(MasterAccountList.builder()
                        .uid(uid)
                        .username(username)
                        .coinDeposit(statistics.getCoinDeposit())
                        .coinWithdrawal(statistics.getCoinWithdrawal())
                        .coinUp(statistics.getCoinUp())
                        .coinDown(statistics.getCoinDown())
                        .build());
            }
        }
        return list;
    }

    /**
     * SQL查询WHERE条件:下级列表
     *
     * @param uidList 下级UID集合
     * @return QueryWrapper
     */
    private QueryWrapper whereMasterAccountOfSubordinate(List<Integer> uidList) {
        QueryWrapper wrapper = Wrappers.query();
        wrapper.in("uid", uidList);
        return wrapper;
    }

    /**
     * SQL查询WHERE条件:下级列表
     *
     * @param uid UID
     * @return LambdaQueryWrapper
     */
    private LambdaQueryWrapper<UserProfile> whereMasterAccountListOfSubordinate(Integer uid) {
        LambdaQueryWrapper<UserProfile> wrapper = Wrappers.lambdaQuery();
        wrapper.select(UserProfile::getUid);
        wrapper.eq(UserProfile::getSupUid1, uid);
        return wrapper;
    }

    /**
     * SQL查询WHERE条件:列表
     *
     * @param reqBody reqBody
     * @return QueryWrapper
     */
    private QueryWrapper whereMasterAccountList(ListMasterAccountReqBody reqBody) {
        QueryWrapper wrapper = Wrappers.query();
        // 状态:0-处理中 1-成功 2-失败
        wrapper.eq(Constant.STATUS, 1);
        if (null != reqBody) {
            wrapper.eq(null != reqBody.getUid(), Constant.UID, reqBody.getUid());
            wrapper.eq(StringUtils.isNotBlank(reqBody.getUsername()), Constant.USERNAME, reqBody.getUsername());
            wrapper.ge(null != reqBody.getStartTime(), Constant.UPDATED_AT, reqBody.getStartTime());
            wrapper.le(null != reqBody.getEndTime(), Constant.UPDATED_AT, reqBody.getEndTime());
        }
        return wrapper;
    }

    /**
     * SQL查询返回属性:列表
     *
     * @param wrapper QueryWrapper
     */
    private void selectListMasterAccount(QueryWrapper wrapper) {
        wrapper.select("uid",
                "sum(case when category=1 then  coin else 0 end ) as coinDeposit",
                "sum(case when category=2 then  coin else 0 end ) as coinWithdrawal",
                "sum(case when category=3 then  coin else 0 end ) as coinUp",
                "sum(case when category=4 then  coin else 0 end ) as coinDown"
        );
    }

    /**
     * SQL查询返回属性:统计
     *
     * @param wrapper QueryWrapper
     */
    private void selectMasterAccountStatistics(QueryWrapper wrapper) {
        wrapper.select("sum(case when category=1 then  coin else 0 end ) as coinDeposit",
                "sum(case when category=2 then  coin else 0 end ) as coinWithdrawal",
                "sum(case when category=3 then  coin else 0 end ) as coinUp",
                "sum(case when category=4 then  coin else 0 end ) as coinDown"
        );
        wrapper.in("category", 1, 2, 3, 4);
    }
}
