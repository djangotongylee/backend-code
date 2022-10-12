package com.xinbo.sports.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xinbo.sports.backend.base.FinancialManagementBase;
import com.xinbo.sports.backend.io.dto.FinancialAdminParameter.AdminTransferReqDto;
import com.xinbo.sports.backend.io.dto.FinancialAdminParameter.PromotionsListReqDto;
import com.xinbo.sports.backend.io.dto.FinancialAdminParameter.UserCoinReqDto;
import com.xinbo.sports.backend.io.dto.FinancialAdminParameter.UserCoinResDto;
import com.xinbo.sports.backend.service.FinancialAdminService;
import com.xinbo.sports.dao.generator.po.CoinDeposit;
import com.xinbo.sports.dao.generator.po.Promotions;
import com.xinbo.sports.dao.generator.po.User;
import com.xinbo.sports.dao.generator.po.UserBank;
import com.xinbo.sports.dao.generator.service.CoinDepositService;
import com.xinbo.sports.dao.generator.service.UserBankService;
import com.xinbo.sports.dao.generator.service.UserService;
import com.xinbo.sports.service.base.PromotionsBase;
import com.xinbo.sports.service.base.UserServiceBase;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.service.cache.redis.PromotionsCache;
import com.xinbo.sports.service.cache.redis.UserCache;
import com.xinbo.sports.service.common.Constant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.dto.promotions.ApplicationActivityReqDto;
import com.xinbo.sports.utils.BeanConvertUtils;
import com.xinbo.sports.utils.components.response.CodeInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.xinbo.sports.backend.io.dto.FinancialAdminParameter.OperatorTypeEnum.*;
import static com.xinbo.sports.service.common.Constant.FIRST_DEPOSIT_DOUBLE;
import static com.xinbo.sports.service.common.Constant.FIRST_DEPOSIT_SUPER_BONUS;
import static java.util.Objects.nonNull;

/**
 * @author: wells
 * @date: 2020/8/13
 * @description:
 */
@Service
public class FinancialAdminServiceImpl implements FinancialAdminService {
    @Autowired
    private PromotionsBase promotionsBase;
    @Autowired
    private PromotionsCache promotionsCache;
    @Autowired
    private FinancialManagementBase financialManagementBase;
    @Autowired
    private UserService userServiceImpl;
    @Autowired
    private ConfigCache configCache;
    @Autowired
    private UserCache userCache;
    @Resource
    private UserBankService userBankServiceImpl;
    @Resource
    private CoinDepositService coinDepositServiceImpl;
    @Resource
    private UserServiceBase userServiceBase;
    //充值状态
    public static final List<Integer> DEPOSIT_STATUS = List.of(1, 2, 9);

    /**
     * 人工调账
     * 1.判断操作类型；1：活动派彩，2：系统调账
     * 活动派彩：
     * a.插入奖金表记录 b.插入日志表 c.插入调账表 d.修改用户金额
     * 系统调整
     * a.插入日志表 b.插入调账表c.修改用户金额
     */
    @Override
    public boolean adminTransfer(AdminTransferReqDto reqDto) {
        //判断操作类型；1：活动派彩，
        if (reqDto.getOperatorType().equals(ACTIVITY.getCode())) {
            var promotions = promotionsCache.getPromotionsCache(reqDto.getPromotionsId());
            var info = promotions.getInfo();
            //查询活动流水倍率
            var rate = StringUtils.isEmpty(info) ? BigDecimal.ONE : parseObject(info).getBigDecimal("flowClaim");
            var mosaicCoin = reqDto.getCoin().multiply(rate);
            var activityReqDto = ApplicationActivityReqDto.builder()
                    .availableCoin(reqDto.getCoin())
                    .mosaicCoin(mosaicCoin)
                    .id(reqDto.getPromotionsId())
                    .info("{}")
                    .build();
            //首充或超高红利
            if (reqDto.getPromotionsId().equals(FIRST_DEPOSIT_DOUBLE) || reqDto.getPromotionsId().equals(FIRST_DEPOSIT_SUPER_BONUS)) {
                //获取奖励表的扩展信息
                var coinDeposit = coinDepositServiceImpl.getOne(new LambdaQueryWrapper<CoinDeposit>()
                                .eq(CoinDeposit::getUid, reqDto.getUid())
                                .in(CoinDeposit::getStatus, DEPOSIT_STATUS)
                                .orderByAsc(CoinDeposit::getCreatedAt)
                        , false);
                if (Objects.isNull(coinDeposit)) {
                    throw new BusinessException(CodeInfo.ACTIVE_NEW_SEASON_NO_RECORD);
                }
                mosaicCoin = coinDeposit.getPayCoin().multiply(rate).add(mosaicCoin);
                activityReqDto.setMosaicCoin(mosaicCoin);
            }
            promotionsBase.executePromotionsPersistence(activityReqDto, reqDto.getUid());
            return true;
            //2：系统调账
        } else if (reqDto.getOperatorType().equals(SYSTEM_TRANSFER.getCode())) {
            return financialManagementBase.transferPersistence(reqDto);
            //3：后台充值
        } else if (reqDto.getOperatorType().equals(BACKEND_DEPOSIT.getCode())) {
            return financialManagementBase.backendDeposit(reqDto);
            //4:后台提款
        } else if (reqDto.getOperatorType().equals(BACKEND_WITHDRAWAL.getCode())) {
            return financialManagementBase.backendWithdrawal(reqDto);
        }
        return false;
    }

    /**
     * 查询用户余额
     */
    @Override
    public UserCoinResDto getUserCoin(UserCoinReqDto reqDto) {
        var user = userServiceImpl.list(new QueryWrapper<User>().eq(nonNull(reqDto.getUid()), "id", reqDto.getUid())
                .or().eq(nonNull(reqDto.getUsername()), "username", reqDto.getUsername()));
        if (user.size() != 1) {
            throw new BusinessException(CodeInfo.ACCOUNT_ERROR);
        }
        List<UserBank> userBankList = userBankServiceImpl.lambdaQuery()
                .eq(nonNull(reqDto.getUid()), UserBank::getUid, reqDto.getUid())
                .eq(nonNull(reqDto.getUsername()), UserBank::getUsername, reqDto.getUsername())
                .list();
        var unaryOperator = userServiceBase.checkShow(Constant.BANK_ACCOUNT);
        userBankList.forEach(userBank -> userBank.setBankAccount(unaryOperator.apply(userBank.getBankAccount())));
        return BeanConvertUtils.copyProperties(user.get(0), UserCoinResDto::new, (sb, bo) -> {
            bo.setUid(sb.getId());
            var userLevel = userCache.getUserLevelById(sb.getLevelId());
            bo.setLevel(userLevel.getCode() + "-" + userLevel.getName());
            bo.setAvatar( sb.getAvatar().startsWith("http")?  sb.getAvatar():configCache.getStaticServer() + sb.getAvatar());
            bo.setUserBank(userBankList);
        });
    }

    /**
     * 获取活动列表
     */
    @Override
    public List<PromotionsListReqDto> getPromotionsList() {
        var promotionsList = promotionsCache.getPromotionsListCache().stream()
                .filter(x -> x.getPayoutCategory().equals(1))
                .sorted(Comparator.comparing(Promotions::getSort))
                .collect(Collectors.toList());
        return BeanConvertUtils.copyListProperties(promotionsList, PromotionsListReqDto::new);
    }
}
