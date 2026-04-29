package com.second.hand.trading.controller;

import com.second.hand.trading.enums.ErrorMsg;
import com.second.hand.trading.model.OrderModel;
import com.second.hand.trading.service.OrderService;
import com.second.hand.trading.utils.IdFactoryUtil;
import com.second.hand.trading.utils.OrderTaskHandler;
import com.second.hand.trading.vo.ResultVo;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;


@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/add")
    public ResultVo addOrder(HttpServletRequest request,
                            @RequestBody OrderModel orderModel) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        String shUserId = (String) session.getAttribute("shUserId");
        if (shUserId == null || shUserId.isEmpty()) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        if (OrderTaskHandler.orderService == null) {
            OrderTaskHandler.orderService = orderService;
        }
        if (orderModel.getOrderCount() == null) {
            orderModel.setOrderCount(1);
        }
        if (orderModel.getOrderCount() <= 0) {
            return ResultVo.fail(ErrorMsg.PARAM_ERROR);
        }
        orderModel.setOrderNumber(IdFactoryUtil.getOrderId());
        orderModel.setCreateTime(new Date());
        orderModel.setUserId(Long.valueOf(shUserId));
        orderModel.setOrderStatus((byte) 0);
        orderModel.setPaymentStatus((byte) 0);
        if (orderService.addOrder(orderModel)) {
            return ResultVo.success(orderModel);
        } else {
            return ResultVo.fail(ErrorMsg.ORDER_CREATE_FAILED);
        }
    }

    @GetMapping("/info")
    public ResultVo getOrderInfo(HttpServletRequest request,
                                @RequestParam Long id) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        String shUserId = (String) session.getAttribute("shUserId");
        if (shUserId == null || shUserId.isEmpty()) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        OrderModel orderModel = orderService.getOrder(id);
        if (orderModel == null || orderModel.getIdleItem() == null) {
            return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
        }
        if (orderModel.getUserId().equals(Long.valueOf(shUserId)) ||
                orderModel.getIdleItem().getUserId().equals(Long.valueOf(shUserId))) {
            return ResultVo.success(orderModel);
        }
        return ResultVo.fail(ErrorMsg.RESOURCE_FORBIDDEN);
    }

    @PostMapping("/update")
    public ResultVo updateOrder(HttpServletRequest request,
                               @RequestBody OrderModel orderModel) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        String shUserId = (String) session.getAttribute("shUserId");
        if (shUserId == null || shUserId.isEmpty()) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }

        if (orderModel.getId() == null || orderModel.getOrderStatus() == null) {
            return ResultVo.fail(ErrorMsg.PARAM_ERROR);
        }

        OrderModel dbOrder = orderService.getOrder(orderModel.getId());
        if (dbOrder == null || dbOrder.getIdleItem() == null) {
            return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
        }

        Long currentUserId = Long.valueOf(shUserId);
        boolean isBuyer = currentUserId.equals(dbOrder.getUserId());
        boolean isSeller = currentUserId.equals(dbOrder.getIdleItem().getUserId());
        if (!isBuyer && !isSeller) {
            return ResultVo.fail(ErrorMsg.RESOURCE_FORBIDDEN);
        }

        Byte oldStatus = dbOrder.getOrderStatus();
        Byte newStatus = orderModel.getOrderStatus();
        boolean allowed = false;
        if (isSeller) {
            allowed = oldStatus == 1 && newStatus == 2;
        }
        if (isBuyer) {
            allowed = allowed || (oldStatus == 2 && newStatus == 3);
        }
        if (!allowed) {
            return ResultVo.fail(ErrorMsg.ORDER_STATE_INVALID);
        }

        // 前端传入的支付状态不可信，仅允许系统支付回调更新
        orderModel.setPaymentStatus(null);
        if (orderService.updateOrder(orderModel)) {
            return ResultVo.success(orderModel);
        }
        return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
    }

    @PostMapping("/cancel")
    public ResultVo cancelOrder(HttpServletRequest request,
                               @RequestParam("id") @NotNull Long orderId) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        String shUserId = (String) session.getAttribute("shUserId");
        if (shUserId == null || shUserId.isEmpty()) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        // 检查订单是否属于当前用户
        OrderModel order = orderService.getOrder(orderId);
        if (order == null || order.getIdleItem() == null) {
            return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
        }
        if (!order.getUserId().equals(Long.valueOf(shUserId))) {
            return ResultVo.fail(ErrorMsg.RESOURCE_FORBIDDEN);
        }
        // 只允许取消待付款的订单
        if (order.getOrderStatus() != 0) {
            return ResultVo.fail(ErrorMsg.ORDER_STATE_INVALID);
        }
        // 设置订单状态为已取消
        OrderModel cancelOrder = new OrderModel();
        cancelOrder.setId(orderId);
        cancelOrder.setOrderStatus((byte) 4);
        if (orderService.updateOrder(cancelOrder)) {
            return ResultVo.success(cancelOrder);
        }
        return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
    }

    @GetMapping("/my")
    public ResultVo getMyOrder(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        String shUserId = (String) session.getAttribute("shUserId");
        if (shUserId == null || shUserId.isEmpty()) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        return ResultVo.success(orderService.getMyOrder(Long.valueOf(shUserId)));
    }

    @GetMapping("/my-sold")
    public ResultVo getMySoldIdle(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        String shUserId = (String) session.getAttribute("shUserId");
        if (shUserId == null || shUserId.isEmpty()) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        return ResultVo.success(orderService.getMySoldIdle(Long.valueOf(shUserId)));
    }

}