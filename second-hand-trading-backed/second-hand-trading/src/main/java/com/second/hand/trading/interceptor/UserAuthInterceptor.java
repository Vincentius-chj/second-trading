package com.second.hand.trading.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.second.hand.trading.enums.ErrorMsg;
import com.second.hand.trading.model.UserModel;
import com.second.hand.trading.service.UserService;
import com.second.hand.trading.vo.ResultVo;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;

@Component
public class UserAuthInterceptor implements HandlerInterceptor {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final UserService userService;

    public UserAuthInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            Object userId = session.getAttribute("shUserId");
            if (userId != null && !String.valueOf(userId).trim().isEmpty()) {
                Long currentUserId;
                try {
                    currentUserId = Long.valueOf(String.valueOf(userId));
                } catch (NumberFormatException e) {
                    session.removeAttribute("shUserId");
                    writeFailResponse(response, ErrorMsg.COOKIE_ERROR);
                    return false;
                }

                UserModel currentUser = userService.getUser(currentUserId);
                if (currentUser == null) {
                    session.removeAttribute("shUserId");
                    writeFailResponse(response, ErrorMsg.COOKIE_ERROR);
                    return false;
                }

                if (currentUser.getUserStatus() != null && currentUser.getUserStatus().equals((byte) 1)) {
                    session.removeAttribute("shUserId");
                    writeFailResponse(response, ErrorMsg.ACCOUNT_Ban);
                    return false;
                }
                return true;
            }
        }

        writeFailResponse(response, ErrorMsg.COOKIE_ERROR);
        return false;
    }

    private void writeFailResponse(HttpServletResponse response, ErrorMsg errorMsg) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(OBJECT_MAPPER.writeValueAsString(ResultVo.fail(errorMsg)));
    }
}
