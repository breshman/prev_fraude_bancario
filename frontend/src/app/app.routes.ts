import { Routes } from '@angular/router';
import { MainLayoutComponent } from './layout/main-layout/main-layout.component';
import { DashboardComponent } from './features/dashboard/dashboard/dashboard.component';
import { AlertsListComponent } from './features/alerts/alerts-list/alerts-list.component';
import { SimulationComponent } from './features/simulation/simulation.component';

export const routes: Routes = [
  {
    path: '',
    component: MainLayoutComponent,
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: DashboardComponent },
      { path: 'alerts', component: AlertsListComponent },
      { path: 'simulation', component: SimulationComponent }
    ]
  },
  { path: '**', redirectTo: 'dashboard' }
];
