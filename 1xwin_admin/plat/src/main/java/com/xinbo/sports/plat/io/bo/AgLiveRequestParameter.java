package com.xinbo.sports.plat.io.bo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import javax.validation.constraints.NotEmpty;

/**
 * <p>
 * AGLive请求参数
 * </p>
 *
 * @author andy
 * @since 2020/7/9
 */
public interface AgLiveRequestParameter {
    /**
     * 通用请求KEY
     */
    @Getter
    enum ReqKey {
        cagent,
        loginname,
        method,
        actype,
        password,
        oddtype,
        cur,
        dm,
        sid,
        lang,
        gameType,
        billno,
        type,
        credit,
        gameCategory,
        fixcredit,
        flag
    }

    /**
     * 拉单请求KEY
     */
    @Getter
    enum BetSlipsReqKey {
        /**
         * 拉单代理编码
         */
        cagent,
        /**
         * 开始时间
         */
        startdate,
        /**
         * 结束时间,每次请求时间只能在 10 分钟以内的数据
         */
        enddate,
        /**
         * 游戏类型
         */
        gametype,
        /**
         * 排序栏名称,默认为 reckontime(订单派彩时间)(只限: billno, username, gmcode, billtime, reckontime, currency)
         */
        order,
        /**
         * 排序 (ASC 或 DESC)
         */
        by,
        /**
         * 页数
         */
        page,
        /**
         * 每页记录数,默认为 100,建议传入参数时<=500
         */
        perpage,
        /**
         * 每页记录数,默认为 100,建议传入参数时<=500
         */
        language,
        /**
         * 游戏局号
         */
        gamecode
    }

    /**
     * config配置类:对应sp_plat_list表的config字段
     */
    @Data
    @Builder
    @AllArgsConstructor
    class AgConfig {
        /**
         * 进入游戏域名地址
         */
        private String gciUrl;
        /**
         * 注册、余额查询、上下分域名地址
         */
        private String apiUrl;
        /**
         * 代理编码
         */
        private String cagent;
        /**
         * MD5 加密钥匙
         */
        private String md5Key;
        /**
         * DES 加密钥匙
         */
        private String desKey;
        /**
         * 默认币种
         */
        private String currency;
        /**
         * 拉单域名地址
         */
        private String betSlipsUrl;
        /**
         * 拉单代理编码
         */
        private String betSlipsCagent;
        /**
         * 拉单明码
         */
        private String betSlipsPidToken;
        /**
         * 环境:PROD-生产环境
         */
        private String environment;
    }

    @Data
    class LanguageDto {
        @NotEmpty(message = "language不能为空")
        @ApiModelProperty(name = "language", value = "支持语言:lang_english英文;默认简体中文", example = "lang_english")
        private String language;
    }
}
