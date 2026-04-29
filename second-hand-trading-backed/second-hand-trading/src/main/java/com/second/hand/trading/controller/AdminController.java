package com.second.hand.trading.controller;

import com.second.hand.trading.enums.ErrorMsg;
import com.second.hand.trading.model.AdminModel;
import com.second.hand.trading.model.IdleItemModel;
import com.second.hand.trading.model.UserModel;
import com.second.hand.trading.service.AdminService;
import com.second.hand.trading.service.IdleItemService;
import com.second.hand.trading.service.OrderService;
import com.second.hand.trading.service.UserService;
import com.second.hand.trading.vo.ResultVo;
import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private IdleItemService idleItemService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @GetMapping("login")
    public ResultVo login(@RequestParam("accountNumber") @NotNull @NotEmpty String accountNumber,
                          @RequestParam("adminPassword") @NotNull @NotEmpty String adminPassword,
                          HttpSession session) {
        // 下面是预留的 MD5 加密登录逻辑示例，当前不启用，避免影响现有登录行为。
        // String md5Password = DigestUtils.md5DigestAsHex(adminPassword.getBytes(StandardCharsets.UTF_8));
        // AdminModel adminModel = adminService.login(accountNumber, md5Password);
        AdminModel adminModel = adminService.login(accountNumber, adminPassword);
        if (null == adminModel) {
            return ResultVo.fail(ErrorMsg.EMAIL_LOGIN_ERROR);
        }
        // 使用独立的管理员会话标记，普通用户登录态保持不变。
        session.setAttribute("admin", adminModel);
        return ResultVo.success(adminModel);
    }

    @GetMapping("loginOut")
    public ResultVo loginOut(HttpSession session) {
        session.removeAttribute("admin");
        return ResultVo.success();
    }

    @GetMapping("list")
    public ResultVo getAdminList(HttpSession session,
                                 @RequestParam(value = "page", required = false) Integer page,
                                 @RequestParam(value = "nums", required = false) Integer nums) {
        if (session.getAttribute("admin") == null) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        int p = 1;
        int n = 8;
        if (null != page) {
            p = page > 0 ? page : 1;
        }
        if (null != nums) {
            n = nums > 0 ? nums : 8;
        }
        return ResultVo.success(adminService.getAdminList(p, n));
    }

    @PostMapping("add")
    public ResultVo addAdmin(HttpSession session,
                             @RequestBody AdminModel adminModel) {
        if (session.getAttribute("admin") == null) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        try {
            if (adminService.addAdmin(adminModel)) {
                return ResultVo.success();
            }
            return ResultVo.fail(ErrorMsg.PARAM_ERROR);
        } catch (DuplicateKeyException e) {
            // 管理员账号唯一键冲突
            return ResultVo.fail(ErrorMsg.ACCOUNT_EXIT);
        } catch (Exception e) {
            return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
        }
    }

    @GetMapping("idleList")
    public ResultVo idleList(HttpSession session,
                             @RequestParam("status") @NotNull @NotEmpty Integer status,
                             @RequestParam(value = "page", required = false) Integer page,
                             @RequestParam(value = "nums", required = false) Integer nums) {
        if (session.getAttribute("admin") == null) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        int p = 1;
        int n = 8;
        if (null != page) {
            p = page > 0 ? page : 1;
        }
        if (null != nums) {
            n = nums > 0 ? nums : 8;
        }
        return ResultVo.success(idleItemService.adminGetIdleList(status, p, n));
    }

    @GetMapping("updateIdleStatus")
    public ResultVo updateIdleStatus(HttpSession session,
                                     @RequestParam("id") @NotNull @NotEmpty Long id,
                                     @RequestParam("status") @NotNull @NotEmpty Integer status,
                                     @RequestParam(value = "rejectReason", required = false) String rejectReason
    ) {
        if (session.getAttribute("admin") == null) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        IdleItemModel idleItemModel = new IdleItemModel();
        idleItemModel.setId(id);
        idleItemModel.setIdleStatus(status.byteValue());
        // 驳回审核和违规下架都要求填写原因
        if (status == 2 || status == 4) {
            if (rejectReason == null || rejectReason.trim().isEmpty()) {
                return ResultVo.fail(ErrorMsg.OFFLINE_REASON_REQUIRED);
            }
            idleItemModel.setRejectReason(rejectReason.trim());
        }
        // 如果是通过审核（status=1），清空驳回理由
        if (status == 1) {
            idleItemModel.setRejectReason(null);
        }
        if (idleItemService.updateIdleItem(idleItemModel)) {
            return ResultVo.success();
        }
        return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
    }

    @GetMapping("orderList")
    public ResultVo orderList(HttpSession session,
                              @RequestParam(value = "page", required = false) Integer page,
                              @RequestParam(value = "nums", required = false) Integer nums) {
        if (session.getAttribute("admin") == null) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        int p = 1;
        int n = 8;
        if (null != page) {
            p = page > 0 ? page : 1;
        }
        if (null != nums) {
            n = nums > 0 ? nums : 8;
        }
        return ResultVo.success(orderService.getAllOrder(p, n));
    }

    @GetMapping("userList")
    public ResultVo userList(HttpSession session,
                             @RequestParam(value = "page", required = false) Integer page,
                             @RequestParam(value = "nums", required = false) Integer nums,
                             @RequestParam("status") @NotNull @NotEmpty Integer status) {
        if (session.getAttribute("admin") == null) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        int p = 1;
        int n = 8;
        if (null != page) {
            p = page > 0 ? page : 1;
        }
        if (null != nums) {
            n = nums > 0 ? nums : 8;
        }
        return ResultVo.success(userService.getUserByStatus(status, p, n));
    }

    @GetMapping("updateUserStatus")
    public ResultVo updateUserStatus(HttpSession session,
                                     @RequestParam("id") @NotNull @NotEmpty Long id,
                                     @RequestParam("status") @NotNull @NotEmpty Integer status) {
        if (session.getAttribute("admin") == null) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        UserModel userModel = new UserModel();
        userModel.setId(id);
        userModel.setUserStatus(status.byteValue());
        if (userService.updateUserInfo(userModel))
            return ResultVo.success();
        return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
    }


    // 按订单闲置物品名称查询
    @GetMapping("queryIdle")
    public ResultVo queryIdle(@RequestParam(value = "idleName", required = false) String idleName,
                              @RequestParam(value = "minPrice", required = false) String minPrice,
                              @RequestParam(value = "maxPrice", required = false) String maxPrice,
                              @RequestParam(value = "idleLabel", required = false) Integer idleLabel,
                              @RequestParam(value = "idlePlace", required = false) String idlePlace,
                              @RequestParam(value = "userNickname", required = false) String userNickname,
                              @RequestParam(value = "startTime", required = false) String startTime,
                              @RequestParam(value = "endTime", required = false) String endTime,
                              @RequestParam(value = "page", required = false) Integer page,
                              @RequestParam(value = "nums", required = false) Integer nums,
                              @RequestParam("status") @NotNull @NotEmpty Integer status) {
        int p = 1;
        int n = 8;
        if (null != page) {
            p = page > 0 ? page : 1;
        }
        if (null != nums) {
            n = nums > 0 ? nums : 8;
        }

        // 如果有多个搜索参数，使用多字段搜索
        if ((null != idleName && !"".equals(idleName)) || (null != minPrice && !"".equals(minPrice)) || 
            (null != maxPrice && !"".equals(maxPrice)) || (null != idleLabel) || 
            (null != idlePlace && !"".equals(idlePlace)) || (null != userNickname && !"".equals(userNickname)) || 
            (null != startTime && !"".equals(startTime)) || (null != endTime && !"".equals(endTime))) {
            // 使用按状态的多字段搜索
            return ResultVo.success(idleItemService.findIdleItemByMultiFieldsWithStatus(idleName, minPrice, maxPrice, idleLabel, idlePlace, userNickname, startTime, endTime, status, p, n));
        } else {
            // 如果没有搜索参数，使用原有搜索
            String findValue = (idleName != null) ? idleName : "";
            System.out.println(findValue + " " + page + " " + nums + " " + status);
            // 根据status参数使用对应的查询方法
            if (status == 1)
                return ResultVo.success(idleItemService.findIdleItem(findValue, p, n));
            else
                return ResultVo.success(idleItemService.findIdleItemByStatus(findValue, status, p, n));
        }
    }

    // 按订单号查询订单
    @GetMapping("queryOrder")
    public ResultVo queryOrder(HttpSession session,
                               @RequestParam(value = "orderNumber", required = false) String orderNumber,
                               @RequestParam(value = "idleName", required = false) String idleName,
                               @RequestParam(value = "minPrice", required = false) String minPrice,
                               @RequestParam(value = "maxPrice", required = false) String maxPrice,
                               @RequestParam(value = "buyerNickname", required = false) String buyerNickname,
                               @RequestParam(value = "sellerNickname", required = false) String sellerNickname,
                               @RequestParam(value = "orderStatus", required = false) Integer orderStatus,
                               @RequestParam(value = "paymentStatus", required = false) Integer paymentStatus,
                               @RequestParam(value = "startTime", required = false) String startTime,
                               @RequestParam(value = "endTime", required = false) String endTime,
                               @RequestParam(value = "page", required = false) Integer page,
                               @RequestParam(value = "nums", required = false) Integer nums) {

        if (session.getAttribute("admin") == null) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }

        int p = 1;
        int n = 8;
        if (null != page) {
            p = page > 0 ? page : 1;
        }
        if (null != nums) {
            n = nums > 0 ? nums : 8;
        }

        // 如果有多个搜索参数，使用多字段搜索
        if ((null != orderNumber && !"".equals(orderNumber)) || (null != idleName && !"".equals(idleName)) || 
            (null != minPrice && !"".equals(minPrice)) || (null != maxPrice && !"".equals(maxPrice)) ||
            (null != buyerNickname && !"".equals(buyerNickname)) || 
            (null != sellerNickname && !"".equals(sellerNickname)) || (null != orderStatus) || 
            (null != paymentStatus) || (null != startTime && !"".equals(startTime)) || (null != endTime && !"".equals(endTime))) {
            return ResultVo.success(orderService.findOrderByMultiFields(orderNumber, idleName, minPrice, maxPrice, buyerNickname, sellerNickname, orderStatus, paymentStatus, startTime, endTime, p, n));
        } else {
            // 如果没有搜索参数，使用原有搜索
            String searchValue = (orderNumber != null) ? orderNumber : "";
            if (null == searchValue || "".equals(searchValue))
                return ResultVo.success(orderService.getAllOrder(p, n));
            return ResultVo.success(orderService.findOrderByNumber(searchValue, p, n));
        }
    }


    // 根据用户ID、昵称、账号或注册时间范围来查找信息
    @GetMapping("queryUser")
    public ResultVo queryUser(HttpSession session,
                              @RequestParam(value = "id", required = false) String id,
                              @RequestParam(value = "nickname", required = false) String nickname,
                              @RequestParam(value = "accountNumber", required = false) String accountNumber,
                              @RequestParam(value = "startTime", required = false) String startTime,
                              @RequestParam(value = "endTime", required = false) String endTime,
                              @RequestParam(value = "mode", required = false) Integer mode,
                              @RequestParam(value = "page", required = false) Integer page,
                              @RequestParam(value = "nums", required = false) Integer nums) {
        if (session.getAttribute("admin") == null) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        int p = 1;
        int n = 8;
        if (null != page) {
            p = page > 0 ? page : 1;
        }
        if (null != nums) {
            n = nums > 0 ? nums : 8;
        }

        if (mode == 1 || mode == 2) {
            if ((null == id || "".equals(id)) && (null == nickname || "".equals(nickname)) && (null == accountNumber || "".equals(accountNumber)) && (null == startTime || "".equals(startTime)) && (null == endTime || "".equals(endTime))) {
                // 如果没有搜索条件，返回对应模式的全部用户
                if (mode == 1) {
                    return ResultVo.success(userService.getUserByStatus(0, p, n));
                } else {
                    return ResultVo.success(userService.getUserByStatus(1, p, n));
                }
            } else {
                // 如果有搜索条件，进行多字段搜索
                return ResultVo.success(userService.getUserByMultiFields(id, nickname, accountNumber, startTime, endTime, mode, p, n));
            }
        } else {
            // 管理员模式
            return ResultVo.success(adminService.getAdminList(p, n));
        }
    }

}
