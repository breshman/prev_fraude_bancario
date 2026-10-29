# Graph Report - .  (2026-10-28)

## Corpus Check
- Corpus is ~14,139 words - fits in a single context window. You may not need a graph.

## Summary
- 527 nodes · 1098 edges · 25 communities (23 shown, 2 thin omitted)
- Extraction: 96% EXTRACTED · 4% INFERRED · 0% AMBIGUOUS · INFERRED: 46 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- Rule Engine and Evaluation Context
- JPA Repositories and Entities
- Spring Validation and DTOs
- Fraud Alert Notifications and Web Adapter
- Authentication Web Adapter
- Fraud Case Management Service
- Fraud Rule Evaluation Service
- Global Exception Handling and REST Responses
- JWT Filter and Token Utilities
- Case Controller Web Adapter
- Spring Security Configuration
- Feature Extraction Service
- WebSocket Configuration
- Maven Wrapper
- Health Check Web Adapter
- User Details Security Service
- Data Initialization Runner
- OpenAPI Documentation Config
- Validation and Domain Tests
- Fraud Alert Domain Model
- Client Domain Model
- Transaction Domain Model
- Base Package Declaration
- Community 23
- Community 24

## God Nodes (most connected - your core abstractions)
1. `EvaluationContext` - 23 edges
2. `RuleEvaluationResult` - 23 edges
3. `FraudRuleStrategy` - 22 edges
4. `TransaccionService` - 18 edges
5. `TransaccionResponse` - 17 edges
6. `TransaccionRequest` - 15 edges
7. `AlertaFraudeService` - 15 edges
8. `CasoFraudeService` - 15 edges
9. `AlertaController` - 14 edges
10. `GlobalExceptionHandler` - 14 edges

## Surprising Connections (you probably didn't know these)
- `CasoController` --references--> `CasoFraudeService`  [EXTRACTED]
  src/main/java/com/empresa/fraude/adapter/in/web/CasoController.java → src/main/java/com/empresa/fraude/application/service/CasoFraudeService.java
- `TransaccionController` --references--> `TransaccionRequestValidator`  [EXTRACTED]
  src/main/java/com/empresa/fraude/adapter/in/web/TransaccionController.java → src/main/java/com/empresa/fraude/adapter/in/web/validator/TransaccionRequestValidator.java
- `EvaluacionFraudeService` --references--> `AlertaFraudeService`  [EXTRACTED]
  src/main/java/com/empresa/fraude/application/service/EvaluacionFraudeService.java → src/main/java/com/empresa/fraude/application/service/AlertaFraudeService.java
- `TransaccionFlowIntegrationTest` --references--> `AlertaFraudeService`  [EXTRACTED]
  src/test/java/com/empresa/fraude/TransaccionFlowIntegrationTest.java → src/main/java/com/empresa/fraude/application/service/AlertaFraudeService.java
- `AuthService` --references--> `JwtService`  [EXTRACTED]
  src/main/java/com/empresa/fraude/application/service/AuthService.java → src/main/java/com/empresa/fraude/config/security/JwtService.java

## Import Cycles
- None detected.

## Communities (25 total, 2 thin omitted)

### Community 0 - "Rule Engine and Evaluation Context"
Cohesion: 0.06
Nodes (32): EvaluationContext, Builder, Value, FraudRuleStrategy, Component, Override, RF001_MontoInusualRule, Component (+24 more)

### Community 1 - "JPA Repositories and Entities"
Cohesion: 0.09
Nodes (36): CanalJpaRepository, CuentaJpaRepository, InitBinder, EvaluacionDetalle, AllArgsConstructor, Builder, Data, NoArgsConstructor (+28 more)

### Community 2 - "Spring Validation and DTOs"
Cohesion: 0.11
Nodes (30): AlertaEmailNotificationPort, AlertaNotificationPort, EvaluacionFraudeEntity, AlertaController, AlertaFraudeEntity, GetMapping, Operation, PatchMapping (+22 more)

### Community 3 - "Fraud Alert Notifications and Web Adapter"
Cohesion: 0.08
Nodes (26): CaracteristicaTransaccionJpaRepository, ReglaFraudeJpaRepository, EvaluacionFraudeService, CuentaEntity, EvaluacionFraudeJpaRepository, RequiredArgsConstructor, Service, TransaccionEntity (+18 more)

### Community 4 - "Authentication Web Adapter"
Cohesion: 0.10
Nodes (33): CasoController, CasoFraudeEntity, GetMapping, HistorialCasoEntity, Operation, PatchMapping, PostMapping, PreAuthorize (+25 more)

### Community 5 - "Fraud Case Management Service"
Cohesion: 0.14
Nodes (16): BadCredentialsException, ConstraintViolationException, ExceptionHandler, Getter, HandlerMethodValidationException, HttpMessageNotReadableException, HttpStatus, MethodArgumentNotValidException (+8 more)

