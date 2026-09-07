import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Observable } from 'rxjs';
import { FraudMockService, DashboardMetrics, FraudAlert } from '../../../core/services/fraud-mock.service';
import { SimulationService, SimulatedTransaction } from '../../../core/services/simulation.service';
import { FraudChartComponent } from '../../../shared/components/fraud-chart/fraud-chart.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FraudChartComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  metrics$!: Observable<DashboardMetrics>;
  recentAlerts$!: Observable<FraudAlert[]>;
  simulations$!: Observable<SimulatedTransaction[]>;
  threshold$!: Observable<number>;
  isLiveStreaming$!: Observable<boolean>;

  toastMsg: string | null = null;
  selectedAlert: FraudAlert | null = null;
  selectedTxDetails: SimulatedTransaction | null = null;

  constructor(
    public fraudService: FraudMockService,
    public simulationService: SimulationService
  ) {}

  ngOnInit(): void {
    this.metrics$ = this.fraudService.getDashboardMetrics();
    this.recentAlerts$ = this.fraudService.getRecentAlerts();
    this.simulations$ = this.simulationService.simulations$;
    this.threshold$ = this.simulationService.threshold$;
    this.isLiveStreaming$ = this.simulationService.isLiveStreaming$;
  }

  onTriggerQuickSimulation(count: number = 20): void {
    this.simulationService.generateMassiveRandom(count);
    this.showToast(`⚡ Generadas ${count} transacciones aleatorias conectadas al Dashboard y Alertas.`);
  }

  onToggleStreaming(): void {
    this.simulationService.toggleLiveStreaming();
  }

  openAlertDetails(alert: FraudAlert): void {
    this.selectedAlert = alert;
    this.simulations$.subscribe(sims => {
      this.selectedTxDetails = sims.find(tx => tx.id === alert.transactionId) || null;
    }).unsubscribe();
  }

  closeAlertDetails(): void {
    this.selectedAlert = null;
    this.selectedTxDetails = null;
  }

  onApproveAlert(): void {
    if (!this.selectedAlert) return;
    this.fraudService.updateAlertStatus(this.selectedAlert.id, 'APROBADA');
    if (this.selectedTxDetails) {
      this.simulationService.updateSingleStatus(this.selectedTxDetails, 'APROBADA');
    }
    this.showToast(`✓ Alerta ${this.selectedAlert.id} aprobada con éxito.`);
    this.selectedAlert.status = 'APROBADA';
  }

  onHoldAlert(): void {
    if (!this.selectedAlert) return;
    this.fraudService.updateAlertStatus(this.selectedAlert.id, 'RETENIDA');
    if (this.selectedTxDetails) {
      this.simulationService.updateSingleStatus(this.selectedTxDetails, 'RETENIDA');
    }
    this.showToast(`⏸️ Alerta ${this.selectedAlert.id} puesta en retención preventiva.`);
    this.selectedAlert.status = 'RETENIDA';
  }

  onBlockAlert(): void {
    if (!this.selectedAlert) return;
    this.fraudService.updateAlertStatus(this.selectedAlert.id, 'BLOQUEADA');
    if (this.selectedTxDetails) {
      this.simulationService.updateSingleStatus(this.selectedTxDetails, 'BLOQUEADA');
    }
    this.showToast(`⛔ Transacción y Alerta ${this.selectedAlert.id} bloqueadas.`);
    this.selectedAlert.status = 'BLOQUEADA';
  }

  showToast(msg: string): void {
    this.toastMsg = msg;
    setTimeout(() => {
      if (this.toastMsg === msg) this.toastMsg = null;
    }, 3500);
  }
}
