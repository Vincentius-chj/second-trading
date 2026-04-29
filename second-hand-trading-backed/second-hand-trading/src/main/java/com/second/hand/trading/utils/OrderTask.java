package com.second.hand.trading.utils;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.second.hand.trading.model.OrderModel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

@Data
@NoArgsConstructor
public class OrderTask implements Delayed {

    /**
     * 延迟时间
     */
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private long time;

    private OrderModel orderModel;

    public OrderTask(OrderModel orderModel, long time) {
        this.orderModel = orderModel;
        this.time = System.currentTimeMillis() + 1000 * time;
    }

    @Override
    public long getDelay(@NotNull TimeUnit unit) {
        return time - System.currentTimeMillis();
    }

    @Override
    public int compareTo(@NotNull Delayed o) {
        OrderTask Order = (OrderTask) o;
        long diff = this.time - Order.time;
        if (diff <= 0) {
            return -1;
        } else {
            return 1;
        }
    }
}