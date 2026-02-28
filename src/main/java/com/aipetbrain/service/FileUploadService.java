package com.aipetbrain.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传服务接口
 */
public interface FileUploadService {

    /**
     * 上传图片到对象存储或本地
     * @param file 图片文件
     * @param directory 目录（可选）
     * @return 文件URL
     */
    String uploadImage(MultipartFile file, String directory) throws Exception;

    /**
     * 上传头像
     * @param file 头像文件
     * @param userId 用户ID
     * @return 头像URL
     */
    String uploadAvatar(MultipartFile file, Long userId) throws Exception;

    /**
     * 上传宠物照片
     * @param file 照片文件
     * @param petId 宠物ID
     * @return 照片URL
     */
    String uploadPetPhoto(MultipartFile file, Long petId) throws Exception;

    /**
     * 上传失物启事图片
     * @param file 图片文件
     * @param lostPetId 失物启事ID
     * @return 图片URL
     */
    String uploadLostPetImage(MultipartFile file, Long lostPetId) throws Exception;

    /**
     * 删除文件
     * @param fileUrl 文件URL
     * @return 是否删除成功
     */
    boolean deleteFile(String fileUrl) throws Exception;
}

