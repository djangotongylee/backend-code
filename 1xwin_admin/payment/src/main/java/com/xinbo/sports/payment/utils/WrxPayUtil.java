package com.xinbo.sports.payment.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xinbo.sports.dao.generator.po.CoinDeposit;
import com.xinbo.sports.dao.generator.po.CoinOnlineWithdrawal;
import com.xinbo.sports.dao.generator.po.CoinWithdrawal;
import com.xinbo.sports.dao.generator.po.User;
import com.xinbo.sports.dao.generator.service.CoinDepositService;
import com.xinbo.sports.dao.generator.service.CoinOnlineWithdrawalService;
import com.xinbo.sports.dao.generator.service.CoinWithdrawalService;
import com.xinbo.sports.dao.generator.service.UserService;
import com.xinbo.sports.payment.base.ThirdPayBase;
import com.xinbo.sports.payment.io.EasyPayParams;
import com.xinbo.sports.payment.io.PayParams.*;
import com.xinbo.sports.payment.io.WrxPayParams;
import com.xinbo.sports.service.base.UpdateUserCoinBase;
import com.xinbo.sports.service.cache.KeyConstant;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.service.cache.redis.DictionaryCache;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.bo.DictionaryBo;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.DateUtils;
import com.xinbo.sports.utils.HttpUtils;
import com.xinbo.sports.utils.components.response.CodeInfo;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.xinbo.sports.payment.io.EasyPayParams.EasyPayMethodEnum.QUERY;
import static com.xinbo.sports.payment.io.PayParams.*;

