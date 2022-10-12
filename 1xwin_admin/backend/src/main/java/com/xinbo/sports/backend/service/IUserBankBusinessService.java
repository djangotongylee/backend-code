package com.xinbo.sports.backend.service;

import com.xinbo.sports.backend.io.bo.user.BankAddReqBody;
import com.xinbo.sports.backend.io.bo.user.BankInfoList;
import com.xinbo.sports.backend.io.bo.user.UserBankList;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author andy
 * @since 2020/6/5
 */
public interface IUserBankBusinessService {
    /**
     * 会员列表-银行卡信息-用户银行卡列表
     *
     * @param uid
     * @return
     */
    List<UserBankList> userBankList(int uid);

    /**
     * 会员列表-银行卡信息-添加用户银行卡
     *
     * @param reqBody
     * @return
     */
    void bankAdd(@Valid @RequestBody BankAddReqBody reqBody);

    /**
     * 会员列表-银行卡信息-银行列表
     *
     * @return
     */
    List<BankInfoList> bankInfoList();
}
