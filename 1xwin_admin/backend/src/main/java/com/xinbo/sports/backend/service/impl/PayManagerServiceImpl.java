package com.xinbo.sports.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinbo.sports.backend.base.DictionaryBase;
import com.xinbo.sports.backend.io.bo.PayManager;
import com.xinbo.sports.backend.service.IPayManagerService;
import com.xinbo.sports.dao.generator.po.*;
import com.xinbo.sports.dao.generator.service.*;
import com.xinbo.sports.service.cache.redis.BankCache;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.service.cache.redis.UserCache;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.dto.Dictionary;
import com.xinbo.sports.utils.BeanConvertUtils;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.pagination.ResPage;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

/**
 * <p>
 * 游戏管理->平台管理
 * </p>
 *
 * @author andy
 * @since 2020/6/8
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PayManagerServiceImpl implements IPayManagerService {
    private final PayPlatService payPlatServiceImpl;
    private final PayOnlineService payOnlineServiceImpl;
    private final PayOfflineService payOfflineServiceImpl;
    private final PayOnlineWithdrawService payOnlineWithdrawServiceImpl;
    private final BankListService bankListServiceImpl;
    private final PayBankListService payBankListServiceImpl;
    private final DictionaryBase dictionaryBase;
    private final UserLevelService userLevelServiceImpl;
    private final UserCache userCache;
    private final ConfigCache configCache;
    private final BankCache bankCache;

    @Override
    public ResPage<PayManager.PayPlatListResBody> payPlatList(ReqPage<PayManager.PayPlatListReqBody> reqBody) {
        if (null == reqBody) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        LambdaQueryWrapper<PayPlat> wrapper = null;
        if (null != reqBody.getData()) {
            PayManager.PayPlatListReqBody data = reqBody.getData();
            wrapper = Wrappers.lambdaQuery();
            wrapper.eq(null != data.getBusinessCode(), PayPlat::getBusinessCode, data.getBusinessCode());
            wrapper.eq(null != data.getName(), PayPlat::getName, data.getName());
            wrapper.eq(null != data.getStatus(), PayPlat::getStatus, data.getStatus());
        }
        Page<PayPlat> page = payPlatServiceImpl.page(reqBody.getPage(), wrapper);
        Page<PayManager.PayPlatListResBody> tmpPage = BeanConvertUtils.copyPageProperties(page, PayManager.PayPlatListResBody::new);
        return ResPage.get(tmpPage);
    }

    @Override
    public void payPlatAdd(PayManager.PayPlatAddReqBody reqBody) {
        PayPlat payPlat = BeanConvertUtils.beanCopy(reqBody, PayPlat::new);
        int now = DateNewUtils.now();
        payPlat.setCreatedAt(now);
        payPlat.setUpdatedAt(now);
        payPlatServiceImpl.save(payPlat);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void payPlatUpdate(PayManager.PayPlatUpdateReqBody reqBody) {
        PayPlat payPlat = BeanConvertUtils.beanCopy(reqBody, PayPlat::new);
        payPlat.setUpdatedAt(DateNewUtils.now());
        payPlatServiceImpl.updateById(payPlat);
        //状态:0-停用 1-启用 2-删除,当status=0或2时，更新字表status
        int status = payPlat.getStatus();
        if (status == 0 || status == 2) {
            PayPlat byId = payPlatServiceImpl.getById(payPlat.getId());
            if (null != byId) {
                payOnlineServiceImpl.lambdaUpdate().set(PayOnline::getStatus, status).eq(PayOnline::getCode, byId.getCode()).update();
            }
        }
        userCache.updateUserPayOnLineList();
    }

    @Override
    public PayManager.PayPlatDetailResBody payPlatDetail(PayManager.CommonIdReq reqBody) {
        PayPlat payPlat = payPlatServiceImpl.getById(reqBody.getId());
        return BeanConvertUtils.beanCopy(payPlat, PayManager.PayPlatDetailResBody::new);
    }

    @Override
    public ResPage<PayManager.PayOnLineListResBody> payOnLineList(ReqPage<PayManager.PayOnLineListReqBody> reqBody) {
        if (null == reqBody) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        LambdaQueryWrapper<PayOnline> wrapper = null;
        if (null != reqBody.getData()) {
            PayManager.PayOnLineListReqBody data = reqBody.getData();
            wrapper = Wrappers.lambdaQuery();
            wrapper.eq(null != data.getPayName(), PayOnline::getPayName, data.getPayName());
            wrapper.eq(null != data.getCategory(), PayOnline::getCategory, data.getCategory());
            wrapper.eq(null != data.getStatus(), PayOnline::getStatus, data.getStatus());
        }
        Page<PayOnline> page = payOnlineServiceImpl.page(reqBody.getPage(), wrapper);
        Page<PayManager.PayOnLineListResBody> tmpPage = BeanConvertUtils.copyPageProperties(page, PayManager.PayOnLineListResBody::new);
        List<PayManager.PayOnLineListResBody> list = new ArrayList<>();
        page.getRecords().forEach(s -> {
            List<UserLevel> userLevelList = userCache.getUserLevelList();
            List<PayManager.LevelBitBo> levelBitAllList = BeanConvertUtils.copyListProperties(userLevelList, PayManager.LevelBitBo::new);
            PayManager.PayOnLineListResBody payOnLineListResBody = BeanConvertUtils.beanCopy(s, PayManager.PayOnLineListResBody::new);
            payOnLineListResBody.setLevelBitCurrentList(getLevelBitCurrentList(s.getLevelBit()));
            payOnLineListResBody.setLevelBitAllList(levelBitAllList);
            list.add(payOnLineListResBody);
        });
        ResPage<PayManager.PayOnLineListResBody> resPage = ResPage.get(tmpPage);
        resPage.setList(list);
        return resPage;
    }

    private List<PayManager.LevelBitBo> getLevelBitCurrentList(Integer levelBit) {
        List<PayManager.LevelBitBo> list = new ArrayList<>();
        LambdaQueryWrapper<UserLevel> where = Wrappers.lambdaQuery();
        where.apply("bit_code & " + levelBit);
        List<UserLevel> userLevelList = userLevelServiceImpl.list(where);
        if (Optional.ofNullable(userLevelList).isPresent()) {
            list = BeanConvertUtils.copyListProperties(userLevelList, PayManager.LevelBitBo::new);
        }
        return list;
    }

    @Override
    public void payOnLineUpdate(PayManager.PayOnLineUpdateReqBody reqBody) {
        PayOnline online = BeanConvertUtils.beanCopy(reqBody, PayOnline::new);
        online.setUpdatedAt(DateNewUtils.now());
        payOnlineServiceImpl.updateById(online);
        userCache.updateUserPayOnLineList();
    }

    @Override
    public ResPage<PayManager.PayOffLineListResBody> payOffLineList(ReqPage<PayManager.PayOffLineListReqBody> reqBody) {
        if (null == reqBody) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        LambdaQueryWrapper<PayOffline> wrapper = null;
        wrapper = Wrappers.lambdaQuery();
        if (null != reqBody.getData()) {
            PayManager.PayOffLineListReqBody data = reqBody.getData();
            wrapper.like(null != data.getUserName(), PayOffline::getUserName, data.getUserName());
            wrapper.eq(null != data.getCategory(), PayOffline::getCategory, data.getCategory());
            wrapper.eq(null != data.getStatus(), PayOffline::getStatus, data.getStatus());
        }
        Page<PayOffline> page = payOfflineServiceImpl.page(reqBody.getPage(), wrapper.ne(PayOffline::getStatus, 2));
        Page<PayManager.PayOffLineListResBody> tmpPage = BeanConvertUtils.copyPageProperties(page, PayManager.PayOffLineListResBody::new);
        List<PayManager.PayOffLineListResBody> list = new ArrayList<>();
        page.getRecords().forEach(s -> {
            List<UserLevel> userLevelList = userCache.getUserLevelList();
            List<PayManager.LevelBitBo> levelBitAllList = BeanConvertUtils.copyListProperties(userLevelList, PayManager.LevelBitBo::new);
            PayManager.PayOffLineListResBody payOnLineListResBody = BeanConvertUtils.beanCopy(s, PayManager.PayOffLineListResBody::new);
            payOnLineListResBody.setLevelBitCurrentList(getLevelBitCurrentList(s.getLevelBit()));
            payOnLineListResBody.setLevelBitAllList(levelBitAllList);
            payOnLineListResBody.setQrCode(s.getQrCode().startsWith("http")?  s.getQrCode():configCache.getStaticServer() + s.getQrCode());
            list.add(payOnLineListResBody);
        });
        ResPage<PayManager.PayOffLineListResBody> resPage = ResPage.get(tmpPage);
        resPage.setList(list);
        return resPage;
    }

    @Override
    public void payOffLineAdd(PayManager.PayOffLineAddReqBody reqBody) {
        PayOffline offline = BeanConvertUtils.beanCopy(reqBody, PayOffline::new);
        int now = DateNewUtils.now();
        offline.setCreatedAt(now);
        offline.setUpdatedAt(now);
        Integer category = reqBody.getCategory();
        // 类型:1-银联 2-微信 3-支付宝 4-QQ 5-QR扫码
        if (Optional.ofNullable(category).isPresent() && 1 == category) {
            if (null == reqBody.getBankId()) {
                throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
            }
            BankList bankList = bankListServiceImpl.getById(reqBody.getBankId());
            if (Optional.ofNullable(bankList).isPresent()) {
                // 收款银行
                offline.setBankName(bankList.getName());
            }
        } else {
            Dictionary.ResDto dictItem = getDictItem(category);
            if (null != dictItem) {
                // 取sp_dict_item表的title
                offline.setUserName(dictItem.getTitle());
                offline.setBankName(dictItem.getTitle());
            }
        }
        offline.setQrCode(subStringPath(offline.getQrCode()));
        payOfflineServiceImpl.save(offline);
        userCache.updateUserPayOffLineList();
    }

    private Dictionary.ResDto getDictItem(Integer code) {
        if (null == code) {
            return null;
        }
        Dictionary.ResDto resDto = null;
        String key = "dic_pay_online_category";
        Map<String, List<Dictionary.ResDto>> dictionary = dictionaryBase.getDictionary(key);
        if (Optional.ofNullable(dictionary).isPresent()) {
            List<Dictionary.ResDto> list = dictionary.get(key);
            resDto = list.stream().filter(s -> code.toString().equals(s.getCode())).findAny().get();
        }
        return resDto;
    }

    @Override
    public void payOffLineUpdate(PayManager.PayOffLineUpdateReqBody reqBody) {
        PayOffline offline = BeanConvertUtils.beanCopy(reqBody, PayOffline::new);
        offline.setUpdatedAt(DateNewUtils.now());
        Integer category = reqBody.getCategory();
        // 类型:1-银联 2-微信 3-支付宝 4-QQ 5-QR扫码
        if (Optional.ofNullable(category).isPresent() && 1 == category) {
            if (null == reqBody.getBankId()) {
                throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
            }
            BankList bankList = bankListServiceImpl.getById(reqBody.getBankId());
            if (Optional.ofNullable(bankList).isPresent()) {
                // 收款银行
                offline.setBankName(bankList.getName());
            }
        } else {
            Dictionary.ResDto dictItem = getDictItem(category);
            if (null != dictItem) {
                // 取sp_dict_item表的title
                offline.setUserName(dictItem.getTitle());
                offline.setBankName(dictItem.getTitle());
            }
        }
        offline.setQrCode(subStringPath(offline.getQrCode()));
        payOfflineServiceImpl.updateById(offline);
        userCache.updateUserPayOffLineList();
    }

    @Override
    public PayManager.PayOffLineDetailResBody payOffLineDetail(PayManager.CommonIdReq reqBody) {
        PayOffline payOffline = payOfflineServiceImpl.getById(reqBody.getId());
        if (null == payOffline) {
            throw new BusinessException(CodeInfo.API_RECORDS_NOT_EXISTS);
        }
        PayManager.PayOffLineDetailResBody detailResBody = BeanConvertUtils.beanCopy(payOffline, PayManager.PayOffLineDetailResBody::new);
        detailResBody.setQrCode(payOffline.getQrCode().startsWith("http")?  payOffline.getQrCode():configCache.getStaticServer() + payOffline.getQrCode());
        detailResBody.setLevelBitCurrentList(getLevelBitCurrentList(payOffline.getLevelBit()));
        return detailResBody;
    }

    @Override
    public ResPage<PayManager.PayoutOnLineListResBody> payoutOnLineList(ReqPage<PayManager.PayoutOnLineListReqBody> reqBody) {
        if (null == reqBody) throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        var wrapper = new LambdaQueryWrapper<PayOnlineWithdraw>();
        if (null != reqBody.getData()) {
            var data = reqBody.getData();
            wrapper.eq(nonNull(data.getPayName()), PayOnlineWithdraw::getPayName, data.getPayName());
            wrapper.eq(nonNull(data.getStatus()), PayOnlineWithdraw::getStatus, data.getStatus());
        }
        Page<PayOnlineWithdraw> page = payOnlineWithdrawServiceImpl.page(reqBody.getPage(), wrapper.ne(PayOnlineWithdraw::getStatus, 2));
        Page<PayManager.PayoutOnLineListResBody> tmpPage = BeanConvertUtils.copyPageProperties(page, PayManager.PayoutOnLineListResBody::new);
        List<PayManager.PayoutOnLineListResBody> list = new ArrayList<>();
        page.getRecords().forEach(s -> {
            List<UserLevel> userLevelList = userCache.getUserLevelList();
            List<PayManager.LevelBitBo> levelBitAllList = BeanConvertUtils.copyListProperties(userLevelList, PayManager.LevelBitBo::new);
            PayManager.PayoutOnLineListResBody payOnLineListResBody = BeanConvertUtils.beanCopy(s, PayManager.PayoutOnLineListResBody::new);
            payOnLineListResBody.setLevelBitCurrentList(getLevelBitCurrentList(s.getLevelBit()));
            payOnLineListResBody.setLevelBitAllList(levelBitAllList);
            list.add(payOnLineListResBody);
        });
        ResPage<PayManager.PayoutOnLineListResBody> resPage = ResPage.get(tmpPage);
        resPage.setList(list);
        return resPage;
    }

    @Override
    public Boolean payoutOnLineUpdate(PayManager.PayoutOnLineUpdateReqBody reqBody) {
        PayOnlineWithdraw payOnlineWithdraw = BeanConvertUtils.beanCopy(reqBody, PayOnlineWithdraw::new);
        payOnlineWithdraw.setUpdatedAt(DateNewUtils.now());
        boolean b = payOnlineWithdrawServiceImpl.updateById(payOnlineWithdraw);
        userCache.updateUserPayoutOnLineList();
        return b;

    }

    @Override
    public Boolean addPayoutOnLine(PayManager.PayoutOnLineAddReqBody reqBody) {
        PayOnlineWithdraw payOnlineWithdraw = BeanConvertUtils.beanCopy(reqBody, PayOnlineWithdraw::new);
        payOnlineWithdraw.setCreatedAt(DateNewUtils.now());
        payOnlineWithdraw.setUpdatedAt(DateNewUtils.now());
        return payOnlineWithdrawServiceImpl.save(payOnlineWithdraw);
    }

    @Override
    public List<PayManager.PayoutBankList> listBank(PayManager.ListBankReqDto dto) {
        var wrapper = new QueryWrapper<PayBankList>();
        UserLevel userLevel = new UserLevel();
        if (nonNull(dto.getUid()))
            userLevel = userCache.getUserLevelById(userCache.getUserInfoById(dto.getUid()).getLevelId());
        if (nonNull(dto.getBankId()) && nonNull(userLevel)) {
            BankList bankList = bankCache.getBankCache(dto.getBankId());
            if (nonNull(bankList)) {
                wrapper.eq(nonNull(bankList.getCode()), "code", bankList.getCode())
                        .eq("status", 1).eq("country", toUpperCaseFirstOne(configCache.getCountry()));
            }
        }
        List<PayBankList> payBankLists = !payBankListServiceImpl.list(wrapper).isEmpty() ? payBankListServiceImpl.list(wrapper)
                : payBankListServiceImpl.list(new QueryWrapper<PayBankList>().eq("status", 1)
                .eq("country", toUpperCaseFirstOne(configCache.getCountry())));
        List<PayManager.PayoutList> payoutList = new ArrayList<>();
        List<PayManager.PayoutBankList> payoutBankList = new ArrayList<>();
        PayManager.PayoutBankList payoutBanks = new PayManager.PayoutBankList();
        List<UserLevel> userLevelList = userCache.getUserLevelList();
        List<PayManager.LevelBitBo> levelBitAllList = BeanConvertUtils.copyListProperties(userLevelList, PayManager.LevelBitBo::new);
        Set<String> collect = payBankLists.stream().map(PayBankList::getPayoutCode).collect(Collectors.toSet());
        List<PayOnlineWithdraw> payOnlineList = payOnlineWithdrawServiceImpl.lambdaQuery().in(PayOnlineWithdraw::getCode, collect)
                .eq(PayOnlineWithdraw::getStatus, 1).apply(nonNull(userLevel.getBitCode()), "level_bit & " + userLevel.getBitCode()).list();
        PayManager.PayoutList payout = null;
        if (!CollectionUtils.isEmpty(payOnlineList)) {
            for (var v : collect) {
                for (var x : payOnlineList) {
                    if (x.getCode().equals(v)) {
                        payout = BeanConvertUtils.copyProperties(x, PayManager.PayoutList::new, (sb, bo) -> {
                            bo.setBankLists(BeanConvertUtils.copyListProperties(payBankLists.stream().filter(j -> j.getPayoutCode().equals(sb.getCode())).collect(Collectors.toList()), PayManager.BankList::new));
                            bo.setPayoutChannel(sb.getPayName());
                            bo.setLevelBitCurrentList(getLevelBitCurrentList(sb.getLevelBit()));
                            bo.setLevelBitAllList(levelBitAllList);
                        });
                        payoutList.add(payout);
                        payoutBanks.setPayoutList(payoutList);
                    }
                }
                payoutBanks.setPayoutCode(v);
                payoutBankList.add(payoutBanks);
                payoutList = new ArrayList<>();
                payoutBanks = new PayManager.PayoutBankList();
            }
        } else {
            throw new BusinessException(CodeInfo.PAY_PLAT_INVALID);
        }
        return payoutBankList;
    }

    @Override
    public List<PayManager.PayoutPayNameListResBody> payoutPayNameList() {
        return BeanConvertUtils.copyListProperties(payOnlineWithdrawServiceImpl.lambdaQuery().select(PayOnlineWithdraw::getPayName).eq(PayOnlineWithdraw::getStatus, 1).list(), PayManager.PayoutPayNameListResBody::new);
    }

    public static String toUpperCaseFirstOne(String s) {
        if (Character.isUpperCase(s.charAt(0))) {
            return s;
        } else {
            return (new StringBuilder()).append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).toString();
        }
    }

    /**
     * 截取相对路径
     *
     * @param path 全路径
     * @return 相对路径
     */
    public String subStringPath(String path) {
        if (StringUtils.isBlank(path)) {
            return path;
        }
        if (path.startsWith(configCache.getStaticServer())) {
            path = path.substring(configCache.getStaticServer().length());
        }
        return path;
    }


}
