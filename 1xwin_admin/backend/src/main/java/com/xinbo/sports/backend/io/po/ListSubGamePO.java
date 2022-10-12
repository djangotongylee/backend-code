package com.xinbo.sports.backend.io.po;

import com.xinbo.sports.dao.generator.po.GameSlot;
import lombok.Data;

/**
 * <p>
 * 平台子游戏管理-列表
 * </p>
 *
 * @author andy
 * @since 2020/6/10
 */
@Data
public class ListSubGamePO extends GameSlot {
    private String name;
}
