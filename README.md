# Code Mother - 智能代码生成平台

## 项目概述

Code Mother 是一个基于大语言模型（LLM）的智能代码生成平台，旨在通过 AI 技术帮助开发者快速生成高质量的前端项目代码。该平台集成了多种 AI 模型，支持流式对话生成、工具调用、项目构建部署等功能，为开发者提供从需求描述到项目部署的一站式解决方案。

## 解决的核心问题

### 1. 开发效率问题
- **传统痛点**：从零开始创建前端项目需要大量重复性工作，包括项目结构搭建、组件开发、样式设计等
- **解决方案**：通过自然语言描述需求，AI 自动生成完整的 Vue3 项目，包括组件、路由、样式等

### 2. 技术门槛问题
- **传统痛点**：初学者难以快速掌握现代前端开发技术栈和最佳实践
- **解决方案**：AI 生成的代码遵循最佳实践，自动集成现代开发工具链，降低学习成本

### 3. 项目部署复杂性
- **传统痛点**：项目构建、部署流程复杂，需要配置多种工具和环境
- **解决方案**：自动化构建部署流程，一键生成可访问的在线应用

### 4. 代码质量一致性
- **传统痛点**：不同开发者编写的代码风格和质量参差不齐
- **解决方案**：AI 生成的代码遵循统一的编码规范和架构模式

## 核心功能特性

### 🤖 智能代码生成
- **多模型支持**：集成 OpenAI GPT、DeepSeek、DashScope 等多种 AI 模型
- **智能路由**：根据任务复杂度自动选择最适合的 AI 模型
- **流式生成**：实时展示代码生成过程，提供良好的用户体验
- **工具调用**：AI 可以调用文件读写、目录操作等工具，实现复杂的代码生成任务

### 🛠️ 工具生态系统
- **FileWriteTool**：支持 AI 自动创建和写入项目文件
- **FileReadTool**：允许 AI 读取现有文件内容进行分析和修改
- **FileDirReadTool**：提供项目目录结构查看功能
- **ToolManager**：统一管理所有工具，支持动态注册和调用

### 🏗️ 项目构建与部署
- **VueProjectBuilder**：自动执行 npm install 和 npm run build
- **自动化部署**：构建完成后自动部署到可访问的 Web 环境
- **截图服务**：自动生成应用预览截图，支持对象存储上传

### 📊 项目管理
- **应用生命周期管理**：创建、更新、删除、查询应用
- **用户权限控制**：基于用户角色的访问控制
- **聊天历史记录**：保存用户与 AI 的对话历史
- **缓存优化**：Redis 缓存提升查询性能

### 🔒 安全与限流
- **生产环境保护**：`@Profile("prod")` 注解控制生产特性
- **多维度限流**：支持用户、IP、API 维度的访问限制
- **权限验证**：管理员功能需要特殊权限验证

## 技术架构

### 后端技术栈
- **Java 21** + **Spring Boot 3.x**：现代化的 Java 开发框架
- **MyBatis-Flex**：灵活的 ORM 框架，支持动态 SQL
- **Redis**：缓存和会话存储
- **LangChain4j**：Java 版本的 LangChain，用于 AI 应用开发
- **Maven**：项目构建和依赖管理

### AI 集成技术
- **多模型适配**：OpenAI GPT-4、DeepSeek、阿里云 DashScope
- **流式处理**：Server-Sent Events (SSE) 实现实时响应
- **工具调用**：LangChain4j Tool 接口实现 AI 工具调用
- **智能路由**：根据任务类型自动选择最优模型

### 前端生成技术
- **Vue 3** + **TypeScript**：生成现代化的前端项目
- **Vite**：快速的构建工具
- **Element Plus**：UI 组件库
- **Vue Router**：路由管理
- **Pinia**：状态管理

### 部署与运维
- **Docker 支持**：容器化部署
- **Selenium WebDriver**：自动化截图服务
- **对象存储**：腾讯云 COS 集成
- **健康检查**：应用状态监控

## 项目结构详解

