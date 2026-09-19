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

- **Último paso completado:** `Fases 1 a 7 COMPLETADAS Y VERIFICADAS` (Suite de tests 8/8 pasando, build success, colección Postman generada y lista).
- **Siguiente paso a ejecutar:** `Pruebas interactivas con Postman o desarrollo de Frontend / Websockets (Opcional)`.

---

## 📋 Lista de Pasos y Estado

### FASE 1: Setup & Conectividad (COMPLETADA)
- [x] **Paso 1.1:** Verificación de Java 21, dependencias Spring Boot en `pom.xml` y permisos de `mvnw`.
- [x] **Paso 1.2:** Configuración de conexión a PostgreSQL (Docker `postgres_18_vector`, puerto `5434`, base de datos `fraude_db`).
- [x] **Paso 1.3:** Integración de Swagger UI y OpenAPI 3 con `springdoc-openapi-starter-webmvc-ui` (3.1.1).
- [x] **Paso 1.4:** Endpoint de verificación `GET /api/v1/health` y validación de ejecución exitosa en terminal.

---

### FASE 2: Modelo de Datos Relacional y Migraciones Flyway (COMPLETADA)
- [x] **Paso 2.1:** Crear script Flyway `V2__full_schema.sql` con las 18 tablas relacionales del PDF:
  - Maestros: `cliente`, `cuenta`, `beneficiario`, `canal`, `tipo_transaccion`, `dispositivo`.
  - Transaccional: `transaccion`.
  - Antifraude: `caracteristica_transaccion`, `regla_fraude`, `evaluacion_fraude`, `evaluacion_regla`, `alerta_fraude`, `caso_fraude`, `historial_caso`.
  - Seguridad/Gobierno: `usuario_sistema`, `rol`, `usuario_rol`, `auditoria`.
- [x] **Paso 2.2:** Semillas maestras (`V3__seed_data.sql`): canales predeterminados (APP, WEB, ATM), tipos de transacción, roles del sistema, y las 8 reglas iniciales con sus pesos y códigos oficiales (RF001 a RF008).
- [x] **Paso 2.3:** Crear Entidades JPA en `adapter/out/persistence/jpa/entity/` respetando las convenciones del diccionario técnico y encapsulación.
- [x] **Paso 2.4:** Crear Mappers bidireccionales (Dominio ↔ Entidad JPA) en `adapter/out/persistence/jpa/mapper/` cumpliendo con Clean Architecture (el dominio no conoce a JPA).
- [x] **Paso 2.5:** Crear Repositorios Spring Data JPA y sus adaptadores que implementan los puertos de salida (`application/port/out/`).

---

### FASE 3: Motor de Reglas y Scoring (Strategy Pattern & SOLID) (COMPLETADA)
- [x] **Paso 3.1:** Diseñar la abstracción central: interfaz `FraudRuleStrategy` y el objeto inmutable de contexto `EvaluationContext` (DIP, ISP).
- [x] **Paso 3.2:** Implementar las 8 reglas antifraude como estrategias independientes (SRP, OCP, LSP):
  - `RF001_MontoInusualRule` (peso 25): monto > 5 * promedio_30d.
  - `RF002_DispositivoNuevoRule` (peso 15): es_dispositivo_nuevo = true.
  - `RF003_IpNuevaRule` (peso 10): es_ip_nueva = true.
  - `RF004_PaisNuevoRule` (peso 30): es_pais_nuevo = true.
  - `RF005_HorarioInusualRule` (peso 10): horario atípico (01:00 a 05:00).
  - `RF006_AltaFrecuenciaRule` (peso 25): transacciones en 1h > 5.
  - `RF007_ViajeImposibleRule` (peso 40): velocidad geográfica > 800 km/h.
  - `RF008_BeneficiarioNuevoMontoAltoRule` (peso 20): beneficiario no conocido + monto > umbral.
- [x] **Paso 3.3:** `FraudScoringEngine`: orquestador desacoplado que inyecta la lista `List<FraudRuleStrategy>`, suma los pesos activos, normaliza el score (0-100) y asigna el nivel de riesgo:
  - `0 - 29`: `BAJO` -> `APROBAR`
  - `30 - 59`: `MEDIO` -> `MONITOREAR`
  - `60 - 79`: `ALTO` -> `RETENER` (genera Alerta)
  - `80 - 100`: `CRITICO` -> `BLOQUEAR` (genera Alerta Crítica)
