package com.xinbo.sports.apiend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinbo.sports.apiend.base.GameCommonBase;
import com.xinbo.sports.apiend.io.dto.promotions.LuxuriousGiftListReqDto;
import com.xinbo.sports.apiend.io.dto.promotions.LuxuriousGiftListResDto;
import com.xinbo.sports.apiend.io.dto.promotions.LuxuriousGiftReceiveResDto;
import com.xinbo.sports.apiend.io.dto.promotions.LuxuriousGiftResDto;
import com.xinbo.sports.apiend.service.ILuxuriousGiftService;
import com.xinbo.sports.apiend.service.IUserInfoService;
import com.xinbo.sports.dao.generator.po.GiftList;
import com.xinbo.sports.dao.generator.po.GiftRecords;
import com.xinbo.sports.dao.generator.po.UserAddress;
import com.xinbo.sports.dao.generator.po.UserLevel;
import com.xinbo.sports.dao.generator.service.GiftListService;
import com.xinbo.sports.dao.generator.service.GiftRecordsService;
import com.xinbo.sports.dao.generator.service.UserAddressService;
import com.xinbo.sports.dao.generator.service.UserLevelService;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams;
import com.xinbo.sports.service.base.PromotionsBase;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.service.common.Constant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.dto.BaseParams;
import com.xinbo.sports.service.io.dto.UserInfo;
import com.xinbo.sports.service.io.dto.promotions.LuxuriousGiftReceiveReqDto;
import com.xinbo.sports.service.io.enums.BaseEnum;
import com.xinbo.sports.service.thread.ThreadHeaderLocalData;
import com.xinbo.sports.utils.BeanConvertUtils;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.DateUtils;
import com.xinbo.sports.utils.SnowFlake;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author: wells
 * @date: 2020/5/8
 * @description:
 */
@Slf4j
@Service("luxuriousGiftServiceImpl")
public class LuxuriousGiftServiceImpl implements ILuxuriousGiftService {
    //VIP ??????
    private static final Integer VIP_CATEGORY = 1;
    @Resource
    IUserInfoService userInfoServiceImpl;
    @Autowired
    private GiftListService giftListServiceImpl;
    @Autowired
    private UserLevelService userLevelServiceImpl;
    @Autowired
    private GiftRecordsService giftRecordsServiceImpl;
    @Autowired
    private UserAddressService userAddressServiceImpl;
    @Resource
    private ConfigCache configCache;
    @Autowired
    private GameCommonBase gameCommonBase;
    @Autowired
    private PromotionsBase promotionsBase;

