package com.second.hand.trading.utils;

import com.aliyun.oss.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;

@Data
@Slf4j
@AllArgsConstructor
public class AliOssUtils {

    // 存储服务的访问地址
    private String endpoint;
    // 身份凭证
    private String accessKeyId;
    // 密钥
    private String accessKeySecret;
    // 存储对象的容器名称
    private String bucketName;

    /**
     * 文件上传
     *
     * @param bytes 文件对象转成的bytes数组
     * @param objectName UUID生成的文件名（防止重名）
     */
    public String upload(byte[] bytes, String objectName) {

        // 创建OSSClient实例
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 创建PutObject请求（OSS阿里云存储对象的容器名称，UUID生成的文件名，文件的输入输出流）
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(bytes));
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                // 关闭ossClient
                ossClient.shutdown();
            }
        }

        //文件访问路径规则 https://BucketName.Endpoint/ObjectName
        String path = "https://" + bucketName + "." + endpoint + "/" + objectName;
        log.info("文件上传到：{}", path);

        return path;
    }
}
