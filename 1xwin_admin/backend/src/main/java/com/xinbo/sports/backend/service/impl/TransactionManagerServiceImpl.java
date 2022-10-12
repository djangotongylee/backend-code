package com.xinbo.sports.backend.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.base.CaseFormat;
import com.xinbo.sports.backend.base.DictionaryBase;
import com.xinbo.sports.backend.io.bo.TransactionManager;
import com.xinbo.sports.backend.io.po.ReportStatisticsPo;
import com.xinbo.sports.backend.redis.GameCache;
import com.xinbo.sports.backend.service.IAdminInfoBase;
import com.xinbo.sports.backend.service.ITransactionManagerService;
import com.xinbo.sports.dao.generator.po.CoinLog;
import com.xinbo.sports.dao.generator.po.GameSlot;
import com.xinbo.sports.dao.generator.service.CoinLogService;
import com.xinbo.sports.service.cache.redis.UserCache;
import com.xinbo.sports.service.common.Constant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.bo.UserCacheBo;
import com.xinbo.sports.service.io.enums.BaseEnum;
import com.xinbo.sports.utils.BeanConvertUtils;
import com.xinbo.sports.utils.components.pagination.BasePage;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.xinbo.sports.service.base.GameStatisticsBase.BET_PREFIX;
import static com.xinbo.sports.service.base.GameStatisticsBase.DIC_PREFIX;
import static com.xinbo.sports.service.common.Constant.GAME_ID_FIELD;

/**
 * <p>
 * 交易管理
 * </p>
 *
 * @author andy
 * @since 2020/6/15
 */
@Service
@Slf4j
public class TransactionManagerServiceImpl implements ITransactionManagerService {
    @Resource
    private CoinLogService coinLogServiceImpl;
    @Resource
    private UserCache userCache;
    @Resource
    private PlatformLeaderBoardServiceImpl platformLeaderBoardServiceImpl;
    @Resource
    private DictionaryBase dictionaryBase;
    @Resource
    private GameCache gameCache;

    @Override
    public ResPage<TransactionManager.TransactionRecord> listTransactionRecord(ReqPage<TransactionManager.ListTransactionRecordReqBody> reqBody) {
        if (null == reqBody) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        Page<CoinLog> coinLogPage = coinLogServiceImpl.page(reqBody.getPage(), whereListTransactionRecord(reqBody.getData()));
        Page<TransactionManager.TransactionRecord> copyPage = BeanConvertUtils.copyPageProperties(coinLogPage, TransactionManager.TransactionRecord::new);
        copyPage.getRecords().stream().parallel().forEach(s -> {
            UserCacheBo.UserCacheInfo userCacheInfo = userCache.getUserInfoById(s.getUid());
            if (null != userCacheInfo) {
                s.setUsername(userCacheInfo.getUsername());
                s.setUserFlagList(userCache.getUserFlagList(userCacheInfo.getUid()));
            }
        });
        return ResPage.get(copyPage);
    }

    @Override
    public TransactionManager.StatisticsTransaction statisticsTransaction(TransactionManager.ListTransactionRecordReqBody reqBody) {
        return getStatistics(reqBody);
    }

    private TransactionManager.StatisticsTransaction getStatistics(TransactionManager.ListTransactionRecordReqBody data) {
        BigDecimal coinDeposit = BigDecimal.ZERO;
        BigDecimal coinWithdrawal = BigDecimal.ZERO;
        BigDecimal coinUp = BigDecimal.ZERO;
        BigDecimal coinDown = BigDecimal.ZERO;
        BigDecimal coinRebate = BigDecimal.ZERO;
        BigDecimal coinCommission = BigDecimal.ZERO;
        BigDecimal coinRewards = BigDecimal.ZERO;
        BigDecimal coinReconciliation = BigDecimal.ZERO;
        QueryWrapper<CoinLog> wrapper = whereListTransactionRecord(data);
        if (null == wrapper) {
            wrapper = Wrappers.query();
        }
        String category = "category";
        String outIn = "out_in";
        wrapper.select(category, "sum(coin) as coin", "out_in as outIn");
        wrapper.groupBy(category, outIn);
        List<CoinLog> list = coinLogServiceImpl.list(wrapper);
        if (!list.isEmpty()) {
            for (CoinLog coinLog : list) {
                BigDecimal coin = coinLog.getCoin();
                Integer category1 = coinLog.getCategory();
                if (null != category1 && 8 == category1 && null != coinLog.getOutIn() && 0 == coinLog.getOutIn()) {
                    coin = coin.multiply(new BigDecimal(-1));
                }
                // 类型:1-存款 2-提款 3-上分 4-下分 5-返水 6-佣金 7-活动(奖励) 8-系统调账
                switch (coinLog.getCategory()) {
                    case 1:
                        coinDeposit = coinDeposit.add(coin);
                        break;
                    case 2:
                        coinWithdrawal = coinWithdrawal.add(coin);
                        break;
                    case 3:
                        coinUp = coinUp.add(coin);
                        break;
                    case 4:
                        coinDown = coinDown.add(coin);
                        break;
                    case 5:
                        coinRebate = coinRebate.add(coin);
                        break;
                    case 6:
                        coinCommission = coinCommission.add(coin);
                        break;
                    case 7:
                        coinRewards = coinRewards.add(coin);
                        break;
                    case 8:
                        coinReconciliation = coinReconciliation.add(coin);
                        break;
                    default:
                        break;
                }
            }
        }
        return TransactionManager.StatisticsTransaction.builder()
                .coinDeposit(coinDeposit)
                .coinWithdrawal(coinWithdrawal)
                .coinUp(coinUp)
                .coinDown(coinDown)
                .coinRebate(coinRebate)
                .coinCommission(coinCommission)
                .coinRewards(coinRewards)
                .coinReconciliation(coinReconciliation)
                .build();
    }

