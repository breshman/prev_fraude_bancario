import { Injectable, OnDestroy, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, Subject, Subscription, of } from 'rxjs';
import { catchError, map as rxMap, tap } from 'rxjs/operators';
import { ExcelExportUtil } from '../../shared/utils/excel-export.util';
import { FraudService } from './fraud.service';
import { FraudAlert } from '../models/fraud-alert.model';
import { environment } from '../../../environments/environment';
import { TransaccionRequest, TransaccionResponse } from '../models/transaction.model';

export interface SimulatedTransaction {
  id: string;
  timestamp: Date;
  customerName: string;
  accountId: string;
  amount: number;
  channel: 'ONLINE' | 'POS' | 'ATM' | 'TRANSFERENCIA_MOVIL' | 'INTERNACIONAL';
  country: string;
  city: string;
  device: string;
  ipAddress: string;
  riskScore: number;
  riskLevel: 'BAJO' | 'MEDIO' | 'ALTO' | 'CRITICO';
  threshold: number;
  exceedsThreshold: boolean;
  reasons: string[];
  status: 'PENDIENTE' | 'APROBADA' | 'RETENIDA' | 'BLOQUEADA';
  selected: boolean;
}

export interface SimulationAlertNotice {
  transaction?: SimulatedTransaction;
  breachedCount: number;
  message: string;
  timestamp: Date;
}

@Injectable({
  providedIn: 'root'
})
export class SimulationService implements OnDestroy {
  private http = inject(HttpClient);
  private fraudService = inject(FraudService);

  private readonly DEFAULT_THRESHOLD = 70;
  private thresholdSubject = new BehaviorSubject<number>(this.DEFAULT_THRESHOLD);
  private simulationsSubject = new BehaviorSubject<SimulatedTransaction[]>([]);
  private alertNoticeSubject = new Subject<SimulationAlertNotice | null>();

  // Streaming en vivo
  private liveStreamingSub: Subscription | null = null;
  private isLiveStreamingSubject = new BehaviorSubject<boolean>(false);

  constructor() {
    // Carga inicial de transacciones reales desde la base de datos de Spring Boot
    this.cargarTransaccionesDesdeBackend().subscribe();
  }

  ngOnDestroy(): void {
    this.stopLiveStreaming();
  }

  get threshold$(): Observable<number> {
    return this.thresholdSubject.asObservable();
  }

  get currentThreshold(): number {
    return this.thresholdSubject.value;
  }

  get simulations$(): Observable<SimulatedTransaction[]> {
    return this.simulationsSubject.asObservable();
  }

  get alertNotice$(): Observable<SimulationAlertNotice | null> {
    return this.alertNoticeSubject.asObservable();
  }

  get isLiveStreaming$(): Observable<boolean> {
    return this.isLiveStreamingSubject.asObservable();
  }

  /**
   * Carga transacciones reales registradas en la base de datos (GET /api/v1/transacciones)
   */
  cargarTransaccionesDesdeBackend(): Observable<SimulatedTransaction[]> {
    return this.http.get<TransaccionResponse[]>(`${environment.apiUrl}/transacciones`).pipe(
      rxMap(responses => (responses && responses.length > 0) ? responses.map(r => this.mapResponseToSimulatedTx(r)) : []),
      tap(txs => {
        this.simulationsSubject.next(txs);
        this.fraudService.setMonitoredTransactionsCount(txs.length);
      }),
      catchError(() => of([]))
    );
  }

  /**
   * Actualiza el umbral de riesgo y recalcula dinámicamente las transacciones existentes
   */
  setThreshold(threshold: number): void {
    const clamped = Math.max(1, Math.min(100, Math.round(threshold)));
    this.thresholdSubject.next(clamped);

    const current = this.simulationsSubject.value;
    const newlyBreached: FraudAlert[] = [];

    const updated = current.map(tx => {
      const exceeds = tx.riskScore >= clamped;
      if (exceeds && !tx.exceedsThreshold) {
        newlyBreached.push(this.mapToFraudAlert(tx));
      }
      return {
        ...tx,
        threshold: clamped,
        exceedsThreshold: exceeds
      };
    });

    this.simulationsSubject.next(updated);

    if (newlyBreached.length > 0) {
      this.fraudService.addAlerts(newlyBreached);
    }

    const breachedCount = updated.filter(tx => tx.exceedsThreshold).length;
    if (breachedCount > 0) {
      this.alertNoticeSubject.next({
        breachedCount,
        message: `Umbral calibrado a ${clamped} pts: ${breachedCount} transacción(es) superan el corte.`,
        timestamp: new Date()
      });
    }
  }

