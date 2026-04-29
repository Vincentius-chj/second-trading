package com.second.hand.trading.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Spring Session配置类
 * 配置使用内存存储Session信息
 */
@Configuration
@EnableSpringHttpSession
public class SessionConfig {

    /**
     * 使用内存存储Session
     */
    @Bean
    public MapSessionRepository sessionRepository() {
        return new MapSessionRepository(new ConcurrentHashMap<>());
    }
}