- [x] **Paso 3.4:** Pruebas unitarias completas para cada regla y para el motor de scoring (Mocking de dependencias, cobertura de casos límites y división por cero).

---

### FASE 4: Servicios de Aplicación y Casos de Uso (Application Layer) (COMPLETADA)
- [x] **Paso 4.1:** `FeatureService`: extracción y cálculo de variables históricas (monto promedio 30d, frecuencia 1h, recencia, distancia geográfica con fórmula de Haversine).
- [x] **Paso 4.2:** `TransaccionService`: validación de cuenta activa, saldo suficiente, verificación de idempotencia y persistencia de la transacción en estado inicial `PENDIENTE`.
- [x] **Paso 4.3:** `EvaluacionFraudeService`: orquestación del flujo transaccional; invoca `FeatureService`, ejecuta `FraudScoringEngine`, persiste en `evaluacion_fraude` y `evaluacion_regla`, y actualiza el estado transaccional.
- [x] **Paso 4.4:** `AlertaFraudeService`: generación y gestión de alertas operativas cuando el score amerite atención (`ALTO` o `CRITICO`).
- [x] **Paso 4.5:** `CasoFraudeService`: apertura de caso de investigación formal, bitácora cronológica inmutable en `historial_caso` y cierre de caso con dictamen final (`FRAUDE_CONFIRMADO`, `FALSO_POSITIVO`, `SIN_EVIDENCIA`).

---

### FASE 5: Seguridad y Control de Acceso (Spring Security + JWT) (COMPLETADA)
- [x] **Paso 5.1:** `JwtService`: generación, firma HMAC-SHA256 y validación de tokens JWT stateless.
- [x] **Paso 5.2:** `JwtAuthenticationFilter`: interceptor para validar y extraer credenciales del header `Authorization: Bearer <token>`.
- [x] **Paso 5.3:** `AuthService` y controlador `POST /api/v1/auth/login` con validación de credenciales cifradas con BCrypt.
- [x] **Paso 5.4:** Configuración de autorización basada en roles (`ADMIN`, `ANALISTA_FRAUDE`, `SUPERVISOR`, `AUDITOR`) protegiendo endpoints sensibles.

---

### FASE 6: Controladores REST, DTOs y Documentación Swagger (Adapter In Web) (COMPLETADA)
- [x] **Paso 6.1:** `TransaccionController`: `POST /api/v1/transacciones` cumpliendo el contrato oficial de respuesta con `correlationId`, `codigoTransaccion`, `estado` y objeto explicable `evaluacion`.
- [x] **Paso 6.2:** `AlertaController`: endpoints REST para listar alertas (`GET /api/v1/alertas`), obtener detalle (`GET /api/v1/alertas/{id}`) y actualizar estado (`PATCH /api/v1/alertas/{id}/estado`).
- [x] **Paso 6.3:** `CasoController`: endpoints REST para gestión e investigación de casos (`POST /api/v1/casos`, `POST /api/v1/casos/{id}/historial`, `PATCH /api/v1/casos/{id}/cerrar`).
- [x] **Paso 6.4:** `GlobalExceptionHandler` (`@ControllerAdvice`): tratamiento uniforme de excepciones de negocio y validación devolviendo códigos HTTP semánticos (400, 404, 409, 422) con el estándar RFC 7807 Problem Details.
- [x] **Paso 6.5:** Enriquecimiento de anotaciones OpenAPI (`@Tag`, `@Operation`, `@ApiResponse`) en todos los controladores para Swagger UI.

---

### FASE 7: Pruebas con Postman y Cierre (COMPLETADA)
- [x] **Paso 7.1:** Importar la colección Postman (`backend/postman/fraud_detection_postman_collection.json`).
- [x] **Paso 7.2:** Ejecución y validación del **Happy Path** (Transacción de bajo riesgo aprobada sin alertas).
- [x] **Paso 7.3:** Ejecución y validación del **No Happy Path A** (Fraude crítico bloqueado, score alto, alerta crítica generada).
- [x] **Paso 7.4:** Ejecución y validación del **No Happy Path B** (Validaciones de monto ≤ 0, cuentas inexistentes y excepciones de negocio).
- [x] **Paso 7.5:** Ejecución de la suite completa de pruebas de integración con `./mvnw test` (8/8 tests exitosos).
