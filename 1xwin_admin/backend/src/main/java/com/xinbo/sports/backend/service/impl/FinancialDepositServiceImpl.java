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
     * ????????????
     *
     * @param reqDto
     * @return
     */
    @Override
    public ResPage<DepositListResDto> depositList(ReqPage<DepositListReqDto> reqDto) {
        /*??????????????????*/
        BaseParams.HeaderInfo currentLoginUser = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        var params = reqDto.getData();
        var queryWrapper = getDepositListQuery(params);
        //???????????????????????????
        queryWrapper.orderByDesc(reqDto.getSortField().length == 0, CoinDeposit::getUpdatedAt);
        Page<CoinDeposit> depositListPage = coinDepositServiceImpl.page(reqDto.getPage(), queryWrapper);
        Page<DepositListResDto> depositListResDtoPage = BeanConvertUtils.copyPageProperties(depositListPage, DepositListResDto::new,
                (source, depositListResDto) -> {
                    //???????????????
                    financialManagementBase.addUserFlagProperties(source.getUid(), depositListResDto);
                    var isSelf = currentLoginUser.getId().equals(source.getAdminId()) ? 1 : 0;
                    depositListResDto.setIsSelf(isSelf);
                }
        );
        return ResPage.get(depositListResDtoPage);
    }

    /**
     * ??????????????????
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
     * ??????????????????
     *
     * @param reqDto
     * @return
     */
    @Override
    public UpdateDepositStatusResDto updateDepositStatus(UpdateDepositStatusReqDto reqDto) {
        var now = DateNewUtils.now();
        var coinDeposit = Optional.ofNullable(coinDepositServiceImpl.getById(reqDto.getId()))
                .orElseThrow(() -> new BusinessException(CodeInfo.PARAMETERS_INVALID));
        //????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        if (List.of(1, 2, 3, 9).contains(coinDeposit.getStatus()) || (reqDto.getStatus() != 8 && reqDto.getStatus().equals(coinDeposit.getStatus()))) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        /*??????????????????*/
        BaseParams.HeaderInfo currentLoginUser = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        var updateWrapper = new LambdaUpdateWrapper<CoinDeposit>()
                .set(nonNull(currentLoginUser.getId()), CoinDeposit::getAuditUid, currentLoginUser.getId())
                .set(nonNull(currentLoginUser.getId()), CoinDeposit::getAdminId, currentLoginUser.getId())
                .set(nonNull(reqDto.getAuditMark()), CoinDeposit::getAuditMark, reqDto.getAuditMark())
                .set(nonNull(reqDto.getDepMark()), CoinDeposit::getDepMark, reqDto.getDepMark())
                .set(nonNull(reqDto.getStatus()), CoinDeposit::getStatus, reqDto.getStatus())
                .set(nonNull(reqDto.getAuditStatus()), CoinDeposit::getAuditStatus, reqDto.getAuditStatus())
                //????????????
                .set(nonNull(reqDto.getStatus()), CoinDeposit::getDepositedAt, now)
                //????????????
                .set(nonNull(reqDto.getAuditStatus()), CoinDeposit::getAuditedAt, now)
                .eq(CoinDeposit::getId, reqDto.getId());
        //??????????????????
        if (reqDto.getStatus() == 1 || reqDto.getStatus() == 3) {
            updateWrapper.set(CoinDeposit::getUpdatedAt, now);
        }
        var flag =financialManagementBase.updateDepositStatus(coinDeposit,updateWrapper, reqDto, now);
        if( List.of(1, 2, 9).contains(reqDto.getStatus()) && flag){
            //???????????????????????????
            noticeBase.writeDepositAndWithdrawal(NoticeEnum.DN_SUCCESS,coinDeposit.getUid(), reqDto.getId(), reqDto.getPayCoin(), now);
        }
        noticeBase.writeDepositAndWithdrawalCount(Constant.PUSH_DN);
        var dicMap = dictionaryBase.getCategoryMap("dic_coin_deposit_status");
        var auditMsg = dicMap.get(String.valueOf(reqDto.getStatus()));
        return UpdateDepositStatusResDto.builder().auditMsg(auditMsg).build();

    }

    /**
     * ??????????????????
     *
     * @param reqDto
     * @return
     */
    @Override
    public DepositDetailResDto depositDetail(DepositDetailReqDto reqDto) {
        var coinDeposit = coinDepositServiceImpl.getById(reqDto.getId());
        //???????????????
        var user = userServiceImpl.getById(coinDeposit.getUid());
        //??????????????????
        var bankAccount = "---";
        if (coinDeposit.getPayType() == 0) {
            var payOff = payOfflineServiceImpl.getById(coinDeposit.getPayRefer());
            bankAccount = payOff == null ? bankAccount : payOff.getBankAccount();
        }
        DepositDetailResDto depositDetailResDto = BeanConvertUtils.copyProperties(coinDeposit, DepositDetailResDto::new);
        depositDetailResDto.setUsername(user.getUsername());
        depositDetailResDto.setBankAccount(bankAccount);
        //???????????????
        financialManagementBase.addUserFlagProperties(coinDeposit.getUid(), depositDetailResDto);
        return depositDetailResDto;
    }


    /**
     * ?????????????????????????????????
     *
     * @param params
     * @return
     */
    public LambdaQueryWrapper<CoinDeposit> getDepositListQuery(DepositListReqDto params) {
        return new QueryWrapper<CoinDeposit>().lambda()
                //??????Id
                .eq(nonNull(params.getUid()), CoinDeposit::getUid, params.getUid())
                //?????????
                .eq(nonNull(params.getUsername()), CoinDeposit::getUsername, params.getUsername())
                //?????????/ID (???????????????)
                .and(nonNull(params.getOrderId()), x -> x.eq(CoinDeposit::getOrderId, params.getOrderId())
                        .or()
                        .eq(CoinDeposit::getId, params.getOrderId())
                )
                //??????????????????:0-?????? 1-??????
                .eq(nonNull(params.getPayType()), CoinDeposit::getPayType, params.getPayType())
                //????????????:1-?????? 2-?????? 3-????????? 4-QQ 5-QR??????
                .eq(nonNull(params.getCategory()), CoinDeposit::getCategory, params.getCategory())
                //????????????:0-????????? 1-???????????? 2-???????????? 3-???????????? 8-???????????? 9-???????????????
                .eq(nonNull(params.getStatus()), CoinDeposit::getStatus, params.getStatus())
                //???????????????
                .and(nonNull(params.getAuditInfo()), x -> x.eq(CoinDeposit::getDepRealname, params.getAuditInfo())
                        .or()
                        //?????????ID
                        .eq(CoinDeposit::getAuditUid, params.getAuditInfo())
                )
                //????????????
                .ge(nonNull(params.getStartTime()), CoinDeposit::getUpdatedAt, params.getStartTime())
                //????????????
                .le(nonNull(params.getEndTime()), CoinDeposit::getUpdatedAt, params.getEndTime());

    }
}
