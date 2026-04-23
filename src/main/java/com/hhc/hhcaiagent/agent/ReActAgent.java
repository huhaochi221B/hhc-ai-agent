package com.hhc.hhcaiagent.agent;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * ReAct （Reasoning and Acting） 模式的代理抽象类
 * 实现了思考-行动的循环模式
 */
@Data
@Slf4j
@EqualsAndHashCode(callSuper = true)
public abstract class ReActAgent extends BaseAgent {
    /**
     * 处理当前状态并决定下一步行动
     * @return true表示下一步需要行动
     */
    public abstract boolean think();

    /**
     * 执行决定的行动
     * @return
     */
    public abstract String act();

    @Override
    public String step() {
        try {
            //先思考
            boolean shouldAct = think();
            if(!shouldAct){
                return "思考完成 - 无需行动";
            }
            //再行动
            return act();
        } catch (Exception e){
            //记录异常日志
            e.printStackTrace();
            return "步骤执行失败：" + e.getMessage();
        }
    }

}
