package com.xinbo.sports.plat.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinbo.sports.dao.generator.po.BetSlipsSupplemental;
import com.xinbo.sports.dao.generator.po.BetslipsTcg;
import com.xinbo.sports.dao.generator.service.BetslipsTcgService;
import com.xinbo.sports.service.base.CoinPlatTransfersBase;
import com.xinbo.sports.plat.base.CommonPersistence;
import com.xinbo.sports.plat.factory.PlatAbstractFactory;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams.*;
import com.xinbo.sports.plat.io.bo.TCGLotteryRequestParameter;
import com.xinbo.sports.plat.io.enums.BasePlatParam;
import com.xinbo.sports.plat.io.enums.TCGPlatEnum;
import com.xinbo.sports.service.base.ExceptionThreadLocal;
import com.xinbo.sports.service.base.UserServiceBase;
import com.xinbo.sports.service.cache.KeyConstant;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.service.common.RedisConstant;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.service.io.enums.BaseEnum;
import com.xinbo.sports.utils.*;
import com.xinbo.sports.utils.components.response.CodeInfo;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.*;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.alibaba.fastjson.JSON.toJSONString;
import static java.util.Objects.nonNull;


/**
 * @author: wells
 * @date: 2020/6/14
 * @description:
 */
@Slf4j
@Service("TCGServiceImpl")
public class TCGServiceImpl implements PlatAbstractFactory {
    @Autowired
    private JedisUtil jedisUtil;
    @Autowired
    private BetslipsTcgService betslipsTcgServiceImpl;
    @Resource
    private CoinPlatTransfersBase coinPlatTransfersBase;
    @Resource
    private UserServiceBase userServiceBase;

    protected static final String MODEL = "TCG";
    /*请求参数*/
    private static final String DES_PARAMS_KEY = "params";
    /*请求参数*/
    private static final String MERCHANT_CODE = "merchant_code";
    /*请求签名*/
    private static final String SIGNATURE = "sign";

    private static final String STATUS = "status";
    private static final String UTF8 = "UTF-8";
    @Setter
    TCGPlatEnum.PlatConfig config;
    @Resource
    protected ConfigCache configCache;
    @Resource
    private CommonPersistence commonPersistence;

    /**
     * 获取游戏登录链接
     *
     * @param platLoginReqDto {username, lang, device, slotId}
     * @return 登录链接
     */
    @Override
    public PlatGameLoginResDto login(PlatLoginReqDto platLoginReqDto) {

        HashMap<Object, Object> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put("game_group_code", "VNC");
        objectObjectHashMap.put("prize_mode_id", "1");
        ArrayList<Object> series = new ArrayList<>();
        series.add(0, objectObjectHashMap);

        var loginBO = TCGLotteryRequestParameter.Login.builder()//（1 =真实，0 =测试）
                .gameMode(1)
                .lotteryBetMode("SEA_Tradition")
                .method(TCGPlatEnum.TCGMethodEnum.LG.getMethodName())
                .platform(platLoginReqDto.getDevice().equals(BaseEnum.DEVICE.M.getValue()) ? "html5" : "flash")
                .productType(config.getProductType()).series(series.toArray())
                .username(platLoginReqDto.getUsername())
                .language(EnumUtils.getEnumIgnoreCase(TCGLotteryRequestParameter.LANGS.class, platLoginReqDto.getLang()).getCode())
                .gameCode(platLoginReqDto.getSlotId() == null ? "Lobby" : platLoginReqDto.getSlotId())
                .build();

        JSONObject call = send(TCGPlatEnum.TCGMethodEnum.LG, config.getApiUrl(), loginBO);
        Integer status = call.getInteger(STATUS);
        String gameUrl = null;
        if (status != null && status.equals(0)) {
            gameUrl = call.getString("game_url");
        } else {
            if (status == 15 && Boolean.TRUE.equals(registerUser(PlatRegisterReqDto.builder().username(platLoginReqDto.getUsername()).build()))) {
                return login(platLoginReqDto);
            }
            log.error("TCG会员登陆失败, error = " + toJSONString(call));
            throw new BusinessException(CodeInfo.LOGIN_INVALID);
        }
        return PlatGameLoginResDto.builder().type(1).url(config.getDomain() + gameUrl).build();
    }

