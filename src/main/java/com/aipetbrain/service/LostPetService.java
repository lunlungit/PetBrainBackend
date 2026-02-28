package com.aipetbrain.service;

import com.aipetbrain.dto.LostPetDTO;
import java.math.BigDecimal;
import java.util.List;

public interface LostPetService {
    /**
     * 获取附近的丢失宠物（按距离排序，支持分页）
     * @param latitude 纬度
     * @param longitude 经度
     * @param radius 搜索半径(米)
     * @param limit 每页返回数量
     * @param page 分页页码（从1开始）
     * @return 丢失宠物列表
     */
    List<LostPetDTO> getNearbyLostPets(BigDecimal latitude, BigDecimal longitude, Double radius, Integer limit, Integer page);

    /**
     * 发布走失宠物
     * @param lostPetDTO 走失宠物信息
     * @return 走失宠物信息
     */
    LostPetDTO publishLostPet(LostPetDTO lostPetDTO);

    /**
     * 上报发现的流浪宠物
     * @param lostPetDTO 流浪宠物信息
     * @return 流浪宠物信息
     */
    LostPetDTO reportStrayPet(LostPetDTO lostPetDTO);

    /**
     * 标记走失/流浪宠物已找到
     * @param lostPetId 走失宠物ID
     */
    void markPetFound(Long lostPetId);

    /**
     * 获取用户发布/上报的走失宠物列表
     * @param userId 用户ID
     * @return 走失宠物列表
     */
    List<LostPetDTO> getUserLostPets(Long userId);

    /**
     * 获取走失宠物详情
     * @param lostPetId 走失宠物ID
     * @return 走失宠物信息
     */
    LostPetDTO getLostPetDetail(Long lostPetId);

    /**
     * 更新走失宠物信息
     * @param lostPetDTO 走失宠物信息
     * @return 更新后的走失宠物信息
     */
    LostPetDTO updateLostPet(LostPetDTO lostPetDTO);

    /**
     * 删除走失宠物
     * @param lostPetId 走失宠物ID
     */
    void deleteLostPet(Long lostPetId);
}

