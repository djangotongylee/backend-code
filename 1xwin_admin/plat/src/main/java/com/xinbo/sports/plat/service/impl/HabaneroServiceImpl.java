package com.xinbo.sports.plat.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinbo.sports.dao.generator.po.CoinPlatTransfer;
import com.xinbo.sports.dao.generator.po.GameSlot;
import com.xinbo.sports.dao.generator.service.BetslipsHbService;
import com.xinbo.sports.dao.generator.service.CoinPlatTransferService;
import com.xinbo.sports.dao.generator.service.GameSlotService;
import com.xinbo.sports.plat.base.SlotServiceBase;
import com.xinbo.sports.plat.factory.PlatSlotAbstractFactory;
import com.xinbo.sports.plat.io.bo.HBRequestParameter;
import com.xinbo.sports.plat.io.bo.HBResponseParameter;
import com.xinbo.sports.plat.io.bo.HBResponseParameter.QueryPlayerResponse;
import com.xinbo.sports.plat.io.enums.HBRoutineParam;
import com.xinbo.sports.plat.io.enums.HBUrlEnum;
import com.xinbo.sports.service.base.CoinPlatTransfersBase;
import com.xinbo.sports.service.base.UserServiceBase;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.utils.HttpUtils;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static com.xinbo.sports.plat.io.bo.PlatFactoryParams.*;

/**
 * @author: David
 * @date: 02/05/2020
 * @description:
 */
@Slf4j
@Service("HabaneroServiceImpl")
public class HabaneroServiceImpl implements PlatSlotAbstractFactory {
    protected static final String MODEL = "Habanero";
    @Setter
    protected HBRoutineParam.PlatConfig config;
    @Resource
    protected GameSlotService gameSlotServiceImpl;
    @Resource
    protected CoinPlatTransfersBase coinPlatTransfersBase;
    @Resource
    protected UserServiceBase userServiceBase;
    @Resource
    protected BetslipsHbService betslipsHbServiceImpl;
    @Resource
    protected ConfigCache configCache;
    @Resource
    private SlotServiceBase slotServiceBase;
    @Resource
    private CoinPlatTransferService coinPlatTransferServiceImpl;

    /**
     * ????????????
     *
     * @param platRegisterReqDto ??????????????????
     * @return true-?????? false-??????
     */
    @Override
    public java.lang.Boolean registerUser(PlatRegisterReqDto platRegisterReqDto) {
        checkReqParams(platRegisterReqDto);
        GameSlot one = gameSlotServiceImpl.lambdaQuery()
                .eq(GameSlot::getGameId, 202)
                .eq(GameSlot::getStatus, 1)
                .orderByDesc(GameSlot::getCreatedAt)
                .last("limit 1")
                .one();
        if (null == one) {
            throw new BusinessException(CodeInfo.PLAT_SYSTEM_ERROR);
        }
        return null != login(PlatLoginReqDto.builder()
                .username(platRegisterReqDto.getUsername())
                .lang(platRegisterReqDto.getLang())
                .device(platRegisterReqDto.getDevice())
                .slotId(one.getId())
                .build());
    }


    /**
     * ????????????????????? ?????? ??????????????????
     *
     * @param lang ????????????
     */
    public String switchLanguage(@NotNull String lang) {
        return EnumUtils.getEnumIgnoreCase(HBRoutineParam.LANGS.class, lang).getCode();
    }

    /**
     * ????????????????????????
     *
     * @param platLoginReqDto {username, lang, device, slotId}
     * @return ????????????
     */
    @Override
    public PlatGameLoginResDto login(PlatLoginReqDto platLoginReqDto) {
        checkReqParams(platLoginReqDto);
        String slotId = platLoginReqDto.getSlotId();
        GameSlot game = gameSlotServiceImpl.lambdaQuery().eq(GameSlot::getId, slotId).one();
        if (slotId == null || game == null) {
            throw new BusinessException(CodeInfo.GAME_NOT_EXISTS);
        }
        HBRequestParameter.LoginOrCreatePlayerRequest m = HBRequestParameter.LoginOrCreatePlayerRequest.builder()
                .BrandId(config.getBrandId())
                .APIKey(config.getApiKey())
                .PlayerHostAddress("")
                .UserAgent("")
                .KeepExistingToken(true)
                .Username(platLoginReqDto.getUsername())
                .Password(config.getPlayerPwd())
                .CurrencyCode(EnumUtils.getEnumIgnoreCase(HBRoutineParam.Currency.class, config.getCurrencyCode()).getCode())
                .build();
        JSONObject send = JSON.parseObject(send(HBUrlEnum.LOGIN_OR_CREATE_PLAYER, JSON.parseObject(JSON.toJSONString(m))));
        String url = config.getLoginUrl()
                + "?brandid=" + config.getBrandId()
                + "&keyname=" + game.getId()
                + "&token=" + send.getString("Token")
                + "&locale=" + switchLanguage(platLoginReqDto.getLang());
        return PlatGameLoginResDto.builder().type(1).url(url).build();
    }