    private QueryWrapper<CoinLog> whereListTransactionRecord(TransactionManager.ListTransactionRecordReqBody data) {
        QueryWrapper<CoinLog> wrapper = Wrappers.query();
        wrapper.eq(null != data.getUid(), Constant.UID, data.getUid());
        wrapper.eq(StringUtils.isNotBlank(data.getUsername()), "username", data.getUsername());
        wrapper.eq(null != data.getCategory(), Constant.CATEGORY, data.getCategory());
        wrapper.eq(null != data.getStatus(), Constant.STATUS, data.getStatus());
        wrapper.ge(null != data.getStartTime(), Constant.UPDATED_AT, data.getStartTime());
        wrapper.le(null != data.getEndTime(), Constant.UPDATED_AT, data.getEndTime());
        wrapper.eq(null != data.getId(), Constant.ID, data.getId()).or();
        wrapper.likeRight(null != data.getId(), "refer_id", data.getId());
        return wrapper;
    }


    /**
     * 交易记录->全平台投注总额->统计
     *
     * @param reqBody reqBody
     * @return PlatBetTotalStatisticsResBody
     */
    @Override
    public TransactionManager.PlatBetTotalStatisticsResBody getPlatBetTotalStatistics(TransactionManager.PlatBetTotalListReqBody reqBody) {
        if (null == reqBody) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        var coin = BigDecimal.ZERO;
        var profit = BigDecimal.ZERO;

        var startTime = reqBody.getStartTime();
        var endTime = reqBody.getEndTime();
        var id = reqBody.getId();
        var platId = reqBody.getPlatId();
        var username = reqBody.getUsername();

        var dataList = platformLeaderBoardServiceImpl.getPlatBetTotalStatistics(startTime, endTime, id, platId, username, null);
        if (Optional.ofNullable(dataList).isPresent()) {
            coin = dataList.stream().map(ReportStatisticsPo::getCoin).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
            profit = dataList.stream().map(ReportStatisticsPo::getProfit).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        }
        return TransactionManager.PlatBetTotalStatisticsResBody.builder()
                .betCoin(coin)
                .profitCoin(profit)
                .build();
    }


    /**
     * 交易记录->全平台投注总额->列表
     *
     * @param reqBody reqBody
     * @return ResPage<TransactionManager.BetTotalListResBody>
     */
    @Override
    public ResPage<TransactionManager.PlatBetTotalListResBody> getPlatBetTotalList(ReqPage<TransactionManager.PlatBetTotalListReqBody> reqBody) {
        if (null == reqBody) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        Integer startTime = null;
        Integer endTime = null;
        String id = null;
        Integer platId = null;
        String username = null;

        if (null != reqBody.getData()) {
            startTime = reqBody.getData().getStartTime();
            endTime = reqBody.getData().getEndTime();
            id = reqBody.getData().getId();
            platId = reqBody.getData().getPlatId();
            username = reqBody.getData().getUsername();

            if (StringUtils.isNotBlank(username)) {
                var user = userCache.getUserInfoByUserName(username);
                if (null == user) {
                    return new ResPage();
                }
            }


            if (null != platId) {
                var tmpPage = platformLeaderBoardServiceImpl.platBetTotalListPageByPlatId(startTime, endTime, id, platId, username, reqBody.getPage(), null);
                return getPlatBetTotalListResPage(tmpPage, tmpPage.getRecords());
            }
        }
        long current = reqBody.getCurrent();
        long size = reqBody.getSize();
        long skip = size * (current - 1);
        var poList = platformLeaderBoardServiceImpl.platBetTotalList(startTime, endTime, id, platId, username, (int) size, null);
        if (Optional.ofNullable(poList).isEmpty() || poList.isEmpty()) {
            return new ResPage<>();
        }

        // 排序
        getPlatBetTotalListOrderByField(reqBody.getSortKey(), reqBody.getSortField(), poList);

        // 内存分页
        var page = new Page<>(current, size, poList.size());
        var tmpList = poList.stream().skip(skip).limit(size).collect(Collectors.toList());
        return getPlatBetTotalListResPage(page, tmpList);

    }

