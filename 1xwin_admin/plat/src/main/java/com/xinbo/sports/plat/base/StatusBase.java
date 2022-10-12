package com.xinbo.sports.plat.base;

import com.xinbo.sports.plat.io.enums.BasePlatParam;

import java.math.BigDecimal;

/**
 * @author: wells
 * @date: 2020/6/12
 * @description:
 */
public final class StatusBase {

    private StatusBase() {
    }

    /**
     * 判断输赢金额获取对应的状态
     *
     * @param profit
     * @return
     */
    public static Integer checkStatus(BigDecimal profit) {
        if (profit == null) {
            return BasePlatParam.BetRecordsStatus.DRAW.getCode();
        }
        if (profit.compareTo(BigDecimal.ZERO) > 0) {
            return BasePlatParam.BetRecordsStatus.WIN.getCode();
        } else if (profit.compareTo(BigDecimal.ZERO) < 0) {
            return BasePlatParam.BetRecordsStatus.LOSE.getCode();
        } else {
            return BasePlatParam.BetRecordsStatus.DRAW.getCode();
        }
    }
}