    /**
     * ????????????
     *
     * @param dto {"username"}
     * @return success:1-?????? 0-??????
     * @author: David
     * @date: 04/05/2020
     */
    @Override
    public PlatLogoutResDto logout(@NotNull PlatLogoutReqDto dto) {
        checkReqParams(dto);
        HBRequestParameter.LogOutPlayer m = HBRequestParameter.LogOutPlayer.builder()
                .APIKey(config.getApiKey())
                .BrandId(config.getBrandId())
                .Username(dto.getUsername())
                .Password(config.getPlayerPwd())
                .build();
        JSONObject send = JSON.parseObject(send(HBUrlEnum.LOGOUT, JSON.parseObject(JSON.toJSONString(m))));
        HBResponseParameter.LogOutPlayerResponse response = send.toJavaObject(HBResponseParameter.LogOutPlayerResponse.class);
        if (java.lang.Boolean.FALSE.equals(response.getSuccess())) {
            log.error(response.toString());
            throw new BusinessException(CodeInfo.PLAT_LOGOUT_ERROR);
        }
        return PlatLogoutResDto.builder().success(1).build();
    }

    @Override
    public PlatCoinTransferResDto coinUp(@NotNull PlatCoinTransferReqDto platCoinTransferUpReqDto) {
        checkReqParams(platCoinTransferUpReqDto);
        HBRequestParameter.DepositPlayerMoneyRequest m = HBRequestParameter.DepositPlayerMoneyRequest.builder()
                .BrandId(config.getBrandId())
                .APIKey(config.getApiKey())
                .CurrencyCode(config.getCurrencyCode())
                .Amount(platCoinTransferUpReqDto.getCoin())
                .Password(config.getPlayerPwd())
                .RequestId(platCoinTransferUpReqDto.getOrderId())
                .Username(platCoinTransferUpReqDto.getUsername())
                .build();

        return coinTransfer(JSON.parseObject(JSON.toJSONString(m)), HBUrlEnum.DEPOSIT_PLAYER_MONEY);
    }

    @Override
    public PlatCoinTransferResDto coinDown(@NotNull PlatCoinTransferReqDto platCoinTransferDownReqDto) {
        checkReqParams(platCoinTransferDownReqDto);
        HBRequestParameter.WithdrawPlayerMoney m = HBRequestParameter.WithdrawPlayerMoney.builder()
                .BrandId(config.getBrandId())
                .APIKey(config.getApiKey())
                .CurrencyCode(config.getCurrencyCode())
                .Amount(platCoinTransferDownReqDto.getCoin().negate())
                .Password(config.getPlayerPwd())
                .RequestId(platCoinTransferDownReqDto.getOrderId())
                .Username(platCoinTransferDownReqDto.getUsername())
                .WithdrawAll(platCoinTransferDownReqDto.getIsFullAmount() != null && platCoinTransferDownReqDto.getIsFullAmount() == 1)
                .build();

        return coinTransfer(JSON.parseObject(JSON.toJSONString(m)), HBUrlEnum.WITHDRAW_PLAYER_MONEY);
    }

    /**
     * ????????????
     *
     * @param object    ??????
     * @param hbUrlEnum ????????????
     * @return {"platCoin"}
     */
    public PlatCoinTransferResDto coinTransfer(JSONObject object, HBUrlEnum hbUrlEnum) {
        JSONObject result = JSON.parseObject(send(hbUrlEnum, object));
        Integer status = java.lang.Boolean.TRUE.equals(result.getBoolean("Success")) ? 1 : 2;
        String mark = java.lang.Boolean.TRUE.equals(result.getBoolean("Success")) ? null : result.toJSONString();
        coinPlatTransfersBase.updateOrderPlat(object.getString("RequestId"), status, result.getBigDecimal("Amount").negate(), result.getString("TransactionId"), mark);
        return PlatCoinTransferResDto.builder().platCoin(result.getBigDecimal("RealBalance")).build();
    }

