package com.xinbo.sports.plat.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xinbo.sports.dao.generator.po.BetSlipsSupplemental;
import com.xinbo.sports.dao.generator.po.BetslipsAeKingMaker;
import com.xinbo.sports.dao.generator.po.BetslipsAeSexy;
import com.xinbo.sports.dao.generator.service.BetSlipsSupplementalService;
import com.xinbo.sports.dao.generator.service.BetslipsAeSexyService;
import com.xinbo.sports.dao.generator.service.impl.BetslipsAeKingMakerServiceImpl;
import com.xinbo.sports.plat.factory.PlatAbstractFactory;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams;
import com.xinbo.sports.plat.io.enums.AEBaseParam;
import com.xinbo.sports.plat.io.enums.AERequestParam;
import com.xinbo.sports.plat.io.enums.BasePlatParam;
import com.xinbo.sports.service.base.CoinPlatTransfersBase;
import com.xinbo.sports.service.base.UserServiceBase;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.enums.BaseEnum;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.HttpUtils;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.http.HttpConnectTimeoutException;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static com.xinbo.sports.plat.io.enums.AERequestParam.*;

/**
 * @author David
 * 性感真人实现类
 */
@Slf4j
@Service("AEServiceImpl")
public class AEServiceImpl implements PlatAbstractFactory {
    protected static final String MODEL = "AE";
    protected static final String TX_STATUS = "txStatus";
    protected static Map<String, Integer> aeSexyStatus;

    static {
        aeSexyStatus = new HashMap<>(4);
        aeSexyStatus.put("WIN", 1);
        aeSexyStatus.put("LOSE", 2);
        aeSexyStatus.put("TIE", 3);
        aeSexyStatus.put("VOID", 4);
    }

    @Resource
    protected BetSlipsSupplementalService betSlipsSupplementalServiceImpl;
    @Setter
    protected AEBaseParam.Config config;
    @Resource
    protected ConfigCache configCache;
    @Resource
    protected CoinPlatTransfersBase coinPlatTransfersBase;
    @Resource
    protected UserServiceBase userServiceBase;
    @Resource
    protected BetslipsAeSexyService betslipsAeSexyServiceImpl;
    @Resource
    protected BetslipsAeKingMakerServiceImpl betslipsAeKingMakerServiceImpl;


    /**
     * 创建会员
     *
     * @param reqDto 创建会员信息
     * @return true-成功 false-失败
     */
    @Override
    public Boolean registerUser(PlatFactoryParams.PlatRegisterReqDto reqDto) {
        CreateMemberReqDto build = CreateMemberReqDto.builder()
                .cert(config.getCert())
                .agentId(config.getAgentId())
                .userId(reqDto.getUsername())
                .currency(EnumUtils.getEnumIgnoreCase(AEBaseParam.Currency.class, config.getCurrency()).getCode())
                .betLimit(config.getBetLimit())
                .build();
        doSend(config.getApiUrl(), AEBaseParam.UrlEnum.CREATE_MEMBER, JSON.parseObject(JSON.toJSON(build).toString()));

        return true;
    }

    /**
     * 获取游戏登录链接
     *
     * @param platLoginReqDto {username, lang, device, slotId}
     * @return 登录链接
     */
    @Override
    public PlatFactoryParams.PlatGameLoginResDto login(PlatFactoryParams.PlatLoginReqDto platLoginReqDto) {
        LoginReqDto build = LoginReqDto.builder()
                .cert(config.getCert())
                .agentId(config.getAgentId())
                .userId(platLoginReqDto.getUsername())
                .externalURL(config.getExternalURL())
                .isMobileLogin(platLoginReqDto.getDevice().equals(BaseEnum.DEVICE.D.getValue()) ? "false" : "true")
                .language(EnumUtils.getEnumIgnoreCase(AEBaseParam.Lang.class, platLoginReqDto.getLang()).getCode())
                .build();

        JSONObject jsonObject = null;
        try {
            jsonObject = doSend(config.getApiUrl(), AEBaseParam.UrlEnum.LOGIN, JSON.parseObject(JSON.toJSON(build).toString()));
        } catch (BusinessException e) {
            // 账号不存在 先注册 再登录
            if (e.getCode().equals(CodeInfo.PLAT_ACCOUNT_NOT_EXISTS.getCode())) {
                boolean regSuccess = registerUser(
                        PlatFactoryParams.PlatRegisterReqDto.builder()
                                .username(platLoginReqDto.getUsername())
                                .device(platLoginReqDto.getDevice())
                                .lang(platLoginReqDto.getLang())
                                .build()
                );
                if (regSuccess) {
                    return login(platLoginReqDto);
                }
            }

        }

        return PlatFactoryParams.PlatGameLoginResDto.builder()
                .type(1)
                .url(jsonObject != null ? jsonObject.getString("url") : "")
                .build();
    }

