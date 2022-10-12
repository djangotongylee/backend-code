package com.xinbo.sports.service.base;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinbo.sports.dao.generator.po.CodeAudit;
import com.xinbo.sports.dao.generator.po.CodeRecords;
import com.xinbo.sports.dao.generator.po.CoinWithdrawal;
import com.xinbo.sports.dao.generator.service.CodeAuditService;
import com.xinbo.sports.dao.generator.service.CodeRecordsService;
import com.xinbo.sports.dao.generator.service.CoinWithdrawalService;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.service.cache.redis.UserCache;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.bo.PlatServiceBaseParams;
import com.xinbo.sports.service.io.dto.AuditBaseParams.AuditReqDto;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

import static com.alibaba.fastjson.JSON.parseObject;

/**
 * @Author : Wells
 * @Date : 2020-12-15 12:49 上午
 * @Description : 打码量稽核
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuditBase {
    private final GameStatisticsBase gameStatisticsBase;
    private final CodeRecordsService codeRecordsServiceImpl;
    private final CodeAuditService codeAuditServiceImpl;
    private final UserCache userCache;
    private final ConfigCache configCache;
    private final CoinWithdrawalService coinWithdrawalServiceImpl;

    /**
     * 提款时验证用户打码量
     *
     * @param uid 用户id
     */
    public void checkWithdrawal(Integer uid) {
        var minDrawCoinJson = parseObject(configCache.getConfigByTitle("MinDrawCoin"));
        var autoAudit = minDrawCoinJson.getInteger("autoAudit");
        //平台配置是否验证提款打码量
        if (Objects.isNull(autoAudit) || autoAudit != 1) {
            return;
        }
        var mark = withdrawalHint(uid);
        if (StringUtils.isNotEmpty(mark)) {
            throwException(mark);
        }
    }

    /**
     * 提款时抛出提示信息
     *
     * @param mark 提示信息
     */
    private void throwException(String mark) {
        var msg = MarkBase.spliceMark(mark);
        var codeInfo = CodeInfo.API_WITHDRAWAL_CHECK_INFO;
        codeInfo.setMsg(msg);
        throw new BusinessException(codeInfo);

    }

    /**
     * 提款提示
     *
     * @param uid 用户ID
     * @return String
     */
    public String withdrawalHint(Integer uid) {
        var mark = "";
        var reqDto = new AuditReqDto();
        reqDto.setUid(uid);
        //有效投注额及未结算的打码量记录
        var pair = getCodeRecords(reqDto, true);
        //获取用户的额外打码量
        var user = userCache.getUserInfoById(reqDto.getUid());
        var codeReal = pair.getLeft();
        var codeRecodesList = pair.getRight();

        //无打码量记录，则稽核通过
        if (!CollectionUtils.isEmpty(codeRecodesList)) {
            var lastCodeRecode = codeRecodesList.get(0);
            //查询最后时间段的有效投注金额
            var patGameQueryDateDto = PlatServiceBaseParams.PlatGameQueryDateDto.builder()
                    .startTime(lastCodeRecode.getCreatedAt())
                    .endTime(DateNewUtils.now())
                    .uid(reqDto.getUid())
                    .build();
            var lastRelCode = gameStatisticsBase.getCoinStatisticsByDate(patGameQueryDateDto).getCoinBet();
            var codeRequire = codeRecodesList.stream().map(CodeRecords::getCodeRequire)
                    .reduce(BigDecimal::add)
                    .orElse(BigDecimal.ZERO);
            //有效投注额》=所需打码量,则成功，记录稽核记录 &&   最后一条打码量记录为开始时间至当前提现时间为结束时间，时间段产生的流水大于等于最后记录产生的打码量
            if (codeReal.compareTo(codeRequire.add(user.getExtraCode())) < 0 || lastRelCode.compareTo(lastCodeRecode.getCodeRequire()) < 0) {
                var subCoin = codeRequire.add(user.getExtraCode()).subtract(codeReal).setScale(2, RoundingMode.DOWN);
                var subLastCoin = lastCodeRecode.getCodeRequire().subtract(lastRelCode).setScale(2, RoundingMode.DOWN);
                mark = String.valueOf(subCoin.compareTo(subLastCoin) > 0 ? subCoin : subLastCoin);
            }
        } else {
            if (codeReal.compareTo(user.getExtraCode()) < 0) {
                mark = String.valueOf(user.getExtraCode().subtract(codeReal).setScale(2, RoundingMode.DOWN));
            }
        }
        return mark;
    }


    /**
     * 获取用户未稽核的打码量记录及真实打码量
     */
    public Pair<BigDecimal, List<CodeRecords>> getCodeRecords(AuditReqDto reqDto, boolean isSettlement) {
        var uid = reqDto.getUid();
        var timePair = getAuditStartTimeAndEndTime(reqDto);
        return getCodeRecords(uid, timePair.getLeft(), timePair.getRight(), isSettlement);
    }

    /**
     * 获取用户未稽核的打码量记录及真实打码量
     *
     * @param uid          用户ID
     * @param startTime    开始时间
     * @param endTime      结束时间
     * @param isSettlement 未结算标识
     * @return 二元组
     */
    public Pair<BigDecimal, List<CodeRecords>> getCodeRecords(Integer uid, Integer startTime, Integer endTime, boolean isSettlement) {
        //查询有效投注金额
        var patGameQueryDateDto = PlatServiceBaseParams.PlatGameQueryDateDto.builder()
                .startTime(startTime)
                .endTime(endTime)
                .uid(uid)
                .build();
        var codeReal = gameStatisticsBase.getCoinStatisticsByDate(patGameQueryDateDto).getCoinBet();
        //查询打码量记录
        var codeRecordsList = codeRecordsServiceImpl.lambdaQuery()
                .eq(CodeRecords::getUid, uid)
                .ge(CodeRecords::getCreatedAt, startTime)
                .le(CodeRecords::getCreatedAt, endTime)
                //状态:0-未结算 1-结算
                .eq(isSettlement, CodeRecords::getStatus, 0)
                .orderByDesc(CodeRecords::getCreatedAt)
                .list();
        return Pair.of(codeReal, codeRecordsList);
    }

    /**
     * 获取查询打码量的开始时间与结束时间
     *
     * @param reqDto 稽核参数实体类
     * @return 开始时间与结束时间的二元组
     */
    public Pair<Integer, Integer> getAuditStartTimeAndEndTime(AuditReqDto reqDto) {
        //计算结束时间
        var lastCodeAudit = codeAuditServiceImpl.getOne(new LambdaQueryWrapper<CodeAudit>()
                        .eq(CodeAudit::getReferId, reqDto.getId())
                , false);
        var endTime = Objects.nonNull(lastCodeAudit) ? lastCodeAudit.getCreatedAt() : DateNewUtils.now();
        //计算开始时间
        var startCodeAudit = codeAuditServiceImpl.getOne(new LambdaQueryWrapper<CodeAudit>()
                .eq(CodeAudit::getUid, reqDto.getUid())
                .lt(CodeAudit::getCreatedAt, endTime)
                //状态：1：稽核成功，2：稽核失败
                .eq(CodeAudit::getStatus, 1)
                .orderByDesc(CodeAudit::getCreatedAt), false);
        var startTime = Objects.nonNull(startCodeAudit) ? startCodeAudit.getCreatedAt() : 0;
        return Pair.of(startTime, endTime);
    }


    /**
     * 可提款次数,可提款额度
     *
     * @param uid     用户ID
     * @param levelId 等级ID
     * @return BigDecimal
     */
    public Pair<Integer, BigDecimal> getWithdrawalInfo(Integer uid, Integer levelId) {
        var userLevel = userCache.getUserLevelById(levelId);
        // 提款次数(每日)
        var withdrawalNumMax = userLevel.getWithdrawalNums();
        // 提款限额(万/日)
        var withdrawalTotalCoinMax = null != userLevel.getWithdrawalTotalCoin() ? new BigDecimal(userLevel.getWithdrawalTotalCoin()) : BigDecimal.ZERO;
        withdrawalTotalCoinMax = withdrawalTotalCoinMax.compareTo(BigDecimal.ZERO) > 0 ? withdrawalTotalCoinMax.multiply(new BigDecimal(10000)) : withdrawalTotalCoinMax;

        var end = ZonedDateTime.now(ZoneId.systemDefault());
        var start = end.with(LocalTime.MIN);
        var list = coinWithdrawalServiceImpl.lambdaQuery()
                .eq(CoinWithdrawal::getUid, uid)
                .eq(CoinWithdrawal::getStatus, 1)
                .ge(CoinWithdrawal::getUpdatedAt, start.toEpochSecond())
                .le(CoinWithdrawal::getUpdatedAt, end.toEpochSecond())
                .list();
        var withdrawalTotalCoin = list.stream().map(CoinWithdrawal::getCoin).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        var availableCount = withdrawalNumMax - list.size();
        return Pair.of(availableCount, withdrawalTotalCoinMax.subtract(withdrawalTotalCoin));
    }

}
