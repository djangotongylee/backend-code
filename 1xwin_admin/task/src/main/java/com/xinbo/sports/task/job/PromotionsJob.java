package com.xinbo.sports.task.job;

import com.xinbo.sports.service.cache.redis.PromotionsCache;
import com.xinbo.sports.service.common.Constant;
import com.xinbo.sports.task.service.impl.IPromotionsTaskServiceImpl;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

import static com.alibaba.fastjson.JSON.parseObject;

/**
 * @author: wells
 * @date: 2020/7/9
 * @description: 活动定时任务
 */
@Component
public class PromotionsJob {
    @Autowired
    private IPromotionsTaskServiceImpl iPromotionsTaskServiceImpl;
    @Autowired
    private PromotionsCache promotionsCache;
    private static final String COUNT = "count";
    private static final String DATE = "date";
    //异常日志打印函数
    BiConsumer<String, Exception> biConsumer = (message, exception) -> {
        XxlJobLogger.log(message + exception);
        StackTraceElement[] stackTrace = exception.getStackTrace();
        for (StackTraceElement element : stackTrace) {
            XxlJobLogger.log(element.toString());
        }
    };

    /***
     * 真人投注送彩金
     * @param param :
     * @Return com.xxl.job.core.biz.model.ReturnT<java.lang.String>
     **/
    @XxlJob("liveDailyBonus")
    public ReturnT<String> liveDailyBonus(String param) {
        try {
            if (checkPromotions(Constant.LIVE_DAILY_BONUS)) {
                XxlJobLogger.log("真人投注送彩金不是自动派彩活动！");
                return ReturnT.FAIL;
            }
            iPromotionsTaskServiceImpl.liveDailyBonus();
        } catch (RuntimeException e) {
            biConsumer.accept("真人投注送彩金异常;", e);
            return ReturnT.FAIL;
        }
        return ReturnT.SUCCESS;
    }

    /**
     * 邀请好友
     *
     * @param param :
     * @Return com.xxl.job.core.biz.model.ReturnT<java.lang.String>
     **/
    @XxlJob("inviteFriends")
    public ReturnT<String> inviteFriends(String param) {
        try {
            if (checkPromotions(Constant.INVITE_FRIENDS)) {
                XxlJobLogger.log("邀请好友不是自动派彩活动！");
                return ReturnT.FAIL;
            }
            iPromotionsTaskServiceImpl.inviteFriends();
        } catch (RuntimeException e) {
            biConsumer.accept("邀请好友;", e);
            return ReturnT.FAIL;
        }
        return ReturnT.SUCCESS;
    }

    /**
     * 全场返水
     *
     * @param param :
     * @Return com.xxl.job.core.biz.model.ReturnT<java.lang.String>
     **/
    @XxlJob("rebateAllGames")
    public ReturnT<String> rebateAllGames(String param) {
        try {
            if (checkPromotions(Constant.REBATE_ALL_GAMES)) {
                XxlJobLogger.log("全场返水不是自动派彩活动！");
                return ReturnT.FAIL;
            }
            iPromotionsTaskServiceImpl.rebateAllGames();
        } catch (RuntimeException e) {
            biConsumer.accept("全场返水;", e);
            return ReturnT.FAIL;
        }
        return ReturnT.SUCCESS;
    }

    /**
     * VIP 会员成长
     *
     * @param param :
     * @Return com.xxl.job.core.biz.model.ReturnT<java.lang.String>
     **/
    @XxlJob("vipGrowUp")
    public ReturnT<String> vipGrowUp(String param) {
        try {
            if (checkPromotions(Constant.VIP_GROW_UP)) {
                XxlJobLogger.log("VIP 会员成长不是自动派彩活动！");
                return ReturnT.FAIL;
            }
            iPromotionsTaskServiceImpl.vipGrowUp();
        } catch (RuntimeException e) {
            biConsumer.accept("VIP 会员成长;", e);
            return ReturnT.FAIL;
        }
        return ReturnT.SUCCESS;
    }

    /**
     * 加盟代理
     *
     * @param param :
     * @Return com.xxl.job.core.biz.model.ReturnT<java.lang.String>
     **/
    @XxlJob("agent")
    public ReturnT<String> agent(String param) {
        try {
            var json = parseObject(param);
            iPromotionsTaskServiceImpl.agent(Optional.ofNullable(json).map(x -> x.getInteger(COUNT)).orElse(1));
        } catch (RuntimeException e) {
            biConsumer.accept("加盟代理;", e);
            return ReturnT.FAIL;
        }
        return ReturnT.SUCCESS;
    }

    /**
     * 流水佣金
     *
     * @param param :
     * @Return com.xxl.job.core.biz.model.ReturnT<java.lang.String>
     **/
    @XxlJob("flowCommission")
    public ReturnT<String> flowCommission(String param) {
        try {
            var json = parseObject(param);
            var count = Optional.ofNullable(json).map(x -> x.getInteger(COUNT)).orElse(1);
            var date = Optional.ofNullable(json).map(x -> x.getString(DATE)).orElse("");
            iPromotionsTaskServiceImpl.flowCommission(count, date);
        } catch (RuntimeException e) {
            biConsumer.accept("流水佣金;", e);
            return ReturnT.FAIL;
        }
        return ReturnT.SUCCESS;
    }

    /**
     * 检测活动是否自动派彩
     *
     * @param promotionsId :
     * @Return boolean
     **/
    private boolean checkPromotions(Integer promotionsId) {
        var promotions = promotionsCache.getPromotionsCache(promotionsId);
        return !(Objects.nonNull(promotions) && promotions.getPayoutCategory().equals(0));
    }
}
