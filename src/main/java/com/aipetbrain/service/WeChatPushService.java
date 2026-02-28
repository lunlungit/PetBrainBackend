package com.aipetbrain.service;

public interface WeChatPushService {
    /**
     * 发送微信小程序订阅消息
     * @param openid 用户openid
     * @param templateId 模板ID
     * @param page 小程序页面路径
     * @param data 消息数据
     */
    void sendSubscribeMessage(String openid, String templateId, String page, String data);

    /**
     * 发送待办超时提醒
     * @param openid 用户openid
     * @param reminderTitle 待办标题
     * @param reminderTime 待办时间
     */
    void sendReminderOverdueNotification(String openid, String reminderTitle, String reminderTime);
}

