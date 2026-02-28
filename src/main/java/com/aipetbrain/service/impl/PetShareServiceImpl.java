package com.aipetbrain.service.impl;

import com.aipetbrain.dto.PetShareCodeDTO;
import com.aipetbrain.entity.PetShareCode;
import com.aipetbrain.entity.UserPet;
import com.aipetbrain.mapper.PetShareCodeMapper;
import com.aipetbrain.mapper.UserPetMapper;
import com.aipetbrain.service.PetShareService;
import com.aipetbrain.service.PetPermissionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 宠物分享服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PetShareServiceImpl implements PetShareService {

    private final PetShareCodeMapper petShareCodeMapper;
    private final UserPetMapper userPetMapper;
    private final PetPermissionService petPermissionService;

    @Value("${app.share-base-url:http://localhost:8080/api}")
    private String shareBaseUrl;

    /**
     * 生成分享码
     */
    @Override
    public PetShareCodeDTO generateShareCode(Long petId, Long userId, String permission) {
        log.info("🔗 [generateShareCode] 生成分享码: petId={}, userId={}, permission={}", petId, userId, permission);

        // 检查用户是否是宠物所有者
        if (!petPermissionService.isOwner(userId, petId)) {
            log.warn("❌ [generateShareCode] 用户{}不是宠物{}的所有者", userId, petId);
            throw new RuntimeException("只有宠物所有者才能生成分享码");
        }

        // 查询是否已存在有效的分享码（相同权限）
        PetShareCode existing = petShareCodeMapper.selectLatestByPetAndUser(petId, userId);
        if (existing != null
            && permission.equals(existing.getPermission())  // 权限必须相同
            && (existing.getExpireTime() == null || existing.getExpireTime().isAfter(LocalDateTime.now()))) {
            log.info("📌 [generateShareCode] 使用已存在的分享码: {}", existing.getShareCode());
            PetShareCodeDTO dto = new PetShareCodeDTO();
            BeanUtils.copyProperties(existing, dto);
            return dto;
        }

        // 生成分享码
        String shareCode = "PET_" + petId + "_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String shareUrl = shareBaseUrl + "/share/" + shareCode;

        PetShareCode petShareCode = new PetShareCode();
        petShareCode.setPetId(petId);
        petShareCode.setUserId(userId);
        petShareCode.setShareCode(shareCode);
        petShareCode.setShareUrl(shareUrl);
        petShareCode.setPermission(permission);
        petShareCode.setActive(1);
        petShareCode.setUsedCount(0);
        petShareCode.setCreateTime(LocalDateTime.now());
        petShareCode.setUpdateTime(LocalDateTime.now());
        petShareCode.setDeleted(0);

        petShareCodeMapper.insert(petShareCode);

        log.info("✅ [generateShareCode] 分享码生成成功: {}", shareCode);

        PetShareCodeDTO dto = new PetShareCodeDTO();
        BeanUtils.copyProperties(petShareCode, dto);
        return dto;
    }

    /**
     * 获取分享码
     */
    @Override
    public PetShareCodeDTO getShareCode(Long petId, Long userId) {
        PetShareCode petShareCode = petShareCodeMapper.selectLatestByPetAndUser(petId, userId);

        if (petShareCode == null) {
            log.info("📌 [getShareCode] 分享码不存在，自动生成新的");
            return generateShareCode(petId, userId, "READ");
        }

        // 检查是否过期
        if (petShareCode.getExpireTime() != null && petShareCode.getExpireTime().isBefore(LocalDateTime.now())) {
            log.info("⏰ [getShareCode] 分享码已过期，生成新的");
            return generateShareCode(petId, userId, petShareCode.getPermission());
        }

        PetShareCodeDTO dto = new PetShareCodeDTO();
        BeanUtils.copyProperties(petShareCode, dto);
        return dto;
    }

    /**
     * 通过分享码绑定宠物
     */
    @Override
    public boolean bindPetByShareCode(String shareCode, Long targetUserId) {
        log.info("🔐 [bindPetByShareCode] 通过分享码绑定: shareCode={}, targetUserId={}", shareCode, targetUserId);

        // 查询分享码
        PetShareCode petShareCode = petShareCodeMapper.selectByShareCode(shareCode);

        if (petShareCode == null) {
            log.warn("❌ [bindPetByShareCode] 分享码不存在或已失效: {}", shareCode);
            return false;
        }

        // 检查是否过期
        if (petShareCode.getExpireTime() != null && petShareCode.getExpireTime().isBefore(LocalDateTime.now())) {
            log.warn("⏰ [bindPetByShareCode] 分享码已过期");
            return false;
        }

        // 检查剩余使用次数
        if (petShareCode.getRemainingUses() != null) {
            if (petShareCode.getRemainingUses() <= 0) {
                log.warn("📊 [bindPetByShareCode] 分享码使用次数已用尽");
                return false;
            }
        }

        Long petId = petShareCode.getPetId();

        // 检查用户是否已经绑定过此宠物
        LambdaQueryWrapper<UserPet> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(UserPet::getUserId, targetUserId).eq(UserPet::getPetId, petId);
        if (userPetMapper.selectOne(checkWrapper) != null) {
            log.warn("⚠️ [bindPetByShareCode] 用户已经绑定过此宠物: userId={}, petId={}", targetUserId, petId);
            return false;
        }

        // 创建用户-宠物关联
        UserPet userPet = new UserPet();
        userPet.setUserId(targetUserId);
        userPet.setPetId(petId);
        userPet.setRole(UserPet.ROLE_SHARED_USER);

        // 根据分享码中的权限设置权限
        String permission = petShareCode.getPermission();
        if ("WRITE".equals(permission)) {
            userPet.setPermission("[\"view\",\"edit\"]");
        } else {
            userPet.setPermission("[\"view\"]");
        }

        userPetMapper.insert(userPet);

        // 更新分享码的使用统计
        petShareCode.setUsedCount((petShareCode.getUsedCount() == null ? 0 : petShareCode.getUsedCount()) + 1);
        if (petShareCode.getRemainingUses() != null) {
            petShareCode.setRemainingUses(petShareCode.getRemainingUses() - 1);
        }
        petShareCodeMapper.updateById(petShareCode);

        log.info("✅ [bindPetByShareCode] 宠物绑定成功: petId={}, targetUserId={}", petId, targetUserId);
        return true;
    }

    /**
     * 根据分享码查询
     */
    @Override
    public PetShareCode getByShareCode(String shareCode) {
        return petShareCodeMapper.selectByShareCode(shareCode);
    }

    /**
     * 撤销分享码
     */
    @Override
    public boolean revokeShareCode(Long shareCodeId, Long userId) {
        log.info("🔄 [revokeShareCode] 撤销分享码: shareCodeId={}, userId={}", shareCodeId, userId);

        PetShareCode petShareCode = petShareCodeMapper.selectById(shareCodeId);

        if (petShareCode == null) {
            log.warn("❌ [revokeShareCode] 分享码不存在");
            return false;
        }

        // 检查所有权
        if (!petShareCode.getUserId().equals(userId)) {
            log.warn("❌ [revokeShareCode] 用户无权撤销此分享码");
            return false;
        }

        petShareCode.setActive(0);
        petShareCode.setUpdateTime(LocalDateTime.now());
        petShareCodeMapper.updateById(petShareCode);

        log.info("✅ [revokeShareCode] 分享码已撤销");
        return true;
    }
}

