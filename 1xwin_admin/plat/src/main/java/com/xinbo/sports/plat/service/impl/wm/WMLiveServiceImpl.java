package com.xinbo.sports.plat.service.impl.wm;

import com.xinbo.sports.dao.generator.service.BetslipsWmService;
import com.xinbo.sports.plat.service.impl.WMServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 * WM真人
 * </p>
 *
 * @author andy
 * @since 2020/5/19
 */
@Service
public class WMLiveServiceImpl extends WMServiceImpl {
    @Resource
    private BetslipsWmService betslipsWmServiceImpl;


}
