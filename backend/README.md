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

- **Contenedor Docker:** `postgres_18_vector`
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

## 5. Estado de Verificación y Diagnóstico

| Componente | Estado | Detalle |
|---|---|---|
| **Java JDK** | ✅ Operativo | Oracle JDK 21.0.2 detectado |
| **Maven Wrapper** | ✅ Operativo | Permisos `+x` otorgados y dependencias descargadas |
| **PostgreSQL (Docker)** | ✅ Operativo | Contenedor `postgres_18_vector` en puerto `5434` |
| **Base de Datos `fraude_db`** | ✅ Creada | Creada e inicializada |
| **Migraciones Flyway (`V1`)** | ✅ Aplicadas | Tablas `cliente`, `transaccion`, `alerta_fraude` creadas |
| **Swagger / OpenAPI** | ✅ Configurado | Springdoc 3.1.1 en `/swagger-ui.html` y `/v3/api-docs` |
| **Compilación & Tests** | ✅ `BUILD SUCCESS` | Contexto de Spring Boot verificado exitosamente |

