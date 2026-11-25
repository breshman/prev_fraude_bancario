import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgbDropdownModule, NgbTooltipModule, NgbProgressbarModule } from '@ng-bootstrap/ng-bootstrap';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';
import { SimulationService, SimulatedTransaction, SimulationAlertNotice } from '../../core/services/simulation.service';
import { FraudService } from '../../core/services/fraud.service';
import { FraudChartComponent } from '../../shared/components/fraud-chart/fraud-chart.component';

@Component({
  selector: 'app-simulation',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NgbDropdownModule,
    NgbTooltipModule,
    NgbProgressbarModule,
    FraudChartComponent
  ],
  templateUrl: './simulation.component.html',
  styleUrl: './simulation.component.scss'
})
export class SimulationComponent implements OnInit {

  // Observables desde el servicio
  threshold$!: Observable<number>;
  simulations$!: Observable<SimulatedTransaction[]>;
  alertNotice$!: Observable<SimulationAlertNotice | null>;
  isLiveStreaming$!: Observable<boolean>;

  // Variables de control de UI
  activeThreshold: number = 70;
  activeFilter: 'ALL' | 'BREACHED' | 'APROBADA' | 'RETENIDA' | 'BLOQUEADA' = 'ALL';
  searchTerm: string = '';
  startDateFilter: string = '';
  endDateFilter: string = '';
  showSingleSimModal: boolean = false;
  showExportDateModal: boolean = false;
  isExporting: boolean = false;
  toastMessage: string | null = null;
  toastType: 'success' | 'warning' | 'info' = 'info';

  // Configuración del Modal de Exportación por Rango de Fechas (Backend)
  exportModalConfig = {
    startDate: '',
    endDate: '',
    format: 'xlsx' as 'xlsx' | 'csv',
    statusFilter: 'ALL' as 'ALL' | 'BREACHED' | 'APROBADA' | 'RETENIDA' | 'BLOQUEADA',
    onlyAlerts: false,
    selectedOnly: false
  };

  // Formulario de Simulación Individual
  singleForm = {
    customerName: 'Carlos Santillán',
    amount: 14850,
    channel: 'ONLINE' as SimulatedTransaction['channel'],
    country: 'Rusia',
    city: 'San Petersburgo',
    device: 'Tor Browser / Proxy Anónimo',
    isNocturnal: true,
    isRapidFrequency: true,
    isNewDevice: true,
    isUnusualLocation: true
  };

  constructor(
    public simulationService: SimulationService,
    public fraudService: FraudService
  ) {}

  ngOnInit(): void {
    this.threshold$ = this.simulationService.threshold$;
    this.simulations$ = this.simulationService.simulations$;
    this.alertNotice$ = this.simulationService.alertNotice$;
    this.isLiveStreaming$ = this.simulationService.isLiveStreaming$;

    this.threshold$.subscribe(val => {
      this.activeThreshold = val;
    });
  }

  // --- Manejo del Umbral ---
  onThresholdChange(value: number): void {
    this.simulationService.setThreshold(value);
  }

  applyThresholdPreset(preset: number): void {
    this.activeThreshold = preset;
    this.simulationService.setThreshold(preset);
  }

  // --- Ejecución de Simulaciones ---
  onSimulateSingle(): void {
    const tx = this.simulationService.simulateSingleTransaction({
      customerName: this.singleForm.customerName,
      amount: Number(this.singleForm.amount),
      channel: this.singleForm.channel,
      country: this.singleForm.country,
      city: this.singleForm.city,
      device: this.singleForm.device,
      isNocturnal: this.singleForm.isNocturnal,
      isRapidFrequency: this.singleForm.isRapidFrequency,
      isNewDevice: this.singleForm.isNewDevice,
      isUnusualLocation: this.singleForm.isUnusualLocation
    });

    this.showSingleSimModal = false;

    if (tx.exceedsThreshold) {
      this.showToast(`🚨 Alerta: Transacción ${tx.id} superó el umbral (${tx.riskScore} pts). Registrada en Dashboard y Alertas.`, 'warning');
    } else {
      this.showToast(`✅ Transacción ${tx.id} simulada dentro del rango seguro (${tx.riskScore} pts).`, 'success');
    }
  }

