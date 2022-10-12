package com.xinbo.sports.service.base;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinbo.sports.dao.generator.po.AuthGroupAccess;
import com.xinbo.sports.dao.generator.po.GameSlotFavorite;
import com.xinbo.sports.dao.generator.service.*;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.service.cache.redis.UserCache;
import com.xinbo.sports.service.common.Constant;
import com.xinbo.sports.service.io.dto.BaseParams;
import com.xinbo.sports.service.thread.ThreadHeaderLocalData;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.alibaba.fastjson.JSON.parseObject;

/**
 * @author: David
 * @date: 15/05/2020
 * @description:
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserServiceBase {
    private final GameSlotFavoriteService gameSlotFavoriteServiceImpl;
    private final ConfigCache configCache;
    private final UserCache userCache;
    private final AuthGroupService authGroupServiceImpl;
    private final AuthRuleService authRuleServiceImpl;
    private final AuthGroupAccessService authGroupAccessServiceImpl;
    private final AdminService adminServiceImpl;

    /**
     * 用户的用户名 UID映射关系
     *
     * @return 集
     */
    public Map<String, Integer> getUsernameIdMap() {
        return userCache.userName2UidCacheMap();
    }

    /**
     * 获取用户收藏的指定老虎机游戏的游戏列表
     *
     * @param uid    UID
     * @param gameId GameId
     * @return 游戏列表集
     */
    public List<String> slotFavoriteByUid(Integer uid, Integer gameId) {
        List<String> collect = gameSlotFavoriteServiceImpl.lambdaQuery()
                .select(GameSlotFavorite::getGameSlotId)
                .eq(GameSlotFavorite::getUid, uid)
                .eq(GameSlotFavorite::getGameId, gameId)
                .list()
                .stream()
                .map(GameSlotFavorite::getGameSlotId)
                .collect(Collectors.toList());

        // 要是游戏列表是空列表,则默认添加空字符串
        if (collect.isEmpty()) {
            collect.add("");
        }

        return collect;
    }

    /**
     * 用户名 + platPrefix 构建三方平台用户名
     *
     * @param username 用户名
     * @return 返回后的用户名
     */
    public String buildUsername(String username) {
        return configCache.getPlatPrefix() + username;
    }

    /**
     * 获取用户名的平台前缀
     *
     * @param username 用户名 xba test999
     * @return 返回后的用户名 test999
     */
    public String filterUsername(String username) {
        username = username.toLowerCase();
        String platPrefix = configCache.getPlatPrefix();
        if (StringUtils.startsWith(username, platPrefix)) {
            return username.substring(platPrefix.length());
        } else {
            return "";
        }
    }

    /**
     * @Author Wells
     * @Description 验证手机号码是否隐藏
     * @Date 2020/9/18 2:22 下午
     * @param1 mobile 需要验证的号码
     * @Return java.lang.String 验证后的号码
     **/
    public UnaryOperator<String> checkShow(String fieldName) {
        UnaryOperator<String> function = x -> x;
        BaseParams.HeaderInfo currentLoginUser = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        var authGroupAccess = authGroupAccessServiceImpl.getOne(new LambdaQueryWrapper<AuthGroupAccess>()
                        .eq(AuthGroupAccess::getUid, currentLoginUser.getId()),
                false);
        //查看当前用户是否有手机号码的隐藏权限
        if (Objects.nonNull(authGroupAccess)) {
            var authGroup = authGroupServiceImpl.getById(authGroupAccess.getGroupId());
            var dataPermission = authGroup.getDataPermission();
            if (StringUtils.isNotEmpty(dataPermission)) {
                var fieldStatus = parseObject(dataPermission).getInteger(fieldName);
                // 0:全隐藏，1：半隐藏：2:全展示
                if (fieldStatus == 0) {
                    function = x -> "******";
                } else if (fieldStatus == 1) {
                    function = x -> isHalfShow(x, fieldName);
                }
            }
        }
        return function;
    }

    /**
     * @param value     原字符
     * @param fieldName 隐藏字段
     * @return 新字符
     */
    public String isHalfShow(String value, String fieldName) {
        if (StringUtils.isEmpty(value)) {
            return value;
        }
        var midStr = "****";
        if (fieldName.equals(Constant.MOBILE) && value.length() > 2) {
            value = value.substring(0, 3) + midStr;
        } else if (fieldName.equals(Constant.REAL_NAME) && value.length() > 1) {
            var pattern = "[\u4E00-\u9FA5]";
            if (Pattern.matches(pattern, value)) {
                value = value.charAt(0) + midStr;
            } else {
                value = value.substring(0, 2) + midStr;
            }
        } else if (fieldName.equals(Constant.BANK_ACCOUNT) && value.length() > 6) {
            value = value.substring(0, 3) + midStr + value.substring(value.length() - 3);
        }
        return value;
    }
}
