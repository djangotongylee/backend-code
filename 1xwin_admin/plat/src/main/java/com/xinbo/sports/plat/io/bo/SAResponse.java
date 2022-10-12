package com.xinbo.sports.plat.io.bo;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
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
public interface SAResponse {

    @Data
    @XStreamAlias("GetUserStatusResponse")
    class GetUserStatusResponse {
        @XStreamAsAttribute
        private Integer ErrorMsgId;
        @XStreamAsAttribute
        private String ErrorMsg;
        @XStreamAsAttribute
        private String IsSuccess;
        @XStreamAsAttribute
        private String Username;
        @XStreamAsAttribute
        private BigDecimal Balance;
        @XStreamAsAttribute
        private String Online;
        @XStreamAsAttribute
        private String Betted;
        @XStreamAsAttribute
        private String BettedAmount;
        @XStreamAsAttribute
        private String MaxBalance;
        @XStreamAsAttribute
        private String MaxWinning;
        @XStreamAsAttribute
        private String WithholdAmount;
    }

    @Data
    @XStreamAlias("LoginRequestResponse")
    class LoginRequestResponse {
        @XStreamAsAttribute
        private Integer ErrorMsgId;
        @XStreamAsAttribute
        private String ErrorMsg;
        @XStreamAsAttribute
        private String Token;
        @XStreamAsAttribute
        private String DisplayName;
    }

    @Data
    @XStreamAlias("CreditBalanceResponse")
    class CreditBalanceDV {
        @XStreamAsAttribute
        private String Username;
        @XStreamAsAttribute
        private BigDecimal Balance;
        @XStreamAsAttribute
        private BigDecimal CreditAmount;
        @XStreamAsAttribute
        private String OrderId;
        @XStreamAsAttribute
        private Integer ErrorMsgId;
        @XStreamAsAttribute
        private String ErrorMsg;
    }

    @Data
    @XStreamAlias("DebitBalanceResponse")
    class DebitBalanceDV {
        @XStreamAsAttribute
        private String Username;
        @XStreamAsAttribute
        private BigDecimal Balance;
        @XStreamAsAttribute
        private BigDecimal DebitAmount;
        @XStreamAsAttribute
        private String OrderId;
        @XStreamAsAttribute
        private Integer ErrorMsgId;
        @XStreamAsAttribute
        private String ErrorMsg;
    }

    @Data
    @XStreamAlias("CheckOrderIdResponse")
    class CheckOrderIdResponse {
        @XStreamAsAttribute
        private Integer ErrorMsgId;
        @XStreamAsAttribute
        private String ErrorMsg;
        @XStreamAsAttribute
        private String isExist;
    }

    @Data
    @XStreamAlias("RegUserInfoResponse")
    class RegUserInfoResponse {
        @XStreamAsAttribute
        private Integer ErrorMsgId;
        @XStreamAsAttribute
        private String ErrorMsg;
        @XStreamAsAttribute
        private String Username;
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
        @XStreamAlias("PayoutTime")
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
        @XStreamAsAttribute
        @XStreamAlias("RouletteResult")
        private RouletteResult RouletteResult;
        @XStreamAsAttribute
        @XStreamAlias("DragonTigerResult")
        private DragonTigerResult DragonTigerResult;
        @XStreamAsAttribute
        @XStreamAlias("SicboResult")
        private SicboResult SicboResult;
        @XStreamAsAttribute
        @XStreamAlias("MoneyWheelResult")
        private MoneyWheelResult MoneyWheelResult;
        @XStreamAsAttribute
        @XStreamAlias("FantanResult")
        private FantanResult FantanResult;
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
        @XStreamAsAttribute
        @XStreamAlias("ResultDetail")
        private ResultDetail ResultDetail;

