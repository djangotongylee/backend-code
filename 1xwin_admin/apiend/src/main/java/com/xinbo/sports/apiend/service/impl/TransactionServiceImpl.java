package com.xinbo.sports.apiend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinbo.sports.apiend.io.dto.wallet.*;
import com.xinbo.sports.apiend.service.ITransactionService;
import com.xinbo.sports.dao.generator.po.*;
import com.xinbo.sports.dao.generator.service.*;
import com.xinbo.sports.service.base.AuditBase;
import com.xinbo.sports.service.base.MarkBase;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.service.cache.redis.PromotionsCache;
import com.xinbo.sports.service.cache.redis.UserCache;
import com.xinbo.sports.service.common.Constant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.dto.BaseParams;
import com.xinbo.sports.service.io.dto.UserInfo;
import com.xinbo.sports.service.thread.ThreadHeaderLocalData;
import com.xinbo.sports.utils.BeanConvertUtils;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.xinbo.sports.service.common.Constant.COIN_RANGE;
import static com.xinbo.sports.service.common.Constant.MIN_COIN;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * <p>
 * 交易记录业务处理接口实现类
 * </p>
 *
 * @author andy
 * @since 2020/4/21
 */
@Slf4j
@Service
public class TransactionServiceImpl implements ITransactionService {
    @Resource
    private CoinLogService coinLogServiceImpl;
    @Resource
    private CoinDepositService coinDepositServiceImpl;
    @Resource
    private CoinWithdrawalService coinWithdrawalServiceImpl;
    @Resource
    private CoinPlatTransferService coinPlatTransferServiceImpl;
    @Resource
    private CoinRebateService coinRebateServiceImpl;
    @Resource
    private CoinRewardsService coinRewardsServiceImpl;
    @Resource
    private PayOfflineService payOfflineServiceImpl;
    @Resource
    private PayOnlineService payOnlineServiceImpl;
    @Resource
    private BankListService bankListServiceImpl;
    @Resource
    private CoinCommissionService coinCommissionServiceImpl;
    @Resource
    private UserInfoServiceImpl userInfoServiceImpl;
    @Resource
    private PlatListService platListServiceImpl;
    @Resource
    private CoinRewardsInviteService coinRewardsInviteServiceImpl;
    @Resource
    private UserCache userCache;
    @Resource
    private PromotionsCache promotionsCache;
    @Resource
    private CodeAuditService codeAuditServiceImpl;
    @Resource
    private AuditBase auditBase;
    @Resource
    private ConfigCache configCache;
    @Resource
    private UserService userServiceImpl;

    /**
     * 交易记录-列表查询
     * <p>
     * 描述：封装查询条件
     *
     * @param req
     * @return
     */
    private static LambdaQueryWrapper<CoinLog> whereTransactionList(TransactionListReqBody req) {
        LambdaQueryWrapper<CoinLog> where = Wrappers.lambdaQuery();
        if (null != req.getUid()) {
            where.eq(CoinLog::getUid, req.getUid());
        }
        if (null != req.getCategory()) {
            where.eq(CoinLog::getCategory, req.getCategory());
        }
        if (null != req.getStartTime()) {
            where.ge(CoinLog::getCreatedAt, req.getStartTime());
        }
        if (null != req.getEndTime()) {
            where.le(CoinLog::getCreatedAt, req.getEndTime());
        }
        return where;
    }

    /**
     * 交易记录-列表查询-充值
     * <p>
     * 描述：封装查询充值记录条件
     *
     * @param req REQ
     * @return 查询充值记录条件
     */
    private static LambdaQueryWrapper<CoinDeposit> whereCoinDepositList(TransactionListReqBody req) {
        LambdaQueryWrapper<CoinDeposit> where = Wrappers.lambdaQuery();
        if (null != req.getUid()) {
            where.eq(CoinDeposit::getUid, req.getUid());
        }
        if (null != req.getStartTime()) {
            where.ge(CoinDeposit::getCreatedAt, req.getStartTime());
        }
        if (null != req.getEndTime()) {
            where.le(CoinDeposit::getCreatedAt, req.getEndTime());
        }
        return where;
    }

    private static TransactionDetailResBody.Detail buildTransactionDetailResBody(Long id, BigDecimal coin, Integer createdAt,
                                                                                 Integer status, String name, Integer type) {
        return TransactionDetailResBody.Detail.builder()
                .coin(coin)
                .createdAt(createdAt)
                .status(status)
                .name(name)
                .type(type)
                .id(id)
                .build();
    }

