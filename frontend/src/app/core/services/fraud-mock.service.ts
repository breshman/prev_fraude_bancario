import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { delay } from 'rxjs/operators';

export interface FraudAlert {
  id: string;
  transactionId: string;
  customerName: string;
  amount: number;
  riskScore: number;
  riskLevel: 'BAJO' | 'MEDIO' | 'ALTO' | 'CRITICO';
  status: 'NUEVA' | 'EN_REVISION' | 'CERRADA';
  date: Date;
  reasons: string[];
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

  private mockAlerts: FraudAlert[] = [
    {
      id: 'ALRT-001',
      transactionId: 'TX-10492',
      customerName: 'Juan Pérez',
      amount: 18500,
      riskScore: 92,
      riskLevel: 'CRITICO',
      status: 'NUEVA',
      date: new Date(),
      reasons: ['País no habitual', 'Monto inusual']
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
      reasons: ['Alta frecuencia']
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
      reasons: ['Dispositivo nuevo']
    }
  ];

  constructor() { }

  getDashboardMetrics(): Observable<DashboardMetrics> {
    const metrics: DashboardMetrics = {
      totalAlerts: 145,
      criticalAlerts: 12,
      avgRiskScore: 34.5,
      monitoredTransactions: 8940
    };
    return of(metrics).pipe(delay(800)); // Simular latencia de red
  }

  getRecentAlerts(): Observable<FraudAlert[]> {
    return of(this.mockAlerts).pipe(delay(1000));
  }
}
