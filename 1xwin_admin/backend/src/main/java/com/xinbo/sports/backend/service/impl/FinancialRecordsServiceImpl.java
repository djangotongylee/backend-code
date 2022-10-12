package com.xinbo.sports.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinbo.sports.backend.io.bo.FinancialRecords;
import com.xinbo.sports.backend.redis.GameCache;
import com.xinbo.sports.backend.service.IFinancialRecordsService;
import com.xinbo.sports.dao.generator.po.*;
import com.xinbo.sports.dao.generator.service.*;
import com.xinbo.sports.service.base.PlatServiceBase;
import com.xinbo.sports.service.base.UserServiceBase;
import com.xinbo.sports.service.cache.redis.AdminCache;
import com.xinbo.sports.service.cache.redis.BankCache;
import com.xinbo.sports.service.cache.redis.UserCache;
import com.xinbo.sports.service.common.Constant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.bo.UserCacheBo;
import com.xinbo.sports.utils.BeanConvertUtils;
import com.xinbo.sports.utils.DateUtils;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;
import com.xinbo.sports.utils.components.response.CodeInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

import static com.alibaba.fastjson.JSON.parseObject;
import static java.util.Objects.nonNull;

/**
 * @description:财务记录
 * @author: andy
 * @date: 2020/8/14
 */
@Service
public class FinancialRecordsServiceImpl implements IFinancialRecordsService {
    private static final String COIN_UP = "coinUp";
    private static final String COIN_DOWN = "coinDown";
    @Resource
    private CoinPlatTransferService coinPlatTransferServiceImpl;
    @Resource
    private CoinAdminTransferService coinAdminTransferServiceImpl;
    @Resource
    private CoinDepositService coinDepositServiceImpl;
    @Resource
    private CoinWithdrawalService coinWithdrawalServiceImpl;
    @Resource
    private CoinOnlineWithdrawalService coinOnlineWithdrawalServiceImpl;
    @Resource
    private CoinLogService coinLogServiceImpl;

    @Resource
    private GameCache gameCache;
    @Resource
    private AdminCache adminCache;
    @Resource
    private UserCache userCache;
    @Resource
    private BankCache bankCache;
    @Resource
    private PlatServiceBase platServiceBase;
    private final ReentrantLock reentrantLock = new ReentrantLock();
    @Resource
    private UserServiceBase userServiceBase;

    @Override
    public ResPage<FinancialRecords.WithdrawalListResBody> withdrawalList(ReqPage<FinancialRecords.WithdrawalListReqBody> reqBody) {
        if (null == reqBody) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        QueryWrapper<CoinWithdrawal> wrapper = whereWithdrawalListOrStatistics(reqBody.getData());
        Page<CoinWithdrawal> page = coinWithdrawalServiceImpl.page(reqBody.getPage(), wrapper);
        var unaryOperator = userServiceBase.checkShow(Constant.BANK_ACCOUNT);
        Page<FinancialRecords.WithdrawalListResBody> tmpPage = BeanConvertUtils.copyPageProperties(page, FinancialRecords.WithdrawalListResBody::new);
        tmpPage.getRecords().forEach(s -> {
            if (StringUtils.isNotBlank(s.getBankInfo())) {
                UserBank userBank = parseObject(s.getBankInfo(), UserBank.class);
                if (Optional.ofNullable(userBank).isPresent()) {
                    s.setAccountName(userBank.getAccountName());
                    s.setBankAccount(unaryOperator.apply(userBank.getBankAccount()));
                    var bankName = Optional.ofNullable(userBank.getBankId()).map(x ->
                            Optional.ofNullable(bankCache.getBankCache(userBank.getBankId()))
                                    .map(BankList::getName).orElse("")
                    ).orElse("");
                    s.setBankName(bankName);
                    s.setAccountAddress(userBank.getMark());
                }
                s.setBankInfo(null);
            }
            UserCacheBo.UserCacheInfo userCacheInfo = userCache.getUserInfoById(s.getUid());
            if (null != userCacheInfo) {
                s.setUsername(userCacheInfo.getUsername());
                s.setUserFlagList(userCache.getUserFlagList(userCacheInfo.getUid()));

                s.setLevelText(getLevelTextByLevelId(userCacheInfo.getLevelId()));
            }
        });
        return ResPage.get(tmpPage);
    }

