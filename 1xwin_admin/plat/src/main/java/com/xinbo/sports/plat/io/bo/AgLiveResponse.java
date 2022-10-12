package com.xinbo.sports.plat.io.bo;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.List;

/**
 * <p>
 * AGLive响应BO
 * </p>
 *
 * @author andy
 * @since 2020/7/10
 */
public interface AgLiveResponse {
    @Data
    @XStreamAlias("result")
    class AgResult {
        @XStreamAsAttribute
        private String info;
        @XStreamAsAttribute
        private String msg;
    }

    @Data
    @XStreamAlias("result")
    class AgGetOrdersResult {
        private String info;
        private Addition addition;
        @XStreamImplicit(itemFieldName = "row")
        private List<BetSlipsAgXml> row;
    }

    @Data
    @XStreamAlias("result")
    class AgGetRoundersResult {
        private String info;
        private Addition addition;
        @XStreamImplicit(itemFieldName = "row")
        private List<RoundersXml> row;
    }

    @Data
    class Addition {
        private int total;
        @XStreamAlias("num_per_page")
        private int numPerPage;
        @XStreamAlias("currentpage")
        private int currentPage;
        @XStreamAlias("totalpage")
        private int totalPage;
        @XStreamAlias("perpage")
        private int perPage;
    }

    @Data
    @XStreamAlias("row")
    class BetSlipsAgXml {
        /**
         * ID -> billNo订单号
         */
        @XStreamAsAttribute
        @XStreamAlias("billNo")
        private Long id;

        /**
         * 用户名
         */
        @XStreamAsAttribute
        private String playName;

        /**
         * 局号
         */
        @XStreamAsAttribute
        private String gameCode;

        /**
         * 派彩额度
         */
        @XStreamAsAttribute
        private String netAmount;

        /**
         * 下注时间
         */
        @XStreamAsAttribute
        private String betTime;

        /**
         * 游戏类型
         */
        @XStreamAsAttribute
        private String gameType;

        /**
         * 投注额度
         */
        @XStreamAsAttribute
        private String betAmount;

        /**
         * 有效投注额度
         */
        @XStreamAsAttribute
        private String validBetAmount;

        /**
         * 订单状态:0异常(请联系客服) 1已派彩 -8取消指定局注单 -9取消指定注单
         */
        @XStreamAsAttribute
        private Integer flag;

        /**
         * 玩法类型
         */
        @XStreamAsAttribute
        private Integer playType;

        /**
         * 投注币种
         */
        @XStreamAsAttribute
        private String currency;

        /**
         * 桌台号 (此處為虛擬桌號，非實際桌號。)
         */
        @XStreamAsAttribute
        private String tableCode;

        /**
         * 派彩时间
         */
        @XStreamAsAttribute
        private String recalcuTime;

        /**
         * 余额
         */
        @XStreamAsAttribute
        private String beforeCredit;

        /**
         * 投注IP
         */
        @XStreamAsAttribute
        @XStreamAlias("betIP")
        private String betIp;

        /**
         * 平台类型为AGIN
         */
        @XStreamAsAttribute
        private String platformType;

        /**
         * 注示
         */
        @XStreamAsAttribute
        private String remark;

        /**
         * 廳別代碼
         */
        @XStreamAsAttribute
        private String round;

        /**
         * 遊戲結果
         */
        @XStreamAsAttribute
        private String result;

        /**
         * 0-PC 大于等于1-手机
         */
        @XStreamAsAttribute
        private Integer deviceType;
    }

    /**
     * 拉单-获取開牌結果 实体
     */
    @Data
    @XStreamAlias("row")
    class RoundersXml {
        /**
         * 局号
         */
        @XStreamAsAttribute
        private String gameCode;
        /**
         * 开始时间
         */
        @XStreamAsAttribute
        @XStreamAlias("begintime")
        private String beginTime;

        /**
         * 结束时间
         */
        @XStreamAsAttribute
        @XStreamAlias("closetime")
        private String closeTime;

        /**
         * 荷官名称
         */
        @XStreamAsAttribute
        private String dealer;

        /**
         * 靴号
         */
        @XStreamAsAttribute
        @XStreamAlias("shoecode")
        private String shoeCode;

        /**
         * 结果状态,0 为无效 ,1 为有效
         */
        @XStreamAsAttribute
        private Integer flag;
        /**
         * 庄分数
         */
        @XStreamAsAttribute
        @XStreamAlias("bankerpoint")
        private Integer bankerPoint;
        /**
         * 闲分数
         */
        @XStreamAsAttribute
        @XStreamAlias("playerpoint")
        private Integer playerPoint;
        /**
         * 牌的张数,可用于判断百家乐的大小玩法
         */
        @XStreamAsAttribute
        @XStreamAlias("cardnum")
        private Integer cardNum;
        /**
         * 对子结果(0 没有对子,1 庄对,2 闲对,3 庄对闲对)
         */
        @XStreamAsAttribute
        private Integer pair;
        /**
         * 龙点数
         */
        @XStreamAsAttribute
        @XStreamAlias("dragonpoint")
        private Integer dragonPoint;
        /**
         * 虎点数
         */
        @XStreamAsAttribute
        @XStreamAlias("tigerpoint")
        private Integer tigerPoint;

        /**
         * 牌结果描述:格式为 “闲家牌;庄家牌” 的牌值,每张牌之间以空格隔开, 如 20 1 6;24 6 44
         */
        @XStreamAsAttribute
        @XStreamAlias("cardlist")
        private String cardList;
        /**
         * 视频 id
         */
        @XStreamAsAttribute
        private String vid;
        /**
         * 平台类型为 AGIN
         */
        @XStreamAsAttribute
        @XStreamAlias("platformtype")
        private String platformType;

        /**
         * 游戏类型
         */
        @XStreamAsAttribute
        @XStreamAlias("gametype")
        private String gameType;
    }

    /**
     * 游戏类型Result
     */
    @Data
    @XStreamAlias("result")
    class AgGameTypesResult {
        private String info;
        @XStreamImplicit(itemFieldName = "row")
        private List<GameTypesXml> row;
    }

    /**
     * 游戏类型实体
     */
    @Data
    @XStreamAlias("row")
    class GameTypesXml {
        /**
         * 游戏类型
         */
        @XStreamAsAttribute
        @XStreamAlias("GAMETYPE")
        private String gameType;

        /**
         * 厅类型,如 VIDEO 为视频游戏，SLOT 为电子游戏，LOTTO 为彩票游戏
         */
        @XStreamAsAttribute
        @XStreamAlias("TYPE")
        private String type;

        /**
         * 游戏类型名称
         */
        @XStreamAsAttribute
        @XStreamAlias("GAMENAME")
        private String gameName;
    }

    /**
     * 游戏类型Result
     */
    @Data
    @XStreamAlias("result")
    class AgGamePlayTypesResult {
        private String info;
        @XStreamImplicit(itemFieldName = "row")
        private List<GamePlayTypesXml> row;
    }

    /**
     * 游戏类型实体
     */
    @Data
    @XStreamAlias("row")
    class GamePlayTypesXml {
        /**
         * 玩法ID
         */
        @XStreamAsAttribute
        @XStreamAlias("playtype")
        private String playType;

        /**
         * 玩法描述
         */
        @XStreamAsAttribute
        private String description;

        /**
         * 游戏类型
         */
        @XStreamAsAttribute
        @XStreamAlias("gametype")
        private String gameType;
    }
}
