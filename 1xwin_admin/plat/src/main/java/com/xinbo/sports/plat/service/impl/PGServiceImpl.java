package com.xinbo.sports.plat.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinbo.sports.dao.generator.po.BetslipsPgChess;
import com.xinbo.sports.dao.generator.po.BetslipsPgGame;
import com.xinbo.sports.dao.generator.po.DictItem;
import com.xinbo.sports.dao.generator.po.GameSlot;
import com.xinbo.sports.dao.generator.service.BetslipsPgChessService;
import com.xinbo.sports.dao.generator.service.BetslipsPgGameService;
import com.xinbo.sports.dao.generator.service.DictItemService;
import com.xinbo.sports.dao.generator.service.GameSlotService;
import com.xinbo.sports.plat.base.SlotServiceBase;
import com.xinbo.sports.plat.factory.ISuppleMethods;
import com.xinbo.sports.plat.factory.PlatSlotAbstractFactory;
import com.xinbo.sports.plat.io.bo.PGRequestParameter;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams;
import com.xinbo.sports.plat.io.enums.BasePlatParam;
import com.xinbo.sports.plat.io.enums.PGPlatEnum;
import com.xinbo.sports.service.base.CoinPlatTransfersBase;
import com.xinbo.sports.service.base.UserServiceBase;
import com.xinbo.sports.service.cache.KeyConstant;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.service.common.RedisConstant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.utils.*;
import com.xinbo.sports.utils.components.pagination.ReqPage;
import com.xinbo.sports.utils.components.response.CodeInfo;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.alibaba.fastjson.JSON.toJSONString;
import static com.xinbo.sports.plat.io.enums.PGPlatEnum.METHOD_MAP;
import static com.xinbo.sports.plat.io.enums.PGPlatEnum.PGMethodEnum.*;
import static com.xinbo.sports.utils.components.response.CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_1;

@Slf4j
@Service("PGServiceImpl")
public class PGServiceImpl implements PlatSlotAbstractFactory, ISuppleMethods {
    protected static final String MODEL = "PG";
    protected static final String ERROR = "error";
    protected static final String CODE = "code";
    @Resource
    private CoinPlatTransfersBase coinPlatTransfersBase;
    @Resource
    private JedisUtil jedisUtil;
    @Resource
    private UserServiceBase userServiceBase;
    @Resource
    private GameSlotService gameSlotServiceImpl;
    @Autowired
    private SlotServiceBase slotServiceBase;
    @Resource
    private ConfigCache configCache;
    @Resource
    private BetslipsPgGameService betslipsPgGameServiceImpl;
    @Resource
    private BetslipsPgChessService betslipsPgChessServiceImpl;
    @Resource
    private DictItemService dictItemServiceImpl;
    /*获取参数配置*/
    @Setter
    public PGPlatEnum.PlatConfig config;
    RestTemplate restTemplate = new RestTemplate();


    @Override
    public Boolean registerUser(PlatFactoryParams.PlatRegisterReqDto reqDto) {
        var registerBO = PGRequestParameter.Register.builder()
                .playerName(reqDto.getUsername())
                .secretKey(config.getSecretKey())
                .currency(config.getCurrency())
                .operatorToken(config.getOperatorToken())
                .build();
        var call = send(config.getApiUrl() + CREATE_PLAYER.getMethodName(), registerBO);
        var error = parseObject(call).getString(ERROR);
        if (null == error) {
            return true;
        } else {
            throw new BusinessException(METHOD_MAP.get(CREATE_PLAYER.getMethodNameDesc()).getOrDefault(parseObject(call).getJSONObject(ERROR).getInteger(CODE), CodeInfo.PLAT_SYSTEM_ERROR));
        }
    }

    public String send(String url, Object bo) {
        HttpHeaders headers = new HttpHeaders();
        MultiValueMap<String, Object> paramsMap = new LinkedMultiValueMap<>();
        Map<String, Object> itemMap = JSON.toJavaObject(parseObject(toJSONString(bo)), Map.class);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        paramsMap.setAll(itemMap);
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(paramsMap, headers);
        log.info("请求参数：" + httpEntity);
        var result = restTemplate.postForObject(url, httpEntity, String.class);
        log.info(MODEL + "三方返回内容" + result);
        return result;
    }