    /**
     * 获取会员等级by等级ID
     *
     * @param levelId 等级ID
     * @return 会员等级:vip1-乒乓球达人
     */
    private String getLevelTextByLevelId(Integer levelId) {
        String levelText = null;
        UserLevel userLevel = userCache.getUserLevelById(levelId);
        if (null != userLevel) {
            levelText = userLevel.getCode() + " - " + userLevel.getName();
        }
        return levelText;
    }

    /**
     * 出款记录-列表与统计:构造Where查询条件
     *
     * @param data 请求参数
     * @return Where查询条件
     */
    private QueryWrapper<CoinWithdrawal> whereWithdrawalListOrStatistics(FinancialRecords.WithdrawalListReqBody data) {
        QueryWrapper<CoinWithdrawal> wrapper = null;
        if (null != data) {
            wrapper = Wrappers.query();
            wrapper.eq(null != data.getId(), Constant.ID, data.getId());
            wrapper.eq(null != data.getUid(), Constant.UID, data.getUid());
            wrapper.eq(null != data.getUsername(), Constant.USERNAME, data.getUsername());
            wrapper.eq(null != data.getAdminUid(), "admin_uid", data.getAdminUid());
            wrapper.eq(null != data.getStatus(), Constant.STATUS, data.getStatus());
            wrapper.ge(null != data.getStartTime(), Constant.UPDATED_AT, data.getStartTime());
            wrapper.le(null != data.getEndTime(), Constant.UPDATED_AT, data.getEndTime());
        }
        return wrapper;
    }

    @Override
    public FinancialRecords.CommonCoinResBody withdrawalStatistics(FinancialRecords.WithdrawalListReqBody reqBody) {
        if (null == reqBody) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        QueryWrapper<CoinWithdrawal> wrapper = whereWithdrawalListOrStatistics(reqBody);
        wrapper.select("ifnull(sum(coin),0) as coin");
        Map<String, Object> map = coinWithdrawalServiceImpl.getMap(wrapper);
        BigDecimal coin = BigDecimal.ZERO;
        if (null != map) {
            coin = (BigDecimal) map.get("coin");
        }
        return FinancialRecords.CommonCoinResBody.builder().coin(coin).build();
    }

    @Override
    public ResPage<FinancialRecords.DepositListResBody> depositList(ReqPage<FinancialRecords.DepositListReqBody> reqBody) {
        if (null == reqBody) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        QueryWrapper<CoinDeposit> wrapper = whereDepositListOrStatistics(reqBody.getData());
        Page<CoinDeposit> page = coinDepositServiceImpl.page(reqBody.getPage(), wrapper);
        Page<FinancialRecords.DepositListResBody> tmpPage = BeanConvertUtils.copyPageProperties(page, FinancialRecords.DepositListResBody::new);
        tmpPage.getRecords().forEach(s -> {
            UserCacheBo.UserCacheInfo userCacheInfo = userCache.getUserInfoById(s.getUid());
            if (null != userCacheInfo) {
                s.setUsername(userCacheInfo.getUsername());
                s.setUserFlagList(userCache.getUserFlagList(userCacheInfo.getUid()));
            }
        });
        return ResPage.get(tmpPage);
    }

    /**
     * 入款记录-列表与统计:构造Where查询条件
     *
     * @param data 请求参数
     * @return Where查询条件
     */
    private QueryWrapper<CoinDeposit> whereDepositListOrStatistics(FinancialRecords.DepositListReqBody data) {
        QueryWrapper<CoinDeposit> wrapper = null;
        if (null != data) {
            wrapper = Wrappers.query();
            wrapper.eq(null != data.getUid(), Constant.UID, data.getUid());
            wrapper.eq(null != data.getUsername(), Constant.USERNAME, data.getUsername());
            wrapper.likeRight(null != data.getDepRealname(), "dep_realname", data.getDepRealname());
            wrapper.eq(null != data.getPayType(), "pay_type", data.getPayType());
            wrapper.eq(null != data.getAdminId(), "admin_id", data.getAdminId());
            wrapper.eq(null != data.getStatus(), Constant.STATUS, data.getStatus());
            // 状态:1-手动到账 2-自动到账 3-充值失败 9-管理员充值
            wrapper.in(null == data.getStatus(), Constant.STATUS, 1, 2, 3, 9);
            wrapper.eq(null != data.getCategory(), Constant.CATEGORY, data.getCategory());
            wrapper.ge(null != data.getStartTime(), Constant.UPDATED_AT, data.getStartTime());
            wrapper.le(null != data.getEndTime(), Constant.UPDATED_AT, data.getEndTime());
            wrapper.eq(null != data.getId(), Constant.ID, data.getId()).or();
            wrapper.likeRight(null != data.getId(), "order_id", data.getId());
        }
        return wrapper;
    }