    /**
     * 登出游戏
     *
     * @param platLoginReqDto {"username"}
     * @return success:1-成功 0-失败
     * @author: David
     * @date: 04/05/2020
     */
    @Override
    public PlatFactoryParams.PlatLogoutResDto logout(PlatFactoryParams.PlatLogoutReqDto platLoginReqDto) {
        LogoutReqDto build = LogoutReqDto.builder()
                .cert(config.getCert())
                .agentId(config.getAgentId())
                .userIds(platLoginReqDto.getUsername())
                .build();

        doSend(config.getApiUrl(), AEBaseParam.UrlEnum.LOGIN, JSON.parseObject(JSON.toJSON(build).toString()));
        return PlatFactoryParams.PlatLogoutResDto.builder().success(1).build();
    }

    /**
     * 三方上分
     *
     * @param platCoinTransferUpReqDto {"coin","orderId","username"}
     * @return {platCoin}
     * @author: David
     * @date: 04/05/2020
     */
    @Override
    public PlatFactoryParams.PlatCoinTransferResDto coinUp(PlatFactoryParams.PlatCoinTransferReqDto platCoinTransferUpReqDto) {
        DepositReqDto build = DepositReqDto.builder()
                .cert(config.getCert())
                .agentId(config.getAgentId())
                .userId(platCoinTransferUpReqDto.getUsername())
                .transferAmount(platCoinTransferUpReqDto.getCoin())
                .txCode(platCoinTransferUpReqDto.getOrderId())
                .build();

        JSONObject result = doSend(config.getApiUrl(), AEBaseParam.UrlEnum.DEPOSIT, JSON.parseObject(JSON.toJSON(build).toString()));

        // 上分成功正常处理流程
        DepositOrWithdrawalResDto transferRes = JSON.toJavaObject(result, DepositOrWithdrawalResDto.class);
        coinPlatTransfersBase.successUpdateTransferOrder(transferRes.getTxCode(), transferRes.getAmount(), transferRes.getDatabaseId());

        return PlatFactoryParams.PlatCoinTransferResDto.builder()
                .platCoin(transferRes.getCurrentBalance())
                .build();
    }

    /**
     * 三方下分
     *
     * @param platCoinTransferDownReqDto {"coin","orderId","username"}
     * @return {platCoin}
     * @author: David
     * @date: 04/05/2020
     */
    @Override
    public PlatFactoryParams.PlatCoinTransferResDto coinDown(PlatFactoryParams.PlatCoinTransferReqDto platCoinTransferDownReqDto) {
        WithdrawalReqDto build = WithdrawalReqDto.builder()
                .cert(config.getCert())
                .agentId(config.getAgentId())
                .userId(platCoinTransferDownReqDto.getUsername())
                .txCode(platCoinTransferDownReqDto.getOrderId())
                // 1: All, 0: Partial; default = 1
                .withdrawType(0)
                .transferAmount(platCoinTransferDownReqDto.getCoin().toString())
                .build();

        JSONObject result = doSend(config.getApiUrl(), AEBaseParam.UrlEnum.WITHDRAW, JSON.parseObject(JSON.toJSON(build).toString()));
        // 下分成功正常处理流程
        DepositOrWithdrawalResDto transferRes = JSON.toJavaObject(result, DepositOrWithdrawalResDto.class);
        coinPlatTransfersBase.successUpdateTransferOrder(transferRes.getTxCode(), transferRes.getAmount(), transferRes.getDatabaseId());

        return PlatFactoryParams.PlatCoinTransferResDto.builder().platCoin(transferRes.getCurrentBalance()).build();
    }

