package com.aipetbrain.controller;

import com.aipetbrain.common.Result;
import com.aipetbrain.dto.PetDTO;
import com.aipetbrain.dto.SharePetRequest;
import com.aipetbrain.dto.UserPetDTO;
import com.aipetbrain.dto.PetShareLogDTO;
import com.aipetbrain.dto.LostPetDTO;
import com.aipetbrain.dto.PetShareCodeDTO;
import com.aipetbrain.entity.Pet;
import com.aipetbrain.entity.UserPet;
import com.aipetbrain.entity.PetShareLog;
import com.aipetbrain.service.PetService;
import com.aipetbrain.service.LostPetService;
import com.aipetbrain.service.PetPermissionService;
import com.aipetbrain.service.PetShareService;
import com.aipetbrain.service.UserService;
import com.aipetbrain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/pet")
@RequiredArgsConstructor
@CrossOrigin
public class PetController {

    private final PetService petService;
    private final LostPetService lostPetService;
    private final PetPermissionService petPermissionService;
    private final PetShareService petShareService;
    private final UserService userService;

    @GetMapping("/list/{userId}")
    public Result<List<PetDTO>> getPetList(@PathVariable Long userId) {
        List<PetDTO> pets = petService.getPetList(userId);
        return Result.success(pets);
    }

    @GetMapping("/detail/{petId}")
    public Result<PetDTO> getPetDetail(@PathVariable Long petId) {
        PetDTO pet = petService.getPetDetail(petId);
        if (pet == null) {
            return Result.error("宠物不存在");
        }
        return Result.success(pet);
    }

    @PostMapping("/add")
    public Result<PetDTO> addPet(@RequestBody PetDTO petDTO,
                                 @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
        // 使用请求ID用于幂等性控制
        petDTO.setRequestId(requestId);
        PetDTO pet = petService.addPet(petDTO);
        return Result.success(pet);
    }

