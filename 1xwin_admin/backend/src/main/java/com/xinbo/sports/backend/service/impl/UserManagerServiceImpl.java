package com.xinbo.sports.backend.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xinbo.sports.backend.io.bo.user.*;
import com.xinbo.sports.backend.mapper.UserManagerMapper;
import com.xinbo.sports.backend.redis.GameCache;
import com.xinbo.sports.backend.service.IUserManagerService;
import com.xinbo.sports.dao.generator.po.*;
import com.xinbo.sports.dao.generator.service.*;
import com.xinbo.sports.plat.io.bo.FuturesLotteryRequestParameter.GetUserCoinListByUserNameListReqBody;
import com.xinbo.sports.plat.io.bo.FuturesLotteryRequestParameter.GetUserCoinListByUserNameListResBody;
import com.xinbo.sports.plat.service.impl.futures.FuturesLotteryServiceImpl;
import com.xinbo.sports.service.base.AuditBase;
import com.xinbo.sports.service.base.RegisterBonusPromotions;
import com.xinbo.sports.service.base.UserServiceBase;
import com.xinbo.sports.service.cache.KeyConstant;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.service.cache.redis.UserCache;
import com.xinbo.sports.service.cache.redis.UserChannelRelCache;
import com.xinbo.sports.service.common.Constant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.bo.UserCacheBo;
import com.xinbo.sports.utils.*;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.alibaba.fastjson.JSON.parseArray;


/**
 * <p>
 * 会员管理 接口实现
 * </p>
 *
 * @author andy
 * @since 2020/3/12
 */
@Slf4j
@Service
public class UserManagerServiceImpl extends ServiceImpl<UserManagerMapper, ListReqBody> implements IUserManagerService {

    @Resource
    private UserService userServiceImpl;
    @Resource
    private UserProfileService userProfileServiceImpl;
    @Resource
    private UserLevelService userLevelServiceImpl;
    @Resource
    private UserFlagService userFlagServiceImpl;
    @Resource
    private UserLoginLogService userLoginLogServiceImpl;
    @Resource
    private UserBankService userBankServiceImpl;
    @Resource
    private UserLevelRebateService userLevelRebateServiceImpl;
    @Resource
    private CodeRecordsService codeRecordsServiceImpl;
    @Resource
    private UserCache userCache;
    @Resource
    private GameCache gameCache;
    @Resource
    private CodeAuditService codeAuditServiceImpl;
    @Resource
    private UserServiceBase userServiceBase;
    @Resource
    private JedisUtil jedisUtil;
    @Resource
    private UserChannelRelCache userChannelRelCache;
    @Resource
    private FuturesLotteryServiceImpl futuresLotteryServiceImpl;
    @Resource
    private FinancialWithdrawalServiceImpl financialWithdrawalServiceImpl;
    @Resource
    private ConfigCache configCache;
    @Resource
    private AuditBase auditBase;
    @Resource
    private RegisterBonusPromotions registerBonusPromotions;

    @Override
    public ResPage<ListResBody> list(ReqPage<ListReqBody> reqBody) {
        if (null == reqBody) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        ListReqBody data = reqBody.getData();
        if (null != data) {
            data.setUidList(getOnLineUidList(data.getOnLineNumFlag()));
        }

        ResPage<ListResBody> result = ResPage.get(baseMapper.list(reqBody.getPage(), data));
        var unaryOperator = userServiceBase.checkShow(Constant.MOBILE);
        var prefix = configCache.getPlatPrefix();
        List<String> userNameList = result.getList().stream().map(o -> prefix + o.getUsername()).collect(Collectors.toList());
        result.getList().stream().forEach(o -> {
            // get上级代理名称
            UserProfile userProfile = userProfileServiceImpl.getById(o.getUid());
            UserCacheBo.UserCacheInfo supUserUid1Po = userCache.getUserInfoById(userProfile.getSupUid1());
            o.setSupUid1Name(null != supUserUid1Po ? supUserUid1Po.getUsername() : null);
            UserCacheBo.UserCacheInfo userInfo = userCache.getUserInfoById(o.getUid());
            // 会员等级 -> vip1-乒乓球达人
            o.setLevelText(getLevelTextByLevelId(userInfo.getLevelId()));

            // 会员旗帜
            o.setUserFlagList(userCache.getUserFlagList(o.getUid()));

            // 手机号码是否隐藏
            o.setMobile(unaryOperator.apply(o.getMobile()));
            o.setFuturesCoin(BigDecimal.ZERO);
        });
        return result;
    }

    private List<Integer> getOnLineUidList(String onLineNumFlag) {
        List<Integer> onLineNumUidList = null;
        if ((StringUtils.isNotBlank(onLineNumFlag) && "Y".equals(onLineNumFlag))) {
            onLineNumUidList = userChannelRelCache.getChannelUids();
        }
        if (Optional.ofNullable(onLineNumUidList).isPresent() && onLineNumUidList.isEmpty()) {
            onLineNumUidList = null;
        }
        return onLineNumUidList;
    }

    /**
     * 获取会员等级by等级ID
     *
     * @param levelId 等级ID
     * @return 会员等级:vip1-乒乓球达人
     */
    private String getLevelTextByLevelId(Integer levelId) {
        String levelText = null;
        UserLevel userLevel = userCache.getUserLevelById(levelId);
        if (null != userLevel) {
            levelText = userLevel.getCode() + " - " + userLevel.getName();
        }
        return levelText;
    }

