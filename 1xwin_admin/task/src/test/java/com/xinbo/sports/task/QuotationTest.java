package com.xinbo.sports.task;

import com.xinbo.sports.dao.generator.po.User;
import io.swagger.models.auth.In;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.jmx.export.naming.IdentityNamingStrategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author: wells
 * @date: 2020/9/2
 * @description:
 */

public class QuotationTest {
    public static QuotationTest create(Supplier<QuotationTest> supplier) {
        return supplier.get();
    }

    public static void collide(QuotationTest user) {
        user.getInt();
    }

    public void follow(QuotationTest user) {
    }

    public void repair() {
    }

    public void noParam() {
    }

    private static void supplier(Supplier<QuotationTest> quotationTestSupplier) {

    }

    private Integer getInt() {
        return 1;
    }

    void consumer(Consumer<? super QuotationTest> consumer) {
        var quotation = QuotationTest.create(QuotationTest::new);
        consumer.accept(quotation);
    }

    public static void main(String[] args) {
        final List<QuotationTest> quotationTestList = Arrays.asList(QuotationTest.create(QuotationTest::new));
        //List<QuotationTest> quotationTestList = new ArrayList<>();
        List<QuotationTest> list = new ArrayList<>();
        quotationTestList.forEach(QuotationTest::collide);
        var quotationTest = QuotationTest.create(QuotationTest::new);
        quotationTestList.forEach(QuotationTest::repair);
        quotationTestList.forEach(quotationTest::follow);
        list.forEach(QuotationTest::collide);

        QuotationTest.supplier(QuotationTest::new);

        quotationTest.consumer(QuotationTest::collide);
    }


}

