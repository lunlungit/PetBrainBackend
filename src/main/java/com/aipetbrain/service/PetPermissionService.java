package com.aipetbrain.service;

import com.aipetbrain.entity.UserPet;

/**
 * 宠物权限管理服务接口
 * 用于检查用户是否有权访问、编辑、管理宠物
 */
public interface PetPermissionService {

    /**
     * 检查用户是否是宠物的拥有者
     *
     * @param userId 用户ID
     * @param petId 宠物ID
     * @return true 如果用户是拥有者
     */
    boolean isOwner(Long userId, Long petId);

    /**
     * 检查用户是否有权访问宠物
     *
     * @param userId 用户ID
     * @param petId 宠物ID
     * @return true 如果用户有权访问
     */
    boolean hasAccessToPet(Long userId, Long petId);

    /**
     * 检查用户是否有特定权限
     *
     * @param userId 用户ID
     * @param petId 宠物ID
     * @param permission 权限类型 (view, edit, manage)
     * @return true 如果用户有该权限
     */
    boolean hasPermission(Long userId, Long petId, String permission);

    /**
     * 检查用户是否可以查看宠物
     *
     * @param userId 用户ID
     * @param petId 宠物ID
     * @return true 如果用户可以查看
     */
    boolean canView(Long userId, Long petId);

    /**
     * 检查用户是否可以编辑宠物
     *
     * @param userId 用户ID
     * @param petId 宠物ID
     * @return true 如果用户可以编辑
     */
    boolean canEdit(Long userId, Long petId);

    /**
     * 检查用户是否可以管理宠物权限
     *
     * @param userId 用户ID
     * @param petId 宠物ID
     * @return true 如果用户可以管理权限
     */
    boolean canManage(Long userId, Long petId);

    /**
     * 检查用户是否可以删除宠物（只有拥有者可以）
     *
     * @param userId 用户ID
     * @param petId 宠物ID
     * @return true 如果用户可以删除
     */
    boolean canDelete(Long userId, Long petId);

    /**
     * 获取用户与宠物的关联关系
     *
     * @param userId 用户ID
     * @param petId 宠物ID
     * @return UserPet 对象，如果不存在返回 null
     */
    UserPet getUserPet(Long userId, Long petId);
}

