package com.xinbo.sports.service.base;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.xinbo.sports.dao.generator.po.GameList;
import com.xinbo.sports.dao.generator.service.GameListService;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.bo.PlatServiceBaseParams.PlatCoinStatisticsResDto;
import com.xinbo.sports.service.io.bo.PlatServiceBaseParams.PlatGameQueryDateDto;
import com.xinbo.sports.utils.SpringUtils;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.alibaba.fastjson.JSON.toJSONString;
import static java.util.Objects.nonNull;

/**
 * @author: wells
 * @date: 2020/6/12
 * @description:
 */
@Slf4j
@Component
public class GameStatisticsBase {
    /*投注金额字段*/
    public static final String COIN_FIELD = "xb_coin";
    /*有效投注金额字段*/
    public static final String BET_FIELD = "xb_valid_coin";
    /*盈亏字段*/
    public static final String WIN_FIELD = "xb_profit";
    /*创建时间*/
    public static final String CREATED_AT = "created_at";
    /*注单表service实例表前缀*/
    public static final String BET_PREFIX = "betslips";
    /*注单表service实例表后缀*/
    public static final String BET_SUFFIX = "ServiceImpl";
    /*游戏Id的映射字段*/
    public static final String GAME_ID_FIELD = "gameIdField";
    /*字典表前缀*/
    public static final String DIC_PREFIX = "dic";
    @Autowired
    private GameListService gameListServiceImpl;

    /**
     * 获取游戏 投注总金额、盈亏总金额
     *
     * @return
     */
    public PlatCoinStatisticsResDto getCoinStatisticsByDate(@Valid PlatGameQueryDateDto reqDto) {
        //获取游戏的基本配置信息
        var gameLists = getConfig(reqDto);
        //有效金额、盈亏总金额
        var coin = BigDecimal.ZERO;
        var coinBet = BigDecimal.ZERO;
        var coinProfit = BigDecimal.ZERO;
        try {
            var futureList = getFuture(gameLists, reqDto);
            for (Triple<BigDecimal, BigDecimal, BigDecimal> future : futureList) {
                coinBet = coinBet.add(future.getLeft());
                coin = coin.add(future.getMiddle());
                coinProfit = coinProfit.add(future.getRight());
            }
        } catch (Exception e) {
            log.info("获取三方有效注单金额失败！" + e.getMessage());
        }
        return PlatCoinStatisticsResDto.builder().coinBet(coinBet).coin(coin).coinProfit(coinProfit).build();
    }

    /**
     * 多线程获取各个游戏的有效投注
     *
     * @param gameLists
     * @param reqDto
     * @return
     */
    public List<Triple<BigDecimal, BigDecimal, BigDecimal>> getFuture(List<GameList> gameLists, PlatGameQueryDateDto reqDto) throws ExecutionException, InterruptedException {
        //创建线程池
        ThreadPoolExecutor pool = new ThreadPoolExecutor(gameLists.size(), gameLists.size(), 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(100));
        ListeningExecutorService guavaExecutors = MoreExecutors.listeningDecorator(pool);
        var futureList = new ArrayList<ListenableFuture<Triple<BigDecimal, BigDecimal, BigDecimal>>>();
        for (var game : gameLists) {
            var futurePair = guavaExecutors.submit(new Callable<Triple<BigDecimal, BigDecimal, BigDecimal>>() {
                @Override
                public Triple<BigDecimal, BigDecimal, BigDecimal> call() throws Exception {
                    var configJsonObject = parseObject(game.getModel());
                    var betslips = BET_PREFIX + configJsonObject.getString(BET_PREFIX) + BET_SUFFIX;
                    var iService = (IService) SpringUtils.getBean(betslips);
                    var stringBuilder = new StringBuilder();
                    //有效投注金额字段
                    stringBuilder.append("ifNull(sum(" + BET_FIELD + "),0 )as  " + BET_FIELD);
                    //投注金额
                    stringBuilder.append(",ifNull(sum(" + COIN_FIELD + "),0 )as  " + COIN_FIELD);
                    //盈亏字段
                    stringBuilder.append(",ifNull(sum(" + WIN_FIELD + "),0)as " + WIN_FIELD);

                    var queryWrapper = new QueryWrapper<>()
                            .select(stringBuilder.toString())
                            .eq(nonNull(reqDto.getUid()), "xb_uid", reqDto.getUid())
                            .in(!CollectionUtils.isEmpty(reqDto.getUidList()), "xb_uid", reqDto.getUidList())
                            .ge(nonNull(reqDto.getStartTime()), CREATED_AT, reqDto.getStartTime())
                            .le(nonNull(reqDto.getEndTime()), CREATED_AT, reqDto.getEndTime())
                            .last("limit 1");
                    var object = iService.getMap(queryWrapper);
                    if (nonNull(object)) {
                        var reJson = parseObject(toJSONString(object));
                        return Triple.of(reJson.getBigDecimal(BET_FIELD), reJson.getBigDecimal(COIN_FIELD), reJson.getBigDecimal(WIN_FIELD));
                    }
                    return Triple.of(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
                }
            });
            futureList.add(futurePair);
        }
        var resultsFuture = Futures.successfulAsList(futureList);
        return resultsFuture.get();
    }

    /**
     * 获取游戏的基本配置信息
     *
     * @param reqDto
     * @return
     */
    public List<GameList> getConfig(@Valid PlatGameQueryDateDto reqDto) {
        var gameLists = gameListServiceImpl.lambdaQuery()
                .eq(nonNull(reqDto.getGameId()), GameList::getId, reqDto.getGameId())
                .eq(nonNull(reqDto.getPlatId()), GameList::getGroupId, reqDto.getPlatId())
                //启动状态
                .eq(GameList::getStatus, 1)
                .list();
        //判断游戏是否存在
        if (CollectionUtils.isEmpty(gameLists)) {
            throw new BusinessException(CodeInfo.GAME_RECORDS_EMPTY);
        }
        return gameLists;
    }

}
