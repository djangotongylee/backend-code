package com.xinbo.sports.apiend.service.impl;

import com.xinbo.sports.apiend.io.dto.mapper.UserInfoResDto;
import com.xinbo.sports.apiend.io.dto.platform.CoinDownBatch.CoinDownBatchPlatInfo;
import com.xinbo.sports.apiend.io.dto.platform.CoinDownBatch.CoinDownBatchReqBody;
import com.xinbo.sports.apiend.io.dto.platform.CoinDownBatch.CoinDownBatchResBody;
import com.xinbo.sports.apiend.io.dto.platform.CoinTransferReqDto;
import com.xinbo.sports.apiend.io.dto.platform.CoinTransferResDto;
import com.xinbo.sports.apiend.mapper.UserInfoMapper;
import com.xinbo.sports.apiend.service.IGameService;
import com.xinbo.sports.dao.generator.po.User;
import com.xinbo.sports.dao.generator.service.UserService;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.dto.BaseParams;
import com.xinbo.sports.service.io.dto.UserInfo;
import com.xinbo.sports.service.thread.ThreadHeaderLocalData;
import com.xinbo.sports.utils.BeanConvertUtils;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * 三方游戏平台->批量下分
 * </p>
 *
 * @author andy
 * @since 2020/12/14
 */
@Service
@Slf4j
public class GameBatchServiceImpl {
    private static final AtomicInteger REPORT_THREAD_POOL_ID = new AtomicInteger();
    @Resource
    private IGameService gameServiceImpl;
    @Resource
    private UserService userServiceImpl;
    @Resource
    private UserInfoMapper userInfoMapper;

    private ThreadPoolExecutor getPool() {
        return new ThreadPoolExecutor(
                32,
                32,
                2L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                x -> new Thread(x, "批量下分_POOL_" + REPORT_THREAD_POOL_ID.getAndIncrement()));
    }

    /**
     * 三方游戏平台->批量下分
     *
     * @param reqBodyList 请求列表
     * @return
     */
    public CoinDownBatchResBody coinDownBatch(List<CoinDownBatchReqBody> reqBodyList, String username) {
        if (Optional.ofNullable(reqBodyList).isEmpty() || reqBodyList.isEmpty()) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        // 当前登录用户信息
        BaseParams.HeaderInfo headerInfo = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        UserInfoResDto userInfoResDto = userInfoMapper.getUserInfoById(headerInfo.getId());
        UserInfo userInfo = BeanConvertUtils.beanCopy(userInfoResDto, UserInfo::new);


        var pool = getPool();
        try {
            List<CoinDownBatchPlatInfo> list = new ArrayList<>();
            for (CoinDownBatchReqBody reqBody : reqBodyList) {
                var platInfoDefault = CoinDownBatchPlatInfo.builder().id(reqBody.getId()).coin(BigDecimal.ZERO).code(0).msg("").build();
                try {
                    // 请求参数
                    CoinTransferReqDto build = CoinTransferReqDto.builder()
                            .id(reqBody.getId())
                            .coin(reqBody.getCoin())
                            .name(headerInfo.getUsername())
                            // 类型:0-上分 1-下分
                            .direction(1)
                            .build();

                    // 并发请求
                    Future<CoinTransferResDto> submit = pool.submit(() -> gameServiceImpl.coinTransfer(build, userInfo, headerInfo));
                    var coinTransferResDto = submit.get(3, TimeUnit.SECONDS);
                    // 平台余额
                    platInfoDefault.setCoin(coinTransferResDto.getPlatCoin());
                } catch (BusinessException e) {
                    // 异常信息
                    platInfoDefault.setCode(e.getCode());
                    platInfoDefault.setMsg(e.getMessage());
                } catch (Exception e) {
                    // 异常信息
                    platInfoDefault.setCode(CodeInfo.RES_CODE_PROCESSING.getCode());
                    platInfoDefault.setMsg(CodeInfo.RES_CODE_PROCESSING.getMsg());
                }
                // 平台列表
                list.add(platInfoDefault);
            }

            // 查询用户余额
            User user = userServiceImpl.getById(headerInfo.getId());
            return CoinDownBatchResBody.builder().coin(user.getCoin()).list(list).build();
        } finally {
            pool.shutdown();
        }
    }
}
