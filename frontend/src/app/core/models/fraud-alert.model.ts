export type NivelPrioridad = 'BAJA' | 'MEDIA' | 'ALTA' | 'CRITICA' | 'BAJO' | 'MEDIO' | 'ALTO' | 'CRITICO';
export type EstadoAlerta = 'GENERADA' | 'EN_REVISION' | 'DESCARTADA' | 'CONFIRMADA' | 'ASIGNADA' | 'CERRADA' | 'APROBADA' | 'RETENIDA' | 'BLOQUEADA' | 'NUEVA' | 'PENDIENTE';

export interface AlertaBackendEntity {
  idAlerta: number;
  codigoAlerta: string;
  tipoAlerta: string;
  nivelPrioridad: NivelPrioridad;
  descripcion: string;
  estadoAlerta: EstadoAlerta;
  fechaGeneracion: string;
  fechaRevision?: string;
  transaccion?: {
    idTransaccion?: number;
    codigoTransaccion?: string;
    monto?: number;
    codigoMoneda?: string;
    canal?: string;
    numeroCuentaOrigen?: string;
  };
  evaluacion?: {
    scoreFraude?: number;
    nivelRiesgo?: string;
    decisionTomada?: string;
  };
}

export interface FraudAlert {
  id: string;
  idAlertaNum?: number;
  transactionId: string;
  customerName: string;
  amount: number;
  riskScore: number;
  riskLevel: 'BAJO' | 'MEDIO' | 'ALTO' | 'CRITICO';
  status: 'NUEVA' | 'EN_REVISION' | 'CERRADA' | 'APROBADA' | 'RETENIDA' | 'BLOQUEADA' | 'PENDIENTE' | 'GENERADA' | 'CONFIRMADA';
  date: Date;
  reasons: string[];
  origin?: 'SISTEMA' | 'SIMULACION';
}

export interface DashboardMetrics {
  totalAlerts: number;
  criticalAlerts: number;
  avgRiskScore: number;
  monitoredTransactions: number;
}
