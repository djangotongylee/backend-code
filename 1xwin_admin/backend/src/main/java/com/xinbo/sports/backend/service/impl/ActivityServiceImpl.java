package com.xinbo.sports.backend.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinbo.sports.backend.base.DictionaryBase;
import com.xinbo.sports.backend.io.dto.PromotionsParameter.*;
import com.xinbo.sports.backend.redis.GameCache;
import com.xinbo.sports.backend.service.IActivityService;
import com.xinbo.sports.dao.generator.po.*;
import com.xinbo.sports.dao.generator.service.*;
import com.xinbo.sports.service.base.PromotionsBase;
import com.xinbo.sports.service.base.RegisterBonusPromotions;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.service.cache.redis.PromotionsCache;
import com.xinbo.sports.service.cache.redis.UserCache;
import com.xinbo.sports.service.common.Constant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.bo.UserCacheBo;
import com.xinbo.sports.service.io.dto.BaseParams;
import com.xinbo.sports.service.io.enums.BaseEnum;
import com.xinbo.sports.service.thread.ThreadHeaderLocalData;
import com.xinbo.sports.utils.BeanConvertUtils;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.DateUtils;
import com.xinbo.sports.utils.I18nUtils;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.groovy.util.Maps;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.alibaba.fastjson.JSON.parseObject;
import static java.util.Objects.nonNull;

/**
 * @author: wells
 * @date: 2020/6/5
 * @description:活动配置
 */
@Slf4j
@Service
public class ActivityServiceImpl implements IActivityService {
    //开始时间
    private static final String CREATED_AT = "created_at";
    //流水倍数
    private static final String FLOW_CLAIM = "flowClaim";
    @Autowired
    private PromotionsGroupService promotionsGroupServiceImpl;
    @Autowired
    private PromotionsService promotionsServiceImpl;
    @Autowired
    private CoinRewardsService coinRewardsServiceImpl;
    @Autowired
    private UserSignService userSignServiceImpl;
    @Autowired
    private CoinRebateService coinRebateServiceImpl;
    @Autowired
    private GiftListService giftListServiceImpl;
    @Autowired
    private GiftRecordsService giftRecordsServiceImpl;
    @Autowired
    private UserService userServiceImpl;
    @Autowired
    private PromotionsCache promotionsCache;
    @Autowired
    private CoinRewardsInviteService coinRewardsInviteServiceImpl;
    @Autowired
    private UserCache userCache;
    @Autowired
    private GameCache gameCache;
    @Resource
    private ConfigCache configCache;
    @Resource
    private DictionaryBase dictionaryBase;
    @Resource
    private RegisterRewardsService registerRewardsServiceImpl;
    @Resource
    private RegisterBonusPromotions registerBonusPromotions;
    @Resource
    private PromotionsBase promotionsBase;

    /***
     * 活动组
     * @return 响应实体类 活动列表
     */
    @Override
    public List<PromotionsGroupResDto> promotionsGroup() {
        List<PromotionsGroup> promotionsGroupList = promotionsGroupServiceImpl.lambdaQuery()
                .eq(PromotionsGroup::getStatus, 1).list();
        if (CollectionUtils.isEmpty(promotionsGroupList)) {
            throw new BusinessException(CodeInfo.NO_ACTIVE_GROUP_RECORD);
        }
        List<Promotions> promotionsList = promotionsCache.getPromotionsListCache();
        if (CollectionUtils.isEmpty(promotionsList)) {
            throw new BusinessException(CodeInfo.NO_ACTIVE_RECORD);
        }
        Map<Integer, List<Promotions>> listMap = promotionsList.stream().collect(Collectors.groupingBy(Promotions::getCategory));
        var promotionsGroupResDtoList = new ArrayList<PromotionsGroupResDto>();
        for (var promotionsGroup : promotionsGroupList) {
            var promotionsGroupResDto = new PromotionsGroupResDto();
            promotionsGroupResDto.setGroupId(promotionsGroup.getId());
            promotionsGroupResDto.setGroupCodeZh(promotionsGroup.getCodeZh());
            var proList = listMap.get(promotionsGroupResDto.getGroupId());
            if (!CollectionUtils.isEmpty(proList)) {
                var proResDtoList = BeanConvertUtils.copyListProperties(proList, PromotionsResDto::new);
                promotionsGroupResDto.setPromotionsList(proResDtoList);
                promotionsGroupResDtoList.add(promotionsGroupResDto);
            }
        }
        return promotionsGroupResDtoList;
    }


