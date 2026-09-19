package com.empresa.fraude.domain.exception;

public class CuentaNoHabilitadaException extends BusinessException {
    public CuentaNoHabilitadaException(String numeroCuenta, String estado) {
        super("CUENTA_NO_HABILITADA", "La cuenta " + numeroCuenta + " se encuentra en estado: " + estado);
    }
}
