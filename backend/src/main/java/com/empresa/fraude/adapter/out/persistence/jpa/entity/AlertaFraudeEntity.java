package com.empresa.fraude.adapter.out.persistence.jpa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "alerta_fraude")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaFraudeEntity {
    @Id
    private UUID id;
    private UUID transaccionId;
    private BigDecimal score;
    private String nivelRiesgo;
    private String decision;
    private LocalDateTime fechaCreacion;
    private String estado;
    private String motivos; // Se almacena como texto separado por comas o JSON
}