    /**
     * 请求三方接口
     *
     * @param url        请求地址
     * @param object     请求体参数
     * @param methodEnum 请求方法
     * @return
     */
    private JSONObject send(TCGPlatEnum.TCGMethodEnum methodEnum, String url, Object object) {
        try {
            String decKey = config.getDesKey();
            String sha256Key = config.getSha256Key();
            DESCUtils descUtils = new DESCUtils(decKey);
            String desParams = descUtils.encrypt(toJSONString(object));
            String sign = HashUtil.sha256(desParams + sha256Key);
            String params = MERCHANT_CODE + "=" + URLEncoder.encode(config.getMerchantCode(), UTF8) + "&" + DES_PARAMS_KEY + "=" + URLEncoder.encode(desParams, UTF8) + "&" + SIGNATURE + "=" + URLEncoder.encode(sign, UTF8);
            log.info("TCG请求参数：" + params);
            String result = HttpUtils.postHttp(url, params);
            log.info("TCG三方返回内容" + result);
            return StringUtils.isEmpty(result) ? new JSONObject() : parseObject(result);
        } catch (Exception e) {
            log.error("TCG获取三方法数据失败,执行方法" + methodEnum.getMethodName() + ";" + e.getMessage());
            return new JSONObject();
        }
    }


    @Override
    public PlatLogoutResDto logout(PlatLogoutReqDto dto) {
        return null;
    }

    /**
     * 三方上分
     *
     * @param platCoinTransferUpReqDto {"coin","orderId","username"}
     * @return {platCoin}
     */
    @Override
    public PlatCoinTransferResDto coinUp(PlatCoinTransferReqDto platCoinTransferUpReqDto) {
        // 测试环境上方金额不能大于100
        if (!"PROD".equals(config.getEnvironment()) && platCoinTransferUpReqDto.getCoin().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BusinessException(CodeInfo.PLAT_COIN_UP_COIN_NOT_GT_100);
        }
        return transfer(platCoinTransferUpReqDto, 1);
    }

    /**
     * 转账
     *
     * @param dto
     * @return
     */
    private PlatCoinTransferResDto transfer(PlatCoinTransferReqDto dto, Integer transferId) {
        var transferBO = TCGLotteryRequestParameter.FundTransfer.builder()
                .method(TCGPlatEnum.TCGMethodEnum.FT.getMethodName())
                .username(dto.getUsername()).productType(config.getProductType())
                .fundType(transferId).amount(dto.getCoin()).referenceNo(dto.getOrderId()).build();
        JSONObject call = send(TCGPlatEnum.TCGMethodEnum.FT, config.getApiUrl(), transferBO);

        if (call.get(STATUS).equals(0) && call.get("transaction_status").equals("SUCCESS")) {
            BigDecimal afterAmount = queryBalance(PlatQueryBalanceReqDto.builder().username(dto.getUsername()).build()).getPlatCoin();
            coinPlatTransfersBase.updateOrderPlat(dto.getOrderId(), 1, dto.getCoin(), null, null);
            return PlatCoinTransferResDto.builder().platCoin(afterAmount).build();
        } else {
            coinPlatTransfersBase.updateOrderPlat(dto.getOrderId(), 2, dto.getCoin(), "", call.get("error_desc").toString());
            log.error("TCG转账失败:" + call.get("error_desc").toString());
            throw new BusinessException(TCGPlatEnum.map.getOrDefault(call.getInteger(STATUS), CodeInfo.PLAT_SYSTEM_ERROR));
        }
    }

    /**
     * 三方下分
     *
     * @param platCoinTransferDownReqDto {"coin","orderId","username"}
     * @return {platCoin}
     */
    @Override
    public PlatCoinTransferResDto coinDown(PlatCoinTransferReqDto platCoinTransferDownReqDto) {
        return transfer(platCoinTransferDownReqDto, 2);
    }


