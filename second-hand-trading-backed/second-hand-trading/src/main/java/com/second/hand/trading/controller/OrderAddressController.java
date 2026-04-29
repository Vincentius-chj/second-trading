package com.second.hand.trading.controller;

import com.second.hand.trading.enums.ErrorMsg;
import com.second.hand.trading.model.OrderAddressModel;
import com.second.hand.trading.service.OrderAddressService;
import com.second.hand.trading.vo.ResultVo;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order-address")
public class OrderAddressController {

    @Autowired
    private OrderAddressService orderAddressService;

    @PostMapping("/add")
    public ResultVo addOrderAddress(HttpServletRequest request,
                                   @RequestBody OrderAddressModel orderAddressModel) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        String shUserId = (String) session.getAttribute("shUserId");
        if (shUserId == null || shUserId.isEmpty()) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        return ResultVo.success(orderAddressService.addOrderAddress(orderAddressModel));
    }

    @PostMapping("/update")
    public ResultVo updateOrderAddress(HttpServletRequest request,
                                      @RequestBody OrderAddressModel orderAddressModel) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        String shUserId = (String) session.getAttribute("shUserId");
        if (shUserId == null || shUserId.isEmpty()) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        if (orderAddressService.updateOrderAddress(orderAddressModel)) {
            return ResultVo.success(orderAddressModel);
        }
        return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
    }

    @GetMapping("/info")
    public ResultVo getOrderAddress(HttpServletRequest request,
                                   @RequestParam Long orderId) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        String shUserId = (String) session.getAttribute("shUserId");
        if (shUserId == null || shUserId.isEmpty()) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        return ResultVo.success(orderAddressService.getOrderAddress(orderId));
    }
}