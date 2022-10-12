package com.xinbo.sports.plat.io.enums;

import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.*;
import org.apache.groovy.util.Maps;

import java.util.Map;

/**
 * @author: wells
 * @date: 2020/5/28
 * @description:
 */

public interface TCGPlatEnum {


    /**
     * 天成彩票方法枚举
     */
    @Getter
    @AllArgsConstructor
    enum TCGMethodEnum {
        LG("lg", "登录游戏"),
        CM("cm", "注册用户"),
        FT("ft", "资金转账"),
        GB("gb", "查询余额"),
        CS("cs", "检查交易状态"),
        GLGL("glgl", "取得彩票游戏列表"),
        GETSETTLEDBET("/ELOTTO/SETTLED", "取得彩票已结算订单"),
        GETUNSETTLEBET("elubd", "取得彩票未完成订单");

        private String methodName;
        private String methodNameDesc;
    }


    /**
     0	Success	成功
     1	Unknown system error, please contact TCG customer support	未知的系统错误，请联系TCG客服
     2	Missing required parameter	缺少必需的参数
     3	Method not supported for the this product type	此产品类型不支持此方法
     4	Merchant is not allowed for this product type	商家不允许使用此产品类型
     5	Merchant not found	找不到商家
     6	Invalid parameters, Failed to decrypt the parameters	参数无效，无法解密参数。
     7	Invalid signature	签名无效
     8	Unsupported currency	不支持的货币
     9	Invalid Account type	帐户类型无效
     10	Invalid product type	产品类型无效
     11	Insufficient balance to fund out / withdraw	提现余额不足
     12	Transaction already exists.	交易序号已經存在
     13	Invalid game code	游戏代码无效
     15	User Does Not Exists	用户不存在
     16	Insufficient merchant credit to fund in	信用额度不足
     18	Trial mode is not supported for this game code	此游戏代码不支持试用模式
     19	Batch not ready	批次理未准备好
     21	Method not found	找不到方法
     22	Parameter Validation Failed	参数验证失败
     23	API is busy	API 繁忙
     24	Transaction not found	未找到此交易
     25	Reservation already process	預約已經完成
     26	Decimal not supported	Decimal 格式位支持
     27	API is under maintenance	接口正在维护中
     28	API is prohibited for this merchant	此商户禁止使用此接口*/


    /**
     * 错误码集合
     */
    Map<Integer, CodeInfo> map = Maps.of(
            1, CodeInfo.STATUS_CODE_ERROR,
            2, CodeInfo.PLAT_INVALID_PARAM,
            6, CodeInfo.PARAMETERS_INVALID,
            12, CodeInfo.PLAT_ID_OCCUPATION,
            13, CodeInfo.GAME_NOT_EXISTS,
            15, CodeInfo.PLAT_ACCOUNT_NOT_EXISTS,
            16, CodeInfo.PLAT_COIN_INSUFFICIENT

    );


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class PlatConfig {
        String sha256Key;
        String apiUrl;
        String desKey;
        String currency;
        String productType;
        String merchantCode;
        String environment;
        String domain;
        String ftpHost;
        String ftpUser;
        String ftpPassword;
    }
}