    @Override
    public FinancialRecords.CommonCoinResBody depositStatistics(FinancialRecords.DepositListReqBody reqBody) {
        if (null == reqBody) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        QueryWrapper<CoinDeposit> wrapper = whereDepositListOrStatistics(reqBody);
        wrapper.select("ifnull(sum(pay_coin),0) as coin");
        Map<String, Object> map = coinDepositServiceImpl.getMap(wrapper);
        BigDecimal coin = BigDecimal.ZERO;
        if (null != map) {
            coin = (BigDecimal) map.get("coin");
        }
        return FinancialRecords.CommonCoinResBody.builder().coin(coin).build();
    }

    @Override
    public ResPage<FinancialRecords.PlatTransferListResBody> platTransferList(ReqPage<FinancialRecords.PlatTransferListReqBody> reqBody) {
        if (null == reqBody) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        QueryWrapper<CoinPlatTransfer> wrapper = wherePlatTransferListOrStatistics(reqBody.getData());
        Page<CoinPlatTransfer> page = coinPlatTransferServiceImpl.page(reqBody.getPage(), wrapper);
        page.getRecords().forEach(s -> s.setMark(getMark(s.getCategory(), s.getPlatListId())));
        Page<FinancialRecords.PlatTransferListResBody> tmpPage = BeanConvertUtils.copyPageProperties(page, FinancialRecords.PlatTransferListResBody::new);
        tmpPage.getRecords().forEach(s -> {
            UserCacheBo.UserCacheInfo userCacheInfo = userCache.getUserInfoById(s.getUid());
            if (null != userCacheInfo) {
                s.setUsername(userCacheInfo.getUsername());
                s.setUserFlagList(userCache.getUserFlagList(userCacheInfo.getUid()));
            }
        });
        return ResPage.get(tmpPage);
    }

    /**
     * 平台转账-列表与统计:构造Where查询条件
     *
     * @param data 请求参数
     * @return Where查询条件
     */
    private QueryWrapper<CoinPlatTransfer> wherePlatTransferListOrStatistics(FinancialRecords.PlatTransferListReqBody data) {
        QueryWrapper<CoinPlatTransfer> wrapper = null;
        if (null != data) {
            wrapper = Wrappers.query();
            wrapper.eq(null != data.getUid(), Constant.UID, data.getUid());
            wrapper.eq(null != data.getUsername(), Constant.USERNAME, data.getUsername());
            wrapper.eq(null != data.getCategory(), Constant.CATEGORY, data.getCategory());
            wrapper.eq(null != data.getStatus(), Constant.STATUS, data.getStatus());
            wrapper.ge(null != data.getStartTime(), Constant.UPDATED_AT, data.getStartTime());
            wrapper.le(null != data.getEndTime(), Constant.UPDATED_AT, data.getEndTime());
            wrapper.eq(null != data.getId(), Constant.ID, data.getId()).or();
            wrapper.likeRight(null != data.getId(), "order_plat", data.getId());
        }
        return wrapper;
    }

    @Override
    public FinancialRecords.PlatTransferStatisticsResBody platTransferStatistics(FinancialRecords.PlatTransferListReqBody reqBody) {
        if (null == reqBody) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        QueryWrapper<CoinPlatTransfer> wrapper = wherePlatTransferListOrStatistics(reqBody);
        wrapper.select("sum(case when category=0 then  coin else 0 end ) as coinUp",
                "sum(case when category=1 then  coin else 0 end ) as coinDown"
        );
        Map<String, Object> map = coinPlatTransferServiceImpl.getMap(wrapper);
        BigDecimal coinUp = BigDecimal.ZERO;
        BigDecimal coinDown = BigDecimal.ZERO;
        if (null != map) {
            coinUp = (BigDecimal) map.get(COIN_UP);
            coinDown = (BigDecimal) map.get(COIN_DOWN);
        }
        return FinancialRecords.PlatTransferStatisticsResBody.builder()
                .coinUp(coinUp)
                .coinDown(coinDown)
                .build();
    }

