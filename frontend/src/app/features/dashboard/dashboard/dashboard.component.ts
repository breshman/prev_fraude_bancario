import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FraudMockService, DashboardMetrics, FraudAlert } from '../../../core/services/fraud-mock.service';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  metrics$!: Observable<DashboardMetrics>;
  recentAlerts$!: Observable<FraudAlert[]>;

  constructor(private fraudService: FraudMockService) {}

  ngOnInit() {
    this.metrics$ = this.fraudService.getDashboardMetrics();
    this.recentAlerts$ = this.fraudService.getRecentAlerts();
  }
}
