import { Routes } from '@angular/router';
import { MainLayoutComponent } from './layout/main-layout/main-layout.component';
import { DashboardComponent } from './features/dashboard/dashboard/dashboard.component';
import { AlertsListComponent } from './features/alerts/alerts-list/alerts-list.component';
import { SimulationComponent } from './features/simulation/simulation.component';
import { LoginComponent } from './features/auth/login/login.component';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent
  },
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: DashboardComponent },
      { path: 'alerts', component: AlertsListComponent },
      { path: 'simulation', component: SimulationComponent }
    ]
  },
  { path: '**', redirectTo: 'dashboard' }
];
