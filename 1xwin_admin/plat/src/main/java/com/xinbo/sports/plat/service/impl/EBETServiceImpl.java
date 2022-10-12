package com.xinbo.sports.plat.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinbo.sports.dao.generator.po.*;
import com.xinbo.sports.dao.generator.service.BetslipsEbetService;
import com.xinbo.sports.plat.factory.PlatAbstractFactory;
import com.xinbo.sports.plat.io.bo.*;
import com.xinbo.sports.plat.io.enums.BasePlatParam;
import com.xinbo.sports.plat.io.enums.EBETPlatEnum;
import com.xinbo.sports.service.base.CoinPlatTransfersBase;
import com.xinbo.sports.service.base.UserServiceBase;
import com.xinbo.sports.service.cache.KeyConstant;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.service.common.RedisConstant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.utils.*;
import com.xinbo.sports.utils.components.response.CodeInfo;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import static com.alibaba.fastjson.JSON.parseObject;
import static com.alibaba.fastjson.JSON.toJSONString;
import static com.xinbo.sports.plat.io.enums.EBETPlatEnum.EBETMethodEnum.*;
import static com.xinbo.sports.utils.components.response.CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_1;

@Slf4j
@Service("EBETServiceImpl")
public class EBETServiceImpl implements PlatAbstractFactory {
    protected static final String MODEL = "EBET";
    protected static final int SUCCESS = 0;
    @Resource
    protected ConfigCache configCache;

    @Resource
    private CoinPlatTransfersBase coinPlatTransfersBase;
    /*获取参数配置*/
    @Setter
    public EBETPlatEnum.PlatConfig config;

    @Resource
    private BetslipsEbetService betslipsEbetServiceImpl;

    @Resource
    private JedisUtil jedisUtil;

    @Resource
    private UserServiceBase userServiceBase;

    @Override
    public Boolean registerUser(PlatFactoryParams.PlatRegisterReqDto reqDto) {
        try {
            var userinfo=EBETRequestParameter.Register.builder()
                    .username(reqDto.getUsername())
                    .channelId(config.getChannelId())
                    .currency(config.getCurrency())
                    .signature(sign(reqDto.getUsername().getBytes(), config.getPrivate_key()))
                    .build();
            var result=HttpUtils.doPost(config.getApiUrl()+ EBETPlatEnum.EBETMethodEnum.LOGINREQUEST.getMethodName(), JSONObject.toJSONString(userinfo),"REGISTERUSER");
            JSONObject object = parseObject(result);
            if(200==object.getInteger("status")){
                return true;
            }
        }catch (Exception e){

        }
        return false;
    }

    /**
     * 获取游戏登录链接
     *
     * @param reqDto {username, lang, device, slotId}
     * @return 登录链接
     */
    @Override
    public PlatFactoryParams.PlatGameLoginResDto login(PlatFactoryParams.PlatLoginReqDto reqDto) {
        try {
            String username = reqDto.getUsername();
            String key = RandomStringUtils.randomAlphanumeric(6);
            String token = sign(key.getBytes(), config.getPrivate_key());
            String url = config.getLoginUrl() +
                    "?username=" + username
                    + "&accessToken=" +token;
            return PlatFactoryParams.PlatGameLoginResDto.builder().type(1).url(url).build();
        }catch (Exception e){
            log.error(e.toString());
            throw new BusinessException(CodeInfo.LOGIN_INVALID);
        }

    }