    /**
     * 活动列表
     *
     * @param reqDto 请求实体类
     * @return 响应实体类
     */
    @Override
    public ResPage<ListResDto> promotionsList(ReqPage<ListReqDto> reqDto) {
        var listReqDto = reqDto.getData();
        var lang = Optional.ofNullable(listReqDto.getLang()).orElse(BaseEnum.LANG.EN.getValue());
        var staticServer = configCache.getStaticServer();
        var lambdaQueryWrapper = new LambdaQueryWrapper<Promotions>()
                //活动标题
                .like(nonNull(listReqDto.getCodeZh()), Promotions::getCodeZh, listReqDto.getCodeZh())
                //活动类型
                .eq(nonNull(listReqDto.getCategory()), Promotions::getCategory, listReqDto.getCategory())
                //状态.ge(
                .eq(nonNull(listReqDto.getStatus()), Promotions::getStatus, listReqDto.getStatus())
                //开始时间
                .ge(nonNull(listReqDto.getStartTime()), Promotions::getStartedAt, listReqDto.getStartTime())
                //结束时间
                .le(nonNull(listReqDto.getEndTime()), Promotions::getEndedAt, listReqDto.getEndTime());
        var page = promotionsServiceImpl.page(reqDto.getPage(), lambdaQueryWrapper);
        var resPage = BeanConvertUtils.copyPageProperties(page, ListResDto::new, (source, listResDto) -> {
                    listResDto.setCodeZh(parseObject(source.getCodeZh()).getString(lang));
                    listResDto.setImg(parseObject(source.getImg()).getString(lang).startsWith("http")?parseObject(source.getImg()).getString(lang):staticServer + parseObject(source.getImg()).getString(lang));
                    listResDto.setDescription(parseObject(source.getDescript()).getString(lang));
                    listResDto.setFlowClaim(parseObject(source.getInfo()).getInteger(FLOW_CLAIM));
                }
        );
        return ResPage.get(resPage);
    }

    /**
     * 修改活动
     *
     * @param reqDto 请求参数实体类
     * @return 响应实体类
     */
    @Override
    public boolean saveOrUpdatePromotions(SavaOrUpdateReqDto reqDto) {
        var promotions = BeanConvertUtils.copyProperties(reqDto, Promotions::new, (source, promo) -> {
            promo.setDescript(source.getDescription());
            promo.setStartedAt(source.getStartTime());
            promo.setEndedAt(source.getEndTime());
        });
        var now = DateUtils.getCurrentTime();
        promotions.setUpdatedAt(now);
        var staticServer = configCache.getStaticServer();
        var returnFlag = false;
        //新增重复判断
        if (reqDto.getId() == null) {
            var codeCount = promotionsServiceImpl.lambdaQuery().eq(Promotions::getCode, reqDto.getCode()).count();
            if (codeCount > 0) {
                throw new BusinessException(CodeInfo.ACTIVE_CODE_REPEAT);
            }
            promotions.setCreatedAt(now);
            BinaryOperator<String> function = (key, value) -> {
                var json = new JSONObject();
                json.put(key, value);
                return json.toJSONString();
            };
            promotions.setCodeZh(function.apply(reqDto.getLang(), reqDto.getCodeZh()));
            promotions.setImg(function.apply(reqDto.getLang(), reqDto.getImg()));
            promotions.setDescript(function.apply(reqDto.getLang(), reqDto.getDescription()));
            promotions.setInfo(function.apply(FLOW_CLAIM, String.valueOf(Optional.ofNullable(reqDto.getFlowClaim()).orElse(0))));
            returnFlag = promotionsServiceImpl.save(promotions);
            //修改重复判断
        } else {
            var codeZhCount = promotionsServiceImpl.lambdaQuery()
                    .like(Promotions::getCodeZh, reqDto.getCodeZh())
                    .ne(Promotions::getId, reqDto.getId())
                    .count();
            if (codeZhCount > 0) {
                throw new BusinessException(CodeInfo.ACTIVE_CODE_REPEAT);
            }
            var originPromotions = promotionsServiceImpl.getById(reqDto.getId());
            var codeZhJson = parseObject(originPromotions.getCodeZh());
            codeZhJson.put(reqDto.getLang(), reqDto.getCodeZh());
            var imgJson = parseObject(originPromotions.getImg());
            imgJson.put(reqDto.getLang(), reqDto.getImg());
            var descriptJson = parseObject(originPromotions.getDescript());
            descriptJson.put(reqDto.getLang(), reqDto.getDescription());
            var infoJson = parseObject(originPromotions.getInfo());
            infoJson.put(FLOW_CLAIM, Optional.ofNullable(reqDto.getFlowClaim()).orElse(0));
            promotions.setImg(JSON.toJSONString(imgJson).replace(staticServer, ""));
            promotions.setCodeZh(JSON.toJSONString(codeZhJson));
            promotions.setDescript(JSON.toJSONString(descriptJson));
            promotions.setInfo(JSON.toJSONString(infoJson));
            returnFlag = promotionsServiceImpl.updateById(promotions);
        }
        promotionsCache.refreshPromotions();
        return returnFlag;
    }

