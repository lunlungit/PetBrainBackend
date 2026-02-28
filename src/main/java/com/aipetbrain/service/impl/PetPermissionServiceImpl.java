package com.aipetbrain.service.impl;

import com.aipetbrain.entity.UserPet;
import com.aipetbrain.mapper.UserPetMapper;
import com.aipetbrain.service.PetPermissionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * 宠物权限管理服务实现类
 */
@Service
public class PetPermissionServiceImpl implements PetPermissionService {

    @Resource
    private UserPetMapper userPetMapper;

    @Override
    public boolean isOwner(Long userId, Long petId) {
        UserPet userPet = getUserPet(userId, petId);
        return userPet != null && userPet.isOwner();
    }

    @Override
    public boolean hasAccessToPet(Long userId, Long petId) {
        UserPet userPet = getUserPet(userId, petId);
        return userPet != null && userPet.canView();
    }

    @Override
    public boolean hasPermission(Long userId, Long petId, String permission) {
        UserPet userPet = getUserPet(userId, petId);
        return userPet != null && userPet.hasPermission(permission);
    }

    @Override
    public boolean canView(Long userId, Long petId) {
        return hasPermission(userId, petId, UserPet.PERMISSION_VIEW);
    }

    @Override
    public boolean canEdit(Long userId, Long petId) {
        return hasPermission(userId, petId, UserPet.PERMISSION_EDIT);
    }

    @Override
    public boolean canManage(Long userId, Long petId) {
        return hasPermission(userId, petId, UserPet.PERMISSION_MANAGE);
    }

    @Override
    public boolean canDelete(Long userId, Long petId) {
        // 只有拥有者可以删除宠物
        return isOwner(userId, petId);
    }

    @Override
    public UserPet getUserPet(Long userId, Long petId) {
        LambdaQueryWrapper<UserPet> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserPet::getUserId, userId)
                .eq(UserPet::getPetId, petId)
                .eq(UserPet::getDeleted, 0);

        return userPetMapper.selectOne(queryWrapper);
    }
}

