package com.xinbo.sports.plat.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.xinbo.sports.dao.generator.po.CoinLog;
import com.xinbo.sports.dao.generator.po.CoinPlatTransfer;
import com.xinbo.sports.dao.generator.service.CoinLogService;
import com.xinbo.sports.dao.generator.service.CoinPlatTransferService;
import com.xinbo.sports.dao.generator.service.UserService;
import com.xinbo.sports.plat.factory.PlatAbstractFactory;
import com.xinbo.sports.plat.io.bo.AgLiveRequestParameter;
import com.xinbo.sports.plat.io.bo.AgLiveResponse;
import com.xinbo.sports.plat.io.bo.PlatFactoryParams;
import com.xinbo.sports.plat.io.dto.base.CoinTransferPlatReqDto;
import com.xinbo.sports.plat.io.enums.AgRoutineParam;
import com.xinbo.sports.plat.io.enums.AgUrlEnum;
import com.xinbo.sports.service.base.CoinPlatTransfersBase;
import com.xinbo.sports.service.exception.BusinessException;
import com.xinbo.sports.utils.*;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.ConnectException;
import java.net.http.HttpConnectTimeoutException;
import java.util.Objects;

import static com.xinbo.sports.plat.io.bo.PlatFactoryParams.*;

/**
 * <p>
 * AG视讯
 * </p>
 *
 * @author andy
 * @since 2020/5/29
 */
@Slf4j
@Service("AGServiceImpl")
public class AGServiceImpl implements PlatAbstractFactory {
    protected static final String MODEL = "AG";
    protected static final String REQ_COMMON_DELIMITER = "/\\\\\\\\/";
    @Resource
    protected UserService userServiceImpl;
    @Setter
    protected AgLiveRequestParameter.AgConfig config;
    @Resource
    protected com.xinbo.sports.service.cache.redis.ConfigCache configCache;
    @Resource
    private CoinPlatTransfersBase coinPlatTransfersBase;
    @Resource
    private CoinPlatTransferService coinPlatTransferServiceImpl;
    @Resource
    private CoinLogService coinLogServiceImpl;
    private int sysLang;

    private static String getCode(AgLiveResponse.AgResult p) {
        String code = null;
        if (Objects.nonNull(p)) {
            code = p.getInfo();
        }
        return code;
    }

    private static boolean isOk(AgLiveResponse.AgResult p) {
        return "0".equals(getCode(p));
    }

    /**
     * 生成密码
     *
     * @param userName userName
     * @return 20位长度的密码
     */
    private static String buildPwd(String userName) {
        String md5Value = MD5.encryption(userName);
        if (StringUtils.isNotBlank(md5Value)) {
            md5Value = md5Value.substring(0, 20);
        }
        return md5Value;
    }

    private static String subOrderId(String orderId) {
        if (StringUtils.isNotBlank(orderId) && orderId.length() > 16) {
            orderId = orderId.substring(0, 16);
        }
        return orderId;
    }

    /**
     * not_enough_credit=余额不足, 未能转账
     *
     * @param msg not_enough_credit
     * @return
     */
    private static boolean notEnough(String msg) {
        return "not_enough_credit".equals(msg) || "not enough credit".equals(msg);
    }

    /**
     * 校验错误码
     */
    private static void checkErrorCode(AgLiveResponse.AgResult agResult) {
        String info = agResult.getInfo();
        String msg = agResult.getMsg();
        // <result info="error" msg="error:60001,Account not exist with this currency value or account hierarchical error!"/>
        if (msg.indexOf("60001") != -1 || info.indexOf("60001") != -1) {
            throw new BusinessException(CodeInfo.PLAT_ACCOUNT_NOT_EXISTS);
        } else if (notEnough(msg) || notEnough(info)) {
            throw new BusinessException(CodeInfo.PLAT_COIN_INSUFFICIENT);
        } else if (msg.indexOf("account_add_fail") != -1 || info.indexOf("account_add_fail") != -1) {
            // 创建新账号失败, 可能是密码不正确或账号已存在
            throw new BusinessException(CodeInfo.PLAT_ACCOUNT_EXISTS);
        } else if (info.indexOf("error") != -1) {
            throw new BusinessException(CodeInfo.PLAT_SYSTEM_ERROR);
        }
    }

