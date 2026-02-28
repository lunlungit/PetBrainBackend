package com.aipetbrain.mapper;

import com.aipetbrain.entity.PetShareLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 宠物权限共享记录表 Mapper 接口
 */
@Mapper
public interface PetShareLogMapper extends BaseMapper<PetShareLog> {

    /**
     * 查询宠物的所有分享记录
     *
     * @param petId 宠物ID
     * @return PetShareLog 列表
     */
    List<PetShareLog> selectLogsByPetId(Long petId);

    /**
     * 查询用户对某宠物的所有分享记录
     *
     * @param petId 宠物ID
     * @param fromUserId 分享者ID
     * @return PetShareLog 列表
     */
    List<PetShareLog> selectLogsByPetIdAndUserId(Long petId, Long fromUserId);
}

