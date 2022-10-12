package com.xinbo.sports.plat.io.dto.base;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author: David
 * @date: 14/05/2020
 * @description:
 */
@Data
public class SlotGameListPlatResDto {
    @ApiModelProperty(name = "id", value = "游戏ID", example = "1")
    private String id;

    @ApiModelProperty(name = "name", value = "游戏名称", example = "倫敦獵人")
    private String name;

    @ApiModelProperty(name = "img", value = "游戏名称", example = "倫敦獵人")
    private String img;

    @ApiModelProperty(name = "isNew", value = "游戏名称", example = "1")
    private Integer isNew;

    @ApiModelProperty(name = "favoriteStar", value = "收藏值", example = "999")
    private Integer favoriteStar;

    @ApiModelProperty(name = "hotStar", value = "热度值", example = "900")
    private Integer hotStar;

    @ApiModelProperty(name = "isFavorite", value = "是否收藏:1-已收藏 0-未收藏", example = "1")
    private Integer isFavorite;
}