    @SneakyThrows
    @Override
    public PlatFactoryParams.PlatGameLoginResDto login(PlatFactoryParams.PlatLoginReqDto platLoginReqDto) {
        var lang = EnumUtils.getEnumIgnoreCase(PGRequestParameter.LANGS.class, platLoginReqDto.getLang()).getCode();
        DESCUtils descUtils = new DESCUtils(config.getOperatorToken());
        String encrypt = descUtils.encrypt(platLoginReqDto.getUsername());
        String url = config.getLaunchUrl() + URL_SCHEME.getMethodName()
                .replace("{GameCode}", platLoginReqDto.getSlotId())
                + "?operator_token=" + config.getOperatorToken()
                + "&operator_player_session=" + URLEncoder.encode(encrypt) + "&bet_type=1&language=" + lang;
        return PlatFactoryParams.PlatGameLoginResDto.builder().type(1).url(url).build();
    }

    @Override
    public PlatFactoryParams.PlatLogoutResDto logout(PlatFactoryParams.PlatLogoutReqDto dto) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PlatFactoryParams.PlatCoinTransferResDto coinUp(PlatFactoryParams.PlatCoinTransferReqDto platCoinTransferUpReqDto) {
        var depositBo = PGRequestParameter.FundTransfer.builder()
                .operatorToken(config.getOperatorToken())
                .secretKey(config.getSecretKey())
                .currency(config.getCurrency())
                .playerName(platCoinTransferUpReqDto.getUsername())
                .transferReference(platCoinTransferUpReqDto.getOrderId())
                .amount(platCoinTransferUpReqDto.getCoin())
                .build();
        var call = send(config.getApiUrl() + DEPOSIT.getMethodName(), depositBo);
        if (null == parseObject(call).getString(ERROR)) {
            BigDecimal afterAmount = parseObject(call).getJSONObject("data").getBigDecimal("balanceAmount");
            coinPlatTransfersBase.updateOrderPlat(platCoinTransferUpReqDto.getOrderId(), 1, platCoinTransferUpReqDto.getCoin(), null, null);
            return PlatFactoryParams.PlatCoinTransferResDto.builder().platCoin(afterAmount).build();
        } else {
            coinPlatTransfersBase.updateOrderPlat(platCoinTransferUpReqDto.getOrderId(), 2, platCoinTransferUpReqDto.getCoin(), "", parseObject(call).getJSONObject(ERROR).getString("message"));
            throw new BusinessException(METHOD_MAP.get(DEPOSIT.getMethodNameDesc()).getOrDefault(parseObject(call).getJSONObject(ERROR).getInteger(CODE), CodeInfo.PLAT_SYSTEM_ERROR));
        }
    }

    @Override
    public PlatFactoryParams.PlatCoinTransferResDto coinDown(PlatFactoryParams.PlatCoinTransferReqDto platCoinTransferDownReqDto) {
        var withdrawBO = PGRequestParameter.FundTransfer.builder()
                .operatorToken(config.getOperatorToken())
                .secretKey(config.getSecretKey())
                .currency(config.getCurrency())
                .playerName(platCoinTransferDownReqDto.getUsername())
                .transferReference(platCoinTransferDownReqDto.getOrderId())
                .amount(platCoinTransferDownReqDto.getCoin())
                .build();
        var call = send(config.getApiUrl() + WITHDRAW.getMethodName(), withdrawBO);
        if (null == parseObject(call).getString(ERROR)) {
            BigDecimal afterAmount = parseObject(call).getJSONObject("data").getBigDecimal("balanceAmount");
            coinPlatTransfersBase.updateOrderPlat(platCoinTransferDownReqDto.getOrderId(), 1, platCoinTransferDownReqDto.getCoin(), null, null);
            return PlatFactoryParams.PlatCoinTransferResDto.builder().platCoin(afterAmount).build();
        } else {
            coinPlatTransfersBase.updateOrderPlat(platCoinTransferDownReqDto.getOrderId(), 2, platCoinTransferDownReqDto.getCoin(), "", parseObject(call).getJSONObject(ERROR).getString("message"));
            throw new BusinessException(METHOD_MAP.get(DEPOSIT.getMethodNameDesc()).getOrDefault(parseObject(call).getJSONObject(ERROR).getInteger(CODE), CodeInfo.PLAT_SYSTEM_ERROR));
        }
    }

