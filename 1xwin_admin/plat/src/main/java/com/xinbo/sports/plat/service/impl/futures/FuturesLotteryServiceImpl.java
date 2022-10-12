package com.xinbo.sports.plat.service.impl.futures;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xinbo.sports.dao.generator.po.PlatList;
import com.xinbo.sports.plat.io.bo.FuturesLotteryRequestParameter;
import com.xinbo.sports.plat.service.impl.FuturesServiceImpl;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * BWG彩票
 * </p>
 *
 * @author andy
 * @since 2020/9/28
 */
@Service
public class FuturesLotteryServiceImpl extends FuturesServiceImpl {
    @Resource
    private ConfigCache configCache;

    public ResPage<FuturesLotteryRequestParameter.GetOpenPresetListResBody> getOpenPresetList(ReqPage<FuturesLotteryRequestParameter.GetOpenPresetListReqBody> reqBody) {
        ResPage<FuturesLotteryRequestParameter.GetOpenPresetListResBody> resBodyPage = new ResPage<>();
        FuturesLotteryRequestParameter.GetOpenPresetListReqBody reqBodyData = reqBody.getData();
        if (null == reqBodyData) {
            reqBodyData = FuturesLotteryRequestParameter.GetOpenPresetListReqBody.builder().build();
        }
        reqBodyData.setPlatId(getConfig().getPlatId());
        reqBodyData.setCompanyKey(getConfig().getCompanyKey());
        reqBody.setData(reqBodyData);
        String responseBody = send(FuturesLotteryRequestParameter.UrlEnum.GET_OPEN_PRESET_LIST.getMethodName(), JSON.toJSONString(reqBody));
        JSONObject result = JSON.parseObject(responseBody);
        if (!isOk(result)) {
            checkErrorCode(getCode(result));
        }
        Object data = result.get("data");
        if (Optional.ofNullable(data).isEmpty() || !(data instanceof JSONObject)) {
            return resBodyPage;
        }
        JSONObject jsonObject = (JSONObject) data;
        JSONArray array = jsonObject.getJSONArray("list");
        if (Optional.ofNullable(array).isEmpty() || array.isEmpty()) {
            return resBodyPage;
        }
        List<FuturesLotteryRequestParameter.GetOpenPresetListResBody> list = array.toJavaList(FuturesLotteryRequestParameter.GetOpenPresetListResBody.class);
        resBodyPage.setSize(jsonObject.getInteger("size"));
        resBodyPage.setCurrent(jsonObject.getInteger("current"));
        resBodyPage.setPages(jsonObject.getInteger("pages"));
        resBodyPage.setTotal(jsonObject.getInteger("total"));
        resBodyPage.setList(list);
        return resBodyPage;
    }

    public boolean saveOrUpdateOpenPreset(FuturesLotteryRequestParameter.SaveOrUpdateOpenPresetReqBody reqBody) {
        reqBody.setPlatId(getConfig().getPlatId());
        reqBody.setCompanyKey(getConfig().getCompanyKey());

        String responseBody = send(FuturesLotteryRequestParameter.UrlEnum.SAVE_OR_UPDATE_OPEN_PRESET.getMethodName(), JSON.toJSONString(reqBody));
        JSONObject result = JSON.parseObject(responseBody);
        if (!isOk(result)) {
            checkErrorCode(getCode(result));
        }
        return isOk(result);
    }

    public boolean deleteOpenPreset(FuturesLotteryRequestParameter.DeleteOpenPresetReqBody reqBody) {
        reqBody.setPlatId(getConfig().getPlatId());
        reqBody.setCompanyKey(getConfig().getCompanyKey());

        String responseBody = send(FuturesLotteryRequestParameter.UrlEnum.DELETE_OPEN_PRESET.getMethodName(), JSON.toJSONString(reqBody));
        JSONObject result = JSON.parseObject(responseBody);
        if (!isOk(result)) {
            checkErrorCode(getCode(result));
        }
        return isOk(result);
    }

    private FuturesLotteryRequestParameter.Config getConfig() {
        PlatList platListEntity = configCache.getPlatListByName(MODEL);
        config = JSON.parseObject(platListEntity.getConfig()).toJavaObject(FuturesLotteryRequestParameter.Config.class);
        return config;
    }

    /**
     * 预设开奖->导出
     *
     * @param reqBody
     * @return
     */
    public List<FuturesLotteryRequestParameter.ExportOpenPresetListResBody> exportOpenPresetList(FuturesLotteryRequestParameter.ExportOpenPresetListReqBody reqBody) {
        reqBody.setPlatId(getConfig().getPlatId());
        reqBody.setCompanyKey(getConfig().getCompanyKey());
        String responseBody = send(FuturesLotteryRequestParameter.UrlEnum.EXPORT_OPEN_PRESET_LIST.getMethodName(), JSON.toJSONString(reqBody));

        JSONObject result = JSON.parseObject(responseBody);
        if (!isOk(result)) {
            checkErrorCode(getCode(result));
        }
        Object data = result.get("data");
        if (Optional.ofNullable(data).isEmpty() || !(data instanceof JSONArray)) {
            return new ArrayList<>();
        }
        JSONArray array = (JSONArray) data;
        return array.toJavaList(FuturesLotteryRequestParameter.ExportOpenPresetListResBody.class);
    }

