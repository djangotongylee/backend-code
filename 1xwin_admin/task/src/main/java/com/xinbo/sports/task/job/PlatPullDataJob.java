package com.xinbo.sports.task.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinbo.sports.dao.generator.po.BetSlipsException;
import com.xinbo.sports.dao.generator.po.BetSlipsSupplemental;
import com.xinbo.sports.dao.generator.po.CoinPlatTransfer;
import com.xinbo.sports.dao.generator.po.GameList;
import com.xinbo.sports.dao.generator.service.BetSlipsExceptionService;
import com.xinbo.sports.dao.generator.service.BetSlipsSupplementalService;
import com.xinbo.sports.dao.generator.service.GameListService;
import com.xinbo.sports.plat.aop.annotation.ExceptionEnum;
import com.xinbo.sports.plat.aop.annotation.ThirdPlatException;
import com.xinbo.sports.plat.factory.PlatAbstractFactory;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams;
import com.xinbo.sports.service.base.ExceptionThreadLocal;
import com.xinbo.sports.service.base.NoticeBase;
import com.xinbo.sports.service.base.PlatServiceBase;
import com.xinbo.sports.service.cache.KeyConstant;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.task.base.GameCommonBase;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.JedisUtil;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.log.XxlJobLogger;
import io.jsonwebtoken.lang.Collections;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.IntFunction;

import static com.alibaba.fastjson.JSON.parseObject;

/**
 * @author: wells
 * @date: 2020/7/9
 * @description: 三方拉单定时任务
 */
@Component
@Slf4j
public class PlatPullDataJob {
    @Autowired
    private static final String GAME_ID = "gameId";
    @Autowired
    ConfigCache configCache;
    @Resource
    private PlatServiceBase platServiceBase;
    @Resource
    private NoticeBase noticeBase;

    /**
     * 异常日志打印函数
     */
    BiConsumer<String, Exception> biConsumer = (message, exception) -> {
        XxlJobLogger.log(message + exception);
        StackTraceElement[] stackTrace = exception.getStackTrace();
        for (StackTraceElement element : stackTrace) {
            XxlJobLogger.log(element.toString());
        }
    };
    @Autowired
    private GameListService gameListServiceImpl;
    @Autowired
    private BetSlipsSupplementalService betSlipsSupplementalServiceImpl;
    @Autowired
    private BetSlipsExceptionService betSlipsExceptionServiceImpl;
    @Autowired
    private GameCommonBase gameCommonBase;
    @Autowired
    private JedisUtil jedisUtil;
    /**
     * 获取工厂抽象类
     */
    IntFunction<PlatAbstractFactory> platFunction = gameId -> {
        var gameList = gameCommonBase.getGameListCache(gameId);
        var model = parseObject(gameList.getModel()).getString("model");
        var parent = parseObject(gameList.getModel()).getString("parent");
        return PlatAbstractFactory.init(model, parent);
    };

    /**
     * 拉取三方数据
     */
    @SneakyThrows
    @XxlJob("pullData")
    @ThirdPlatException(exceptionType = ExceptionEnum.PULL_EXCEPTION)
    public ReturnT<String> pullData(String param) {
        if (StringUtils.isNotEmpty(param)) {
            var gameId = parseObject(param).getInteger(GAME_ID);
            var gameList = gameCommonBase.getGameListCache(gameId);
            var status = gameList.getStatus();
            if (2 == status) {
                XxlJobLogger.log("很抱歉，" + configCache.getPlatPrefix() + ":" + gameId + "维护中～～～～");
                return ReturnT.SUCCESS;
            }
            var platAbstractFactory = platFunction.apply(gameId);
            XxlJobLogger.log("进入" + configCache.getPlatPrefix() + "拉单");
            platAbstractFactory.pullBetsLips();
            return ReturnT.SUCCESS;
        } else {
            XxlJobLogger.log("请配置三方信息，样例：{\"gameId\":401,\"parent\":\"GG\",\"model\":\"GGFishingGame\"}");
            return ReturnT.FAIL;
        }
    }


