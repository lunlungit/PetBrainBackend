package com.aipetbrain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户-宠物关联表
 * 支持多用户管理同一宠物
 */
@TableName("user_pet")
public class UserPet implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 宠物ID
     */
    private Long petId;

    /**
     * 角色 1:拥有者 2:共享用户
     */
    private Integer role;

    /**
     * 权限(JSON格式)
     * 如: ["view","edit","manage"]
     */
    private String permission;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 删除标记 0:未删除 1:已删除
     */
    @TableLogic
    private Integer deleted;

    // ======================== Constructors ========================

    public UserPet() {
    }

    public UserPet(Long userId, Long petId, Integer role, String permission) {
        this.userId = userId;
        this.petId = petId;
        this.role = role;
        this.permission = permission;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        this.deleted = 0;
    }

    // ======================== Role Constants ========================

    public static final Integer ROLE_OWNER = 1;
    public static final Integer ROLE_SHARED_USER = 2;

    // ======================== Permission Constants ========================

    public static final String PERMISSION_VIEW = "view";
    public static final String PERMISSION_EDIT = "edit";
    public static final String PERMISSION_MANAGE = "manage";

    // ======================== Helper Methods ========================

    /**
     * 判断是否为拥有者
     */
    public boolean isOwner() {
        return ROLE_OWNER.equals(this.role);
    }

    /**
     * 判断是否为共享用户
     */
    public boolean isSharedUser() {
        return ROLE_SHARED_USER.equals(this.role);
    }

    /**
     * 判断是否具有指定权限
     */
    public boolean hasPermission(String permission) {
        if (this.permission == null) {
            return false;
        }
        return this.permission.contains(permission);
    }

    /**
     * 判断是否具有查看权限
     */
    public boolean canView() {
        return hasPermission(PERMISSION_VIEW);
    }

    /**
     * 判断是否具有编辑权限
     */
    public boolean canEdit() {
        return hasPermission(PERMISSION_EDIT);
    }

    /**
     * 判断是否具有管理权限
     */
    public boolean canManage() {
        return hasPermission(PERMISSION_MANAGE);
    }

    // ======================== Getters and Setters ========================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPetId() {
        return petId;
    }

    public void setPetId(Long petId) {
        this.petId = petId;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
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

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return "UserPet{" +
                "id=" + id +
                ", userId=" + userId +
                ", petId=" + petId +
                ", role=" + role +
                ", permission='" + permission + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", deleted=" + deleted +
                '}';
    }
}

