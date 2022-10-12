package com.xinbo.sports.plat.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.xinbo.sports.dao.generator.service.BetslipsSboSportsService;
import com.xinbo.sports.plat.factory.PlatAbstractFactory;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams.*;
import com.xinbo.sports.plat.io.bo.SBOSportsRequestParameter;
import com.xinbo.sports.plat.io.enums.SBORoutineParam;
import com.xinbo.sports.plat.io.enums.SBOUrlEnum;
import com.xinbo.sports.service.base.CoinPlatTransfersBase;
import com.xinbo.sports.service.base.UserServiceBase;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.utils.HttpUtils;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.xinbo.sports.plat.io.enums.SBORoutineParam.Lang;

/**
 * @author: David
 * @date: 02/05/2020
 * @description:
 */
@Slf4j
@Service("SBOServiceImpl")
public class SBOServiceImpl implements PlatAbstractFactory {
    protected static final String MODEL = "SBO";
    private static final String BALANCE = "balance";
    /**
     * 游戏类Model
     */
    protected String portfolio = SBORoutineParam.Portfolio.SportsBook.name();
    @Resource
    protected BetslipsSboSportsService betslipsSboSportsServiceImpl;
    @Resource
    protected UserServiceBase userServiceBase;
    @Resource
    protected ConfigCache configCache;
    /**
     * 父类Model
     */
    @Setter
    protected SBORoutineParam.PlatConfig config;

    @Resource
    CoinPlatTransfersBase coinPlatTransfersBase;


    @Override
    public PlatLogoutResDto logout(PlatLogoutReqDto dto) {
        SBOSportsRequestParameter.Logout m = SBOSportsRequestParameter.Logout.builder()
                .username(dto.getUsername())
                .build();

        try {
            JSONObject jsonObject = buildCommonReqParams(m);
            this.send(SBOUrlEnum.LOGOUT, jsonObject.toJSONString());
        } catch (Exception e) {
            log.error(e.toString());
            throw new BusinessException(CodeInfo.PLAT_SYSTEM_ERROR);
        }

        return null;
    }

    /**
     * 创建用户代理
     *
     * @return true-成功 false-不成功
     */
    public Boolean registerAgent() {
        SBOSportsRequestParameter.RegisterAgent m = SBOSportsRequestParameter.RegisterAgent
                .builder()
                .companyKey(config.getCompanyKey())
                .serverId(config.getServerId())
                .username(config.getAgentName())
                .password(config.getAgentPwd())
                .currency(EnumUtils.getEnumIgnoreCase(SBORoutineParam.Currency.class, config.getCurrency()).getCode())
                .min(1)
                .max(500000)
                .maxPerMatch(500000)
                .casinoTableLimit(1)
                .build();

        try {
            JSONObject jsonObject = buildCommonReqParams(m);
            this.send(SBOUrlEnum.REGISTER_AGENT, jsonObject.toJSONString());
        } catch (Exception e) {
            log.warn(e.toString());
            throw e;
        }

        return true;
    }

    /**
     * 创建会员
     *
     * @param platLoginReqDto 入参
     * @return true-成功 false-不成功
     */
    @Override
    public Boolean registerUser(@NotNull PlatRegisterReqDto platLoginReqDto) {
        SBOSportsRequestParameter.RegisterPlayer m = SBOSportsRequestParameter.RegisterPlayer.builder()
                .username(platLoginReqDto.getUsername())
                .agent(config.getAgentName())
                .build();
        JSONObject reqParams = buildCommonReqParams(m);
        return null != send(SBOUrlEnum.REGISTER_PLAYER, reqParams.toJSONString());
    }

    @Override
    public PlatCoinTransferResDto coinUp(PlatCoinTransferReqDto platCoinTransferUpReqDto) {
        SBOSportsRequestParameter.Deposit m = SBOSportsRequestParameter.Deposit.builder()
                .amount(platCoinTransferUpReqDto.getCoin())
                .username(platCoinTransferUpReqDto.getUsername())
                .txnId(platCoinTransferUpReqDto.getOrderId())
                .build();
        try {
            JSONObject reqParams = buildCommonReqParams(m);
            JSONObject result = this.send(SBOUrlEnum.DEPOSIT, reqParams.toJSONString());
            coinPlatTransfersBase.updateOrderPlat(platCoinTransferUpReqDto.getOrderId(), 1, result.getBigDecimal(BALANCE), result.getString("refno"), null);
            return PlatCoinTransferResDto.builder().platCoin(result.getBigDecimal(BALANCE)).build();
        } catch (Exception e) {
            log.error(e.toString());
            coinPlatTransfersBase.updateOrderPlat(platCoinTransferUpReqDto.getOrderId(), 2, platCoinTransferUpReqDto.getCoin(), "", e.toString());
            throw e;
        }
    }

