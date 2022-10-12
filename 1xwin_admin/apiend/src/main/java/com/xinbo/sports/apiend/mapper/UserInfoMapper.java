package com.xinbo.sports.apiend.mapper;

import com.xinbo.sports.apiend.io.dto.mapper.UserInfoResDto;

/**
 * @author: David
 * @date: 19/04/2020
 * @description:
 */
public interface UserInfoMapper {
    /**
     * @param uid uid
     * @return 用户信息
     */
    UserInfoResDto getUserInfoById(Integer uid);

    /**
     * @param username username
     * @return 用户信息
     */
    UserInfoResDto getUserInfoByUsername(String username);
}
