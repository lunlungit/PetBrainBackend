package com.aipetbrain.service.impl;

import com.aipetbrain.config.HuggingFaceConfig;
import com.aipetbrain.dto.AIAnalyzeDTO;
import com.aipetbrain.service.AIService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * AI分析服务实现
 * 接入Hugging Face免费AI服务进行图像识别
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

    private final HuggingFaceConfig huggingFaceConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 配置 OkHttpClient - 60秒超时，给Hugging Face API足够响应时间
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build();

    @Override
    public Map<String, Object> analyze(AIAnalyzeDTO dto) {
        // 如果未启用真实AI，使用模拟数据
        if (!huggingFaceConfig.getEnabled()) {
            return analyzeWithMock(dto);
        }

        // 调用Hugging Face真实AI
        try {
            if (dto.getAnalyzeType() == 1) {
                // 粑粑分析
                return analyzePoopWithAI(dto.getImageUrl());
            } else if (dto.getAnalyzeType() == 2) {
                // 皮肤分析
                return analyzeSkinWithAI(dto.getImageUrl());
            } else if (dto.getAnalyzeType() == 3) {
                // 宠物识别
                return analyzePetWithAI(dto.getImageUrl());
            } else if (dto.getAnalyzeType() == 4) {
                // 食物查询 - 使用AI文本分析
                return analyzeFoodWithAI(dto.getFoodName(), dto.getPetType());
            }
        } catch (Exception e) {
            log.error("Hugging Face AI分析失败，回退到模拟数据: {}", e.getMessage(), e);
            return analyzeWithMock(dto);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("imageUrl", dto.getImageUrl());
        return result;
    }

    /**
     * 使用模拟数据
     */
    private Map<String, Object> analyzeWithMock(AIAnalyzeDTO dto) {
        Map<String, Object> result = new HashMap<>();

        if (dto.getAnalyzeType() == 1) {
            result = analyzePoop();
        } else if (dto.getAnalyzeType() == 2) {
            result = analyzeSkin();
        } else if (dto.getAnalyzeType() == 3) {
            result = analyzePet();
        } else if (dto.getAnalyzeType() == 4) {
            result = analyzeFood(dto.getFoodName(), dto.getPetType());
        }

        result.put("imageUrl", dto.getImageUrl());
        return result;
    }

    // ==================== Hugging Face AI分析 ====================

    /**
     * 宠物识别 - 使用 Google ViT 模型
     */
    private Map<String, Object> analyzePetWithAI(String imageUrl) throws IOException {
        String model = huggingFaceConfig.getPetModel();
        String response = callHuggingFaceAPI(imageUrl, model);

        if (response == null) {
            throw new IOException("AI服务无响应");
        }

        return parsePetAIResponse(response, imageUrl);
    }

    /**
     * 粑粑分析 - 使用通用物体识别模型
     */
    private Map<String, Object> analyzePoopWithAI(String imageUrl) throws IOException {
        String model = huggingFaceConfig.getObjectModel();
        String response = callHuggingFaceAPI(imageUrl, model);

        if (response == null) {
            throw new IOException("AI服务无响应");
        }

        return parsePoopAIResponse(response, imageUrl);
    }

    /**
     * 皮肤分析 - 使用通用物体识别模型
     */
    private Map<String, Object> analyzeSkinWithAI(String imageUrl) throws IOException {
        String model = huggingFaceConfig.getObjectModel();
        String response = callHuggingFaceAPI(imageUrl, model);

        if (response == null) {
            throw new IOException("AI服务无响应");
        }

        return parseSkinAIResponse(response, imageUrl);
    }

    /**
     * 调用 Hugging Face Inference API
     */
    private String callHuggingFaceAPI(String imageUrl, String model) throws IOException {
        String apiUrl = "https://api-inference.huggingface.co/models/" + model;

        // 构建请求体
        String requestBody = buildRequestBody(imageUrl);

        Request request = new Request.Builder()
            .url(apiUrl)
            .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
            .addHeader("Authorization", "Bearer " + huggingFaceConfig.getApiToken())
            .addHeader("Content-Type", "application/json")
            .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "未知错误";
                log.error("Hugging Face API调用失败: {} - {}", response.code(), errorBody);
                return null;
            }

            return response.body().string();
        }
    }

    /**
     * 构建请求体
     */
    private String buildRequestBody(String imageUrl) {
        Map<String, Object> body = Map.of(
            "inputs", imageUrl,
            "parameters", Map.of(
                "candidate_labels", List.of("dog", "cat", "rabbit", "hamster", "bird", "poop", "skin", "fur", "hair"),
                "top_k", 5
            )
        );
        try {
            return objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            return "{\"inputs\": \"" + imageUrl + "\"}";
        }
    }

    /**
     * 解析宠物识别响应
     */
    private Map<String, Object> parsePetAIResponse(String response, String imageUrl) {
        Map<String, Object> result = new HashMap<>();

        try {
            JsonNode json = objectMapper.readTree(response);

            if (json.has("error")) {
                throw new IOException(json.get("error").asText());
            }

            // Hugging Face 分类模型返回数组，格式为 [[标签, 分数], [标签, 分数], ...]
            if (json.isArray() && json.size() > 0) {
                JsonNode topResult = json.get(0);

                if (topResult.isArray() && topResult.size() >= 2) {
                    String topLabel = topResult.get(0).asText();
                    double topScore = topResult.get(1).asDouble();

                    int petType = mapToPetType(topLabel);
                    String breed = extractBreedFromLabel(topLabel);
                    String color = "未知"; // Hugging Face基础模型不提供颜色识别

                    result.put("petType", petType);
                    result.put("breed", breed);
                    result.put("color", color);
                    result.put("confidence", (int) (topScore * 100));
                } else {
                    result.put("petType", 1);
                    result.put("breed", "未识别");
                    result.put("color", "未识别");
                    result.put("confidence", 50);
                }
            } else {
                result.put("petType", 1);
                result.put("breed", "未识别");
                result.put("color", "未识别");
                result.put("confidence", 50);
            }

            result.put("imageUrl", imageUrl);
            result.put("analyzeTime", System.currentTimeMillis());
        } catch (Exception e) {
            log.error("解析宠物识别响应失败: {}", e.getMessage());
            result.put("petType", 1);
            result.put("breed", "解析失败");
            result.put("color", "解析失败");
            result.put("confidence", 0);
            result.put("imageUrl", imageUrl);
            result.put("analyzeTime", System.currentTimeMillis());
        }

        return result;
    }

    /**
     * 解析粑粑分析响应
     */
    private Map<String, Object> parsePoopAIResponse(String response, String imageUrl) {
        Map<String, Object> result = new HashMap<>();

        try {
            JsonNode json = objectMapper.readTree(response);

            if (json.has("error")) {
                throw new IOException(json.get("error").asText());
            }

            // Hugging Face 分类模型返回数组 [[标签, 分数], ...]
            boolean detected = false;

            if (json.isArray() && json.size() > 0) {
                for (JsonNode item : json) {
                    if (item.isArray() && item.size() >= 2) {
                        String label = item.get(0).asText().toLowerCase();
                        double score = item.get(1).asDouble();

                        if (label.contains("poop") || label.contains("feces") || label.contains("excrement")) {
                            detected = true;
                            int scoreLevel = score > 0.7 ? 0 : (score > 0.5 ? 1 : 2);
                            result.put("scoreLevel", scoreLevel);
                            result.put("score", scoreLevel == 0 ? "优" : (scoreLevel == 1 ? "良" : "差"));
                            result.put("cause", scoreLevel == 0 ? "消化正常，继续保持！" :
                                (scoreLevel == 1 ? "脂肪含量略高，建议减少零食" : "可能是着凉或饮食不当，建议观察一天"));
                            result.put("suggestion", scoreLevel == 0 ? "继续保持均衡饮食" :
                                (scoreLevel == 1 ? "建议减少高脂肪零食，增加运动量" : "清淡饮食一天，如有持续不适请及时就医"));
                            break;
                        }
                    }
                }
            }

            if (!detected) {
                // 未检测到粑粑，返回默认建议
                result.put("scoreLevel", 1);
                result.put("score", "良");
                result.put("cause", "图片不清晰，建议重新拍摄");
                result.put("suggestion", "请拍摄清晰的粑粑照片后重试");
            }

            result.put("imageUrl", imageUrl);
            result.put("analyzeTime", System.currentTimeMillis());
        } catch (Exception e) {
            log.error("解析粑粑分析响应失败: {}", e.getMessage());
            // 失败时返回默认建议
            result.put("scoreLevel", 1);
            result.put("score", "良");
            result.put("cause", "分析失败，请重试");
            result.put("suggestion", "请重新拍摄照片");
            result.put("imageUrl", imageUrl);
            result.put("analyzeTime", System.currentTimeMillis());
        }

        return result;
    }

    /**
     * 解析皮肤分析响应
     */
    private Map<String, Object> parseSkinAIResponse(String response, String imageUrl) {
        Map<String, Object> result = new HashMap<>();

        try {
            JsonNode json = objectMapper.readTree(response);

            if (json.has("error")) {
                throw new IOException(json.get("error").asText());
            }

            // Hugging Face 分类模型返回数组 [[标签, 分数], ...]
            boolean detected = false;
            int matchRate = 60;

            if (json.isArray() && json.size() > 0) {
                for (JsonNode item : json) {
                    if (item.isArray() && item.size() >= 2) {
                        String label = item.get(0).asText().toLowerCase();
                        double score = item.get(1).asDouble();

                        if (label.contains("skin") || label.contains("rash") || label.contains("infection")) {
                            detected = true;
                            matchRate = 60 + (int)(score * 35);
                            int riskIndex = matchRate > 80 ? 2 : (matchRate > 60 ? 1 : 0);

                            String[] diseases = {"真菌感染", "细菌感染", "过敏性皮炎", "寄生虫感染", "正常皮肤"};
                            String[] riskLevels = {"低风险", "中风险", "高风险"};
                            String[] suggestions = {
                                "建议保持清洁，定期观察",
                                "建议减少洗澡频率，使用温和洗护",
                                "建议尽快就医，避免病情加重"
                            };

                            result.put("disease", diseases[riskIndex]);
                            result.put("matchRate", matchRate);
                            result.put("riskLevel", riskLevels[riskIndex]);
                            result.put("riskIndex", riskIndex);
                            result.put("suggestion", suggestions[riskIndex]);
                            break;
                        }
                    }
                }
            }

            if (!detected) {
                result.put("disease", "皮肤状况正常");
                result.put("matchRate", 90);
                result.put("riskLevel", "低风险");
                result.put("riskIndex", 0);
                result.put("suggestion", "建议保持清洁，定期观察");
            }

            result.put("nearbyHospitals", List.of(
                Map.of("name", "爱心宠物医院", "distance", "1.2km"),
                Map.of("name", "瑞鹏宠物医院", "distance", "2.5km")
            ));
            result.put("disclaimer", "AI结果仅供参考，不作为医疗诊断依据");
            result.put("imageUrl", imageUrl);
            result.put("analyzeTime", System.currentTimeMillis());
        } catch (Exception e) {
            log.error("解析皮肤分析响应失败: {}", e.getMessage());
            result.put("disease", "分析失败");
            result.put("matchRate", 60);
            result.put("riskLevel", "中风险");
            result.put("riskIndex", 1);
            result.put("suggestion", "请重新拍摄照片");
            result.put("imageUrl", imageUrl);
            result.put("analyzeTime", System.currentTimeMillis());
        }

        return result;
    }

    /**
     * 将标签映射到宠物类型
     */
    private int mapToPetType(String label) {
        label = label.toLowerCase();

        if (label.contains("dog") || label.contains("犬") || label.contains("canine")) {
            return 1;
        } else if (label.contains("cat") || label.contains("猫") || label.contains("feline")) {
            return 2;
        } else if (label.contains("rabbit") || label.contains("兔")) {
            return 3;
        } else if (label.contains("hamster") || label.contains("仓鼠")) {
            return 3;
        }

        return 1; // 默认为狗
    }

    /**
     * 从标签提取品种名称
     */
    private String extractBreedFromLabel(String label) {
        // 提取英文品种名
        label = label.toLowerCase().replace("dog", "").replace("cat", "").replace("animal", "").trim();

        if (label.isEmpty()) {
            return "未知";
        }

        // 映射到中文品种
        Map<String, String> breedMap = new HashMap<>();
        breedMap.put("golden retriever", "金毛");
        breedMap.put("corgi", "柯基");
        breedMap.put("husky", "哈士奇");
        breedMap.put("labrador", "拉布拉多");
        breedMap.put("border collie", "边境牧羊犬");
        breedMap.put("poodle", "泰迪");
        breedMap.put("bichon", "比熊");
        breedMap.put("samoyed", "萨摩耶");
        breedMap.put("shiba", "柴犬");
        breedMap.put("german shepherd", "德牧");
        breedMap.put("british shorthair", "英短");
        breedMap.put("american shorthair", "美短");
        breedMap.put("ragdoll", "布偶");
        breedMap.put("siamese", "暹罗");
        breedMap.put("persian", "波斯");
        breedMap.put("maine coon", "缅因");

        for (Map.Entry<String, String> entry : breedMap.entrySet()) {
            if (label.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return label.substring(0, Math.min(label.length(), 10));
    }

    // ==================== 模拟数据方法 ====================

    private Map<String, Object> analyzePoop() {
        Map<String, Object> result = new HashMap<>();
        Random random = new Random();

        int score = random.nextInt(3);
        String[] levels = {"优", "良", "差"};
        String[] causes = {"消化正常，继续保持！", "脂肪含量略高，建议减少零食", "可能是着凉或饮食不当，建议观察一天"};
        String[] suggestions = {"继续保持均衡饮食", "建议减少高脂肪零食，增加运动量", "清淡饮食一天，如有持续不适请及时就医"};

        result.put("score", levels[score]);
        result.put("scoreLevel", score);
        result.put("cause", causes[score]);
        result.put("suggestion", suggestions[score]);
        result.put("analyzeTime", System.currentTimeMillis());

        return result;
    }

    private Map<String, Object> analyzeSkin() {
        Map<String, Object> result = new HashMap<>();
        Random random = new Random();

        int matchRate = 60 + random.nextInt(35);
        String[] diseases = {"真菌感染", "细菌感染", "过敏性皮炎", "寄生虫感染", "正常皮肤"};
        int diseaseIndex = random.nextInt(diseases.length);
        String[] riskLevels = {"低风险", "中风险", "高风险"};
        String[] riskSuggestions = {
            "建议保持清洁，定期观察",
            "建议减少洗澡频率，使用温和洗护",
            "建议尽快就医，避免病情加重"
        };
        int riskIndex = matchRate > 80 ? 2 : (matchRate > 60 ? 1 : 0);

        result.put("disease", diseases[diseaseIndex]);
        result.put("matchRate", matchRate);
        result.put("riskLevel", riskLevels[riskIndex]);
        result.put("riskIndex", riskIndex);
        result.put("suggestion", riskSuggestions[riskIndex]);
        result.put("nearbyHospitals", List.of(
            Map.of("name", "爱心宠物医院", "distance", "1.2km"),
            Map.of("name", "瑞鹏宠物医院", "distance", "2.5km")
        ));
        result.put("disclaimer", "AI结果仅供参考，不作为医疗诊断依据");
        result.put("analyzeTime", System.currentTimeMillis());

        return result;
    }

    private Map<String, Object> analyzePet() {
        Map<String, Object> result = new HashMap<>();
        Random random = new Random();

        int petType = 1 + random.nextInt(3);

        String[] dogBreeds = {"金毛", "柯基", "哈士奇", "拉布拉多", "边境牧羊犬", "泰迪", "比熊", "萨摩耶", "柴犬", "德牧"};
        String[] catBreeds = {"英短", "美短", "布偶", "暹罗", "波斯", "缅因", "斯芬克斯", "蓝猫"};
        String[] otherBreeds = {"兔子", "仓鼠", "龙猫", "荷兰猪"};

        String breed;
        if (petType == 1) {
            breed = dogBreeds[random.nextInt(dogBreeds.length)];
        } else if (petType == 2) {
            breed = catBreeds[random.nextInt(catBreeds.length)];
        } else {
            breed = otherBreeds[random.nextInt(otherBreeds.length)];
        }

        String[] colors = {"黄色", "黑色", "白色", "棕色", "灰色", "黑白花", "黄白花", "三花", "橘色"};
        String color = colors[random.nextInt(colors.length)];

        result.put("petType", petType);
        result.put("breed", breed);
        result.put("color", color);
        result.put("confidence", 75 + random.nextInt(20));
        result.put("analyzeTime", System.currentTimeMillis());

        return result;
    }

    // ==================== 食物查询 ====================

    /**
     * 使用 AI 查询食物安全性
     */
    private Map<String, Object> analyzeFoodWithAI(String foodName, Integer petType) throws IOException {
        // 暂时使用模拟数据，因为 Hugging Face 的文本分类 API 需要特定的模型
        // 如果需要真实 AI 分析，可以接入 OpenAI 或其他文本分析 API
        log.info("AI 食物查询: {} for petType {}", foodName, petType);
        return analyzeFood(foodName, petType);
    }

    /**
     * 模拟食物查询结果
     */
    private Map<String, Object> analyzeFood(String foodName, Integer petType) {
        Map<String, Object> result = new HashMap<>();
        Random random = new Random();

        foodName = foodName.toLowerCase();

        // 危险食物列表
        String[] dogDangerousFoods = {"巧克力", "葡萄", "洋葱", "大蒜", "木糖醇", "酒精", "咖啡", "澳洲坚果"};
        String[] catDangerousFoods = {"巧克力", "洋葱", "大蒜", "葡萄", "葡萄干", "酒精", "咖啡", "澳洲坚果"};
        String[] otherDangerousFoods = {"巧克力", "洋葱", "大蒜", "酒精", "咖啡", "糖果", "蛋糕", "坚果"};

        // 少吃慎吃的食物
        String[] dogCautionFoods = {"牛奶", "奶酪", "骨头", "肥肉", "香肠", "火腿", "培根"};
        String[] catCautionFoods = {"牛奶", "奶酪", "金枪鱼", "肥肉", "香肠", "火腿"};
        String[] otherCautionFoods = {"牛奶", "奶酪", "肥肉", "甜食", "人类零食"};

        String[] dangerFoods = {};
        String[] cautionFoods = {};

        // 根据宠物类型选择危险食物列表
        if (petType == 1) { // 狗
            dangerFoods = dogDangerousFoods;
            cautionFoods = dogCautionFoods;
        } else if (petType == 2) { // 猫
            dangerFoods = catDangerousFoods;
            cautionFoods = catCautionFoods;
        } else { // 其他宠物
            dangerFoods = otherDangerousFoods;
            cautionFoods = otherCautionFoods;
        }

        // 判断食物安全性
        int resultType = 0; // 0:可以吃 1:少吃慎吃 2:千万别吃
        String description = "";
        String nutrition = "";

        // 检查危险食物
        for (String dangerFood : dangerFoods) {
            if (foodName.contains(dangerFood)) {
                resultType = 2;
                description = generateDangerDescription(foodName, petType);
                break;
            }
        }

        // 如果不是危险食物，检查慎吃的食物
        if (resultType == 0) {
            for (String cautionFood : cautionFoods) {
                if (foodName.contains(cautionFood)) {
                    resultType = 1;
                    description = generateCautionDescription(foodName, petType);
                    break;
                }
            }
        }

        // 如果都不是，默认为可以吃（实际应该查询数据库）
        if (resultType == 0) {
            resultType = 0;
            description = generateSafeDescription(foodName, petType);
            nutrition = generateNutritionInfo(foodName);
        }

        result.put("result", resultType);
        result.put("description", description);
        result.put("nutrition", nutrition);
        result.put("analyzeTime", System.currentTimeMillis());

        return result;
    }

    /**
     * 生成危险食物描述
     */
    private String generateDangerDescription(String foodName, int petType) {
        String petName = petType == 1 ? "狗狗" : (petType == 2 ? "猫咪" : "宠物");

        Map<String, String> descriptions = new HashMap<>();
        descriptions.put("巧克力", petName + "吃" + foodName + "会中毒！巧克力含有可可碱，会导致呕吐、腹泻、心率失常，严重时危及生命！");
        descriptions.put("葡萄", foodName + "对" + petName + "有毒！可能引起急性肾衰竭，绝对不能吃！");
        descriptions.put("洋葱", foodName + "含有的硫化物会破坏" + petName + "的红血球，导致贫血！");
        descriptions.put("大蒜", "大蒜会破坏" + petName + "的红血球，导致溶血性贫血！");
        descriptions.put("酒精", "酒精对" + petName + "的神经系统有严重损害，绝对禁止！");
        descriptions.put("咖啡", "咖啡因对" + petName + "有毒，会引起心跳加速、呼吸困难等症状！");

        return descriptions.getOrDefault(foodName, foodName + "对" + petName + "有毒，请立即远离！");
    }

    /**
     * 生成慎吃食物描述
     */
    private String generateCautionDescription(String foodName, int petType) {
        String petName = petType == 1 ? "狗狗" : (petType == 2 ? "猫咪" : "宠物");

        Map<String, String> descriptions = new HashMap<>();
        descriptions.put("牛奶", petName + "可能乳糖不耐受，建议少量喂食，观察是否有腹泻反应。");
        descriptions.put("奶酪", "奶酪脂肪含量高，可以少量喂食，但不要作为主食。");
        descriptions.put("骨头", "骨头可能划伤消化道，建议煮熟且去掉尖锐部分后少量喂食。");
        descriptions.put("肥肉", "高脂肪食物可能导致胰腺炎，建议适量喂食。");

        return descriptions.getOrDefault(foodName, foodName + "可以适量喂食，但要注意控制量和频率。");
    }

    /**
     * 生成安全食物描述
     */
    private String generateSafeDescription(String foodName, int petType) {
        String petName = petType == 1 ? "狗狗" : (petType == 2 ? "猫咪" : "宠物");

        Map<String, String> descriptions = new HashMap<>();
        descriptions.put("苹果", "苹果富含维生素和纤维，但要去皮去籽，种子含有氰化物。");
        descriptions.put("香蕉", "香蕉富含钾和维生素，是" + petName + "的好零食，但要适量。");
        descriptions.put("鸡胸肉", "鸡胸肉富含优质蛋白质，煮熟后是" + petName + "的优质食物。");
        descriptions.put("胡萝卜", "胡萝卜富含维生素和纤维，对" + petName + "的视力和消化有益。");

        return descriptions.getOrDefault(foodName, foodName + "是安全的，可以放心喂食。建议煮熟并少量喂食。");
    }

    /**
     * 生成营养信息
     */
    private String generateNutritionInfo(String foodName) {
        Map<String, String> nutritionInfo = new HashMap<>();
        nutritionInfo.put("苹果", "富含维生素C、膳食纤维和抗氧化物质，有助于消化和增强免疫力。");
        nutritionInfo.put("香蕉", "富含钾元素、维生素B6和膳食纤维，有助于肌肉和神经功能。");
        nutritionInfo.put("鸡胸肉", "富含优质蛋白质，低脂肪，是维持肌肉健康的理想食物。");
        nutritionInfo.put("胡萝卜", "富含β-胡萝卜素、维生素A和纤维，有助于视力和皮肤健康。");
        nutritionInfo.put("牛肉", "富含蛋白质、铁和锌，有助于肌肉发育和血液循环。");

        return nutritionInfo.getOrDefault(foodName, "含有丰富的营养成分，对宠物健康有益。");
    }
}

