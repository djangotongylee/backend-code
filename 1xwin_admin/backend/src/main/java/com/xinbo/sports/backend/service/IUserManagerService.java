package com.xinbo.sports.backend.service;

import com.alibaba.fastjson.JSONObject;
import com.xinbo.sports.backend.io.bo.user.*;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;

import java.util.List;

/**
 * <p>
 * 会员管理 接口
 * </p>
 *
 * @author andy
 * @since 2020/3/12
 */
public interface IUserManagerService {
    /**
     * 会员列表
     *
     * @param reqBody
     * @return 分页
     */
    ResPage<ListResBody> list(ReqPage<ListReqBody> reqBody);

    /**
     * 会员详情
     *
     * @param po
     * @return
     */
    DetailResBody detail(DetailReqBody po);

    /**
     * 新增会员
     *
     * @param reqBody
     * @return
     */
    AddUserResBody addUser(AddUserReqBody reqBody);

    /**
     * 修改会员
     *
     * @param reqBody
     * @return
     */
    boolean updateUser(UpdateUserReqBody reqBody);

    /**
     * 会员等级管理-等级列表
     *
     * @return
     */
    List<ListLevelResBody> listLevel();

    /**
     * 会员等级管理-修改等级
     *
     * @param reqBody
     * @return
     */
    boolean updateLevel(UpdateLevelReqBody reqBody);

    /**
     * 会员管理-会员旗管理-列表查询
     *
     * @return
     */
    List<ListFlagResBody> listFlag();

    /**
     * 会员管理-会员旗管理-启用或禁用
     *
     * @param reqBody
     * @return
     */
    boolean updateFlagStatus(UpdateFlagStatusReqBody reqBody);

    /**
     * 会员管理-稽核明细-详情
     *
     * @return
     */
    ResPage<AuditDetailsResBody> auditDetails(ReqPage<CodeUidReqBody> reqPage);

    /**
     * 会员管理-打码量明细-详情
     *
     * @param reqPage
     * @return
     */
    CodeDetailsResBody codeDetails(ReqPage<CodeUidReqBody> reqPage);

    /**
     * 会员管理-会员旗管理-修改会员旗
     *
     * @param reqBody
     * @return
     */
    boolean updateFlag(UpdateFlagReqBody reqBody);


    /**
     * 会员列表-团队在线-人数
     *
     * @param reqBody uid
     * @return
     */
    ListOnlineCountResBody listOnlineCount(ListOnlineReqBody reqBody);

    /**
     * 会员列表-路线转移
     *
     * @param reqBody
     * @return
     */
    boolean routeTransfer(RouteTransferReqBody reqBody);

    /**
     * 会员旗管理-用户会员旗列表(弹框)
     *
     * @param reqBody
     * @return
     */
    ResPage<ListFlagUsedResBody> listFlagUsed(ReqPage<ListFlagUsedReqBody> reqBody);

    /**
     * 会员旗管理-用户会员旗列表(弹框)-批量删除
     *
     * @param reqBody
     * @return
     */
    boolean delUserFlag(DelUserFlagReqBody reqBody);

    /**
     * 会员旗管理-新增用户会员旗
     *
     * @param reqBody
     * @return
     */
    boolean addUserFlag(AddUserFlagReqBody reqBody);

    /**
     * 会员旗管理-下拉列表
     *
     * @return
     */
    List<UserFlagDict> userFlagDict();

    /**
     * 上级代理
     *
     * @param uid
     * @return
     */
    List<SupProxyListReqBody> supProxyList(Integer uid, String username);

    /**
     * 会员旗管理-获取用户会员旗
     *
     * @param reqBody
     * @return
     */
    List<Integer> listUserFlag(ListUserFlagReqBody reqBody);

    /**
     * 会员列表-下级列表
     *
     * @param reqBody
     * @return
     */
    ResPage<ListOnlineResBody> listChild(ListOnlineReqBody reqBody);

    /**
     * 会员等级管理-详情
     *
     * @param reqBody
     * @return
     */
    LevelDetailResBody levelDetail(JSONObject reqBody);

    /**
     * 会员管理-提款消费明细
     *
     * @param reqBody
     * @return
     */
    ResPage<ListCodeRecords> listCodeRecords(ListCodeRecordsReqBody reqBody);

    /**
     * 会员管理-清空提款消费量
     *
     * @param uid
     */
    void clearCodeRecords(Integer uid);

    /**
     * 会员管理-批量修改会员等级
     *
     * @param reqBody
     */
    void updateBatchLevel(UpdateBatchLevelReqBody reqBody);

    ResPage<AgentCenterParameter.SmsCodeResDto> getVerifyCodeList(ReqPage<AgentCenterParameter.SmsCodeReqDto> reqBody);

    /**
     * 在线人数->列表查询(分页)
     *
     * @param reqBody reqBody
     * @return ResPage<OnlineUserCountListResBody>
     */
    ResPage<OnlineUserCountListResBody> onlineUserCountList(ReqPage<OnlineUserCountListReqBody> reqBody);

    /**
     * 清除用户token与手机验证次数
     */
    Boolean clearTokenCode(ClearTokenCodeReqBody reqBody);
}
