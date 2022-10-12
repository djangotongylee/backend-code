package com.xinbo.sports.backend;

import com.xinbo.sports.backend.base.DictionaryBase;
import com.xinbo.sports.dao.generator.po.Admin;
import com.xinbo.sports.dao.generator.po.AuthRule;
import com.xinbo.sports.dao.generator.service.AdminService;
import com.xinbo.sports.dao.generator.service.AuthRuleService;
import com.xinbo.sports.service.base.UserServiceBase;
import com.xinbo.sports.utils.I18nUtils;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Author : Wells
 * @Date : 2020/10/1 7:30 下午
 * @Description : XX
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = BackendApplication.class)
public class DifferenceI18nTest {
    @Resource
    private DictionaryBase dictionaryBase;
    @Resource
    private AuthRuleService authRuleServiceImpl;
    @Resource
    private UserServiceBase userServiceBase;
    @Resource
    private AdminService adminServiceImpl;

    /**
     * 1.获取字段表的tile与codeInfo的msg
     * 2.对比国际化文件
     * 3.列出差异字段
     */

    @Test
    void diffTest() {
        var map = dictionaryBase.getDictionary(null);
        var set = new HashSet<String>();
//        map.values().forEach(x -> x.forEach(resDto -> {
//            set.add(resDto.getTitle());
//        }));
//        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
//        var codeArr = CodeInfo.values();
//        for (CodeInfo codeInfo : codeArr) {
//            set.add(codeInfo.getMsg());
//        }
//
//        set.forEach(e -> {
//             e = e.replace(" ", "").trim();
//            var usMsg = I18nUtils.getLocaleMessage(e);
//            if (usMsg.equals(e) && p.matcher(usMsg).find()) {
//                System.out.println(e);
//            }
//        });

        var ruleSet = authRuleServiceImpl.list().stream().map(AuthRule::getTitle).collect(Collectors.toSet());
        set.addAll(ruleSet);
        set.forEach(e -> {
            // e = e.replace(" ", "").trim();
            var usMsg = I18nUtils.getLocaleMessage(e);
            Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
            if (usMsg.equals(e) && p.matcher(usMsg).find()) {
                System.out.println(e);
            }
        });
    }

    @Test
    void isHalfShow() {
//        var name1 = userServiceBase.isHalfShow("wwww", Constant.REAL_NAME);
//        System.out.println("name1=" + name1);
//        var name2 = userServiceBase.isHalfShow("李明", Constant.REAL_NAME);
//        System.out.println("name2=" + name2);
//        var mobile = userServiceBase.isHalfShow("135678987", Constant.MOBILE);
//        System.out.println("mobile=" + mobile);
//        var bankAccount = userServiceBase.isHalfShow("87623241242312234", Constant.BANK_ACCOUNT);
//        System.out.println("bankAccount=" + bankAccount);

        var value = "1234343286328763283";
        value = value.substring(0, 3) + "****" + value.substring(value.length() - 3);

    }

    @Test
    void treeTest() {
        var adminList = adminServiceImpl.list();
        var id = 1;
        var set = new HashSet<Integer>();
        set.add(id);
       var pidMap = adminList.stream().collect(Collectors.groupingBy(Admin::getParent));
        var ids = getSelfChild(pidMap, id, set);
        System.out.println("ids=" + ids);
    }

    public HashSet<Integer> getSelfChild(Map<Integer,List<Admin>> pidMap, Integer pid, HashSet<Integer> ids) {
        var list =pidMap.get(pid);
        if(!CollectionUtils.isEmpty(list)){
            ids.add(pid);
            ids.addAll(list.stream().map(Admin::getId).collect(Collectors.toList()));
            list.forEach(admin ->   getSelfChild(pidMap,admin.getId(),ids));
        }
        return ids;
    }
}