    /**
     * ????????????->????????????
     * ??????VIP???????????????????????????,???????????????????????????
     *
     * @param reqDto
     * @return
     */
    @Override
    public LuxuriousGiftListResDto luxuriousGiftList(LuxuriousGiftListReqDto reqDto) {
        //??????????????????
        UserInfo userInfo = userInfoServiceImpl.findIdentityByApiToken();
        LuxuriousGiftListResDto luxuriousGiftListResDto = new LuxuriousGiftListResDto();
        //??????????????????
        List<GiftList> giftLists = giftListServiceImpl.list(new LambdaQueryWrapper<GiftList>()
                .eq(GiftList::getStatus, 1)
                .eq(Objects.nonNull(reqDto.getCategory()), GiftList::getCategory, reqDto.getCategory()));
        //?????????-???1???VIP??????????????????????????????
        Map<Integer, String> userLevelMap = userLevelServiceImpl.list().stream().
                collect(Collectors.toMap(UserLevel::getId, UserLevel::getCode));
        //??????????????????
        if (userInfo.getId() == null) {
            return noUserReturn(luxuriousGiftListResDto, giftLists, reqDto, userLevelMap);
        }
        //????????????????????????,????????????????????????
        List<Integer> giftIds = giftLists.stream().map(GiftList::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(giftLists)) {
            throw new BusinessException(CodeInfo.ACTIVE_NO_GIFT_LIST);
        }
        List<GiftRecords> giftRecords = giftRecordsServiceImpl.list(new LambdaQueryWrapper<GiftRecords>()
                .eq(GiftRecords::getUid, userInfo.getId())
                .in(GiftRecords::getStatus, List.of(0, 1, 3, 4))
                .in(GiftRecords::getGiftId, giftIds)
                .orderByDesc(GiftRecords::getUpdatedAt));
        //??????startTime
        var createdAt = CollectionUtils.isEmpty(giftRecords) ? 0 : giftRecords.get(0).getCreatedAt();
        BigDecimal betTotalCoin = BigDecimal.ZERO;
        if (!VIP_CATEGORY.equals(reqDto.getCategory())) {
            //????????????->???????????????
            var platGameQueryDateDto = PlatFactoryParams.PlatGameQueryDateDto.builder()
                    .uid(userInfo.getId())
                    //?????????????????????????????????
                    .platId(Constant.SPORTS_PLAT_ID)
                    .startTime(createdAt)
                    .build();
            //????????????????????????
            betTotalCoin = gameCommonBase.getCoinStatisticsByDate(platGameQueryDateDto).getCoinBet();
        }
        //??????????????????
        List<LuxuriousGiftResDto> luxuriousGiftResList = this.getGiftList(userLevelMap, giftLists, giftRecords, reqDto, betTotalCoin, userInfo.getLevelId());
        //???????????????????????????
        if (reqDto.getStatus() != 0) {
            luxuriousGiftResList = luxuriousGiftResList.stream()
                    .filter(luxuriousGiftResDto -> reqDto.getStatus().equals(luxuriousGiftResDto.getReceiveStatus()))
                    .collect(Collectors.toList());
        }
        luxuriousGiftListResDto.setLuxuriousGifList(luxuriousGiftResList);
        String alreadyCoin = VIP_CATEGORY.equals(reqDto.getCategory()) ? userLevelMap.get(userInfo.getLevelId()) : String.valueOf(betTotalCoin);
        //??????????????????
        var requiredCoin = getRequiredCoin(giftRecords, giftLists, reqDto.getCategory());
        var vipValue = VIP_CATEGORY.equals(reqDto.getCategory()) ? userLevelMap.get(requiredCoin) : requiredCoin;
        luxuriousGiftListResDto.setRequiredCoin(vipValue + "");
        alreadyCoin = !VIP_CATEGORY.equals(reqDto.getCategory()) && betTotalCoin.compareTo(new BigDecimal(requiredCoin)) > 0
                ? vipValue + "" : alreadyCoin;
        luxuriousGiftListResDto.setAreadyCoin(alreadyCoin);
        return luxuriousGiftListResDto;
    }

