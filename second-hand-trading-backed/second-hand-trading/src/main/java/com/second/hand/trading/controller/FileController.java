package com.second.hand.trading.controller;

import com.second.hand.trading.enums.ErrorMsg;
import com.second.hand.trading.utils.AliOssUtils;
import com.second.hand.trading.vo.ResultVo;
import kotlin.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
public class FileController {

    @Autowired
    private AliOssUtils aliOssUtils;

    /**
     * 文件上传
     *
     * @param multipartFile
     * @return
     */
    @PostMapping("/upload")
    public ResultVo upload(@RequestParam("file") MultipartFile multipartFile) {
        log.info("文件上传：{}", multipartFile);

        try {
            // 获取原始文件名
            String originalFilename = multipartFile.getOriginalFilename();
            // 获取原始文件名后缀
            String extension = null;
            if (originalFilename != null && !originalFilename.isEmpty()) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            // 构造新文件名
            String objectName = UUID.randomUUID() + extension;
            // 获取文件的请求路径
            String filePath = aliOssUtils.upload(multipartFile.getBytes(), objectName);
            // 返回
            return ResultVo.success(filePath);
        } catch (IOException e) {
            log.info("文件上传失败");
        }

        // 文件上传失败
        return ResultVo.fail(ErrorMsg.FILE_UPLOAD_FAILED);
    }

}