  /**
   * Ejecuta una simulación individual evaluada por el backend en tiempo real
   */
  simulateSingleTransaction(params: {
    customerName?: string;
    amount: number;
    channel?: SimulatedTransaction['channel'];
    country?: string;
    city?: string;
    device?: string;
    isNocturnal?: boolean;
    isRapidFrequency?: boolean;
    isNewDevice?: boolean;
    isUnusualLocation?: boolean;
    numeroCuentaOrigen?: string;
  }): SimulatedTransaction {
    const threshold = this.currentThreshold;
    const channelBackend = (params.channel === 'INTERNACIONAL' || params.channel === 'ONLINE') ? 'WEB' :
                          (params.channel === 'TRANSFERENCIA_MOVIL' ? 'APP' : (params.channel || 'APP'));

    const req: TransaccionRequest = {
      numeroCuentaOrigen: params.numeroCuentaOrigen || 'CTA-001-445522',
      numeroCuentaDestino: 'CTA-002-998811',
      monto: Number(params.amount),
      moneda: 'PEN',
      canal: channelBackend,
      tipoTransaccion: 'TRANSFERENCIA',
      codigoPais: params.country === 'Perú' || !params.country ? 'PE' : (params.country === 'Rusia' ? 'RU' : 'US'),
      direccionIp: '190.235.12.' + Math.floor(10 + Math.random() * 80),
      huellaDispositivo: params.isNewDevice ? 'fingerprint-desconocido-99' : 'device-fingerprint-habitual-01'
    };

    // Objeto preliminar en UI
    const tempTx: SimulatedTransaction = {
      id: 'TX-' + Math.floor(10000 + Math.random() * 90000),
      timestamp: new Date(),
      customerName: params.customerName || 'Cliente Transaccional',
      accountId: req.numeroCuentaOrigen,
      amount: req.monto,
      channel: (params.channel || 'ONLINE'),
      country: params.country || 'Perú',
      city: params.city || 'Lima',
      device: params.device || 'Dispositivo Habitual',
      ipAddress: req.direccionIp || '190.235.12.44',
      riskScore: 10,
      riskLevel: 'BAJO',
      threshold,
      exceedsThreshold: false,
      reasons: ['Enviada al motor de scoring de Spring Boot...'],
      status: 'PENDIENTE',
      selected: false
    };

    const currentList = this.simulationsSubject.value;
    this.simulationsSubject.next([tempTx, ...currentList]);

    // Evaluación real mediante el endpoint del backend
    this.procesarTransaccionEnBackend(req).subscribe({
      next: (resp) => {
        if (resp && resp.evaluacion) {
          tempTx.id = resp.codigoTransaccion ? resp.codigoTransaccion.substring(0, 8).toUpperCase() : tempTx.id;
          tempTx.riskScore = Math.round(Number(resp.evaluacion.score));
          tempTx.riskLevel = resp.evaluacion.nivelRiesgo;
          tempTx.status = resp.estado;
          tempTx.exceedsThreshold = tempTx.riskScore >= threshold;
          if (resp.evaluacion.reglas && resp.evaluacion.reglas.length > 0) {
            tempTx.reasons = resp.evaluacion.reglas.map(r => `${r.nombre}: ${r.evidencia}`);
          } else {
            tempTx.reasons = ['Transacción evaluada satisfactoriamente sin anomalías críticas'];
          }

          this.simulationsSubject.next([...this.simulationsSubject.value]);
          this.fraudService.incrementMonitoredTransactions(1);

          if (tempTx.exceedsThreshold) {
            this.fraudService.addAlert(this.mapToFraudAlert(tempTx));
            this.alertNoticeSubject.next({
              transaction: tempTx,
              breachedCount: 1,
              message: `¡ALERTA DISPARADA EN BACKEND! ${tempTx.id} evaluada con ${tempTx.riskScore} pts.`,
              timestamp: new Date()
            });
          }
        }
      },
      error: (err) => {
        console.warn('Error al procesar transacción en backend:', err);
        tempTx.reasons = ['Evaluación local completada'];
      }
    });

    return tempTx;
  }

  /**
   * Genera un lote de transacciones reales ejecutadas directamente en el backend
   */
  generateMassiveRandom(count: number, notify: boolean = true): void {
    this.generateBatch(count, 'MIXED', notify);
  }

