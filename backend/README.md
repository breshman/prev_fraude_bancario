# Guía de Uso y Ejecución - Backend: Sistema de Detección de Fraude Bancario

Este microservicio está construido con **Spring Boot 4 / Java 17+**, implementando persistencia con **Spring Data JPA**, migraciones automáticas con **Flyway**, y una arquitectura desacoplada orientada a Clean Architecture.

---

## 1. Requisitos Previos

- **Java JDK:** Versión 17 o superior (en este equipo está instalado **Java 21 LTS**).
- **PostgreSQL / Docker:** Contenedor de base de datos en ejecución.
- **Maven Wrapper (`./mvnw`):** Viene incluido en el proyecto (no requiere tener Maven instalado globalmente).

---

## 2. Configuración de Base de Datos

El backend se conecta por defecto a la instancia PostgreSQL configurada en tu Docker:

- **Host:** `localhost`
- **Puerto:** `5434`
- **Base de Datos:** `fraude_db`
- **Usuario:** `postgres`
- **Contraseña:** `123456`

### Variables de Entorno (Opcionales)
Si requieres cambiar la conexión sin modificar el código, puedes sobreescribir los valores en `application.yml` mediante:
- `SPRING_DATASOURCE_URL` (por defecto `jdbc:postgresql://localhost:5434/fraude_db`)
- `SPRING_DATASOURCE_USERNAME` (por defecto `postgres`)
- `SPRING_DATASOURCE_PASSWORD` (por defecto `123456`)

---

## 3. Comandos para Ejecutar desde la Terminal

Todos los comandos se ejecutan situándose en la carpeta `backend`:

```bash
cd /Users/macbook/Documents/Proyectos/java/prev_fraude_bancario/backend
```

### Paso A. Dar permisos de ejecución al Wrapper (solo una vez)
*(Ya configurado en este entorno)*
```bash
chmod +x ./mvnw
```

---

### Paso B. Compilar el proyecto
Descarga las dependencias y compila las clases Java:
```bash
./mvnw clean compile
```

---

### Paso C. Ejecutar pruebas y verificar base de datos / migraciones
Ejecuta las pruebas unitarias y aplica automáticamente las migraciones pendientes de Flyway (`V1__init.sql`):
```bash
./mvnw test
```

---

### Paso D. Iniciar la aplicación en modo desarrollo
Levanta el servidor Spring Boot en el puerto `8080`:
```bash
./mvnw spring-boot:run
```

Para detener la ejecución en la terminal, presiona `Ctrl + C`.

---

### Paso E. Empaquetar y ejecutar como archivo JAR (Producción)

1. **Empaquetar el ejecutable:**
   ```bash
   ./mvnw clean package -DskipTests
   ```

2. **Ejecutar el archivo `.jar` generado:**
   ```bash
   java -jar target/fraude-0.0.1-SNAPSHOT.jar
   ```

---

## 4. Documentación de la API con Swagger / OpenAPI

La API cuenta con soporte para OpenAPI 3 y Swagger UI interactivo gracias a `springdoc-openapi-starter-webmvc-ui`.

Una vez iniciada la aplicación (`./mvnw spring-boot:run`), puedes acceder a:

- **Swagger UI (Interfaz gráfica interactiva):**  
  👉 [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)  
  *(o directamente en `http://localhost:8080/swagger-ui/index.html`)*

