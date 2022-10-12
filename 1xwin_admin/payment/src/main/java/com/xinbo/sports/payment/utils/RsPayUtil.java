package com.xinbo.sports.payment.utils;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.xinbo.sports.payment.io.PayParams.ACCOUNT_HOLDER;
import static com.xinbo.sports.payment.io.PayParams.BANK_CARD_ACCOUNT;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xinbo.sports.dao.generator.po.CoinDeposit;
import com.xinbo.sports.dao.generator.po.CoinOnlineWithdrawal;
import com.xinbo.sports.dao.generator.po.CoinWithdrawal;
import com.xinbo.sports.dao.generator.service.CoinDepositService;
import com.xinbo.sports.dao.generator.service.CoinOnlineWithdrawalService;
import com.xinbo.sports.dao.generator.service.CoinWithdrawalService;
import com.xinbo.sports.dao.generator.service.UserService;
import com.xinbo.sports.payment.base.ThirdPayBase;
import com.xinbo.sports.payment.io.EasyPayParams;
import com.xinbo.sports.payment.io.PayParams.OnlinePayReqDto;
import com.xinbo.sports.payment.io.PayParams.OnlinePayoutReqDto;
import com.xinbo.sports.payment.io.PayParams.STATUS;
import com.xinbo.sports.payment.io.PayParams.UrlEnum;
import com.xinbo.sports.payment.io.PayParams.WithdrawalNotifyResDto;
import com.xinbo.sports.payment.io.RsPayParams;
import com.xinbo.sports.service.base.UpdateUserCoinBase;
import com.xinbo.sports.service.cache.KeyConstant;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.bo.DictionaryBo;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.DateUtils;
import com.xinbo.sports.utils.HttpUtils;
import com.xinbo.sports.utils.IpUtils;
import com.xinbo.sports.utils.components.response.CodeInfo;
import com.xxl.job.core.log.XxlJobLogger;
import com.xxl.job.core.util.IpUtil;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author: David
 * @date: 14/07/2020
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RsPayUtil implements PayAbstractFactory {
    private final ThirdPayBase thirdPayBase;
    private final CoinDepositService coinDepositServiceImpl;
    private final ConfigCache configCache;
    private final CoinOnlineWithdrawalService coinOnlineWithdrawalServiceImpl;
    private final CoinWithdrawalService coinWithdrawalServiceImpl;
    private final UpdateUserCoinBase updateUserCoinBase;
    private static final String MODEL = "RsPayUtil";
    @Setter
    public RsPayParams.PayPlat plat;

    private static final int STATUS_SUCCESS=1;

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
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        //  计算金额 单位(分)
        BigDecimal amount = coinDeposit.getCoin();
        DecimalFormat df = new DecimalFormat("0.00");
        // 组装数据
        SortedMap<String, String> paramsMap = new TreeMap<>();
        paramsMap.put("appId", plat.getBusinessCode());
        paramsMap.put("outOrderNo", orderSn);
        paramsMap.put("applyDate", DateNewUtils.getNowFormatTime(DateNewUtils.Format.yyyy_MM_dd_HH_mm_ss));
        paramsMap.put("channel", "912");
        paramsMap.put("notifyUrl", plat.getNotifyUrl() + UrlEnum.DEPOSIT_NOTIFY_URL.getUrl());
        paramsMap.put("amount", df.format(amount));
        paramsMap.put("userId", coinDeposit.getUid().toString());
        paramsMap.put("clientIp", IpUtils.getIpAddr(request));
        paramsMap.put("contact", "9682177681");
        paramsMap.put("email", RandomUtils.nextLong(00000000L,99999999L)+"@gmail.com");
        paramsMap.put("contactName", "Lin");
        paramsMap.put("sign", getSign(paramsMap, plat.getBusinessPwd()));
        // 网关、快捷下单接口地址
        String payUrl = plat.getUrl();
         String param = stringAppend(paramsMap);
        return Map.of("method", "POST", "url", payUrl+"?"+param, "category", 3);
    }


    private static String getSign(Map<String, String> map, String signKey) {
        String[] keySet = map.keySet().toArray(new String[0]);
        Arrays.sort(keySet);
        StringBuilder builder = new StringBuilder();
        for(String key: keySet){
            if(null != map.get(key) && !"".equals(map.get(key))){
                String value = map.get(key);
                if(StringUtils.isEmpty(value)){
                    continue;
                }
                builder.append(key).append("=").append(map.get(key)).append("&");
            }
        }
        builder.append("key=").append(signKey);
        String md5 = DigestUtils.md5DigestAsHex(builder.toString().getBytes()).toUpperCase();
        return md5;
    }



    private static String stringAppend(Map<String, String> map) {
        String[] keySet = map.keySet().toArray(new String[0]);
        Arrays.sort(keySet);
        StringBuilder builder = new StringBuilder();
        for(String key: keySet){
            if(null != map.get(key) && !"".equals(map.get(key))){
                String value = map.get(key);
                if(StringUtils.isEmpty(value)){
                    continue;
                }
                builder.append(key).append("=").append(map.get(key)).append("&");
            }
        }
         String substring = builder.substring(0, builder.length() - 1);
        return substring;
    }

    /**
     * 创建代付订单
     *
     * @param dto 入参
     * @return
     */
    @Override
    public WithdrawalNotifyResDto onlineWithdraw(OnlinePayoutReqDto dto) {
        // 获取当前订单ID
        CoinOnlineWithdrawal coinOnlineWithdrawal = coinOnlineWithdrawalServiceImpl.getOne(new QueryWrapper<CoinOnlineWithdrawal>().eq("order_id", dto.getOrderId()));
        if (null == coinOnlineWithdrawal) {
            throw new BusinessException(CodeInfo.PAY_PLAT_ORDER_ID_INVALID);
        }

        //  计算金额 单位(分)
        BigDecimal amount = coinOnlineWithdrawal.getCoin();
        // 组装数据
        HashMap<String, String> paramsMap = new HashMap<>(16);
        DecimalFormat df = new DecimalFormat("0.00");
        String format = df.format(amount);
        var bankInfo = parseObject(coinOnlineWithdrawal.getBankInfo());
        var currency = EnumUtils.getEnumIgnoreCase(EasyPayParams.CURRENCY.class, configCache.getCountry());

        paramsMap.put("appId", plat.getBusinessCode());
        paramsMap.put("outOrderNo", dto.getOrderId());
        paramsMap.put("applyDate", DateNewUtils.getNowFormatTime(DateNewUtils.Format.yyyy_MM_dd_HH_mm_ss));
        paramsMap.put("channel", "912");
        paramsMap.put("notifyUrl", plat.getNotifyUrl() + UrlEnum.WITHDRAW_NOTIFY_URL.getUrl());
        paramsMap.put("amount", format);
        paramsMap.put("mode", "NEFT");
        paramsMap.put("account", bankInfo.getString(BANK_CARD_ACCOUNT));
        paramsMap.put("name", bankInfo.getString(ACCOUNT_HOLDER));
        paramsMap.put("accountIFSC", bankInfo.getString("mark"));
        paramsMap.put("userId", coinOnlineWithdrawal.getUid().toString());
        paramsMap.put("clientIp", IpUtil.getIp());
        paramsMap.put("sign", getSign(paramsMap, plat.getBusinessPwd()));

        // 网关、快捷下单接口地址
        String payoutUrl = plat.getWithdrawUrl();


        String result = null;
        try {
            result = HttpUtils.doPost(payoutUrl, JSON.toJSONString(paramsMap), "PaymentReqDto");
            JSONObject jsonObject = JSON.parseObject(result);
            if (jsonObject.getString("statusCode").equalsIgnoreCase("00")) {
                return WithdrawalNotifyResDto.builder().code(true).msg(jsonObject.getString("message")).build();
            }
            coinOnlineWithdrawalServiceImpl.remove(new QueryWrapper<CoinOnlineWithdrawal>().eq("order_id", dto.getOrderId()));
            throw new BusinessException(jsonObject.getString("message"));

        } catch (Exception e) {
            coinOnlineWithdrawalServiceImpl.lambdaUpdate().set(CoinOnlineWithdrawal::getStatus, 2).eq(CoinOnlineWithdrawal::getOrderId, dto.getOrderId()).update();
            log.error(e.toString());
            log.error("=============================================");
            log.error("Class: " + "RsPayUtil" + "\n");
            log.error("URL: " + payoutUrl + "\n");
            log.error("postData: " + JSON.toJSONString(paramsMap) + "\n");
            throw new BusinessException(e.getMessage());
        }
    }


    /**
     * 通用请求对象
     *
     * @param category 字典类型
     * @return DictionaryBo
     */
    private DictionaryBo buildDictionaryBo(String category) {
        return DictionaryBo.builder()
                // 类型: 0-全部 1-前端 2-后台
                .isShowList(List.of(0, 2))
                .category(category)
                // 查询backend(后台)字典
                .dictHashKey(KeyConstant.DICTIONARY_BACKEND_HASH)
                .build();
    }

    /**
     * 异步回调接口
     *
     * @param dto 入参
     * @return success-成功 fail-失败
     */
    @SneakyThrows
    public String notifyUrl(@NotNull RsPayParams.NotifyUrlReqDto dto) {
        // 记录回调的数据
        log.info(String.format("=====EasyPay NotifyUrl Record : %s", dto.toString()));
        var path = Paths.get("RsPay-" + DateUtils.yyyyMMdd(DateNewUtils.now()) + ".txt");
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
        if (null == plat) {
            PayAbstractFactory.init(MODEL);
        }
        Files.write(path, dto.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        SortedMap<String, String> paramsMap = new TreeMap<>();
        paramsMap.put("appId", dto.getAppId());
        paramsMap.put("outOrderNo", dto.getOutOrderNo());
        paramsMap.put("amount", dto.getAmount());
        paramsMap.put("transactionId", dto.getTransactionId());
        paramsMap.put("paidDate", dto.getPaidDate());
        paramsMap.put("statusCode", dto.getStatusCode());
        paramsMap.put("payStatus", dto.getPayStatus().toString());
        paramsMap.put("message", dto.getMessage());
        String sign = getSign(paramsMap, plat.getBusinessPwd());
        if (!dto.getSign().equals(sign)) {
            Files.write(path, "=====RsPay NotifyUrl : 校验 或 Status 失败".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            return STATUS.FAIL.getCode();
        }

        // 判定OrderId 是否存在
        CoinDeposit coinDeposit = coinDepositServiceImpl.lambdaQuery()
                .eq(CoinDeposit::getOrderId, dto.getOutOrderNo())
                .orderByDesc(CoinDeposit::getId)
                .one();
        if (null == coinDeposit) {
            log.info("=====RsPay NotifyUrl : 订单号不存在");
            Files.write(path, "======RsPay NotifyUrl : 订单号不存在".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            return STATUS.FAIL.getCode();
        } else if (coinDeposit.getStatus() != 0) {
            log.info("=====RsPay NotifyUrl : 重复回调");
            Files.write(path, "======RsPay NotifyUrl : 重复回调".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            return STATUS.FAIL.getCode();
        }

        try {
            if(1==dto.getPayStatus()){
                // 正常业务处理流程
                thirdPayBase.updateCoinDeposit(coinDeposit.getId(), new BigDecimal(dto.getAmount()));
            }
          /*  if(3==dto.getPayStatus()){
                coinDepositServiceImpl.lambdaUpdate().set(CoinDeposit::getStatus,3).eq(CoinDeposit::getId,coinDeposit.getId()).update();
            }*/
        } catch (Exception e) {
            log.info("=====RsPay 更新数据失败 :" + e.toString());
            Files.write(path, "======RsPay NotifyUrl : 重复回调".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        }
        Files.write(path, STATUS.SUCCESS.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        return "OK";
    }


    @SneakyThrows
    public String withdrawNotifyUrl(RsPayParams.DFNotifyUrlReqDto dto) {
        // 记录回调的数据
        var path = Paths.get(MODEL + "-" + DateUtils.yyyyMMdd(DateNewUtils.now()) + ".txt");
        if (!Files.exists(path)) Files.createFile(path);
        Files.write(path, dto.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        if (null == plat) PayAbstractFactory.init(MODEL);
        log.info(String.format("=====" + MODEL + " withdrawNotifyUrl Record : %s", dto));

        SortedMap<String, String> paramsMap = new TreeMap<>();
        paramsMap.put("statusCode", dto.getStatusCode());
        paramsMap.put("message", dto.getMessage());
        paramsMap.put("appId", dto.getAppId());
        paramsMap.put("outOrderNo", dto.getOutOrderNo());
        paramsMap.put("amount", dto.getAmount());
        paramsMap.put("transactionId", dto.getTransactionId());
        paramsMap.put("paidDate", dto.getPaidDate());
        paramsMap.put("payStatus", dto.getPayStatus().toString());
        String sign = getSign(paramsMap, plat.getBusinessPwd());
        if (!dto.getSign().equals(sign)) {
            Files.write(path, "=====RsPay 代付NotifyUrl : 校验 或 Status 失败".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            return STATUS.FAIL.getCode();

        }
        // 判定OrderId 是否存在
        CoinOnlineWithdrawal coinOnlineWithdrawal = coinOnlineWithdrawalServiceImpl.lambdaQuery()
                .eq(CoinOnlineWithdrawal::getOrderId, dto.getOutOrderNo())
                .orderByDesc(CoinOnlineWithdrawal::getId)
                .one();
        if (null == coinOnlineWithdrawal) {
            log.info("=====" + MODEL + "NotifyUrl : 订单号不存在");
            return STATUS.FAIL.getCode();
        } else if (coinOnlineWithdrawal.getStatus() != 0) {
            log.info("=====" + MODEL + "NotifyUrl : 重复回调");
            return "OK";
        }

        try {
            if (dto.getPayStatus() == 11) {
                coinOnlineWithdrawalServiceImpl.lambdaUpdate().set(CoinOnlineWithdrawal::getStatus, 1).eq(CoinOnlineWithdrawal::getOrderId, coinOnlineWithdrawal.getOrderId()).update();
                coinWithdrawalServiceImpl.lambdaUpdate().set(CoinWithdrawal::getStatus, 1).eq(CoinWithdrawal::getId, coinOnlineWithdrawal.getWithdrawalOrderId()).update();
                updateUserCoinBase.updateCoinLog(Long.valueOf(coinOnlineWithdrawal.getWithdrawalOrderId()), 2, 1, DateUtils.getCurrentTime());
                log.info("修改日志状态成功!");
                return "OK";
            } else if (dto.getPayStatus() == 12) {
                coinOnlineWithdrawalServiceImpl.lambdaUpdate().set(CoinOnlineWithdrawal::getStatus, 2).eq(CoinOnlineWithdrawal::getOrderId, coinOnlineWithdrawal.getOrderId()).update();
                return "OK";
            }
        } catch (Exception e) {
            Files.write(path, STATUS.FAIL.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            log.info("=====" + MODEL + "  更新数据失败 :" + e.toString());
            return STATUS.FAIL.getCode();
        }
        Files.write(path, STATUS.SUCCESS.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        return STATUS.FAIL.getCode();
    }

@Override
    public Boolean checkPaymentStatus(String orderId) {
        if (null == plat) PayAbstractFactory.init(MODEL);
        var url = plat.getUrl();
        try {
            Map paramsMap = new HashMap<>();
            CoinOnlineWithdrawal withdrawalList = coinOnlineWithdrawalServiceImpl.lambdaQuery().eq(CoinOnlineWithdrawal::getOrderId, orderId).one();
            if (withdrawalList == null) {
                XxlJobLogger.log(MODEL + "当前已稽核订单可处理");
                return true;
            }
            paramsMap.put("out_trade_no", orderId);
            paramsMap.put("mchid", plat.getBusinessCode());
            String sign = getSign(paramsMap, plat.getBusinessPwd());
            paramsMap.put("pay_md5sign", sign);
            HashMap map = new HashMap();
            map.put("Content-Type", "application/x-www-form-urlencoded");
            var result = HttpUtils.doPost(url, JSONObject.parseObject(JSON.toJSONString(paramsMap)), map, "PaymentReqDto");
            JSONObject jsonObject = JSON.parseObject(result);
            if (jsonObject.getString("status").equalsIgnoreCase("success")) {

            } else if (jsonObject.getInteger("status") == 1) {
                coinOnlineWithdrawalServiceImpl.lambdaUpdate().set(CoinOnlineWithdrawal::getStatus, 1).eq(CoinOnlineWithdrawal::getOrderId, orderId).update();
                coinWithdrawalServiceImpl.lambdaUpdate().set(CoinWithdrawal::getStatus, 1).eq(CoinWithdrawal::getId, withdrawalList.getWithdrawalOrderId()).update();
                updateUserCoinBase.updateCoinLog(Long.valueOf(withdrawalList.getWithdrawalOrderId()), 2, 1, DateUtils.getCurrentTime());
                log.info("修改日志状态成功!");
            } else if (jsonObject.getInteger("status") == 2 || jsonObject.getInteger("status") == 3) {
                coinOnlineWithdrawalServiceImpl.lambdaUpdate().set(CoinOnlineWithdrawal::getStatus, 2).eq(CoinOnlineWithdrawal::getOrderId, orderId).update();
            }
            return true;
        } catch (Exception e) {
            log.error(e.toString());
            log.error("=============================================");
            log.error("Class: " + "EasyPayUtil" + "\n");
            log.error("URL: " + url + "\n");
            e.printStackTrace();
            throw new BusinessException(CodeInfo.PAY_PLAT_UPDATE_ORDER_FAILURE);
        }
    }
}