    private static UserBank parseUserBank(String bankInfo) {
        UserBank userBank = parseObject(bankInfo, UserBank.class);
        if (Optional.ofNullable(userBank).isEmpty()) {
            return null;
        }
        return userBank;
    }

    @Override
    public ResPage<TransactionListResBody> transactionList(ReqPage<TransactionListReqBody> reqBody) {
        UserInfo userInfo = userInfoServiceImpl.findIdentityByApiToken();
        if (Objects.isNull(userInfo)) {
            throw new BusinessException(CodeInfo.PLAT_ACCOUNT_NOT_EXISTS);
        }
        if (Objects.isNull(reqBody)) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        TransactionListReqBody reqBodyData = reqBody.getData();
        if (Objects.isNull(reqBodyData)) {
            reqBodyData = TransactionListReqBody.builder().build();
        }
        Integer category = reqBodyData.getCategory();
        if (Objects.isNull(category)) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        // 设置UID
        reqBodyData.setUid(userInfo.getId());
        // 直接查询sp_coin_deposit表记录，否则查sp_coin_log表记录
        if (Constant.API_COIN_LOG_CATEGORY_CK == category) {
            Page<CoinDeposit> page = coinDepositServiceImpl.page(reqBody.getPage(), whereCoinDepositList(reqBodyData));
            Page<TransactionListResBody> tmpPage = BeanConvertUtils.copyPageProperties(page, TransactionListResBody::new);
            ResPage<TransactionListResBody> resPage = ResPage.get(tmpPage);
            resPage.setList(processCoinDepositList(page.getRecords()));
            return resPage;
        }
        Page<CoinLog> page = coinLogServiceImpl.page(reqBody.getPage(), whereTransactionList(reqBodyData));
        Page<TransactionListResBody> tmpPage = BeanConvertUtils.copyPageProperties(page, TransactionListResBody::new);
        // 类型:1-存款 2-提款 3-上分 4-下分 5-返水 6-佣金 7-活动(奖励)
        for (TransactionListResBody resBody : tmpPage.getRecords()) {
            if (nonNull(resBody)) {
                processTransactionListResBody(resBody.getCategory(), resBody.getReferId(), resBody, resBody.getSubCategory());
            }
        }
        return ResPage.get(tmpPage);
    }

    private List<TransactionListResBody> processCoinDepositList(List<CoinDeposit> depositList) {
        List<TransactionListResBody> resBodyList = new ArrayList<>();
        for (CoinDeposit deposit : depositList) {
            if (nonNull(deposit)) {
                resBodyList.add(processDePosit(deposit));
            }
        }
        return resBodyList;
    }

    @Override
    public Object transactionDetail(@Valid TransactionDetailReqBody reqBody) {
        checkParams(reqBody);
        return processTransactionDetail(reqBody.getCategory(), reqBody.getReferId(), reqBody.getSubCategory());
    }

    private Object processTransactionDetail(Integer type, Long referId, Integer subCategory) {
        Object result = null;
        switch (type) {
            case Constant.API_COIN_LOG_CATEGORY_CK:
                result = processDePositDetail(referId, type);
                break;
            case Constant.API_COIN_LOG_CATEGORY_TK:
                result = processWithdrawalDetail(referId, type);
                break;
            case Constant.API_COIN_LOG_CATEGORY_SF:
            case Constant.API_COIN_LOG_CATEGORY_XF:
                result = getCoinPlatTransferDetailResBody(referId, type);
                break;
            case Constant.API_COIN_LOG_CATEGORY_FS:
                result = getCoinRebateDetailResBody(referId, type);
                break;
            case Constant.API_COIN_LOG_CATEGORY_YJ:
                result = getCoinCommissionDetailResBody(referId, type);
                break;
            case Constant.API_COIN_LOG_CATEGORY_HD:
                result = getCoinRewardsDetailResBody(referId, type, subCategory);
                break;
            default:
                break;
        }
        return result;
    }

    private TransactionDetailResBody.Detail getCoinCommissionDetailResBody(Long referId, Integer type) {
        CoinCommission po = getCoinCommissionById(referId);
        if (Optional.ofNullable(po).isEmpty()) {
            throw new BusinessException((CodeInfo.API_RECORDS_NOT_EXISTS));
        }
        return buildTransactionDetailResBody(po.getId(), po.getCoin(), po.getCreatedAt(), po.getStatus(), po.getRiqi() + "", type);
    }

