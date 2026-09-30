export interface HistorialCaso {
  idHistorial?: number;
  tipoEvento: 'CREACION' | 'EVIDENCIA' | 'COMENTARIO' | 'CIERRE';
  comentario: string;
  fechaRegistro?: string;
  nombreUsuario?: string;
}

export interface CasoFraude {
  idCaso: number;
  codigoCaso: string;
  codigoAlerta: string;
  motivoApertura: string;
  estadoCaso: 'ABIERTO' | 'EN_INVESTIGACION' | 'CERRADO';
  conclusion?: string;
  dictamenFinal?: 'FRAUDE_CONFIRMADO' | 'FALSO_POSITIVO' | 'SIN_EVIDENCIA';
  fechaApertura: string;
  fechaCierre?: string;
  historial?: HistorialCaso[];
}

export interface AbrirCasoRequest {
  codigoAlerta: string;
  motivoApertura: string;
}

export interface CerrarCasoRequest {
  resultado: 'FRAUDE_CONFIRMADO' | 'FALSO_POSITIVO' | 'SIN_EVIDENCIA';
  comentarioCierre: string;
}
