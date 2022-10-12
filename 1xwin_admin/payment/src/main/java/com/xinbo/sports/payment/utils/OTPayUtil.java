package com.xinbo.sports.payment.utils;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.xinbo.sports.payment.io.EasyPayParams.EasyPayMethodEnum.GATEWAY;
import static com.xinbo.sports.payment.io.EasyPayParams.EasyPayMethodEnum.QUERY;
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
import com.xinbo.sports.payment.io.OTPayParams;
import com.xinbo.sports.payment.io.PayParams;
import com.xinbo.sports.payment.io.PayParams.OnlinePayReqDto;
import com.xinbo.sports.payment.io.PayParams.OnlinePayoutReqDto;
import com.xinbo.sports.payment.io.PayParams.STATUS;
import com.xinbo.sports.payment.io.PayParams.UrlEnum;
import com.xinbo.sports.payment.io.PayParams.WithdrawalNotifyResDto;
import com.xinbo.sports.service.base.UpdateUserCoinBase;
import com.xinbo.sports.service.cache.KeyConstant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.bo.DictionaryBo;
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
import java.util.ArrayList;
import java.util.Arrays;
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
import org.apache.commons.lang3.StringUtils;
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
public class OTPayUtil implements PayAbstractFactory {
  private final ThirdPayBase thirdPayBase;
  private final CoinDepositService coinDepositServiceImpl;
  private final CoinOnlineWithdrawalService coinOnlineWithdrawalServiceImpl;
  private final CoinWithdrawalService coinWithdrawalServiceImpl;
  private final UpdateUserCoinBase updateUserCoinBase;
  private final UserService userServiceImpl;
  private static final String MODEL = "OTPayUtil";
  @Setter public OTPayParams.PayPlat plat;

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
    paramsMap.put("mch_id", plat.getBusinessCode());
    paramsMap.put("notify_url", plat.getNotifyUrl() + UrlEnum.DEPOSIT_NOTIFY_URL.getUrl());
    paramsMap.put("mch_order_no", orderSn);
    paramsMap.put("pay_type", dto.getChannel());
    paramsMap.put("trade_amount", coinDeposit.getCoin().intValue()+"");
    paramsMap.put("order_date", DateNewUtils.getNowFormatTime(DateNewUtils.Format.yyyy_MM_dd_HH_mm_ss));
    paramsMap.put("bank_code", "IDPT0001");
    paramsMap.put("goods_name", orderSn);
    paramsMap.put("sign", getSign(paramsMap, plat.getBusinessPwd()));
    paramsMap.put("sign_type", "MD5");
    // 网关、快捷下单接口地址
    String payUrl = plat.getUrl();
    String param = stringAppend(paramsMap);
    return Map.of("method", "POST", "url", payUrl+"?"+param, "category", 3);
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

