package com.xinbo.sports.plat.io.dto.base;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.math.BigDecimal;

/**
 * @author: David
 * @date: 14/05/2020
 * @description:
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CoinTransferPlatReqDto extends BasePlatReqDto {
    @ApiModelProperty(name = "username", value = "用户名", example = "test999")
    private String username;

    @ApiModelProperty(name = "coin", value = "金额", example = "100")
    private BigDecimal coin;

    @ApiModelProperty(name = "orderId", value = "订单号", example = "TRANSFER202001101")
    private String orderId;

    @ApiModelProperty(name = "IsFullAmount", value = "是否全部下分: 1-全部 0-按金额", example = "1")
    private Integer IsFullAmount;
}