    /**
     * 查询三方余额
     *
     * @param platQueryBalanceReqDto {"username"}
     * @return {platCoin}
     */
    @Override
    public PlatQueryBalanceResDto queryBalance(PlatQueryBalanceReqDto platQueryBalanceReqDto) {
        var checkUserBalanceBo = TCGLotteryRequestParameter.QueryBalance.builder()
                .username(platQueryBalanceReqDto.getUsername())
                .method(TCGPlatEnum.TCGMethodEnum.GB.getMethodName())
                .productType(config.getProductType())
                .build();
        JSONObject call = send(TCGPlatEnum.TCGMethodEnum.FT, config.getApiUrl(), checkUserBalanceBo);
        Integer status = call.getInteger(STATUS);
        var balance = BigDecimal.ZERO;
        if (status != null && status.equals(0)) {
            balance = call.getBigDecimal("balance");
        }
        return PlatQueryBalanceResDto.builder().platCoin(balance).build();
    }


    /**
     * 拉取三方注单信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pullBetsLips() {
        int startDate;
        int endDate;
        String pullEndTime = jedisUtil.hget(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH, RedisConstant.TCG);
        if (Strings.isEmpty(pullEndTime)) {
            BetslipsTcg betslipsTCG = betslipsTcgServiceImpl.getOne(new LambdaQueryWrapper<BetslipsTcg>()
                    .orderByDesc(BetslipsTcg::getCreatedAt).last("limit 1"));
            startDate = (betslipsTCG != null && nonNull(betslipsTCG.getCreatedAt())) ? betslipsTCG.getCreatedAt() : DateUtils.getCurrentTime() - 24 * 60 * 60;
        } else {
            startDate = Integer.parseInt(pullEndTime);
        }
        endDate = (startDate + 5 * 60 <= DateUtils.getCurrentTime()) ? (startDate + 5 * 60) : startDate;

        pullData(startDate, endDate);
        pullSettledBetList(startDate);
    }

    /**
     * 检查转账状态
     * * {"status":0, "error_ desc": null, "transaction_status": 'SUCCESS"}
     * * PENDING = 延迟，SUCCESS = 成功，FAILED = 失败，UNKNOWN = 未知，NOT FOUND =未找到
     *
     * @author: David
     * @date: 04/05/2020
     */
    @Override
    public Boolean checkTransferStatus(String orderId) {
        var checkTransactionBO = TCGLotteryRequestParameter.CheckTransactionStatus.builder()
                .method(TCGPlatEnum.TCGMethodEnum.CS.getMethodName())
                .productType(config.getProductType())
                .refNo(orderId)
                .build();
        JSONObject call = send(TCGPlatEnum.TCGMethodEnum.CS, config.getApiUrl(), checkTransactionBO);
        if (call.size() != 0 && call.getInteger(STATUS) == 0) {
            String transactionStatus = call.getString("transaction_status");
            Map<String, Integer> transferStatusMap = Map.of("SUCCESS", 1, "FAILED", 2, "NOT FOUND", 2, "UNKNOWN", 3, "PENDING", 3);
            if (transferStatusMap.get(transactionStatus).equals(1)) {
                return true;
            } else if (transferStatusMap.get(transactionStatus).equals(2)) {
                return false;
            } else {
                throw new BusinessException(CodeInfo.PLAT_SYSTEM_ERROR);
            }
        } else {
            throw new BusinessException(TCGPlatEnum.map.getOrDefault(call.getInteger(STATUS), CodeInfo.PLAT_SYSTEM_ERROR));
        }
    }

    @Override
    public Boolean registerUser(PlatRegisterReqDto platLoginReqDto) {
        var registerBO = TCGLotteryRequestParameter.Register.builder()
                .username(platLoginReqDto.getUsername())
                .method(TCGPlatEnum.TCGMethodEnum.CM.getMethodName())
                .password(MD5.encryption(platLoginReqDto.getUsername()).substring(0, 12))
                .currency(EnumUtils.getEnumIgnoreCase(TCGLotteryRequestParameter.CURRENCY.class, config.getCurrency()).getCode()).build();

        JSONObject call = send(TCGPlatEnum.TCGMethodEnum.CM, config.getApiUrl(), registerBO);
        Integer status = call.getInteger(STATUS);
        if (status != null && status.equals(0)) {
            log.info("TCG会员创建成功,  " + toJSONString(call));
            return true;
        } else {
            log.error("TCG会员创建失败, error = " + toJSONString(call));
            throw new BusinessException(CodeInfo.REGISTER_INVALID);
        }
    }

