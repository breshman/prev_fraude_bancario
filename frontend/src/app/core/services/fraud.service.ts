import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, BehaviorSubject, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { AlertaBackendEntity, FraudAlert, DashboardMetrics } from '../models/fraud-alert.model';
export type { FraudAlert, DashboardMetrics, AlertaBackendEntity };

@Injectable({
  providedIn: 'root'
})
export class FraudService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/alertas`;

  // Inicializado vacío: los datos se cargan exclusivamente desde la base de datos de Spring Boot
  private alertsSubject = new BehaviorSubject<FraudAlert[]>([]);

  private monitoredCount = 0;

  private metricsSubject = new BehaviorSubject<DashboardMetrics>({
    totalAlerts: 0,
    criticalAlerts: 0,
    avgRiskScore: 0,
    monitoredTransactions: 0
  });

  constructor() {
    this.cargarAlertasDesdeBackend().subscribe();
  }

  getDashboardMetrics(): Observable<DashboardMetrics> {
    return this.metricsSubject.asObservable();
  }

  getRecentAlerts(): Observable<FraudAlert[]> {
    return this.alertsSubject.asObservable();
  }

  /**
   * Carga las alertas directamente desde la base de datos vía Spring Boot (GET /api/v1/alertas)
   */
  cargarAlertasDesdeBackend(estado?: string): Observable<FraudAlert[]> {
    let params = new HttpParams();
    if (estado) {
      params = params.set('estado', estado);
    }

    return this.http.get<AlertaBackendEntity[]>(this.baseUrl, { params }).pipe(
      map(entities => (entities && entities.length > 0) ? entities.map(e => this.mapBackendToUiAlert(e)) : []),
      tap(backendAlerts => {
        this.alertsSubject.next(backendAlerts);
        this.refreshMetrics(backendAlerts);
      }),
      catchError(() => {
        return of(this.alertsSubject.value);
      })
    );
  }

  /**
   * Actualiza el estado de una alerta en la base de datos (PATCH /api/v1/alertas/{id}/estado)
   */
  actualizarEstadoAlerta(alertId: string, status: FraudAlert['status'], comentario?: string): Observable<any> {
    const current = this.alertsSubject.value;
    const target = current.find(a => a.id === alertId);

    // Actualización local
    const updated = current.map(alert =>
      alert.id === alertId ? { ...alert, status } : alert
    );
    this.alertsSubject.next(updated);
    this.refreshMetrics(updated);

    if (target && target.idAlertaNum) {
      const backendStatus = status === 'BLOQUEADA' ? 'CONFIRMADA' :
                            (status === 'APROBADA' ? 'DESCARTADA' :
                            (status === 'RETENIDA' ? 'EN_REVISION' : status));

      return this.http.patch(`${this.baseUrl}/${target.idAlertaNum}/estado`, {
        estado: backendStatus,
        comentario: comentario || `Estado actualizado a ${status} por el analista en consola central.`
      }).pipe(
        catchError(err => {
          console.warn('Fallo en sincronización con backend:', err);
          return of(null);
        })
      );
    }

    return of(null);
  }

  updateAlertStatus(alertId: string, status: FraudAlert['status']): void {
    this.actualizarEstadoAlerta(alertId, status).subscribe();
  }

  addAlert(alert: FraudAlert): void {
    const current = this.alertsSubject.value;
    if (current.some(a => a.transactionId === alert.transactionId || a.id === alert.id)) {
      return;
    }
    const updated = [alert, ...current];
    this.alertsSubject.next(updated);
    this.refreshMetrics(updated);
  }

  addAlerts(newAlerts: FraudAlert[]): void {
    const current = this.alertsSubject.value;
    const existingTxIds = new Set(current.map(a => a.transactionId));
    const toAdd = newAlerts.filter(a => !existingTxIds.has(a.transactionId));
    if (toAdd.length === 0) return;

    const updated = [...toAdd, ...current];
    this.alertsSubject.next(updated);
    this.refreshMetrics(updated);
  }

  setMonitoredTransactionsCount(count: number): void {
    this.monitoredCount = count;
    const currentMetrics = this.metricsSubject.value;
    this.metricsSubject.next({
      ...currentMetrics,
      monitoredTransactions: count
    });
  }

  incrementMonitoredTransactions(count: number = 1): void {
    this.monitoredCount += count;
    const currentMetrics = this.metricsSubject.value;
    this.metricsSubject.next({
      ...currentMetrics,
      monitoredTransactions: this.monitoredCount
    });
  }

  private refreshMetrics(alerts: FraudAlert[]): void {
    const critical = alerts.filter(a => a.riskLevel === 'CRITICO' && a.status !== 'CERRADA' && a.status !== 'APROBADA').length;
    const totalScore = alerts.reduce((acc, a) => acc + a.riskScore, 0);
    const avgScore = alerts.length > 0 ? Math.round((totalScore / alerts.length) * 10) / 10 : 0;
    const currentMetrics = this.metricsSubject.value;

    this.metricsSubject.next({
      ...currentMetrics,
      totalAlerts: alerts.length,
      criticalAlerts: critical,
      avgRiskScore: avgScore,
      monitoredTransactions: Math.max(this.monitoredCount, alerts.length)
    });
  }

  private mapBackendToUiAlert(entity: AlertaBackendEntity): FraudAlert {
    const score = Number(entity.evaluacion?.scoreFraude || (entity.nivelPrioridad === 'CRITICA' ? 92 : 65));
    const riskLevel: FraudAlert['riskLevel'] =
      entity.nivelPrioridad === 'CRITICA' ? 'CRITICO' :
      (entity.nivelPrioridad === 'ALTA' ? 'ALTO' :
      (entity.nivelPrioridad === 'MEDIA' ? 'MEDIO' : 'BAJO'));

    let uiStatus: FraudAlert['status'] = 'NUEVA';
    if (entity.estadoAlerta === 'EN_REVISION') uiStatus = 'EN_REVISION';
    else if (entity.estadoAlerta === 'CONFIRMADA') uiStatus = 'BLOQUEADA';
    else if (entity.estadoAlerta === 'DESCARTADA') uiStatus = 'APROBADA';

    const clienteNombre = entity.transaccion?.numeroCuentaOrigen ? `Cuenta ${entity.transaccion.numeroCuentaOrigen}` : 'Cliente Banco';

    return {
      id: `ALRT-${String(entity.idAlerta).padStart(3, '0')}`,
      idAlertaNum: entity.idAlerta,
      transactionId: entity.transaccion?.codigoTransaccion || `TX-${entity.idAlerta}`,
      customerName: clienteNombre,
      amount: Number(entity.transaccion?.monto || 0),
      riskScore: score,
      riskLevel,
      status: uiStatus,
      date: entity.fechaGeneracion ? new Date(entity.fechaGeneracion) : new Date(),
      reasons: [entity.descripcion || entity.tipoAlerta || 'Alerta registrada por motor de reglas antifraude'],
      origin: 'SISTEMA'
    };
  }
}
