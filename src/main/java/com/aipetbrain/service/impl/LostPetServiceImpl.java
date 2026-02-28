package com.aipetbrain.service.impl;

import com.aipetbrain.dto.LostPetDTO;
import com.aipetbrain.entity.LostPet;
import com.aipetbrain.mapper.LostPetMapper;
import com.aipetbrain.service.LostPetService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.alibaba.fastjson.JSON;

@Slf4j
@Service
@RequiredArgsConstructor
public class LostPetServiceImpl implements LostPetService {

    private final LostPetMapper lostPetMapper;

    @Override
    public List<LostPetDTO> getNearbyLostPets(BigDecimal latitude, BigDecimal longitude, Double radius, Integer limit, Integer page) {
        // 参数验证
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        if (page == null || page < 1) {
            page = 1;
        }

        // 计算分页偏移量：offset = (page - 1) * limit
        int offset = (page - 1) * limit;

        // 查询走失中的宠物（status=0）
        LambdaQueryWrapper<LostPet> wrapper1 = new LambdaQueryWrapper<>();
        wrapper1.eq(LostPet::getStatus, 0);  // 走失中/流浪中
        wrapper1.eq(LostPet::getDeleted, 0);
        wrapper1.orderByDesc(LostPet::getLostTime);  // 按走失时间倒序

        // 查询已找到的宠物（status=1）
        LambdaQueryWrapper<LostPet> wrapper2 = new LambdaQueryWrapper<>();
        wrapper2.eq(LostPet::getStatus, 1);  // 已找到
        wrapper2.eq(LostPet::getDeleted, 0);
        wrapper2.orderByDesc(LostPet::getFoundTime);  // 按找到时间倒序

        // 先查询所有符合条件的宠物
        List<LostPet> allLostPets = lostPetMapper.selectList(wrapper1);
        List<LostPet> allFoundPets = lostPetMapper.selectList(wrapper2);

        // 合并列表：走失中的放在前面，已找到的放在后面
        List<LostPet> allPets = new ArrayList<>();
        allPets.addAll(allLostPets);
        allPets.addAll(allFoundPets);

        // 分页处理：根据 page 和 limit 返回对应的数据
        int endIndex = offset + limit;
        List<LostPet> pagedPets = new ArrayList<>();

        if (offset < allPets.size()) {
            if (endIndex > allPets.size()) {
                endIndex = allPets.size();
            }
            pagedPets = allPets.subList(offset, endIndex);
        }

        System.out.println("🐾 [getNearbyLostPets] 走失宠物总数: " + allLostPets.size() + ", 已找到宠物总数: " + allFoundPets.size() + ", 分页参数 - page=" + page + ", limit=" + limit);

        // 转换为 DTO
        return pagedPets.stream()
            .map(lostPet -> {
                LostPetDTO dto = new LostPetDTO();
                BeanUtils.copyProperties(lostPet, dto);
                // 距离由前端根据用户位置计算
                dto.setDistance(null);
                return dto;
            })
            .collect(Collectors.toList());
    }

