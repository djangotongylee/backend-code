package com.xinbo.sports.plat.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinbo.sports.dao.generator.po.BetSlipsSupplemental;
import com.xinbo.sports.dao.generator.po.BetslipsSlotto;
import com.xinbo.sports.dao.generator.service.BetslipsSlottoService;
import com.xinbo.sports.service.base.CoinPlatTransfersBase;
import com.xinbo.sports.plat.base.CommonPersistence;
import com.xinbo.sports.plat.factory.PlatAbstractFactory;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams;
import com.xinbo.sports.plat.io.bo.SLottoRequestParameter;
import com.xinbo.sports.plat.io.enums.BasePlatParam;
import com.xinbo.sports.plat.io.enums.SLottoPlatEnum;
import com.xinbo.sports.service.base.ExceptionThreadLocal;
import com.xinbo.sports.service.base.UserServiceBase;
import com.xinbo.sports.service.cache.KeyConstant;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.service.common.RedisConstant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.utils.BeanConvertUtils;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.DateUtils;
import com.xinbo.sports.utils.JedisUtil;
import com.xinbo.sports.utils.components.response.CodeInfo;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.*;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.alibaba.fastjson.JSON.toJSONString;
import static com.xinbo.sports.utils.components.response.CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0;

/**
 * @author suki
 */
@Slf4j
@Service("SLottoServiceImpl")
public class SLottoServiceImpl implements PlatAbstractFactory {
    /*三方名称*/
    private static final String MODEL = "SLotto";
    private static final String ERRORCODE = "ErrorCode";
    RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private JedisUtil jedisUtil;
    @Resource
    protected ConfigCache configCache;
    @Setter
    SLottoPlatEnum.PlatConfig config;
    @Resource
    private UserServiceBase userServiceBase;
    @Resource
    private CoinPlatTransfersBase coinPlatTransfersBase;
    @Autowired
    private BetslipsSlottoService betslipsSlottoServiceImpl;
    @Resource
    private CommonPersistence commonPersistence;
    SimpleClientHttpRequestFactory clientHttpRequestFactory = new SimpleClientHttpRequestFactory();


    /**
     * 获取游戏登录链接
     *
     * @param platLoginReqDto {username, lang, device, slotId}
     * @return 登录链接
     */
    @Override
    public PlatFactoryParams.PlatGameLoginResDto login(PlatFactoryParams.PlatLoginReqDto platLoginReqDto) {
        String key = betLogin(platLoginReqDto);
        String call = config.getApiUrl() + SLottoPlatEnum.SLActionEnum.LOBBY.getMethodName() + "?user=" + platLoginReqDto.getUsername() + "&sessionID=" + parseObject(key).getJSONObject("Data").getString("SessionID") + "&tokenCode=" + parseObject(key).getJSONObject("Data").getString("TokenCode");
        return PlatFactoryParams.PlatGameLoginResDto.builder().type(1).url(call).build();
    }

