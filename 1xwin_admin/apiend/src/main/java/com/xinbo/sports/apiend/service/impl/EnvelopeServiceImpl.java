package com.xinbo.sports.apiend.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.xinbo.sports.apiend.io.dto.promotions.EnvelopeDateResDto;
import com.xinbo.sports.apiend.io.dto.promotions.ReceiveEnvelopeReqDto;
import com.xinbo.sports.apiend.service.IEnvelopeService;
import com.xinbo.sports.apiend.service.IUserInfoService;
import com.xinbo.sports.dao.generator.po.CoinRewards;
import com.xinbo.sports.dao.generator.service.CoinRewardsService;
import com.xinbo.sports.dao.generator.service.PromotionsService;
import com.xinbo.sports.service.base.PromotionsBase;
import com.xinbo.sports.service.common.Constant;
import com.xinbo.sports.service.common.RedisConstant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.dto.UserInfo;
import com.xinbo.sports.service.io.dto.promotions.ApplicationActivityReqDto;
import com.xinbo.sports.utils.components.response.CodeInfo;
import com.xinbo.sports.utils.JedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.*;

import static com.alibaba.fastjson.JSON.*;


/**
 * @author: wells
 * @date: 2020/5/2
 * @description:
 */
@Slf4j
@Service("envelopeServiceImpl")
public class EnvelopeServiceImpl implements IEnvelopeService {
    @Autowired
    private PromotionsService promotionsServiceImpl;
    @Autowired
    private CoinRewardsService coinRewardsServiceImpl;
    @Autowired
    private JedisUtil jedisUtil;
    @Autowired
    private PromotionsBase promotionsBase;
    @Resource
    private IUserInfoService userInfoServiceImpl;

    /**
     * 优惠活动生成红包雨
     */
    public JSONObject generateEnvelope() {
        BigDecimal startValue = new BigDecimal(200);
        int envCount = 100;
        var evnJson = new JSONObject();
        List<BigDecimal> list = new ArrayList<>();
        while (list.size() < envCount) {
            BigDecimal temp = list.size() + 1 == envCount ? startValue : BigDecimal.valueOf(Math.random())
                    .multiply(startValue).setScale(2, RoundingMode.DOWN);
            //单个红包不超过剩余金额的10%
            if (temp.compareTo(BigDecimal.ZERO) > 0 && list.size() + 1 != envCount &&
                    temp.compareTo(startValue.multiply(new BigDecimal("0.1"))) > 0) {
                continue;
            }
            list.add(temp);
            startValue = startValue.subtract(temp);
        }
        Collections.shuffle(list);
        list.forEach(x -> evnJson.put(String.valueOf(evnJson.size()), x));
        jedisUtil.setex(RedisConstant.ENVELOPE_RECORD, 20, toJSONString(evnJson));
        jedisUtil.setex(RedisConstant.ENVELOPE_COUNT, 20, toJSONString(100));
        return evnJson;
    }

