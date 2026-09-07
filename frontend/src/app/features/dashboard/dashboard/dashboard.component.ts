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

  showToast(msg: string): void {
    this.toastMsg = msg;
    setTimeout(() => {
      if (this.toastMsg === msg) this.toastMsg = null;
    }, 3500);
  }
}
