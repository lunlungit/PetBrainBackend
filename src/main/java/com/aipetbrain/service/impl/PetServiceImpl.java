package com.aipetbrain.service.impl;

import com.aipetbrain.dto.PetDTO;
import com.aipetbrain.dto.PetWithPermissionDTO;
import com.aipetbrain.entity.Pet;
import com.aipetbrain.entity.UserPet;
import com.aipetbrain.entity.PetShareLog;
import com.aipetbrain.mapper.PetMapper;
import com.aipetbrain.mapper.UserPetMapper;
import com.aipetbrain.mapper.PetShareLogMapper;
import com.aipetbrain.service.PetService;
import com.aipetbrain.service.PetPermissionService;
import com.aipetbrain.common.PermissionDeniedException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PetServiceImpl implements PetService {

    private final PetMapper petMapper;
    private final UserPetMapper userPetMapper;
    private final PetShareLogMapper petShareLogMapper;
    private final PetPermissionService petPermissionService;

    // 幂等性控制：记录已处理的请求ID和结果
    private static final Map<String, Object> processedRequests = new ConcurrentHashMap<>();
    // 用户级别的操作锁，用于并发控制
    private static final Map<Long, ReentrantReadWriteLock> userLocks = new ConcurrentHashMap<>();

    /**
     * 获取用户的操作锁，确保同一用户的操作不会并发执行
     */
    private ReentrantReadWriteLock getUserLock(Long userId) {
        return userLocks.computeIfAbsent(userId, k -> new ReentrantReadWriteLock());
    }

    /**
     * 检查请求是否已处理过（幂等性）
     */
    private boolean isRequestProcessed(String requestId) {
        return requestId != null && processedRequests.containsKey(requestId);
    }

    /**
     * 标记请求为已处理
     */
    private void markRequestProcessed(String requestId) {
        if (requestId != null) {
            processedRequests.put(requestId, true);
            // 30秒后自动清理
            new Thread(() -> {
                try {
                    Thread.sleep(30000);
                    processedRequests.remove(requestId);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

    @Override
    public List<PetDTO> getPetList(Long userId) {
        LambdaQueryWrapper<Pet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Pet::getUserId, userId);
        wrapper.orderByDesc(Pet::getCreateTime);
        List<Pet> pets = petMapper.selectList(wrapper);

        return pets.stream().map(pet -> {
            PetDTO dto = new PetDTO();
            BeanUtils.copyProperties(pet, dto);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public PetDTO getPetDetail(Long petId) {
        Pet pet = petMapper.selectById(petId);
        if (pet == null) {
            return null;
        }
        PetDTO dto = new PetDTO();
        BeanUtils.copyProperties(pet, dto);
        return dto;
    }

    @Override
    public Pet getPetEntity(Long petId) {
        return petMapper.selectById(petId);
    }

    @Override
    public PetDTO addPet(PetDTO petDTO) {
        String requestId = petDTO.getRequestId();

        // 幂等性检查：如果请求已处理过，直接返回成功
        if (isRequestProcessed(requestId)) {
            // 返回一个有效的PetDTO，实际应该从缓存中获取
            return petDTO;
        }

        // 获取用户级别的写锁，防止并发操作
        ReentrantReadWriteLock lock = getUserLock(petDTO.getUserId());
        lock.writeLock().lock();
        try {
            // 再次检查，防止双重检查问题
            if (isRequestProcessed(requestId)) {
                return petDTO;
            }

            Pet pet = new Pet();
            BeanUtils.copyProperties(petDTO, pet);
            pet.setUserId(petDTO.getUserId()); // 保持兼容性
            pet.setCreatorId(petDTO.getUserId()); // 设置创建者ID
            pet.setCreateTime(LocalDateTime.now());
            petMapper.insert(pet);

            // 在 user_pet 表中创建拥有者记录，拥有者拥有所有权限
            UserPet userPet = new UserPet(
                petDTO.getUserId(),
                pet.getId(),
                UserPet.ROLE_OWNER,
                "[\"view\",\"edit\",\"manage\"]"
            );
            userPetMapper.insert(userPet);

            PetDTO result = new PetDTO();
            BeanUtils.copyProperties(pet, result);

            // 标记请求为已处理
            markRequestProcessed(requestId);

            return result;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public PetDTO updatePet(PetDTO petDTO) {
        String requestId = petDTO.getRequestId();

        // 幂等性检查：如果请求已处理过，直接返回成功
        if (isRequestProcessed(requestId)) {
            return petDTO;
        }

        // 从数据库查询原宠物信息以获取 userId（防止前端篡改）
        Pet existingPet = petMapper.selectById(petDTO.getId());
        if (existingPet == null) {
            throw new RuntimeException("宠物不存在");
        }

        // 获取用户级别的写锁，防止并发操作
        ReentrantReadWriteLock lock = getUserLock(existingPet.getUserId());
        lock.writeLock().lock();
        try {
            // 再次检查，防止双重检查问题
            if (isRequestProcessed(requestId)) {
                return petDTO;
            }

            Pet pet = new Pet();
            BeanUtils.copyProperties(petDTO, pet);
            pet.setUpdateTime(LocalDateTime.now());
            petMapper.updateById(pet);

            PetDTO result = new PetDTO();
            BeanUtils.copyProperties(pet, result);

            // 标记请求为已处理
            markRequestProcessed(requestId);

            return result;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void deletePet(Long petId) {
        petMapper.deleteById(petId);
    }

    @Override
    public void deletePet(Long petId, String requestId) {
        // 幂等性检查：如果请求已处理过，直接返回
        if (isRequestProcessed(requestId)) {
            return;
        }

        try {
            // 从数据库查询宠物所属用户
            Pet pet = petMapper.selectById(petId);
            if (pet == null) {
                // 宠物不存在，标记为已处理
                markRequestProcessed(requestId);
                return;
            }

            Long userId = pet.getUserId();

            // 获取用户级别的写锁，防止并发操作
            ReentrantReadWriteLock lock = getUserLock(userId);
            lock.writeLock().lock();
            try {
                // 再次检查，防止双重检查问题
                if (isRequestProcessed(requestId)) {
                    return;
                }

                petMapper.deleteById(petId);

                // 标记请求为已处理
                markRequestProcessed(requestId);
            } finally {
                lock.writeLock().unlock();
            }
        } catch (Exception e) {
            // 异常情况下不标记为已处理，允许重试
            throw new RuntimeException("删除宠物失败", e);
        }
    }


    /**
     * 获取用户有权访问的所有宠物（包括拥有的和共享的）
     */
    @Override
    public List<PetDTO> getUserAccessiblePets(Long userId) {
        // 查询 user_pet 表中该用户关联的所有宠物
        List<Long> petIds = userPetMapper.selectPetIdsByUserId(userId);

        if (petIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 根据宠物 ID 查询宠物信息
        LambdaQueryWrapper<Pet> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Pet::getId, petIds);
        wrapper.eq(Pet::getDeleted, 0);
        wrapper.orderByDesc(Pet::getCreateTime);

        List<Pet> pets = petMapper.selectList(wrapper);

        return pets.stream().map(pet -> {
            PetDTO dto = new PetDTO();
            BeanUtils.copyProperties(pet, dto);
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 分享宠物给其他用户
     */
    @Override
    public void sharePetWithUser(Long petId, Long toUserId, List<String> permissions, Long currentUserId) {
        // 检查当前用户是否是宠物的拥有者
        if (!petPermissionService.isOwner(currentUserId, petId)) {
            throw new PermissionDeniedException("只有宠物拥有者才能分享");
        }

        // 检查被分享者是否已存在
        UserPet existing = userPetMapper.selectOne(
            new LambdaQueryWrapper<UserPet>()
                .eq(UserPet::getUserId, toUserId)
                .eq(UserPet::getPetId, petId)
        );

        String permissionJson = String.join(",", permissions);

        if (existing != null) {
            // 如果已存在，更新权限
            existing.setPermission("[\"" + String.join("\",\"", permissions) + "\"]");
            existing.setUpdateTime(LocalDateTime.now());
            existing.setDeleted(0);
            userPetMapper.updateById(existing);
        } else {
            // 创建新的关联关系
            UserPet userPet = new UserPet(toUserId, petId, 2, "[\"" + String.join("\",\"", permissions) + "\"]");
            userPetMapper.insert(userPet);
        }

        // 记录分享操作
        PetShareLog log = new PetShareLog(
            petId,
            currentUserId,
            toUserId,
            "share",
            "[\"" + String.join("\",\"", permissions) + "\"]"
        );
        petShareLogMapper.insert(log);
    }

    /**
     * 撤销用户的宠物访问权
     */
    @Override
    public void revokePetAccess(Long petId, Long userId, Long currentUserId) {
        // 检查当前用户是否是宠物的拥有者
        if (!petPermissionService.isOwner(currentUserId, petId)) {
            throw new PermissionDeniedException("只有宠物拥有者才能撤销权限");
        }

        // 查找并删除关联
        UserPet userPet = userPetMapper.selectOne(
            new LambdaQueryWrapper<UserPet>()
                .eq(UserPet::getUserId, userId)
                .eq(UserPet::getPetId, petId)
        );

        if (userPet != null) {
            userPet.setDeleted(1);
            userPetMapper.updateById(userPet);
        }

        // 记录撤销操作
        PetShareLog log = new PetShareLog(petId, currentUserId, userId, "revoke", null);
        petShareLogMapper.insert(log);
    }

    /**
     * 更新用户的权限
     */
    @Override
    public void updateUserPermissions(Long petId, Long userId, List<String> permissions, Long currentUserId) {
        // 检查当前用户是否是宠物的拥有者
        if (!petPermissionService.isOwner(currentUserId, petId)) {
            throw new PermissionDeniedException("只有宠物拥有者才能修改权限");
        }

        // 查找并更新权限
        UserPet userPet = userPetMapper.selectOne(
            new LambdaQueryWrapper<UserPet>()
                .eq(UserPet::getUserId, userId)
                .eq(UserPet::getPetId, petId)
        );

        if (userPet != null) {
            userPet.setPermission("[\"" + String.join("\",\"", permissions) + "\"]");
            userPet.setUpdateTime(LocalDateTime.now());
            userPetMapper.updateById(userPet);

            // 记录权限更新操作
            PetShareLog log = new PetShareLog(
                petId,
                currentUserId,
                userId,
                "update",
                "[\"" + String.join("\",\"", permissions) + "\"]"
            );
            petShareLogMapper.insert(log);
        }
    }

    /**
     * 获取宠物的共享用户列表
     */
    @Override
    public List<UserPet> getPetSharedUsers(Long petId, Long currentUserId) {
        // 检查当前用户是否是宠物的拥有者
        if (!petPermissionService.isOwner(currentUserId, petId)) {
            throw new PermissionDeniedException("只有宠物拥有者才能查看共享用户列表");
        }

        return userPetMapper.selectList(
            new LambdaQueryWrapper<UserPet>()
                .eq(UserPet::getPetId, petId)
                .eq(UserPet::getDeleted, 0)
                .ne(UserPet::getRole, 1)  // 排除拥有者自己（role=1）
                .orderByAsc(UserPet::getCreateTime)
        );
    }

    /**
     * 获取宠物的权限分享历史
     */
    @Override
    public List<PetShareLog> getPetShareLogs(Long petId, Long currentUserId) {
        // 检查当前用户是否是宠物的拥有者
        if (!petPermissionService.isOwner(currentUserId, petId)) {
            throw new PermissionDeniedException("只有宠物拥有者才能查看分享历史");
        }

        return petShareLogMapper.selectLogsByPetId(petId);
    }

    /**
     * 删除宠物（只有拥有者可以删除）
     */
    @Override
    public void deletePetByOwner(Long petId, Long currentUserId) {
        // 检查当前用户是否是宠物的拥有者
        if (!petPermissionService.isOwner(currentUserId, petId)) {
            throw new PermissionDeniedException("只有宠物拥有者才能删除宠物");
        }

        // 逻辑删除宠物
        Pet pet = new Pet();
        pet.setId(petId);
        pet.setDeleted(1);
        pet.setUpdateTime(LocalDateTime.now());
        petMapper.updateById(pet);

        // 同时删除所有的 user_pet 关联（逻辑删除）
        UserPet userPet = new UserPet();
        userPet.setDeleted(1);
        userPet.setUpdateTime(LocalDateTime.now());
        userPetMapper.update(userPet,
            new LambdaQueryWrapper<UserPet>()
                .eq(UserPet::getPetId, petId)
        );
    }
}

