package com.empresa.fraude.domain.model;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class Transaccion {
    private UUID id;
    private UUID clienteId;
    private BigDecimal monto;
    private String moneda;
    private String paisOrigen;
    private String ipOrigen;
    private LocalDateTime fecha;
    private String estado;
}
