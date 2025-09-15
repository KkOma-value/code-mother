# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 3.5.5 project with Java 21, featuring AI-powered code generation using LangChain4j. The project combines traditional web application architecture with AI capabilities for generating HTML and multi-file code (HTML/CSS/JS) through OpenAI-compatible APIs.

## Key Technologies

- **Spring Boot 3.5.5** with Java 21
- **MyBatis-Flex 1.11.0** for database operations
- **MySQL** database with HikariCP connection pool
- **LangChain4j 1.1.0** for AI integration and code generation
- **DeepSeek AI API** for code generation model
- **Project Reactor** for reactive programming and streaming
- **Lombok** for reducing boilerplate code
- **Hutool 5.8.38** utility library
- **Knife4j 4.4.0** for API documentation (OpenAPI 3)
- **Maven** for build management

## Database Configuration

- **Database**: MySQL on localhost:3306, database name: `code-mother-master`
- **Connection pool**: HikariCP
- **Schema**: SQL scripts located in `src/main/java/com/example/codemother/sql/`
- **Code Generation**: MyBatis-Flex code generator in `MyBatisCodeGenerator.java`

## Common Commands

### Running the Application
```bash
# Run the application
mvn spring-boot:run

# Build the project
mvn clean package

# Run tests
mvn test

# Run with specific profile (if needed)
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Database Operations
```bash
# Execute SQL scripts (example)
mysql -u root -p code-mother-master < src/main/java/com/example/codemother/sql/create_table.sql
```

### AI Code Generation Testing
```bash
# Test AI code generation via API endpoints
curl -X POST http://localhost:8123/api/code/generate \
  -H "Content-Type: application/json" \
  -d '{"prompt": "Create a simple HTML page with CSS", "type": "HTML"}'

# Test streaming AI code generation
curl -X POST http://localhost:8123/api/code/generate/stream \
  -H "Content-Type: application/json" \
  -d '{"prompt": "Create a responsive website", "type": "MULTI_FILE"}'
```

## Project Structure

### Core Architecture
- **Controllers**: REST API endpoints in `controller/` package
- **Services**: Business logic in `service/` package with implementations in `service/impl/`
- **Mappers**: MyBatis-Flex mappers in `mapper/` package with XML configs in `resources/mapper/`
- **Entities**: Database entities in `model/entity/` package
- **DTOs**: Data transfer objects in `model/dto/` package
- **Enums**: Enumerations in `model/enums/` package

### Key Packages
- `com.example.codemother.ai` - AI code generation services and models
- `com.example.codemother.core` - Core business logic and facade classes
- `com.example.codemother.common` - Common utilities and base classes
- `com.example.codemother.config` - Configuration classes (CORS, etc.)
- `com.example.codemother.exception` - Exception handling and error codes
- `com.example.codemother.generator` - MyBatis code generation utilities

### Configuration Files
- `application.yml` - Main application configuration
- `application-local.yml` - Local development configuration with AI API settings
- `pom.xml` - Maven dependencies and build configuration
- **AI Prompt Files**:
  - `src/main/resources/codegen-html-system-prompt.txt` - HTML generation system prompt
  - `src/main/resources/codegen-multi-file-system-prompt.txt` - Multi-file generation system prompt

## Code Conventions

### Entity Classes
- Use `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` from Lombok
- Implement `Serializable`
- Use `@Table` annotation for table mapping
- Use `@Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)` for primary keys
- Include `@Serial` and `serialVersionUID` for serialization

### Database Patterns
- Logical deletion using `isDelete` field with `@Column(value = "isDelete", isLogicDelete = true)`
- Audit fields: `createTime`, `updateTime`, `editTime`
- Use MyBatis-Flex annotations for column mapping

### API Patterns
- REST controllers with `@RestController`
- Use `BaseResponse<T>` for consistent API responses
- Exception handling with `@ControllerAdvice` and custom exceptions
- Input validation with `ThrowUtils.throwIf()` for parameter checking

### Service Layer
- Services extend `IService<Entity>` from MyBatis-Flex
- Service implementations extend `ServiceImpl<Mapper, Entity>`
- Follow standard Spring service patterns

### AI Code Generation Architecture
- **Facade Pattern**: `AiCodeGeneratorFacade` provides unified interface for code generation
- **Strategy Pattern**: `CodeGenTypeEnum` supports HTML and MULTI_FILE generation types
- **Factory Pattern**: `AiCodeGeneratorServiceFactory` creates LangChain4j AI service instances
- **Reactive Programming**: Uses `Flux<String>` for streaming AI responses
- **Structured Output**: LangChain4j `@Description` annotations ensure consistent AI response format

## AI Configuration and Development Notes

### AI API Configuration
- **Provider**: DeepSeek API (OpenAI-compatible)
- **Model**: `deepseek-chat`
- **Base URL**: `https://api.deepseek.com`
- **Max Tokens**: 8192
- **Streaming**: Supported via Project Reactor
- **Configuration**: Located in `application-local.yml`

### AI Code Generation Workflow
1. **Request Processing**: Facade validates input and dispatches to appropriate generator
2. **AI Generation**: LangChain4j service calls AI model with system prompts
3. **Code Parsing**: `CodeParser` extracts structured code from AI responses using regex
4. **File Management**: `CodeFileSaver` creates unique directories and saves generated files
5. **Streaming Support**: Real-time code generation with deferred file saving

### Code Generation Types
- **HTML**: Single HTML file with inline CSS/JS
- **MULTI_FILE**: Separate HTML, CSS, and JS files
- **Output Directory**: `tmp/code_output/` with snowflake ID-based naming

### API Documentation
- Swagger UI available at `/api/doc.html` (Knife4j)
- API base path: `/api`
- Default server port: 8123
- Key endpoints:
  - `POST /api/code/generate` - Synchronous code generation
  - `POST /api/code/generate/stream` - Streaming code generation

### Database Schema
- Current tables: `user` (see `create_table.sql`)
- Uses logical deletion pattern
- Includes user management with roles (user/admin)

### Code Generation
- **MyBatis Generation**: `MyBatisCodeGenerator` for database CRUD code
- **AI Generation**: LangChain4j-based for frontend code generation
- **Output**: Generated code saved to `tmp/code_output/` directory

### Testing
- Standard Spring Boot test structure in `src/test/`
- Use `@SpringBootTest` for integration tests
- Test configuration in `application-test.yml` (if needed)

## Current Implementation Status

The project combines traditional Spring Boot architecture with AI capabilities:
- **AI Code Generation**: Fully functional HTML and multi-file code generation
- **User Management**: Basic system with authentication
- **Database Integration**: MyBatis-Flex with MySQL
- **Reactive Programming**: Streaming AI responses
- **API Documentation**: Complete OpenAPI documentation

### Known Issues
- **UserController has duplicate method definitions**: Two `addUser` methods with the same mapping `/add` (lines 48 and 84) causing compilation errors
- **Streaming Error Handling**: Limited error recovery in streaming code generation
- **AI Response Parsing**: May need enhanced parsing for edge cases in AI output formats