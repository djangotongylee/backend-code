package com.xinbo.sports.service.cache.redis;

import com.alibaba.fastjson.JSON;
import com.xinbo.sports.dao.generator.po.BankList;
import com.xinbo.sports.dao.generator.service.BankListService;
import com.xinbo.sports.service.cache.KeyConstant;
import com.xinbo.sports.utils.JedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


/**
 * <p>
 * redis缓存类:银行卡相关缓存
 * </p>
 *
 * @author David
 * @since 2020/6/23
 */
@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BankCache {
    private final JedisUtil jedisUtil;
    private final BankListService bankListServiceImpl;
    private final ConfigCache configCache;


    /**
     * 获取银行卡列表缓存
     *
     * @return 银行卡列表
     */
    public BankList getBankCache(@NotNull Integer bankId) {
        String data = jedisUtil.hget(KeyConstant.BANK_LIST_HASH, bankId.toString());
        if (StringUtils.isNotBlank(data)) {
            return JSON.parseObject(data).toJavaObject(BankList.class);
        }
        BankList bank = bankListServiceImpl.lambdaQuery().eq(BankList::getId, bankId).one();
        if (bank != null) {
            bank.setIcon(bank.getIcon().startsWith("http")? bank.getIcon():configCache.getStaticServer() + bank.getIcon());
            jedisUtil.hset(KeyConstant.BANK_LIST_HASH, bankId.toString(), JSON.toJSONString(bank));
        }
        return bank;
    }

    /**
     * 根据语言获取当前国家银行列表
     *
     * @return 银行列表
     */
    public List<BankList> getBankListCache() {
        String country = configCache.getCountry();
        String key = KeyConstant.BANK_LIST_COUNTRY_HASH;
        String data = jedisUtil.hget(key, country);
        if (StringUtils.isNotBlank(data)) {
            return JSON.parseArray(data, BankList.class);
        }
        String staticServer = configCache.getStaticServer();
        // 获取游戏列表
        List<BankList> collect = bankListServiceImpl.lambdaQuery()
                .eq(BankList::getStatus, 1)
                .orderByDesc(BankList::getSort)
                .eq(BankList::getCountry, configCache.getCountry())
                .list()
                .stream()
                .map(o -> {
                    o.setIcon(o.getIcon().startsWith("http")?o.getIcon():staticServer + o.getIcon());
                    return o;
                }).collect(Collectors.toList());

        if (!collect.isEmpty()) {
            jedisUtil.hset(key, country, JSON.toJSONString(collect));
        }

        return collect;
    }

}