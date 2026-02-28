package com.aipetbrain.service;

import com.aipetbrain.entity.Reminder;
import java.util.List;

public interface ReminderService {
    List<Reminder> getReminders(Long userId);
    Reminder getReminder(Long id);
    Reminder addReminder(Reminder reminder);
    Reminder updateReminder(Reminder reminder);
    void deleteReminder(Long id);
    void completeReminder(Long id);
}

