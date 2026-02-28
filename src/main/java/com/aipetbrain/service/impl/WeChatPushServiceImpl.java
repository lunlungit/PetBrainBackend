package com.aipetbrain.service.impl;

import com.aipetbrain.service.WeChatPushService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeChatPushServiceImpl implements WeChatPushService {

    @Value("${wechat.appid}")
    private String appId;

    @Value("${wechat.secret}")
    private String appSecret;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 待办超时提醒模板ID
    private static final String REMINDER_TEMPLATE_ID = "BBmAu5NDpS4TmtMCy5sjqyHLoUywdH_W1DawG8ecGzs";

    @Override
    public void sendSubscribeMessage(String openid, String templateId, String page, String data) {
        try {
            // 获取access_token
            String accessToken = getAccessToken();
            if (accessToken == null) {
                log.error("获取微信access_token失败");
                return;
            }

            // 构建请求URL
            String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + accessToken;

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("touser", openid);
            requestBody.put("template_id", templateId);
            requestBody.put("page", page);
            requestBody.put("data", objectMapper.readValue(data, Map.class));

            // 发送请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            log.info("微信推送响应: {}", response.getBody());

        } catch (Exception e) {
            log.error("发送微信推送失败", e);
        }
    }

    @Override
    public void sendReminderOverdueNotification(String openid, String reminderTitle, String reminderTime) {
        try {
            String accessToken = getAccessToken();
            if (accessToken == null) {
                log.error("获取微信access_token失败");
                return;
            }

            String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + accessToken;

            // 构建消息数据
            Map<String, Object> data = new HashMap<>();
            Map<String, Object> thing1 = new HashMap<>();
            thing1.put("value", reminderTitle);
            data.put("thing1", thing1);

            Map<String, Object> time2 = new HashMap<>();
            time2.put("value", reminderTime);
            data.put("time2", time2);

            Map<String, Object> thing3 = new HashMap<>();
            thing3.put("value", "您的待办已超过10分钟，请及时处理");
            data.put("thing3", thing3);

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("touser", openid);
            requestBody.put("template_id", REMINDER_TEMPLATE_ID);
            requestBody.put("page", "pages/index/index");
            requestBody.put("data", data);

            // 发送请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            log.info("发送待办超时提醒: openid={}, title={}, response={}", openid, reminderTitle, response.getBody());

        } catch (Exception e) {
            log.error("发送待办超时提醒失败", e);
        }
    }

    /**
     * 获取微信access_token
     */
    private String getAccessToken() {
        try {
            String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + appId + "&secret=" + appSecret;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> result = objectMapper.readValue(response.getBody(), Map.class);
                String accessToken = (String) result.get("access_token");
                log.info("获取access_token成功");
                return accessToken;
            }

        } catch (Exception e) {
            log.error("获取access_token失败", e);
        }
        return null;
    }
}

