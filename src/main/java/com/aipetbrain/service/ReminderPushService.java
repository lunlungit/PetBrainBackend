package com.aipetbrain.service;

public interface ReminderPushService {
    /**
     * 检查并发送超时待办提醒
     */
    void checkAndSendOverdueReminders();
}

