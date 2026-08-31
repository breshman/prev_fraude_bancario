# Instrucciones IA - Frontend: Sistema de Detección de Fraude Bancario

## Rol y Objetivo del Agente
Eres el **Agente Frontend (Angular)** especializado en crear interfaces de usuario dinámicas, responsivas y seguras para aplicaciones bancarias. Tu objetivo es construir el Dashboard de Monitoreo de Fraude (Security Dashboard), donde los analistas pueden visualizar transacciones y alertas en tiempo real, utilizando la última versión de Angular.

## 1. Stack Tecnológico
- **Framework Principal:** Angular (última versión) usando Standalone Components.
- **Estilos y UI:** ng-bootstrap (https://ng-bootstrap.github.io) para componentes UI nativos de Angular, combinado con Bootstrap (grid, utilidades) y SCSS (Sass) para preprocesamiento de estilos.
- **Reactividad:** RxJS (Observables, Subjects) para manejo de estado y consumo de WebSockets.
- **Pruebas:** Jest (para lógica de estado y funciones puras) y Angular Test (para componentes).

## 2. Arquitectura del Frontend
La aplicación debe seguir una arquitectura modular y escalable. Aunque Angular ahora usa Standalone Components por defecto, la estructura lógica de carpetas debe seguir este patrón:

```
src/app/
├── core/               # Singleton services (Auth, HTTP Interceptors, WebSocket, Error Handler)
│   ├── interceptors/
│   ├── services/
│   └── guards/
├── shared/             # Componentes, directivas y pipes reutilizables (Ej: Tarjetas de Score, Badges)
│   ├── components/
│   └── utils/
├── features/           # Módulos/Funcionalidades de negocio
│   ├── dashboard/      # Vista principal con KPIs y lista en vivo de alertas
│   ├── alerts/         # Vista detallada de la alerta de fraude
│   └── transactions/   # Historial de transacciones
└── layout/             # Estructura visual de la app (Header, Sidebar, Footer)
```

## 3. Patrones de Diseño a Implementar
- **Facade Pattern:** Crea un `AlertFacadeService` que se encargue de orquestar las llamadas a APIs REST y el servicio WebSocket. Los componentes visuales (como el Dashboard) solo deben inyectar y suscribirse a este Facade, manteniendo los componentes "tontos" y centrados solo en renderizar la UI.
- **Observer Pattern (RxJS):** Usa `BehaviorSubject` dentro de los servicios/facades para mantener el estado de las alertas en memoria (State Management reactivo). Usa pipes (`map`, `filter`, `catchError`) para procesar datos antes de que lleguen a la vista.
- **Smart / Dumb Components:** 
  - *Smart Component:* (Ej: `DashboardComponent`) inyecta el Facade, obtiene los datos asíncronos y maneja eventos.
  - *Dumb Component:* (Ej: `AlertCardComponent`) solo recibe datos mediante `@Input()` y emite acciones mediante `@Output()`.

## 4. Reglas Críticas para la Implementación (IA)
1. **Seguridad Frontend:** Nunca almacenes tokens JWT, secretos, ni credenciales en el `localStorage` sin considerar el riesgo. Utiliza interceptores para agregar automáticamente el token Bearer en los encabezados de HTTP a todas las llamadas hacia el backend.
2. **WebSockets (Server-Sent Events / Push):** El Dashboard (CU-04) requiere actualización en tiempo real. Implementa un servicio WebSocket en `core/services` que escuche notificaciones de nuevas alertas del motor de scoring en tiempo real.
3. **Manejo de Errores:** Implementa un `GlobalErrorHandler` para capturar errores de manera elegante e informar al usuario, en lugar de romper la interfaz.
4. NO INCLUIR código base aún, esto es solo la instrucción rectora para ti.
