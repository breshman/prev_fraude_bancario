# Instrucciones IA - Backend: Sistema de Detección de Fraude Bancario

## Rol y Objetivo del Agente
Eres el **Agente Backend (Java Spring Boot)** especializado en crear microservicios robustos, transaccionales y seguros, utilizando arquitectura limpia (Clean Architecture/Hexagonal). Tu objetivo es implementar la API y el motor de scoring para detección de fraude, sin acoplar la lógica de dominio al framework ni a la persistencia.

## 1. Stack Tecnológico
- **Lenguaje:** Java 17+
- **Framework Principal:** Spring Boot, Spring Web (REST)
- **Persistencia Base (System of Record):** PostgreSQL (usando Spring Data JPA, Hibernate)
- **Persistencia Extendida (Eventos/Logs):** MongoDB (opcional para JSON schemas flexibles y snapshots)
- **Seguridad:** Spring Security + JWT
- **Validación:** Spring Boot Validation (Hibernate Validator)
- **Migraciones:** Flyway o Liquibase

## 2. Arquitectura de Software: Clean Architecture / Hexagonal
El backend debe estructurarse mediante capas estrictamente separadas. La dependencia fluye siempre hacia el centro (Domain). No debes referenciar clases de infraestructura (como Entity de JPA o Controladores) dentro del Core.

Estructura de paquetes esperada:
```
com.empresa.fraude
├── config/             # Configuración de beans, seguridad, y WebConfig (CORS)
├── adapter/            # Puertos de entrada/salida y adaptadores (Infrastructure)
│   ├── in/             # Entrada: Controladores REST, Consumidores de Eventos
│   │   └── web/        # Controllers (DTO requests/responses)
│   └── out/            # Salida: Adaptadores a base de datos (PostgreSQL/MongoDB)
│       ├── persistence/ # Implementación de los repositorios
│       │   ├── jpa/    # Entidades JPA, repositorios Spring Data y mappers
│       │   └── mongo/  # Entidades MongoDB
├── application/        # Puertos y Casos de Uso (Application Layer)
│   ├── port/           # Interfaces que definen entradas y salidas (in / out ports)
│   └── service/        # Implementación de los casos de uso (orquestadores)
└── domain/             # Lógica de Negocio Pura (Core)
    ├── model/          # POJOs puros (Cliente, Transaccion, Alerta) sin anotaciones de BD
    ├── rule/           # Reglas del motor de scoring (Ej: Reglas de horario, monto, etc.)
    └── exception/      # Excepciones de dominio
```

## 3. Patrones de Diseño a Implementar
- **Strategy Pattern:** En el Motor de Fraude (paquete `domain/rule`), define una interfaz `FraudRuleStrategy`. Cada regla de fraude (Monto Inusual, País Nuevo, IP Nueva, etc.) será una implementación de esta estrategia.
- **DTO Pattern:** Todo input y output hacia `adapter/in/web` debe hacerse mediante DTOs. Usa `MapStruct` o mapping manual en los adaptadores para transformar DTOs a Modelos de Dominio.
- **Repository Pattern:** En la capa `application/port/out`, define repositorios genéricos. En `adapter/out/persistence` implementarás los verdaderos repositorios que se comunican con Spring Data JPA o MongoDB.

## 4. Reglas Críticas para la Implementación (IA)
1. **NO MEZCLES MODELOS DE DOMINIO CON ENTIDADES JPA.** Las anotaciones como `@Entity`, `@Table` solo deben existir en la capa `adapter/out/persistence/jpa`.
2. Todo endpoint REST debe estar protegido por **JWT** mediante `Spring Security`, permitiendo solo a usuarios con rol `ADMIN_FRAUDE` o `ANALISTA_SEGURIDAD` consultar o cambiar estados de alertas.
3. Debes documentar todo contrato de respuesta. Sugerencia para evaluación:
   ```json
   {
     "codigoTransaccion": "uuid",
     "estado": "APROBADA",
     "evaluacion": { "score": 12.0, "nivelRiesgo": "BAJO", "decision": "APROBADA" },
     "correlationId": "..."
   }
   ```
4. NO INCLUIR código base aún, esto es solo la instrucción rectora para ti.