    @PutMapping("/update")
    public Result<PetDTO> updatePet(@RequestBody PetDTO petDTO,
                                    @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                    @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
        try {
            log.info("【PET UPDATE】 petId={}, userId={}, requestId={}", petDTO.getId(), userId, requestId);

            // 使用请求ID用于幂等性控制
            petDTO.setRequestId(requestId);
            PetDTO pet = petService.updatePet(petDTO);

            log.info("【PET UPDATE】 更新成功，petId={}", petDTO.getId());
            return Result.success(pet);
        } catch (Exception e) {
            log.error("【PET UPDATE】 更新失败", e);
            return Result.error("保存失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{petId}")
    public Result<Void> deletePet(@PathVariable Long petId,
                                  @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
        // 使用请求ID用于幂等性控制
        petService.deletePet(petId, requestId);
        return Result.success();
    }

    /**
     * 获取附近的丢失宠物（支持分页）
     * @param latitude 纬度，可为空
     * @param longitude 经度，可为空
     * @param radius 搜索半径(米)，默认5000米
     * @param limit 每页返回数量，默认10条
     * @param page 分页页码（从1开始），默认1
     */
    @GetMapping("/lost/nearby")
    public Result<List<LostPetDTO>> getNearbyLostPets(
            @RequestParam(required = false) BigDecimal latitude,
            @RequestParam(required = false) BigDecimal longitude,
            @RequestParam(defaultValue = "5000") Double radius,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(defaultValue = "1") Integer page) {
        // 如果参数为null，使用默认值 0
        if (latitude == null) {
            latitude = BigDecimal.ZERO;
        }
        if (longitude == null) {
            longitude = BigDecimal.ZERO;
        }
        List<LostPetDTO> pets = lostPetService.getNearbyLostPets(latitude, longitude, radius, limit, page);
        return Result.success(pets);
    }

    /**
     * 获取单个走失宠物详情
     */
    @GetMapping("/lost/{lostPetId}")
    public Result<LostPetDTO> getLostPetDetail(@PathVariable Long lostPetId,
                                               @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        LostPetDTO lostPet = lostPetService.getLostPetDetail(lostPetId);
        if (lostPet == null) {
            return Result.error("走失宠物不存在");
        }

        // 检查用户是否有编辑权限
        boolean canEdit = false;

        // 1. 如果是流浪宠物 (isStray=1)，所有人都能编辑
        if (lostPet.getIsStray() != null && lostPet.getIsStray() == 1) {
            canEdit = true;
            log.info("📝 [getLostPetDetail] 流浪宠物，所有人都能编辑");
        }
        // 2. 如果是用户发布的走失宠物 (isStray=0) 且 petId 不为空，检查是否共同管理同一个宠物
        else if (lostPet.getIsStray() != null && lostPet.getIsStray() == 0 && lostPet.getPetId() != null && userId != null) {
            canEdit = petPermissionService.canEdit(userId, lostPet.getPetId());
            if (!canEdit && userId.equals(lostPet.getCreatorId())) {
                canEdit = true;
            }
            log.info("📝 [getLostPetDetail] 关联宠物的走失信息，canEdit={}", canEdit);
        }
        // 3. 其他所有登录用户都能编辑
        else if (userId != null) {
            canEdit = true;
            log.info("📝 [getLostPetDetail] 登录用户可以编辑");
        }

        lostPet.setCanEdit(canEdit);
        log.info("📝 [getLostPetDetail] lostPetId={}, userId={}, isStray={}, petId={}, canEdit={}",
                 lostPetId, userId, lostPet.getIsStray(), lostPet.getPetId(), canEdit);
        return Result.success(lostPet);
    }

    /**
     * 发布走失宠物（用户发布自己的宠物走失信息）
     */
    @PostMapping("/lost/publish")
    public Result<LostPetDTO> publishLostPet(@RequestBody LostPetDTO lostPetDTO,
                                              @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        log.info("📢 [publishLostPet] 收到发布走失宠物请求，userId={}, lostPetDTO={}", userId, lostPetDTO);
        lostPetDTO.setCreatorId(userId);
        log.info("📝 [publishLostPet] 设置 creatorId={}", userId);
        LostPetDTO result = lostPetService.publishLostPet(lostPetDTO);
        log.info("✅ [publishLostPet] 发布成功，result={}", result);
        return Result.success(result);
    }

    /**
     * 上报发现的流浪宠物
     */
    @PostMapping("/lost/report-stray")
    public Result<LostPetDTO> reportStrayPet(@RequestBody LostPetDTO lostPetDTO,
                                              @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        lostPetDTO.setCreatorId(userId);
        LostPetDTO result = lostPetService.reportStrayPet(lostPetDTO);
        return Result.success(result);
    }

    /**
     * 更新走失宠物信息
     */
    @PutMapping("/lost/update")
    public Result<LostPetDTO> updateLostPet(@RequestBody LostPetDTO lostPetDTO,
                                            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            log.info("📝 [updateLostPet] 收到更新走失宠物请求，userId={}, lostPetDTO={}", userId, lostPetDTO);
            LostPetDTO result = lostPetService.updateLostPet(lostPetDTO);
            log.info("✅ [updateLostPet] 更新成功，result={}", result);
            return Result.success(result);
        } catch (Exception e) {
            log.error("【LOST PET UPDATE】 更新失败", e);
            return Result.error("保存失败：" + e.getMessage());
        }
    }

    /**
     * 删除走失宠物
     */
    @DeleteMapping("/lost/delete/{lostPetId}")
    public Result<Void> deleteLostPet(@PathVariable Long lostPetId) {
        lostPetService.deleteLostPet(lostPetId);
        return Result.success();
    }

    /**
     * 标记走失宠物已找到
     */
    @PutMapping("/lost/found/{lostPetId}")
    public Result<Void> markPetFound(@PathVariable Long lostPetId) {
        lostPetService.markPetFound(lostPetId);
        return Result.success();
    }

    /**
     * 获取用户有权访问的所有宠物（包括拥有的和共享的）
     */
    @GetMapping("/my-pets/{userId}")
    public Result<List<PetDTO>> getMyPets(@PathVariable Long userId) {
        List<PetDTO> pets = petService.getUserAccessiblePets(userId);
        return Result.success(pets);
    }

    /**
     * 分享宠物给其他用户
     */
    @PostMapping("/{petId}/share")
    public Result<Void> sharePet(
            @PathVariable Long petId,
            @RequestParam Long currentUserId,
            @RequestBody SharePetRequest request) {
        petService.sharePetWithUser(petId, request.getToUserId(), request.getPermissions(), currentUserId);
        return Result.success();
    }

    /**
     * 撤销用户的宠物访问权
     */
    @DeleteMapping("/{petId}/share/{userId}")
    public Result<Void> revokePetAccess(
            @PathVariable Long petId,
            @PathVariable Long userId,
            @RequestParam Long currentUserId) {
        petService.revokePetAccess(petId, userId, currentUserId);
        return Result.success();
    }

    /**
     * 修改用户权限
     */
    @PutMapping("/{petId}/share/{userId}")
    public Result<Void> updateUserPermissions(
            @PathVariable Long petId,
            @PathVariable Long userId,
            @RequestParam Long currentUserId,
            @RequestBody SharePetRequest request) {
        petService.updateUserPermissions(petId, userId, request.getPermissions(), currentUserId);
        return Result.success();
    }

    /**
     * 查看宠物的共享用户列表
     */
    @GetMapping("/{petId}/shared-users")
    public Result<List<UserPetDTO>> getSharedUsers(
            @PathVariable Long petId,
            @RequestParam Long currentUserId) {
        List<UserPet> userPets = petService.getPetSharedUsers(petId, currentUserId);
        List<UserPetDTO> result = userPets.stream().map(up -> {
            UserPetDTO dto = new UserPetDTO();
            BeanUtils.copyProperties(up, dto);
            dto.setIsOwner(up.getRole() == 1);

            // 查询用户信息，填充用户名和头像
            User user = userService.getUserById(up.getUserId());
            if (user != null) {
                dto.setUserName(user.getNickname());
                dto.setUserAvatar(user.getAvatar());
            }

            return dto;
        }).collect(Collectors.toList());
        return Result.success(result);
    }

    /**
     * 查看宠物的权限分享历史
     */
    @GetMapping("/{petId}/share-logs")
    public Result<List<PetShareLogDTO>> getPetShareLogs(
            @PathVariable Long petId,
            @RequestParam Long currentUserId) {
        List<PetShareLog> logs = petService.getPetShareLogs(petId, currentUserId);
        List<PetShareLogDTO> result = logs.stream().map(log -> {
            PetShareLogDTO dto = new PetShareLogDTO();
            BeanUtils.copyProperties(log, dto);
            dto.setActionDesc(log.getActionDescription());
            return dto;
        }).collect(Collectors.toList());
        return Result.success(result);
    }

    /**
     * 生成或获取宠物分享码（用于微信分享）
     */
    @GetMapping("/{petId}/share-code")
    public Result<PetShareCodeDTO> getOrGenerateShareCode(
            @PathVariable Long petId,
            @RequestParam Long userId,
            @RequestParam(defaultValue = "READ") String permission) {
        try {
            log.info("🔗 [getOrGenerateShareCode] 获取分享码: petId={}, userId={}, permission={}", petId, userId, permission);
            PetShareCodeDTO shareCode = petShareService.generateShareCode(petId, userId, permission);
            return Result.success(shareCode);
        } catch (Exception e) {
            log.error("❌ [getOrGenerateShareCode] 获取分享码失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 通过分享码绑定宠物
     */
    @PostMapping("/bind-by-code")
    public Result<String> bindPetByShareCode(
            @RequestParam String shareCode,
            @RequestParam Long userId) {
        try {
            log.info("🔐 [bindPetByShareCode] 通过分享码绑定: shareCode={}, userId={}", shareCode, userId);
            boolean success = petShareService.bindPetByShareCode(shareCode, userId);
            if (success) {
                return Result.success("宠物绑定成功");
            } else {
                return Result.error("分享码无效或已失效");
            }
        } catch (Exception e) {
            log.error("❌ [bindPetByShareCode] 绑定失败", e);
            return Result.error(e.getMessage());
        }
    }
}

