package com.xinbo.sports.payment.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.ImmutableMap;
import com.xinbo.sports.dao.generator.po.CoinDeposit;
import com.xinbo.sports.dao.generator.po.CoinOnlineWithdrawal;
import com.xinbo.sports.dao.generator.po.CoinWithdrawal;
import com.xinbo.sports.dao.generator.service.CoinDepositService;
import com.xinbo.sports.dao.generator.service.CoinOnlineWithdrawalService;
import com.xinbo.sports.dao.generator.service.CoinWithdrawalService;
import com.xinbo.sports.payment.base.ThirdPayBase;
import com.xinbo.sports.payment.io.*;
import com.xinbo.sports.payment.io.PayParams.OnlinePayReqDto;
import com.xinbo.sports.payment.io.PayParams.OnlinePayoutReqDto;
import com.xinbo.sports.payment.io.PayParams.UrlEnum;
import com.xinbo.sports.service.base.UpdateUserCoinBase;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.DateUtils;
import com.xinbo.sports.utils.HttpUtils;
import com.xinbo.sports.utils.components.response.CodeInfo;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;

import java.util.HashMap;
import java.util.Map;

import static com.alibaba.fastjson.JSON.*;
import static com.xinbo.sports.payment.io.PayParams.*;
import static com.xinbo.sports.payment.io.PayParams.OPEN_ACCOUNT_BANK;
import static com.xinbo.sports.payment.io.ShineUPayParams.ShineUPayUrlEnum.WITHDRAW_QUERY;
import static java.util.Objects.nonNull;

