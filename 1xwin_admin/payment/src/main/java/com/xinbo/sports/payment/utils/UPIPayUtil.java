package com.xinbo.sports.payment.utils;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xinbo.sports.dao.generator.po.CoinDeposit;
import com.xinbo.sports.dao.generator.po.CoinOnlineWithdrawal;
import com.xinbo.sports.dao.generator.service.CoinDepositService;
import com.xinbo.sports.dao.generator.service.CoinOnlineWithdrawalService;
import com.xinbo.sports.payment.base.ThirdPayBase;
import com.xinbo.sports.payment.io.PayParams;
import com.xinbo.sports.payment.io.PayParams.*;
import com.xinbo.sports.payment.io.UPIPayParams;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.DateUtils;
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
import org.springframework.util.DigestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.xinbo.sports.payment.io.PayParams.*;

/**
 * @author: David
 * @date: 14/07/2020
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UPIPayUtil implements PayAbstractFactory {
    private final ThirdPayBase thirdPayBase;
    private final CoinDepositService coinDepositServiceImpl;
    private final CoinOnlineWithdrawalService coinOnlineWithdrawalServiceImpl;
    private static final String STATUS = "status";
    private static final String AMOUNT = "amount";
    private static final String MERCHANTORDER = "merchant_order";
    private static final String MERCHANTNO = "merchant_no";
    RestTemplate restTemplate = new RestTemplate();
    private static final String MODEL = "UPIPayUtil";

    @Setter
    UPIPayParams.PayPlat plat;

    /**
     * ????????????
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
        if (!update) throw new BusinessException(CodeInfo.PAY_PLAT_UPDATE_ORDER_ID_INVALID);
        //  ???????????? ??????(???)
        BigDecimal amount = coinDeposit.getCoin();
        // ????????????
        SortedMap<String, String> paramsMap = new TreeMap<>();
        paramsMap.put(MERCHANTNO, plat.getBusinessCode());
        paramsMap.put(MERCHANTORDER, orderSn);
        paramsMap.put(AMOUNT, amount.toBigInteger().toString());
        // ????????????????????????
        var notifyUrl = plat.getNotifyUrl() + PayParams.UrlEnum.DEPOSIT_NOTIFY_URL.getUrl();
        paramsMap.put("notify_url", notifyUrl);
        paramsMap.put("return_url", plat.getReturnUrl());
        //????????????????????????
        String key = plat.getBusinessPwd();
        String sign = signForInspiry(paramsMap, key);
        var params = "merchant_no=" + plat.getBusinessCode() + "&merchant_order=" + orderSn + "&" + AMOUNT + "=" + amount.toBigInteger().toString() + "&notify_url=" + notifyUrl + "&sign=" + sign + "&return_url=" + plat.getReturnUrl();
        var category = 1;
        var method = "GET";
        var url = plat.getUrl() + UPIPayParams.UPIPayMethodEnum.PAY_IN.getUrl() + "?" + params;
        return Map.of("method", method, "url", url, "category", category);
    }

    /**
     * ??????????????????
     * @param dto ??????
     * @return
     */
    @Override
    public WithdrawalNotifyResDto onlineWithdraw(PayParams.OnlinePayoutReqDto dto) {
        // ??????????????????ID
        CoinOnlineWithdrawal coinOnlineWithdrawal = coinOnlineWithdrawalServiceImpl.getOne(new QueryWrapper<CoinOnlineWithdrawal>().eq("order_id", dto.getOrderId()));
        if (null == coinOnlineWithdrawal) throw new BusinessException(CodeInfo.PAY_PLAT_ORDER_ID_INVALID);
        //  ???????????? ??????(???)
        BigDecimal amount = coinOnlineWithdrawal.getCoin();
        // ????????????
        SortedMap<String, String> paramsMap = new TreeMap<>();
        var bankInfo = parseObject(coinOnlineWithdrawal.getBankInfo());
        paramsMap.put(MERCHANTNO, plat.getBusinessCode());
        paramsMap.put(MERCHANTORDER, dto.getOrderId());
        paramsMap.put(AMOUNT, amount.toBigInteger().toString());
        paramsMap.put("ifsc", bankInfo.getJSONObject(MARK).getString("ifsc"));
        paramsMap.put("bank_name", bankInfo.getString(OPEN_ACCOUNT_BANK));
        Map<Integer, String> channel = Map.of(1, "?????????", 6, "UPI");
        paramsMap.put("receiver_type", channel.get(coinOnlineWithdrawal.getCategory()));
        paramsMap.put("receiver_account", bankInfo.getString(BANK_CARD_ACCOUNT));
        paramsMap.put("receiver_name", bankInfo.getString(ACCOUNT_HOLDER));
        // ????????????????????????
        var notifyUrl = plat.getNotifyUrl() + PayParams.UrlEnum.DEPOSIT_NOTIFY_URL.getUrl();
        paramsMap.put("notify_url", notifyUrl);
        //????????????????????????
        paramsMap.put("sign", signForInspiry(paramsMap, plat.getBusinessPwd()));
        var url = plat.getUrl() + UPIPayParams.UPIPayMethodEnum.AGENT_PAY.getUrl();
        ResponseEntity<String> response = restTemplate.postForEntity(url, paramsMap, String.class);
        JSONObject result = parseObject(response.getBody());
        if (result.getInteger("result_code") == 200) {
            if (result.getInteger("status") == 3) {
                return null;
            }
        } else {
            log.info("======UPIPay: ??????Code???????????? : " + result);
            throw new BusinessException(CodeInfo.PAY_PLAT_LOAD_INVALID);
        }
        return null;
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
        log.info(String.format("=====UPIPay NotifyUrl Record : %s", dto.toString()));
        var path = Paths.get("UPIPay-" + DateUtils.yyyyMMdd(DateNewUtils.now()) + ".txt");
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
        if (null == plat) PayAbstractFactory.init(MODEL);
        Files.write(path, dto.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        SortedMap<String, String> paramsMap = new TreeMap<>();
        paramsMap.put(MERCHANTNO, dto.get(MERCHANTNO));
        paramsMap.put(MERCHANTORDER, dto.get(MERCHANTORDER));
        paramsMap.put("paid", dto.get("paid"));
        paramsMap.put("fee", dto.get("fee"));
        paramsMap.put(AMOUNT, dto.get(AMOUNT));
        paramsMap.put(STATUS, dto.get(STATUS));
        paramsMap.put("additional", dto.get("additional"));
        paramsMap.put("paid_at", dto.get("paid_at"));
        // ?????????????????????????????????
        String sign = signForInspiry(paramsMap, this.plat.getBusinessPwd());
        // ??????????????????????????????
        if (!sign.equalsIgnoreCase(dto.get("sign")) || !dto.get(STATUS).equals("2")) {
            log.info("=====UPIPay NotifyUrl : ?????? ??? Status ?????? ");
            Files.write(path, PayParams.STATUS.FAIL.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            return PayParams.STATUS.FAIL.getCode();
        }
        // ??????OrderId ????????????
        CoinDeposit coinDeposit = coinDepositServiceImpl.lambdaQuery()
                .eq(CoinDeposit::getOrderId, dto.get(MERCHANTORDER))
                .orderByDesc(CoinDeposit::getId)
                .one();
        if (null == coinDeposit) {
            log.info("=====UPIPay NotifyUrl : ??????????????????");
            Files.write(path, PayParams.STATUS.FAIL.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            return PayParams.STATUS.FAIL.getCode();
        } else if (coinDeposit.getStatus() != 0) {
            Files.write(path, PayParams.STATUS.FAIL.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            log.info("=====UPIPay NotifyUrl : ????????????");
            return PayParams.STATUS.FAIL.getCode();
        }
        try {
            // ????????????????????????
            thirdPayBase.updateCoinDeposit(coinDeposit.getId(), new BigDecimal(dto.get("paid")), dto.toString());
        } catch (Exception e) {
            Files.write(path, PayParams.STATUS.FAIL.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            log.info("=====UPIPay ?????????????????? :" + e.toString());
        }
        Files.write(path, PayParams.STATUS.SUCCESS.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        return PayParams.STATUS.SUCCESS.getCode();
    }


    public String withdrawNotifyUrl(Map<String, String> dto) {
        return null;
    }

    /**
     * ????????????
     * secret???????????????????????????????????????
     * @param paramsMap
     * @param key
     * @return
     */
    private String signForInspiry(SortedMap<String, String> paramsMap, String key) {
        StringBuilder sbkey = new StringBuilder();
        Set<Map.Entry<String, String>> es = paramsMap.entrySet();
        for (Map.Entry<String, String> entry : es) {
            String k = entry.getKey();
            Object v = entry.getValue();
            //???????????????????????????????????????
            if (null != v && !"".equals(v)) {
                sbkey.append(k).append("=").append(v).append("&");
            }
        }

        sbkey = sbkey.append("secret=").append(key);

        //MD5??????,???????????????????????????
        return DigestUtils.md5DigestAsHex(sbkey.toString().getBytes()).toUpperCase();
    }
}
