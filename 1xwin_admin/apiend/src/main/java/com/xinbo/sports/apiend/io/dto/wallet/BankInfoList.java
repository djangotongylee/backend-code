package com.xinbo.sports.apiend.io.dto.wallet;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 银行列表
 * </p>
 *
 * @author andy
 * @since 2020/4/8
 */
@Data
public class BankInfoList {

    @ApiModelProperty(name = "id", value = "银行Id", example = "1")
    private Integer id;

    @ApiModelProperty(name = "code", value = "银行编号", example = "HSBC")
    private String code;

    @ApiModelProperty(name = "name", value = "汇丰银行", example = "汇丰银行")
    private String name;

    @ApiModelProperty(name = "icon", value = "Logo", example = "/icon/bank/hsbc.png")
    private String icon;

    @ApiModelProperty(name = "value", value = "控件id", example = "1")
    private Integer value;

    @ApiModelProperty(name = "label", value = "控件值", example = "汇丰银行")
    private String label;

}
