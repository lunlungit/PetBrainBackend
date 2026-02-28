package com.aipetbrain.mapper;

import com.aipetbrain.entity.PetShareCode;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

/**
 * PetShareCode Mapper
 */
public interface PetShareCodeMapper extends BaseMapper<PetShareCode> {

    /**
     * 根据分享码查询
     */
    @Select("SELECT * FROM pet_share_code WHERE share_code = #{shareCode} AND deleted = 0 AND active = 1")
    PetShareCode selectByShareCode(String shareCode);

    /**
     * 根据宠物ID和用户ID查询最新的分享码
     */
    @Select("SELECT * FROM pet_share_code WHERE pet_id = #{petId} AND user_id = #{userId} AND deleted = 0 AND active = 1 ORDER BY create_time DESC LIMIT 1")
    PetShareCode selectLatestByPetAndUser(Long petId, Long userId);
}