    @Override
    public DetailResBody detail(DetailReqBody po) {
        if (null == userCache.getUserInfoById(po.getUid())) {
            throw new BusinessException(CodeInfo.USER_NOT_EXISTS);
        }
        DetailResBody data = baseMapper.getDetail(po);
        if (null != data) {
            // 下级会员数
            int childCount = 0;
            LambdaQueryWrapper<UserProfile> where = whereUserPro(data.getUid());
            List<UserProfile> childList = userProfileServiceImpl.list(where);
            if (null != childList) {
                childCount = childList.size();
            }
            // 上级代理
            List<SupProxyListReqBody> userNameList = supProxyList(data.getUid(), data.getUsername());
            int userBankCount = getUserBankCount(data.getUid());
            data.setIp(data.getIp() != null ? data.getIp().split(":")[0].replace("/", "") : data.getIp());
            data.setBindBankCount(userBankCount);
            data.setProxyList(userNameList);
            data.setChildCount(childCount);
            // 会员旗
            data.setUserFlagList(userCache.getUserFlagList(data.getUid()));
            // 手机号码是否隐藏
            data.setMobile(userServiceBase.checkShow(Constant.MOBILE).apply(data.getMobile()));
            // 用户名是否隐藏
            data.setRealname(userServiceBase.checkShow(Constant.REAL_NAME).apply(data.getRealname()));
            // 获取登录日志记录-最近一次
            UserLoginLog one = userLoginLogServiceImpl.lambdaQuery()
                    .eq(UserLoginLog::getUid, data.getUid())
                    .orderByDesc(UserLoginLog::getCreatedAt).last("limit 1")
                    .one();
            if (null != one) {
                data.setIp(one.getIp());
                data.setCategory(one.getCategory());
                data.setLoginTime(one.getCreatedAt());
                data.setDevice(one.getDevice());
            }
        }
        return data;
    }

