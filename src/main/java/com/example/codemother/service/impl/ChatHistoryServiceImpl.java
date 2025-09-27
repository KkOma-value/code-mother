package com.example.codemother.service.impl;

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
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;

import cn.hutool.core.util.StrUtil;

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
public class ChatHistoryServiceImpl implements ChatHistoryService {

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

    @Override
    public BaseMapper<ChatHistory> getMapper() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMapper'");
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

}
