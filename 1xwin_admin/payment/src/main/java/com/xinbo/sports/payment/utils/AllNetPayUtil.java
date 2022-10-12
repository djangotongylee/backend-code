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
import com.xinbo.sports.payment.io.AllNetPayParams;
import com.xinbo.sports.payment.io.PayParams;
import com.xinbo.sports.service.base.UpdateUserCoinBase;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.DateUtils;
import com.xinbo.sports.utils.HttpUtils;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.xinbo.sports.payment.io.AllNetPayParams.allNetPayUrlEnum.PAY;
import static com.xinbo.sports.payment.io.AllNetPayParams.allNetPayUrlEnum.PAYOUT;
import static com.xinbo.sports.payment.io.PayParams.*;
import static com.xinbo.sports.payment.io.PayParams.OPEN_ACCOUNT_BANK;
import static com.xinbo.sports.utils.MD5.signForInspiry;

/**
 * @author: David
 * @date: 14/07/2020
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AllNetPayUtil implements PayAbstractFactory {
    private final ThirdPayBase thirdPayBase;
    private final CoinDepositService coinDepositServiceImpl;
    private final CoinOnlineWithdrawalService coinOnlineWithdrawalServiceImpl;
    private final CoinWithdrawalService coinWithdrawalServiceImpl;
    private final UpdateUserCoinBase updateUserCoinBase;
    private static final String MODEL = "AllNetPayUtil";
    @Setter
    AllNetPayParams.PayPlat plat;

    /**
     * 创建支付
     *
     * @param dto 入参
     * @return
     */
    @Override
    public Map<Object, Object> onlinePay(@NotNull PayParams.OnlinePayReqDto dto) {
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
        SortedMap<String, String> paramsMap = new TreeMap<>();
        paramsMap.put("pay_memberid", plat.getBusinessCode());
        paramsMap.put("pay_orderid", orderSn);
        paramsMap.put("pay_applydate", DateNewUtils.getNowFormatTime(DateNewUtils.Format.yyyy_MM_dd_HH_mm_ss));
        paramsMap.put("pay_bankcode", plat.getPublicKey());
        paramsMap.put("pay_notifyurl", plat.getNotifyUrl() + PayParams.UrlEnum.DEPOSIT_NOTIFY_URL.getUrl());
        paramsMap.put("pay_callbackurl", plat.getReturnUrl());
        paramsMap.put("pay_amount", df.format(amount));
        paramsMap.put("pay_name", coinDeposit.getUsername());
        paramsMap.put("pay_mobile", "998800");
        paramsMap.put("pay_email", "122@163.com");
        //签名算法签名生成设所有发送或者接收到的数据为集合M，将集合M内非空参数值的参数按照参数名ASCII码从小到大排序（字典序），使用URL键值对的格式（即key1=value1&key2=value2…）拼接成字符串。
        //在stringA最后拼接上key得到stringSignTemp字符串，并对stringSignTemp进行MD5运算，再将得到的字 符串所有字符转换为小写，得到sign值signValue。
        var sign = signForInspiry(paramsMap, plat.getBusinessPwd()).toLowerCase();
        paramsMap.put("pay_md5sign", sign);
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(paramsMap);
        // 网关、快捷下单接口地址
        var url = plat.getUrl() + PAY.getUrl();
        log.info("======" + MODEL + ": sendUrl : " + url);
        log.info("======" + MODEL + ": sendParams : " + paramsMap);
        Map<String, String> header = new ImmutableMap.Builder<String, String>().
                put("Content-Type", "application/x-www-form-urlencoded").
                put("charset", "UTF-8").
                build();
        try {
            String result = HttpUtils.doPost(url, jsonObject, header, MODEL);
            log.info("======" + MODEL + ": result : " + result);
            return Map.of("method", "POST", "url", result.replace("<script type='text/javascript'>function load_submit(){document.form1.submit()}load_submit();</script>", ""), "category", 4);
        } catch (Exception e) {
            log.error(e.toString());
            log.error("===========" + MODEL + " URL: " + url + "\n");
            log.error("===========" + MODEL + " postData: " + jsonObject + "\n");
        }
        log.info("======" + MODEL + ": 支付类型错误\n");
        log.info("======" + MODEL + ": result = " + jsonObject.toJSONString());
        throw new BusinessException(CodeInfo.PAY_PLAT_LOAD_INVALID);
    }

    @Override
    public PayParams.WithdrawalNotifyResDto onlineWithdraw(PayParams.OnlinePayoutReqDto dto) {
        // 获取当前订单ID
        CoinOnlineWithdrawal coinOnlineWithdrawal = coinOnlineWithdrawalServiceImpl.getOne(new QueryWrapper<CoinOnlineWithdrawal>().eq("order_id", dto.getOrderId()));
        if (null == coinOnlineWithdrawal) {
            throw new BusinessException(CodeInfo.PAY_PLAT_ORDER_ID_INVALID);
        }

        //  计算金额 单位(分)
        BigDecimal amount = coinOnlineWithdrawal.getCoin();
        // 组装数据
        SortedMap<String, String> paramsMap = new TreeMap<>();
        DecimalFormat df = new DecimalFormat("0.00");
        var bankInfo = parseObject(coinOnlineWithdrawal.getBankInfo());
        paramsMap.put("mchid", plat.getBusinessCode());
        paramsMap.put("out_trade_no", dto.getOrderId());
        paramsMap.put("money", df.format(amount));
        paramsMap.put("bankname", bankInfo.getString(OPEN_ACCOUNT_BANK));
        paramsMap.put("ifsc", bankInfo.getString("mark"));
        paramsMap.put("accountname", bankInfo.getString(ACCOUNT_HOLDER));
        paramsMap.put("cardnumber", bankInfo.getString(BANK_CARD_ACCOUNT));
        paramsMap.put("mobile", "city");
        paramsMap.put("email", "5667888");
        paramsMap.put("notify_url", plat.getNotifyUrl() + UrlEnum.WITHDRAW_NOTIFY_URL.getUrl());
        var sign = signForInspiry(paramsMap, plat.getBusinessPwd()).toLowerCase();
        paramsMap.put("pay_md5sign", sign);
        // 网关、快捷下单接口地址
        String payoutUrl = plat.getUrl() + PAYOUT.getUrl();

        //2、header头部信息
        Map<String, String> header = new ImmutableMap.Builder<String, String>().
                put("Content-Type", "application/x-www-form-urlencoded").
                put("charset", "UTF-8").
                build();
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(paramsMap);

        JSONObject result = null;
        try {
            result = parseObject(HttpUtils.doPost(payoutUrl, jsonObject, header, MODEL));
            if ("success".equals(result.getString("status"))) {
                coinOnlineWithdrawalServiceImpl.lambdaUpdate().set(CoinOnlineWithdrawal::getMark, jsonObject.getString("transaction_id")).eq(CoinOnlineWithdrawal::getOrderId, dto.getOrderId()).update();
                return WithdrawalNotifyResDto.builder().code(true).msg(jsonObject.getString("msg")).build();
            }
            coinOnlineWithdrawalServiceImpl.remove(new QueryWrapper<CoinOnlineWithdrawal>().eq("order_id", dto.getOrderId()));
            CodeInfo.PAY_PLAT_ADD_ORDER_FAILURE.setMsg(result.getString("msg"));
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
        var sign = signForInspiry(new TreeMap<>(dto), plat.getBusinessPwd());
        if (!sign.equalsIgnoreCase(dto.get("sign")) || !dto.get("returncode").equals("00")) {
            log.info("=====" + MODEL + " NotifyUrl : 校验 或 Status 失败 ");
            return AllNetPayParams.STATUS.FAIL.getCode();
        }
        // 判定OrderId 是否存在
        CoinDeposit coinDeposit = coinDepositServiceImpl.lambdaQuery()
                .eq(CoinDeposit::getOrderId, dto.get("orderid"))
                .orderByDesc(CoinDeposit::getId)
                .one();
        if (null == coinDeposit) {
            log.info("=====" + MODEL + " NotifyUrl : 订单号不存在");
            Files.write(path, AllNetPayParams.STATUS.FAIL.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            return AllNetPayParams.STATUS.FAIL.getCode();
        } else if (coinDeposit.getStatus() != 0) {
            Files.write(path, AllNetPayParams.STATUS.FAIL.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            log.info("=====" + MODEL + " NotifyUrl : 重复回调");
            return AllNetPayParams.STATUS.FAIL.getCode();
        }

        try {
            // 正常业务处理流程
            thirdPayBase.updateCoinDeposit(coinDeposit.getId(), new BigDecimal(dto.get("amount")));

        } catch (Exception e) {
            Files.write(path, AllNetPayParams.STATUS.FAIL.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            log.info("=====" + MODEL + "  更新数据失败 :" + e.toString());
        }
        Files.write(path, AllNetPayParams.STATUS.SUCCESS.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        return AllNetPayParams.STATUS.SUCCESS.getCode();
    }

    @SneakyThrows
    public String withdrawNotifyUrl(Map<String, String> dto) {
        // 记录回调的数据
        var path = Paths.get(MODEL + "-" + DateUtils.yyyyMMdd(DateNewUtils.now()) + ".txt");
        if (!Files.exists(path)) Files.createFile(path);
        Files.write(path, dto.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        if (null == plat) PayAbstractFactory.init(MODEL);
        log.info(String.format("=====" + MODEL + " withdrawNotifyUrl Record : %s", dto));
        // 判定签名规则是否正确
        var sign = signForInspiry(new TreeMap<>(dto), plat.getBusinessPwd());
        if (!sign.equalsIgnoreCase(dto.get("sign")) || Integer.parseInt(dto.get("refCode")) > 2) {
            log.info("=====" + MODEL + " NotifyUrl : 校验 或 status失败 ");
            Files.write(path, PayParams.STATUS.FAIL.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            return PayParams.STATUS.FAIL.getCode();
        }
        // 判定OrderId 是否存在
        CoinOnlineWithdrawal coinOnlineWithdrawal = coinOnlineWithdrawalServiceImpl.lambdaQuery()
                .eq(CoinOnlineWithdrawal::getOrderId, dto.get("out_trade_no"))
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
            if (Integer.parseInt(dto.get("refCode")) == 1) {
                coinOnlineWithdrawalServiceImpl.lambdaUpdate().set(CoinOnlineWithdrawal::getStatus, 1).eq(CoinOnlineWithdrawal::getOrderId, coinOnlineWithdrawal.getOrderId()).update();
                coinWithdrawalServiceImpl.lambdaUpdate().set(CoinWithdrawal::getStatus, 1).eq(CoinWithdrawal::getId, coinOnlineWithdrawal.getWithdrawalOrderId()).update();
                updateUserCoinBase.updateCoinLog(Long.valueOf(coinOnlineWithdrawal.getWithdrawalOrderId()), 2, 1, DateUtils.getCurrentTime());
                log.info("修改日志状态成功!");
                return PayParams.STATUS.SUCCESS.getCode();
            } else if (Integer.parseInt(dto.get("refCode")) == 2) {
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