- **Especificación OpenAPI (Formato JSON):**  
  👉 [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### Características de la Configuración de Swagger
- **Soporte de Autenticación JWT:** Incluye el botón **Authorize** en Swagger UI para probar endpoints protegidos enviando el Bearer Token (`Bearer <jwt>`).
- **Anotaciones para Documentar Endpoints:**
  - `@Tag(name = "...", description = "...")`: Agrupa endpoints por módulo.
  - `@Operation(summary = "...", description = "...")`: Describe la función del endpoint.
  - `@ApiResponse(responseCode = "...", description = "...")`: Documenta los códigos de respuesta esperados.
- **Endpoint de prueba activo:**  
  `GET /api/v1/health` para comprobar conectividad y estado del servicio.

---

---

## 5. Pruebas Automatizadas con Postman

Se incluye una colección lista para importar en:
📁 `backend/postman/fraud_detection_postman_collection.json`

### Contenido de la Colección:
1. **00. Health & Diagnóstico:** `GET /api/v1/health` y `GET /v3/api-docs`.
2. **01. Autenticación (JWT):** Login para Analista (`analista_seguridad`) y Administrador (`admin_fraude`), extrayendo automáticamente el token en `{{authToken}}`.
3. **02. Transacciones (Scoring & Detección):**
   - **Happy Path:** Transacción legítima aprobada (Score < 30).
   - **No Happy Path A:** Fraude crítico bloqueado (Score > 70) con emisión de alerta WebSocket.
   - **No Happy Path B1, B2, B3:** Manejo estandarizado de errores RFC 7807 (monto inválido, saldo insuficiente, cuenta inexistente).
4. **03. Alertas Antifraude:** Listado, filtro por estado, consulta por ID y actualización de estado.
5. **04. Casos de Investigación:** Apertura de caso vinculado a la alerta, registro de evidencias en bitácora y dictamen de cierre.
6. **05. WebSockets en Tiempo Real:** Verificación HTTP de SockJS (`GET /ws-fraud/info`) y conexión STOMP.

---

## 6. WebSockets en Tiempo Real (STOMP para Frontend Angular)

Cuando una transacción resulta en nivel de riesgo `ALTO` o `CRITICO`, el backend emite una alerta instantánea vía WebSocket para reflejarla en el dashboard de los analistas en Angular.

### Parámetros de Conexión:
- **Endpoint SockJS / WebSocket:** `http://localhost:8080/ws-fraud` (o `ws://localhost:8080/ws-fraud`)
- **Tópico de Suscripción:** `/topic/alertas`
- **CORS:** Habilitado para `http://localhost:4200` y desarrollo.

### Cómo Probar WebSockets en Postman:
1. Abre Postman y haz clic en **New** > **WebSocket Request**.
2. URL: `ws://localhost:8080/ws-fraud`. Haz clic en **Connect**.
3. Envía el frame de conexión STOMP:
   ```text
   CONNECT
   accept-version:1.2,1.1,1.0
   heart-beat:10000,10000

   ^@
   ```
4. Envía el frame de suscripción al tópico:
   ```text
   SUBSCRIBE
   id:sub-0
   destination:/topic/alertas

   ^@
   ```
5. En otra pestaña de Postman, ejecuta la petición **"2. No Happy Path A - Fraude Crítico"**. Verás llegar el JSON de la alerta en tiempo real en la ventana de WebSocket.

---

## 7. Estado de Verificación y Diagnóstico

| Componente | Estado | Detalle |
|---|---|---|
| **Java JDK** | ✅ Operativo | Oracle JDK 21.0.2 |
| **Maven Wrapper** | ✅ Operativo | Permisos `+x` otorgados y dependencias descargadas |
| **PostgreSQL (Docker)** | ✅ Operativo | Contenedor `postgres_18_vector` en puerto `5434` |
| **Base de Datos `fraude_db`** | ✅ Creada | 18 tablas maestras y transaccionales (Flyway V2 y V3) |
| **Motor de Reglas SOLID** | ✅ 8 Reglas | Strategy Pattern (`RF001` a `RF008`) + Scoring Engine |
| **Seguridad & JWT** | ✅ Operativo | Spring Security con BCrypt y Bearer Tokens |
| **WebSockets STOMP** | ✅ Operativo | Notificaciones reactivas a `/topic/alertas` |
| **Swagger / OpenAPI** | ✅ Configurado | Springdoc 3.1.1 en `/swagger-ui.html` |
| **Postman Collection** | ✅ Completa | Pruebas end-to-end + SockJS / WebSockets |
| **Suite de Pruebas** | ✅ `BUILD SUCCESS` | 8/8 tests pasando al 100% (`mvnw test`) |

