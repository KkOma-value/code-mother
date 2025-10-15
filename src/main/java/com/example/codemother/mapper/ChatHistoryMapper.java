package com.example.codemother.mapper;

import com.example.codemother.model.dto.ChatHistory;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 对话历史 映射层。
 */
@Mapper
public interface ChatHistoryMapper extends BaseMapper<ChatHistory> {

}
