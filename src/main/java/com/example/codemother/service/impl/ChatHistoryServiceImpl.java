package com.example.codemother.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.example.codemother.exception.ErrorCode;
import com.example.codemother.exception.ThrowUtils;
import com.example.codemother.mapper.ChatHistoryMapper;
import com.example.codemother.model.dto.ChatHistory;
import com.example.codemother.model.dto.chat.ChatHistoryQueryRequest;
import com.example.codemother.model.enums.MessageTypeEnum;
import com.example.codemother.model.vo.ChatHistoryVO;
import com.example.codemother.service.AppService;
import com.example.codemother.service.ChatHistoryService;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;

import cn.hutool.core.util.StrUtil;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 对话历史 服务层实现。
 */
@Service
@Slf4j
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    @Autowired
    @Lazy
    private AppService appService;

    @Autowired
    private ChatHistoryMapper chatHistoryMapper;


    @Override
    public void saveUserMessage(Long appId, Long userId, String message) {
        addChatMessage(appId, message, MessageTypeEnum.USER.name(), userId);
    }

    @Override
    public void saveAiMessage(Long appId, Long userId, String message) {
        addChatMessage(appId, message, MessageTypeEnum.AI.name(), userId);
    }

    @Override
    public void saveErrorMessage(Long appId, Long userId, String errorMessage) {
        addChatMessage(appId, errorMessage, MessageTypeEnum.ERROR.name(), userId);
    }

    @Override
    public boolean addChatMessage(Long appId, String message, String messageType, Long userId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "消息内容不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(messageType), ErrorCode.PARAMS_ERROR, "消息类型不能为空");
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        // 验证消息类型是否有效
        MessageTypeEnum messageTypeEnum = MessageTypeEnum.getEnumByValue(messageType);
        ThrowUtils.throwIf(messageTypeEnum == null, ErrorCode.PARAMS_ERROR, "不支持的消息类型: " + messageType);
        ChatHistory chatHistory = ChatHistory.builder()
                .appId(appId)
                .message(message)
                .messageType(messageType)
                .userId(userId)
                .build();
        return this.save(chatHistory);
    }



    @Override
    public Page<ChatHistoryVO> listByAppIdPage(Long appId, long pageNum, long pageSize) {
        QueryWrapper query = QueryWrapper.create()
                .eq("appId", appId)
                .orderBy("createTime", false); // false = DESC
        Page<ChatHistory> page = this.page(Page.of(pageNum, pageSize), query);
        return toVOPage(page);
    }

    @Override
    public Page<ChatHistoryVO> adminListPage(long pageNum, long pageSize) {
        QueryWrapper query = QueryWrapper.create()
                .orderBy("createTime", false);
        Page<ChatHistory> page = this.page(Page.of(pageNum, pageSize), query);
        return toVOPage(page);
    }

    @Override
    public boolean deleteByAppId(Long appId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("appId", appId);
        return this.remove(queryWrapper);
    }


    private Page<ChatHistoryVO> toVOPage(Page<ChatHistory> entityPage) {
        Page<ChatHistoryVO> voPage = new Page<>(entityPage.getPageNumber(), entityPage.getPageSize(), entityPage.getTotalRow());
        List<ChatHistoryVO> vos = entityPage.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        voPage.setRecords(vos);
        return voPage;
    }

    private ChatHistoryVO toVO(ChatHistory entity) {
        ChatHistoryVO vo = new ChatHistoryVO();
        vo.setId(entity.getId());
        vo.setMessage(entity.getMessage());
        vo.setMessageType(entity.getMessageType());
        vo.setAppId(entity.getAppId());
        vo.setUserId(entity.getUserId());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    
        /**
     * 获取查询包装类
     *
     * @param chatHistoryQueryRequest
     * @return
     */
    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (chatHistoryQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chatHistoryQueryRequest.getId();
        String message = chatHistoryQueryRequest.getMessage();
        String messageType = chatHistoryQueryRequest.getMessageType();
        Long appId = chatHistoryQueryRequest.getAppId();
        Long userId = chatHistoryQueryRequest.getUserId();
        LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
        String sortField = chatHistoryQueryRequest.getSortField();
        String sortOrder = chatHistoryQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq("id", id)
                .like("message", message)
                .eq("messageType", messageType)
                .eq("appId", appId)
                .eq("userId", userId);
        // 游标查询逻辑 - 只使用 createTime 作为游标
        if (lastCreateTime != null) {
            queryWrapper.lt("createTime", lastCreateTime);
        }
        // 排序
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            // 默认按创建时间降序排列
            queryWrapper.orderBy("createTime", false);
        }
        return queryWrapper;
    }


    @Override
    public int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount) {
        try {
            // 直接构造查询条件，起始点为 1 而不是 0，用于排除最新的用户消息
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .eq(ChatHistory::getAppId, appId)
                    .orderBy(ChatHistory::getCreateTime, false)
                    .limit(1, maxCount);
            List<ChatHistory> historyList = this.list(queryWrapper);
            if (CollUtil.isEmpty(historyList)) {
                return 0;
            }
            // 反转列表，确保按时间正序（老的在前，新的在后）
            historyList = historyList.reversed();
            // 按时间顺序添加到记忆中
            int loadedCount = 0;
            // 先清理历史缓存，防止重复加载
            chatMemory.clear();
            for (ChatHistory history : historyList) {
                if (MessageTypeEnum.USER.getValue().equals(history.getMessageType())) {
                    chatMemory.add(UserMessage.from(history.getMessage()));
                    loadedCount++;
                } else if (MessageTypeEnum.AI.getValue().equals(history.getMessageType())) {
                    chatMemory.add(AiMessage.from(history.getMessage()));
                    loadedCount++;
                }
            }
            log.info("成功为 appId: {} 加载了 {} 条历史对话", appId, loadedCount);
            return loadedCount;
        } catch (Exception e) {
            log.error("加载历史对话失败，appId: {}, error: {}", appId, e.getMessage(), e);
            // 加载失败不影响系统运行，只是没有历史上下文
            return 0;
        }
    }

}
