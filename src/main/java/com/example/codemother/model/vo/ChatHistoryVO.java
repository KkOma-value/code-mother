package com.example.codemother.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 对话历史 VO。
 */
@Data
public class ChatHistoryVO {
    private Long id;
    private String message;
    private String messageType;
    private Long appId;
    private Long userId;
    private LocalDateTime createTime;
}