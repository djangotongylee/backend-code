package com.xinbo.sports.plat.base;

import com.xinbo.sports.dao.generator.po.BetSlipsException;
import com.xinbo.sports.dao.generator.po.BetSlipsSupplemental;
import com.xinbo.sports.dao.generator.service.BetSlipsExceptionService;
import com.xinbo.sports.dao.generator.service.BetSlipsSupplementalService;
import com.xinbo.sports.utils.DateNewUtils;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * @description: Plat持久化公共类
 * @author: andy
 * @date: 2020/8/19
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CommonPersistence {
    private final BetSlipsExceptionService betSlipsExceptionServiceImpl;
    private final BetSlipsSupplementalService betSlipsSupplementalServiceImpl;

    /**
     * 添加拉单异常表
     *
     * @param gameListId    sp_game_list表主键ID
     * @param requestParams 请求参数
     * @param category      状态:0-三方异常 1-数据异常
     * @param exceptionInfo 异常信息:三方-返回数据 数据-异常处理
     */
    public void addBetSlipsException(Integer gameListId, String requestParams, Integer category, String exceptionInfo, Integer status) {
        //异常信息大于1000字符取1000；
        exceptionInfo = Strings.isNotEmpty(exceptionInfo) && exceptionInfo.length() > 1000 ? exceptionInfo.substring(0, 1000) : exceptionInfo;
        int now = DateNewUtils.now();
        BetSlipsException betSlipsException = new BetSlipsException();
        betSlipsException.setGameListId(gameListId);
        betSlipsException.setRequest(requestParams);
        betSlipsException.setCategory(category);
        betSlipsException.setInfo(exceptionInfo);
        betSlipsException.setStatus(status);
        betSlipsException.setCreatedAt(now);
        betSlipsException.setUpdatedAt(now);
        betSlipsExceptionServiceImpl.save(betSlipsException);
    }

    /**
     * 添加注单补单记录
     *
     * @param gameListId    sp_game_list表主键ID
     * @param requestParams 请求参数
     * @param category      状态:0-三方异常 1-数据异常
     * @param exceptionInfo 异常信息:三方-返回数据 数据-异常处理
     * @param timeStart     开始时间
     * @param timeEnd       结束时间
     */
    public void addBetSlipsSupplemental(Integer gameListId, String requestParams, Integer category, String exceptionInfo, String timeStart, String timeEnd) {
        int now = DateNewUtils.now();
        BetSlipsSupplemental po = new BetSlipsSupplemental();
        po.setGameListId(gameListId);
        po.setRequest(requestParams);
        po.setCategory(category);
        po.setInfo(exceptionInfo);
        po.setTimeStart(timeStart);
        po.setTimeEnd(timeEnd);
        po.setCreatedAt(now);
        po.setUpdatedAt(now);
        betSlipsSupplementalServiceImpl.save(po);
    }

    /**
     * 批量保存注单补单记录
     *
     * @param entityList 注单补单记录集合
     */
    public void addBatchBetSlipsSupplementalList(Collection<BetSlipsSupplemental> entityList) {
        betSlipsSupplementalServiceImpl.saveBatch(entityList);
    }

    /**
     * 批量保存拉单异常表记录
     *
     * @param entityList 拉单异常表记录集合
     */
    public void addBatchBetSlipsExceptionList(Collection<BetSlipsException> entityList) {
        betSlipsExceptionServiceImpl.saveBatch(entityList);
    }
}
