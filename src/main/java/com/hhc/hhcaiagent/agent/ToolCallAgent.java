package com.hhc.hhcaiagent.agent;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.dashscope.agent.DashScopeAgentOptions;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.hhc.hhcaiagent.agent.model.AgentState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 处理工具调用的基础代理类，具体实现了 think 和 act 方法，可以用作创建实例的父类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class ToolCallAgent extends ReActAgent {

    //可用工具
    private final ToolCallback[] availableTools;

    //保存工具调用信息的响应结果
    private ChatResponse toolCallChatResponse;

    //工具调用管理者
    private final ToolCallingManager toolCallingManager;

    //禁用SpringAI 内置的工具调用，自己维护选项和消息上下文
    private final ChatOptions chatOptions;

    public ToolCallAgent(ToolCallback[] availableTools){
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        //禁用SpringAI 内置的工具调用，自己维护选项和消息上下文
        this.chatOptions = DashScopeChatOptions.builder()
                .withProxyToolCalls(true)
                .build();
    }

    /**
     * 处理当前状态并决定下一步行动
     *
     * @return 是否需要行动
     */
    @Override
    public boolean think() {
        //校验提示词，拼接用户提示词
        if( StrUtil.isNotBlank(getNextStepPrompt())){
            UserMessage userMessage = new UserMessage(getNextStepPrompt());
            getMessageList().add(userMessage);
        }
        //调用 AI 大模型，获取工具调用列表
        List<Message> messageList = getMessageList();
        Prompt prompt = new Prompt(messageList,this.chatOptions);
        try {
            ChatResponse chatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .tools(availableTools)
                    .call()
                    .chatResponse();
            //记录响应， 用于等下 act
            this.toolCallChatResponse = chatResponse;
            //解析调用结果,获取所需要的工具
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            //获取要调用的工具列表
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();
            //输出提示信息
            String result = assistantMessage.getText();
            log.info(getName() + "的思考" + result);
            log.info(getName() + "选择了" + toolCallList.size() + "个工具");
            String toolCallInfo = toolCallList.stream()
                    .map(toolCall -> String.format("工具名称: %s ， 工具参数：%s", toolCall.name(), toolCall.arguments()))
                    .collect(Collectors.joining("\n"));
            log.info(toolCallInfo);
            //如果不需要调用工具，返回 false
            if(toolCallList.isEmpty()){
                //不需要调用工具，才需要手动记录助手消息
                getMessageList().add(assistantMessage);
                return false;
            }else {
                return true;
            }
        } catch (Exception e) {
            log.error(getName() + "的思考遇到问题：" + e.getMessage());
            getMessageList().add(new AssistantMessage( "处理时遇到错误：" + e.getMessage()));
            return false;
        }
        //异常处理
    }

    @Override
    public String act() {
        if(!toolCallChatResponse.hasToolCalls()){
            return "不需要调用工具";
        }
        Prompt prompt = new Prompt(getMessageList(),this.chatOptions);
        //调用工具
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
        //记录消息上下文, conversationHistory 已经包含了助手消息和工具调用的返回结果
        setMessageList(toolExecutionResult.conversationHistory());
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
        //判断是否调用了终止工具
        boolean terminateToolCalled = toolResponseMessage.getResponses().stream()
                .anyMatch(response -> response.name().equals("doterminate"));
        if(terminateToolCalled){
            //任务结束，更改状态
            setState(AgentState.FINISHED);
        }

        String results= toolResponseMessage.getResponses().stream()
                .map(response -> "工具" + response.name() + "返回的结果：" + response.responseData())
                .collect(Collectors.joining("\n"));


        log.info(results);
        return results;
    }
}