    /**
     * 删除活动
     *
     * @param reqDto 请求参数实体类
     * @return 响应实体类
     */
    @Override
    public boolean deletePromotions(DeleteReqDto reqDto) {
        boolean b = promotionsServiceImpl.removeById(reqDto.getId());
        promotionsCache.refreshPromotions();
        return b;
    }

    /**
     * 签到列表
     *
     * @param reqDto 请求参数实体类
     * @return 响应实体类
     */
    @Override
    public SingListResDto signList(ReqPage<SingListReqDto> reqDto) {
        var singListReqDto = reqDto.getData();
        var wrapper = new QueryWrapper<CoinRewards>()
                .select("uid", "username", "ifNull(sum(coin),0) as coin", "count(refer_id) as referId")
                //UID
                .eq(nonNull(singListReqDto.getUid()), "uid", singListReqDto.getUid())
                //用户名
                .eq(nonNull(singListReqDto.getUsername()), "username", singListReqDto.getUsername())
                //开始时间
                .ge(nonNull(singListReqDto.getStartTime()), CREATED_AT, singListReqDto.getStartTime())
                //结束时间
                .le(nonNull(singListReqDto.getEndTime()), CREATED_AT, singListReqDto.getEndTime())
                //活动类型签到
                .eq("refer_id", Constant.SIGN_IN_BONUS)
                //UID分组
                .groupBy("uid", "username");
        List<CoinRewards> totalOne = coinRewardsServiceImpl.list(wrapper);
        //总派彩金额
        var totalRewardsCoin = CollectionUtils.isEmpty(totalOne) ? BigDecimal.ZERO :
                totalOne.stream().map(CoinRewards::getCoin).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        //列表数据
        Page<CoinRewards> page = coinRewardsServiceImpl.page(reqDto.getPage(), wrapper);
        var resPage = BeanConvertUtils.copyPageProperties(page, SingList::new, (source, singList) -> {
            singList.setSignDay(source.getReferId());
            singList.setReceiveCoin(source.getCoin());
        });
        return SingListResDto.builder().totalRewardsCoin(totalRewardsCoin).singList(ResPage.get(resPage)).build();
    }

    /**
     * 签到详情
     *
     * @param reqDto 请求参数实体类
     * @return 响应实体类
     */
    @Override
    public ResPage<SingDetailResDto> signDetail(ReqPage<SingDetailReqDto> reqDto) {
        Map<String, String> dayOfWeekMap = Maps.of(
                "1", "星期一",
                "2", "星期二",
                "3", "星期三",
                "4", "星期四",
                "5", "星期五",
                "6", "星期六",
                "7", "星期天");
        var singDetailReqDto = reqDto.getData();
        var lambdaQueryWrapper = new LambdaQueryWrapper<UserSign>()
                //UID
                .eq(nonNull(singDetailReqDto.getUid()), UserSign::getUid, singDetailReqDto.getUid())
                //年份
                .eq(nonNull(singDetailReqDto.getYear()), UserSign::getYear, singDetailReqDto.getYear())
                //周数
                .eq(nonNull(singDetailReqDto.getNw()), UserSign::getNw, singDetailReqDto.getNw());
        Page<UserSign> page = userSignServiceImpl.page(reqDto.getPage(), lambdaQueryWrapper);
        var resPage = BeanConvertUtils.copyPageProperties(page, SingDetailResDto::new,
                (source, singDetailResDto) -> {
                    UserCacheBo.UserCacheInfo userCacheInfo = userCache.getUserInfoById(source.getUid());
                    var userName = userCacheInfo != null ? userCacheInfo.getUsername() : "";
                    singDetailResDto.setUserName(userName);
                    //已签到的天转换对应的星期数
                    var day = Strings.isEmpty(source.getDay()) ? "" : source.getDay();
                    var dayArr = day.split(",");
                    var dayList = Arrays.stream(dayArr).map(x ->
                            I18nUtils.getLocaleMessage(dayOfWeekMap.getOrDefault(x, "")))
                            .collect(Collectors.toList());
                    var returnDay = StringUtils.join(dayList, ",");
                    singDetailResDto.setDayOfWeek(returnDay);
                });
        return ResPage.get(resPage);
    }


