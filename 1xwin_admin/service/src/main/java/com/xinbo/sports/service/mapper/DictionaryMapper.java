package com.xinbo.sports.service.mapper;

import com.xinbo.sports.service.io.bo.DictionaryBo;

import java.util.List;

/**
 * @description: 获取字典
 * @author: andy
 * @date: 2020/8/22
 */
public interface DictionaryMapper {
    /**
     * 获取字典列表
     *
     * @param bo 业务BO
     * @return 字典列表
     */
    List<DictionaryBo> listDictionary(DictionaryBo bo);
}
