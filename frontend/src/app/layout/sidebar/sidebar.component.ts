import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FraudService } from '../../core/services/fraud.service';
import { SimulationService } from '../../core/services/simulation.service';
import { AuthService } from '../../core/services/auth.service';
import { map } from 'rxjs';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss'
})
export class SidebarComponent {
  private fraudService = inject(FraudService);
  private simulationService = inject(SimulationService);
  public authService = inject(AuthService);

  // Conteo reactivo de alertas pendientes / nuevas
  alertCount$ = this.fraudService.getRecentAlerts().pipe(
    map(alerts => alerts.filter(a => a.status === 'NUEVA' || a.status === 'EN_REVISION').length)
  );

  // Estado de streaming en vivo
  isLiveStreaming$ = this.simulationService.isLiveStreaming$;

  logout(): void {
    this.authService.logout();
  }
}


