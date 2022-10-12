package com.xinbo.sports.plat.io.dto.base;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author: David
 * @date: 14/05/2020
 * @description:
 */
@Data
public class SlotGameListPlatReqDto extends BasePlatReqDto {
    @NotNull
    @ApiModelProperty(name = "category", value = "种类:0-全部游戏 1-热门游戏 2-最新游戏 3-我的收藏", example = "1")
    private Integer category;

    @ApiModelProperty(name = "name", value = "游戏名称(搜索用)", example = "经典扑克100手")
    private String name;

    @ApiModelProperty(name = "uid", value = "用户ID", example = "1")
    private Integer uid;
}