    private ResPage<TransactionManager.PlatBetTotalListResBody> getPlatBetTotalListResPage(Page page, List<ReportStatisticsPo> tmpList) {
        var tmpPage = BeanConvertUtils.copyPageProperties(page, TransactionManager.PlatBetTotalListResBody::new);
        var dataList = new ArrayList<TransactionManager.PlatBetTotalListResBody>();
        // 老虎机缓存
        var gameSlotListCache = gameCache.getGameSlotListCache();
        var currentAdmin = IAdminInfoBase.getHeadLocalData();
        for (var o : tmpList) {
            var bean = BeanConvertUtils.beanCopy(o, TransactionManager.PlatBetTotalListResBody::new);
            // 会员旗帜
            bean.setUserFlagList(userCache.getUserFlagList(o.getUid()));

            // 游戏名称
            String gameName = null;
            // 老虎机
            if (2 == o.getGroupId() && Optional.ofNullable(gameSlotListCache).isPresent()) {
                gameSlotListCache = gameSlotListCache.stream().filter(x -> x.getGameId().equals(o.getGameListId())).collect(Collectors.toList());
                gameName = getGameId2GameNameByGroupId(gameSlotListCache, currentAdmin.getLang(), o.getGameId());
            } else {
                gameName = getGameId2GameName(o.getModel(), o.getGameId());
            }
            bean.setGameName(gameName);
            dataList.add(bean);
        }
        var resPage = ResPage.get(tmpPage);
        resPage.setList(dataList);
        return resPage;
    }

    /**
     * 交易记录->全平台投注总额->列表:排序
     *
     * @param sortKey   sortKey
     * @param sortField sortField
     * @param list      list
     */
    private void getPlatBetTotalListOrderByField(String sortKey, String[] sortField, List<ReportStatisticsPo> list) {
        if (StringUtils.isNotBlank(sortKey) && Optional.ofNullable(sortField).isPresent() && sortField.length > 0) {
            if ("createdAt".equals(sortField[0]) && BasePage.DESC_SORT.equalsIgnoreCase(sortKey)) {
                list.sort((x, y) -> y.getCreatedAt() - x.getCreatedAt());
            } else if ("createdAt".equals(sortField[0]) && BasePage.ASC_SORT.equalsIgnoreCase(sortKey)) {
                list.sort(Comparator.comparingInt(ReportStatisticsPo::getCreatedAt));
            } else if ("updatedAt".equals(sortField[0]) && BasePage.DESC_SORT.equalsIgnoreCase(sortKey)) {
                list.sort((x, y) -> y.getUpdatedAt() - x.getUpdatedAt());
            } else if ("updatedAt".equals(sortField[0]) && BasePage.ASC_SORT.equalsIgnoreCase(sortKey)) {
                list.sort(Comparator.comparingInt(ReportStatisticsPo::getUpdatedAt));
            } else if ("coin".equals(sortField[0]) && BasePage.DESC_SORT.equalsIgnoreCase(sortKey)) {
                list.sort((x, y) -> y.getCoin().compareTo(x.getCoin()));
            } else if ("coin".equals(sortField[0]) && BasePage.ASC_SORT.equalsIgnoreCase(sortKey)) {
                list.sort(Comparator.comparing(ReportStatisticsPo::getCoin));
            } else if ("profit".equals(sortField[0]) && BasePage.DESC_SORT.equalsIgnoreCase(sortKey)) {
                list.sort((x, y) -> y.getProfit().compareTo(x.getProfit()));
            } else if ("profit".equals(sortField[0]) && BasePage.ASC_SORT.equalsIgnoreCase(sortKey)) {
                list.sort(Comparator.comparing(ReportStatisticsPo::getProfit));
            }
        }
    }

    /**
     * 获取游戏名称-非老虎机
     *
     * @param model  model
     * @param gameId gameId
     * @return GameName
     */
    private String getGameId2GameName(String model, String gameId) {
        String gameName = null;
        if (StringUtils.isNotBlank(model) && StringUtils.isNotBlank(gameId)) {
            var json = JSON.parseObject(model);
            var gameIdField = json.getString(GAME_ID_FIELD);
            var businessName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, json.getString(BET_PREFIX));
            var dicKey = DIC_PREFIX + "_" + BET_PREFIX + "_" + businessName + "_" + gameIdField;
            var gameNameMap = dictionaryBase.getCategoryMap(dicKey);
            gameName = gameNameMap.get(gameId);
        }
        return gameName;
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
        var gameNameMap = lang.equals(BaseEnum.LANG.ZH.getValue()) ? gameSlotList.stream().collect(Collectors.toMap(GameSlot::getId, GameSlot::getNameZh)) : gameSlotList.stream().collect(Collectors.toMap(GameSlot::getId, GameSlot::getName));
        return null != gameNameMap ? gameNameMap.get(gameId) : null;
    }

}
