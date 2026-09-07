import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';

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
export class FraudMockService {

  private alertsSubject = new BehaviorSubject<FraudAlert[]>([
    {
      id: 'ALRT-001',
      transactionId: 'TX-10492',
      customerName: 'Juan Pérez',
      amount: 18500,
      riskScore: 92,
      riskLevel: 'CRITICO',
      status: 'NUEVA',
      date: new Date(),
      reasons: ['País no habitual', 'Monto inusual'],
      origin: 'SISTEMA'
    },
    {
      id: 'ALRT-002',
      transactionId: 'TX-10493',
      customerName: 'María García',
      amount: 4500,
      riskScore: 65,
      riskLevel: 'ALTO',
      status: 'EN_REVISION',
      date: new Date(Date.now() - 3600000), // Hace 1 hora
      reasons: ['Alta frecuencia'],
      origin: 'SISTEMA'
    },
    {
      id: 'ALRT-003',
      transactionId: 'TX-10494',
      customerName: 'Carlos López',
      amount: 800,
      riskScore: 45,
      riskLevel: 'MEDIO',
      status: 'NUEVA',
      date: new Date(Date.now() - 7200000), // Hace 2 horas
      reasons: ['Dispositivo nuevo'],
      origin: 'SISTEMA'
    }
  ]);

  private baseMonitored = 8940;
  private extraMonitored = 0;

  private metricsSubject = new BehaviorSubject<DashboardMetrics>({
    totalAlerts: 145,
    criticalAlerts: 12,
    avgRiskScore: 34.5,
    monitoredTransactions: 8940
  });

  constructor() { }

  getDashboardMetrics(): Observable<DashboardMetrics> {
    return this.metricsSubject.asObservable();
  }

  getRecentAlerts(): Observable<FraudAlert[]> {
    return this.alertsSubject.asObservable();
  }

  addAlert(alert: FraudAlert): void {
    const current = this.alertsSubject.value;
    // Evitar duplicados por transactionId
    if (current.some(a => a.transactionId === alert.transactionId)) {
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

  updateAlertStatus(alertId: string, status: FraudAlert['status']): void {
    const current = this.alertsSubject.value;
    const updated = current.map(alert =>
      alert.id === alertId ? { ...alert, status } : alert
    );
    this.alertsSubject.next(updated);
    this.refreshMetrics(updated);
  }

  incrementMonitoredTransactions(count: number): void {
    this.extraMonitored += count;
    const currentMetrics = this.metricsSubject.value;
    this.metricsSubject.next({
      ...currentMetrics,
      monitoredTransactions: this.baseMonitored + this.extraMonitored
    });
  }

  private refreshMetrics(alerts: FraudAlert[]): void {
    const critical = alerts.filter(a => a.riskLevel === 'CRITICO' && a.status !== 'CERRADA' && a.status !== 'APROBADA').length;
    const totalScore = alerts.reduce((acc, a) => acc + a.riskScore, 0);
    const avgScore = alerts.length > 0 ? Math.round((totalScore / alerts.length) * 10) / 10 : 34.5;
    const currentMetrics = this.metricsSubject.value;

    this.metricsSubject.next({
      ...currentMetrics,
      totalAlerts: 145 + (alerts.length - 3),
      criticalAlerts: Math.max(12, critical),
      avgRiskScore: avgScore,
      monitoredTransactions: this.baseMonitored + this.extraMonitored
    });
  }
}
