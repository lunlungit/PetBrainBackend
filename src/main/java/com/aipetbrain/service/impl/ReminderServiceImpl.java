package com.aipetbrain.service.impl;

import com.aipetbrain.entity.Reminder;
import com.aipetbrain.mapper.ReminderMapper;
import com.aipetbrain.service.ReminderService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReminderServiceImpl implements ReminderService {

    private final ReminderMapper reminderMapper;

    @Override
    public List<Reminder> getReminders(Long userId) {
        LambdaQueryWrapper<Reminder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reminder::getUserId, userId);
        wrapper.orderByAsc(Reminder::getRemindTime);
        return reminderMapper.selectList(wrapper);
    }

    @Override
    public Reminder getReminder(Long id) {
        return reminderMapper.selectById(id);
    }

    @Override
    public Reminder addReminder(Reminder reminder) {
        System.out.println("=== 添加待办，接收到的数据: " + reminder);
        reminder.setCreateTime(LocalDateTime.now());
        reminderMapper.insert(reminder);
        return reminder;
    }

    @Override
    public Reminder updateReminder(Reminder reminder) {
        reminder.setUpdateTime(LocalDateTime.now());
        reminderMapper.updateById(reminder);
        return reminder;
    }

    @Override
    public void deleteReminder(Long id) {
        reminderMapper.deleteById(id);
    }

    @Override
    public void completeReminder(Long id) {
        Reminder reminder = new Reminder();
        reminder.setId(id);
        reminder.setStatus(1); // 已完成
        reminder.setUpdateTime(LocalDateTime.now());
        reminderMapper.updateById(reminder);
    }
}