    protected String send(String tmpUrl, String reqParams) {
        String result = null;
        String uri = "";
        try {
            uri = tmpUrl + reqParams;
            // uri = "http://fv3pw7.gdcapi.com:3333/getorders.xml?cagent=FW7&startdate=2020-07-13 05:31:47&enddate=2020-07-13 05:41:47&gametype=BAC&page=1&perpage=500&key=f91606c420f122d052256b88858d9089";
            result = HttpUtils.doGet(uri, MODEL);
        } catch (ConnectException e) {
            throw new BusinessException(CodeInfo.PLAT_CONNECTION_REFUSED);
        } catch (HttpConnectTimeoutException e) {
            throw new BusinessException(CodeInfo.PLAT_TIME_OUT);
        } catch (Exception e) {
            throw new BusinessException(CodeInfo.PLAT_CONNECTION_EXCEPTION);
        }
        return result;
    }

    @Override
    public PlatGameLoginResDto login(PlatLoginReqDto platLoginReqDto) {
        if (null == platLoginReqDto) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        switchLang(platLoginReqDto.getLang());
        String result = send(config.getApiUrl(), buildCommonRequestParams(AgUrlEnum.AGLIVE_DOBUSINESS.getMethod(), buildRegisterParams(platLoginReqDto.getUsername())));
        log.info(MODEL + " login result=" + result);
        AgLiveResponse.AgResult agResult = XmlBuilder.xmlStrToObject(result, AgLiveResponse.AgResult::new);
        if (!isOk(agResult)) {
            log.error(MODEL + "登录失败:" + result);
            checkErrorCode(agResult);
        }
        // 成功后,拼接登录地址
        String loginUrl = config.getGciUrl() + buildCommonRequestParams(AgUrlEnum.AGLIVE_FORWARDGAME.getMethod(), buildLoginUrl(platLoginReqDto.getUsername()));
        return PlatGameLoginResDto.builder().type(1).url(loginUrl).build();
    }

    @Override
    public PlatQueryBalanceResDto queryBalance(PlatQueryBalanceReqDto platQueryBalanceReqDto) {
        if (null == platQueryBalanceReqDto) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        String username = platQueryBalanceReqDto.getUsername();
        String result = send(config.getApiUrl(), buildCommonRequestParams(AgUrlEnum.AGLIVE_DOBUSINESS.getMethod(), buildQueryBalanceParams(username)));
        log.info("result=" + result);
        AgLiveResponse.AgResult agResult = XmlBuilder.xmlStrToObject(result, AgLiveResponse.AgResult::new);
        if (!isOk(agResult)) {
            checkErrorCode(agResult);
        }
        BigDecimal balance = BigDecimal.ZERO;
        if (StringUtils.isNotBlank(agResult.getInfo())) {
            balance = new BigDecimal(agResult.getInfo());
        }
        return PlatQueryBalanceResDto.builder().platCoin(balance).build();
    }

    @Override
    public PlatCoinTransferResDto coinUp(@Valid PlatFactoryParams.PlatCoinTransferReqDto platCoinTransferUpReqDto) {
        CoinTransferPlatReqDto build = CoinTransferPlatReqDto.builder()
                .coin(platCoinTransferUpReqDto.getCoin())
                .orderId(platCoinTransferUpReqDto.getOrderId())
                .username(platCoinTransferUpReqDto.getUsername())
                .build();
        // 测试环境上方金额不能大于100
        if (!"PROD".equals(config.getEnvironment()) && platCoinTransferUpReqDto.getCoin().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BusinessException(CodeInfo.PLAT_COIN_UP_COIN_NOT_GT_100);
        }
        return processCoinUpDown(build, "IN");
    }