```
code-mother/
├── src/main/java/com/example/codemother/
│   ├── ai/                          # AI 相关模块
│   │   ├── factory/                 # AI 服务工厂
│   │   │   ├── AiCodeGeneratorServiceFactory.java      # 代码生成服务工厂
│   │   │   ├── AiCodeGenTypeRoutingServiceFactory.java # 类型路由服务工厂
│   │   │   └── ImageCollectionServiceFactory.java     # 图片收集服务工厂
│   │   ├── facade/                  # AI 门面层
│   │   │   └── AiCodeGeneratorFacade.java             # 代码生成统一入口
│   │   ├── model/                   # AI 消息模型
│   │   │   ├── StreamMessage.java                     # 流消息基类
│   │   │   ├── AiResponseMessage.java                 # AI 响应消息
│   │   │   ├── ToolRequestMessage.java                # 工具请求消息
│   │   │   └── ToolExecutedMessage.java               # 工具执行结果消息
│   │   └── service/                 # AI 服务接口
│   │       ├── AiCodeGeneratorService.java            # 代码生成服务接口
│   │       └── AiCodeGenTypeRoutingService.java       # 类型路由服务接口
│   ├── config/                      # 配置类
│   │   ├── ai/                      # AI 模型配置
│   │   │   ├── OpenAiChatModelConfig.java             # OpenAI 模型配置
│   │   │   ├── RoutingAiModelConfig.java              # 路由模型配置
│   │   │   └── DeepSeekChatModelConfig.java           # DeepSeek 模型配置
│   │   ├── RedisConfig.java         # Redis 配置
│   │   ├── RedissonConfig.java      # Redisson 配置
│   │   ├── CorsConfig.java          # 跨域配置
│   │   └── RedisCacheManagerConfig.java               # Redis 缓存管理配置
│   ├── controller/                  # 控制器层
│   │   ├── AppController.java       # 应用管理控制器
│   │   ├── UserController.java      # 用户管理控制器
│   │   └── ChatHistoryController.java                 # 聊天历史控制器
│   ├── service/                     # 服务层
│   │   ├── AppService.java          # 应用服务接口
│   │   ├── UserService.java         # 用户服务接口
│   │   ├── ChatHistoryService.java  # 聊天历史服务接口
│   │   ├── ScreenshotService.java   # 截图服务接口
│   │   └── impl/                    # 服务实现
│   │       ├── AppServiceImpl.java                    # 应用服务实现
│   │       ├── UserServiceImpl.java                   # 用户服务实现
│   │       └── ScreenshotServiceImpl.java             # 截图服务实现
│   ├── Tools/                       # 工具模块
│   │   ├── BaseTool.java            # 工具基类
│   │   ├── ToolManager.java         # 工具管理器
│   │   ├── FileWriteTool.java       # 文件写入工具
│   │   ├── FileReadTool.java        # 文件读取工具
│   │   └── FileDirReadTool.java     # 目录读取工具
│   ├── core/                        # 核心模块
│   │   ├── builder/                 # 构建器
│   │   │   └── VueProjectBuilder.java                 # Vue 项目构建器
│   │   ├── handler/                 # 处理器
│   │   │   ├── JsonMessageStreamHandler.java          # JSON 消息流处理器
│   │   │   └── StreamHandlerExecutor.java             # 流处理执行器
│   │   └── template/                # 模板
│   │       └── CodeFileSaverTemplate.java             # 代码文件保存模板
│   ├── model/                       # 数据模型
│   │   ├── entity/                  # 实体类
│   │   │   ├── App.java             # 应用实体
│   │   │   ├── User.java            # 用户实体
│   │   │   └── ChatHistory.java     # 聊天历史实体
│   │   ├── dto/                     # 数据传输对象
│   │   └── vo/                      # 视图对象
│   ├── utils/                       # 工具类
│   │   ├── WebScreenshotUtils.java  # 网页截图工具
│   │   └── CacheKeyUtils.java       # 缓存键工具
│   ├── exception/                   # 异常处理
│   │   ├── BusinessException.java   # 业务异常
│   │   ├── ErrorCode.java           # 错误码定义
│   │   └── GlobalExceptionHandler.java               # 全局异常处理器
│   ├── ratelimit/                   # 限流模块
│   │   ├── annotation/              # 限流注解
│   │   ├── aspect/                  # 限流切面
│   │   └── enums/                   # 限流枚举
│   └── constant/                    # 常量定义
│       ├── AppConstant.java         # 应用常量
│       └── UserConstant.java        # 用户常量
├── src/main/resources/
│   ├── application.yml              # 基础配置
│   ├── application-local.yml        # 本地环境配置
│   └── prompt/                      # AI 提示词模板
│       ├── codegen-vue-project-system-prompt.txt     # Vue 项目生成提示词
│       └── codegen-routing-system-prompt.txt         # 代码生成路由提示词
└── pom.xml                          # Maven 配置文件
```

