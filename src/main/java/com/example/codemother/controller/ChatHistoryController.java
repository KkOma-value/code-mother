package com.example.codemother.controller;

import com.example.codemother.annotation.AuthCheck;
import com.example.codemother.common.BaseResponse;
import com.example.codemother.common.ResultUtils;
import com.example.codemother.constant.UserConstant;
import com.example.codemother.exception.BusinessException;
import com.example.codemother.exception.ErrorCode;
import com.example.codemother.exception.ThrowUtils;
import com.example.codemother.model.dto.ChatHistory;
import com.example.codemother.model.dto.chat.ChatHistoryQueryRequest;
import com.example.codemother.model.entity.App;
import com.example.codemother.model.entity.User;
import com.example.codemother.model.vo.ChatHistoryVO;
import com.example.codemother.service.AppService;
import com.example.codemother.service.ChatHistoryService;
import com.example.codemother.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 对话历史 控制层。
 */
@RestController
@RequestMapping("/chat/history")
public class ChatHistoryController {

    @Autowired
    private ChatHistoryService chatHistoryService;

    @Autowired
    private UserService userService;

    @Autowired
    private AppService appService;

    /**
     * 分页获取某应用的对话历史（仅应用创建者或管理员可见）
     * 默认每次拉取10条，按时间降序（最新在前），前端可通过递增 pageNum 继续向前加载。
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<ChatHistoryVO>> listByApp(@RequestBody ChatHistoryQueryRequest query, HttpServletRequest request) {
        ThrowUtils.throwIf(query == null || query.getAppId() == null || query.getAppId() <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        App app = appService.getById(query.getAppId());
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        // 权限：仅创建者或管理员
        if (!app.getUserId().equals(loginUser.getId()) && !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        long pageNum = query.getPageNum();
        long pageSize = query.getPageSize();
        if (pageSize <= 0) {
            pageSize = 10;
        }
        Page<ChatHistoryVO> page = chatHistoryService.listByAppIdPage(query.getAppId(), pageNum, pageSize);
        return ResultUtils.success(page);
    }

    /**
     * 管理员分页查看所有应用的对话历史（按时间降序）
     */
    @PostMapping("/admin/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ChatHistoryVO>> adminList(@RequestBody ChatHistoryQueryRequest query) {
        ThrowUtils.throwIf(query == null, ErrorCode.PARAMS_ERROR);
        long pageNum = query.getPageNum();
        long pageSize = query.getPageSize();
        if (pageSize <= 0) {
            pageSize = 10;
        }
        Page<ChatHistoryVO> page = chatHistoryService.adminListPage(pageNum, pageSize);
        return ResultUtils.success(page);
    }

        /**
     * 分页查询某个应用的对话历史（游标查询）
     *
     * @param appId          应用ID
     * @param pageSize       页面大小
     * @param lastCreateTime 最后一条记录的创建时间
     * @param request        请求
     * @return 对话历史分页
     */
    @GetMapping("/app/{appId}")
    public BaseResponse<Page<ChatHistoryVO>> listAppChatHistory(@PathVariable Long appId,
                                                                  @RequestParam(defaultValue = "10") int pageSize,
                                                                  @RequestParam(required = false) LocalDateTime lastCreateTime,
                                                                  HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        // 改用功能相同、名称统一的 listByAppIdPage
        long pageNum = 1;                       // 游标模式默认取第一页
        Page<ChatHistoryVO> result = chatHistoryService.listByAppIdPage(appId, pageNum, pageSize);
        return ResultUtils.success(result);
    }

        /**
     * 管理员分页查询所有对话历史
     *
     * @param chatHistoryQueryRequest 查询请求
     * @return 对话历史分页
     */
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ChatHistory>> listAllChatHistoryByPageForAdmin(@RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest) {
        ThrowUtils.throwIf(chatHistoryQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = chatHistoryQueryRequest.getPageNum();
        long pageSize = chatHistoryQueryRequest.getPageSize();
        // 查询数据
        QueryWrapper queryWrapper = chatHistoryService.getQueryWrapper(chatHistoryQueryRequest);
        Page<ChatHistory> result = chatHistoryService.page(Page.of(pageNum, pageSize), queryWrapper);
        return ResultUtils.success(result);
    }


}
