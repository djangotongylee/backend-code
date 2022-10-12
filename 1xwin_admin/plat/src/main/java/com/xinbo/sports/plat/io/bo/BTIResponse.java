package com.xinbo.sports.plat.io.bo;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import io.swagger.models.auth.In;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * SALive响应BO
 * </p>
 *
 * @author andy
 * @since 2020/7/10
 */
public interface BTIResponse {


    @Data
    @XStreamAlias("MerchantResponse")
    class MerchantResponse {
        @XStreamAsAttribute
        private String ErrorCode;
        @XStreamAsAttribute
        private Integer CustomerID;
        @XStreamAsAttribute
        private String AuthToken;
        @XStreamAsAttribute
        private BigDecimal Balance;
        @XStreamAsAttribute
        private BigDecimal OpenBetsBalance;
        @XStreamAsAttribute
        private String TransactionID;
    }


    /**
     * 游戏类型Result
     */
    @Data
    @XStreamAlias("GetAllBetDetailsForTimeIntervalResponse")
    class GetAllBetDetailsForTimeIntervalResponse {
        @XStreamAsAttribute
        private Integer ErrorMsgId;
        @XStreamAsAttribute
        private String ErrorMsg;
        @XStreamAsAttribute
        private BetDetailList BetDetailList;
    }

    /**
     * 游戏类型实体
     */
    @Data
    @XStreamAlias("BetDetailList")
    class BetDetailList {
        @XStreamImplicit(itemFieldName = "BetDetail")
        private List<BetDetail> betDetail;

    }

    @Data
    @XStreamAlias("BetDetail")
    class BetDetail {
        /**
         * 玩法ID
         */
        @XStreamAsAttribute
        @XStreamAlias("BetTime")
        private String BetTime;

        /**
         * 玩法描述
         */
        @XStreamAsAttribute
        private String PayoutTime;

        /**
         * 游戏类型
         */
        @XStreamAsAttribute
        @XStreamAlias("Username")
        private String Username;
        /**
         * 游戏类型
         */
        @XStreamAsAttribute
        @XStreamAlias("HostID")
        private String HostID;
        /**
         * 游戏类型
         */
        @XStreamAsAttribute
        @XStreamAlias("GameID")
        private String GameID;
        /**
         * 游戏类型
         */
        @XStreamAsAttribute
        @XStreamAlias("Round")
        private String Round;
        /**
         * 游戏类型
         */
        @XStreamAsAttribute
        @XStreamAlias("Set")
        private String Set;
        /**
         * 游戏类型
         */
        @XStreamAsAttribute
        @XStreamAlias("BetID")
        private String BetID;
        @XStreamAsAttribute
        @XStreamAlias("BetAmount")
        private String BetAmount;
        /**
         * 游戏类型
         */
        @XStreamAsAttribute
        @XStreamAlias("Rolling")
        private String Rolling;
        @XStreamAsAttribute
        @XStreamAlias("Balance")
        private String Balance;
        /**
         * 游戏类型
         */
        @XStreamAsAttribute
        @XStreamAlias("ResultAmount")
        private String ResultAmount;

        @XStreamAsAttribute
        @XStreamAlias("GameType")
        private String GameType;
        @XStreamAsAttribute
        @XStreamAlias("BetType")
        private String BetType;
        /**
         * 游戏类型
         */
        @XStreamAsAttribute
        @XStreamAlias("BetSource")
        private String BetSource;
        @XStreamAsAttribute
        @XStreamAlias("Detail")
        private String Detail;
        @XStreamAsAttribute
        @XStreamAlias("TransactionID")
        private String TransactionID;
        @XStreamAsAttribute
        @XStreamAlias("GameResult")
        private List<GameResult> GameResult;
        @XStreamAsAttribute
        @XStreamAlias("State")
        private String State;
    }

    @Data
    @XStreamAlias("GameResult")
    class GameResult {

        @XStreamAsAttribute
        @XStreamAlias("BaccaratResult")
        private BaccaratResult BaccaratResult;


    }

    @Data
    @XStreamAlias("BaccaratResult")
    class BaccaratResult {
        @XStreamAsAttribute
        @XStreamAlias("PlayerCard1")
        private PlayerCard1 PlayerCard1;
        @XStreamAsAttribute
        @XStreamAlias("PlayerCard2")
        private PlayerCard2 PlayerCard2;
        @XStreamAsAttribute
        @XStreamAlias("PlayerCard3")
        private PlayerCard3 PlayerCard3;
        @XStreamAsAttribute
        @XStreamAlias("BankerCard1")
        private BankerCard1 BankerCard1;
        @XStreamAsAttribute
        @XStreamAlias("BankerCard2")
        private BankerCard2 BankerCard2;
        @XStreamAsAttribute
        @XStreamAlias("BankerCard3")
        private BankerCard3 BankerCard3;
        /**
         * 游戏类型
         */
    }