## 核心业务流程

### 1. 代码生成流程
```
用户输入需求 → AI 类型路由 → 选择合适模型 → 流式生成代码 → 工具调用写入文件 → 项目构建 → 部署上线
```

### 2. 工具调用机制
```
AI 模型 → 识别需要的工具 → ToolManager 获取工具实例 → 执行工具方法 → 返回执行结果 → 继续生成
```

### 3. 项目构建流程
```
代码生成完成 → VueProjectBuilder 执行 npm install → 执行 npm run build → 验证 dist 目录 → 部署到 Web 服务器
```

## API 接口文档

### 应用管理接口

#### 1. 创建应用
```http
POST /api/app/add
Content-Type: application/json

{
  "appName": "我的Vue应用",
  "appDescription": "一个简单的待办事项管理应用"
}
```

#### 2. 智能代码生成（流式）
```http
GET /api/app/chat/gen/code?appId=1&message=创建一个待办事项管理应用&agent=false
Accept: text/event-stream
```

响应格式（SSE）：
```
data: {"d": "正在分析您的需求..."}
data: {"d": "开始生成项目结构..."}
data: {"d": "[选择工具] 写入文件"}
data: {"d": "生成 package.json 文件..."}
event: done
data: 
```

#### 3. 应用部署
```http
POST /api/app/deploy
Content-Type: application/json

{
  "appId": 1
}
```

响应：
```json
{
  "code": 0,
  "data": "https://deploy.example.com/app_1/",
  "message": "ok"
}
```

#### 4. 下载应用代码
```http
GET /api/app/download/1
```

返回 ZIP 格式的项目文件。

#### 5. 获取应用列表
```http
POST /api/app/good/list/page/vo
Content-Type: application/json

{
  "pageNum": 1,
  "pageSize": 10
}
```

### 用户管理接口

#### 1. 用户注册
```http
POST /api/user/register
Content-Type: application/json

{
  "userAccount": "testuser",
  "userPassword": "12345678",
  "checkPassword": "12345678"
}
```

#### 2. 用户登录
```http
POST /api/user/login
Content-Type: application/json

{
  "userAccount": "testuser",
  "userPassword": "12345678"
}
```

## 配置说明

### 基础配置 (application.yml)
```yaml
server:
  port: 8123
  servlet:
    context-path: /api

spring:
  profiles:
    active: local
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/code_mother
    username: root
    password: 123456
  data:
    redis:
      host: localhost
      port: 6379
      database: 1
```

### AI 模型配置 (application-local.yml)
```yaml
langchain4j:
  open-ai:
    chat-model:
      api-key: ${DEEPSEEK_API_KEY:your-deepseek-api-key}
      base-url: https://api.deepseek.com
      model-name: deepseek-chat
    streaming-chat-model:
      api-key: ${DEEPSEEK_API_KEY:your-deepseek-api-key}
      base-url: https://api.deepseek.com
      model-name: deepseek-chat
    routing-chat-model:
      api-key: ${OPENAI_API_KEY:your-openai-api-key}
      base-url: https://api.openai.com
      model-name: gpt-4

dashscope:
  api-key: ${DASHSCOPE_API_KEY:your-dashscope-api-key}
  image-model: qwen-vl-plus

pexels:
  api-key: ${PEXELS_API_KEY:your-pexels-api-key}
```

## 快速开始

### 1. 环境要求
- Java 21+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- Node.js 18+ (用于构建生成的前端项目)

