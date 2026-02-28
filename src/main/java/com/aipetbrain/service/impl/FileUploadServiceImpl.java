package com.aipetbrain.service.impl;

import com.aipetbrain.config.TencentCosConfig;
import com.aipetbrain.service.FileUploadService;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * 文件上传服务实现 - 支持腾讯云 COS 和本地存储
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {

    private final COSClient cosClient;
    private final TencentCosConfig.CosProperties cosProperties;

    @Value("${cos.enabled:false}")
    private boolean cosEnabled;

    @Value("${file.upload-path:/app/uploads/}")
    private String uploadPath;

    @Value("${file.base-url:http://localhost:8080/api/files/}")
    private String baseUrl;

    @Override
    public String uploadImage(MultipartFile file, String directory) throws Exception {
        if (cosEnabled && cosClient != null) {
            return uploadToCOS(file, "images/" + (directory != null ? directory + "/" : ""));
        } else {
            return uploadToLocal(file, "images/" + (directory != null ? directory + "/" : ""));
        }
    }

    @Override
    public String uploadAvatar(MultipartFile file, Long userId) throws Exception {
        return uploadImage(file, "avatars/user_" + userId);
    }

    @Override
    public String uploadPetPhoto(MultipartFile file, Long petId) throws Exception {
        return uploadImage(file, "pets/pet_" + petId);
    }

    @Override
    public String uploadLostPetImage(MultipartFile file, Long lostPetId) throws Exception {
        return uploadImage(file, "lost-pets/pet_" + lostPetId);
    }

    @Override
    public boolean deleteFile(String fileUrl) throws Exception {
        if (cosEnabled && cosClient != null) {
            try {
                // 提取对象键（从 URL 中提取）
                String key = extractKeyFromUrl(fileUrl);
                cosClient.deleteObject(cosProperties.getBucket(), key);
                log.info("Successfully deleted COS object: {}", key);
                return true;
            } catch (Exception e) {
                log.error("Failed to delete COS object", e);
                return false;
            }
        } else {
            try {
                // 从 URL 中提取本地文件路径
                String filePath = fileUrl.replace(baseUrl, uploadPath);
                File file = new File(filePath);
                if (file.exists()) {
                    boolean deleted = file.delete();
                    log.info("Deleted local file: {} (success: {})", filePath, deleted);
                    return deleted;
                }
                return false;
            } catch (Exception e) {
                log.error("Failed to delete local file", e);
                return false;
            }
        }
    }

    /**
     * 上传文件到腾讯云 COS
     */
    private String uploadToCOS(MultipartFile file, String directory) throws Exception {
        try {
            // 生成文件名
            String fileName = generateFileName(file.getOriginalFilename());
            String key = directory + fileName;

            // 将上传的文件转换为临时 File
            File tempFile = File.createTempFile("upload_", null);
            file.transferTo(tempFile);

            // 上传到 COS
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    cosProperties.getBucket(),
                    key,
                    tempFile
            );
            PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);

            // 删除临时文件
            tempFile.delete();

            // 构建文件 URL
            String fileUrl = cosProperties.getCdnUrl() + "/" + key;
            log.info("Successfully uploaded to COS: {}", fileUrl);

            return fileUrl;
        } catch (Exception e) {
            log.error("Failed to upload to COS", e);
            throw new Exception("COS 上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传文件到本地
     */
    private String uploadToLocal(MultipartFile file, String directory) throws Exception {
        try {
            // 确保目录存在
            File dir = new File(uploadPath + directory);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 生成文件名
            String fileName = generateFileName(file.getOriginalFilename());
            String filePath = uploadPath + directory + fileName;

            // 保存文件
            file.transferTo(new File(filePath));

            // 构建文件 URL
            String fileUrl = baseUrl + directory + fileName;
            log.info("Successfully uploaded to local: {}", fileUrl);

            return fileUrl;
        } catch (IOException e) {
            log.error("Failed to upload to local", e);
            throw new Exception("本地上传失败: " + e.getMessage());
        }
    }

    /**
     * 生成随机文件名
     */
    private String generateFileName(String originalFileName) {
        // 获取文件扩展名
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        // 生成新文件名：时间戳_UUID.扩展名
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        return timestamp + "_" + uuid + extension;
    }

    /**
     * 从 URL 中提取对象键
     */
    private String extractKeyFromUrl(String fileUrl) {
        // 从 CDN URL 中提取键
        if (fileUrl.contains(cosProperties.getCdnUrl())) {
            return fileUrl.replace(cosProperties.getCdnUrl() + "/", "");
        }
        // 从本地 URL 中提取键
        if (fileUrl.contains(baseUrl)) {
            return fileUrl.replace(baseUrl, "");
        }
        return fileUrl;
    }
}

