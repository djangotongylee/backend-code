package com.xinbo.sports.backend.service.impl;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.xinbo.sports.backend.base.DictionaryBase;
import com.xinbo.sports.backend.io.bo.ReportCenter;
import com.xinbo.sports.backend.io.po.ReportStatisticsPo;
import com.xinbo.sports.backend.redis.GameCache;
import com.xinbo.sports.dao.generator.po.GameSlot;
import com.xinbo.sports.dao.generator.po.UserLevel;
import com.xinbo.sports.service.cache.redis.UserCache;
import com.xinbo.sports.service.common.Constant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.enums.BaseEnum;
import com.xinbo.sports.service.thread.ThreadHeaderLocalData;
import com.xinbo.sports.utils.BeanConvertUtils;
import com.xinbo.sports.utils.components.pagination.BasePage;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * <p>
 * 各平台盈亏报表Service
 * </p>
 *
 * @author andy
 * @since 2020/12/10
 */
@Service
@Slf4j
public class PlatformProfitReportServiceImpl {
    @Resource
    private DictionaryBase dictionaryBase;
    @Resource
    private GameCache gameCache;
    @Resource
    private UserCache userCache;

    @Resource
    private PlatformLeaderBoardServiceImpl platformLeaderBoardServiceImpl;
    @Resource
    private DailyReportExStatisticsServiceImpl dailyReportExStatisticsServiceImpl;

    private static final AtomicInteger REPORT_THREAD_POOL_ID = new AtomicInteger();


    private ThreadPoolExecutor getPool() {
        return new ThreadPoolExecutor(
                32,
                48,
                2L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                x -> new Thread(x, "各平台盈亏报表_POOL_" + REPORT_THREAD_POOL_ID.getAndIncrement()));
    }

    /**
     * 各平台盈亏报表->统计
     *
     * @param reqBody
     * @return
     */
    public ReportCenter.PlatformProfitResBody getPlatformProfit(ReportCenter.PlatformProfitListReqBody reqBody) {
        if (null == reqBody) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }


        List<Integer> levelUidList = null;
        List<Integer> searchUidList = null;
        /** 排除测试账号 **/
        var testUidList = userCache.getTestUidList();
        var groupId = reqBody.getGroupId();
        var gameListId = reqBody.getGameListId();
        var gameId = reqBody.getGameId();
        var levelId = reqBody.getLevelId();
        // 类型:1-代理 2-会员
        var type = reqBody.getType();
        var username = reqBody.getUsername();
        var startTime = reqBody.getStartTime();
        var endTime = reqBody.getEndTime();

        if (null != levelId) {
            levelUidList = userCache.getUidListByLevelId(levelId);
            if (Optional.ofNullable(levelUidList).isEmpty() || levelUidList.isEmpty()) {
                return ReportCenter.PlatformProfitResBody.builder().totalProfit(BigDecimal.ZERO).build();
            }
            searchUidList = levelUidList;
        }

        if (StringUtils.isNotBlank(username)) {
            if (null == type || !List.of(1, 2).contains(type)) {
                throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
            }
            var user = userCache.getUserInfoByUserName(username);
            if (null == user) {
                return ReportCenter.PlatformProfitResBody.builder().totalProfit(BigDecimal.ZERO).build();
            }

            if (Constant.USER_ROLE_CS == user.getRole()) {
                /** 测试账号不能查询报表数据 **/
                throw new BusinessException(CodeInfo.REPORT_TEST_USER_NOT_SEARCH_DATA);
            }

            searchUidList = dailyReportExStatisticsServiceImpl.searchUserName2Uid(username, type);
            if (null == searchUidList || searchUidList.isEmpty()) {
                return ReportCenter.PlatformProfitResBody.builder().totalProfit(BigDecimal.ZERO).build();
            }
            if (Optional.ofNullable(levelUidList).isPresent() && !levelUidList.isEmpty()) {
                searchUidList = searchUidList.stream().filter(levelUidList::contains).collect(toList());
            }
            if (Optional.ofNullable(searchUidList).isEmpty() || searchUidList.isEmpty()) {
                return ReportCenter.PlatformProfitResBody.builder().totalProfit(BigDecimal.ZERO).build();
            }
        }

