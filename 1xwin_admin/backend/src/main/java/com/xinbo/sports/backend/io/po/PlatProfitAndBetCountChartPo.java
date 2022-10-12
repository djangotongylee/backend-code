package com.xinbo.sports.backend.io.po;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @description:综合走势图-游戏盈亏与投注注数PO
 * @author: andy
 * @date: 2020/8/10
 */
@Data
public class PlatProfitAndBetCountChartPo {
    @ApiModelProperty(name = "name", value = "月份")
    private String name;
    @ApiModelProperty(name = "count", value = "数量")
    private Integer count;
    @ApiModelProperty(name = "count", value = "金额")
    private BigDecimal coin;
}