    /**
     * 查询三方余额
     *
     * @param platQueryBalanceReqDto {"username"}
     * @return {platCoin}
     * @author: David
     * @date: 04/05/2020
     */
    @Override
    public PlatFactoryParams.PlatQueryBalanceResDto queryBalance(PlatFactoryParams.PlatQueryBalanceReqDto platQueryBalanceReqDto) {
        QueryBalanceReqDto build = QueryBalanceReqDto.builder()
                .cert(config.getCert())
                .agentId(config.getAgentId())
                .userIds(platQueryBalanceReqDto.getUsername())
                .alluser(0)
                .build();

        JSONObject result = doSend(config.getApiUrl(), AEBaseParam.UrlEnum.GET_BALANCE, JSON.parseObject(JSON.toJSON(build).toString()));
        JSONArray results = result.getJSONArray("results");
        BigDecimal platCoin = BigDecimal.ZERO;
        for (Object sub : results) {
            JSONObject details = JSON.parseObject(sub.toString());
            if (details.getString("userId").equals(platQueryBalanceReqDto.getUsername())) {
                platCoin = details.getBigDecimal("balance");
            }
        }
        return PlatFactoryParams.PlatQueryBalanceResDto.builder().platCoin(platCoin).build();
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
        CheckTransferReqDto build = CheckTransferReqDto.builder()
                .cert(config.getCert())
                .agentId(config.getAgentId())
                .txCode(orderId)
                .build();
        JSONObject result = doSend(config.getApiUrl(), AEBaseParam.UrlEnum.CHECK_TRANSFER_OPERATION, JSON.parseObject(JSON.toJSON(build).toString()));

        // scenario 1:response status == 0000 and txStatus == 1 means deposit/withdraw success
        // scenario 2:response status == 0000 and txStatus == 0 means deposit/withdraw failure
        // scenario 3:response status == 1017 means deposit/withdraw failure
        String status = result.getString(AEBaseParam.RESPONSE_FLAG);
        if (status.equals(AEBaseParam.ErrorCode.Success.getCode())) {
            return result.getInteger(TX_STATUS).equals(1);
        } else if (status.equals(AEBaseParam.ErrorCode.TX_CODE_IS_NOT_EXIST.getCode())) {
            return false;
        } else {
            throw new BusinessException(CodeInfo.PLAT_SYSTEM_ERROR);
        }
    }

    /**
     * 以小时为时间跨度获取注单信息
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计信息
     */
    protected AERequestParam.GetSummaryByTxTimeHourResDto getSummaryByTxTimeHour(String startTime, String endTime) {
        // 检查时间段内投注信息是否一致
        GetSummaryByTxTimeHourReqDto build = GetSummaryByTxTimeHourReqDto.builder()
                .cert(config.getCert())
                .agentId(config.getAgentId())
                .startTime(startTime)
                .endTime(endTime)
                .build();

        JSONObject result = doSend(config.getRecordUrl(), AEBaseParam.UrlEnum.GET_SUMMARY_BY_TX_TIME_HOUR, JSON.parseObject(JSON.toJSON(build).toString()));
        return JSON.toJavaObject(result.getJSONObject("transactions"), GetSummaryByTxTimeHourResDto.class);
    }

    /**
     * 发送HTTP请求
     *
     * @param uri      请求地址
     * @param urlEnum  请求方法Enum
     * @param postData 请求参数
     * @return 请求响应
     */
    public JSONObject doSend(String uri, @NotNull AEBaseParam.UrlEnum urlEnum, JSONObject postData) {
        return doSend(uri, urlEnum, postData, MODEL);
    }

