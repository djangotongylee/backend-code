package com.xinbo.sports.payment.utils;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xinbo.sports.dao.generator.po.CoinDeposit;
import com.xinbo.sports.dao.generator.po.CoinOnlineWithdrawal;
import com.xinbo.sports.dao.generator.service.CoinDepositService;
import com.xinbo.sports.dao.generator.service.CoinOnlineWithdrawalService;
import com.xinbo.sports.payment.base.ThirdPayBase;
import com.xinbo.sports.payment.io.PayParams;
import com.xinbo.sports.payment.io.PayParams.OnlinePayReqDto;
import com.xinbo.sports.payment.io.PayParams.OnlinePayoutReqDto;
import com.xinbo.sports.payment.io.PayParams.UrlEnum;
import com.xinbo.sports.payment.io.Zf777PayParams;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.DateUtils;
import com.xinbo.sports.utils.TextUtils;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.alibaba.fastjson.JSON.toJSONString;
import static com.xinbo.sports.payment.io.PayParams.*;
import static com.xinbo.sports.payment.io.Zf777PayParams.Zf777PayUrlEnum.FORM;

/**
 * @author: David
 * @date: 14/07/2020
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class Zf777PayUtil implements PayAbstractFactory {
    private final ThirdPayBase thirdPayBase;
    private final CoinDepositService coinDepositServiceImpl;
    private final CoinOnlineWithdrawalService coinOnlineWithdrawalServiceImpl;
    private static final String AMOUNT = "amount";
    private static final String MODEL = "Zf777PayUtil";
    @Setter
    Zf777PayParams.PayPlat plat;

    /**
     * 创建支付
     *
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
        HashMap<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("userid", plat.getBusinessCode());
        paramsMap.put("orderid", orderSn);
        paramsMap.put(AMOUNT, amount.intValue());
        paramsMap.put("type", "paytm");
        if (coinDeposit.getCategory() == 6) paramsMap.put("payload", "{useupi:1}");
        paramsMap.put("returnurl", plat.getReturnUrl());
        paramsMap.put("note", "111222");
        paramsMap.put("ordertype", "1");
        // 异步通知回调地址
        paramsMap.put("notifyurl", plat.getNotifyUrl() + UrlEnum.DEPOSIT_NOTIFY_URL.getUrl());
        //签名算法签名生成sign = md5(token + orderid + amount.toString()).toLowercase())
        var sign = DigestUtils.md5DigestAsHex((plat.getBusinessPwd() + orderSn + paramsMap.get(AMOUNT).toString()).getBytes()).toLowerCase();
        paramsMap.put("sign", sign);
        // 网关、快捷下单接口地址
        var url = plat.getUrl().replace("api", "form") + FORM.getUrl();
        return Map.of("method", "POST", "url", TextUtils.renderForm(url, paramsMap), "category", 4);
    }

    @Override
    public PayParams.WithdrawalNotifyResDto onlineWithdraw(OnlinePayoutReqDto dto) {
        // 获取当前订单ID
        CoinOnlineWithdrawal coinOnlineWithdrawal = coinOnlineWithdrawalServiceImpl.getOne(new QueryWrapper<CoinOnlineWithdrawal>().eq("order_id", dto.getOrderId()));
        if (null == coinOnlineWithdrawal) throw new BusinessException(CodeInfo.PAY_PLAT_ORDER_ID_INVALID);
        //  计算金额 单位(分)
        BigDecimal amount = coinOnlineWithdrawal.getCoin().multiply(BigDecimal.valueOf(100));
        var bankInfo = parseObject(coinOnlineWithdrawal.getBankInfo());
        // 组装数据
        HashMap<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("userid", plat.getBusinessCode());
        paramsMap.put("orderid", coinOnlineWithdrawal.getOrderId());
        paramsMap.put(AMOUNT, amount.intValue());
        paramsMap.put("type", "paytm");
        paramsMap.put("returnurl", plat.getReturnUrl());
        paramsMap.put("ordertype", "2");
        // 异步通知回调地址
        paramsMap.put("notifyurl", plat.getNotifyUrl() + UrlEnum.WITHDRAW_NOTIFY_URL.getUrl());
        var jsonObject = new JSONObject();
        jsonObject.put("cardname", bankInfo.getString(ACCOUNT_HOLDER));
        jsonObject.put("cardno", bankInfo.getString(BANK_CARD_ACCOUNT));
        jsonObject.put("bankid", bankInfo.getString(BANK_CODE));
        jsonObject.put("bankname", bankInfo.getString(OPEN_ACCOUNT_BANK));
        jsonObject.put("province", "aa");
        jsonObject.put("city", "bb");
        jsonObject.put("branchname", "cc");
        paramsMap.put("payload", toJSONString(jsonObject));
        //签名算法签名生成sign = md5(token + orderid + amount.toString()).toLowercase())
        var sign = DigestUtils.md5DigestAsHex((plat.getBusinessPwd() + coinOnlineWithdrawal.getOrderId() + paramsMap.get(AMOUNT).toString()).getBytes()).toLowerCase();
        paramsMap.put("sign", sign);
        // 网关、快捷下单接口地址
        var url = plat.getUrl().replace("api", "form") + FORM.getUrl();
        log.info("======777Pay: sendUrl : " + url);
        log.info("======777Pay: sendParams : " + paramsMap);
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
        log.info(String.format("=====" + MODEL + " NotifyUrl Record : %s", dto.toString()));
        var path = Paths.get(MODEL + "-" + DateUtils.yyyyMMdd(DateNewUtils.now()) + ".txt");
        if (!Files.exists(path)) Files.createFile(path);
        Files.write(path, dto.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        if (null == plat) PayAbstractFactory.init(MODEL);
        // 判定签名规则是否正确
        var sign = DigestUtils.md5DigestAsHex((plat.getBusinessPwd() + dto.get("orderid") + dto.get("qrurl")).getBytes()).toLowerCase();
        if (sign.equalsIgnoreCase(dto.get("sign")) && dto.get("success").equals("1")) {
            log.info("=====" + MODEL + " NotifyUrl : 接口2回调已收到 ");
            Files.write(path, PayParams.STATUS.SUCCESS.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            return PayParams.STATUS.SUCCESS.getCode();
        }
        //字符串类型 规则：
        //sign=md5(token + orderid + payamount.toString()).toLowercase())
        var signature = DigestUtils.md5DigestAsHex((plat.getBusinessPwd() + dto.get("orderid") + dto.get("payamount")).getBytes()).toLowerCase();
        if (!signature.equalsIgnoreCase(dto.get("sign")) && !dto.get("success").equals("1")) {
            log.info("=====" + MODEL + " NotifyUrl : 校验 或 status失败 ");
            Files.write(path, PayParams.STATUS.FAIL.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            return PayParams.STATUS.FAIL.getCode();
        }
        // 判定OrderId 是否存在
        CoinDeposit coinDeposit = coinDepositServiceImpl.lambdaQuery()
                .eq(CoinDeposit::getOrderId, dto.get("orderid"))
                .orderByDesc(CoinDeposit::getId)
                .one();
        if (null == coinDeposit) {
            log.info("=====" + MODEL + " NotifyUrl : 订单号不存在");
            Files.write(path, PayParams.STATUS.FAIL.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            return PayParams.STATUS.FAIL.getCode();
        } else if (coinDeposit.getStatus() != 0) {
            Files.write(path, PayParams.STATUS.FAIL.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            log.info("=====" + MODEL + " NotifyUrl : 重复回调");
            return PayParams.STATUS.FAIL.getCode();
        }

        try {
            if (1 == Integer.parseInt(dto.get("ispay"))) {
                // 正常业务处理流程
                thirdPayBase.updateCoinDeposit(coinDeposit.getId(), new BigDecimal(dto.get("payamount")));
            }

        } catch (Exception e) {
            Files.write(path, PayParams.STATUS.FAIL.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            log.info("=====" + MODEL + "  更新数据失败 :" + e.toString());
        }
        Files.write(path, PayParams.STATUS.SUCCESS.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        return PayParams.STATUS.SUCCESS.getCode();
    }


    public String withdrawNotifyUrl(Map<String, String> dto) {
        return PayParams.STATUS.SUCCESS.getCode();
    }
}
