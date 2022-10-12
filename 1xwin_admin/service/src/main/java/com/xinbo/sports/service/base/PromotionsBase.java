package com.xinbo.sports.service.base;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinbo.sports.dao.generator.po.*;
import com.xinbo.sports.dao.generator.service.*;
import com.xinbo.sports.service.cache.redis.PromotionsCache;
import com.xinbo.sports.service.common.Constant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.dto.UpdateUserCoinParams;
import com.xinbo.sports.service.io.dto.promotions.ApplicationActivityReqDto;
import com.xinbo.sports.service.io.dto.promotions.LuxuriousGiftReceiveReqDto;
import com.xinbo.sports.utils.DateUtils;
import com.xinbo.sports.utils.components.response.CodeInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.alibaba.fastjson.JSON.toJSONString;
import static com.xinbo.sports.service.common.Constant.*;
import static java.util.Objects.nonNull;

/**
 * @author: wells
 * @date: 2020/6/9
 * @description:
 */
@Service
public class PromotionsBase {
    /*coin_log 类型7为活动*/
    public static final int LOG_CATEGORY = 7;
    /*code_record 类型3为活动*/
    private static final int CODE_CATEGORY = 2;
    @Autowired
    private UserService userServiceImpl;
    @Resource
    private CoinLogService coinLogServiceImpl;
    @Autowired
    private CodeRecordsService codeRecordsServiceImpl;
    @Autowired
    private CoinRewardsService coinRewardsServiceImpl;
    @Autowired
    private GiftRecordsService giftRecordsServiceImpl;
    @Autowired
    private GiftListService giftListServiceImpl;
    @Autowired
    private CoinRewardsInviteService coinRewardsInviteServiceImpl;
    @Autowired
    private UpdateUserCoinBase updateUserCoinBase;
    @Autowired
    private PromotionsService promotionsServiceImpl;
    /**
     * 线程变量保持邀请记录的类型与金额
     */
    public static final ThreadLocal<CoinRewardsInvite> localInvite = new ThreadLocal<>();

    /**
     * 活动奖励入库处理
     * <p>
     * 处理获取优惠活动持久化
     * 1.修改用户金额
     * 2.插入奖金表数据 ->CoinRewards
     * 3.插入账变记录-> CoinLog
     * 4.插入打码量记录->CodeRecords
     *
     * @param reqDto 奖励入库参数
     * @param uid    用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void executePromotionsPersistence(ApplicationActivityReqDto reqDto, Integer uid) {
        User userInfo = userServiceImpl.getById(uid);
        var now = DateUtils.getCurrentTime();
        var coin = reqDto.getAvailableCoin();
        CoinRewards coinRewards = new CoinRewards();
        coinRewards.setUid(userInfo.getId());
        coinRewards.setUsername(userInfo.getUsername());
        coinRewards.setCoin(coin);
        coinRewards.setCoinBefore(userInfo.getCoin());
        coinRewards.setReferId(reqDto.getId());
        coinRewards.setCreatedAt(now);
        coinRewards.setUpdatedAt(now);
        //扩展信息处理
        coinRewards.setInfo(reqDto.getInfo());
        coinRewardsServiceImpl.save(coinRewards);
        //修改用户金额
        updateUserCoinBase.updateUserCoinSaveLog(UpdateUserCoinParams.UpdateUserCoinSaveLogDto.builder()
                .outIn(0)
                .uid(uid)
                .coin(coin)
                .referId(coinRewards.getId())
                .category(LOG_CATEGORY)
                .subCategory(reqDto.getId())
                .status(1)
                .now(now)
                .build());
        CodeRecords codeRecords = new CodeRecords();
        codeRecords.setUid(userInfo.getId());
        codeRecords.setUsername(userInfo.getUsername());
        codeRecords.setCoin(coin);
        codeRecords.setCodeRequire(reqDto.getMosaicCoin());
        //活动类型
        codeRecords.setCategory(CODE_CATEGORY);
        codeRecords.setReferId(coinRewards.getId());
        codeRecords.setCreatedAt(now);
        codeRecords.setUpdatedAt(now);
        codeRecordsServiceImpl.save(codeRecords);
        //被邀请人或被邀请人入库
        if (Constant.INVITE_FRIENDS.equals(reqDto.getId())) {
            CoinRewardsInvite coinRewardsInvite = localInvite.get();
            coinRewardsInvite.setReferId(coinRewards.getId());
            coinRewardsInvite.setCreatedAt(now);
            coinRewardsInvite.setUpdatedAt(now);
            coinRewardsInviteServiceImpl.save(coinRewardsInvite);
        }
    }

    /**
     * 领取豪礼入库
     *
     * @param reqDto   豪礼请求实例类
     * @param giftList 豪礼列表
     * @param uid      用户ID
     * @param address  地址
     */
    @Transactional(rollbackFor = Exception.class)
    public void executeLuxuriousGiftReceivePersistence(LuxuriousGiftReceiveReqDto reqDto, GiftList giftList, Integer uid, String address, String postNo) {
        var user = userServiceImpl.getById(uid);
        int now = DateUtils.getCurrentTime();
        //新增豪礼记录
        var giftRecords = new GiftRecords();
        giftRecords.setGiftId(reqDto.getGiftId());
        giftRecords.setPostNo(postNo);
        giftRecords.setGiftName(giftList.getName());
        giftRecords.setUid(uid);
        giftRecords.setUsername(user.getUsername());
        giftRecords.setAddr(address);
        giftRecords.setMark(toJSONString(reqDto.getMark()));
        //状态:0-申请中 1-同意 2-拒绝 3-已发货 4-已送达
        giftRecords.setStatus(0);
        giftRecords.setCreatedAt(now);
        giftRecords.setUpdatedAt(now);
        giftRecordsServiceImpl.save(giftRecords);
        // 修改礼物表数量及状态
        int num = giftList.getNums() - 1;
        Integer status = num == 0 ? 0 : 1;
        var reGiftList = new GiftList();
        reGiftList.setId(reqDto.getGiftId());
        reGiftList.setNums(giftList.getNums() - 1);
        reGiftList.setStatus(status);
        giftListServiceImpl.updateBatchById(Collections.singleton(reGiftList));
    }

