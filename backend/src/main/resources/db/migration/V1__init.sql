CREATE TABLE cliente (
    id UUID PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    fecha_registro TIMESTAMP NOT NULL
);

CREATE TABLE transaccion (
    id UUID PRIMARY KEY,
    cliente_id UUID NOT NULL REFERENCES cliente(id),
    monto DECIMAL(15,2) NOT NULL,
    moneda VARCHAR(3) NOT NULL,
    pais_origen VARCHAR(2) NOT NULL,
    ip_origen VARCHAR(45) NOT NULL,
    fecha TIMESTAMP NOT NULL,
    estado VARCHAR(50) NOT NULL
);

CREATE TABLE alerta_fraude (
    id UUID PRIMARY KEY,
    transaccion_id UUID NOT NULL REFERENCES transaccion(id),
    score DECIMAL(5,2) NOT NULL,
    nivel_riesgo VARCHAR(20) NOT NULL,
    decision VARCHAR(20) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL,
    estado VARCHAR(50) NOT NULL,
    motivos TEXT NOT NULL
);
