package com.xinbo.sports.payment.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xinbo.sports.dao.generator.po.CoinDeposit;
import com.xinbo.sports.dao.generator.po.CoinOnlineWithdrawal;
import com.xinbo.sports.dao.generator.po.UserBank;
import com.xinbo.sports.dao.generator.service.CoinDepositService;
import com.xinbo.sports.dao.generator.service.CoinOnlineWithdrawalService;
import com.xinbo.sports.dao.generator.service.CoinWithdrawalService;
import com.xinbo.sports.dao.generator.service.UserBankService;
import com.xinbo.sports.payment.base.ThirdPayBase;
import com.xinbo.sports.payment.io.PayParams;
import com.xinbo.sports.payment.io.PayParams.*;
import com.xinbo.sports.payment.io.YaarPayParams;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.DateUtils;
import com.xinbo.sports.utils.TextUtils;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.xinbo.sports.payment.io.PayParams.*;
import static com.xinbo.sports.utils.MD5.signForInspiry;

/**
 * @author: David
 * @date: 14/07/2020
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class YaarPayUtil implements PayAbstractFactory {
    private final ThirdPayBase thirdPayBase;
    private final CoinDepositService coinDepositServiceImpl;
    private final CoinWithdrawalService coinWithdrawalServiceImpl;
    private final CoinOnlineWithdrawalService coinOnlineWithdrawalServiceImpl;
    private final ConfigCache configCache;
    private final UserBankService userBankServiceImpl;
    private static final String MODEL = "YaarPayUti";
    @Setter
    public YaarPayParams.PayPlat plat;

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
        BigDecimal amount = coinDeposit.getCoin().multiply(BigDecimal.valueOf(100));
        // 组装数据
        SortedMap<String, String> paramsMap = new TreeMap<>();
        paramsMap.put("amount", amount.toBigInteger().toString());
        paramsMap.put("appId", plat.getPublicKey());
        Map<String, String> chnnelIdMap = Map.of("YaarPay OnlineBank", "8035", "YaarPay P2A Transfer", "8036");
        var channelId = chnnelIdMap.get(coinDeposit.getTitle());
        paramsMap.put("channelId", channelId);
        UserBank uid = userBankServiceImpl.getOne(new QueryWrapper<UserBank>().eq("uid", coinDeposit.getUid()).eq("status", 1));
        if (channelId.equals("8036")) {
            if (uid != null && uid.getAccountName() != null) {
                paramsMap.put("depositName", uid.getAccountName());
            } else {
                paramsMap.put("depositName", "unknown");
            }
        }

        var currency = EnumUtils.getEnumIgnoreCase(YaarPayParams.CURRENCY.class, configCache.getCountry()).getCode();
        paramsMap.put("currency", currency);
        paramsMap.put("mchId", plat.getBusinessCode());
        paramsMap.put("mchOrderNo", orderSn);
        // 异步通知回调地址
        paramsMap.put("notifyUrl", plat.getNotifyUrl() + PayParams.UrlEnum.DEPOSIT_NOTIFY_URL.getUrl());
        // 同步通知回调地址
        paramsMap.put("returnUrl", plat.getReturnUrl());
        paramsMap.put("version", "1.0");
        paramsMap.put("sign", signForInspiry(paramsMap, plat.getBusinessPwd()));
        return Map.of("method", "POST", "url", TextUtils.renderForm(plat.getUrl() + YaarPayParams.YaarPayMethodEnum.PAY_IN.getUrl(), new HashMap<>(paramsMap)), "category", 4);
    }

    /**
     * 创建代付订单
     * @param dto 入参
     * @return
     */
    @Override
    public WithdrawalNotifyResDto onlineWithdraw(PayParams.OnlinePayoutReqDto dto) {
        // 获取当前订单ID
        CoinOnlineWithdrawal coinOnlineWithdrawal = coinOnlineWithdrawalServiceImpl.getOne(new QueryWrapper<CoinOnlineWithdrawal>().eq("order_id", dto.getOrderId()));
        if (null == coinOnlineWithdrawal) {
            throw new BusinessException(CodeInfo.PAY_PLAT_ORDER_ID_INVALID);
        }

        //  计算金额 单位(分)
        BigDecimal amount = coinOnlineWithdrawal.getCoin().multiply(BigDecimal.valueOf(100));
        // 组装数据
        SortedMap<String, String> paramsMap = new TreeMap<>();
        var bankInfo = parseObject(coinOnlineWithdrawal.getBankInfo());
        paramsMap.put("accountName", bankInfo.getString(ACCOUNT_HOLDER));
        paramsMap.put("accountNo", bankInfo.getString(BANK_CARD_ACCOUNT));
        paramsMap.put("amount", amount.toBigInteger().toString());
        paramsMap.put("mchId", plat.getBusinessCode());
        paramsMap.put("mchOrderNo", dto.getOrderId());
        paramsMap.put("notifyUrl", plat.getNotifyUrl() + PayParams.UrlEnum.WITHDRAW_NOTIFY_URL.getUrl());
        paramsMap.put("payoutBankCode", bankInfo.getString(OPEN_ACCOUNT_BANK));
        paramsMap.put("reqTime", String.valueOf(DateNewUtils.now()));
        paramsMap.put("ifscCode", bankInfo.getJSONObject(MARK).getString("ifsc"));
        paramsMap.put("sign", signForInspiry(paramsMap, plat.getBusinessPwd()));

        // 网关、快捷下单接口地址
        var payoutUrl = plat.getUrl() + YaarPayParams.YaarPayMethodEnum.AGENT_PAY.getUrl();
        log.info("======YaarPay: sendUrl : " + payoutUrl);
        log.info("======YaarPay: sendParams : " + paramsMap);
        return null;
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
        log.info(String.format("=====YaarPay NotifyUrl Record : %s", dto.toString()));
        var path = Paths.get("YaarPay-" + DateUtils.yyyyMMdd(DateNewUtils.now()) + ".txt");
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
        if (null == plat) PayAbstractFactory.init(MODEL);
        Files.write(path, dto.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        SortedMap<String, String> paramsMap = new TreeMap<>();
        paramsMap.put("amount", String.valueOf(dto.get("amount")));
        paramsMap.put("completedTime", dto.get("completedTime"));
        paramsMap.put("currency", dto.get("currency"));
        paramsMap.put("mchOrderNo", dto.get("mchOrderNo"));
        paramsMap.put("orderTime", dto.get("orderTime"));
        paramsMap.put("depositBankCode", dto.get("depositBankCode"));
        paramsMap.put("payOrderId", dto.get("payOrderId"));
        paramsMap.put("status", dto.get("status"));

        var signature = signForInspiry(paramsMap, plat.getBusinessPwd());
        // 判定签名规则是否正确
        if (!signature.equalsIgnoreCase(dto.get("sign")) || !dto.get("status").equals("2")) {
            log.info("=====YaarPay NotifyUrl : 校验 或 Status 失败 ");
            Files.write(path, "=====YaarPay NotifyUrl : 校验 或 Status 失败".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            return PayParams.STATUS.FAIL.getCode();
        }
        // 判定OrderId 是否存在
        CoinDeposit coinDeposit = coinDepositServiceImpl.lambdaQuery()
                .eq(CoinDeposit::getOrderId, dto.get("mchOrderNo"))
                .orderByDesc(CoinDeposit::getId)
                .one();
        if (null == coinDeposit) {
            log.info("=====YaarPay NotifyUrl : 订单号不存在");
            Files.write(path, "======YaarPay NotifyUrl : 订单号不存在".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            return PayParams.STATUS.FAIL.getCode();
        } else if (coinDeposit.getStatus() != 0) {
            log.info("=====YaarPay NotifyUrl : 重复回调");
            Files.write(path, "======YaarPay NotifyUrl : 重复回调".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            return PayParams.STATUS.FAIL.getCode();
        }

        try {
            // 正常业务处理流程
            thirdPayBase.updateCoinDeposit(coinDeposit.getId(), BigDecimal.valueOf(Long.valueOf(dto.get("amount")) / 100));
        } catch (Exception e) {
            log.info("=====YaarPay 更新数据失败 :" + e.toString());
            Files.write(path, "======YaarPay NotifyUrl : 重复回调".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        }
        Files.write(path, PayParams.STATUS.SUCCESS.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        return PayParams.STATUS.SUCCESS.getCode();
    }


    public String withdrawNotifyUrl(Map<String, String> dto) {
        return PayParams.STATUS.SUCCESS.getCode();

    }
}