    private String betLogin(PlatFactoryParams.PlatLoginReqDto platLoginReqDto) {
        var betLoginBO = SLottoRequestParameter.BetLogin.builder()
                .apiuser(config.getApiuser())
                .apipass(config.getApipass())
                .user(platLoginReqDto.getUsername())
                .pass(platLoginReqDto.getUsername() + "S1")
                .build();
        String call = sendGet(SLottoPlatEnum.SLActionEnum.BETLOGIN.getMethodNameDesc(),
                config.getApiUrl() + SLottoPlatEnum.SLActionEnum.BETLOGIN.getMethodName(), betLoginBO);
        Integer errorCode = parseObject(call).getInteger(ERRORCODE);
        if (errorCode != 0) {
            if (errorCode == 2 && Boolean.TRUE.equals(registerUser(PlatFactoryParams.PlatRegisterReqDto.builder().username(platLoginReqDto.getUsername()).build()))) {
                return betLogin(platLoginReqDto);
            }
            throw new BusinessException(SLottoPlatEnum.MAP.getOrDefault(errorCode, CodeInfo.PLAT_SYSTEM_ERROR));
        }
        return call;
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
        return null;
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
        var depositBO = SLottoRequestParameter.Deposit.builder()
                .apiuser(config.getApiuser())
                .apipass(config.getApipass())
                .user(config.getUser())
                .pass(config.getPass())
                .loginID(platCoinTransferUpReqDto.getUsername())
                .amount(platCoinTransferUpReqDto.getCoin())
                .remarks(platCoinTransferUpReqDto.getOrderId())
                .build();
        // 测试环境上方金额不能大于100
        if (!"PROD".equals(config.getEnvironment()) && platCoinTransferUpReqDto.getCoin().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BusinessException(CodeInfo.PLAT_COIN_UP_COIN_NOT_GT_100);
        }
        JSONObject call = parseObject(sendGet(SLottoPlatEnum.SLActionEnum.DEPOSIT.getMethodNameDesc(),
                config.getApiUrl() + SLottoPlatEnum.SLActionEnum.DEPOSIT.getMethodName(), depositBO));
        if (call.getInteger(ERRORCODE).equals(0)) {
            BigDecimal afterAmount = queryBalance(PlatFactoryParams.PlatQueryBalanceReqDto.builder().username(platCoinTransferUpReqDto.getUsername()).build()).getPlatCoin();
            coinPlatTransfersBase.updateOrderPlat(platCoinTransferUpReqDto.getOrderId(), 1, platCoinTransferUpReqDto.getCoin(), null, null);
            return PlatFactoryParams.PlatCoinTransferResDto.builder().platCoin(afterAmount).build();
        } else {
            coinPlatTransfersBase.updateOrderPlat(platCoinTransferUpReqDto.getOrderId(), 2, platCoinTransferUpReqDto.getCoin(), "", call.getInteger(ERRORCODE).toString());
            throw new BusinessException(SLottoPlatEnum.MAP.getOrDefault(call.getInteger(ERRORCODE), CodeInfo.PLAT_SYSTEM_ERROR));
        }

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
        BigDecimal afterAmount = BigDecimal.ZERO;
        var withdrawBO = SLottoRequestParameter.Deposit.builder()
                .apiuser(config.getApiuser())
                .apipass(config.getApipass())
                .user(config.getUser())
                .pass(config.getPass())
                .loginID(platCoinTransferDownReqDto.getUsername())
                .amount(platCoinTransferDownReqDto.getCoin())
                .remarks(platCoinTransferDownReqDto.getOrderId())
                .build();
        JSONObject call = parseObject(sendGet(SLottoPlatEnum.SLActionEnum.WITHDRAW.getMethodNameDesc(),
                config.getApiUrl() + SLottoPlatEnum.SLActionEnum.WITHDRAW.getMethodName(), withdrawBO));
        if (call.getInteger(ERRORCODE).equals(0)) {
            afterAmount = queryBalance(PlatFactoryParams.PlatQueryBalanceReqDto.builder().username(platCoinTransferDownReqDto.getUsername()).build()).getPlatCoin();
        }
        coinPlatTransfersBase.successUpdateTransferOrder(platCoinTransferDownReqDto.getOrderId(), platCoinTransferDownReqDto.getCoin(), "");
        return PlatFactoryParams.PlatCoinTransferResDto.builder().platCoin(afterAmount).build();
    }

    /**
     * 查询三方余额
     *
     * @param reqDto {"username"}
     * @return {platCoin}
     * @author: David
     * @date: 04/05/2020
     */
    @Override
    public PlatFactoryParams.PlatQueryBalanceResDto queryBalance(PlatFactoryParams.PlatQueryBalanceReqDto reqDto) {
        var balanceBO = SLottoRequestParameter.QueryBalance.builder()
                .apiuser(config.getApiuser())
                .apipass(config.getApipass())
                .user(config.getUser())
                .pass(config.getPass())
                .loginID(reqDto.getUsername())
                .build();
        JSONObject call = parseObject(sendGet(SLottoPlatEnum.SLActionEnum.QUERYBALANCE.getMethodNameDesc(),
                config.getApiUrl() + SLottoPlatEnum.SLActionEnum.QUERYBALANCE.getMethodName(), balanceBO));
        if (call.getInteger(ERRORCODE).equals(0)) {
            var balance = call.getJSONObject("Data").getBigDecimal("Balance");
            return PlatFactoryParams.PlatQueryBalanceResDto.builder().platCoin(balance).build();
        } else {
            throw new BusinessException(SLottoPlatEnum.MAP.getOrDefault(call.getInteger(ERRORCODE), CodeInfo.PLAT_SYSTEM_ERROR));
        }
    }

