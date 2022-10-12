package com.xinbo.sports.plat.io.enums;

import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.groovy.util.Maps;

import java.util.Map;

/**
 * @author: wells
 * @date: 2020/7/22
 * @description: BBIN
 */

public interface BBINPlatEnum {
    String BET_RECORD_CODE = "dipqD8972";
    String GAME_URL_CODE = "YzD551kj3";

    /**
     * BBIN方法枚举
     */
    @Getter
    @AllArgsConstructor
    enum BBINKeyEnum {
        CREATE("CreateMember", "Iv7hEKc", "注册用户", 7, 9),
        LOGIN("Login2", "CDGsR3I", "登录", 8, 6),
        DEPOSIT("Transfer", "hu0XE09Us2", "上分", 6, 7),
        WITHDRAW("Transfer", "hu0XE09Us2", "下分", 6, 7),
        BALANCE("CheckUsrBalance", "yI1ou", "查询余额", 7, 6),
        CHECK_TRANSFER("CheckTransfer", "GyYD7d", "查询会员转帐是否成功", 8, 6),
        TRANS_RECORD("TransferRecord", "GyYD7d", "获取会员转账信息", 8, 6),
        BET_RECORD("BetRecord", BET_RECORD_CODE, "获取会员投注信息", 5, 8),
        BET_EXPERT("WagersRecordBy30", BET_RECORD_CODE, "获取会员捕鱼达人投注信息", 5, 8),
        BET_MASTER("WagersRecordBy38", BET_RECORD_CODE, "获取会员捕鱼大师投注信息", 5, 8),
        BET_LIVE("WagersRecordBy3", BET_RECORD_CODE, "获取会员视讯投注信息", 5, 8),
        BET_NEW_SPORTS("WagersRecordBy31", BET_RECORD_CODE, "获取会员新体育投注信息", 5, 8),
        CREATE_SESSION("CreateSession", "CDGsR3I", "获取会员SessionId", 8, 6),
        GAME_URL_LIVE("GameUrlBy3", GAME_URL_CODE, "获取会员BB视讯URL", 4, 7),
        GAME_URL_GAME("GameUrlBy5", GAME_URL_CODE, "获取会员BB电子URL", 4, 7),
        GAME_URL_EXPERT("GameUrlBy30", GAME_URL_CODE, "获取会员捕鱼达人URL", 4, 7),
        GAME_URL_MASTER("ForwardGameH5By38", GAME_URL_CODE, "获取会员捕鱼大师URL", 4, 7);


        private String methodName;
        private String key;
        private String methodNameDesc;
        private Integer keyALength;
        private Integer keyCLength;
    }


