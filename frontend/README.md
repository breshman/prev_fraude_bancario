# 🏦 Sistema de Prevención y Detección de Fraude Bancario - Frontend

Aplicación web para analistas de ciberseguridad y prevención de fraude financiero, construida en **Angular (v22)** con **Standalone Components**, **Ng-Bootstrap**, **Chart.js** y arquitectura reactiva orientada a eventos con **RxJS**.

La aplicación opera con la moneda oficial **Soles Peruanos (`S/`)**, permitiendo la inspección forense de transacciones, calibración interactiva de umbrales de riesgo, ejecución de simulaciones masivas y exportación de auditorías a Excel.

---

## 📑 Tabla de Contenidos
1. [Guía de Uso de la Aplicación](#-guía-de-uso-de-la-aplicación)
   - [Dashboard de Seguridad](#1-dashboard-de-seguridad)
   - [Calibración Dinámica de Umbrales](#2-calibración-dinámica-de-umbrales)
   - [Módulo de Simulación y Telemetría](#3-módulo-de-simulación-y-telemetría)
   - [Gestión y Acciones Masivas](#4-gestión-y-acciones-masivas)
   - [Exportación de Reportes a Excel](#5-exportación-de-reportes-a-excel)
2. [Stack Tecnológico y Arquitectura](#-stack-tecnológico-y-arquitectura)
3. [🔌 Guía de Integración con el Backend (Spring Boot)](#-guía-de-integración-con-el-backend-spring-boot)
   - [Resumen: ¿Qué archivos se deben modificar o crear?](#resumen-qué-archivos-se-deben-modificar-o-crear)
   - [Paso 1: Variables de Entorno (`environments/`)](#paso-1-variables-de-entorno)
   - [Paso 2: Habilitar HttpClient en `app.config.ts`](#paso-2-habilitar-httpclient-en-appconfigts)
   - [Paso 3: Interceptor JWT para Seguridad](#paso-3-interceptor-jwt-para-seguridad)
   - [Paso 4: Conectar el Servicio de Alertas y Métricas](#paso-4-conectar-el-servicio-de-alertas-y-métricas)
   - [Paso 5: Conexión en Tiempo Real (WebSocket / SSE)](#paso-5-conexión-en-tiempo-real-websocket--sse)
   - [Paso 6: Conectar el Servicio de Simulación](#paso-6-conectar-el-servicio-de-simulación)
4. [📡 Contratos de API Backend (Endpoints Esperados)](#-contratos-de-api-backend-endpoints-esperados)
5. [Comandos de Ejecución](#-comandos-de-ejecución)

---

## 🖥️ Guía de Uso de la Aplicación

### 1. Dashboard de Seguridad
* **Tarjetas de KPIs:** Monitorea en tiempo real el total de alertas activas, alertas críticas no resueltas, score promedio de riesgo del sistema y transacciones totales analizadas.
* **Gráfico de Dispersión y Umbral (Chart.js):**
  * Visualiza los scores de riesgo (0 a 100) en el eje Y vs las transacciones en el eje X.
  * Los puntos rojos representan transacciones que superaron el umbral y dispararon alertas.
  * Incluye una línea horizontal guía de corte calibrable (`Línea de Umbral`).
  * Los tooltips interactivos detallan: ID, cliente, score, nivel, estado, monto en Soles (`S/`), canal y reglas violadas.
* **Tabla de Alertas Recientes con Modal de Inspección Forense:**
  * Al hacer clic en cualquier fila o en el botón `🔍 Ver Detalle`, se abre un **Modal de Inspección Detallada** con fondo difuminado.
  * Muestra telemetría completa: IP, país, ciudad, canal, dispositivo, reglas disparadas e identificador de origen (`⚡ Simulación` vs `🏦 Sistema Central`).
  * Permite ejecutar resoluciones operativas inmediatas:
    * `✓ Aprobar`: Cierra la alerta y autoriza la transacción.
    * `⏸️ Retener`: Coloca la operación en cuarentena preventiva para análisis.
    * `⛔ Bloquear`: Bloquea la transacción y registra la alerta como confirmación de fraude.

### 2. Calibración Dinámica de Umbrales
* Permite al analista ajustar el umbral de corte del motor de reglas mediante un control deslizante (**Slider de 10 a 95 puntos**).
* Dispone de tres botones de calibración rápida:
  * **Conservador (50 pts):** Mayor sensibilidad, genera alertas preventivas ante cualquier sospecha media.
  * **Estándar (70 pts):** Recomendado para operaciones bancarias habituales.
  * **Agresivo (85 pts):** Solo alerta sobre anomalías de alta certeza o transacciones críticas.
* El ajuste recalcula al instante en memoria todas las alertas generadas y actualiza el gráfico en tiempo real sin recargar la página.

### 3. Módulo de Simulación y Telemetría
* **Inyección Manual:** Permite ingresar una transacción individual con parámetros personalizados (monto en `S/`, canal, geolocalización, dispositivo, flags nocturnos o de frecuencia rápida).
* **Generación Aleatoria Masiva:** Botones directos para disparar lotes de **10**, **25** o **50** transacciones aleatorias que simulan comportamiento bancario y se propagan automáticamente al Dashboard y a la lista de Alertas.
* **Escenarios Predeterminados:**
  * *Tráfico Mixto:* Distribución realista con mayoría de transacciones legítimas y anomalías ocasionales.
  * *Ataque Crítico / Phishing:* Alta concentración de montos elevados en Soles, IPs extranjeras e inicios de sesión sospechosos.
  * *Operación Estándar:* Operaciones seguras dentro de umbrales habituales.
* **Streaming en Vivo:** Activa un generador continuo en segundo plano (emulando telemetría de transacciones entrantes cada 2.5 segundos con animaciones pulsantes y actualización de métricas).

### 4. Gestión y Acciones Masivas
* **Filtros Rápidos:** Filtrar por transacciones que superaron el umbral (`Solo Alertas Disparadas`), aprobadas, retenidas, bloqueadas o búsqueda libre por ID, cliente, cuenta o país.
* **Selección Múltiple:** Checkbox maestro en el encabezado de la tabla para seleccionar todas las transacciones visibles.
* **Barra de Acciones por Lote:**
  * Aprobar seleccionadas.
  * Retener seleccionadas.
  * Bloquear seleccionadas.
  * Escalar alertas críticas seleccionadas a la bandeja oficial.
  * Eliminar seleccionadas del simulador.

### 5. Exportación de Reportes a Excel
* **Excel Nativo Estilizado (`.xls` XML Spreadsheet):** Genera un archivo con formato corporativo bancario, encabezados azul marino oscuro, celdas formateadas con moneda `S/ #,##0.00`, colores condicionales según el nivel de riesgo y marcas de estado.
* **CSV Plano (`.csv` UTF-8 con BOM):** Compatible con herramientas de análisis de datos como Python, R, PowerBI o Tableau.

---

## 🛠️ Stack Tecnológico y Arquitectura

```text
src/app/
├── core/
│   ├── interceptors/    # Interceptor HTTP para inyección de token JWT
│   ├── models/          # Interfaces TypeScript (Alertas, Transacciones, Métricas)
│   └── services/        # Servicios singleton (FraudService, SimulationService, WebSocketService)
├── features/
│   ├── dashboard/       # Vista ejecutiva con KPIs, gráfico Chart.js y tabla de alertas recientes
│   ├── alerts/          # Bandeja de investigación y gestión de alertas
│   └── simulation/      # Panel de simulación masiva, calibración y exportación
├── layout/              # Navbar, Sidebar y componentes de navegación estructural
└── shared/
    ├── components/      # Componentes compartidos (FraudChartComponent)
    └── utils/           # Utilidades puras (excel-export.util.ts)
```

---

## 🔌 Guía de Integración con el Backend (Spring Boot)

Actualmente, el frontend utiliza servicios de simulación y mocks en memoria (`FraudMockService` y `SimulationService`) para permitir el desarrollo y validación visual independiente de la interfaz.

Cuando el backend en **Java Spring Boot** esté listo, sigue esta guía paso a paso para sustituir los mocks por comunicación real HTTP REST y WebSockets.

---

### Resumen: ¿Qué archivos se deben modificar o crear?

| # | Archivo | Acción | Propósito |
|---|---|---|---|
| 1 | `src/environments/environment.ts` | **Crear** | Definir URLs base del API REST y WebSocket para desarrollo. |
| 2 | `src/environments/environment.prod.ts` | **Crear** | Definir URLs para entorno de producción. |
| 3 | `src/app/app.config.ts` | **Modificar** | Registrar `provideHttpClient` y el interceptor de autenticación JWT. |
| 4 | `src/app/core/interceptors/jwt.interceptor.ts` | **Crear** | Adjuntar automáticamente el encabezado `Authorization: Bearer <token>` a cada petición HTTP. |
| 5 | `src/app/core/services/fraud.service.ts` | **Modificar / Reemplazar** | Reemplazar `fraud-mock.service.ts` con llamadas `HttpClient` a los endpoints REST de Spring Boot. |
| 6 | `src/app/core/services/websocket.service.ts` | **Crear** | Escuchar alertas y transacciones en tiempo real emitidas por Spring Boot (STOMP / SockJS o SSE). |
| 7 | `src/app/core/services/simulation.service.ts` | **Modificar** | Delegar la generación y persistencia de simulaciones al endpoint del backend. |

---

### Paso 1: Variables de Entorno

Crea la carpeta `src/environments/` y los dos archivos de configuración de entorno:

#### 📄 `src/environments/environment.ts` (Desarrollo)
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api/v1',
  wsUrl: 'http://localhost:8080/ws-fraud' // Endpoint STOMP de Spring Boot
};
```

#### 📄 `src/environments/environment.prod.ts` (Producción)
```typescript
export const environment = {
  production: true,
  apiUrl: 'https://api.tu-banco.com/api/v1',
  wsUrl: 'https://api.tu-banco.com/ws-fraud'
};
```

---

### Paso 2: Habilitar HttpClient en `app.config.ts`

Abre [app.config.ts](file:///c:/Users/bresh/Documents/gitHub/prev_fraude_bancario/frontend/src/app/app.config.ts) e incorpora `provideHttpClient` con el interceptor JWT:

```typescript
import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';

import { routes } from './app.routes';
import { jwtInterceptor } from './core/interceptors/jwt.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptors([jwtInterceptor])) // <-- Habilitar HttpClient + Interceptor
  ]
};
```

---

### Paso 3: Interceptor JWT para Seguridad

Crea el archivo `src/app/core/interceptors/jwt.interceptor.ts`:

```typescript
import { HttpInterceptorFn } from '@angular/common/http';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  // Obtén el token de sesión almacenado tras el login
  const token = sessionStorage.getItem('auth_token') || localStorage.getItem('auth_token');

  if (token) {
    const authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    return next(authReq);
  }

  return next(req);
};
```

---

### Paso 4: Conectar el Servicio de Alertas y Métricas

En [src/app/core/services/fraud-mock.service.ts](file:///c:/Users/bresh/Documents/gitHub/prev_fraude_bancario/frontend/src/app/core/services/fraud-mock.service.ts) (o creando un nuevo `fraud.service.ts`), sustituye la generación interna en memoria por llamadas a la API REST de Spring Boot:

```typescript
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface FraudAlert {
  id: string;
  transactionId: string;
  customerName: string;
  amount: number;
  riskScore: number;
  riskLevel: 'BAJO' | 'MEDIO' | 'ALTO' | 'CRITICO';
  status: 'NUEVA' | 'EN_REVISION' | 'CERRADA' | 'APROBADA' | 'RETENIDA' | 'BLOQUEADA' | 'PENDIENTE';
  date: Date;
  reasons: string[];
  origin?: 'SISTEMA' | 'SIMULACION';
}

export interface DashboardMetrics {
  totalAlerts: number;
  criticalAlerts: number;
  avgRiskScore: number;
  monitoredTransactions: number;
}

@Injectable({
  providedIn: 'root'
})
export class FraudService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/alerts`;

  private alertsSubject = new BehaviorSubject<FraudAlert[]>([]);
  public alerts$ = this.alertsSubject.asObservable();

  // 1. Obtener métricas para los KPI Cards
  getDashboardMetrics(): Observable<DashboardMetrics> {
    return this.http.get<DashboardMetrics>(`${this.baseUrl}/metrics`);
  }

  // 2. Cargar alertas recientes iniciales
  getRecentAlerts(): Observable<FraudAlert[]> {
    return this.http.get<FraudAlert[]>(`${this.baseUrl}/recent`).pipe(
      tap(alerts => this.alertsSubject.next(alerts))
    );
  }

  // 3. Actualizar estado de una alerta (Aprobar, Retener, Bloquear)
  updateAlertStatus(alertId: string, status: FraudAlert['status']): Observable<FraudAlert> {
    return this.http.patch<FraudAlert>(`${this.baseUrl}/${alertId}/status`, { status }).pipe(
      tap(updatedAlert => {
        const current = this.alertsSubject.value.map(a =>
          a.id === alertId ? { ...a, status } : a
        );
        this.alertsSubject.next(current);
      })
    );
  }

  // 4. Incorporar alertas recibidas en tiempo real (por WebSockets)
  addAlertFromStream(alert: FraudAlert): void {
    const current = this.alertsSubject.value;
    if (!current.some(a => a.transactionId === alert.transactionId)) {
      this.alertsSubject.next([alert, ...current]);
    }
  }
}
```

---

### Paso 5: Conexión en Tiempo Real (WebSocket / SSE)

Spring Boot utiliza **Spring WebSocket con STOMP**. Para conectarlo desde Angular:

1. Instala el cliente STOMP oficial:
   ```bash
   npm install @stomp/stompjs sockjs-client
   ```
2. Crea `src/app/core/services/websocket.service.ts`:

```typescript
import { Injectable, inject } from '@angular/core';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { environment } from '../../../environments/environment';
import { FraudMockService, FraudAlert } from './fraud-mock.service';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private fraudService = inject(FraudMockService);
  private stompClient!: Client;

  connect(): void {
    this.stompClient = new Client({
      webSocketFactory: () => new SockJS(environment.wsUrl),
      reconnectDelay: 5000, // Reconexión automática
      debug: (str) => console.log('[STOMP]:', str)
    });

    this.stompClient.onConnect = () => {
      console.log('✅ Conectado al canal WebSocket de Spring Boot');

      // Suscribirse al topic de alertas de fraude en tiempo real
      this.stompClient.subscribe('/topic/alerts', (message) => {
        const newAlert: FraudAlert = JSON.parse(message.body);
        newAlert.date = new Date(newAlert.date);
        this.fraudService.addAlert(newAlert);
      });
    };

    this.stompClient.activate();
  }

  disconnect(): void {
    if (this.stompClient) {
      this.stompClient.deactivate();
    }
  }
}
```

> **Alternativa con Server-Sent Events (SSE):** Si el backend usa `SseEmitter` en lugar de WebSockets, basta con usar la API nativa de JavaScript `new EventSource(`${environment.apiUrl}/alerts/stream`)` sin necesidad de librerías adicionales.

---

### Paso 6: Conectar el Servicio de Simulación

En [src/app/core/services/simulation.service.ts](file:///c:/Users/bresh/Documents/gitHub/prev_fraude_bancario/frontend/src/app/core/services/simulation.service.ts):

* Sustituye el cálculo aleatorio local de scores por el llamado al motor de reglas del backend:
  ```typescript
  // Enviar transacción al motor de evaluación de Spring Boot
  simulateSingleTransaction(params: SingleSimulationParams): Observable<SimulatedTransaction> {
    return this.http.post<SimulatedTransaction>(`${environment.apiUrl}/simulation/evaluate`, params);
  }

  // Generar lote masivo en el servidor
  generateMassiveRandom(count: number): Observable<SimulatedTransaction[]> {
    return this.http.post<SimulatedTransaction[]>(`${environment.apiUrl}/simulation/batch`, { count });
  }

  // Persistir el umbral en la base de datos de Spring Boot
  setThreshold(threshold: number): Observable<void> {
    return this.http.put<void>(`${environment.apiUrl}/rules/threshold`, { threshold });
  }
  ```

---

## 📡 Contratos de API Backend (Endpoints Esperados)

A continuación se detallan los endpoints REST y canales WebSocket que el backend en Spring Boot debe exponer para integrarse con este frontend:

### 1. Endpoints REST

| Método | Endpoint | Descripción | Body Request (Ejemplo) | Response (HTTP 200) |
|---|---|---|---|---|
| `GET` | `/api/v1/alerts/metrics` | KPIs del Dashboard | Ninguno | `{ "totalAlerts": 145, "criticalAlerts": 12, "avgRiskScore": 34.5, "monitoredTransactions": 8940 }` |
| `GET` | `/api/v1/alerts/recent` | Lista de alertas recientes | Query params opcionales: `?limit=10` | `Array<FraudAlert>` |
| `PATCH` | `/api/v1/alerts/{id}/status` | Modificar estado de una alerta | `{ "status": "APROBADA" }` | `FraudAlert` actualizada |
| `POST` | `/api/v1/alerts/bulk-action` | Acción masiva sobre alertas | `{ "alertIds": ["ALRT-001"], "action": "BLOQUEAR" }` | `{ "updatedCount": 1 }` |
| `POST` | `/api/v1/simulation/evaluate` | Evaluar transacción individual | `{ "customerName": "Juan", "amount": 5400, "channel": "ONLINE", ... }` | `SimulatedTransaction` con `riskScore`, `riskLevel`, etc. |
| `POST` | `/api/v1/simulation/batch` | Inyectar lote de transacciones | `{ "count": 25, "scenario": "MIXED" }` | `Array<SimulatedTransaction>` |
| `GET` | `/api/v1/rules/threshold` | Obtener umbral activo | Ninguno | `{ "threshold": 70 }` |
| `PUT` | `/api/v1/rules/threshold` | Actualizar umbral activo | `{ "threshold": 75 }` | `{ "threshold": 75, "updatedAt": "..." }` |

### 2. Canales en Tiempo Real (WebSocket STOMP)

* **Conexión:** `ws://localhost:8080/ws-fraud` (con fallback SockJS).
* **Suscripción Alertas:** `/topic/alerts` -> Emite objetos `FraudAlert` cada vez que el motor de scoring supera el umbral configurado.
* **Suscripción Telemetría Transaccional:** `/topic/transactions` -> Emite todas las transacciones procesadas para alimentar el gráfico en tiempo real.

---

## 🚀 Comandos de Ejecución

### Requisitos Previos
* **Node.js:** Versión 20 o superior (`node -v`).
* **Angular CLI:** Instalado globalmente o mediante `npx ng`.

### Instalación de dependencias
```bash
npm install
```

### Iniciar servidor de desarrollo
```bash
ng serve
```
Disponible en: `http://localhost:4200/` con recarga automática en caliente (*hot-reload*).

### Compilación para Producción
```bash
ng build
```
Los artefactos compilados y optimizados se generarán en la carpeta `dist/frontend`.
