package com.xinbo.sports.payment.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xinbo.sports.dao.generator.po.CoinDeposit;
import com.xinbo.sports.dao.generator.po.CoinOnlineWithdrawal;
import com.xinbo.sports.dao.generator.po.CoinWithdrawal;
import com.xinbo.sports.dao.generator.po.UserBank;
import com.xinbo.sports.dao.generator.service.CoinDepositService;
import com.xinbo.sports.dao.generator.service.CoinOnlineWithdrawalService;
import com.xinbo.sports.dao.generator.service.CoinWithdrawalService;
import com.xinbo.sports.dao.generator.service.UserBankService;
import com.xinbo.sports.payment.base.ThirdPayBase;
import com.xinbo.sports.payment.io.HeroPayParams;
import com.xinbo.sports.payment.io.PayParams;
import com.xinbo.sports.payment.io.PayParams.OnlinePayReqDto;
import com.xinbo.sports.service.base.UpdateUserCoinBase;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.DateUtils;
import com.xinbo.sports.utils.HttpUtils;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.RandomUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.alibaba.fastjson.JSON.toJSONString;
import static com.xinbo.sports.payment.io.PayParams.*;
import static com.xinbo.sports.utils.MD5.signForInspiry;

/**
 * @author: David
 * @date: 14/07/2020
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HeroPayUtil implements PayAbstractFactory {
    private final ThirdPayBase thirdPayBase;
    private final CoinDepositService coinDepositServiceImpl;
    private final CoinOnlineWithdrawalService coinOnlineWithdrawalServiceImpl;
    private final UserBankService userBankServiceImpl;
    private final CoinWithdrawalService coinWithdrawalServiceImpl;
    private final UpdateUserCoinBase updateUserCoinBase;
    private static final String RETCODE = "retCode";
    private static final String PAYURL = "payUrl";
    private static final String MCHID = "mchId";
    private static final String AMOUNT = "amount";
    private static final String APPID = "appId";
    private static final String PRODUCTID = "productId";
    private static final String MCHORDERNO = "mchOrderNo";
    private static final String PARAM1 = "param1";
    private static final String STATUS = "status";
    private static final String PAY_SUCC_TIME = "paySuccTime";
    private final ConfigCache configCache;
    private static final String MODEL = "HeroPayUtil";
    @Setter
    HeroPayParams.PayPlat plat;

    /**
     * ????????????
     *
     * @param dto ??????
     * @return
     */
    @Override
    public Map<Object, Object> onlinePay(@NotNull OnlinePayReqDto dto) {
        // ??????????????????ID
        CoinDeposit coinDeposit = coinDepositServiceImpl.getById(dto.getDepositId());
        if (null == coinDeposit) {
            throw new BusinessException(CodeInfo.PAY_PLAT_ORDER_ID_INVALID);
        }

        // ???????????????
        int i = RandomUtils.nextInt(10000, 99999);
        String orderSn = "I" + DateNewUtils.getNowFormatTime(DateNewUtils.Format.yyyyMMddHHmmss) + i;
        boolean update = coinDepositServiceImpl.lambdaUpdate().eq(CoinDeposit::getId, dto.getDepositId()).set(CoinDeposit::getOrderId, orderSn).update();
        if (!update) {
            throw new BusinessException(CodeInfo.PAY_PLAT_UPDATE_ORDER_ID_INVALID);
        }
        //  ???????????? ??????(???)
        BigDecimal amount = coinDeposit.getCoin().multiply(BigDecimal.valueOf(100));
        // ????????????
        SortedMap<String, String> paramsMap = new TreeMap<>();
        paramsMap.put(MCHID, plat.getPrivateKey());
        paramsMap.put(APPID, plat.getBusinessCode());
        Map<String, String> channelMap = Map.of("HeroPay OnlineBank", "8000", "HeroPay Paytm", "8201");
        paramsMap.put(PRODUCTID, channelMap.get(coinDeposit.getTitle()));
        paramsMap.put(MCHORDERNO, orderSn);
        var currency = EnumUtils.getEnumIgnoreCase(HeroPayParams.CURRENCY.class, configCache.getCountry()).getCode();
        paramsMap.put("currency", currency);
        paramsMap.put(AMOUNT, amount.toBigInteger().toString());
        // ????????????????????????
        paramsMap.put("notifyUrl", plat.getNotifyUrl() + PayParams.UrlEnum.DEPOSIT_NOTIFY_URL.getUrl());
        // ????????????????????????
        paramsMap.put("returnUrl", plat.getReturnUrl());
        paramsMap.put("subject", "test");
        paramsMap.put("body", "test");
        String param1 = "test;test;test";
        UserBank uid = userBankServiceImpl.getOne(new QueryWrapper<UserBank>().eq("uid", coinDeposit.getUid()).eq(STATUS, 1));
        if (uid != null) {
            param1 = (uid.getMark() != null ? uid.getMark() + ";" : "test;") + (uid.getAccountName() != null ? uid.getAccountName() + ";" : "test;") + (uid.getBankAccount() != null ? uid.getBankAccount() + ";" : "test");
        }
        paramsMap.put(PARAM1, param1);

        //????????????????????????
        String key = plat.getBusinessPwd();
        String sign = signForInspiry(paramsMap, key);
        paramsMap.put("sign", sign);
        String params = "params" + "=" + toJSONString(paramsMap);

        // ?????????????????????????????????
        String payUrl = plat.getUrl() + HeroPayParams.HeroPayUrlEnum.PAY.getUrl();
        JSONObject data = null;
        String url = null;
        String method = null;
        int category = 0;
        var result = HttpUtils.postHttp(payUrl, params);
        JSONObject jsonObject = JSON.parseObject(result);
        if (jsonObject.containsKey("errCode")) {
            log.info("======HeroPay: Code???????????? : " + jsonObject.toJSONString());
            throw new BusinessException(CodeInfo.PAY_PLAT_LOAD_INVALID);
        } else if (jsonObject.getString(RETCODE).equals("SUCCESS")) {
            data = jsonObject.getJSONObject("payParams");
            if (data.getString(PAYURL).contains("POST")) {
                url = data.getString(PAYURL).split(";")[1];
                category = 3;
                method = "POST";
            } else {
                category = 1;
                url = data.getString(PAYURL);
                method = "GET";
            }
        } else {
            log.info("======HeroPay: ??????????????????\n");
            log.info("======HeroPay: result = " + jsonObject.toJSONString());
        }
        return Map.of("method", method, "url", url, "category", category);
    }

    /**
     * ??????????????????
     *
     * @param dto ??????
     * @return
     */
    @Override
    public WithdrawalNotifyResDto onlineWithdraw(PayParams.OnlinePayoutReqDto dto) {
        // ??????????????????ID
        CoinOnlineWithdrawal coinOnlineWithdrawal = coinOnlineWithdrawalServiceImpl.getOne(new QueryWrapper<CoinOnlineWithdrawal>().eq("order_id", dto.getOrderId()));
        if (null == coinOnlineWithdrawal) {
            throw new BusinessException(CodeInfo.PAY_PLAT_ORDER_ID_INVALID);
        }
        var bankInfo = parseObject(coinOnlineWithdrawal.getBankInfo());
        //  ???????????? ??????(???)
        BigDecimal amount = coinOnlineWithdrawal.getCoin().multiply(BigDecimal.valueOf(100));
        // ????????????
        SortedMap<String, String> paramsMap = new TreeMap<>();
        paramsMap.put(MCHID, plat.getPrivateKey());
        paramsMap.put(APPID, plat.getBusinessCode());
        paramsMap.put("mchTransOrderNo", dto.getOrderId());
        var currency = EnumUtils.getEnumIgnoreCase(HeroPayParams.CURRENCY.class, configCache.getCountry()).getCode();
        paramsMap.put("currency", currency);
        paramsMap.put(AMOUNT, amount.toBigInteger().toString());
        // ????????????????????????
        paramsMap.put("notifyUrl", plat.getNotifyUrl() + PayParams.UrlEnum.WITHDRAW_NOTIFY_URL.getUrl());
        paramsMap.put(BANK_CODE, bankInfo.getString(BANK_CODE));
        paramsMap.put("bankName", bankInfo.getString(OPEN_ACCOUNT_BANK));
        //1-???????????????,2-????????????,3-???????????????,4-????????????
        //??????:1-?????? 2-?????? 3-????????? 4-QQ 5-QR??????
        Map<Integer, String> accountTypes = Map.of(1, "1", 2, "2", 3, "3", 4, "4", 5, "4", 6, "4");
        paramsMap.put("accountType", accountTypes.get(coinOnlineWithdrawal.getCategory()));
        paramsMap.put("accountName", bankInfo.getString(ACCOUNT_HOLDER));
        paramsMap.put("accountNo", bankInfo.getString(BANK_CARD_ACCOUNT));
        paramsMap.put("province", "test");
        paramsMap.put("city", "test");
        //????????????????????????
        String sign = signForInspiry(paramsMap, plat.getBusinessPwd());
        paramsMap.put("sign", sign);
        String params = "params" + "=" + toJSONString(paramsMap);

        // ?????????????????????????????????
        var payUrl = plat.getUrl() + HeroPayParams.HeroPayUrlEnum.TRANS.getUrl();
        var result = HttpUtils.postHttp(payUrl, params);
        JSONObject jsonObject = JSON.parseObject(result);
        if (jsonObject.getString(RETCODE).equals("SUCCESS")) {
            return WithdrawalNotifyResDto.builder().code(true).msg(jsonObject.getString("retMsg")).build();
        } else {
            CodeInfo.PAY_PLAT_ADD_ORDER_FAILURE.setMsg(jsonObject.getString("retMsg"));
            throw new BusinessException(CodeInfo.PAY_PLAT_ADD_ORDER_FAILURE);
        }
    }

    @Override
    public Boolean checkPaymentStatus(String orderId) {
        CoinOnlineWithdrawal one = coinOnlineWithdrawalServiceImpl.lambdaQuery().eq(CoinOnlineWithdrawal::getOrderId, orderId).eq(CoinOnlineWithdrawal::getStatus, 0).one();
        SortedMap<String, String> paramsMap = new TreeMap<>();
        paramsMap.put(MCHID, plat.getPrivateKey());
        paramsMap.put(APPID, plat.getBusinessCode());
        paramsMap.put("mchTransOrderNo", orderId);
        paramsMap.put("executeNotify", "false");
        var sign = signForInspiry(paramsMap, plat.getBusinessPwd());
        paramsMap.put("sign", sign);
        String params = "params" + "=" + toJSONString(paramsMap);
        // ?????????????????????????????????
        var payUrl = plat.getUrl() + HeroPayParams.HeroPayUrlEnum.CHECK_WITHDRAWAl.getUrl();
        var result = HttpUtils.postHttp(payUrl, params);
        JSONObject jsonObject = JSON.parseObject(result);
        if (jsonObject.getString(RETCODE).equals("SUCCESS")) {
            //????????????::0-????????????,1-?????????,2-????????????,3-????????????
            Integer status = jsonObject.getJSONObject("data").getInteger("status");
            if (status == 3) {
                coinOnlineWithdrawalServiceImpl.lambdaUpdate().set(CoinOnlineWithdrawal::getStatus, 1).eq(CoinOnlineWithdrawal::getOrderId, orderId).update();
                coinWithdrawalServiceImpl.lambdaUpdate().set(CoinWithdrawal::getStatus, 1).eq(CoinWithdrawal::getId, one.getWithdrawalOrderId()).update();
                //'??????:0-????????? 1-?????? 2-??????'
                updateUserCoinBase.updateCoinLog(Long.valueOf(one.getWithdrawalOrderId()), 2, 1, DateUtils.getCurrentTime());
            } else if (status == 4) {
                coinOnlineWithdrawalServiceImpl.lambdaUpdate().set(CoinOnlineWithdrawal::getStatus, 2).eq(CoinOnlineWithdrawal::getOrderId, orderId).update();
            }
            return true;
        } else {
            CodeInfo.PAY_PLAT_ADD_ORDER_FAILURE.setMsg(jsonObject.getString("retMsg"));
            throw new BusinessException(CodeInfo.PAY_PLAT_ADD_ORDER_FAILURE);
        }
    }


    /**
     * ??????????????????
     *
     * @param dto ??????
     * @return success-?????? fail-??????
     */
    @SneakyThrows
    public String notifyUrl(@NotNull Map<String, String> dto) {
        // ?????????????????????
        log.info(String.format("=====HeroPay NotifyUrl Record : %s", dto.toString()));
        var path = Paths.get("HeroPay-" + DateUtils.yyyyMMdd(DateNewUtils.now()) + ".txt");
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
        if (null == plat) PayAbstractFactory.init(MODEL);
        Files.write(path, dto.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        SortedMap<String, String> paramsMap = new TreeMap<>();
        paramsMap.put(MCHID, dto.get(MCHID));
        paramsMap.put("payOrderId", dto.get("payOrderId"));
        paramsMap.put(AMOUNT, dto.get(AMOUNT));
        paramsMap.put(APPID, plat.getBusinessCode());
        paramsMap.put(PRODUCTID, dto.get(PRODUCTID));
        paramsMap.put(MCHORDERNO, dto.get(MCHORDERNO));
        paramsMap.put(PARAM1, dto.get(PARAM1));
        paramsMap.put("backType", dto.get("backType"));
        paramsMap.put(STATUS, dto.get(STATUS));
        if (dto.get(PAY_SUCC_TIME) != null) {
            paramsMap.put(PAY_SUCC_TIME, dto.get(PAY_SUCC_TIME));
        }
        // ?????????????????????????????????
        String sign = signForInspiry(paramsMap, this.plat.getBusinessPwd());
        // ??????????????????????????????
        if (!sign.equalsIgnoreCase(dto.get("sign")) || !dto.get(STATUS).equals("2")) {
            log.info("=====HeroPay NotifyUrl : ?????? ??? status?????? ");
            Files.write(path, PayParams.STATUS.FAIL.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            return PayParams.STATUS.FAIL.getCode();
        }
        // ??????OrderId ????????????
        CoinDeposit coinDeposit = coinDepositServiceImpl.lambdaQuery()
                .eq(CoinDeposit::getOrderId, dto.get(MCHORDERNO))
                .orderByDesc(CoinDeposit::getId)
                .one();
        if (null == coinDeposit) {
            log.info("=====HeroPay NotifyUrl : ??????????????????");
            Files.write(path, PayParams.STATUS.FAIL.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            return PayParams.STATUS.FAIL.getCode();
        } else if (coinDeposit.getStatus() != 0) {
            Files.write(path, PayParams.STATUS.FAIL.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            log.info("=====HeroPay NotifyUrl : ????????????");
            return PayParams.STATUS.FAIL.getCode();
        }

        try {
            // ????????????????????????
            thirdPayBase.updateCoinDeposit(coinDeposit.getId(), new BigDecimal(dto.get(AMOUNT)).divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP));
        } catch (Exception e) {
            Files.write(path, PayParams.STATUS.FAIL.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            log.info("=====HeroPay ?????????????????? :" + e.toString());
        }
        Files.write(path, PayParams.STATUS.SUCCESS.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        return PayParams.STATUS.SUCCESS.getCode();
    }

    /**
     * ????????????????????????
     *
     * @param dto ??????
     * @return success-?????? fail-??????
     */
    @SneakyThrows
    public String withdrawNotifyUrl(Map<String, String> dto) {
        // ?????????????????????
        log.info(String.format("=====" + MODEL + " NotifyUrl Record : %s", dto.toString()));
        var path = Paths.get("HeroPay-" + DateUtils.yyyyMMdd(DateNewUtils.now()) + ".txt");
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
        // ?????????????????????????????????
        String sign = signForInspiry(new TreeMap<>(dto), this.plat.getBusinessPwd());
        // ??????????????????????????????
        if (!sign.equalsIgnoreCase(dto.get("sign")) || !dto.get(STATUS).equals("2")) {
            log.info("=====HeroPay NotifyUrl : ?????? ??? status?????? ");
            Files.write(path, PayParams.STATUS.FAIL.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            return PayParams.STATUS.FAIL.getCode();
        }
        // ??????OrderId ????????????
        CoinOnlineWithdrawal coinOnlineWithdrawal = coinOnlineWithdrawalServiceImpl.lambdaQuery()
                .eq(CoinOnlineWithdrawal::getOrderId, dto.get("mchTransOrderNo"))
                .orderByDesc(CoinOnlineWithdrawal::getId)
                .one();
        if (null == coinOnlineWithdrawal) {
            log.info("=====" + MODEL + " NotifyUrl : ??????????????????");
            return PayParams.STATUS.FAIL.getCode();
        } else if (coinOnlineWithdrawal.getStatus() != 0) {
            log.info("=====" + MODEL + " NotifyUrl : ????????????");
            return PayParams.STATUS.FAIL.getCode();
        }

        try {
            if ("2".equals(dto.get(STATUS))) {
                coinOnlineWithdrawalServiceImpl.lambdaUpdate().set(CoinOnlineWithdrawal::getStatus, 1).eq(CoinOnlineWithdrawal::getOrderId, coinOnlineWithdrawal.getOrderId()).update();
                coinWithdrawalServiceImpl.lambdaUpdate().set(CoinWithdrawal::getStatus, 1).eq(CoinWithdrawal::getId, coinOnlineWithdrawal.getWithdrawalOrderId()).update();
                updateUserCoinBase.updateCoinLog(Long.valueOf(coinOnlineWithdrawal.getWithdrawalOrderId()), 2, 1, DateUtils.getCurrentTime());
                log.info("????????????????????????!");
                return PayParams.STATUS.SUCCESS.getCode();
            } else if ("3".equals(dto.get(STATUS))) {
                coinOnlineWithdrawalServiceImpl.lambdaUpdate().set(CoinOnlineWithdrawal::getStatus, 2).eq(CoinOnlineWithdrawal::getOrderId, coinOnlineWithdrawal.getOrderId()).update();
                return PayParams.STATUS.SUCCESS.getCode();
            }
        } catch (Exception e) {
            log.info("=====" + MODEL + " ?????????????????? :" + e.toString());
            return PayParams.STATUS.FAIL.getCode();
        }
        return PayParams.STATUS.FAIL.getCode();
    }

}
