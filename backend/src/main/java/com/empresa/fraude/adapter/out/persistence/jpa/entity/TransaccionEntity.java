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
@Table(name = "transaccion")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransaccionEntity {
    @Id
    private UUID id;
    private UUID clienteId;
    private BigDecimal monto;
    private String moneda;
    private String paisOrigen;
    private String ipOrigen;
    private LocalDateTime fecha;
    private String estado;
}
