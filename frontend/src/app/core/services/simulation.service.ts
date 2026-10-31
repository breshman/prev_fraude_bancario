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
  private liveStreamingTimer: any = null;
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
   * Resuelve código de país ISO, coordenadas representativas e IP según el país o anomalía
   */
  private resolveGeoAndNetworkData(country?: string, isUnusual?: boolean): {
    codigoPais: string;
    latitud: number;
    longitud: number;
    direccionIp: string;
  } {
    const c = (country || 'Perú').toLowerCase().trim();
    const rndIp = Math.floor(10 + Math.random() * 80);

    if (c.includes('rusia') || c.includes('russia')) {
      return {
        codigoPais: 'RU',
        latitud: 55.7558,
        longitud: 37.6173,
        direccionIp: `95.173.136.${rndIp}`
      };
    }
    if (c.includes('nigeria')) {
      return {
        codigoPais: 'NG',
        latitud: 6.5244,
        longitud: 3.3792,
        direccionIp: `102.89.33.${rndIp}`
      };
    }
    if (c.includes('china')) {
      return {
        codigoPais: 'CN',
        latitud: 39.9042,
        longitud: 116.4074,
        direccionIp: `202.108.22.${rndIp}`
      };
    }
    if (c.includes('estados unidos') || c.includes('ee.uu') || c.includes('usa') || c.includes('us')) {
      return {
        codigoPais: 'US',
        latitud: 40.7128,
        longitud: -74.0060,
        direccionIp: `198.51.100.${rndIp}`
      };
    }
    if (c.includes('méxico') || c.includes('mexico')) {
      return {
        codigoPais: 'MX',
        latitud: 19.4326,
        longitud: -99.1332,
        direccionIp: `187.189.45.${rndIp}`
      };
    }
    if (c.includes('colombia')) {
      return {
        codigoPais: 'CO',
        latitud: 4.7110,
        longitud: -74.0721,
        direccionIp: `181.143.21.${rndIp}`
      };
    }

    if (isUnusual) {
      return {
        codigoPais: 'RU',
        latitud: 55.7558,
        longitud: 37.6173,
        direccionIp: `95.173.136.${rndIp}`
      };
    }

    return {
      codigoPais: 'PE',
      latitud: -12.046374,
      longitud: -77.042793,
      direccionIp: `190.235.12.${rndIp}`
    };
  }

  /**
   * Ejecuta una simulación individual evaluada por el backend en tiempo real
   */
  simulateSingleTransaction(params: {
    customerName?: string;
    amount: number;
    channel?: SimulatedTransaction['channel'] | 'APP';
    country?: string;
    city?: string;
    device?: string;
    isNocturnal?: boolean;
    isRapidFrequency?: boolean;
    isNewDevice?: boolean;
    isUnusualLocation?: boolean;
    numeroCuentaOrigen?: string;
    numeroCuentaDestino?: string;
    latitud?: number;
    longitud?: number;
    direccionIp?: string;
    codigoPais?: string;
  }): SimulatedTransaction {
    const threshold = this.currentThreshold;
    const channelBackend = (params.channel === 'INTERNACIONAL' || params.channel === 'ONLINE') ? 'WEB' :
      (params.channel === 'TRANSFERENCIA_MOVIL' ? 'APP' : (params.channel || 'APP'));

    // Resolver geolocalización e IP para permitir evaluar reglas de velocidad, viaje imposible y país
    const geoData = this.resolveGeoAndNetworkData(params.country, params.isUnusualLocation);
    const resolvedCodigoPais = params.codigoPais || geoData.codigoPais;
    const resolvedLatitud = params.latitud !== undefined ? params.latitud : geoData.latitud;
    const resolvedLongitud = params.longitud !== undefined ? params.longitud : geoData.longitud;
    const resolvedIp = params.direccionIp || geoData.direccionIp;

    // Destinatario: si es desconocido, ubicación atípica o monto alto sospechoso, asignar cuenta externa no habitual para RF008
    const isSuspiciousBeneficiary = params.isUnusualLocation || (resolvedCodigoPais !== 'PE') || (Number(params.amount) > 5000 && params.isNewDevice);
    const resolvedDestino = params.numeroCuentaDestino || (isSuspiciousBeneficiary
      ? `CTA-DEST-${Math.floor(100000 + Math.random() * 900000)}`
      : 'CTA-002-998811');

    const req: TransaccionRequest = {
      numeroCuentaOrigen: params.numeroCuentaOrigen || 'CTA-001-445522',
      numeroCuentaDestino: resolvedDestino,
      monto: Number(params.amount),
      moneda: 'PEN',
      canal: channelBackend,
      tipoTransaccion: 'TRANSFERENCIA',
      codigoPais: resolvedCodigoPais,
      direccionIp: resolvedIp,
      huellaDispositivo: params.isNewDevice
        ? `fingerprint-desconocido-${Math.floor(100 + Math.random() * 900)}`
        : 'device-fingerprint-habitual-01',
      latitud: resolvedLatitud,
      longitud: resolvedLongitud
    };

    // Objeto preliminar en UI
    const tempTx: SimulatedTransaction = {
      id: 'TX-' + Math.floor(10000 + Math.random() * 90000),
      timestamp: new Date(),
      customerName: params.customerName || 'Cliente Transaccional',
      accountId: req.numeroCuentaOrigen,
      amount: req.monto,
      channel: (params.channel === 'APP' ? 'TRANSFERENCIA_MOVIL' : (params.channel || 'ONLINE')),
      country: params.country || 'Perú',
      city: params.city || (resolvedCodigoPais === 'PE' ? 'Lima' : (resolvedCodigoPais === 'RU' ? 'Moscú' : (resolvedCodigoPais === 'NG' ? 'Lagos' : 'New York'))),
      device: params.device || (params.isNewDevice ? 'Dispositivo No Habitual' : 'Dispositivo Habitual'),
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
              message: `¡ALERTA DISPARADA EN BACKEND! ${tempTx.id} evaluada con ${tempTx.riskScore} pts (${tempTx.status}).`,
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
        // Enviar transacciones periódicas con distribución probabilística realista de escenarios y scores
        this.liveStreamingTimer = setInterval(() => {
          if (!this.isLiveStreamingSubject.value) {
            clearInterval(this.liveStreamingTimer);
            return;
          }

          const rand = Math.random();

          // 1. ESCENARIO FRAUDE CRÍTICO (~20%): Score 85 - 100 pts -> Decisión: BLOQUEAR (Estado: BLOQUEADA)
          // Dispara múltiples reglas críticas en el backend:
          // RF001 (Monto inusual: +25), RF004 (País nuevo: +30), RF007 (Viaje imposible: +40),
          // RF008 (Beneficiario nuevo y monto alto: +20), RF002 (Dispositivo nuevo: +15), RF003 (IP nueva: +10)
          // Total score sumado: 140 pts -> Capped a 100 pts (MAX_SCORE) -> BLOQUEAR / BLOQUEADA
          if (rand < 0.20) {
            const fraudScenarios = [
              { country: 'Rusia', city: 'Moscú', device: 'Tor Browser / Proxy Anónimo v13' },
              { country: 'Rusia', city: 'San Petersburgo', device: 'Emulador Android Nox Rooted' },
              { country: 'Nigeria', city: 'Lagos', device: 'Dispositivo Clonado / Botnet' },
              { country: 'China', city: 'Pekín', device: 'VPN Anónima / SOCKS5 Proxy' }
            ];
            const target = fraudScenarios[Math.floor(Math.random() * fraudScenarios.length)];
            const victimNames = ['Juan Alberto Pérez', 'Héctor Balbuena', 'Rosa María Vega', 'Carlos Santillán'];

            this.simulateSingleTransaction({
              customerName: victimNames[Math.floor(Math.random() * victimNames.length)] + ' (Suplantado)',
              amount: 12000 + Math.floor(Math.random() * 16000), // S/ 12,000 - S/ 28,000 (Monto muy alto)
              channel: 'ONLINE',
              country: target.country,
              city: target.city,
              device: target.device,
              isNewDevice: true,
              isUnusualLocation: true,
              numeroCuentaDestino: `CTA-FRAUD-${Math.floor(100000 + Math.random() * 900000)}`
            });
            return;
          }

          // 2. ESCENARIO RIESGO ALTO (~15%): Score 60 - 79 pts -> Decisión: RETENER (Estado: RETENIDA)
          // Dispara: RF004 (País nuevo: 30 pts), RF008 (Destino nuevo monto > 5000: 20 pts), RF002 (Dispositivo nuevo: 15 pts)
          if (rand < 0.35) {
            const highRiskScenarios = [
              { country: 'Estados Unidos', city: 'Miami', device: 'Navegador Firefox Linux No Habitual' },
              { country: 'Estados Unidos', city: 'Los Ángeles', device: 'Dispositivo Móvil No Reconocido' },
              { country: 'México', city: 'Ciudad de México', device: 'Windows 11 Chrome No Registrado' }
            ];
            const target = highRiskScenarios[Math.floor(Math.random() * highRiskScenarios.length)];

            this.simulateSingleTransaction({
              customerName: 'Cliente Corporativo / ' + (Math.random() > 0.5 ? 'Exportadora Lima' : 'Servicios Globales'),
              amount: 5500 + Math.floor(Math.random() * 3500), // S/ 5,500 - S/ 9,000
              channel: 'INTERNACIONAL',
              country: target.country,
              city: target.city,
              device: target.device,
              isNewDevice: true,
              isUnusualLocation: false, // Sin salto geográfico para mantener score entre 60 y 75
              numeroCuentaDestino: `CTA-EXT-${Math.floor(100000 + Math.random() * 900000)}`
            });
            return;
          }

          // 3. ESCENARIO RIESGO MEDIO (~25%): Score 30 - 59 pts -> Decisión: MONITOREAR (Estado: APROBADA)
          // Dispara: RF002 (Dispositivo nuevo: 15 pts) + RF003 (IP nueva: 10 pts) = 25 pts, o país 'US' (30 pts)
          if (rand < 0.60) {
            const mediumRiskScenarios = [
              { country: 'Perú', city: 'Lima', device: 'iPhone 15 Pro No Registrado' },
              { country: 'Perú', city: 'Arequipa', device: 'MacBook Air Safari Dinámico' },
              { country: 'Estados Unidos', city: 'Orlando', device: 'Portal Web Hotelería' }
            ];
            const target = mediumRiskScenarios[Math.floor(Math.random() * mediumRiskScenarios.length)];

            this.simulateSingleTransaction({
              customerName: 'Andrea Palacios / Compra En Línea',
              amount: 1200 + Math.floor(Math.random() * 3200), // S/ 1,200 - S/ 4,400
              channel: 'ONLINE',
              country: target.country,
              city: target.city,
              device: target.device,
              isNewDevice: true,
              isUnusualLocation: false,
              numeroCuentaDestino: 'CTA-002-998811' // Beneficiario habitual
            });
            return;
          }

          // 4. ESCENARIO LEGÍTIMO / BAJO RIESGO (~40%): Score 0 - 25 pts -> Decisión: APROBAR (Estado: APROBADA)
          // Operaciones cotidianas habituales
          const legitScenarios: Array<{
            country: string;
            city: string;
            channel: SimulatedTransaction['channel'];
            device: string;
          }> = [
            { country: 'Perú', city: 'Lima', channel: 'POS', device: 'POS Terminal Izipay Habitual' },
            { country: 'Perú', city: 'Lima', channel: 'TRANSFERENCIA_MOVIL', device: 'Samsung Galaxy Habitual' },
            { country: 'Perú', city: 'Miraflores', channel: 'ATM', device: 'Cajero Automático BCP' },
            { country: 'Perú', city: 'Trujillo', channel: 'TRANSFERENCIA_MOVIL', device: 'device-fingerprint-habitual-01' },
            { country: 'Perú', city: 'Cusco', channel: 'POS', device: 'POS Niubiz Establecimiento' }
          ];
          const legit = legitScenarios[Math.floor(Math.random() * legitScenarios.length)];
          const names = ['Juan Alberto Pérez', 'María Elena Rosas', 'Ana Torres Rojas', 'Luis Delgado Castro', 'Lucía Paredes'];

          this.simulateSingleTransaction({
            customerName: names[Math.floor(Math.random() * names.length)],
            amount: 25 + Math.floor(Math.random() * 750), // S/ 25 - S/ 775 (Montos comunes)
            channel: legit.channel,
            country: legit.country,
            city: legit.city,
            device: legit.device,
            isNewDevice: false,
            isUnusualLocation: false,
            numeroCuentaDestino: 'CTA-002-998811'
          });
        }, 3000);
      })
    ).subscribe();
  }

  stopLiveStreaming(): void {
    if (this.liveStreamingTimer) {
      clearInterval(this.liveStreamingTimer);
      this.liveStreamingTimer = null;
    }
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
