package com.second.hand.trading.utils;

import com.second.hand.trading.model.OrderModel;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.*;

@Component
public class OrderTimeoutScheduler {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

    private final ApplicationEventPublisher eventPublisher;

    public OrderTimeoutScheduler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    private final ConcurrentHashMap<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    /**
     * 订单超时事件
     */
    public static class OrderTimeoutEvent extends ApplicationEvent {
        private final OrderModel orderModel;

        public OrderTimeoutEvent(Object source, OrderModel orderModel) {
            super(source);
            this.orderModel = orderModel;
        }

        public OrderModel getOrderModel() {
            return orderModel;
        }
    }

    /**
     * 为订单安排超时取消任务
     * @param orderModel 订单对象
     * @param timeoutSeconds 超时时间（秒）
     */
    public void scheduleOrderTimeout(OrderModel orderModel, long timeoutSeconds) {
        // 取消之前可能存在的任务
        cancelOrderTimeout(orderModel.getId());
        
        // 创建新的超时任务
        ScheduledFuture<?> future = scheduler.schedule(() -> {
            // 发布订单超时事件
            eventPublisher.publishEvent(new OrderTimeoutEvent(this, orderModel));
        }, timeoutSeconds, TimeUnit.SECONDS);
        
        // 存储任务引用，以便后续取消
        scheduledTasks.put(orderModel.getId(), future);
        
        System.out.println("已为订单 " + orderModel.getId() + " 安排 " + timeoutSeconds + " 秒后超时取消");
    }

    /**
     * 取消订单的超时任务
     * @param orderId 订单ID
     */
    public void cancelOrderTimeout(Long orderId) {
        ScheduledFuture<?> future = scheduledTasks.remove(orderId);
        if (future != null && !future.isDone()) {
            future.cancel(false);
            System.out.println("已取消订单 " + orderId + " 的超时任务");
        }
    }

    /**
     * 检查订单是否有超时任务
     * @param orderId 订单ID
     * @return 是否存在超时任务
     */
    public boolean hasTimeoutTask(Long orderId) {
        ScheduledFuture<?> future = scheduledTasks.get(orderId);
        return future != null && !future.isDone();
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}