package com.xinbo.sports.payment.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xinbo.sports.dao.generator.po.CoinDeposit;
import com.xinbo.sports.dao.generator.po.CoinOnlineWithdrawal;
import com.xinbo.sports.dao.generator.po.CoinWithdrawal;
import com.xinbo.sports.dao.generator.service.CoinDepositService;
import com.xinbo.sports.dao.generator.service.CoinOnlineWithdrawalService;
import com.xinbo.sports.dao.generator.service.CoinWithdrawalService;
import com.xinbo.sports.payment.base.ThirdPayBase;
import com.xinbo.sports.payment.io.MangoPayParams;
import com.xinbo.sports.payment.io.PayParams;
import com.xinbo.sports.payment.io.PayParams.OnlinePayReqDto;
import com.xinbo.sports.service.base.UpdateUserCoinBase;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.dto.BaseParams;
import com.xinbo.sports.service.thread.ThreadHeaderLocalData;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.DateUtils;
import com.xinbo.sports.utils.HttpUtils;
import com.xinbo.sports.utils.components.response.CodeInfo;
import com.xxl.job.core.log.XxlJobLogger;
import io.jsonwebtoken.lang.Collections;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.RandomUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.xinbo.sports.payment.io.MangoPayParams.MangoPayUrlEnum.*;
import static com.xinbo.sports.payment.io.PayParams.*;

