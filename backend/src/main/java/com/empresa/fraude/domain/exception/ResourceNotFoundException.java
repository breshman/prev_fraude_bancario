package com.empresa.fraude.domain.exception;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String recurso, String id) {
        super("RECURSO_NO_ENCONTRADO", "No se encontró " + recurso + " con identificador: " + id);
    }
}