        @Data
        private class ResultDetail {
            @XStreamAsAttribute
            @XStreamAlias("BRTie")
            private String BRTie;
            @XStreamAsAttribute
            @XStreamAlias("BRPlayerWin")
            private String BRPlayerWin;
            @XStreamAsAttribute
            @XStreamAlias("BRBankerWin")
            private String BRBankerWin;
            @XStreamAsAttribute
            @XStreamAlias("BRBankerPair")
            private String BRBankerPair;
            @XStreamAsAttribute
            @XStreamAlias("BRSSTie")
            private String BRSSTie;
            @XStreamAsAttribute
            @XStreamAlias("BRSSPlayerWin")
            private String BRSSPlayerWin;
            @XStreamAsAttribute
            @XStreamAlias("BRSSBankerWin")
            private String BRSSBankerWin;
            @XStreamAsAttribute
            @XStreamAlias("BRSSPlayerPair")
            private String BRSSPlayerPair;
            @XStreamAsAttribute
            @XStreamAlias("BRSSBankerPair")
            private String BRSSBankerPair;
            @XStreamAsAttribute
            @XStreamAlias("BRSSSuperSix")
            private String BRSSSuperSix;
            @XStreamAsAttribute
            @XStreamAlias("BRPlayerNatural")
            private String BRPlayerNatural;
            @XStreamAsAttribute
            @XStreamAlias("BRBankerNatural")
            private String BRBankerNatural;
            @XStreamAsAttribute
            @XStreamAlias("BRSSPlayerNatural")
            private String BRSSPlayerNatural;
            @XStreamAsAttribute
            @XStreamAlias("BRSSBankerNatural")
            private String BRSSBankerNatural;
            @XStreamAsAttribute
            @XStreamAlias("BRCowPlayerWin")
            private String BRCowPlayerWin;
            @XStreamAsAttribute
            @XStreamAlias("BRCowBankerWin")
            private String BRCowBankerWin;
            @XStreamAsAttribute
            @XStreamAlias("BRCowTie")
            private String BRCowTie;
            @XStreamAsAttribute
            @XStreamAlias("BRCowPlayerPair")
            private String BRCowPlayerPair;
            @XStreamAsAttribute
            @XStreamAlias("BRCowBankerPair")
            private String BRCowBankerPair;
            @XStreamAsAttribute
            @XStreamAlias("BRCowPlayerNatural")
            private String BRCowPlayerNatural;
            @XStreamAsAttribute
            @XStreamAlias("BRCowBankerNatural")
            private String BRCowBankerNatural;

        }
    }

    @Data
    @XStreamAlias("RouletteResult")
    class RouletteResult {
        @XStreamAsAttribute
        @XStreamAlias("Point")
        private Integer Point;
        @XStreamAsAttribute
        @XStreamAlias("ResultDetail")
        private ResultDetail ResultDetail;

