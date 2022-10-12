package com.xinbo.sports.backend.service;

import com.xinbo.sports.backend.io.bo.user.AgentCenterParameter.*;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;
import com.xinbo.sports.utils.components.response.CodeInfo;
import com.xinbo.sports.utils.components.response.Result;

/**
 * @author: wells
 * @date: 2020/8/27
 * @description:
 */

public interface IAgentCenterService {
    /**
     * 代理中心-会员列表
     *
     * @param reqBody
     * @return
     */
    ResPage<AgentUserListResBody> agentUserList(ReqPage<AgentUserListReqBody> reqBody);

    /**
     * 代理中心-佣金盈利
     *
     * @param reqBody
     * @return
     */
    AgentCommissionResBody agentCommissionProfit(ReqPage<AgentCommissionReqBody> reqBody);

    /**
     * @Author Wells
     * @Description 佣金收益用户详情
     * @Date 2020/9/17 10:58 下午
     * @param1 reqBody
     * @Return com.xinbo.sports.utils.components.response.CodeInfo
     **/
    ResPage<AgentCommissionDetailsResDtoBody> agentCommissionDetails(ReqPage<AgentCommissionDetailsReqBody> reqBody);
}
