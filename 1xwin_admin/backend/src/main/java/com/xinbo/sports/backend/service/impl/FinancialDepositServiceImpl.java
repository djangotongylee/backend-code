package com.xinbo.sports.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinbo.sports.backend.base.DictionaryBase;
import com.xinbo.sports.backend.base.FinancialManagementBase;
import com.xinbo.sports.backend.io.dto.FinancialDepositParameter.*;
import com.xinbo.sports.backend.service.FinancialDepositService;
import com.xinbo.sports.dao.generator.po.CoinDeposit;
import com.xinbo.sports.dao.generator.service.CoinDepositService;
import com.xinbo.sports.dao.generator.service.PayOfflineService;
import com.xinbo.sports.dao.generator.service.UserService;
import com.xinbo.sports.service.base.NoticeBase;
import com.xinbo.sports.service.common.Constant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.dto.BaseParams;
import com.xinbo.sports.service.io.enums.NoticeEnum;
import com.xinbo.sports.service.thread.ThreadHeaderLocalData;
import com.xinbo.sports.utils.BeanConvertUtils;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.nonNull;

/**
 * @author: wells
 * @date: 2020/8/12
 * @description:
 */
@Slf4j
@Service
public class FinancialDepositServiceImpl implements FinancialDepositService {
    @Autowired
    private CoinDepositService coinDepositServiceImpl;
    @Autowired
    private FinancialManagementBase financialManagementBase;
    @Autowired
    private UserService userServiceImpl;
    @Autowired
    private PayOfflineService payOfflineServiceImpl;
    @Resource
    private DictionaryBase dictionaryBase;
    @Autowired
    private NoticeBase noticeBase;

    /**
     * 充值列表
     *
     * @param reqDto
     * @return
     */
    @Override
    public ResPage<DepositListResDto> depositList(ReqPage<DepositListReqDto> reqDto) {
        /*当前登录用户*/
        BaseParams.HeaderInfo currentLoginUser = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        var params = reqDto.getData();
        var queryWrapper = getDepositListQuery(params);
        //无排序启动默认排序
        queryWrapper.orderByDesc(reqDto.getSortField().length == 0, CoinDeposit::getUpdatedAt);
        Page<CoinDeposit> depositListPage = coinDepositServiceImpl.page(reqDto.getPage(), queryWrapper);
        Page<DepositListResDto> depositListResDtoPage = BeanConvertUtils.copyPageProperties(depositListPage, DepositListResDto::new,
                (source, depositListResDto) -> {
                    //会员旗处理
                    financialManagementBase.addUserFlagProperties(source.getUid(), depositListResDto);
                    var isSelf = currentLoginUser.getId().equals(source.getAdminId()) ? 1 : 0;
                    depositListResDto.setIsSelf(isSelf);
                }
        );
        return ResPage.get(depositListResDtoPage);
    }

    /**
     * 充值列表汇总
     *
     * @param reqDto
     * @return
     */
    @Override
    public DepositSumResDto depositSum(DepositListReqDto reqDto) {
        var queryWrapper = getDepositListQuery(reqDto).select(CoinDeposit::getPayCoin);
        queryWrapper.in(CoinDeposit::getStatus, List.of(1, 2, 9));
        var list = coinDepositServiceImpl.list(queryWrapper);
        var totalCoin = list.stream().map(CoinDeposit::getPayCoin).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        return DepositSumResDto.builder().count(list.size()).totalCoin(totalCoin).build();
    }