        @Data
        private class ResultDetail {
            @XStreamAsAttribute
            @XStreamAlias("RRZero")
            private Integer RRZero;
            @XStreamAsAttribute
            @XStreamAlias("RROne")
            private Integer RROne;
            @XStreamAsAttribute
            @XStreamAlias("RRTwo")
            private String RRTwo;
            @XStreamAsAttribute
            @XStreamAlias("RRThree")
            private String RRThree;
            @XStreamAsAttribute
            @XStreamAlias("RRFour")
            private String RRFour;
            @XStreamAsAttribute
            @XStreamAlias("RRFive")
            private String RRFive;
            @XStreamAsAttribute
            @XStreamAlias("RRSix")
            private String RRSix;
            @XStreamAsAttribute
            @XStreamAlias("RRSeven")
            private String RRSeven;
            @XStreamAsAttribute
            @XStreamAlias("RREight")
            private String RREight;
            @XStreamAsAttribute
            @XStreamAlias("RRNine")
            private String RRNine;
            @XStreamAsAttribute
            @XStreamAlias("RRTen")
            private String RRTen;
            @XStreamAsAttribute
            @XStreamAlias("RREleven")
            private String RREleven;
            @XStreamAsAttribute
            @XStreamAlias("RRTwelve")
            private String RRTwelve;
            @XStreamAsAttribute
            @XStreamAlias("RRThirteen")
            private String RRThirteen;
            @XStreamAsAttribute
            @XStreamAlias("RRForteen")
            private String RRForteen;
            @XStreamAsAttribute
            @XStreamAlias("RRFifthteen")
            private String RRFifthteen;
            @XStreamAsAttribute
            @XStreamAlias("RRSixteen")
            private String RRSixteen;
            @XStreamAsAttribute
            @XStreamAlias("RRSeventeen")
            private String RRSeventeen;
            @XStreamAsAttribute
            @XStreamAlias("RREighteen")
            private String RREighteen;
            @XStreamAsAttribute
            @XStreamAlias("RRNineteen")
            private String RRNineteen;
            @XStreamAsAttribute
            @XStreamAlias("RRTwenty")
            private String RRTwenty;
            @XStreamAsAttribute
            @XStreamAlias("RRTwentyOne")
            private String RRTwentyOne;
            @XStreamAsAttribute
            @XStreamAlias("RRTwentyTwo")
            private String RRTwentyTwo;
            @XStreamAsAttribute
            @XStreamAlias("RRTwentyThree")
            private String RRTwentyThree;
            @XStreamAsAttribute
            @XStreamAlias("RRTwentyFour")
            private String RRTwentyFour;
            @XStreamAsAttribute
            @XStreamAlias("RRTwentyFive")
            private String RRTwentyFive;
            @XStreamAsAttribute
            @XStreamAlias("RRTwentySix")
            private String RRTwentySix;
            @XStreamAsAttribute
            @XStreamAlias("RRTwentySeven")
            private String RRTwentySeven;
            @XStreamAsAttribute
            @XStreamAlias("RRTwentyEight")
            private String RRTwentyEight;
            @XStreamAsAttribute
            @XStreamAlias("RRTwentyNine")
            private String RRTwentyNine;
            @XStreamAsAttribute
            @XStreamAlias("RRThirty")
            private String RRThirty;
            @XStreamAsAttribute
            @XStreamAlias("RRThirtyOne")
            private String RRThirtyOne;
            @XStreamAsAttribute
            @XStreamAlias("RRThirtyTwo")
            private String RRThirtyTwo;
            @XStreamAsAttribute
            @XStreamAlias("RRThirtyThree")
            private String RRThirtyThree;
            @XStreamAsAttribute
            @XStreamAlias("RRThirtyFour")
            private String RRThirtyFour;
            @XStreamAsAttribute
            @XStreamAlias("RRThirtyFive")
            private String RRThirtyFive;
            @XStreamAsAttribute
            @XStreamAlias("RRThirtySix")
            private String RRThirtySix;
            @XStreamAsAttribute
            @XStreamAlias("RRSet1")
            private String RRSet1;
            @XStreamAsAttribute
            @XStreamAlias("RRSet2")
            private String RRSet2;
            @XStreamAsAttribute
            @XStreamAlias("RRSet3")
            private String RRSet3;
            @XStreamAsAttribute
            @XStreamAlias("RRRow1")
            private String RRRow1;
            @XStreamAsAttribute
            @XStreamAlias("RRRow2")
            private String RRRow2;
            @XStreamAsAttribute
            @XStreamAlias("RRRow3")
            private String RRRow3;
            @XStreamAsAttribute
            @XStreamAlias("RR1To18")
            private String RR1To18;
            @XStreamAsAttribute
            @XStreamAlias("RR19To36")
            private String RR19To36;
            @XStreamAsAttribute
            @XStreamAlias("RROdd")
            private String RROdd;
            @XStreamAsAttribute
            @XStreamAlias("RREven")
            private String RREven;
            @XStreamAsAttribute
            @XStreamAlias("RRRed")
            private String RRRed;
            @XStreamAsAttribute
            @XStreamAlias("RRBlack")
            private String RRBlack;

        }
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
    @XStreamAlias("DragonTigerResult")
    class DragonTigerResult {
        @XStreamAsAttribute
        @XStreamAlias("DragonCard")
        private DragonCard DragonCard;
        @XStreamAsAttribute
        @XStreamAlias("TigerCard")
        private TigerCard TigerCard;
        @XStreamAlias("ResultDetail")
        private ResultDetail ResultDetail;

        @Data
        private class ResultDetail {
            @XStreamAsAttribute
            @XStreamAlias("DTRTie")
            private String DTRTie;
            @XStreamAsAttribute
            @XStreamAlias("DTRDragonWin")
            private String DTRDragonWin;
            @XStreamAsAttribute
            @XStreamAlias("DTRTigerWin")
            private String DTRTigerWin;
        }
    }

