package com.xinbo.sports.payment.utils;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xinbo.sports.dao.generator.po.CoinDeposit;
import com.xinbo.sports.dao.generator.po.CoinOnlineWithdrawal;
import com.xinbo.sports.dao.generator.service.CoinDepositService;
import com.xinbo.sports.dao.generator.service.CoinOnlineWithdrawalService;
import com.xinbo.sports.dao.generator.service.UserBankService;
import com.xinbo.sports.payment.base.ThirdPayBase;
import com.xinbo.sports.payment.io.DsPayParams;
import com.xinbo.sports.payment.io.PayParams;
import com.xinbo.sports.payment.io.PayParams.OnlinePayReqDto;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.DateUtils;
import com.xinbo.sports.utils.IpUtil;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.*;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.xinbo.sports.payment.io.PayParams.BANK_CARD_ACCOUNT;
import static com.xinbo.sports.utils.MD5.signForInspiry;

/**
 * @author: David
 * @date: 14/07/2020
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DsPayUtil implements PayAbstractFactory {
    private final ThirdPayBase thirdPayBase;
    private final CoinDepositService coinDepositServiceImpl;
    private final CoinOnlineWithdrawalService coinOnlineWithdrawalServiceImpl;
    private final HttpServletRequest httpServletRequest;
    private final UserBankService userBankServiceImpl;
    private static final String MONEY = "money";
    private static final String APP_ID = "appid";
    private static final String OUT_TRADE_NO = "out_trade_no";
    private static final String AMOUNT = "amount";
    private static final String PAY_TYPE = "pay_type";
    private static final String SUCCESS_URL = "success_url";
    private static final String CALLBACKS = "callbacks";
    private static final String ACCOUNT = "account";
    private static final String BANK_TYPE = "bank_type";
    private static final String CHANNEL_CODE = "channelCode";
    private static final String IFSC = "ifsc";
    private static final String BANK_ID = "bank_id";
    RestTemplate restTemplate = new RestTemplate();
    private static final String MODEL = "DsPayUtil";


    @Setter
    public DsPayParams.PayPlat plat;

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
        DecimalFormat df = new DecimalFormat("0.00");
        BigDecimal amount = coinDeposit.getCoin();
        // ????????????
        SortedMap<String, String> paramsMap = new TreeMap<>();
        paramsMap.put(APP_ID, plat.getBusinessCode());
        paramsMap.put(OUT_TRADE_NO, orderSn);
        paramsMap.put("version", "v2.0");
        Map<Integer, String> channel = Map.of(1, "CopyToBank", 6, "upi");
        paramsMap.put(PAY_TYPE, channel.get(coinDeposit.getCategory()));
        if (coinDeposit.getCategory() == 1) {
            paramsMap.put("full_name", String.valueOf(coinDeposit.getCategory()));
        }
        paramsMap.put(AMOUNT, df.format(amount));
        // ????????????????????????
        paramsMap.put("callback_url", plat.getNotifyUrl() + PayParams.UrlEnum.DEPOSIT_NOTIFY_URL.getUrl());
        paramsMap.put(SUCCESS_URL, plat.getReturnUrl());
        paramsMap.put("error_url", plat.getReturnUrl());
        //????????????????????????
        String sign = signForInspiry(paramsMap, plat.getBusinessPwd());
        paramsMap.put("sign", sign);
        var payUrl = plat.getUrl() + DsPayParams.DsPayMethodEnum.DEPOSIT.getUrl();
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(payUrl + "?format=json", paramsMap, String.class);
        if (responseEntity.getStatusCodeValue() == 200) {
            var call = parseObject(responseEntity.getBody());
            if (call.getInteger("code") == 200) {
                return Map.of("method", "GET", "url", call.getString("url"), "category", 1);
            }
        }
        log.info("======DsPay: ??????Code???????????? : " + responseEntity.toString());
        throw new BusinessException(CodeInfo.PAY_PLAT_LOAD_INVALID);
    }

    /**
     * ??????????????????
     *
     * @param dto ??????
     * @return
     */
    @Override
    public PayParams.WithdrawalNotifyResDto onlineWithdraw(PayParams.OnlinePayoutReqDto dto) {
        // ??????????????????ID
        CoinOnlineWithdrawal coinOnlineWithdrawal = coinOnlineWithdrawalServiceImpl.getOne(new QueryWrapper<CoinOnlineWithdrawal>().eq("order_id", dto.getOrderId()));
        if (null == coinOnlineWithdrawal) {
            throw new BusinessException(CodeInfo.PAY_PLAT_ORDER_ID_INVALID);
        }
        //  ???????????? ??????(???)
        DecimalFormat df = new DecimalFormat("0.00");
        BigDecimal amount = coinOnlineWithdrawal.getCoin();
        // ????????????
        var bankInfo = parseObject(coinOnlineWithdrawal.getBankInfo());
        SortedMap<String, String> paramsMap = new TreeMap<>();
        paramsMap.put(APP_ID, plat.getBusinessCode());
        paramsMap.put(ACCOUNT, bankInfo.getString(BANK_CARD_ACCOUNT));
        paramsMap.put(MONEY, df.format(amount));
        paramsMap.put("name", bankInfo.getString("accountHolder"));

        Map<String, String> bankCardMap = Map.of(CHANNEL_CODE, "bankcard", IFSC, bankInfo.getJSONObject("mark").getString(IFSC), BANK_ID, bankInfo.getString("bankCode"));
        Map<String, String> upiMap = Map.of(CHANNEL_CODE, "upi", IFSC, "0", BANK_ID, bankInfo.getString("bankCode"));
        Map<Integer, Map<String, String>> channelMap = Map.of(1, bankCardMap, 6, upiMap);
        paramsMap.put(BANK_TYPE, channelMap.get(coinOnlineWithdrawal.getCategory()).get(CHANNEL_CODE));
        paramsMap.put(BANK_ID, bankInfo.getString("bankCode"));
        paramsMap.put("callback", plat.getNotifyUrl() + PayParams.UrlEnum.WITHDRAW_NOTIFY_URL.getUrl());
        paramsMap.put(OUT_TRADE_NO, dto.getOrderId());
        paramsMap.put(IFSC, channelMap.get(coinOnlineWithdrawal.getCategory()).get(IFSC));

        //????????????????????????
        String sign = signForInspiry(paramsMap, plat.getBusinessPwd());
        paramsMap.put("sign", sign);
        var payoutUrl = plat.getUrl() + DsPayParams.DsPayMethodEnum.WITHDRAW.getUrl();
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(payoutUrl, paramsMap, String.class);
        if (responseEntity.getStatusCodeValue() == 200) {
            var call = parseObject(responseEntity.getBody());
            if (call.getInteger("code") == 200) {
                var data = parseObject(call.getString("data"));
                return null;
            }
        }
        log.info("======DsPay: ??????Code???????????? : " + responseEntity.toString());
        throw new BusinessException(CodeInfo.PAY_PLAT_LOAD_INVALID);
    }

    @Override
    public Boolean checkPaymentStatus(String orderId) {
        throw new UnsupportedOperationException();
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
        log.info(String.format("=====DsPay NotifyUrl Record : %s", dto.toString()));
        var path = Paths.get("DsPay-" + DateUtils.yyyyMMdd(DateNewUtils.now()) + ".txt");
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
        if (!IpUtil.getIp(httpServletRequest).equals("147.139.42.219")) {
            throw new BusinessException(CodeInfo.PLAT_IP_NOT_ACCESS);
        }
        if (null == plat) PayAbstractFactory.init(MODEL);
        Files.write(path, dto.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        SortedMap<String, String> paramsMap = new TreeMap<>();
        paramsMap.put(CALLBACKS, dto.get(CALLBACKS));
        paramsMap.put(APP_ID, plat.getBusinessCode());
        paramsMap.put(OUT_TRADE_NO, dto.get(OUT_TRADE_NO));
        paramsMap.put(PAY_TYPE, dto.get(PAY_TYPE));
        paramsMap.put(AMOUNT, dto.get(AMOUNT));
        paramsMap.put("amount_true", dto.get("amount_true"));
        paramsMap.put(SUCCESS_URL, dto.get(SUCCESS_URL));
        paramsMap.put("error_url", dto.get("error_url"));
        paramsMap.put("out_uid", dto.get("out_uid"));

        var signature = signForInspiry(paramsMap, plat.getBusinessPwd());
        // ??????????????????????????????
        if (!signature.equalsIgnoreCase(dto.get("sign")) || !dto.get(CALLBACKS).equals("CODE_SUCCESS")) {
            log.info("=====DsPay NotifyUrl : ?????? ??? Status ?????? ");
            Files.write(path, "=====DsPay NotifyUrl : ?????? ??? Status ??????".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            return PayParams.STATUS.FAIL.getCode();
        }
        // ??????OrderId ????????????
        CoinDeposit coinDeposit = coinDepositServiceImpl.lambdaQuery()
                .eq(CoinDeposit::getOrderId, dto.get(OUT_TRADE_NO))
                .orderByDesc(CoinDeposit::getId)
                .one();
        if (null == coinDeposit) {
            log.info("=====DsPay NotifyUrl : ??????????????????");
            Files.write(path, "======DsPay NotifyUrl : ??????????????????".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            return PayParams.STATUS.FAIL.getCode();
        } else if (coinDeposit.getStatus() != 0) {
            log.info("=====DsPay NotifyUrl : ????????????");
            Files.write(path, "======DsPay NotifyUrl : ????????????".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            return PayParams.STATUS.FAIL.getCode();
        }
        try {
            // ????????????????????????
            thirdPayBase.updateCoinDeposit(coinDeposit.getId(), new BigDecimal(dto.get(AMOUNT)));
        } catch (Exception e) {
            log.info("=====DsPay ?????????????????? :" + e.toString());
            Files.write(path, "======DsPay NotifyUrl : ????????????".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        }
        Files.write(path, PayParams.STATUS.SUCCESS.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        return PayParams.STATUS.SUCCESS.getCode();
    }

    /**
     * ????????????
     *
     * @param dto
     * @return
     */
    @SneakyThrows
    public String withdrawNotifyUrl(Map<String, String> dto) {
        // ?????????????????????
        log.info(String.format("=====DsPay withdrawNotifyUrl Record : %s", dto.toString()));
        var path = Paths.get("DsPay-" + DateUtils.yyyyMMdd(DateNewUtils.now()) + ".txt");
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
        if (null == plat) PayAbstractFactory.init(MODEL);
        Files.write(path, dto.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        SortedMap<String, String> paramsMap = new TreeMap<>();
        JSONObject data = parseObject(dto.get("data"));
        paramsMap.put(APP_ID, data.getString(APP_ID));
        paramsMap.put("order_no", data.getString("order_no"));
        paramsMap.put(OUT_TRADE_NO, data.getString(OUT_TRADE_NO));
        paramsMap.put(ACCOUNT, data.getString(ACCOUNT));
        paramsMap.put(BANK_TYPE, data.getString(BANK_TYPE));
        paramsMap.put(MONEY, data.getString(MONEY));

        var signature = signForInspiry(paramsMap, plat.getBusinessPwd());
        // ??????????????????????????????
        if (!signature.equalsIgnoreCase(data.getString("sign"))) {
            log.info("=====DsPay NotifyUrl : ?????? ??? Status ?????? ");
            Files.write(path, "=====DsPay NotifyUrl : ?????? ??? Status ??????".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            return PayParams.STATUS.FAIL.getCode();
        }
        /*try {
            // ??????OrderId ????????????
            CoinWithdrawal coinWithdrawal = coinWithdrawalServiceImpl.lambdaQuery()
                    .eq(CoinWithdrawal::getOrderId, data.getString(OUT_TRADE_NO)).eq(CoinWithdrawal::getCategory, 3)
                    .orderByDesc(CoinWithdrawal::getId)
                    .one();
            if (null != coinWithdrawal) {
                log.info("=====DsPay NotifyUrl : ????????????");
                if (dto.get("code").equals("0")) {
                    coinWithdrawalServiceImpl.lambdaUpdate().set(CoinWithdrawal::getCategory, 1).update();
                    Files.write(path, DsPayParams.STATUS.SUCCESS.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                    return DsPayParams.STATUS.SUCCESS.getCode();
                } else if (dto.get("code").equals("1")) {
                    // ??????????????????
                    coinWithdrawalServiceImpl.lambdaUpdate().set(CoinWithdrawal::getCategory, 2).eq(CoinWithdrawal::getOrderId, data.getString(OUT_TRADE_NO)).update();
                    thirdPayBase.updateCoinDeposit(coinWithdrawal.getId(), new BigDecimal(dto.get(AMOUNT)));
                    return DsPayParams.STATUS.SUCCESS.getCode();
                }
            }
        } catch (Exception e) {
            log.info("=====DsPay ?????????????????? :" + e.toString());
            Files.write(path, "======DsPay WithdrawNotifyUrl : ????????????".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        }*/
        return PayParams.STATUS.SUCCESS.getCode();
    }
}