    @Override
    public PlatCoinTransferResDto coinDown(@Valid PlatFactoryParams.PlatCoinTransferReqDto platCoinTransferDownReqDto) {
        BigDecimal coin = platCoinTransferDownReqDto.getCoin();
        // 是否全部下分: 1-全部 0-按金额
        if (null != platCoinTransferDownReqDto.getIsFullAmount() && platCoinTransferDownReqDto.getIsFullAmount().intValue() == 1) {
            // 查询余额
            PlatQueryBalanceResDto platQueryBalanceResDto = queryBalance(
                    PlatQueryBalanceReqDto.builder()
                            .username(platCoinTransferDownReqDto.getUsername())
                            .build()
            );
            if (Objects.nonNull(platQueryBalanceResDto)) {
                coin = platQueryBalanceResDto.getPlatCoin();
            }
        }
        if (coin.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(CodeInfo.PLAT_COIN_INSUFFICIENT);
        }
        CoinTransferPlatReqDto build = CoinTransferPlatReqDto.builder()
                .IsFullAmount(platCoinTransferDownReqDto.getIsFullAmount())
                .coin(coin)
                .orderId(platCoinTransferDownReqDto.getOrderId())
                .username(platCoinTransferDownReqDto.getUsername())
                .build();
        return processCoinUpDown(build, "OUT");
    }