    /**
     * 上级代理
     *
     * @param uid
     * @return
     */
    @Override
    public List<SupProxyListReqBody> supProxyList(Integer uid, String username) {
        List<SupProxyListReqBody> userNameList = new ArrayList<>();
        uid = supProxyListReqBody(uid, username);
        UserProfile userProfile = userProfileServiceImpl.getOne(new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUid, uid));
        if (null != userProfile) {
            if (null != userProfile.getSupUid6()) {
                processSupProxyList(userNameList, userProfile.getSupUid6());
            }
            if (null != userProfile.getSupUid5()) {
                processSupProxyList(userNameList, userProfile.getSupUid5());
            }
            if (null != userProfile.getSupUid4()) {
                processSupProxyList(userNameList, userProfile.getSupUid4());
            }
            if (null != userProfile.getSupUid3()) {
                processSupProxyList(userNameList, userProfile.getSupUid3());
            }
            if (null != userProfile.getSupUid2()) {
                processSupProxyList(userNameList, userProfile.getSupUid2());
            }
            if (null != userProfile.getSupUid1()) {
                processSupProxyList(userNameList, userProfile.getSupUid1());
            }
        }
        Collections.reverse(userNameList);
        return userNameList;
    }

    private Integer supProxyListReqBody(Integer uid, String username) {
        if (StringUtils.isNotBlank(username)) {
            UserCacheBo.UserCacheInfo user = userCache.getUserInfoByUserName(username);
            if (null == user) {
                throw new BusinessException(CodeInfo.USER_NOT_EXISTS);
            }
            uid = user.getUid();
        }
        return uid;
    }


    private void processSupProxyList(List<SupProxyListReqBody> userNameList, Integer uid) {
        //用于过滤代理用户，故而不用缓存
        var user = userServiceImpl.getById(uid);
        if (null != user) {
            SupProxyListReqBody reqBody = new SupProxyListReqBody();
            reqBody.setUid(uid);
            reqBody.setUsername(user.getUsername());
            userNameList.add(reqBody);
        }
    }

    private String passwordHash(String pwd) {
        return PasswordUtils.generatePasswordHash(pwd);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AddUserResBody addUser(AddUserReqBody reqBody) {
        if (StringUtils.isNotBlank(reqBody.getUsername()) && reqBody.getUsername().length() > 0 && reqBody.getUsername().equals(reqBody.getSupUsername())) {
            throw new BusinessException(CodeInfo.USER_ACCOUNT_SUP_NOT_SAME);
        }
        // 1.判断上级代理是否存在，如果不存在:提示上级代理不存在
        LambdaQueryWrapper<User> where = Wrappers.lambdaQuery();
        where.eq(User::getUsername, reqBody.getSupUsername());
        User supUser = userServiceImpl.getOne(where);
        // 上级代理不存在
        if (null == supUser) {
            throw new BusinessException(CodeInfo.USER_SUP_ACCOUNT_NOT_EXISTS);
        }

        String realUsername = reqBody.getUsername().toLowerCase();
        where = Wrappers.lambdaQuery();
        where.eq(User::getUsername, realUsername);
        if (null != userServiceImpl.getOne(where)) {
            throw new BusinessException(CodeInfo.ACCOUNT_EXISTS);
        }

        UserProfile supUserProfile = userProfileServiceImpl.getOne(new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUid, supUser.getId()));
        Integer currentTime = DateUtils.getCurrentTime();
        User user = new User();
        user.setUsername(realUsername);
        user.setRole(reqBody.getRole());
        user.setAvatar("/avatar/9.png");
        user.setLevelId(reqBody.getLevelId());
        user.setCreatedAt(currentTime);
        user.setUpdatedAt(currentTime);
        userServiceImpl.save(user);
        log.info("userServiceImpl.save userId=" + user.getId().toString());

        UserProfile userProfile = new UserProfile();
        userProfile.setUid(user.getId());
        userProfile.setSupUid1(supUser.getId());
        setSupUid(userProfile, supUserProfile);
        userProfile.setCreatedAt(currentTime);
        userProfile.setUpdatedAt(currentTime);

        userProfile.setPasswordHash(passwordHash(reqBody.getPasswordHash()));
        Integer promoCode = new Random().nextInt(9999999 - 100000 + 1) + 100000;
        while (userProfileServiceImpl.getOne(new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getPromoCode, promoCode)) != null) {
            promoCode = new Random().nextInt(9999999 - 100000 + 1) + 100000;
        }

        userProfile.setPromoCode(promoCode);
        userProfileServiceImpl.save(userProfile);
        // 更新上级代理缓存
        userCache.updateUserSubordinateUidListHash(supUser.getId());
        userCache.updateUserName2UidCacheMap();
        userCache.updateTestUidListCache();
        //新用户注册送彩金
        registerBonusPromotions.userRegisterAutoPromotions(user.getId());
        return baseMapper.getUserInfo(user.getId());
    }

    /**
     * 设置上级代理
     *
     * @param profile        当前
     * @param supUserProfile 上级代理
     */
    private void setSupUid(UserProfile profile, UserProfile supUserProfile) {
        if (null != profile && null != supUserProfile) {
            profile.setSupUid1(supUserProfile.getUid());
            profile.setSupUid2(supUserProfile.getSupUid1());
            profile.setSupUid3(supUserProfile.getSupUid2());
            profile.setSupUid4(supUserProfile.getSupUid3());
            profile.setSupUid5(supUserProfile.getSupUid4());
            profile.setSupUid6(supUserProfile.getSupUid5());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(UpdateUserReqBody reqBody) {
        Integer current = DateUtils.getCurrentTime();
        User userById = userServiceImpl.getById(reqBody.getUid());
        if (null == userById) {
            throw new BusinessException(CodeInfo.ACCOUNT_NOT_EXISTS);
        }

        User user = new User();
        if (null != reqBody.getRole()) {
            user.setRole(reqBody.getRole());
        }
        if (null != reqBody.getLevelId()) {
            user.setLevelId(reqBody.getLevelId());
        }
        user.setUpdatedAt(current);
        userServiceImpl.update(user, new LambdaQueryWrapper<User>()
                .eq(User::getId, reqBody.getUid()));

        // 更新账号状态:10-正常 9-冻结 8-永久冻结
        UserProfile userProfile = new UserProfile();
        if (null != reqBody.getStatus()) {
            userProfile.setStatus(reqBody.getStatus());
        }
        if (null != reqBody.getSex()) {
            userProfile.setSex(reqBody.getSex());
        }
        if (StringUtils.isNotEmpty(reqBody.getBirthday())) {
            userProfile.setBirthday(reqBody.getBirthday());
        }
        if (StringUtils.isNotBlank(reqBody.getMobile()) && StringUtils.isNotBlank(reqBody.getAreaCode())) {
            // 检查手机号是否存在
            UserProfile checkMobile = userProfileServiceImpl.lambdaQuery()
                    .eq(UserProfile::getAreaCode, reqBody.getAreaCode())
                    .eq(UserProfile::getMobile, reqBody.getMobile())
                    .ne(UserProfile::getUid, reqBody.getUid())
                    .one();
            if (checkMobile != null) {
                throw new BusinessException(CodeInfo.MOBILE_EXISTS);
            }

            userProfile.setMobile(reqBody.getMobile());
        }
        if (StringUtils.isNotEmpty(reqBody.getRealname())) {
            userProfile.setRealname(reqBody.getRealname());
        }
        if (StringUtils.isNotEmpty(reqBody.getPasswordHash())) {
            userProfile.setPasswordHash(passwordHash(reqBody.getPasswordHash()));
        }
        if (StringUtils.isNotEmpty(reqBody.getPasswordCoin())) {
            userProfile.setPasswordCoin(passwordHash(reqBody.getPasswordCoin()));
        }
        if (StringUtils.isNotEmpty(reqBody.getAreaCode())) {
            userProfile.setAreaCode(reqBody.getAreaCode());
        }
        userProfile.setUpdatedAt(current);
        if (Objects.nonNull(reqBody.getExtraCode())) {
            userProfile.setExtraCode(reqBody.getExtraCode());
        }
        if (Objects.nonNull(reqBody.getExtraCodeRule())) {
            userProfile.setExtraCodeRule(reqBody.getExtraCodeRule());
        }
        userProfileServiceImpl.update(userProfile, new LambdaQueryWrapper<UserProfile>()
                .eq(UserProfile::getUid, reqBody.getUid()));
        // 更新缓存
        userCache.updateUserInfoById(reqBody.getUid());
        userCache.updateUserFlag(reqBody.getUid());
        userCache.delInvalidLoginTimes(userById.getUsername());
        userCache.updateTestUidListCache();
        return true;
    }

    @Override
    public List<ListLevelResBody> listLevel() {
        List<UserLevel> list = userLevelServiceImpl.list();
        List<ListLevelResBody> resBodyList = BeanConvertUtils.copyListProperties(list, ListLevelResBody::new,
                (source, target) -> target.setName(getLevelTextByLevelId((source.getId())))
        );
        for (ListLevelResBody entity : resBodyList) {
            QueryWrapper<User> where = Wrappers.query();
            where.select(" ifnull(sum(coin),0) as totalCoin,count(id) as totalCount");
            where.eq("level_id", entity.getId());
            Map<String, Object> coinObj = userServiceImpl.getMap(where);
            if (null != coinObj) {
                // 账号总金额
                Object totalCoin = coinObj.get("totalCoin");
                // 会员人数
                Object totalCount = coinObj.get("totalCount");
                if (null != totalCoin) {
                    entity.setTotalCoin(new BigDecimal(totalCoin.toString()));
                }
                if (null != totalCount) {
                    entity.setTotalCount(Integer.parseInt(totalCount.toString()));
                }
            }
        }
        return resBodyList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateLevel(UpdateLevelReqBody reqBody) {
        Integer currentTime = DateUtils.getCurrentTime();
        UserLevel userLevel = getUserLevel(reqBody);
        userLevel.setUpdatedAt(currentTime);
        userLevelServiceImpl.update(userLevel, new LambdaQueryWrapper<UserLevel>()
                .eq(UserLevel::getId, reqBody.getId()));
        jedisUtil.del(KeyConstant.USER_LEVEL_ID_LIST_HASH);
        // 更新会员等级返水
        updateUserLevelRebate(getUserLevelRebate(reqBody, currentTime));
        return true;
    }

    /**
     * 更新会员等级返水
     *
     * @param list 待更新集合
     */
    private void updateUserLevelRebate(List<UserLevelRebate> list) {
        if (Optional.ofNullable(list).isPresent() && !list.isEmpty()) {
            for (UserLevelRebate entity : list) {
                LambdaUpdateWrapper<UserLevelRebate> updateWrapper = Wrappers.lambdaUpdate();
                updateWrapper.eq(UserLevelRebate::getLevelId, entity.getLevelId());
                updateWrapper.eq(UserLevelRebate::getGroupId, entity.getGroupId());
                userLevelRebateServiceImpl.saveOrUpdate(entity, updateWrapper);
                gameCache.updateGroupGameListCache();

            }
        }
    }

    @NotNull
    private List<UserLevelRebate> getUserLevelRebate(UpdateLevelReqBody reqBody, Integer currentTime) {
        BigDecimal percent100 = new BigDecimal(100);
        List<UserLevelRebate> list = new ArrayList<>();
        BiConsumer<Integer, BigDecimal> biFunction = (groupId, rebate) -> {
            UserLevelRebate userLevelRebate = new UserLevelRebate();
            userLevelRebate.setLevelId(reqBody.getId());
            userLevelRebate.setGroupId(groupId);
            userLevelRebate.setRebateRate(rebate);
            userLevelRebate.setUpdatedAt(currentTime);
            userLevelRebate.setCreatedAt(currentTime);
            userLevelRebate.setStatus(1);
            list.add(userLevelRebate);
        };
        biFunction.accept(1, reqBody.getSports().divide(percent100));
        biFunction.accept(2, reqBody.getEGames().divide(percent100));
        biFunction.accept(3, reqBody.getLivesGame().divide(percent100));
        biFunction.accept(4, reqBody.getFinishGame().divide(percent100));
        biFunction.accept(5, reqBody.getChess().divide(percent100));
        return list;
    }

    @NotNull
    private UserLevel getUserLevel(UpdateLevelReqBody reqBody) {
        UserLevel userLevel = new UserLevel();
        if (null != reqBody.getScoreUpgrade()) {
            userLevel.setScoreUpgrade(reqBody.getScoreUpgrade());
        }
        if (null != reqBody.getScoreRelegation()) {
            userLevel.setScoreRelegation(reqBody.getScoreRelegation());
        }
        if (null != reqBody.getRewardsUpgrade()) {
            userLevel.setRewardsUpgrade(reqBody.getRewardsUpgrade());
        }
        if (null != reqBody.getRewardsBirthday()) {
            userLevel.setRewardsBirthday(reqBody.getRewardsBirthday());
        }
        if (null != reqBody.getWithdrawalNums()) {
            userLevel.setWithdrawalNums(reqBody.getWithdrawalNums());
        }
        if (null != reqBody.getWithdrawalTotalCoin()) {
            userLevel.setWithdrawalTotalCoin(reqBody.getWithdrawalTotalCoin());
        }
        return userLevel;
    }

    @Override
    public List<ListFlagResBody> listFlag() {
        return baseMapper.listFlag();
    }

    @Override
    public boolean updateFlagStatus(UpdateFlagStatusReqBody reqBody) {
        UserFlag userFlag = new UserFlag();
        userFlag.setStatus(reqBody.getStatus());
        userFlag.setUpdatedAt(DateNewUtils.now());
        boolean flag = userFlagServiceImpl.update(userFlag, new LambdaQueryWrapper<UserFlag>()
                .eq(UserFlag::getId, reqBody.getId()));
        if (flag) {
            userCache.updateUserFlagList();
        }
        return flag;
    }

    /**
     * 会员管理-稽核明细-详情
     *
     * @param reqPage@return
     */
    @Override
    public ResPage<AuditDetailsResBody> auditDetails(ReqPage<CodeUidReqBody> reqPage) {
        Page<CodeAudit> page = codeAuditServiceImpl.page(reqPage.getPage(),
                new LambdaQueryWrapper<CodeAudit>().eq(CodeAudit::getUid, reqPage.getData().getUid()));
        Page<AuditDetailsResBody> returnPage = BeanConvertUtils.copyPageProperties(page, AuditDetailsResBody::new);
        return ResPage.get(returnPage);
    }

    /**
     * 会员管理-打码量明细-详情
     *
     * @param reqPage@return
     */
    @Override
    public CodeDetailsResBody codeDetails(ReqPage<CodeUidReqBody> reqPage) {
        var resDto = new CodeDetailsResBody();
        var uid = reqPage.getData().getUid();
        var page = codeRecordsServiceImpl.page(reqPage.getPage(),
                new LambdaQueryWrapper<CodeRecords>().eq(CodeRecords::getUid, uid));
        var returnPage = BeanConvertUtils.copyPageProperties(page, CodeListResBody::new);
        //计算开始时间
        var endTime = DateNewUtils.now();
        var startCodeAudit = codeAuditServiceImpl.getOne(new LambdaQueryWrapper<CodeAudit>()
                .eq(CodeAudit::getUid, uid)
                .lt(CodeAudit::getCreatedAt, endTime)
                //状态：1：稽核成功，2：稽核失败
                .eq(CodeAudit::getStatus, 1)
                .orderByDesc(CodeAudit::getCreatedAt), false);
        var startTime = Objects.nonNull(startCodeAudit) ? startCodeAudit.getCreatedAt() : 0;
        var pair = auditBase.getCodeRecords(uid, startTime, endTime, false);
        var codeRequire = pair.getRight().stream().map(CodeRecords::getCodeRequire)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
        var extraCode = Optional.ofNullable(userCache.getUserInfoById(uid))
                .map(UserCacheBo.UserCacheInfo::getExtraCode)
                .orElse(BigDecimal.ZERO);
        resDto.setCodeReal(pair.getLeft());
        resDto.setCodeRequire(codeRequire);
        resDto.setCodeListResDtoList(ResPage.get(returnPage));
        resDto.setExtraCode(extraCode);
        return resDto;
    }


    @Override
    public boolean updateFlag(UpdateFlagReqBody reqBody) {
        UserFlag userFlag = new UserFlag();
        userFlag.setIcon(reqBody.getIcon());
        userFlag.setIconColor(reqBody.getIconColor());
        userFlag.setName(reqBody.getName());
        userFlag.setUpdatedAt(DateNewUtils.now());
        boolean flag = userFlagServiceImpl.update(userFlag, new LambdaQueryWrapper<UserFlag>()
                .eq(UserFlag::getId, reqBody.getId()));
        if (flag) {
            userCache.updateUserFlagList();
        }
        return flag;
    }

    @Override
    public ResPage<ListFlagUsedResBody> listFlagUsed(ReqPage<ListFlagUsedReqBody> reqBody) {
        if (null == reqBody) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        ListFlagUsedReqBody data = reqBody.getData();
        if (null == data) {
            data = new ListFlagUsedReqBody();
        }
        Integer bitCode = data.getBitCode();
        if (null == data.getBitCode()) {
            // 全部bitCode
            QueryWrapper<UserFlag> where = Wrappers.query();
            where.select("sum(bit_code) as bitCode");
            UserFlag userFlag = userFlagServiceImpl.getOne(where);
            if (null != userFlag) {
                bitCode = userFlag.getBitCode();
            }
        }
        data.setBitCode(bitCode);

        Page<ListFlagUsedPO> page = baseMapper.listFlagUsed(reqBody.getPage(), data);
        Page<ListFlagUsedResBody> tmpPage = BeanConvertUtils.copyPageProperties(page, ListFlagUsedResBody::new);
        ResPage<ListFlagUsedResBody> resPage = ResPage.get(tmpPage);
        List<ListFlagUsedResBody> resList = new ArrayList<>();
        if (!page.getRecords().isEmpty()) {
            List<ListFlagUsedPO> listFlagUsedPOList = page.getRecords();
            for (ListFlagUsedPO po : listFlagUsedPOList) {
                if (null != po) {
                    List<UserCacheBo.UserFlagInfo> userFlagInfoList = getUserFlagInfoList(po);
                    ListFlagUsedResBody listFlagUsedResBody = new ListFlagUsedResBody();
                    listFlagUsedResBody.setUid(po.getUid());
                    listFlagUsedResBody.setUsername(po.getUsername());
                    listFlagUsedResBody.setUserFlagList(userFlagInfoList);
                    resList.add(listFlagUsedResBody);
                }
            }
        }
        resPage.setList(resList);
        return resPage;
    }

    @NotNull
    private List<UserCacheBo.UserFlagInfo> getUserFlagInfoList(ListFlagUsedPO po) {
        List<UserCacheBo.UserFlagInfo> userFlagInfoList = new ArrayList<>();
        String name = po.getName();
        String icon = po.getIcon();
        String iconColor = po.getIconColor();
        String bitCode = po.getBitCode();
        if (StringUtils.isNotBlank(name)) {
            String[] names = name.split(",");
            String[] icons = icon.split(",");
            String[] iconColors = iconColor.split(",");
            String[] bitCodes = bitCode.split(",");
            if (names.length > 0) {
                for (int i = 0; i < names.length; i++) {
                    UserCacheBo.UserFlagInfo userFlagInfo = new UserCacheBo.UserFlagInfo();
                    userFlagInfo.setIcon(icons[i]);
                    userFlagInfo.setIconColor(iconColors[i]);
                    userFlagInfo.setName(names[i]);
                    userFlagInfo.setBitCode(Integer.parseInt(bitCodes[i]));
                    userFlagInfoList.add(userFlagInfo);
                }
            }
        }
        return userFlagInfoList;
    }

    @Override
    @Transactional
    public boolean delUserFlag(DelUserFlagReqBody reqBody) {
        List<Integer> list = reqBody.getUidList();
        List<User> userList = new ArrayList<>();
        for (Integer uid : list) {
            User user = new User();
            user.setId(uid);
            user.setFlag(0);
            userList.add(user);
        }
        userServiceImpl.updateBatchById(userList);
        list.forEach(uid -> {
            userCache.updateUserInfoById(uid);
            userCache.updateUserFlag(uid);
        });
        return true;
    }

    @Override
    public boolean addUserFlag(AddUserFlagReqBody reqBody) {
        User user = new User();
        user.setFlag(reqBody.getBitCode());
        user.setUpdatedAt(DateUtils.getCurrentTime());
        userServiceImpl.update(user, new LambdaQueryWrapper<User>()
                .eq(User::getId, reqBody.getUid()));

        userCache.updateUserInfoById(reqBody.getUid());
        userCache.updateUserFlag(reqBody.getUid());
        return true;
    }

    @Override
    public List<UserFlagDict> userFlagDict() {
        LambdaQueryWrapper<UserFlag> where = Wrappers.lambdaQuery();
        where.eq(UserFlag::getStatus, 1);
        return BeanConvertUtils.copyListProperties(userFlagServiceImpl.list(where), UserFlagDict::new);
    }

    @Override
    public List<Integer> listUserFlag(ListUserFlagReqBody reqBody) {
        Integer uid = reqBody.getUid();
        LambdaQueryWrapper<User> userWhere = Wrappers.lambdaQuery();
        userWhere.select(User::getFlag);
        userWhere.eq(User::getId, uid);
        User user = userServiceImpl.getOne(userWhere);
        if (null == user) {
            throw new BusinessException(CodeInfo.USER_NOT_EXISTS);
        }
        List<Integer> resultList = baseMapper.listUserFlag(user.getFlag());
        if (null == resultList) {
            resultList = new ArrayList<>();
        }
        return resultList;
    }

    @Override
    public ListOnlineCountResBody listOnlineCount(ListOnlineReqBody reqBody) {
        int count = 0;
        ListOnlineCountResBody resBody = new ListOnlineCountResBody();
        LambdaQueryWrapper<UserProfile> where = whereUserPro(reqBody.getUid());
        List<UserProfile> list = userProfileServiceImpl.list(where);
        if (Optional.ofNullable(list).isPresent() && !list.isEmpty()) {
            List<Integer> uidList = list.stream().map(UserProfile::getUid).collect(Collectors.toList());
            count = userServiceImpl.lambdaQuery().in(User::getRole, 0, 1).in(User::getId, uidList).count();
        }
        resBody.setCount(count);
        resBody.setUid(reqBody.getUid());
        return resBody;
    }

    @Override
    public ResPage<ListOnlineResBody> listChild(ListOnlineReqBody reqBody) {
        ResPage<ListOnlineResBody> resPage = new ResPage<>();
        LambdaQueryWrapper<UserProfile> where = whereUserProSup1(reqBody.getUid());
        Page<UserProfile> page = userProfileServiceImpl.page(reqBody.getPage(), where);
        if (page.getRecords().isEmpty()) {
            return resPage;
        }
        Page<ListOnlineResBody> tmpPage = BeanConvertUtils.copyPageProperties(page, ListOnlineResBody::new);
        resPage = ResPage.get(tmpPage);
        resPage.getList().stream().parallel().forEach(s -> {
            DetailReqBody req = new DetailReqBody();
            req.setUid(s.getUid());
            DetailResBody detail = baseMapper.getDetail(req);
            if (null != detail) {
                BeanConvertUtils.beanCopy(detail, s);
                s.setUserFlagList(userCache.getUserFlagList(s.getUid()));
            }
        });
        return resPage;
    }

    @Override
    public LevelDetailResBody levelDetail(JSONObject reqBody) {
        if (null == reqBody) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        Integer id = reqBody.getInteger("id");
        UserLevel userLevel = userLevelServiceImpl.getById(id);
        LevelDetailResBody result = BeanConvertUtils.beanCopy(userLevel, LevelDetailResBody::new);
        List<UserLevelRebate> userLevelRebates = userLevelRebateServiceImpl.lambdaQuery()
                .eq(UserLevelRebate::getLevelId, userLevel.getId())
                .eq(UserLevelRebate::getStatus, 1)
                .list();
        BigDecimal percent100 = new BigDecimal(100);
        for (UserLevelRebate userGroup : userLevelRebates) {
            var groupId = userGroup.getGroupId();
            switch (groupId) {
                case 1:
                    result.setSports(userGroup.getRebateRate().multiply(percent100));
                    break;
                case 2:
                    result.setEGames(userGroup.getRebateRate().multiply(percent100));
                    break;
                case 3:
                    result.setLivesGame(userGroup.getRebateRate().multiply(percent100));
                    break;
                case 4:
                    result.setFinishGame(userGroup.getRebateRate().multiply(percent100));
                    break;
                case 5:
                    result.setChess(userGroup.getRebateRate().multiply(percent100));
                    break;
                default:
                    break;
            }
        }
        return result;
    }

    @Override
    public ResPage<ListCodeRecords> listCodeRecords(ListCodeRecordsReqBody reqBody) {
        ResPage<ListCodeRecords> resPage = new ResPage<>();
        LambdaQueryWrapper<CodeRecords> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(CodeRecords::getUid, reqBody.getUid());
        Page<CodeRecords> page = codeRecordsServiceImpl.page(reqBody.getPage(), wrapper);
        Page<ListCodeRecords> target = BeanConvertUtils.copyPageProperties(page, ListCodeRecords::new);
        if (target.getRecords().isEmpty()) {
            return resPage;
        }
        for (ListCodeRecords records : target.getRecords()) {
            UserCacheBo.UserCacheInfo userCacheInfo = userCache.getUserInfoById(records.getUid());
            records.setUsername(userCacheInfo.getUsername());
        }
        return ResPage.get(target);
    }

    @Override
    public void clearCodeRecords(Integer uid) {
        if (null != uid) {
            LambdaQueryWrapper<CodeRecords> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(CodeRecords::getUid, uid);
            codeRecordsServiceImpl.remove(wrapper);
        }
    }


    /**
     * ---------------------------
     * uid  s1  s2  s3  s4  s5  s6
     * ---------------------------
     * 1    0   0   0   0   0   0
     * 2    1   0   0   0   0   0
     * 3    2   1   0   0   0   0
     * 4    3   2   1   0   0   0
     * 5    4   3   2   1   0   0
     * 6    5   4   3   2   1   0
     * 7    6   5   4   3   2   1
     * 8    3   2   1   0   0   0
     * <p>
     * 8    4   3   2   1   0   0
     * <p>
     * 5    2   1   0   0   0   0
     * <p>
     * 6    5   2   1   0   0   0
     * 7    6   5   2   1   0   0
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean routeTransfer(RouteTransferReqBody reqBody) {
        String source = reqBody.getUsernameSource();
        String target = reqBody.getUsernameTarget();
        if (StringUtils.isNotBlank(source) && source.length() > 0 && source.equals(target)) {
            throw new BusinessException(CodeInfo.ROUTE_TRANSFER_NOT_SAME);
        }
        User sourceUser = getUser(source);
        if (null == sourceUser) {
            throw new BusinessException(CodeInfo.ROUTE_TRANSFER_SOURCE_NOT_EXISTS);
        }
        User targetUser = getUser(target);
        if (null == targetUser) {
            throw new BusinessException(CodeInfo.ROUTE_TRANSFER_TARGET_NOT_EXISTS);
        }
        UserProfile userProfile = getUserProfile(sourceUser.getId());
        if (null == userProfile) {
            throw new BusinessException(CodeInfo.ROUTE_TRANSFER_SOURCE_NOT_EXISTS);
        }
        UserProfile targetUserProfile = getUserProfile(targetUser.getId());
        if (null == targetUserProfile) {
            throw new BusinessException(CodeInfo.ROUTE_TRANSFER_TARGET_NOT_EXISTS);
        }
        // 查找所有下级
        Integer sourceUID = sourceUser.getId();
        List<UserProfile> childList = userProfileServiceImpl.list(whereUserPro(sourceUID));
        // 上级不能往下级转换
        checkChildList(targetUserProfile, childList);
        userProfile.setSupUid1(targetUserProfile.getUid());
        userProfile.setSupUid2(targetUserProfile.getSupUid1());
        userProfile.setSupUid3(targetUserProfile.getSupUid2());
        userProfile.setSupUid4(targetUserProfile.getSupUid3());
        userProfile.setSupUid5(targetUserProfile.getSupUid4());
        userProfile.setSupUid6(targetUserProfile.getSupUid5());
        userProfile.setUpdatedAt(DateNewUtils.now());
        // 更新当前节点
        boolean updateFlag = userProfileServiceImpl.updateById(userProfile);
        if (updateFlag) {
            // 更新下级代理缓存->SupUid1
            userCache.updateUserSubordinateUidListHash(targetUserProfile.getUid());
        }
        if (!childList.isEmpty()) {
            for (UserProfile entity : childList) {
                if (Objects.nonNull(entity)) {
                    setUserProfileSup(userProfile, sourceUID, entity);
                }
            }
            // 更新子节点
            userProfileServiceImpl.updateBatchById(childList);
        }
        return true;
    }

    private void checkChildList(UserProfile targetUserProfile, List<UserProfile> childList) {
        if (!childList.isEmpty()) {
            for (UserProfile entity : childList) {
                if (null != entity && null != entity.getUid()) {
                    int uid = entity.getUid();
                    if (uid > 0 && uid == targetUserProfile.getUid()) {
                        throw new BusinessException(CodeInfo.ROUTE_TRANSFER_SUP_UN2_CHILD);
                    }
                }
            }
        }
    }

    private UserProfile getUserProfile(Integer uid) {
        return userProfileServiceImpl.getOne(new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUid, uid));
    }

    private boolean supUidEqSourceUID(int pid, int sourceUID) {
        return (pid > 0 && pid == sourceUID);
    }

    private void setUserProfileSup(UserProfile userProfile, Integer sourceUID, UserProfile entity) {
        int supUid1 = null != entity.getSupUid1() ? entity.getSupUid1().intValue() : 0;
        int supUid2 = null != entity.getSupUid2() ? entity.getSupUid2().intValue() : 0;
        int supUid3 = null != entity.getSupUid3() ? entity.getSupUid3().intValue() : 0;
        int supUid4 = null != entity.getSupUid4() ? entity.getSupUid4().intValue() : 0;
        int supUid5 = null != entity.getSupUid5() ? entity.getSupUid5().intValue() : 0;
        int supUid6 = null != entity.getSupUid6() ? entity.getSupUid6().intValue() : 0;
        if (supUidEqSourceUID(supUid1, sourceUID)) {
            supUid1 = sourceUID;
            supUid2 = userProfile.getSupUid1();
            supUid3 = userProfile.getSupUid2();
            supUid4 = userProfile.getSupUid3();
            supUid5 = userProfile.getSupUid4();
            supUid6 = userProfile.getSupUid5();
        } else if (supUidEqSourceUID(supUid2, sourceUID)) {
            supUid2 = sourceUID;
            supUid3 = userProfile.getSupUid1();
            supUid4 = userProfile.getSupUid2();
            supUid5 = userProfile.getSupUid3();
            supUid6 = userProfile.getSupUid4();
        } else if (supUidEqSourceUID(supUid3, sourceUID)) {
            supUid3 = sourceUID;
            supUid4 = userProfile.getSupUid1();
            supUid5 = userProfile.getSupUid2();
            supUid6 = userProfile.getSupUid3();
        } else if (supUidEqSourceUID(supUid4, sourceUID)) {
            supUid4 = sourceUID;
            supUid5 = userProfile.getSupUid1();
            supUid6 = userProfile.getSupUid2();
        } else if (supUidEqSourceUID(supUid5, sourceUID)) {
            supUid5 = sourceUID;
            supUid6 = userProfile.getSupUid1();
        } else if (supUidEqSourceUID(supUid6, sourceUID)) {
            supUid6 = sourceUID;
        }
        entity.setSupUid1(supUid1);
        entity.setSupUid2(supUid2);
        entity.setSupUid3(supUid3);
        entity.setSupUid4(supUid4);
        entity.setSupUid5(supUid5);
        entity.setSupUid6(supUid6);
    }

    private LambdaQueryWrapper<UserProfile> whereUserPro(Integer uid) {
        LambdaQueryWrapper<UserProfile> where = Wrappers.lambdaQuery();
        where.eq(UserProfile::getSupUid1, uid).or();
        where.eq(UserProfile::getSupUid2, uid).or();
        where.eq(UserProfile::getSupUid3, uid).or();
        where.eq(UserProfile::getSupUid4, uid).or();
        where.eq(UserProfile::getSupUid5, uid).or();
        where.eq(UserProfile::getSupUid6, uid);
        return where;
    }

    private LambdaQueryWrapper<UserProfile> whereUserProSup1(Integer uid) {
        LambdaQueryWrapper<UserProfile> where = Wrappers.lambdaQuery();
        where.eq(UserProfile::getSupUid1, uid);
        return where;
    }

    /**
     * get用户银行卡数
     *
     * @param uid
     * @return
     */
    private int getUserBankCount(Integer uid) {
        LambdaQueryWrapper<UserBank> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(UserBank::getUid, uid);
        wrapper.in(UserBank::getStatus, 1, 2);
        return userBankServiceImpl.count(wrapper);
    }

    /**
     * 根据用户名查询用户信息
     *
     * @param username
     * @return user
     */
    public User getUser(String username) {
        LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(User::getUsername, username);
        return userServiceImpl.getOne(wrapper);
    }

    @Override
    public void updateBatchLevel(UpdateBatchLevelReqBody reqBody) {
        Integer levelId = reqBody.getLevelId();
        List<Integer> uidList = reqBody.getUidList();
        if (null != levelId && !uidList.isEmpty()) {
            List<User> entityList = new ArrayList<>();
            Integer currentTime = DateUtils.getCurrentTime();
            for (Integer uid : uidList) {
                if (null != uid && uid > 0) {
                    User user = new User();
                    user.setUpdatedAt(currentTime);
                    user.setLevelId(levelId);
                    user.setId(uid);
                    entityList.add(user);
                }
            }
            userServiceImpl.updateBatchById(entityList);
            uidList.forEach(s -> userCache.updateUserInfoById(s));
            log.info("updateBatchLevel ok.." + entityList.size());
        }
    }

    @Override
    public ResPage<AgentCenterParameter.SmsCodeResDto> getVerifyCodeList(ReqPage<AgentCenterParameter.SmsCodeReqDto> reqBody) {
        List<AgentCenterParameter.SmsCodeResDto> verifyList = new ArrayList<>();
        List<List<AgentCenterParameter.SmsCodeResDto>> resultList = new ArrayList<>();
        var unaryOperator = userServiceBase.checkShow(Constant.MOBILE);
        if (reqBody.getData() != null && reqBody.getData().getNumber() != null) {
            var hget = jedisUtil.hget(KeyConstant.SMS_CODE_HASH, reqBody.getData().getNumber());
            if (hget != null) {
                List<AgentCenterParameter.SmsCodeDto> smsCodeDtos = parseArray(hget, AgentCenterParameter.SmsCodeDto.class);
                verifyList = BeanConvertUtils.copyListProperties(smsCodeDtos, AgentCenterParameter.SmsCodeResDto::new, (sb, bo) -> {
                    bo.setMobile(sb.getAreaCode() + unaryOperator.apply(sb.getMobile()));
                    bo.setCreatedAt((int) (sb.getCreateAt() / 1000));
                });
            }
        } else {
            List<String> hvals = jedisUtil.hvals(KeyConstant.SMS_CODE_HASH);
            if (!hvals.isEmpty()) {
                for (String val : hvals) {
                    List<AgentCenterParameter.SmsCodeDto> smsCodeDtos = parseArray(val, AgentCenterParameter.SmsCodeDto.class);
                    List<AgentCenterParameter.SmsCodeResDto> smsCodeResDtos = BeanConvertUtils.copyListProperties(smsCodeDtos, AgentCenterParameter.SmsCodeResDto::new, (sb, bo) -> {
                        bo.setMobile(sb.getAreaCode() + unaryOperator.apply(sb.getMobile()));
                        bo.setCreatedAt((int) (sb.getCreateAt() / 1000));
                    });
                    resultList.add(smsCodeResDtos);
                }
            }
            verifyList = resultList.stream().flatMap(Collection::stream)
                    .collect(Collectors.toList());
        }
        // 内存分页
        Page<AgentCenterParameter.SmsCodeResDto> page = new Page<>(reqBody.getCurrent(), reqBody.getSize(), verifyList.size());
        List<AgentCenterParameter.SmsCodeResDto> collect = verifyList.stream()
                .skip(page.getSize() * (page.getCurrent() - 1))
                .limit(page.getSize()).collect(Collectors.toList());
        collect = reqBody.getSortKey() != null && !reqBody.getSortKey().equals("ASC") ?
                collect.stream().sorted(Comparator.comparing(AgentCenterParameter.SmsCodeResDto::getCreatedAt).reversed()).collect(Collectors.toList())
                : collect.stream().sorted(Comparator.comparing(AgentCenterParameter.SmsCodeResDto::getCreatedAt)).collect(Collectors.toList());
        page.setRecords(collect);
        return ResPage.get(page);
    }

    @Override
    public ResPage<OnlineUserCountListResBody> onlineUserCountList(ReqPage<OnlineUserCountListReqBody> reqBody) {
        if (null == reqBody) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        var onlineUidList = userChannelRelCache.getChannelUids();
        if (Optional.ofNullable(onlineUidList).isEmpty() || onlineUidList.isEmpty()) {
            return new ResPage();
        }

        OnlineUserCountListReqBody data = reqBody.getData();
        if (null == data) {
            data = new OnlineUserCountListReqBody();
        }
        if (StringUtils.isNotBlank(data.getUsername())) {
            var userInfo = userCache.getUserInfoByUserName(data.getUsername());
            if (null == userInfo || null == userInfo.getUid()) {
                return new ResPage();
            }
            onlineUidList = onlineUidList.stream().filter(o -> o.equals(userInfo.getUid())).collect(Collectors.toList());
        }
        if (Optional.ofNullable(onlineUidList).isEmpty() || onlineUidList.isEmpty()) {
            return new ResPage();
        }
        if (StringUtils.isNotBlank(data.getSupUsername())) {
            var userInfo = userCache.getUserInfoByUserName(data.getSupUsername());
            if (null == userInfo || null == userInfo.getUid()) {
                return new ResPage();
            }
            data.setSupUid1(userInfo.getUid());
        }
        data.setUidList(onlineUidList);


        var poPage = baseMapper.onlineUserCountList(reqBody.getPage(), data);
        if (Optional.ofNullable(poPage.getRecords()).isEmpty() || poPage.getRecords().isEmpty()) {
            return new ResPage();
        }
        var tmpPage = BeanConvertUtils.copyPageProperties(poPage, OnlineUserCountListResBody::new);
        var resPage = ResPage.get(tmpPage);
        var prefix = configCache.getPlatPrefix();
        List<String> userNameList = resPage.getList().stream().map(o -> prefix + o.getUsername()).collect(Collectors.toList());
        var futureUserCoinMap = getFutureUserCoinMap(userNameList, prefix);
        for (var o : resPage.getList()) {
            // get上级代理名称
            var supUserUid1Po = userCache.getUserInfoById(o.getSupUid1());
            o.setSupUsername(null != supUserUid1Po ? supUserUid1Po.getUsername() : null);
            UserCacheBo.UserCacheInfo userInfo = userCache.getUserInfoById(o.getUid());
            // 会员等级 -> vip1-乒乓球达人
            o.setLevelText(getLevelTextByLevelId(userInfo.getLevelId()));
            // 会员旗帜
            o.setUserFlagList(userCache.getUserFlagList(o.getUid()));
            if (null != futureUserCoinMap && futureUserCoinMap.containsKey(o.getUsername())) {
                o.setFuturesCoin(futureUserCoinMap.get(o.getUsername()));
            } else {
                o.setFuturesCoin(BigDecimal.ZERO);
            }
        }
        return resPage;
    }

    /**
     * 清除用户token与手机验证次数
     */
    @Override
    public Boolean clearTokenCode(ClearTokenCodeReqBody reqBody) {
        var uid = reqBody.getUid();
        var code = reqBody.getCode();
        if (Objects.nonNull(uid)) {
            var username = Optional.ofNullable(userCache.getUserInfoById(uid)).map(UserCacheBo.UserCacheInfo::getUsername).orElse("");
            jedisUtil.hdel(KeyConstant.USER_LOGIN_INVALID_TIMES, username);
        }
        if (Objects.nonNull(code)) {
            jedisUtil.hdel(KeyConstant.SMS_CODE_HASH, code);
        }
        return true;
    }

    /**
     * 获取Futures彩票余额列表
     *
     * @param userNameList 用户名列表
     * @return 获取Futures彩票用户余额列表
     */
    private Map<String, BigDecimal> getFutureUserCoinMap(List<String> userNameList, String prefix) {
        var getUserCoinListByUserNameListReqBody = GetUserCoinListByUserNameListReqBody.builder().usernameReqList(userNameList).build();
        // future彩票余额
        var futureCoinList = futuresLotteryServiceImpl.getUserCoinListByUserNameList(getUserCoinListByUserNameListReqBody);
        Map<String, BigDecimal> futureUserCoinMap = null;
        if (Optional.ofNullable(futureCoinList).isPresent()) {
            futureUserCoinMap = futureCoinList.stream().collect(Collectors.toMap(o -> o.getUsername().substring(prefix.length()), GetUserCoinListByUserNameListResBody::getCoin));
        }
        return futureUserCoinMap;
    }
}
