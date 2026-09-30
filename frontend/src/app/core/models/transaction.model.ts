export interface TransaccionRequest {
  numeroCuentaOrigen: string;
  numeroCuentaDestino?: string;
  monto: number;
  moneda: string;
  canal: string;
  tipoTransaccion?: string;
  codigoPais?: string;
  direccionIp?: string;
  huellaDispositivo?: string;
  latitud?: number;
  longitud?: number;
}

export interface ReglaDisparadaDto {
  codigo: string;
  nombre: string;
  puntos: number;
  evidencia: string;
}

export interface EvaluacionDetalleDto {
  score: number;
  nivelRiesgo: 'BAJO' | 'MEDIO' | 'ALTO' | 'CRITICO';
  decision: 'APROBAR' | 'MONITOREAR' | 'RETENER' | 'BLOQUEAR';
  reglas: ReglaDisparadaDto[];
}

export interface TransaccionResponse {
  codigoTransaccion: string;
  estado: 'APROBADA' | 'RETENIDA' | 'BLOQUEADA';
  evaluacion: EvaluacionDetalleDto;
  correlationId?: string;
  fechaRegistro: string;
  monto?: number;
  moneda?: string;
  canal?: string;
  numeroCuentaOrigen?: string;
}
