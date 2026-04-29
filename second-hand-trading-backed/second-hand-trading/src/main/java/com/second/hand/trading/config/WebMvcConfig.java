package com.second.hand.trading.config;

import com.second.hand.trading.interceptor.LogCostInterceptor;
import com.second.hand.trading.interceptor.UserAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private UserAuthInterceptor userAuthInterceptor;

    /**
     * 允许跨域访问
     * 当一个请求url的协议、域名、端口三者之间任意一个与当前页面url不同即为跨域。
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedHeaders("*")
                .allowedMethods("*")
                .allowedOriginPatterns("*")
                .allowCredentials(true)
                .exposedHeaders(HttpHeaders.SET_COOKIE);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 日志记录拦截器，应用到所有路径
        registry.addInterceptor(new LogCostInterceptor()).addPathPatterns("/**");

        // 用户鉴权拦截器：统一拦截需要登录的用户接口，并在拦截器内做封禁态校验
        registry.addInterceptor(userAuthInterceptor)
            .addPathPatterns(
                "/address/**",
                "/favorite/**",
                "/order-address/**",
                "/order/**",
                "/review/add",
                "/review/order",
                "/message/send",
                "/message/my",
                "/message/delete",
                "/idle/add",
                "/idle/all",
                "/idle/update",
                "/user/info",
                "/user/current-user",
                "/user/background",
                "/user/password"
            );
    }
}