    @Data
    @XStreamAlias("DragonCard")
    class DragonCard {
        @XStreamAsAttribute
        @XStreamAlias("Suit")
        private String Suit;
        @XStreamAsAttribute
        @XStreamAlias("Rank")
        private String Rank;
    }

    @Data
    @XStreamAlias("TigerCard")
    class TigerCard {
        @XStreamAsAttribute
        @XStreamAlias("Suit")
        private String Suit;
        @XStreamAsAttribute
        @XStreamAlias("Rank")
        private String Rank;
    }

    @Data
    @XStreamAlias("SicboResult")
    class SicboResult {
        @XStreamAsAttribute
        @XStreamAlias("Dice1")
        private String Dice1;
        @XStreamAsAttribute
        @XStreamAlias("Dice2")
        private String Dice2;
        @XStreamAsAttribute
        @XStreamAlias("Dice3")
        private String Dice3;
        @XStreamAsAttribute
        @XStreamAlias("TotalPoint")
        private Integer RTotalPointank;
        @XStreamAsAttribute
        @XStreamAlias("ResultDetail")
        private ResultDetail ResultDetail;

        @Data
        private class ResultDetail {
            @XStreamAsAttribute
            @XStreamAlias("SRBigSmall")
            private Integer SRBigSmall;
            @XStreamAsAttribute
            @XStreamAlias("SROddEven")
            private Integer SROddEven;
            @XStreamAsAttribute
            @XStreamAlias("SRTripleArmyOne")
            private String SRTripleArmyOne;
            @XStreamAsAttribute
            @XStreamAlias("SRTripleArmyTwo")
            private String SRTripleArmyTwo;
            @XStreamAsAttribute
            @XStreamAlias("SRTripleArmyThree")
            private String SRTripleArmyThree;
            @XStreamAsAttribute
            @XStreamAlias("SRTripleArmyFour")
            private String SRTripleArmyFour;
            @XStreamAsAttribute
            @XStreamAlias("SRTripleArmyFive")
            private String SRTripleArmyFive;
            @XStreamAsAttribute
            @XStreamAlias("SRTripleArmySix")
            private String SRTripleArmySix;
            @XStreamAsAttribute
            @XStreamAlias("SRTriple")
            private Integer SRTriple;
            @XStreamAsAttribute
            @XStreamAlias("SRAllTriple")
            private String SRAllTriple;
            @XStreamAsAttribute
            @XStreamAlias("SRPoint")
            private Integer SRPoint;
            @XStreamAsAttribute
            @XStreamAlias("SRLongOneTwo")
            private String SRLongOneTwo;
            @XStreamAsAttribute
            @XStreamAlias("SRLongOneThree")
            private String SRLongOneThree;
            @XStreamAsAttribute
            @XStreamAlias("SRLongOneFour")
            private String SRLongOneFour;
            @XStreamAsAttribute
            @XStreamAlias("SRLongOneFive")
            private String SRLongOneFive;
            @XStreamAsAttribute
            @XStreamAlias("SRLongOneSix")
            private String SRLongOneSix;
            @XStreamAsAttribute
            @XStreamAlias("SRLongTwoThree")
            private String SRLongTwoThree;
            @XStreamAsAttribute
            @XStreamAlias("SRLongTwoFour")
            private String SRLongTwoFour;
            @XStreamAsAttribute
            @XStreamAlias("SRLongTwoFive")
            private String SRLongTwoFive;
            @XStreamAsAttribute
            @XStreamAlias("SRLongTwoSix")
            private String SRLongTwoSix;
            @XStreamAsAttribute
            @XStreamAlias("SRLongThreeFour")
            private String SRLongThreeFour;
            @XStreamAsAttribute
            @XStreamAlias("SRLongThreeFive")
            private String SRLongThreeFive;
            @XStreamAsAttribute
            @XStreamAlias("SRLongThreeSix")
            private String SRLongThreeSix;
            @XStreamAsAttribute
            @XStreamAlias("SRLongFourFive")
            private String SRLongFourFive;
            @XStreamAsAttribute
            @XStreamAlias("SRLongFourSix")
            private String SRLongFourSix;
            @XStreamAsAttribute
            @XStreamAlias("SRLongFiveSix")
            private String SRLongFiveSix;
            @XStreamAsAttribute
            @XStreamAlias("SRShort")
            private Integer SRShort;
            @XStreamAsAttribute
            @XStreamAlias("SROddEvenCombination")
            private String SROddEvenCombination;
            @XStreamAsAttribute
            @XStreamAlias("SR_1_2_3_4")
            private String SR_1_2_3_4;
            @XStreamAsAttribute
            @XStreamAlias("SR_2_3_4_5")
            private String SR_2_3_4_5;
            @XStreamAsAttribute
            @XStreamAlias("SR_2_3_5_6")
            private String SR_2_3_5_6;
            @XStreamAsAttribute
            @XStreamAlias("SR_3_4_5_6")
            private String SR_3_4_5_6;
            @XStreamAsAttribute
            @XStreamAlias("SRCombination")
            private Integer SRCombination;
        }
    }

