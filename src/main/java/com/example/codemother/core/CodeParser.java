package com.example.codemother.core;

import com.example.codemother.ai.model.HtmlCodeResult;
import com.example.codemother.ai.model.MultiFileCodeResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 代码解析器
 * 提供静态方法解析不同类型的代码内容
 *
 * @author kkoma
 */
@Deprecated
public class CodeParser {

    // 标准代码块格式模式
    private static final Pattern STANDARD_HTML_PATTERN = Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    private static final Pattern STANDARD_CSS_PATTERN = Pattern.compile("```css\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    private static final Pattern STANDARD_JS_PATTERN = Pattern.compile("```(?:js|javascript)\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    // 文字描述格式模式
    private static final Pattern TEXT_HTML_PATTERN = Pattern.compile(
            "(?:html|HTML)\\s*格式\\s*\\n([\\s\\S]*?)(?=```|\\n\\s*[a-zA-Z]+\\s*格式|\\Z)",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern TEXT_CSS_PATTERN = Pattern.compile(
            "(?:css|CSS)\\s*格式\\s*\\n([\\s\\S]*?)(?=```|\\n\\s*[a-zA-Z]+\\s*格式|\\Z)",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern TEXT_JS_PATTERN = Pattern.compile(
            "(?:js|JS|javascript|Javascript)\\s*格式\\s*\\n([\\s\\S]*?)(?=```|\\n\\s*[a-zA-Z]+\\s*格式|\\Z)",
            Pattern.CASE_INSENSITIVE
    );

    // 纯代码内容提取模式
    private static final Pattern PURE_HTML_PATTERN = Pattern.compile("(<!DOCTYPE html>[\\s\\S]*?</html>)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PURE_CSS_PATTERN = Pattern.compile("([a-zA-Z][^{]*\\{[^}]*\\})", Pattern.CASE_INSENSITIVE);
    private static final Pattern PURE_JS_PATTERN = Pattern.compile("(console\\.log[^;]*;|function[\\s\\S]*?\\})", Pattern.CASE_INSENSITIVE);

    /**
     * 解析 HTML 单文件代码
     */
    public static HtmlCodeResult parseHtmlCode(String codeContent) {
        HtmlCodeResult result = new HtmlCodeResult();
        // 使用多种模式提取HTML代码
        String htmlCode = extractWithMultiplePatterns(codeContent,
                STANDARD_HTML_PATTERN, TEXT_HTML_PATTERN, PURE_HTML_PATTERN);
        if (htmlCode != null && !htmlCode.trim().isEmpty()) {
            result.setHtmlCode(htmlCode.trim());
        } else {
            // 如果没有找到代码块，将整个内容作为HTML
            result.setHtmlCode(codeContent.trim());
        }
        return result;
    }

    /**
     * 解析多文件代码（HTML + CSS + JS）
     */
    public static MultiFileCodeResult parseMultiFileCode(String codeContent) {
        MultiFileCodeResult result = new MultiFileCodeResult();
        // 使用多种模式提取各类代码
        String htmlCode = extractWithMultiplePatterns(codeContent,
                STANDARD_HTML_PATTERN, TEXT_HTML_PATTERN, PURE_HTML_PATTERN);
        String cssCode = extractWithMultiplePatterns(codeContent,
                STANDARD_CSS_PATTERN, TEXT_CSS_PATTERN, PURE_CSS_PATTERN);
        String jsCode = extractWithMultiplePatterns(codeContent,
                STANDARD_JS_PATTERN, TEXT_JS_PATTERN, PURE_JS_PATTERN);
        // 设置HTML代码
        if (htmlCode != null && !htmlCode.trim().isEmpty()) {
            result.setHtmlCode(htmlCode.trim());
        }
        // 设置CSS代码
        if (cssCode != null && !cssCode.trim().isEmpty()) {
            result.setCssCode(cssCode.trim());
        }
        // 设置JS代码
        if (jsCode != null && !jsCode.trim().isEmpty()) {
            result.setJsCode(jsCode.trim());
        }
        return result;
    }

    /**
     * 使用多种模式提取代码
     */
    private static String extractWithMultiplePatterns(String content, Pattern... patterns) {
        for (Pattern pattern : patterns) {
            String result = extractCodeByPattern(content, pattern);
            if (result != null && !result.trim().isEmpty()) {
                return result;
            }
        }
        return null;
    }

    /**
     * 根据正则模式提取代码
     */
    private static String extractCodeByPattern(String content, Pattern pattern) {
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            // 返回第一个非空的匹配组
            for (int i = 1; i <= matcher.groupCount(); i++) {
                String group = matcher.group(i);
                if (group != null && !group.trim().isEmpty()) {
                    return group.trim();
                }
            }
        }
        return null;
    }
}