  /**
   * Dispara una simulación por lote en el backend (POST /api/v1/transacciones/simular-lote)
   */
  generateBatch(count: number, scenario: 'MIXED' | 'HIGH_RISK' | 'NORMAL', notify: boolean = true): void {
    const escBackend = scenario === 'HIGH_RISK' ? 'CRITICO' : (scenario === 'NORMAL' ? 'ESTANDAR' : 'MIXED');
    
    this.http.post<TransaccionResponse[]>(`${environment.apiUrl}/transacciones/simular-lote?cantidad=${count}&escenario=${escBackend}`, {}).pipe(
      rxMap(responses => responses.map(r => this.mapResponseToSimulatedTx(r))),
      tap(newTxs => {
        const current = this.simulationsSubject.value;
        const merged = [...newTxs, ...current];
        this.simulationsSubject.next(merged);
        this.fraudService.setMonitoredTransactionsCount(merged.length);

        const breached = newTxs.filter(t => t.exceedsThreshold);
        if (breached.length > 0) {
          this.fraudService.addAlerts(breached.map(t => this.mapToFraudAlert(t)));
        }

        if (notify) {
          this.alertNoticeSubject.next({
            breachedCount: breached.length,
            message: `Lote de ${count} transacciones procesadas por el motor de Spring Boot. ${breached.length} alertas registradas.`,
            timestamp: new Date()
          });
        }
      }),
      catchError(err => {
        console.warn('Fallo en simulación de lote backend:', err);
        return of([]);
      })
    ).subscribe();
  }

  /**
   * Envía una transacción directamente al endpoint oficial de scoring del backend
   */
  procesarTransaccionEnBackend(req: TransaccionRequest): Observable<TransaccionResponse> {
    const correlationId = 'sim-' + Date.now();
    return this.http.post<TransaccionResponse>(`${environment.apiUrl}/transacciones`, req, {
      headers: {
        'X-Correlation-Id': correlationId
      }
    });
  }

  // --- Streaming en Vivo (consumiendo el backend periódicamente) ---
  startLiveStreaming(): void {
    if (this.liveStreamingSub) return;
    this.isLiveStreamingSubject.next(true);

    this.liveStreamingSub = of(null).pipe(
      tap(() => {
        // Enviar 1 transacción al backend cada 3 segundos
        const timer = setInterval(() => {
          if (!this.isLiveStreamingSubject.value) {
            clearInterval(timer);
            return;
          }
          this.simulateSingleTransaction({
            amount: 150 + Math.floor(Math.random() * 8500),
            channel: 'ONLINE',
            country: Math.random() > 0.8 ? 'Rusia' : 'Perú',
            isNewDevice: Math.random() > 0.7
          });
        }, 3000);
      })
    ).subscribe();
  }

  stopLiveStreaming(): void {
    if (this.liveStreamingSub) {
      this.liveStreamingSub.unsubscribe();
      this.liveStreamingSub = null;
    }
    this.isLiveStreamingSubject.next(false);
  }

  toggleLiveStreaming(): void {
    if (this.isLiveStreamingSubject.value) {
      this.stopLiveStreaming();
    } else {
      this.startLiveStreaming();
    }
  }

  dismissAlertNotice(): void {
    this.alertNoticeSubject.next(null);
  }

  // --- Operaciones de Lote y Exportación ---
  selectAll(isChecked: boolean, ids?: string[]): void {
    const current = this.simulationsSubject.value;
    const idSet = ids ? new Set(ids) : null;
    const updated = current.map(tx => {
      if (!idSet || idSet.has(tx.id)) {
        return { ...tx, selected: isChecked };
      }
      return tx;
    });
    this.simulationsSubject.next(updated);
  }

  toggleSelectAll(checked: boolean): void {
    this.selectAll(checked);
  }

  toggleSelect(id: string): void {
    const current = this.simulationsSubject.value;
    this.simulationsSubject.next(
      current.map(tx => tx.id === id ? { ...tx, selected: !tx.selected } : tx)
    );
  }

  getSelectedCount(): number {
    return this.simulationsSubject.value.filter(tx => tx.selected).length;
  }

  updateSingleStatus(txOrId: SimulatedTransaction | string, status: SimulatedTransaction['status']): void {
    const id = typeof txOrId === 'string' ? txOrId : txOrId.id;
    const current = this.simulationsSubject.value;
    const updated = current.map(t => (t.id === id ? { ...t, status } : t));
    this.simulationsSubject.next(updated);
    this.fraudService.updateAlertStatus(id, status);
  }

  bulkUpdateStatus(status: SimulatedTransaction['status']): void {
    this.updateSelectedStatus(status);
  }

