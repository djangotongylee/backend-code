package com.xinbo.sports.backend.io.bo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * <p>
 * 问题反馈
 * </p>
 *
 * @author andy
 * @since 2020/6/1
 */
public interface Feedback {

    /**
     * RequestBody 问题反馈-列表
     */
    @Data
    class ListFeedBackReqBody extends StartEndTime {
        @ApiModelProperty(name = "category", value = "问题类型")
        private Integer category;
        @ApiModelProperty(name = "username", value = "用户名", example = "andy66")
        private String username;
    }

    /**
     * ResponseBody 问题反馈-列表
     */
    @Data
    class ListFeedBackResBody {

        @ApiModelProperty(name = "username", value = "用户名", example = "andy66")
        private String username;

        @ApiModelProperty(name = "id", value = "ID", example = "1")
        private Integer id;

        @ApiModelProperty(name = "uid", value = "UID", example = "1")
        private Integer uid;

        @ApiModelProperty(name = "category", value = "问题类型")
        private Integer category;

        @ApiModelProperty(name = "content", value = "内容", example = "充值问题无法到账...")
        private String content;

        @ApiModelProperty(name = "img", value = "图片")
        private String img;

        @ApiModelProperty(name = "imgList", value = "图片")
        private List<String> imgList;

        @ApiModelProperty(name = "status", value = "状态:1-已回复 0-未回复")
        private Integer status;

        @ApiModelProperty(name = "updatedAt", value = "时间")
        private Integer updatedAt;

        @ApiModelProperty(name = "replyContent", value = "回复内容", example = "回复内容....")
        private String replyContent;
    }

    /**
     * RequestBody 问题反馈-回复
     */
    @Data
    class AddFeedBackReplyReqBody {

        @NotNull(message = "id不能为空")
        @ApiModelProperty(name = "id", value = "ID", example = "1", required = true)
        private Integer id;

        @NotEmpty(message = "content不能为空")
        @ApiModelProperty(name = "content", value = "回复内容", required = true)
        private String content;

    }
}
