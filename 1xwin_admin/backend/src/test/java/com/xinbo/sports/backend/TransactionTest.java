package com.xinbo.sports.backend;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.xinbo.sports.backend.base.FinancialManagementBase;
import com.xinbo.sports.backend.configuration.tenant.TableDao;
import com.xinbo.sports.dao.generator.po.UserLoginLog;
import com.xinbo.sports.dao.generator.service.BetslipsDgService;
import com.xinbo.sports.dao.generator.service.UserLoginLogService;
import com.xinbo.sports.dao.generator.service.UserProfileService;
import com.xinbo.sports.dao.generator.service.UserService;
import com.xinbo.sports.service.base.MarkBase;
import com.xinbo.sports.service.cache.redis.PromotionsCache;
import com.xinbo.sports.utils.DateUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.stream.Stream;

/**
 * @author: wells
 * @date: 2020/8/17
 * @description:
 */
//@SpringBootTest
public class TransactionTest {
    @Autowired
    private TransactionalInstall transactionalInstall;
    @Autowired
    private FinancialManagementBase financialManagementBase;
    @Autowired
    private UserService userServiceImpl;
    volatile int a;
    @Autowired
    private PromotionsCache promotionsCache;
    @Autowired
    TableDao tableDao;
    @Autowired
    private UserProfileService userProfileServiceImpl;
    @Autowired
    private BetslipsDgService betslipsDgServiceImpl;
    @Autowired
    private UserLoginLogService userLoginLogServiceImpl;


    @Test
    void Test() {
        //多线程执行100次
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                20,
                128,
                1L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>());
        ListeningExecutorService guavaExecutors = MoreExecutors.listeningDecorator(pool);
        var futureList = new ArrayList<ListenableFuture<Boolean>>();
        for (int i = 0; i < 100; i++) {

            var future = guavaExecutors.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    return transactionalInstall.updateUser(a);
                }
            });
            futureList.add(future);
        }
        var resultsFuture = Futures.successfulAsList(futureList);
        try {
            var list = resultsFuture.get();
            int correct = 0;
            int error = 0;
            for (var b : list) {
                if (b) {
                    correct++;
                } else {
                    error++;
                }
            }
            System.out.println("correct=" + correct + "error=" + error);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Test
    void updateUser() {
        // transactionalInstall.updateUser();
        var user = userServiceImpl.getById(13);
        //  financialManagementBase.firstOrSecondDeposit(user);
    }

    @Test
    void promotions() {
        var promotions = promotionsCache.getPromotionsCache(1);
    }


    @Test
    void allTable() {
        var tableNameList = new ArrayList<String>();
        var list = tableDao.listTable();
        for (var map : list) {
            var columnList = tableDao.listTableColumn(map.get("TABLE_NAME") + "");
            boolean flag = true;
            for (var columnMap : columnList) {
                var columnName = columnMap.get("COLUMN_NAME");
                if (columnName.equals("xb_uid")) {
                    flag = false;
                    break;
                }
            }
            if (!flag) {
                tableNameList.add(map.get("TABLE_NAME") + "");
            }
        }
        tableNameList.forEach(x -> {
            System.out.println("\"" + x + "\",");
        });
        System.out.println("tableNameList=" + tableNameList);
    }

    @Test
    void test() {
        // TenantContextHolder.setTenantId("12,13,14");
        //userProfileServiceImpl.list();
        // betslipsDgServiceImpl.list();
        var userLoginLog = new UserLoginLog();
        userLoginLog.setUid(1);
        userLoginLog.setUsername("zs");
        userLoginLog.setCoin(BigDecimal.valueOf(100));
        userLoginLog.setGameName("");
        userLoginLog.setUrl("");
        userLoginLog.setDevice("H5");
        userLoginLog.setCategory(1);
        userLoginLog.setRemark("wwww");
        userLoginLog.setCreatedAt(DateUtils.getCurrentTime());
        userLoginLog.setUpdatedAt(DateUtils.getCurrentTime());
        userLoginLogServiceImpl.save(userLoginLog);
    }

    @Test
    void mark() {
        var mark = MarkBase.spliceMark("www");
        var subStr = "mark".chars();
        var stream = Stream.of(2, 3, 4, 0, 6, 8, 9, 0, 1, 2);
        // var list = stream.takeWhile(x -> x != 0).collect(Collectors.toList());
        //var dropList = stream.dropWhile(x -> x != 0).collect(Collectors.toList());
        // var b1 = stream.allMatch(x -> x > 0);
        var b2 = stream.anyMatch(x -> x > 0);

    }
}
