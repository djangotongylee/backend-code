package com.xinbo.sports;

import com.alibaba.fastjson.JSONObject;
import com.xinbo.sports.apiend.ApiendApplication;
import com.xinbo.sports.apiend.base.DictionaryBase;
import com.xinbo.sports.dao.generator.po.AuthRule;
import com.xinbo.sports.dao.generator.po.BetslipsFuturesLottery;
import com.xinbo.sports.dao.generator.po.Promotions;
import com.xinbo.sports.dao.generator.service.AuthRuleService;
import com.xinbo.sports.dao.generator.service.BetslipsFuturesLotteryService;
import com.xinbo.sports.dao.generator.service.PromotionsService;
import com.xinbo.sports.plat.base.CommissionBase;
import com.xinbo.sports.service.base.AuditBase;
import com.xinbo.sports.utils.I18nUtils;
import com.xinbo.sports.utils.components.response.CodeInfo;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Author : Wells
 * @Date : 2020/10/1 7:30 下午
 * @Description : XX
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApiendApplication.class)
class DifferenceI18nTest {
    @Resource
    private DictionaryBase dictionaryBase;
    @Resource
    private AuthRuleService authRuleServiceImpl;
    @Resource
    private PromotionsService promotionsServiceImpl;
    @Resource
    private CommissionBase commissionBase;
    @Resource
    private BetslipsFuturesLotteryService betslipsFuturesLotteryServiceImpl;
    @Resource
    private AuditBase auditBase;

    /**
     * 1.获取字段表的tile与codeInfo的msg
     * 2.对比国际化文件
     * 3.列出差异字段
     */

    @Test
    void diffTest() {
        var map = dictionaryBase.getDictionary(null);
        var set = new HashSet<String>();
        map.values().forEach(x -> x.forEach(resDto -> {
            // set.add(resDto.getTitle());
        }));
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        var codeArr = CodeInfo.values();
        for (CodeInfo codeInfo : codeArr) {
            // set.add(codeInfo.getMsg());
        }
        var ruleSet = authRuleServiceImpl.list().stream().map(AuthRule::getTitle).collect(Collectors.toSet());
        set.addAll(ruleSet);
        set.forEach(e -> {
            // e = e.replace(" ", "").trim();
            var usMsg = I18nUtils.getLocaleMessage(e);
            if (usMsg.equals(e) && p.matcher(usMsg).find()) {
                System.out.println(e);
            }
        });
    }

    @Test
    void addCodeZh() {
        var set = promotionsServiceImpl.list();
        set.forEach(promotions -> {
            var json = JSONObject.parseObject(promotions.getCodeZh());
            var codeZh = json.getString("zh");
            codeZh = codeZh.replace(" ", "");
            json.put("th", I18nUtils.getLocaleMessage(codeZh));
            var rePromotions = new Promotions();
            rePromotions.setId(promotions.getId());
            rePromotions.setCodeZh(json.toJSONString());
            //  promotionsServiceImpl.updateById(rePromotions);

            //  var str = "update sp_promotions set code_zh='" + json.toJSONString() + "' where id=" + promotions.getId() + ";";
            // System.out.println(str);
        });

    }

    @Test
    void testPro() {
        new I18nUtils().resolveCode(Locale.US, "Bank of China");
    }

    @Test
    void commissionBase() {
        var bets = betslipsFuturesLotteryServiceImpl
                .lambdaQuery()
                .gt(BetslipsFuturesLottery::getCoinFee, BigDecimal.ZERO)
                .last("limit 100")
                .list();
        commissionBase.commissionFutures(bets);
    }

    @Test
    void zhTransfer() {
        var list = new I18nUtils().resolveCode(Locale.SIMPLIFIED_CHINESE);
        writeFileContext(list, File.separator + "Users" + File.separator + "mac" + File.separator + "Desktop" + File.separator + "i18n.txt");
    }

    @SneakyThrows
    public static void writeFileContext(List<String> strings, String path) {
        File file = new File(path);
        //如果没有文件就创建
        if (!file.isFile()) {
            file.createNewFile();
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        for (String l : strings) {
            writer.write(l + "\r\n");
        }
        writer.close();
    }

    @Test
    void testCheckWithdrawal() {
        auditBase.checkWithdrawal(87);
    }
}