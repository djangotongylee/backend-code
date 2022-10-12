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
        if (!update) throw new BusinessException(CodeInfo.PAY_PLAT_UPDATE_ORDER_ID_INVALID);
        //  计算金额 单位(分)
        BigDecimal amount = coinDeposit.getCoin();
        // 组装数据
        SortedMap<String, String> paramsMap = new TreeMap<>();
        paramsMap.put(MERCHANTNO, plat.getBusinessCode());
        paramsMap.put(MERCHANTORDER, orderSn);
        paramsMap.put(AMOUNT, amount.toBigInteger().toString());
        // 异步通知回调地址
        var notifyUrl = plat.getNotifyUrl() + PayParams.UrlEnum.DEPOSIT_NOTIFY_URL.getUrl();
        paramsMap.put("notify_url", notifyUrl);
        paramsMap.put("return_url", plat.getReturnUrl());
        //签名算法签名生成
        String key = plat.getBusinessPwd();
        String sign = signForInspiry(paramsMap, key);
        var params = "merchant_no=" + plat.getBusinessCode() + "&merchant_order=" + orderSn + "&" + AMOUNT + "=" + amount.toBigInteger().toString() + "&notify_url=" + notifyUrl + "&sign=" + sign + "&return_url=" + plat.getReturnUrl();
        var category = 1;
        var method = "GET";
        var url = plat.getUrl() + UPIPayParams.UPIPayMethodEnum.PAY_IN.getUrl() + "?" + params;
        return Map.of("method", method, "url", url, "category", category);
    }

    /**
     * 代付订单创建
     * @param dto 入参
     * @return
     */
    @Override
    public WithdrawalNotifyResDto onlineWithdraw(PayParams.OnlinePayoutReqDto dto) {
        // 获取当前订单ID
        CoinOnlineWithdrawal coinOnlineWithdrawal = coinOnlineWithdrawalServiceImpl.getOne(new QueryWrapper<CoinOnlineWithdrawal>().eq("order_id", dto.getOrderId()));
        if (null == coinOnlineWithdrawal) throw new BusinessException(CodeInfo.PAY_PLAT_ORDER_ID_INVALID);
        //  计算金额 单位(分)
        BigDecimal amount = coinOnlineWithdrawal.getCoin();
        // 组装数据
        SortedMap<String, String> paramsMap = new TreeMap<>();
        var bankInfo = parseObject(coinOnlineWithdrawal.getBankInfo());
        paramsMap.put(MERCHANTNO, plat.getBusinessCode());
        paramsMap.put(MERCHANTORDER, dto.getOrderId());
        paramsMap.put(AMOUNT, amount.toBigInteger().toString());
        paramsMap.put("ifsc", bankInfo.getJSONObject(MARK).getString("ifsc"));
        paramsMap.put("bank_name", bankInfo.getString(OPEN_ACCOUNT_BANK));
        Map<Integer, String> channel = Map.of(1, "借记卡", 6, "UPI");
        paramsMap.put("receiver_type", channel.get(coinOnlineWithdrawal.getCategory()));
        paramsMap.put("receiver_account", bankInfo.getString(BANK_CARD_ACCOUNT));
        paramsMap.put("receiver_name", bankInfo.getString(ACCOUNT_HOLDER));
        // 异步通知回调地址
        var notifyUrl = plat.getNotifyUrl() + PayParams.UrlEnum.DEPOSIT_NOTIFY_URL.getUrl();
        paramsMap.put("notify_url", notifyUrl);
        //签名算法签名生成
        paramsMap.put("sign", signForInspiry(paramsMap, plat.getBusinessPwd()));
        var url = plat.getUrl() + UPIPayParams.UPIPayMethodEnum.AGENT_PAY.getUrl();
        ResponseEntity<String> response = restTemplate.postForEntity(url, paramsMap, String.class);
        JSONObject result = parseObject(response.getBody());
        if (result.getInteger("result_code") == 200) {
            if (result.getInteger("status") == 3) {
                return null;
            }
        } else {
            log.info("======UPIPay: 代付Code状态不对 : " + result);
            throw new BusinessException(CodeInfo.PAY_PLAT_LOAD_INVALID);
        }
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
        // 判定加解密规则是否正确
        String sign = signForInspiry(paramsMap, this.plat.getBusinessPwd());
        // 判定签名规则是否正确
        if (!sign.equalsIgnoreCase(dto.get("sign")) || !dto.get(STATUS).equals("2")) {
            log.info("=====UPIPay NotifyUrl : 校验 或 Status 失败 ");
            Files.write(path, PayParams.STATUS.FAIL.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            return PayParams.STATUS.FAIL.getCode();
        }
        // 判定OrderId 是否存在
        CoinDeposit coinDeposit = coinDepositServiceImpl.lambdaQuery()
                .eq(CoinDeposit::getOrderId, dto.get(MERCHANTORDER))
                .orderByDesc(CoinDeposit::getId)
                .one();
        if (null == coinDeposit) {
            log.info("=====UPIPay NotifyUrl : 订单号不存在");
            Files.write(path, PayParams.STATUS.FAIL.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            return PayParams.STATUS.FAIL.getCode();
        } else if (coinDeposit.getStatus() != 0) {
            Files.write(path, PayParams.STATUS.FAIL.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            log.info("=====UPIPay NotifyUrl : 重复回调");
            return PayParams.STATUS.FAIL.getCode();
        }
        try {
            // 正常业务处理流程
            thirdPayBase.updateCoinDeposit(coinDeposit.getId(), new BigDecimal(dto.get("paid")), dto.toString());
        } catch (Exception e) {
            Files.write(path, PayParams.STATUS.FAIL.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            log.info("=====UPIPay 更新数据失败 :" + e.toString());
        }
        Files.write(path, PayParams.STATUS.SUCCESS.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        return PayParams.STATUS.SUCCESS.getCode();
    }


    public String withdrawNotifyUrl(Map<String, String> dto) {
        return null;
    }

    /**
     * 生成签名
     * secret字段，故没有使用公共工具类
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
            //空值不传递，不参与签名组串
            if (null != v && !"".equals(v)) {
                sbkey.append(k).append("=").append(v).append("&");
            }
        }

        sbkey = sbkey.append("secret=").append(key);

        //MD5加密,结果转换为大写字符
        return DigestUtils.md5DigestAsHex(sbkey.toString().getBytes()).toUpperCase();
    }
}