    /**
     * 首充活动列表
     *
     * @param reqDto 请求参数实体类
     * @return 响应实体类
     */
    @Override
    public FirstDepositResDto firstDepositList(ReqPage<RewardsReqDto> reqDto) {
        var pair = getCoinRewardPage(reqDto);
        var page = pair.getRight();
        var proMap = promotionsCache.getPromotionsListCache().stream().collect(Collectors.toMap(Promotions::getId, Promotions::getCodeZh));
        var resPage = BeanConvertUtils.copyPageProperties(page, RewardsList::new, (source, rewardsList) -> {
            rewardsList.setCodeZh(proMap.getOrDefault(rewardsList.getReferId(), ""));
            var info = source.getInfo();
            if (Strings.isNotEmpty(info)) {
                //获取充值金额、充值类型、充值方式
                var jsonObject = parseObject(info);
                rewardsList.setDepositCoin(jsonObject.getBigDecimal("depositCoin"));
                rewardsList.setDepositType(jsonObject.getInteger("depositType"));
                rewardsList.setDepositCategory(jsonObject.getInteger("depositCategory"));
            }
        });
        return FirstDepositResDto.builder().totalRewardsCoin(pair.getLeft()).rewardsList(ResPage.get(resPage)).build();
    }

    /**
     * 体育与真人列表
     *
     * @param reqDto 请求参数实体类
     * @return 响应实体类
     */
    @Override
    public SportAndLiveResDto sportsAndLiveList(@Valid ReqPage<RewardsReqDto> reqDto) {
        var pair = getCoinRewardPage(reqDto);
        var proMap = promotionsCache.getPromotionsListCache().stream().collect(Collectors.toMap(Promotions::getId, Promotions::getCodeZh));
        var resPage = BeanConvertUtils.copyPageProperties(pair.getRight(), SportsAndLiveList::new, (source, sportsAndLiveList) -> {
            sportsAndLiveList.setCodeZh(proMap.getOrDefault(sportsAndLiveList.getReferId(), ""));
            var info = source.getInfo();
            if (Strings.isNotEmpty(info)) {
                //获取游戏名称，下注金额，输赢金额
                var jsonObject = parseObject(info);
                sportsAndLiveList.setGameName(jsonObject.getString("gameName"));
                sportsAndLiveList.setGames(jsonObject.getInteger("games"));
                sportsAndLiveList.setBetCoin(jsonObject.getBigDecimal("betCoin"));
                sportsAndLiveList.setWinCoin(jsonObject.getBigDecimal("winCoin"));
            }
        });
        return SportAndLiveResDto.builder().totalRewardsCoin(pair.getLeft()).sportAndLiveList(ResPage.get(resPage)).build();
    }

    /**
     * 红包雨列表
     *
     * @param reqDto 请求参数实体类
     * @return 响应实体类
     */
    @Override
    public RedEnvelopeWarListResDto redEnvelopeWarList(ReqPage<RewardsReqDto> reqDto) {
        var pair = getCoinRewardPage(reqDto);
        var proMap = promotionsCache.getPromotionsListCache().stream().collect(Collectors.toMap(Promotions::getId, Promotions::getCodeZh));
        var resPage = BeanConvertUtils.copyPageProperties(pair.getRight(), RedEnvelopeWarResDto::new, (source, redEnvelopeWarResDto) ->
                redEnvelopeWarResDto.setCodeZh(proMap.getOrDefault(redEnvelopeWarResDto.getReferId(), ""))
        );
        return RedEnvelopeWarListResDto.builder().totalRewardsCoin(pair.getLeft()).redWarList(ResPage.get(resPage)).build();
    }

