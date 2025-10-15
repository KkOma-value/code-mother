package com.example.codemother.langgraph4j.node;

import com.example.codemother.langgraph4j.model.ImageCollectionPlan;
import com.example.codemother.langgraph4j.state.ImageResource;
import com.example.codemother.langgraph4j.state.WorkflowContext;
import com.example.codemother.langgraph4j.tools.ImageSearchTool;
import com.example.codemother.langgraph4j.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.ArrayList;
import java.util.List;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class ContentImageCollectorNode {

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            ImageCollectionPlan plan = context.getImageCollectionPlan();
            List<ImageResource> contentImages = new ArrayList<>();
            try {
                if (plan == null || plan.getContentImageTasks() == null || plan.getContentImageTasks().isEmpty()) {
                    log.warn("无内容图片收集任务，跳过内容图片收集节点");
                } else {
                    ImageSearchTool searchTool = SpringContextUtil.getBean(ImageSearchTool.class);
                    for (ImageCollectionPlan.ImageSearchTask task : plan.getContentImageTasks()) {
                        List<ImageResource> images = searchTool.searchContentImages(task.query());
                        if (images != null) {
                            contentImages.addAll(images);
                        }
                    }
                    log.info("已收集到内容图片 {} 张", contentImages.size());
                }
                context.setCurrentStep("内容图片收集");
            } catch (Exception e) {
                log.error("内容图片收集失败: {}", e.getMessage(), e);
            }

            context.setContentImages(contentImages);
            return WorkflowContext.saveContext(context);
        });
    }
}