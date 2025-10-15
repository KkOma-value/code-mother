package com.example.codemother.langgraph4j.node;

import com.example.codemother.core.AiCodeGeneratorFacade;
import com.example.codemother.langgraph4j.model.QualityResult;
import com.example.codemother.langgraph4j.state.WorkflowContext;
import com.example.codemother.langgraph4j.util.SpringContextUtil;
import com.example.codemother.model.enums.CodeGenTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.io.File;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class CodeGeneratorNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 代码生成");

            // 1. 构造用户消息（若质检失败则覆盖为错误修复提示）
            String userMessage = buildUserMessage(context);

            // 2. 获取生成类型（为空则回退到 HTML）
            CodeGenTypeEnum generationType = context.getGenerationType();
            if (generationType == null) {
                generationType = CodeGenTypeEnum.HTML;
            }

            // 3. 调用生成并保存代码
            File savedDir;
            try {
                AiCodeGeneratorFacade facade = SpringContextUtil.getBean(AiCodeGeneratorFacade.class);
                // 从上下文获取 appId（兼容空值，回退为 0L）
                Long appId = context.getAppId() != null ? context.getAppId() : 0L;
                savedDir = facade.generateAndSaveCode(userMessage, generationType, appId);
                log.info("代码生成完成，目录: {}", savedDir.getAbsolutePath());
            } catch (Exception e) {
                log.error("代码生成异常: {}", e.getMessage(), e);
                savedDir = new File("");
            }

            // 4. 更新状态
            context.setCurrentStep("代码生成");
            context.setGeneratedCodeDir(savedDir.getAbsolutePath());
            return WorkflowContext.saveContext(context);
        });
    }

    /**
     * 构造用户消息，如果存在质检失败结果则添加错误修复信息
     */
    private static String buildUserMessage(WorkflowContext context) {
        String userMessage = context.getEnhancedPrompt();
        // 检查是否存在质检失败结果
        QualityResult qualityResult = context.getQualityResult();
        if (isQualityCheckFailed(qualityResult)) {
            // 直接将错误修复信息作为新的提示词（起到了修改的作用）
            userMessage = buildErrorFixPrompt(qualityResult);
        }
        return userMessage;
    }

    /**
     * 判断质检是否失败
     */
    private static boolean isQualityCheckFailed(QualityResult qualityResult) {
        return qualityResult != null &&
                !qualityResult.getIsValid() &&
                qualityResult.getErrors() != null &&
                !qualityResult.getErrors().isEmpty();
    }

    /**
     * 构造错误修复提示词
     */
    private static String buildErrorFixPrompt(QualityResult qualityResult) {
        StringBuilder errorInfo = new StringBuilder();
        errorInfo.append("\n\n## 上次生成的代码存在以下问题，请修复：\n");
        // 添加错误列表
        qualityResult.getErrors().forEach(error ->
                errorInfo.append("- ").append(error).append("\n"));
        // 添加修复建议（如果有）
        if (qualityResult.getSuggestions() != null && !qualityResult.getSuggestions().isEmpty()) {
            errorInfo.append("\n## 修复建议：\n");
            qualityResult.getSuggestions().forEach(suggestion ->
                    errorInfo.append("- ").append(suggestion).append("\n"));
        }
        errorInfo.append("\n请根据上述问题和建议重新生成代码，确保修复所有提到的问题。");
        return errorInfo.toString();
    }
}