    /**
     * 全场返水列表
     *
     * @param reqDto 请求参数实体类
     * @return 响应实体类
     */
    @Override
    public AllRebateListResDto allRebateList(ReqPage<AllRebateListReqDto> reqDto) {
        var allRebateListReqDto = reqDto.getData();
        var wrapper = new LambdaQueryWrapper<CoinRebate>()
                //UID
                .eq(nonNull(allRebateListReqDto.getUid()), CoinRebate::getUid, allRebateListReqDto.getUid())
                //用户名
                .eq(nonNull(allRebateListReqDto.getUsername()), CoinRebate::getUsername, allRebateListReqDto.getUsername())
                //关联ID
                .eq(nonNull(allRebateListReqDto.getId()), CoinRebate::getId, allRebateListReqDto.getId())
                //开始时间
                .ge(nonNull(allRebateListReqDto.getStartTime()), CoinRebate::getCreatedAt, allRebateListReqDto.getStartTime())
                //结束时
                .le(nonNull(allRebateListReqDto.getEndTime()), CoinRebate::getCreatedAt, allRebateListReqDto.getEndTime());
        //求领取彩金
        List<CoinRebate> totalOne = coinRebateServiceImpl.list(wrapper);
        var totalRewardsCoin = CollectionUtils.isEmpty(totalOne) ? BigDecimal.ZERO :
                totalOne.stream().map(CoinRebate::getCoin).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        Page<CoinRebate> page = coinRebateServiceImpl.page(reqDto.getPage(), wrapper);
        var proMap = promotionsServiceImpl.list().stream().collect(Collectors.toMap(Promotions::getId, Promotions::getCodeZh));
        var resPage = BeanConvertUtils.copyPageProperties(page, AllRebate::new, (source, allRebate) -> {
                    allRebate.setCodeZh(proMap.getOrDefault(source.getGameListId(), ""));
                    var gameList = gameCache.getGameListCache(source.getGameListId());
                    allRebate.setGameName(gameList.getName());
                }
        );
        return AllRebateListResDto.builder().totalRewardsCoin(totalRewardsCoin).allRebateList(ResPage.get(resPage)).build();
    }

    /**
     * 邀请好友返水列表
     *
     * @param reqDto 请求参数实体类
     * @return 响应实体类
     */
    @Override
    public FriendRebateListResDto friendRebateList(ReqPage<RewardsReqDto> reqDto) {
        var pair = getCoinRewardPage(reqDto);
        var proMap = promotionsCache.getPromotionsListCache().stream().collect(Collectors.toMap(Promotions::getId, Promotions::getCodeZh));
        var rewardsList = pair.getRight().getRecords();
        Map<Long, CoinRewardsInvite> rewardsInviteMap = new HashMap<>();
        //获取邀请好友的记录
        if (!CollectionUtils.isEmpty(rewardsList)) {
            var ids = rewardsList.stream().map(CoinRewards::getId).collect(Collectors.toList());
            var coinRewardsInviteList = coinRewardsInviteServiceImpl.lambdaQuery()
                    .in(CoinRewardsInvite::getReferId, ids)
                    .list();
            rewardsInviteMap.putAll(coinRewardsInviteList.stream()
                    .collect(Collectors.toMap(CoinRewardsInvite::getReferId, Function.identity())));
        }
        var resPage = BeanConvertUtils.copyPageProperties(pair.getRight(), FriendRebate::new, (source, friendRebate) -> {
            friendRebate.setCodeZh(proMap.getOrDefault(friendRebate.getReferId(), ""));
            //获取邀请Id，被邀请Id，邀请金额
            var coinRewardsInvite = rewardsInviteMap.get(source.getId());
            if (nonNull(coinRewardsInvite)) {
                friendRebate.setBeInviteId(coinRewardsInvite.getUid());
                friendRebate.setInviteCoin(coinRewardsInvite.getCoin());
            }
            friendRebate.setCategory(coinRewardsInvite.getCategory());
        });
        BigDecimal totalRewardsCoin;
        if (reqDto.getData().getCategory() != null) {
            List<FriendRebate> collect = resPage.getRecords().stream().filter(x -> x.getCategory().equals(reqDto.getData().getCategory())).collect(Collectors.toList());
            totalRewardsCoin = collect.stream().map(FriendRebate::getCoin).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
            resPage.setRecords(collect);
            resPage.setSize(collect.size());
            resPage.setTotal(collect.size());
        } else {
            totalRewardsCoin = pair.getLeft();
        }

        return FriendRebateListResDto.builder().totalRewardsCoin(totalRewardsCoin).friendRebateList(ResPage.get(resPage)).build();
    }

