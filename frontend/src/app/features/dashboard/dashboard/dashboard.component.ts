import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FraudMockService, DashboardMetrics, FraudAlert } from '../../../core/services/fraud-mock.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  metrics?: DashboardMetrics;
  recentAlerts: FraudAlert[] = [];

  constructor(private fraudService: FraudMockService) {}

  ngOnInit() {
    this.fraudService.getDashboardMetrics().subscribe(m => this.metrics = m);
    this.fraudService.getRecentAlerts().subscribe(a => this.recentAlerts = a);
  }
}
