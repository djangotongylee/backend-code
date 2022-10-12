package com.xinbo.sports.plat.service.impl.ae;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xinbo.sports.dao.generator.po.BetslipsAeKingMaker;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams;
import com.xinbo.sports.plat.io.enums.AEBaseParam;
import com.xinbo.sports.plat.io.enums.AERequestParam.GetTransactionByUpdateDateReqDto;
import com.xinbo.sports.plat.service.impl.AEServiceImpl;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Contract;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AE KINGMAKER
 *
 * @author David
 */
@Service
@Slf4j
public class KingMakerChessServiceImpl extends AEServiceImpl {
    /**
     * 拉取三方注单信息
     *
     * @author: David
     * @date: 04/05/2020
     */
    @Override
    public void pullBetsLips() {
        AEBaseParam.PLATFORM platform = AEBaseParam.PLATFORM.KINGMAKER;
        // 当前时间
        ZonedDateTime now = ZonedDateTime.now();
        try {
            GetTransactionByUpdateDateReqDto build = buildPullBetsLipsParams(platform);
            JSONObject result = pullSend(config.getRecordUrl(), AEBaseParam.UrlEnum.GET_TRANSACTION_BY_UPDATE_DATE, JSON.parseObject(JSON.toJSON(build).toString()), platform.getModel());
            dealBetsLipsRecords(result.getJSONArray("transactions"));



            // 更新拉单数据缓存
            configCache.reSetLastUpdateTimeByModel(
                    platform.getModel(),
                    DateNewUtils.utc8Str(
                            now.minusMinutes(1),
                            DateNewUtils.Format.yyyy_MM_dd_T_HH_mm_ss_XXX
                    )
            );
        } catch (BusinessException e) {
            // 状态:0-三方异常
            buildCodeInfo(e.getMessage());
        } catch (Exception e) {
            // 状态:1-数据异常
            log.error(MODEL + CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_1.getMsg() + ":" + e);
            throw e;
        }
    }

    /**
     * 补充注单信息
     *
     * @param dto dto.requestInfo 补单请求参数
     * @author: David
     * @date: 04/05/2020
     */
    @Override
    public void betsRecordsSupplemental(PlatFactoryParams.BetsRecordsSupplementReqDto dto) {
        try {
            // 发送补单请求
            JSONObject result = pullSend(
                    config.getRecordUrl(),
                    AEBaseParam.UrlEnum.GET_TRANSACTION_BY_TX_TIME,
                    JSON.parseObject(dto.getRequestInfo()),
                    AEBaseParam.PLATFORM.KINGMAKER.getModel()
            );
            dealBetsLipsRecords(result.getJSONArray("transactions"));
        } catch (BusinessException e) {
            // 状态:0-三方异常
            buildCodeInfo(e.getMessage());
        } catch (Exception e) {
            // 状态:1-数据异常
            log.error(MODEL + CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_1.getMsg() + ":" + e);
            throw e;
        }
    }

    /**
     * 拉单数据统一处理入库
     *
     * @param arrayData 注单记录
     */
    @Contract(pure = true)
    private void dealBetsLipsRecords(JSONArray arrayData) {
        // 用户表 id - username 映射集
        Map<String, Integer> usernameIdMap = userServiceBase.getUsernameIdMap();

        List<BetslipsAeKingMaker> collect = arrayData.stream().map(o -> {
            JSONObject data = (JSONObject) o;
            BetslipsAeKingMaker betsLips = JSON.toJavaObject(data, BetslipsAeKingMaker.class);
            betsLips.setId(data.getString("platformTxId"));
            betsLips.setAeId(data.getInteger("ID"));

            // 用户名找到就为对应的UID 否则存0
            String username = userServiceBase.filterUsername(betsLips.getUserId());
            Integer uid = usernameIdMap.get(username);
            betsLips.setXbUid(uid);
            betsLips.setXbUsername(username);

            // 状态规则: -1-cancel 0-bet 1-settled 2-void 9-invalid,hide in the report
            switch (data.getInteger(TX_STATUS)) {
                case 1:
                    betsLips.setXbCoin(betsLips.getRealBetAmount());
                    betsLips.setXbValidCoin(betsLips.getTurnover());

                    JSONObject gameInfo = data.getJSONObject("gameInfo");
                    betsLips.setXbProfit(gameInfo.getBigDecimal("profit"));
                    if (betsLips.getXbProfit().compareTo(BigDecimal.ZERO) > 0) {
                        betsLips.setXbStatus(1);
                    } else if (betsLips.getXbProfit().compareTo(BigDecimal.ZERO) < 0) {
                        betsLips.setXbStatus(2);
                    } else {
                        betsLips.setXbStatus(3);
                    }
                    break;
                case 0:
                    betsLips.setXbStatus(5);
                    break;
                case -1:
                case 2:
                case 9:
                    betsLips.setXbStatus(4);
                    break;
                default:
                    break;
            }

            if (StringUtils.isNotBlank(betsLips.getTxTime())) {
                ZonedDateTime utc = ZonedDateTime.parse(betsLips.getTxTime()).withZoneSameInstant(DateNewUtils.getZoneId("UTC", "+8"));
                betsLips.setCreatedAt((int) utc.toEpochSecond());
            }
            betsLips.setUpdatedAt(DateNewUtils.now());
            return betsLips;
        }).filter(o -> o.getXbUid() != 0).collect(Collectors.toList());
        if (!collect.isEmpty()) betslipsAeKingMakerServiceImpl.saveOrUpdateBatch(collect, collect.size());
    }

    /**
     * 拉单异常 再次拉单
     *
     * @param dto dto.requestInfo 拉单请求参数
     */
    @Override
    public void betSlipsExceptionPull(@javax.validation.constraints.NotNull PlatFactoryParams.BetsRecordsSupplementReqDto dto) {
        this.betsRecordsSupplemental(dto);
    }
}