    @Data
    @XStreamAlias("PlayerCard1")
    class PlayerCard1 {
        @XStreamAsAttribute
        @XStreamAlias("Suit")
        private String Suit;
        @XStreamAsAttribute
        @XStreamAlias("Rank")
        private String Rank;
    }

    @Data
    @XStreamAlias("PlayerCard2")
    class PlayerCard2 {
        @XStreamAsAttribute
        @XStreamAlias("Suit")
        private String Suit;
        @XStreamAsAttribute
        @XStreamAlias("Rank")
        private String Rank;
    }

    @Data
    @XStreamAlias("BankerCard1")
    class BankerCard1 {
        @XStreamAsAttribute
        @XStreamAlias("Suit")
        private String Suit;
        @XStreamAsAttribute
        @XStreamAlias("Rank")
        private String Rank;
    }

    @Data
    @XStreamAlias("BankerCard2")
    class BankerCard2 {
        @XStreamAsAttribute
        @XStreamAlias("Suit")
        private String Suit;
        @XStreamAsAttribute
        @XStreamAlias("Rank")
        private String Rank;
    }

    @Data
    @XStreamAlias("BankerCard3")
    class BankerCard3 {
        @XStreamAsAttribute
        @XStreamAlias("Suit")
        private String Suit;
        @XStreamAsAttribute
        @XStreamAlias("Rank")
        private String Rank;
    }

    @Data
    @XStreamAlias("PlayerCard3")
    class PlayerCard3 {
        @XStreamAsAttribute
        @XStreamAlias("Suit")
        private String Suit;
        @XStreamAsAttribute
        @XStreamAlias("Rank")
        private String Rank;
    }

    @Data
    @XStreamAlias("Events")
    class Events {
        @XStreamImplicit(itemFieldName = "Event")
        private List<Event> Event;
    }

    @Data
    class Event {
        @XStreamAsAttribute
        @XStreamAlias("DateTimeGMT")
        private String DateTimeGMT;
        @XStreamAsAttribute
        @XStreamAlias("Branch")
        private String Branch;
        @XStreamAsAttribute
        @XStreamAlias("Sport")
        private String Sport;
        @XStreamAsAttribute
        @XStreamAlias("BranchID")
        private Integer BranchID;
        @XStreamAsAttribute
        @XStreamAlias("League")
        private String League;
        @XStreamAsAttribute
        @XStreamAlias("LeagueID")
        private Integer LeagueID;
        @XStreamAsAttribute
        @XStreamAlias("ID")
        private String ID;
        @XStreamAsAttribute
        @XStreamAlias("IsOption")
        private Integer IsOption;
        @XStreamAsAttribute
        @XStreamAlias("EventType")
        private Integer EventType;
        @XStreamAsAttribute
        @XStreamAlias("MEID")
        private Integer MEID;
        @XStreamAsAttribute
        @XStreamAlias("Participants")
        private Participants Participants;
        @XStreamAsAttribute
        @XStreamAlias("MoneyLine")
        private MoneyLine MoneyLine;

        @Data
        public
        class Participants {
            @XStreamAsAttribute
            @XStreamAlias("Participant1")
            private Participant1 Participant1;
            @XStreamAsAttribute
            @XStreamAlias(" Participant2")
            private Participant2 Participant2;
        }

        @Data
        public
        class Participant1 {
            @XStreamAsAttribute
            @XStreamAlias("Name")
            private String Name;
            @XStreamAsAttribute
            @XStreamAlias("Home_Visiting")
            private String Home_Visiting;
        }

        @Data
        public
        class Participant2 {
            @XStreamAsAttribute
            @XStreamAlias("Name")
            private String Name;
            @XStreamAsAttribute
            @XStreamAlias("Home_Visiting")
            private String Home_Visiting;
        }

        @Data
        class MoneyLine {
            @XStreamAsAttribute
            @XStreamAlias("Home")
            private String Home;
            @XStreamAsAttribute
            @XStreamAlias(" Draw")
            private String Draw;
            @XStreamAsAttribute
            @XStreamAlias("Away")
            private String Away;
        }

        @XStreamAsAttribute
        @XStreamAlias("Spread")
        private Spread Spread;

        @Data
        class Spread {
            @XStreamAsAttribute
            @XStreamAlias("Home_Odds")
            private String Home_Odds;
            @XStreamAsAttribute
            @XStreamAlias(" Home_Points")
            private String Home_Points;
            @XStreamAsAttribute
            @XStreamAlias("Away_Points")
            private String Away_Points;
            @XStreamAsAttribute
            @XStreamAlias("Away_Odds")
            private String Away_Odds;
        }

        @XStreamAsAttribute
        @XStreamAlias("Total")
        private Total Total;

        @Data
        private class Total {
            @XStreamAsAttribute
            @XStreamAlias(" Points")
            private String Points;
            @XStreamAsAttribute
            @XStreamAlias("Over")
            private String Over;
            @XStreamAsAttribute
            @XStreamAlias("Under")
            private String Under;
        }
    }

}
