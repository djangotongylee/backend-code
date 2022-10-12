package com.xinbo.sports.task.base;

import com.alibaba.fastjson.JSONObject;
import com.xinbo.sports.dao.generator.po.*;
import com.xinbo.sports.dao.generator.service.*;
import com.xinbo.sports.service.base.UpdateUserCoinBase;
import com.xinbo.sports.service.common.Constant;
import com.xinbo.sports.service.io.dto.UpdateUserCoinParams;
import com.xinbo.sports.service.io.dto.promotions.ApplicationActivityReqDto;
import com.xinbo.sports.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;

import static com.alibaba.fastjson.JSON.parseObject;

/**
 * @author: wells
 * @date: 2020/6/9
 * @description:
 */
@Service
public class PromotionsTaskBase {
    /*coin_log 类型7为活动*/
    private static final int LOG_CATEGORY = 7;
    /*coin_rewards 类型2为活动奖励*/
    private static final int CODE_CATEGORY = 2;
    @Autowired
    private UserService userServiceImpl;
    @Resource
    private CoinLogService coinLogServiceImpl;
    @Autowired
    private CodeRecordsService codeRecordsServiceImpl;
    @Resource
    private CoinRewardsService coinRewardsServiceImpl;
    @Autowired
    private CoinRewardsInviteService coinRewardsInviteServiceImpl;
    @Autowired
    private CoinRebateService coinRebateServiceImpl;
    @Autowired
    private UpdateUserCoinBase updateUserCoinBase;
    @Autowired
    private UserProfileService userProfileServiceImpl;
    /*奖励表ID*/
    ThreadLocal<Long> rewardThreadLocal = new ThreadLocal<>();

    /**
     * 活动奖励入库处理
     * <p>
     * 处理获取优惠活动持久化
     * 1.修改用户金额
     * 2.插入奖金表数据 ->CoinRewards
     * 3.插入账变记录-> CoinLog
     * 4.插入打码量记录->CodeRecords
     */
    @Transactional(rollbackFor = Exception.class)
    public void handlePromotionsPersistence(ApplicationActivityReqDto reqDto, Integer uid) {
        var user = userServiceImpl.getById(uid);
        //任务调度活动是结算前一天的彩金
        var now = DateUtils.getCurrentTime();
        //活动奖金入库
        CoinRewards coinRewards = new CoinRewards();
        coinRewards.setUid(uid);
        coinRewards.setUsername(user.getUsername());
        coinRewards.setCoin(reqDto.getAvailableCoin());
        coinRewards.setCoinBefore(user.getCoin());
        coinRewards.setReferId(reqDto.getId());
        coinRewards.setCreatedAt(now);
        coinRewards.setUpdatedAt(now);
        //扩展信息处理
        coinRewards.setInfo(reqDto.getInfo());
        coinRewardsServiceImpl.save(coinRewards);
        //修改用户金额
        updateUserCoinBase.updateUserCoinSaveLog(UpdateUserCoinParams.UpdateUserCoinSaveLogDto.builder()
                .uid(uid)
                .coin(reqDto.getAvailableCoin())
                .referId(coinRewards.getId())
                .category(LOG_CATEGORY)
                .subCategory(reqDto.getId())
                .status(1)
                .now(now)
                .build());
        //打码量入库
        CodeRecords codeRecords = new CodeRecords();
        codeRecords.setUid(uid);
        codeRecords.setUsername(user.getUsername());
        codeRecords.setCoin(reqDto.getAvailableCoin());
        codeRecords.setCodeRequire(reqDto.getAvailableCoin());
        //活动类型
        codeRecords.setCategory(CODE_CATEGORY);
        codeRecords.setReferId(coinRewards.getId());
        codeRecords.setCreatedAt(now);
        codeRecords.setUpdatedAt(now);
        codeRecordsServiceImpl.save(codeRecords);
        if (Constant.INVITE_FRIENDS.equals(reqDto.getId())) {
            rewardThreadLocal.set(coinRewards.getId());
        }
        //VIP升级后余额处理
        if (Constant.VIP_GROW_UP.equals(reqDto.getId())) {
            //计算升级后的余额（投注金额+上一级剩余余额-升级金额）
            var balance = parseObject(reqDto.getInfo()).getJSONObject("details").getBigDecimal("balance");
            //更新userprofile的upgradeBalance字段
            userProfileServiceImpl.lambdaUpdate()
                    .set(UserProfile::getUpgradeBalance, balance)
                    .set(UserProfile::getUpdatedAt, now)
                    .eq(UserProfile::getUid, uid)
                    .update();
        }
    }

