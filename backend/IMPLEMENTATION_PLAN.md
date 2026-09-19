# Estado de Ejecución y Plan Paso a Paso
## Sistema de Detección de Fraude Bancario

> **Instrucción para la IA / Desarrollador:**  
> Este archivo mantiene el estado vivo del avance. Cada vez que se complete un paso, marca la casilla correspondiente con `[x]` y actualiza la sección **"Último paso completado"** y **"Siguiente paso a ejecutar"**. Si la sesión se interrumpe, la IA debe leer este archivo y continuar inmediatamente desde el **Siguiente paso a ejecutar**.

---

## 🏛️ Principios de Diseño, Patrones y Arquitectura

Todo el desarrollo debe regirse estrictamente por los siguientes estándares arquitectónicos:

### 1. Arquitectura Hexagonal / Clean Architecture
- **Inward Dependency Rule:** El dominio (`domain/`) se ubica en el centro y **no depende de ningún framework, anotación JPA (`@Entity`), base de datos ni librerías de infraestructura**.
- **Capas definidas:**
  1. `domain/`: Modelos de negocio puros, Value Objects, motor de reglas y excepciones de dominio.
  2. `application/`: Casos de uso y puertos de entrada/salida (`port/in`, `port/out`).
  3. `adapter/in/`: Controladores REST, DTOs de request/response y validaciones.
  4. `adapter/out/`: Entidades JPA, repositorios Spring Data, mappers y adaptadores externos.
  5. `config/`: Configuración de seguridad, OpenAPI, beans de Spring.

### 2. Principios SOLID
- **S - Single Responsibility Principle (SRP):**  
  Cada clase tiene una única responsabilidad y una sola razón para cambiar:
  - Cada regla de fraude (`RF001` a `RF008`) reside en su propia clase y solo evalúa su condición específica.
  - `FeatureService` únicamente calcula métricas históricas; no toma decisiones de aprobación ni bloqueo.
  - Los DTOs de transporte se separan estrictamente de los modelos de dominio y las entidades de persistencia.
- **O - Open/Closed Principle (OCP):**  
  El motor de scoring (`FraudScoringEngine`) está cerrado a modificación pero abierto a extensión mediante el patrón **Strategy**. Incorporar una nueva regla (ej. `RF009`) se realiza creando una nueva clase que implemente `FraudRuleStrategy` sin alterar una sola línea del motor existente.
- **L - Liskov Substitution Principle (LSP):**  
  Todas las implementaciones de `FraudRuleStrategy` son completamente intercambiables; cumplen el mismo contrato `evaluate(EvaluationContext)` devolviendo un `RuleEvaluationResult` uniforme, sin generar efectos secundarios inesperados.
- **I - Interface Segregation Principle (ISP):**  
  Se definen puertos delgados y específicos por responsabilidad (`TransaccionRepositoryPort`, `AlertaFraudeRepositoryPort`, etc.), evitando interfaces monolíticas y sobrecargadas.
- **D - Dependency Inversion Principle (DIP):**  
  La capa de aplicación y los servicios de dominio dependen exclusivamente de abstracciones (interfaces en `application/port/out`), nunca de implementaciones de infraestructura (JPA, Hibernate o PostgreSQL). La inyección de dependencias de Spring se usa para suministrar las implementaciones concretas en tiempo de ejecución.

### 3. Patrones de Diseño Clave
- **Strategy Pattern:** En el motor de fraude para desacoplar dinámicamente cada regla del cálculo de scoring.
- **Repository Pattern:** Abstracción total de la persistencia relacional.
- **DTO & Mapper Pattern:** Desacoplamiento del contrato HTTP respecto a los modelos internos (evitando fugas de datos y sobreexposición).
- **Value Objects / Inmutabilidad:** Modelos de dominio enriquecidos que protegen sus invariantes de negocio (ej. estados controlados por Enums, montos monetarios precisos con `BigDecimal`).
- **Global Error Handler (Controller Advice):** Manejo centralizado y semántico de excepciones (RFC 7807 Problem Details).

---

## 📌 Puntero de Continuación

- **Último paso completado:** `Fase 8: Notificaciones en Tiempo Real con WebSockets (STOMP) COMPLETADA Y PROBADA CON ÉXITO`.
- **Siguiente paso a ejecutar:** `Desarrollo o integración del Frontend Angular consumiendo el endpoint /ws-fraud y el tópico /topic/alertas`.

---

## 📋 Lista de Pasos y Estado

### FASE 1: Setup & Conectividad (COMPLETADA)
- [x] **Paso 1.1:** Verificación de Java 21, dependencias Spring Boot en `pom.xml` y permisos de `mvnw`.
- [x] **Paso 1.2:** Configuración de conexión a PostgreSQL (Docker `postgres_18_vector`, puerto `5434`, base de datos `fraude_db`).
- [x] **Paso 1.3:** Integración de Swagger UI y OpenAPI 3 con `springdoc-openapi-starter-webmvc-ui` (3.1.1).
- [x] **Paso 1.4:** Endpoint de verificación `GET /api/v1/health` y validación de ejecución exitosa en terminal.

---

