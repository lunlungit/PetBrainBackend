package com.aipetbrain.mapper;

import com.aipetbrain.entity.UserPet;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 用户-宠物关联表 Mapper 接口
 */
@Mapper
public interface UserPetMapper extends BaseMapper<UserPet> {

    /**
     * 查询用户拥有的所有宠物ID
     *
     * @param userId 用户ID
     * @return 宠物ID列表
     */
    List<Long> selectPetIdsByUserId(Long userId);

    /**
     * 查询宠物的所有可访问用户
     *
     * @param petId 宠物ID
     * @return UserPet 列表
     */
    List<UserPet> selectUsersByPetId(Long petId);
}

