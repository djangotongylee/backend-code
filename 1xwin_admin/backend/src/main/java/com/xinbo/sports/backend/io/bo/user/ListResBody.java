package com.xinbo.sports.backend.io.bo.user;

import com.xinbo.sports.service.io.bo.UserCacheBo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 会员管理-列表查询
 * </p>
 *
 * @author andy
 * @since 2020/3/12
 */
@Data
public class ListResBody extends UserInfo {
    @ApiModelProperty(name = "coin", value = "账户余额", example = "99999.00")
    private BigDecimal coin;
    @ApiModelProperty(name = "loginTime", value = "最近登录时间，不能排序", example = "1583907337")
    private Integer loginTime;

    @ApiModelProperty(name = "createdAt", value = "创建时间，可排序", example = "1583907337")
    private Integer createdAt;

    @ApiModelProperty(name = "userFlagList", value = "会员旗列表")
    private List<UserCacheBo.UserFlagInfo> userFlagList;
    @ApiModelProperty(hidden = true)
    private Integer flag;

    @ApiModelProperty(name = "levelText", value = "会员等级:vip1-乒乓球达人", example = "vip1-乒乓球达人")
    private String levelText;
    @ApiModelProperty(name = "supUid1Name", value = "上级代理", example = "andy6666")
    private String supUid1Name;

    @ApiModelProperty(name = "mobile", value = "手机号", example = "19912345678")
    private String mobile;

    @ApiModelProperty(name = "futuresCoin", value = "future余额", example = "99999.00")
    private BigDecimal futuresCoin;
}