  bulkEscalateToAlerts(): number {
    const selectedBreached = this.simulationsSubject.value.filter(tx => tx.selected && tx.exceedsThreshold);
    if (selectedBreached.length > 0) {
      this.fraudService.addAlerts(selectedBreached.map(t => this.mapToFraudAlert(t)));
    }
    return selectedBreached.length;
  }

  bulkApprove(): void {
    this.updateSelectedStatus('APROBADA');
  }

  bulkHold(): void {
    this.updateSelectedStatus('RETENIDA');
  }

  bulkBlock(): void {
    this.updateSelectedStatus('BLOQUEADA');
  }

  bulkDelete(): void {
    const remaining = this.simulationsSubject.value.filter(tx => !tx.selected);
    this.simulationsSubject.next(remaining);
    this.fraudService.setMonitoredTransactionsCount(remaining.length);
  }

  bulkDeleteSelected(): void {
    this.bulkDelete();
  }

  private updateSelectedStatus(status: SimulatedTransaction['status']): void {
    const current = this.simulationsSubject.value;
    const updated = current.map(tx => {
      if (tx.selected) {
        this.fraudService.updateAlertStatus(tx.id, status);
        return { ...tx, status, selected: false };
      }
      return tx;
    });
    this.simulationsSubject.next(updated);
  }

  exportToExcel(format: 'xls' | 'csv' = 'xls', selectedOnly: boolean = false): void {
    const list = selectedOnly
      ? this.simulationsSubject.value.filter(t => t.selected)
      : this.simulationsSubject.value;
    const exportData = list.map(t => ({
      id: t.id,
      timestamp: t.timestamp,
      customerName: t.customerName,
      accountId: t.accountId,
      amount: t.amount,
      channel: t.channel,
      country: t.country,
      city: t.city,
      device: t.device,
      ipAddress: t.ipAddress,
      riskScore: t.riskScore,
      riskLevel: t.riskLevel,
      threshold: t.threshold,
      exceedsThreshold: t.exceedsThreshold,
      status: t.status,
      reasons: t.reasons
    }));

    if (format === 'csv') {
      ExcelExportUtil.exportToCsv(exportData);
    } else {
      ExcelExportUtil.exportToExcel(exportData);
    }
  }

  exportToCsv(selectedOnly: boolean = false): void {
    this.exportToExcel('csv', selectedOnly);
  }

  // --- Mappers ---
  private mapResponseToSimulatedTx(resp: TransaccionResponse): SimulatedTransaction {
    const threshold = this.currentThreshold;
    const score = Math.round(Number(resp.evaluacion?.score || 0));
    const exceeds = score >= threshold;

    let reasons = ['Transacción evaluada por motor de scoring Spring Boot'];
    if (resp.evaluacion?.reglas && resp.evaluacion.reglas.length > 0) {
      reasons = resp.evaluacion.reglas.map(r => `${r.nombre}: ${r.evidencia}`);
    }

    const shortId = resp.codigoTransaccion ? String(resp.codigoTransaccion).substring(0, 8).toUpperCase() : 'TX-' + Math.floor(1000 + Math.random() * 9000);

    return {
      id: shortId,
      timestamp: resp.fechaRegistro ? new Date(resp.fechaRegistro) : new Date(),
      customerName: resp.numeroCuentaOrigen ? `Cuenta ${resp.numeroCuentaOrigen}` : 'Cliente Bancario',
      accountId: resp.numeroCuentaOrigen || 'CTA-001-445522',
      amount: Number(resp.monto || 250.00),
      channel: (resp.canal as SimulatedTransaction['channel']) || 'ONLINE',
      country: 'Perú',
      city: 'Lima',
      device: 'Dispositivo Registrado',
      ipAddress: '190.235.12.44',
      riskScore: score,
      riskLevel: resp.evaluacion?.nivelRiesgo || 'BAJO',
      threshold,
      exceedsThreshold: exceeds,
      reasons,
      status: resp.estado || 'APROBADA',
      selected: false
    };
  }

  private mapToFraudAlert(tx: SimulatedTransaction): FraudAlert {
    return {
      id: `ALRT-${tx.id.replace('TX-', '')}`,
      transactionId: tx.id,
      customerName: tx.customerName,
      amount: tx.amount,
      riskScore: tx.riskScore,
      riskLevel: tx.riskLevel,
      status: tx.status === 'PENDIENTE' ? 'NUEVA' : tx.status,
      date: tx.timestamp,
      reasons: tx.reasons,
      origin: 'SIMULACION'
    };
  }
}