    @Override
    public void pullBetsLips() {
        try {
            String pullEndTime;
            var pullStartTime = jedisUtil.hget(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH, RedisConstant.EBET);
            XxlJobLogger.log(MODEL + "进入拉单==================");
            if (Strings.isEmpty(pullStartTime)) {
                var one = betslipsEbetServiceImpl.getOne(new LambdaQueryWrapper<BetslipsEbet>()
                        .orderByDesc(BetslipsEbet::getCreatedAt), false);
                if(one != null && one.getCreatedAt() != null){
                    pullStartTime=LocalDateUtils.longTransferString(one.getCreatedAt().longValue(),DateUtils.YYYYMMDDHHMMSS);
                    //pullEndTime = LocalDateUtils.addMins(pullStartTime,5);
                    pullEndTime = LocalDateUtils.getNowString();
                }else {
                    pullStartTime=DateUtils.longToString(DateNewUtils.now());
                    pullEndTime = LocalDateUtils.getNowString();
                }
            }else {
                pullEndTime = LocalDateUtils.getNowString();
            }
            Long timeMillis = System.currentTimeMillis();
            var pullBetsDataBO = EBETRequestParameter.GetHistoryForSpecificTimeRange.builder()
                    .channelId(config.getChannelId())
                    .currency(config.getCurrency())
                    .startTimeStr(pullStartTime)
                    .endTimeStr(pullEndTime)
                    .pageNum(EBETRequestParameter.PAGE_NUM)
                    .pageSize(EBETRequestParameter.PAGE_SIZE)
                    .timestamp(timeMillis)
                    .betstatus(1)
                    .signature(sign(timeMillis.toString().getBytes(),config.getPrivate_key()))
                    .build();
            int num=0;
            while (true) {
                var call = HttpUtils.doPost(config.getApiUrl() + GETBETSDETAILS.getMethodName(), JSONObject.toJSONString(pullBetsDataBO), "EBETBETSLIPS");
                XxlJobLogger.log(MODEL + "进入拉单:" + call+",参数:"+JSONObject.toJSONString(pullBetsDataBO));
                JSONObject result = parseObject(call);
                if (result.getInteger("status") == 200 && result.getJSONArray("betHistories").size() > 0) {
                    Integer count = result.getInteger("count");
                    count=num==0?count:count-num*EBETRequestParameter.PAGE_SIZE;
                    insertEBETData(result);
                    if(count.equals(EBETRequestParameter.PAGE_SIZE) ||count/EBETRequestParameter.PAGE_SIZE==0){
                        jedisUtil.hset(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH, RedisConstant.EBET, pullEndTime);
                        break;
                    }
                    ++num;
                    pullBetsDataBO.setPageNum(pullBetsDataBO.getPageNum()+1);
                }else {
                    break;
                }

            }
        }catch (Exception e){
              e.printStackTrace();
        }

    }



    private void insertEBETData(JSONObject result) {
        JSONArray jsonArray = result.getJSONArray("betHistories");
        List<BetslipsEbet> betslipsEbetList = new ArrayList<>();
        Map<String, Integer> userMap = userServiceBase.getUsernameIdMap();
        if (!jsonArray.isEmpty()) {
            for (var x : jsonArray) {
                var betDetailBo = parseObject(toJSONString(x), EBETRequestParameter.BetDetail.class);
                var userName = userServiceBase.filterUsername(betDetailBo.getUsername());
                var uid = userMap.get(userName);
                if (StringUtils.isEmpty(userName) || uid == null) {
                    continue;
                }
                var retBetslipsPg = BeanConvertUtils.copyProperties(betDetailBo, BetslipsEbet::new, (bo, sb) -> {
                    sb.setXbUid(uid);
                    sb.setXbUsername(userName);
                    sb.setGameType(bo.getGameType());
                    sb.setGameName(bo.getGameName());
                    sb.setBet(bo.getBet());
                    sb.setRoundNo(bo.getRoundNo());
                    sb.setPayout(bo.getPayout());
                    sb.setPayoutWithoutholding(bo.getPayoutWithoutholding());
                    sb.setCreateTime(bo.getCreateTime());
                    sb.setPayoutTime(bo.getPayoutTime());
                    sb.setBetHistoryId(bo.getBetHistoryId());
                    sb.setValidBet(bo.getValidBet());
                    sb.setBalance(bo.getBalance());
                    sb.setUsername(bo.getUsername());
                    sb.setUserId(bo.getUserId());
                    sb.setPlatform(bo.getPlatform());
                    sb.setXbCoin(bo.getBet());
                    sb.setXbValidCoin(bo.getValidBet());
                    sb.setXbProfit(bo.getBalance());
                    sb.setXbStatus(transferStatus(bo.getBalance()));
                    sb.setCreatedAt(bo.getCreateTime());
                    sb.setUpdatedAt(bo.getPayoutTime());
                    sb.setPlayerResult(bo.getPlayerResult());
                    sb.setBankerResult(bo.getBankerResult());
                    sb.setDragonCard(bo.getDragonCard());
                    sb.setTigerCard(bo.getTigerCard());
                    sb.setNumber(bo.getNumber());
                });
                betslipsEbetList.add(retBetslipsPg);
            }
            if(!CollectionUtils.isEmpty(betslipsEbetList)) {
                betslipsEbetServiceImpl.saveOrUpdateBatch(betslipsEbetList, betslipsEbetList.size());
            }
        } else if (jsonArray.isEmpty()) {
            log.info(MODEL + "当前无新注单");
        } else {
            XxlJobLogger.log(MODEL + "失败!" + result.toJSONString());
            PLAT_BET_SLIPS_EXCEPTION_CATEGORY_1.setMsg(result.toJSONString());
            throw new BusinessException(PLAT_BET_SLIPS_EXCEPTION_CATEGORY_1);
        }
    }