  onGenerateMassive(count: number): void {
    this.simulationService.generateMassiveRandom(count);
    this.showToast(`⚡ Generadas ${count} transacciones aleatorias masivas conectadas con el Dashboard y Alertas.`, 'info');
  }

  onGenerateBatch(count: number, scenario: 'MIXED' | 'HIGH_RISK' | 'NORMAL'): void {
    this.simulationService.generateBatch(count, scenario);
    this.showToast(`Generadas ${count} transacciones bajo el escenario "${this.getScenarioLabel(scenario)}"`, 'info');
  }

  onToggleLiveStreaming(): void {
    this.simulationService.toggleLiveStreaming();
  }

  // --- Filtrado y Búsqueda ---
  getFilteredSimulations(simulations: SimulatedTransaction[]): SimulatedTransaction[] {
    const startMs = this.startDateFilter ? new Date(`${this.startDateFilter}T00:00:00`).getTime() : null;
    const endMs = this.endDateFilter ? new Date(`${this.endDateFilter}T23:59:59.999`).getTime() : null;

    return simulations.filter(tx => {
      // Filtro de Estado / Umbral
      if (this.activeFilter === 'BREACHED' && !tx.exceedsThreshold) return false;
      if (this.activeFilter === 'APROBADA' && tx.status !== 'APROBADA') return false;
      if (this.activeFilter === 'RETENIDA' && tx.status !== 'RETENIDA') return false;
      if (this.activeFilter === 'BLOQUEADA' && tx.status !== 'BLOQUEADA') return false;

      // Filtro por Rango de Fechas
      if (startMs !== null || endMs !== null) {
        const txTime = new Date(tx.timestamp).getTime();
        if (!isNaN(txTime)) {
          if (startMs !== null && !isNaN(startMs) && txTime < startMs) return false;
          if (endMs !== null && !isNaN(endMs) && txTime > endMs) return false;
        }
      }

      // Filtro de Búsqueda
      if (this.searchTerm.trim()) {
        const term = this.searchTerm.toLowerCase();
        const matchesId = tx.id.toLowerCase().includes(term);
        const matchesCust = tx.customerName.toLowerCase().includes(term);
        const matchesAcc = tx.accountId.toLowerCase().includes(term);
        const matchesCountry = tx.country.toLowerCase().includes(term);
        return matchesId || matchesCust || matchesAcc || matchesCountry;
      }

      return true;
    });
  }

  onApplyDateRangeBackend(): void {
    this.simulationService.cargarTransaccionesDesdeBackend(
      this.startDateFilter || undefined,
      this.endDateFilter || undefined
    ).subscribe(txs => {
      const rangoMsg = (this.startDateFilter || this.endDateFilter)
        ? `(${this.startDateFilter || 'Inicio'} a ${this.endDateFilter || 'Hoy'})`
        : '(Historial completo)';
      this.showToast(`📅 Se cargaron ${txs.length} transacciones desde el backend ${rangoMsg}.`, 'info');
    });
  }

  onClearDateRange(): void {
    this.startDateFilter = '';
    this.endDateFilter = '';
    this.simulationService.cargarTransaccionesDesdeBackend().subscribe();
  }

  // --- Selección y Acciones Masivas ---
  isAllSelected(displayedList: SimulatedTransaction[]): boolean {
    return displayedList.length > 0 && displayedList.every(tx => tx.selected);
  }

  toggleSelectAll(displayedList: SimulatedTransaction[], event: Event): void {
    const isChecked = (event.target as HTMLInputElement).checked;
    const ids = displayedList.map(tx => tx.id);
    this.simulationService.selectAll(isChecked, ids);
  }

