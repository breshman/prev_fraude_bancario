import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgbDropdownModule, NgbTooltipModule, NgbProgressbarModule } from '@ng-bootstrap/ng-bootstrap';
import { Observable } from 'rxjs';
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
  showSingleSimModal: boolean = false;
  toastMessage: string | null = null;
  toastType: 'success' | 'warning' | 'info' = 'info';

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
    return simulations.filter(tx => {
      // Filtro de Estado / Umbral
      if (this.activeFilter === 'BREACHED' && !tx.exceedsThreshold) return false;
      if (this.activeFilter === 'APROBADA' && tx.status !== 'APROBADA') return false;
      if (this.activeFilter === 'RETENIDA' && tx.status !== 'RETENIDA') return false;
      if (this.activeFilter === 'BLOQUEADA' && tx.status !== 'BLOQUEADA') return false;

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

  // --- Exportación a Excel ---
  onExportExcel(format: 'xls' | 'csv' = 'xls', selectedOnly: boolean = false): void {
    this.simulationService.exportToExcel(format, selectedOnly);
    this.showToast(`Archivo ${format.toUpperCase()} generado y descargado exitosamente.`, 'success');
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
