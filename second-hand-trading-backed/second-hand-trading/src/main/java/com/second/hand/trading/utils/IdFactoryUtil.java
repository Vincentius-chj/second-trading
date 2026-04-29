package com.second.hand.trading.utils;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.Random;

// id自增原子操作

public class IdFactoryUtil {

    private static AtomicInteger orderIdEnd = new AtomicInteger(1);
    private static AtomicInteger fileIdEnd = new AtomicInteger(1);

    public static String getOrderId() {
        // 使用时间戳+自增序号生成纯数字订单号
        long timestamp = System.currentTimeMillis();
        int sequence = orderIdEnd.getAndIncrement();
        // 限制序列号在合理范围内，防止溢出
        if (sequence > 9999) {
            orderIdEnd.set(1);
            sequence = 1;
        }
        // 确保序列号至少为4位，不足补零
        String seqStr = String.format("%04d", sequence);
        return String.valueOf(timestamp) + seqStr;
    }

    public static String getFileId() {
        int newI;
        int ord;
        do {
            ord = fileIdEnd.get();
            newI = (ord + 1) % 1000;
        }
        while (!fileIdEnd.compareAndSet(ord, newI));
        return System.currentTimeMillis() + "" + (newI + 1000);
    }
}
