package com.xinbo.sports.plat.service.impl;

import com.google.common.collect.HashMultimap;
import org.junit.jupiter.api.Test;
import org.springframework.cglib.proxy.Callback;

/**
 * @author: wells
 * @date: 2020/5/29
 * @description:
 */

public class TestCallBack {
    public void callBack(Callback callback) {

    }

    @Test
    void testPrintlnMap() {
        HashMultimap<Integer, Integer> hashMultimap = HashMultimap.create();
        hashMultimap.put(1, 2);
        hashMultimap.put(1, 3);
        System.out.println("hashMultimap" + hashMultimap);
    }
}
