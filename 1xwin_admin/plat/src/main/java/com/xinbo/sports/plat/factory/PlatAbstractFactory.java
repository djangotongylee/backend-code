package com.xinbo.sports.plat.factory;

import com.google.common.base.CaseFormat;
import com.xinbo.sports.dao.generator.po.BetSlipsSupplemental;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams.*;
import com.xinbo.sports.plat.io.constant.ConstData;
import com.xinbo.sports.service.aop.annotation.RepeatCommit;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.utils.SpringUtils;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

/**
 * @author: David
 * @date: 03/05/2020
 * @description: 第三方游戏平台基类
 */
@Component
public interface PlatAbstractFactory {

    /**
     * 初始化工厂实体
     *
     * @param model  游戏实体名称
     * @param parent 父类目录
     * @return 工厂实体
     */
    @Nullable
    static PlatAbstractFactory init(String model, @NotNull String parent) {

        String className = ConstData.CLASS_PATH_PREFIX + parent.toLowerCase() + "." + model + "ServiceImpl";
        try {
            Class<?> cls = Class.forName(className);
            var plat = (PlatAbstractFactory) SpringUtils.getBean(cls);
            setConfig(cls, parent, plat, false);
            return plat;
        } catch (Exception e) {
            // 无需处理
            return null;
        }
    }

    /**
     * @Author Wells
     * @Description set值各个平台配置
     * @Date 2020/9/16 7:52 下午
     * @param1 cls 平台class类
     * @param2 parent 平台名称
     * @param3 plat 平台实体类
     * @param4 isParent 是否父类model
     * @Return void
     **/
    @SneakyThrows
    static void setConfig(Class<?> cls, String parent, PlatAbstractFactory plat, boolean isParent) {
        var configField = "config";
        var setField = "set" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, configField);
        //获取平台配置
        var configCache = SpringUtils.getBean(ConfigCache.class);
        var classInstance = isParent ? cls : cls.getSuperclass();
        var field = classInstance.getDeclaredField(configField);
        var value = configCache.platConfigByModelName(parent, Class.forName(field.getType().getName()));
        var method = classInstance.getDeclaredMethod(setField, Class.forName(field.getType().getName()));
        //set平台配置到对应的配置类
        method.invoke(plat, value);
    }

    /**
     * 初始化工厂实体
     *
     * @param parent 父类Model
     * @return 工厂实体
     */
    @Nullable
    static PlatAbstractFactory initByPlat(String parent) {
        String beanName = parent + "ServiceImpl";
        String className = ConstData.CLASS_PATH_PREFIX + beanName;
        try {
            Class<?> cls = Class.forName(className);
            var plat = (PlatAbstractFactory) SpringUtils.getBean(beanName, cls);
            setConfig(cls, parent, plat, true);
            return plat;
        } catch (Exception e) {
            // 无需处理
        }
        return null;
    }

    /**
     * 构造BetSlipsSupplemental对象
     *
     * @param gameListId    sp_game_list表主键ID
     * @param start         开始时间
     * @param end           结束时间
     * @param requestParams 请求参数
     * @param now           当前时间
     * @return BetSlipsSupplemental
     */
    static BetSlipsSupplemental buildBetSlipsSupplemental(Integer gameListId, String start, String end, String requestParams, Integer now) {
        BetSlipsSupplemental po = new BetSlipsSupplemental();
        po.setGameListId(gameListId);
        po.setRequest(requestParams);
        po.setTimeStart(start);
        po.setTimeEnd(end);
        po.setCreatedAt(now);
        po.setUpdatedAt(now);
        return po;
    }

    /**
     * 创建会员
     *
     * @param reqDto 创建会员信息
     * @return true-成功 false-失败
     */
    Boolean registerUser(PlatRegisterReqDto reqDto);

    /**
     * 获取游戏登录链接
     *
     * @param platLoginReqDto {username, lang, device, slotId}
     * @return 登录链接
     */
    PlatGameLoginResDto login(PlatLoginReqDto platLoginReqDto);

    /**
     * 登出游戏
     *
     * @param dto {"username"}
     * @return success:1-成功 0-失败
     * @author: David
     * @date: 04/05/2020
     */
    PlatLogoutResDto logout(PlatLogoutReqDto dto);

    /**
     * 三方上分
     *
     * @param platCoinTransferUpReqDto {"coin","orderId","username"}
     * @return {platCoin}
     * @author: David
     * @date: 04/05/2020
     */
    @RepeatCommit
    PlatCoinTransferResDto coinUp(PlatCoinTransferReqDto platCoinTransferUpReqDto);

    /**
     * 三方下分
     *
     * @param platCoinTransferDownReqDto {"coin","orderId","username"}
     * @return {platCoin}
     * @author: David
     * @date: 04/05/2020
     */
    @RepeatCommit
    PlatCoinTransferResDto coinDown(PlatCoinTransferReqDto platCoinTransferDownReqDto);

    /**
     * 查询三方余额
     *
     * @param platQueryBalanceReqDto {"username"}
     * @return {platCoin}
     * @author: David
     * @date: 04/05/2020
     */
    PlatQueryBalanceResDto queryBalance(PlatQueryBalanceReqDto platQueryBalanceReqDto);

    /**
     * 拉取三方注单信息
     *
     * @author: David
     * @date: 04/05/2020
     */
    void pullBetsLips();

    /**
     * 检查转账状态
     *
     * @param orderId 订单号
     * @return true-成功 false-失败
     * @author: David
     * @date: 04/05/2020
     */
    Boolean checkTransferStatus(String orderId);

    /**
     * 根据起始、结束时间 生成补单信息
     *
     * @param dto dto.start 开始时间 dto.end结束时间
     */
    default void genSupplementsOrders(GenSupplementsOrdersReqDto dto) {
    }

    /**
     * 补充注单信息
     *
     * @param dto dto.requestInfo 补单请求参数
     * @author: David
     * @date: 04/05/2020
     */
    default void betsRecordsSupplemental(BetsRecordsSupplementReqDto dto) {

    }

    /**
     * 拉单异常 再次拉单
     *
     * @param dto dto.requestInfo 拉单请求参数
     * @author: David
     * @date: 04/05/2020
     */
    default void betSlipsExceptionPull(BetsRecordsSupplementReqDto dto) {

    }
}

