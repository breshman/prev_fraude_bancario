package com.empresa.fraude.domain.model;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Data
@Builder
public class AlertaFraude {
    private UUID id;
    private UUID transaccionId;
    private BigDecimal score;
    private String nivelRiesgo; // BAJO, MEDIO, ALTO, CRITICO
    private String decision; // APROBADA, RETENIDA, BLOQUEADA
    private LocalDateTime fechaCreacion;
    private String estado; // NUEVA, EN_REVISION, CERRADA
    private List<String> motivos;
}
