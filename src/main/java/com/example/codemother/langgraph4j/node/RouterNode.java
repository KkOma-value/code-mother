package com.example.codemother.langgraph4j.node;

import com.example.codemother.ai.AiCodeGenTypeRoutingService;
import com.example.codemother.ai.AiCodeGenTypeRoutingServiceFactory;
import com.example.codemother.langgraph4j.state.WorkflowContext;
import com.example.codemother.config.SpringContextUtil;
import com.example.codemother.model.enums.CodeGenTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class RouterNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 智能路由");

            // 从上下文获取用户原始需求
            String originalPrompt = context.getOriginalPrompt();
            CodeGenTypeEnum generationType;
            try {
                // 获取工厂并创建新的路由服务实例（prototype，支持并发）
                AiCodeGenTypeRoutingServiceFactory factory = SpringContextUtil.getBean(AiCodeGenTypeRoutingServiceFactory.class);
                AiCodeGenTypeRoutingService routingService = factory.createAiCodeGenTypeRoutingService();
                generationType = routingService.routeCodeGenType(originalPrompt);
            } catch (Exception e) {
                log.error("智能路由失败，回退为 HTML。原因: {}", e.getMessage(), e);
                generationType = CodeGenTypeEnum.HTML;
            }

            // 更新状态
            context.setCurrentStep("智能路由");
            context.setGenerationType(generationType);
            log.info("路由决策完成，选择类型: {}", generationType.getText());
            return WorkflowContext.saveContext(context);
        });
    }
}