    /**
     * ?????????????????????????????????
     *
     * @param giftRecords ????????????
     * @param giftLists   ????????????
     * @param category    ????????????
     * @return ??????????????????
     */
    public Integer getRequiredCoin(List<GiftRecords> giftRecords, List<GiftList> giftLists, Integer category) {
        List<GiftList> betGifts = giftLists.stream().filter(x -> x.getCategory().equals(category))
                .sorted(Comparator.comparing(GiftList::getRequired)).collect(Collectors.toList());
        int requiredCoin;
        if (!CollectionUtils.isEmpty(giftRecords)) {
            var giftIdOptional = giftRecords.stream().max(Comparator.comparing(GiftRecords::getGiftId));
            var maxGiftId = giftIdOptional.isPresent() ? giftIdOptional.get().getGiftId() : giftRecords.get(0).getGiftId();
            //2.???????????????????????????????????????
            if (betGifts.get(betGifts.size() - 1).getId().equals(maxGiftId)) {
                requiredCoin = betGifts.get(betGifts.size() - 1).getRequired();
            } else {
                //3.????????????????????????????????????.
                List<GiftList> maxGiftList = betGifts.stream().filter(x -> x.getId().equals(maxGiftId + 1)).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(maxGiftList)) {
                    requiredCoin = betGifts.get(0).getRequired();
                } else {
                    requiredCoin = maxGiftList.get(0).getRequired();
                }
            }
        } else {
            //1.???????????????????????????????????????????????????,
            requiredCoin = betGifts.get(0).getRequired();
        }
        return requiredCoin;
    }

    /**
     * ??????????????????????????????????????????
     *
     * @param luxuriousGiftListResDto
     * @param giftLists
     * @param reqDto
     * @return
     */
    public LuxuriousGiftListResDto noUserReturn(LuxuriousGiftListResDto luxuriousGiftListResDto, List<GiftList> giftLists,
                                                LuxuriousGiftListReqDto reqDto, Map<Integer, String> userLevelMap) {
        BaseParams.HeaderInfo headerInfo = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        String staticServer = configCache.getStaticServer();
        List<LuxuriousGiftResDto> luxuriousGiftResDtos = BeanConvertUtils.copyListProperties(giftLists, LuxuriousGiftResDto::new,
                (giftList, luxuriousGiftResDto) -> {
                    var img = headerInfo.getDevice().equalsIgnoreCase(BaseEnum.DEVICE.D.getValue()) ? giftList.getImgPc() : giftList.getImgH5();
                    luxuriousGiftResDto.setImg(img.startsWith("http")?img:staticServer + img);
                    var requiredCoin = VIP_CATEGORY.equals(reqDto.getCategory()) ?
                            userLevelMap.get(giftList.getRequired()) : String.valueOf(giftList.getRequired());
                    luxuriousGiftResDto.setRequired(requiredCoin);
                });
        //???????????????????????????,?????????????????????
        if (reqDto.getStatus() != 0) {
            luxuriousGiftResDtos = luxuriousGiftResDtos.stream()
                    .filter(luxuriousGiftResDto -> reqDto.getStatus().equals(luxuriousGiftResDto.getReceiveStatus()))
                    .collect(Collectors.toList());
        }
        luxuriousGiftListResDto.setLuxuriousGifList(luxuriousGiftResDtos);
        if (VIP_CATEGORY.equals(reqDto.getCategory())) {
            luxuriousGiftListResDto.setAreadyCoin("vip0");
            luxuriousGiftListResDto.setRequiredCoin("vip1");
        } else {
            var mixRequire = giftLists.stream().filter(x -> x.getCategory().equals(reqDto.getCategory())).map(GiftList::getRequired)
                    .min(Integer::compareTo).orElse(0);
            luxuriousGiftListResDto.setAreadyCoin("0");
            luxuriousGiftListResDto.setRequiredCoin(mixRequire + "");
        }
        return luxuriousGiftListResDto;
    }

    /**
     * ??????????????????
     *
     * @param userLevelMap ????????????
     * @param giftLists    ????????????
     * @param giftRecords  ????????????
     * @param reqDto       ????????????
     * @return ??????????????????
     */
    public List<LuxuriousGiftResDto> getGiftList(Map<Integer, String> userLevelMap, List<GiftList> giftLists,
                                                 List<GiftRecords> giftRecords, LuxuriousGiftListReqDto reqDto,
                                                 BigDecimal betTotalCoin, Integer levelId) {
        BaseParams.HeaderInfo headerInfo = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL.get();
        String staticServer = configCache.getStaticServer();
        return BeanConvertUtils.copyListProperties(giftLists, LuxuriousGiftResDto::new, (giftList, luxuriousGiftResDto) -> {
            var reCategory = reqDto.getCategory();
            if (VIP_CATEGORY.equals(reCategory)) {
                luxuriousGiftResDto.setLevelId(giftList.getRequired());
                luxuriousGiftResDto.setRequired(userLevelMap.get(giftList.getRequired()));
            } else {
                luxuriousGiftResDto.setRequired(String.valueOf(giftList.getRequired()));
            }
            var img = headerInfo.getDevice().equalsIgnoreCase(BaseEnum.DEVICE.D.getValue()) ? giftList.getImgPc() : giftList.getImgH5();
            luxuriousGiftResDto.setImg(img.startsWith("http")?img:staticServer + img);
            //????????????????????????-???1-?????????????????????????????????????????????2-??????????????????????????????????????? 3-?????????
            int receiveStatus;
            var isVip = reCategory == 1 && levelId >= giftList.getRequired();
            var giftCount = giftRecords.stream().filter(x -> x.getGiftId().equals(giftList.getId())).count();
            var isLuxurious = reCategory == 2 && betTotalCoin.compareTo(BigDecimal.valueOf(giftList.getRequired())) >= 0;
            if (giftCount > 0) {
                receiveStatus = 3;
            } else if (isVip || isLuxurious) {
                receiveStatus = 2;
            } else {
                receiveStatus = 1;
            }
            luxuriousGiftResDto.setReceiveStatus(receiveStatus);
        });
    }

    /***
     * ????????????->????????????
     * @param reqDto
     * @return
     */
    @Override
    public LuxuriousGiftReceiveResDto luxuriousGiftReceive(LuxuriousGiftReceiveReqDto reqDto) {
        //??????????????????
        UserInfo userInfo = userInfoServiceImpl.findIdentityByApiToken();
        //??????????????????
        GiftList giftList = giftListServiceImpl.getOne(new LambdaQueryWrapper<GiftList>().eq(GiftList::getId, reqDto.getGiftId()));
        //??????????????????
        if (giftList.getNums() <= 0) {
            throw new BusinessException(CodeInfo.ACTIVE_NO_GIFT_NUMS);
        }
        //????????????????????????
        var giftCount = giftRecordsServiceImpl.lambdaQuery()
                .eq(GiftRecords::getUid, userInfo.getId())
                //???????????????????????????
                .ne(GiftRecords::getStatus, 2)
                .eq(GiftRecords::getGiftId, reqDto.getGiftId())
                .count();
        if (giftCount > 0) {
            throw new BusinessException(CodeInfo.ACTIVE_GIFT_IS_RECEIVE);
        }
        //????????????????????????
        List<UserAddress> userAddressList = userAddressServiceImpl.list(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUid, userInfo.getId())
                .eq(UserAddress::getStatus, 1));
        if (CollectionUtils.isEmpty(userAddressList)) {
            throw new BusinessException(CodeInfo.ACTIVE_NO_USER_ADDRESS);
        }
        //??????????????????????????????
        if (giftList.getCategory() == 1 && userInfo.getLevelId() < giftList.getRequired()) {
            throw new BusinessException(CodeInfo.ACTIVE_BET_COIN_NO_ENOUGH);
        }
        List<GiftRecords> giftRecords = giftRecordsServiceImpl.list(new LambdaQueryWrapper<GiftRecords>()
                .eq(GiftRecords::getUid, userInfo.getId())
                .in(GiftRecords::getStatus, List.of(1, 3, 4))
                .in(GiftRecords::getGiftId, giftList.getId())
                .orderByDesc(GiftRecords::getUpdatedAt));
        //??????startTime
        var createdAt = CollectionUtils.isEmpty(giftRecords) ? 0 : giftRecords.get(0).getCreatedAt();
        //????????????->???????????????
        var platGameQueryDateDto = PlatFactoryParams.PlatGameQueryDateDto.builder().uid(userInfo.getId())
                .startTime(createdAt).endTime(DateUtils.getCurrentTime()).build();
        BigDecimal betTotalCoin = gameCommonBase.getCoinStatisticsByDate(platGameQueryDateDto).getCoinBet();
        if (giftList.getCategory() == 2 && giftList.getRequired() > betTotalCoin.intValue()) {
            throw new BusinessException(CodeInfo.ACTIVE_BET_COIN_NO_ENOUGH);
        }
        var postNo = String.valueOf(SnowFlake.getInstance().nextId());
        var address = userAddressList.get(0).getAddress();
        var receiveName = userAddressList.get(0).getName();
        //????????????
        promotionsBase.executeLuxuriousGiftReceivePersistence(reqDto, giftList, userInfo.getId(), address, postNo);
        //??????????????????
        return LuxuriousGiftReceiveResDto.builder()
                .receiveName(receiveName)
                .giftName(giftList.getName())
                .receiveAddress(address)
                .orderId(postNo)
                .submitAt(DateNewUtils.now())
                .build();
    }
}
