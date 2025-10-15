package com.example.codemother.common;

import cn.hutool.json.JSONUtil;
import com.example.codemother.exception.BusinessException;
import com.example.codemother.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.Map;

@Hidden
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        // 尝试处理 SSE 请求
        if (handleSseError(e.getCode(), e.getMessage())) {
            return null;
        }
        // 普通请求返回标准 JSON
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        // 尝试处理 SSE 请求
        if (handleSseError(ErrorCode.SYSTEM_ERROR.getCode(), "系统错误")) {
            return null;
        }
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }

    /**
     * 处理 SSE 请求的错误响应
     * @param errorCode 错误码
     * @param errorMessage 错误信息
     * @return true 表示是 SSE 请求并已处理；false 表示不是 SSE 请求
     */
    private boolean handleSseError(int errorCode, String errorMessage) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return false;
        }
        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();
        if (response == null) {
            return false;
        }
        // 判断是否是 SSE 请求（通过 Accept 头或特定路径）
        String accept = request.getHeader("Accept");
        String uri = request.getRequestURI();
        boolean isSse = (accept != null && accept.contains("text/event-stream")) ||
                (uri != null && uri.contains("/app/chat/gen/code"));
        if (!isSse) {
            return false;
        }
        try {
            // 设置 SSE 响应头
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/event-stream");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Connection", "keep-alive");

            // 构造错误消息
            Map<String, Object> errorData = Map.of(
                    "error", true,
                    "code", errorCode,
                    "message", errorMessage
            );
            String errorJson = JSONUtil.toJsonStr(errorData);

            // 发送业务错误事件
            String sseError = "event: business-error\n" +
                    "data: " + errorJson + "\n\n";
            response.getWriter().write(sseError);
            response.getWriter().flush();

            // 发送结束事件
            String sseDone = "event: done\n" +
                    "data: {}\n\n";
            response.getWriter().write(sseDone);
            response.getWriter().flush();
            return true;
        } catch (IOException ioException) {
            log.error("Failed to write SSE error response", ioException);
            return true; // 仍视为 SSE 请求，避免再次返回 JSON
        }
    }
}
