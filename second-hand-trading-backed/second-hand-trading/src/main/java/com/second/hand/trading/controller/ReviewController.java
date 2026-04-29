package com.second.hand.trading.controller;

import com.second.hand.trading.enums.ErrorMsg;
import com.second.hand.trading.model.MessageModel;
import com.second.hand.trading.model.OrderModel;
import com.second.hand.trading.service.MessageService;
import com.second.hand.trading.service.OrderService;
import com.second.hand.trading.vo.ResultVo;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/review")
public class ReviewController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private OrderService orderService;

    @PostMapping("/add")
    public ResultVo addReview(HttpServletRequest request,
                              @RequestBody MessageModel reviewModel) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        String shUserId = (String) session.getAttribute("shUserId");
        if (shUserId == null || shUserId.isEmpty()) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        if (reviewModel.getOrderId() == null || reviewModel.getScore() == null) {
            return ResultVo.fail(ErrorMsg.PARAM_ERROR);
        }
        if (reviewModel.getContent() == null || reviewModel.getContent().trim().isEmpty()) {
            return ResultVo.fail(ErrorMsg.PARAM_ERROR);
        }
        if (reviewModel.getScore() < 1 || reviewModel.getScore() > 5) {
            return ResultVo.fail(ErrorMsg.REVIEW_SCORE_INVALID);
        }

        Long currentUserId = Long.valueOf(shUserId);
        OrderModel orderModel = orderService.getOrder(reviewModel.getOrderId());
        if (orderModel == null || orderModel.getIdleItem() == null) {
            return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
        }
        // 仅允许买家评价，且必须是已完成订单
        if (!currentUserId.equals(orderModel.getUserId()) || orderModel.getOrderStatus() != 3) {
            return ResultVo.fail(ErrorMsg.REVIEW_NOT_ALLOWED);
        }

        MessageModel existReview = messageService.findReviewByOrderIdAndUserId(orderModel.getId(), currentUserId);
        if (existReview != null) {
            return ResultVo.fail(ErrorMsg.REVIEW_ALREADY_EXISTS);
        }

        reviewModel.setUserId(currentUserId);
        reviewModel.setMessageType(1);
        reviewModel.setOrderId(orderModel.getId());
        reviewModel.setIdleId(orderModel.getIdleId());
        reviewModel.setToUser(orderModel.getIdleItem().getUserId());
        reviewModel.setContent(reviewModel.getContent().trim());
        reviewModel.setToMessage(null);
        reviewModel.setCreateTime(new Date());

        if (messageService.addMessage(reviewModel)) {
            return ResultVo.success(reviewModel);
        }
        return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
    }

    @GetMapping("/idle")
    public ResultVo getIdleReview(@RequestParam Long idleId) {
        return ResultVo.success(messageService.getAllIdleReview(idleId));
    }

    @GetMapping("/order")
    public ResultVo getOrderReview(HttpServletRequest request,
                                   @RequestParam Long orderId) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        String shUserId = (String) session.getAttribute("shUserId");
        if (shUserId == null || shUserId.isEmpty()) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }

        OrderModel orderModel = orderService.getOrder(orderId);
        if (orderModel == null || orderModel.getIdleItem() == null) {
            return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
        }

        Long currentUserId = Long.valueOf(shUserId);
        if (!currentUserId.equals(orderModel.getUserId()) && !currentUserId.equals(orderModel.getIdleItem().getUserId())) {
            return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
        }

        return ResultVo.success(messageService.getOrderReview(orderId));
    }
}
