package com.xinbo.sports.apiend.mapper;

import com.xinbo.sports.apiend.io.dto.platform.GameModelDto;

import java.util.List;

/**
 * @author: David
 * @date: 19/04/2020
 * @description:
 */
public interface GamePropMapper {
    GameModelDto getGameProp(Integer gameId);

    List<GameModelDto> getGamePropList(Integer gameId);
}
