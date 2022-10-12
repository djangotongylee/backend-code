package com.xinbo.sports.plat.factory;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams.PlatSlotGameFavoriteReqDto;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams.PlatSlotGameReqDto;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams.PlatSlotGameResDto;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;


/**
 * @author: David
 * @date: 03/05/2020
 * @description: 第三方游戏平台基类
 */
@Component
public interface PlatSlotAbstractFactory extends PlatAbstractFactory {
    /**
     * 初始化工厂实体
     *
     * @param model  子项目
     * @param parent 父类路径
     * @return 工厂实体
     */
    @Nullable
    static PlatSlotAbstractFactory init(String model, @NotNull String parent) {
        try {
            return (PlatSlotAbstractFactory) PlatAbstractFactory.init(model, parent);
        } catch (Exception e) {
            //
        }
        return null;
    }

    /**
     * 获取老虎机游戏列表(电子游戏类实现)
     *
     * @param platSlotgameReqDto {"id","uid","category","name}
     * @return 游戏列表
     * @author: David
     * @date: 04/05/2020
     */
    Page<PlatSlotGameResDto> getSlotGameList(ReqPage<PlatSlotGameReqDto> platSlotgameReqDto);

    /**
     * 收藏/取消 老虎机游戏(电子游戏类实现)
     *
     * @param platSlotGameFavoriteReqDto {gameId, gameSlotId, uid, direction}
     * @return success:1-成功 0-失败
     * @author: David
     * @date: 04/05/2020
     */
    Boolean favoriteSlotGame(PlatSlotGameFavoriteReqDto platSlotGameFavoriteReqDto);

    /**
     * 同步老虎机游戏列表
     */
    default void pullGames() {

    }
}

