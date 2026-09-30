export interface AlertaWebSocketEventDto {
  idAlerta: number;
  codigoAlerta: string;
  tipoAlerta: string;
  nivelPrioridad: 'BAJA' | 'MEDIA' | 'ALTA' | 'CRITICA' | 'BAJO' | 'MEDIO' | 'ALTO' | 'CRITICO';
  estadoAlerta: string;
  descripcion: string;
  fechaGeneracion: string;

  codigoTransaccion?: string;
  montoTransaccion?: number;
  codigoMoneda?: string;
  canal?: string;
  numeroCuenta?: string;
  scoreRiesgo?: number;
  nivelRiesgo?: 'BAJO' | 'MEDIO' | 'ALTO' | 'CRITICO';
  decisionTomada?: 'APROBAR' | 'MONITOREAR' | 'RETENER' | 'BLOQUEAR';
}
