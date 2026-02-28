package com.aipetbrain.service;

import com.aipetbrain.dto.PetShareCodeDTO;
import com.aipetbrain.entity.PetShareCode;

/**
 * 宠物分享服务接口
 */
public interface PetShareService {

    /**
     * 生成宠物分享码
     * @param petId 宠物ID
     * @param userId 用户ID
     * @param permission 权限 READ:查看 WRITE:编辑
     * @return 分享码DTO
     */
    PetShareCodeDTO generateShareCode(Long petId, Long userId, String permission);

    /**
     * 获取宠物分享码
     * @param petId 宠物ID
     * @param userId 用户ID
     * @return 分享码DTO
     */
    PetShareCodeDTO getShareCode(Long petId, Long userId);

    /**
     * 通过分享码绑定宠物
     * @param shareCode 分享码
     * @param targetUserId 目标用户ID
     * @return 是否成功
     */
    boolean bindPetByShareCode(String shareCode, Long targetUserId);

    /**
     * 根据分享码查询
     * @param shareCode 分享码
     * @return PetShareCode实体
     */
    PetShareCode getByShareCode(String shareCode);

    /**
     * 撤销分享码
     * @param shareCodeId 分享码ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean revokeShareCode(Long shareCodeId, Long userId);
}