    /**
     * 修改充值记录
     *
     * @param reqDto
     * @return
     */
    @Override
    public UpdateDepositStatusResDto updateDepositStatus(UpdateDepositStatusReqDto reqDto) {
        var now = DateNewUtils.now();
        var coinDeposit = Optional.ofNullable(coinDepositServiceImpl.getById(reqDto.getId()))
                .orElseThrow(() -> new BusinessException(CodeInfo.PARAMETERS_INVALID));
        //记录状态为充值成功，手动到账，充值失败，管理员充值不处理或者更新状态与当前记录一样不更新
        if (List.of(1, 2, 3, 9).contains(coinDeposit.getStatus()) || (reqDto.getStatus() != 8 && reqDto.getStatus().equals(coinDeposit.getStatus()))) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        /*当前登录用户*/
        BaseParams.HeaderInfo currentLoginUser = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        var updateWrapper = new LambdaUpdateWrapper<CoinDeposit>()
                .set(nonNull(currentLoginUser.getId()), CoinDeposit::getAuditUid, currentLoginUser.getId())
                .set(nonNull(currentLoginUser.getId()), CoinDeposit::getAdminId, currentLoginUser.getId())
                .set(nonNull(reqDto.getAuditMark()), CoinDeposit::getAuditMark, reqDto.getAuditMark())
                .set(nonNull(reqDto.getDepMark()), CoinDeposit::getDepMark, reqDto.getDepMark())
                .set(nonNull(reqDto.getStatus()), CoinDeposit::getStatus, reqDto.getStatus())
                .set(nonNull(reqDto.getAuditStatus()), CoinDeposit::getAuditStatus, reqDto.getAuditStatus())
                //上分时间
                .set(nonNull(reqDto.getStatus()), CoinDeposit::getDepositedAt, now)
                //审核时间
                .set(nonNull(reqDto.getAuditStatus()), CoinDeposit::getAuditedAt, now)
                .eq(CoinDeposit::getId, reqDto.getId());
        //修改上分时间
        if (reqDto.getStatus() == 1 || reqDto.getStatus() == 3) {
            updateWrapper.set(CoinDeposit::getUpdatedAt, now);
        }
        var flag =financialManagementBase.updateDepositStatus(coinDeposit,updateWrapper, reqDto, now);
        if( List.of(1, 2, 9).contains(reqDto.getStatus()) && flag){
            //推送充值成功的消息
            noticeBase.writeDepositAndWithdrawal(NoticeEnum.DN_SUCCESS,coinDeposit.getUid(), reqDto.getId(), reqDto.getPayCoin(), now);
        }
        noticeBase.writeDepositAndWithdrawalCount(Constant.PUSH_DN);
        var dicMap = dictionaryBase.getCategoryMap("dic_coin_deposit_status");
        var auditMsg = dicMap.get(String.valueOf(reqDto.getStatus()));
        return UpdateDepositStatusResDto.builder().auditMsg(auditMsg).build();

    }

    /**
     * 充值记录详情
     *
     * @param reqDto
     * @return
     */
    @Override
    public DepositDetailResDto depositDetail(DepositDetailReqDto reqDto) {
        var coinDeposit = coinDepositServiceImpl.getById(reqDto.getId());
        //查询用户名
        var user = userServiceImpl.getById(coinDeposit.getUid());
        //查询打卡账号
        var bankAccount = "---";
        if (coinDeposit.getPayType() == 0) {
            var payOff = payOfflineServiceImpl.getById(coinDeposit.getPayRefer());
            bankAccount = payOff == null ? bankAccount : payOff.getBankAccount();
        }
        DepositDetailResDto depositDetailResDto = BeanConvertUtils.copyProperties(coinDeposit, DepositDetailResDto::new);
        depositDetailResDto.setUsername(user.getUsername());
        depositDetailResDto.setBankAccount(bankAccount);
        //会员旗处理
        financialManagementBase.addUserFlagProperties(coinDeposit.getUid(), depositDetailResDto);
        return depositDetailResDto;
    }


    /**
     * 获取充值列表的查询条件
     *
     * @param params
     * @return
     */
    public LambdaQueryWrapper<CoinDeposit> getDepositListQuery(DepositListReqDto params) {
        return new QueryWrapper<CoinDeposit>().lambda()
                //用户Id
                .eq(nonNull(params.getUid()), CoinDeposit::getUid, params.getUid())
                //用户名
                .eq(nonNull(params.getUsername()), CoinDeposit::getUsername, params.getUsername())
                //订单号/ID (三方平台用)
                .and(nonNull(params.getOrderId()), x -> x.eq(CoinDeposit::getOrderId, params.getOrderId())
                        .or()
                        .eq(CoinDeposit::getId, params.getOrderId())
                )
                //类型支付类型:0-离线 1-在线
                .eq(nonNull(params.getPayType()), CoinDeposit::getPayType, params.getPayType())
                //类型类型:1-银联 2-微信 3-支付宝 4-QQ 5-QR扫码
                .eq(nonNull(params.getCategory()), CoinDeposit::getCategory, params.getCategory())
                //上分状态:0-申请中 1-手动到账 2-自动到账 3-充值失败 8-充值锁定 9-管理员充值
                .eq(nonNull(params.getStatus()), CoinDeposit::getStatus, params.getStatus())
                //汇款人姓名
                .and(nonNull(params.getAuditInfo()), x -> x.eq(CoinDeposit::getDepRealname, params.getAuditInfo())
                        .or()
                        //审核人ID
                        .eq(CoinDeposit::getAuditUid, params.getAuditInfo())
                )
                //开始时间
                .ge(nonNull(params.getStartTime()), CoinDeposit::getUpdatedAt, params.getStartTime())
                //结束时间
                .le(nonNull(params.getEndTime()), CoinDeposit::getUpdatedAt, params.getEndTime());

    }
}
