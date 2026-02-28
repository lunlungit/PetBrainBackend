package com.aipetbrain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 宠物权限共享记录表
 * 用于记录所有宠物权限分享、撤销、更新的操作历史
 */
@TableName("pet_share_log")
public class PetShareLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 宠物ID
     */
    private Long petId;

    /**
     * 分享者用户ID
     */
    private Long fromUserId;

    /**
     * 被分享者用户ID
     */
    private Long toUserId;

    /**
     * 操作类型: share(分享),revoke(撤销),update(更新权限)
     */
    private String action;

    /**
     * 权限详情(JSON格式)
     * 如: ["view","edit","manage"]
     */
    private String permission;

    /**
     * 操作时间
     */
    private LocalDateTime createTime;

    // ======================== Action Constants ========================

    public static final String ACTION_SHARE = "share";
    public static final String ACTION_REVOKE = "revoke";
    public static final String ACTION_UPDATE = "update";

    // ======================== Constructors ========================

    public PetShareLog() {
    }

    public PetShareLog(Long petId, Long fromUserId, Long toUserId, String action, String permission) {
        this.petId = petId;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.action = action;
        this.permission = permission;
        this.createTime = LocalDateTime.now();
    }

    // ======================== Helper Methods ========================

    /**
     * 判断是否为分享操作
     */
    public boolean isShare() {
        return ACTION_SHARE.equals(this.action);
    }

    /**
     * 判断是否为撤销操作
     */
    public boolean isRevoke() {
        return ACTION_REVOKE.equals(this.action);
    }

    /**
     * 判断是否为权限更新操作
     */
    public boolean isUpdate() {
        return ACTION_UPDATE.equals(this.action);
    }

    /**
     * 获取操作描述
     */
    public String getActionDescription() {
        switch (this.action) {
            case ACTION_SHARE:
                return "分享宠物";
            case ACTION_REVOKE:
                return "撤销权限";
            case ACTION_UPDATE:
                return "更新权限";
            default:
                return "未知操作";
        }
    }

    // ======================== Getters and Setters ========================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPetId() {
        return petId;
    }

    public void setPetId(Long petId) {
        this.petId = petId;
    }

    public Long getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(Long fromUserId) {
        this.fromUserId = fromUserId;
    }

    public Long getToUserId() {
        return toUserId;
    }

    public void setToUserId(Long toUserId) {
        this.toUserId = toUserId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "PetShareLog{" +
                "id=" + id +
                ", petId=" + petId +
                ", fromUserId=" + fromUserId +
                ", toUserId=" + toUserId +
                ", action='" + action + '\'' +
                ", permission='" + permission + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}

