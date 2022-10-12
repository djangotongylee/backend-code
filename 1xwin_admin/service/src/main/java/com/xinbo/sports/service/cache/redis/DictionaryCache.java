package com.xinbo.sports.service.cache.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.google.common.collect.Maps;
import com.xinbo.sports.dao.generator.po.GameSlot;
import com.xinbo.sports.dao.generator.service.GameSlotService;
import com.xinbo.sports.service.cache.KeyConstant;
import com.xinbo.sports.service.io.bo.DictionaryBo;
import com.xinbo.sports.service.io.dto.Dictionary;
import com.xinbo.sports.service.mapper.DictionaryMapper;
import com.xinbo.sports.utils.JedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.alibaba.fastjson.JSON.parseObject;

/**
 * @description:字典缓存
 * @author: andy
 * @date: 2020/9/6
 */
@Component
public class DictionaryCache {
    @Resource
    private JedisUtil jedisUtil;
    @Resource
    private DictionaryMapper dictionaryMapper;
    @Resource
    private GameSlotService gameSlotServiceImpl;

    /**
     * 根据category获取字典项列表
     * 描述:当category为null时查询所有
     *
     * @param reqDictionary 字典类型
     * @return map集合
     */
    public Map<String, List<Dictionary.ResDto>> listDictionary(DictionaryBo reqDictionary) {
        String key = reqDictionary.getDictHashKey();
        String subKey = KeyConstant.COMMON_TOTAL_HASH;
        if (StringUtils.isNotBlank(reqDictionary.getCategory())) {
            subKey = reqDictionary.getCategory();
        }
        String data = jedisUtil.hget(key, subKey);
        if (StringUtils.isNotBlank(data)) {
            JSONObject jsonObject = parseObject(data);
            return jsonObject.toJavaObject(new TypeReference<Map<String, List<Dictionary.ResDto>>>() {
            });
        }

        Map<String, List<Dictionary.ResDto>> mapAllDictionary = Maps.newHashMap();
        List<DictionaryBo> dictionaryBoList = dictionaryMapper.listDictionary(reqDictionary);
        dictionaryBoList.forEach(s -> {
            Dictionary.ResDto dto = new Dictionary.ResDto();
            dto.setCode(s.getCode());
            dto.setTitle(s.getTitle());

            String category = s.getCategory();
            if (mapAllDictionary.containsKey(category)) {
                mapAllDictionary.get(category).add(dto);
            } else {
                List<Dictionary.ResDto> list = new ArrayList<>();
                list.add(dto);
                mapAllDictionary.put(category, list);
            }
        });
        if (Optional.ofNullable(mapAllDictionary).isPresent() && mapAllDictionary.size() > 0) {
            jedisUtil.hset(key, subKey, JSON.toJSONString(mapAllDictionary));
        }
        return mapAllDictionary;
    }

    /**
     * 获取sp_dict_item表的一条记录
     *
     * @param reqDictionary sp_dict表的category
     * @return sp_dict_item表的当条记录
     */
    public Map<String, String> getCategoryMap(DictionaryBo reqDictionary) {
        Map<String, String> map = new HashMap<>();
        Map<String, List<Dictionary.ResDto>> tmpMap = this.listDictionary(reqDictionary);
        if (Optional.ofNullable(tmpMap).isEmpty() || tmpMap.size() == 0) {
            return map;
        }
        List<Dictionary.ResDto> listDictItem = tmpMap.get(reqDictionary.getCategory());
        if (!CollectionUtils.isEmpty(listDictItem)) {
            map = listDictItem.stream().collect(Collectors.toMap(Dictionary.ResDto::getCode, Dictionary.ResDto::getTitle));
        }
        return map;
    }

    /**
     * 获取老虎机游戏列表
     */
    public GameSlot getSlotGameCache(@NotNull String id) {
        String data = jedisUtil.hget(KeyConstant.GAME_SLOT_LIST_HASH, id);
        if (StringUtils.isNotBlank(data)) {
            return JSON.parseObject(data).toJavaObject(GameSlot.class);
        }

        GameSlot one = gameSlotServiceImpl.lambdaQuery().eq(GameSlot::getId, id).one();

        if (one != null) {
            jedisUtil.hset(KeyConstant.GAME_SLOT_LIST_HASH, id, JSON.toJSONString(one));
        }

        return one;
    }

    public void updateSlotGameCache(@NotNull String id) {
        jedisUtil.hdel(KeyConstant.GAME_SLOT_LIST_HASH, id);
        getSlotGameCache(id);
    }
}
