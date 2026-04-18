package com.hhc.hhcaiagent.demo.invoke;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;

import java.util.*;

/**
 * HTTP方式调用ai
 */
public class HttpAiInvoke {

    public static void main(String[] args) {
        String apiKey = TestApiKey.API_KEY;
        String url = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";

        Map<String, Object> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", "You are a helpful assistant.");

        Map<String, Object> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", "你是谁？");

        List<Map<String, Object>> messages = Arrays.asList(systemMsg, userMsg);

        Map<String, Object> input = new HashMap<>();
        input.put("messages", messages);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("result_format", "message");

        Map<String, Object> body = new HashMap<>();
        body.put("model", "qwen-plus");
        body.put("input", input);
        body.put("parameters", parameters);

        String jsonBody = JSONUtil.toJsonStr(body);

        HttpResponse response = HttpRequest.post(url)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(jsonBody)
                .execute();

        System.out.println("HTTP Status: " + response.getStatus());
        System.out.println("Response: " + response.body());
    }
}