    /**
     * 拉取三方注单信息
     *
     * @author: David
     * @date: 04/05/2020
     */
    @Override
    public void pullBetsLips() {
        Integer currentPage = 1;
        String dateTo = null;
        String pullEndDay = jedisUtil.hget(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH, RedisConstant.SLOTTO);
        if (null == pullEndDay) {
            BetslipsSlotto betslipsSlotto = betslipsSlottoServiceImpl.getOne(new LambdaQueryWrapper<BetslipsSlotto>()
                    .orderByDesc(BetslipsSlotto::getCreatedAt).last("limit 1"));
            pullEndDay = (betslipsSlotto != null && betslipsSlotto.getCreatedAt() != null) ? DateUtils.yyyyMMdd(betslipsSlotto.getCreatedAt()) : DateUtils.yyyyMMdd(DateUtils.getCurrentTime() - 24 * 60 * 60);
        }
        currentPage = pullEndDay.contains("_") ? Integer.valueOf(pullEndDay.split("_")[1]) : currentPage;
        pullEndDay = pullEndDay.contains("_") ? pullEndDay.split("_")[0] : pullEndDay;
        dateTo = (pullEndDay.equals(DateUtils.yyyyMMdd(DateUtils.getCurrentTime()))) ? pullEndDay : addDay(pullEndDay);
        var pullSettledBetBO = SLottoRequestParameter.BetDetailslist.builder()
                .apiuser(config.getApiuser())
                .apipass(config.getApipass())
                .user(config.getUser())
                .pass(config.getPass())
                .dateFrom(pullEndDay)
                .dateTo(dateTo)
                .currentPage(currentPage.toString())
                .build();

        JSONObject call = parseObject(sendGet(SLottoPlatEnum.SLActionEnum.BETDETAILSLIST.getMethodNameDesc(),
                config.getApiUrl() + SLottoPlatEnum.SLActionEnum.BETDETAILSLIST.getMethodName(), pullSettledBetBO));

        if (call.getInteger(ERRORCODE).equals(0)) {
            JSONArray data = call.getJSONObject("Data").getJSONArray("Item");
            XxlJobLogger.log(data.toJSONString() + "SLotto拉取注单:" + pullEndDay);
            insertSLottoData(data);
            pullEndDay = call.getJSONObject("Data").getInteger("TotalPage") > currentPage ? (dateTo + "_" + (currentPage + 1)) : dateTo;

            jedisUtil.hset(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH, RedisConstant.SLOTTO, pullEndDay);
        } else if (call.getInteger(ERRORCODE).equals(11)) {
            XxlJobLogger.log("SLotto拉取注单: Data not found 当前日期无记录");
            jedisUtil.hset(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH, RedisConstant.SLOTTO, dateTo);
        } else {
            XxlJobLogger.log("SLotto拉取注单!" + call.toJSONString());
            PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0.setMsg(call.toJSONString());
            throw new BusinessException(PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0);
        }
    }

    /**
     * 数据入库
     *
     * @param data
     */
    private void insertSLottoData(JSONArray data) {
        if (!data.isEmpty()) {
            List<BetslipsSlotto> betslipsSlottoList = new ArrayList<>();
            Map<String, Integer> userMap = userServiceBase.getUsernameIdMap();
            for (var x : data) {
                var betDetailBo = parseObject(toJSONString(x), SLottoRequestParameter.BetDetail.class);
                var userName = userServiceBase.filterUsername(betDetailBo.getLoginName());
                var uid = userMap.get(userName);
                if (StringUtils.isEmpty(userName) || uid == null) {
                    continue;
                }
                var retBetslipsSlotto = BeanConvertUtils.copyProperties(betDetailBo, BetslipsSlotto::new, (bo, sb) -> {
                    sb.setXbUid(uid);
                    sb.setXbUsername(userName);
                    sb.setXbCoin(bo.getConfirmed());
                    sb.setXbValidCoin(Arrays.asList("open", "void").stream().anyMatch(s -> s.equals(bo.getStatus())) ? BigDecimal.ZERO : bo.getConfirmed());
                    sb.setXbProfit(Arrays.asList("open", "void").stream().anyMatch(s -> s.equals(bo.getStatus())) ? BigDecimal.ZERO : bo.getStrikePL().subtract(bo.getConfirmed()).add(bo.getCommPL()));
                    sb.setXbStatus(transferStatus(bo.getStatus(), bo.getStrikePL()));
                    sb.setCreatedAt((int) (bo.getDateBet().getTime() / 1000));
                    sb.setUpdatedAt(DateUtils.getCurrentTime());
                });
                betslipsSlottoList.add(retBetslipsSlotto);
            }
            betslipsSlottoServiceImpl.saveOrUpdateBatch(betslipsSlottoList, 1000);
        }
    }

