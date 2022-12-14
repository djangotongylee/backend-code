package com.xinbo.sports.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinbo.sports.backend.base.DictionaryBase;
import com.xinbo.sports.backend.base.FinancialManagementBase;
import com.xinbo.sports.backend.io.dto.FinancialWithdrawalParameter.*;
import com.xinbo.sports.backend.service.FinancialWithdrawalService;
import com.xinbo.sports.dao.generator.po.CodeAudit;
import com.xinbo.sports.dao.generator.po.CodeRecords;
import com.xinbo.sports.dao.generator.po.CoinOnlineWithdrawal;
import com.xinbo.sports.dao.generator.po.CoinWithdrawal;
import com.xinbo.sports.dao.generator.service.*;
import com.xinbo.sports.service.base.*;
import com.xinbo.sports.service.cache.redis.UserCache;
import com.xinbo.sports.service.common.Constant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.bo.PlatServiceBaseParams;
import com.xinbo.sports.service.io.bo.UserCacheBo;
import com.xinbo.sports.service.io.dto.AuditBaseParams.AuditReqDto;
import com.xinbo.sports.service.io.dto.BaseParams;
import com.xinbo.sports.service.io.enums.NoticeEnum;
import com.xinbo.sports.service.thread.ThreadHeaderLocalData;
import com.xinbo.sports.utils.BeanConvertUtils;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;
import com.xinbo.sports.utils.components.response.CodeInfo;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.alibaba.fastjson.JSON.toJSONString;
import static java.util.Objects.nonNull;

/**
 * @author: wells
 * @date: 2020/8/13
 * @description:
 */
@Service
public class FinancialWithdrawalServiceImpl implements FinancialWithdrawalService {
    @Resource
    private CoinWithdrawalService coinWithdrawalServiceImpl;
    @Resource
    private FinancialManagementBase financialManagementBase;
    @Resource
    private CodeRecordsService codeRecordsServiceImpl;
    @Resource
    private CodeAuditService codeAuditServiceImpl;
    @Resource
    private GameStatisticsBase gameStatisticsBase;
    @Resource
    private BankListService bankListServiceImpl;
    @Resource
    private DictionaryBase dictionaryBase;
    @Resource
    private CoinOnlineWithdrawalService coinOnlineWithdrawalServiceImpl;
    @Resource
    private UserCache userCache;
    @Resource
    private NoticeBase noticeBase;
    @Resource
    private AuditBase auditBase;
    @Resource
    private UserServiceBase userServiceBase;

    /**
     * ????????????
     */
    @Override
    public ResPage<WithdrawalListResDto> withdrawalList(@NotNull ReqPage<WithdrawalListReqDto> reqDto) {
        var params = reqDto.getData();
        var queryWrapper = getWithdrawalListQuery(params);
        //???????????????????????????
        if (ArrayUtils.isEmpty(reqDto.getSortField())) {
            queryWrapper.orderByDesc(CoinWithdrawal::getUpdatedAt);
        }
        Page<CoinWithdrawal> withdrawalListPage = coinWithdrawalServiceImpl.page(reqDto.getPage(), queryWrapper);
        List<Map<String, Object>> onLineWithdrawalList = coinOnlineWithdrawalServiceImpl.listMaps(new QueryWrapper<CoinOnlineWithdrawal>()
                .select("payout_code AS payoutCode", "order_id AS orderId", "coin", "status", "withdrawal_order_id")
                .orderByDesc("created_at"));
        var unaryOperator = userServiceBase.checkShow(Constant.BANK_ACCOUNT);
        Page<WithdrawalListResDto> withdrawalListResDtoPage = BeanConvertUtils.copyPageProperties(withdrawalListPage, WithdrawalListResDto::new,
                (source, resDto) -> {
                    resDto.setMark(MarkBase.spliceMark(source.getMark()));
                    //??????????????????
                    var json = parseObject(source.getBankInfo());
                    var payOutList = onLineWithdrawalList.stream().filter(x -> source.getId()
                            .equals(Long.valueOf(x.getOrDefault("withdrawal_order_id", "0") + "")))
                            .collect(Collectors.toList());
                    resDto.setPayoutInfo(toJSONString(payOutList));
                    resDto.setAccountName(json.getString("accountName"));
                    resDto.setBankAccount(unaryOperator.apply(json.getString("bankAccount")));
                    resDto.setAccountAddress(json.getString("mark"));
                    resDto.setBankId(json.getInteger("bankId"));
                    //???????????????
                    financialManagementBase.addUserFlagProperties(source.getUid(), resDto);
                });
        return ResPage.get(withdrawalListResDtoPage);
    }


