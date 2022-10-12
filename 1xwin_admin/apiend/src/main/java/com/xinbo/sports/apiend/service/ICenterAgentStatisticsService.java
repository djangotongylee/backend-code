package com.xinbo.sports.apiend.service;

import com.xinbo.sports.apiend.io.dto.StartEndTime;
import com.xinbo.sports.apiend.io.dto.centeragent.MyTeamStatisticsResBody;
import com.xinbo.sports.apiend.io.dto.centeragent.ReportsReqBody;
import com.xinbo.sports.apiend.io.dto.centeragent.ReportsResBody;

/**
 * <p>
 * 代理中心业务处理接口
 * </p>
 *
 * @author andy
 * @since 2020/4/22
 */
public interface ICenterAgentStatisticsService {

    /**
     * 代理中心-我的报表-PC
     *
     * @param reqBody reqBody
     * @return 报表总额统计实体
     */
    ReportsResBody reports(ReportsReqBody reqBody);

    /**
     * 代理中心-我的报表
     *
     * @param reqBody reqBody
     * @return 报表总额统计实体
     */
    MyTeamStatisticsResBody myTeamStatisticsHisOrNew(StartEndTime reqBody);
}
