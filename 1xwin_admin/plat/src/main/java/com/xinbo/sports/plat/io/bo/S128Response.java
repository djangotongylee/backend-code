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
public interface S128Response {
    @Data
    @XStreamAlias("active_player")
    class ActiveAccountResult {
        @XStreamAsAttribute
        private String status_code;
        @XStreamAsAttribute
        private String status_text;
    }

    @Data
    @XStreamAlias("get_balance")
    class GetBalanceResult {
        @XStreamAsAttribute
        private String status_code;
        @XStreamAsAttribute
        private String status_text;
        @XStreamAsAttribute
        private String balance;
    }

    @Data
    @XStreamAlias("get_session_id")
    class GetSessionIdResult {
        @XStreamAsAttribute
        private String status_code;
        @XStreamAsAttribute
        private String status_text;
        @XStreamAsAttribute
        private String session_id;
    }

    @Data
    @XStreamAlias("deposit")
    class Deposit {
        @XStreamAsAttribute
        private String status_code;
        @XStreamAsAttribute
        private String status_text;
        @XStreamAsAttribute
        private String amount;
        @XStreamAsAttribute
        private String trans_id;
        @XStreamAsAttribute
        private String balance_open;
        @XStreamAsAttribute
        private String balance_close;
    }

    @Data
    @XStreamAlias("withdraw")
    class Withdraw {
        @XStreamAsAttribute
        private String status_code;
        @XStreamAsAttribute
        private String status_text;
        @XStreamAsAttribute
        private String amount;
        @XStreamAsAttribute
        private String trans_id;
        @XStreamAsAttribute
        private String balance_open;
        @XStreamAsAttribute
        private String balance_close;
    }

    @Data
    @XStreamAlias("get_cockfight_processed_ticket")
    class ProcessedTicket {
        @XStreamAsAttribute
        private String status_code;
        @XStreamAsAttribute
        private String status_text;
        @XStreamAsAttribute
        private String total_records;
        @XStreamAsAttribute
        private String data;

    }

    @Data
    @XStreamAlias("get_cockfight_open_ticket")
    class OpenTicket {
        @XStreamAsAttribute
        private String status_code;
        @XStreamAsAttribute
        private String status_text;
        @XStreamAsAttribute
        private String total_records;
        @XStreamAsAttribute
        private String data;

    }

    @Data
    @XStreamAlias("check_transfer")
    class CheckTransfer {
        @XStreamAsAttribute
        private String status_code;
        @XStreamAsAttribute
        private String status_text;
        @XStreamAsAttribute
        private String found;
        @XStreamAsAttribute
        private String amount;
        @XStreamAsAttribute
        private String trans_id;
        @XStreamAsAttribute
        private String balance_open;
        @XStreamAsAttribute
        private String balance_close;

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