    /**
     * 金额为0的红包处理
     *
     * @param uid          用户id
     * @param promotionsId 活动ID
     */
    public void saveRedEnvelopeCoinRewards(Integer uid, Integer promotionsId) {
        User userInfo = userServiceImpl.getById(uid);
        var now = DateUtils.getCurrentTime();
        CoinRewards coinRewards = new CoinRewards();
        coinRewards.setUid(userInfo.getId());
        coinRewards.setUsername(userInfo.getUsername());
        coinRewards.setCoin(BigDecimal.ZERO);
        coinRewards.setCoinBefore(userInfo.getCoin());
        coinRewards.setReferId(promotionsId);
        coinRewards.setCreatedAt(now);
        coinRewards.setUpdatedAt(now);

        coinRewardsServiceImpl.save(coinRewards);
    }
    /**
     * 1.验证活动是否在活动时间内
     * 2.是否领取奖金
     *
     * @param pId 活动ID
     */
    public void isPromotionsEffective(Integer pId, Integer uid) {
        Promotions promotions = promotionsServiceImpl.getById(pId);
        int now = DateUtils.getCurrentTime();
        if (now < promotions.getStartedAt() || now > promotions.getEndedAt()) {
            throw new BusinessException(CodeInfo.ACTIVE_CLOSE);
        }
        var wrapper = new LambdaQueryWrapper<CoinRewards>();
        if (Constant.FIRST_DEPOSIT_DOUBLE.equals(pId) || Constant.FIRST_DEPOSIT_SUPER_BONUS.equals(pId)) {
            wrapper.in(CoinRewards::getReferId, List.of(Constant.FIRST_DEPOSIT_DOUBLE, Constant.FIRST_DEPOSIT_SUPER_BONUS));
        } else {
            wrapper.eq(CoinRewards::getReferId, pId);
        }
        wrapper.eq(CoinRewards::getUid, uid);
        int count = coinRewardsServiceImpl.count(wrapper);
        if (count > 0) {
            throw new BusinessException(CodeInfo.ACTIVE_COIN_ALREADY_RECEIVE);
        }
    }
}
