package com.aipetbrain.dto;

import com.aipetbrain.entity.User;
import lombok.Data;

/**
 * 登录响应 DTO
 * 返回用户信息和首次登录标识
 */
@Data
public class LoginResponseDTO {
    private User user;
    private boolean firstLogin; // true: 首次登录，需要创建用户; false: 已有账户，直接登录
    private String message;
}