    private TransactionDetailResBody.Detail getCoinRewardsDetailResBody(Long referId, Integer type, Integer subCategory) {
        CoinRewards po = getCoinRewardsById(referId, subCategory);
        if (Optional.ofNullable(po).isEmpty()) {
            throw new BusinessException((CodeInfo.API_RECORDS_NOT_EXISTS));
        }
        // 好友奖励:对应sp_promotions表的主键ID=9
        if (null != subCategory && 9 == subCategory) {
            // 获取邀请好友奖励详情
            return buildCoinRewardsInvite(po, type);
        }
        var title = promotionsCache.getPromotionsCache(po.getReferId()).getCodeZh();
        return buildTransactionDetailResBody(po.getId(), po.getCoin(), po.getCreatedAt(), po.getStatus(), title, type);
    }

    /**
     * 获取邀请好友奖励详情
     *
     * @param po   邀请好友奖励实体
     * @param type sp_promotions表的主键ID=9
     * @return 邀请好友奖励详情
     */
    private TransactionDetailResBody.Detail buildCoinRewardsInvite(CoinRewards po, Integer type) {
        CoinRewardsInvite coinRewardsInvite = coinRewardsInviteServiceImpl.lambdaQuery().eq(CoinRewardsInvite::getReferId, po.getId()).one();
        if (Optional.ofNullable(coinRewardsInvite).isEmpty()) {
            return null;
        }
        var title = promotionsCache.getPromotionsCache(po.getReferId()).getCodeZh();
        TransactionDetailResBody.Detail detail = buildTransactionDetailResBody(po.getId(), po.getCoin(), po.getCreatedAt(),
                po.getStatus(), title, type);
        // 好友注册时间
        detail.setFCreateAt(coinRewardsInvite.getCreatedAt());
        return detail;
    }

    private TransactionDetailResBody.Detail getCoinPlatTransferDetailResBody(Long referId, Integer type) {
        CoinPlatTransfer po = getCoinPlatTransferById(referId, type);
        if (Optional.ofNullable(po).isEmpty()) {
            throw new BusinessException((CodeInfo.API_RECORDS_NOT_EXISTS));
        }
        String method = getMethod(po.getCategory(), po.getPlatListId());
        return buildTransactionDetailResBody(po.getId(), po.getCoin(), po.getCreatedAt(), po.getStatus(), method, type);
    }

    private TransactionDetailResBody.Detail getCoinRebateDetailResBody(Long referId, Integer type) {
        CoinRebate po = getCoinRebateById(referId);
        if (Optional.ofNullable(po).isEmpty()) {
            throw new BusinessException((CodeInfo.API_RECORDS_NOT_EXISTS));
        }
        return buildTransactionDetailResBody(po.getId(), po.getCoin(), po.getCreatedAt(), po.getStatus(), po.getTitle(), type);
    }

    /**
     * 处理转账方式
     *
     * @param category
     * @param gameListId
     * @return
     */
    private String getMethod(Integer category, Integer gameListId) {
        String method = "";
        // 转账类型:0-转出(主->第三方) 1-转入(第三方->主)
        PlatList gameList = getPlatListById(gameListId);
        if (nonNull(gameList)) {
            if (category == 0) {
                method = "Platform -> " + gameList.getName();
            } else {
                method = gameList.getName() + " -> Platform";
            }
        }
        return method;
    }

    private void processTransactionListResBody(Integer category, Long referId, TransactionListResBody resBody, Integer subCategory) {
        switch (category) {
            case Constant.API_COIN_LOG_CATEGORY_TK:
                processWithdrawal(referId, resBody);
                break;
            case Constant.API_COIN_LOG_CATEGORY_SF:
            case Constant.API_COIN_LOG_CATEGORY_XF:
                processPlatTransfer(category, referId, resBody);
                break;
            case Constant.API_COIN_LOG_CATEGORY_YJ:
                processCoinCommission(referId, resBody);
                break;
            case Constant.API_COIN_LOG_CATEGORY_HD:
                processRewards(referId, resBody, subCategory);
                break;
            default:
                break;
        }
    }

