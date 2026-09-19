package com.empresa.fraude.domain.exception;

public class CuentaNotFoundException extends BusinessException {
    public CuentaNotFoundException(String numeroCuenta) {
        super("CUENTA_NO_ENCONTRADA", "No se encontró la cuenta bancaria: " + numeroCuenta);
    }
}