    public Integer transferStatus(BigDecimal ticketStatus) {
        //状态码
        Integer status;
        if (ticketStatus.equals(BigDecimal.ZERO)) {
            status = BasePlatParam.BetRecordsStatus.DRAW.getCode();
        } else if (ticketStatus.compareTo(BigDecimal.ZERO) < 0) {
            status = BasePlatParam.BetRecordsStatus.LOSE.getCode();
        } else {
            status = BasePlatParam.BetRecordsStatus.WIN.getCode();
        }
        return status;
    }




    @Override
    public PlatFactoryParams.PlatLogoutResDto logout(PlatFactoryParams.PlatLogoutReqDto dto) {
        return null;
    }

    private void userinfo(String userName){
        try {
            Long timeMillis = System.currentTimeMillis();
            var userinfo=EBETRequestParameter.GetUserInfo.builder()
                    .timestamp(timeMillis)
                    .channelId(config.getChannelId())
                    .currency(config.getCurrency())
                    .username(userName)
                    .signature(sign((userName + timeMillis).getBytes(), config.getPrivate_key()))
                    .build();
            var result=HttpUtils.doPost(config.getApiUrl()+ EBETPlatEnum.EBETMethodEnum.USERINFO.getMethodName(), JSONObject.toJSONString(userinfo),"USERINFO");
        }catch (Exception e){

        }

    }

    @Override
    public PlatFactoryParams.PlatCoinTransferResDto coinUp(PlatFactoryParams.PlatCoinTransferReqDto platCoinTransferUpReqDto) {
        Long timeMillis = System.currentTimeMillis();
        JSONObject result;
        try {
            var depositBo = EBETRequestParameter.FundTransfer.builder()
                    .currency(config.getCurrency())
                    .username(platCoinTransferUpReqDto.getUsername())
                    .rechargeReqId(platCoinTransferUpReqDto.getOrderId())
                    .money(platCoinTransferUpReqDto.getCoin())
                    .channelId(config.getChannelId())
                    .timestamp(timeMillis)
                    .signature(sign((platCoinTransferUpReqDto.getUsername() + timeMillis).getBytes(), config.getPrivate_key()))
                    .build();
            var call = HttpUtils.doPost(config.getApiUrl() + DEPOSIT.getMethodName(), JSONObject.toJSONString(depositBo),"RECHARGE");
            platCoinTransferUpReqDto.setCoin(platCoinTransferUpReqDto.getCoin().abs());
             result = parseObject(call);
            if (200 == result.getInteger("status")) {
                depositBo.setCoin(platCoinTransferUpReqDto.getCoin());
                if(checkRechargestatus(depositBo)) {
                    return PlatFactoryParams.PlatCoinTransferResDto.builder().platCoin(result.getBigDecimal("money")).build();
                }
            } else {
                coinPlatTransfersBase.updateOrderPlat(platCoinTransferUpReqDto.getOrderId(), 2, platCoinTransferUpReqDto.getCoin(), "", EBETPlatEnum.MAP.getOrDefault(result.get("status"),CodeInfo.PLAT_SYSTEM_ERROR).getMsg());
            }
        }catch (Exception e) {
            throw new BusinessException(CodeInfo.PLAT_SYSTEM_ERROR);
        }
        CodeInfo codeInfo = EBETPlatEnum.MAP.getOrDefault(result.get("status"), CodeInfo.PLAT_SYSTEM_ERROR);
        throw new BusinessException(codeInfo);
    }


