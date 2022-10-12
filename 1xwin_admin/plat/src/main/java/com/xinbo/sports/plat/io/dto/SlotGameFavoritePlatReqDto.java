package com.xinbo.sports.plat.io.dto;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author: David
 * @date: 14/05/2020
 * @description:
 */
@Data
public class SlotGameFavoritePlatReqDto {
    @NotNull
    @ApiModelProperty(name = "uid", value = "UID", example = "1")
    private Integer uid;

    @NotNull
    @ApiModelProperty(name = "gameId", value = "游戏ID", example = "1")
    private Integer gameId;

    @ApiModelProperty(name = "gameSlotId", value = "老虎机游戏ID(电子类填写)", example = "af4e5644-c016-4a3f-8623-4fef7c58938a")
    private String gameSlotId;

    @NotNull
    @ApiModelProperty(name = "direction", value = "类型:0-取消关注 1-添加关注", example = "1")
    private Integer direction;
}
