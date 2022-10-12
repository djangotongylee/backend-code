package com.xinbo.sports.payment.utils;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.xinbo.sports.payment.io.EasyPayNewParams.EasyPayMethodEnum.GATEWAY;
import static com.xinbo.sports.payment.io.EasyPayNewParams.EasyPayMethodEnum.QUERY;
import static com.xinbo.sports.payment.io.PayParams.ACCOUNT_HOLDER;
import static com.xinbo.sports.payment.io.PayParams.BANK_CARD_ACCOUNT;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xinbo.sports.dao.generator.po.CoinDeposit;
import com.xinbo.sports.dao.generator.po.CoinOnlineWithdrawal;
import com.xinbo.sports.dao.generator.po.CoinWithdrawal;
import com.xinbo.sports.dao.generator.po.User;
import com.xinbo.sports.dao.generator.service.CoinDepositService;
import com.xinbo.sports.dao.generator.service.CoinOnlineWithdrawalService;
import com.xinbo.sports.dao.generator.service.CoinWithdrawalService;
import com.xinbo.sports.dao.generator.service.UserService;
import com.xinbo.sports.payment.base.ThirdPayBase;
import com.xinbo.sports.payment.io.EasyPayNewParams;
import com.xinbo.sports.payment.io.PayParams.OnlinePayReqDto;
import com.xinbo.sports.payment.io.PayParams.OnlinePayoutReqDto;
import com.xinbo.sports.payment.io.PayParams.STATUS;
import com.xinbo.sports.payment.io.PayParams.UrlEnum;
import com.xinbo.sports.payment.io.PayParams.WithdrawalNotifyResDto;
import com.xinbo.sports.service.base.UpdateUserCoinBase;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.DateUtils;
import com.xinbo.sports.utils.HttpUtils;
import com.xinbo.sports.utils.components.response.CodeInfo;
import com.xxl.job.core.log.XxlJobLogger;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * @author: David
 * @date: 14/07/2020
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EasyNewPayUtil implements PayAbstractFactory {

  private final ThirdPayBase thirdPayBase;
  private final CoinDepositService coinDepositServiceImpl;
  private final CoinOnlineWithdrawalService coinOnlineWithdrawalServiceImpl;
  private final CoinWithdrawalService coinWithdrawalServiceImpl;
  private final UpdateUserCoinBase updateUserCoinBase;
  private final UserService userServiceImpl;
  private static final String MODEL = "EasyNewPayUtil";
  @Setter
  public EasyPayNewParams.PayPlat plat;

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
    boolean update =
        coinDepositServiceImpl
            .lambdaUpdate()
            .eq(CoinDeposit::getId, dto.getDepositId())
            .set(CoinDeposit::getOrderId, orderSn)
            .update();
    if (!update) {
      throw new BusinessException(CodeInfo.PAY_PLAT_UPDATE_ORDER_ID_INVALID);
    }
    DecimalFormat df = new DecimalFormat("0.00");
    String format = df.format(coinDeposit.getCoin());
    // 组装数据
    SortedMap<String, String> paramsMap = new TreeMap<>();
    paramsMap.put("merchantId", plat.getBusinessCode());
    paramsMap.put("orderId", orderSn);
    paramsMap.put("coin", format);
    paramsMap.put("productId", dto.getChannel());
    paramsMap.put("notifyUrl", plat.getNotifyUrl() + UrlEnum.DEPOSIT_NOTIFY_URL.getUrl());
    paramsMap.put("returnUrl", plat.getReturnUrl());
    paramsMap.put("sign", getSign(paramsMap, plat.getBusinessPwd()));
    paramsMap.put("goods", orderSn);
    paramsMap.put("attach", orderSn);

    // 网关、快捷下单接口地址
    String payUrl = plat.getUrl() + GATEWAY.getUrl();
    String result = null;

    String url = null;
    int category = 1;
    try {
      result =
          HttpUtils.doPost(
              payUrl, JSON.toJSONString(paramsMap), "PaymentReqDto");
      JSONObject jsonObject = JSON.parseObject(result);
      if (!(jsonObject.getInteger("code") == 0)) {
        log.info("======EasyPayNew: 支付失败 : " + jsonObject.toJSONString());
        throw new BusinessException(jsonObject.getString("msg"));
      } else {
        JSONObject data = jsonObject.getJSONObject("data");
        url = data.getString("url");
        Integer integer = data.getInteger("category");
        if (integer == null) {
          category = 1;
        } else {
          category = data.getInteger("category").equals(2) ? 4 : 1;
        }
      }
    } catch (Exception e) {
      log.error(e.toString());
      log.error("===========EasyPayNewUtil URL: " + payUrl + "\n");
      log.error("===========EasyPayNew"
          + "Util postData: " + JSON.toJSONString(paramsMap) + "\n");
      throw new BusinessException(e.getMessage());
    }
    return Map.of("method", "GET", "url", url, "category", category);
  }

  private String getSign(Map<String, String> map, String signKey) {
    if (map == null) {
      return null;
    }
    List<String> keyList = new ArrayList<>(map.keySet());
    Collections.sort(keyList);
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < keyList.size(); i++) {
      String key = keyList.get(i);
      Object value = map.get(key);
      sb.append(key + "=" + value + "&");
    }
    String signStr = sb.append("key=") + signKey;
    String md5Str = DigestUtils.md5DigestAsHex(signStr.getBytes());
    return md5Str.toUpperCase();
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
    CoinOnlineWithdrawal coinOnlineWithdrawal =
        coinOnlineWithdrawalServiceImpl.getOne(
            new QueryWrapper<CoinOnlineWithdrawal>().eq("order_id", dto.getOrderId()));
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
    paramsMap.put("merchantId", plat.getBusinessCode());
    paramsMap.put("outTradeNo", dto.getOrderId());
    paramsMap.put("coin", format);
    paramsMap.put("bankName", dto.getOpenAccountBank());
    paramsMap.put("callbackUrl", plat.getNotifyUrl() + UrlEnum.WITHDRAW_NOTIFY_URL.getUrl());
    paramsMap.put("city", "city");
    paramsMap.put("ifscCode", bankInfo.getString("mark"));
    paramsMap.put("province", "province");
    paramsMap.put("bankBranchName", "city");
    paramsMap.put("bankAccountName", bankInfo.getString(ACCOUNT_HOLDER));
    paramsMap.put("bankCardNum", bankInfo.getString(BANK_CARD_ACCOUNT));
    paramsMap.put("sign", getSign(paramsMap, plat.getBusinessPwd()));
    // 网关、快捷下单接口地址
    String payoutUrl = plat.getWithdrawUrl();

    String result = null;
    try {
      result =
          HttpUtils.doPost(
              payoutUrl,
              JSON.toJSONString(paramsMap),
              "PaymentReqDto");
      JSONObject jsonObject = JSON.parseObject(result);
      if (jsonObject.getInteger("code")==0) {
        return WithdrawalNotifyResDto.builder().code(true).msg(jsonObject.getString("message")).build();
      }
      coinOnlineWithdrawalServiceImpl.remove(
          new QueryWrapper<CoinOnlineWithdrawal>().eq("order_id", dto.getOrderId()));
      throw new BusinessException(jsonObject.getString("message"));

    } catch (Exception e) {
      coinOnlineWithdrawalServiceImpl
          .lambdaUpdate()
          .set(CoinOnlineWithdrawal::getStatus, 2)
          .eq(CoinOnlineWithdrawal::getOrderId, dto.getOrderId())
          .update();
      log.error(e.toString());
      log.error("=============================================");
      log.error("Class: " + "EasyPayNewUtil" + "\n");
      log.error("URL: " + payoutUrl + "\n");
      log.error("postData: " + JSON.toJSONString(paramsMap) + "\n");
      throw new BusinessException(e.getMessage());
    }
  }



  /**
   * 异步回调接口
   *
   * @param dto 入参
   * @return success-成功 fail-失败
   */
  @SneakyThrows
  public String notifyUrl(@NotNull EasyPayNewParams.NotifyUrlReqDto dto) {
    // 记录回调的数据
    log.info(String.format("=====EasyPayNew NotifyUrl Record : %s", dto.toString()));
    var path = Paths.get("EasyPayNew-" + DateUtils.yyyyMMdd(DateNewUtils.now()) + ".txt");
    if (!Files.exists(path)) {
      Files.createFile(path);
    }
    if (null == plat) {
      PayAbstractFactory.init(MODEL);
    }
    Files.write(path, dto.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
    SortedMap<String, String> paramsMap = new TreeMap<>();
    paramsMap.put("code", dto.getCode().toString());
    paramsMap.put("merchantId", dto.getMerchantId());
    paramsMap.put("message", dto.getMessage());
    paramsMap.put("outTradeNo", dto.getOutTradeNo());
    paramsMap.put("coin", dto.getCoin().toString());
    String sign = getSign(paramsMap, plat.getBusinessPwd());
    if (!dto.getSign().equals(sign)) {
      Files.write(
          path,
          "=====EasyPayNew NotifyUrl : 校验 或 Status 失败".getBytes(StandardCharsets.UTF_8),
          StandardOpenOption.APPEND);
      return STATUS.FAIL.getCode();
    }

    // 判定OrderId 是否存在
    CoinDeposit coinDeposit =
        coinDepositServiceImpl
            .lambdaQuery()
            .eq(CoinDeposit::getOrderId, dto.getOutTradeNo())
            .orderByDesc(CoinDeposit::getId)
            .one();
    if (null == coinDeposit) {
      log.info("=====EasyPayNew NotifyUrl : 订单号不存在");
      Files.write(
          path,
          "======EasyPayNew NotifyUrl : 订单号不存在".getBytes(StandardCharsets.UTF_8),
          StandardOpenOption.APPEND);
      return STATUS.FAIL.getCode();
    } else if (coinDeposit.getStatus() != 0) {
      log.info("=====EasyPayNew NotifyUrl : 重复回调");
      Files.write(
          path,
          "======EasyPayNew NotifyUrl : 重复回调".getBytes(StandardCharsets.UTF_8),
          StandardOpenOption.APPEND);
      return "OK";
    }

    try {
      try {
        if (dto.getCode() == 1) {
          // 正常业务处理流程
          thirdPayBase.updateCoinDeposit(
              coinDeposit.getId(), coinDeposit.getCoin());
        }
        if (dto.getCode() == 2) {
          // 支付失败
          coinDepositServiceImpl.lambdaUpdate().set(CoinDeposit::getStatus, 3)
              .eq(CoinDeposit::getId, coinDeposit.getId()).update();
        }
      } catch (Exception e) {
        log.info("=====EasyPayNew 更新数据失败 :" + e.toString());
        Files.write(
            path,
            "======EasyPayNew NotifyUrl : 重复回调".getBytes(StandardCharsets.UTF_8),
            StandardOpenOption.APPEND);
      }

    } catch (Exception e) {
      log.info("=====EasyPayNew 更新数据失败 :" + e.toString());
      Files.write(
          path,
          "======EasyPayNew NotifyUrl : 重复回调".getBytes(StandardCharsets.UTF_8),
          StandardOpenOption.APPEND);
    }
    Files.write(
        path, STATUS.SUCCESS.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
    return "OK";
  }

  @SneakyThrows
  public String withdrawNotifyUrl(EasyPayNewParams.DFNotifyUrlReqDto dto) {
    // 记录回调的数据
    var path = Paths.get(MODEL + "-" + DateUtils.yyyyMMdd(DateNewUtils.now()) + ".txt");
    if (!Files.exists(path)) {
      Files.createFile(path);
    }
    Files.write(path, dto.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
    if (null == plat) {
      PayAbstractFactory.init(MODEL);
    }
    log.info(String.format("=====" + MODEL + " withdrawNotifyUrl Record : %s", dto));

    SortedMap<String, String> paramsMap = new TreeMap<>();
    paramsMap.put("code", dto.getCode().toString());
    paramsMap.put("message", dto.getMessage());
    paramsMap.put("outTradeNo", dto.getOutTradeNo());
    paramsMap.put("coin", dto.getCoin().toString());
    paramsMap.put("merchantId", dto.getMerchantId());
    String sign = getSign(paramsMap, plat.getBusinessPwd());
    if (!dto.getSign().equals(sign)) {
      Files.write(
          path,
          "=====EasyPayNew 代付NotifyUrl : 校验 或 Status 失败".getBytes(StandardCharsets.UTF_8),
          StandardOpenOption.APPEND);
      return STATUS.FAIL.getCode();
    }
    // 判定OrderId 是否存在
    CoinOnlineWithdrawal coinOnlineWithdrawal =
        coinOnlineWithdrawalServiceImpl
            .lambdaQuery()
            .eq(CoinOnlineWithdrawal::getOrderId, dto.getOutTradeNo())
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
      userServiceImpl
          .lambdaUpdate()
          .setSql("fcoin = fcoin -" + coinOnlineWithdrawal.getCoin())
          .set(User::getUpdatedAt, DateUtils.getCurrentTime())
          .eq(User::getId, coinOnlineWithdrawal.getUid())
          .update();
      if (dto.getCode() == 1) {
        coinOnlineWithdrawalServiceImpl
            .lambdaUpdate()
            .set(CoinOnlineWithdrawal::getStatus, 1)
            .eq(CoinOnlineWithdrawal::getOrderId, coinOnlineWithdrawal.getOrderId())
            .update();
        coinWithdrawalServiceImpl
            .lambdaUpdate()
            .set(CoinWithdrawal::getStatus, 1)
            .eq(CoinWithdrawal::getId, coinOnlineWithdrawal.getWithdrawalOrderId())
            .update();
        updateUserCoinBase.updateCoinLog(
            Long.valueOf(coinOnlineWithdrawal.getWithdrawalOrderId()),
            2,
            1,
            DateUtils.getCurrentTime());
        log.info("修改日志状态成功!");
        return "OK";
      } else if (dto.getCode() == 2 ) {
        coinOnlineWithdrawalServiceImpl
            .lambdaUpdate()
            .set(CoinOnlineWithdrawal::getStatus, 2)
            .eq(CoinOnlineWithdrawal::getOrderId, coinOnlineWithdrawal.getOrderId())
            .update();
        return "OK";
      }
    } catch (Exception e) {
      Files.write(
          path,
          STATUS.FAIL.getCode().getBytes(StandardCharsets.UTF_8),
          StandardOpenOption.APPEND);
      log.info("=====" + MODEL + "  更新数据失败 :" + e.toString());
      return STATUS.FAIL.getCode();
    }
    Files.write(
        path,
        STATUS.SUCCESS.getCode().getBytes(StandardCharsets.UTF_8),
        StandardOpenOption.APPEND);
    return STATUS.FAIL.getCode();
  }

  public Boolean checkPaymentStatus(String orderId) {
    if (null == plat) {
      PayAbstractFactory.init(MODEL);
    }
    var url = plat.getUrl() + QUERY.getUrl();
    try {
      Map paramsMap = new HashMap<>();
      CoinOnlineWithdrawal withdrawalList =
          coinOnlineWithdrawalServiceImpl
              .lambdaQuery()
              .eq(CoinOnlineWithdrawal::getOrderId, orderId)
              .one();
      if (withdrawalList == null) {
        XxlJobLogger.log(MODEL + "当前已稽核订单可处理");
        return true;
      }
      paramsMap.put("merchantId", plat.getBusinessCode());
      paramsMap.put("outTradeNo", orderId);
      String sign = getSign(paramsMap, plat.getBusinessPwd());
      paramsMap.put("sign", sign);
      var result =
          HttpUtils.doPost(
              url, JSON.toJSONString(paramsMap), "PaymentReqDto");
      JSONObject jsonObject = JSON.parseObject(result);
      if (jsonObject.getString("status").equalsIgnoreCase("success")) {

      } else if (jsonObject.getInteger("code") == 1) {
        coinOnlineWithdrawalServiceImpl
            .lambdaUpdate()
            .set(CoinOnlineWithdrawal::getStatus, 1)
            .eq(CoinOnlineWithdrawal::getOrderId, orderId)
            .update();
        coinWithdrawalServiceImpl
            .lambdaUpdate()
            .set(CoinWithdrawal::getStatus, 1)
            .eq(CoinWithdrawal::getId, withdrawalList.getWithdrawalOrderId())
            .update();
        updateUserCoinBase.updateCoinLog(
            Long.valueOf(withdrawalList.getWithdrawalOrderId()), 2, 1, DateUtils.getCurrentTime());
        log.info("修改日志状态成功!");
      } else if (jsonObject.getInteger("code") == 2 ) {
        coinOnlineWithdrawalServiceImpl
            .lambdaUpdate()
            .set(CoinOnlineWithdrawal::getStatus, 2)
            .eq(CoinOnlineWithdrawal::getOrderId, orderId)
            .update();
      }
      return true;
    } catch (Exception e) {
      log.error(e.toString());
      log.error("=============================================");
      log.error("Class: " + "EasyPayNewUtil" + "\n");
      log.error("URL: " + url + "\n");
      e.printStackTrace();
      throw new BusinessException(CodeInfo.PAY_PLAT_UPDATE_ORDER_FAILURE);
    }
  }
}
