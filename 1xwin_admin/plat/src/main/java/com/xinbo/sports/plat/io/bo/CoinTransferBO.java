package com.xinbo.sports.plat.io.bo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author: David
 * @date: 02/05/2020
 * @description: 三方平台上下、下分结果集
 */
@Data
@Builder
public class CoinTransferBO {
    /**
     * 交易金额
     */
    private BigDecimal coin;

    /**
     * 平台余额
     */
    private BigDecimal platCoin;

    /**
     * 交易订单
     */
    private String orderPlat;
}
