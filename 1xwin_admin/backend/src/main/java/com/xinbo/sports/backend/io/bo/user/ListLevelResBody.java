package com.xinbo.sports.backend.io.bo.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 会员等级管理-等级列表 响应body
 * </p>
 *
 * @author andy
 * @since 2020/3/12
 */
@Data
public class ListLevelResBody implements Serializable {

    @ApiModelProperty(name = "id", value = "等级Id", example = "1")
    private Integer id;

    @ApiModelProperty(name = "name", value = "等级+名称", example = "vip0 - 暂无奖牌")
    private String name;

    @ApiModelProperty(name = "scoreUpgrade", value = "升级积分", example = "100")
    private BigDecimal scoreUpgrade;

    @ApiModelProperty(name = "totalCoin", value = "账户总金额", example = "10000.00")
    private BigDecimal totalCoin;

    @ApiModelProperty(name = "totalCount", value = "会员人数", example = "19")
    private Integer totalCount;
}