    /**
     * ??????????????????
     *
     * @param platQueryBalanceReqDto {"username"}
     * @return {platCoin}
     * @author: David
     * @date: 04/05/2020
     */
    @Override
    public PlatQueryBalanceResDto queryBalance(PlatQueryBalanceReqDto platQueryBalanceReqDto) {
        checkReqParams(platQueryBalanceReqDto);
        HBRequestParameter.QueryPlayerRequest m = HBRequestParameter.QueryPlayerRequest.builder()
                .APIKey(config.getApiKey())
                .BrandId(config.getBrandId())
                .Username(platQueryBalanceReqDto.getUsername())
                .Password(config.getPlayerPwd())
                .build();
        JSONObject send = JSON.parseObject(send(HBUrlEnum.QUERY_PLAYER, JSON.parseObject(JSON.toJSONString(m))));
        QueryPlayerResponse queryPlayerResponse = send.toJavaObject(QueryPlayerResponse.class);
        if (java.lang.Boolean.FALSE.equals(queryPlayerResponse.getFound())) {
            throw new BusinessException(CodeInfo.PLAT_ACCOUNT_NOT_EXISTS);
        }
        return PlatQueryBalanceResDto.builder().platCoin(queryPlayerResponse.getRealBalance()).build();
    }

    @Override
    public void pullBetsLips() {
        // ????????????????????????????????????(????????? ???????????????)
    }

    /**
     * ??????????????????
     *
     * @author: David
     * @date: 04/05/2020
     */
    @Override
    public Boolean checkTransferStatus(String orderId) {
        CoinPlatTransfer one = coinPlatTransferServiceImpl.getById(orderId);
        if (one == null) {
            throw new BusinessException(CodeInfo.PLAT_TRANSFER_CHECK_ORDER_INVALID);
        }

        HBRequestParameter.checkTransferStatusReqDto build = HBRequestParameter.checkTransferStatusReqDto.builder()
                .BrandId(config.getBrandId())
                .APIKey(config.getApiKey())
                .RequestId(orderId)
                .build();
        JSONObject send = JSON.parseObject(send(HBUrlEnum.QUERY_TRANSFER, JSON.parseObject(JSON.toJSONString(build))));
        HBRequestParameter.checkTransferStatusResDto resDto = JSON.toJavaObject(send, HBRequestParameter.checkTransferStatusResDto.class);
        return Boolean.TRUE.equals(resDto.getSuccess());
    }


    @Override
    public Page<PlatSlotGameResDto> getSlotGameList(ReqPage<PlatSlotGameReqDto> platSlotgameReqDto) {
        return slotServiceBase.getSlotGameList(platSlotgameReqDto);
    }

    /**
     * ??????/?????????????????????
     *
     * @param platSlotGameFavoriteReqDto {gameId, gameSlotId}
     * @author: David
     * @date: 04/05/2020
     */
    @Override
    public Boolean favoriteSlotGame(PlatSlotGameFavoriteReqDto platSlotGameFavoriteReqDto) {
        // ??????????????????????????????
        return true;
    }

    /**
     * @param hbUrlEnum uri??????
     */
    public String send(@NotNull HBUrlEnum hbUrlEnum, JSONObject json) {
        String url = config.getApiUrl() + hbUrlEnum.getUrl();
        String call = null;
        try {
            call = HttpUtils.doPost(url, json.toJSONString(), MODEL);
            parseSendError(hbUrlEnum.getName(), url, call);
        } catch (Exception e) {
            log.error(e.toString());
        }
        return call;
    }

    /**
     * ??????????????????
     */
    public void parseSendError(String name, String url, @NotNull String response) {
        CodeInfo codeInfo;
        if (response.contains(HBRoutineParam.ErrorCode.PARAMETER_ERROR.getMessage())) {
            codeInfo = CodeInfo.PLAT_SEND_PARAMS_INVALID;
        } else if (response.contains(HBRoutineParam.ErrorCode.TIME_OUT.getMessage())) {
            codeInfo = CodeInfo.PLAT_TIME_OUT;
        } else {
            return;
        }
        log.error("Habanero??????????????????, name = " + name
                + " url = " + url
                + ", response = " + response);

        throw new BusinessException(codeInfo);
    }

    /**
     * ??????????????????,???null?????????
     *
     * @param params ????????????
     * @param <T>    ??????
     */
    protected <T> void checkReqParams(T params) {
        if (null == params) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
    }
}
