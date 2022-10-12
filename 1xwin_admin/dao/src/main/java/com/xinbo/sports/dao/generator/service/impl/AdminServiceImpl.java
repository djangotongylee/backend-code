package com.xinbo.sports.dao.generator.service.impl;

import com.xinbo.sports.dao.generator.po.Admin;
import com.xinbo.sports.dao.generator.mapper.AdminMapper;
import com.xinbo.sports.dao.generator.service.AdminService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 后台管理员账号 服务实现类
 * </p>
 *
 * @author David
 * @since 2021-02-01
 */
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

}
