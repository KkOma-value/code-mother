# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 3.5.5 project with Java 21, using MyBatis-Flex for database operations. The project follows a typical layered architecture with controllers, services, mappers, and entities.

## Key Technologies

- **Spring Boot 3.5.5** with Java 21
- **MyBatis-Flex 1.11.0** for database operations
- **MySQL** database with HikariCP connection pool
- **Lombok** for reducing boilerplate code
- **Hutool 5.8.38** utility library
- **Knife4j 4.4.0** for API documentation (OpenAPI 3)
- **Maven** for build management

## Database Configuration

- **Database**: MySQL on localhost:3306, database name: `code-mother`
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

### Code Generation
```bash
# Run the MyBatis code generator
java -cp target/classes com.example.codemother.generator.MyBatisCodeGenerator

# Or run directly from IDE using the main method in MyBatisCodeGenerator
```

### Database Operations
```bash
# Execute SQL scripts (example)
mysql -u root -p code-mother < src/main/java/com/example/codemother/sql/create_table.sql
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
- `com.example.codemother.common` - Common utilities and base classes
- `com.example.codemother.config` - Configuration classes (CORS, etc.)
- `com.example.codemother.exception` - Exception handling and error codes
- `com.example.codemother.generator` - Code generation utilities

### Configuration Files
- `application.yml` - Main application configuration
- `pom.xml` - Maven dependencies and build configuration

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

## Development Notes

### API Documentation
- Swagger UI available at `/api/doc.html` (Knife4j)
- API base path: `/api`
- Default server port: 8123

### Database Schema
- Current tables: `user` (see `create_table.sql`)
- Uses logical deletion pattern
- Includes user management with roles (user/admin)

### Code Generation
- The `MyBatisCodeGenerator` class can generate complete CRUD code
- Generates to temporary package `com.example.codemother.genresult`
- Configured for Lombok, Java 21, and MyBatis-Flex patterns

### Testing
- Standard Spring Boot test structure in `src/test/`
- Use `@SpringBootTest` for integration tests
- Test configuration in `application-test.yml` (if needed)

## Current Implementation Status

The project is in early development with:
- Basic user management system
- User login functionality (partially implemented)
- Database schema and entities
- Code generation setup
- Basic exception handling
- API documentation setup

Note: The UserController implementation appears to be incomplete with some compilation issues that need to be addressed.