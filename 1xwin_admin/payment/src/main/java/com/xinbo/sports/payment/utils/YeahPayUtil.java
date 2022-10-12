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
import com.xinbo.sports.payment.io.PayParams.*;
import com.xinbo.sports.payment.io.YeahPayParams;
import com.xinbo.sports.service.base.UpdateUserCoinBase;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.utils.*;
import com.xinbo.sports.utils.components.response.CodeInfo;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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
import java.util.*;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.xinbo.sports.payment.io.PayParams.*;
import static com.xinbo.sports.payment.io.PayParams.UrlEnum.WITHDRAW_NOTIFY_URL;
import static com.xinbo.sports.payment.io.ShineUPayParams.ShineUPayUrlEnum.WITHDRAW_QUERY;
import static com.xinbo.sports.payment.io.YeahPayParams.YeahPayUrlEnum.PAYOUT;
import static com.xinbo.sports.payment.io.YeahPayParams.YeahPayUrlEnum.QUERY_PAYOUT_ORDER;
import static java.util.Objects.nonNull;

/**
 * @author: David
 * @date: 14/07/2020
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class YeahPayUtil implements PayAbstractFactory {
    private final ThirdPayBase thirdPayBase;
    private final CoinDepositService coinDepositServiceImpl;
    private final CoinOnlineWithdrawalService coinOnlineWithdrawalServiceImpl;
    private final CoinWithdrawalService coinWithdrawalServiceImpl;
    private final UpdateUserCoinBase updateUserCoinBase;
    private final HttpServletRequest httpServletRequest;
    private final JedisUtil jedisUtil;
    private static final String AMOUNT = "amount";
    private static final String MODEL = "YeahPayUtil";
    private static final String ORDER_ID = "merchantOrderId";
    private static final String PAYOUT_ORDER_ID = "merchantPayoutId";
    RestTemplate restTemplate = new RestTemplate();
    @Setter
    YeahPayParams.PayPlat plat;

    /**
     * 创建支付
     *
     * @param dto 入参
     * @return
     */
    @Override
    public Map<Object, Object> onlinePay(@NotNull OnlinePayReqDto dto) {
        String url = null;
        var jsonObject2 = new JSONObject();
        String payUrl = plat.getUrl() + YeahPayParams.YeahPayUrlEnum.PAYMENT.getUrl();

        try {
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
            Map<String, Object> params = new HashMap<>();
            params.put(AMOUNT, df.format(amount));
            params.put("merchantID", plat.getPublicKey());
            params.put("payType", "6");
            params.put(ORDER_ID, orderSn);
            params.put("productName", orderSn);
            params.put("productDescription", orderSn);
            params.put("merchantUserId", String.valueOf(coinDeposit.getUid()));
            params.put("merchantUserName", coinDeposit.getUsername());
            params.put("merchantUserEmail", "");
            params.put("merchantUserIp", IpUtil.getIp(httpServletRequest));
            params.put("merchantUserCitizenId", "9877889");
            params.put("countryCode", "IN");
            params.put("currency", "INR");
            params.put("merchantUserPhone", "");
            params.put("redirectUrl", plat.getReturnUrl());
            var sign = ParamSignUtil.sign(params, plat.getBusinessPwd());
            params.put("sign", sign);
            var jsonObject = new JSONObject();
            jsonObject.put("addrCity", "Meban");
            jsonObject.put("addrStreet", "87 avenue");
            jsonObject.put("addrNumber", "9877889");
            params.put("ext", jsonObject);
            jsonObject2.putAll(params);
            Map<String, String> header = new ImmutableMap.Builder<String, String>().
                    put("Content-Type", "application/json").
                    put("charset", "UTF-8").
                    put("Authorization", "Bearer " + getToken()).
                    build();
            JSONObject call = parseObject(HttpUtils.doPost(payUrl, jsonObject2, header, MODEL));
            if (call.getInteger("errorCode") == 1000) {
                var payOrder = parseObject(call.getString("orderPaymentLoad")).getString("payOrder");
                url = parseObject(payOrder).getString("checkPageUrl");
            } else {
                log.info("======" + MODEL + ": 支付类型错误\n");
                log.info("======" + MODEL + ": result = " + jsonObject.toJSONString());
                throw new BusinessException(CodeInfo.STATUS_CODE_403_6);
            }
        } catch (Exception e) {
            log.error(e.toString());
            log.error("===========" + MODEL + " URL: " + payUrl + "\n");
            log.error("===========" + MODEL + " postData: " + jsonObject2 + "\n");
        }
        return Map.of("method", "GET", "url", url, "category", 1);
    }

    /**
     * Authorization：Basic <APP_ID>:<APP_KEY> 的base64值(符合Basic认证规则)(**注意Basic后面有个空格**)
     * Content-Type：application/x-www-form-urlencoded
     * grant_type	String	token生成模式，固定值：client_credentials
     *
     * @return
     */
    private String getToken() {
        var accessToken = jedisUtil.get(MODEL);
        if (null != accessToken) {
            return accessToken;
        }
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        String sign = plat.getBusinessCode() + ":" + plat.getBusinessPwd();
        var token = Base64.encodeBase64String(sign.getBytes());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(token);
        paramsMap.add("grant_type", "client_credentials");
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(paramsMap, headers);
        var call = restTemplate.postForEntity(plat.getUrl() + YeahPayParams.YeahPayUrlEnum.TOKEN.getUrl(), httpEntity, String.class);
        if (call.getStatusCodeValue() == 200) {
            JSONObject data = parseObject(call.getBody());
            jedisUtil.setex(MODEL, data.getInteger("expires_in") - 10, data.getString("access_token"));
            return data.getString("access_token");
        }
        throw new BusinessException(CodeInfo.PAY_PLAT_LOAD_INVALID);
    }

    /**
     * 代付订单创建
     *
     * @param dto 入参
     * @return
     */
    @Override
    public WithdrawalNotifyResDto onlineWithdraw(OnlinePayoutReqDto dto) {
        String payoutUrl = plat.getUrl() + PAYOUT.getUrl();
        var params = new JSONObject();
        try {
            // 获取当前订单ID
            CoinOnlineWithdrawal coinOnlineWithdrawal = coinOnlineWithdrawalServiceImpl.getOne(new QueryWrapper<CoinOnlineWithdrawal>().eq("order_id", dto.getOrderId()));
            if (null == coinOnlineWithdrawal) throw new BusinessException(CodeInfo.PAY_PLAT_ORDER_ID_INVALID);
            //  计算金额 单位(分)
            var bankInfo = parseObject(coinOnlineWithdrawal.getBankInfo());
            BigDecimal amount = coinOnlineWithdrawal.getCoin();
            DecimalFormat df = new DecimalFormat("0.00");
            // 组装数据
            params.put("countryCode", "IN");
            params.put("currency", "INR");
            //代付类型。upi就代表通过upi ID 代付，card就代表通过银行卡代付，固定值
            Map<Integer, String> payTypeMap = Map.of(1, "card", 6, "upi");
            var payType = payTypeMap.get(coinOnlineWithdrawal.getCategory());
            params.put("payType", payType);
            params.put("payoutId", dto.getOrderId());
            params.put("callBackUrl", plat.getNotifyUrl() + WITHDRAW_NOTIFY_URL.getUrl());
            var jsonObject = new JSONObject();
            if (payType.equalsIgnoreCase("card")) {
                jsonObject.put("payeeAccount", bankInfo.getString(BANK_CARD_ACCOUNT));
                jsonObject.put("payeeName", bankInfo.getString(ACCOUNT_HOLDER));
                jsonObject.put("ifsc", bankInfo.getString("mark"));
                jsonObject.put("panNum", "");
            }
            if (payType.equalsIgnoreCase("upi")) {
                jsonObject.put("walletId", bankInfo.getString(BANK_CARD_ACCOUNT));
                jsonObject.put("walletOwnName", bankInfo.getString(ACCOUNT_HOLDER));
            }
            jsonObject.put(AMOUNT, df.format(amount));
            jsonObject.put("phone", "");
            jsonObject.put("email", "");
            params.put("details", List.of(jsonObject));
            var sign = ParamSignUtil.sign(params, plat.getBusinessPwd());
            // 网关、快捷下单接口地址
            Map<String, String> header = new ImmutableMap.Builder<String, String>().
                    put("Content-Type", "application/json")
                    .put("charset", "UTF-8")
                    .put("Authorization", "Bearer " + getToken())
                    .build();
            log.info(MODEL + "-->sigh=" + sign);
            String call = HttpUtils.doPost(payoutUrl, params, header, MODEL);
            log.info(MODEL + "-->response=" + sign);
            JSONObject result = parseObject(call);
            if (result.getInteger("code") == 1000) {
                coinOnlineWithdrawalServiceImpl.lambdaUpdate().set(nonNull(result.getJSONObject("result").getString("merchantPayoutId")), CoinOnlineWithdrawal::getMark, result.getJSONObject("result").getString("merchantPayoutId"))
                        .eq(CoinOnlineWithdrawal::getOrderId, dto.getOrderId()).update();
                return WithdrawalNotifyResDto.builder().code(result.getInteger("code") == 1000).msg(result.getString("result")).build();
            }
            coinOnlineWithdrawalServiceImpl.remove(new QueryWrapper<CoinOnlineWithdrawal>().eq("order_id", dto.getOrderId()));
            CodeInfo.PAY_PLAT_ADD_ORDER_FAILURE.setMsg(result.getString("result"));
            throw new BusinessException(CodeInfo.PAY_PLAT_ADD_ORDER_FAILURE);
        } catch (Exception e) {
            log.error(e.toString());
            log.error("=============================================");
            log.error("Class: " + MODEL + "\n");
            log.error("URL: " + payoutUrl + "\n");
            log.error("postData: " + JSON.toJSONString(params) + "\n");
            throw new BusinessException(CodeInfo.PAY_PLAT_ADD_ORDER_FAILURE);
        }
    }

    @Override
    public Boolean checkPaymentStatus(String orderId) {
        var url = plat.getUrl() + QUERY_PAYOUT_ORDER.getUrl();
        try {
            CoinOnlineWithdrawal one = coinOnlineWithdrawalServiceImpl.lambdaQuery().eq(CoinOnlineWithdrawal::getOrderId, orderId).one();
            if (null != one) {
                CoinWithdrawal coinWithdrawal = coinWithdrawalServiceImpl.lambdaQuery().eq(CoinWithdrawal::getStatus, 3).eq(CoinWithdrawal::getId, one.getWithdrawalOrderId()).one();
                if (null == coinWithdrawal) {
                    XxlJobLogger.log(MODEL + "当前已稽核订单可处理");
                    return true;
                }
                HttpHeaders header = new HttpHeaders();
                header.setContentType(MediaType.APPLICATION_JSON);
                header.setBearerAuth(getToken());
                var requestEntity = new HttpEntity<String>(null, header);
                // 网关、快捷下单接口地址
                var call = restTemplate.exchange(url + one.getOrderId(), HttpMethod.GET, requestEntity, String.class);
                JSONObject result = JSON.parseObject(call.getBody());
                if (result.getInteger("code") == 1000) {
                    Integer status = result.getJSONObject("result").getInteger("status");
                    if (status == 1) {
                        coinOnlineWithdrawalServiceImpl.lambdaUpdate().set(CoinOnlineWithdrawal::getStatus, 1).eq(CoinOnlineWithdrawal::getOrderId, orderId).update();
                        coinWithdrawalServiceImpl.lambdaUpdate().set(CoinWithdrawal::getStatus, 1).eq(CoinWithdrawal::getId, one.getWithdrawalOrderId()).update();
                        //'状态:0-处理中 1-成功 2-失败'
                        updateUserCoinBase.updateCoinLog(Long.valueOf(one.getWithdrawalOrderId()), 2, 1, DateUtils.getCurrentTime());
                    } else if (status == 5) {
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
            log.error("Class: " + MODEL + "\n");
            log.error("URL: " + url + "\n");
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
     * @param dto
     * @return success-成功 fail-失败
     */
    @SneakyThrows
    public String notifyUrl(Map<String, String> dto) {
        // 记录回调的数据
        log.info(String.format("=====" + MODEL + " NotifyUrl Record : %s", dto.toString()));
        var path = Paths.get(MODEL + "-" + DateUtils.yyyyMMdd(DateNewUtils.now()) + ".txt");
        if (!Files.exists(path)) Files.createFile(path);
        Files.write(path, dto.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        // 判定签名规则是否正确
        if (null == plat) PayAbstractFactory.init(MODEL);
        if (!ParamSignUtil.verifySign(dto, plat.getBusinessPwd()) || !dto.get("status").equals("1")) {
            log.info("=====" + MODEL + " NotifyUrl : 校验 或 status失败 ");
            Files.write(path, STATUS.FAIL.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            return STATUS.FAIL.getCode();
        }
        // 判定OrderId 是否存在
        CoinDeposit coinDeposit = coinDepositServiceImpl.lambdaQuery()
                .eq(CoinDeposit::getOrderId, dto.get(ORDER_ID))
                .orderByDesc(CoinDeposit::getId)
                .one();
        if (null == coinDeposit) {
            log.info("=====" + MODEL + " NotifyUrl : 订单号不存在");
            Files.write(path, STATUS.FAIL.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            return STATUS.FAIL.getCode();
        } else if (coinDeposit.getStatus() != 0) {
            Files.write(path, STATUS.FAIL.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            log.info("=====" + MODEL + " NotifyUrl : 重复回调");
            return STATUS.FAIL.getCode();
        }

        try {
            // 正常业务处理流程
            thirdPayBase.updateCoinDeposit(coinDeposit.getId(), new BigDecimal(dto.get(AMOUNT)));
        } catch (Exception e) {
            Files.write(path, STATUS.FAIL.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            log.info("=====" + MODEL + "  更新数据失败 :" + e.toString());
        }
        Files.write(path, STATUS.SUCCESS.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        return STATUS.SUCCESS.getCode();
    }

    /**
     * 代付订单回调（重复订单仍返回成功）
     *
     * @param dto
     * @return
     */
    @SneakyThrows
    public String withdrawNotifyUrl(Map<String, String> dto) {
        // 记录回调的数据
        var path = Paths.get(MODEL + "-" + DateUtils.yyyyMMdd(DateNewUtils.now()) + ".txt");
        if (!Files.exists(path)) Files.createFile(path);
        Files.write(path, dto.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        // 判定签名规则是否正确
        if (null == plat) PayAbstractFactory.init(MODEL);
        var status = dto.get("status");
        log.info(String.format("=====" + MODEL + " withdrawNotifyUrl Record : %s", dto));
        if (!ParamSignUtil.verifySign(dto, plat.getBusinessPwd()) || status.equals("0")) {
            log.info("=====" + MODEL + " NotifyUrl : 校验 或 status失败 ");
            Files.write(path, STATUS.FAIL.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            return STATUS.FAIL.getCode();
        }
        // 判定OrderId 是否存在
        CoinOnlineWithdrawal coinOnlineWithdrawal = coinOnlineWithdrawalServiceImpl.lambdaQuery()
                .eq(CoinOnlineWithdrawal::getOrderId, dto.get(PAYOUT_ORDER_ID))
                .orderByDesc(CoinOnlineWithdrawal::getId)
                .one();
        if (null == coinOnlineWithdrawal) {
            log.info("=====" + MODEL + "NotifyUrl : 订单号不存在");
            return STATUS.FAIL.getCode();
        } else if (coinOnlineWithdrawal.getStatus() != 0) {
            log.info("=====" + MODEL + "NotifyUrl : 重复回调");
            return STATUS.SUCCESS.getCode();
        }

        try {
            if (status.equals("1")) {
                coinOnlineWithdrawalServiceImpl.lambdaUpdate().set(CoinOnlineWithdrawal::getStatus, 1).eq(CoinOnlineWithdrawal::getOrderId, coinOnlineWithdrawal.getOrderId()).update();
                coinWithdrawalServiceImpl.lambdaUpdate().set(CoinWithdrawal::getStatus, 1).eq(CoinWithdrawal::getId, coinOnlineWithdrawal.getWithdrawalOrderId()).update();
                updateUserCoinBase.updateCoinLog(Long.valueOf(coinOnlineWithdrawal.getWithdrawalOrderId()), 2, 1, DateUtils.getCurrentTime());
                log.info("修改日志状态成功!");
                return STATUS.SUCCESS.getCode();
            } else if (status.equals("2")) {
                coinOnlineWithdrawalServiceImpl.lambdaUpdate().set(CoinOnlineWithdrawal::getStatus, 2).eq(CoinOnlineWithdrawal::getOrderId, coinOnlineWithdrawal.getOrderId()).update();
                return STATUS.SUCCESS.getCode();
            }
        } catch (Exception e) {
            Files.write(path, STATUS.FAIL.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            log.info("=====" + MODEL + "  更新数据失败 :" + e.toString());
            return STATUS.FAIL.getCode();
        }
        Files.write(path, STATUS.SUCCESS.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        return STATUS.FAIL.getCode();
    }
}
