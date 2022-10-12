package com.xinbo.sports.backend.service;

import com.xinbo.sports.backend.io.bo.notice.*;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;

/**
 * <p>
 * 公告管理
 * </p>
 *
 * @author andy
 * @since 2020/6/8
 */
public interface INoticeManagerService {
    /**
     * 公告管理-列表
     *
     * @param reqBody
     * @return
     */
    ResPage<ListNoticeResBody> list(ReqPage<ListNoticeReqBody> reqBody);

    /**
     * 公告管理-添加
     *
     * @param reqBody
     * @return
     */
    void add(AddNoticeReqBody reqBody);

    /**
     * 公告管理-编辑
     *
     * @param reqBody
     * @return
     */
    void update(UpdateNoticeReqBody reqBody);

    /**
     * 公告管理-删除
     *
     * @param reqBody
     * @return
     */
    void delete(DeleteNoticeReqBody reqBody);

    /**
     * 公告管理-查询UID(站内信)
     *
     * @param reqBody
     * @return
     */
    ResPage<UidListResBody> uidList(ReqPage<UidListReqBody> reqBody);
}
