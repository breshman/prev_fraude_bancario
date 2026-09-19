package com.empresa.fraude.domain.exception;

public class MontoInvalidoException extends BusinessException {
    public MontoInvalidoException(String mensaje) {
        super("VALIDACION_MONTO", mensaje);
    }
}