    @Override
    public PlatFactoryParams.PlatQueryBalanceResDto queryBalance(PlatFactoryParams.PlatQueryBalanceReqDto platQueryBalanceReqDto) {
        var balanceBO = PGRequestParameter.QueryBalance.builder()
                .operatorToken(config.getOperatorToken())
                .secretKey(config.getSecretKey())
                .playerName(platQueryBalanceReqDto.getUsername())
                .build();
        String call = send(config.getApiUrl() + GET_BALANCE.getMethodName(), balanceBO);
        var error = parseObject(call).getString(ERROR);
        if (null == error) {
            return PlatFactoryParams.PlatQueryBalanceResDto.builder().platCoin(parseObject(call).getJSONObject("data").getBigDecimal("cashBalance")).build();
        } else {
            throw new BusinessException(METHOD_MAP.get(GET_BALANCE.getMethodNameDesc()).getOrDefault(parseObject(call).getJSONObject(ERROR).getInteger(CODE), CodeInfo.PLAT_SYSTEM_ERROR));
        }
    }

    @Override
    public void pullBetsLips() {
        var pullStartTime = jedisUtil.hget(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH, RedisConstant.PG);
        XxlJobLogger.log(MODEL + "进入拉单==================");
        if (Strings.isEmpty(pullStartTime)) {
            var one = betslipsPgGameServiceImpl.getOne(new LambdaQueryWrapper<BetslipsPgGame>()
                    .orderByDesc(BetslipsPgGame::getCreatedAt), false);
            pullStartTime = (one != null && one.getCreatedAt() != null) ? String.valueOf(one.getCreatedAt()) : String.valueOf(DateNewUtils.now() - 30 * 60);
        }
        var pullEndTime = Math.min((Integer.parseInt(pullStartTime) + 60 * 30), (DateNewUtils.now() - 10 * 60));
        var pullBetsDataBO = PGRequestParameter.GetHistoryForSpecificTimeRange.builder()
                .operator_token(config.getOperatorToken())
                .secret_key(config.getSecretKey())
                .bet_type(1)
                .count(1500)
                .from_time(Long.parseLong(pullStartTime)*1000)
                .to_time(Long.parseLong(pullEndTime + "")*1000)
                .build();
        var call = send(config.getDataUrl() + GetHistoryForSpecificTimeRange.getMethodName(), pullBetsDataBO);
        XxlJobLogger.log(MODEL + "进入拉单:" + call);
        if (call != null) {
            insertPGData(call);
            jedisUtil.hset(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH, RedisConstant.PG, String.valueOf(pullEndTime));
        }
    }