    private void processRewards(Long referId, TransactionListResBody resBody, Integer subCategory) {
        // 返水
        if (null != subCategory && 10 == subCategory) {
            processRebate(referId, resBody);
        } else {
            CoinRewards entity = getCoinRewardsById(referId);
            if (nonNull(entity) && nonNull(resBody)) {
                var title = Optional.ofNullable(promotionsCache.getPromotionsCache(entity.getReferId()))
                        .map(Promotions::getCodeZh)
                        .orElse("");
                resBody.setStatus(entity.getStatus());
                resBody.setName(title);
            }
        }
    }

    private void processCoinCommission(Long referId, TransactionListResBody resBody) {
        CoinCommission entity = getCoinCommissionById(referId);
        if (nonNull(entity) && nonNull(resBody)) {
            resBody.setStatus(entity.getStatus());
            resBody.setName(new StringBuilder(entity.getRiqi() + "").insert(4, "-").toString());
            resBody.setSubCategory(entity.getCategory());
        }
    }

    private void processRebate(Long referId, TransactionListResBody resBody) {
        CoinRebate entity = getCoinRebateById(referId);
        if (nonNull(entity) && nonNull(resBody)) {
            resBody.setStatus(entity.getStatus());
            resBody.setName(entity.getTitle());
        }
    }

    private void processPlatTransfer(Integer category, Long referId, TransactionListResBody resBody) {
        CoinPlatTransfer entity = getCoinPlatTransferById(referId, category);
        if (nonNull(entity) && nonNull(resBody)) {
            resBody.setStatus(entity.getStatus());
            String method = getMethod(entity.getCategory(), entity.getPlatListId());
            resBody.setName(method);
        }
    }

    private void processWithdrawal(Long referId, TransactionListResBody resBody) {
        CoinWithdrawal entity = getCoinWithdrawalById(referId);
        if (isNull(entity)) {
            return;
        }
        //提款失败与稽核失败则展示备注
        var mark = "";
        if (entity.getStatus() == 2 || entity.getStatus() == 4) {
            mark = MarkBase.spliceMark(entity.getMark());
        }
        resBody.setMark(mark);
        resBody.setStatus(entity.getStatus());
        if (Optional.ofNullable(entity.getBankInfo()).isEmpty()) {
            return;
        }
        UserBank userBank = parseUserBank(entity.getBankInfo());
        if (null != userBank) {
            resBody.setBankAccount(userBank.getBankAccount());
            BankList bank = getBankListById(userBank.getBankId());
            if (Optional.ofNullable(bank).isEmpty()) {
                return;
            }
            resBody.setName(bank.getName());
        }
    }

    private TransactionListResBody processDePosit(CoinDeposit entity) {
        TransactionListResBody resBody = new TransactionListResBody();
        resBody.setReferId(entity.getId());
        resBody.setCoin(entity.getCoin());
        resBody.setCreatedAt(entity.getCreatedAt());
        resBody.setCategory(Constant.API_COIN_LOG_CATEGORY_CK);
        resBody.setStatus(entity.getStatus());
        resBody.setPayCoin(entity.getPayCoin());
        if (Constant.API_COIN_DEPOSIT_PAY_TYPE_OFFLINE == entity.getPayType()) {
            PayOffline payOffline = getPayOfflineById(entity.getPayRefer());
            if (null != payOffline) {
                resBody.setBankAccount(payOffline.getBankAccount());
                resBody.setMethod(payOffline.getCategory());
                resBody.setName(payOffline.getBankName());
            }
        } else {
            PayOnline online = getPayOnlineById(entity.getPayRefer());
            if (null != online) {
                resBody.setName(online.getPayName());
                resBody.setMethod(online.getCategory());
            }
        }
        resBody.setSubCategory(0);
        return resBody;
    }

    private TransactionDetailResBody.DePosit processDePositDetail(Long id, Integer type) {
        CoinDeposit coinDeposit = getCoinDepositById(id);
        TransactionDetailResBody.DePosit resBody = BeanConvertUtils.beanCopy(coinDeposit, TransactionDetailResBody.DePosit::new);
        if (Optional.ofNullable(coinDeposit).isEmpty() || Optional.ofNullable(resBody).isEmpty()) {
            throw new BusinessException((CodeInfo.API_RECORDS_NOT_EXISTS));
        }
        if (Constant.API_COIN_DEPOSIT_PAY_TYPE_OFFLINE == coinDeposit.getPayType()) {
            PayOffline payOffline = getPayOfflineById(coinDeposit.getPayRefer());
            if (null != payOffline) {
                resBody.setBankAccount(payOffline.getBankAccount());
                resBody.setBankName(payOffline.getBankName());
                resBody.setAccountName(payOffline.getUserName());
            }
        } else {
            PayOnline online = getPayOnlineById(coinDeposit.getPayRefer());
            if (null != online) {
                resBody.setBankName(online.getPayName());
            }
        }
        resBody.setType(type);
        return resBody;
    }