    @Override
    public LostPetDTO publishLostPet(LostPetDTO lostPetDTO) {
        log.info("📢 [publishLostPet] 开始发布走失宠物，DTO={}", lostPetDTO);

        // 验证必需字段
        if (lostPetDTO.getCreatorId() == null) {
            log.error("❌ [publishLostPet] 发布者ID不能为空");
            throw new RuntimeException("发布者ID不能为空");
        }
        if (lostPetDTO.getPetType() == null) {
            log.error("❌ [publishLostPet] 宠物类型不能为空");
            throw new RuntimeException("宠物类型不能为空");
        }

        // 检查该宠物是否已有未找到的走失记录
        // 同一宠物只保留一条走失记录，多次发布会更新该记录而不是创建新记录
        LambdaQueryWrapper<LostPet> existingWrapper = new LambdaQueryWrapper<>();
        existingWrapper.eq(LostPet::getPetId, lostPetDTO.getPetId());  // 宠物ID
        existingWrapper.eq(LostPet::getCreatorId, lostPetDTO.getCreatorId());  // 同一发布者
        existingWrapper.eq(LostPet::getIsStray, 0);  // 走失宠物
        existingWrapper.eq(LostPet::getStatus, 0);   // 未找到的状态
        existingWrapper.eq(LostPet::getDeleted, 0);
        List<LostPet> existingRecords = lostPetMapper.selectList(existingWrapper);

        LostPet lostPet = new LostPet();
        BeanUtils.copyProperties(lostPetDTO, lostPet);

        // 处理多张照片：如果提供了 imageList，则转换为 JSON 字符串存储，并将第一张设为 avatar
        if (lostPetDTO.getImageList() != null && !lostPetDTO.getImageList().isEmpty()) {
            lostPet.setImages(JSON.toJSONString(lostPetDTO.getImageList()));
            // 将第一张图设为 avatar，用于列表页展示
            lostPet.setAvatar(lostPetDTO.getImageList().get(0));
        }

        // 设置默认值和必需值
        lostPet.setIsStray(0);  // 用户发布的走失宠物
        lostPet.setStatus(0);   // 走失中
        if (lostPet.getLostTime() == null) {
            lostPet.setLostTime(LocalDateTime.now());
        }
        if (lostPet.getDeleted() == null) {
            lostPet.setDeleted(0);
        }

        LostPetDTO result = new LostPetDTO();

        if (existingRecords.isEmpty()) {
            // 没有现存记录，创建新记录
            lostPet.setCreateTime(LocalDateTime.now());
            log.info("📝 [publishLostPet] 准备插入数据库，LostPet={}", lostPet);
            int insertResult = lostPetMapper.insert(lostPet);
            log.info("✅ [publishLostPet] 新建记录成功，影响行数={}, 生成ID={}", insertResult, lostPet.getId());
            BeanUtils.copyProperties(lostPet, result);
        } else {
            // 存在现存记录，更新该记录
            LostPet existingRecord = existingRecords.get(0);
            log.info("📝 [publishLostPet] 发现已有走失记录，ID={}，将其更新", existingRecord.getId());

            lostPet.setId(existingRecord.getId());
            lostPet.setCreateTime(existingRecord.getCreateTime());  // 保留原创建时间
            lostPet.setUpdateTime(LocalDateTime.now());

            int updateResult = lostPetMapper.updateById(lostPet);
            log.info("✅ [publishLostPet] 更新记录成功，影响行数={}, ID={}", updateResult, lostPet.getId());
            BeanUtils.copyProperties(lostPet, result);
        }

        log.info("✅ [publishLostPet] 发布走失宠物成功，result={}", result);
        return result;
    }

    @Override
    public LostPetDTO reportStrayPet(LostPetDTO lostPetDTO) {
        log.info("📢 [reportStrayPet] 开始上报流浪宠物，DTO={}", lostPetDTO);

        // 验证必需字段
        if (lostPetDTO.getCreatorId() == null) {
            log.error("❌ [reportStrayPet] 上报者ID不能为空");
            throw new RuntimeException("上报者ID不能为空");
        }
        if (lostPetDTO.getPetType() == null) {
            log.error("❌ [reportStrayPet] 宠物类型不能为空");
            throw new RuntimeException("宠物类型不能为空");
        }

        LostPet lostPet = new LostPet();
        BeanUtils.copyProperties(lostPetDTO, lostPet);

        // 处理多张照片：如果提供了 imageList，则转换为 JSON 字符串存储，并将第一张设为 avatar
        if (lostPetDTO.getImageList() != null && !lostPetDTO.getImageList().isEmpty()) {
            lostPet.setImages(JSON.toJSONString(lostPetDTO.getImageList()));
            // 将第一张图设为 avatar，用于列表页展示
            lostPet.setAvatar(lostPetDTO.getImageList().get(0));
        }

        // 设置默认值和必需值
        lostPet.setIsStray(1);  // 用户发现的流浪宠物
        lostPet.setStatus(0);   // 流浪中
        if (lostPet.getLostTime() == null) {
            lostPet.setLostTime(LocalDateTime.now());
        }
        lostPet.setCreateTime(LocalDateTime.now());
        if (lostPet.getDeleted() == null) {
            lostPet.setDeleted(0);
        }

        log.info("📝 [reportStrayPet] 准备插入数据库，LostPet={}", lostPet);
        int insertResult = lostPetMapper.insert(lostPet);
        log.info("✅ [reportStrayPet] 插入成功，影响行数={}, 生成ID={}", insertResult, lostPet.getId());

        LostPetDTO result = new LostPetDTO();
        BeanUtils.copyProperties(lostPet, result);
        log.info("✅ [reportStrayPet] 上报流浪宠物成功，result={}", result);
        return result;
    }

    @Override
    public void markPetFound(Long lostPetId) {
        LostPet lostPet = lostPetMapper.selectById(lostPetId);
        if (lostPet == null) {
            throw new RuntimeException("走失宠物不存在");
        }

        LostPet update = new LostPet();
        update.setId(lostPetId);
        update.setStatus(1);  // 已找到
        update.setFoundTime(LocalDateTime.now());
        update.setUpdateTime(LocalDateTime.now());

        lostPetMapper.updateById(update);
    }