/**
 * @author: David
 * @date: 14/07/2020
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ShineUPayUtil implements PayAbstractFactory {
    private final ThirdPayBase thirdPayBase;
    private final CoinDepositService coinDepositServiceImpl;
    private final CoinOnlineWithdrawalService coinOnlineWithdrawalServiceImpl;
    private final CoinWithdrawalService coinWithdrawalServiceImpl;
    private final UpdateUserCoinBase updateUserCoinBase;
    private static final String AMOUNT = "amount";
    private static final String MODEL = "ShineUPayUtil";
    private static final String ORDER_ID = "orderId";
    @Setter
    ShineUPayParams.PayPlat plat;

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
        DecimalFormat df = new DecimalFormat("0.00");
        // 组装数据
        var params = new JSONObject();
        params.put("userId", coinDeposit.getUsername());
        params.put(AMOUNT, new BigDecimal(df.format(amount)));
        params.put(ORDER_ID, orderSn);
        params.put("details", "paytm");
        params.put("payTypeId", "");
        params.put("redirectUrl", "");
        // 异步通知回调地址
        params.put("notifyUrl", plat.getNotifyUrl() + UrlEnum.DEPOSIT_NOTIFY_URL.getUrl());
        var jsonObject = new JSONObject();
        jsonObject.put("merchantId", plat.getBusinessCode());
        jsonObject.put("timestamp", String.valueOf(System.currentTimeMillis()));
        jsonObject.put("body", params);
        //签名:请求报文的完整内容，加上字符串“|”，再加上商户密钥，形成的新文本串进行MD5，最后全小写化。
        var sign = DigestUtils.md5DigestAsHex((jsonObject + "|" + plat.getBusinessPwd()).getBytes()).toLowerCase();
        String payUrl = plat.getUrl() + ShineUPayParams.ShineUPayUrlEnum.PAY.getUrl();
        String url = null;
        try {
            Map<String, String> header = new ImmutableMap.Builder<String, String>().
                    put("Content-Type", "application/json").
                    put("charset", "UTF-8").
                    put("Api-Sign", sign).
                    build();
            JSONObject call = parseObject(HttpUtils.doPost(payUrl, jsonObject, header, MODEL));
            if (call.getInteger("status") == 0) {
                url = parseObject(call.getString("body")).getString("content");
            } else {
                log.info("======" + MODEL + ": 支付类型错误\n");
                log.info("======" + MODEL + ": result = " + jsonObject.toJSONString());
                throw new BusinessException(CodeInfo.STATUS_CODE_403_6);
            }
        } catch (Exception e) {
            log.error(e.toString());
            log.error("===========" + MODEL + " URL: " + payUrl + "\n");
            log.error("===========" + MODEL + " postData: " + jsonObject + "\n");
        }
        return Map.of("method", "GET", "url", url, "category", 1);
    }

    /**
     * 代付订单创建
     *
     * @param dto 入参
     * @return
     */
    @Override
    public PayParams.WithdrawalNotifyResDto onlineWithdraw(OnlinePayoutReqDto dto) {
        // 获取当前订单ID
        CoinOnlineWithdrawal coinOnlineWithdrawal = coinOnlineWithdrawalServiceImpl.getOne(new QueryWrapper<CoinOnlineWithdrawal>().eq("order_id", dto.getOrderId()));
        if (null == coinOnlineWithdrawal) throw new BusinessException(CodeInfo.PAY_PLAT_ORDER_ID_INVALID);
        //  计算金额 单位(分)
        var bankInfo = parseObject(coinOnlineWithdrawal.getBankInfo());
        BigDecimal amount = coinOnlineWithdrawal.getCoin();
        DecimalFormat df = new DecimalFormat("0.00");
        BigDecimal realAmount = new BigDecimal(df.format(amount));
        // 组装数据
        var params = new JSONObject();
        params.put("advPasswordMd5", DigestUtils.md5DigestAsHex(plat.getPublicKey().getBytes()).toLowerCase());
        params.put(ORDER_ID, dto.getOrderId());
        params.put("flag", 0);
        params.put("bankCode", bankInfo.getString(BANK_CARD_ACCOUNT));
        params.put("bankUser", bankInfo.getString(ACCOUNT_HOLDER));
        // 异步通知回调地址
        params.put("bankUserPhone", "test");
        params.put("bankProvinceName", "");
        params.put("bankCityName", "");
        params.put("bankAddress", dto.getOpenAccountBank());
        params.put("bankUserEmail", "test");
        params.put("bankUserIFSC", bankInfo.getString(MARK));
        params.put(AMOUNT, realAmount);
        params.put("realAmount", realAmount);
        params.put("details", "");
        params.put("notifyUrl", plat.getNotifyUrl() + UrlEnum.WITHDRAW_NOTIFY_URL.getUrl());
        var jsonObject = new JSONObject();
        jsonObject.put("merchantId", plat.getBusinessCode());
        jsonObject.put("timestamp", String.valueOf(System.currentTimeMillis()));
        jsonObject.put("body", params);
        //签名:请求报文的完整内容，加上字符串“|”，再加上商户密钥，形成的新文本串进行MD5，最后全小写化。
        var sign = DigestUtils.md5DigestAsHex((jsonObject + "|" + plat.getBusinessPwd()).getBytes()).toLowerCase();
        // 网关、快捷下单接口地址
        String payoutUrl = plat.getUrl() + ShineUPayParams.ShineUPayUrlEnum.WITHDRAW.getUrl();

        try {
            Map<String, String> header = new ImmutableMap.Builder<String, String>().
                    put("Content-Type", "application/json")
                    .put("charset", "UTF-8")
                    .put("Api-Sign", sign)
                    .build();
            log.info(MODEL + "-->sigh=" + sign);
            String call = HttpUtils.doPost(payoutUrl, jsonObject, header, MODEL);
            log.info(MODEL + "-->response=" + sign);
            JSONObject result = parseObject(call);
            if (result.getInteger("status") == 0) {
                coinOnlineWithdrawalServiceImpl.lambdaUpdate().set(nonNull(result.getJSONObject("body").getString("platformOrderId")), CoinOnlineWithdrawal::getMark, result.getJSONObject("body").getString("platformOrderId"))
                        .eq(CoinOnlineWithdrawal::getOrderId, dto.getOrderId()).update();
                return WithdrawalNotifyResDto.builder().code(result.getInteger("status") == 0).msg(result.getString("message")).build();
            }
            coinOnlineWithdrawalServiceImpl.remove(new QueryWrapper<CoinOnlineWithdrawal>().eq("order_id", dto.getOrderId()));
            CodeInfo.PAY_PLAT_ADD_ORDER_FAILURE.setMsg(result.getString("message"));
            throw new BusinessException(CodeInfo.PAY_PLAT_ADD_ORDER_FAILURE);
        } catch (Exception e) {
            log.error(e.toString());
            log.error("=============================================");
            log.error("Class: " + MODEL + "\n");
            log.error("URL: " + payoutUrl + "\n");
            log.error("postData: " + JSON.toJSONString(jsonObject) + "\n");
            throw new BusinessException(CodeInfo.PAY_PLAT_ADD_ORDER_FAILURE);
        }
    }

    @Override
    public Boolean checkPaymentStatus(String orderId) {
        var jsonObject = new JSONObject();
        var url = plat.getUrl() + WITHDRAW_QUERY.getUrl();
        try {
            CoinOnlineWithdrawal one = coinOnlineWithdrawalServiceImpl.lambdaQuery().eq(CoinOnlineWithdrawal::getOrderId, orderId).one();
            if (null != one) {
                CoinWithdrawal coinWithdrawal = coinWithdrawalServiceImpl.lambdaQuery().eq(CoinWithdrawal::getStatus, 3).eq(CoinWithdrawal::getId, one.getWithdrawalOrderId()).one();
                if (null == coinWithdrawal) {
                    XxlJobLogger.log(MODEL + "当前已稽核订单可处理");
                    return true;
                }
                var params = new JSONObject();
                params.put("orderId", orderId);
                jsonObject.put("merchantId", plat.getBusinessCode());
                jsonObject.put("timestamp", String.valueOf(System.currentTimeMillis()));
                jsonObject.put("body", params);
                //签名:请求报文的完整内容，加上字符串“|”，再加上商户密钥，形成的新文本串进行MD5，最后全小写化。
                var sign = DigestUtils.md5DigestAsHex((jsonObject + "|" + plat.getBusinessPwd()).getBytes()).toLowerCase();
                // 网关、快捷下单接口地址
                Map<String, String> header = new ImmutableMap.Builder<String, String>().
                        put("Content-Type", "application/json")
                        .put("charset", "UTF-8")
                        .put("Api-Sign", sign)
                        .build();
                String call = HttpUtils.doPost(url, jsonObject, header, MODEL);
                JSONObject result = JSON.parseObject(call);
                if (result.getInteger("status") == 0) {
                    Integer status = result.getJSONObject("body").getInteger("status");
                    if (status == 1) {
                        coinOnlineWithdrawalServiceImpl.lambdaUpdate().set(CoinOnlineWithdrawal::getStatus, 1).eq(CoinOnlineWithdrawal::getOrderId, orderId).update();
                        coinWithdrawalServiceImpl.lambdaUpdate().set(CoinWithdrawal::getStatus, 1).eq(CoinWithdrawal::getId, one.getWithdrawalOrderId()).update();
                        //'状态:0-处理中 1-成功 2-失败'
                        updateUserCoinBase.updateCoinLog(Long.valueOf(one.getWithdrawalOrderId()), 2, 1, DateUtils.getCurrentTime());
                    } else if (status == 2) {
                        coinOnlineWithdrawalServiceImpl.lambdaUpdate().set(CoinOnlineWithdrawal::getStatus, 2).eq(CoinOnlineWithdrawal::getOrderId, orderId).update();
                    }
                    return true;
                } else {
                    XxlJobLogger.log(MODEL + result.getString("message"));
                    return false;
                }
            }
        } catch (Exception e) {
            log.error(e.toString());
            log.error("=============================================");
            log.error("Class: " + "MangoPayUtil" + "\n");
            log.error("URL: " + url + "\n");
            log.error("postData: " + jsonObject + "\n");
            e.printStackTrace();
            throw new BusinessException(CodeInfo.PAY_PLAT_UPDATE_ORDER_FAILURE);
        }
        XxlJobLogger.log(MODEL + "当前无订单可处理");
        return true;
    }

    /**
     * 异步回调接口
     *
     * @param request 入参
     * @param request
     * @return success-成功 fail-失败
     */
    @SneakyThrows
    public String notifyUrl(HttpServletRequest request) {
        // 记录回调的数据
        log.info(String.format("=====" + MODEL + " NotifyUrl Record : %s", request.toString()));
        var path = Paths.get(MODEL + "-" + DateUtils.yyyyMMdd(DateNewUtils.now()) + ".txt");
        if (!Files.exists(path)) Files.createFile(path);
        Files.write(path, request.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        // 判定签名规则是否正确
        BufferedReader br = request.getReader();
        String str = "";
        String wholeStr = "";
        while ((str = br.readLine()) != null) {
            wholeStr += str;
        }
        if (null == plat) PayAbstractFactory.init(MODEL);
        var sign = DigestUtils.md5DigestAsHex((wholeStr + "|" + plat.getBusinessPwd()).getBytes()).toLowerCase();
        JSONObject body = parseObject(wholeStr).getJSONObject("body");
        if (!sign.equalsIgnoreCase(request.getHeader("Api-Sign")) || body.getInteger("status") != 1) {
            log.info("=====" + MODEL + " NotifyUrl : 校验 或 status失败 ");
            Files.write(path, PayParams.STATUS.FAIL.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            return PayParams.STATUS.FAIL.getCode();
        }
        // 判定OrderId 是否存在
        CoinDeposit coinDeposit = coinDepositServiceImpl.lambdaQuery()
                .eq(CoinDeposit::getOrderId, body.getString(ORDER_ID))
                .orderByDesc(CoinDeposit::getId)
                .one();
        if (null == coinDeposit) {
            log.info("=====" + MODEL + " NotifyUrl : 订单号不存在");
            Files.write(path, PayParams.STATUS.FAIL.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            return PayParams.STATUS.FAIL.getCode();
        } else if (coinDeposit.getStatus() != 0) {
            Files.write(path, PayParams.STATUS.FAIL.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            log.info("=====" + MODEL + " NotifyUrl : 重复回调");
            return PayParams.STATUS.SUCCESS.getCode();
        }

        try {
            // 正常业务处理流程
            thirdPayBase.updateCoinDeposit(coinDeposit.getId(), body.getBigDecimal(AMOUNT));
        } catch (Exception e) {
            Files.write(path, PayParams.STATUS.FAIL.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            log.info("=====" + MODEL + "  更新数据失败 :" + e.toString());
        }
        Files.write(path, PayParams.STATUS.SUCCESS.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        return PayParams.STATUS.SUCCESS.getCode();
    }

    /**
     * 代付订单回调（重复订单仍返回成功）
     *
     * @param request
     * @return
     */
    @SneakyThrows
    public String withdrawNotifyUrl(HttpServletRequest request) {
        // 记录回调的数据
        var path = Paths.get(MODEL + "-" + DateUtils.yyyyMMdd(DateNewUtils.now()) + ".txt");
        if (!Files.exists(path)) Files.createFile(path);
        Files.write(path, request.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        // 判定签名规则是否正确
        BufferedReader br = request.getReader();
        String str = "";
        String wholeStr = "";
        while ((str = br.readLine()) != null) {
            wholeStr += str;
        }
        if (null == plat) PayAbstractFactory.init(MODEL);
        log.info(String.format("=====" + MODEL + " withdrawNotifyUrl Record : %s", wholeStr));
        log.info(String.format("=====" + MODEL + " withdrawNotifyUrl api-sign : %s", request.getHeader("Api-Sign")));
        var sign = DigestUtils.md5DigestAsHex((wholeStr + "|" + plat.getBusinessPwd()).getBytes()).toLowerCase();
        JSONObject body = parseObject(wholeStr).getJSONObject("body");
        if (!sign.equalsIgnoreCase(request.getHeader("Api-Sign")) || 0 != parseObject(wholeStr).getInteger("status")) {
            log.info("=====" + MODEL + " NotifyUrl : 校验 或 status失败 ");
            Files.write(path, PayParams.STATUS.FAIL.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            return PayParams.STATUS.FAIL.getCode();
        }
        // 判定OrderId 是否存在
        CoinOnlineWithdrawal coinOnlineWithdrawal = coinOnlineWithdrawalServiceImpl.lambdaQuery()
                .eq(CoinOnlineWithdrawal::getOrderId, body.getString(ORDER_ID))
                .orderByDesc(CoinOnlineWithdrawal::getId)
                .one();
        if (null == coinOnlineWithdrawal) {
            log.info("=====" + MODEL + "NotifyUrl : 订单号不存在");
            return PayParams.STATUS.FAIL.getCode();
        } else if (coinOnlineWithdrawal.getStatus() != 0) {
            log.info("=====" + MODEL + "NotifyUrl : 重复回调");
            return PayParams.STATUS.SUCCESS.getCode();
        }

        try {
            if (body.getInteger("status") == 1) {
                coinOnlineWithdrawalServiceImpl.lambdaUpdate().set(CoinOnlineWithdrawal::getStatus, 1).eq(CoinOnlineWithdrawal::getOrderId, coinOnlineWithdrawal.getOrderId()).update();
                coinWithdrawalServiceImpl.lambdaUpdate().set(CoinWithdrawal::getStatus, 1).eq(CoinWithdrawal::getId, coinOnlineWithdrawal.getWithdrawalOrderId()).update();
                updateUserCoinBase.updateCoinLog(Long.valueOf(coinOnlineWithdrawal.getWithdrawalOrderId()), 2, 1, DateUtils.getCurrentTime());
                log.info("修改日志状态成功!");
                return PayParams.STATUS.SUCCESS.getCode();
            } else if (body.getInteger("status") == 2) {
                coinOnlineWithdrawalServiceImpl.lambdaUpdate().set(CoinOnlineWithdrawal::getStatus, 2).eq(CoinOnlineWithdrawal::getOrderId, coinOnlineWithdrawal.getOrderId()).update();
                return PayParams.STATUS.SUCCESS.getCode();
            }
        } catch (Exception e) {
            Files.write(path, PayParams.STATUS.FAIL.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            log.info("=====" + MODEL + "  更新数据失败 :" + e.toString());
            return PayParams.STATUS.FAIL.getCode();
        }
        Files.write(path, PayParams.STATUS.SUCCESS.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        return PayParams.STATUS.FAIL.getCode();
    }
}
