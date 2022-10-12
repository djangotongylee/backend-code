package com.xinbo.sports.payment.utils;

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
import com.xinbo.sports.payment.io.EasyPayParams;
import com.xinbo.sports.payment.io.PayParams;
import com.xinbo.sports.payment.io.PayParams.*;
import com.xinbo.sports.service.base.UpdateUserCoinBase;
import com.xinbo.sports.service.cache.KeyConstant;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.service.cache.redis.DictionaryCache;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.bo.DictionaryBo;
import com.xinbo.sports.service.io.dto.BaseParams;
import com.xinbo.sports.service.thread.ThreadHeaderLocalData;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.DateUtils;
import com.xinbo.sports.utils.HttpUtils;
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
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.*;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.xinbo.sports.payment.io.EasyPayParams.EasyPayMethodEnum.GATEWAY;
import static com.xinbo.sports.payment.io.EasyPayParams.EasyPayMethodEnum.QUERY;
import static com.xinbo.sports.payment.io.PayParams.*;

/**
 * @author: David
 * @date: 14/07/2020
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EasyPayUtil implements PayAbstractFactory {
  private final ThirdPayBase thirdPayBase;
  private final CoinDepositService coinDepositServiceImpl;
  private final ConfigCache configCache;
  private final CoinOnlineWithdrawalService coinOnlineWithdrawalServiceImpl;
  private final CoinWithdrawalService coinWithdrawalServiceImpl;
  private final UpdateUserCoinBase updateUserCoinBase;
  private final UserService userServiceImpl;
  private static final String MODEL = "EasyPayUtil";
  private static final String DICT_KEY = "dic_easypay_code";
  private final DictionaryCache dictionaryBase;
  @Setter public EasyPayParams.PayPlat plat;

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
    // 组装数据
    SortedMap<String, String> paramsMap = new TreeMap<>();
    paramsMap.put("pay_memberid", plat.getBusinessCode());
    paramsMap.put("pay_orderid", orderSn);
    paramsMap.put(
        "pay_applydate", DateNewUtils.getNowFormatTime(DateNewUtils.Format.yyyy_MM_dd_HH_mm_ss));
    paramsMap.put("pay_bankcode", dto.getChannel());
    paramsMap.put(
        "pay_notifyurl", plat.getNotifyUrl() + PayParams.UrlEnum.DEPOSIT_NOTIFY_URL.getUrl());
    paramsMap.put("pay_callbackurl", plat.getReturnUrl());
    paramsMap.put("pay_amount", coinDeposit.getCoin().toString());
    paramsMap.put("pay_md5sign", getSign(paramsMap, plat.getBusinessPwd()));
    paramsMap.put("pay_productname", orderSn);
    BaseParams.HeaderInfo headerInfo = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
    paramsMap.put(
        "lang",
        EnumUtils.getEnumIgnoreCase(EasyPayParams.LANGS.class, headerInfo.getLang()).getCode());

    // 网关、快捷下单接口地址
    String payUrl = plat.getUrl() + GATEWAY.getUrl();
    String result = null;

    String url = null;
    int category = 1;
    try {
      HashMap map = new HashMap();
      map.put("Content-Type", "application/x-www-form-urlencoded");
      result =
          HttpUtils.doPost(
              payUrl, JSONObject.parseObject(JSON.toJSONString(paramsMap)), map, "PaymentReqDto");
      JSONObject jsonObject = JSON.parseObject(result);
      if (!jsonObject.getString("status").equalsIgnoreCase("success")) {
        log.info("======Easypay: 支付失败 : " + jsonObject.toJSONString());
        throw new BusinessException(jsonObject.getString("msg"));
      } else {
        JSONObject data = jsonObject.getJSONObject("data");
        url = data.getString("pay_url");
        Integer integer = data.getInteger("category");
        if (integer == null) {
          category = 1;
        } else {
          category = data.getInteger("category").equals(2) ? 4 : 1;
        }
      }
    } catch (Exception e) {
      log.error(e.toString());
      log.error("===========EasyPayUtil URL: " + payUrl + "\n");
      log.error("===========EasyPayUtil postData: " + JSON.toJSONString(paramsMap) + "\n");
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
    var currency =
        EnumUtils.getEnumIgnoreCase(EasyPayParams.CURRENCY.class, configCache.getCountry());

    paramsMap.put("mchid", plat.getBusinessCode());
    paramsMap.put("out_trade_no", dto.getOrderId());
    paramsMap.put("money", format);
    paramsMap.put("bankname", dto.getOpenAccountBank());
    paramsMap.put("subbranch", "city");
    paramsMap.put("accountname", bankInfo.getString(ACCOUNT_HOLDER));
    paramsMap.put("cardnumber", bankInfo.getString(BANK_CARD_ACCOUNT));
    paramsMap.put("province", "province");
    paramsMap.put("city", "city");
    paramsMap.put("currency", currency.getDesc());
    var gameNameMap = dictionaryBase.getCategoryMap(buildDictionaryBo(DICT_KEY));
    paramsMap.put(
        "bank_code",
        currency.getDesc().equals("INR") ? gameNameMap.get(dto.getBankCode()) : dto.getBankCode());
    paramsMap.put(
        "callback_url", plat.getNotifyUrl() + PayParams.UrlEnum.WITHDRAW_NOTIFY_URL.getUrl());
    if (currency.getDesc().equals("INR")) {
      paramsMap.put("extends", bankInfo.getString("mark"));
    }
    paramsMap.put("pay_md5sign", getSign(paramsMap, plat.getBusinessPwd()));

    // 网关、快捷下单接口地址
    String payoutUrl = plat.getWithdrawUrl();

    String result = null;
    try {
      HashMap map = new HashMap();
      map.put("Content-Type", "application/x-www-form-urlencoded");
      result =
          HttpUtils.doPost(
              payoutUrl,
              JSONObject.parseObject(JSON.toJSONString(paramsMap)),
              map,
              "PaymentReqDto");
      JSONObject jsonObject = JSON.parseObject(result);
      if (jsonObject.getString("status").equalsIgnoreCase("success")) {
        return WithdrawalNotifyResDto.builder().code(true).msg(jsonObject.getString("msg")).build();
      }
      coinOnlineWithdrawalServiceImpl.remove(
          new QueryWrapper<CoinOnlineWithdrawal>().eq("order_id", dto.getOrderId()));
      throw new BusinessException(jsonObject.getString("msg"));

    } catch (Exception e) {
      coinOnlineWithdrawalServiceImpl
          .lambdaUpdate()
          .set(CoinOnlineWithdrawal::getStatus, 2)
          .eq(CoinOnlineWithdrawal::getOrderId, dto.getOrderId())
          .update();
      log.error(e.toString());
      log.error("=============================================");
      log.error("Class: " + "EasyPayUtil" + "\n");
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
  public String notifyUrl(@NotNull EasyPayParams.NotifyUrlReqDto dto) {
    // 记录回调的数据
    log.info(String.format("=====EasyPay NotifyUrl Record : %s", dto.toString()));
    var path = Paths.get("EasyPay-" + DateUtils.yyyyMMdd(DateNewUtils.now()) + ".txt");
    if (!Files.exists(path)) {
      Files.createFile(path);
    }
    if (null == plat) {
      PayAbstractFactory.init(MODEL);
    }
    Files.write(path, dto.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
    SortedMap<String, String> paramsMap = new TreeMap<>();
    paramsMap.put("amount", dto.getAmount());
    paramsMap.put("memberid", dto.getMemberid());
    paramsMap.put("orderid", dto.getOrderid());
    paramsMap.put("transaction_id", dto.getTransaction_id());
    paramsMap.put("datetime", dto.getDatetime());
    paramsMap.put("returncode", dto.getReturncode());
    String sign = getSign(paramsMap, plat.getBusinessPwd());
    if (!dto.getSign().equals(sign)) {
      Files.write(
          path,
          "=====EasyPay NotifyUrl : 校验 或 Status 失败".getBytes(StandardCharsets.UTF_8),
          StandardOpenOption.APPEND);
      return STATUS.FAIL.getCode();
    }

    // 判定OrderId 是否存在
    CoinDeposit coinDeposit =
        coinDepositServiceImpl
            .lambdaQuery()
            .eq(CoinDeposit::getOrderId, dto.getOrderid())
            .orderByDesc(CoinDeposit::getId)
            .one();
    if (null == coinDeposit) {
      log.info("=====EasyPay NotifyUrl : 订单号不存在");
      Files.write(
          path,
          "======EasyPay NotifyUrl : 订单号不存在".getBytes(StandardCharsets.UTF_8),
          StandardOpenOption.APPEND);
      return STATUS.FAIL.getCode();
    } else if (coinDeposit.getStatus() != 0) {
      log.info("=====EasyPay NotifyUrl : 重复回调");
      Files.write(
          path,
          "======EasyPay NotifyUrl : 重复回调".getBytes(StandardCharsets.UTF_8),
          StandardOpenOption.APPEND);
      return STATUS.FAIL.getCode();
    }

    try {
      try {
        if ("00".equals(dto.getReturncode())) {
          // 正常业务处理流程
          thirdPayBase.updateCoinDeposit(
              coinDeposit.getId(), BigDecimal.valueOf(Double.valueOf(dto.getAmount())));
        }
      } catch (Exception e) {
        log.info("=====RsPay 更新数据失败 :" + e.toString());
        Files.write(
            path,
            "======RsPay NotifyUrl : 重复回调".getBytes(StandardCharsets.UTF_8),
            StandardOpenOption.APPEND);
      }

    } catch (Exception e) {
      log.info("=====EasyPay 更新数据失败 :" + e.toString());
      Files.write(
          path,
          "======EasyPay NotifyUrl : 重复回调".getBytes(StandardCharsets.UTF_8),
          StandardOpenOption.APPEND);
    }
    Files.write(
        path, STATUS.SUCCESS.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
    return "OK";
  }

  @SneakyThrows
  public String withdrawNotifyUrl(EasyPayParams.DFNotifyUrlReqDto dto) {
    // 记录回调的数据
    var path = Paths.get(MODEL + "-" + DateUtils.yyyyMMdd(DateNewUtils.now()) + ".txt");
    if (!Files.exists(path)) Files.createFile(path);
    Files.write(path, dto.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
    if (null == plat) PayAbstractFactory.init(MODEL);
    log.info(String.format("=====" + MODEL + " withdrawNotifyUrl Record : %s", dto));

    SortedMap<String, String> paramsMap = new TreeMap<>();
    paramsMap.put("status", dto.getStatus());
    paramsMap.put("out_trade_no", dto.getOut_trade_no());
    paramsMap.put("amount", dto.getAmount());
    paramsMap.put("message", dto.getMessage());
    String sign = getSign(paramsMap, plat.getBusinessPwd());
    if (!dto.getPay_md5sign().equals(sign)) {
      Files.write(
          path,
          "=====EasyPay 代付NotifyUrl : 校验 或 Status 失败".getBytes(StandardCharsets.UTF_8),
          StandardOpenOption.APPEND);
      return STATUS.FAIL.getCode();
    }
    // 判定OrderId 是否存在
    CoinOnlineWithdrawal coinOnlineWithdrawal =
        coinOnlineWithdrawalServiceImpl
            .lambdaQuery()
            .eq(CoinOnlineWithdrawal::getOrderId, dto.getOut_trade_no())
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
      userServiceImpl
          .lambdaUpdate()
          .setSql("fcoin = fcoin -" + coinOnlineWithdrawal.getCoin())
          .set(User::getUpdatedAt, DateUtils.getCurrentTime())
          .eq(User::getId, coinOnlineWithdrawal.getUid())
          .update();
      if (Integer.parseInt(dto.getStatus()) == 1) {
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
        return PayParams.STATUS.SUCCESS.getCode();
      } else if (Integer.parseInt(dto.getStatus()) == 2 || Integer.parseInt(dto.getStatus()) == 3) {
        coinOnlineWithdrawalServiceImpl
            .lambdaUpdate()
            .set(CoinOnlineWithdrawal::getStatus, 2)
            .eq(CoinOnlineWithdrawal::getOrderId, coinOnlineWithdrawal.getOrderId())
            .update();
        return PayParams.STATUS.SUCCESS.getCode();
      }
    } catch (Exception e) {
      Files.write(
          path,
          PayParams.STATUS.FAIL.getCode().getBytes(StandardCharsets.UTF_8),
          StandardOpenOption.APPEND);
      log.info("=====" + MODEL + "  更新数据失败 :" + e.toString());
      return PayParams.STATUS.FAIL.getCode();
    }
    Files.write(
        path,
        PayParams.STATUS.SUCCESS.getCode().getBytes(StandardCharsets.UTF_8),
        StandardOpenOption.APPEND);
    return PayParams.STATUS.FAIL.getCode();
  }

  public Boolean checkPaymentStatus(String orderId) {
    if (null == plat) PayAbstractFactory.init(MODEL);
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
      paramsMap.put("out_trade_no", orderId);
      paramsMap.put("mchid", plat.getBusinessCode());
      String sign = getSign(paramsMap, plat.getBusinessPwd());
      paramsMap.put("pay_md5sign", sign);
      HashMap map = new HashMap();
      map.put("Content-Type", "application/x-www-form-urlencoded");
      var result =
          HttpUtils.doPost(
              url, JSONObject.parseObject(JSON.toJSONString(paramsMap)), map, "PaymentReqDto");
      JSONObject jsonObject = JSON.parseObject(result);
      if (jsonObject.getString("status").equalsIgnoreCase("success")) {

      } else if (jsonObject.getInteger("status") == 1) {
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
      } else if (jsonObject.getInteger("status") == 2 || jsonObject.getInteger("status") == 3) {
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
      log.error("Class: " + "EasyPayUtil" + "\n");
      log.error("URL: " + url + "\n");
      e.printStackTrace();
      throw new BusinessException(CodeInfo.PAY_PLAT_UPDATE_ORDER_FAILURE);
    }
  }
}
