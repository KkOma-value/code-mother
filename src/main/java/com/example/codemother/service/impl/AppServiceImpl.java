package com.example.codemother.service.impl;

import cn.hutool.core.bean.BeanUtil;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.example.codemother.ai.AiCodeGenTypeRoutingService;
import com.example.codemother.core.builder.VueProjectBuilder;
import com.example.codemother.core.handler.StreamHandlerExecutor;
import com.example.codemother.core.handler.SimpleTextStreamHandler;
import com.example.codemother.langgraph4j.CodeGenWorkflow;
import com.example.codemother.model.dto.App.AppAddRequest;
import com.example.codemother.service.ScreenshotService;
import lombok.extern.slf4j.Slf4j;

import com.example.codemother.constant.AppConstant;
import com.example.codemother.core.AiCodeGeneratorFacade;
import com.example.codemother.exception.BusinessException;
import com.example.codemother.exception.ErrorCode;
import com.example.codemother.exception.ThrowUtils;
import com.example.codemother.model.dto.App.AppQueryRequest;
import com.example.codemother.model.entity.User;
import com.example.codemother.model.enums.CodeGenTypeEnum;
import com.example.codemother.model.enums.MessageTypeEnum;
import com.example.codemother.model.vo.AppVO;
import com.example.codemother.model.vo.UserVO;
import com.example.codemother.service.AppService;
import com.example.codemother.service.ChatHistoryService;
import com.example.codemother.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.example.codemother.model.entity.App;
import com.example.codemother.mapper.AppMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author kkoma
 * @since 1.1
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {


    @Autowired
    private ChatHistoryService chatHistoryService;

    @Autowired
    private UserService userService;

    @Autowired
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Autowired
    private StreamHandlerExecutor streamHandlerExecutor;

    @Autowired
    private VueProjectBuilder vueProjectBuilder;

    @Autowired
    private ScreenshotService screenshotService;

    @Autowired
    private AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService;
    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        // 关联查询用户信息
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        return appVO;
    }

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        return QueryWrapper.create().eq("id", id).like("appName", appName).like("cover", cover).like("initPrompt", initPrompt).eq("codeGenType", codeGenType).eq("deployKey", deployKey).eq("priority", priority).eq("userId", userId).orderBy(sortField, "ascend".equals(sortOrder));
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 批量获取用户信息，避免 N+1 查询问题
        Set<Long> userIds = appList.stream().map(App::getUserId).collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream().collect(Collectors.toMap(User::getId, userService::getUserVO));
        return appList.stream().map(app -> {
            AppVO appVO = getAppVO(app);
            UserVO userVO = userVOMap.get(app.getUserId());
            appVO.setUser(userVO);
            return appVO;
        }).collect(Collectors.toList());
    }

    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 验证用户是否有权限访问该应用，仅本人可以生成代码
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");
        }
        String codeGenTypeStr = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenTypeStr);
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        }
        // 5. 通过校验后，添加用户消息到对话历史
        chatHistoryService.addChatMessage(appId, message, MessageTypeEnum.USER.getValue(), loginUser.getId());
        // 6. 调用 AI 生成代码（流式）
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId);
        // 7. 收集 AI 响应内容并在完成后记录到对话历史
        return streamHandlerExecutor.doExecute(codeStream, chatHistoryService, appId, loginUser, codeGenTypeEnum);

    }


    @Override
    public String deployApp(Long appId, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 验证用户是否有权限部署该应用，仅本人可以部署
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限部署该应用");
        }
        // 4. 检查是否已有 deployKey
        String deployKey = app.getDeployKey();
        // 没有则生成 6 位 deployKey（大小写字母 + 数字）
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }
        // 5. 获取代码生成类型，构建源目录路径
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        // 6. 检查源目录是否存在
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用代码不存在，请先生成代码");
        }
        // 7. Vue 项目特殊处理：执行构建
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if (codeGenTypeEnum == CodeGenTypeEnum.VUE_PROJECT) {
            // Vue 项目需要构建
            boolean buildSuccess = vueProjectBuilder.buildProject(sourceDirPath);
            ThrowUtils.throwIf(!buildSuccess, ErrorCode.SYSTEM_ERROR, "Vue 项目构建失败，请检查代码和依赖");
            // 检查 dist 目录是否存在
            File distDir = new File(sourceDirPath, "dist");
            ThrowUtils.throwIf(!distDir.exists(), ErrorCode.SYSTEM_ERROR, "Vue 项目构建完成但未生成 dist 目录");
            // 将 dist 目录作为部署源
            sourceDir = distDir;
            log.info("Vue 项目构建成功，将部署 dist 目录: {}", distDir.getAbsolutePath());
        }
        // 8. 复制文件到部署目录
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;

        // 9. 返回可访问的 URL
        // return String.format("%s/%s/", AppConstant.CODE_DEPLOY_HOST, deployKey);

        // 10. 构建应用访问 URL
        String appDeployUrl = String.format("%s/%s/", AppConstant.CODE_DEPLOY_HOST, deployKey);
        // 11. 异步生成截图并更新应用封面
        generateAppScreenshotAsync(appId, appDeployUrl);
        return appDeployUrl;

    }

    /**
     * 删除应用时关联删除对话历史
     *
     * @param id 应用ID
     * @return 是否成功
     */
    @Override
    public boolean removeById(Serializable id) {
        if (id == null) {
            return false;
        }
        // 转换为 Long 类型
        Long appId = Long.valueOf(id.toString());
        if (appId <= 0) {
            return false;
        }
        // 先删除关联的对话历史
        try {
            chatHistoryService.deleteByAppId(appId);
        } catch (Exception e) {
            // 记录日志但不阻止应用删除
            log.error("删除应用关联对话历史失败: {}", e.getMessage());
        }
        // 删除应用
        return super.removeById(id);
    }


    /**
     * 异步生成应用截图并更新封面
     *
     * @param appId  应用ID
     * @param appUrl 应用访问URL
     */
    @Override
    public void generateAppScreenshotAsync(Long appId, String appUrl) {
        // 使用虚拟线程异步执行
        Thread.startVirtualThread(() -> {
            // 调用截图服务生成截图并上传
            String screenshotUrl = screenshotService.generateAndUploadScreenshot(appUrl);
            // 更新应用封面字段
            App updateApp = new App();
            updateApp.setId(appId);
            updateApp.setCover(screenshotUrl);
            boolean updated = this.updateById(updateApp);
            ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "更新应用封面字段失败");
        });
    }



    @Override
    public Long createApp(AppAddRequest appAddRequest, User loginUser) {
        // 参数校验
        String initPrompt = appAddRequest.getInitPrompt();
        ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "初始化 prompt 不能为空");
        // 构造入库对象
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        app.setUserId(loginUser.getId());
        // 应用名称暂时为 initPrompt 前 12 位
        app.setAppName(initPrompt.substring(0, Math.min(initPrompt.length(), 12)));
        // 使用 AI 智能选择代码生成类型
        CodeGenTypeEnum selectedCodeGenType = aiCodeGenTypeRoutingService.routeCodeGenType(initPrompt);
        app.setCodeGenType(selectedCodeGenType.getValue());
        // 插入数据库
        boolean result = this.save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        log.info("应用创建成功，ID: {}, 类型: {}", app.getId(), selectedCodeGenType.getValue());
        return app.getId();
    }


    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser, boolean agent) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 验证用户是否有权限访问该应用，仅本人可以生成代码
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");
        }
        String codeGenTypeStr = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenTypeStr);
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        }
        // 4. 通过校验后，添加用户消息到对话历史
        chatHistoryService.addChatMessage(appId, message, MessageTypeEnum.USER.getValue(), loginUser.getId());

        // 5. 根据 agent 参数选择生成方式
        Flux<String> codeStream;
        if (agent) {
            // Agent 模式：使用工作流生成代码（Flux 流）
            codeStream = new CodeGenWorkflow().executeWorkflowWithFlux(message, appId);
            // 工作流输出为纯文本事件，使用简单文本流处理器统一保存历史
            return new SimpleTextStreamHandler().handle(codeStream, chatHistoryService, appId, loginUser);
        } else {
            // 传统模式：调用 AI 生成代码（流式）
            codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId);
            // 选择合适的处理器并保存历史
            return streamHandlerExecutor.doExecute(codeStream, chatHistoryService, appId, loginUser, codeGenTypeEnum);
        }
    }
}
