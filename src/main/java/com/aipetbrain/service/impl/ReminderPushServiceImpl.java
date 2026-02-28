package com.aipetbrain.service.impl;

import com.aipetbrain.entity.Reminder;
import com.aipetbrain.entity.User;
import com.aipetbrain.mapper.ReminderMapper;
import com.aipetbrain.mapper.UserMapper;
import com.aipetbrain.service.ReminderPushService;
import com.aipetbrain.service.WeChatPushService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderPushServiceImpl implements ReminderPushService {

    private final ReminderMapper reminderMapper;
    private final UserMapper userMapper;
    private final WeChatPushService weChatPushService;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * 每30分钟执行一次检查
     */
    @Scheduled(fixedRate = 30 * 60 * 1000)
    @Override
    public void checkAndSendOverdueReminders() {
        try {
            log.info("开始检查超时待办（每30分钟执行一次）...");

            // 获取当前日期和今日时间
            LocalDate today = LocalDate.now();
            LocalDateTime now = LocalDateTime.now();

            // 查询所有待完成的待办（status = 0）
            LambdaQueryWrapper<Reminder> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Reminder::getStatus, 0);

            List<Reminder> allReminders = reminderMapper.selectList(wrapper);

            int pushCount = 0;
            for (Reminder reminder : allReminders) {
                // 如果没有设置提醒日期，跳过
                if (reminder.getRemindDate() == null || reminder.getRemindTime() == null) {
                    continue;
                }

                // 构建提醒的日期和时间
                LocalDateTime remindDateTime = LocalDateTime.of(reminder.getRemindDate(), reminder.getRemindTime());

                // 如果提醒时间在明天之后，或者提醒时间超过24小时前，则不处理
                if (remindDateTime.isAfter(now) || remindDateTime.isBefore(now.minusHours(24))) {
                    continue;
                }

                // 计算超时时长（分钟）
                long overdueMinutes = Duration.between(remindDateTime, now).toMinutes();

                // 如果超过10分钟
                if (overdueMinutes >= 10) {
                    // 检查今天是否已经推送过
                    LocalDateTime lastPushTime = reminder.getLastPushTime();
                    boolean alreadyPushedToday = lastPushTime != null
                        && lastPushTime.toLocalDate().equals(today);

                    if (!alreadyPushedToday) {
                        // 获取用户信息
                        User user = userMapper.selectById(reminder.getUserId());
                        if (user != null && user.getOpenid() != null) {
                            try {
                                // 发送推送
                                String remindTimeStr = reminder.getRemindTime().format(TIME_FORMATTER);
                                weChatPushService.sendReminderOverdueNotification(
                                    user.getOpenid(),
                                    reminder.getTitle(),
                                    remindTimeStr
                                );

                                // 更新最后推送时间
                                reminder.setLastPushTime(now);
                                reminderMapper.updateById(reminder);

                                pushCount++;
                                log.info("发送待办超时提醒成功: userId={}, reminderId={}, title={}",
                                    reminder.getUserId(), reminder.getId(), reminder.getTitle());
                            } catch (Exception e) {
                                log.error("发送待办超时提醒失败: reminderId={}", reminder.getId(), e);
                            }
                        } else {
                            log.warn("用户信息不存在或openid为空: userId={}", reminder.getUserId());
                        }
                    }
                }
            }

            log.info("检查超时待办完成，共推送 {} 条消息", pushCount);

        } catch (Exception e) {
            log.error("检查超时待办时发生错误", e);
        }
    }
}

