package com.xinbo.sports.utils;


import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;


public class IdUtils {


    /**
     * 获取雪花算序列号
     *
     * @return
     */
    public static Long getSnowFlakeId() {
        return SnowFlake.getInstance().nextId();
    }


    /**
     * 以毫微秒做基础计数, 返回唯一有序增长ID
     * <pre>System.nanoTime()</pre>
     * <pre>
     * 线程数量: 100
     * 执行次数: 1000
     * 平均耗时: 222 ms
     * 数组长度: 100000
     * Map Size: 100000
     * </pre>
     *
     * @return ID长度32位
     */
    public static String getPrimaryKey() {
        return MathUtils.makeUpNewData(Thread.currentThread().hashCode() + "", 3) + MathUtils.randomDigitNumber(7);           //随机7位数
    }

    /**
     * 获取uuid
     *
     * @return
     */
    public static String getSerialId() {
        UUID uuid = UUID.randomUUID();
        String serialId = uuid.toString().replace("-", "");
        return serialId;
    }

    /**
     * 随机获取ASCII码表35-122的字符
     *
     * @return
     */
    private static char getChar() {
        Random random = new Random();
        int num = random.nextInt(87) + 35;
        return (char) num;
    }
}
