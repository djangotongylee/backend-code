package com.xinbo.sports.dao.generator.po;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

/**
 * <p>
 * CQ9注单表
 * </p>
 *
 * @author David
 * @since 2021-02-01
 */
@TableName("sp_betslips_cq9_game")
public class BetslipsCq9Game extends Model<BetslipsCq9Game> {

    private static final long serialVersionUID=1L;

    /**
     * ID -> round订单号
     */
    @TableId("id")
    private String id;

    /**
     * 对应user表id
     */
    @TableField("xb_uid")
    private Integer xbUid;

    /**
     * 对应user表username
     */
    @TableField("xb_username")
    private String xbUsername;

    /**
     * 玩家帐号
     */
    @TableField("account")
    private String account;

    /**
     * 遊戲後餘額
     */
    @TableField("balance")
    private BigDecimal balance;

    /**
     * 游戏商
     */
    @TableField("game_hall")
    private String gameHall;

    /**
     * 遊戲種類
     */
    @TableField("game_type")
    private String gameType;

    /**
     * 遊戲平台
     */
    @TableField("game_plat")
    private String gamePlat;

    /**
     * 游戏商
     */
    @TableField("game_code")
    private String gameCode;

    /**
     * 投注额度
     */
    @TableField("bet")
    private BigDecimal bet;

    /**
     * 有效投注额度
     */
    @TableField("valid_bet")
    private BigDecimal validBet;

    /**
     * 彩池獎金
     */
    @TableField("jackpot")
    private BigDecimal jackpot;

    /**
     * 彩池獎金貢獻值※從小彩池到大彩池依序排序
     */
    @TableField("jackpot_contribution")
    private String jackpotContribution;

    /**
     * 彩池獎金類別※此欄位值為空字串時，表示未獲得彩池獎金
     */
    @TableField("jackpot_type")
    private String jackpotType;

    /**
     * 注單狀態 [complete]-complete:完成
     */
    @TableField("status")
    private String status;

    /**
     * 遊戲結束時間，格式為 RFC3339
     */
    @TableField("end_round_time")
    private Date endRoundTime;

    /**
     * 當筆資料建立時間，格式為 RFC3339\n※系統結算時間, 注單結算時間及報表結算時間都是createtime
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 下注時間，格式為 RFC3339
     */
    @TableField("bet_time")
    private Date betTime;

    /**
     * 回傳 free game / bonus game / luckydraw / item / reward 資訊
※slot 會回傳 free game / bonus game / luckydraw 資訊
※fish 會回傳 item / reward 資訊
※table 會回傳空陣列
     */
    @TableField("detail")
    private String detail;

    /**
     * [true|false]是否為再旋轉形成的注單
     */
    @TableField("single_row_bet")
    private Integer singleRowBet;

    /**
     * 庄(banker) or 閒(player)※此欄位為牌桌遊戲使用，非牌桌遊戲此欄位值為空字串
     */
    @TableField("game_role")
    private String gameRole;

    /**
     * 對戰玩家是否有真人[pc|human]\npc：對戰玩家沒有真人\nhuman：對戰玩家有真人\n※此欄位為牌桌遊戲使用，非牌桌遊戲此欄位值為空字串※如果玩家不支持上庄，只存在與系统對玩。則bankertype 為 PC
     */
    @TableField("banke_type")
    private String bankeType;

    /**
     * 抽水金額※此欄位為牌桌遊戲使用
     */
    @TableField("rake")
    private BigDecimal rake;

    /**
     * 開房費用
     */
    @TableField("roomfee")
    private BigDecimal roomfee;

    /**
     * 下注玩法※此欄位為真人遊戲使用，非真人遊戲此欄位值為空字串
     */
    @TableField("bet_type")
    private String betType;

    /**
     * 遊戲結果※此欄位為真人遊戲使用，非真人遊戲此欄位值為空字串
     */
    @TableField("game_result")
    private String gameResult;

    /**
     * 真人注單參數說明名稱 (1=百家，4=龍虎 )※此欄位為真人遊戲使用，非真人遊戲此欄位值為空字串
     */
    @TableField("table_type")
    private String tableType;

    /**
     * 桌號
     */
    @TableField("table_id")
    private String tableId;

    /**
     * 局號
     */
    @TableField("round_number")
    private String roundNumber;

    /**
     * 投注金额
     */
    @TableField("xb_coin")
    private BigDecimal xbCoin;

    /**
     * 有效投注额
     */
    @TableField("xb_valid_coin")
    private BigDecimal xbValidCoin;

    /**
     * 盈亏金额
     */
    @TableField("xb_profit")
    private BigDecimal xbProfit;

    /**
     * 注单状态
     */
    @TableField("xb_status")
    private Integer xbStatus;

    @TableField("created_at")
    private Integer createdAt;

    @TableField("updated_at")
    private Integer updatedAt;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getXbUid() {
        return xbUid;
    }

    public void setXbUid(Integer xbUid) {
        this.xbUid = xbUid;
    }

    public String getXbUsername() {
        return xbUsername;
    }

