package com.empresa.fraude.domain.model;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class Cliente {
    private UUID id;
    private String nombre;
    private String email;
    private LocalDateTime fechaRegistro;
}
