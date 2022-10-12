package com.xinbo.sports.task.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xinbo.sports.dao.generator.po.CoinDeposit;
import com.xinbo.sports.dao.generator.po.PayPlat;
import com.xinbo.sports.dao.generator.service.CoinDepositService;
import com.xinbo.sports.dao.generator.service.PayPlatService;
import com.xinbo.sports.utils.DateNewUtils;
import com.xinbo.sports.utils.HttpUtils;
import com.xinbo.sports.utils.JedisUtil;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class PaymentNotifyJob {
    @Autowired
    private JedisUtil jedisUtil;
    @Autowired
    private CoinDepositService coinDepositServiceImpl;
    @Autowired
    private PayPlatService payPlatServiceImpl;

    @XxlJob("withdrawNotifyUrl")
    public ReturnT<String> withdrawNotifyUrl(String param) {
        try {
            PayPlat one = payPlatServiceImpl.getOne(new QueryWrapper<PayPlat>().eq("status", 1), false);
            HttpUtils.doPost(one.getNotifyUrl().split("/v1")[0] + "/v1/pay/checkPayment", "", one.getPayModel());
            return ReturnT.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return ReturnT.FAIL;
        }
    }

    @XxlJob("delUnfinishedDeposit")
    public ReturnT<String> delUnfinishedDeposit(String param) {
        try {
            coinDepositServiceImpl.remove(new QueryWrapper<CoinDeposit>().eq("status", 0).eq("pay_type", 1).eq("audit_status", 0).lt("created_at", DateNewUtils.now() - 60 * 60 * 24));
            return ReturnT.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return ReturnT.FAIL;
        }
    }
}