    /**
     * 发送HTTP请求
     *
     * @param uri      请求地址
     * @param urlEnum  请求方法Enum
     * @param postData 请求参数
     * @return 请求响应
     */
    public JSONObject doSend(String uri, @NotNull AEBaseParam.UrlEnum urlEnum, JSONObject postData, String model) {
        String url = uri + urlEnum.getUrl();

        try {
            String result = HttpUtils.doPost(url, postData, AEBaseParam.HTTP_HEADER, model);
            JSONObject json = JSON.parseObject(result);
            if (json.getString(AEBaseParam.RESPONSE_FLAG).equals(AEBaseParam.ErrorCode.Success.getCode())) {
                return json;
            }

            switch (urlEnum) {
                case DEPOSIT:
                case WITHDRAW:
                    // 上分异常处理 回滚用户金额, 下分异常处理  status: 1-成功 2-失败
                    coinPlatTransfersBase.failureUpdateTransferOrder(postData.getString("txCode"), json.toJSONString());
                    break;
                // 检查下分、下分状态直接返回
                case CHECK_TRANSFER_OPERATION:
                    return json;
                default:
                    break;
            }

            // 转义平台自定义异常
            throw new BusinessException(AEBaseParam.ERROR_CODE_MAPPER.getOrDefault(json.getString(AEBaseParam.RESPONSE_FLAG), CodeInfo.PLAT_SYSTEM_ERROR));
        } catch (BusinessException e) {
            throw e;
        } catch (ConnectException e) {
            throw new BusinessException(CodeInfo.PLAT_CONNECTION_REFUSED);
        } catch (HttpConnectTimeoutException e) {
            throw new BusinessException(CodeInfo.PLAT_TIME_OUT);
        } catch (Exception e) {
            throw new BusinessException(CodeInfo.PLAT_SYSTEM_ERROR);
        }
    }

    /**
     * 拉单发送HTTP请求
     *
     * @param uri      请求地址
     * @param urlEnum  请求方法Enum
     * @param postData 请求参数
     * @return 请求响应
     */
    public JSONObject pullSend(String uri, @NotNull AEBaseParam.UrlEnum urlEnum, JSONObject postData, String model) {
        JSONObject json = null;
        try {
            String url = uri + urlEnum.getUrl();
            String result = HttpUtils.doPost(url, postData, AEBaseParam.HTTP_HEADER, model);
            json = JSON.parseObject(result);
            if (json.getString(AEBaseParam.RESPONSE_FLAG).equals(AEBaseParam.ErrorCode.Success.getCode())) {
                return json;
            }
            // 三方异常
            buildCodeInfo(result);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            buildCodeInfo(e.toString());
        }
        return json;
    }

    /**
     * 获取上一次注单时间
     *
     * @param platform 子平台
     * @return 拉单时间
     */
    public AERequestParam.GetTransactionByUpdateDateReqDto buildPullBetsLipsParams(@NotNull AEBaseParam.PLATFORM platform) {
        // 根据MODEL_SUB获取最近更新时间
        String lastTime = configCache.getLastUpdateTimeByModel(platform.getModel());
        if (StringUtils.isNotBlank(lastTime)) {
            return GetTransactionByUpdateDateReqDto.builder()
                    .cert(config.getCert())
                    .agentId(config.getAgentId())
                    .timeFrom(lastTime)
                    .platform(platform.getCode())
                    .build();
        }

        String updateTime = "";
        switch (platform) {
            case SEXYBCRT:
                BetslipsAeSexy sexy = betslipsAeSexyServiceImpl.lambdaQuery().orderByDesc(BetslipsAeSexy::getUpdateTime).last("limit 1").one();
                if (sexy != null) {
                    int utc = sexy.getUpdatedAt();
                    int max = Math.max(utc, DateNewUtils.now() - 60 * 60 * 23);
                    updateTime = DateNewUtils.utc8Str(DateNewUtils.utc8Zoned(max), DateNewUtils.Format.yyyy_MM_dd_T_HH_mm_ss_XXX);
                }
                break;
            case KINGMAKER:
                BetslipsAeKingMaker kingMaker = betslipsAeKingMakerServiceImpl.lambdaQuery().orderByDesc(BetslipsAeKingMaker::getUpdateTime).last("limit 1").one();
                if (kingMaker != null) {
                    int utc = kingMaker.getCreatedAt();
                    int max = Math.max(utc, DateNewUtils.now() - 60 * 60 * 23);
                    updateTime = DateNewUtils.utc8Str(DateNewUtils.utc8Zoned(max), DateNewUtils.Format.yyyy_MM_dd_T_HH_mm_ss_XXX);
                }
                break;
            default:
                break;
        }

        if (StringUtils.isBlank(updateTime)) {
            // 数据库不存在数据 && 上次拉单节点不存在  取12小时前数据
            updateTime = DateNewUtils.utc8Str(ZonedDateTime.now().minusHours(12), DateNewUtils.Format.yyyy_MM_dd_T_HH_mm_ss_XXX);
        }

        return GetTransactionByUpdateDateReqDto.builder()
                .cert(config.getCert())
                .agentId(config.getAgentId())
                .timeFrom(updateTime)
                .platform(platform.getCode())
                .build();
    }

