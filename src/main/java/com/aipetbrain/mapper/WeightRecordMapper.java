package com.aipetbrain.mapper;

import com.aipetbrain.entity.WeightRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 体重记录 Mapper 接口
 */
@Mapper
public interface WeightRecordMapper extends BaseMapper<WeightRecord> {

    /**
     * 查询指定宠物指定年月的体重记录
     *
     * @param petId 宠物ID
     * @param year 年份
     * @param month 月份
     * @return 体重记录列表
     */
    @Select("SELECT * FROM weight_record WHERE pet_id = #{petId} AND YEAR(record_date) = #{year} AND MONTH(record_date) = #{month} ORDER BY record_date DESC")
    List<WeightRecord> selectByPetIdAndYearMonth(Long petId, int year, int month);

    /**
     * 查询指定宠物指定年份的所有体重记录
     *
     * @param petId 宠物ID
     * @param year 年份
     * @return 体重记录列表
     */
    @Select("SELECT * FROM weight_record WHERE pet_id = #{petId} AND YEAR(record_date) = #{year} ORDER BY record_date DESC")
    List<WeightRecord> selectByPetIdAndYear(Long petId, int year);

    /**
     * 查询指定宠物最近的体重记录（最近30条）
     *
     * @param petId 宠物ID
     * @return 体重记录列表
     */
    @Select("SELECT * FROM weight_record WHERE pet_id = #{petId} ORDER BY record_date DESC LIMIT 30")
    List<WeightRecord> selectRecentByPetId(Long petId);

    /**
     * 删除指定宠物指定年份的所有体重记录
     *
     * @param petId 宠物ID
     * @param year 年份
     * @return 删除记录数
     */
    @Delete("UPDATE weight_record SET deleted = 1 WHERE pet_id = #{petId} AND YEAR(record_date) = #{year}")
    int deleteByPetIdAndYear(Long petId, int year);
}