    private TransactionDetailResBody.Withdrawal processWithdrawalDetail(long referId, Integer type) {
        CoinWithdrawal entity = getCoinWithdrawalById(referId);
        TransactionDetailResBody.Withdrawal resBody = BeanConvertUtils.beanCopy(entity, TransactionDetailResBody.Withdrawal::new);
        if (Optional.ofNullable(entity).isEmpty() || Optional.ofNullable(resBody).isEmpty()) {
            throw new BusinessException((CodeInfo.API_RECORDS_NOT_EXISTS));
        }
        //提款失败与稽核失败则展示备注
        var mark = "";
        if (entity.getStatus() == 2 || entity.getStatus() == 4) {
            mark = MarkBase.spliceMark(entity.getMark());
        }
        resBody.setMark(mark);
        resBody.setStatus(entity.getStatus());
        if (Optional.ofNullable(entity.getBankInfo()).isEmpty()) {
            return resBody;
        }
        UserBank userBank = parseUserBank(entity.getBankInfo());
        if (nonNull(userBank)) {
            resBody.setBankAccount(userBank.getBankAccount());
            BankList bank = getBankListById(userBank.getBankId());
            if (nonNull(bank)) {
                resBody.setBankName(bank.getName());
            }
        }
        resBody.setType(type);
        return resBody;
    }

    private CoinWithdrawal getCoinWithdrawalById(Long id) {
        return coinWithdrawalServiceImpl.getById(id);
    }

    private BankList getBankListById(Integer id) {
        return bankListServiceImpl.getById(id);
    }

    private CoinCommission getCoinCommissionById(Long id) {
        return coinCommissionServiceImpl.getById(id);
    }

    private CoinPlatTransfer getCoinPlatTransferById(Long id, Integer category) {
        return coinPlatTransferServiceImpl.lambdaQuery()
                .eq(CoinPlatTransfer::getCategory, category == 3 ? 0 : 1)
                .eq(CoinPlatTransfer::getId, id)
                .one();
    }

    private CoinRebate getCoinRebateById(Long id) {
        return coinRebateServiceImpl.getById(id);
    }

    private CoinRewards getCoinRewardsById(Long id) {
        return coinRewardsServiceImpl.getById(id);
    }

    private CoinRewards getCoinRewardsById(Long id, Integer referId) {
        if (Objects.isNull(referId)) {
            return getCoinRewardsById(id);
        } else {
            return coinRewardsServiceImpl.lambdaQuery().eq(CoinRewards::getId, id).eq(CoinRewards::getReferId, referId).one();
        }
    }

    private CoinDeposit getCoinDepositById(Long id) {
        return coinDepositServiceImpl.getById(id);
    }

    private PayOffline getPayOfflineById(Integer id) {
        return payOfflineServiceImpl.getById(id);
    }

    private PayOnline getPayOnlineById(Integer id) {
        return payOnlineServiceImpl.getById(id);
    }

    private PlatList getPlatListById(Integer id) {
        return platListServiceImpl.getById(id);
    }

