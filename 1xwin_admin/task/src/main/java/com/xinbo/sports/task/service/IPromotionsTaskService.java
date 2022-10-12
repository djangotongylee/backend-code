package com.xinbo.sports.task.service;

/**
 * @author: wells
 * @date: 2020/5/3
 * @description: 优惠活动定时任务
 */

public interface IPromotionsTaskService {

    /***
     * 真人投注送彩金
     */
    void liveDailyBonus();

    /***
     * 邀请好友
     */
    void inviteFriends();

    /***
     * 全场返水
     */
    void rebateAllGames();

    /***
     * VIP 会员成长
     */
    void vipGrowUp();

    /**
     * 加盟代理
     */
    void agent(int count);

    /**
     * 流水佣金
     */
    void flowCommission(int count,String date);
}