    /**
     * 10002	Insufficient Account Balance	餘額不足
     * 10003	Transfer Failed	轉帳失敗
     * 10005	Check Limit Error	額度檢查錯誤
     * 10006	remit must be positive integer	remit需為正整數
     * 10007	The serial number of transaction is digits and it cannot be blank or 0.	交易序號是數字型態且不得為空或0
     * 10008	remit cannot be smaller or equal to 0	remit不能小於等於0
     * 10009	newcredit cannot be smaller than 0	newcredit不能小於0
     * 10010	credit cannot be smaller than 0	credit不能小於0
     * 10011	The format of transfer is wrong. (remit,credit,newcredit)	轉帳格式錯誤
     * 10012	action must be IN or OUT	action必須是IN或OUT
     * 10013	The column of action is wrong	action格式錯誤
     * 10014	The serial number of transaction is too long. (int 19).	交易序號長度過長(int 19)
     * 10015	The transfer is running, please wait	API忙碌中
     * 10016	amount cannot be smaller than 0 or equal to 0	amount不能小於等於0
     * 10017	idr or vnd amount cannot be smaller than a thousand	印尼幣或越南盾amount不能小於1000,額度換算比1000:1
     * 10018	amount must be positive integer	amount需為正整數
     * 10019	idr or vnd amount must be positive integer	印尼幣或越南盾amount需為正整數,額度換算比1000:1
     * 10020	transid must be positive integer	transid 需為正整數
     * 11000	Repeat Transfer	重複轉帳
     * 11100	Transfer Successful	轉帳成功
     * 21000	Add account failed.	帳號新增失敗
     * 21001	The account is repeated.	帳號重複
     * 21100	Add account successful.	帳號新增成功
     * 22000	User hasn't Login.	使用者未登入
     * 22001	User Logout	使用者登出
     * 22002	The user is not exist.	使用者不存在
     * 22003	The user is not under this Agent.	使用者不存在此代理底下
     * 22004	Username is not in the Member's Level.	username非會員階層
     * 22006	Upper Level is not exist.	上層不存在
     * 22011	The member is not exist.	會員不存在
     * 22013	The player is not exist.	帳號不存在
     * 23000	Hierarchy Error	體系錯誤
     * 23100	Cease User ID successful	停用帳號成功
     * 23101	Activate User ID successful	啟用帳號成功
     * 23103	Cease User ID failed	停用帳號失敗
     * 23104	Activate User ID failed	啟用帳號失敗
     * 23105	Change Password successful	變更密碼成功
     * 23106	Change Password failed	變更密碼失敗
     * 25000	Agent Login Successful	管端登入成功
     * 25001	Password is failed.	密碼驗證失敗
     * 25002	ID is failed. (the first letter is wrong)	帳號新增失敗(第一個英文字母錯誤)
     * 25003	The Upper Level hasn't Login.	上層未登入
     * 25004	The Upper Level is in the different system.	上層層級錯誤
     * 25005	The Upper Level ID is Error.	上層層級id驗證錯誤
     * 25006	The user's level is wrong.	使用者層級錯誤
     * 25100	Add Agent ID successful	代理帳號新增成功
     * 30001	Successful	設定成功
     * 30002	Failed	設定失敗
     * 40001	The owner is not exist.	廳不存在
     * 40002	The member is ceased.	會員已經停用
     * 40003	The agent is ceased.	上層已經停用
     * 40005	Level of the agent is wrong.	上層階層錯誤
     * 40006	User ID is failed.	帳號驗證失敗
     * 40007	Length of the password is wrong.	密碼長度錯誤
     * 40008	API is not open.	接口未開放
     * 40009	Exceed the limit.	超過限制筆數
     * 40010	Start Date Error	開始日期驗證錯誤
     * 40011	End Date Error	結束日期驗證錯誤
     * 40012	Start Time Error	開始時間驗證錯誤
     * 40013	End Time Error	結束時間驗證錯誤
     * 40014	Date Error	日期驗證錯誤
     * 40015	Length of the account is too long.	帳號長度過長
     * 40016	User has been suspend	帳號已被停權
     * 40020	The account is frozen.	帳號已經凍結
     * 44000	Key error	key驗證錯誤
     * (key驗證錯誤，請檢查該API組Key方式是否正確)
     * 44001	The parameters are not complete.	參數未帶齊
     * (該API必要參數未帶齊，請查看該API使用之方式)
     * 44002	Don't have authority.	無權限
     * 44003	Now the API is busy, please wait.	API忙碌中(請等待前一筆 Result 回傳後，再送新的 Request。)
     * 44004	Now the API is busy, please wait.	API忙碌中
     * 44440	System anomaly	系統異常
     * 44444	System is in maintenance	系統維護中
     * (系統維護中，請待系統維護完成)
     * 44445	Game is in maintenance	單項遊戲維護
     * 44900	IP is not accepted.	IP不被允許
     * (請至【廳主端 - 網站管理 - IP白名單管理 】新增IP至白名單)
     * 45000	The game is not exist.	桌號不存在
     * 45003	The Live game is not exist.	視訊遊戲不存在
     * 45005	The Casino game is not exist.	機率遊戲不存在
     * 45006	GameType Error	桌號不存在
     * 45007	Game is not open.	遊戲大廳未開放
     * 45008	Game is not exist.	產品不存在
     * 45009	GameKind Parameter is not complete.	遊戲參數不完全
     * 45010	GameKind value can not be same.	不可為相同遊戲
     * 45011	Enter the game failed.	進入遊戲失敗
     * 47002	Parameter "action" is wrong.	參數action錯誤
     * 47003	Parameter is wrong.	參數錯誤
     * 47004	Unsupported gamekind.	不支援的遊戲種類
     * 50001	Successful	成功
     * 50002	Fail	失敗
     * 50003	HallID Error	未選取廳主
     * 90006	Re-login within 30 seconds.	30秒重覆登入
     * 99999	Login Successful	登入成功
     */
    Map<Integer, CodeInfo> EXCEPTION_MAP = Maps.of(
            22013, CodeInfo.PLAT_ACCOUNT_NOT_EXISTS,
            22002, CodeInfo.PLAT_ACCOUNT_NOT_EXISTS,
            22002, CodeInfo.PLAT_ACCOUNT_NOT_EXISTS,
            22011, CodeInfo.PLAT_ACCOUNT_NOT_EXISTS,
            44900, CodeInfo.PLAT_IP_NOT_ACCESS,
            10002, CodeInfo.PLAT_COIN_INSUFFICIENT,
            10015, CodeInfo.PLAT_TIME_OUT,
            44444, CodeInfo.PLAT_UNDER_MAINTENANCE,
            0, CodeInfo.PLAT_UNDER_MAINTENANCE
    );

    @Getter
    @AllArgsConstructor
    enum CURRENCY {
        RMB("RMB", "人民币"),
        EUR("EUR", "欧元  "),
        GBP("GBP", "英镑  "),
        HKD("HKD", "港币 "),
        IDR("IDR", "印尼币"),
        JPY("JPY", "日币 "),
        KRW("KRW", "韩币 "),
        MYR("MYR", "马币 "),
        SGD("SGD", "新币 "),
        THB("THB", "泰铢 "),
        USD("USD", "美金"),
        VND("VND", "越南盾"),
        INR("INR", "卢比");

        private String code;
        private String name;
    }
}
