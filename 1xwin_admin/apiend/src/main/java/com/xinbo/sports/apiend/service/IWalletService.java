package com.xinbo.sports.apiend.service;

import com.xinbo.sports.apiend.io.dto.wallet.*;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * 钱包业务接口
 * </p>
 *
 * @author andy
 * @since 2020/6/2
 */
public interface IWalletService {
    /**
     * 钱包-卡片管理-银行列表
     *
     * @return
     */
    List<BankInfoList> bankInfoList();

    /**
     * 钱包-卡片管理-用户银行卡列表
     *
     * @return 用户的银行卡列表
     */
    List<UserBankList> userBankList();

    /**
     * 钱包-卡片管理-添加用户银行卡
     *
     * @param reqBody
     * @return
     */
    void bankAdd(@Valid @RequestBody BankAddReqBody reqBody);

    /**
     * 钱包-卡片管理-修改用户银行卡
     *
     * @param reqBody
     * @return
     */
    void bankUpdate(@Valid @RequestBody BankUpdateReqBody reqBody);
}