  toggleSelect(id: string): void {
    this.simulationService.toggleSelect(id);
  }

  getSelectedCount(simulations: SimulatedTransaction[]): number {
    return simulations.filter(tx => tx.selected).length;
  }

  getSelectedBreachedCount(simulations: SimulatedTransaction[]): number {
    return simulations.filter(tx => tx.selected && tx.exceedsThreshold).length;
  }

  onBulkApprove(): void {
    this.simulationService.bulkUpdateStatus('APROBADA');
    this.showToast('Transacciones seleccionadas marcadas como APROBADAS y sincronizadas.', 'success');
  }

  onBulkRetain(): void {
    this.simulationService.bulkUpdateStatus('RETENIDA');
    this.showToast('Transacciones seleccionadas puestas en RETENCIÓN preventiva.', 'warning');
  }

  onBulkBlock(): void {
    this.simulationService.bulkUpdateStatus('BLOQUEADA');
    this.showToast('Transacciones seleccionadas BLOQUEADAS por sospecha de fraude.', 'warning');
  }

  onBulkEscalate(): void {
    const count = this.simulationService.bulkEscalateToAlerts();
    if (count > 0) {
      this.showToast(`🚨 Se escalaron ${count} alerta(s) críticas a la bandeja oficial del sistema.`, 'warning');
    } else {
      this.showToast('No hay transacciones seleccionadas que superen el umbral para escalar.', 'info');
    }
  }

  onBulkDelete(): void {
    this.simulationService.bulkDeleteSelected();
    this.showToast('Transacciones seleccionadas eliminadas del simulador.', 'info');
  }

  updateSingleStatus(tx: SimulatedTransaction, status: SimulatedTransaction['status']): void {
    this.simulationService.updateSingleStatus(tx, status);
    this.showToast(`Transacción ${tx.id} actualizada a ${status} y sincronizada.`, 'info');
  }

  // --- Métricas Calculadas ---
  getMetrics(simulations: SimulatedTransaction[]) {
    const total = simulations.length;
    const breached = simulations.filter(tx => tx.exceedsThreshold);
    const totalAmountAtRisk = breached.reduce((sum, tx) => sum + tx.amount, 0);
    const avgScore = total > 0
      ? Math.round(simulations.reduce((sum, tx) => sum + tx.riskScore, 0) / total)
      : 0;

    return {
      total,
      breachedCount: breached.length,
      breachedPercent: total > 0 ? Math.round((breached.length / total) * 100) : 0,
      totalAmountAtRisk,
      avgScore
    };
  }

  // --- Exportación a Excel (.xlsx) o CSV desde el Backend ---
  onExportExcel(format: 'xlsx' | 'xls' | 'csv' = 'xlsx', selectedOnly: boolean = false): void {
    if (this.isExporting) return;
    const fmt = format === 'csv' ? 'csv' : 'xlsx';
    this.isExporting = true;

    this.simulationService.exportToExcel(fmt, selectedOnly, {
      startDate: this.startDateFilter || undefined,
      endDate: this.endDateFilter || undefined,
      statusFilter: this.activeFilter !== 'ALL' ? this.activeFilter : undefined
    }).pipe(
      finalize(() => {
        this.isExporting = false;
      })
    ).subscribe(fromBackend => {
      const rango = (this.startDateFilter || this.endDateFilter)
        ? ` [${this.startDateFilter || 'Inicio'} a ${this.endDateFilter || 'Hoy'}]`
        : '';
      const origen = fromBackend ? 'desde el servidor Backend' : 'localmente';
      this.showToast(`📊 Reporte ${fmt.toUpperCase()}${rango} descargado ${origen}.`, 'success');
    });
  }

