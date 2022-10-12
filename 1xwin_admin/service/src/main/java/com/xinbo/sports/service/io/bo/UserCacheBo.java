package com.xinbo.sports.service.io.bo;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * <p>
 * 用户缓存实体
 * </p>
 *
 * @author andy
 * @since 2020/7/24
 */
public interface UserCacheBo {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class UserCacheInfo {
        /**
         * UID
         */
        private Integer uid;

        /**
         * 用户名
         */
        private String username;

        /**
         * 头像
         */
        @TableField("avatar")
        private String avatar;

        /**
         * 会员等级
         */
        private Integer levelId;

        /**
         * 角色:0-会员 1-代理 2-总代理 3-股东 4-测试 10-系统账号
         */
        private Integer role;

        /**
         * 推广码
         */
        private Integer promoCode;

        /**
         * 会员旗
         */
        private Integer flag;

        /**
         * 注册时间
         */
        private Integer createdAt;

        /**
         * 额外打码量
         */
        private BigDecimal extraCode;

    }

    /**
     * 用户会员旗
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class UserFlagInfo {
        @ApiModelProperty(name = "icon", value = "图标", example = "icon-ordinary_vip-5")
        private String icon;

        @ApiModelProperty(name = "iconColor", value = "图标颜色", example = "#A2BF8B")
        private String iconColor;

        @ApiModelProperty(name = "name", value = "会员旗名称", example = "VIP会员")
        private String name;

        @ApiModelProperty(name = "bitCode", value = "会员旗", example = "512")
        private Integer bitCode;
    }

}
