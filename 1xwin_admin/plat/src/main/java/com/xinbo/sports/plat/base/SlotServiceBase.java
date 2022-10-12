package com.xinbo.sports.plat.base;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinbo.sports.dao.generator.mapper.GameSlotFavoriteMapper;
import com.xinbo.sports.dao.generator.po.GameSlot;
import com.xinbo.sports.dao.generator.po.GameSlotFavorite;
import com.xinbo.sports.dao.generator.service.GameSlotFavoriteService;
import com.xinbo.sports.dao.generator.service.GameSlotService;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams.PlatSlotGameFavoriteReqDto;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams.PlatSlotGameReqDto;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams.PlatSlotGameResDto;
import com.xinbo.sports.service.base.UserServiceBase;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.enums.BaseEnum;
import com.xinbo.sports.utils.BeanConvertUtils;
import com.xinbo.sports.utils.DateUtils;
import com.xinbo.sports.utils.I18nUtils;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.response.CodeInfo;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * @author: wells
 * @date: 2020/6/8
 * @description:
 */
@Service
public class SlotServiceBase {
    public static final int HB_SLOT_ID = 202;
    public static final int MG_SLOT_ID = 203;
    public static final int UPG_SLOT_ID = 204;
    public static final int CQ9_SLOT_ID = 205;
    public static final int JOKER_SLOT_ID = 206;
    public static final int PG_SLOT_ID = 207;
    @Resource
    protected UserServiceBase userServiceBase;
    @Resource
    protected ConfigCache configCache;
    @Resource
    protected GameSlotService gameSlotServiceImpl;
    @Resource
    private GameSlotFavoriteService gameSlotFavoriteServiceImpl;
    @Resource
    private GameSlotFavoriteMapper gameSlotFavoriteMapper;

    /**
     * 获取老虎机游戏列表(电子游戏类实现)
     *
     * @param platSlotgameReqDto {"id","uid","category","name}
     * @return 游戏列表
     * @author: David
     * @date: 04/05/2020
     */
    public Page<PlatSlotGameResDto> getSlotGameList(@NotNull ReqPage<PlatSlotGameReqDto> platSlotgameReqDto) {
        String staticServer = configCache.getStaticServer();
        PlatSlotGameReqDto platSlotGameReqDto = platSlotgameReqDto.getData();
        List<String> favoriteList = userServiceBase.slotFavoriteByUid(platSlotGameReqDto.getUid(), platSlotGameReqDto.getId());
        LambdaQueryChainWrapper<GameSlot> eq = gameSlotServiceImpl.lambdaQuery()
                .eq(GameSlot::getGameId, platSlotGameReqDto.getId())
                .eq(GameSlot::getStatus, 1);

        if (null != platSlotGameReqDto.getDevice()) {
            int d = platSlotGameReqDto.getDevice().equals(BaseEnum.DEVICE.D.getValue()) ? 1 : 2;
            eq.in(GameSlot::getDevice, 0, d);
        }
        Integer category = platSlotgameReqDto.getData().getCategory();
        switch (category) {
            // 种类:0-全部游戏 1-热门游戏 2-最新游戏 3-我的收藏
            case 0:
                eq.orderByDesc(GameSlot::getHotStar);
                break;
            case 1:
                eq.orderByDesc(GameSlot::getHotStar)
                        .orderByDesc(GameSlot::getSort);
                break;
            case 2:
                eq.orderByDesc(GameSlot::getIsNew)
                        .orderByDesc(GameSlot::getSort);
                break;
            case 3:
                eq.in(GameSlot::getId, favoriteList)
                        .orderByDesc(GameSlot::getHotStar)
                        .orderByDesc(GameSlot::getSort);
                break;
            default:
                break;
        }
        String gameName = platSlotgameReqDto.getData().getName();
        if (gameName != null && !"".equals(gameName)) {
            if (platSlotGameReqDto.getLang().equals(BaseEnum.LANG.ZH.getValue())) {
                eq.likeRight(GameSlot::getNameZh, gameName);
            } else {
                eq.likeRight(GameSlot::getName, gameName);
            }
        }

        // 分页数据结果集
        Page<GameSlot> page = eq.page(platSlotgameReqDto.getPage());
        Locale localeZhCn = new Locale("zh", "CN");
        return BeanConvertUtils.copyPageProperties(
                page,
                PlatSlotGameResDto::new,
                (ori, dest) -> {
                    dest.setIsFavorite(favoriteList.contains(ori.getId()) ? 1 : 0);
                    // 201 HB是支持中文图片
                    List<Integer> slots = Arrays.asList(HB_SLOT_ID, MG_SLOT_ID, UPG_SLOT_ID, CQ9_SLOT_ID, JOKER_SLOT_ID,PG_SLOT_ID);
                    if (slots.stream().anyMatch(x -> x.equals(platSlotGameReqDto.getId())) && platSlotGameReqDto.getLang().equals(BaseEnum.LANG.ZH.getValue())) {
                        dest.setName(ori.getNameZh());
                        dest.setImg(I18nUtils.getLocaleImg(staticServer + dest.getImg(), localeZhCn));
                    } else {
                        dest.setImg(dest.getImg().startsWith("http")?dest.getImg():staticServer + dest.getImg());
                    }
                }
        );
    }

    /**
     * 收藏/取消老虎机游戏
     *
     * @param platSlotGameFavoriteReqDto {gameId, gameSlotId}
     * @author: David
     * @date: 04/05/2020
     */
    public Boolean favoriteSlotGame(PlatSlotGameFavoriteReqDto platSlotGameFavoriteReqDto) {
        GameSlot slotId = gameSlotServiceImpl.lambdaQuery()
                .eq(GameSlot::getId, platSlotGameFavoriteReqDto.getGameSlotId())
                .one();
        if (slotId == null) {
            throw new BusinessException(CodeInfo.GAME_NOT_EXISTS);
        }

        GameSlotFavorite one = gameSlotFavoriteServiceImpl.lambdaQuery()
                .eq(GameSlotFavorite::getUid, platSlotGameFavoriteReqDto.getUid())
                .eq(GameSlotFavorite::getGameId, platSlotGameFavoriteReqDto.getGameId())
                .eq(GameSlotFavorite::getGameSlotId, platSlotGameFavoriteReqDto.getGameSlotId())
                .one();

        if (platSlotGameFavoriteReqDto.getDirection().equals(1) && null == one) {
            Integer time = DateUtils.getCurrentTime();
            GameSlotFavorite gsf = new GameSlotFavorite();
            gsf.setUid(platSlotGameFavoriteReqDto.getUid());
            gsf.setGameId(platSlotGameFavoriteReqDto.getGameId());
            gsf.setGameSlotId(platSlotGameFavoriteReqDto.getGameSlotId());
            gsf.setCreatedAt(time);
            gsf.setUpdatedAt(time);
            gameSlotFavoriteServiceImpl.save(gsf);
        } else if (platSlotGameFavoriteReqDto.getDirection().equals(0) && null != one) {
            gameSlotFavoriteMapper.deleteById(one.getId());
        } else {
            if (platSlotGameFavoriteReqDto.getDirection().equals(1)) {
                throw new BusinessException(CodeInfo.GAME_SLOT_FAVORITE_ALREADY);
            } else {
                throw new BusinessException(CodeInfo.GAME_SLOT_FAVORITE_NOT_YET);

            }
        }

        return Boolean.TRUE;
    }

}