### 2. 安装步骤
```bash
# 1. 克隆项目
git clone https://github.com/your-repo/code-mother.git
cd code-mother

# 2. 配置数据库
# 创建数据库 code_mother
# 执行 SQL 脚本初始化表结构

# 3. 配置 Redis
# 启动 Redis 服务

# 4. 配置 AI API 密钥
# 设置环境变量或直接修改 application-local.yml

# 5. 编译运行
mvn clean package
java -jar target/code-mother-1.0.jar
```

### 3. 访问应用
- 应用地址：http://localhost:8123/api
- API 文档：http://localhost:8123/api/doc.html

## 扩展开发

### 1. 添加新的 AI 工具
```java
@Component
public class CustomTool extends BaseTool {
    
    @Tool("工具描述")
    public String customMethod(@P("参数描述") String param) {
        // 工具逻辑实现
        return "执行结果";
    }
    
    @Override
    public String getToolName() {
        return "customMethod";
    }
    
    @Override
    public String getDisplayName() {
        return "自定义工具";
    }
}
```

### 2. 添加新的代码生成类型
```java
public enum CodeGenTypeEnum {
    VUE_PROJECT("vue_project", "Vue3项目"),
    REACT_PROJECT("react_project", "React项目"), // 新增
    HTML_PAGE("html_page", "HTML页面");
}
```

### 3. 自定义缓存策略
```java
@Configuration
public class CustomCacheConfig {
    
    @Bean
    public RedisCacheConfiguration customCacheConfig() {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()));
    }
}
```

## 常见问题

### 1. AI API 调用失败
- 检查 API 密钥是否正确配置
- 确认网络连接是否正常
- 查看 API 配额是否充足

### 2. 项目构建失败
- 确保系统已安装 Node.js 18+
- 检查生成的 package.json 是否正确
- 查看构建日志排查具体错误

### 3. 部署访问异常
- 检查部署目录权限
- 确认 Web 服务器配置
- 查看应用日志

### 4. 数据库连接问题
- 确认 MySQL 服务是否启动
- 检查数据库连接配置
- 验证用户权限设置

## 性能优化

### 1. 缓存策略
- Redis 缓存热点数据
- 应用级别的查询结果缓存
- AI 生成结果缓存

### 2. 限流保护
- 用户维度限流：防止单用户过度使用
- IP 维度限流：防止恶意攻击
- API 维度限流：保护核心接口

### 3. 异步处理
- 截图生成异步执行
- 项目构建异步处理
- 文件上传异步操作

## 安全考虑

### 1. API 密钥保护
- 使用环境变量存储敏感信息
- 避免在代码中硬编码密钥
- 定期轮换 API 密钥

### 2. 用户权限控制
- 基于角色的访问控制 (RBAC)
- 接口级别的权限验证
- 数据级别的权限隔离

### 3. 输入验证
- 严格的参数校验
- SQL 注入防护
- XSS 攻击防护

## 监控与运维

### 1. 日志管理
- 结构化日志输出
- 错误日志告警
- 性能日志分析

### 2. 健康检查
- 应用健康状态监控
- 数据库连接监控
- Redis 连接监控

### 3. 性能监控
- 接口响应时间监控
- AI 调用成功率监控
- 系统资源使用监控

## 开发路线图

### 短期目标
- [ ] 支持更多前端框架（React、Angular）
- [ ] 增加代码质量检查功能
- [ ] 优化 AI 模型选择策略
- [ ] 完善错误处理和重试机制

### 中期目标
- [ ] 支持后端代码生成
- [ ] 集成更多 AI 模型
- [ ] 添加项目模板市场
- [ ] 实现协作开发功能

### 长期目标
- [ ] 支持微服务架构生成
- [ ] 集成 CI/CD 流水线
- [ ] 提供云原生部署方案
- [ ] 构建开发者生态

## 贡献指南

欢迎提交 Issue 和 Pull Request 来改进项目。在贡献代码前，请确保：

1. 遵循项目的编码规范
2. 添加必要的测试用例
3. 更新相关文档
4. 通过所有 CI 检查

## 许可证

本项目采用 MIT 许可证，详见 [LICENSE](LICENSE) 文件。

## 联系方式

- 项目地址：https://github.com/your-repo/code-mother
- 问题反馈：https://github.com/your-repo/code-mother/issues
- 邮箱：lijinhang460@gmail.com

---

**Code Mother - 让 AI 成为你的编程伙伴！** 🚀