package com.example.codemother.service;

import com.example.codemother.model.dto.ChatHistory;
import com.example.codemother.model.dto.chat.ChatHistoryQueryRequest;
import com.example.codemother.model.vo.ChatHistoryVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

/**
 * 对话历史 服务层。
 *
 * @author kkoma
 * @since 1.1
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    void saveUserMessage(Long appId, Long userId, String message);

    void saveAiMessage(Long appId, Long userId, String message);

    void saveErrorMessage(Long appId, Long userId, String errorMessage);

    Page<ChatHistoryVO> listByAppIdPage(Long appId, long pageNum, long pageSize);

    Page<ChatHistoryVO> adminListPage(long pageNum, long pageSize);

    boolean deleteByAppId(Long appId);

    boolean addChatMessage(Long appId, String message, String messageType, Long userId);

    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);
}
