import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgbAccordionModule, NgbTooltipModule } from '@ng-bootstrap/ng-bootstrap';
import { FraudMockService, FraudAlert } from '../../../core/services/fraud-mock.service';
import { SimulationService } from '../../../core/services/simulation.service';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-alerts-list',
  standalone: true,
  imports: [CommonModule, FormsModule, NgbAccordionModule, NgbTooltipModule],
  templateUrl: './alerts-list.component.html',
  styleUrl: './alerts-list.component.scss'
})
export class AlertsListComponent implements OnInit {
  alerts$!: Observable<FraudAlert[]>;

  searchTerm: string = '';
  selectedRisk: string = '';
  selectedOrigin: string = '';

  toastMsg: string | null = null;
  toastType: 'success' | 'warning' | 'info' = 'info';

  constructor(
    public fraudService: FraudMockService,
    public simulationService: SimulationService
  ) {}

  ngOnInit(): void {
    this.alerts$ = this.fraudService.getRecentAlerts();
  }

  getFilteredAlerts(alerts: FraudAlert[]): FraudAlert[] {
    return alerts.filter(alert => {
      // Filtro por Nivel de Riesgo
      if (this.selectedRisk && alert.riskLevel !== this.selectedRisk) {
        return false;
      }

      // Filtro por Origen (Simulación vs Sistema)
      if (this.selectedOrigin && alert.origin !== this.selectedOrigin) {
        return false;
      }

      // Filtro por Búsqueda
      if (this.searchTerm.trim()) {
        const term = this.searchTerm.toLowerCase();
        const matchesId = alert.id.toLowerCase().includes(term);
        const matchesTx = alert.transactionId.toLowerCase().includes(term);
        const matchesName = alert.customerName.toLowerCase().includes(term);
        const matchesReason = alert.reasons.some(r => r.toLowerCase().includes(term));
        return matchesId || matchesTx || matchesName || matchesReason;
      }

      return true;
    });
  }

  onApprove(alert: FraudAlert): void {
    this.fraudService.updateAlertStatus(alert.id, 'APROBADA');
    this.showToast(`Alerta ${alert.id} aprobada y cerrada exitosamente.`, 'success');
  }

  onHold(alert: FraudAlert): void {
    this.fraudService.updateAlertStatus(alert.id, 'RETENIDA');
    this.showToast(`Alerta ${alert.id} colocada en retención preventiva para investigación.`, 'warning');
  }

  onBlock(alert: FraudAlert): void {
    this.fraudService.updateAlertStatus(alert.id, 'BLOQUEADA');
    this.showToast(`🚨 Alerta ${alert.id} bloqueada por confirmación de fraude.`, 'warning');
  }

  onRefresh(): void {
    this.showToast('Bandeja de alertas sincronizada en tiempo real.', 'info');
  }

  showToast(msg: string, type: 'success' | 'warning' | 'info' = 'info'): void {
    this.toastMsg = msg;
    this.toastType = type;
    setTimeout(() => {
      if (this.toastMsg === msg) this.toastMsg = null;
    }, 3500);
  }
}