    @Data
    @XStreamAlias("MoneyWheelResult")
    class MoneyWheelResult {
        @XStreamAsAttribute
        @XStreamAlias("Main")
        private Integer Main;
        @XStreamAsAttribute
        @XStreamAlias("Side")
        private Integer Side;
        @XStreamAsAttribute
        @XStreamAlias("ResultDetail")
        private SicboResult.ResultDetail ResultDetail;

        @Data
        private class ResultDetail {
            @XStreamAsAttribute
            @XStreamAlias("MWRMain1")
            private String MWRMain1;
            @XStreamAsAttribute
            @XStreamAlias("MWRMain2")
            private String MWRMain2;
            @XStreamAsAttribute
            @XStreamAlias("MWRMain3")
            private String MWRMain3;
            @XStreamAsAttribute
            @XStreamAlias("MWRMain4")
            private String MWRMain4;
            @XStreamAsAttribute
            @XStreamAlias("MWRMain5")
            private String MWRMain5;
            @XStreamAsAttribute
            @XStreamAlias("MWRMain6")
            private String MWRMain6;
            @XStreamAsAttribute
            @XStreamAlias("MWRSide1")
            private String MWRSide1;
            @XStreamAsAttribute
            @XStreamAlias("MWRSide2")
            private String MWRSide2;
            @XStreamAsAttribute
            @XStreamAlias("MWRSide3")
            private String MWRSide3;
            @XStreamAsAttribute
            @XStreamAlias("MWRSide4")
            private String MWRSide4;
            @XStreamAsAttribute
            @XStreamAlias("MWRSide5")
            private String MWRSide5;
            @XStreamAsAttribute
            @XStreamAlias("MWRSide6")
            private String MWRSide6;
        }
    }

    @Data
    @XStreamAlias("FantanResult")
    class FantanResult {
        @XStreamAsAttribute
        @XStreamAlias("Point")
        private String Point;
        @XStreamAsAttribute
        @XStreamAlias("ResultDetail")
        private ResultDetail ResultDetail;