/**
 * @author: David
 * @date: 14/07/2020
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MangoPayUtil implements PayAbstractFactory {
    private final ThirdPayBase thirdPayBase;
    private final CoinDepositService coinDepositServiceImpl;
    private final CoinOnlineWithdrawalService coinOnlineWithdrawalServiceImpl;
    private final CoinWithdrawalService coinWithdrawalServiceImpl;
    private final ConfigCache configCache;
    private static final String TYPE = "type";
    private static final String URL = "url";
    private static final String MODEL = "MangoPayUtil";
    private static final String MERCHANT_ID = "merchantId";
    private static final String ORDER_SN = "orderSn";
    private static final String CURRENCY = "currency";
    private static final String AMOUNT = "amount";
    private static final String ACCEPT_LANGUAGE = "Accept-Language";
    private static final String USER_NAME = "userName";
    private static final String CARD_NO = "cardNo";
    private final UpdateUserCoinBase updateUserCoinBase;

    @Setter
    MangoPayParams.PayPlat plat;


    /**
     * ????????????
     *
     * @param dto ??????
     * @return ??????
     */
    @Override
    public Map<Object, Object> onlinePay(@NotNull OnlinePayReqDto dto) {
        Integer category = 0;
        String method = null;
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
        HashMap<String, String> map = new HashMap<>(16);
        map.put(MERCHANT_ID, plat.getBusinessCode());
        map.put(ORDER_SN, orderSn);
        Map<String, String> bankMap = Map.of(TYPE, "1", URL, GATEWAY.getUrl());
        Map<String, String> scanMap = Map.of(TYPE, "2", URL, SCANPAY.getUrl());
        Map<Integer, Map<String, String>> typeMap = Map.of(1, bankMap, 5, scanMap);
        map.put(TYPE, typeMap.get(coinDeposit.getCategory()).get(TYPE));
        //?????????????????????*???-?????????3??????????????????????????????[3:????????????,1:????????????,2:???????????????,4:????????????,5:?????????,6:???????????????,7:??????h5,8:?????????,9;?????????wap,19:????????????]
        map.put("scanType", "19");

        var currency = EnumUtils.getEnumIgnoreCase(MangoPayParams.CURRENCY.class, configCache.getCountry()).getCode();
        map.put(CURRENCY, currency);
        map.put(AMOUNT, amount.toString());
        // ????????????????????????
        map.put("notifyUrl", plat.getNotifyUrl() + PayParams.UrlEnum.DEPOSIT_NOTIFY_URL.getUrl());
        // ????????????????????????
        map.put("returnUrl", plat.getReturnUrl());
        map.put("goodsName", "IPHONE");
        map.put("goodsDetail", "GOOD");
        map.put("extra", "god");

        //?????????????????? ?????? md5(merchantId+ORDER_SN+currency+amount+notifyUrl+type+key)-??????key ???????????????
        String sign = map.get(MERCHANT_ID)
                + map.get(ORDER_SN)
                + map.get(CURRENCY)
                + map.get(AMOUNT)
                + map.get("notifyUrl")
                + map.get("type")
                + plat.getBusinessPwd();
        map.put("sign", DigestUtils.md5DigestAsHex(sign.getBytes()));

        //2???header????????????
        BaseParams.HeaderInfo headerInfo = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        HashMap<String, String> header = new HashMap<>(16);
        header.put(ACCEPT_LANGUAGE, EnumUtils.getEnumIgnoreCase(MangoPayParams.LANGS.class, headerInfo.getLang()).getCode());
        // ?????????????????????????????????
        String payUrl = plat.getUrl() + typeMap.get(coinDeposit.getCategory()).get(URL);
        String result = null;
        String url = null;
        try {
            result = HttpUtils.send(payUrl, map, header);
            JSONObject jsonObject = JSON.parseObject(result);
            if (jsonObject.getInteger("code") != 200) {
                log.info("======MangoPay: Code???????????? : " + jsonObject.toJSONString());
                throw new BusinessException(CodeInfo.PAY_PLAT_LOAD_INVALID);
            } else {
                JSONObject data = jsonObject.getJSONObject("data");
                Integer type = data.getInteger("type");
                url = data.getString("gopayUrl");
                if (type == 1) {
                    category = 1;
                    method = "GET";
                } else if (type == 2) {
                    url = data.getString("qrCode");
                    // ??????qrCode
                    category = 2;
                    method = "QRCODE";
                } else {
                    log.info("======MangoPay: ??????????????????\n");
                    log.info("======MangoPay: result = " + jsonObject.toJSONString());
                }
            }

        } catch (Exception e) {
            log.error(e.toString());
            log.error("===========MangoPayUtil URL: " + payUrl + "\n");
            log.error("===========MangoPayUtil postData: " + JSON.toJSONString(map) + "\n");
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

        //  ???????????? ??????(???)
        BigDecimal amount = coinOnlineWithdrawal.getCoin().multiply(BigDecimal.valueOf(100));
        // ????????????
        HashMap<String, String> paramsMap = new HashMap<>(16);
        DecimalFormat df = new DecimalFormat("0.00");
        var bankInfo = parseObject(coinOnlineWithdrawal.getBankInfo());
        var currency = EnumUtils.getEnumIgnoreCase(MangoPayParams.CURRENCY.class, configCache.getCountry()).getCode();
        paramsMap.put(MERCHANT_ID, plat.getBusinessCode());
        paramsMap.put(ORDER_SN, dto.getOrderId());
        paramsMap.put(CURRENCY, currency);
        paramsMap.put(AMOUNT, df.format(amount));
        Map<Integer, String> typeMap = Map.of(1, "bank", 3, "alipay");
        paramsMap.put(TYPE, typeMap.get(coinOnlineWithdrawal.getCategory()));
        paramsMap.put(USER_NAME, bankInfo.getString(ACCOUNT_HOLDER));
        paramsMap.put(CARD_NO, bankInfo.getString(BANK_CARD_ACCOUNT));
        paramsMap.put(BANK_CODE, dto.getBankCode());
        paramsMap.put("bankName", dto.getOpenAccountBank());
        paramsMap.put("bankBranch", dto.getOpenAccountBank());
        paramsMap.put("branchAddress", "branchAddress");
        paramsMap.put("province", "province");
        paramsMap.put("city", "city");
        paramsMap.put("unionCode", "5667888");
        paramsMap.put("bankMobile", "bankMobile");

          /*?????????md5(?????????+???????????????+????????????+??????????????????+????????????)???
        ?????????????????????????????????????????????????????????????????????????????????????????????????????????md5?????????*/
        var sign = DigestUtils.md5DigestAsHex((plat.getBusinessCode() + dto.getOrderId() + currency + df.format(amount)
                + paramsMap.get(TYPE) + paramsMap.get(USER_NAME) + paramsMap.get(CARD_NO) + plat.getBusinessPwd()).getBytes());
        paramsMap.put("sign", sign);
        // ?????????????????????????????????
        String payoutUrl = plat.getUrl() + PAYMENT.getUrl();

        //2???header????????????
        HashMap<String, String> header = new HashMap<>(16);
        header.put(ACCEPT_LANGUAGE, "en");

        String result = null;
        try {
            result = HttpUtils.send(payoutUrl, paramsMap, header);
            JSONObject jsonObject = JSON.parseObject(result);
            if (jsonObject.getInteger("code") == 200) {
                return WithdrawalNotifyResDto.builder().code(jsonObject.getInteger("code") == 200).msg(jsonObject.getString("message")).build();
            }
            coinOnlineWithdrawalServiceImpl.remove(new QueryWrapper<CoinOnlineWithdrawal>().eq("order_id", dto.getOrderId()));
            CodeInfo.PAY_PLAT_ADD_ORDER_FAILURE.setMsg(jsonObject.getString("message"));
            throw new BusinessException(CodeInfo.PAY_PLAT_ADD_ORDER_FAILURE);
        } catch (Exception e) {
            log.error(e.toString());
            log.error("=============================================");
            log.error("Class: " + "MangoPayUtil" + "\n");
            log.error("URL: " + payoutUrl + "\n");
            log.error("postData: " + JSON.toJSONString(paramsMap) + "\n");
            throw new BusinessException(CodeInfo.PAY_PLAT_ADD_ORDER_FAILURE);
        }
    }

    @Override
    public Boolean checkPaymentStatus(String orderId) {
        HashMap<String, String> paramsMap = new HashMap<>(16);
        var currency = EnumUtils.getEnumIgnoreCase(MangoPayParams.CURRENCY.class, configCache.getCountry()).getCode();
        Map<Integer, String> typeMap = Map.of(1, "bank", 3, "alipay");
        DecimalFormat df = new DecimalFormat("0.00");
        paramsMap.put(MERCHANT_ID, plat.getBusinessCode());
        paramsMap.put(CURRENCY, currency);
        var url = plat.getUrl() + QUERY.getUrl();
        //2???header????????????
        HashMap<String, String> header = new HashMap<>(16);
        header.put(ACCEPT_LANGUAGE, "en");
        try {
            CoinOnlineWithdrawal one = coinOnlineWithdrawalServiceImpl.lambdaQuery().eq(CoinOnlineWithdrawal::getOrderId, orderId).one();
            if (null != one) {
                CoinWithdrawal coinWithdrawal = coinWithdrawalServiceImpl.lambdaQuery().eq(CoinWithdrawal::getStatus, 3).eq(CoinWithdrawal::getId, one.getWithdrawalOrderId()).one();
                if (null == coinWithdrawal) {
                    XxlJobLogger.log(MODEL + "?????????????????????????????????");
                    return true;
                }
                paramsMap.put(ORDER_SN, orderId);
                paramsMap.put(AMOUNT, df.format(one.getCoin().multiply(BigDecimal.valueOf(100))));
                paramsMap.put(TYPE, typeMap.get(one.getCategory()));
                var sign = DigestUtils.md5DigestAsHex((plat.getBusinessCode() + orderId + currency + df.format(one.getCoin().multiply(BigDecimal.valueOf(100))) + paramsMap.get(TYPE) + plat.getBusinessPwd()).getBytes());
                paramsMap.put("sign", sign);
                String result = null;
                result = HttpUtils.send(url, paramsMap, header);
                JSONObject jsonObject = JSON.parseObject(result);
                if (jsonObject.getInteger("code") == 200) {
                    Integer status = jsonObject.getJSONObject("data").getInteger("status");
                    if (status == 3) {
                        coinOnlineWithdrawalServiceImpl.lambdaUpdate().set(CoinOnlineWithdrawal::getStatus, 1).set(CoinOnlineWithdrawal::getUpdatedAt, DateNewUtils.now()).eq(CoinOnlineWithdrawal::getOrderId, orderId).update();
                        coinWithdrawalServiceImpl.lambdaUpdate().set(CoinWithdrawal::getStatus, 1).set(CoinWithdrawal::getUpdatedAt, DateNewUtils.now()).eq(CoinWithdrawal::getId, one.getWithdrawalOrderId()).update();
                        //'??????:0-????????? 1-?????? 2-??????'
                        updateUserCoinBase.updateCoinLog(Long.valueOf(one.getWithdrawalOrderId()), 2, 1, DateUtils.getCurrentTime());
                    } else if (status == 4) {
                        coinOnlineWithdrawalServiceImpl.lambdaUpdate().set(CoinOnlineWithdrawal::getStatus, 2).eq(CoinOnlineWithdrawal::getOrderId, orderId).update();
                    }
                    return true;
                } else {
                    XxlJobLogger.log(MODEL + jsonObject.getString("message"));
                    return false;
                }
            }
        } catch (Exception e) {
            log.error(e.toString());
            log.error("=============================================");
            log.error("Class: " + "MangoPayUtil" + "\n");
            log.error("URL: " + url + "\n");
            log.error("postData: " + JSON.toJSONString(paramsMap) + "\n");
            e.printStackTrace();
            throw new BusinessException(CodeInfo.PAY_PLAT_UPDATE_ORDER_FAILURE);
        }
        XxlJobLogger.log(MODEL + "????????????????????????");
        return true;
    }


    /**
     * ??????????????????
     *
     * @param dto ??????
     * @return success-?????? fail-??????
     */
    @SneakyThrows
    public String notifyUrl(@NotNull MangoPayParams.NotifyUrlReqDto dto) {
        // ?????????????????????
        log.info(String.format("=====MangoPay NotifyUrl Record : %s", dto.toString()));
        var path = Paths.get("MangoPay-" + DateUtils.yyyyMMdd(DateNewUtils.now()) + ".txt");
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
        Files.write(path, dto.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        if (null == plat) PayAbstractFactory.init(MODEL);
        // ?????????????????????????????????
        String strSign = dto.getMerchantId() + dto.getOrderSn() + dto.getAmount() + this.plat.getBusinessPwd();
        String sign = DigestUtils.md5DigestAsHex(strSign.getBytes());

        // ??????????????????????????????
        if (!sign.equalsIgnoreCase(dto.getSign()) || dto.getStatus() != 3) {
            log.info("=====MangoPay NotifyUrl : ?????? ??? Status ?????? ");
            return PayParams.STATUS.FAIL.getCode();
        }

        // ??????OrderId ????????????
        CoinDeposit coinDeposit = coinDepositServiceImpl.lambdaQuery()
                .eq(CoinDeposit::getOrderId, dto.getOrderSn())
                .orderByDesc(CoinDeposit::getId)
                .one();
        if (null == coinDeposit) {
            log.info("=====MangoPay NotifyUrl : ??????????????????");
            return PayParams.STATUS.FAIL.getCode();
        } else if (coinDeposit.getStatus() != 0) {
            log.info("=====MangoPay NotifyUrl : ????????????");
            return PayParams.STATUS.FAIL.getCode();
        }

        try {
            // ????????????????????????
            thirdPayBase.updateCoinDeposit(coinDeposit.getId(), dto.getAmount().divide(BigDecimal.valueOf(100), RoundingMode.DOWN));
        } catch (Exception e) {
            log.info("=====MangoPay ?????????????????? :" + e.toString());
        }

        return PayParams.STATUS.SUCCESS.getCode();
    }


    /**
     * mangoPay??????????????????xxlJob?????????????????????????????????????????????????????????
     *
     * @return
     */
    public String withdrawNotifyUrl() {
        List<CoinOnlineWithdrawal> list = coinOnlineWithdrawalServiceImpl.list(new QueryWrapper<CoinOnlineWithdrawal>().eq("status", 0).eq("payout_code", "mango_pay"));
        HashMap<String, String> paramsMap = new HashMap<>(16);
        if (null == plat) PayAbstractFactory.init(MODEL);
        var currency = EnumUtils.getEnumIgnoreCase(MangoPayParams.CURRENCY.class, configCache.getCountry()).getCode();
        Map<Integer, String> typeMap = Map.of(1, "bank", 3, "alipay");
        DecimalFormat df = new DecimalFormat("0.00");
        paramsMap.put(MERCHANT_ID, plat.getBusinessCode());
        paramsMap.put(CURRENCY, currency);
        var url = plat.getUrl() + QUERY.getUrl();
        //2???header????????????
        HashMap<String, String> header = new HashMap<>(16);
        header.put(ACCEPT_LANGUAGE, "en");
        if (!Collections.isEmpty(list)) {
            for (var x : list) {
                try {
                    List<CoinWithdrawal> withdrawalList = coinWithdrawalServiceImpl.lambdaQuery().eq(CoinWithdrawal::getStatus, 3).eq(CoinWithdrawal::getId, x.getWithdrawalOrderId()).list();
                    if (withdrawalList.isEmpty()) {
                        XxlJobLogger.log(MODEL + "??????????????????????????????");
                        return PayParams.STATUS.SUCCESS.getCode();
                    }
                    paramsMap.put(ORDER_SN, x.getOrderId());
                    paramsMap.put(AMOUNT, df.format(x.getCoin().multiply(BigDecimal.valueOf(100))));
                    paramsMap.put(TYPE, typeMap.get(x.getCategory()));
                    var sign = DigestUtils.md5DigestAsHex((plat.getBusinessCode() + x.getOrderId() + currency + df.format(x.getCoin().multiply(BigDecimal.valueOf(100))) + paramsMap.get(TYPE) + plat.getBusinessPwd()).getBytes());
                    paramsMap.put("sign", sign);
                    String result = null;
                    result = HttpUtils.send(url, paramsMap, header);
                    JSONObject jsonObject = JSON.parseObject(result);
                    if (jsonObject.getInteger("code") == 200) {
                        Integer status = jsonObject.getJSONObject("data").getInteger("status");
                        if (status == 3) {
                            coinOnlineWithdrawalServiceImpl.lambdaUpdate().set(CoinOnlineWithdrawal::getStatus, 1).eq(CoinOnlineWithdrawal::getOrderId, x.getOrderId()).update();
                            coinWithdrawalServiceImpl.lambdaUpdate().set(CoinWithdrawal::getStatus, 1).eq(CoinWithdrawal::getId, x.getWithdrawalOrderId()).update();
                            //'??????:0-????????? 1-?????? 2-??????'
                            updateUserCoinBase.updateCoinLog(Long.valueOf(x.getWithdrawalOrderId()), 2, 1, DateUtils.getCurrentTime());
                        } else if (status == 4) {
                            coinOnlineWithdrawalServiceImpl.lambdaUpdate().set(CoinOnlineWithdrawal::getStatus, 2).eq(CoinOnlineWithdrawal::getOrderId, x.getOrderId()).update();
                        }
                        return PayParams.STATUS.SUCCESS.getCode();
                    } else {
                        XxlJobLogger.log(MODEL + jsonObject.getString("message"));
                        return STATUS.FAIL.getCode();
                    }
                } catch (Exception e) {
                    log.error(e.toString());
                    log.error("=============================================");
                    log.error("Class: " + "MangoPayUtil" + "\n");
                    log.error("URL: " + url + "\n");
                    log.error("postData: " + JSON.toJSONString(paramsMap) + "\n");
                    e.printStackTrace();
                    throw new BusinessException(CodeInfo.PAY_PLAT_UPDATE_ORDER_FAILURE);
                }
            }
        }
        XxlJobLogger.log(MODEL + "????????????????????????");
        return PayParams.STATUS.SUCCESS.getCode();
    }

}
