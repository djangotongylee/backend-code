package com.xinbo.sports.payment.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xinbo.sports.dao.generator.po.PayOffline;
import com.xinbo.sports.dao.generator.po.PayOnline;
import com.xinbo.sports.dao.generator.po.*;
import com.xinbo.sports.dao.generator.service.*;
import com.xinbo.sports.payment.io.PayParams;
import com.xinbo.sports.payment.service.PayService;
import com.xinbo.sports.payment.utils.PayAbstractFactory;
import com.xinbo.sports.service.base.NoticeBase;
import com.xinbo.sports.service.cache.redis.BankCache;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.service.cache.redis.UserCache;
import com.xinbo.sports.service.common.Constant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.thread.ThreadHeaderLocalData;
import com.xinbo.sports.utils.BeanConvertUtils;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.alibaba.fastjson.JSON.toJSONString;
import static com.xinbo.sports.payment.io.PayParams.*;
import static com.xinbo.sports.service.io.dto.BaseParams.HeaderInfo;
import static java.util.Objects.nonNull;

/**
 * @author: David
 * @date: 14/07/2020
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PayServiceImpl implements PayService {
    private final UserService userServiceImpl;
    private final CoinDepositService coinDepositServiceImpl;
    private final CoinWithdrawalService coinWithdrawalServiceImpl;
    private final ConfigCache configCache;
    private final BankCache bankCache;
    private final UserCache userCache;
    private final PayOnlineService payOnlineServiceImpl;
    private final PayOnlineWithdrawService payOnlineWithdrawServiceImpl;
    private final PayOfflineService payOfflineServiceImpl;
    private final CoinOnlineWithdrawalService coinOnlineWithdrawalServiceImpl;
    private final PayBankListService payBankListServiceImpl;
    private final ReentrantLock reentrantLock = new ReentrantLock();
    private final NoticeBase noticeBase;

    /**
     * ????????????
     *
     * @return ????????????
     */
    @Override
    public PayList list() {
        HeaderInfo currentLoginUser = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();

        // ????????????
        List<PayParams.PayOnline> onlinePay = getPayOnlineList(currentLoginUser.getId());
        // ????????????

        List<PayParams.PayOffline> offlinePay = getPayOfflineList(currentLoginUser.getId());

        return PayList.builder()
                .payOnlineList(onlinePay)
                .payOfflineList(offlinePay)
                .build();
    }

    private List<PayParams.PayOffline> getPayOfflineList(Integer currentUid) {
        String staticServer = configCache.getStaticServer();

        return userCache.getUserPayOffLineListByUid(currentUid)
                .stream()
                .map(o -> {
                    PayParams.PayOffline payOffline = BeanConvertUtils.beanCopy(o, PayParams.PayOffline::new);
                    payOffline.setRange(getRange(o.getCoinRange()));
                    payOffline.setIcon(o.getCategory().equals(1) && o.getBankId() != null ? switchIcon(o.getCategory(), o.getBankId()) : switchIcon(o.getCategory()));
                    if (StringUtils.isNotBlank(o.getQrCode())) {
                        payOffline.setQrCode(o.getQrCode().startsWith("http")?o.getQrCode():staticServer + o.getQrCode());
                    }
                    return payOffline;
                }).collect(Collectors.toList());
    }

    private List<PayParams.PayOnline> getPayOnlineList(Integer currentUid) {
        return userCache.getUserPayOnLineListByUid(currentUid)
                .stream()
                .map(o -> {
                    PayParams.PayOnline payOnline = BeanConvertUtils.beanCopy(o, PayParams.PayOnline::new);
                    payOnline.setRange(getRange(o.getCoinRange()));
                    payOnline.setIcon(switchIcon(o.getCategory()));
                    return payOnline;
                }).collect(Collectors.toList());
    }

    /**
     * ????????????
     *
     * @param dto {id, coin, category, realname}
     */
    @Override
    public PayOnlineResDto onlinePay(PaymentReqDto dto) {
        // ??????:??????????????????1???
        if (dto.getCoin().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(CodeInfo.COIN_TRANSFER_OVER_LIMIT);
        }
        // ??????????????????????????????
        PayOnline online = payOnlineServiceImpl.lambdaQuery()
                .eq(PayOnline::getId, dto.getId())
                .eq(PayOnline::getStatus, 1)
                .one();
        if (online == null) {
            throw new BusinessException(CodeInfo.PAY_PAY_ID_INVALID);
        }
        if (dto.getCoin().compareTo(BigDecimal.valueOf(online.getCoinMin())) < 0 || dto.getCoin().compareTo(BigDecimal.valueOf(online.getCoinMax())) > 0) {
            throw new BusinessException(CodeInfo.COIN_TRANSFER_EXCEED);
        }

        // ????????????????????????
        HeaderInfo headerInfo = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        User user = userServiceImpl.lambdaQuery().eq(User::getId, headerInfo.getId()).one();
        if (user == null) {
            throw new BusinessException(CodeInfo.PAY_USER_INVALID);
        }

        // ????????????????????????
        long time = Instant.now().getEpochSecond();
        CoinDeposit coinDeposit = new CoinDeposit();
        coinDeposit.setTitle(online.getPayName());
        coinDeposit.setUid(user.getId());
        coinDeposit.setUsername(user.getUsername());
        coinDeposit.setPayType(1);
        coinDeposit.setCategory(online.getCategory());
        coinDeposit.setPayRefer(online.getId());
        coinDeposit.setCoin(dto.getCoin());
        coinDeposit.setCoinBefore(user.getCoin());
        // ???????????? ??????realname
        coinDeposit.setCreatedAt((int) time);
        coinDeposit.setUpdatedAt((int) time);
        coinDepositServiceImpl.save(coinDeposit);
        // ????????????????????????Utils
        log.info("==========??????????????????????????????"+online.getPayModel());
        PayAbstractFactory init = PayAbstractFactory.init(online.getPayModel());
        if (init == null) {
            throw new BusinessException(CodeInfo.PAY_PLAT_INVALID);
        }
        var depositId = coinDeposit.getId().toString();
        OnlinePayReqDto build = OnlinePayReqDto.builder()
                .lang(ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get().getLang())
                .depositId(depositId)
                .channel(online.getMark())
                .build();
        Map<Object, Object> payMap = init.onlinePay(build);
        if (payMap.isEmpty()) {
            log.info("======Pay: ??????????????????\n");
            throw new BusinessException(CodeInfo.STATUS_CODE_500);
        }
        return PayOnlineResDto.builder()
                .id(coinDeposit.getId())
                .coin(coinDeposit.getCoin())
                .createdAt(coinDeposit.getCreatedAt())
                .payName(online.getPayName())
                .category((Integer) payMap.get("category"))
                .coinRange(online.getCoinRange())
                .method(String.valueOf(payMap.get("method")))
                .url(String.valueOf(payMap.get("url")))
                .build();
    }


    /**
     * ????????????
     *
     * @param dto ??????
     * @return {category, url}
     */
    @Override
    public PayOfflineResDto offlinePay(PaymentReqDto dto) {
        boolean b = reentrantLock.tryLock();
        try {
            if (!b) {
                throw new BusinessException(CodeInfo.PLAT_REQUEST_FREQUENT);
            }
            // ????????????????????????
            HeaderInfo headerInfo = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
            User user = userServiceImpl.lambdaQuery().eq(User::getId, headerInfo.getId()).one();
            if (user == null) {
                throw new BusinessException(CodeInfo.PAY_USER_INVALID);
            }
            // ??????:??????????????????1???
            if (dto.getCoin().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(CodeInfo.COIN_TRANSFER_OVER_LIMIT);
            }

            // ??????????????????:??????????????????
            Integer count = coinDepositServiceImpl.lambdaQuery()
                    // ????????????:0-????????? 1-???????????? 2-???????????? 3-???????????? 8-???????????? 9-???????????????
                    .in(CoinDeposit::getStatus, 0, 8)
                    // ????????????:0-?????? 1-??????
                    .eq(CoinDeposit::getPayType, 0)
                    .eq(CoinDeposit::getUid, user.getId())
                    .last("limit 1")
                    .count();
            if (0 < count) {
                throw new BusinessException(CodeInfo.REPEATED_SUBMIT);
            }

            // ??????????????????????????????
            PayOffline payOffline = payOfflineServiceImpl.getById(dto.getId());
            if (null == payOffline) {
                throw new BusinessException(CodeInfo.PAY_PAY_ID_INVALID);
            }
            // ????????????????????????
            long time = Instant.now().getEpochSecond();
            CoinDeposit coinDeposit = new CoinDeposit();
            coinDeposit.setTitle(payOffline.getBankAccount());
            coinDeposit.setUid(user.getId());
            coinDeposit.setUsername(user.getUsername());
            // ????????????
            coinDeposit.setPayType(0);
            coinDeposit.setCategory(payOffline.getCategory());
            // ??????
            coinDeposit.setPayRefer(payOffline.getId());
            coinDeposit.setCoin(dto.getCoin());
            coinDeposit.setCoinBefore(user.getCoin());
            coinDeposit.setDepRealname(dto.getRealname());
            // ???????????? ??????realname
            coinDeposit.setCreatedAt((int) time);
            coinDeposit.setUpdatedAt((int) time);
            coinDepositServiceImpl.save(coinDeposit);
            //??????????????????
            noticeBase.writeDepositAndWithdrawalCount(Constant.PUSH_DN);
            // ??????????????????
            return PayOfflineResDto.builder()
                    .category(coinDeposit.getCategory())
                    .coin(coinDeposit.getCoin())
                    .depRealname(coinDeposit.getDepRealname())
                    .id(coinDeposit.getId())
                    .coinRange(payOffline.getCoinRange())
                    .createdAt(coinDeposit.getCreatedAt())
                    .userName(payOffline.getUserName())
                    .bankAccount(payOffline.getBankAccount())
                    .bankName(payOffline.getBankName())
                    .build();
        } finally {
            if (b) {
                reentrantLock.unlock();
            }
        }
    }

    @Override
    public WithdrawalNotifyResDto onlineWithdraw(WithdrawalReqDto dto) {
        boolean b = reentrantLock.tryLock();
        try {
            if (!b) {
                throw new BusinessException(CodeInfo.PLAT_REQUEST_FREQUENT);
            }

            // ??????????????????????????????
            PayOnlineWithdraw payOnlineWithdraw = payOnlineWithdrawServiceImpl.lambdaQuery()
                    .eq(PayOnlineWithdraw::getId, dto.getId())
                    .eq(PayOnlineWithdraw::getStatus, 1)
                    .one();
            if (payOnlineWithdraw == null) {
                throw new BusinessException(CodeInfo.PAY_PAY_ID_INVALID);
            }

            //????????????????????????????????????
            CoinWithdrawal coinWithdrawal = coinWithdrawalServiceImpl.getOne(new QueryWrapper<CoinWithdrawal>().eq("id", dto.getWithdrawalId()).eq("status", 3));
            if (coinWithdrawal == null) throw new BusinessException(CodeInfo.USER_NO_PASS_AUDIT);
            // ??????:??????????????????1???
            if (coinWithdrawal.getCoin().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(CodeInfo.COIN_TRANSFER_OVER_LIMIT);
            }
            if (coinWithdrawal.getCoin().compareTo(BigDecimal.valueOf(payOnlineWithdraw.getCoinMin())) < 0 || coinWithdrawal.getCoin().compareTo(BigDecimal.valueOf(payOnlineWithdraw.getCoinMax())) > 0) {
                throw new BusinessException(CodeInfo.COIN_TRANSFER_EXCEED);
            }
            List<CoinOnlineWithdrawal> list = coinOnlineWithdrawalServiceImpl.list(new QueryWrapper<CoinOnlineWithdrawal>().eq("withdrawal_order_id", dto.getWithdrawalId()).eq("status", 0));
            if (!CollectionUtils.isEmpty(list)) throw new BusinessException(CodeInfo.REPEATED_SUBMIT);
            // ????????????????????????
            User user = userServiceImpl.lambdaQuery().eq(nonNull(coinWithdrawal.getUsername()), User::getUsername, coinWithdrawal.getUsername()).one();
            if (user == null) {
                throw new BusinessException(CodeInfo.PAY_USER_INVALID);
            }
            var bankId = parseObject(coinWithdrawal.getBankInfo()).getInteger("bankId");
            BankList bankCache = this.bankCache.getBankCache(bankId);
            if (!nonNull(bankCache) || !dto.getBankCode().equals(bankCache.getCode()))
                throw new BusinessException(CodeInfo.BANK_INVALID);
            // ???????????????
            int i = RandomUtils.nextInt(10000, 99999);
            String orderSn = "O" + DateNewUtils.getNowFormatTime(DateNewUtils.Format.yyyyMMddHHmmss) + i;
            long time = Instant.now().getEpochSecond();
            CoinOnlineWithdrawal coinOnlineWithdrawal = new CoinOnlineWithdrawal();
            coinOnlineWithdrawal.setUid(user.getId());
            coinOnlineWithdrawal.setUsername(user.getUsername());
            coinOnlineWithdrawal.setPayoutCode(payOnlineWithdraw.getCode());
            coinOnlineWithdrawal.setWithdrawalOrderId(dto.getWithdrawalId());
            coinOnlineWithdrawal.setCoin(coinWithdrawal.getCoin());
            coinOnlineWithdrawal.setBankInfo(coinWithdrawal.getBankInfo());
            coinOnlineWithdrawal.setOrderId(orderSn);
            coinOnlineWithdrawal.setCategory(payOnlineWithdraw.getCategory());
            coinOnlineWithdrawal.setStatus(0);
            coinOnlineWithdrawal.setCreatedAt((int) time);
            coinOnlineWithdrawal.setUpdatedAt((int) time);
            coinOnlineWithdrawalServiceImpl.save(coinOnlineWithdrawal);
            // ????????????????????????Utils
            PayAbstractFactory init = PayAbstractFactory.init(payOnlineWithdraw.getPayModel());
            if (init == null) {
                throw new BusinessException(CodeInfo.PAY_PLAT_INVALID);
            }
            OnlinePayoutReqDto build = OnlinePayoutReqDto.builder()
                    .lang(ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get().getLang())
                    .bankCode(bankCache.getCode())
                    .openAccountBank(dto.getOpenAccountBank())
                    .orderId(orderSn)
                    .build();
            return init.onlineWithdraw(build);
        } finally {
            if (b) {
                reentrantLock.unlock();
            }
        }
    }

    @Override
    public Object payment(Payment dto) {
        // ????????????:0-?????? 1-??????
        if (!List.of(0, 1).contains(dto.getPayType())) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        if (0 == dto.getPayType()) {
            return offlinePay(dto);
        }
        return onlinePay(dto);
    }

    @Override
    public List<PayListResBody> payList() {
        List<PayListResBody> list = new ArrayList<>();
        // ????????????:0-?????? 1-??????
        HeaderInfo currentLoginUser = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();


        // ??????
        var offlineList = getPayOfflineList(currentLoginUser.getId());
        if (Optional.ofNullable(offlineList).isPresent() && !offlineList.isEmpty()) {
            list.addAll(offlineList.stream().map(o -> {
                var resBody = BeanConvertUtils.beanCopy(o, PayListResBody::new);
                resBody.setPayType(0);
                var payInfo = new JSONObject();
                payInfo.put("userName", o.getUserName());
                payInfo.put("bankName", o.getBankName());
                payInfo.put("bankAccount", o.getBankAccount());
                payInfo.put("bankAddress", o.getBankAddress());
                payInfo.put("qrCode", o.getQrCode());
                resBody.setPayInfo(payInfo);
                return resBody;
            }).collect(Collectors.toList()));
        }
        // ??????
        var onlineList = getPayOnlineList(currentLoginUser.getId());
        if (Optional.ofNullable(onlineList).isPresent() && !onlineList.isEmpty()) {
            list.addAll(onlineList.stream().map(o -> {
                var resBody = BeanConvertUtils.beanCopy(o, PayListResBody::new);
                resBody.setPayType(1);

                var payInfo = new JSONObject();
                payInfo.put("payName", o.getPayName());
                resBody.setPayInfo(payInfo);
                return resBody;
            }).collect(Collectors.toList()));
        }
        return list;
    }

    @Override
    public Boolean checkPayment() {
        CoinOnlineWithdrawal one = coinOnlineWithdrawalServiceImpl.getOne(new QueryWrapper<CoinOnlineWithdrawal>().eq("status", 0).orderByAsc("created_at"), false);
        if (null != one) {
            // ??????????????????????????????
            PayOnlineWithdraw payOnlineWithdraw = payOnlineWithdrawServiceImpl.getOne(new QueryWrapper<PayOnlineWithdraw>().eq("code", one.getPayoutCode()), false);
            if (payOnlineWithdraw == null) {
                throw new BusinessException(CodeInfo.PAY_PAY_ID_INVALID);
            }
            PayAbstractFactory init = PayAbstractFactory.init(payOnlineWithdraw.getPayModel());
            if (init == null) {
                throw new BusinessException(CodeInfo.PAY_PLAT_INVALID);
            }
            String orderId = one.getOrderId();
            return init.checkPaymentStatus(orderId);
        }
        return true;
    }


    /**
     * ????????????????????????
     *
     * @param maxRange ?????????
     * @return ????????????
     */
    @NotNull
    private List<String> getRange(String maxRange) {
        String str[] = maxRange.split(",");
        return Arrays.asList(str);
    }

    /**
     * ??????????????????
     *
     * @param category ??????
     * @return ????????????
     */
    @NotNull
    private String switchIcon(int category) {
        String staticServer = configCache.getStaticServer();
        String icon;
        switch (category) {
            case 2:
                icon = "/icon/bank/pay_wechat.png";
                break;
            case 3:
                icon = "/icon/bank/pay_zfb.png";
                break;
            case 4:
                icon = "/icon/bank/pay_qq.png";
                break;
            case 5:
                icon = "/icon/bank/pay_qr_code.png";
                break;
            case 1:
            default:
                icon = "/icon/bank/pay_yl.png";
                break;
        }

        return staticServer + icon;
    }

    /**
     * ??????????????????
     *
     * @param category ????????? bankId??????ID
     * @return ????????????
     */
    @NotNull
    private String switchIcon(int category, int bankId) {
        String staticServer = configCache.getStaticServer();
        String icon;
        switch (category) {
            case 1:
                BankList bank = this.bankCache.getBankCache(bankId);
                return bank.getIcon();
            case 2:
                icon = "/icon/bank/pay_wechat.png";
                break;
            case 3:
                icon = "/icon/bank/pay_zfb.png";
                break;
            case 4:
                icon = "/icon/bank/pay_qq.png";
                break;
            case 5:
                icon = "/icon/bank/pay_qr_code.png";
                break;
            case 6:
                icon = "/icon/bank/pay_upi.png";
                break;
            default:
                icon = "/icon/bank/pay_yl.png";
                break;
        }

        return staticServer + icon;
    }
}

