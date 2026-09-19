package com.empresa.fraude.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tipo_transaccion")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoTransaccionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_transaccion")
    private Short idTipoTransaccion;

    @Column(name = "codigo_tipo", nullable = false, unique = true, length = 40)
    private String codigoTipo;

    @Column(name = "nombre_tipo", nullable = false, length = 120)
    private String nombreTipo;

    @Column(name = "naturaleza", nullable = false, length = 10)
    private String naturaleza;

    @Column(name = "es_activo", nullable = false)
    private Boolean esActivo;

    @PrePersist
    protected void onCreate() {
        if (esActivo == null) esActivo = true;
    }
}