        var poList = platformLeaderBoardServiceImpl.getPlatformProfit(startTime, endTime, gameId, gameListId, groupId, testUidList, searchUidList);
        if (Optional.ofNullable(poList).isEmpty() || poList.isEmpty()) {
            return ReportCenter.PlatformProfitResBody.builder().totalProfit(BigDecimal.ZERO).build();
        }
        var profit = poList.stream().map(ReportStatisticsPo::getProfit).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        return ReportCenter.PlatformProfitResBody.builder().totalProfit(profit).build();
    }

    /**
     * 各平台盈亏报表->导出列表
     *
     * @param reqBody
     * @return
     */
    public List<ReportCenter.PlatformProfitListResBody> exportPlatformProfitList(ReportCenter.PlatformProfitListReqBody reqBody) {
        if (null == reqBody) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        List<ReportCenter.PlatformProfitListResBody> exportList = new ArrayList<>();


        List<Integer> levelUidList = null;
        List<Integer> searchUidList = null;
        /** 排除测试账号 **/
        var testUidList = userCache.getTestUidList();


        var currentLoginAdmin = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        String groupName = ALL_TEXT;
        String gameListName = ALL_TEXT;
        String gameName = ALL_TEXT;
        String levelText = null;

        var groupId = reqBody.getGroupId();
        var gameListId = reqBody.getGameListId();
        var gameId = reqBody.getGameId();
        var levelId = reqBody.getLevelId();
        // 类型:1-代理 2-会员
        var type = reqBody.getType();
        var username = reqBody.getUsername();
        var startTime = reqBody.getStartTime();
        var endTime = reqBody.getEndTime();

        if (null != groupId) {
            groupName = getGroupName(groupId);
        }

        if (null != levelId) {
            levelUidList = userCache.getUidListByLevelId(levelId);
            if (Optional.ofNullable(levelUidList).isEmpty() || levelUidList.isEmpty()) {
                return exportList;
            }
            levelText = getLevelTextByLevelId(levelId);
        }

        if (StringUtils.isNotBlank(username)) {
            if (null == type || !List.of(1, 2).contains(type)) {
                throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
            }
            var user = userCache.getUserInfoByUserName(username);
            if (null == user) {
                return exportList;
            }

            if (Constant.USER_ROLE_CS == user.getRole()) {
                /** 测试账号不能查询报表数据 **/
                throw new BusinessException(CodeInfo.REPORT_TEST_USER_NOT_SEARCH_DATA);
            }

            searchUidList = dailyReportExStatisticsServiceImpl.searchUserName2Uid(username, type);
            if (null == searchUidList || searchUidList.isEmpty()) {
                return exportList;
            }
            if (Optional.ofNullable(levelUidList).isPresent() && !levelUidList.isEmpty()) {
                searchUidList = searchUidList.stream().filter(levelUidList::contains).collect(toList());
            }
        }

        long maxSize = 3200L;
        long size = null != gameListId ? maxSize : 200;

        // SQL查询依赖实体
        var reqPo = new ReportStatisticsPo();
        reqPo.setStartTime(startTime);
        reqPo.setEndTime(endTime);
        reqPo.setGameId(gameId);
        reqPo.setGroupId(groupId);
        reqPo.setGameListId(gameListId);
        reqPo.setSize((int) size);
        reqPo.setUidList(searchUidList);
        /** 排除测试账号 **/
        reqPo.setTestUidList(testUidList);
        var reportStatisticsPoList = platformLeaderBoardServiceImpl.getPlatformProfitList(reqPo);
        if (Optional.ofNullable(reportStatisticsPoList).isEmpty() || reportStatisticsPoList.isEmpty()) {
            return exportList;
        }

        var dataList = BeanConvertUtils.copyListProperties(reportStatisticsPoList, ReportCenter.PlatformProfitListResBody::new);

        if (null != groupId && null != gameListId && StringUtils.isNotBlank(gameId)) {
            gameName = getGameName(groupId, gameListId, gameId, currentLoginAdmin.getLang());
        }

        if (dataList.size() > maxSize) {
            dataList = dataList.stream().limit(maxSize).collect(toList());
        }


        // UID分组
        var groupingByUserName = dataList.stream().collect(
                Collectors.groupingBy(
                        ReportCenter.PlatformProfitListResBody::getUid,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                ReportCenter.PlatformProfitListResBody::getProfit,
                                BigDecimal::add))
        );

        dataList = dataList.stream().filter(distinctByKey(ReportCenter.PlatformProfitListResBody::getUid)).collect(toList());
        dataList.forEach(o -> {
            if (groupingByUserName.containsKey(o.getUid())) {
                o.setProfit(groupingByUserName.get(o.getUid()));
            }
        });


        int limitSize = 100;
        if (dataList.size() > limitSize) {
            List<Future<List<ReportCenter.PlatformProfitListResBody>>> futureList = new ArrayList<>();
            try {
                List<List<ReportCenter.PlatformProfitListResBody>> partitionList = Lists.partition(dataList, limitSize);
                for (var tmpList : partitionList) {
                    var finalGroupName = groupName;
                    var finalGameName = gameName;
                    var finalLevelText = levelText;
                    futureList.add(getPool().submit(() -> processDataList(tmpList, finalGroupName, gameListName, finalGameName, finalLevelText)));
                }
                for (var future : futureList) {
                    exportList.addAll(future.get());
                }


                getPlatBetTotalListOrderByField("desc", new String[]{PROFIT}, exportList);
                return exportList;
            } catch (Exception e) {
                String error = "各平台盈亏报表->导出列表异常";
                log.error("{}", error, e.getMessage(), e);
            }
        }

        // 数据处理
        processDataList(dataList, groupName, gameListName, gameName, levelText);
        // 排序
        getPlatBetTotalListOrderByField("desc", new String[]{PROFIT}, dataList);
        return dataList;
    }

    /**
     * 各平台盈亏报表->列表
     *
     * @param reqBody
     * @return
     */
    public ResPage<ReportCenter.PlatformProfitListResBody> getPlatformProfitList(ReqPage<ReportCenter.PlatformProfitListReqBody> reqBody) {
        if (null == reqBody) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        var resBodyResPage = new ResPage<ReportCenter.PlatformProfitListResBody>();

        Integer groupId = null;
        Integer gameListId = null;
        String gameId = null;
        Integer startTime = null;
        Integer endTime = null;
        Integer type = null;

        List<Integer> levelUidList = null;
        List<Integer> searchUidList = null;
        /** 排除测试账号 **/
        var testUidList = userCache.getTestUidList();
        var data = reqBody.getData();


        var currentLoginAdmin = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        String groupName = ALL_TEXT;
        String gameListName = ALL_TEXT;
        String gameName = ALL_TEXT;
        String levelText = null;

        if (null != data) {
            groupId = data.getGroupId();
            gameListId = data.getGameListId();
            gameId = data.getGameId();
            var levelId = data.getLevelId();
            type = data.getType();
            var username = data.getUsername();
            startTime = data.getStartTime();
            endTime = data.getEndTime();

            if (null != levelId) {
                levelUidList = userCache.getUidListByLevelId(levelId);
                if (Optional.ofNullable(levelUidList).isEmpty() || levelUidList.isEmpty()) {
                    return resBodyResPage;
                }
                searchUidList = levelUidList;
                levelText = getLevelTextByLevelId(levelId);
            }

            if (null != groupId) {
                groupName = getGroupName(groupId);
            }

            if (StringUtils.isNotBlank(username)) {
                // 类型:1-代理 2-会员
                if (null == type || !List.of(1, 2).contains(type)) {
                    throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
                }
                var user = userCache.getUserInfoByUserName(username);
                if (null == user) {
                    return resBodyResPage;
                }

                if (Constant.USER_ROLE_CS == user.getRole()) {
                    /** 测试账号不能查询报表数据 **/
                    throw new BusinessException(CodeInfo.REPORT_TEST_USER_NOT_SEARCH_DATA);
                }

                searchUidList = dailyReportExStatisticsServiceImpl.searchUserName2Uid(username, type);
                if (null == searchUidList || searchUidList.isEmpty()) {
                    return resBodyResPage;
                }
                if (Optional.ofNullable(levelUidList).isPresent() && !levelUidList.isEmpty()) {
                    searchUidList = searchUidList.stream().filter(levelUidList::contains).collect(toList());
                }
                if (Optional.ofNullable(searchUidList).isEmpty() || searchUidList.isEmpty()) {
                    return resBodyResPage;
                }
            }

            if (null != gameListId) {
                gameListName = getGameListName(gameListId);

                if (StringUtils.isNotBlank(gameId)) {
                    gameName = getGameName(groupId, gameListId, gameId, currentLoginAdmin.getLang());
                }

                // SQL查询依赖实体
                var reqPo = new ReportStatisticsPo();
                reqPo.setStartTime(startTime);
                reqPo.setEndTime(endTime);
                reqPo.setGameId(gameId);
                reqPo.setGroupId(groupId);
                reqPo.setGameListId(gameListId);
                reqPo.setUidList(searchUidList);
                /** 排除测试账号 **/
                reqPo.setTestUidList(testUidList);
                var page1 = reqBody.getPage();
                setOrders(reqBody.getSortField(), page1);
                var page = platformLeaderBoardServiceImpl.getPlatformProfitListPageByPlatId(reqPo, page1);
                var tmpPage = BeanConvertUtils.copyPageProperties(page, ReportCenter.PlatformProfitListResBody::new);
                processDataList(tmpPage.getRecords(), groupName, gameListName, gameName, levelText);
                return ResPage.get(tmpPage);
            }
        }

        // SQL查询依赖实体
        var reqPo = new ReportStatisticsPo();
        reqPo.setStartTime(startTime);
        reqPo.setEndTime(endTime);
        reqPo.setGameId(gameId);
        reqPo.setGroupId(groupId);
        reqPo.setGameListId(gameListId);
        reqPo.setSize(100);
        reqPo.setUidList(searchUidList);
        /** 排除测试账号 **/
        reqPo.setTestUidList(testUidList);
        var reportStatisticsPoList = platformLeaderBoardServiceImpl.getPlatformProfitList(reqPo);
        if (Optional.ofNullable(reportStatisticsPoList).isEmpty() || reportStatisticsPoList.isEmpty()) {
            return new ResPage<>();
        }

        log.info("====reportStatisticsPoList.size={}", reportStatisticsPoList.size());

        var dataList = BeanConvertUtils.copyListProperties(reportStatisticsPoList, ReportCenter.PlatformProfitListResBody::new);


        // UID分组
        var groupingByUserName = dataList.stream().collect(
                Collectors.groupingBy(
                        ReportCenter.PlatformProfitListResBody::getUid,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                ReportCenter.PlatformProfitListResBody::getProfit,
                                BigDecimal::add))
        );
        log.info("========groupingByUserName.size={}", groupingByUserName.size());
        log.info("===000======dataList.size={}", dataList.size());
        dataList = dataList.stream().filter(distinctByKey(ReportCenter.PlatformProfitListResBody::getUid)).collect(toList());
        log.info("===111======dataList.size={}", dataList.size());
        dataList.forEach(o -> {
            if (groupingByUserName.containsKey(o.getUid())) {
                o.setProfit(groupingByUserName.get(o.getUid()));
            }
        });
        log.info("===222======dataList.size={}", dataList.size());

        // 数据处理
        processDataList(dataList, groupName, gameListName, gameName, levelText);

        // 排序
        getPlatBetTotalListOrderByField(reqBody.getSortKey(), reqBody.getSortField(), dataList);

        // 内存分页
        Page<ReportCenter.PlatformProfitListResBody> page = new Page<>(reqBody.getCurrent(), reqBody.getSize(), dataList.size());
        dataList = dataList.stream().skip(page.getSize() * (page.getCurrent() - 1)).limit(page.getSize()).collect(Collectors.toList());
        var resPage = ResPage.get(page);
        resPage.setList(dataList);
        return resPage;
    }

    private void setOrders(String[] sortField, Page page) {
        if (Optional.ofNullable(sortField).isEmpty() || sortField.length == 0 || !sortField[0].equals(PROFIT)) {
            page.setOrders(OrderItem.descs(PROFIT));
        }
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    private List<ReportCenter.PlatformProfitListResBody> processDataList(List<ReportCenter.PlatformProfitListResBody> tmpDataList, String groupName, String gameListName, String gameName, String levelText) {
        for (var entity : tmpDataList) {
            Integer uid = entity.getUid();
            // 会员旗帜
            entity.setUserFlagList(userCache.getUserFlagList(uid));
            // 会员等级 -> vip1-乒乓球达人
            var userInfo = userCache.getUserInfoById(uid);
            if (null != userInfo) {
                entity.setLevelText(StringUtils.isNotBlank(levelText) ? levelText : getLevelTextByLevelId(userInfo.getLevelId()));
                entity.setCreatedAt(userInfo.getCreatedAt());
                if (null != userInfo.getRole()) {
                    // 会员类型:0-会员 1-代理 2-总代理 3-股东 4-测试 10-系统账号'
                    entity.setTypeName(1 == userInfo.getRole() ? ROLE_TEXT_1 : ROLE_TEXT_0);
                }
            }
            // 设置游戏类型名称(游戏组名称)
            entity.setGroupName(groupName);
            entity.setGameListName(gameListName);
            entity.setGameName(gameName);
        }
        return tmpDataList;
    }

    /**
     * 各平台盈亏报表->列表:排序
     *
     * @param sortKey   sortKey
     * @param sortField sortField
     * @param list      list
     */
    private void getPlatBetTotalListOrderByField(String sortKey, String[]
            sortField, List<ReportCenter.PlatformProfitListResBody> list) {
        if (StringUtils.isNotBlank(sortKey) && Optional.ofNullable(sortField).isPresent() && sortField.length > 0) {
            if ("createdAt".equals(sortField[0]) && BasePage.DESC_SORT.equalsIgnoreCase(sortKey)) {
                list.sort((x, y) -> y.getCreatedAt() - x.getCreatedAt());
            } else if ("createdAt".equals(sortField[0]) && BasePage.ASC_SORT.equalsIgnoreCase(sortKey)) {
                list.sort(Comparator.comparingInt(ReportCenter.PlatformProfitListResBody::getCreatedAt));
            } else if (PROFIT.equals(sortField[0]) && BasePage.DESC_SORT.equalsIgnoreCase(sortKey)) {
                list.sort((x, y) -> y.getProfit().compareTo(x.getProfit()));
            } else if (PROFIT.equals(sortField[0]) && BasePage.ASC_SORT.equalsIgnoreCase(sortKey)) {
                list.sort(Comparator.comparing(ReportCenter.PlatformProfitListResBody::getProfit));
            } else if ("typeName".equals(sortField[0]) && BasePage.DESC_SORT.equalsIgnoreCase(sortKey)) {
                list.sort((x, y) -> y.getTypeName().compareTo(x.getTypeName()));
            } else if ("typeName".equals(sortField[0]) && BasePage.ASC_SORT.equalsIgnoreCase(sortKey)) {
                list.sort(Comparator.comparing(ReportCenter.PlatformProfitListResBody::getTypeName));
            } else if ("levelText".equals(sortField[0]) && BasePage.DESC_SORT.equalsIgnoreCase(sortKey)) {
                list.sort((x, y) -> y.getLevelText().compareTo(x.getLevelText()));
            } else if ("levelText".equals(sortField[0]) && BasePage.ASC_SORT.equalsIgnoreCase(sortKey)) {
                list.sort(Comparator.comparing(ReportCenter.PlatformProfitListResBody::getLevelText));
            }
        }
    }

    /**
     * 获取游戏名称-老虎机
     *
     * @param gameSlotList 老虎机列表
     * @param lang         语言
     * @param gameId       gameId
     * @return GameName
     */
    private String getGameId2GameNameByGroupId(List<GameSlot> gameSlotList, String lang, String gameId) {
        var gameNameMap = lang.equals(BaseEnum.LANG.ZH.getValue()) ? gameSlotList.stream().filter(Objects::nonNull).collect(Collectors.toMap(GameSlot::getId, GameSlot::getNameZh)) : gameSlotList.stream().filter(Objects::nonNull).collect(Collectors.toMap(GameSlot::getId, GameSlot::getName));
        return null != gameNameMap ? gameNameMap.get(gameId) : null;
    }

    /**
     * 获取会员等级by等级ID
     *
     * @param levelId 等级ID
     * @return 会员等级:vip1-乒乓球达人
     */
    private String getLevelTextByLevelId(Integer levelId) {
        String levelText = null;
        UserLevel userLevel = userCache.getUserLevelById(levelId);
        if (null != userLevel) {
            levelText = userLevel.getCode() + " - " + userLevel.getName();
        }
        return levelText;
    }

    private String getGroupName(Integer groupId) {
        return gameCache.getGameGroupCache().stream().filter(o -> o.getId().equals(groupId)).findAny().get().getNameAbbr();
    }

    private String getGameListName(Integer gameListId) {
        return gameCache.getGameListCache(gameListId).getName();
    }

    private String getGameName(Integer groupId, Integer gameListId, String gameId, String lang) {
        String gameName = "--";
        // 游戏列表(老虎机)
        if (2 == groupId) {
            var gameSlotListCache = gameCache.getGameSlotListCache();
            if (Optional.ofNullable(gameSlotListCache).isPresent() && !gameSlotListCache.isEmpty()) {
                gameName = getGameId2GameNameByGroupId(gameSlotListCache, lang, gameId);
            }
        } else {
            var gameList = gameCache.getGameListCache(gameListId);
            if (null != gameList) {
                var map = dictionaryBase.getDictItemMapByModel(gameList.getModel());
                gameName = map.get(gameId);
            }
        }
        return gameName;
    }


    private static final String ALL_TEXT = "--";
    private static final String ROLE_TEXT_0 = "会员";
    private static final String ROLE_TEXT_1 = "代理";
    private static final String PROFIT = "profit";
}