    /**
     * ??????????????????
     */
    @Override
    public WithdrawalSumResDto withdrawalSum(WithdrawalListReqDto reqDto) {
        var queryWrapper = getWithdrawalListQuery(reqDto).select(CoinWithdrawal::getCoin);
        queryWrapper.in(CoinWithdrawal::getStatus, List.of(1, 9));
        var list = coinWithdrawalServiceImpl.list(queryWrapper);
        var totalCoin = list.stream().map(CoinWithdrawal::getCoin).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        return WithdrawalSumResDto.builder().count(list.size()).totalCoin(totalCoin).build();
    }

    /**
     * ??????????????????
     */
    @Override
    public UpdateWithdrawalStatusResDto updateWithdrawalStatus(UpdateWithdrawalStatusReqDto reqDto) {
        BaseParams.HeaderInfo currentLoginUser = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        var updateWrapper = new LambdaUpdateWrapper<CoinWithdrawal>()
                .set(nonNull(currentLoginUser.getId()), CoinWithdrawal::getAdminUid, currentLoginUser.getId())
                .set(nonNull(reqDto.getMark()), CoinWithdrawal::getMark, reqDto.getMark())
                .set(nonNull(reqDto.getStatus()), CoinWithdrawal::getStatus, reqDto.getStatus())
                .set(CoinWithdrawal::getUpdatedAt, DateNewUtils.now())
                .eq(CoinWithdrawal::getId, reqDto.getId());
        var withdrawal = Optional.ofNullable(coinWithdrawalServiceImpl.getById(reqDto.getId()))
                .orElseThrow(() -> new BusinessException(CodeInfo.PARAMETERS_INVALID));

        var flag = financialManagementBase.updateWithdrawalStatus(withdrawal, updateWrapper, reqDto);
        if (WithdrawalStatus.WITHDRAWAL_SUCCESS.getCode().equals(reqDto.getStatus())
                || WithdrawalStatus.SYSTEM_WITHDRAWAL.getCode().equals(reqDto.getStatus())
                && flag) {
            //???????????????????????????
            noticeBase.writeDepositAndWithdrawal(NoticeEnum.WN_SUCCESS, withdrawal.getUid(), reqDto.getId(), withdrawal.getCoin(), DateNewUtils.now());
        }
        noticeBase.writeDepositAndWithdrawalCount(Constant.PUSH_WN);
        var dicMap = dictionaryBase.getCategoryMap("dic_coin_withdrawal_status");
        var auditMsg = dicMap.get(String.valueOf(reqDto.getStatus()));
        return UpdateWithdrawalStatusResDto.builder().auditMsg(auditMsg).build();
    }

    /**
     * ??????????????????
     *
     * @param reqDto ???????????????
     * @return ?????????????????????
     */
    @Override
    public WithdrawalDetailResDto withdrawalDetail(WithdrawalDetailReqDto reqDto) {
        var coinWithdrawal = coinWithdrawalServiceImpl.getById(reqDto.getId());
        WithdrawalDetailResDto withdrawalDetailResDto = BeanConvertUtils.copyProperties(coinWithdrawal, WithdrawalDetailResDto::new);
        var json = parseObject(coinWithdrawal.getBankInfo());
        //??????????????????
        var bankList = bankListServiceImpl.getById(json.getInteger("bankId"));
        withdrawalDetailResDto.setAccountName(json.getString("accountName"));
        withdrawalDetailResDto.setBankAccount(userServiceBase.checkShow(Constant.BANK_ACCOUNT).apply(json.getString("bankAccount")));
        withdrawalDetailResDto.setBankName(bankList.getName());
        withdrawalDetailResDto.setBankBranchName(json.getString("mark"));
        //???????????????
        financialManagementBase.addUserFlagProperties(coinWithdrawal.getUid(), withdrawalDetailResDto);
        return withdrawalDetailResDto;
    }

