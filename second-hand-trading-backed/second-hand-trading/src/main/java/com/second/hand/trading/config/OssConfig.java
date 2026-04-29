package com.second.hand.trading.config;

import com.second.hand.trading.properties.AliOssProperties;
import com.second.hand.trading.utils.AliOssUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置类，用于创建AliOssUtils对象
 */
@Configuration
public class OssConfig {

    @Bean
    public AliOssUtils aliOssUtils(AliOssProperties aliOssProperties) {
        return new AliOssUtils(
                aliOssProperties.getEndpoint(),
                aliOssProperties.getAccessKeyId(),
                aliOssProperties.getAccessKeySecret(),
                aliOssProperties.getBucketName());
    }
}