    /**
     * ftp拉取已结算注单
     *
     * @param startDate
     */
    private void pullSettledBetList(int startDate) {
        InputStream in = null;
        BufferedReader br = null;
        String path = null;
        String data = null;
        String startPath = DateUtils.yyyyMMdd2(startDate);
        Integer mark = 0;
        try {
            XxlJobLogger.log("TCG拉取进入结算注单:" + startPath);
            String lastFileName = jedisUtil.hget(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH, RedisConstant.TCG + ":file");
            if (lastFileName != null && lastFileName.split("/_")[0].split("/")[3].equals(DateUtils.yyyyMMdd2(startDate))) {
                mark = Integer.valueOf(lastFileName.split("_")[1]);
                if (lastFileName.split("_")[2].endsWith("2355")) {
                    startPath = addDay(DateUtils.yyyyMMdd2(startDate));
                    mark = 0;
                }
            }
            path = TCGPlatEnum.TCGMethodEnum.GETSETTLEDBET.getMethodName() + String.format("/%s/", startPath);
            FTPClient ftpClient = FTPUtils.getFTPClient(config.getFtpHost(),
                    config.getFtpUser(), config.getFtpPassword());
            FTPFile[] files = FTPUtils.getFTPDirectoryFiles(ftpClient, path);
            if (files != null && files.length > 0) {
                for (int i = mark; i < files.length; i++) {
                    if (files[i].isFile()) {
                        in = ftpClient.retrieveFileStream(files[i].getName());
                        br = new BufferedReader(new InputStreamReader(in, "GBK"));
                        if ((data = br.readLine()) != null) {
                            log.info("======================================================" + data);
                            JSONArray jsonArray = parseObject(data).getJSONArray("list");
                            XxlJobLogger.log(jsonArray.toJSONString() + "TCG拉取注单:" + files[i].getName());
                            insertTCGData(jsonArray);
                            jedisUtil.hset(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH, RedisConstant.TCG + ":file", path + "_" + i + "_" + files[i].getName());
                        }
                        ftpClient.completePendingCommand();
                    }
                }
            }
            //关闭连接
            FTPUtils.disConnection(ftpClient);
        } catch (Exception e) {
            log.error("ftp读取文件出错" + e);
        } finally {
            try {
                //关闭流
                if (br != null) {
                    br.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 拉取未结算注单
     *
     * @param startDate
     * @param endDate
     */
    private void pullData(int startDate, int endDate) {
        var batchName = String.format("%s,%s", DateUtils.YYYYMMDDHH24MI(startDate), DateUtils.YYYYMMDDHH24MI(endDate));
        var pullDataBO = TCGLotteryRequestParameter.GetUnsettleBetList.builder()
                .method(TCGPlatEnum.TCGMethodEnum.GETUNSETTLEBET.getMethodName())
                .batchName(batchName).build();
        JSONObject call = send(TCGPlatEnum.TCGMethodEnum.GETUNSETTLEBET, config.getApiUrl(), pullDataBO);
        XxlJobLogger.log("TCG拉取进入未结算注单:" + DateUtils.YYYYMMDDHH24MI(startDate));
        if (call.size() != 0 && call.get(STATUS).equals(0)) {
            JSONArray jsonArray = call.getJSONArray("details");
            List<BetslipsTcg> betslipsTcglist = new ArrayList<>();
            Map<String, Integer> userMap = userServiceBase.getUsernameIdMap();
            if (jsonArray != null && !jsonArray.isEmpty()) {
                for (var x : jsonArray) {
                    var betDetailBo = parseObject(toJSONString(x), TCGLotteryRequestParameter.BetDetail.class);
                    var userName = userServiceBase.filterUsername(betDetailBo.getUsername());
                    var uid = userMap.get(userName);
                    if (StringUtils.isEmpty(userName) || uid == null) {
                        continue;
                    }
                    var retBetslipsTcg = BeanConvertUtils.copyProperties(betDetailBo, BetslipsTcg::new, (bo, sb) -> {
                        sb.setXbUsername(userName);
                        sb.setXbUid(uid);
                        sb.setXbCoin(bo.getBetAmount());
                        sb.setXbStatus(statusMap.get(5));
                        sb.setCreatedAt(endDate);
                        sb.setUpdatedAt(DateNewUtils.now());
                    });
                    betslipsTcglist.add(retBetslipsTcg);
                }
                betslipsTcgServiceImpl.saveOrUpdateBatch(betslipsTcglist, 1000);
            }
            jedisUtil.hset(KeyConstant.PLATFORM_GAME_PULL_DATA_HASH, RedisConstant.TCG, String.valueOf(endDate));
        }


    }

    /**
     * 状态码转换
     * (1:WIN | 2:LOSE | 3:CANCELLED | 4:TIE )
     *
     * @param ticketStatus
     * @return
     */
    Map<Integer, Integer> statusMap = Map.of(
            1, BasePlatParam.BetRecordsStatus.WIN.getCode(),
            2, BasePlatParam.BetRecordsStatus.LOSE.getCode(),
            3, BasePlatParam.BetRecordsStatus.GAME_CANCEL.getCode(),
            4, BasePlatParam.BetRecordsStatus.DRAW.getCode(),
            5, BasePlatParam.BetRecordsStatus.WAIT_SETTLE.getCode()
    );

    /**
     * 日期增加
     *
     * @param s
     * @return
     */
    public static String addDay(String s) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            Calendar cd = Calendar.getInstance();
            cd.setTime(sdf.parse(s));
            cd.add(Calendar.DATE, 1);//增加一天
            return sdf.format(cd.getTime());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 根据起始、结束时间 生成补单信息
     * 最长可以拉取180天的时间
     *
     * @param dto dto.start 开始时间 dto.end结束时间
     */
    @Override
    public void genSupplementsOrders(GenSupplementsOrdersReqDto dto) {
        if (ZonedDateTime.now().minusDays(180).toInstant().getEpochSecond() > dto.getStart()) {
            throw new BusinessException(CodeInfo.PLAT_BET_SLIPS_SUPPLE_OVER_180_DAYS);
        } else {
            ZonedDateTime start = DateNewUtils.utc8Zoned(dto.getStart());
            ZonedDateTime end = DateNewUtils.utc8Zoned(dto.getEnd());
            ZonedDateTime now = ZonedDateTime.now();
            if (end.compareTo(now) > 0) {
                end = now;
            }
            ZonedDateTime temp;
            LinkedList<BetSlipsSupplemental> list = new LinkedList<>();
            do {
                temp = start.plusDays(1);
                Integer currentTime = (int) now.toInstant().getEpochSecond();
                String path = TCGPlatEnum.TCGMethodEnum.GETSETTLEDBET.getMethodName() + String.format("/%s/", DateNewUtils.utc8Str(start, DateNewUtils.Format.yyyyMMdd));
                BetSlipsSupplemental po = PlatAbstractFactory
                        .buildBetSlipsSupplemental(dto.getGameId(), start.toString(), temp.toString(), path, currentTime);
                list.add(po);
                start = start.plusDays(1);
            } while (temp.compareTo(end) <= 0);
            if (!list.isEmpty()) {
                commonPersistence.addBatchBetSlipsSupplementalList(list);
            }
        }
    }

    /**
     * 补充注单信息
     *
     * @param dto dto.requestInfo 补单请求参数
     * @author: David
     * @date: 04/05/2020
     */
    @Override
    public void betsRecordsSupplemental(BetsRecordsSupplementReqDto dto) {
        Integer mark = 0;
        do {
            mark = jedisUtil.get(RedisConstant.TCG + "betsRecordsSupplemental:file") == null || jedisUtil.get(RedisConstant.TCG + "betsRecordsSupplemental:file").contains("_") ? 0 : Integer.valueOf(RedisConstant.TCG + "betsRecordsSupplemental:file".split("_")[1]);
            mark = tcgFtpRequest(dto, mark);

        } while (mark != 0);

    }

    /**
     * ftp请求
     *
     * @param dto
     * @param mark
     * @return
     */
    private Integer tcgFtpRequest(BetsRecordsSupplementReqDto dto, Integer mark) {
        InputStream in = null;
        BufferedReader br = null;
        String data = null;
        try {
            //异常处理
            ExceptionThreadLocal.setRequestParams(toJSONString(dto));
            FTPClient ftpClient = FTPUtils.getFTPClient(config.getFtpHost(),
                    config.getFtpUser(), config.getFtpPassword());
            FTPFile[] files = FTPUtils.getFTPDirectoryFiles(ftpClient, dto.getRequestInfo());
            if (files != null && files.length > 0) {
                for (int i = mark; i < files.length; i++) {
                    if (files[i].isFile()) {
                        in = ftpClient.retrieveFileStream(files[i].getName());
                        br = new BufferedReader(new InputStreamReader(in, "GBK"));
                        if ((data = br.readLine()) != null) {
                            log.info("======================================================" + data);
                            JSONArray jsonArray = parseObject(data).getJSONArray("list");
                            XxlJobLogger.log(jsonArray.toJSONString() + "TCG拉取注单:" + files[i].getName());
                            insertTCGData(jsonArray);
                            if (files.length - 1 == i) {
                                jedisUtil.setex(RedisConstant.TCG + "betsRecordsSupplemental:file", 60 * 60, dto.getRequestInfo() + "_" + 0 + "_" + files[i].getName());
                            } else {
                                jedisUtil.setex(RedisConstant.TCG + "betsRecordsSupplemental:file", 60 * 60, dto.getRequestInfo() + "_" + i + "_" + files[i].getName());
                            }
                        }
                        ftpClient.completePendingCommand();
                    }
                }
            }
            //关闭连接
            FTPUtils.disConnection(ftpClient);
        } catch (Exception e) {
            CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0.setMsg(e.getMessage());
            throw new BusinessException(CodeInfo.PLAT_BET_SLIPS_EXCEPTION_CATEGORY_0);
        } finally {
            try {
                //关闭流
                if (br != null) {
                    br.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return mark;
    }

    /**
     * 数据入库
     *
     * @param jsonArray
     */
    private void insertTCGData(JSONArray jsonArray) {
        List<BetslipsTcg> betslipsTcglist = new ArrayList<>();
        Map<String, Integer> userMap = userServiceBase.getUsernameIdMap();
        if (!jsonArray.isEmpty()) {
            for (var x : jsonArray) {

                var betDetailBo = parseObject(toJSONString(x), TCGLotteryRequestParameter.BetDetail.class);
                var userName = userServiceBase.filterUsername(betDetailBo.getUsername());
                var uid = userMap.get(userName);
                if (StringUtils.isEmpty(userName) || uid == null) {
                    continue;
                }
                var retBetslipsTcg = BeanConvertUtils.copyProperties(betDetailBo, BetslipsTcg::new, (bo, sb) -> {
                    sb.setXbUid(uid);
                    sb.setXbUsername(userName);
                    sb.setXbCoin(bo.getBetAmount());
                    sb.setXbValidCoin(bo.getBetStatus() == 3 ? BigDecimal.ZERO : bo.getBetAmount());
                    sb.setXbStatus(statusMap.get(bo.getBetStatus()));
                    sb.setXbProfit(bo.getNetPNL());
                    sb.setCreatedAt((int) (bo.getTransTime().getTime() / 1000));
                    sb.setUpdatedAt(DateUtils.getCurrentTime());
                });
                betslipsTcglist.add(retBetslipsTcg);
            }
            betslipsTcgServiceImpl.saveOrUpdateBatch(betslipsTcglist, 1000);
        }
    }

    /**
     * 拉单异常 再次拉单
     *
     * @param dto dto.requestInfo 拉单请求参数
     * @author: David
     * @date: 04/05/2020
     */
    @Override
    public void betSlipsExceptionPull(BetsRecordsSupplementReqDto dto) {
        this.betsRecordsSupplemental(dto);
    }
}
