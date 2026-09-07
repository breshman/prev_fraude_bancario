import { Injectable, OnDestroy } from '@angular/core';
import { BehaviorSubject, Observable, Subject, Subscription, interval } from 'rxjs';
import { ExcelExportUtil } from '../../shared/utils/excel-export.util';
import { FraudMockService, FraudAlert } from './fraud-mock.service';

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

  private readonly DEFAULT_THRESHOLD = 70;
  private thresholdSubject = new BehaviorSubject<number>(this.DEFAULT_THRESHOLD);
  private simulationsSubject = new BehaviorSubject<SimulatedTransaction[]>([]);
  private alertNoticeSubject = new Subject<SimulationAlertNotice | null>();

  // Streaming en vivo
  private liveStreamingSub: Subscription | null = null;
  private isLiveStreamingSubject = new BehaviorSubject<boolean>(false);

  private namesPool = [
    'Alejandro Vega', 'Patricia Mendoza', 'Roberto Sánchez', 'Claudia Quispe',
    'Fernando Castro', 'Sofía Benítez', 'Guillermo Alarcón', 'Valeria Ramos',
    'Esteban Morales', 'Daniela Ortiz', 'Javier Navarro', 'Lucía Paredes',
    'Mateo Salazar', 'Camila Vargas', 'Rodrigo Silva', 'Gabriela Torres',
    'Enrique Cárdenas', 'Natalia Flores', 'Gonzalo Espinoza', 'Renata Delgado'
  ];

  private channels: Array<SimulatedTransaction['channel']> = [
    'ONLINE', 'POS', 'ATM', 'TRANSFERENCIA_MOVIL', 'INTERNACIONAL'
  ];

  private countries = [
    { country: 'Perú', city: 'Lima', risk: 0 },
    { country: 'Perú', city: 'Arequipa', risk: 0 },
    { country: 'Perú', city: 'Trujillo', risk: 0 },
    { country: 'Perú', city: 'Cusco', risk: 0 },
    { country: 'EE.UU.', city: 'Miami', risk: 15 },
    { country: 'España', city: 'Madrid', risk: 12 },
    { country: 'Rusia', city: 'Moscú', risk: 42 },
    { country: 'Nigeria', city: 'Lagos', risk: 45 },
    { country: 'Islas Caimán', city: 'George Town', risk: 38 },
    { country: 'Panamá', city: 'Ciudad de Panamá', risk: 22 }
  ];

  private devices = [
    'iPhone 15 Pro - App BCR', 'Samsung Galaxy S24 - App BCR', 'Xiaomi Redmi Note 13',
    'Chrome en Windows 11', 'Safari en macOS Sonoma', 'Dispositivo Desconocido (Linux/Emulador)',
    'Tor Browser / Proxy Anónimo', 'Firefox en Ubuntu VM'
  ];

  constructor(private fraudMockService: FraudMockService) {
    // Generar un lote inicial de transacciones con picos variados
    this.generateMassiveRandom(16, false);
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
   * Actualiza el umbral de riesgo y recalcula inmediatamente todas las transacciones existentes.
   */
  setThreshold(threshold: number): void {
    const clamped = Math.max(1, Math.min(100, Math.round(threshold)));
    this.thresholdSubject.next(clamped);

    // Recalcular el flag de superación de umbral en tiempo real
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

    // Sincronizar con el servicio oficial de alertas
    if (newlyBreached.length > 0) {
      this.fraudMockService.addAlerts(newlyBreached);
    }

    const breachedCount = updated.filter(tx => tx.exceedsThreshold).length;
    if (breachedCount > 0) {
      this.alertNoticeSubject.next({
        breachedCount,
        message: `Umbral calibrado a ${clamped} pts: ${breachedCount} transacción(es) ahora superan el umbral y generan alertas.`,
        timestamp: new Date()
      });
    }
  }

  /**
   * Simula una transacción individual con parámetros especificados o generados.
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
  }): SimulatedTransaction {
    const threshold = this.currentThreshold;
    const randomId = 'SIM-' + Math.floor(10000 + Math.random() * 90000);
    const accountId = 'BCR-' + Math.floor(10000000 + Math.random() * 90000000);
    const customer = params.customerName || this.getRandomItem(this.namesPool);
    const channel = params.channel || this.getRandomItem(this.channels);
    const country = params.country || 'Perú';
    const city = params.city || 'Lima';
    const device = params.device || this.getRandomItem(this.devices);

    const reasons: string[] = [];
    let score = 5 + Math.floor(Math.random() * 10); // Base mínima

    // Regla 1: Monto
    if (params.amount >= 10000) {
      score += 32;
      reasons.push(`Monto elevado ($${params.amount.toFixed(2)}) muy superior a la media histórica`);
    } else if (params.amount >= 4000) {
      score += 18;
      reasons.push(`Monto considerable ($${params.amount.toFixed(2)}) requiere validación`);
    }

    // Regla 2: Ubicación
    if (country !== 'Perú' || params.isUnusualLocation) {
      const locScore = country === 'Rusia' || country === 'Nigeria' || country === 'Islas Caimán' ? 38 : 22;
      score += locScore;
      reasons.push(`Operación internacional desde ${country} (${city}) sin registro de viaje`);
    }

    // Regla 3: Canal y Dispositivo
    if (device.includes('Desconocido') || device.includes('Tor') || params.isNewDevice) {
      score += 25;
      reasons.push('Dispositivo no reconocido o conexión a través de proxy anónimo');
    }

    // Regla 4: Horario nocturno
    if (params.isNocturnal) {
      score += 15;
      reasons.push('Horario no habitual (02:00 - 05:00 hrs)');
    }

    // Regla 5: Frecuencia inusual
    if (params.isRapidFrequency) {
      score += 24;
      reasons.push('Patrón de velocidad de transacción anómalo (múltiples intentos en < 60s)');
    }

    // Regla 6: Canal específico
    if (channel === 'INTERNACIONAL') {
      score += 14;
      reasons.push('Canal de transferencia internacional swift');
    }

    score = Math.min(100, Math.max(1, score));
    const exceedsThreshold = score >= threshold;
    const riskLevel: SimulatedTransaction['riskLevel'] =
      score >= 85 ? 'CRITICO' : score >= 70 ? 'ALTO' : score >= 40 ? 'MEDIO' : 'BAJO';

    // Estado sugerido inicial
    let initialStatus: SimulatedTransaction['status'] = 'PENDIENTE';
    if (score < 40) {
      initialStatus = 'APROBADA';
    }

    const tx: SimulatedTransaction = {
      id: randomId,
      timestamp: new Date(),
      customerName: customer,
      accountId,
      amount: params.amount,
      channel,
      country,
      city,
      device,
      ipAddress: this.generateRandomIp(),
      riskScore: score,
      riskLevel,
      threshold,
      exceedsThreshold,
      reasons: reasons.length > 0 ? reasons : ['Transacción dentro de los parámetros biométricos y de comportamiento habitual'],
      status: initialStatus,
      selected: false
    };

    const currentList = this.simulationsSubject.value;
    const updatedList = [tx, ...currentList];
    this.simulationsSubject.next(updatedList);

    // Incrementar TX Monitoreadas en Dashboard
    this.fraudMockService.incrementMonitoredTransactions(1);

    // Conexión automática: Si supera el umbral, registrar inmediatamente como alerta oficial
    if (exceedsThreshold) {
      this.fraudMockService.addAlert(this.mapToFraudAlert(tx));

      this.alertNoticeSubject.next({
        transaction: tx,
        breachedCount: 1,
        message: `¡ALERTA DISPARADA! La transacción ${tx.id} superó el umbral (${tx.riskScore} pts >= ${threshold} pts).`,
        timestamp: new Date()
      });
    }

    return tx;
  }

  /**
   * Generación MASIVA Y TOTALMENTE ALEATORIA con distribución estocástica de picos de riesgo.
   */
  generateMassiveRandom(count: number, notify: boolean = true): void {
    const threshold = this.currentThreshold;
    const newItems: SimulatedTransaction[] = [];
    const alertsToPush: FraudAlert[] = [];
    let breachedCount = 0;

    for (let i = 0; i < count; i++) {
      const customer = this.getRandomItem(this.namesPool);
      const randomId = 'SIM-' + Math.floor(10000 + Math.random() * 90000);
      const accountId = 'BCR-' + Math.floor(10000000 + Math.random() * 90000000);
      const reasons: string[] = [];

      // Probabilidad de pico de riesgo:
      // 60% operaciones normales bajas (Score 5-38)
      // 20% operaciones medias (Score 40-69)
      // 20% picos agudos de fraude que superan umbral (Score 72-98)
      const roll = Math.random();
      let score = 10;
      let amount = 0;
      let loc = this.countries[0]; // Lima, Perú por defecto
      let dev = this.devices[0];
      let ch: SimulatedTransaction['channel'] = 'ONLINE';

      if (roll < 0.60) {
        // Operación normal
        amount = Math.floor(15 + Math.random() * 850);
        score = Math.floor(5 + Math.random() * 32);
        loc = this.getRandomItem(this.countries.filter(c => c.risk === 0));
        dev = this.getRandomItem(this.devices.slice(0, 5));
        ch = this.getRandomItem(['POS', 'ONLINE', 'ATM', 'TRANSFERENCIA_MOVIL'] as const);
        reasons.push('Patrón de consumo biométrico y geográfico habitual');
      } else if (roll < 0.80) {
        // Operación riesgo medio
        amount = Math.floor(1200 + Math.random() * 3800);
        score = Math.floor(40 + Math.random() * 28);
        loc = this.getRandomItem(this.countries);
        dev = this.getRandomItem(this.devices);
        ch = this.getRandomItem(this.channels);
        reasons.push(`Monto medio-alto de $${amount} requiere validación`);
        if (loc.risk > 0) reasons.push(`Ubicación inusual: ${loc.country} (${loc.city})`);
      } else {
        // PICO AGUDO DE FRAUDE (Supera el umbral)
        amount = Math.floor(4800 + Math.random() * 24000);
        score = Math.floor(74 + Math.random() * 25);
        loc = this.getRandomItem(this.countries.filter(c => c.risk > 0));
        dev = this.getRandomItem(this.devices.slice(5)); // Anónimos o emuladores
        ch = this.getRandomItem(['INTERNACIONAL', 'ONLINE', 'TRANSFERENCIA_MOVIL'] as const);
        reasons.push(`Pico de riesgo: Retiro o transferencia de alto valor ($${amount})`);
        reasons.push(`Geolocalización crítica desde ${loc.country} (${loc.city})`);
        reasons.push('Dispositivo sospechoso o red con proxy Tor/VPN');
      }

      score = Math.min(100, Math.max(1, score));
      const exceedsThreshold = score >= threshold;
      if (exceedsThreshold) breachedCount++;

      const riskLevel: SimulatedTransaction['riskLevel'] =
        score >= 85 ? 'CRITICO' : score >= 70 ? 'ALTO' : score >= 40 ? 'MEDIO' : 'BAJO';

      const tx: SimulatedTransaction = {
        id: randomId,
        timestamp: new Date(Date.now() - Math.floor(Math.random() * 10800000)),
        customerName: customer,
        accountId,
        amount,
        channel: ch,
        country: loc.country,
        city: loc.city,
        device: dev,
        ipAddress: this.generateRandomIp(),
        riskScore: score,
        riskLevel,
        threshold,
        exceedsThreshold,
        reasons,
        status: score < 40 ? 'APROBADA' : 'PENDIENTE',
        selected: false
      };

      newItems.push(tx);

      if (exceedsThreshold) {
        alertsToPush.push(this.mapToFraudAlert(tx));
      }
    }

    const current = this.simulationsSubject.value;
    const updated = [...newItems, ...current];
    this.simulationsSubject.next(updated);

    // Incrementar transacciones monitoreadas en el Dashboard
    this.fraudMockService.incrementMonitoredTransactions(count);

    // Conectar automáticamente con la vista de Alertas y Dashboard
    if (alertsToPush.length > 0) {
      this.fraudMockService.addAlerts(alertsToPush);
    }

    if (notify) {
      this.alertNoticeSubject.next({
        breachedCount,
        message: `Lote aleatorio de ${count} transacciones generado. ${breachedCount} superaron el umbral y se registraron en el sistema de Alertas.`,
        timestamp: new Date()
      });
    }
  }

  /**
   * Genera un lote masivo bajo escenarios preconfigurados.
   */
  generateBatch(count: number, scenario: 'MIXED' | 'HIGH_RISK' | 'NORMAL', notify: boolean = true): void {
    if (scenario === 'MIXED') {
      this.generateMassiveRandom(count, notify);
      return;
    }

    const threshold = this.currentThreshold;
    const newItems: SimulatedTransaction[] = [];
    const alertsToPush: FraudAlert[] = [];
    let breachedInBatch = 0;

    for (let i = 0; i < count; i++) {
      const customer = this.getRandomItem(this.namesPool);
      const loc = this.getRandomItem(this.countries);
      const dev = this.getRandomItem(this.devices);
      const ch = this.getRandomItem(this.channels);
      const randomId = 'SIM-' + Math.floor(10000 + Math.random() * 90000);
      const accountId = 'BCR-' + Math.floor(10000000 + Math.random() * 90000000);
      const reasons: string[] = [];

      let amount = 0;
      let score = 10;

      if (scenario === 'NORMAL') {
        amount = Math.floor(20 + Math.random() * 650);
        score = Math.floor(5 + Math.random() * 32);
        reasons.push('Comportamiento usual del cuentahabiente en comercios frecuentes');
      } else {
        // HIGH_RISK
        amount = Math.floor(4500 + Math.random() * 22000);
        score = Math.floor(75 + Math.random() * 25);
        reasons.push('Vaciado de fondos / Intento de suplantación de identidad (Phishing)');
        reasons.push('IP geográfica remota ' + loc.country);
        if (dev.includes('Desconocido') || dev.includes('Tor')) {
          reasons.push('Sesión originada en navegador anónimo o emulador virtual');
        }
      }

      score = Math.min(100, Math.max(1, score));
      const exceedsThreshold = score >= threshold;
      if (exceedsThreshold) breachedInBatch++;

      const riskLevel: SimulatedTransaction['riskLevel'] =
        score >= 85 ? 'CRITICO' : score >= 70 ? 'ALTO' : score >= 40 ? 'MEDIO' : 'BAJO';

      const tx: SimulatedTransaction = {
        id: randomId,
        timestamp: new Date(Date.now() - Math.floor(Math.random() * 14400000)),
        customerName: customer,
        accountId,
        amount,
        channel: ch,
        country: scenario === 'NORMAL' ? 'Perú' : loc.country,
        city: scenario === 'NORMAL' ? 'Lima' : loc.city,
        device: scenario === 'NORMAL' ? 'iPhone 15 Pro - App BCR' : dev,
        ipAddress: this.generateRandomIp(),
        riskScore: score,
        riskLevel,
        threshold,
        exceedsThreshold,
        reasons,
        status: score < 40 ? 'APROBADA' : 'PENDIENTE',
        selected: false
      };

      newItems.push(tx);
      if (exceedsThreshold) {
        alertsToPush.push(this.mapToFraudAlert(tx));
      }
    }

    const current = this.simulationsSubject.value;
    const updated = [...newItems, ...current];
    this.simulationsSubject.next(updated);

    this.fraudMockService.incrementMonitoredTransactions(count);

    if (alertsToPush.length > 0) {
      this.fraudMockService.addAlerts(alertsToPush);
    }

    if (notify) {
      this.alertNoticeSubject.next({
        breachedCount: breachedInBatch,
        message: `Lote de ${count} transacciones generado. ${breachedInBatch} superaron el umbral y dispararon alertas.`,
        timestamp: new Date()
      });
    }
  }

  /**
   * MODO SIMULACIÓN EN VIVO / STREAMING CONTINUO
   */
  startLiveStreaming(intervalMs: number = 2200): void {
    if (this.liveStreamingSub) return;

    this.isLiveStreamingSubject.next(true);
    this.liveStreamingSub = interval(intervalMs).subscribe(() => {
      // Cada tick genera 1 transacción aleatoria
      this.generateMassiveRandom(1, false);
    });
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

  /**
   * Métodos para Revisión Masiva
   */
  toggleSelect(id: string): void {
    const list = this.simulationsSubject.value.map(tx =>
      tx.id === id ? { ...tx, selected: !tx.selected } : tx
    );
    this.simulationsSubject.next(list);
  }

  selectAll(select: boolean, filteredIds?: string[]): void {
    const list = this.simulationsSubject.value.map(tx => {
      if (filteredIds) {
        return filteredIds.includes(tx.id) ? { ...tx, selected: select } : tx;
      }
      return { ...tx, selected: select };
    });
    this.simulationsSubject.next(list);
  }

  bulkUpdateStatus(status: 'APROBADA' | 'RETENIDA' | 'BLOQUEADA'): void {
    const selectedIds = new Set(
      this.simulationsSubject.value.filter(tx => tx.selected).map(tx => tx.id)
    );

    const list = this.simulationsSubject.value.map(tx =>
      selectedIds.has(tx.id) ? { ...tx, status, selected: false } : tx
    );
    this.simulationsSubject.next(list);

    // Sincronizar en FraudMockService
    selectedIds.forEach(id => {
      const alertId = 'ALRT-' + id.replace('SIM-', '');
      this.fraudMockService.updateAlertStatus(alertId, status);
    });
  }

  updateSingleStatus(tx: SimulatedTransaction, status: SimulatedTransaction['status']): void {
    const list = this.simulationsSubject.value.map(item =>
      item.id === tx.id ? { ...item, status } : item
    );
    this.simulationsSubject.next(list);

    const alertId = 'ALRT-' + tx.id.replace('SIM-', '');
    this.fraudMockService.updateAlertStatus(alertId, status);
  }

  bulkDeleteSelected(): void {
    const list = this.simulationsSubject.value.filter(tx => !tx.selected);
    this.simulationsSubject.next(list);
  }

  bulkEscalateToAlerts(): number {
    const selected = this.simulationsSubject.value.filter(tx => tx.selected && tx.exceedsThreshold);
    if (selected.length === 0) return 0;

    const newAlerts = selected.map(tx => this.mapToFraudAlert(tx));
    this.fraudMockService.addAlerts(newAlerts);

    const escalatedIds = new Set(selected.map(s => s.id));
    const updated = this.simulationsSubject.value.map(tx =>
      escalatedIds.has(tx.id) ? { ...tx, status: 'RETENIDA' as const, selected: false } : tx
    );
    this.simulationsSubject.next(updated);

    return selected.length;
  }

  dismissAlertNotice(): void {
    this.alertNoticeSubject.next(null);
  }

  clearAll(): void {
    this.simulationsSubject.next([]);
    this.alertNoticeSubject.next(null);
  }

  exportToExcel(format: 'xls' | 'csv' = 'xls', selectedOnly: boolean = false): void {
    const all = this.simulationsSubject.value;
    const dataset = selectedOnly ? all.filter(tx => tx.selected) : all;
    if (dataset.length === 0) return;

    if (format === 'xls') {
      ExcelExportUtil.exportToExcel(dataset);
    } else {
      ExcelExportUtil.exportToCsv(dataset);
    }
  }

  private mapToFraudAlert(tx: SimulatedTransaction): FraudAlert {
    return {
      id: 'ALRT-' + tx.id.replace('SIM-', ''),
      transactionId: tx.id,
      customerName: tx.customerName,
      amount: tx.amount,
      riskScore: tx.riskScore,
      riskLevel: tx.riskLevel,
      status: tx.status === 'BLOQUEADA' ? 'BLOQUEADA' : tx.status === 'RETENIDA' ? 'RETENIDA' : 'NUEVA',
      date: tx.timestamp,
      reasons: tx.reasons,
      origin: 'SIMULACION'
    };
  }

  private getRandomItem<T>(arr: T[]): T {
    return arr[Math.floor(Math.random() * arr.length)];
  }

  private generateRandomIp(): string {
    return `${Math.floor(Math.random() * 200 + 10)}.${Math.floor(Math.random() * 255)}.${Math.floor(Math.random() * 255)}.${Math.floor(Math.random() * 250 + 1)}`;
  }
}
