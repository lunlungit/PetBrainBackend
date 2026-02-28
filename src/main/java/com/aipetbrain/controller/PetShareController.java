package com.aipetbrain.controller;

import com.aipetbrain.common.Result;
import com.aipetbrain.dto.PetShareCodeDTO;
import com.aipetbrain.service.PetShareService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 宠物分享Controller
 */
@Slf4j
@RestController
@RequestMapping("/pet/share")
@RequiredArgsConstructor
@CrossOrigin
public class PetShareController {

    private final PetShareService petShareService;

    /**
     * 生成或获取宠物分享码
     * @param petId 宠物ID
     * @param userId 用户ID
     * @return 分享码DTO
     */
    @GetMapping("/code/{petId}")
    public Result<PetShareCodeDTO> getShareCode(
            @PathVariable Long petId,
            @RequestParam Long userId,
            @RequestParam(defaultValue = "READ") String permission) {
        try {
            log.info("📋 [getShareCode] 获取分享码: petId={}, userId={}, permission={}", petId, userId, permission);
            PetShareCodeDTO shareCode = petShareService.generateShareCode(petId, userId, permission);
            return Result.success(shareCode);
        } catch (Exception e) {
            log.error("❌ [getShareCode] 获取分享码失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 通过分享码绑定宠物
     * @param shareCode 分享码
     * @param userId 用户ID
     * @return 是否成功
     */
    @PostMapping("/bind")
    public Result<String> bindPetByShareCode(
            @RequestParam String shareCode,
            @RequestParam Long userId) {
        try {
            log.info("🔗 [bindPetByShareCode] 通过分享码绑定: shareCode={}, userId={}", shareCode, userId);
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

    /**
     * 撤销分享码
     * @param shareCodeId 分享码ID
     * @param userId 用户ID
     * @return 是否成功
     */
    @DeleteMapping("/revoke/{shareCodeId}")
    public Result<String> revokeShareCode(
            @PathVariable Long shareCodeId,
            @RequestParam Long userId) {
        try {
            log.info("🔄 [revokeShareCode] 撤销分享码: shareCodeId={}, userId={}", shareCodeId, userId);
            boolean success = petShareService.revokeShareCode(shareCodeId, userId);
            if (success) {
                return Result.success("分享码已撤销");
            } else {
                return Result.error("撤销失败");
            }
        } catch (Exception e) {
            log.error("❌ [revokeShareCode] 撤销失败", e);
            return Result.error(e.getMessage());
        }
    }
}