    /**
     * 检查转账状态
     *
     * @author: David
     * @date: 04/05/2020
     */
    @Override
    public Boolean checkTransferStatus(String orderId) {
        try {
            var checkTransactionBO = SLottoRequestParameter.CheckTransaction.builder()
                    .apiuser(config.getApiuser())
                    .apipass(config.getApipass())
                    .user(config.getUser())
                    .pass(config.getPass())
                    .transactionId(orderId)
                    .build();
            JSONObject call = parseObject(sendGet(SLottoPlatEnum.SLActionEnum.CHECKTRANSACTION.getMethodNameDesc(),
                    config.getApiUrl() + SLottoPlatEnum.SLActionEnum.CHECKTRANSACTION.getMethodName(), checkTransactionBO));
            if (call.getInteger(ERRORCODE).equals(0)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            throw new BusinessException(CodeInfo.PLAT_SYSTEM_ERROR);

        }

    }


    /**
     * 创建会员
     * dateFrom
     *
     * @param reqDto 创建会员信息
     * @return true-成功 false-失败
     */
    @Override
    public Boolean registerUser(PlatFactoryParams.PlatRegisterReqDto reqDto) {
        var registerBO = SLottoRequestParameter.Register.builder()
                .apiuser(config.getApiuser())
                .apipass(config.getApipass())
                .user(config.getUser())
                .pass(config.getPass())
                .loginID(reqDto.getUsername())
                .loginPass(reqDto.getUsername() + "S1")
                .fullName(reqDto.getUsername())
                .build();
        JSONObject call = parseObject(sendGet(SLottoPlatEnum.SLActionEnum.CREATE.getMethodNameDesc(),
                config.getApiUrl() + SLottoPlatEnum.SLActionEnum.CREATE.getMethodName(), registerBO));
        if (call.getInteger(ERRORCODE).equals(0)) {
            return true;
        } else {
            throw new BusinessException(SLottoPlatEnum.MAP.getOrDefault(call.getInteger(ERRORCODE), CodeInfo.PLAT_SYSTEM_ERROR));
        }
    }

    /**
     * 发送方法
     *
     * @param method
     * @param url
     * @param object
     * @return
     */
    private String sendGet(String method, String url, Object object) {
        try {
            clientHttpRequestFactory.setConnectTimeout(1000 * 3 * 60);
            restTemplate.setRequestFactory(clientHttpRequestFactory);
            JSONObject call = parseObject(toJSONString(object));
            //异常处理
            ExceptionThreadLocal.setRequestParams(toJSONString(object));
            Map<String, Object> itemMap = JSON.toJavaObject(call, Map.class);
            log.info("Slotto请求参数：" + method + object);
            String result = restTemplate.getForEntity(url, String.class, itemMap).getBody();
            log.info("Slotto三方返回内容" + result);
            return result;
        } catch (Exception e) {
            if (method.contains("betdetailslist")) {
                CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0.setMsg(e.getMessage());
                throw new BusinessException(CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0);
            } else {
                throw e;
            }
        }
    }

    /**
     * 天数增加
     *
     * @param s
     * @return
     */
    public static String addDay(String s) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cd = Calendar.getInstance();
            cd.setTime(sdf.parse(s));
            cd.add(Calendar.DATE, 1);//增加一天
            if (String.valueOf(cd.getActualMaximum(Calendar.DATE)).equals(sdf.format(cd.getTime()).substring(6, 8))) {
                cd.add(Calendar.MONTH, 1);//增加一个月
            }
            return sdf.format(cd.getTime());

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 状态码转换
     *
     * @param ticketStatus
     * @param strikePL
     * @return
     */
    public Integer transferStatus(String ticketStatus, BigDecimal strikePL) {
        //状态码
        Integer status;
        if (ticketStatus.equals("open")) {//(1:WIN | 2:LOSE | 3:CANCELLED )
            status = BasePlatParam.BetRecordsStatus.WAIT_SETTLE.getCode();
        } else if (ticketStatus.equals("void")) {
            status = BasePlatParam.BetRecordsStatus.CANCEL.getCode();
        } else {
            if (strikePL.compareTo(BigDecimal.ZERO) == 0) {
                status = BasePlatParam.BetRecordsStatus.LOSE.getCode();
            } else {
                status = BasePlatParam.BetRecordsStatus.WIN.getCode();
            }
        }
        return status;
    }

    /**
     * 根据起始、结束时间 生成补单信息
     * 支持一个月内的补单
     *
     * @param dto dto.start 开始时间 dto.end结束时间
     */
    @Override
    public void genSupplementsOrders(PlatFactoryParams.GenSupplementsOrdersReqDto dto) {
        if (ZonedDateTime.now().minusDays(30).toInstant().getEpochSecond() > dto.getStart()) {
            throw new BusinessException(CodeInfo.PLAT_BET_SLIPS_SUPPLE_OVER_30_DAYS);
        } else {
            LinkedList<BetSlipsSupplemental> list = new LinkedList<>();
            ZonedDateTime now = ZonedDateTime.now();
            var pullSettledBetBO = SLottoRequestParameter.BetDetailslist.builder()
                    .apiuser(config.getApiuser())
                    .apipass(config.getApipass())
                    .user(config.getUser())
                    .pass(config.getPass())
                    .dateFrom(DateNewUtils.utc8Str(DateNewUtils.utc8Zoned(dto.getStart()), DateNewUtils.Format.yyyy_MM_dd))
                    .dateTo(DateNewUtils.utc8Str(DateNewUtils.utc8Zoned(dto.getEnd()), DateNewUtils.Format.yyyy_MM_dd))
                    .currentPage("%s")
                    .build();
            Integer currentTime = (int) now.toInstant().getEpochSecond();
            BetSlipsSupplemental po = PlatAbstractFactory
                    .buildBetSlipsSupplemental(dto.getGameId(), DateNewUtils.utc8Zoned(dto.getStart()).toString(), DateNewUtils.utc8Zoned(dto.getEnd()).toString(), toJSONString(pullSettledBetBO), currentTime);
            list.add(po);
            if (!list.isEmpty()) {
                commonPersistence.addBatchBetSlipsSupplementalList(list);
            }
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
        int currentPage = 1;
        JSONObject call = pullHistoryData(dto.getRequestInfo(), currentPage);
        if (call.getInteger(ERRORCODE).equals(0)) {
            if (call.getJSONObject("Data").getInteger("TotalPage") > currentPage) {
                for (int i = 2; i <= call.getJSONObject("Data").getInteger("TotalPage"); i++) {
                    pullHistoryData(dto.getRequestInfo(), i);
                }
            }
        } else if (call.getInteger(ERRORCODE).equals(11)) {
            XxlJobLogger.log("SLotto拉取注单: Data not found 当前日期无记录");
        } else {
            throw new BusinessException(SLottoPlatEnum.MAP.getOrDefault(call.getInteger(ERRORCODE), CodeInfo.PLAT_SYSTEM_ERROR));
        }
    }

    /**
     * hui qu
     *
     * @param object
     * @param currentPage
     * @return
     */
    private JSONObject pullHistoryData(String object, int currentPage) {
        Object pullSettledBetBO = parseObject(String.format(object, currentPage));
        JSONObject call = parseObject(sendGet(SLottoPlatEnum.SLActionEnum.BETDETAILSLIST.getMethodNameDesc(),
                config.getApiUrl() + SLottoPlatEnum.SLActionEnum.BETDETAILSLIST.getMethodName(), pullSettledBetBO));
        if (call.getInteger(ERRORCODE).equals(0)) {
            JSONArray data = call.getJSONObject("Data").getJSONArray("Item");
            XxlJobLogger.log(data.toJSONString() + "SLotto补单:" + object);
            insertSLottoData(data);
            return call;
        } else {
            XxlJobLogger.log("SLotto补单!" + call.toJSONString());
            PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0.setMsg(call.toJSONString());
            throw new BusinessException(PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0);
        }
    }

    /**
     * 拉单异常 再次拉单
     *
     * @param dto dto.requestInfo 拉单请求参数
     * @author: David
     * @date: 04/05/2020
     */
    @Override
    public void betSlipsExceptionPull(PlatFactoryParams.BetsRecordsSupplementReqDto dto) {
        this.betsRecordsSupplemental(dto);
    }
}
