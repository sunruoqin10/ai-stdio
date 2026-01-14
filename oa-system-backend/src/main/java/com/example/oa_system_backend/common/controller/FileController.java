package com.example.oa_system_backend.common.controller;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/api/upload")
public class FileController {

    @Value("${file.upload.path:/tmp/uploads}")
    private String uploadPath;

    @Value("${file.upload.url-prefix:/uploads}")
    private String urlPrefix;

    /**
     * 上传文件
     */
    @PostMapping
    public UploadResponse uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        try {
            // 创建按日期分类的目录
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String relativePath = dateDir + "/" + UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path targetPath = Paths.get(uploadPath, relativePath);

            // 确保目录存在
            Files.createDirectories(targetPath.getParent());

            // 保存文件
            file.transferTo(targetPath.toFile());

            // 返回文件访问URL
            String fileUrl = urlPrefix + "/" + relativePath.replace("\\", "/");

            UploadResponse response = new UploadResponse();
            response.setCode(0);
            response.setMessage("上传成功");
            response.setUrl(fileUrl);

            return response;

        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 上传响应对象
     */
    @Data
    public static class UploadResponse {
        private Integer code;
        private String message;
        private String url;
    }
}
