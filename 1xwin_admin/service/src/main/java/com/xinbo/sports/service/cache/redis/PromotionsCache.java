package com.xinbo.sports.service.cache.redis;

import com.alibaba.fastjson.JSON;
import com.xinbo.sports.dao.generator.po.Promotions;
import com.xinbo.sports.dao.generator.service.PromotionsService;
import com.xinbo.sports.service.cache.KeyConstant;
import com.xinbo.sports.service.io.dto.BaseParams;
import com.xinbo.sports.service.io.enums.BaseEnum;
import com.xinbo.sports.service.thread.ThreadHeaderLocalData;
import com.xinbo.sports.utils.JedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.alibaba.fastjson.JSON.parseObject;

/**
 * @author: wells
 * @date: 2020/8/14
 * @description:
 */
@Component
public class PromotionsCache {
    @Autowired
    private PromotionsService promotionsServiceImpl;
    @Autowired
    private JedisUtil jedisUtil;
    @Autowired
    private ConfigCache configCache;

    /**
     * 获取活动列表
     */
    public Promotions getPromotionsCache(Integer promotionId) {
        var promotionsStr = jedisUtil.hget(KeyConstant.PROMOTIONS_HASH, String.valueOf(promotionId));
        var promotions = new Promotions();
        if (Objects.isNull(promotionsStr)) {
            List<Promotions> promotionsList = promotionsServiceImpl.lambdaQuery()
                    .eq(Promotions::getStatus, 1)
                    .list();
            if (!CollectionUtils.isEmpty(promotionsList)) {
                for (Promotions obj : promotionsList) {
                    if (promotionId != null && promotionId.equals(obj.getId())) {
                        promotions = obj;
                    }
                    jedisUtil.hset(KeyConstant.PROMOTIONS_HASH, String.valueOf(obj.getId()), JSON.toJSONString(obj));
                }
            }
        } else {
            promotions = parseObject(promotionsStr, Promotions.class);
        }
        BaseParams.HeaderInfo currentLoginUser = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        var lang = Objects.nonNull(currentLoginUser) ? currentLoginUser.getLang() : BaseEnum.LANG.EN.getValue();
        lang = !StringUtils.isEmpty(lang) ? lang : Optional.ofNullable(configCache.getConfigByTitle("lang")).orElse("").split(",")[0];
        promotions.setCodeZh(Optional.ofNullable(parseObject(promotions.getCodeZh()).getString(lang)).orElse(""));
        promotions.setImg(Optional.ofNullable(parseObject(promotions.getImg()).getString(lang)).orElse(""));
        promotions.setDescript(Optional.ofNullable(parseObject(promotions.getDescript()).getString(lang)).orElse(""));
        return promotions;
    }

    /**
     * 获取活动列表
     */
    public List<Promotions> getPromotionsListCache() {
        var set = jedisUtil.hkeys(KeyConstant.PROMOTIONS_HASH);
        List<Promotions> promotionsList = new ArrayList<>();
        BaseParams.HeaderInfo currentLoginUser = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        var lang = Objects.nonNull(currentLoginUser) ? currentLoginUser.getLang() : BaseEnum.LANG.EN.getValue();
        lang = !StringUtils.isEmpty(lang) ? lang : Optional.ofNullable(configCache.getConfigByTitle("lang")).orElse("").split(",")[0];
        if (CollectionUtils.isEmpty(set)) {
            promotionsList = promotionsServiceImpl.lambdaQuery()
                    .eq(Promotions::getStatus, 1)
                    .orderByDesc(Promotions::getSort)
                    .orderByDesc(Promotions::getId)
                    .list();
            if (!CollectionUtils.isEmpty(promotionsList)) {
                for (Promotions obj : promotionsList) {
                    jedisUtil.hset(KeyConstant.PROMOTIONS_HASH, String.valueOf(obj.getId()), JSON.toJSONString(obj));
                }
            }
        } else {
            for (String key : set) {
                var promotionsStr = jedisUtil.hget(KeyConstant.PROMOTIONS_HASH, key);
                var promotions = parseObject(promotionsStr, Promotions.class);
                promotionsList.add(promotions);
            }
        }
        for (var promotions : promotionsList) {
            promotions.setCodeZh(Optional.ofNullable(parseObject(promotions.getCodeZh()).getString(lang)).orElse(""));
            promotions.setImg(Optional.ofNullable(parseObject(promotions.getImg()).getString(lang)).orElse(""));
            promotions.setDescript(Optional.ofNullable(parseObject(promotions.getDescript()).getString(lang)).orElse(""));
        }
        return promotionsList;
    }

    /**
     * 刷新活动列表
     */
    public void refreshPromotions() {
        jedisUtil.del(KeyConstant.PROMOTIONS_HASH);
        getPromotionsListCache();
    }
}