    /**
     * 礼品领取列表
     *
     * @param reqDto 请求参数实体类
     * @return 响应实体类
     */
    @Override
    public ResPage<GiftReceiveListResDto> giftReceiveList(ReqPage<GiftReceiveListReqDto> reqDto) {
        var giftReceiveListReqDto = reqDto.getData();
        //游戏类型
        List<Integer> ids = new ArrayList<>();
        if (nonNull(giftReceiveListReqDto.getCategory())) {
            //类型:1-升级奖励 2-投注豪礼',
            ids = giftListServiceImpl.lambdaQuery().eq(GiftList::getCategory, giftReceiveListReqDto.getCategory()).list()
                    .stream().map(GiftList::getId).collect(Collectors.toList());
        }
        var lambdaQueryWrapper = new LambdaQueryWrapper<GiftRecords>()
                //UID
                .eq(nonNull(giftReceiveListReqDto.getUid()), GiftRecords::getUid, giftReceiveListReqDto.getUid())
                //用户名
                .eq(nonNull(giftReceiveListReqDto.getUsername()), GiftRecords::getUsername, giftReceiveListReqDto.getUsername())
                .in(!CollectionUtils.isEmpty(ids), GiftRecords::getGiftId, ids)
                //状态
                .eq(nonNull(giftReceiveListReqDto.getStatus()), GiftRecords::getStatus, giftReceiveListReqDto.getStatus())
                //开始时间
                .ge(nonNull(giftReceiveListReqDto.getStartTime()), GiftRecords::getCreatedAt, giftReceiveListReqDto.getStartTime())
                //结束时间
                .le(nonNull(giftReceiveListReqDto.getEndTime()), GiftRecords::getCreatedAt, giftReceiveListReqDto.getEndTime());
        Page<GiftRecords> page = giftRecordsServiceImpl.page(reqDto.getPage(), lambdaQueryWrapper);
        var proMap = promotionsCache.getPromotionsListCache().stream().collect(Collectors.toMap(Promotions::getId, Promotions::getCodeZh));
        var giftListMap = giftListServiceImpl.lambdaQuery().eq(GiftList::getStatus, 1).list()
                .stream().collect(Collectors.toMap(GiftList::getId, GiftList::getCategory));
        var resPage = BeanConvertUtils.copyPageProperties(page, GiftReceiveListResDto::new, (giftRecord, giftReceiveListResDto) -> {
            var referId = giftListMap.get(giftRecord.getGiftId()) == 1 ? Constant.SPORTS_EXCLUSIVE_VIP : Constant.BET_LUXURY_GIFT;
            giftReceiveListResDto.setCodeZh(proMap.getOrDefault(referId, ""));
        });
        return ResPage.get(resPage);
    }

    /**
     * 礼品领取修改状态
     *
     * @param reqDto 请求参数实体类
     * @return 响应实体类
     */
    @Override
    public boolean giftReceiveUpdate(GiftReceiveUpdateReqDto reqDto) {
        var promotions = BeanConvertUtils.copyProperties(reqDto, GiftRecords::new);
        return giftRecordsServiceImpl.updateById(promotions);
    }

    /**
     * 补充单号详情
     *
     * @param reqDto 请求参数实体类
     * @return 响应实体类
     */
    @Override
    public OrderDetailDto orderDetail(OrderDetailReqDto reqDto) {
        var giftRecords = giftRecordsServiceImpl.getById(reqDto.getId());
        var user = userServiceImpl.getById(giftRecords.getUid());
        var orderDetailDto = new OrderDetailDto();
        orderDetailDto.setId(reqDto.getId());
        var mark = giftRecords.getMark();
        //同意获取豪礼订单详情
        if (giftRecords.getStatus() != 0x2) {
            var jsonObject = parseObject(mark);
            orderDetailDto.setUsername(user.getUsername());
            var number = jsonObject.getString("number");
            number = number == null ? "" : number;
            orderDetailDto.setNumber(number);
            var username = jsonObject.getString("username");
            username = username == null ? "" : username;
            orderDetailDto.setConsignee(username);
            var consignee = jsonObject.getString("consignee");
            consignee = consignee == null ? "" : consignee;
            orderDetailDto.setConsignee(consignee);
            orderDetailDto.setPostNo(giftRecords.getPostNo());
            orderDetailDto.setAddressDetail(giftRecords.getAddr());
        } else {
            //拒绝返回拒绝理由
            orderDetailDto.setReason(mark);
        }
        return orderDetailDto;
    }

    /**
     * 补充单号修改
     *
     * @param reqDto 请求参数实体类
     * @return 响应实体类
     */
    @Override
    public boolean orderUpdate(OrderDetailDto reqDto) {
        var giftRecords = new GiftRecords();
        giftRecords.setId(reqDto.getId());
        giftRecords.setPostNo(reqDto.getPostNo());
        var jsonObject = new JSONObject();
        jsonObject.put("consignee", reqDto.getConsignee());
        jsonObject.put("number", reqDto.getNumber());
        jsonObject.put("username", reqDto.getUsername());
        giftRecords.setAddr(reqDto.getAddressDetail());
        giftRecords.setMark(jsonObject.toJSONString());
        return giftRecordsServiceImpl.updateById(giftRecords);
    }

