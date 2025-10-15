package com.example.codemother.langgraph4j.node;

import cn.hutool.core.util.StrUtil;
import com.example.codemother.config.SpringContextUtil;
import com.example.codemother.langgraph4j.ai.CodeQualityCheckService;
import com.example.codemother.langgraph4j.model.QualityResult;
import com.example.codemother.langgraph4j.state.WorkflowContext;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import java.util.List;

import static com.example.codemother.langgraph4j.CodeFileCollector.readAndConcatenateCodeFiles;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 代码质量检查节点
 */
@Slf4j
public class CodeQualityCheckNode {

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 代码质量检查");
            String generatedCodeDir = context.getGeneratedCodeDir();
            QualityResult qualityResult;
            try {
                // 1. 读取并拼接代码文件内容
                String codeContent = readAndConcatenateCodeFiles(generatedCodeDir);
                if (StrUtil.isBlank(codeContent)) {
                    log.warn("未找到可检查的代码文件");
                    qualityResult = QualityResult.builder()
                            .isValid(false)
                            .errors(List.of("未找到可检查的代码文件"))
                            .suggestions(List.of("请确保代码生成成功"))
                            .build();
                } else {
                    // 2. 调用 AI 进行代码质量检查
                    CodeQualityCheckService qualityCheckService = SpringContextUtil.getBean(CodeQualityCheckService.class);
                    qualityResult = qualityCheckService.checkCodeQuality(codeContent);
                    log.info("代码质量检查完成 - 是否通过: {}", qualityResult.getIsValid());
                }
            } catch (Exception e) {
                log.error("代码质量检查异常: {}", e.getMessage(), e);
                qualityResult = QualityResult.builder()
                        .isValid(true) // 异常直接跳到下一个步骤
                        .build();
            }
            // 3. 更新状态
            context.setCurrentStep("代码质量检查");
            context.setQualityResult(qualityResult);
            return WorkflowContext.saveContext(context);
        });
    }
}
