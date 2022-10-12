package com.xinbo.sports.plat.factory;

import com.xinbo.sports.plat.io.dto.base.BetslipsDetailDto;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author: David
 * @date: 03/05/2020
 * @description: 第三方游戏平台基类
 */
@Component
public interface PlatSportsAbstractFactory extends PlatAbstractFactory {
    /**
     * 初始化工厂实体
     *
     * @param model  子项目
     * @param parent 父类路径
     * @return 工厂实体
     */
    @Nullable
    static PlatSportsAbstractFactory init(String model, @NotNull String parent) {
        try {
            return (PlatSportsAbstractFactory) PlatAbstractFactory.init(model, parent);
        } catch (Exception e) {
            //
        }
        return null;
    }

    List<BetslipsDetailDto.SportSchedule> getSportSchedule(ReqPage<BetslipsDetailDto.SportScheduleReqDto> dto, Integer gameId);

    BetslipsDetailDto.ForwardEvent forwardEvent(String username, Integer masterEventID);

}