    /**
     * 邀请好友入库处理
     */
    @Transactional(rollbackFor = Exception.class)
    public void invitePersistence(ApplicationActivityReqDto reqDto, Integer uid, CoinRewardsInvite coinRewardsInvite) {
        //活动奖励入库处理
        handlePromotionsPersistence(reqDto, uid);
        coinRewardsInvite.setReferId(rewardThreadLocal.get());
        //邀请好友奖励入库
        coinRewardsInviteServiceImpl.save(coinRewardsInvite);

    }

    /**
     * 全场返水入库
     */
    public void rebatePersistence(ApplicationActivityReqDto reqDto, Integer uid, GameList game, BigDecimal rate, BigDecimal validCoin) {
        var user = userServiceImpl.getById(uid);
        var now = DateUtils.getCurrentTime();
        var coinRebate = new CoinRebate();
        coinRebate.setTitle(game.getName());
        coinRebate.setUid(uid);
        coinRebate.setUsername(user.getUsername());
        coinRebate.setGameListId(game.getId());
        coinRebate.setCoin(reqDto.getAvailableCoin());
        coinRebate.setCoinBefore(user.getCoin());
        coinRebate.setCoinBetValid(validCoin);
        coinRebate.setRate(rate);
        coinRebate.setPlatId(game.getGroupId());
        coinRebate.setCreatedAt(now);
        coinRebate.setUpdatedAt(now);
        coinRebateServiceImpl.save(coinRebate);
        //修改用户金额
        updateUserCoinBase.updateUserCoinSaveLog(UpdateUserCoinParams.UpdateUserCoinSaveLogDto.builder()
                .uid(uid)
                .coin(reqDto.getAvailableCoin())
                .referId(coinRebate.getId())
                .category(LOG_CATEGORY)
                .subCategory(reqDto.getId())
                .status(1)
                .now(now)
                .build());
        //打码量入库
        CodeRecords codeRecords = new CodeRecords();
        codeRecords.setUid(uid);
        codeRecords.setUsername(user.getUsername());
        codeRecords.setCoin(reqDto.getMosaicCoin());
        codeRecords.setCodeRequire(reqDto.getMosaicCoin());
        //活动类型
        codeRecords.setCategory(CODE_CATEGORY);
        codeRecords.setReferId(coinRebate.getId());
        codeRecords.setCreatedAt(now);
        codeRecords.setUpdatedAt(now);
        codeRecordsServiceImpl.save(codeRecords);
        //活动奖金入库
        CoinRewards coinRewards = new CoinRewards();
        coinRewards.setId(coinRebate.getId());
        coinRewards.setUid(uid);
        coinRewards.setUsername(user.getUsername());
        coinRewards.setCoin(reqDto.getAvailableCoin());
        coinRewards.setCoinBefore(user.getCoin());
        coinRewards.setReferId(reqDto.getId());
        coinRewards.setCreatedAt(now);
        coinRewards.setUpdatedAt(now);
        //扩展信息处理
        coinRewards.setInfo(reqDto.getInfo());
        coinRewardsServiceImpl.save(coinRewards);
    }

    /**
     * 用户降级处理
     *
     * @param uid 用户id
     */
    @Transactional(rollbackFor = Exception.class)
    public void handlerVipRelegation(Integer uid) {
        var user = userServiceImpl.getById(uid);
        //任务调度活动是结算前一天的彩金
        var now = DateUtils.getCurrentTime();
        //插入奖金记录
        CoinRewards coinRewards = new CoinRewards();
        coinRewards.setUid(uid);
        coinRewards.setUsername(user.getUsername());
        coinRewards.setCoin(BigDecimal.ZERO);
        coinRewards.setCoinBefore(user.getCoin());
        coinRewards.setReferId(Constant.VIP_GROW_UP);
        coinRewards.setCreatedAt(now);
        coinRewards.setUpdatedAt(now);
        var infoJSONObject = new JSONObject();
        infoJSONObject.put("payType", Constant.VIP_RELEGATION);
        //扩展信息处理
        coinRewards.setInfo(infoJSONObject.toJSONString());
        coinRewardsServiceImpl.save(coinRewards);
        //修改用户升级剩余金额
        userProfileServiceImpl.lambdaUpdate()
                .set(UserProfile::getUpgradeBalance, BigDecimal.ZERO)
                .set(UserProfile::getUpdatedAt, now)
                .eq(UserProfile::getUid, uid)
                .update();

    }
}