    /**
     * 活动默认模板
     *
     * @param reqDto 请求参数实体类
     * @return 响应实体类
     */
    @Override
    public RewardsReDto promotionsDefault(ReqPage<RewardsReqDto> reqDto) {
        var pair = getCoinRewardPage(reqDto);
        var page = pair.getRight();
        var proMap = promotionsCache.getPromotionsListCache().stream().collect(Collectors.toMap(Promotions::getId, Promotions::getCodeZh));
        var resPage = BeanConvertUtils.copyPageProperties(page, RewardsDefaultList::new, (source, rewardsList) -> {
            rewardsList.setCodeZh(proMap.getOrDefault(source.getReferId(), ""));
            var info = source.getInfo();
            if (Strings.isNotEmpty(info)) {
                var jsonObject = parseObject(info);
                rewardsList.setBetCoin(jsonObject.getBigDecimal("betCoin"));
                var detailsJson = jsonObject.getJSONObject("details");
                Optional.ofNullable(detailsJson).ifPresent(x -> {
                    detailsJson.put("payType", I18nUtils.getLocaleMessage(x.getString("payType")));
                    rewardsList.setDetails(detailsJson.toJSONString());
                });
            }
        });
        return RewardsReDto.builder().totalRewardsCoin(pair.getLeft()).rewardsList(ResPage.get(resPage)).build();
    }

    /**
     * 根据语言获取活动信息
     *
     * @param reqDto :
     * @Return com.xinbo.sports.backend.io.dto.PromotionsParameter.PromotionsByLangReqDto
     **/
    @Override
    public PromotionsByLangResDto getPromotionsByLang(PromotionsByLangReqDto reqDto) {
        var promotions = promotionsServiceImpl.getById(reqDto.getId());
        var langMsg = dictionaryBase.getCategoryMap("dic_acept_language").get(reqDto.getLang());
        var codeZh = parseObject(promotions.getCodeZh()).getString(reqDto.getLang());
        var img =  parseObject(promotions.getImg()).getString(reqDto.getLang()).startsWith("http")?parseObject(promotions.getImg()).getString(reqDto.getLang()):configCache.getStaticServer() + parseObject(promotions.getImg()).getString(reqDto.getLang());
        var descript = parseObject(promotions.getDescript()).getString(reqDto.getLang());
        return PromotionsByLangResDto.builder().codeZh(codeZh).langMsg(langMsg).img(img).descript(descript).build();
    }

    /**
     * x
     * 平台语言列表
     *
     * @Return java.util.List<com.xinbo.sports.backend.io.dto.PromotionsParameter.PlatLangResDto>
     **/
    @Override
    public List<PlatLangResDto> getPlatLang() {
        var platLang = Optional.ofNullable(configCache.getConfigByTitle("lang")).orElse("");
        var langArr = platLang.split(",");
        var list = new ArrayList<PlatLangResDto>();
        var langMap = dictionaryBase.getCategoryMap("dic_acept_language");
        for (String lang : langArr) {
            var langMsg = langMap.get(lang);
            list.add(PlatLangResDto.builder().lang(lang).langMsg(langMsg).build());
        }
        return list;
    }

    @Resource
    private HttpServletRequest httpServletRequest;

    /**
     * 注册彩金列表
     *
     * @param reqDto 注册彩金列表请求实体类
     * @return registerRewardsListResDto
     */
    @Override
    public ResPage<RegisterRewardsListResDto> registerRewardsList(ReqPage<RegisterRewardsListReqDto> reqDto) {
        var reqData = reqDto.getData();
        var lang = Optional.ofNullable(httpServletRequest.getHeader("Accept-Language")).orElse(BaseEnum.LANG.EN.getValue());
        var lambdaQueryWrapper = new LambdaQueryWrapper<RegisterRewards>()
                //ID
                .eq(nonNull(reqData.getId()), RegisterRewards::getId, reqData.getId())
                //用户名
                .eq(nonNull(reqData.getUsername()), RegisterRewards::getUsername, reqData.getUsername())
                //状态
                .eq(nonNull(reqData.getStatus()), RegisterRewards::getStatus, reqData.getStatus())
                //手机号码
                .eq(nonNull(reqData.getMobile()), RegisterRewards::getMobile, reqData.getMobile())
                //开始时间
                .ge(nonNull(reqData.getStartTime()), RegisterRewards::getCreatedAt, reqData.getStartTime())
                //结束时间
                .le(nonNull(reqData.getEndTime()), RegisterRewards::getCreatedAt, reqData.getEndTime());
        if (reqDto.getSortField().length == 0) {
            //创建时间排序
            lambdaQueryWrapper.orderByDesc(RegisterRewards::getCreatedAt);
        }
        var page = registerRewardsServiceImpl.page(reqDto.getPage(), lambdaQueryWrapper);
        var resPage = BeanConvertUtils.copyPageProperties(page, RegisterRewardsListResDto::new,
                (source, target) -> {
                    target.setUserFlagList(userCache.getUserFlagList(source.getUid()));
                    target.setOperationAt(source.getUpdatedAt());
                    target.setPromotionsName(parseObject(source.getPromotionsName()).getString(lang));
                }
        );
        return ResPage.get(resPage);
    }

