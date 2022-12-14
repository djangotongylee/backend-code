package com.xinbo.sports.payment.utils;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xinbo.sports.dao.generator.po.CoinDeposit;
import com.xinbo.sports.dao.generator.po.CoinLog;
import com.xinbo.sports.dao.generator.po.CoinOnlineWithdrawal;
import com.xinbo.sports.dao.generator.po.CoinWithdrawal;
import com.xinbo.sports.dao.generator.service.CoinDepositService;
import com.xinbo.sports.dao.generator.service.CoinLogService;
import com.xinbo.sports.dao.generator.service.CoinOnlineWithdrawalService;
import com.xinbo.sports.dao.generator.service.CoinWithdrawalService;
import com.xinbo.sports.payment.base.ThirdPayBase;
import com.xinbo.sports.payment.io.*;
import com.xinbo.sports.payment.io.PayParams.OnlinePayReqDto;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Map;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.xinbo.sports.payment.io.PayParams.*;

/**
 * @author: David
 * @date: 14/07/2020
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class InPayUtil implements PayAbstractFactory {
    private final ThirdPayBase thirdPayBase;
    private final CoinDepositService coinDepositServiceImpl;
    private final CoinWithdrawalService coinWithdrawalServiceImpl;
    private final CoinOnlineWithdrawalService coinOnlineWithdrawalServiceImpl;
    private final CoinLogService coinLogServiceImpl;
    RestTemplate restTemplate = new RestTemplate();
    private static final String MODEL = "InPayUtil";
    private static final String MERCHANT_NUM = "merchantNum";
    private static final String ORDER_NO = "orderNo";
    private static final String AMOUNT = "amount";
    private static final String CHANNEL_CODE = "channelCode";

    @Setter
    InPayParams.PayPlat plat;

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
        if (!update) {
            throw new BusinessException(CodeInfo.PAY_PLAT_UPDATE_ORDER_ID_INVALID);
        }
        //  ???????????? ??????(???)
        BigDecimal amount = coinDeposit.getCoin();
        // ????????????
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        DecimalFormat df = new DecimalFormat("0.00");
        paramsMap.add(MERCHANT_NUM, plat.getBusinessCode());
        paramsMap.add(ORDER_NO, orderSn);
        paramsMap.add(AMOUNT, df.format(amount));
        // ????????????????????????
        var notifyUrl = plat.getNotifyUrl() + PayParams.UrlEnum.DEPOSIT_NOTIFY_URL.getUrl();
        paramsMap.add("notifyUrl", notifyUrl);
        // ????????????????????????
        paramsMap.add("returnUrl", plat.getReturnUrl());
        //???????????????????????????????????????upi?????????bankCard/upi???
        Map<Integer, String> channelMap = Map.of(1, "bankCard", 6, "upi");
        paramsMap.add("payType", channelMap.get(coinDeposit.getCategory()));
          /*?????????md5(?????????+???????????????+????????????+??????????????????+????????????)???
        ?????????????????????????????????????????????????????????????????????????????????????????????????????????md5?????????*/
        var sign = DigestUtils.md5DigestAsHex((plat.getBusinessCode() + orderSn + df.format(amount) + notifyUrl + plat.getBusinessPwd()).getBytes());
        paramsMap.add("sign", sign);
        // ?????????????????????????????????
        var payUrl = plat.getUrl() + InPayParams.InPayUrlEnum.PAY.getUrl();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(paramsMap, headers);
        log.info("======InPay: sendUrl : " + payUrl);
        log.info("======InPay: sendParams : " + paramsMap);
        var call = restTemplate.postForEntity(payUrl, httpEntity, String.class);
        JSONObject jsonObject = parseObject(call.getBody());
        if (jsonObject.getInteger("code") == 200) {
            var url = parseObject(jsonObject.getString("data")).getString("payUrl");
            var category = 1;
            var method = "GET";
            return Map.of("method", method, "url", url.replace(plat.getPrivateKey(), plat.getUrl()), "category", category);
        } else {
            log.info("======InPay: Code???????????? : " + call);
            throw new BusinessException(CodeInfo.PAY_PLAT_LOAD_INVALID);
        }
    }
    /**
     * ??????????????????
     * @param dto ??????
     * @return
     */
    @Override
    public WithdrawalNotifyResDto onlineWithdraw(@NotNull PayParams.OnlinePayoutReqDto dto) {
        // ??????????????????ID
        CoinOnlineWithdrawal coinOnlineWithdrawal = coinOnlineWithdrawalServiceImpl.getOne(new QueryWrapper<CoinOnlineWithdrawal>().eq("order_id", dto.getOrderId()));
        if (null == coinOnlineWithdrawal) {
            throw new BusinessException(CodeInfo.PAY_PLAT_ORDER_ID_INVALID);
        }
        //  ???????????? ??????(???)
        BigDecimal amount = coinOnlineWithdrawal.getCoin();
        // ????????????
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        DecimalFormat df = new DecimalFormat("0.00");
        paramsMap.add(MERCHANT_NUM, plat.getBusinessCode());
        paramsMap.add(ORDER_NO, dto.getOrderId());
        paramsMap.add(AMOUNT, df.format(amount));
        // ????????????????????????
        var bankInfo = parseObject(coinOnlineWithdrawal.getBankInfo());
        var notifyUrl = plat.getNotifyUrl() + PayParams.UrlEnum.WITHDRAW_NOTIFY_URL.getUrl();
        paramsMap.add("notifyUrl", notifyUrl);
        Map<String, String> bankCardMap = Map.of(CHANNEL_CODE, "bankCard", ACCOUNT_HOLDER, ACCOUNT_HOLDER, BANK_CARD_ACCOUNT, BANK_CARD_ACCOUNT, OPEN_ACCOUNT_BANK, OPEN_ACCOUNT_BANK);
        Map<String, String> upiMap = Map.of(CHANNEL_CODE, "upi", ACCOUNT_HOLDER, "upiHolderName", BANK_CARD_ACCOUNT, "upiId");
        Map<Integer, Map<String, String>> channelMap = Map.of(1, bankCardMap, 6, upiMap);
        paramsMap.add(CHANNEL_CODE, channelMap.get(coinOnlineWithdrawal.getCategory()).get(CHANNEL_CODE));
        paramsMap.add(channelMap.get(coinOnlineWithdrawal.getCategory()).get(ACCOUNT_HOLDER), bankInfo.getString(ACCOUNT_HOLDER));
        paramsMap.add(channelMap.get(coinOnlineWithdrawal.getCategory()).get(BANK_CARD_ACCOUNT), bankInfo.getString(BANK_CARD_ACCOUNT));
        if (coinOnlineWithdrawal.getCategory() == 1 && bankInfo.getString(BANK_CARD_ACCOUNT) != null) {
            paramsMap.add(OPEN_ACCOUNT_BANK, bankInfo.getString(OPEN_ACCOUNT_BANK));
            paramsMap.add("ifsc", bankInfo.getJSONObject(MARK).getString("ifsc"));
        }
          /*?????????md5(?????????+???????????????+????????????+??????????????????+????????????)???
        ?????????????????????????????????????????????????????????????????????????????????????????????????????????md5?????????*/
        var sign = DigestUtils.md5DigestAsHex((plat.getBusinessCode() + dto.getOrderId() + df.format(amount) + notifyUrl + plat.getPublicKey()).getBytes());
        paramsMap.add("sign", sign);
        // ?????????????????????????????????
        String payoutUrl = plat.getWithdrawUrl() + InPayParams.InPayUrlEnum.TRANS.getUrl();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(paramsMap, headers);
        log.info("======InPay: sendUrl : " + payoutUrl);
        log.info("======InPay: sendParams : " + paramsMap);
        var call = restTemplate.postForEntity(payoutUrl, httpEntity, String.class);
        JSONObject jsonObject = parseObject(call.getBody());
        if (jsonObject.getInteger("code") == 200) {
            return WithdrawalNotifyResDto.builder().code(jsonObject.getInteger("code") == 200).msg(jsonObject.getString("msg")).build();
        } else {
            log.info("======InPay: ??????Code???????????? : " + call);
            throw new BusinessException(CodeInfo.PAY_PLAT_LOAD_INVALID);
        }
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
        log.info(String.format("=====InPay NotifyUrl Record : %s", dto.toString()));
        if (null == plat) PayAbstractFactory.init(MODEL);
        // ????????????????????????????????????md5(????????????+?????????+???????????????+????????????+????????????)???
        String signature = DigestUtils.md5DigestAsHex((dto.get("state") + dto.get(MERCHANT_NUM) + dto.get(ORDER_NO) + dto.get(AMOUNT)
                + plat.getBusinessPwd()).getBytes());

        // ??????????????????????????????
        if (!signature.equalsIgnoreCase(dto.get("sign")) || !dto.get("state").equals("1")) {
            log.info("=====InPay NotifyUrl : ?????? ??? Status ?????? ");
            return PayParams.STATUS.FAIL.getCode();
        }
        // ??????OrderId ????????????
        CoinDeposit coinDeposit = coinDepositServiceImpl.lambdaQuery()
                .eq(CoinDeposit::getOrderId, dto.get(ORDER_NO))
                .orderByDesc(CoinDeposit::getId)
                .one();
        if (null == coinDeposit) {
            log.info("=====InPay NotifyUrl : ??????????????????");
            return PayParams.STATUS.FAIL.getCode();
        } else if (coinDeposit.getStatus() != 0) {
            log.info("=====InPay NotifyUrl : ????????????");
            return PayParams.STATUS.FAIL.getCode();
        }

        try {
            // ????????????????????????
            thirdPayBase.updateCoinDeposit(coinDeposit.getId(), new BigDecimal(dto.get(AMOUNT)));
        } catch (Exception e) {
            log.info("=====InPay ?????????????????? :" + e.toString());
        }

        return PayParams.STATUS.SUCCESS.getCode();
    }

    /**
     * ????????????
     * @param dto
     * @return
     */
    @SneakyThrows
    public String withdrawNotifyUrl(@NotNull Map<String, String> dto) {
        // ?????????????????????
        log.info(String.format("=====InPay withdrawalNotifyUrl Record : %s", dto.toString()));
        if (null == plat) PayAbstractFactory.init(MODEL);
        String signature = DigestUtils.md5DigestAsHex((dto.get(MERCHANT_NUM) + dto.get(ORDER_NO) + dto.get("withdrawAmount")
                + plat.getBusinessPwd()).getBytes());

        // ??????????????????????????????
        if (!signature.equalsIgnoreCase(dto.get("sign"))) {
            log.info("=====InPay NotifyUrl : ?????? ?????? ");
            return PayParams.STATUS.FAIL.getCode();
        }
        // ??????OrderId ????????????
        CoinOnlineWithdrawal coinWithdrawal = coinOnlineWithdrawalServiceImpl.lambdaQuery()
                .eq(CoinOnlineWithdrawal::getOrderId, dto.get(ORDER_NO))
                .eq(CoinOnlineWithdrawal::getCategory, 0)
                .orderByDesc(CoinOnlineWithdrawal::getId)
                .one();

        if (null == coinWithdrawal) {
            log.info("=====" + MODEL + "NotifyUrl : ??????????????????");
            return PayParams.STATUS.FAIL.getCode();
        } else if (coinWithdrawal.getStatus() != 0) {
            log.info("=====" + MODEL + "NotifyUrl : ????????????");
            return PayParams.STATUS.SUCCESS.getCode();
        }

        if (dto.get("status").equals("1")) {
            coinOnlineWithdrawalServiceImpl.lambdaUpdate().set(CoinOnlineWithdrawal::getStatus, 1).eq(CoinOnlineWithdrawal::getOrderId, dto.get(ORDER_NO)).update();
            coinWithdrawalServiceImpl.lambdaUpdate().set(CoinWithdrawal::getStatus, 1).eq(CoinWithdrawal::getId, coinWithdrawal.getWithdrawalOrderId()).update();
            coinLogServiceImpl.lambdaUpdate().set(CoinLog::getStatus, 1)
                    .eq(CoinLog::getReferId, coinWithdrawal.getWithdrawalOrderId())
                    .eq(CoinLog::getCategory, 2)
                    .set(CoinLog::getUpdatedAt, DateUtils.getCurrentTime())
                    //'??????:0-????????? 1-?????? 2-??????'
                    .set(CoinLog::getStatus, 1)
                    .update();
            log.info("????????????????????????!");
            return PayParams.STATUS.SUCCESS.getCode();
        } else if (dto.get("status").equals("1")) {
            coinOnlineWithdrawalServiceImpl.lambdaUpdate().set(CoinOnlineWithdrawal::getStatus, 2).eq(CoinOnlineWithdrawal::getOrderId, dto.get(ORDER_NO)).update();
            return PayParams.STATUS.SUCCESS.getCode();
        } else {
            return PayParams.STATUS.FAIL.getCode();
        }
    }
}