    /**
     * 根据起始、结束时间 生成补单信息
     *
     * @param dto dto.start 开始时间 dto.end结束时间
     */
    @Override
    public void genSupplementsOrders(@NotNull PlatFactoryParams.GenSupplementsOrdersReqDto dto) {
        // 补单最大时间跨度7天
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime start = DateNewUtils.utc8Zoned(dto.getStart());
        ZonedDateTime end = DateNewUtils.utc8Zoned(dto.getEnd());
        if (start.compareTo(now.minusDays(7)) < 0 || start.compareTo(end) >= 0) {
            throw new BusinessException(CodeInfo.PLAT_BET_SLIPS_SUPPLE_OVER_7_DAYS);
        }
        if (end.compareTo(now) > 0) {
            end = now;
        }

        ZonedDateTime temp;
        LinkedList<BetSlipsSupplemental> list = new LinkedList<>();
        do {
            temp = start.plusMinutes(10);

            BetSlipsSupplemental betSlipsSupplemental = new BetSlipsSupplemental();
            betSlipsSupplemental.setGameListId(dto.getGameId());
            betSlipsSupplemental.setTimeStart(start.toString());
            betSlipsSupplemental.setTimeEnd(temp.toString());

            GetTransactionByTxTimeReqDto build = GetTransactionByTxTimeReqDto.builder()
                    .cert(config.getCert())
                    .agentId(config.getAgentId())
                    .startTime(DateNewUtils.utc8Str(start, DateNewUtils.Format.yyyy_MM_dd_T_HH_mm_ss_XXX))
                    .endTime(DateNewUtils.utc8Str(temp, DateNewUtils.Format.yyyy_MM_dd_T_HH_mm_ss_XXX))
                    .platform(dto.getGameId().equals(BasePlatParam.GAME.SEXY_LIVE.getCode()) ? AEBaseParam.PLATFORM.SEXYBCRT.getCode() : AEBaseParam.PLATFORM.KINGMAKER.getCode())
                    .build();
            betSlipsSupplemental.setRequest(JSON.toJSONString(build));
            betSlipsSupplemental.setUpdatedAt((int) now.toInstant().getEpochSecond());
            betSlipsSupplemental.setCreatedAt((int) now.toInstant().getEpochSecond());
            list.add(betSlipsSupplemental);

            start = start.plusMinutes(10);
        } while (temp.compareTo(end) <= 0);

        betSlipsSupplementalServiceImpl.saveBatch(list);
    }

    /**
     * 封装三方异常抛出
     *
     * @param msg MSG
     */
    protected void buildCodeInfo(String msg) {
        CodeInfo codeInfo = CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0;
        log.error(MODEL + codeInfo.getMsg() + ":" + msg);
        codeInfo.setMsg(msg);
        throw new BusinessException(codeInfo);
    }
}