    /**
     * 预设开奖->新增批量预设
     *
     * @param reqBody
     * @return
     */
    public boolean saveOrUpdateBatchOpenPreset(FuturesLotteryRequestParameter.SaveOrUpdateBatchOpenPresetReqBody reqBody) {
        reqBody.setPlatId(getConfig().getPlatId());
        reqBody.setCompanyKey(getConfig().getCompanyKey());

        String responseBody = send(FuturesLotteryRequestParameter.UrlEnum.SAVE_OR_UPDATE_BATCH_OPEN_PRESET.getMethodName(), JSON.toJSONString(reqBody));
        JSONObject result = JSON.parseObject(responseBody);
        if (!isOk(result)) {
            checkErrorCode(getCode(result));
        }
        return isOk(result);
    }

    /**
     * 预设开奖->获取当前期号信息
     *
     * @param reqBody
     * @return
     */
    public FuturesLotteryRequestParameter.GetLotteryActionNoResBody getLotteryActionNo(FuturesLotteryRequestParameter.GetLotteryActionNoReqBody reqBody) {
        reqBody.setPlatId(getConfig().getPlatId());
        reqBody.setCompanyKey(getConfig().getCompanyKey());

        String responseBody = send(FuturesLotteryRequestParameter.UrlEnum.GET_LOTTERY_ACTION_NO.getMethodName(), JSON.toJSONString(reqBody));
        JSONObject result = JSON.parseObject(responseBody);
        if (!isOk(result)) {
            checkErrorCode(getCode(result));
        }

        Object data = result.get("data");
        if (Optional.ofNullable(data).isEmpty() || !(data instanceof JSONObject)) {
            return null;
        }
        JSONObject entity = (JSONObject) data;
        return entity.toJavaObject(FuturesLotteryRequestParameter.GetLotteryActionNoResBody.class);
    }

    /**
     * 批量获取会员余额
     *
     * @param reqBody reqBody
     * @return List<FuturesLotteryRequestParameter.GetUserCoinListByUserNameListResBody>
     */
    public List<FuturesLotteryRequestParameter.GetUserCoinListByUserNameListResBody> getUserCoinListByUserNameList(FuturesLotteryRequestParameter.GetUserCoinListByUserNameListReqBody reqBody) {
        reqBody.setPlatId(getConfig().getPlatId());
        reqBody.setCompanyKey(getConfig().getCompanyKey());

        String responseBody = send(FuturesLotteryRequestParameter.UrlEnum.GET_USERLIST.getMethodName(), JSON.toJSONString(reqBody));
        JSONObject result = JSON.parseObject(responseBody);
        if (!isOk(result)) {
            checkErrorCode(getCode(result));
        }

        Object data = result.get("data");
        if (Optional.ofNullable(data).isEmpty() || !(data instanceof JSONObject)) {
            return new ArrayList<>();
        }
        JSONObject jsonObject = (JSONObject) data;
        JSONArray array = jsonObject.getJSONArray("usernameResList");
        if (Optional.ofNullable(array).isEmpty() || array.isEmpty()) {
            return new ArrayList<>();
        }
        return array.toJavaList(FuturesLotteryRequestParameter.GetUserCoinListByUserNameListResBody.class);
    }

    /**
     * 预设开奖分布
     *
     * @param reqBody reqBody
     * @return FuturesLotteryRequestParameter.OpenRateDistributeResDto
     */
    public FuturesLotteryRequestParameter.OpenRateDistributeResDto getOpenRateDistribute(FuturesLotteryRequestParameter.OpenRateDistributeReqDto reqBody) {
        reqBody.setPlatId(getConfig().getPlatId());
        reqBody.setCompanyKey(getConfig().getCompanyKey());
        String responseBody = send(FuturesLotteryRequestParameter.UrlEnum.GET_OPEN_RATE_DISTRIBUTE.getMethodName(), JSON.toJSONString(reqBody));
        JSONObject result = JSON.parseObject(responseBody);
        if (!isOk(result)) {
            checkErrorCode(getCode(result));
        }

        Object data = result.get("data");
        if (Optional.ofNullable(data).isEmpty() || !(data instanceof JSONObject)) {
            return null;
        }
        JSONObject entity = (JSONObject) data;
        return entity.toJavaObject(FuturesLotteryRequestParameter.OpenRateDistributeResDto.class);
    }
}
