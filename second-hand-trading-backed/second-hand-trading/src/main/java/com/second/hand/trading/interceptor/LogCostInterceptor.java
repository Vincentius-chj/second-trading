package com.second.hand.trading.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;

/**
 * 拦截器
 */
public class LogCostInterceptor implements HandlerInterceptor {
    private long start = System.currentTimeMillis();

    @Override
    public boolean preHandle(@NotNull HttpServletRequest httpServletRequest, @NotNull HttpServletResponse httpServletResponse, @NotNull Object o) throws Exception {
        start = System.currentTimeMillis();
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, @NotNull HttpServletResponse httpServletResponse, @NotNull Object o, ModelAndView modelAndView) throws Exception {
        System.out.println("请求：" + httpServletRequest.getRequestURI() + "?" + httpServletRequest.getQueryString() + "，耗时" + (System.currentTimeMillis() - start) + "ms  " + new Date());
    }

    @Override
    public void afterCompletion(@NotNull HttpServletRequest httpServletRequest, @NotNull HttpServletResponse httpServletResponse, @NotNull Object o, Exception e) throws Exception {
    }
}