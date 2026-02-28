package com.aipetbrain.service;

import com.aipetbrain.entity.Pet;
import com.aipetbrain.entity.User;
import com.aipetbrain.entity.UserPet;
import com.aipetbrain.mapper.PetMapper;
import com.aipetbrain.mapper.UserMapper;
import com.aipetbrain.mapper.UserPetMapper;
import com.aipetbrain.service.impl.PetPermissionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 宠物权限检查服务测试
 */
@DisplayName("宠物权限检查服务")
public class PetPermissionServiceTest {

    @Mock
    private PetMapper petMapper;

    @Mock
    private UserPetMapper userPetMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private PetPermissionServiceImpl petPermissionService;

    private Pet testPet;
    private User testUser;
    private User otherUser;
    private UserPet userPetRead;
    private UserPet userPetWrite;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // 创建测试宠物
        testPet = new Pet();
        testPet.setId(1L);
        testPet.setName("测试宠物");
        testPet.setCreatorId(1L);

        // 创建测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setNickname("owner");

        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setNickname("other");

        // 创建只读权限关系
        userPetRead = new UserPet();
        userPetRead.setUserId(2L);
        userPetRead.setPetId(1L);
        userPetRead.setPermission("READ");

        // 创建编辑权限关系
        userPetWrite = new UserPet();
        userPetWrite.setUserId(3L);
        userPetWrite.setPetId(1L);
        userPetWrite.setPermission("WRITE");
    }

    @Test
    @DisplayName("所有者拥有完全权限")
    void testOwnerHasFullPermission() {
        when(petMapper.selectById(1L)).thenReturn(testPet);

        // 所有者应该有完全权限
        assertTrue(petPermissionService.isOwner(1L, 1L));
        assertTrue(petPermissionService.canView(1L, 1L));
        assertTrue(petPermissionService.canEdit(1L, 1L));
        assertTrue(petPermissionService.canManage(1L, 1L));
        assertTrue(petPermissionService.canDelete(1L, 1L));
    }

    @Test
    @DisplayName("编辑权限用户可以修改")
    void testWritePermissionUser() {
        when(petMapper.selectById(1L)).thenReturn(testPet);
        when(userPetMapper.selectOne(any())).thenReturn(userPetWrite);

        assertTrue(petPermissionService.canEdit(3L, 1L));
        assertTrue(petPermissionService.canView(3L, 1L));
        assertFalse(petPermissionService.canDelete(3L, 1L));
    }

    @Test
    @DisplayName("查看权限用户只能查看")
    void testReadPermissionUser() {
        when(petMapper.selectById(1L)).thenReturn(testPet);
        when(userPetMapper.selectOne(any())).thenReturn(userPetRead);

        assertTrue(petPermissionService.canView(2L, 1L));
        assertFalse(petPermissionService.canEdit(2L, 1L));
        assertFalse(petPermissionService.canDelete(2L, 1L));
    }

    @Test
    @DisplayName("无权限用户不能访问")
    void testUnauthorizedUser() {
        when(petMapper.selectById(1L)).thenReturn(testPet);
        when(userPetMapper.selectOne(any())).thenReturn(null);

        assertFalse(petPermissionService.canView(999L, 1L));
        assertFalse(petPermissionService.canEdit(999L, 1L));
        assertFalse(petPermissionService.canDelete(999L, 1L));
    }

    @Test
    @DisplayName("所有者不能被删除权限")
    void testOwnerCannotBeDenied() {
        when(petMapper.selectById(1L)).thenReturn(testPet);

        assertTrue(petPermissionService.canDelete(1L, 1L));
        assertTrue(petPermissionService.isOwner(1L, 1L));
    }

    @Test
    @DisplayName("hasAccessToPet 方法测试 - 所有者")
    void testHasAccessToPetOwner() {
        when(petMapper.selectById(1L)).thenReturn(testPet);

        assertTrue(petPermissionService.hasAccessToPet(1L, 1L));
    }

    @Test
    @DisplayName("hasAccessToPet 方法测试 - 编辑权限用户")
    void testHasAccessToPetEditUser() {
        when(petMapper.selectById(1L)).thenReturn(testPet);
        when(userPetMapper.selectOne(any())).thenReturn(userPetWrite);

        assertTrue(petPermissionService.hasAccessToPet(3L, 1L));
    }

    @Test
    @DisplayName("hasAccessToPet 方法测试 - 无权限用户")
    void testHasAccessToPetUnauthorized() {
        when(petMapper.selectById(1L)).thenReturn(testPet);
        when(userPetMapper.selectOne(any())).thenReturn(null);

        assertFalse(petPermissionService.hasAccessToPet(999L, 1L));
    }

    @Test
    @DisplayName("hasPermission 方法测试")
    void testHasPermission() {
        when(petMapper.selectById(1L)).thenReturn(testPet);
        when(userPetMapper.selectOne(any())).thenReturn(userPetRead);

        assertTrue(petPermissionService.hasPermission(2L, 1L, "READ"));
        assertFalse(petPermissionService.hasPermission(2L, 1L, "WRITE"));
    }

    @Test
    @DisplayName("getUserPet 方法测试")
    void testGetUserPet() {
        when(userPetMapper.selectOne(any())).thenReturn(userPetWrite);

        UserPet result = petPermissionService.getUserPet(3L, 1L);
        assertNotNull(result);
        assertEquals(3L, result.getUserId());
        assertEquals(1L, result.getPetId());
        assertEquals("WRITE", result.getPermission());
    }
}

