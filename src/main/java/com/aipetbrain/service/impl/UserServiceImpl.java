package com.aipetbrain.service.impl;

import com.aipetbrain.dto.LoginDTO;
import com.aipetbrain.entity.User;
import com.aipetbrain.entity.Achievement;
import com.aipetbrain.entity.Territory;
import com.aipetbrain.mapper.UserMapper;
import com.aipetbrain.mapper.AchievementMapper;
import com.aipetbrain.mapper.TerritoryMapper;
import com.aipetbrain.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final AchievementMapper achievementMapper;
    private final TerritoryMapper territoryMapper;

    @Override
    public User login(LoginDTO loginDTO) {
        // 这里简化处理，实际需要调用微信小程序登录接口获取openid
        String openid = "mock_openid_" + System.currentTimeMillis();

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenid, openid);
        User user = userMapper.selectOne(wrapper);

        // 首次登录：返回 null，前端需要提示用户创建账户
        // 如果用户存在，直接返回用户信息
        return user;
    }

    /**
     * 创建用户（首次登录时调用）
     */
    public User createUser(LoginDTO loginDTO) {
        // 这里简化处理，实际需要调用微信小程序登录接口获取openid
        String openid = "mock_openid_" + System.currentTimeMillis();

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenid, openid);
        User existingUser = userMapper.selectOne(wrapper);

        if (existingUser != null) {
            return existingUser;
        }

        User user = new User();
        user.setOpenid(openid);
        user.setNickname("宠物主人");
        user.setAvatar("https://cdn-icons-png.flaticon.com/512/3048/3048122.png");
        user.setGender(0);
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);

        return user;
    }

    @Override
    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public User updateUser(User user) {
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return user;
    }

    @Override
    public User updatePhone(Long userId, String phone) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setPhone(phone);
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
        }
        return user;
    }

    @Override
    public Map<String, Object> getUserStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();

        // 获取用户信息（用于计算陪伴天数）
        User user = userMapper.selectById(userId);
        if (user != null && user.getCreateTime() != null) {
            // 计算陪伴天数
            long days = ChronoUnit.DAYS.between(user.getCreateTime(), LocalDateTime.now());
            stats.put("companionDays", (int) days + 1); // 至少1天
        } else {
            stats.put("companionDays", 1);
        }

        // 获取勋章数量
        LambdaQueryWrapper<Achievement> achievementWrapper = new LambdaQueryWrapper<>();
        achievementWrapper.eq(Achievement::getUserId, userId);
        Long achievementCount = achievementMapper.selectCount(achievementWrapper);
        stats.put("achievementCount", achievementCount.intValue());

        // 获取领地数量
        LambdaQueryWrapper<Territory> territoryWrapper = new LambdaQueryWrapper<>();
        territoryWrapper.eq(Territory::getUserId, userId);
        Long territoryCount = territoryMapper.selectCount(territoryWrapper);
        stats.put("territoryCount", territoryCount.intValue());

        return stats;
    }
}