    /**
     * @param dto
     * @param type IN-平台2第三发(上方); OUT-第三方2平台(下方)
     * @return
     */
    private PlatCoinTransferResDto processCoinUpDown(CoinTransferPlatReqDto dto, String type) {
        if (null == dto) {
            log.error("params is null...");
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        String username = dto.getUsername();
        String orderId = dto.getOrderId();
        BigDecimal coin = dto.getCoin();
        // 保留2位小数点
        coin = coin.setScale(2, RoundingMode.DOWN);
        // 更新coinPlatTransfer主键=16位
        orderId = String.valueOf(updateOrderId(orderId));
        String params = buildPrepareTransferCreditParams(username, type, coin, orderId);
        String result = send(config.getApiUrl(), buildCommonRequestParams(AgUrlEnum.AGLIVE_DOBUSINESS.getMethod(), params));
        AgLiveResponse.AgResult agResult = XmlBuilder.xmlStrToObject(result, AgLiveResponse.AgResult::new);
        // 1.预备转账OK
        if (!isOk(agResult)) {
            log.error(MODEL + "1.预备转账失败:" + result);
            updateCoinUpDown(orderId, 2, coin, null, result);
            checkErrorCode(agResult);
        }
        log.info("1.预备转账OK..username=" + username);
        params = buildTransferCreditConfirmParams(username, type, coin, orderId, "1");
        // 2.转账确认
        result = send(config.getApiUrl(), buildCommonRequestParams(AgUrlEnum.AGLIVE_DOBUSINESS.getMethod(), params));
        agResult = XmlBuilder.xmlStrToObject(result, AgLiveResponse.AgResult::new);
        if (!isOk(agResult)) {
            log.error(MODEL + "2.转账确认失败:" + result);
            updateCoinUpDown(orderId, 2, coin, null, result);
            checkErrorCode(agResult);
        }
        log.info("2.转账确认OK..username=" + username);
        params = buildQueryOrderStatusParams(orderId);
        // 3.查询订单状态
        result = send(config.getApiUrl(), buildCommonRequestParams(AgUrlEnum.AGLIVE_DOBUSINESS.getMethod(), params));
        agResult = XmlBuilder.xmlStrToObject(result, AgLiveResponse.AgResult::new);
        if (!isOk(agResult)) {
            log.error(MODEL + "3.查询订单状态失败:" + result);
            updateCoinUpDown(orderId, 2, coin, null, result);
            checkErrorCode(agResult);
        }
        log.info("3.查询订单状态Ok..username=" + username);
        // 更新转账逻辑
        updateCoinUpDown(orderId, 1, coin, null, null);
        // 4.查询AG平台余额
        PlatQueryBalanceResDto platQueryBalanceResDto = queryBalance(PlatQueryBalanceReqDto.builder().username(username).build());
        BigDecimal platCoin = BigDecimal.ZERO;
        if (Objects.nonNull(platQueryBalanceResDto)) {
            log.info("4.查询订单状态Ok..username=" + username);
            platCoin = platQueryBalanceResDto.getPlatCoin();
        }
        // 返回AG平台余额
        return PlatCoinTransferResDto.builder().platCoin(platCoin).build();
    }

    /**
     * 更新上下分记录
     *
     * @param orderId   上下分主键Id
     * @param status    状态
     * @param coin      金额
     * @param orderPlat 第三方平台ID
     * @param msg       错误信息
     */
    private void updateCoinUpDown(String orderId, Integer status, BigDecimal coin, String orderPlat, String msg) {
        coinPlatTransfersBase.updateOrderPlat(orderId, status, coin, orderPlat, msg);
    }

    @Transactional(rollbackFor = Exception.class)
    Long updateOrderId(String orderId) {
        String updateOrderId = subOrderId(orderId);
        if (StringUtils.isNotBlank(updateOrderId)) {
            Long id = Long.valueOf(updateOrderId);
            coinPlatTransferServiceImpl.lambdaUpdate().set(CoinPlatTransfer::getId, id).eq(CoinPlatTransfer::getId, orderId).update();
            coinLogServiceImpl.lambdaUpdate().set(CoinLog::getReferId, id).eq(CoinLog::getReferId, orderId)
                    .in(CoinLog::getCategory, 3, 4)
                    .eq(CoinLog::getStatus, 0)
                    .update();
            return id;
        }
        return null;
    }

    @Override
    public void pullBetsLips() {
        throw new UnsupportedOperationException();
    }

    /**
     * 检查转账状态
     *
     * @author: David
     * @date: 04/05/2020
     */
    @Override
    public Boolean checkTransferStatus(String orderId) {
        if (org.apache.commons.lang3.StringUtils.isBlank(orderId)) {
            throw new BusinessException(CodeInfo.PARAMETERS_INVALID);
        }
        String params = buildQueryOrderStatusParams(orderId);
        String result = send(config.getApiUrl(), buildCommonRequestParams(AgUrlEnum.AGLIVE_DOBUSINESS.getMethod(), params));
        AgLiveResponse.AgResult agResult = XmlBuilder.xmlStrToObject(result, AgLiveResponse.AgResult::new);
        return isOk(agResult);
    }

    @Override
    public Boolean registerUser(PlatRegisterReqDto reqDto) {
        return null != login(PlatLoginReqDto.builder().username(reqDto.getUsername()).device(reqDto.getDevice()).lang(reqDto.getLang()).build());
    }

    /**
     * 语言切换
     *
     * @param lang
     */
    private void switchLang(String lang) {
        switch (lang) {
            case "en":
                sysLang = AgRoutineParam.Lang.EN.getId();
                break;
            case "zh":
                sysLang = AgRoutineParam.Lang.ZH_CN.getId();
                break;
            case "tw":
                sysLang = AgRoutineParam.Lang.ZH_TW.getId();
                break;
            case "jp":
                sysLang = AgRoutineParam.Lang.JP.getId();
                break;
            case "ko":
                sysLang = AgRoutineParam.Lang.KO.getId();
                break;
            case "th":
                sysLang = AgRoutineParam.Lang.TH.getId();
                break;
            case "vi":
                sysLang = AgRoutineParam.Lang.VI.getId();
                break;
            case "khm":
                sysLang = AgRoutineParam.Lang.KHM.getId();
                break;
            case "id":
                sysLang = AgRoutineParam.Lang.ID.getId();
                break;
            case "prt":
                sysLang = AgRoutineParam.Lang.PRT.getId();
                break;
            default:
                break;
        }
    }

    /**
     * 注册params
     *
     * @param userName 登录名
     * @return des后的密文
     */
    private String buildRegisterParams(String userName) {
        StringBuilder textParams = new StringBuilder();
        textParams.append(AgLiveRequestParameter.ReqKey.cagent).append("=").append(config.getCagent()).append(REQ_COMMON_DELIMITER);
        textParams.append(AgLiveRequestParameter.ReqKey.loginname).append("=").append(userName).append(REQ_COMMON_DELIMITER);
        textParams.append(AgLiveRequestParameter.ReqKey.method).append("=").append("lg").append(REQ_COMMON_DELIMITER);
        textParams.append(AgLiveRequestParameter.ReqKey.actype).append("=").append("1").append(REQ_COMMON_DELIMITER);
        textParams.append(AgLiveRequestParameter.ReqKey.password).append("=").append(buildPwd(userName)).append(REQ_COMMON_DELIMITER);
        textParams.append(AgLiveRequestParameter.ReqKey.oddtype).append("=").append("A").append(REQ_COMMON_DELIMITER);
        textParams.append(AgLiveRequestParameter.ReqKey.cur).append("=").append(config.getCurrency());
        return desEncrypt(textParams.toString());
    }

    /**
     * 登录params
     *
     * @param userName 登录名
     * @return des后的密文
     */
    private String buildLoginUrl(String userName) {
        StringBuilder textParams = new StringBuilder();
        textParams.append(AgLiveRequestParameter.ReqKey.cagent).append("=").append(config.getCagent()).append(REQ_COMMON_DELIMITER);
        textParams.append(AgLiveRequestParameter.ReqKey.loginname).append("=").append(userName).append(REQ_COMMON_DELIMITER);
        textParams.append(AgLiveRequestParameter.ReqKey.password).append("=").append(buildPwd(userName)).append(REQ_COMMON_DELIMITER);
        textParams.append(AgLiveRequestParameter.ReqKey.dm).append("=").append("").append(REQ_COMMON_DELIMITER);
        textParams.append(AgLiveRequestParameter.ReqKey.sid).append("=").append(config.getCagent() + TextUtils.generateRandomString(13)).append(REQ_COMMON_DELIMITER);
        textParams.append(AgLiveRequestParameter.ReqKey.actype).append("=").append("1").append(REQ_COMMON_DELIMITER);
        textParams.append(AgLiveRequestParameter.ReqKey.lang).append("=").append(sysLang).append(REQ_COMMON_DELIMITER);
        textParams.append(AgLiveRequestParameter.ReqKey.gameType).append("=").append("0").append(REQ_COMMON_DELIMITER);
        textParams.append(AgLiveRequestParameter.ReqKey.oddtype).append("=").append("A").append(REQ_COMMON_DELIMITER);
        textParams.append(AgLiveRequestParameter.ReqKey.cur).append("=").append(config.getCurrency());
        return desEncrypt(textParams.toString());
    }

    /**
     * 余额查询params
     *
     * @param userName 登录名
     * @return des后的密文
     */
    private String buildQueryBalanceParams(String userName) {
        StringBuilder textParams = new StringBuilder();
        textParams.append(AgLiveRequestParameter.ReqKey.cagent).append("=").append(config.getCagent()).append(REQ_COMMON_DELIMITER);
        textParams.append(AgLiveRequestParameter.ReqKey.loginname).append("=").append(userName).append(REQ_COMMON_DELIMITER);
        textParams.append(AgLiveRequestParameter.ReqKey.method).append("=").append("gb").append(REQ_COMMON_DELIMITER);
        textParams.append(AgLiveRequestParameter.ReqKey.actype).append("=").append("1").append(REQ_COMMON_DELIMITER);
        textParams.append(AgLiveRequestParameter.ReqKey.password).append("=").append(buildPwd(userName)).append(REQ_COMMON_DELIMITER);
        textParams.append(AgLiveRequestParameter.ReqKey.cur).append("=").append(config.getCurrency());
        return desEncrypt(textParams.toString());
    }

    /**
     * 预备转账params
     *
     * @param userName 登录名
     * @param type     IN-平台2第三发(上方); OUT-第三方2平台(下方)
     * @return des后的密文
     */
    private String buildPrepareTransferCreditParams(String userName, String type, BigDecimal coin, String orderId) {
        StringBuilder textParams = new StringBuilder();
        textParams.append(AgLiveRequestParameter.ReqKey.cagent).append("=").append(config.getCagent()).append(REQ_COMMON_DELIMITER);
        textParams.append(AgLiveRequestParameter.ReqKey.loginname).append("=").append(userName).append(REQ_COMMON_DELIMITER);
        textParams.append(AgLiveRequestParameter.ReqKey.method).append("=").append("tc").append(REQ_COMMON_DELIMITER);
        textParams.append(AgLiveRequestParameter.ReqKey.billno).append("=").append(config.getCagent() + orderId).append(REQ_COMMON_DELIMITER);
        textParams.append(AgLiveRequestParameter.ReqKey.type).append("=").append(type).append(REQ_COMMON_DELIMITER);
        textParams.append(AgLiveRequestParameter.ReqKey.credit).append("=").append(coin).append(REQ_COMMON_DELIMITER);
        textParams.append(AgLiveRequestParameter.ReqKey.actype).append("=").append("1").append(REQ_COMMON_DELIMITER);
        textParams.append(AgLiveRequestParameter.ReqKey.password).append("=").append(buildPwd(userName)).append(REQ_COMMON_DELIMITER);
        textParams.append(AgLiveRequestParameter.ReqKey.gameCategory).append("=").append("0").append(REQ_COMMON_DELIMITER);
        textParams.append(AgLiveRequestParameter.ReqKey.cur).append("=").append(config.getCurrency());
        return desEncrypt(textParams.toString());
    }

    /**
     * 转账确认params
     *
     * @param userName 登录名
     * @param type     IN-平台2第三发; OUT-第三方2平台
     * @return des后的密文
     */
    private String buildTransferCreditConfirmParams(String userName, String type, BigDecimal coin, String orderId, String flag) {
        StringBuilder textParams = new StringBuilder();
        textParams.append(AgLiveRequestParameter.ReqKey.cagent).append("=").append(config.getCagent()).append(REQ_COMMON_DELIMITER);
        textParams.append(AgLiveRequestParameter.ReqKey.loginname).append("=").append(userName).append(REQ_COMMON_DELIMITER);
        textParams.append(AgLiveRequestParameter.ReqKey.method).append("=").append("tcc").append(REQ_COMMON_DELIMITER);
        textParams.append(AgLiveRequestParameter.ReqKey.billno).append("=").append(config.getCagent() + orderId).append(REQ_COMMON_DELIMITER);
        textParams.append(AgLiveRequestParameter.ReqKey.type).append("=").append(type).append(REQ_COMMON_DELIMITER);
        textParams.append(AgLiveRequestParameter.ReqKey.credit).append("=").append(coin).append(REQ_COMMON_DELIMITER);
        textParams.append(AgLiveRequestParameter.ReqKey.actype).append("=").append("1").append(REQ_COMMON_DELIMITER);
        textParams.append(AgLiveRequestParameter.ReqKey.flag).append("=").append(flag).append(REQ_COMMON_DELIMITER);
        textParams.append(AgLiveRequestParameter.ReqKey.password).append("=").append(buildPwd(userName)).append(REQ_COMMON_DELIMITER);
        textParams.append(AgLiveRequestParameter.ReqKey.gameCategory).append("=").append("0").append(REQ_COMMON_DELIMITER);
        textParams.append(AgLiveRequestParameter.ReqKey.cur).append("=").append(config.getCurrency());
        return desEncrypt(textParams.toString());
    }

    /**
     * 查询订单状态params
     *
     * @param orderId
     * @return des后的密文
     */
    private String buildQueryOrderStatusParams(String orderId) {
        StringBuilder textParams = new StringBuilder();
        textParams.append(AgLiveRequestParameter.ReqKey.cagent).append("=").append(config.getCagent()).append(REQ_COMMON_DELIMITER);
        textParams.append(AgLiveRequestParameter.ReqKey.billno).append("=").append(config.getCagent() + orderId).append(REQ_COMMON_DELIMITER);
        textParams.append(AgLiveRequestParameter.ReqKey.method).append("=").append("qos").append(REQ_COMMON_DELIMITER);
        textParams.append(AgLiveRequestParameter.ReqKey.actype).append("=").append("1").append(REQ_COMMON_DELIMITER);
        textParams.append(AgLiveRequestParameter.ReqKey.cur).append("=").append(config.getCurrency());
        return desEncrypt(textParams.toString());
    }


    /**
     * DES加密
     *
     * @param params 明文
     * @return 密文
     */
    private String desEncrypt(String params) {
        String result = "";
        try {
            DESCUtils descUtils = new DESCUtils(this.config.getDesKey());
            result = descUtils.encrypt(params);
        } catch (Exception e) {
            log.error("des加密失败:" + e);
        }
        return result;
    }

    private String buildMd5Key(String params) {
        String value = "";
        try {
            value = MD5.encryption(params + config.getMd5Key());
        } catch (Exception e) {
            log.error("MD5加密失败:" + e);
        }
        return value;
    }

    /**
     * 通用请求方法
     *
     * @param desParams DES密文
     * @return 请求参数
     */
    private String buildCommonRequestParams(String method, String desParams) {
        StringBuilder result = new StringBuilder(method);
        result.append("?").append(AgUrlEnum.AGLIVE_DOBUSINESS.getParams()).append("=").append(desParams);
        result.append("&").append(AgUrlEnum.AGLIVE_DOBUSINESS.getKey()).append("=").append(buildMd5Key(desParams));
        return result.toString();
    }

    @Override
    public PlatLogoutResDto logout(PlatLogoutReqDto dto) {
        throw new UnsupportedOperationException();
    }
}
