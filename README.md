# Code Mother（Spring Boot 应用）

一个基于 Spring Boot 的后端应用，集成了缓存、限流、AI 模型与工具调用、代码生成等能力，提供可视化 API 文档与多环境配置。项目支持本地开发（local）与生产环境（prod）分离，第三方服务可以通过 `application-local.yml` 或环境变量按需启用。

## 项目特性
- 应用与代码生成：支持页面代码生成、项目模板、文件保存等能力。
- AI 集成：
  - ChatModel 与 StreamingChatModel（OpenAI/DeepSeek）
  - Reasoning Streaming Chat（DeepSeek）
  - Routing Chat Model（OpenAI，用于路由判断）
  - 图像生成（阿里 DashScope）与图片搜索（Pexels）
- 缓存与存储：
  - Redis 缓存（`@EnableCaching`），默认 TTL 合理设置（如页面缓存 5 分钟等）。
  - 聊天记忆：本地环境使用内存存储；生产环境可启用 RedisChatMemoryStore。
- 生产能力隔离：
  - `@Profile("prod")` 启用 Redisson 与限流（`RateLimitAspect`）等生产特性。
- API 文档与跨域：
  - SpringDoc/Swagger/Knife4j 提供交互式接口文档。
  - 可配置的 CORS。

## 技术栈
- Java 21（推荐）
- Spring Boot 3.x
- MyBatis-Flex（数据访问）
- Redis、Redisson（缓存与分布式限流）
- LangChain4j（AI 模型与工具调用）
- Swagger/Knife4j（API 文档）

## 快速开始
### 前置条件
- 安装 JDK 21+
- 本地 MySQL 与 Redis（可按需调整）

### 克隆与构建
```powershell
# Windows（项目根目录）
./mvnw.cmd -DskipTests package
```

### 启动（本地环境）
```powershell
java -jar target/code-mother-0.0.1-SNAPSHOT.jar --spring.profiles.active=local --debug
```
- 默认端口：`8123`
- 默认上下文路径：`/api`
- 成功后日志会显示已注册的控制器与工具，以及缓存配置加载情况。

### 运行（可选，插件启动）
```powershell
# 如需使用 Spring Boot 插件运行
./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

## 配置说明
### 核心配置文件
- `src/main/resources/application.yml`
  - 基本服务配置（端口 `8123`、`/api` 上下文路径、数据源、Redis 等）
  - 默认设置 `spring.profiles.active: local`
- `src/main/resources/application-local.yml`
  - 本地环境下的第三方服务配置（AI 与工具调用）。
  - 可直接写入你的 API Key，或使用环境变量占位。

### 第三方服务（本地）
`application-local.yml` 支持以下键位（节选）：
- `langchain4j.open-ai.chat-model.apiKey`（DeepSeek/OpenAI）
- `langchain4j.open-ai.streaming-chat-model.apiKey`（OpenAI）
- `langchain4j.open-ai.reasoning-streaming-chat-model.apiKey`（DeepSeek）
- `langchain4j.open-ai.routing-chat-model.apiKey`（OpenAI）
- `dashscope.api-key`（阿里 DashScope 图像生成）
- `pexels.api-key`（Pexels 图片检索）

你可以：
- 直接写密钥，例如：`apiKey: "你的OpenAI_API_KEY"`
- 或通过环境变量（当前模板已支持），例如：`apiKey: ${OPENAI_API_KEY:}`

> 说明：未配置密钥时，应用可正常启动，但对应第三方接口调用会鉴权失败。

### 生产环境（`prod`）
- `@Profile("prod")` 下启用：
  - `RedissonConfig`（Redisson 客户端与限流）
  - `RateLimitAspect`（注解式限流，支持按用户/IP/API 维度）
  - `RedisChatMemoryStoreConfig`（聊天记忆存储到 Redis）
- 需要在 `application-prod.yml`（如需）与环境变量中配置生产服务地址与密钥。

## 接口与文档
- Swagger UI：`http://localhost:8123/api/swagger-ui/index.html`
- Knife4j：`http://localhost:8123/api/doc.html`（如启用）
- OpenAPI JSON：`http://localhost:8123/api/v3/api-docs`

> 控制器会被 SpringDoc 自动扫描；你可以在以上页面查看和调试接口。

## 数据库与缓存
- 数据库：在 `application.yml` 中配置 `spring.datasource`（URL、用户名、密码）。
- 缓存：
  - 已启用 Redis 缓存管理器（本地只要 Redis 连接正常即可生效）。
  - 常用缓存 TTL 已预设（如页面缓存 5 分钟、其余 30 分钟等）。

## 常见问题
- Bean Validation 警告：如见 `jakarta.validation.NoProviderFoundException`，可添加 `hibernate-validator` 依赖以启用校验。
- 插件启动参数：如命令行解析报错，优先使用打包后的 `jar` 启动方式。
- 第三方密钥：直接写入 `application-local.yml` 能快速验证，但请避免提交到仓库。推荐改回环境变量或使用私密配置管理。

## 项目结构（节选）
```
code-mother/
├── pom.xml
├── src/
│   └── main/
│       ├── java/            # 业务代码
│       └── resources/
│           ├── application.yml
│           └── application-local.yml
└── README.md
```

## 贡献与建议
- 欢迎在本地完善 `application-local.yml` 并为各模型提供更合理的默认参数。
- 如需扩展工具（如图片生成、检索等），建议统一走 LangChain4j 的 Tool 接口以便管理与路由。

---
如需我为你补充更详细的 API 列表、示例请求/响应或环境模板（`application-prod.yml`），告诉我使用场景即可，我会补充到本文档中。