    public void setXbUsername(String xbUsername) {
        this.xbUsername = xbUsername;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getGameHall() {
        return gameHall;
    }

    public void setGameHall(String gameHall) {
        this.gameHall = gameHall;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    public String getGamePlat() {
        return gamePlat;
    }

    public void setGamePlat(String gamePlat) {
        this.gamePlat = gamePlat;
    }

    public String getGameCode() {
        return gameCode;
    }

    public void setGameCode(String gameCode) {
        this.gameCode = gameCode;
    }

    public BigDecimal getBet() {
        return bet;
    }

    public void setBet(BigDecimal bet) {
        this.bet = bet;
    }

    public BigDecimal getValidBet() {
        return validBet;
    }

    public void setValidBet(BigDecimal validBet) {
        this.validBet = validBet;
    }

    public BigDecimal getJackpot() {
        return jackpot;
    }

    public void setJackpot(BigDecimal jackpot) {
        this.jackpot = jackpot;
    }

    public String getJackpotContribution() {
        return jackpotContribution;
    }

    public void setJackpotContribution(String jackpotContribution) {
        this.jackpotContribution = jackpotContribution;
    }

    public String getJackpotType() {
        return jackpotType;
    }

    public void setJackpotType(String jackpotType) {
        this.jackpotType = jackpotType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getEndRoundTime() {
        return endRoundTime;
    }

    public void setEndRoundTime(Date endRoundTime) {
        this.endRoundTime = endRoundTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getBetTime() {
        return betTime;
    }

    public void setBetTime(Date betTime) {
        this.betTime = betTime;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public Integer getSingleRowBet() {
        return singleRowBet;
    }

    public void setSingleRowBet(Integer singleRowBet) {
        this.singleRowBet = singleRowBet;
    }

    public String getGameRole() {
        return gameRole;
    }

    public void setGameRole(String gameRole) {
        this.gameRole = gameRole;
    }

    public String getBankeType() {
        return bankeType;
    }

    public void setBankeType(String bankeType) {
        this.bankeType = bankeType;
    }

    public BigDecimal getRake() {
        return rake;
    }

    public void setRake(BigDecimal rake) {
        this.rake = rake;
    }

    public BigDecimal getRoomfee() {
        return roomfee;
    }

    public void setRoomfee(BigDecimal roomfee) {
        this.roomfee = roomfee;
    }

    public String getBetType() {
        return betType;
    }

    public void setBetType(String betType) {
        this.betType = betType;
    }

    public String getGameResult() {
        return gameResult;
    }

    public void setGameResult(String gameResult) {
        this.gameResult = gameResult;
    }

    public String getTableType() {
        return tableType;
    }

    public void setTableType(String tableType) {
        this.tableType = tableType;
    }

    public String getTableId() {
        return tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

    public String getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(String roundNumber) {
        this.roundNumber = roundNumber;
    }

    public BigDecimal getXbCoin() {
        return xbCoin;
    }

    public void setXbCoin(BigDecimal xbCoin) {
        this.xbCoin = xbCoin;
    }

    public BigDecimal getXbValidCoin() {
        return xbValidCoin;
    }

    public void setXbValidCoin(BigDecimal xbValidCoin) {
        this.xbValidCoin = xbValidCoin;
    }

    public BigDecimal getXbProfit() {
        return xbProfit;
    }

    public void setXbProfit(BigDecimal xbProfit) {
        this.xbProfit = xbProfit;
    }

    public Integer getXbStatus() {
        return xbStatus;
    }

    public void setXbStatus(Integer xbStatus) {
        this.xbStatus = xbStatus;
    }

    public Integer getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Integer createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Integer updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    @Override
    public String toString() {
        return "BetslipsCq9Game{" +
        "id=" + id +
        ", xbUid=" + xbUid +
        ", xbUsername=" + xbUsername +
        ", account=" + account +
        ", balance=" + balance +
        ", gameHall=" + gameHall +
        ", gameType=" + gameType +
        ", gamePlat=" + gamePlat +
        ", gameCode=" + gameCode +
        ", bet=" + bet +
        ", validBet=" + validBet +
        ", jackpot=" + jackpot +
        ", jackpotContribution=" + jackpotContribution +
        ", jackpotType=" + jackpotType +
        ", status=" + status +
        ", endRoundTime=" + endRoundTime +
        ", createTime=" + createTime +
        ", betTime=" + betTime +
        ", detail=" + detail +
        ", singleRowBet=" + singleRowBet +
        ", gameRole=" + gameRole +
        ", bankeType=" + bankeType +
        ", rake=" + rake +
        ", roomfee=" + roomfee +
        ", betType=" + betType +
        ", gameResult=" + gameResult +
        ", tableType=" + tableType +
        ", tableId=" + tableId +
        ", roundNumber=" + roundNumber +
        ", xbCoin=" + xbCoin +
        ", xbValidCoin=" + xbValidCoin +
        ", xbProfit=" + xbProfit +
        ", xbStatus=" + xbStatus +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        "}";
    }
}