    private void insertPGData(String call) {
        JSONArray jsonArray = parseObject(call).getJSONArray("data");
        List<BetslipsPgGame> betslipsPgGames = new ArrayList<>();
        Map<String, Integer> userMap = userServiceBase.getUsernameIdMap();
        if (null != jsonArray || !jsonArray.isEmpty()) {
            for (var x : jsonArray) {
                var betDetailBo = parseObject(toJSONString(x), PGRequestParameter.BetDetail.class);
                var userName = userServiceBase.filterUsername(betDetailBo.getPlayerName());
                var uid = userMap.get(userName);
                if (StringUtils.isEmpty(userName) || uid == null) {
                    continue;
                }
                var retBetslipsPg = BeanConvertUtils.copyProperties(betDetailBo, BetslipsPgGame::new, (bo, sb) -> {
                    sb.setXbUsername(userName);
                    sb.setXbUid(uid);
                    sb.setXbCoin(bo.getBetAmount());
                    sb.setXbValidCoin(bo.getBetAmount());
                    sb.setXbStatus(transferStatus(bo.getWinAmount().subtract(bo.getBetAmount())));
                    sb.setXbProfit(bo.getWinAmount().subtract(bo.getBetAmount()));
                    sb.setCreatedAt((int) (Long.parseLong(bo.getBetTime()) / 1000));
                    sb.setUpdatedAt((int) (Long.parseLong(bo.getBetEndTime()) / 1000));
                });
                betslipsPgGames.add(retBetslipsPg);
            }
            Map<String, String> referId = dictItemServiceImpl.list(new QueryWrapper<DictItem>().eq("refer_id", "142")).stream().collect(Collectors.toMap(DictItem::getCode, DictItem::getTitle));
            List<BetslipsPgGame> pokerList = betslipsPgGames.stream().filter(x -> referId.get(x.getGameId().toString()) != null).collect(Collectors.toList());
            List<BetslipsPgGame> gameList = betslipsPgGames.stream().filter(x -> referId.get(x.getGameId().toString()) == null).collect(Collectors.toList());
            List<BetslipsPgChess> betslipsPgChesses = BeanConvertUtils.copyListProperties(pokerList, BetslipsPgChess::new);
            for (BetslipsPgChess a : betslipsPgChesses) {
                BetslipsPgChess one = betslipsPgChessServiceImpl.getById(a.getId());
                if (one != null) {
                    betslipsPgChessServiceImpl.lambdaUpdate().setSql("bet_id=concat(bet_id,'," + a.getBetId() + "'),bet_amount=bet_amount+" + a.getBetAmount() + ",xb_coin=bet_amount+" + a.getBetAmount()
                            + ",xb_valid_coin=bet_amount+" + a.getBetAmount() + ",win_amount=win_amount+" + a.getWinAmount()
                            + ",xb_profit=xb_profit+" + a.getXbProfit() + ",xb_status= case when xb_profit>0 then 1 when xb_profit<0 then 2 ELSE 0 END")
                            .eq(BetslipsPgChess::getId, a.getId()).update();
                } else {
                    betslipsPgChessServiceImpl.save(a);
                }
            }
            List<BetslipsPgGame> betslipsPgGame = BeanConvertUtils.copyListProperties(gameList, BetslipsPgGame::new);
            for (BetslipsPgGame b : betslipsPgGame) {
                BetslipsPgGame one = betslipsPgGameServiceImpl.getById(b.getId());
                if (one != null) {
                    betslipsPgGameServiceImpl.lambdaUpdate().setSql("bet_id=concat(bet_id,'," + b.getBetId() + "'),bet_amount=bet_amount+" + b.getBetAmount() + ",xb_coin=bet_amount+" + b.getBetAmount()
                            + ",xb_valid_coin=bet_amount+" + b.getBetAmount() + ",win_amount=win_amount+" + b.getWinAmount()
                            + ",xb_profit=xb_profit+" + b.getXbProfit() + ",xb_status= case when xb_profit>0 then 1 when xb_profit<0 then 2 ELSE 0 END")
                            .eq(BetslipsPgGame::getId, b.getId()).update();
                } else {
                    betslipsPgGameServiceImpl.save(b);
                }
            }
        } else if (jsonArray == null) {
            log.info(MODEL + "当前无新注单");
        } else {
            XxlJobLogger.log(MODEL + "失败!" + toJSONString(call));
            PLAT_BET_SLIPS_EXCEPTION_CATEGORY_1.setMsg(toJSONString(call));
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
    public Boolean checkTransferStatus(String orderId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Page<PlatFactoryParams.PlatSlotGameResDto> getSlotGameList(ReqPage<PlatFactoryParams.PlatSlotGameReqDto> platSlotgameReqDto) {
        return slotServiceBase.getSlotGameList(platSlotgameReqDto);
    }

    @Override
    public Boolean favoriteSlotGame(PlatFactoryParams.PlatSlotGameFavoriteReqDto platSlotGameFavoriteReqDto) {
        return slotServiceBase.favoriteSlotGame(platSlotGameFavoriteReqDto);
    }

    @Override
    public void pullGames() {
        var gamesBO = PGRequestParameter.GameList.builder()
                .operatorToken(config.getOperatorToken())
                .secretKey(config.getSecretKey())
                .currency(config.getCurrency()).build();
        var call = send(config.getApiUrl() + GAME_LIST.getMethodName(), gamesBO);
        JSONArray data = parseObject(call).getJSONArray("data");
        if (data != null && !data.isEmpty()) {
            GameSlot gameSlot = new GameSlot();
            var device = 0;
            for (var x : data) {
                JSONObject jo = (JSONObject) x;
                if (jo.getInteger("status") == 1) {
                    gameSlot.setGameId(207);
                    gameSlot.setStatus(1);
                    gameSlot.setId(jo.getString("gameCode"));
                    gameSlot.setName(jo.getString("gameName"));
                    gameSlot.setImg(String.format("/icon/pg/%s.png", jo.getString("gameCode").replace(" ", "")));
                    gameSlot.setGameTypeName("slot");
                    gameSlot.setGameTypeId(jo.getString("gameId"));
                    gameSlot.setNameZh(jo.getString("gameName"));
                    gameSlot.setCreatedAt(DateUtils.getCurrentTime());
                    gameSlot.setUpdatedAt(DateUtils.getCurrentTime());
                    gameSlot.setDevice(device);
                    gameSlot.setSort(1);
                    gameSlot.setIsNew(0);
                    gameSlotServiceImpl.saveOrUpdate(gameSlot);
                }
            }
        }
    }


}