/**
 * @author: David
 * @date: 14/07/2020
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WrxPayUtil implements PayAbstractFactory {
    private final ThirdPayBase thirdPayBase;
    private final CoinDepositService coinDepositServiceImpl;
    private final ConfigCache configCache;
    private final CoinOnlineWithdrawalService coinOnlineWithdrawalServiceImpl;
    private final CoinWithdrawalService coinWithdrawalServiceImpl;
    private final UserService userServiceImpl;
    private final UpdateUserCoinBase updateUserCoinBase;
    private static final String MODEL = "WrxPayUtil";
    private static final String DICT_KEY = "dic_easypay_code";
    private final DictionaryCache dictionaryBase;
    @Setter
    public WrxPayParams.PayPlat plat;

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
        // ????????????
        SortedMap<String, String> paramsMap = new TreeMap<>();
        paramsMap.put("gymchtId", plat.getBusinessCode());
        paramsMap.put("tradeSn", orderSn);
        paramsMap.put("orderAmount", String.valueOf(coinDeposit.getCoin().intValue()*100));
        paramsMap.put("goodsName", orderSn);
        paramsMap.put("tradeSource", "upi");
        paramsMap.put("realname", "HU LIN");
        paramsMap.put("userMobile", String.valueOf(RandomUtils.nextLong(00000000L,99999999L)));
        paramsMap.put("userEmail", "888888@gmail.com");
        paramsMap.put("callback_url", plat.getReturnUrl());
        paramsMap.put("notifyUrl", plat.getNotifyUrl() + UrlEnum.DEPOSIT_NOTIFY_URL.getUrl());
        paramsMap.put("sign", getSign(paramsMap, plat.getBusinessPwd()));


        // ?????????????????????????????????
        String payUrl = plat.getUrl();
        String result = null;

        String url = null;
        try {
            HashMap map = new HashMap();
            map.put("Content-Type", "application/x-www-form-urlencoded");
            result = HttpUtils.doPost(payUrl, JSONObject.parseObject(JSON.toJSONString(paramsMap)), map, "PaymentReqDto");
            JSONObject jsonObject = JSON.parseObject(result);
            if (!jsonObject.getString("resultCode").equalsIgnoreCase("00000")) {
                log.info("======WrxpayUtil: ???????????? : " + jsonObject.toJSONString());
                throw new BusinessException(jsonObject.getString("message"));
            } else {
                url = jsonObject.getString("code_url");
            }
        } catch (Exception e) {
            log.error(e.toString());
            log.error("===========WrxpayUtil URL: " + payUrl + "\n");
            log.error("===========WrxpayUtil postData: " + JSON.toJSONString(paramsMap) + "\n");
            throw new BusinessException(e.getMessage());
        }
        return Map.of("method", "GET", "url", url, "category",1 );
    }



    private  String getSign(Map<String, String> map, String signKey) {
        if (map == null) {
            return null;
        }
        List<String> keyList = new ArrayList<>(map.keySet());
        Collections.sort(keyList);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < keyList.size(); i++) {
            String key = keyList.get(i);
            String value = map.get(key);
            if(StringUtils.isEmpty(value)){
                continue;
            }
            sb.append(key + "=" + value + "&");
        }
        String signStr = sb.append("key=") + signKey;
        String md5Str = DigestUtils.md5DigestAsHex(signStr.getBytes());
        return md5Str.toUpperCase();
    }

    /**
     * ??????????????????
     *
     * @param dto ??????
     * @return
     */
    @Override
    public WithdrawalNotifyResDto onlineWithdraw(OnlinePayoutReqDto dto) {
        // ??????????????????ID
        CoinOnlineWithdrawal coinOnlineWithdrawal = coinOnlineWithdrawalServiceImpl.getOne(new QueryWrapper<CoinOnlineWithdrawal>().eq("order_id", dto.getOrderId()));
        if (null == coinOnlineWithdrawal) {
            throw new BusinessException(CodeInfo.PAY_PLAT_ORDER_ID_INVALID);
        }

        //  ???????????? ??????(???)
        BigDecimal amount = coinOnlineWithdrawal.getCoin();
        // ????????????
        HashMap<String, String> paramsMap = new HashMap<>(16);
        var bankInfo = parseObject(coinOnlineWithdrawal.getBankInfo());
        var currency = EnumUtils.getEnumIgnoreCase(EasyPayParams.CURRENCY.class, configCache.getCountry());

        paramsMap.put("gymchtId", plat.getBusinessCode());
        paramsMap.put("dfSn", dto.getOrderId());
        paramsMap.put("receiptAmount", String.valueOf(amount.intValue()*100));
        paramsMap.put("curType", "1");
        paramsMap.put("payType", "1");
        paramsMap.put("paymentModes", "imps");
        paramsMap.put("receiptName", bankInfo.getString(ACCOUNT_HOLDER));
        paramsMap.put("receiptPan", bankInfo.getString(BANK_CARD_ACCOUNT));
        paramsMap.put("receiptBankNm", dto.getOpenAccountBank());
        if (currency.getDesc().equals("INR")) {
            paramsMap.put("settleNo", bankInfo.getString("mark"));
        }
        paramsMap.put("beneficiaryEmail", "11@gmail.com");
        paramsMap.put("beneficiaryAddress", "province");
        paramsMap.put("mobile", String.valueOf(RandomUtils.nextLong(00000000L,99999999L)));
        paramsMap.put("acctType", "0");
        paramsMap.put("nonce", dto.getOrderId());
        paramsMap.put("notifyUrl", plat.getNotifyUrl() + UrlEnum.WITHDRAW_NOTIFY_URL.getUrl());
        paramsMap.put("sign", getSign(paramsMap, plat.getBusinessPwd()));

        // ?????????????????????????????????
        String payoutUrl = plat.getWithdrawUrl();


        String result = null;
        try {
            HashMap map = new HashMap();
            map.put("Content-Type", "application/x-www-form-urlencoded");
            result = HttpUtils.doPost(payoutUrl, JSONObject.parseObject(JSON.toJSONString(paramsMap)), map, "PaymentReqDto");
            JSONObject jsonObject = JSON.parseObject(result);
            if (jsonObject.getString("resultCode").equalsIgnoreCase("00000")) {
                return WithdrawalNotifyResDto.builder().code(true).msg(jsonObject.getString("message")).build();
            }
            coinOnlineWithdrawalServiceImpl.remove(new QueryWrapper<CoinOnlineWithdrawal>().eq("order_id", dto.getOrderId()));
            throw new BusinessException(jsonObject.getString("message"));

        } catch (Exception e) {
            coinOnlineWithdrawalServiceImpl.lambdaUpdate().set(CoinOnlineWithdrawal::getStatus, 2).eq(CoinOnlineWithdrawal::getOrderId, dto.getOrderId()).update();
            log.error(e.toString());
            log.error("=============================================");
            log.error("Class: " + "WrxPayUtil" + "\n");
            log.error("URL: " + payoutUrl + "\n");
            log.error("postData: " + JSON.toJSONString(paramsMap) + "\n");
            throw new BusinessException(e.getMessage());
        }
    }


    /**
     * ??????????????????
     *
     * @param category ????????????
     * @return DictionaryBo
     */
    private DictionaryBo buildDictionaryBo(String category) {
        return DictionaryBo.builder()
                // ??????: 0-?????? 1-?????? 2-??????
                .isShowList(List.of(0, 2))
                .category(category)
                // ??????backend(??????)??????
                .dictHashKey(KeyConstant.DICTIONARY_BACKEND_HASH)
                .build();
    }


    /**
     * ??????????????????
     *
     * @param dto ??????
     * @return success-?????? fail-??????
     */
    @SneakyThrows
    public String notifyUrl(@NotNull WrxPayParams.NotifyUrlReqDto dto) {
        // ?????????????????????
        log.info(String.format("=====WrxPay NotifyUrl Record : %s", dto.toString()));
        var path = Paths.get("WrxPay-" + DateUtils.yyyyMMdd(DateNewUtils.now()) + ".txt");
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
        if (null == plat) {
            PayAbstractFactory.init(MODEL);
        }
        Files.write(path, dto.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        SortedMap<String, String> paramsMap = new TreeMap<>();
        paramsMap.put("gymchtId", dto.getGymchtId());
        paramsMap.put("orderAmount", dto.getOrderAmount().toString());
        paramsMap.put("pay_result", dto.getPay_result());
        paramsMap.put("t0Flag", dto.getT0Flag());
        paramsMap.put("timeEnd", dto.getTimeEnd());
        paramsMap.put("tradeSn", dto.getTradeSn());
        paramsMap.put("transaction_id", dto.getTransaction_id());
        String sign = getSign(paramsMap, plat.getBusinessPwd());
        if (!dto.getSign().equals(sign)) {
            Files.write(path, "=====WrxPay NotifyUrl : ?????? ??? Status ??????".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            return STATUS.FAIL.getCode();
        }

        // ??????OrderId ????????????
        CoinDeposit coinDeposit = coinDepositServiceImpl.lambdaQuery()
                .eq(CoinDeposit::getOrderId, dto.getTradeSn())
                .orderByDesc(CoinDeposit::getId)
                .one();
        if (null == coinDeposit) {
            log.info("=====WrxPay NotifyUrl : ??????????????????");
            Files.write(path, "======WrxPay NotifyUrl : ??????????????????".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            return STATUS.FAIL.getCode();
        } else if (coinDeposit.getStatus() != 0) {
            log.info("=====WrxPay NotifyUrl : ????????????");
            Files.write(path, "======WrxPay NotifyUrl : ????????????".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            return STATUS.FAIL.getCode();
        }

        try {
            // ????????????????????????
            thirdPayBase.updateCoinDeposit(coinDeposit.getId(), BigDecimal.valueOf(dto.getOrderAmount()/100));
        } catch (Exception e) {
            log.info("=====WrxPay ?????????????????? :" + e.toString());
            Files.write(path, "======WrxPay NotifyUrl : ????????????".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        }
        Files.write(path, STATUS.SUCCESS.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        return "success";
    }


    @SneakyThrows
    public String withdrawNotifyUrl(WrxPayParams.DFNotifyUrlReqDto dto) {
        // ?????????????????????
        var path = Paths.get(MODEL + "-" + DateUtils.yyyyMMdd(DateNewUtils.now()) + ".txt");
        if (!Files.exists(path)) {Files.createFile(path);}
        Files.write(path, dto.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        if (null == plat) {
            PayAbstractFactory.init(MODEL);
        }
        log.info(String.format("=====" + MODEL + " withdrawNotifyUrl Record : %s", dto));

        SortedMap<String, String> paramsMap = new TreeMap<>();
        paramsMap.put("dfDesc", dto.getDfDesc());
        paramsMap.put("dfSn", dto.getDfSn());
        paramsMap.put("dfState", dto.getDfState());
        paramsMap.put("dfTransactionId", dto.getDfTransactionId());

        paramsMap.put("message", dto.getMessage());
        paramsMap.put("mobile", dto.getMobile());
        paramsMap.put("receiptAmount", dto.getReceiptAmount().toString());
        paramsMap.put("receiptBankNm", dto.getReceiptBankNm());

        paramsMap.put("receiptName", dto.getReceiptName());
        paramsMap.put("receiptPan", dto.getReceiptPan());
        paramsMap.put("resultCode", dto.getResultCode());
        paramsMap.put("timeEnd", dto.getTimeEnd());
        String sign = getSign(paramsMap, plat.getBusinessPwd());
        if (!dto.getSign().equals(sign)) {
            Files.write(path, "=====WrxPay ??????NotifyUrl : ?????? ??? Status ??????".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            return STATUS.FAIL.getCode();

        }
        // ??????OrderId ????????????
        CoinOnlineWithdrawal coinOnlineWithdrawal = coinOnlineWithdrawalServiceImpl.lambdaQuery()
                .eq(CoinOnlineWithdrawal::getOrderId, dto.getDfSn())
                .orderByDesc(CoinOnlineWithdrawal::getId)
                .one();
        if (null == coinOnlineWithdrawal) {
            log.info("=====" + MODEL + "NotifyUrl : ??????????????????");
            return STATUS.FAIL.getCode();
        } else if (coinOnlineWithdrawal.getStatus() != 0) {
            log.info("=====" + MODEL + "NotifyUrl : ????????????");
            return STATUS.SUCCESS.getCode();
        }

        try {
            userServiceImpl.lambdaUpdate()
                .setSql("fcoin = fcoin -" + coinOnlineWithdrawal.getCoin())
                .set(User::getUpdatedAt, DateUtils.getCurrentTime())
                .eq(User::getId, coinOnlineWithdrawal.getUid())
                .update();
            if ("00".equals(dto.getDfState())) {
                coinOnlineWithdrawalServiceImpl.lambdaUpdate().set(CoinOnlineWithdrawal::getStatus, 1).eq(CoinOnlineWithdrawal::getOrderId, coinOnlineWithdrawal.getOrderId()).update();
                coinWithdrawalServiceImpl.lambdaUpdate().set(CoinWithdrawal::getStatus, 1).eq(CoinWithdrawal::getId, coinOnlineWithdrawal.getWithdrawalOrderId()).update();
                updateUserCoinBase.updateCoinLog(Long.valueOf(coinOnlineWithdrawal.getWithdrawalOrderId()), 2, 1, DateUtils.getCurrentTime());
                log.info("????????????????????????!");
                return STATUS.SUCCESS.getCode();
            } /*else if (Integer.parseInt(dto.getStatus()) == 2 || Integer.parseInt(dto.getStatus()) == 3) {
                coinOnlineWithdrawalServiceImpl.lambdaUpdate().set(CoinOnlineWithdrawal::getStatus, 2).eq(CoinOnlineWithdrawal::getOrderId, coinOnlineWithdrawal.getOrderId()).update();
                return STATUS.SUCCESS.getCode();
            }*/
        } catch (Exception e) {
            Files.write(path, STATUS.FAIL.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            log.info("=====" + MODEL + "  ?????????????????? :" + e.toString());
            return STATUS.FAIL.getCode();
        }
        Files.write(path, STATUS.SUCCESS.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        return STATUS.FAIL.getCode();
    }


    @Override
    public Boolean checkPaymentStatus(String orderId) {
        if (null == plat) PayAbstractFactory.init(MODEL);
        var url = plat.getUrl() + QUERY.getUrl();
        try {
            Map paramsMap = new HashMap<>();
            CoinOnlineWithdrawal withdrawalList = coinOnlineWithdrawalServiceImpl.lambdaQuery().eq(CoinOnlineWithdrawal::getOrderId, orderId).one();
            if (withdrawalList == null) {
                XxlJobLogger.log(MODEL + "??????????????????????????????");
                return true;
            }
            paramsMap.put("out_trade_no", orderId);
            paramsMap.put("mchid", plat.getBusinessCode());
            String sign = getSign(paramsMap, plat.getBusinessPwd());
            paramsMap.put("pay_md5sign", sign);
            HashMap map = new HashMap();
            map.put("Content-Type", "application/x-www-form-urlencoded");
            var result = HttpUtils.doPost(url, JSONObject.parseObject(JSON.toJSONString(paramsMap)), map, "PaymentReqDto");
            JSONObject jsonObject = JSON.parseObject(result);
            if (jsonObject.getString("status").equalsIgnoreCase("success")) {

            } else if (jsonObject.getInteger("status") == 1) {
                coinOnlineWithdrawalServiceImpl.lambdaUpdate().set(CoinOnlineWithdrawal::getStatus, 1).eq(CoinOnlineWithdrawal::getOrderId, orderId).update();
                coinWithdrawalServiceImpl.lambdaUpdate().set(CoinWithdrawal::getStatus, 1).eq(CoinWithdrawal::getId, withdrawalList.getWithdrawalOrderId()).update();
                updateUserCoinBase.updateCoinLog(Long.valueOf(withdrawalList.getWithdrawalOrderId()), 2, 1, DateUtils.getCurrentTime());
                log.info("????????????????????????!");
            } else if (jsonObject.getInteger("status") == 2 || jsonObject.getInteger("status") == 3) {
                coinOnlineWithdrawalServiceImpl.lambdaUpdate().set(CoinOnlineWithdrawal::getStatus, 2).eq(CoinOnlineWithdrawal::getOrderId, orderId).update();
            }
            return true;
        } catch (Exception e) {
            log.error(e.toString());
            log.error("=============================================");
            log.error("Class: " + "EasyPayUtil" + "\n");
            log.error("URL: " + url + "\n");
            e.printStackTrace();
            throw new BusinessException(CodeInfo.PAY_PLAT_UPDATE_ORDER_FAILURE);
        }
    }
}
