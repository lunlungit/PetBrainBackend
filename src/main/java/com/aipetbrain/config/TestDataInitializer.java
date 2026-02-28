package com.aipetbrain.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 测试数据初始化组件
 * 在应用启动后添加测试数据，用于功能测试
 * 生产环境应禁用此组件
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TestDataInitializer {

    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void initTestData() {
        // 检查环境变量或配置决定是否初始化测试数据
        String env = System.getenv("SPRING_PROFILES_ACTIVE");
        if ("prod".equals(env)) {
            log.info("⏭️  生产环境，跳过测试数据初始化");
            return;
        }

        log.info("🧪 开始初始化测试数据...");
        try {
            // 延迟执行，确保 DatabaseInitializer 已经完成
            Thread.sleep(500);
            insertTestLostPets();
            log.info("✅ 测试数据初始化完成");
        } catch (Exception e) {
            log.error("❌ 测试数据初始化失败:", e);
        }
    }

    /**
     * 插入测试的走失宠物和已找到宠物
     */
    private void insertTestLostPets() {
        try {
            // 检查是否已经有测试数据
            String checkSql = "SELECT COUNT(*) FROM lost_pet WHERE pet_name LIKE '%测试%'";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);

            if (count != null && count > 0) {
                log.info("⏭️  已存在测试数据，跳过插入");
                return;
            }

            // 获取一个现有的用户ID，或使用 ID 1
            String getUserSql = "SELECT id FROM user_info LIMIT 1";
            Long userId = null;
            try {
                userId = jdbcTemplate.queryForObject(getUserSql, Long.class);
            } catch (Exception e) {
                log.warn("⚠️  没有找到用户，使用默认 userId=1");
                userId = 1L;
            }

//            // 插入测试走失宠物数据
//            String insertLostPetSql = "INSERT INTO `lost_pet` " +
//                    "(`creator_id`, `pet_type`, `is_stray`, `pet_name`, `breed`, `color`, `avatar`, `description`, " +
//                    "`lost_time`, `lost_location_name`, `lost_latitude`, `lost_longitude`, " +
//                    "`contact_name`, `contact_phone`, `contact_wechat`, " +
//                    "`status`, `health_status`, `behavior_description`, `create_time`, `update_time`, `deleted`) " +
//                    "VALUES " +
//                    // 1. 测试走失狗 - 走失中
//                    "(" + userId + ", 1, 0, '测试走失狗', '泰迪犬', '棕色', '/static/logo.png', " +
//                    "'小王子是一只活泼的泰迪犬，昨天下午3点在XX公园走失，如果看到请立即联系我！', " +
//                    "DATE_SUB(NOW(), INTERVAL 1 DAY), 'XX公园北门', 39.9042, 116.4074, " +
//                    "'张三', '13800138000', 'zhangsan123', 0, 0, '很活泼，会回应名字', NOW(), NOW(), 0)," +
//                    // 2. 测试走失猫 - 走失中
//                    "(" + userId + ", 2, 0, '测试走失猫', '英短', '灰白色', '/static/logo.png', " +
//                    "'小花是一只温顺的英短，上周在住宅区走失，非常想家的猫咪，请帮忙留意！', " +
//                    "DATE_SUB(NOW(), INTERVAL 3 DAY), '中关村大街', 39.9826, 116.3055, " +
//                    "'李四', '13800138001', 'lisi456', 0, 0, '性格温顺，怕陌生人', NOW(), NOW(), 0)," +
//                    // 3. 测试已找到的狗 - 已找到
//                    "(" + userId + ", 1, 0, '测试已找到狗', '金毛犬', '金黄色', '/static/logo.png', " +
//                    "'金毛旺旺已经被找到并安全送回主人身边', " +
//                    "DATE_SUB(NOW(), INTERVAL 7 DAY), 'XX小区门口', 39.9750, 116.3270, " +
//                    "'王五', '13800138002', 'wangwu789', 1, 0, NULL, " +
//                    "DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY), 0)," +
//                    // 4. 测试流浪狗 - 流浪中
//                    "(" + userId + ", 1, 1, '测试流浪狗', '混血狗', '黑白色', '/static/logo.png', " +
//                    "'在郊区发现了一只流浪狗，看起来挺可怜的，体型中等，需要帮助！', " +
//                    "DATE_SUB(NOW(), INTERVAL 2 DAY), '郊区农田', 39.8865, 116.2887, " +
//                    "'赵六', '13800138003', 'zhaoliu000', 0, 2, '不太亲近人，害怕', NOW(), NOW(), 0)," +
//                    // 5. 测试已救助流浪猫 - 已找到
//                    "(" + userId + ", 2, 1, '测试流浪猫', '野生猫', '橙色', '/static/logo.png', " +
//                    "'成功救助了一只街边流浪猫，已送往宠物医院医治', " +
//                    "DATE_SUB(NOW(), INTERVAL 5 DAY), '市中心街道', 39.9075, 116.4087, " +
//                    "'孙七', '13800138004', 'sunqi111', 1, 1, '有点凶，可能打过架', " +
//                    "DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY), 0)";
//
//            int insertedRows = jdbcTemplate.update(insertLostPetSql);
//            log.info("✅ 成功插入 {} 条测试走失宠物数据", insertedRows);

        } catch (Exception e) {
            log.error("❌ 插入测试数据失败:", e);
        }
    }
}

