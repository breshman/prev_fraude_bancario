import {
  Component,
  Input,
  OnChanges,
  AfterViewInit,
  OnDestroy,
  ViewChild,
  ElementRef,
  SimpleChanges
} from '@angular/core';
import { CommonModule } from '@angular/common';
import Chart from 'chart.js/auto';
import { SimulatedTransaction } from '../../../core/services/simulation.service';

@Component({
  selector: 'app-fraud-chart',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './fraud-chart.component.html',
  styleUrl: './fraud-chart.component.scss'
})
export class FraudChartComponent implements OnChanges, AfterViewInit, OnDestroy {
  @Input() transactions: SimulatedTransaction[] = [];
  @Input() threshold: number = 70;
  @Input() title: string = 'Monitor de Picos de Riesgo en Tiempo Real';
  @Input() maxDisplayCount: number = 24;

  @ViewChild('chartCanvas') chartCanvas!: ElementRef<HTMLCanvasElement>;

  private chart: Chart | null = null;

  // Estadísticas del gráfico
  peakCount: number = 0;
  maxScore: number = 0;
  avgDisplayedScore: number = 0;
  displayedCount: number = 0;

  ngAfterViewInit(): void {
    this.createOrUpdateChart();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.chartCanvas) {
      this.createOrUpdateChart();
    }
  }

  ngOnDestroy(): void {
    if (this.chart) {
      this.chart.destroy();
      this.chart = null;
    }
  }

  private createOrUpdateChart(): void {
    if (!this.chartCanvas) return;

    const canvas = this.chartCanvas.nativeElement;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    // Ordenar dataset cronológicamente (más antiguo a la izquierda, más reciente a la derecha)
    const dataset = [...(this.transactions || [])]
      .slice(0, this.maxDisplayCount)
      .reverse();

    this.displayedCount = dataset.length;

    // Calcular estadísticas
    let peaks = 0;
    let maxSc = 0;
    let sumSc = 0;

    dataset.forEach(tx => {
      if (tx.riskScore >= this.threshold) peaks++;
      if (tx.riskScore > maxSc) maxSc = tx.riskScore;
      sumSc += tx.riskScore;
    });

    this.peakCount = peaks;
    this.maxScore = maxSc;
    this.avgDisplayedScore = dataset.length > 0 ? Math.round(sumSc / dataset.length) : 0;

    // Etiquetas X
    const labels = dataset.map(tx => tx.id);
    const scores = dataset.map(tx => tx.riskScore);
    const thresholdData = dataset.map(() => this.threshold);

    // Colores y tamaños de puntos según superación de umbral
    const pointBackgroundColors = dataset.map(tx =>
      tx.riskScore >= this.threshold ? '#dc2626' : tx.riskScore >= 40 ? '#f59e0b' : '#10b981'
    );
    const pointBorderColors = dataset.map(() => '#ffffff');
    const pointRadii = dataset.map(tx => tx.riskScore >= this.threshold ? 8 : 4);
    const pointHoverRadii = dataset.map(tx => tx.riskScore >= this.threshold ? 12 : 7);

    // Crear gradiente de fondo para la curva de riesgo
    const gradient = ctx.createLinearGradient(0, 0, 0, 240);
    gradient.addColorStop(0, 'rgba(239, 68, 68, 0.45)');  // Rojo en zona alta
    gradient.addColorStop(0.5, 'rgba(245, 158, 11, 0.25)'); // Ámbar en zona media
    gradient.addColorStop(1, 'rgba(37, 99, 235, 0.05)');   // Azul en zona baja

    const chartData = {
      labels,
      datasets: [
        {
          type: 'line' as const,
          label: 'Score de Riesgo',
          data: scores,
          borderColor: '#2563eb',
          borderWidth: 3,
          backgroundColor: gradient,
          fill: true,
          tension: 0.35,
          pointBackgroundColor: pointBackgroundColors,
          pointBorderColor: pointBorderColors,
          pointBorderWidth: 2,
          pointRadius: pointRadii,
          pointHoverRadius: pointHoverRadii,
          order: 1
        },
        {
          type: 'line' as const,
          label: `Línea de Umbral (${this.threshold} pts)`,
          data: thresholdData,
          borderColor: '#dc2626',
          borderWidth: 2.5,
          borderDash: [8, 4],
          fill: false,
          pointRadius: 0,
          pointHoverRadius: 0,
          order: 0
        }
      ]
    };

    if (this.chart) {
      // Actualizar datos existentes in-place sin destruir la referencia del dataset
      this.chart.data.labels = labels;

      const dsRisk = this.chart.data.datasets[0] as any;
      if (dsRisk) {
        dsRisk.data = scores;
        dsRisk.pointBackgroundColor = pointBackgroundColors;
        dsRisk.pointBorderColor = pointBorderColors;
        dsRisk.pointRadius = pointRadii;
        dsRisk.pointHoverRadius = pointHoverRadii;
        dsRisk.backgroundColor = gradient;
      }

      const dsThreshold = this.chart.data.datasets[1] as any;
      if (dsThreshold) {
        dsThreshold.data = thresholdData;
        dsThreshold.label = `Línea de Umbral (${this.threshold} pts)`;
      }

      this.chart.options.plugins = this.buildPlugins(dataset);
      // Actualización en tiempo real fluida: evita que la línea caiga a 0 y vuelva a cargar
      this.chart.update('none');
    } else {
      this.chart = new Chart(canvas, {
        type: 'line',
        data: chartData,
        options: {
          responsive: true,
          maintainAspectRatio: false,
          animation: {
            duration: 400 // Animación de entrada solo en la carga inicial
          },
          interaction: {
            mode: 'index',
            intersect: false
          },
          plugins: this.buildPlugins(dataset),
          scales: {
            y: {
              min: 0,
              max: 100,
              grid: {
                color: 'rgba(226, 232, 240, 0.7)'
              },
              ticks: {
                stepSize: 25,
                color: '#64748b',
                font: {
                  weight: 'bold',
                  size: 11
                },
                callback: (val) => `${val} pts`
              }
            },
            x: {
              grid: {
                display: false
              },
              ticks: {
                color: '#64748b',
                font: {
                  size: 11
                },
                maxRotation: 45
              }
            }
          }
        }
      });
    }
  }

  private buildPlugins(dataset: SimulatedTransaction[]): any {
    return {
      legend: {
        display: false
      },
      tooltip: {
        backgroundColor: '#0f172a',
        titleColor: '#ffffff',
        bodyColor: '#e2e8f0',
        padding: 12,
        cornerRadius: 8,
        borderColor: 'rgba(255, 255, 255, 0.1)',
        borderWidth: 1,
        callbacks: {
          title: (items: any[]) => {
            const index = items[0]?.dataIndex;
            const tx = dataset[index];
            return tx ? `${tx.id} - ${tx.customerName}` : '';
          },
          label: (context: any) => {
            if (context.datasetIndex === 1) {
              return `Umbral de Corte: ${this.threshold} pts`;
            }
            const tx = dataset[context.dataIndex];
            if (!tx) return '';
            const statusTag = tx.riskScore >= this.threshold ? '🚨 SUPERA UMBRAL (ALERTA)' : '✓ Dentro de Umbral';
            return [
              `Score: ${tx.riskScore}/100 [${tx.riskLevel}]`,
              `Estado: ${statusTag}`,
              `Monto: $${tx.amount.toFixed(2)} | Canal: ${tx.channel}`,
              `Ubicación: ${tx.country} (${tx.city})`,
              `Reglas: ${tx.reasons?.[0] || 'Comportamiento habitual'}`
            ];
          }
        }
      }
    };
  }
}
