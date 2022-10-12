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
     * 创建支付
     * @param dto 入参
     * @return
     */
    @Override
    public Map<Object, Object> onlinePay(@NotNull OnlinePayReqDto dto) {
        // 获取当前订单ID
        CoinDeposit coinDeposit = coinDepositServiceImpl.getById(dto.getDepositId());
        if (null == coinDeposit) {
            throw new BusinessException(CodeInfo.PAY_PLAT_ORDER_ID_INVALID);
        }

        // 更新订单号
        int i = RandomUtils.nextInt(10000, 99999);
        String orderSn = "I" + DateNewUtils.getNowFormatTime(DateNewUtils.Format.yyyyMMddHHmmss) + i;
        boolean update = coinDepositServiceImpl.lambdaUpdate().eq(CoinDeposit::getId, dto.getDepositId()).set(CoinDeposit::getOrderId, orderSn).update();
        if (!update) {
            throw new BusinessException(CodeInfo.PAY_PLAT_UPDATE_ORDER_ID_INVALID);
        }
        //  计算金额 单位(分)
        BigDecimal amount = coinDeposit.getCoin();
        // 组装数据
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        DecimalFormat df = new DecimalFormat("0.00");
        paramsMap.add(MERCHANT_NUM, plat.getBusinessCode());
        paramsMap.add(ORDER_NO, orderSn);
        paramsMap.add(AMOUNT, df.format(amount));
        // 异步通知回调地址
        var notifyUrl = plat.getNotifyUrl() + PayParams.UrlEnum.DEPOSIT_NOTIFY_URL.getUrl();
        paramsMap.add("notifyUrl", notifyUrl);
        // 同步通知回调地址
        paramsMap.add("returnUrl", plat.getReturnUrl());
        //请求支付类型。支持银行卡，upi（可选bankCard/upi）
        Map<Integer, String> channelMap = Map.of(1, "bankCard", 6, "upi");
        paramsMap.add("payType", channelMap.get(coinDeposit.getCategory()));
          /*签名【md5(商户号+商户订单号+支付金额+异步通知地址+商户秘钥)】
        通过签名算法计算得出的签名值。（注意：支付金额的值需要转为字符串再进行md5加密）*/
        var sign = DigestUtils.md5DigestAsHex((plat.getBusinessCode() + orderSn + df.format(amount) + notifyUrl + plat.getBusinessPwd()).getBytes());
        paramsMap.add("sign", sign);
        // 网关、快捷下单接口地址
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
            log.info("======InPay: Code状态不对 : " + call);
            throw new BusinessException(CodeInfo.PAY_PLAT_LOAD_INVALID);
        }
    }
    /**
     * 创建代付订单
     * @param dto 入参
     * @return
     */
    @Override
    public WithdrawalNotifyResDto onlineWithdraw(@NotNull PayParams.OnlinePayoutReqDto dto) {
        // 获取当前订单ID
        CoinOnlineWithdrawal coinOnlineWithdrawal = coinOnlineWithdrawalServiceImpl.getOne(new QueryWrapper<CoinOnlineWithdrawal>().eq("order_id", dto.getOrderId()));
        if (null == coinOnlineWithdrawal) {
            throw new BusinessException(CodeInfo.PAY_PLAT_ORDER_ID_INVALID);
        }
        //  计算金额 单位(分)
        BigDecimal amount = coinOnlineWithdrawal.getCoin();
        // 组装数据
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        DecimalFormat df = new DecimalFormat("0.00");
        paramsMap.add(MERCHANT_NUM, plat.getBusinessCode());
        paramsMap.add(ORDER_NO, dto.getOrderId());
        paramsMap.add(AMOUNT, df.format(amount));
        // 异步通知回调地址
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
          /*签名【md5(商户号+商户订单号+支付金额+异步通知地址+商户秘钥)】
        通过签名算法计算得出的签名值。（注意：支付金额的值需要转为字符串再进行md5加密）*/
        var sign = DigestUtils.md5DigestAsHex((plat.getBusinessCode() + dto.getOrderId() + df.format(amount) + notifyUrl + plat.getPublicKey()).getBytes());
        paramsMap.add("sign", sign);
        // 网关、快捷下单接口地址
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
            log.info("======InPay: 代付Code状态不对 : " + call);
            throw new BusinessException(CodeInfo.PAY_PLAT_LOAD_INVALID);
        }
    }

    @Override
    public Boolean checkPaymentStatus(String orderId) {
        throw new UnsupportedOperationException();
    }


    /**
     * 异步回调接口
     *
     * @param dto 入参
     * @return success-成功 fail-失败
     */
    @SneakyThrows
    public String notifyUrl(@NotNull Map<String, String> dto) {
        // 记录回调的数据
        log.info(String.format("=====InPay NotifyUrl Record : %s", dto.toString()));
        if (null == plat) PayAbstractFactory.init(MODEL);
        // 判定加解密规则是否正确【md5(订单状态+商户号+商户订单号+支付金额+商户秘钥)】
        String signature = DigestUtils.md5DigestAsHex((dto.get("state") + dto.get(MERCHANT_NUM) + dto.get(ORDER_NO) + dto.get(AMOUNT)
                + plat.getBusinessPwd()).getBytes());

        // 判定签名规则是否正确
        if (!signature.equalsIgnoreCase(dto.get("sign")) || !dto.get("state").equals("1")) {
            log.info("=====InPay NotifyUrl : 校验 或 Status 失败 ");
            return PayParams.STATUS.FAIL.getCode();
        }
        // 判定OrderId 是否存在
        CoinDeposit coinDeposit = coinDepositServiceImpl.lambdaQuery()
                .eq(CoinDeposit::getOrderId, dto.get(ORDER_NO))
                .orderByDesc(CoinDeposit::getId)
                .one();
        if (null == coinDeposit) {
            log.info("=====InPay NotifyUrl : 订单号不存在");
            return PayParams.STATUS.FAIL.getCode();
        } else if (coinDeposit.getStatus() != 0) {
            log.info("=====InPay NotifyUrl : 重复回调");
            return PayParams.STATUS.FAIL.getCode();
        }

        try {
            // 正常业务处理流程
            thirdPayBase.updateCoinDeposit(coinDeposit.getId(), new BigDecimal(dto.get(AMOUNT)));
        } catch (Exception e) {
            log.info("=====InPay 更新数据失败 :" + e.toString());
        }

        return PayParams.STATUS.SUCCESS.getCode();
    }

    /**
     * 代付回调
     * @param dto
     * @return
     */
    @SneakyThrows
    public String withdrawNotifyUrl(@NotNull Map<String, String> dto) {
        // 记录回调的数据
        log.info(String.format("=====InPay withdrawalNotifyUrl Record : %s", dto.toString()));
        if (null == plat) PayAbstractFactory.init(MODEL);
        String signature = DigestUtils.md5DigestAsHex((dto.get(MERCHANT_NUM) + dto.get(ORDER_NO) + dto.get("withdrawAmount")
                + plat.getBusinessPwd()).getBytes());

        // 判定签名规则是否正确
        if (!signature.equalsIgnoreCase(dto.get("sign"))) {
            log.info("=====InPay NotifyUrl : 校验 失败 ");
            return PayParams.STATUS.FAIL.getCode();
        }
        // 判定OrderId 是否存在
        CoinOnlineWithdrawal coinWithdrawal = coinOnlineWithdrawalServiceImpl.lambdaQuery()
                .eq(CoinOnlineWithdrawal::getOrderId, dto.get(ORDER_NO))
                .eq(CoinOnlineWithdrawal::getCategory, 0)
                .orderByDesc(CoinOnlineWithdrawal::getId)
                .one();

        if (null == coinWithdrawal) {
            log.info("=====" + MODEL + "NotifyUrl : 订单号不存在");
            return PayParams.STATUS.FAIL.getCode();
        } else if (coinWithdrawal.getStatus() != 0) {
            log.info("=====" + MODEL + "NotifyUrl : 重复回调");
            return PayParams.STATUS.SUCCESS.getCode();
        }

        if (dto.get("status").equals("1")) {
            coinOnlineWithdrawalServiceImpl.lambdaUpdate().set(CoinOnlineWithdrawal::getStatus, 1).eq(CoinOnlineWithdrawal::getOrderId, dto.get(ORDER_NO)).update();
            coinWithdrawalServiceImpl.lambdaUpdate().set(CoinWithdrawal::getStatus, 1).eq(CoinWithdrawal::getId, coinWithdrawal.getWithdrawalOrderId()).update();
            coinLogServiceImpl.lambdaUpdate().set(CoinLog::getStatus, 1)
                    .eq(CoinLog::getReferId, coinWithdrawal.getWithdrawalOrderId())
                    .eq(CoinLog::getCategory, 2)
                    .set(CoinLog::getUpdatedAt, DateUtils.getCurrentTime())
                    //'状态:0-处理中 1-成功 2-失败'
                    .set(CoinLog::getStatus, 1)
                    .update();
            log.info("修改日志状态成功!");
            return PayParams.STATUS.SUCCESS.getCode();
        } else if (dto.get("status").equals("1")) {
            coinOnlineWithdrawalServiceImpl.lambdaUpdate().set(CoinOnlineWithdrawal::getStatus, 2).eq(CoinOnlineWithdrawal::getOrderId, dto.get(ORDER_NO)).update();
            return PayParams.STATUS.SUCCESS.getCode();
        } else {
            return PayParams.STATUS.FAIL.getCode();
        }
    }
}