    /**
     * @param reqDto 注册彩金修改响应实体类
     * @return Boolean
     */
    @Override
    public Boolean registerRewardsUpdate(RegisterRewardsUpdateReqDto reqDto) {
        /*当前登录用户*/
        BaseParams.HeaderInfo currentLoginUser = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        var now = DateNewUtils.now();
        var ids = reqDto.getRegisterRewardsUpdateList().stream().map(RegisterRewardsUpdate::getId).collect(Collectors.toList());
        var map = reqDto.getRegisterRewardsUpdateList().stream()
                .collect(Collectors.toMap(RegisterRewardsUpdate::getId, RegisterRewardsUpdate::getStatus));
        var registerRewardList = registerRewardsServiceImpl.lambdaQuery()
                .in(RegisterRewards::getId, ids)
                .eq(RegisterRewards::getStatus, 0)
                .list();
        if (CollectionUtils.isEmpty(registerRewardList)) {
            throw new BusinessException(CodeInfo.NO_ACTIVE_RECORD);
        }
        registerRewardList.forEach(registerRewards -> {
            if (Constant.SUCCESS.equals(map.get(registerRewards.getId()))) {
                var promotions = promotionsCache.getPromotionsCache(registerRewards.getPromotionsId());
                //是否领取奖金
                promotionsBase.isPromotionsEffective(promotions.getId(), registerRewards.getUid());
                //新用户注册送彩金
                registerBonusPromotions.getPayCoin(promotions, registerRewards.getUid());
            }
            registerRewards.setStatus(map.get(registerRewards.getId()));
            registerRewards.setOperationId(currentLoginUser.id);
            registerRewards.setOperationName(currentLoginUser.username);
            registerRewards.setUpdatedAt(now);
        });
        return registerRewardsServiceImpl.updateBatchById(registerRewardList);
    }

    /**
     * 获取活动总金额与列表
     *
     * @param reqDto 请求实体类
     * @return 响应实体类
     */
    public Pair<BigDecimal, Page<CoinRewards>> getCoinRewardPage(ReqPage<RewardsReqDto> reqDto) {
        var rewardsReqDto = reqDto.getData();
        var wrapper = new QueryWrapper<CoinRewards>()
                //ID
                .eq(nonNull(rewardsReqDto.getId()), "id", rewardsReqDto.getId())
                //UID
                .eq(nonNull(rewardsReqDto.getUid()), "uid", rewardsReqDto.getUid())
                //用户名
                .eq(nonNull(rewardsReqDto.getUsername()), "username", rewardsReqDto.getUsername())
                //关联ID
                .eq(nonNull(rewardsReqDto.getReferId()), "refer_id", rewardsReqDto.getReferId())
                //开始时间
                .ge(nonNull(rewardsReqDto.getStartTime()), CREATED_AT, rewardsReqDto.getStartTime())
                //结束时间
                .le(nonNull(rewardsReqDto.getEndTime()), CREATED_AT, rewardsReqDto.getEndTime());
        if (reqDto.getSortKey() != null) {
            wrapper.orderBy(reqDto.getSortField() != null, reqDto.getSortKey().equals("ASC"), reqDto.getSortField());
        } else {
            //按创建时间倒序
            wrapper.orderByDesc(CREATED_AT);
        }

        if (Constant.INVITE_FRIENDS.equals(rewardsReqDto.getReferId())) {
            var inviteList = coinRewardsInviteServiceImpl.lambdaQuery().eq(CoinRewardsInvite::getCategory, rewardsReqDto.getCategory()).list();
            if (!CollectionUtils.isEmpty(inviteList)) {
                var rewardIds = inviteList.stream().map(CoinRewardsInvite::getReferId).collect(Collectors.toList());
                wrapper.in("id", rewardIds);
            }
        }
        //求领取彩金
        List<CoinRewards> totalOne = coinRewardsServiceImpl.list(wrapper);
        var totalRewardsCoin = CollectionUtils.isEmpty(totalOne) ? BigDecimal.ZERO :
                totalOne.stream().map(CoinRewards::getCoin).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        Page<CoinRewards> page = coinRewardsServiceImpl.page(reqDto.getPage(), wrapper);
        return Pair.of(totalRewardsCoin, page);
    }

}
