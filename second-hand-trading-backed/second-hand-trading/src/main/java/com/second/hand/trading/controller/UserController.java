package com.second.hand.trading.controller;


import com.second.hand.trading.enums.ErrorMsg;
import com.second.hand.trading.model.UserModel;
import com.second.hand.trading.service.UserService;
import com.second.hand.trading.vo.ResultVo;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Duration;

@RestController
@RequestMapping("user")
public class UserController {

    @Autowired
    private UserService userService;


    /**
     * 注册账号
     *
     * @param userModel
     * @return
     */
    @PostMapping("sign-in")
    public ResultVo signIn(@RequestBody UserModel userModel) {
        System.out.println(userModel);
        userModel.setSignInTime(new Timestamp(System.currentTimeMillis()));
        if (userModel.getAvatar() == null || "".equals(userModel.getAvatar())) {
            userModel.setAvatar("https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png");
        }
        if (userService.userSignIn(userModel)) {
            return ResultVo.success(userModel);
        }
        return ResultVo.fail(ErrorMsg.REGISTER_ERROR);
    }

    /**
     * 登录，使用Spring Session管理登录状态
     *
     * @param accountNumber
     * @param userPassword
     * @param request
     * @return
     */
    @RequestMapping("login")
    public ResultVo login(@RequestParam("accountNumber") @NotEmpty @NotNull String accountNumber,
                          @RequestParam("userPassword") @NotEmpty @NotNull String userPassword,
                          HttpServletRequest request) {
        // 下面是预留的 MD5 加密登录逻辑示例，当前不启用，避免影响现有登录行为。
        // String md5Password = DigestUtils.md5DigestAsHex(userPassword.getBytes(StandardCharsets.UTF_8));
        // UserModel userModel = userService.userLogin(accountNumber, md5Password);
        UserModel userModel = userService.userLogin(accountNumber, userPassword);
        System.out.println("登录：" + userModel);

        System.out.println("===================================");
        System.out.println(accountNumber + "   " + userPassword);
        System.out.println("===================================");

        if (null == userModel) {
            return ResultVo.fail(ErrorMsg.EMAIL_LOGIN_ERROR);
        }

        // 用户名或者密码为空
        if (accountNumber.isEmpty() || userPassword.isEmpty()) {
            return ResultVo.fail(ErrorMsg.EMAIL_LOGIN_ERROR);
        }

        // 手机号长度不足11位
        if (userModel.getAccountNumber().length() != 11) {
            return ResultVo.fail(ErrorMsg.EMAIL_LOGIN_ERROR);
        }

        if (userModel.getUserStatus() != null && userModel.getUserStatus().equals((byte) 1)) {
            return ResultVo.fail(ErrorMsg.ACCOUNT_Ban);
        }

        // 使用Spring Session存储用户ID；普通用户与管理员会话互不影响。
        HttpSession session = request.getSession();
        session.setAttribute("shUserId", String.valueOf(userModel.getId()));
        
        return ResultVo.success(userModel);
    }

    /**
     * 退出登录
     *
     * @param request
     * @return
     */
    @RequestMapping("logout")
    public ResultVo logout(HttpServletRequest request) {
        // 仅清理普通用户登录态，保留管理员会话
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute("shUserId");
        }
        return ResultVo.success();
    }

    /**
     * 获取用户信息
     *
     * @param request
     * @return
     */
    @GetMapping("info")
    public ResultVo getOneUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        String userId = (String) session.getAttribute("shUserId");
        if (userId == null || userId.isEmpty()) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        return ResultVo.success(userService.getUser(Long.valueOf(userId)));
    }
    
    /**
     * 获取当前登录用户信息
     *
     * @param request
     * @return
     */
    @GetMapping("current-user")
    public ResultVo getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        String userId = (String) session.getAttribute("shUserId");
        if (userId == null || userId.isEmpty()) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        return ResultVo.success(userService.getUser(Long.valueOf(userId)));
    }

    /**
     * 修改用户公开信息
     *
     * @param request
     * @param userModel
     * @return
     */
    @PostMapping("/info")
    public ResultVo updateUserPublicInfo(HttpServletRequest request, @RequestBody UserModel userModel) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        String userId = (String) session.getAttribute("shUserId");
        if (userId == null || userId.isEmpty()) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        userModel.setId(Long.valueOf(userId));
        if (userService.updateUserInfo(userModel)) {
            return ResultVo.success();
        }
        return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
    }


    /**
     * 更新用户背景图片
     *
     * @param request
     * @param userModel
     * @return
     */
    @PostMapping("/background")
    public ResultVo updateUserBackground(HttpServletRequest request, @RequestBody UserModel userModel) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        String userId = (String) session.getAttribute("shUserId");
        if (userId == null || userId.isEmpty()) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        if (userService.updateUserBackground(userModel.getBackgroundImg(), Long.valueOf(userId))) {
            return ResultVo.success();
        }
        return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
    }


    /**
     * 修改密码
     *
     * @param request
     * @param oldPassword
     * @param newPassword
     * @return
     */
    @GetMapping("/password")
    public ResultVo updateUserPassword(HttpServletRequest request,
                                       @RequestParam("oldPassword") @NotEmpty @NotNull String oldPassword,
                                       @RequestParam("newPassword") @NotEmpty @NotNull String newPassword) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        String userId = (String) session.getAttribute("shUserId");
        if (userId == null || userId.isEmpty()) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        if (userService.updatePassword(newPassword, oldPassword, Long.valueOf(userId))) {
            return ResultVo.success();
        }
        return ResultVo.fail(ErrorMsg.PASSWORD_RESET_ERROR);
    }
}