    /**
     * 拆解补单信息
     */
    @SneakyThrows
    @XxlJob("supplementData")
    public ReturnT<String> supplementData(String param) {
        try {
            XxlJobLogger.log("进入" + configCache.getPlatPrefix() + "supplementData");
            // 昨天的开始时间、今天的开始时间
            ZonedDateTime now = DateNewUtils.utc8Zoned(DateNewUtils.now());
            int startTime = (int) now.minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0).toEpochSecond();
            int endTime = (int) now.withHour(0).withMinute(0).withSecond(0).withNano(0).toEpochSecond();
            List<GameList> gameLists = gameListServiceImpl.lambdaQuery()
                    .eq(GameList::getStatus, 1)
                    .list();
            gameLists.forEach(gameList -> {
                var platAbstractFactory = platFunction.apply(gameList.getId());
                var reqDto = PlatFactoryParams.GenSupplementsOrdersReqDto.builder()
                        .gameId(gameList.getId())
                        .start(startTime)
                        .end(endTime)
                        .build();
                platAbstractFactory.genSupplementsOrders(reqDto);
            });
        } catch (Exception e) {
            biConsumer.accept("每日补单分解信息异常", e);
        }
        return ReturnT.SUCCESS;
    }

    /**
     * 扫描补单信息
     */
    @SneakyThrows
    @XxlJob("scanSupplement")
    @ThirdPlatException(exceptionType = ExceptionEnum.SUPPLEMENT_EXCEPTION)
    public ReturnT<String> scanSupplement(String param) {
        if (StringUtils.isNotEmpty(param)) {
            var json = parseObject(param);
            var gameId = json.getInteger(GAME_ID);
            var platAbstractFactory = platFunction.apply(gameId);
            var supplemental = betSlipsSupplementalServiceImpl.getOne(new LambdaQueryWrapper<BetSlipsSupplemental>()
                            .eq(BetSlipsSupplemental::getGameListId, gameId)
                            .eq(BetSlipsSupplemental::getStatus, 0)
                            .orderByAsc(BetSlipsSupplemental::getTimeStart),
                    false);
            XxlJobLogger.log("进入" + configCache.getPlatPrefix() + "scanSupplement");
            if (Objects.nonNull(supplemental)) {
                //加入异常信息
                ExceptionThreadLocal.setOrderId(supplemental.getId());
                var reqDto = PlatFactoryParams.BetsRecordsSupplementReqDto.builder().requestInfo(supplemental.getRequest()).build();
                platAbstractFactory.betsRecordsSupplemental(reqDto);
                //修改补单记录状态
                supplemental.setStatus(1);
                supplemental.setUpdatedAt(DateNewUtils.now());
                betSlipsSupplementalServiceImpl.updateById(supplemental);
            } else {
                XxlJobLogger.log("无补单记录");
            }
        } else {
            XxlJobLogger.log("请配置三方信息，样例：{\"gameId\":401}");
            return ReturnT.FAIL;
        }
        return ReturnT.SUCCESS;
    }

    /**
     * 回归拉单异常
     */
    @SneakyThrows
    @XxlJob("regressionPullData")
    @ThirdPlatException(exceptionType = ExceptionEnum.REGRESSION_EXCEPTION)
    public ReturnT<String> regressionPullData(String param) {
        if (StringUtils.isNotEmpty(param)) {
            var json = parseObject(param);
            var gameId = json.getInteger(GAME_ID);
            XxlJobLogger.log("进入" + configCache.getPlatPrefix() + "regressionPullData");
            var betSlipException = betSlipsExceptionServiceImpl.getOne(new LambdaQueryWrapper<BetSlipsException>()
                            .eq(BetSlipsException::getStatus, 0)
                            .eq(BetSlipsException::getGameListId, gameId)
                            .orderByAsc(),
                    false);
            if (Objects.nonNull(betSlipException)) {
                var dto = ExceptionThreadLocal.ExceptionDto.builder().orderId(betSlipException.getId()).build();
                ExceptionThreadLocal.THREAD_LOCAL.set(dto);
                var platAbstractFactory = platFunction.apply(gameId);
                var reqDto = PlatFactoryParams.BetsRecordsSupplementReqDto.builder().requestInfo(betSlipException.getRequest()).build();
                platAbstractFactory.betsRecordsSupplemental(reqDto);
                //修改拉单异常信息
                betSlipException.setStatus(1);
                betSlipException.setUpdatedAt(DateNewUtils.now());
                betSlipsExceptionServiceImpl.updateById(betSlipException);
            }
        } else {
            XxlJobLogger.log("请配置三方信息，样例：{\"gameId\":401}");
            return ReturnT.FAIL;
        }
        return ReturnT.SUCCESS;
    }

    /**
     * 检查转账状态
     */
    @SneakyThrows
    @XxlJob("checkTransferStatus")
    public ReturnT<String> checkTransferStatus(String param) {
        //查询所有的游戏
        var gameList = gameCommonBase.getGameListCacheAll();
        XxlJobLogger.log("进入" + configCache.getPlatPrefix() + "检查转账状态");
        for (var game : gameList) {
            var platAbstractFactory = platFunction.apply(game.getId());
            // 获取申请中的数据,状态:0-提交申请 1-成功 2-失败
            List<CoinPlatTransfer> list = platServiceBase.getCoinPlatTransferList(game.getPlatListId(), 0);
            if (Optional.ofNullable(list).isPresent() && !list.isEmpty()) {
                for (CoinPlatTransfer p : list) {
                    try {
                        Boolean result = platAbstractFactory.checkTransferStatus(p.getId().toString());
                        platServiceBase.updateCoinPlatTransferStatusById(p, result);
                    } catch (Exception e) {
                        log.error("====检查转账状态异常:{}", e.getMessage(), e);
                    }
                }
            }
        }
        return ReturnT.SUCCESS;
    }

    @XxlJob("checkMaintenance")
    public ReturnT<String> checkMaintenance(String param) {
        var now = DateNewUtils.now();
        List<GameList> list = gameListServiceImpl.lambdaQuery().ne(GameList::getMaintenance, "{}").list();
        if (!Collections.isEmpty(list)) {
            for (GameList x : list) {
                boolean update = false;
                JSONObject jsonObject = parseObject(x.getMaintenance());
                if (!jsonObject.isEmpty()) {//1607688349
                    if (x.getStatus() != 2 && now > jsonObject.getInteger("start") && now <= jsonObject.getInteger("end")) {
                        update = gameListServiceImpl.lambdaUpdate().set(GameList::getStatus, 2).eq(GameList::getId, x.getId()).update();
                    } else if (x.getStatus() != 1 && now > jsonObject.getInteger("end")) {
                        update = gameListServiceImpl.lambdaUpdate().set(GameList::getStatus, 1).set(GameList::getMaintenance, "{}").eq(GameList::getId, x.getId()).update();
                    } else if (x.getStatus() == 2 && now < jsonObject.getInteger("start")) {
                        update = gameListServiceImpl.lambdaUpdate().set(GameList::getStatus, 1).eq(GameList::getId, x.getId()).update();
                    }
                    jedisUtil.hset(KeyConstant.GAME_LIST_HASH, String.valueOf(x.getId()), JSON.toJSONString(gameListServiceImpl.lambdaQuery().eq(GameList::getId, x.getId()).one()));
                    if (update) {
                        noticeBase.writeMaintainInfo(x.getId());
                        XxlJobLogger.log(x.getId() + "维护状态更新成功");
                    } else {
                        XxlJobLogger.log(x.getId() + "维护状态更新失败");
                    }
                }
            }
            jedisUtil.hdel(KeyConstant.PLATFORM_HASH, KeyConstant.PLATFORM_HASH_GROUP_GAME_LIST);
            XxlJobLogger.log("维护已处理更新");
            return ReturnT.SUCCESS;
        }
        XxlJobLogger.log(list + "无维护可更新");
        return ReturnT.SUCCESS;
    }
}