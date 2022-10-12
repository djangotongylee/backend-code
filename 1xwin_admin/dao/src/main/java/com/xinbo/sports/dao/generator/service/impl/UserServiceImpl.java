package com.xinbo.sports.dao.generator.service.impl;

import com.xinbo.sports.dao.generator.po.User;
import com.xinbo.sports.dao.generator.mapper.UserMapper;
import com.xinbo.sports.dao.generator.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 客户表 服务实现类
 * </p>
 *
 * @author David
 * @since 2021-02-01
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

}
