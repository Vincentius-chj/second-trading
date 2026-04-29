package com.second.hand.trading.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "alioss")
public class AliOssProperties {
    // 存储服务的访问地址
    private String endpoint;
    // 身份凭证
    private String accessKeyId;
    // 密钥
    private String accessKeySecret;
    // 存储对象的容器名称
    private String bucketName;
}
