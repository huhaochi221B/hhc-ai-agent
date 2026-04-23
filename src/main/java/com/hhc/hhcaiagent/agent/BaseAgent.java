package com.hhc.hhcaiagent.agent;

import cn.hutool.core.util.StrUtil;
import com.hhc.hhcaiagent.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;
import java.util.List;

/*
 * 智能体基类，定义基本信息和多步骤执行流程
 */
@Data
@Slf4j
public abstract class BaseAgent {
    private String name;
    //提示词
    private String systemPrompt;
    private String userPrompt;
    private String nextStepPrompt;
    //代理状态
    private AgentState state = AgentState.IDLE;

    //执行步骤控制
    private int currentStep = 0;
    private int maxSteps = 10;

    // LLM 大模型
    private ChatClient chatClient;

    //Memory 记忆 (需要自主维护上下文)
    private List<Message> messageList = new ArrayList<>();

    /**
     *  运行代理
     * @return
     * @return  String
     */
    public String run(String userPrompt){
        //基础校验
        if(this.state != AgentState.IDLE){
            throw new RuntimeException("Cannot run agent from state: " + this.state);
        }
        if(StrUtil.isBlank(userPrompt)){
            throw new RuntimeException("Cannot run agent with empty userPrompt");
        }

        this.userPrompt = userPrompt;
        //执行
        this.state = AgentState.RUNNING;
        //记录消息上下文
        messageList.add(new UserMessage(this.userPrompt));
        //报保存结果列表
        List<String> results = new ArrayList<>();

        try {
            //执行循环
            for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                int stepNumber = i + 1;
                currentStep = stepNumber;
                log.info("Executing step {}/{}",stepNumber,maxSteps);
                //单步执行
                String stepResult = step();
                String result = "Step" + stepNumber + ": " + stepResult;
                results.add(result);
            }
            //检查是否超出
            if(currentStep >= maxSteps){
                this.state = AgentState.FINISHED;
                results.add("Terminated: Reached max steps (" + maxSteps + ")");
            }
            return String.join("\n" ,results);

        }catch (Exception e){
            state = AgentState.ERROR;
            log.error("Error running agent",e);
            return "执行错误" + e.getMessage();
        }finally {
            this.cleanup();
        }

    }

    /**
     * 定义单个步骤
     * @return
     */
    public abstract String step();

    /**
     * 清理资源
     */
    protected void cleanup(){
        //子类可以重写此方法调用
    }

}
