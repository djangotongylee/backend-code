package com.xinbo.sports.apiend.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinbo.sports.apiend.io.dto.center.GiftRecordsDetailsResDto;
import com.xinbo.sports.apiend.io.dto.center.GiftRecordsReqDto;
import com.xinbo.sports.apiend.io.dto.center.GiftRecordsResDto;
import com.xinbo.sports.apiend.service.ICenterService;
import com.xinbo.sports.apiend.service.IUserInfoService;
import com.xinbo.sports.dao.generator.po.GiftRecords;
import com.xinbo.sports.dao.generator.service.GiftRecordsService;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.dto.UserInfo;
import com.xinbo.sports.utils.BeanConvertUtils;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;
import com.xinbo.sports.utils.components.response.CodeInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.Objects;

/**
 * @author: David
 * @date: 22/04/2020
 * @description: 个人中心-实物领取
 */
@Service
public class CenterServiceImpl implements ICenterService {
    @Autowired
    IUserInfoService userInfoServiceImpl;
    @Autowired
    GiftRecordsService giftRecordsServiceImpl;


    /**
     * @desc: 好礼领取
     * @params: [dto]
     * @return: com.xinbo.sports.utils.components.pagination.ResPage<com.xinbo.sports.apiend.io.dto.center.GiftRecordsResDto>
     * @author: David
     * @date: 22/04/2020
     */
    @Override
    public ResPage<GiftRecordsResDto> giftRecords(@Valid ReqPage<GiftRecordsReqDto> dto) {
        Integer status = dto.getData().getStatus();
        UserInfo userInfo = userInfoServiceImpl.findIdentityByApiToken();
        Page<GiftRecords> page = giftRecordsServiceImpl.lambdaQuery()
                .eq(GiftRecords::getUid, userInfo.getId())
                .eq(Objects.nonNull(status), GiftRecords::getStatus, status)
                .between(GiftRecords::getCreatedAt, dto.getData().getStartTime(), dto.getData().getEndTime())
                .page(dto.getPage());
        Page<GiftRecordsResDto> tmpPage = BeanConvertUtils.copyPageProperties(page, GiftRecordsResDto::new, (source, giftRecordsResDto) -> {
            if (source.getStatus() != 2) {
                giftRecordsResDto.setMark("--");
            }
        });
        return ResPage.get(tmpPage);
    }

    @Override
    public GiftRecordsDetailsResDto giftRecordsDetails(Long id) {
        if (Objects.isNull(id)) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        GiftRecords one = giftRecordsServiceImpl.lambdaQuery().eq(GiftRecords::getId, id).one();
        return BeanConvertUtils.beanCopy(one, GiftRecordsDetailsResDto::new);
    }
}
