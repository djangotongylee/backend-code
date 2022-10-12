package com.xinbo.sports.apiend.service;

import com.xinbo.sports.apiend.io.dto.centeragent.*;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;
import com.xinbo.sports.utils.components.response.CodeInfo;

import javax.validation.Valid;

/**
 * <p>
 * 代理中心业务处理接口
 * </p>
 *
 * @author andy
 * @since 2020/4/22
 */
public interface ICenterAgentService {

    /**
     * 代理中心-我的下级-列表
     *
     * @param reqBody reqBody
     * @return 下级分页列表
     */
    SubordinateListResBody subordinateList(ReqPage<SubordinateListReqBody> reqBody);

    /**
     * 代理中心-我的下级-统计
     *
     * @return 统计金额实体
     */
    SubordinateStatisticsResBody subordinateStatistics();

    /**
     * 代理中心-我的下级-列表-直推人数列表(弹框)
     *
     * @param reqBody reqBody
     * @return 直推人数列表
     */
    SubordinateListZtResBody subordinateListZT(ReqPage<SubordinateListZtReqBody> reqBody);

    /**
     * 代理中心-我的报表-充值与提现详情
     *
     * @param reqBody reqBody
     * @return 充值与提现详情列表
     */
    ResPage<DepositWithdrawalDetailsResBody> depositWithdrawalDetails(ReqPage<DepositWithdrawalDetailsReqBody> reqBody);

    /**
     * 代理中心-我的报表-充值与提现详情-用户详情-列表
     *
     * @param reqBody reqBody
     * @return 用户详情
     */
    ResPage<SubordinateInfo> depositWithdrawalUserDetails(ReqPage<DepositWithdrawalUserDetailsReqBody> reqBody);

    /**
     * 代理中心-我的报表-充值与提现详情-用户详情-统计
     *
     * @param reqBody reqBody
     * @return 用户详情
     */
    DepositWithdrawalUserDetailsResBody depositWithdrawalUserDetailsStatistics(@Valid DepositWithdrawalUserDetailsReqBody reqBody);

    /**
     * 代理中心-我的报表-玩家活跃度详情
     *
     * @param reqBody reqBody
     * @return 玩家活跃度详情列表
     */
    ResPage<PlayerActivityDetailsResBody> playerActivityDetails(ReqPage<PlayerActivityDetailsReqBody> reqBody);

    /**
     * 代理中心-我的报表-奖金与佣金详情-列表
     *
     * @param reqBody ReqBody
     * @return 奖金与佣金详情列表
     */
    ResPage<RewardsCommissionDetailsResBody> rewardsCommissionDetails(ReqPage<RewardsCommissionDetailsReqBody> reqBody);

    /**
     * 代理中心-我的报表-奖金与佣金详情-统计
     *
     * @param reqBody ReqBody
     * @param uid     UID
     * @return 统计实体
     */
    RewardsCommissionDetailsStatisticsResBody rewardsCommissionDetailsStatistics(RewardsCommissionDetailsReqBody reqBody, Integer uid);

    /**
     * 代理中心-我的报表-充值与提现详情-统计
     *
     * @param reqBody ReqBody
     * @return 统计实体
     */
    DepositWithdrawalDetailsStatisticsResBody depositWithdrawalDetailsStatistics(DepositWithdrawalDetailsReqBody reqBody);

    /**
     * 代理中心-我的报表-玩家活跃度详情-统计
     *
     * @param reqBody ReqBody
     * @return 统计实体
     */
    DepositWithdrawalDetailsStatisticsResBody playerActivityDetailsStatistics(PlayerActivityDetailsReqBody reqBody);

    /**
     * 检查用户是否存在
     *
     * @param user USER
     * @param <U>  <U>
     */
    default <U> void checkUserIsNotExists(U user) {
        if (null == user) {
            throw new BusinessException(CodeInfo.ACCOUNT_NOT_EXISTS);
        }
    }

    /**
     * 代理中心-我的报表-奖金与佣金详情-列表->活跃会员佣金->会员详情
     *
     * @param reqBody ReqBody
     * @return
     */
    ResPage<SubordinateInfo> rewardsCommissionActiveDetails(ReqPage<RewardsCommissionDetailsActiveReqBody> reqBody);
}