### FASE 2: Modelo de Datos Relacional y Migraciones Flyway (COMPLETADA)
- [x] **Paso 2.1:** Crear script Flyway `V2__full_schema.sql` con las 18 tablas relacionales del PDF.
- [x] **Paso 2.2:** Semillas maestras (`V3__seed_data.sql`): canales, tipos de transacción, roles y las 8 reglas iniciales.
- [x] **Paso 2.3:** Crear Entidades JPA en `adapter/out/persistence/jpa/entity/`.
- [x] **Paso 2.4:** Crear Mappers bidireccionales (Dominio ↔ Entidad JPA) en `adapter/out/persistence/jpa/mapper/`.
- [x] **Paso 2.5:** Crear Repositorios Spring Data JPA y sus adaptadores (`application/port/out/`).

---

### FASE 3: Motor de Reglas y Scoring (Strategy Pattern & SOLID) (COMPLETADA)
- [x] **Paso 3.1:** Diseñar la abstracción central: interfaz `FraudRuleStrategy` y el objeto inmutable de contexto `EvaluationContext`.
- [x] **Paso 3.2:** Implementar las 8 reglas antifraude como estrategias independientes (RF001 a RF008).
- [x] **Paso 3.3:** `FraudScoringEngine`: orquestador desacoplado de scoring y categorización de riesgo.
- [x] **Paso 3.4:** Pruebas unitarias completas para cada regla y scoring engine.

---

### FASE 4: Servicios de Aplicación y Casos de Uso (Application Layer) (COMPLETADA)
- [x] **Paso 4.1:** `FeatureService`: extracción histórica y distancia geodésica con Haversine.
- [x] **Paso 4.2:** `TransaccionService`: validación de saldo, idempotencia y registro transaccional.
- [x] **Paso 4.3:** `EvaluacionFraudeService`: orquestación de evaluación y persistencia.
- [x] **Paso 4.4:** `AlertaFraudeService`: generación y gestión de alertas operativas.
- [x] **Paso 4.5:** `CasoFraudeService`: apertura, bitácora cronológica y cierre de casos.

---

### FASE 5: Seguridad y Control de Acceso (Spring Security + JWT) (COMPLETADA)
- [x] **Paso 5.1:** `JwtService`: generación, firma HMAC-SHA256 y validación de tokens JWT stateless.
- [x] **Paso 5.2:** `JwtAuthenticationFilter`: interceptor para validar y extraer credenciales Bearer.
- [x] **Paso 5.3:** `AuthService` y controlador `POST /api/v1/auth/login` con BCrypt.
- [x] **Paso 5.4:** Configuración de autorización basada en roles (`ADMIN`, `ANALISTA_FRAUDE`, `SUPERVISOR`, `AUDITOR`).

---

### FASE 6: Controladores REST, DTOs y Documentación Swagger (Adapter In Web) (COMPLETADA)
- [x] **Paso 6.1:** `TransaccionController`: `POST /api/v1/transacciones` con correlationId y evaluación explicable.
- [x] **Paso 6.2:** `AlertaController`: listado, detalle y actualización de alertas.
- [x] **Paso 6.3:** `CasoController`: endpoints REST para gestión e investigación de casos.
- [x] **Paso 6.4:** `GlobalExceptionHandler`: estándar RFC 7807 Problem Details.
- [x] **Paso 6.5:** Enriquecimiento de OpenAPI para Swagger UI.

---

### FASE 7: Pruebas con Postman y Cierre (COMPLETADA)
- [x] **Paso 7.1:** Colección Postman generada (`backend/postman/fraud_detection_postman_collection.json`).
- [x] **Paso 7.2:** Validación del Happy Path (Aprobada).
- [x] **Paso 7.3:** Validación del No Happy Path A (Bloqueo crítico y alerta).
- [x] **Paso 7.4:** Validación del No Happy Path B (Errores de validación RFC 7807).
- [x] **Paso 7.5:** Ejecución de suite completa con `./mvnw test` (8/8 tests exitosos).

---

### FASE 8: Notificaciones en Tiempo Real con WebSockets (STOMP para Frontend Angular) (COMPLETADA)
- [x] **Paso 8.1:** Agregar dependencia `spring-boot-starter-websocket` al `pom.xml`.
- [x] **Paso 8.2:** Definir puerto de salida `AlertaNotificationPort` en la capa de aplicación (DIP - Dependency Inversion Principle).
- [x] **Paso 8.3:** Crear DTO del evento `AlertaWebSocketEventDto` optimizado para visualización reactiva en el Frontend Angular (código alerta, transacción, score, nivel de riesgo, timestamp, decisión tomada).
- [x] **Paso 8.4:** Configuración de WebSocket STOMP (`WebSocketConfig.java`):
  - Endpoint de conexión `/ws-fraud` con SockJS fallback y soporte CORS para Angular (`http://localhost:4200`, `*`).
  - Broker de mensajes en memoria con prefijo `/topic` (ej. `/topic/alertas`).
- [x] **Paso 8.5:** Implementar el adaptador `WebSocketAlertaNotificationAdapter` utilizando `SimpMessagingTemplate` para emitir eventos al tópico `/topic/alertas`.
- [x] **Paso 8.6:** Habilitar el endpoint WebSocket en `SecurityConfig.java` para permitir handshake de clientes web.
- [x] **Paso 8.7:** Integrar la emisión del evento en `AlertaFraudeService` cuando se detecte una alerta (Nivel ALTO o CRITICO).
- [x] **Paso 8.8:** Verificación mediante suite de tests y documentación de integración para cliente Angular con `@stomp/stompjs` y RxJS.