    private boolean checkRechargestatus(EBETRequestParameter.FundTransfer fundTransfer){
        try {
            var depositBo = EBETRequestParameter.FundTransferStatus.builder()
                    .currency(fundTransfer.getCurrency())
                    .rechargeReqId(fundTransfer.getRechargeReqId())
                    .channelId(fundTransfer.getChannelId())
                    .signature(sign(fundTransfer.getRechargeReqId().getBytes(), config.getPrivate_key()))
                    .build();
            var call = HttpUtils.doPost(config.getApiUrl() + DEPOSITSTATUS.getMethodName(), JSONObject.toJSONString(depositBo),"CHECKRECHARGESTATUS");
            if (200 == parseObject(call).getInteger("status")) {
                coinPlatTransfersBase.updateOrderPlat(fundTransfer.getRechargeReqId(), 1, fundTransfer.getCoin(), null, null);
               return true;
            } else {
                coinPlatTransfersBase.updateOrderPlat(fundTransfer.getRechargeReqId(), 2, fundTransfer.getCoin(), "", null);
                throw new BusinessException( CodeInfo.COIN_TRANSFER_SF_ERROR);
            }
        }catch (Exception e){

        }
        return false;


    }





    @Override
    public PlatFactoryParams.PlatCoinTransferResDto coinDown(PlatFactoryParams.PlatCoinTransferReqDto platCoinTransferDownReqDto) {
        BigDecimal coin = platCoinTransferDownReqDto.getCoin().negate();
        platCoinTransferDownReqDto.setCoin(coin);
        PlatFactoryParams.PlatCoinTransferResDto resDto = coinUp(platCoinTransferDownReqDto);
        return resDto;
    }

    @Override
    public PlatFactoryParams.PlatQueryBalanceResDto queryBalance(PlatFactoryParams.PlatQueryBalanceReqDto platQueryBalanceReqDto) {
        try {
            var balanceBO = EBETRequestParameter.QueryBalance.builder()
                    .channelId(config.getChannelId())
                    .username(platQueryBalanceReqDto.getUsername())
                    .signature(sign(platQueryBalanceReqDto.getUsername().getBytes(),config.getPrivate_key()))
                    .minMoney(0)
                    .currency(config.getCurrency())
                    .build();
            String call = HttpUtils.doPost(config.getApiUrl()+ EBETPlatEnum.EBETMethodEnum.BALANCE.getMethodName(), JSONObject.toJSONString(balanceBO),"EBETRequest");
            var status = parseObject(call).getInteger("status");
            if (200 == status) {
                BigDecimal coin=BigDecimal.ZERO;
                JSONArray results = parseObject(call).getJSONArray("results");
                if(results.size()>0){
                    JSONObject jsonObject = results.getJSONObject(0);
                    coin=jsonObject.getBigDecimal("money");
                }
                return PlatFactoryParams.PlatQueryBalanceResDto.builder().platCoin(coin).build();
            } else {
                throw new BusinessException(CodeInfo.COIN_QUERY_INVALID);
            }
        }catch (Exception e){
            log.error(e.getMessage());
            throw new BusinessException(CodeInfo.COIN_QUERY_INVALID);
        }
    }




    @Override
    public Boolean checkTransferStatus(String orderId) {
        try {
            var depositBo = EBETRequestParameter.FundTransferStatus.builder()
                    .currency(config.getCurrency())
                    .rechargeReqId(orderId)
                    .channelId(config.getChannelId())
                    .signature(sign(orderId.getBytes(), config.getPrivate_key()))
                    .build();
            var call = HttpUtils.doPost(config.getApiUrl() + DEPOSITSTATUS.getMethodName(), JSONObject.toJSONString(depositBo),"CHECKTRANSFERSTATUS");
            if (200 == parseObject(call).getInteger("status")) {
                return true;
            }
        }catch (Exception e){
            log.error(e.getMessage());
            throw new BusinessException(CodeInfo.USER_TRANSACTION_RECORD_LIST_ERROR);
        }
        return false;
    }


    //公钥匙验证
    public static boolean verify(byte[] data,String publicKey,String sign)throws Exception{
        byte[] keyBytes = Base64.getDecoder().decode(publicKey.getBytes());
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey2 = keyFactory.generatePublic(x509EncodedKeySpec);
        Signature signature = Signature.getInstance("MD5withRSA");
        signature.initVerify(publicKey2);
        signature.update(data);
        return signature.verify(Base64.getDecoder().decode(sign));
    }



    //私钥匙加密
    public static String sign(byte[] data,String privateKey)throws NoSuchAlgorithmException,
            InvalidKeySpecException, InvalidKeyException, SignatureException
    {
        byte[] keyBytes = Base64.getDecoder().decode(privateKey.getBytes());
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey priKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        Signature signature = Signature.getInstance("MD5withRSA");
        signature.initSign(priKey);
        signature.update(data);
        return new String(Base64.getEncoder().encode(signature.sign()));
    }

}