  private  String getSign(Map<String, String> map, String signKey) {
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
    return DigestUtils.md5DigestAsHex(signStr.getBytes());
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
    var bankInfo = parseObject(coinOnlineWithdrawal.getBankInfo());
    paramsMap.put("mch_id", plat.getBusinessCode());
    paramsMap.put("mch_transferId", dto.getOrderId());
    paramsMap.put("transfer_amount", amount.intValue()+"");
    paramsMap.put("apply_date", DateNewUtils.getNowFormatTime(DateNewUtils.Format.yyyy_MM_dd_HH_mm_ss));
    paramsMap.put("bank_code", "IDPT0025");
    paramsMap.put("receive_name", bankInfo.getString(ACCOUNT_HOLDER));
    paramsMap.put("receive_account", bankInfo.getString(BANK_CARD_ACCOUNT));
    paramsMap.put("remark", bankInfo.getString("mark"));
    paramsMap.put("back_url", plat.getNotifyUrl() + UrlEnum.WITHDRAW_NOTIFY_URL.getUrl());
    paramsMap.put("sign", getSign(paramsMap, plat.getBusinessPwd()));
    paramsMap.put("sign_type", "MD5");

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
      if (jsonObject.getString("respCode").equalsIgnoreCase("SUCCESS")) {
        return WithdrawalNotifyResDto.builder().code(true).msg("成功").build();
      }
      coinOnlineWithdrawalServiceImpl.remove(
          new QueryWrapper<CoinOnlineWithdrawal>().eq("order_id", dto.getOrderId()));
      throw new BusinessException(jsonObject.getString("errorMsg"));

    } catch (Exception e) {
      coinOnlineWithdrawalServiceImpl
          .lambdaUpdate()
          .set(CoinOnlineWithdrawal::getStatus, 2)
          .eq(CoinOnlineWithdrawal::getOrderId, dto.getOrderId())
          .update();
      log.error(e.toString());
      log.error("=============================================");
      log.error("Class: " + "OTPay" + "\n");
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
  public String notifyUrl(@NotNull OTPayParams.NotifyUrlReqDto dto) {
    // 记录回调的数据
    log.info(String.format("=====OtPay NotifyUrl Record : %s", dto.toString()));
    var path = Paths.get("OtPay-" + DateUtils.yyyyMMdd(DateNewUtils.now()) + ".txt");
    if (!Files.exists(path)) {
      Files.createFile(path);
    }
    if (null == plat) {
      PayAbstractFactory.init(MODEL);
    }
    Files.write(path, dto.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
    SortedMap<String, String> paramsMap = new TreeMap<>();
    paramsMap.put("tradeResult", dto.getTradeResult());
    paramsMap.put("mchId", dto.getMchId());
    paramsMap.put("mchOrderNo", dto.getMchOrderNo());
    paramsMap.put("oriAmount", dto.getOriAmount());
    paramsMap.put("amount", dto.getAmount());
    paramsMap.put("orderDate", dto.getOrderDate());
    paramsMap.put("orderNo", dto.getOrderNo());
    String sign = getSign(paramsMap, plat.getBusinessPwd());
    if (!dto.getSign().equals(sign)) {
      Files.write(
          path,
          "=====OtPay NotifyUrl : 校验 或 Status 失败".getBytes(StandardCharsets.UTF_8),
          StandardOpenOption.APPEND);
      log.info("=====OtPay NotifyUrl 验签失败");
      return STATUS.FAIL.getCode();
    }

    // 判定OrderId 是否存在
    CoinDeposit coinDeposit =
        coinDepositServiceImpl
            .lambdaQuery()
            .eq(CoinDeposit::getOrderId, dto.getMchOrderNo())
            .orderByDesc(CoinDeposit::getId)
            .one();
    if (null == coinDeposit) {
      log.info("=====OtPay NotifyUrl : 订单号不存在");
      Files.write(
          path,
          "======OtPay NotifyUrl : 订单号不存在".getBytes(StandardCharsets.UTF_8),
          StandardOpenOption.APPEND);
      return STATUS.FAIL.getCode();
    } else if (coinDeposit.getStatus() != 0) {
      log.info("=====OtPay NotifyUrl : 重复回调");
      Files.write(
          path,
          "======OtPay NotifyUrl : 重复回调".getBytes(StandardCharsets.UTF_8),
          StandardOpenOption.APPEND);
      return STATUS.SUCCESS.getCode();
    }

    try {
      try {
        if ("1".equals(dto.getTradeResult())) {
          // 正常业务处理流程
          thirdPayBase.updateCoinDeposit(
              coinDeposit.getId(), BigDecimal.valueOf(Double.valueOf(dto.getAmount())));
          return "success";
        }
      } catch (Exception e) {
        log.info("=====OtPay 更新数据失败 :" + e.toString());
        Files.write(
            path,
            "======OtPay NotifyUrl : 重复回调".getBytes(StandardCharsets.UTF_8),
            StandardOpenOption.APPEND);
      }

    } catch (Exception e) {
      log.info("=====OtPay 更新数据失败 :" + e.toString());
      Files.write(
          path,
          "======OtPay NotifyUrl : 重复回调".getBytes(StandardCharsets.UTF_8),
          StandardOpenOption.APPEND);
    }
    Files.write(
        path, STATUS.SUCCESS.getCode().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
    return STATUS.FAIL.getCode();
  }

  @SneakyThrows
  public String withdrawNotifyUrl(OTPayParams.DFNotifyUrlReqDto dto) {
    // 记录回调的数据
    var path = Paths.get(MODEL + "-" + DateUtils.yyyyMMdd(DateNewUtils.now()) + ".txt");
    if (!Files.exists(path)) Files.createFile(path);
    Files.write(path, dto.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
    if (null == plat) PayAbstractFactory.init(MODEL);
    log.info(String.format("=====" + MODEL + " withdrawNotifyUrl Record : %s", dto));

    SortedMap<String, String> paramsMap = new TreeMap<>();
    paramsMap.put("tradeResult", dto.getTradeResult());
    paramsMap.put("merTransferId", dto.getMerTransferId());
    paramsMap.put("merNo", dto.getMerNo());
    paramsMap.put("tradeNo", dto.getTradeNo());
    paramsMap.put("transferAmount", dto.getTransferAmount());
    paramsMap.put("applyDate", dto.getApplyDate());
    paramsMap.put("version", dto.getVersion());
    paramsMap.put("respCode", dto.getRespCode());
    String sign = getSign(paramsMap, plat.getBusinessPwd());
    if (!dto.getSign().equals(sign)) {
      Files.write(
          path,
          "=====OtPay 代付NotifyUrl : 校验 或 Status 失败".getBytes(StandardCharsets.UTF_8),
          StandardOpenOption.APPEND);
      return STATUS.FAIL.getCode();
    }
    // 判定OrderId 是否存在
    CoinOnlineWithdrawal coinOnlineWithdrawal =
        coinOnlineWithdrawalServiceImpl
            .lambdaQuery()
            .eq(CoinOnlineWithdrawal::getOrderId, dto.getMerTransferId())
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
      userServiceImpl
          .lambdaUpdate()
          .setSql("fcoin = fcoin -" + coinOnlineWithdrawal.getCoin())
          .set(User::getUpdatedAt, DateUtils.getCurrentTime())
          .eq(User::getId, coinOnlineWithdrawal.getUid())
          .update();
      if (Integer.parseInt(dto.getTradeResult()) == 1) {
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
        return STATUS.SUCCESS.getCode();
      }else if (Integer.parseInt(dto.getTradeResult()) == 2 || Integer.parseInt(dto.getTradeResult()) == 3) {
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
