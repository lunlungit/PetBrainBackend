package com.aipetbrain.service;

import com.aipetbrain.dto.PetDTO;
import com.aipetbrain.entity.Pet;
import com.aipetbrain.entity.UserPet;
import com.aipetbrain.entity.PetShareLog;
import java.util.List;

public interface PetService {
    List<PetDTO> getPetList(Long userId);
    PetDTO getPetDetail(Long petId);
    Pet getPetEntity(Long petId);
    PetDTO addPet(PetDTO petDTO);
    PetDTO updatePet(PetDTO petDTO);
    void deletePet(Long petId);
    void deletePet(Long petId, String requestId);

    // ==================== 多用户相关方法 ====================

    /**
     * 获取用户有权访问的所有宠物（包括拥有的和共享的）
     * @param userId 用户ID
     * @return 宠物列表
     */
    List<PetDTO> getUserAccessiblePets(Long userId);

    /**
     * 分享宠物给其他用户
     * @param petId 宠物ID
     * @param toUserId 被分享者用户ID
     * @param permissions 权限列表，如["view","edit"]
     * @param currentUserId 当前用户ID（必须是拥有者）
     */
    void sharePetWithUser(Long petId, Long toUserId, List<String> permissions, Long currentUserId);

    /**
     * 撤销用户的宠物访问权
     * @param petId 宠物ID
     * @param userId 被撤销权限的用户ID
     * @param currentUserId 当前用户ID（必须是拥有者）
     */
    void revokePetAccess(Long petId, Long userId, Long currentUserId);

    /**
     * 更新用户的权限
     * @param petId 宠物ID
     * @param userId 用户ID
     * @param permissions 新权限列表
     * @param currentUserId 当前用户ID（必须是拥有者）
     */
    void updateUserPermissions(Long petId, Long userId, List<String> permissions, Long currentUserId);

    /**
     * 获取宠物的共享用户列表
     * @param petId 宠物ID
     * @param currentUserId 当前用户ID（必须是拥有者）
     * @return UserPet列表
     */
    List<UserPet> getPetSharedUsers(Long petId, Long currentUserId);

    /**
     * 获取宠物的权限分享历史
     * @param petId 宠物ID
     * @param currentUserId 当前用户ID（必须是拥有者）
     * @return PetShareLog列表
     */
    List<PetShareLog> getPetShareLogs(Long petId, Long currentUserId);

    /**
     * 删除宠物（只有拥有者可以删除）
     * @param petId 宠物ID
     * @param currentUserId 当前用户ID
     */
    void deletePetByOwner(Long petId, Long currentUserId);
}