    /**
     * ??????
     * 1.???????????????(????????????:????????????????????????????????????????????????????????????)
     * 2.???????????????????????????
     * 3????????????????????????????????????
     * 4??????????????????<???????????????,??????????????????????????????
     * 5.??????????????????=???????????????,??????????????????????????????
     */
    @Override
    public UpdateWithdrawalStatusResDto isAudit(AuditReqDto reqDto) {
        var count = codeAuditServiceImpl.lambdaQuery().eq(CodeAudit::getReferId, reqDto.getId()).count();
        if (count > 0) {
            throw new BusinessException(CodeInfo.WITHDRAWAL_ALREADY_AUDIT);
        }
        //?????????????????????????????????????????????
        var pair = auditBase.getCodeRecords(reqDto, true);
        //??????????????????????????????
        var user = userCache.getUserInfoById(reqDto.getUid());
        var codeReal = pair.getLeft();
        var codeRecodesList = pair.getRight();
        var mark = "";
        //????????????????????????????????????
        if (!CollectionUtils.isEmpty(codeRecodesList)) {
            var lastCodeRecode = codeRecodesList.get(0);
            //??????????????????????????????????????????
            var patGameQueryDateDto = PlatServiceBaseParams.PlatGameQueryDateDto.builder()
                    .startTime(lastCodeRecode.getCreatedAt())
                    .endTime(DateNewUtils.now())
                    .uid(reqDto.getUid())
                    .build();
            var lastRelCode = gameStatisticsBase.getCoinStatisticsByDate(patGameQueryDateDto).getCoinBet();
            var codeRequire = codeRecodesList.stream().map(CodeRecords::getCodeRequire)
                    .reduce(BigDecimal::add)
                    .orElse(BigDecimal.ZERO);
            //??????????????????=???????????????,?????????????????????????????? &&   ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            if (codeReal.compareTo(codeRequire.add(user.getExtraCode())) >= 0 && lastRelCode.compareTo(lastCodeRecode.getCodeRequire()) >= 0) {
                financialManagementBase.auditPersistence(reqDto, codeReal, codeRequire, 1, codeRecodesList, "");
                return getUpdateWithdrawal("3");
            }
            var subCoin = codeRequire.add(user.getExtraCode()).subtract(codeReal).setScale(2, RoundingMode.DOWN);
            var subLastCoin = lastCodeRecode.getCodeRequire().subtract(lastRelCode).setScale(2, RoundingMode.DOWN);
            mark = String.valueOf(subCoin.compareTo(subLastCoin) > 0 ? subCoin : subLastCoin);
            financialManagementBase.auditPersistence(reqDto, codeReal, codeRequire, 2, codeRecodesList, mark);
        } else {
            if (codeReal.compareTo(user.getExtraCode()) >= 0) {
                //????????????????????????
                financialManagementBase.auditPersistence(reqDto, codeReal, BigDecimal.ZERO, 1, codeRecodesList, "");
                return getUpdateWithdrawal("3");
            } else {
                mark = String.valueOf(user.getExtraCode().setScale(2, RoundingMode.DOWN));
                financialManagementBase.auditPersistence(reqDto, codeReal, user.getExtraCode(), 2, codeRecodesList, mark);
            }
        }
        return getUpdateWithdrawal("4");
    }

    /**
     * ??????????????????
     *
     * @param auditStatus ????????????
     * @return ????????????
     */
    public UpdateWithdrawalStatusResDto getUpdateWithdrawal(String auditStatus) {
        var dicMap = dictionaryBase.getCategoryMap("dic_coin_withdrawal_status");
        var auditMsg = dicMap.get(auditStatus);
        return UpdateWithdrawalStatusResDto.builder().auditMsg(auditMsg).build();
    }

    /**
     * ????????????
     */
    @Override
    public AuditDetailResDto auditDetail(ReqPage<AuditReqDto> pageReqDto) {
        var reqDto = pageReqDto.getData();
        //???????????????????????????????????????
        var pair = auditBase.getCodeRecords(reqDto, false);
        var codeReal = pair.getLeft();
        var codeRecodesList = pair.getRight();
        var codeRequire = codeRecodesList.stream().map(CodeRecords::getCodeRequire)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
        //???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        var lastRelCode = BigDecimal.ZERO;
        var lastCodeRequire = BigDecimal.ZERO;
        if (!CollectionUtils.isEmpty(codeRecodesList)) {
            var timePair = auditBase.getAuditStartTimeAndEndTime(reqDto);
            var lastCodeRecode = codeRecodesList.get(0);
            lastCodeRequire = lastCodeRecode.getCodeRequire();
            //??????????????????????????????????????????
            var patGameQueryDateDto = PlatServiceBaseParams.PlatGameQueryDateDto.builder()
                    .startTime(lastCodeRecode.getCreatedAt())
                    .endTime(timePair.getRight())
                    .uid(reqDto.getUid())
                    .build();
            lastRelCode = gameStatisticsBase.getCoinStatisticsByDate(patGameQueryDateDto).getCoinBet();
        }
        //?????????1??????????????????2???????????????
        var realStatus = codeReal.compareTo(codeRequire) >= 0 && lastRelCode.compareTo(lastCodeRequire) >= 0 ? 1 : 2;
        var audit = codeAuditServiceImpl.getOne(new LambdaQueryWrapper<CodeAudit>()
                        .eq(CodeAudit::getReferId, reqDto.getId())
                , false);
        var auditStatus = Optional.ofNullable(audit).map(CodeAudit::getStatus).orElse(2);
        var timePair = auditBase.getAuditStartTimeAndEndTime(reqDto);
        var queryWrapper = new LambdaQueryWrapper<CodeRecords>()
                .eq(CodeRecords::getUid, reqDto.getUid())
                .ge(CodeRecords::getCreatedAt, timePair.getLeft())
                .le(CodeRecords::getCreatedAt, timePair.getRight());
        var page = codeRecordsServiceImpl.page(pageReqDto.getPage(), queryWrapper);
        var resPage = BeanConvertUtils.copyPageProperties(page, CodeRecordsResDto::new,
                (source, codeRecodes) ->
                        //???????????????
                        financialManagementBase.addUserFlagProperties(source.getUid(), codeRecodes)
        );
        var extraCode = Optional.ofNullable(userCache.getUserInfoById(reqDto.getUid()))
                .map(UserCacheBo.UserCacheInfo::getExtraCode)
                .orElse(BigDecimal.ZERO);
        return AuditDetailResDto.builder()
                .id(reqDto.getId())
                .codeRequire(codeRequire)
                .codeReal(codeReal)
                .lastCodeReal(lastRelCode)
                .realStatus(realStatus)
                .extraCode(extraCode)
                .auditStatus(auditStatus)
                .codeRecordsResDtoList(ResPage.get(resPage))
                .build();
    }


    /**
     * ?????????????????????????????????
     */
    public LambdaQueryWrapper<CoinWithdrawal> getWithdrawalListQuery(WithdrawalListReqDto params) {
        return new LambdaQueryWrapper<CoinWithdrawal>()
                //??????Id
                .eq(nonNull(params.getUid()), CoinWithdrawal::getUid, params.getUid())
                //?????????
                .eq(nonNull(params.getUsername()), CoinWithdrawal::getUsername, params.getUsername())
                //?????????
                .eq(nonNull(params.getId()), CoinWithdrawal::getId, params.getId())
                //????????????
                .and(nonNull(params.getBankInfo()), x -> x.apply("JSON_EXTRACT(bank_info , '$.accountName') ='" + params.getBankInfo() + "'")
                        .or()
                        .apply("JSON_EXTRACT(bank_info , '$.bankAccount')  ='" + params.getBankInfo() + "'")
                )
                //????????????:0-????????? 1-???????????? 2-???????????? 3-???????????? 8-???????????? 9-???????????????
                .eq(nonNull(params.getStatus()), CoinWithdrawal::getStatus, params.getStatus())
                //?????????UID
                .eq(nonNull(params.getAdminUid()), CoinWithdrawal::getAdminUid, params.getAdminUid())
                //????????????
                .ge(nonNull(params.getStartTime()), CoinWithdrawal::getUpdatedAt, params.getStartTime())
                //????????????
                .le(nonNull(params.getEndTime()), CoinWithdrawal::getUpdatedAt, params.getEndTime());
    }

}
