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
import com.xinbo.sports.payment.io.PayParams;
import com.xinbo.sports.payment.io.PayParams.OnlinePayReqDto;
import com.xinbo.sports.payment.io.WealthPayParams;
import com.xinbo.sports.service.base.UpdateUserCoinBase;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.utils.*;
import com.xinbo.sports.utils.components.response.CodeInfo;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.RandomUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.Map;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.xinbo.sports.payment.io.PayParams.*;
import static com.xinbo.sports.payment.io.WealthPayParams.WealthPayMethodEnum.QUERY_WITHDRAWAL_STATUS;

/**
 * @author: David
 * @date: 14/07/2020
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WealthPayUtil implements PayAbstractFactory {
    private final ThirdPayBase thirdPayBase;
    private final CoinDepositService coinDepositServiceImpl;
    private final CoinOnlineWithdrawalService coinOnlineWithdrawalServiceImpl;
    private final CoinWithdrawalService coinWithdrawalServiceImpl;
    RestTemplate restTemplate = new RestTemplate();
    private final HttpServletRequest httpServletRequest;
    private final ConfigCache configCache;
    private static final String MODEL = "WealthPayUtil";
    private final UpdateUserCoinBase updateUserCoinBase;
    @Setter
    WealthPayParams.PayPlat plat;

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
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        DecimalFormat df = new DecimalFormat("0.00");
        paramsMap.add("DepositType", "0");
        Map<Integer, String> channelMap = Map.of(1, "1", 5, "3");
        paramsMap.add("DepositChannel", channelMap.get(coinDeposit.getCategory()));
        paramsMap.add("TransactionID", orderSn);
        paramsMap.add("MemberID", plat.getPublicKey());
        var currency = EnumUtils.getEnumIgnoreCase(WealthPayParams.CURRENCY.class, configCache.getCountry()).getCode();
        paramsMap.add("CurrencyCode", currency);
        paramsMap.add("Amount", df.format(amount));
        paramsMap.add("MerchantCode", plat.getBusinessCode());
        paramsMap.add("BankCode", "");
        // 同步通知回调地址
        paramsMap.add("RedirectURL", plat.getReturnUrl());
        // 异步通知回调地址
        paramsMap.add("CallbackURL", plat.getNotifyUrl());
        paramsMap.add("ClientIP", IpUtil.getIp(httpServletRequest));
        String transactionTime = DateNewUtils.utc8Str(DateNewUtils.utc8Zoned(DateNewUtils.now()), DateNewUtils.Format.yyyy_MM_dd_HH_mm_ss);
        paramsMap.add("TransactionTime", transactionTime);

        /*签名算法签名生成 Signature is SHA256 hash
        MerchantCode + TransactionID + MemberID + CurrencyCode + BankCode + Amount
        + Note + RedirectURL + CallbackURL + ClientIP + TransactionTime + SecretKey
         */
        log.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        log.info(plat.getBusinessCode() + orderSn + plat.getPublicKey() + currency + df.format(amount) + plat.getReturnUrl() + plat.getNotifyUrl() + IpUtil.getIp(httpServletRequest) + transactionTime + plat.getBusinessPwd());
        var signature = HashUtil.sha256(plat.getBusinessCode() + orderSn + plat.getPublicKey() + currency + df.format(amount) + plat.getReturnUrl() + plat.getNotifyUrl() + IpUtil.getIp(httpServletRequest) + transactionTime + plat.getBusinessPwd());
        paramsMap.add("Signature", signature);
        // 网关、快捷下单接口地址
        String payUrl = plat.getUrl() + WealthPayParams.WealthPayMethodEnum.DEPOSIT.getMethodName();
        String url = null;
        String method = null;
        int category = 0;
        ResponseEntity<String> call = null;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(paramsMap, headers);
        log.info("======WealthPay: sendUrl : " + payUrl);
        log.info("======WealthPay: sendParams : " + paramsMap);
        call = restTemplate.postForEntity(payUrl, httpEntity, String.class);
        if (call.getStatusCodeValue() != 200) {
            log.info("======WealthPay: Code状态不对 : " + call);
            throw new BusinessException(CodeInfo.PAY_PLAT_LOAD_INVALID);
        } else if (call.getStatusCodeValue() == 200) {
            category = 3;
            method = "POST";
            url = call.getBody().split("action=\"")[1].split("\"")[0] + "?Uid=" + call.getBody().split("Uid\" value=\"")[1].split("\"")[0];
        } else {
            log.info("======WealthPay: 支付类型错误\n");
            log.info("======WealthPay: result = " + call.toString());
        }
        return Map.of("method", method, "url", url, "category", category);
    }

    /**
     * 创建代付订单
     *
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
        BigDecimal amount = coinOnlineWithdrawal.getCoin();
        var bankInfo = parseObject(coinOnlineWithdrawal.getBankInfo());
        // 组装数据
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        DecimalFormat df = new DecimalFormat("0.00");
        paramsMap.add("MerchantCode", plat.getBusinessCode());
        paramsMap.add("TransactionID", dto.getOrderId());
        paramsMap.add("MemberID", plat.getPublicKey());
        var currency = EnumUtils.getEnumIgnoreCase(WealthPayParams.CURRENCY.class, configCache.getCountry()).getCode();
        paramsMap.add("CurrencyCode", currency);
        paramsMap.add("BankCode", bankInfo.getString(BANK_CODE));
        paramsMap.add("ToAccountNumber", bankInfo.getString(BANK_CARD_ACCOUNT));
        paramsMap.add("ToAccountName", bankInfo.getString(ACCOUNT_HOLDER));
        paramsMap.add("Amount", df.format(amount));
        // 异步通知回调地址
        var callbackUrl = plat.getNotifyUrl() + PayParams.UrlEnum.WITHDRAW_NOTIFY_URL.getUrl();
        paramsMap.add("CallbackURL", callbackUrl);
        paramsMap.add("ClientIP", IpUtil.getIp(httpServletRequest));
        String transactionTime = DateNewUtils.utc8Str(DateNewUtils.utc8Zoned(DateNewUtils.now()), DateNewUtils.Format.yyyy_MM_dd_HH_mm_ss);
        paramsMap.add("TransactionTime", transactionTime);

        //签名生成
        log.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        var signature = plat.getBusinessCode() + dto.getOrderId() + plat.getPublicKey() + currency + bankInfo.getString(BANK_CODE)
                + bankInfo.getString(BANK_CARD_ACCOUNT) + bankInfo.getString(ACCOUNT_HOLDER) + df.format(amount) + callbackUrl
                + IpUtil.getIp(httpServletRequest) + transactionTime + plat.getBusinessPwd();
        log.info("======WealthPay encryption：" + signature);
        paramsMap.add("Signature", HashUtil.sha256(signature));
        // 网关、快捷下单接口地址
        String payUrl = plat.getUrl() + WealthPayParams.WealthPayMethodEnum.WITHDRAWAL.getMethodName();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(paramsMap, headers);
        log.info("======WealthPay: sendUrl : " + payUrl);
        log.info("======WealthPay: sendParams : " + paramsMap);
        ResponseEntity<String> call = restTemplate.postForEntity(payUrl, httpEntity, String.class);
        log.info("======WealthPay: result : " + call);
        if (call.getStatusCodeValue() == 200) {
            if (parseObject(call.getBody()).getString("status").equals("0000")) {
                return WithdrawalNotifyResDto.builder().code(parseObject(call.getBody()).getString("status").equals("0000")).msg(parseObject(call.getBody()).getString("message")).build();
            }
            coinOnlineWithdrawalServiceImpl.remove(new QueryWrapper<CoinOnlineWithdrawal>().eq("order_id", dto.getOrderId()));
            CodeInfo.PAY_PLAT_ADD_ORDER_FAILURE.setMsg(parseObject(call.getBody()).getString("message"));
            throw new BusinessException(CodeInfo.PAY_PLAT_ADD_ORDER_FAILURE);
        } else {
            log.info("======WealthPay: Code状态不对 : " + call);
            throw new BusinessException(CodeInfo.PAY_PLAT_ADD_ORDER_FAILURE);
        }
    }

    @Override
    public Boolean checkPaymentStatus(String orderId) {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        var url = plat.getUrl() + QUERY_WITHDRAWAL_STATUS.getMethodName();
        try {
            CoinOnlineWithdrawal one = coinOnlineWithdrawalServiceImpl.lambdaQuery().eq(CoinOnlineWithdrawal::getOrderId, orderId).one();
            if (null != one) {
                CoinWithdrawal coinWithdrawal = coinWithdrawalServiceImpl.lambdaQuery().eq(CoinWithdrawal::getStatus, 3).eq(CoinWithdrawal::getId, one.getWithdrawalOrderId()).one();
                if (null == coinWithdrawal) {
                    XxlJobLogger.log(MODEL + "当前已稽核订单可处理");
                    return true;
                }
                var currency = EnumUtils.getEnumIgnoreCase(WealthPayParams.CURRENCY.class, configCache.getCountry()).getCode();
                var transactionTime = DateNewUtils.utc8Str(DateNewUtils.utc8Zoned(DateNewUtils.now()), DateNewUtils.Format.yyyy_MM_dd_HH_mm_ss);
                paramsMap.add("MerchantCode", plat.getBusinessCode());
                paramsMap.add("CurrencyCode", currency);
                paramsMap.add("TransactionID", orderId);
                paramsMap.add("TransactionTime", transactionTime);
                //Signature is SHA256 hash of the data below: MerchantCode + CurrencyCode + TransactionID + TransactionTime + SecretKey
                var signature = plat.getBusinessCode() + currency + orderId + transactionTime + plat.getBusinessPwd();
                log.info("======WealthPay encryption：" + signature);
                paramsMap.add("Signature", HashUtil.sha256(signature));
                // 网关、快捷下单接口地址
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(paramsMap, headers);
                log.info("======WealthPay: sendUrl : " + url);
                log.info("======WealthPay: sendParams : " + paramsMap);
                ResponseEntity<String> call = restTemplate.postForEntity(url, httpEntity, String.class);
                log.info("======WealthPay: result : " + call);
                JSONObject result = JSON.parseObject(call.getBody());
                if (call.getStatusCodeValue() == 200) {
                    Integer status = result.getInteger("Status");
                    if (status == 0001) {
                        coinOnlineWithdrawalServiceImpl.lambdaUpdate().set(CoinOnlineWithdrawal::getStatus, 1).eq(CoinOnlineWithdrawal::getOrderId, orderId).update();
                        coinWithdrawalServiceImpl.lambdaUpdate().set(CoinWithdrawal::getStatus, 1).eq(CoinWithdrawal::getId, one.getWithdrawalOrderId()).update();
                        //'状态:0-处理中 1-成功 2-失败'
                        updateUserCoinBase.updateCoinLog(Long.valueOf(one.getWithdrawalOrderId()), 2, 1, DateUtils.getCurrentTime());
                    } else if (status == 0003) {
                        coinOnlineWithdrawalServiceImpl.lambdaUpdate().set(CoinOnlineWithdrawal::getStatus, 2).eq(CoinOnlineWithdrawal::getOrderId, orderId).update();
                    }
                    return true;
                } else {
                    XxlJobLogger.log(MODEL + result.getString("Message"));
                    return false;
                }
            }
        } catch (Exception e) {
            log.error(e.toString());
            log.error("=============================================");
            log.error("Class: " + "MangoPayUtil" + "\n");
            log.error("URL: " + url + "\n");
            log.error("postData: " + paramsMap + "\n");
            e.printStackTrace();
            throw new BusinessException(CodeInfo.PAY_PLAT_UPDATE_ORDER_FAILURE);
        }
        XxlJobLogger.log(MODEL + "当前无订单可处理");
        return true;
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
        log.info(String.format("=====WealthPay NotifyUrl Record : %s", dto.toString()));
        var path = Paths.get("WealthPay-" + DateUtils.yyyyMMdd(DateNewUtils.now()) + ".txt");
        if (Files.exists(path)) {
            Files.write(path, dto.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        } else {
            Files.createFile(path);
            Files.write(path, dto.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        }
        if (null == plat) PayAbstractFactory.init(MODEL);
        // 判定加解密规则是否正确
        String signature = HashUtil.sha256(dto.get("MerchantCode") + dto.get("CurrencyCode") + dto.get("BankCode") + dto.get("TransactionID")
                + dto.get("Amount") + dto.get("TransactionTime") + dto.get("ID") + dto.get("Status") + dto.get("Message") + plat.getBusinessPwd());

        // 判定签名规则是否正确
        if (!signature.equalsIgnoreCase(dto.get("Signature")) || !dto.get("Status").equals("0001")) {
            log.info("=====WealthPay NotifyUrl : 校验 或 Status 失败 ");
            return PayParams.STATUS.FAIL.getCode();
        }
        // 判定OrderId 是否存在
        CoinDeposit coinDeposit = coinDepositServiceImpl.lambdaQuery()
                .eq(CoinDeposit::getOrderId, dto.get("TransactionID"))
                .orderByDesc(CoinDeposit::getId)
                .one();
        if (null == coinDeposit) {
            log.info("=====WealthPay NotifyUrl : 订单号不存在");
            return PayParams.STATUS.FAIL.getCode();
        } else if (coinDeposit.getStatus() != 0) {
            log.info("=====WealthPay NotifyUrl : 重复回调");
            return PayParams.STATUS.FAIL.getCode();
        }
        try {
            // 正常业务处理流程
            thirdPayBase.updateCoinDeposit(coinDeposit.getId(), new BigDecimal(dto.get("Amount")));
        } catch (Exception e) {
            log.info("=====WealthPay 更新数据失败 :" + e.toString());
        }
        return PayParams.STATUS.SUCCESS.getCode();
    }

    /**
     * 提款异步回调
     *
     * @param dto
     * @return
     */
    public String withdrawNotifyUrl(Map<String, String> dto) {
        // 记录回调的数据
        log.info(String.format("=====WealthPay NotifyUrl Record : %s", dto.toString()));
        // 判定加解密规则是否正确
        var bankCode = null == dto.get("BankCode") ? "" : dto.get("BankCode");
        var status = dto.get("Status");
        if (null == plat) PayAbstractFactory.init(MODEL);
        String signature = HashUtil.sha256(dto.get("MerchantCode") + dto.get("CurrencyCode") + bankCode +
                dto.get("TransactionID") + dto.get("Amount") + dto.get("TransactionTime") + dto.get("ID") + status
                + dto.get("Message") + plat.getBusinessPwd());
        // 判定签名规则是否正确
        if (!signature.equalsIgnoreCase(dto.get("Signature"))) {
            log.info("=====WealthPay NotifyUrl : 校验失败 ");
            return PayParams.STATUS.FAIL.getCode();
        }
        // 判定OrderId 是否存在
        CoinOnlineWithdrawal coinOnlineWithdrawal = coinOnlineWithdrawalServiceImpl.lambdaQuery()
                .eq(CoinOnlineWithdrawal::getOrderId, dto.get("TransactionID"))
                .orderByDesc(CoinOnlineWithdrawal::getId)
                .one();
        if (null == coinOnlineWithdrawal) {
            log.info("=====WealthPay NotifyUrl : 订单号不存在");
            return PayParams.STATUS.FAIL.getCode();
        } else if (coinOnlineWithdrawal.getStatus() != 0) {
            log.info("=====WealthPay NotifyUrl : 重复回调");
            return PayParams.STATUS.FAIL.getCode();
        }

        try {
            if ("0001".equals(status)) {
                coinOnlineWithdrawalServiceImpl.lambdaUpdate().set(CoinOnlineWithdrawal::getStatus, 1).eq(CoinOnlineWithdrawal::getOrderId, coinOnlineWithdrawal.getOrderId()).update();
                coinWithdrawalServiceImpl.lambdaUpdate().set(CoinWithdrawal::getStatus, 1).eq(CoinWithdrawal::getId, coinOnlineWithdrawal.getWithdrawalOrderId()).update();
                updateUserCoinBase.updateCoinLog(Long.valueOf(coinOnlineWithdrawal.getWithdrawalOrderId()), 2, 1, DateUtils.getCurrentTime());
                log.info("修改日志状态成功!");
                return PayParams.STATUS.SUCCESS.getCode();
            } else if ("0003".equals(status)) {
                coinOnlineWithdrawalServiceImpl.lambdaUpdate().set(CoinOnlineWithdrawal::getStatus, 2).eq(CoinOnlineWithdrawal::getOrderId, coinOnlineWithdrawal.getOrderId()).update();
                return PayParams.STATUS.SUCCESS.getCode();
            }
        } catch (Exception e) {
            log.info("=====WealthPay 更新数据失败 :" + e.toString());
            return PayParams.STATUS.FAIL.getCode();
        }
        return PayParams.STATUS.FAIL.getCode();
    }

}