### Community 6 - "Fraud Rule Evaluation Service"
Cohesion: 0.15
Nodes (16): Errors, Pattern, AllArgsConstructor, Builder, Data, NoArgsConstructor, Schema, TransaccionRequest (+8 more)

### Community 7 - "Global Exception Handling and REST Responses"
Cohesion: 0.13
Nodes (25): AuthController, ApiResponse, Operation, PostMapping, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController (+17 more)

### Community 8 - "JWT Filter and Token Utilities"
Cohesion: 0.16
Nodes (14): Claims, FilterChain, HttpServletResponse, OncePerRequestFilter, SecretKey, Component, HttpServletRequest, Override (+6 more)

### Community 9 - "Case Controller Web Adapter"
Cohesion: 0.13
Nodes (17): ConstraintValidator, CanalValidator, ConstraintValidatorContext, Override, ConstraintValidatorContext, Override, MonedaValidator, Constraint (+9 more)

### Community 10 - "Spring Security Configuration"
Cohesion: 0.20
Nodes (14): AuthenticationConfiguration, AuthenticationProvider, CorsConfigurationSource, EnableMethodSecurity, EnableWebSecurity, HttpSecurity, SecurityFilterChain, AuthenticationManager (+6 more)

### Community 11 - "Feature Extraction Service"
Cohesion: 0.22
Nodes (11): CasoFraudeJpaRepository, HistorialCasoJpaRepository, CasoFraudeService, AlertaFraudeJpaRepository, CasoFraudeEntity, HistorialCasoEntity, RequiredArgsConstructor, Service (+3 more)

### Community 12 - "WebSocket Configuration"
Cohesion: 0.33
Nodes (5): BeforeAll, DtoValidationTest, DisplayName, Test, Validator

### Community 13 - "Maven Wrapper"
Cohesion: 0.33
Nodes (7): EnableWebSocketMessageBroker, MessageBrokerRegistry, Configuration, Override, WebSocketConfig, StompEndpointRegistry, WebSocketMessageBrokerConfigurer

### Community 14 - "Health Check Web Adapter"
Cohesion: 0.33
Nodes (6): mvnw script, clean(), die(), exec_maven(), set_java_home(), verbose()

### Community 15 - "User Details Security Service"
Cohesion: 0.36
Nodes (8): HealthCheckController, ApiResponse, GetMapping, Operation, RequestMapping, ResponseEntity, RestController, Tag

### Community 16 - "Data Initialization Runner"
Cohesion: 0.33
Nodes (8): CustomUserDetailsService, Override, RequiredArgsConstructor, Service, Transactional, UserDetails, UsuarioSistemaJpaRepository, UserDetailsService

### Community 17 - "OpenAPI Documentation Config"
Cohesion: 0.36
Nodes (7): CommandLineRunner, DataInitializer, Component, Override, PasswordEncoder, RequiredArgsConstructor, UsuarioSistemaJpaRepository

### Community 18 - "Validation and Domain Tests"
Cohesion: 0.53
Nodes (4): OpenAPI, Bean, Configuration, OpenApiConfig

### Community 19 - "Fraud Alert Domain Model"
Cohesion: 0.60
Nodes (3): FraudeApplicationTests, SpringBootTest, Test

### Community 21 - "Transaction Domain Model"
Cohesion: 0.83
Nodes (3): AlertaFraude, Builder, Data

### Community 22 - "Base Package Declaration"
Cohesion: 0.83
Nodes (3): Cliente, Builder, Data

### Community 23 - "Community 23"
Cohesion: 0.83
Nodes (3): Builder, Data, Transaccion

## Knowledge Gaps
- **1 isolated node(s):** `com.empresa:fraude`
  These have ≤1 connection - possible missing edges or undocumented components.
- **2 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `EvaluacionFraudeService` connect `Fraud Alert Notifications and Web Adapter` to `JPA Repositories and Entities`, `Spring Validation and DTOs`?**
  _High betweenness centrality (0.164) - this node is a cross-community bridge._
- **Why does `AuthService` connect `Global Exception Handling and REST Responses` to `JWT Filter and Token Utilities`, `JPA Repositories and Entities`?**
  _High betweenness centrality (0.161) - this node is a cross-community bridge._
- **Why does `AlertaFraudeService` connect `Spring Validation and DTOs` to `JPA Repositories and Entities`, `Fraud Alert Notifications and Web Adapter`?**
  _High betweenness centrality (0.143) - this node is a cross-community bridge._
- **What connects `com.empresa:fraude` to the rest of the system?**
  _1 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Rule Engine and Evaluation Context` be split into smaller, more focused modules?**
  _Cohesion score 0.06202435312024353 - nodes in this community are weakly interconnected._
- **Should `JPA Repositories and Entities` be split into smaller, more focused modules?**
  _Cohesion score 0.08653061224489796 - nodes in this community are weakly interconnected._
- **Should `Spring Validation and DTOs` be split into smaller, more focused modules?**
  _Cohesion score 0.10631229235880399 - nodes in this community are weakly interconnected._