        @Data
        private class ResultDetail {
            @XStreamAsAttribute
            @XStreamAlias("FTROdd")
            private String FTROdd;
            @XStreamAsAttribute
            @XStreamAlias("FTREven")
            private String FTREven;
            @XStreamAsAttribute
            @XStreamAlias("FTRZhengOne")
            private String FTRZhengOne;
            @XStreamAsAttribute
            @XStreamAlias("FTRZhengTwo")
            private String FTRZhengTwo;
            @XStreamAsAttribute
            @XStreamAlias("FTRZhengThree")
            private String FTRZhengThree;
            @XStreamAsAttribute
            @XStreamAlias("FTRZhengFour")
            private String FTRZhengFour;
            @XStreamAsAttribute
            @XStreamAlias("FTRFanOne")
            private String FTRFanOne;
            @XStreamAsAttribute
            @XStreamAlias("FTRFanTwo")
            private String FTRFanTwo;
            @XStreamAsAttribute
            @XStreamAlias("FTRFanThree")
            private String FTRFanThree;
            @XStreamAsAttribute
            @XStreamAlias("FTRFanFour")
            private String FTRFanFour;
            @XStreamAsAttribute
            @XStreamAlias("FTROneNimTwo")
            private String FTROneNimTwo;
            @XStreamAsAttribute
            @XStreamAlias("FTROneNimThree")
            private String FTROneNimThree;
            @XStreamAsAttribute
            @XStreamAlias("FTROneNimFour")
            private String FTROneNimFour;
            @XStreamAsAttribute
            @XStreamAlias("FTRTwoNimOne")
            private String FTRTwoNimOne;
            @XStreamAsAttribute
            @XStreamAlias("FTRTwoNimThree")
            private String FTRTwoNimThree;
            @XStreamAsAttribute
            @XStreamAlias("FTRTwoNimFour")
            private String FTRTwoNimFour;
            @XStreamAsAttribute
            @XStreamAlias("FTRThreeNimOne")
            private String FTRThreeNimOne;
            @XStreamAsAttribute
            @XStreamAlias("FTRThreeNimTwo")
            private String FTRThreeNimTwo;
            @XStreamAsAttribute
            @XStreamAlias("FTRThreeNimFour")
            private String FTRThreeNimFour;
            @XStreamAsAttribute
            @XStreamAlias("FTRFourNimOne")
            private String FTRFourNimOne;
            @XStreamAsAttribute
            @XStreamAlias("FTRFourNimTwo")
            private String FTRFourNimTwo;
            @XStreamAsAttribute
            @XStreamAlias("FTRFourNimThree")
            private String FTRFourNimThree;
            @XStreamAsAttribute
            @XStreamAlias("FTRKwokOneTwo")
            private String FTRKwokOneTwo;
            @XStreamAsAttribute
            @XStreamAlias("FTRKwokOneFour")
            private String FTRKwokOneFour;
            @XStreamAsAttribute
            @XStreamAlias("FTRKwokTwoThree")
            private String FTRKwokTwoThree;
            @XStreamAsAttribute
            @XStreamAlias("FTRKwokThreeFour")
            private String FTRKwokThreeFour;
            @XStreamAsAttribute
            @XStreamAlias("FTROneTongTwoThree")
            private String FTROneTongTwoThree;
            @XStreamAsAttribute
            @XStreamAlias("FTROneTongTwoFour")
            private String FTROneTongTwoFour;
            @XStreamAsAttribute
            @XStreamAlias("FTROneTongThreeFour")
            private String FTROneTongThreeFour;
            @XStreamAsAttribute
            @XStreamAlias("FTRTwoTongOneThree")
            private String FTRTwoTongOneThree;
            @XStreamAsAttribute
            @XStreamAlias("FTRTwoTongOneFour")
            private String FTRTwoTongOneFour;
            @XStreamAsAttribute
            @XStreamAlias("FTRTwoTongThreeFour")
            private String FTRTwoTongThreeFour;
            @XStreamAsAttribute
            @XStreamAlias("FTRThreeTongOneTwo")
            private String FTRThreeTongOneTwo;
            @XStreamAsAttribute
            @XStreamAlias("FTRThreeTongOneFour")
            private String FTRThreeTongOneFour;
            @XStreamAsAttribute
            @XStreamAlias("FTRThreeTongTwoFour")
            private String FTRThreeTongTwoFour;
            @XStreamAsAttribute
            @XStreamAlias("FTRFourTongOneTwo")
            private String FTRFourTongOneTwo;
            @XStreamAsAttribute
            @XStreamAlias("FTRFourTongOneThree")
            private String FTRFourTongOneThree;
            @XStreamAsAttribute
            @XStreamAlias("FTRFourTongTwoThree")
            private String FTRFourTongTwoThree;
            @XStreamAsAttribute
            @XStreamAlias("FTRChunOneTwoThree")
            private String FTRChunOneTwoThree;
            @XStreamAsAttribute
            @XStreamAlias("FTRChunOneTwoFour")
            private String FTRChunOneTwoFour;
            @XStreamAsAttribute
            @XStreamAlias("FTRChunOneThreeFour")
            private String FTRChunOneThreeFour;
            @XStreamAsAttribute
            @XStreamAlias("FTRChunTwoThreeFour")
            private String FTRChunTwoThreeFour;
        }
    }
}
