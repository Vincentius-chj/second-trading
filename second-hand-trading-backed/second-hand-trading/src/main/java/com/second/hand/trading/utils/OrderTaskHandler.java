package com.second.hand.trading.utils;

import com.second.hand.trading.service.OrderService;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.DelayQueue;

@Slf4j
public class OrderTaskHandler {

    public static OrderService orderService = null;

    private static final DelayQueue<OrderTask> delayQueue = new DelayQueue<>();

    public static void run() {
        new Thread(() -> {
            log.info("订单超时处理线程已启动");
            while (true) {
                try {
                    // 使用 take() 方法阻塞等待，直到有任务到期
                    OrderTask orderTask = delayQueue.take();
                    if (orderService != null && orderTask != null) {
                        if (orderService.updateOrder(orderTask.getOrderModel())) {
                            System.out.println("成功取消超时订单：" + orderTask.getOrderModel().getId());
                        } else {
                            System.out.println("取消超时订单失败：" + orderTask.getOrderModel().getId());
                        }
                    }
                } catch (InterruptedException e) {
                    System.out.println("订单超时处理线程被中断");
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.out.println("处理订单超时任务时发生异常：" + e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void addOrder(OrderTask o) {
        System.out.println("添加任务：" + o);
        delayQueue.put(o);
    }
}