  openExportDateModal(format: 'xlsx' | 'xls' | 'csv' = 'xlsx'): void {
    const fmt = format === 'csv' ? 'csv' : 'xlsx';
    this.exportModalConfig = {
      startDate: this.startDateFilter || this.getFormattedDateOffset(-7),
      endDate: this.endDateFilter || this.getFormattedDateOffset(0),
      format: fmt,
      statusFilter: this.activeFilter,
      onlyAlerts: this.activeFilter === 'BREACHED',
      selectedOnly: false
    };
    this.showExportDateModal = true;
  }

  applyExportPreset(days: number | 'TODAY' | 'MONTH' | 'ALL'): void {
    if (days === 'ALL') {
      this.exportModalConfig.startDate = '';
      this.exportModalConfig.endDate = '';
      return;
    }
    const today = this.getFormattedDateOffset(0);
    if (days === 'TODAY') {
      this.exportModalConfig.startDate = today;
      this.exportModalConfig.endDate = today;
    } else if (days === 'MONTH') {
      const now = new Date();
      const firstDay = new Date(now.getFullYear(), now.getMonth(), 1);
      this.exportModalConfig.startDate = this.formatDateIso(firstDay);
      this.exportModalConfig.endDate = today;
    } else {
      this.exportModalConfig.startDate = this.getFormattedDateOffset(-days);
      this.exportModalConfig.endDate = today;
    }
  }

  onConfirmBackendDateExport(): void {
    if (this.isExporting) return;
    if (this.exportModalConfig.startDate && this.exportModalConfig.endDate &&
        this.exportModalConfig.startDate > this.exportModalConfig.endDate) {
      this.showToast('⚠️ La fecha inicial no puede ser mayor que la fecha final.', 'warning');
      return;
    }

    // Sincronizar filtro activo de fechas en la vista
    this.startDateFilter = this.exportModalConfig.startDate;
    this.endDateFilter = this.exportModalConfig.endDate;

    this.isExporting = true;
    this.simulationService.exportToExcel(
      this.exportModalConfig.format,
      this.exportModalConfig.selectedOnly,
      {
        startDate: this.exportModalConfig.startDate || undefined,
        endDate: this.exportModalConfig.endDate || undefined,
        statusFilter: this.exportModalConfig.statusFilter,
        onlyAlerts: this.exportModalConfig.onlyAlerts
      }
    ).pipe(
      finalize(() => {
        this.isExporting = false;
      })
    ).subscribe(fromBackend => {
      this.showExportDateModal = false;
      const rango = (this.exportModalConfig.startDate || this.exportModalConfig.endDate)
        ? ` (${this.exportModalConfig.startDate || 'Inicio'} al ${this.exportModalConfig.endDate || 'Hoy'})`
        : ' (Historial completo)';
      const origen = fromBackend ? 'generado por el Backend' : 'generado en modo respaldo';
      this.showToast(`📊 Reporte ${this.exportModalConfig.format.toUpperCase()}${rango} ${origen} y descargado con éxito.`, 'success');
    });
  }

  private getFormattedDateOffset(daysOffset: number): string {
    const d = new Date();
    d.setDate(d.getDate() + daysOffset);
    return this.formatDateIso(d);
  }

  private formatDateIso(d: Date): string {
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  // --- Toasts y Notificaciones ---
  showToast(msg: string, type: 'success' | 'warning' | 'info' = 'info'): void {
    this.toastMessage = msg;
    this.toastType = type;
    setTimeout(() => {
      if (this.toastMessage === msg) {
        this.toastMessage = null;
      }
    }, 4500);
  }

  dismissToast(): void {
    this.toastMessage = null;
  }

  dismissAlertNotice(): void {
    this.simulationService.dismissAlertNotice();
  }

  filterByBreached(): void {
    this.activeFilter = 'BREACHED';
  }

  private getScenarioLabel(scenario: string): string {
    switch (scenario) {
      case 'MIXED': return 'Tráfico Mixto Realista';
      case 'HIGH_RISK': return 'Ataque Crítico / Phishing';
      case 'NORMAL': return 'Operación Estándar Segura';
      default: return scenario;
    }
  }
}