    @Override
    public ResPage<FinancialRecords.AdminTransferListResBody> adminTransferList(ReqPage<FinancialRecords.AdminTransferListReqBody> reqBody) {
        if (null == reqBody) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        QueryWrapper<CoinAdminTransfer> wrapper = whereAdminTransferListOrStatistics(reqBody.getData());
        Page<CoinAdminTransfer> page = coinAdminTransferServiceImpl.page(reqBody.getPage(), wrapper);
        Page<FinancialRecords.AdminTransferListResBody> tmpPage = BeanConvertUtils.copyPageProperties(page, FinancialRecords.AdminTransferListResBody::new);
        tmpPage.getRecords().forEach(s -> {
            Admin admin = adminCache.getAdminInfoById(s.getAdminId());
            if (null != admin) {
                s.setAdminName(admin.getUsername());
            }
            UserCacheBo.UserCacheInfo userCacheInfo = userCache.getUserInfoById(s.getUid());
            if (null != userCacheInfo) {
                s.setUsername(userCacheInfo.getUsername());
                s.setUserFlagList(userCache.getUserFlagList(userCacheInfo.getUid()));
            }
        });
        return ResPage.get(tmpPage);
    }

    /**
     * 调账记录-列表与统计:构造Where查询条件
     *
     * @param data 请求参数
     * @return Where查询条件
     */
    private QueryWrapper<CoinAdminTransfer> whereAdminTransferListOrStatistics(FinancialRecords.AdminTransferListReqBody data) {
        QueryWrapper<CoinAdminTransfer> wrapper = null;
        if (null != data) {
            wrapper = Wrappers.query();
            wrapper.eq(null != data.getId(), Constant.ID, data.getId());
            wrapper.eq(null != data.getUid(), Constant.UID, data.getUid());
            wrapper.eq(null != data.getUsername(), Constant.USERNAME, data.getUsername());
            wrapper.eq(null != data.getAdminId(), "admin_id", data.getAdminId());
            wrapper.eq(null != data.getCategory(), Constant.CATEGORY, data.getCategory());
            wrapper.ge(null != data.getStartTime(), Constant.UPDATED_AT, data.getStartTime());
            wrapper.le(null != data.getEndTime(), Constant.UPDATED_AT, data.getEndTime());
        }
        return wrapper;
    }

    @Override
    public FinancialRecords.CommonCoinResBody adminTransferStatistics(FinancialRecords.AdminTransferListReqBody reqBody) {
        if (null == reqBody) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        QueryWrapper<CoinAdminTransfer> wrapper = whereAdminTransferListOrStatistics(reqBody);
        wrapper.select("ifnull(sum(coin),0) as coin");
        Map<String, Object> map = coinAdminTransferServiceImpl.getMap(wrapper);
        BigDecimal coin = BigDecimal.ZERO;
        if (null != map) {
            coin = (BigDecimal) map.get("coin");
        }
        return FinancialRecords.CommonCoinResBody.builder().coin(coin).build();
    }

    @Override
    public boolean updatePlatTransferStatusById(FinancialRecords.UpdatePlatTransferStatusByIdReqBody reqBody) {
        if (null == reqBody) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        CoinPlatTransfer coinPlatTransfer = coinPlatTransferServiceImpl.getById(reqBody.getId());
        // 状态:1-成功 2-失败
        Integer status = reqBody.getStatus();
        boolean flag = false;
        if (1 == status) {
            flag = true;
        }
        if (2 == status) {
            flag = false;
        }
        return platServiceBase.updateCoinPlatTransferStatusById(coinPlatTransfer, flag);
    }

    @Override
    public ResPage<FinancialRecords.OnlineWithdrawalListResBody> onlineWithdrawalList(ReqPage<FinancialRecords.OnlineWithdrawalListReqBody> reqBody) {
        if (null == reqBody) throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        var wrapper = whereOnlineWithdrawalListOrStatistics(reqBody.getData());
        Page<CoinOnlineWithdrawal> page = coinOnlineWithdrawalServiceImpl.page(reqBody.getPage(), wrapper);
        var unaryOperator = userServiceBase.checkShow(Constant.BANK_ACCOUNT);
        Page<FinancialRecords.OnlineWithdrawalListResBody> resBodyPage = BeanConvertUtils.copyPageProperties(page, FinancialRecords.OnlineWithdrawalListResBody::new, (sb, bo) -> {
            bo.setId(sb.getOrderId());
            var bankJson = parseObject(bo.getBankInfo());
            var bankAccount = unaryOperator.apply(bankJson.getString("bankCardAccount"));
            bankJson.put("bankCardAccount", bankAccount);
            bo.setBankInfo(bankJson.toJSONString());
        });
        return ResPage.get(resBodyPage);
    }

