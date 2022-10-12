package com.xinbo.sports.service.base;

import com.xinbo.sports.dao.generator.po.CoinDeposit;
import com.xinbo.sports.dao.generator.po.CoinRewardsInvite;
import com.xinbo.sports.dao.generator.service.CoinDepositService;
import com.xinbo.sports.dao.generator.service.UserProfileService;
import com.xinbo.sports.service.cache.redis.PromotionsCache;
import com.xinbo.sports.service.cache.redis.UserCache;
import com.xinbo.sports.service.io.dto.promotions.ApplicationActivityReqDto;
import com.xinbo.sports.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static com.xinbo.sports.service.common.Constant.INVITE_FRIENDS;

/**
 * @Author : Wells
 * @Date : 2020/10/21 2:30 下午
 * @Description : 首充二充三充
 **/
@Component
@Slf4j
public class DepositBase {
    @Autowired
    private PromotionsCache promotionsCache;
    @Autowired
    private UserProfileService userProfileServiceImpl;
    @Autowired
    private PromotionsBase promotionsBase;
    @Autowired
    private CoinDepositService coinDepositServiceImpl;
    @Autowired
    private UserCache userCache;

    /**
     * 充值成功，判断是否是首充二充三充
     * 1.修改充值记录dep_status状态
     * 2.邀请好友活动
     *
     * @param uid 用户id
     * @return 状态
     */
    public int firstOrSecondDeposit(Integer uid, BigDecimal payCoin) {
        var now = DateUtils.getCurrentTime();
        var coinDepositList = coinDepositServiceImpl.lambdaQuery()
                .eq(CoinDeposit::getUid, uid)
                .in(CoinDeposit::getStatus, List.of(1, 2, 9))
                .orderByDesc(CoinDeposit::getUpdatedAt)
                .list();
        //充值标识:1-首充 2-二充 3-三充 9-其他
        var depStatus = 9;
        try {
            //首充判断
            if (coinDepositList.isEmpty()) {
                depStatus = 1;
                //邀请好友活动奖励
                var promotions = promotionsCache.getPromotionsCache(INVITE_FRIENDS);
                //派彩类型: 0-自动派彩  1-手动派彩
                var payOutFlag = Objects.nonNull(promotions) && promotions.getPayoutCategory().equals(0);
                if (payOutFlag && now >= promotions.getStartedAt() && now <= promotions.getEndedAt()) {
                    var userProfile = userProfileServiceImpl.getById(uid);
                    //邀请好友活动
                    var activityReqDto = ApplicationActivityReqDto.builder()
                            .availableCoin(BigDecimal.valueOf(8))
                            .mosaicCoin(BigDecimal.valueOf(8))
                            .id(INVITE_FRIENDS)
                            .info("{}")
                            .build();
                    //类型:0-被邀请奖金1-邀请奖金
                    CoinRewardsInvite coinRewardsInvite0 = new CoinRewardsInvite();
                    coinRewardsInvite0.setCategory(0);
                    coinRewardsInvite0.setCoin(BigDecimal.ZERO);
                    coinRewardsInvite0.setUid(userProfile.getSupUid1());
                    coinRewardsInvite0.setUsername(userCache.getUserInfoById(uid).getUsername());
                    PromotionsBase.localInvite.set(coinRewardsInvite0);
                    promotionsBase.executePromotionsPersistence(activityReqDto, userProfile.getUid());
                    CoinRewardsInvite coinRewardsInvite1 = new CoinRewardsInvite();
                    coinRewardsInvite1.setCategory(1);
                    coinRewardsInvite1.setCoin(payCoin);
                    coinRewardsInvite1.setUid(uid);
                    coinRewardsInvite1.setUsername(userCache.getUserInfoById(userProfile.getSupUid1()).getUsername());
                    PromotionsBase.localInvite.set(coinRewardsInvite1);
                    promotionsBase.executePromotionsPersistence(activityReqDto, userProfile.getSupUid1());
                }
                //二充判断
            } else if (coinDepositList.size() == 1) {
                depStatus = 2;
            } else if (coinDepositList.size() == 2) {
                depStatus = 3;
            }
        } catch (Exception e) {
            log.error("充值判断出错：" + e.toString());
        }
        return depStatus;
    }

}