    /***
     * 优惠活动->判断能否领取
     * 1.用户是否已领取红包
     * 2.查询用户的会员等级
     * 3.根据会员等级获取相应的次数
     * 4.查询用户的抢包次数，是否还剩余抢包次数
     * 5.返回提示信息
     @return
     */
    @Override
    public void isReceive() {
        //获取用户信息
        UserInfo userInfo = userInfoServiceImpl.findIdentityByApiToken();
        //判断当前时间是否在红包雨活动期间
        int reTime = isDataInterval();
        if (reTime <= 0) {
            throw new BusinessException(CodeInfo.ACTIVE_CLOSE);
        }
        //用户是否已领取红包
        int reCount = coinRewardsServiceImpl.count(new LambdaQueryWrapper<CoinRewards>()
                .eq(CoinRewards::getReferId, Constant.RED_ENVELOPE_RAIN)
                .ge(CoinRewards::getCreatedAt, reTime - Constant.DATA_INTERVAL)
                .le(CoinRewards::getCreatedAt, reTime));
        if (reCount > 0) {
            throw new BusinessException(CodeInfo.ACTIVE_RECEIVE_ENVELOPE_RAIN);
        }
        String envelopeRecord = jedisUtil.get(RedisConstant.ENVELOPE_RECORD);
        Map<Integer, BigDecimal> envMap;
        if (StringUtils.isEmpty(envelopeRecord)) {
            envMap = toJavaObject(generateEnvelope(), Map.class);
        } else {
            envMap = (Map<Integer, BigDecimal>) parse(envelopeRecord);
        }
        String envelopeCount = jedisUtil.get(RedisConstant.ENVELOPE_COUNT);
        if (Integer.valueOf(envelopeCount).compareTo(0) <= 0) {
            throw new BusinessException(CodeInfo.ACTIVE_NO_RED_ENVELOPE_RAIN);
        }
        int index = Integer.parseInt(envelopeCount) - 1;
        Integer level = userInfo.getLevelId();
        String info = promotionsServiceImpl.getById(Constant.RED_ENVELOPE_RAIN).getInfo();
        JSONArray jsonArray = parseArray(toJSONString(parseObject(info).get("rule")));
        String countRule = "";
        for (Object o : jsonArray) {
            JSONObject jo = (JSONObject) o;
            List<String> levels = Arrays.asList(jo.getString("level").split(","));
            if (levels.contains(String.valueOf(level))) {
                countRule = jo.getString("count");
                break;
            }
        }
        int count = 0;
        int startTime = 0;
        int endTime = 0;
        if (StringUtils.isEmpty(countRule)) {
            throw new BusinessException(CodeInfo.ACTIVE_NO_RED_ENVELOPE_RAIN);
        }
        String[] arr = countRule.split("/");
        switch (arr[1]) {
            case "year":
                int day = LocalDateTime.now().getDayOfYear();
                startTime = (int) LocalDate.now().plusDays((long) (1 - day)).toEpochSecond(LocalTime.of(0, 0, 0), ZoneOffset.of("+8"));
                endTime = (int) LocalDate.now().plusDays((long) (366 - day)).toEpochSecond(LocalTime.of(23, 59, 59), ZoneOffset.of("+8"));
                count = Integer.parseInt(arr[0]);
                break;
            case "week":
                int dayOfWeek = LocalDate.now().getDayOfWeek().getValue();
                startTime = (int) LocalDate.now().plusDays(-dayOfWeek).toEpochSecond(LocalTime.of(0, 0, 0), ZoneOffset.of("+8"));
                endTime = (int) LocalDate.now().plusDays((long) (6 - dayOfWeek)).toEpochSecond(LocalTime.of(23, 59, 59), ZoneOffset.of("+8"));
                count = Integer.parseInt(arr[0]);
                break;
            case "day":
                count = -1;
                break;
            default:
                break;
        }
        //count为-1时表示不限次数
        if (count != -1) {
            int envCount = coinRewardsServiceImpl.count(new LambdaQueryWrapper<CoinRewards>()
                    .eq(CoinRewards::getUid, userInfo.getId()).eq(CoinRewards::getReferId, Constant.RED_ENVELOPE_RAIN)
                    .gt(CoinRewards::getCreatedAt, startTime).le(CoinRewards::getCreatedAt, endTime));
            //次数不够提示
            if (count < envCount) {
                throw new BusinessException(CodeInfo.ACTIVE_NO_ENVELOPE_COUNT);
            }
        }
        ApplicationActivityReqDto applicationActivityReqDto = ApplicationActivityReqDto.builder()
                .id(Constant.RED_ENVELOPE_RAIN)
                .mosaicCoin(envMap.get(index))
                .availableCoin(envMap.get(index))
                .build();
        promotionsBase.executePromotionsPersistence(applicationActivityReqDto, userInfo.getId());
        jedisUtil.setex(RedisConstant.ENVELOPE_COUNT, 20, toJSONString(--index));
    }

    /**
     * 优惠活动领取红包
     *
     * @param reqDto
     * @return
     */
    public void receiveEnvelope(ReceiveEnvelopeReqDto reqDto) {
        //获取用户信息
        UserInfo userInfo = userInfoServiceImpl.findIdentityByApiToken();
        ApplicationActivityReqDto applicationActivityReqDto = ApplicationActivityReqDto.builder()
                .id(Constant.RED_ENVELOPE_RAIN)
                .mosaicCoin(reqDto.getCoin())
                .availableCoin(reqDto.getCoin())
                .build();
        promotionsBase.executePromotionsPersistence(applicationActivityReqDto, userInfo.getId());
    }

    /***
     * 优惠活动->红包领取时间
     * @return
     */
    @Override
    public EnvelopeDateResDto envelopeDate() {
        long startTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(20, 0, 0)).toEpochSecond(ZoneOffset.of("+8"));
        EnvelopeDateResDto envelopeDateResDto = new EnvelopeDateResDto();
        int endTime = isDataInterval();
        if (endTime > 0) {
            envelopeDateResDto.setStartTime((int) startTime);
            envelopeDateResDto.setEndTime(endTime);
            envelopeDateResDto.setStatus(1);
        }
        return envelopeDateResDto;
    }

    /**
     * 判断当前时间是否在红包雨活动期间
     *
     * @return
     */
    public int isDataInterval() {
        //红包雨开启时间
        ArrayList<LocalDateTime> localTimes = Lists.newArrayList(
                LocalDateTime.of(LocalDate.now(), LocalTime.of(20, 0, 0)),
                LocalDateTime.of(LocalDate.now(), LocalTime.of(21, 0, 0)),
                LocalDateTime.of(LocalDate.now(), LocalTime.of(22, 0, 0)));
        //是否开启红包雨
        long startTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(20, 0, 0)).toEpochSecond(ZoneOffset.of("+8"));
        for (LocalDateTime time : localTimes) {
            if (startTime >= time.toEpochSecond(ZoneOffset.of("+8")) &&
                    startTime - time.toEpochSecond(ZoneOffset.of("+8")) <= Constant.DATA_INTERVAL) {
                return (int) time.toEpochSecond(ZoneOffset.of("+8")) + Constant.DATA_INTERVAL;
            }
        }
        return 0;
    }


}
