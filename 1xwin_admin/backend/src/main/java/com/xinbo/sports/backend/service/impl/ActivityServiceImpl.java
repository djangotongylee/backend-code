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
 * @description:????????????
 */
@Slf4j
@Service
public class ActivityServiceImpl implements IActivityService {
    //????????????
    private static final String CREATED_AT = "created_at";
    //????????????
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
     * ?????????
     * @return ??????????????? ????????????
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
     * ????????????
     *
     * @param reqDto ???????????????
     * @return ???????????????
     */
    @Override
    public ResPage<ListResDto> promotionsList(ReqPage<ListReqDto> reqDto) {
        var listReqDto = reqDto.getData();
        var lang = Optional.ofNullable(listReqDto.getLang()).orElse(BaseEnum.LANG.EN.getValue());
        var staticServer = configCache.getStaticServer();
        var lambdaQueryWrapper = new LambdaQueryWrapper<Promotions>()
                //????????????
                .like(nonNull(listReqDto.getCodeZh()), Promotions::getCodeZh, listReqDto.getCodeZh())
                //????????????
                .eq(nonNull(listReqDto.getCategory()), Promotions::getCategory, listReqDto.getCategory())
                //??????.ge(
                .eq(nonNull(listReqDto.getStatus()), Promotions::getStatus, listReqDto.getStatus())
                //????????????
                .ge(nonNull(listReqDto.getStartTime()), Promotions::getStartedAt, listReqDto.getStartTime())
                //????????????
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
     * ????????????
     *
     * @param reqDto ?????????????????????
     * @return ???????????????
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
        //??????????????????
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
            //??????????????????
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
     * ????????????
     *
     * @param reqDto ?????????????????????
     * @return ???????????????
     */
    @Override
    public boolean deletePromotions(DeleteReqDto reqDto) {
        boolean b = promotionsServiceImpl.removeById(reqDto.getId());
        promotionsCache.refreshPromotions();
        return b;
    }

    /**
     * ????????????
     *
     * @param reqDto ?????????????????????
     * @return ???????????????
     */
    @Override
    public SingListResDto signList(ReqPage<SingListReqDto> reqDto) {
        var singListReqDto = reqDto.getData();
        var wrapper = new QueryWrapper<CoinRewards>()
                .select("uid", "username", "ifNull(sum(coin),0) as coin", "count(refer_id) as referId")
                //UID
                .eq(nonNull(singListReqDto.getUid()), "uid", singListReqDto.getUid())
                //?????????
                .eq(nonNull(singListReqDto.getUsername()), "username", singListReqDto.getUsername())
                //????????????
                .ge(nonNull(singListReqDto.getStartTime()), CREATED_AT, singListReqDto.getStartTime())
                //????????????
                .le(nonNull(singListReqDto.getEndTime()), CREATED_AT, singListReqDto.getEndTime())
                //??????????????????
                .eq("refer_id", Constant.SIGN_IN_BONUS)
                //UID??????
                .groupBy("uid", "username");
        List<CoinRewards> totalOne = coinRewardsServiceImpl.list(wrapper);
        //???????????????
        var totalRewardsCoin = CollectionUtils.isEmpty(totalOne) ? BigDecimal.ZERO :
                totalOne.stream().map(CoinRewards::getCoin).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        //????????????
        Page<CoinRewards> page = coinRewardsServiceImpl.page(reqDto.getPage(), wrapper);
        var resPage = BeanConvertUtils.copyPageProperties(page, SingList::new, (source, singList) -> {
            singList.setSignDay(source.getReferId());
            singList.setReceiveCoin(source.getCoin());
        });
        return SingListResDto.builder().totalRewardsCoin(totalRewardsCoin).singList(ResPage.get(resPage)).build();
    }

    /**
     * ????????????
     *
     * @param reqDto ?????????????????????
     * @return ???????????????
     */
    @Override
    public ResPage<SingDetailResDto> signDetail(ReqPage<SingDetailReqDto> reqDto) {
        Map<String, String> dayOfWeekMap = Maps.of(
                "1", "?????????",
                "2", "?????????",
                "3", "?????????",
                "4", "?????????",
                "5", "?????????",
                "6", "?????????",
                "7", "?????????");
        var singDetailReqDto = reqDto.getData();
        var lambdaQueryWrapper = new LambdaQueryWrapper<UserSign>()
                //UID
                .eq(nonNull(singDetailReqDto.getUid()), UserSign::getUid, singDetailReqDto.getUid())
                //??????
                .eq(nonNull(singDetailReqDto.getYear()), UserSign::getYear, singDetailReqDto.getYear())
                //??????
                .eq(nonNull(singDetailReqDto.getNw()), UserSign::getNw, singDetailReqDto.getNw());
        Page<UserSign> page = userSignServiceImpl.page(reqDto.getPage(), lambdaQueryWrapper);
        var resPage = BeanConvertUtils.copyPageProperties(page, SingDetailResDto::new,
                (source, singDetailResDto) -> {
                    UserCacheBo.UserCacheInfo userCacheInfo = userCache.getUserInfoById(source.getUid());
                    var userName = userCacheInfo != null ? userCacheInfo.getUsername() : "";
                    singDetailResDto.setUserName(userName);
                    //???????????????????????????????????????
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
     * ??????????????????
     *
     * @param reqDto ?????????????????????
     * @return ???????????????
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
                //????????????????????????????????????????????????
                var jsonObject = parseObject(info);
                rewardsList.setDepositCoin(jsonObject.getBigDecimal("depositCoin"));
                rewardsList.setDepositType(jsonObject.getInteger("depositType"));
                rewardsList.setDepositCategory(jsonObject.getInteger("depositCategory"));
            }
        });
        return FirstDepositResDto.builder().totalRewardsCoin(pair.getLeft()).rewardsList(ResPage.get(resPage)).build();
    }

