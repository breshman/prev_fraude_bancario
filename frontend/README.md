# Sistema de Prevención de Fraude Bancario - Frontend

Esta es la aplicación web para los analistas de seguridad, diseñada para monitorear, auditar y gestionar las alertas de fraude generadas por el motor de evaluación.

## Arquitectura y Principios

Este proyecto de Angular implementa:
- **Standalone Components:** Arquitectura modular moderna sin `NgModules`.
- **Patrón Facade:** Toda la comunicación HTTP y WebSockets pasa por un servicio Facade, manteniendo a los componentes limpios y enfocados en la UI.
- **Estado Reactivo (RxJS):** Se aprovechan los Observables (y el `async` pipe en los templates) para propagar los cambios en tiempo real, evitando errores de Change Detection y garantizando el flujo unidireccional.
- **Smart & Dumb Components:** Separación lógica entre contenedores que manejan estado (Smart) y componentes puramente visuales (Dumb).

## Stack Tecnológico
- Angular (v22)
- Ng-Bootstrap (Componentes UI y Grid)
- SCSS
- RxJS

## Estructura del Proyecto

```text
src/app/
├── core/               # Interceptores (JWT), Manejadores de Errores, Servicios WebSocket
├── shared/             # Componentes, directivas y pipes reutilizables
├── features/           # Módulos Funcionales (Dashboard, Alertas, Transacciones)
└── layout/             # Componentes de estructura global (Sidebar, Navbar)
```

## Comandos de Desarrollo

Levantar servidor de desarrollo:
```bash
ng serve
```
La aplicación estará disponible en `http://localhost:4200/`. La página se recargará automáticamente si haces cambios en el código.

Compilar para producción:
```bash
ng build
```
Los artefactos se almacenarán en el directorio `dist/`.
