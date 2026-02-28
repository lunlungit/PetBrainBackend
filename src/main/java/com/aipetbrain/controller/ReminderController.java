package com.aipetbrain.controller;

import com.aipetbrain.common.Result;
import com.aipetbrain.entity.Reminder;
import com.aipetbrain.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reminder")
@RequiredArgsConstructor
@CrossOrigin
public class ReminderController {

    private final ReminderService reminderService;

    @GetMapping("/list/{userId}")
    public Result<List<Reminder>> getReminders(@PathVariable Long userId) {
        List<Reminder> reminders = reminderService.getReminders(userId);
        return Result.success(reminders);
    }

    @GetMapping("/{id}")
    public Result<Reminder> getReminder(@PathVariable Long id) {
        Reminder reminder = reminderService.getReminder(id);
        if (reminder == null) {
            return Result.error("待办不存在");
        }
        return Result.success(reminder);
    }

    @PostMapping("/add")
    public Result<Reminder> addReminder(@RequestBody Reminder reminder) {
        Reminder result = reminderService.addReminder(reminder);
        return Result.success(result);
    }

    @PutMapping("/update")
    public Result<Reminder> updateReminder(@RequestBody Reminder reminder) {
        Reminder result = reminderService.updateReminder(reminder);
        return Result.success(result);
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteReminder(@PathVariable Long id) {
        reminderService.deleteReminder(id);
        return Result.success();
    }

    @PutMapping("/complete/{id}")
    public Result<Void> completeReminder(@PathVariable Long id) {
        reminderService.completeReminder(id);
        return Result.success();
    }
}