    /**
     * ?????????????????????
     *
     * @param reqDto ?????????????????????
     * @return ???????????????
     */
    @Override
    public SportAndLiveResDto sportsAndLiveList(@Valid ReqPage<RewardsReqDto> reqDto) {
        var pair = getCoinRewardPage(reqDto);
        var proMap = promotionsCache.getPromotionsListCache().stream().collect(Collectors.toMap(Promotions::getId, Promotions::getCodeZh));
        var resPage = BeanConvertUtils.copyPageProperties(pair.getRight(), SportsAndLiveList::new, (source, sportsAndLiveList) -> {
            sportsAndLiveList.setCodeZh(proMap.getOrDefault(sportsAndLiveList.getReferId(), ""));
            var info = source.getInfo();
            if (Strings.isNotEmpty(info)) {
                //????????????????????????????????????????????????
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
     * ???????????????
     *
     * @param reqDto ?????????????????????
     * @return ???????????????
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
     * ??????????????????
     *
     * @param reqDto ?????????????????????
     * @return ???????????????
     */
    @Override
    public AllRebateListResDto allRebateList(ReqPage<AllRebateListReqDto> reqDto) {
        var allRebateListReqDto = reqDto.getData();
        var wrapper = new LambdaQueryWrapper<CoinRebate>()
                //UID
                .eq(nonNull(allRebateListReqDto.getUid()), CoinRebate::getUid, allRebateListReqDto.getUid())
                //?????????
                .eq(nonNull(allRebateListReqDto.getUsername()), CoinRebate::getUsername, allRebateListReqDto.getUsername())
                //??????ID
                .eq(nonNull(allRebateListReqDto.getId()), CoinRebate::getId, allRebateListReqDto.getId())
                //????????????
                .ge(nonNull(allRebateListReqDto.getStartTime()), CoinRebate::getCreatedAt, allRebateListReqDto.getStartTime())
                //?????????
                .le(nonNull(allRebateListReqDto.getEndTime()), CoinRebate::getCreatedAt, allRebateListReqDto.getEndTime());
        //???????????????
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
     * ????????????????????????
     *
     * @param reqDto ?????????????????????
     * @return ???????????????
     */
    @Override
    public FriendRebateListResDto friendRebateList(ReqPage<RewardsReqDto> reqDto) {
        var pair = getCoinRewardPage(reqDto);
        var proMap = promotionsCache.getPromotionsListCache().stream().collect(Collectors.toMap(Promotions::getId, Promotions::getCodeZh));
        var rewardsList = pair.getRight().getRecords();
        Map<Long, CoinRewardsInvite> rewardsInviteMap = new HashMap<>();
        //???????????????????????????
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
            //????????????Id????????????Id???????????????
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
     * ??????????????????
     *
     * @param reqDto ?????????????????????
     * @return ???????????????
     */
    @Override
    public ResPage<GiftReceiveListResDto> giftReceiveList(ReqPage<GiftReceiveListReqDto> reqDto) {
        var giftReceiveListReqDto = reqDto.getData();
        //????????????
        List<Integer> ids = new ArrayList<>();
        if (nonNull(giftReceiveListReqDto.getCategory())) {
            //??????:1-???????????? 2-????????????',
            ids = giftListServiceImpl.lambdaQuery().eq(GiftList::getCategory, giftReceiveListReqDto.getCategory()).list()
                    .stream().map(GiftList::getId).collect(Collectors.toList());
        }
        var lambdaQueryWrapper = new LambdaQueryWrapper<GiftRecords>()
                //UID
                .eq(nonNull(giftReceiveListReqDto.getUid()), GiftRecords::getUid, giftReceiveListReqDto.getUid())
                //?????????
                .eq(nonNull(giftReceiveListReqDto.getUsername()), GiftRecords::getUsername, giftReceiveListReqDto.getUsername())
                .in(!CollectionUtils.isEmpty(ids), GiftRecords::getGiftId, ids)
                //??????
                .eq(nonNull(giftReceiveListReqDto.getStatus()), GiftRecords::getStatus, giftReceiveListReqDto.getStatus())
                //????????????
                .ge(nonNull(giftReceiveListReqDto.getStartTime()), GiftRecords::getCreatedAt, giftReceiveListReqDto.getStartTime())
                //????????????
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
     * ????????????????????????
     *
     * @param reqDto ?????????????????????
     * @return ???????????????
     */
    @Override
    public boolean giftReceiveUpdate(GiftReceiveUpdateReqDto reqDto) {
        var promotions = BeanConvertUtils.copyProperties(reqDto, GiftRecords::new);
        return giftRecordsServiceImpl.updateById(promotions);
    }

    /**
     * ??????????????????
     *
     * @param reqDto ?????????????????????
     * @return ???????????????
     */
    @Override
    public OrderDetailDto orderDetail(OrderDetailReqDto reqDto) {
        var giftRecords = giftRecordsServiceImpl.getById(reqDto.getId());
        var user = userServiceImpl.getById(giftRecords.getUid());
        var orderDetailDto = new OrderDetailDto();
        orderDetailDto.setId(reqDto.getId());
        var mark = giftRecords.getMark();
        //??????????????????????????????
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
            //????????????????????????
            orderDetailDto.setReason(mark);
        }
        return orderDetailDto;
    }

    /**
     * ??????????????????
     *
     * @param reqDto ?????????????????????
     * @return ???????????????
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
     * ??????????????????
     *
     * @param reqDto ?????????????????????
     * @return ???????????????
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
     * ??????????????????????????????
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
     * ??????????????????
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
     * ??????????????????
     *
     * @param reqDto ?????????????????????????????????
     * @return registerRewardsListResDto
     */
    @Override
    public ResPage<RegisterRewardsListResDto> registerRewardsList(ReqPage<RegisterRewardsListReqDto> reqDto) {
        var reqData = reqDto.getData();
        var lang = Optional.ofNullable(httpServletRequest.getHeader("Accept-Language")).orElse(BaseEnum.LANG.EN.getValue());
        var lambdaQueryWrapper = new LambdaQueryWrapper<RegisterRewards>()
                //ID
                .eq(nonNull(reqData.getId()), RegisterRewards::getId, reqData.getId())
                //?????????
                .eq(nonNull(reqData.getUsername()), RegisterRewards::getUsername, reqData.getUsername())
                //??????
                .eq(nonNull(reqData.getStatus()), RegisterRewards::getStatus, reqData.getStatus())
                //????????????
                .eq(nonNull(reqData.getMobile()), RegisterRewards::getMobile, reqData.getMobile())
                //????????????
                .ge(nonNull(reqData.getStartTime()), RegisterRewards::getCreatedAt, reqData.getStartTime())
                //????????????
                .le(nonNull(reqData.getEndTime()), RegisterRewards::getCreatedAt, reqData.getEndTime());
        if (reqDto.getSortField().length == 0) {
            //??????????????????
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
     * @param reqDto ?????????????????????????????????
     * @return Boolean
     */
    @Override
    public Boolean registerRewardsUpdate(RegisterRewardsUpdateReqDto reqDto) {
        /*??????????????????*/
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
                //??????????????????
                promotionsBase.isPromotionsEffective(promotions.getId(), registerRewards.getUid());
                //????????????????????????
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
     * ??????????????????????????????
     *
     * @param reqDto ???????????????
     * @return ???????????????
     */
    public Pair<BigDecimal, Page<CoinRewards>> getCoinRewardPage(ReqPage<RewardsReqDto> reqDto) {
        var rewardsReqDto = reqDto.getData();
        var wrapper = new QueryWrapper<CoinRewards>()
                //ID
                .eq(nonNull(rewardsReqDto.getId()), "id", rewardsReqDto.getId())
                //UID
                .eq(nonNull(rewardsReqDto.getUid()), "uid", rewardsReqDto.getUid())
                //?????????
                .eq(nonNull(rewardsReqDto.getUsername()), "username", rewardsReqDto.getUsername())
                //??????ID
                .eq(nonNull(rewardsReqDto.getReferId()), "refer_id", rewardsReqDto.getReferId())
                //????????????
                .ge(nonNull(rewardsReqDto.getStartTime()), CREATED_AT, rewardsReqDto.getStartTime())
                //????????????
                .le(nonNull(rewardsReqDto.getEndTime()), CREATED_AT, rewardsReqDto.getEndTime());
        if (reqDto.getSortKey() != null) {
            wrapper.orderBy(reqDto.getSortField() != null, reqDto.getSortKey().equals("ASC"), reqDto.getSortField());
        } else {
            //?????????????????????
            wrapper.orderByDesc(CREATED_AT);
        }

        if (Constant.INVITE_FRIENDS.equals(rewardsReqDto.getReferId())) {
            var inviteList = coinRewardsInviteServiceImpl.lambdaQuery().eq(CoinRewardsInvite::getCategory, rewardsReqDto.getCategory()).list();
            if (!CollectionUtils.isEmpty(inviteList)) {
                var rewardIds = inviteList.stream().map(CoinRewardsInvite::getReferId).collect(Collectors.toList());
                wrapper.in("id", rewardIds);
            }
        }
        //???????????????
        List<CoinRewards> totalOne = coinRewardsServiceImpl.list(wrapper);
        var totalRewardsCoin = CollectionUtils.isEmpty(totalOne) ? BigDecimal.ZERO :
                totalOne.stream().map(CoinRewards::getCoin).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        Page<CoinRewards> page = coinRewardsServiceImpl.page(reqDto.getPage(), wrapper);
        return Pair.of(totalRewardsCoin, page);
    }

}
