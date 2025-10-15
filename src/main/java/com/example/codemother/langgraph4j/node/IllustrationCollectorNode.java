package com.example.codemother.langgraph4j.node;

import com.example.codemother.langgraph4j.model.ImageCollectionPlan;
import com.example.codemother.langgraph4j.state.ImageResource;
import com.example.codemother.langgraph4j.state.WorkflowContext;
import com.example.codemother.langgraph4j.tools.UndrawIllustrationTool;
import com.example.codemother.config.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.ArrayList;
import java.util.List;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class IllustrationCollectorNode {

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            ImageCollectionPlan plan = context.getImageCollectionPlan();
            List<ImageResource> illustrations = new ArrayList<>();
            try {
                if (plan == null || plan.getIllustrationTasks() == null || plan.getIllustrationTasks().isEmpty()) {
                    log.warn("无插画收集任务，跳过插画收集节点");
                } else {
                    UndrawIllustrationTool illustrationTool = SpringContextUtil.getBean(UndrawIllustrationTool.class);
                    for (ImageCollectionPlan.IllustrationTask task : plan.getIllustrationTasks()) {
                        List<ImageResource> images = illustrationTool.searchIllustrations(task.query());
                        if (images != null) {
                            illustrations.addAll(images);
                        }
                    }
                    log.info("已收集到插画 {} 张", illustrations.size());
                }
                context.setCurrentStep("插画收集");
            } catch (Exception e) {
                log.error("插画收集失败: {}", e.getMessage(), e);
            }

            context.setIllustrations(illustrations);
            return WorkflowContext.saveContext(context);
        });
    }
}