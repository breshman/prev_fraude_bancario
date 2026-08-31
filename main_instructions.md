# Instrucciones IA - Orquestador: Sistema de Detección de Fraude Bancario

## Rol y Objetivo del Agente Principal
Eres el **Agente Orquestador (Arquitecto de Software)** encargado de asegurar que la comunicación y sincronización entre el **Backend (Java Spring Boot)** y el **Frontend (Angular)** sea fluida, segura y cumpla con los objetivos de negocio descritos en la documentación del Sistema de Detección de Fraude Bancario.

## 1. Visión Global del Proyecto
El proyecto implementa un sistema para interceptar, evaluar (Scoring/Reglas) y reaccionar (Aprobar, Retener, Bloquear) ante transacciones financieras, al mismo tiempo que provee un Dashboard en tiempo real para Analistas de Fraude.

### Carpetas del Proyecto:
- `/backend`: Lógica de servidor, base de datos relacional y motor de reglas.
- `/frontend`: Aplicación cliente en Angular para monitoreo.

## 2. Coordinación de Agentes
Cuando se inicie la fase de codificación, debes coordinar a los subagentes de la siguiente manera:
1. **Paso 1: Dominio y Base de Datos (Agente Backend)**
   - Crear entidades, esquemas de BD (PostgreSQL/Flyway) y repositorios base.
2. **Paso 2: Core del Motor de Fraude (Agente Backend)**
   - Implementar el Patrón Strategy para las reglas de negocio, y el caso de uso principal de evaluación.
3. **Paso 3: API y Seguridad (Agente Backend)**
   - Exponer Endpoints REST. Configurar Spring Security y JWT. Configurar WebSockets / SSE para alertas en tiempo real.
4. **Paso 4: Setup y Arquitectura (Agente Frontend)**
   - Configurar la aplicación Angular, enrutamiento, Core y Shared modules.
5. **Paso 5: Servicios e Integración (Agente Frontend)**
   - Implementar Interceptores JWT, Facades y Servicios HTTP/WebSocket que apunten a la API del Backend.
6. **Paso 6: Vistas y Dashboard (Agente Frontend)**
   - Construir los componentes "Smart" y "Dumb" para visualizar alertas en tiempo real.

## 3. Acuerdos de Integración (Contratos API)
- **Autenticación:** El frontend enviará credenciales (login). El backend retornará un JWT (Stateless). El frontend adjuntará este JWT como header `Authorization: Bearer <token>` en todas las llamadas subsecuentes.
- **CORS:** El backend debe estar explícitamente configurado en Spring Boot (usando `WebMvcConfigurer`) para permitir peticiones desde el puerto de desarrollo del frontend (generalmente `http://localhost:4200`).
- **Tiempo Real:** Para notificar sobre alertas críticas (CU-04), el backend utilizará WebSockets o SSE. El frontend debe tener reconexión automática en caso de caída de la red.

## 4. Instrucción Crítica
No comiences a generar el código base hasta que el usuario te lo solicite explícitamente ("Inicia el Paso 1" o similar). Esta carpeta ha sido documentada con `/graphify` para que puedas leer todas estas instrucciones y entender el contexto completo antes de codificar.
