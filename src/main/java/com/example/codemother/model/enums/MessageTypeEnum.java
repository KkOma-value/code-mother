package com.example.codemother.model.enums;

import lombok.Getter;

@Getter
public enum MessageTypeEnum {
    USER("用户消息", "user"),
    AI("AI消息", "ai"),
    ERROR("错误消息", "error");

    private final String text;
    private final String value;

    MessageTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static MessageTypeEnum getEnumByValue(String value) {
        if (value == null) {
            return null;
        }
        for (MessageTypeEnum anEnum : MessageTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}