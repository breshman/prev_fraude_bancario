import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FraudMockService } from '../../core/services/fraud-mock.service';
import { SimulationService } from '../../core/services/simulation.service';
import { map } from 'rxjs';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss'
})
export class SidebarComponent {
  private fraudService = inject(FraudMockService);
  private simulationService = inject(SimulationService);

  // Conteo reactivo de alertas pendientes / nuevas
  alertCount$ = this.fraudService.getRecentAlerts().pipe(
    map(alerts => alerts.filter(a => a.status === 'NUEVA' || a.status === 'EN_REVISION').length)
  );

  // Estado de streaming en vivo
  isLiveStreaming$ = this.simulationService.isLiveStreaming$;
}


