package com.second.hand.trading.controller;

import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.payment.page.models.AlipayTradePagePayResponse;
import com.second.hand.trading.config.AliPayConfig;
import com.second.hand.trading.model.OrderModel;
import com.second.hand.trading.service.OrderService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/alipay")
public class AliPayController {

    @Resource
    private AliPayConfig aliPayConfig;

    @Resource
    private OrderService orderService;

    @GetMapping("/pay")
    public String pay(HttpServletRequest request,
                      @RequestParam(value = "name", required = false) String name,
                      @RequestParam(value = "no", required = false) String no,
                      @RequestParam(value = "price", required = false) String price) {
        AlipayTradePagePayResponse response;
        try {
            if (name == null || name.trim().isEmpty()) {
                throw new RuntimeException("订单参数缺失");
            }

            // 根据订单ID查询订单信息，获取订单号
            OrderModel order = orderService.getOrder(Long.parseLong(name));
            if (order == null) {
                throw new RuntimeException("订单不存在");
            }
            if (order.getPaymentStatus() != null && order.getPaymentStatus() == 1) {
                throw new RuntimeException("订单已支付");
            }
            if (order.getOrderStatus() == null || order.getOrderStatus() != 0) {
                throw new RuntimeException("当前订单状态不可支付");
            }

            HttpSession session = requestSession(request);
            if (session != null) {
                String shUserId = (String) session.getAttribute("shUserId");
                if (shUserId != null && !shUserId.isEmpty() && !Long.valueOf(shUserId).equals(order.getUserId())) {
                    throw new RuntimeException("无权限操作该资源");
                }
            }

            String orderNumber = order.getOrderNumber(); // 使用订单号
            String payAmount = order.getOrderPrice() == null ? null : order.getOrderPrice().stripTrailingZeros().toPlainString();
            if (payAmount == null) {
                throw new RuntimeException("订单金额异常");
            }
            
            // 设置同步返回地址，支付成功后跳转到订单页
            String returnUrl = aliPayConfig.getReturnUrl() + "?id=" + name;
            // 使用订单号作为 out_trade_no
            response = Factory.Payment.Page()
                    .pay("校园二手闲置物品交易平台", orderNumber, payAmount, returnUrl);
        } catch (Exception e) {
            System.err.println("调用遭遇异常，原因：" + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }

        System.out.println("***********************************************");
        System.out.println(response.getBody());
        System.out.println("***********************************************");
        return response.getBody();
    }

    /**
     * 支付宝异步回调
     * 支付成功后，支付宝会调用这个接口通知后端
     */
    @PostMapping("/notify")
    public String notify(HttpServletRequest request) {
        try {
            // 获取支付宝回调参数
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (String name : requestParams.keySet()) {
                params.put(name, request.getParameter(name));
            }

            System.out.println("收到支付宝回调：" + params);

            // 验证签名
            if (Factory.Payment.Common().verifyNotify(params)) {
                // 支付成功，更新订单状态
                String outTradeNo = params.get("out_trade_no"); // 商户订单号 ORDER_123
                String tradeStatus = params.get("trade_status"); // 交易状态

                System.out.println("订单号：" + outTradeNo + ", 状态：" + tradeStatus);

                if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                    // 直接使用订单号进行查询
                    if (outTradeNo != null) {
                        System.out.println("支付成功，开始更新订单状态，订单号: " + outTradeNo);
                        
                        // 根据订单号查询订单
                        OrderModel order = orderService.getOrderByNumber(outTradeNo);
                        if (order != null) {
                            // 防止重复支付：检查订单是否已经是已支付状态
                            if (order.getPaymentStatus() != null && order.getPaymentStatus() == 1) {
                                System.out.println("订单已经支付过：" + outTradeNo);
                                return "success";
                            }
                            if (order.getOrderStatus() == null || order.getOrderStatus() != 0) {
                                System.out.println("订单状态非法，拒绝更新：" + outTradeNo + " status=" + order.getOrderStatus());
                                return "fail";
                            }

                            String totalAmount = params.get("total_amount");
                            if (totalAmount == null || order.getOrderPrice() == null) {
                                System.out.println("回调金额参数缺失，拒绝更新：" + outTradeNo);
                                return "fail";
                            }
                            BigDecimal callbackAmount = new BigDecimal(totalAmount);
                            if (callbackAmount.compareTo(order.getOrderPrice()) != 0) {
                                System.out.println("金额校验失败，拒绝更新：" + outTradeNo + " callback=" + callbackAmount + " order=" + order.getOrderPrice());
                                return "fail";
                            }
                            
                            // 更新订单：支付状态=1（已支付），订单状态=1（待发货）
                            OrderModel orderModel = new OrderModel();
                            orderModel.setId(order.getId());
                            orderModel.setPaymentStatus((byte) 1); // 已支付
                            orderModel.setOrderStatus((byte) 1); // 待发货
                            orderModel.setPaymentTime(new java.util.Date());
                            
                            if (orderService.updateOrder(orderModel)) {
                                System.out.println("订单状态更新成功：" + order.getId());
                            } else {
                                System.out.println("订单状态更新失败：" + order.getId());
                            }
                        } else {
                            System.out.println("未找到对应的订单：" + outTradeNo);
                        }
                    }
                }

                return "success";
            } else {
                System.out.println("签名验证失败");
                return "fail";
            }
        } catch (Exception e) {
            System.err.println("回调处理异常：" + e.getMessage());
            e.printStackTrace();
            return "fail";
        }
    }

    private HttpSession requestSession(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return request.getSession(false);
    }
}