    @Override
    public List<LostPetDTO> getUserLostPets(Long userId) {
        LambdaQueryWrapper<LostPet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LostPet::getCreatorId, userId);
        wrapper.eq(LostPet::getDeleted, 0);
        wrapper.orderByDesc(LostPet::getCreateTime);

        List<LostPet> lostPets = lostPetMapper.selectList(wrapper);

        return lostPets.stream()
            .map(lostPet -> {
                LostPetDTO dto = new LostPetDTO();
                BeanUtils.copyProperties(lostPet, dto);
                return dto;
            })
            .collect(Collectors.toList());
    }

    @Override
    public LostPetDTO getLostPetDetail(Long lostPetId) {
        LostPet lostPet = lostPetMapper.selectById(lostPetId);
        if (lostPet == null) {
            return null;
        }

        LostPetDTO dto = new LostPetDTO();
        BeanUtils.copyProperties(lostPet, dto);
        return dto;
    }

    @Override
    public LostPetDTO updateLostPet(LostPetDTO lostPetDTO) {
        log.info("📝 [updateLostPet] 开始更新走失宠物，ID={}, DTO={}", lostPetDTO.getId(), lostPetDTO);

        // 验证宠物是否存在
        LostPet existingLostPet = lostPetMapper.selectById(lostPetDTO.getId());
        if (existingLostPet == null) {
            log.error("❌ [updateLostPet] 走失宠物不存在，ID={}", lostPetDTO.getId());
            throw new RuntimeException("走失宠物不存在");
        }

        // 创建更新对象，只更新允许修改的字段
        LostPet lostPet = new LostPet();
        lostPet.setId(lostPetDTO.getId());

        // 可以编辑的字段
        lostPet.setPetName(lostPetDTO.getPetName());
        lostPet.setBreed(lostPetDTO.getBreed());
        lostPet.setColor(lostPetDTO.getColor());
        lostPet.setAvatar(lostPetDTO.getAvatar());

        // 处理多张照片：如果提供了 imageList，则转换为 JSON 字符串存储
        if (lostPetDTO.getImageList() != null && !lostPetDTO.getImageList().isEmpty()) {
            lostPet.setImages(JSON.toJSONString(lostPetDTO.getImageList()));
        } else if (lostPetDTO.getImages() != null) {
            lostPet.setImages(lostPetDTO.getImages());
        }

        lostPet.setDescription(lostPetDTO.getDescription());
        lostPet.setLostLocationName(lostPetDTO.getLostLocationName());
        lostPet.setLostLatitude(lostPetDTO.getLostLatitude());
        lostPet.setLostLongitude(lostPetDTO.getLostLongitude());
        lostPet.setLostTime(lostPetDTO.getLostTime());
        lostPet.setContactName(lostPetDTO.getContactName());
        lostPet.setContactPhone(lostPetDTO.getContactPhone());
        lostPet.setContactWechat(lostPetDTO.getContactWechat());
        lostPet.setUpdateTime(LocalDateTime.now());

        log.info("📝 [updateLostPet] 准备更新数据库，LostPet={}", lostPet);
        int updateResult = lostPetMapper.updateById(lostPet);
        log.info("✅ [updateLostPet] 更新完成，影响行数={}", updateResult);

        // 查询更新后的完整数据
        LostPet updatedLostPet = lostPetMapper.selectById(lostPetDTO.getId());
        LostPetDTO result = new LostPetDTO();
        BeanUtils.copyProperties(updatedLostPet, result);

        log.info("✅ [updateLostPet] 走失宠物更新成功，result={}", result);
        return result;
    }

    @Override
    public void deleteLostPet(Long lostPetId) {
        log.info("🗑️ [deleteLostPet] 开始删除走失宠物，ID={}", lostPetId);

        // 检查宠物是否存在
        LostPet existingLostPet = lostPetMapper.selectById(lostPetId);
        if (existingLostPet == null) {
            log.error("❌ [deleteLostPet] 走失宠物不存在，ID={}", lostPetId);
            throw new RuntimeException("走失宠物不存在");
        }

        // 使用 MyBatis-Plus 的 deleteById 方法进行逻辑删除
        // 这会自动处理 @TableLogic 注解，将 deleted 字段设置为 1
        int deleteResult = lostPetMapper.deleteById(lostPetId);
        log.info("✅ [deleteLostPet] 删除成功，影响行数={}", deleteResult);
    }
}

