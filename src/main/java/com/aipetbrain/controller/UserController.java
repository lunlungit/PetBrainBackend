package com.aipetbrain.controller;

import com.aipetbrain.common.Result;
import com.aipetbrain.dto.LoginDTO;
import com.aipetbrain.dto.LoginResponseDTO;
import com.aipetbrain.entity.User;
import com.aipetbrain.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@CrossOrigin
public class UserController {

    private final UserService userService;

    @PostMapping("/login")
    public Result<LoginResponseDTO> login(@RequestBody LoginDTO loginDTO) {
        User user = userService.login(loginDTO);
        LoginResponseDTO response = new LoginResponseDTO();

        if (user == null) {
            // 首次登录，需要创建用户
            response.setFirstLogin(true);
            response.setMessage("首次登录，请创建账户");
            response.setUser(null);
        } else {
            // 已有账户，直接登录
            response.setFirstLogin(false);
            response.setMessage("登录成功");
            response.setUser(user);
        }

        return Result.success(response);
    }

    /**
     * 创建用户（首次登录时调用）
     */
    @PostMapping("/register")
    public Result<User> register(@RequestBody LoginDTO loginDTO) {
        User user = userService.createUser(loginDTO);
        return Result.success(user);
    }

    @GetMapping("/info/{userId}")
    public Result<User> getUserInfo(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        return Result.success(user);
    }

    @PutMapping("/update")
    public Result<User> updateUser(@RequestBody User user) {
        User updated = userService.updateUser(user);
        return Result.success(updated);
    }

    @PostMapping("/phone")
    public Result<User> updatePhone(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        Long userId = Long.valueOf(request.get("userId"));
        // TODO: 实际应该调用微信API解析手机号
        User updated = userService.updatePhone(userId, "13800138000");
        return Result.success(updated);
    }

    @GetMapping("/stats/{userId}")
    public Result<Map<String, Object>> getUserStats(@PathVariable Long userId) {
        Map<String, Object> stats = userService.getUserStats(userId);
        return Result.success(stats);
    }
}

