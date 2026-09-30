import { Injectable, inject, OnDestroy } from '@angular/core';
import { Observable, Subject, BehaviorSubject } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AlertaWebSocketEventDto } from '../models/websocket-event.model';
import { FraudService } from './fraud.service';
import { FraudAlert } from '../models/fraud-alert.model';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService implements OnDestroy {
  private fraudService = inject(FraudService);

  private socket: WebSocket | null = null;
  private reconnectTimeout: any = null;
  private shouldReconnect = true;

  private alertSubject = new Subject<AlertaWebSocketEventDto>();
  public alert$ = this.alertSubject.asObservable();

  private isConnectedSubject = new BehaviorSubject<boolean>(false);
  public isConnected$ = this.isConnectedSubject.asObservable();

  constructor() {
    this.connect();
  }

  connect(): void {
    if (typeof window === 'undefined' || !window.WebSocket) {
      return;
    }

    try {
      const wsUrl = environment.wsUrl.startsWith('http')
        ? environment.wsUrl.replace(/^http/, 'ws')
        : environment.wsUrl;

      this.socket = new WebSocket(wsUrl);

      this.socket.onopen = () => {
        this.sendStompConnect();
      };

      this.socket.onmessage = (event: MessageEvent) => {
        this.handleStompMessage(event.data);
      };

      this.socket.onclose = () => {
        this.isConnectedSubject.next(false);
        this.scheduleReconnect();
      };

      this.socket.onerror = () => {
        this.isConnectedSubject.next(false);
      };
    } catch {
      this.scheduleReconnect();
    }
  }

  disconnect(): void {
    this.shouldReconnect = false;
    if (this.reconnectTimeout) {
      clearTimeout(this.reconnectTimeout);
      this.reconnectTimeout = null;
    }
    if (this.socket) {
      this.socket.close();
      this.socket = null;
    }
    this.isConnectedSubject.next(false);
  }

  private sendStompConnect(): void {
    if (!this.socket || this.socket.readyState !== WebSocket.OPEN) return;
    const connectFrame = 'CONNECT\naccept-version:1.2,1.1,1.0\nheart-beat:10000,10000\n\n\0';
    this.socket.send(connectFrame);
  }

  private subscribeToAlerts(): void {
    if (!this.socket || this.socket.readyState !== WebSocket.OPEN) return;
    const topic = environment.wsTopic || '/topic/alertas';
    const subFrame = `SUBSCRIBE\nid:sub-0\ndestination:${topic}\n\n\0`;
    this.socket.send(subFrame);
    this.isConnectedSubject.next(true);
  }

  private handleStompMessage(data: string): void {
    if (typeof data !== 'string') return;

    if (data.startsWith('CONNECTED')) {
      this.subscribeToAlerts();
      return;
    }

    if (data.startsWith('MESSAGE')) {
      const splitIdx = data.indexOf('\n\n');
      if (splitIdx !== -1) {
        let body = data.substring(splitIdx + 2);
        // Quitar terminador NULL de STOMP
        if (body.endsWith('\0')) {
          body = body.slice(0, -1);
        }
        try {
          const event: AlertaWebSocketEventDto = JSON.parse(body.trim());
          this.alertSubject.next(event);
          this.forwardToFraudService(event);
        } catch {
          // Frame no JSON o heartbeat
        }
      }
    }
  }

  private forwardToFraudService(event: AlertaWebSocketEventDto): void {
    const riskLevel: FraudAlert['riskLevel'] =
      event.nivelPrioridad === 'CRITICA' || event.nivelRiesgo === 'CRITICO' ? 'CRITICO' :
      (event.nivelPrioridad === 'ALTA' || event.nivelRiesgo === 'ALTO' ? 'ALTO' :
      (event.nivelPrioridad === 'MEDIA' || event.nivelRiesgo === 'MEDIO' ? 'MEDIO' : 'BAJO'));

    const alert: FraudAlert = {
      id: `ALRT-${String(event.idAlerta).padStart(3, '0')}`,
      idAlertaNum: event.idAlerta,
      transactionId: event.codigoTransaccion || `TX-${event.idAlerta}`,
      customerName: 'Cliente Centralizado',
      amount: Number(event.montoTransaccion || 18500),
      riskScore: Math.round(Number(event.scoreRiesgo || 92)),
      riskLevel,
      status: 'NUEVA',
      date: event.fechaGeneracion ? new Date(event.fechaGeneracion) : new Date(),
      reasons: [event.descripcion || event.tipoAlerta || 'Alerta en tiempo real detectada'],
      origin: 'SISTEMA'
    };

    this.fraudService.addAlert(alert);
  }

  private scheduleReconnect(): void {
    if (!this.shouldReconnect) return;
    if (this.reconnectTimeout) clearTimeout(this.reconnectTimeout);
    this.reconnectTimeout = setTimeout(() => {
      this.connect();
    }, 5000);
  }

  ngOnDestroy(): void {
    this.disconnect();
  }
}