    @Override
    public PlatCoinTransferResDto coinDown(PlatCoinTransferReqDto platCoinTransferDownReqDto) {
        SBOSportsRequestParameter.Withdraw m = SBOSportsRequestParameter.Withdraw.builder()
                .amount(platCoinTransferDownReqDto.getCoin())
                .isFullAmount(platCoinTransferDownReqDto.getIsFullAmount() != null && platCoinTransferDownReqDto.getIsFullAmount() == 1)
                .username(platCoinTransferDownReqDto.getUsername())
                .txnId(platCoinTransferDownReqDto.getOrderId())
                .build();
        try {
            JSONObject reqParams = buildCommonReqParams(m);
            JSONObject result = this.send(SBOUrlEnum.WITHDRAW, reqParams.toJSONString());
            coinPlatTransfersBase.updateOrderPlat(platCoinTransferDownReqDto.getOrderId(), 1, result.getBigDecimal("amount"), result.getString("refno"), null);
            return PlatCoinTransferResDto.builder().platCoin(result.getBigDecimal(BALANCE)).build();
        } catch (Exception e) {
            log.error(e.toString());
            coinPlatTransfersBase.updateOrderPlat(platCoinTransferDownReqDto.getOrderId(), 2, platCoinTransferDownReqDto.getCoin(), "", e.toString());
            throw e;
        }
    }

    @Override
    public PlatQueryBalanceResDto queryBalance(PlatQueryBalanceReqDto platQueryBalanceReqDto) {
        SBOSportsRequestParameter.GetPlayerBalance m = SBOSportsRequestParameter.GetPlayerBalance.builder()
                .username(platQueryBalanceReqDto.getUsername())
                .build();

        try {
            JSONObject reqParams = buildCommonReqParams(m);
            JSONObject result = this.send(SBOUrlEnum.GET_PLAYER_BALANCE, reqParams.toJSONString());

            BigDecimal bBalance = result.getBigDecimal(BALANCE)
                    .subtract(result.getBigDecimal("outstanding"))
                    .setScale(2, RoundingMode.DOWN);

            return PlatQueryBalanceResDto.builder().platCoin(bBalance).build();
        } catch (Exception e) {
            log.error(e.toString());
            throw e;
        }
    }

    @Override
    public PlatGameLoginResDto login(PlatLoginReqDto platLoginReqDto) {
        // 子类实现
        return null;
    }

    /**
     * 拉取三方注单信息
     *
     * @author: David
     * @date: 04/05/2020
     */
    @Override
    public void pullBetsLips() {
        // 子类实现
    }

    /**
     * 检查转账状态
     *
     * @author: David
     * @date: 04/05/2020
     */
    @Override
    public Boolean checkTransferStatus(String orderId) {
        if (org.apache.commons.lang3.StringUtils.isBlank(orderId)) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        JSONObject params = new JSONObject();
        params.put("TxnId", orderId);
        JSONObject reqParams = buildCommonReqParams(params);
        JSONObject jResult = this.send(SBOUrlEnum.CHECK_TRANSACTION_STATUS, reqParams.toJSONString());
        JSONObject error = jResult.getJSONObject("error");
        Integer code = error.getInteger("id");
        return (null != code && 0 == code);
    }

    public void parseSendError(String name, String url, @NotNull JSONObject error) {
        Integer code = error.getInteger("id");
        log.error("SBO调用出现异常, name = " + name
                + " url = " + url
                + ", code = " + code
                + ", message = " + error.getString("msg"));
        CodeInfo codeInfo = SBORoutineParam.ERROR_CODE_MAPPER.get(code);
        if (null == codeInfo) {
            throw new BusinessException(CodeInfo.PLAT_SYSTEM_ERROR);
        }
        throw new BusinessException(codeInfo);
    }

    /**
     * 调用三方客户端
     *
     * @param thirdSBOSEnum 请求实体
     * @param reqParams     请求数据
     * @return 请求结果
     */
    public JSONObject send(SBOUrlEnum thirdSBOSEnum, String reqParams) {
        /* 通过model从redis获取域名、CompanyKey、ServerId */
        String url = thirdSBOSEnum.getUrl();
        String apiAddress = config.getApiUrl() + url;


        String sResult;
        try {
            sResult = HttpUtils.doPost(apiAddress, reqParams, MODEL);
            log.info(apiAddress);
            log.info(reqParams);
        } catch (Exception e) {
            log.error("调用第三方接口出现异常, 三方平台code = " + "SBO" + ", errorMsg = " + e.getMessage());
            throw new BusinessException(CodeInfo.PLAT_SYSTEM_ERROR);
        }

        JSONObject jResult = JSON.parseObject(sResult);
        JSONObject error = jResult.getJSONObject("error");
        Integer code = error.getInteger("id");
        if (code == null || code != 0) {
            parseSendError(thirdSBOSEnum.getName(), thirdSBOSEnum.getUrl(), error);
        }

        return jResult;
    }

    protected JSONObject buildCommonReqParams(Object object) {
        String params = JSON.toJSONString(object, SerializerFeature.WriteMapNullValue,
                SerializerFeature.DisableCircularReferenceDetect,
                SerializerFeature.WriteNonStringKeyAsString);
        JSONObject json = JSON.parseObject(params);
        json.put("CompanyKey", config.getCompanyKey());
        json.put("ServerId", config.getServerId());
        return json;
    }

    /**
     * 系统语言标识切换到平台方认证语言标识
     *
     * @param lang en zh-cn zh-tw
     */
    public String switchLanguage(String lang) {
        String language;
        switch (lang) {
            case "zh":
                language = Lang.ZH_CN.getValue();
                break;
            case "th":
                language = Lang.TH_TH.getValue();
                break;
            case "vi":
                language = Lang.VI_VN.getValue();
                break;
            case "en":
            default:
                language = Lang.EN.getValue();
                break;
        }

        return language;
    }
}
