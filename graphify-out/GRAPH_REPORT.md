# Graph Report - prev_fraude_bancario  (2026-09-29)

## Corpus Check
- 109 files · ~40,700 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 860 nodes · 1541 edges · 49 communities (43 shown, 6 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 36 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `baee05a8`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- FraudMockService
- Guía de Uso y Ejecución - Backend: Sistema de Detección de Fraude Bancario
- sidebar.component.ts
- Instrucciones IA - Backend: Sistema de Detección de Fraude Bancario
- Instrucciones IA - Orquestador: Sistema de Detección de Fraude Bancario
- Instrucciones IA - Frontend: Sistema de Detección de Fraude Bancario
- EvaluationContext
- FraudeApplication
- FraudeApplicationTests.java
- AlertaFraude.java
- Cliente.java
- Transaccion.java
- TransaccionService
- CasoFraudeService
- TransaccionResponse
- devDependencies
- AlertaFraudeService
- AuthController.java
- dependencies
- SimulationComponent
- JwtService
- SimulationService
- SecurityConfig.java
- 📋 Lista de Pasos y Estado
- AuthService
- 📋 Lista Maestra de Fases y Tareas de Integración
- options
- case.service.ts
- WebSocketService
- mvnw
- HealthCheckController.java
- CustomUserDetailsService.java
- WebSocketConfig.java
- simulation.service.ts
- ExcelExportUtil
- DataInitializer
- production
- development
- FraudChartComponent
- frontend
- angular.json
- OpenApiConfig.java
- architect
- environment.prod.ts
- com.empresa:fraude

## God Nodes (most connected - your core abstractions)
1. `SimulationService` - 43 edges
2. `SimulationComponent` - 32 edges
3. `EvaluationContext` - 27 edges
4. `RuleEvaluationResult` - 23 edges
5. `FraudService` - 23 edges
6. `FraudRuleStrategy` - 22 edges
7. `FraudAlert` - 22 edges
8. `TransaccionService` - 18 edges
9. `TransaccionResponse` - 17 edges
10. `DashboardComponent` - 17 edges

## Surprising Connections (you probably didn't know these)
- `EvaluacionFraudeService` --references--> `AlertaFraudeService`  [EXTRACTED]
  backend/src/main/java/com/empresa/fraude/application/service/EvaluacionFraudeService.java → backend/src/main/java/com/empresa/fraude/application/service/AlertaFraudeService.java
- `TransaccionFlowIntegrationTest` --references--> `AlertaFraudeService`  [EXTRACTED]
  backend/src/test/java/com/empresa/fraude/TransaccionFlowIntegrationTest.java → backend/src/main/java/com/empresa/fraude/application/service/AlertaFraudeService.java
- `AuthService` --references--> `JwtService`  [EXTRACTED]
  backend/src/main/java/com/empresa/fraude/application/service/AuthService.java → backend/src/main/java/com/empresa/fraude/config/security/JwtService.java
- `TransaccionFlowIntegrationTest` --references--> `AuthService`  [EXTRACTED]
  backend/src/test/java/com/empresa/fraude/TransaccionFlowIntegrationTest.java → backend/src/main/java/com/empresa/fraude/application/service/AuthService.java
- `EvaluacionFraudeService` --references--> `FraudScoringEngine`  [EXTRACTED]
  backend/src/main/java/com/empresa/fraude/application/service/EvaluacionFraudeService.java → backend/src/main/java/com/empresa/fraude/domain/rule/FraudScoringEngine.java

## Import Cycles
- None detected.

## Communities (49 total, 6 thin omitted)

### Community 0 - "FraudMockService"
Cohesion: 0.07
Nodes (17): AlertaBackendEntity, DashboardMetrics, EstadoAlerta, FraudAlert, NivelPrioridad, AlertaWebSocketEventDto, FraudService, Injectable (+9 more)

### Community 1 - "Guía de Uso y Ejecución - Backend: Sistema de Detección de Fraude Bancario"
Cohesion: 0.06
Nodes (31): 1. Requisitos Previos, 2. Configuración de Base de Datos, 3. Comandos para Ejecutar desde la Terminal, 4. Documentación de la API con Swagger / OpenAPI, 5. Pruebas Automatizadas con Postman, 6. WebSockets en Tiempo Real (STOMP para Frontend Angular), 7. Estado de Verificación y Diagnóstico, Características de la Configuración de Swagger (+23 more)

### Community 2 - "sidebar.component.ts"
Cohesion: 0.10
Nodes (35): AllArgsConstructor, Builder, Data, NoArgsConstructor, Schema, TransaccionRequest, EvaluacionDetalle, AllArgsConstructor (+27 more)

### Community 4 - "Instrucciones IA - Backend: Sistema de Detección de Fraude Bancario"
Cohesion: 0.22
Nodes (8): 1. Stack Tecnológico, 2. Arquitectura de Software: Clean Architecture / Hexagonal, 3. Patrones de Diseño a Implementar, 4. Reglas Críticas para la Implementación (IA), code:block1 (com.empresa.fraude), code:json ({), Instrucciones IA - Backend: Sistema de Detección de Fraude Bancario, Rol y Objetivo del Agente

### Community 5 - "Instrucciones IA - Orquestador: Sistema de Detección de Fraude Bancario"
Cohesion: 0.25
Nodes (7): 1. Visión Global del Proyecto, 2. Coordinación de Agentes, 3. Acuerdos de Integración (Contratos API), 4. Instrucción Crítica, Carpetas del Proyecto:, Instrucciones IA - Orquestador: Sistema de Detección de Fraude Bancario, Rol y Objetivo del Agente Principal

### Community 6 - "Instrucciones IA - Frontend: Sistema de Detección de Fraude Bancario"
Cohesion: 0.25
Nodes (7): 1. Stack Tecnológico, 2. Arquitectura del Frontend, 3. Patrones de Diseño a Implementar, 4. Reglas Críticas para la Implementación (IA), code:block1 (src/app/), Instrucciones IA - Frontend: Sistema de Detección de Fraude Bancario, Rol y Objetivo del Agente

### Community 7 - "EvaluationContext"
Cohesion: 0.05
Nodes (37): EvaluationContext, Builder, Value, FraudRuleStrategy, FraudScoringEngine, Service, Component, Override (+29 more)

### Community 9 - "FraudeApplicationTests.java"
Cohesion: 0.60
Nodes (3): FraudeApplicationTests, SpringBootTest, Test

### Community 10 - "AlertaFraude.java"
Cohesion: 0.83
Nodes (3): AlertaFraude, Builder, Data

### Community 11 - "Cliente.java"
Cohesion: 0.83
Nodes (3): Cliente, Builder, Data

### Community 12 - "Transaccion.java"
Cohesion: 0.83
Nodes (3): Builder, Data, Transaccion

### Community 13 - "TransaccionService"
Cohesion: 0.15
Nodes (13): GlobalExceptionHandler, HttpServletRequest, ResponseEntity, BusinessException, CuentaNoHabilitadaException, CuentaNotFoundException, MontoInvalidoException, BadCredentialsException (+5 more)

### Community 14 - "CasoFraudeService"
Cohesion: 0.10
Nodes (29): CasoController, CasoFraudeEntity, GetMapping, HistorialCasoEntity, Operation, PatchMapping, PostMapping, PreAuthorize (+21 more)

### Community 15 - "TransaccionResponse"
Cohesion: 0.10
Nodes (21): EvaluacionFraudeService, CuentaEntity, EvaluacionFraudeJpaRepository, RequiredArgsConstructor, Service, TransaccionEntity, Transactional, FeatureService (+13 more)

### Community 16 - "devDependencies"
Cohesion: 0.06
Nodes (32): @angular/cli, @angular/compiler-cli, @angular-devkit/build-angular, devDependencies, @angular/cli, @angular/compiler-cli, @angular-devkit/build-angular, jasmine-core (+24 more)

### Community 17 - "AlertaFraudeService"
Cohesion: 0.14
Nodes (21): AlertaNotificationPort, AlertaController, AlertaFraudeEntity, GetMapping, Operation, PatchMapping, PreAuthorize, RequestMapping (+13 more)

### Community 18 - "AuthController.java"
Cohesion: 0.12
Nodes (25): AuthController, ApiResponse, Operation, PostMapping, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController (+17 more)

### Community 19 - "dependencies"
Cohesion: 0.07
Nodes (27): @angular/common, @angular/compiler, @angular/core, @angular/forms, @angular/platform-browser, @angular/platform-browser-dynamic, @angular/router, bootstrap (+19 more)

### Community 20 - "SimulationComponent"
Cohesion: 0.18
Nodes (3): SimulatedTransaction, SimulationComponent, Component

### Community 21 - "JwtService"
Cohesion: 0.16
Nodes (14): Component, HttpServletRequest, Override, RequiredArgsConstructor, UserDetailsService, JwtAuthenticationFilter, Service, UserDetails (+6 more)

### Community 23 - "SecurityConfig.java"
Cohesion: 0.22
Nodes (13): AuthenticationConfiguration, AuthenticationProvider, AuthenticationManager, Bean, Configuration, PasswordEncoder, RequiredArgsConstructor, UserDetailsService (+5 more)

### Community 24 - "📋 Lista de Pasos y Estado"
Cohesion: 0.12
Nodes (16): 1. Arquitectura Hexagonal / Clean Architecture, 2. Principios SOLID, 3. Patrones de Diseño Clave, Estado de Ejecución y Plan Paso a Paso, FASE 1: Setup & Conectividad (COMPLETADA), FASE 2: Modelo de Datos Relacional y Migraciones Flyway (COMPLETADA), FASE 3: Motor de Reglas y Scoring (Strategy Pattern & SOLID) (COMPLETADA), FASE 4: Servicios de Aplicación y Casos de Uso (Application Layer) (COMPLETADA) (+8 more)

### Community 25 - "AuthService"
Cohesion: 0.05
Nodes (24): AppComponent, compiled, fixture, Component, appConfig, routes, authGuard(), jwtInterceptor() (+16 more)

### Community 26 - "📋 Lista Maestra de Fases y Tareas de Integración"
Cohesion: 0.12
Nodes (15): 🧭 Diagnóstico Topológico con Graphify, FASE 1: Preparación de Entornos y Configuración Base HTTP (COMPLETADA), FASE 2: Autenticación, Seguridad y Creación de la Página de Login (COMPLETADA), FASE 3: Transacciones y Simulación con Scoring Real (Backend Core) (COMPLETADA), FASE 4: Alertas Antifraude y Métricas del Dashboard (REST API) (COMPLETADA), FASE 5: Notificaciones Reactivas en Vivo (WebSockets STOMP) (COMPLETADA), FASE 6: Casos de Investigación Formal y Auditoría (Postman Carpeta 04) (COMPLETADA), FASE 7: Pruebas Integradas E2E, Verificación Postman y Depuración del Grafo (COMPLETADA) (+7 more)

### Community 27 - "options"
Cohesion: 0.20
Nodes (14): options, assets, browser, index, inlineStyleLanguage, outputPath, polyfills, scripts (+6 more)

### Community 28 - "case.service.ts"
Cohesion: 0.32
Nodes (6): AbrirCasoRequest, CasoFraude, CerrarCasoRequest, HistorialCaso, CaseService, Injectable

### Community 30 - "mvnw"
Cohesion: 0.33
Nodes (6): mvnw script, clean(), die(), exec_maven(), set_java_home(), verbose()

### Community 31 - "HealthCheckController.java"
Cohesion: 0.36
Nodes (8): HealthCheckController, ApiResponse, GetMapping, Operation, RequestMapping, ResponseEntity, RestController, Tag

### Community 32 - "CustomUserDetailsService.java"
Cohesion: 0.33
Nodes (8): CustomUserDetailsService, Override, RequiredArgsConstructor, Service, Transactional, UserDetails, UsuarioSistemaJpaRepository, UserDetailsService

### Community 33 - "WebSocketConfig.java"
Cohesion: 0.33
Nodes (7): Configuration, Override, WebSocketConfig, EnableWebSocketMessageBroker, MessageBrokerRegistry, StompEndpointRegistry, WebSocketMessageBrokerConfigurer

### Community 34 - "simulation.service.ts"
Cohesion: 0.33
Nodes (4): EvaluacionDetalleDto, ReglaDisparadaDto, TransaccionRequest, TransaccionResponse

### Community 36 - "DataInitializer"
Cohesion: 0.36
Nodes (7): DataInitializer, Component, Override, PasswordEncoder, RequiredArgsConstructor, UsuarioSistemaJpaRepository, CommandLineRunner

### Community 37 - "production"
Cohesion: 0.22
Nodes (9): build, builder, configurations, defaultConfiguration, production, budgets, buildTarget, fileReplacements (+1 more)

### Community 38 - "development"
Cohesion: 0.22
Nodes (9): serve, development, buildTarget, extractLicenses, optimization, sourceMap, builder, configurations (+1 more)

### Community 42 - "FraudChartComponent"
Cohesion: 0.27
Nodes (4): FraudChartComponent, Component, Input, ViewChild

### Community 43 - "frontend"
Cohesion: 0.25
Nodes (8): prefix, projectType, root, schematics, sourceRoot, frontend, style, @schematics/angular:component

### Community 44 - "angular.json"
Cohesion: 0.29
Nodes (6): cli, analytics, newProjectRoot, projects, $schema, version

### Community 45 - "OpenApiConfig.java"
Cohesion: 0.53
Nodes (4): Bean, Configuration, OpenApiConfig, OpenAPI

### Community 46 - "architect"
Cohesion: 0.40
Nodes (5): extract-i18n, test, builder, architect, builder

## Knowledge Gaps
- **131 isolated node(s):** `com.empresa:fraude`, `$schema`, `version`, `newProjectRoot`, `projectType` (+126 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **6 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `EvaluacionFraudeService` connect `TransaccionResponse` to `AlertaFraudeService`, `sidebar.component.ts`, `EvaluationContext`?**
  _High betweenness centrality (0.046) - this node is a cross-community bridge._
- **Why does `AuthService` connect `AuthController.java` to `JwtService`, `CasoFraudeService`?**
  _High betweenness centrality (0.044) - this node is a cross-community bridge._
- **Why does `AlertaFraudeService` connect `AlertaFraudeService` to `CasoFraudeService`, `TransaccionResponse`?**
  _High betweenness centrality (0.036) - this node is a cross-community bridge._
- **What connects `com.empresa:fraude`, `$schema`, `version` to the rest of the system?**
  _131 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `FraudMockService` be split into smaller, more focused modules?**
  _Cohesion score 0.07270865335381464 - nodes in this community are weakly interconnected._
- **Should `Guía de Uso y Ejecución - Backend: Sistema de Detección de Fraude Bancario` be split into smaller, more focused modules?**
  _Cohesion score 0.0625 - nodes in this community are weakly interconnected._
- **Should `sidebar.component.ts` be split into smaller, more focused modules?**
  _Cohesion score 0.09696969696969697 - nodes in this community are weakly interconnected._