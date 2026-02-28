package com.aipetbrain.service;

import com.aipetbrain.dto.LoginDTO;
import com.aipetbrain.entity.User;

import java.util.Map;

public interface UserService {
    /**
     * 登录（首次登录返回 null，已有账户返回用户信息）
     */
    User login(LoginDTO loginDTO);

    /**
     * 创建用户（首次登录时调用）
     */
    User createUser(LoginDTO loginDTO);

    User getUserById(Long userId);
    User updateUser(User user);

    User updatePhone(Long userId, String phone);

    /**
     * 获取用户统计数据
     */
    Map<String, Object> getUserStats(Long userId);
}

