import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgbAccordionModule, NgbTooltipModule } from '@ng-bootstrap/ng-bootstrap';
import { FraudMockService, FraudAlert } from '../../../core/services/fraud-mock.service';

@Component({
  selector: 'app-alerts-list',
  standalone: true,
  imports: [CommonModule, NgbAccordionModule, NgbTooltipModule],
  templateUrl: './alerts-list.component.html',
  styleUrl: './alerts-list.component.scss'
})
export class AlertsListComponent implements OnInit {
  alerts: FraudAlert[] = [];

  constructor(private fraudService: FraudMockService) {}

  ngOnInit() {
    this.fraudService.getRecentAlerts().subscribe(a => this.alerts = a);
  }
}