    /**
     * 钱包-交易记录-邀请记录
     *
     * @param reqBody
     * @return
     */
    @Override
    public TransactionInvite.ResBody transactionInvite(ReqPage<TransactionInvite.ReqBody> reqBody) {
        //获取用户信息
        var reqDto = reqBody.getData();
        var userInfo = userInfoServiceImpl.findIdentityByApiToken();
        var wrapper = new LambdaQueryWrapper<CoinRewards>()
                .eq(nonNull(userInfo.getId()), CoinRewards::getUid, userInfo.getId())
                .eq(CoinRewards::getReferId, Constant.INVITE_FRIENDS)
                .ge(nonNull(reqDto.getStartTime()), CoinRewards::getCreatedAt, reqDto.getStartTime())
                .le(nonNull(reqDto.getEndTime()), CoinRewards::getCreatedAt, reqDto.getEndTime());
        var coinRewardsList = coinRewardsServiceImpl.list(wrapper);
        //邀请总奖励
        var totalInviteCoin = BigDecimal.ZERO;
        //返利总奖金
        var totalRebateCoin = BigDecimal.ZERO;
        var rewardMap = coinRewardsList.stream().collect(Collectors.toMap(CoinRewards::getId, v -> v));
        var rewardsQuery = new LambdaQueryWrapper<CoinRewardsInvite>();
        if (!CollectionUtils.isEmpty(rewardMap.keySet())) {
            rewardsQuery.in(CoinRewardsInvite::getReferId, rewardMap.keySet());
        } else {
            return TransactionInvite.ResBody.builder()
                    .totalInviteCoin(totalInviteCoin)
                    .totalRewardCoin(totalRebateCoin)
                    .resPage(ResPage.get(new Page<>()))
                    .build();
        }
        Page<CoinRewardsInvite> invitePage = coinRewardsInviteServiceImpl.page(reqBody.getPage(), rewardsQuery);
        var records = invitePage.getRecords();
        if (!CollectionUtils.isEmpty(records)) {
            //类型:0:被邀请奖励,1-邀请奖金 2-充值返利',
            totalInviteCoin = records.stream().filter(x -> x.getCategory() == 1 || x.getCategory() == 0)
                    .map(coinRewardsInvite -> rewardMap.get(coinRewardsInvite.getReferId()).getCoin())
                    .reduce(BigDecimal::add)
                    .orElse(BigDecimal.ZERO);
            totalRebateCoin = records.stream().filter(x -> x.getCategory() == 2)
                    .map(coinRewardsInvite -> rewardMap.get(coinRewardsInvite.getReferId()).getCoin())
                    .reduce(BigDecimal::add)
                    .orElse(BigDecimal.ZERO);
        }
        Page<TransactionInvite.ResListBody> returnPage = BeanConvertUtils.copyPageProperties(invitePage, TransactionInvite.ResListBody::new, (source, resListBody) -> {
            var coinReward = rewardMap.get(source.getReferId());
            var user = userCache.getUserInfoById(coinReward.getUid());
            resListBody.setUserName(user.getUsername());
            resListBody.setRegisterAt(user.getCreatedAt());
            resListBody.setRewardCoin(coinReward.getCoin());
            resListBody.setStatus(coinReward.getStatus());
        });
        return TransactionInvite.ResBody.builder()
                .totalInviteCoin(totalInviteCoin)
                .totalRewardCoin(totalRebateCoin)
                .resPage(ResPage.get(returnPage))
                .build();
    }

    /**
     * 提款提示信息及金额
     */
    @Override
    public WithdrawalHint.HintResBody withdrawalHint() {
        BaseParams.HeaderInfo headerInfo = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        var mark = auditBase.withdrawalHint(headerInfo.getId());
        //mark提示信息为空则默认额外打码量
        var codeRequire = StringUtils.isNotBlank(mark) ? new BigDecimal(mark) : BigDecimal.ZERO;
        //显示单笔最低额度
        var minDrawCoinJson = parseObject(configCache.getConfigByTitle(WithdrawalHint.MIN_DRAW_COIN));
        var minCoin = BigDecimal.valueOf(minDrawCoinJson.getInteger(WithdrawalHint.MIN_COIN));
        var maxCoin = BigDecimal.valueOf(minDrawCoinJson.getInteger(WithdrawalHint.MAX_COIN));

        var user = userServiceImpl.getById(headerInfo.id);
        var pair = auditBase.getWithdrawalInfo(headerInfo.getId(), user.getLevelId());
        //可提款次数
        var availableCount = pair.getLeft();
        //可提款金额
        var availableCoin = pair.getRight();
        var coin = availableCoin.compareTo(user.getCoin()) > 0 ? user.getCoin() : availableCoin;
        coin = maxCoin.compareTo(coin) > 0 ? coin : maxCoin;
        //获取快捷金额
        var jsonArray = minDrawCoinJson.getJSONArray(COIN_RANGE);
        return WithdrawalHint.HintResBody.builder()
                .codeRequire(codeRequire)
                .maxCoin(maxCoin)
                .minCoin(minCoin)
                .coin(coin)
                .surplusCoin(availableCoin)
                .withdrawalCount(availableCount)
                .fastCoin(jsonArray.toJavaList(Integer.class))
                .build();
    }
}
