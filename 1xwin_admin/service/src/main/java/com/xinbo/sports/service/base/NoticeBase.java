package com.xinbo.sports.service.base;

import com.alibaba.fastjson.JSONObject;
import com.xinbo.sports.dao.generator.po.CoinDeposit;
import com.xinbo.sports.dao.generator.po.CoinWithdrawal;
import com.xinbo.sports.dao.generator.service.CoinDepositService;
import com.xinbo.sports.dao.generator.service.CoinWithdrawalService;
import com.xinbo.sports.dao.generator.service.GameListService;
import com.xinbo.sports.dao.generator.service.NoticeService;
import com.xinbo.sports.service.cache.KeyConstant;
import com.xinbo.sports.service.common.Constant;
import com.xinbo.sports.service.io.enums.BaseEnum;
import com.xinbo.sports.service.io.enums.NoticeEnum;
import com.xinbo.sports.utils.JedisUtil;
import lombok.RequiredArgsConstructor;
import org.apache.groovy.util.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static com.alibaba.fastjson.JSON.parseObject;

/**
 * @Author : Wells
 * @Date : 2020-12-12 1:53 下午
 * @Description :  组装消息内容
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NoticeBase {
    private final JedisUtil jedisUtil;
    private final GameListService gameListServiceImpl;
    private final CoinDepositService coinDepositServiceImpl;
    private final CoinWithdrawalService coinWithdrawalServiceImpl;
    private final NoticeService noticeServiceImpl;


    private static final Map<Integer, String> NOTIFICATION_MAP = Maps.of(
            1, NoticeEnum.SYSTEM_NOTIFICATION.getCode(),
            2, NoticeEnum.STATION_LETTER.getCode(),
            3, NoticeEnum.SPORTS_TRAILER.getCode(),
            4, NoticeEnum.ACTIVITY_NOTIFICATION.getCode()
    );


    /**
     * 存款与提款条数
     *
     * @param action 类型
     */
    @Async
    public void writeDepositAndWithdrawalCount(String action) {
        var count = 0;
        //存款
        if (Constant.PUSH_DN.equals(action)) {
            count = coinDepositServiceImpl.lambdaQuery()
                    .eq(CoinDeposit::getStatus, 0)
                    // 支付类型:0-离线 1-在线
                    .eq(CoinDeposit::getPayType, 0)
                    .count();
            //提款
        } else if (Constant.PUSH_WN.equals(action)) {
            count = coinWithdrawalServiceImpl.lambdaQuery()
                    .eq(CoinWithdrawal::getStatus, 0)
                    .count();
        }
        var reJson = new JSONObject();
        reJson.put("action", action);
        reJson.put("device", BaseEnum.MessageDevice.B.getCode());
        var msgJson = new JSONObject();
        msgJson.put("count", count);
        reJson.put("message", msgJson);
        jedisUtil.leftPush(KeyConstant.DEPOSIT_WITHDRAWAL_QUEUE, reJson.toJSONString());
    }

    /**
     * 存款与提款信息
     *
     * @param noticeEnum  类型
     * @param orderId     订单ID
     * @param uid         uid
     * @param coin        金额
     * @param accountDate 到账时间
     */
    @Async
    public void writeDepositAndWithdrawal(NoticeEnum noticeEnum, Integer uid, Long orderId, BigDecimal coin, Integer accountDate) {
        var msgJson = new JSONObject();
        msgJson.put("uid", String.valueOf(uid));
        msgJson.put("orderId", orderId);
        msgJson.put("coin", coin);
        msgJson.put("accountDate", accountDate);
        cacheMessage(noticeEnum.getCode(), msgJson);
    }

    /**
     * 维护信息
     * 游戏的维护信息数据格式{"start":1607695200,"end":1608134399,"info":""}
     *
     * @param gameId 游戏ID
     * @return String
     */
    @Async
    public void writeMaintainInfo(Integer gameId) {
        var game = gameListServiceImpl.getById(gameId);
        var maintenanceJson = parseObject(game.getMaintenance());
        var startTime = Optional.ofNullable(maintenanceJson.getInteger("start")).orElse(0);
        var endTime = Optional.ofNullable(maintenanceJson.getInteger("end")).orElse(0);
        var msgJson = new JSONObject();
        msgJson.put("gameId", gameId);
        msgJson.put("status", game.getStatus());
        msgJson.put("gameName", game.getName());
        msgJson.put("startTime", startTime);
        msgJson.put("endTime", endTime);
        cacheMessage(NoticeEnum.MAINTAIN_INFO.getCode(), msgJson);
    }

    /**
     * 公告消息
     *
     * @param noticeId 消息ID
     */
    @Async
    public void writeNotification(Integer category, Integer noticeId) {
        var notice = noticeServiceImpl.getById(noticeId);
        var msgJson = new JSONObject();
        msgJson.put("title", notice.getTitle());
        msgJson.put("content", notice.getContent());
        if (notice.getCategory() == 2) {
            msgJson.put("uid", notice.getUids());
        }
        cacheMessage(NOTIFICATION_MAP.get(category), msgJson);
    }

    /**
     * 消息格式
     *
     * @param action  消息头
     * @param mesJson 消息内容
     * @return JSONObject
     */
    private void cacheMessage(String action, JSONObject mesJson) {
        var reJson = new JSONObject();
        reJson.put("action", action);
        reJson.put("device", BaseEnum.MessageDevice.D.getCode());
        reJson.put("message", mesJson);
        jedisUtil.leftPush(KeyConstant.NOTIFICATION_QUEUE, reJson.toJSONString());
    }

}