    @Override
    public FinancialRecords.CommonCoinResBody onlineWithdrawalStatistics(FinancialRecords.OnlineWithdrawalListReqBody reqBody) {
        if (null == reqBody) throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        var wrapper = whereOnlineWithdrawalListOrStatistics(reqBody);
        wrapper.select("ifnull(sum(coin),0) as coin");
        Map<String, Object> map = coinOnlineWithdrawalServiceImpl.getMap(wrapper);
        BigDecimal coin = BigDecimal.ZERO;
        if (null != map) {
            coin = (BigDecimal) map.get("coin");
        }
        return FinancialRecords.CommonCoinResBody.builder().coin(coin).build();
    }

    @Override
    public Boolean updateOnlineWithdrawalStatus(FinancialRecords.UpdateOnlineWithdrawalReqBody reqBody) {
        boolean b = reentrantLock.tryLock();
        try {
            if (!b) {
                throw new BusinessException(CodeInfo.PLAT_REQUEST_FREQUENT);
            }
            if (1 == reqBody.getStatus()) {
                coinWithdrawalServiceImpl.lambdaUpdate().set(CoinWithdrawal::getStatus, 1).eq(CoinWithdrawal::getId, reqBody.getWithdrawalOrderId()).update();
                coinLogServiceImpl.lambdaUpdate().set(CoinLog::getStatus, 1)
                        .eq(CoinLog::getReferId, reqBody.getWithdrawalOrderId())
                        .eq(CoinLog::getCategory, 2)
                        .set(CoinLog::getUpdatedAt, DateUtils.getCurrentTime())
                        //'状态:0-处理中 1-成功 2-失败'
                        .set(CoinLog::getStatus, 1)
                        .update();
            }
            return coinOnlineWithdrawalServiceImpl.lambdaUpdate().set(nonNull(reqBody.getStatus()), CoinOnlineWithdrawal::getStatus, reqBody.getStatus())
                    .eq(CoinOnlineWithdrawal::getOrderId, reqBody.getOrderId())
                    .eq(CoinOnlineWithdrawal::getStatus, 0).update();
        } finally {
            if (b) {
                reentrantLock.unlock();
            }
        }
    }

    /**
     * 平台转账:获取备注信息
     *
     * @param category   转账类型:0-转出(主->第三方) 1-转入(第三方->主)
     * @param platListId 平台ID
     * @return 备注信息
     */
    private String getMark(Integer category, Integer platListId) {
        String method = "";
        // 转账类型:0-转出(主->第三方) 1-转入(第三方->主)
        PlatList platList = gameCache.getPlatListCache(platListId);
        if (null != platList) {
            if (category == 0) {
                method = "Platform -> " + platList.getName();
            } else {
                method = platList.getName() + " -> Platform";
            }
        }
        return method;
    }

    private QueryWrapper<CoinOnlineWithdrawal> whereOnlineWithdrawalListOrStatistics(FinancialRecords.OnlineWithdrawalListReqBody data) {
        QueryWrapper<CoinOnlineWithdrawal> wrapper = null;
        if (null != data) {
            wrapper = Wrappers.query();
            wrapper.eq(nonNull(data.getUsername()), "username", data.getUsername());
            wrapper.eq(nonNull(data.getOrderId()), "order_id", data.getOrderId());
            wrapper.eq(nonNull(data.getWithdrawalOrderId()), "withdrawal_order_id", data.getWithdrawalOrderId());
            wrapper.eq(nonNull(data.getCategory()), Constant.CATEGORY, data.getCategory());
            wrapper.eq(nonNull(data.getStatus()), Constant.STATUS, data.getStatus());
            wrapper.ge(nonNull(data.getStartTime()), Constant.UPDATED_AT, data.getStartTime());
            wrapper.le(nonNull(data.getEndTime()), Constant.UPDATED_AT, data.getEndTime());
        }
        return wrapper;
    }

}
